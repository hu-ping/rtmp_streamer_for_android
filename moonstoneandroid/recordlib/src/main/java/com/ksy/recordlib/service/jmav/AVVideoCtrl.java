package com.ksy.recordlib.service.jmav;

import android.content.Context;
import android.util.Log;

import com.ksy.recordlib.service.core.SMRecordClientConfig;
import com.ksy.recordlib.service.magicfilter.MagicEngine;
import com.ksy.recordlib.service.magicfilter.widget.base.MagicBaseView;

/**
 * Created by huping on 2016/9/23.
 */
public class AVVideoCtrl {

    public int nativeObj = 0;
    public static final String TAG = "AVVideoCtrl";
    public static final int AVPresetType320240 = 1;
    public static final int AVPresetType480360 = 2;
    public static final int AVPresetType640480 = 3;
    public static final int AVPresetType640368 = 4;
    public static final int AVPresetType960540 = 5;
    public static final int AVPresetType1280720 = 6;
    public static final int FRONT_CAMERA = 0;
    public static final int BACK_CAMERA = 1;
    public static final int COLOR_FORMAT_I420 = 0;

    private Context mContext = null;
    private MagicBaseView mMagicView = null;
    private MagicEngine magicEngine;
    private SMRecordClientConfig config = null;

    public AVVideoCtrl() {
        this.nativeObj = 0;
    }

    public void init(Context var0, MagicBaseView var1, SMRecordClientConfig var2) {
        this.mContext = var0;
        this.mMagicView = var1;
        this.config = var2;

        MagicEngine.Builder builder = new MagicEngine.Builder();
        magicEngine = builder.build(mMagicView);
    }


    public void setRecordClientConfig(SMRecordClientConfig config) {
        this.config = config;
        magicEngine.setConfig(config);
    }

    public int enableCamera(int cameraType, boolean var2, AVVideoCtrl.EnableCameraCompleteCallback var3) {
        int ret = AVError.AV_OK;

        if (var2) {
            config.setmCameraType(cameraType);
            magicEngine.startRecord();
            var3.onComplete(true, AVError.AV_OK);
        } else {
            magicEngine.stopRecord();
        }

        return ret;
    }

    public void unInit() {
        if(magicEngine != null) {
            magicEngine.stopRecord();
            magicEngine = null;
        }

    }

    public  void inputBeautyParam(float var1) {
        magicEngine.setBeautyLevel((int) var1);
    }

    public  void inputWhiteningParam(float var1) {

    }

    public int switchCamera(int var1, AVVideoCtrl.SwitchCameraCompleteCallback var2) {
        int ret = AVError.AV_OK;

        magicEngine.switchCamera();

        return ret;
    }

    public static class SwitchCameraCompleteCallback {
        static final String TAG = "SdkJni";

        public SwitchCameraCompleteCallback() {
        }

        protected void onComplete(int var1, int var2) {
            Log.d("SdkJni", "SwitchCameraCompleteCallback.OnComplete. result = " + var2);
        }
    }

//    public native int enableExternalCapture(boolean var1, AVVideoCtrl.EnableExternalCaptureCompleteCallback var2);
//
//    public native int fillExternalCaptureFrame(byte[] var1, int var2, int var3, int var4, int var5, int var6, int var7);
//
//    public native int switchCamera(int var1, AVVideoCtrl.SwitchCameraCompleteCallback var2);
//
//    public native int getCameraNum();
//
//    public native void setRotation(int var1);
//
//    public native void setCameraPara(Camera.Parameters var1);
//
//    public native Object getCameraPara();
//
//    public native String getQualityTips();
//
//    public native boolean setLocalVideoPreviewCallback(AVVideoCtrl.LocalVideoPreviewCallback var1);
//
//    public native boolean setLocalVideoPreProcessCallback(AVVideoCtrl.LocalVideoPreProcessCallback var1);
//
//    /** @deprecated */
//    @Deprecated
//    public native boolean setRemoteVideoPreviewCallback(AVVideoCtrl.RemoteVideoPreviewCallback var1);
//
//    public native boolean setRemoteVideoPreviewCallbackWithByteBuffer(AVVideoCtrl.RemoteVideoPreviewCallbackWithByteBuffer var1);
//
//    public native boolean setRemoteScreenVideoPreviewCallback(AVVideoCtrl.RemoteScreenVideoPreviewCallback var1);
//
//    public static boolean isEnableBeauty() {
//        QLog.d("AVVideoCtrl", 0, "isEnable = " + VcSystemInfo.isBeautySupported());
//        return VcSystemInfo.isBeautySupported();
//    }
//
//    public native void inputBeautyParam(float var1);
//
//    public native void inputWhiteningParam(float var1);
//
//    public synchronized native void setCameraPreviewChangeCallback(AVVideoCtrl.CameraPreviewChangeCallback var1);
//
//    public native Object getCamera();
//
//    public native Object getCameraHandler();
//
//    public native void setAfterPreviewListener(AVVideoCtrl.AfterPreviewListener var1);
//
//    public int addWatermark(int var1, Bitmap var2) {
//        if(var1 >= 1 && var1 <= 6) {
//            if(var2 == null) {
//                QLog.d("AVVideoCtrl", 0, "bitmap null");
//                return 1004;
//            } else {
//                int[] var3 = new int[var2.getWidth() * var2.getHeight()];
//                var2.getPixels(var3, 0, var2.getWidth(), 0, 0, var2.getWidth(), var2.getHeight());
//                return this.nativeAddWatermark(var1, var3, var2.getWidth(), var2.getHeight());
//            }
//        } else {
//            QLog.d("AVVideoCtrl", 0, "AVPresetType error");
//            return 1004;
//        }
//    }
//
//    private native int nativeAddWatermark(int var1, int[] var2, int var3, int var4);
//
//    void init(int var1) {
//        this.initNative(var1);
//    }
//
//    void unInit() {
//        this.unInitNative();
//    }
//
//    private native void initNative(int var1);
//
//    private native void unInitNative();
//
//    public static class VideoFrameWithByteBuffer extends AVVideoCtrl.VideoFrameWithoutData {
//        public ByteBuffer data;
//
//        public VideoFrameWithByteBuffer() {
//        }
//    }
//
//    public static class VideoFrame extends AVVideoCtrl.VideoFrameWithoutData {
//        public byte[] data;
//
//        public VideoFrame() {
//        }
//    }
//
//    static class VideoFrameWithoutData {
//        public int dataLen;
//        public int width;
//        public int height;
//        public int rotate;
//        public int videoFormat;
//        public String identifier;
//        public int srcType;
//
//        VideoFrameWithoutData() {
//        }
//    }
//
//    public interface AfterPreviewListener {
//        void onFrameReceive(AVVideoCtrl.VideoFrame var1);
//    }
//
//    public static class CameraPreviewChangeCallback {
//        static final String TAG = "SdkJni";
//
//        public CameraPreviewChangeCallback() {
//        }
//
//        public void onCameraPreviewChangeCallback(int var1) {
//            Log.d("SdkJni", "base class CameraPreviewChangeCallback.onCameraPreviewChangeCallback");
//        }
//    }
//
//    public static class RemoteScreenVideoPreviewCallback {
//        static final String TAG = "SdkJni";
//
//        public RemoteScreenVideoPreviewCallback() {
//        }
//
//        public void onFrameReceive(AVVideoCtrl.VideoFrame var1) {
//            Log.d("SdkJni", "base class RemoteScreenVideoPreviewCallback.onFrameReceive");
//        }
//    }
//
//    public interface RemoteVideoPreviewCallbackWithByteBuffer {
//        void onFrameReceive(AVVideoCtrl.VideoFrameWithByteBuffer var1);
//    }
//
//    /** @deprecated */
//    @Deprecated
//    public static class RemoteVideoPreviewCallback {
//        static final String TAG = "SdkJni";
//
//        public RemoteVideoPreviewCallback() {
//        }
//
//        public void onFrameReceive(AVVideoCtrl.VideoFrame var1) {
//            Log.d("SdkJni", "base class RemoteVideoPreviewCallback.onFrameReceive");
//        }
//    }
//
//    public static class LocalVideoPreProcessCallback {
//        static final String TAG = "SdkJni";
//
//        public LocalVideoPreProcessCallback() {
//        }
//
//        public void onFrameReceive(AVVideoCtrl.VideoFrame var1) {
//            Log.d("SdkJni", "base class SetLocalPreProcessCallback.onFrameReceive");
//        }
//    }
//
//    public static class LocalVideoPreviewCallback {
//        static final String TAG = "SdkJni";
//
//        public LocalVideoPreviewCallback() {
//        }
//
//        public void onFrameReceive(AVVideoCtrl.VideoFrame var1) {
//            Log.d("SdkJni", "base class LocalVideoPreviewCallback.onFrameReceive");
//        }
//    }
//
//    public static class SwitchCameraCompleteCallback {
//        static final String TAG = "SdkJni";
//
//        public SwitchCameraCompleteCallback() {
//        }
//
//        protected void onComplete(int var1, int var2) {
//            Log.d("SdkJni", "SwitchCameraCompleteCallback.OnComplete. result = " + var2);
//        }
//    }
//
//    public static class EnableExternalCaptureCompleteCallback {
//        static final String TAG = "SdkJni";
//
//        public EnableExternalCaptureCompleteCallback() {
//        }
//
//        protected void onComplete(boolean var1, int var2) {
//            Log.d("SdkJni", "EnableExternalCaptureCompleteCallback.OnComplete. enable = " + var1 + "  result = " + var2);
//        }
//    }
//
    public static class EnableCameraCompleteCallback {
        static final String TAG = "RTMP";

        public EnableCameraCompleteCallback() {
        }

        protected void onComplete(boolean var1, int var2) {
            Log.d("SdkJni", "EnableCameraCompleteCallback.OnComplete. result = " + var2);
        }
    }
}
