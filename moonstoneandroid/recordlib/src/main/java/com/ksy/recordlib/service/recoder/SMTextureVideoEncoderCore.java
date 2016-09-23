package com.ksy.recordlib.service.recoder;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.Camera;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.opengl.EGLContext;
import android.os.Build;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;

import com.ksy.recordlib.service.core.KsyRecordClient;
import com.ksy.recordlib.service.core.KsyRecordClientConfig;
import com.ksy.recordlib.service.core.KsyRecordSender;
import com.ksy.recordlib.service.magicfilter.utils.MagicParams;
import com.ksy.recordlib.service.muxer.FlvTagMuxer;
import com.ksy.recordlib.service.muxer.H264Utils;
import com.ksy.recordlib.service.util.Constants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;

/**
 * Created by huping on 2016/9/1.
 */
public class SMTextureVideoEncoderCore {


//    private MediaMuxer mMuxer;
//    private MediaCodec mEncoder;
//    private MediaCodec.BufferInfo mBufferInfo;
//    private int mTrackIndex;


    /**
     * Bitrate controller module
     */
    private int[] bitrateArray = new int[] {
            Constants.CONFIG_VIDEO_BITRATE_250K,
            Constants.CONFIG_VIDEO_BITRATE_300K,
            Constants.CONFIG_VIDEO_BITRATE_350K,
            Constants.CONFIG_VIDEO_BITRATE_400K,
            Constants.CONFIG_VIDEO_BITRATE_450K,
            Constants.CONFIG_VIDEO_BITRATE_500K,
            Constants.CONFIG_VIDEO_BITRATE_550K,
            Constants.CONFIG_VIDEO_BITRATE_600K,
            Constants.CONFIG_VIDEO_BITRATE_650K,
            Constants.CONFIG_VIDEO_BITRATE_700K,
            Constants.CONFIG_VIDEO_BITRATE_750K,
            Constants.CONFIG_VIDEO_BITRATE_800K,
            Constants.CONFIG_VIDEO_BITRATE_850K,
    };
    private int bitrateIndex = 0;
    private int currentBitrate = 0;

    private KsyRecordSender ksyVideoSender;
    private KsyRecordClientConfig mVideoConfig;

    private boolean mMuxerStarted;
    private Surface mInputSurface;

    private MediaCodec mVideoEncoder;
    private MediaCodecInfo codecInfo;
    private MediaCodec.BufferInfo codecBufferInfo;
    private byte[] videoBuffer;
    private int videoColor;
    private int rotateDegrees = 0;
    private long presentationTimeUs;

    private MediaFormat videoFormat = null;

    private String pps;
    private String sps;

    private FlvTagMuxer muxer;

    private FileOutputStream h264Stream;

    /**
     * Configures encoder and muxer state, and prepares the input Surface.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public SMTextureVideoEncoderCore(int width, int height, int bitRate, File outputFile)
            throws IOException {
    }

    public SMTextureVideoEncoderCore(KsyRecordClientConfig videoConfig, SurfaceView mSurfaceView) {
        this.mVideoConfig = videoConfig;
        this.ksyVideoSender = KsyRecordSender.getRecordInstance();
        this.rotateDegrees = videoConfig.updateRecordOrientation();


//        try {
//            this.h264Stream = new FileOutputStream(new File(MagicParams.videoPath,MagicParams.h264Name));
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
    }

    public void initialize() {
        this.muxer = new FlvTagMuxer(mVideoConfig, null);
        // the pts for video and audio encoder.
        presentationTimeUs = new Date().getTime() * 1000;

        // choose the right vencoder, perfer qcom then google.
        videoColor = chooseVideoEncoder();

        codecBufferInfo = new MediaCodec.BufferInfo();

        // setup the vencoder.
        // @see https://developer.android.com/reference/android/media/MediaCodec.html
//        if (rotateDegrees == 90 || rotateDegrees == 270) {
//            videoFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC,
//                    mVideoConfig.getVideoHeight(), mVideoConfig.getVideoWidth());
//        } else {
//            videoFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC,
//                    mVideoConfig.getVideoWidth(),  mVideoConfig.getVideoHeight());
//        }
        videoFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC,
                    480,  640);

        // Set some properties.  Failing to specify some of these can cause the MediaCodec
        // configure() call to throw an unhelpful exception.
        videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
//        videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, videoColor);

//        videoFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 0);
        videoFormat.setInteger(MediaFormat.KEY_BIT_RATE, mVideoConfig.getVideoBitRate());
        videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, mVideoConfig.getVideoFrameRate());
        videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, mVideoConfig.getVideoGop());
        Log.e(Constants.LOG_TAG, String.format("vencoder %s, color=%d, bitrate=%d, fps=%d, gop=%d, size=%dx%d",
                codecInfo.getName(), videoColor, mVideoConfig.getVideoBitRate(), mVideoConfig.getVideoFrameRate(),
                mVideoConfig.getVideoGop(),  mVideoConfig.getVideoWidth(), mVideoConfig.getVideoHeight()));


        currentBitrate = mVideoConfig.getVideoBitRate();
        for(int i = 0; i < bitrateArray.length; i++) {
            if (mVideoConfig.getVideoBitRate() == bitrateArray[i]) {
                bitrateIndex = i;
            }
        }

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void start() {

        // videoEncoder yuv to 264 es stream.
        // requires sdk level 16+, Android 4.1, 4.1.1, the JELLY_BEAN
        try {
            mVideoEncoder = MediaCodec.createByCodecName(codecInfo.getName());
        } catch (IOException e) {
            Log.e(Constants.LOG_TAG, "create videoEncoder failed.");
            e.printStackTrace();
            return;
        }

        // the following error can be ignored:
        // 1. the storeMetaDataInBuffers error:
        //      [OMX.qcom.video.encoder.avc] storeMetaDataInBuffers (output) failed w/ err -2147483648
        //      @see http://bigflake.com/mediacodec/#q12
        mVideoEncoder.configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mInputSurface = mVideoEncoder.createInputSurface();
        mVideoEncoder.start();
    }


    /**
     * Returns the encoder's input surface.
     */
    public Surface getInputSurface() {
        return mInputSurface;
    }

    /**
     * Releases encoder resources.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void release() {
//        if (VERBOSE) Log.d(TAG, "releasing encoder objects");
        if (mVideoEncoder != null) {
            mVideoEncoder.stop();
            mVideoEncoder.release();
            mVideoEncoder = null;
        }
        if (muxer != null) {
            // TODO: stop() throws an exception if you haven't fed it any data.  Keep track
            //       of frames submitted, and don't call stop() if we haven't written anything.
            muxer.stop();
            muxer.release();
            muxer = null;
        }

        mMuxerStarted = false;
    }

    /**
     * Extracts all pending data from the encoder and forwards it to the muxer.
     * <p>
     * If endOfStream is not set, this returns when there is no more data to drain.  If it
     * is set, we send EOS to the encoder, and then iterate until we see EOS on the output.
     * Calling this with endOfStream set should be done once, right before stopping the muxer.
     * <p>
     * We're just using the muxer to get a .mp4 file (instead of a raw H.264 stream).  We're
     * not recording audio.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void drainEncoder(boolean endOfStream) {
        final int TIMEOUT_USEC = 10000;
        Log.d(Constants.LOG_TAG, "drainEncoder(" + endOfStream + ")");

        if (endOfStream) {
//            if (VERBOSE) Log.d(TAG, "sending EOS to encoder");
//            mEncoder.signalEndOfInputStream();
        }

        ByteBuffer[] encoderOutputBuffers = mVideoEncoder.getOutputBuffers();
        while (true) {
            int encoderStatus = mVideoEncoder.dequeueOutputBuffer(codecBufferInfo, TIMEOUT_USEC);
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                // no output available yet
                if (!endOfStream) {
                    break;      // out of while
                } else {
                    Log.d(Constants.LOG_TAG, "no output available, spinning to await EOS");
                }
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                // not expected for an encoder
                encoderOutputBuffers = mVideoEncoder.getOutputBuffers();
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                // should happen before receiving buffers, and should only happen once
                if (mMuxerStarted) {
                    throw new RuntimeException("format changed twice");
                }
                MediaFormat newFormat = mVideoEncoder.getOutputFormat();
                Log.d(Constants.LOG_TAG, "encoder output format changed: " + newFormat);

                // now that we have the Magic Goodies, start the muxer
//                mTrackIndex = mMuxer.addTrack(newFormat);
//                mMuxer.start();

                mMuxerStarted = true;
            } else if (encoderStatus < 0) {
                Log.w(Constants.LOG_TAG, "unexpected result from encoder.dequeueOutputBuffer: " +
                        encoderStatus);
                // let's ignore it
            } else {
                ByteBuffer encodedData = encoderOutputBuffers[encoderStatus];
                if (encodedData == null) {
                    throw new RuntimeException("encoderOutputBuffer " + encoderStatus +
                            " was null");
                }

                H264Utils.srs_print_bytes(Constants.LOG_TAG, encodedData, 10);
//                for (int i = 0; i < encodedData.limit(); i++) {
//                    try {
//                        byte data = encodedData.get(i);
//                        h264Stream.write(data);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }

                if ((codecBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    // The codec config data was pulled out and fed to the muxer when we got
                    // the INFO_OUTPUT_FORMAT_CHANGED status.  Ignore it.
                    Log.d(Constants.LOG_TAG, "ignoring BUFFER_FLAG_CODEC_CONFIG");
//                    codecBufferInfo.size = 0;
                }

                if (codecBufferInfo.size != 0) {
                    if (!mMuxerStarted) {
                        throw new RuntimeException("muxer hasn't started");
                    }

                    // adjust the ByteBuffer values to match BufferInfo (not needed?)
                    encodedData.position(codecBufferInfo.offset);
                    encodedData.limit(codecBufferInfo.offset + codecBufferInfo.size);

                    onEncodedAnnexbFrame(encodedData, codecBufferInfo);

                    Log.d(Constants.LOG_TAG, "sent " + codecBufferInfo.size + " bytes to muxer, ts=" +
                            codecBufferInfo.presentationTimeUs);

                }

                mVideoEncoder.releaseOutputBuffer(encoderStatus, false);

                if ((codecBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    if (!endOfStream) {
                        Log.w(Constants.LOG_TAG, "reached end of stream unexpectedly");
                    } else {
                        Log.d(Constants.LOG_TAG, "end of stream reached");
                    }
                    break;// out of while
                }
            }
        }
    }


    // when got encoded h264 es stream.
    private void onEncodedAnnexbFrame(ByteBuffer bb, MediaCodec.BufferInfo bi) {
        try {
            muxer.writeSampleData(FlvTagMuxer.VIDEO_TRACK, bb, bi);
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG, "" + e);
            e.printStackTrace();
        }
    }

    // choose the right supported color format. @see below:
    // https://developer.android.com/reference/android/media/MediaCodecInfo.html
    // https://developer.android.com/reference/android/media/MediaCodecInfo.CodecCapabilities.html
    private int chooseVideoEncoder() {
        // choose the encoder "video/avc":
        //      1. select one when type matched.
        //      2. perfer google avc.
        //      3. perfer qcom avc.
        codecInfo = chooseVideoEncoder(null, null);
        //codecInfo = chooseVideoEncoder("google", codecInfo);
        //codecInfo = chooseVideoEncoder("qcom", codecInfo);

        int matchedColorFormat = 0;
        MediaCodecInfo.CodecCapabilities cc = codecInfo.getCapabilitiesForType(mVideoConfig.getVideoCodec());
        for (int i = 0; i < cc.colorFormats.length; i++) {
            int cf = cc.colorFormats[i];
            Log.i(Constants.LOG_TAG, String.format("videoEncoder %s supports color fomart 0x%x(%d)",
                    codecInfo.getName(), cf, cf));

            // choose YUV for h.264, prefer the bigger one.
            // corresponding to the color space transform in onPreviewFrame
            if ((cf >= cc.COLOR_FormatYUV420Planar && cf <= cc.COLOR_FormatYUV420SemiPlanar)) {
                if (cf > matchedColorFormat) {
                    matchedColorFormat = cf;
                }
            }
        }

        for (int i = 0; i < cc.profileLevels.length; i++) {
            MediaCodecInfo.CodecProfileLevel pl = cc.profileLevels[i];
            Log.i(Constants.LOG_TAG, String.format("videoEncoder %s support profile %d, level %d",
                    codecInfo.getName(), pl.profile, pl.level));
        }

        Log.i(Constants.LOG_TAG, String.format("videoEncoder %s choose color format 0x%x(%d)",
                codecInfo.getName(), matchedColorFormat, matchedColorFormat));

        return matchedColorFormat;
    }

    // choose the video encoder by name.
    private MediaCodecInfo chooseVideoEncoder(String name, MediaCodecInfo def) {
        int nbCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < nbCodecs; i++) {
            MediaCodecInfo mci = MediaCodecList.getCodecInfoAt(i);
            if (!mci.isEncoder()) {
                continue;
            }

            String[] types = mci.getSupportedTypes();
            for (int j = 0; j < types.length; j++) {
                if (types[j].equalsIgnoreCase(mVideoConfig.getVideoCodec())) {
                    //Log.i(TAG, String.format("videoEncoder %s types: %s", mci.getName(), types[j]));
                    if (name == null) {
                        return mci;
                    }

                    if (mci.getName().contains(name)) {
                        return mci;
                    }
                }
            }
        }

        return def;
    }
}
