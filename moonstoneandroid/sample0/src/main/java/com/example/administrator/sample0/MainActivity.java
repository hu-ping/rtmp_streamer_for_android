package com.example.administrator.sample0;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.ksy.recordlib.service.jmav.AVAudioCtrl;
import com.ksy.recordlib.service.jmav.AVContext;
import com.ksy.recordlib.service.jmav.AVError;
import com.ksy.recordlib.service.jmav.AVImageView;
import com.ksy.recordlib.service.jmav.AVRoomMulti;
import com.ksy.recordlib.service.jmav.AVVideoCtrl;

public class MainActivity extends Activity {
    private static int ROOM_NUM = 0;

    private AVContext mAVcontext = AVContext.createInstance(this);
    private AVContext.StartParam startParam = null;

    private FloatingActionButton mFab;
    private FloatingActionButton change;
    private FloatingActionButton flashlight;
    private AVImageView mImageView;

    private boolean mRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RtmfpCrashHandler crashHandler = RtmfpCrashHandler.getInstance();
        crashHandler.init(getApplicationContext()); //在Appliction里面设置我们的异常处理器为UncaughtExceptionHandler处理器

        registerReceiver();

        initView();
    }

    private void initView() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mImageView = (AVImageView)findViewById(R.id.glsurfaceview_camera);
        mAVcontext.setMagicView(mImageView);

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(btn_listener);

        flashlight = (FloatingActionButton) findViewById(R.id.flash);
        flashlight.setOnClickListener(btn_listener);

        change = (FloatingActionButton) findViewById(R.id.change);
        change.setOnClickListener(btn_listener);

        findViewById(R.id.btn_camera_beauty).setOnClickListener(btn_listener);
        findViewById(R.id.btn_camera_filter).setOnClickListener(btn_listener);
    }

    private View.OnClickListener btn_listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.btn_camera_filter:
//                    showFilters();
                    break;

                case R.id.btn_camera_beauty:
                    new AlertDialog.Builder(MainActivity.this)
                            .setSingleChoiceItems(new String[] { "关闭", "1", "2", "3", "4", "5"}, 5,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            mAVcontext.getVideoCtrl().inputBeautyParam(which);
                                            dialog.dismiss();
                                        }
                                    })
                            .setNegativeButton("取消", null)
                            .show();
                    break;
                case R.id.fab:
                    toggleContext();
                    break;

                case R.id.flash:
                    toggleFlash();
                    break;

                case R.id.change:
                    mAVcontext.getVideoCtrl().switchCamera(0, null);
                    break;
            }
        }
    };

    private void toggleContext() {
        if (!mRecording) {
            mRecording = true;
            mFab.setImageDrawable(getResources().getDrawable(R.mipmap.btn_stop));

            startParam = new AVContext.StartParam();
            startParam.app_version = "1";
            startParam.os = "Android";
            startParam.device_type = "device_type";
            startParam.sdk_version = "23";
            startParam.user_id = "user_id";
            mAVcontext.start(startParam, mStartContextCompleteCallback);
        } else {
            mRecording = false;
            mFab.setImageDrawable(getResources().getDrawable(R.mipmap.btn_record));

            mAVcontext.stop();
        }
    }

    private void toggleFlash() {
    }

    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AppConstants.ACTION_START_CONTEXT_COMPLETE);
        intentFilter.addAction(AppConstants.ACTION_ENTER_ROOM);
        registerReceiver(mBroadcastReceiver, intentFilter);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver();
        mAVcontext.destroy();
    }

    private void unregisterReceiver() {
        unregisterReceiver(mBroadcastReceiver);
    }

    /**
     * 开启摄像头和MIC
     */
    public void openCameraAndMic() {
        AVAudioCtrl avAudioCtrl = mAVcontext.getAudioCtrl();//开启Mic
        avAudioCtrl.enableMic(true);

        AVVideoCtrl avVideoCtrl = mAVcontext.getVideoCtrl();
        avVideoCtrl.enableCamera(0, true, new AVVideoCtrl.EnableCameraCompleteCallback() {
            protected void onComplete(boolean var1, int var2) {
                Log.d("RTMP", "EnableCameraCompleteCallback.OnComplete. result = " + var2);
            }
        });
    }

    private void startEnterRoom(int roomNum) {
        AVRoomMulti.EnterParam.Builder enterRoomParam = new AVRoomMulti.EnterParam.Builder(roomNum);

        enterRoomParam
                .auth(AppConstants.HOST_AUTH, null)
                .avControlRole(AppConstants.HOST_ROLE)
                .autoCreateRoom(true)
                .isEnableMic(true)
                .isEnableSpeaker(true);//；TODO：主播权限 所有权限

        enterRoomParam
                .audioCategory(AppConstants.AUDIO_VOICE_CHAT_MODE)
                .videoRecvMode(AVRoomMulti.VIDEO_RECV_MODE_SEMI_AUTO_RECV_CAMERA_VIDEO);

        if (mAVcontext != null) {
            // create room
            int ret = mAVcontext.enterRoom(mEventListener, enterRoomParam.build());
        }
    }

    /**
     * 房间回调
     */
    private AVRoomMulti.EventListener mEventListener = new AVRoomMulti.EventListener() {
        // 创建房间成功回调
        public void onEnterRoomComplete(int result) {
            if (result == 0) {
                MainActivity.this.sendBroadcast(new Intent(AppConstants.ACTION_ENTER_ROOM));
            } else {
//                quiteAVRoom();
            }
        }

        @Override
        public void onExitRoomComplete() {}

        @Override
        public void onRoomDisconnect(int i) {}

        //房间成员变化回调
        public void onEndpointsUpdateInfo(int eventid, String[] updateList) {}

        @Override
        public void onPrivilegeDiffNotify(int i) {}

        @Override
        public void onSemiAutoRecvCameraVideo(String[] strings) {}

        @Override
        public void onCameraSettingNotify(int i, int i1, int i2) {}

        @Override
        public void onRoomEvent(int i, int i1, Object o) {}
    };


    private AVContext.AVCallback mStartContextCompleteCallback = new AVContext.AVCallback() {
        public void onComplete(int result, String s) {
//            Toast.makeText(MainActivity.this, "SDKLogin complete : " + result, Toast.LENGTH_SHORT).show();

            if (result == AVError.AV_OK) {
                MainActivity.this.sendBroadcast(new Intent(AppConstants.ACTION_START_CONTEXT_COMPLETE));
            } else {

            }
        }
    };


    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //AvSurfaceView 初始化成功
            if (action.equals(AppConstants.ACTION_START_CONTEXT_COMPLETE)) {
                startEnterRoom(ROOM_NUM);
            } else if (action.equals(AppConstants.ACTION_ENTER_ROOM)) {
                openCameraAndMic();
            }

        }
    };
}
