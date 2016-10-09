package com.ksy.recordlib.service.jmav;

import android.content.Context;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import com.ksy.recordlib.service.core.SMRecordClientConfig;
import com.ksy.recordlib.service.jmav.packet.AVEnterRoomPacket;
import com.ksy.recordlib.service.jmav.packet.AVInitImagePacket;
import com.ksy.recordlib.service.magicfilter.widget.base.MagicBaseView;
import com.ksy.recordlib.service.util.Constants;

/**
 * Created by huping on 2016/9/23.
 */
public class AVContextImpl extends AVContext{
    private static final String TAG = "SdkJni";
    private static boolean sLoadLibrary = false;
    private static int sExtractSoError = 0;
    private AVRoomMulti room = null;
    private int mNativeEntity = 0;
    private Context mContext = null;

    private Thread mWorker = null;

    // TODO: 2016/9/28 现在还没有使用到，等以后再用
//    private AVImageConfig mImageConfig = null;

    private SMRecordClientConfig mRecordClientConfig = null;
    private AVRoomMulti.EventListener mEventListener = null;

    private AVInitImagePacket mInitImagePacket = new AVInitImagePacket();
    private AVEnterRoomPacket mEnterRoomPacket = new AVEnterRoomPacket();

    private MagicBaseView mMagicView = null;

    private AVAudioCtrl mAudioCtrl = null;
    private AVVideoCtrl mVideoCtrl = null;

    public AVContextImpl(Context var0) {
        this.mContext = var0;
        this.room = new AVRoomMulti();

        if(null == this.mVideoCtrl) {
            this.mVideoCtrl = new AVVideoCtrl();
        }
    }

    @Override
    public int start(StartParam var1, AVCallback var2) {
        return doInitImageConfig(var1, var2);
    }

    @Override
    public int enterRoom(AVRoomMulti.EventListener var1, AVRoomMulti.EnterParam var2) {
        this.mEventListener = var1;
        return doEnterRoom(var2);
    }

    @Override
    public void setMagicView(AVImageView view) {
        this.mMagicView = view;

        if(null != this.mVideoCtrl) {
            this.mVideoCtrl.init(this.mContext, this.mMagicView, this.mRecordClientConfig);
        }
    }

    @Override
    public AVAudioCtrl getAudioCtrl() {
        if(null == this.mAudioCtrl) {
            this.mAudioCtrl = new AVAudioCtrl();
            this.mAudioCtrl.init(this.mContext, this.mRecordClientConfig);
        }

        return this.mAudioCtrl;
    }

    @Override
    public AVVideoCtrl getVideoCtrl() {
        if(null == this.mVideoCtrl) {
            this.mVideoCtrl = new AVVideoCtrl();
            this.mVideoCtrl.init(this.mContext, this.mMagicView, this.mRecordClientConfig);
        }

        return this.mVideoCtrl;
    }

    @Override
    public int exitRoom() {
        int ret = AVError.AV_OK;

        doExitRoom();
        getAudioCtrl().enableMic(false);
        getVideoCtrl().enableCamera(0, false, null);

        return ret;
    }

    @Override
    public int stop() {
        int ret = AVError.AV_OK;

        doExitRoom();
        getAudioCtrl().enableMic(false);
        getVideoCtrl().enableCamera(0, false, null);

        return ret;
    }

    @Override
    public void destroy() {
        doExitRoom();

        if(this.mAudioCtrl != null) {
            this.mAudioCtrl.unInit();
            this.mAudioCtrl = null;
        }

        if(this.mVideoCtrl != null) {
            this.mVideoCtrl.unInit();
            this.mVideoCtrl = null;
        }


        this.mContext = null;
    }

    private int doExitRoom() {
        int ret = AVError.AV_OK;

        new Thread(new Runnable() {
            @Override
            public void run() {
//                AVExitRoomPacket exitRoomPacket = new AVExitRoomPacket();
//                exitRoomPacket.mUserId = mInitImagePacket.mStartParam.user_id;
//                exitRoomPacket.mRoomId = mEnterRoomPacket.mEnterParam.getRelationId();
//                exitRoomPacket.doHttpConnect();
            }
        }).start();

        return ret;
    }

    private int doEnterRoom(final AVRoomMulti.EnterParam var2) {
        int ret = AVError.AV_OK;

        new Thread(new Runnable() {
            @Override
            public void run() {
//                mEnterRoomPacket.mEnterParam = var2;
//                int ret = mEnterRoomPacket.doHttpConnect();
//                mEventListener.onEnterRoomComplete(ret);

                mEventListener.onEnterRoomComplete(AVError.AV_OK);

            }
        }).start();

        return ret;
    }


    private int doInitImageConfig(final StartParam var1, final AVCallback var2) {
        int ret = AVError.AV_OK;

        new Thread(new Runnable() {
            @Override
            public void run() {
//                mInitImagePacket.mStartParam = var1;
//                int ret = mInitImagePacket.doHttpConnect();
//                if (mInitImagePacket.mImageConfig != null) {

                    SMRecordClientConfig.Builder builder = new SMRecordClientConfig.Builder();
                    builder.setVideoProfile(CamcorderProfile.QUALITY_480P);
                    builder.setUrl(Constants.URL_DEFAULT);
                    builder.setCameraType(Camera.CameraInfo.CAMERA_FACING_BACK);
                    mRecordClientConfig = builder.build();

                    Display display = ((WindowManager)mContext
                            .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
                    int rotation = display.getRotation();
                    int degrees = 0;
                    switch (rotation) {
                        case Surface.ROTATION_0:
                            degrees = 0;
                            break;
                        case Surface.ROTATION_90:
                            degrees = 90;
                            break;
                        case Surface.ROTATION_180:
                            degrees = 180;
                            break;
                        case Surface.ROTATION_270:
                            degrees = 270;
                            break;
                    }
                    mRecordClientConfig.setRecordOrientation(degrees);

                mVideoCtrl.setRecordClientConfig(mRecordClientConfig);

//                }
//                var2.onComplete(ret, null);

                var2.onComplete(AVError.AV_OK, null);
            }
        }).start();

        return ret;
    }

}
