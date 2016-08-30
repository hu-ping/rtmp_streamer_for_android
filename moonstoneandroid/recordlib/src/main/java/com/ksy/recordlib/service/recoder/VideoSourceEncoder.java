package com.ksy.recordlib.service.recoder;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.Camera;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;

import com.ksy.recordlib.service.core.KsyMediaSource;
import com.ksy.recordlib.service.core.KsyRecordClient;
import com.ksy.recordlib.service.core.KsyRecordClientConfig;
import com.ksy.recordlib.service.core.KsyRecordSender;
import com.ksy.recordlib.service.core.ThroughputStatistic;
import com.ksy.recordlib.service.muxer.FlvTagMuxer;
import com.ksy.recordlib.service.util.Constants;
import com.ksy.recordlib.service.util.YunManager;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;

/**
 * Created by Administrator on 2016/8/9.
 */
public class VideoSourceEncoder extends KsyMediaSource{

    private final KsyRecordClient.RecordHandler mHandler;
    private final Context mContext;
    private Camera mCamera;
    private KsyRecordSender ksyVideoSender;
    private KsyRecordClientConfig mConfig;

    /**
     * MediaCodec module
     */
    private MediaCodec videoEncoder;
    private MediaCodecInfo codecInfo;
    private MediaCodec.BufferInfo codecBufferInfo;
    private byte[] videoBuffer;
    private int videoColor;
    private int rotateDegrees = 0;
    private long presentationTimeUs;

    private String pps;
    private String sps;

    private FlvTagMuxer muxer;

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

    public VideoSourceEncoder(Camera mCamera, KsyRecordClientConfig mConfig, SurfaceView mSurfaceView,
                              KsyRecordClient.RecordHandler mRecordHandler, Context mContext) {
        this.mCamera = mCamera;
        this.mConfig = mConfig;
        this.mHandler = mRecordHandler;
        this.mContext = mContext;
        this.ksyVideoSender = KsyRecordSender.getRecordInstance();
        this.rotateDegrees = mConfig.updateRecordOrientation();
        this.muxer = new FlvTagMuxer(mConfig, sync);
    }


    @Override
    public void prepare() {
        // the pts for video and audio encoder.
        presentationTimeUs = new Date().getTime() * 1000;

        // choose the right vencoder, perfer qcom then google.
        videoColor = chooseVideoEncoder();
        // videoEncoder yuv to 264 es stream.
        // requires sdk level 16+, Android 4.1, 4.1.1, the JELLY_BEAN
        try {
            videoEncoder = MediaCodec.createByCodecName(codecInfo.getName());
        } catch (IOException e) {
            Log.e(Constants.LOG_TAG, "create videoEncoder failed.");
            e.printStackTrace();
            return;
        }
        codecBufferInfo = new MediaCodec.BufferInfo();

        // setup the vencoder.
        // @see https://developer.android.com/reference/android/media/MediaCodec.html
        MediaFormat videoFormat = null;
        if (rotateDegrees == 90 || rotateDegrees == 270) {
            videoFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC,
                    mConfig.getVideoHeight(), mConfig.getVideoWidth());
        } else {
            videoFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC,
                    mConfig.getVideoWidth(),  mConfig.getVideoHeight());
        }

        videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, videoColor);
        videoFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 0);
        videoFormat.setInteger(MediaFormat.KEY_BIT_RATE, mConfig.getVideoBitRate());
        videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, mConfig.getVideoFrameRate());
        videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, mConfig.getVideoGop());
        Log.i(Constants.LOG_TAG, String.format("vencoder %s, color=%d, bitrate=%d, fps=%d, gop=%d, size=%dx%d",
                codecInfo.getName(), videoColor, mConfig.getVideoBitRate(), mConfig.getVideoFrameRate(),
                mConfig.getVideoGop(),  mConfig.getVideoWidth(), mConfig.getVideoHeight()));
        // the following error can be ignored:
        // 1. the storeMetaDataInBuffers error:
        //      [OMX.qcom.video.encoder.avc] storeMetaDataInBuffers (output) failed w/ err -2147483648
        //      @see http://bigflake.com/mediacodec/#q12
        videoEncoder.configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

        mHandler.sendEmptyMessage(Constants.MESSAGE_SWITCH_CAMERA_FINISH);

        currentBitrate = mConfig.getVideoBitRate();
        for(int i = 0; i < bitrateArray.length; i++) {
            if (mConfig.getVideoBitRate() == bitrateArray[i]) {
                bitrateIndex = i;
            }
        }
    }

    @Override
    public void start() {
        // prepare video encoder.
        prepare();

        // set the callback and start the preview.
        videoBuffer = new byte[YunManager.getYuvBuffer(mConfig.getVideoWidth(), mConfig.getVideoHeight())];
        Log.i(Constants.LOG_TAG, "getYuvBuffer(size.width, size.height) == "
                + YunManager.getYuvBuffer(mConfig.getVideoWidth(), mConfig.getVideoHeight()));

        mCamera.addCallbackBuffer(videoBuffer);

        mCamera.setPreviewCallbackWithBuffer((Camera.PreviewCallback) fetchVideoFromDevice());

        // start device and encoder.
        Log.i(Constants.LOG_TAG, "start avc vencoder");
        videoEncoder.start();

        mCamera.startPreview();

    }


    private Object fetchVideoFromDevice() {
        return new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                adjustEncoder();

                // color space transform.
                byte[] frame = new byte[data.length];
                YunManager.rotateYV12(data, frame, mConfig.getVideoWidth(), mConfig.getVideoHeight(),
                        rotateDegrees, mConfig.getCameraType());

                if (videoColor == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar) {
                    YunManager.YV12toYUV420Planar(frame, data, mConfig.getVideoWidth(), mConfig.getVideoHeight());
                } else if (videoColor == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar) {
                    YunManager.YV12toYUV420PackedSemiPlanar(frame, data, mConfig.getVideoWidth(), mConfig.getVideoHeight());
                } else if (videoColor == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar) {
                    YunManager.YV12toYUV420PackedSemiPlanar(frame, data, mConfig.getVideoWidth(), mConfig.getVideoHeight());
                } else {
                    try {
                        onGetYuvFrame(frame);
                    } catch (Exception e) {
                        Log.e(Constants.LOG_TAG, String.format("consume yuv frame failed. e=%s", e.toString()));
                        e.printStackTrace();
                        throw e;
                    }

                    // to fetch next frame.
                    camera.addCallbackBuffer(videoBuffer);
                    return;
                }

                // feed the frame to vencoder and muxer.
                try {
                    onGetYuvFrame(data);
                } catch (Exception e) {
                    Log.e(Constants.LOG_TAG, String.format("consume yuv frame failed. e=%s", e.toString()));
                    e.printStackTrace();
                    throw e;
                }

                // to fetch next frame.
                camera.addCallbackBuffer(videoBuffer);
            }
        };
    }

    private void onGetYuvFrame(byte[] data) {
        //Log.i(TAG, String.format("got YUV image, size=%d", data.length));

        // feed the vencoder with yuv frame, got the encoded 264 es stream.
        ByteBuffer[] inBuffers = videoEncoder.getInputBuffers();
        ByteBuffer[] outBuffers = videoEncoder.getOutputBuffers();

        if (true) {
            int inBufferIndex = videoEncoder.dequeueInputBuffer(-1);
            //Log.i(TAG, String.format("try to dequeue input vbuffer, ii=%d", inBufferIndex));
            if (inBufferIndex >= 0) {
                ByteBuffer bb = inBuffers[inBufferIndex];
                bb.clear();
                bb.put(data, 0, data.length);
                long pts = new Date().getTime() * 1000 - presentationTimeUs;
                //Log.i(TAG, String.format("feed YUV to encode %dB, pts=%d", data.length, pts / 1000));
                videoEncoder.queueInputBuffer(inBufferIndex, 0, data.length, pts, 0);
            }
        }

        for (; ; ) {
            int outBufferIndex = videoEncoder.dequeueOutputBuffer(codecBufferInfo, 0);
            //Log.i(TAG, String.format("try to dequeue output vbuffer, ii=%d, oi=%d", inBufferIndex, outBufferIndex));
            if (outBufferIndex >= 0) {
                ByteBuffer bb = outBuffers[outBufferIndex];
                onEncodedAnnexbFrame(bb, codecBufferInfo);
                videoEncoder.releaseOutputBuffer(outBufferIndex, false);
            }

            if (outBufferIndex < 0) {
                break;
            }
        }
    }

    private void adjustEncoder() {
        ThroughputStatistic statistic = ksyVideoSender.monitorThroughput();
        if (statistic.isCompleteMonitor) {
            int weight = statistic.weight;
            int configBitrateIndex = clacBitrateIndex(mConfig.getVideoBitRate());

            if (weight != ThroughputStatistic.WEIGHT_LEVEL_0) {
                if (weight > ThroughputStatistic.WEIGHT_LEVEL_0 && bitrateIndex == configBitrateIndex) {
                    return;
                }

                if (weight == ThroughputStatistic.WEIGHT_LEVEL_1) {
                    if (currentBitrate >= Constants.CONFIG_VIDEO_BITRATE_600K) {
                        currentBitrate += 20*1000;

                        if (bitrateIndex > configBitrateIndex) {
                            bitrateIndex = configBitrateIndex;
                            currentBitrate = bitrateArray[bitrateIndex];
                        }
                        resetRates(currentBitrate);
                        return;
                    }
                }

                if (weight == ThroughputStatistic.WEIGHT_LEVEL_MINIMUM) {
                    if(currentBitrate == bitrateArray[0]) {
                        ksyVideoSender.flushQueue();
                    }
                    bitrateIndex += ThroughputStatistic.WEIGHT_LEVEL_NEGATIVE_7;
                } else {
                    bitrateIndex += weight;
                }

                if (bitrateIndex < 0) {
                    bitrateIndex = 0;
                } else if (bitrateIndex > configBitrateIndex) {
                    bitrateIndex = configBitrateIndex;
                }

                if (currentBitrate != bitrateArray[bitrateIndex]) {
                    currentBitrate = bitrateArray[bitrateIndex];
                    resetRates(currentBitrate);
                }
            }
        }
    }


    private int clacBitrateIndex(int kbps) {
        int index = 0;

        for (int i = 0; i < bitrateArray.length; i++) {
            if (bitrateArray[i] == kbps) {
                index = i;
                break;
            }
        }

        return index;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private boolean resetRates(int kbps) {
        Log.i(Constants.LOG_TAG, "reset bitrate:" + kbps);
        try {
            Bundle params = new Bundle();
            params.putInt(MediaCodec.PARAMETER_KEY_VIDEO_BITRATE, kbps);
            videoEncoder.setParameters(params);
            return true;
        } catch (IllegalStateException e) {
            e.printStackTrace();
            Log.e(Constants.LOG_TAG, "setRates failed dynamically.", e);
            return false;
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
        MediaCodecInfo.CodecCapabilities cc = codecInfo.getCapabilitiesForType(mConfig.getVideoCodec());
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
                if (types[j].equalsIgnoreCase(mConfig.getVideoCodec())) {
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


    @Override
    public void stop() {
        if (mRunning == true) {
//            if (mCamera != null) {
//                try {
//                    mCamera.stopPreview();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }

            if (videoEncoder != null) {
                videoEncoder.stop();
                Log.d(Constants.LOG_TAG, "videoEncoder stop");
            }
        }

        mRunning = false;
    }

    public void close() {
        mRunning = false;

        //        if (mCamera != null) {
//            try {
//                mCamera.stopPreview();
//                mCamera.release();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//            mCamera = null;
//        }

        if (videoEncoder != null) {
            videoEncoder.release();
            Log.d(Constants.LOG_TAG, "videoEncoder release");

            videoEncoder = null;
            Log.d(Constants.LOG_TAG, "videoEncoder complete");
        }
    }


    @Override
    public void release() {
        mRunning = false;

//        if (mCamera != null) {
//            try {
//                mCamera.stopPreview();
//                mCamera.release();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//            mCamera = null;
//        }

        if (videoEncoder != null) {
            videoEncoder.release();
            Log.d(Constants.LOG_TAG, "videoEncoder release");

            videoEncoder = null;
            Log.d(Constants.LOG_TAG, "videoEncoder complete");
        }

        sync.clear();
    }


    @Override
    public void run() {
    }


}
