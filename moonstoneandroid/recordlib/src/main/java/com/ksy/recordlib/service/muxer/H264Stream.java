package com.ksy.recordlib.service.muxer;

import android.media.MediaCodec;
import android.util.Log;

import com.ksy.recordlib.service.util.Constants;

import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * the raw h.264 stream, in annexb.
 */
public class H264Stream {
    private H264Utils utils;


    public H264Stream() {
        utils = new H264Utils();
    }

    public boolean is_sps(RawFrameBytes frame) {
        if (frame.size < 1) {
            return false;
        }

        // 5bits, 7.3.1 NAL unit syntax,
        // H.264-AVC-ISO_IEC_14496-10.pdf, page 44.
        //  7: SPS, 8: PPS, 5: I Frame, 1: P Frame
        int nal_unit_type = (int) (frame.frame.get(0) & 0x1f);

        return nal_unit_type == H264NaluType.SPS;
    }

    public boolean is_pps(RawFrameBytes frame) {
        if (frame.size < 1) {
            return false;
        }

        // 5bits, 7.3.1 NAL unit syntax,
        // H.264-AVC-ISO_IEC_14496-10.pdf, page 44.
        //  7: SPS, 8: PPS, 5: I Frame, 1: P Frame
        int nal_unit_type = (int) (frame.frame.get(0) & 0x1f);

        return nal_unit_type == H264NaluType.PPS;
    }

    public RawFrameBytes mux_ibp_frame(RawFrameBytes frame) {
        RawFrameBytes nalu_header = new RawFrameBytes();
        nalu_header.size = 4;
        nalu_header.frame = ByteBuffer.allocate(nalu_header.size);

        // 5.3.4.2.1 Syntax, H.264-AVC-ISO_IEC_14496-15.pdf, page 16
        // lengthSizeMinusOne, or NAL_unit_length, always use 4bytes size
        int NAL_unit_length = frame.size;

        // mux the avc NALU in "ISO Base Media File Format"
        // from H.264-AVC-ISO_IEC_14496-15.pdf, page 20
        // NALUnitLength
        nalu_header.frame.putInt(NAL_unit_length);

        // reset the buffer.
        nalu_header.frame.rewind();

        //Log.i(TAG, String.format("mux ibp frame %dB", frame.size));
        //SrsHttpFlv.srs_print_bytes(TAG, nalu_header.frame, 16);

        return nalu_header;
    }

    public void mux_sequence_header(byte[] sps, byte[] pps, int dts, int pts, ArrayList<RawFrameBytes> frames) {
        // 5bytes sps/pps header:
        //      configurationVersion, AVCProfileIndication, profile_compatibility,
        //      AVCLevelIndication, lengthSizeMinusOne
        // 3bytes size of sps:
        //      numOfSequenceParameterSets, sequenceParameterSetLength(2B)
        // Nbytes of sps.
        //      sequenceParameterSetNALUnit
        // 3bytes size of pps:
        //      numOfPictureParameterSets, pictureParameterSetLength
        // Nbytes of pps:
        //      pictureParameterSetNALUnit

        // decode the SPS:
        // @see: 7.3.2.1.1, H.264-AVC-ISO_IEC_14496-10-2012.pdf, page 62
        if (true) {
            RawFrameBytes hdr = new RawFrameBytes();
            hdr.size = 5;
            hdr.frame = ByteBuffer.allocate(hdr.size);

            // @see: Annex A Profiles and levels, H.264-AVC-ISO_IEC_14496-10.pdf, page 205
            //      Baseline profile profile_idc is 66(0x42).
            //      Main profile profile_idc is 77(0x4d).
            //      Extended profile profile_idc is 88(0x58).
            byte profile_idc = sps[1];
            //u_int8_t constraint_set = frame[2];
            byte level_idc = sps[3];

            // generate the sps/pps header
            // 5.3.4.2.1 Syntax, H.264-AVC-ISO_IEC_14496-15.pdf, page 16
            // configurationVersion
            hdr.frame.put((byte) 0x01);
            // AVCProfileIndication
            hdr.frame.put(profile_idc);
            // profile_compatibility
            hdr.frame.put((byte) 0x00);
            // AVCLevelIndication
            hdr.frame.put(level_idc);
            // lengthSizeMinusOne, or NAL_unit_length, always use 4bytes size,
            // so we always set it to 0x03.
            hdr.frame.put((byte) 0x03);

            // reset the buffer.
            hdr.frame.rewind();
            frames.add(hdr);
        }

        // sps
        if (true) {
            RawFrameBytes sps_hdr = new RawFrameBytes();
            sps_hdr.size = 3;
            sps_hdr.frame = ByteBuffer.allocate(sps_hdr.size);

            // 5.3.4.2.1 Syntax, H.264-AVC-ISO_IEC_14496-15.pdf, page 16
            // numOfSequenceParameterSets, always 1
            sps_hdr.frame.put((byte) 0x01);
            // sequenceParameterSetLength
            sps_hdr.frame.putShort((short) sps.length);

            sps_hdr.frame.rewind();
            frames.add(sps_hdr);

            // sequenceParameterSetNALUnit
            RawFrameBytes sps_bb = new RawFrameBytes();
            sps_bb.size = sps.length;
            sps_bb.frame = ByteBuffer.wrap(sps);
            frames.add(sps_bb);
        }

        // pps
        if (true) {
            RawFrameBytes pps_hdr = new RawFrameBytes();
            pps_hdr.size = 3;
            pps_hdr.frame = ByteBuffer.allocate(pps_hdr.size);

            // 5.3.4.2.1 Syntax, H.264-AVC-ISO_IEC_14496-15.pdf, page 16
            // numOfPictureParameterSets, always 1
            pps_hdr.frame.put((byte) 0x01);
            // pictureParameterSetLength
            pps_hdr.frame.putShort((short) pps.length);

            pps_hdr.frame.rewind();
            frames.add(pps_hdr);

            // pictureParameterSetNALUnit
            RawFrameBytes pps_bb = new RawFrameBytes();
            pps_bb.size = pps.length;
            pps_bb.frame = ByteBuffer.wrap(pps);
            frames.add(pps_bb);
        }
    }

    public RawFrameBytes mux_avc2flv(ArrayList<RawFrameBytes> frames, int frame_type, int avc_packet_type, int dts, int pts) {
        RawFrameBytes flv_tag = new RawFrameBytes();

        // for h264 in RTMP video payload, there is 5bytes header:
        //      1bytes, FrameType | CodecID
        //      1bytes, AVCPacketType
        //      3bytes, CompositionTime, the cts.
        // @see: E.4.3 Video Tags, video_file_format_spec_v10_1.pdf, page 78
        flv_tag.size = 5;
        for (int i = 0; i < frames.size(); i++) {
            RawFrameBytes frame = frames.get(i);
            flv_tag.size += frame.size;
        }

        flv_tag.frame = ByteBuffer.allocate(flv_tag.size);

        // @see: E.4.3 Video Tags, video_file_format_spec_v10_1.pdf, page 78
        // Frame Type, Type of video frame.
        // CodecID, Codec Identifier.
        // set the rtmp header
        flv_tag.frame.put((byte) ((frame_type << 4) | SrsCodecVideo.AVC));

        // AVCPacketType
        flv_tag.frame.put((byte) avc_packet_type);

        // CompositionTime
        // pts = dts + cts, or
        // cts = pts - dts.
        // where cts is the header in rtmp video packet payload header.
        int cts = pts - dts;
        flv_tag.frame.put((byte) (cts >> 16));
        flv_tag.frame.put((byte) (cts >> 8));
        flv_tag.frame.put((byte) cts);

        // h.264 raw data.
        for (int i = 0; i < frames.size(); i++) {
            RawFrameBytes frame = frames.get(i);
            byte[] frame_bytes = new byte[frame.size];
            frame.frame.get(frame_bytes);
            flv_tag.frame.put(frame_bytes);
        }

        // reset the buffer.
        flv_tag.frame.rewind();

        //Log.i(TAG, String.format("flv tag muxed, %dB", flv_tag.size));
        //SrsHttpFlv.srs_print_bytes(TAG, flv_tag.frame, 128);

        return flv_tag;
    }

    /**
     * The data of MediaCodec's outBuffer may be more than one frame.
     */
    public RawFrameBytes annexb_demux(ByteBuffer bb, MediaCodec.BufferInfo bi) throws Exception {
        RawFrameBytes rawFrameBytes = new RawFrameBytes();

        while (bb.position() < bi.size) {
            // each frame must prefixed by annexb format.
            // about annexb, @see H.264-AVC-ISO_IEC_14496-10.pdf, page 211.
            Log.i(Constants.LOG_TAG, "start position:" + bb.position() + " size:" + bi.size);
            // TODO: 2016/8/22 bb.position()会出现等于25的情况。
            // 虽然打印的内容会包含00 00 00 01的头，因为打印是从bb.get(i) i=0开始的，
            // 但是函数utils.srs_avc_startswith_annexb检测不出来，
            // 因为它是从position为25的位置开始的。
            H264AnnexbSearch h264AnnexbSearch = utils.srs_avc_startswith_annexb(bb, bi);
            if (!h264AnnexbSearch.match || h264AnnexbSearch.nb_start_code < 3) {
                Log.e(Constants.LOG_TAG, "annexb not match."
                        + " match:" + h264AnnexbSearch.match
                        + " code:" + h264AnnexbSearch.nb_start_code
                        + " pos:" + bb.position()
                        + " size:" + bi.size);

                H264Utils.srs_print_bytes(Constants.LOG_TAG, bb, 50);
                throw new Exception(String.format("annexb not match for %dB, pos=%d", bi.size, bb.position()));
            }

            // the start codes.
            ByteBuffer tbbs = bb.slice();
            for (int i = 0; i < h264AnnexbSearch.nb_start_code; i++) {
                bb.get();
            }

            // find out the frame size.
            rawFrameBytes.frame = bb.slice();
            int pos = bb.position();
            while (bb.position() < bi.size) {
                H264AnnexbSearch bsc = utils.srs_avc_startswith_annexb(bb, bi);
                if (bsc.match) {
                    break;
                }
                // Returns the byte at the current position and increases the position by 1.
                bb.get();
            }

            rawFrameBytes.size = bb.position() - pos;
//            if (bb.position() < bi.size) {
                Log.i(Constants.LOG_TAG, "position:" + bb.position() + " size:" + bi.size);
                Log.i(Constants.LOG_TAG, String.format("annexb multiple match ok, pts=%d", bi.presentationTimeUs / 1000));
                H264Utils.srs_print_bytes(Constants.LOG_TAG, tbbs, 16);
                H264Utils.srs_print_bytes(Constants.LOG_TAG, bb.slice(), 16);
//            }
            //Log.i(TAG, String.format("annexb match %d bytes", tbb.size));
            break;
        }

        return rawFrameBytes;
    }

    // E.4.3.1 VIDEODATA
    // CodecID UB [4]
    // Codec Identifier. The following values are defined:
    //     2 = Sorenson H.263
    //     3 = Screen video
    //     4 = On2 VP6
    //     5 = On2 VP6 with alpha channel
    //     6 = Screen video version 2
    //     7 = AVC
    class SrsCodecVideo {
        // set to the zero to reserved, for array map.
        public final static int Reserved = 0;
        public final static int Reserved1 = 1;
        public final static int Reserved2 = 9;

        // for user to disable video, for example, use pure audio hls.
        public final static int Disabled = 8;

        public final static int SorensonH263 = 2;
        public final static int ScreenVideo = 3;
        public final static int On2VP6 = 4;
        public final static int On2VP6WithAlphaChannel = 5;
        public final static int ScreenVideoVersion2 = 6;
        public final static int AVC = 7;
    }
}