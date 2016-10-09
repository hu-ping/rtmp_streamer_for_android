package com.ksy.recordlib.service.jmav;

/**
 * Created by huping on 2016/9/23.
 */
public class AVEndpoint {
    public int nativeObj = 0;

    public String user_id;
    public boolean is_audio;
    public boolean is_camera_video;
    public boolean is_screen_video;
    public long last_audio_stamp_recv;
    public long last_audio_stamp_send;
    public long last_video_stamp_recv;
    public long last_video_stamp_send;
    public String stream_role;
    public String stream_address;

    public AVEndpoint() {
        this.nativeObj = 0;
    }



    public native String getId();

    public native AVEndpoint.Info getInfo();

    public native boolean hasAudio();

    public native boolean hasCameraVideo();

    public native boolean hasScreenVideo();

    public native long getLastAudioStampRecv();

    public native long getLastAudioStampSend();

    public native long getLastVideoStampRecv();

    public native long getLastVideoStampSend();

    public static class Info {
        static final String TAG = "SdkJni";
        public static final int AVTerminal_Unknown = 0;
        public static final int AVTerminal_Mobile = 1;
        public static final int AVTerminal_iPhone = 2;
        public static final int AVTerminal_iPad = 3;
        public static final int AVTerminal_Android = 4;
        public static final int AVTerminal_PC = 5;
        public static final int AVTerminal_WINRTPAD = 6;
        public static final int AVTerminal_WINRTPHONE = 7;
        public static final int AVTerminal_AndroidPad = 8;

        public String openId;
        public int sdkVersion;
        public int terminalType;
        /** @deprecated */
        public int state;

        public Info() {
            this.openId = "";
            this.sdkVersion = 0;
            this.terminalType = 0;
            this.state = 0;
        }

        public Info(String var1, int var2, int var3, int var4) {
            this.openId = var1;
            this.sdkVersion = var2;
            this.terminalType = var3;
            this.state = var4;
        }
    }

}
