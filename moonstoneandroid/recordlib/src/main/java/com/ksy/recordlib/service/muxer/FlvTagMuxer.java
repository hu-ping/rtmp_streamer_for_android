package com.ksy.recordlib.service.muxer;

import android.media.MediaCodec;
import android.util.Log;

import com.ksy.recordlib.service.core.KSYFlvData;
import com.ksy.recordlib.service.core.KsyMediaSource;
import com.ksy.recordlib.service.core.KsyRecordClient;
import com.ksy.recordlib.service.core.KsyRecordClientConfig;
import com.ksy.recordlib.service.core.KsyRecordSender;
import com.ksy.recordlib.service.util.ByteConvert;
import com.ksy.recordlib.service.util.Constants;
import com.ksy.recordlib.service.util.TimeStampCreator;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by Administrator on 2016/8/9.
 */
public class FlvTagMuxer implements Muxer {
    public static final int VIDEO_TRACK = 100;
    public static final int AUDIO_TRACK = 101;

    private H264Stream avc;
    private byte[] h264_sps;
    private boolean h264_sps_changed;
    private byte[] h264_pps;
    private boolean h264_pps_changed;
    private boolean h264_sps_pps_sent;

    private byte[] aac_specific_config;

    private H264Utils utils;

    byte SEI_ROTATION_0[] = {0x00, 0x00, 0x03, 0x08, 0x00, 0x0A, -128};
    byte SEI_ROTATION_90[] = {0x66, 0x2F, 0x03, 0x08, 0x00, 0x0A, -128};
    byte SEI_ROTATION_180[] = {0x66, 0x2F, 0x03, 0x10, 0x00, 0x0A, -128};
    byte SEI_ROTATION_270[] = {0x66, 0x2F, 0x03, 0x18, 0x00, 0x0A, -128};
    private static final int FRAME_TYPE_SPS = 0;
    private static final int FRAME_TYPE_DATA = 2;
    private static final int FRAME_DEFINE_TYPE_VIDEO = 9;
    private static final int FRAME_DEFINE_HEAD_LENGTH = 11;
    private static final int FRAME_DEFINE_FOOTER_LENGTH = 4;
    private int last_sum = 0;
    private KsyRecordClientConfig mConfig;

    private byte[] flvFrameByteArray;
    private byte[] dataLengthArray;
    private byte[] timestampArray;
    private byte[] allFrameLengthArray;
    private int videoExtraSize = 5;
    private KsyMediaSource.ClockSync sync;
    protected long timeStamp = 0;
    private KsyRecordSender ksyVideoSender;
    private static final int FROM_VIDEO_DATA = 6;
    private Byte kFlag;
    private int nalutype;

    public FlvTagMuxer(KsyRecordClientConfig mConfig, KsyMediaSource.ClockSync sync) {
        this.sync = sync;
        this.mConfig = mConfig;
        this.ksyVideoSender = KsyRecordSender.getRecordInstance();

        utils = new H264Utils();

        avc = new H264Stream();
        h264_sps = new byte[0];
        h264_sps_changed = false;
        h264_pps = new byte[0];
        h264_pps_changed = false;
        h264_sps_pps_sent = false;

        aac_specific_config = null;
    }

    @Override
    public void start() throws IOException {

    }

    @Override
    public void stop() {

    }

    @Override
    public void release() {

    }

    @Override
    public void writeSampleData(int trackIndex, ByteBuffer byteBuf, MediaCodec.BufferInfo bufferInfo) throws Exception {
//        Log.i(Constants.LOG_TAG, String.format("dumps the %s stream %dB, pts=%d",
//                (trackIndex == VIDEO_TRACK) ? "Vdieo" : "Audio", bufferInfo.size, bufferInfo.presentationTimeUs / 1000));

        if (bufferInfo.offset > 0) {
            Log.w(Constants.LOG_TAG, String.format("encoded frame %dB, offset=%d pts=%dms",
                    bufferInfo.size, bufferInfo.offset, bufferInfo.presentationTimeUs / 1000
            ));
        }

        if (VIDEO_TRACK == trackIndex) {
            writeVideoSample(byteBuf, bufferInfo);
        } else {
            writeAudioSample(byteBuf, bufferInfo);
        }
    }

    int encodeCount = 0;
    private void writeVideoSample(ByteBuffer bb, MediaCodec.BufferInfo bi) {
        int pts = (int) (bi.presentationTimeUs / 1000);
        int dts = (int) pts;
        boolean isiFrame = ((bi.flags == MediaCodec.BUFFER_FLAG_KEY_FRAME)
                || (bi.flags == MediaCodec.BUFFER_FLAG_SYNC_FRAME));

        ArrayList<RawFrameBytes> ibps = new ArrayList<RawFrameBytes>();
        int frame_type = CodecVideoAVCFrame.InterFrame;
//        Log.i(Constants.LOG_TAG, String.format("video %d/%d bytes, offset=%d, position=%d, pts=%d",
//                bb.remaining(), bi.size, bi.offset, bb.position(), pts));

        // send each frame.
        while (bb.position() < bi.size) {
            RawFrameBytes frame = null;
            try {
                frame = avc.annexb_demux(bb, bi);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 5bits, 7.3.1 NAL unit syntax,
            // H.264-AVC-ISO_IEC_14496-10.pdf, page 44.
            //  7: SPS, 8: PPS, 5: I Frame, 1: P Frame
            int nal_unit_type = (int) (frame.frame.get(0) & 0x1f);
            if (nal_unit_type == H264NaluType.SPS || nal_unit_type == H264NaluType.PPS) {
                Log.i(Constants.LOG_TAG, String.format("annexb demux %dB, pts=%d, frame=%dB, nalu=%d",
                        bi.size, pts, frame.size, nal_unit_type));
            }

            // for IDR frame, the frame is keyframe.
            if (nal_unit_type == H264NaluType.IDR) {
                frame_type = CodecVideoAVCFrame.KeyFrame;
            }

            // ignore the nalu type aud(9)
            if (nal_unit_type == H264NaluType.AccessUnitDelimiter) {
                continue;
            }

            Log.i(Constants.LOG_TAG, "isiFrame:" + isiFrame);
            encodeCount++;
            if (bi.flags != 0) {
                Log.e(Constants.LOG_TAG, "flags:" + bi.flags
                        + ", nal_unit_type:" + nal_unit_type
                        + ", count:" + encodeCount);
                encodeCount = 0;
            }

            // for sps
            if (avc.is_sps(frame)) {
                byte[] sps = new byte[frame.size];
                frame.frame.get(sps);

                if (utils.srs_bytes_equals(h264_sps, sps)) {
                    continue;
                }
                h264_sps_changed = true;
                h264_sps = sps;
                continue;
            }

            // for pps
            if (avc.is_pps(frame)) {
                byte[] pps = new byte[frame.size];
                frame.frame.get(pps);

                if (utils.srs_bytes_equals(h264_pps, pps)) {
                    continue;
                }
                h264_pps_changed = true;
                h264_pps = pps;
                continue;
            }

            // ibp frame.
//            RawFrameBytes nalu_header = avc.mux_ibp_frame(frame);
//            ibps.add(nalu_header);
            ibps.add(frame);
        }

        write_h264_sps_pps(dts, pts);

        write_h264_ipb_frame(ibps, frame_type, dts, pts, isiFrame);
    }


    private void write_h264_ipb_frame(ArrayList<RawFrameBytes> ibps, int frame_type, int dts, int pts, boolean isiFrame) {
        // when sps or pps not sent, ignore the packet.
        // @see https://github.com/simple-rtmp-server/srs/issues/203
        if (!h264_sps_pps_sent) {
            return;
        }



        if (frame_type == CodecVideoAVCFrame.KeyFrame) {
            //Log.i(TAG, String.format("flv: keyframe %dB, dts=%d", flv_tag.size, dts));
            Log.e(Constants.LOG_TAG, String.format("flv: keyframe, dts=%d", dts));
        }

        // h.264 raw data.
        for (int i = 0; i < ibps.size(); i++) {
            RawFrameBytes frame = ibps.get(i);

            if(frame.size > 0) {
                byte[] data = new byte[frame.size];
                frame.frame.get(data);
                kFlag = data[0];
                nalutype = kFlag & 0x1F;

                // Three types of flv video frame
                writeFlvFrame(FRAME_TYPE_DATA, data, frame.size, frame_type, isiFrame);
            }
        }
    }

    private void write_h264_sps_pps(int dts, int pts) {
        // when sps or pps changed, update the sequence header,
        // for the pps maybe not changed while sps changed.
        // so, we must check when each video timeStamp message frame parsed.
        if (h264_sps_pps_sent && !h264_sps_changed && !h264_pps_changed) {
            return;
        }

        // Step One ,insert in header,sps & pps prefix & data
        byte[] sps_prefix = ByteConvert.hexStringToBytes("0142C028FFE1");
        byte[] sps_only = h264_sps;
        byte[] sps_length = ByteConvert.intToByteArrayTwoByte(sps_only.length);
        byte[] pps_prefix = ByteConvert.hexStringToBytes("01");
        byte[] pps_only = h264_pps;
        byte[] pps_length = ByteConvert.intToByteArrayTwoByte(pps_only.length);

        byte[] sps_pps = new byte[sps_prefix.length + sps_length.length + sps_only.length
                + pps_prefix.length + pps_only.length + pps_length.length];
        fillArray(sps_pps, sps_prefix);
        fillArray(sps_pps, sps_length);
        fillArray(sps_pps, sps_only);
        fillArray(sps_pps, pps_prefix);
        fillArray(sps_pps, pps_length);
        fillArray(sps_pps, pps_only);

        writeFlvFrame(FRAME_TYPE_SPS, sps_pps, sps_pps.length, CodecVideoAVCFrame.KeyFrame, true);

        // reset sps and pps.
        h264_sps_changed = false;
        h264_pps_changed = false;
        h264_sps_pps_sent = true;
    }



    private void writeFlvFrame(int type, byte[] frame, int length, int frame_type, boolean isiFrame) {
//        timeStamp = sync.getTime();
        timeStamp = TimeStampCreator.getTimeStampCreator().createAbsoluteTimestamp();

        videoExtraSize = 5;
        int frameTotalLength;
        int degree = mConfig.getRecordOrientation();
        if (type == FRAME_TYPE_SPS) {
//            timeStamp = 0;
            frameTotalLength = FRAME_DEFINE_HEAD_LENGTH + length + videoExtraSize + FRAME_DEFINE_FOOTER_LENGTH;
            dataLengthArray = ByteConvert.intToByteArray(length + videoExtraSize);
        } else if (degree == 0) {
            frameTotalLength = FRAME_DEFINE_HEAD_LENGTH + length + videoExtraSize + 4 + FRAME_DEFINE_FOOTER_LENGTH;
            dataLengthArray = ByteConvert.intToByteArray(length + videoExtraSize + 4);
        } else {
            frameTotalLength = FRAME_DEFINE_HEAD_LENGTH + length + videoExtraSize + 11 + 4 + FRAME_DEFINE_FOOTER_LENGTH;
            dataLengthArray = ByteConvert.intToByteArray(length + videoExtraSize + 11 + 4);
        }
        flvFrameByteArray = new byte[frameTotalLength];
        flvFrameByteArray[0] = (byte) FRAME_DEFINE_TYPE_VIDEO;
        flvFrameByteArray[1] = dataLengthArray[0];
        flvFrameByteArray[2] = dataLengthArray[1];
        flvFrameByteArray[3] = dataLengthArray[2];
        timestampArray = ByteConvert.longToByteArray(timeStamp);
        flvFrameByteArray[4] = timestampArray[1];
        flvFrameByteArray[5] = timestampArray[2];
        flvFrameByteArray[6] = timestampArray[3];
        flvFrameByteArray[7] = timestampArray[0];
        flvFrameByteArray[8] = (byte) 0;
        flvFrameByteArray[9] = (byte) 0;
        flvFrameByteArray[10] = (byte) 0;
        // added 5 extra bytes
        for (int i = 0; i < videoExtraSize; i++) {
            if (i == 0) {
                //1 byte flag
//                flvFrameByteArray[FRAME_DEFINE_HEAD_LENGTH + i] = (byte) 23;
                flvFrameByteArray[FRAME_DEFINE_HEAD_LENGTH + i] = (byte) ((frame_type << 4) | 7);

            } else if (i == 1) {
                if (type == FRAME_TYPE_SPS) {
                    flvFrameByteArray[FRAME_DEFINE_HEAD_LENGTH + i] = (byte) 0;
                } else {
                    flvFrameByteArray[FRAME_DEFINE_HEAD_LENGTH + i] = (byte) 1;
                }
            } else if (i < 5) {
                flvFrameByteArray[FRAME_DEFINE_HEAD_LENGTH + i] = (byte) 0;
            }
        }
        // Add Sei Content and Replace Data content here
//        int pos = 0;
        int pos = FRAME_DEFINE_HEAD_LENGTH + videoExtraSize;
        if (type != FRAME_TYPE_SPS) {
            if (degree != 0) {
                // copy sei content
                int sei_length = 7;
                byte[] sei_content = SEI_ROTATION_0;
                byte[] sei_length_array = ByteConvert.intToByteArrayFull(sei_length);
                System.arraycopy(sei_length_array, 0, flvFrameByteArray, pos, 4);
                pos += 4;
                if (degree == 90) {
                    sei_content = SEI_ROTATION_90;
                } else if (degree == 180) {
                    sei_content = SEI_ROTATION_180;
                } else if (degree == 270) {
                    sei_content = SEI_ROTATION_270;
                }
                System.arraycopy(sei_content, 0, flvFrameByteArray, pos, 7);
                pos += 7;

            }
            byte[] real_data_length_array = ByteConvert.intToByteArrayFull(length);
            System.arraycopy(real_data_length_array, 0, flvFrameByteArray, pos, real_data_length_array.length);
            pos += 4;
        } else {
            KsyRecordClient.startWaitTIme = System.currentTimeMillis() - KsyRecordClient.startTime;
        }
        //copy real frame  data

        System.arraycopy(frame, 0, flvFrameByteArray, pos, length);
        pos += length;

        allFrameLengthArray = ByteConvert.intToByteArrayFull(pos + FRAME_DEFINE_FOOTER_LENGTH);
        System.arraycopy(allFrameLengthArray, 0, flvFrameByteArray, pos, allFrameLengthArray.length);

        //添加视频数据到队列
        KSYFlvData ksyVideo = new KSYFlvData();
        ksyVideo.currentTimeMs = System.currentTimeMillis();

        ksyVideo.byteBuffer = flvFrameByteArray;
        ksyVideo.size = flvFrameByteArray.length;
        ksyVideo.dts = (int) timeStamp;
        ksyVideo.type = 11;
        if (type == FRAME_TYPE_SPS) {
            ksyVideo.frameType = KSYFlvData.NALU_TYPE_IDR;
            ksyVideo.iFrameFlag = true;
        } else {
            ksyVideo.frameType = nalutype;
            ksyVideo.iFrameFlag = isiFrame;
        }
        ksyVideoSender.addToQueue(ksyVideo, FROM_VIDEO_DATA);
    }

    private void fillArray(byte[] sps_pps, byte[] target) {
        for (int i = 0; i < target.length; i++) {
            sps_pps[last_sum + i] = target[i];
        }
        last_sum += target.length;
    }

    private void writeAudioSample(ByteBuffer bb, MediaCodec.BufferInfo bi) {

    }

}
