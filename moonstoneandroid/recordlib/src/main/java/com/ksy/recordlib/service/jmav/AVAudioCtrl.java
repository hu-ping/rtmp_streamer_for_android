package com.ksy.recordlib.service.jmav;

import android.content.Context;

import com.ksy.recordlib.service.core.KsyRecordClient;
import com.ksy.recordlib.service.core.SMRecordClientConfig;
import com.ksy.recordlib.service.exception.KsyRecordException;
import com.ksy.recordlib.service.util.OnClientErrorListener;

/**
 * Created by huping on 2016/9/23.
 */
public class AVAudioCtrl {

    static final String TAG = "SdkJni";
    public static final int OUTPUT_MODE_HEADSET = 0;
    public static final int OUTPUT_MODE_SPEAKER = 1;
    public static final int AUDIO_CODEC_TYPE_SILK = 4102;
    public static final int AUDIO_CODEC_TYPE_CELT = 4103;
    public int nativeObj = 0;
//    boolean mIsCalling = false;
//    private String mAudioSessionType = "DEVICE_EARPHONE;DEVICE_SPEAKERPHONE;DEVICE_BLUETOOTHHEADSET;DEVICE_WIREDHEADSET;";
//    private String mSelectedDeviceName = "";
//    private String[] mDeviceList;
//    private TraeAudioSession mAudioSession = null;
//    private int mVoiceStreamType = 0;
//    private String mAudioStateBeforePhoneCall = "DEVICE_NONE";
//    private AVAudioCtrl.Delegate mDelegate = null;
//    private boolean isSystemApp = false;
//    private PhoneStatusMonitor mPhoneStatusMonitor;
//    private PhoneStatusListener mPhoneStatusListener;


    private Context mContext = null;
    private SMRecordClientConfig config = null;
    private KsyRecordClient client;
    private RecordClientListener listener = new RecordClientListener();

    public AVAudioCtrl() {
        this.nativeObj = 0;
    }

    public void init(Context var0, SMRecordClientConfig var1) {
        this.mContext = var0;
        this.config = var1;

        client = KsyRecordClient.getInstance(mContext.getApplicationContext());
        client.setConfig(config);

        client.setNetworkChangeListener(listener);
        client.setPushStreamStateListener(listener);
        client.setSwitchCameraStateListener(listener);
        client.setStartListener(listener);
        client.setOnClientErrorListener(listener);
    }

    public int enableMic(boolean var1) {
        int ret = AVError.AV_OK;

        if(var1) {
            try {
                client.startRecord();
            } catch (KsyRecordException e) {
                e.printStackTrace();
                return AVError.AV_ERR_FAILED;
            }
        } else {
            client.stopRecord();
        }

        return ret;
    }

    public void unInit() {
        if (client != null) {
            client.stopRecord();
            client = null;
        }
    }

    private class RecordClientListener implements  KsyRecordClient.NetworkChangeListener,
            KsyRecordClient.PushStreamStateListener, KsyRecordClient.SwitchCameraStateListener,
            KsyRecordClient.StartListener, OnClientErrorListener {

        @Override
        public void onNetworkChanged(int state) {

        }

        @Override
        public void onClientError(int source, int what) {

        }

        @Override
        public void onPushStreamState(int state) {

        }

        @Override
        public void OnStartComplete() {

        }

        @Override
        public void OnStartFailed() {

        }

        @Override
        public void onSwitchCameraDisable() {

        }

        @Override
        public void onSwitchCameraEnable() {

        }
    }

//    /** @deprecated */
//    @Deprecated
//    public native int registAudioDataCallback(int var1, AVAudioCtrl.RegistAudioDataCompleteCallback var2);
//
//    public native int registAudioDataCallbackWithByteBuffer(int var1, AVAudioCtrl.RegistAudioDataCompleteCallbackWithByteBuffer var2);
//
//    public native int unregistAudioDataCallback(int var1);
//
//    public native int unregistAudioDataCallbackAll();
//
//    public native boolean setAudioDataFormat(int var1, AVAudioCtrl.AudioFrameDesc var2);
//
//    public native AVAudioCtrl.AudioFrameDesc getAudioDataFormat(int var1);
//
//    public native int setAudioDataVolume(int var1, float var2);
//
//    public native float getAudioDataVolume(int var1);
//
//    public native int SetAudioDataDBVolume(int var1, int var2);
//
//    public native int GetAudioDataDBVolume(int var1);
//
//    public native int getVolume();
//
//    public native int getDynamicVolume();
//
//    public native void setVolume(int var1);
//
//    public boolean enableMic(boolean var1) {
//        return this.mIsCalling?false:this.nativeEnableMic(var1);
//    }
//
//    native boolean nativeEnableMic(boolean var1);
//
//    public boolean enableSpeaker(boolean var1) {
//        return this.mIsCalling?false:this.nativeEnableSpeaker(var1);
//    }
//
//    native boolean nativeEnableSpeaker(boolean var1);
//
//    public boolean setAudioOutputMode(int var1) {
//        if(0 == var1) {
//            if(null == this.mDeviceList) {
//                return false;
//            }
//
//            boolean var2 = false;
//
//            do {
//                int var3;
//                for(var3 = 0; var3 < this.mDeviceList.length && !var2; ++var3) {
//                    if("DEVICE_WIREDHEADSET".equals(this.mDeviceList[var3])) {
//                        this.mAudioSession.connectDevice("DEVICE_WIREDHEADSET");
//                        var2 = true;
//                        break;
//                    }
//                }
//
//                for(var3 = 0; var3 < this.mDeviceList.length && !var2; ++var3) {
//                    if("DEVICE_BLUETOOTHHEADSET".equals(this.mDeviceList[var3])) {
//                        this.mAudioSession.connectDevice("DEVICE_BLUETOOTHHEADSET");
//                        var2 = true;
//                        break;
//                    }
//                }
//
//                for(var3 = 0; var3 < this.mDeviceList.length && !var2; ++var3) {
//                    if("DEVICE_EARPHONE".equals(this.mDeviceList[var3])) {
//                        this.mAudioSession.connectDevice("DEVICE_EARPHONE");
//                        var2 = true;
//                        break;
//                    }
//                }
//            } while(!var2);
//        } else {
//            if(1 != var1) {
//                return false;
//            }
//
//            this.mAudioSession.connectDevice("DEVICE_SPEAKERPHONE");
//        }
//
//        return true;
//    }
//
//    public int getAudioOutputMode() {
//        byte var1 = 0;
//        if(this.mSelectedDeviceName.endsWith("DEVICE_SPEAKERPHONE")) {
//            var1 = 1;
//        } else if(this.mSelectedDeviceName.endsWith("DEVICE_WIREDHEADSET")) {
//            var1 = 0;
//        } else if(this.mSelectedDeviceName.endsWith("DEVICE_BLUETOOTHHEADSET")) {
//            var1 = 0;
//        } else if(this.mSelectedDeviceName.endsWith("DEVICE_EARPHONE")) {
//            var1 = 0;
//        }
//
//        return var1;
//    }
//
//    public void setDelegate(AVAudioCtrl.Delegate var1) {
//        this.mDelegate = var1;
//    }
//
//    public native String getQualityTips();
//
//    public int changeAudioCategory(int var1) {
//        return this.nativeChangeAudioCategory(var1);
//    }
//
//    private native int nativeChangeAudioCategory(int var1);
//
//    native boolean initNative(int var1);
//
//    native boolean uninitNative();
//
//    native void pauseAudio();
//
//    native void resumeAudio();
//
//    boolean init(Context var1, int var2) {
//        if(!this.initNative(var2)) {
//            return false;
//        } else {
//            TraeAudioManager.init(var1);
//            this.mAudioSession = new TraeAudioSession(var1, new ITraeAudioCallback() {
//                public void onServiceStateUpdate(boolean var1) {
//                }
//
//                public void onDeviceListUpdate(String[] var1, String var2, String var3, String var4) {
//                    Log.e("SdkJni", "Connect Device:" + var2);
//                    AVAudioCtrl.this.mSelectedDeviceName = var2;
//                    AVAudioCtrl.this.mDeviceList = var1;
//                    if(null != AVAudioCtrl.this.mDelegate) {
//                        AVAudioCtrl.this.mDelegate.onOutputModeChange(AVAudioCtrl.this.mSelectedDeviceName.endsWith("DEVICE_SPEAKERPHONE")?1:0);
//                    }
//
//                    if(!AVAudioCtrl.this.mAudioStateBeforePhoneCall.equals("DEVICE_NONE")) {
//                        Log.e("SdkJni", "!mAudioStateBeforePhoneCall.equals(TraeAudioManager.DEVICE_NONE");
//                        if(!var2.equals(AVAudioCtrl.this.mAudioStateBeforePhoneCall)) {
//                            AVAudioCtrl.this.mAudioSession.connectDevice(AVAudioCtrl.this.mAudioStateBeforePhoneCall);
//                        }
//
//                        AVAudioCtrl.this.mAudioStateBeforePhoneCall = "DEVICE_NONE";
//                    }
//
//                }
//
//                public void onDeviceChangabledUpdate(boolean var1) {
//                }
//
//                public void onStreamTypeUpdate(int var1) {
//                    AVAudioCtrl.this.mVoiceStreamType = var1;
//                }
//
//                public void onGetDeviceListRes(int var1, String[] var2, String var3, String var4, String var5) {
//                    Log.e("SdkJni", "onGetDeviceListRes" + var3);
//                    if(var1 == 0) {
//                        AVAudioCtrl.this.mDeviceList = var2;
//                        AVAudioCtrl.this.mSelectedDeviceName = var3;
//                    }
//                }
//
//                public void onConnectDeviceRes(int var1, String var2, boolean var3) {
//                    Log.e("SdkJni", "onConnectDeviceRes" + var2);
//                    if(var1 == 0 && var3) {
//                        if(var3) {
//                            AVAudioCtrl.this.mSelectedDeviceName = var2;
//                        }
//
//                    }
//                }
//
//                public void onIsDeviceChangabledRes(int var1, boolean var2) {
//                }
//
//                public void onGetConnectedDeviceRes(int var1, String var2) {
//                    Log.e("SdkJni", "onGetConnectedDeviceRes" + var2);
//                }
//
//                public void onGetConnectingDeviceRes(int var1, String var2) {
//                    Log.e("SdkJni", "onGetConnectingDeviceRes" + var2);
//                }
//
//                public void onGetStreamTypeRes(int var1, int var2) {
//                    AVAudioCtrl.this.mVoiceStreamType = var2;
//                }
//
//                public void onRingCompletion(int var1, String var2) {
//                }
//
//                public void onVoicecallPreprocessRes(int var1) {
//                }
//
//                public void onAudioRouteSwitchStart(String var1, String var2) {
//                }
//
//                public void onAudioRouteSwitchEnd(String var1, long var2) {
//                }
//            });
//            this.mAudioSession.startService(this.mAudioSessionType);
//            this.mAudioSession.getDeviceList();
//            this.mPhoneStatusListener = new AVAudioCtrl.MyPhoneStatusListener();
//            this.mPhoneStatusMonitor = new PhoneStatusMonitor(var1, this.mPhoneStatusListener);
//            return true;
//        }
//    }
//
//    void uninit() {
//        if(this.mAudioSession != null) {
//            this.mAudioSession.setCallback((ITraeAudioCallback)null);
//
//            try {
//                this.mAudioSession.stopService();
//                this.mAudioSession.release();
//            } catch (Exception var5) {
//                ;
//            } finally {
//                this.mAudioSession = null;
//            }
//        }
//
//        TraeAudioManager.uninit();
//        if(this.mPhoneStatusMonitor != null) {
//            this.mPhoneStatusMonitor.uninit();
//            this.mPhoneStatusMonitor = null;
//        }
//
//        this.mPhoneStatusListener = null;
//        this.uninitNative();
//    }
//
//    public void stopTRAEService() {
//        if(this.mAudioSession != null) {
//            Log.e("SdkJni", "AVAudioCtrl stopTRAEService succ");
//            this.mAudioSession.stopService();
//        } else {
//            Log.e("SdkJni", "AVAudioCtrl stopTRAEService mAudioSession = null");
//        }
//
//    }
//
//    public void startTRAEService() {
//        if(this.mAudioSession != null) {
//            this.mAudioSession.startService(this.mAudioSessionType);
//            Log.e("SdkJni", "AVAudioCtrl startTRAEService succ");
//        } else {
//            Log.e("SdkJni", "AVAudioCtrl startTRAEService mAudioSession = null");
//        }
//
//    }
//
//    public void setIsSystemApp(boolean var1) {
//        this.isSystemApp = var1;
//    }
//
//    public void startTRAEServiceWhenIsSystemApp() {
//        if(this.mAudioSession != null) {
//            this.resumeAudio();
//            this.mAudioSession.startService(this.mAudioSessionType);
//            Log.e("SdkJni", "AVAudioCtrl startTRAEServiceWhenIsSystemApp succ");
//        } else {
//            Log.e("SdkJni", "AVAudioCtrl startTRAEServiceWhenIsSystemApp mAudioSession = null");
//        }
//
//    }
//
//    public void stopTRAEServiceWhenIsSystemApp() {
//        if(this.mAudioSession != null) {
//            Log.e("SdkJni", "AVAudioCtrl stopTRAEServiceWhenIsSystemApp succ");
//            this.pauseAudio();
//            this.mAudioSession.stopService();
//        } else {
//            Log.e("SdkJni", "AVAudioCtrl stopTRAEServiceWhenIsSystemApp mAudioSession = null");
//        }
//
//    }
//
//    class MyPhoneStatusListener implements PhoneStatusListener {
//        MyPhoneStatusListener() {
//        }
//
//        public void onCallStateChanged(boolean var1) {
//            if(AVAudioCtrl.this.isSystemApp) {
//                Log.e("SdkJni", "onCallStateChanged isSystemApp return");
//            } else {
//                Log.e("SdkJni", "onCallStateChanged isCalling: " + var1);
//                AVAudioCtrl.this.mIsCalling = var1;
//                if(var1) {
//                    AVAudioCtrl.this.pauseAudio();
//                    Log.e("SdkJni", "MyPhoneStatusListener iscalling ");
//                    if(AVAudioCtrl.this.mAudioSession != null) {
//                        Log.e("SdkJni", "MyPhoneStatusListener stopService ");
//                        AVAudioCtrl.this.mAudioSession.stopService();
//                    }
//                } else {
//                    Log.e("SdkJni", "MyPhoneStatusListener notcalling ");
//                    AVAudioCtrl.this.resumeAudio();
//                    if(AVAudioCtrl.this.mAudioSession != null) {
//                        AVAudioCtrl.this.mAudioSession.startService(AVAudioCtrl.this.mAudioSessionType);
//                        Log.e("SdkJni", "MyPhoneStatusListener startService ");
//                    }
//                }
//
//            }
//        }
//    }
//
//    public static class Delegate {
//        static final String TAG = "SdkJni";
//
//        public Delegate() {
//        }
//
//        protected void onOutputModeChange(int var1) {
//            Log.d("SdkJni", "onOutputModeChange outputMode = " + var1);
//        }
//    }
//
//    public interface RegistAudioDataCompleteCallbackWithByteBuffer {
//        int onComplete(AVAudioCtrl.AudioFrameWithByteBuffer var1, int var2);
//    }
//
//    /** @deprecated */
//    @Deprecated
//    public static class RegistAudioDataCompleteCallback {
//        static final String TAG = "SdkJni";
//
//        public RegistAudioDataCompleteCallback() {
//        }
//
//        protected int onComplete(AVAudioCtrl.AudioFrame var1, int var2) {
//            return 1;
//        }
//    }
//
//    public static class AudioDataSourceType {
//        public static final int AUDIO_DATA_SOURCE_MIC = 0;
//        public static final int AUDIO_DATA_SOURCE_MIXTOSEND = 1;
//        public static final int AUDIO_DATA_SOURCE_SEND = 2;
//        public static final int AUDIO_DATA_SOURCE_MIXTOPLAY = 3;
//        public static final int AUDIO_DATA_SOURCE_PLAY = 4;
//        public static final int AUDIO_DATA_SOURCE_NETSTREM = 5;
//        public static final int AUDIO_DATA_SOURCE_VOICEDISPOSE = 6;
//        public static final int AUDIO_DATA_SOURCE_END = 7;
//
//        public AudioDataSourceType() {
//        }
//    }
//
//    public static class AudioFrameDesc {
//        public int sampleRate;
//        public int channelNum;
//        public int bits;
//        public int srcTye;
//
//        public AudioFrameDesc() {
//        }
//    }
//
//    public static class AudioFrameWithByteBuffer extends AVAudioCtrl.AudioFrameWithoutData {
//        public ByteBuffer data;
//
//        public AudioFrameWithByteBuffer() {
//        }
//    }
//
//    /** @deprecated */
//    @Deprecated
//    public static class AudioFrame extends AVAudioCtrl.AudioFrameWithoutData {
//        public byte[] data;
//
//        public AudioFrame() {
//        }
//    }
//
//    static class AudioFrameWithoutData {
//        public String identifier;
//        public int sampleRate;
//        public int channelNum;
//        public int bits;
//        public int srcTye;
//        public int dataLen;
//
//        AudioFrameWithoutData() {
//        }
//    }
}
