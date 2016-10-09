package com.ksy.recordlib.service.jmav;

/**
 * Created by huping on 2016/9/23.
 */
public class AVRoomMulti {

    static final String TAG = "SdkJni";
    protected int nativeObj = 0;
    public static final long AUTH_BITS_DEFAULT = -1L;
    public static final long AUTH_BITS_CREATE_ROOM = 1L;
    public static final long AUTH_BITS_JOIN_ROOM = 2L;
    public static final long AUTH_BITS_SEND_AUDIO = 4L;
    public static final long AUTH_BITS_RECV_AUDIO = 8L;
    public static final long AUTH_BITS_SEND_CAMERA_VIDEO = 16L;
    public static final long AUTH_BITS_RECV_CAMERA_VIDEO = 32L;
    public static final long AUTH_BITS_SEND_SCREEN_VIDEO = 64L;
    public static final long AUTH_BITS_RECV_SCREEN_VIDEO = 128L;
    public static final int AUDIO_CATEGORY_VOICECHAT = 0;
    public static final int AUDIO_CATEGORY_MEDIA_PLAY_AND_RECORD = 1;
    public static final int AUDIO_CATEGORY_MEDIA_PLAYBACK = 2;
    public static final int AUDIO_CATEGORY_MEDIA_PLAY_AND_RECORD_HIGH_QUALITY = 3;
    public static final int VIDEO_RECV_MODE_MANUAL = 0;
    public static final int VIDEO_RECV_MODE_SEMI_AUTO_RECV_CAMERA_VIDEO = 1;

    AVRoomMulti() {
        this.nativeObj = 0;
    }

//    public native int getRoomId();
//
//    public native int getEndpointCount();
//
//    public native AVEndpoint getEndpointById(String var1);
//
//    public native void setNetType(int var1);
//
//    public native boolean changeAuthority(long var1, byte[] var3, int var4, AVRoomMulti.ChangeAuthorityCallback var5);
//
//    public native int changeAVControlRole(String var1, AVRoomMulti.ChangeAVControlRoleCompleteCallback var2);
//
//    public native String getQualityTips();
//
//    public native String getQualityParam();
//
//    public native String getStatisticsParam();
//
//    public native int requestViewList(String[] var1, AVView[] var2, int var3, AVRoomMulti.RequestViewListCompleteCallback var4);
//
//    public native int cancelAllView(AVCallback var1);
//
//    public static class NetWorkInfo {
//        public int ip = -1;
//        public int port = -1;
//
//        public NetWorkInfo() {
//        }
//    }


    //    public interface RequestViewListCompleteCallback {
//        void OnComplete(String[] var1, AVView[] var2, int var3, int var4);
//    }
//

    public interface EventListener {
        void onEnterRoomComplete(int var1);

        void onExitRoomComplete();

        void onRoomDisconnect(int var1);

        void onEndpointsUpdateInfo(int var1, String[] var2);

        void onPrivilegeDiffNotify(int var1);

        void onSemiAutoRecvCameraVideo(String[] var1);

        void onCameraSettingNotify(int var1, int var2, int var3);

        void onRoomEvent(int var1, int var2, Object var3);
    }


    public static class EnterParam {
        private int relationId;
        private long authBits;
        private byte[] authBuffer;
        private String controlRole;
        private int audioCategory;
        private boolean createRoom;
        private int videoRecvMode;
        private boolean autoRotateVideo;
        private boolean enableMic;
        private boolean enableSpeaker;
        private boolean enableHdAudio;

        private EnterParam(AVRoomMulti.EnterParam.Builder var1) {
            this.relationId = var1.relationId;
            this.authBits = var1.authBits;
            this.authBuffer = var1.authBuffer;
            this.controlRole = var1.controlRole;
            this.audioCategory = var1.audioCategory;
            this.createRoom = var1.autoCreateRoom;
            this.videoRecvMode = var1.videoRecvMode;
            this.autoRotateVideo = var1.autoRotateVideo;
            this.enableMic = var1.enableMic;
            this.enableSpeaker = var1.enableSpeaker;
            this.enableHdAudio = var1.enableHdAudio;
        }

        public int getRelationId() {
            return this.relationId;
        }

        public long getAuthBits() {
            return authBits;
        }

        public byte[] getAuthBuffer() {
            return authBuffer;
        }

        public String getControlRole() {
            return controlRole;
        }

        public int getAudioCategory() {
            return audioCategory;
        }

        public boolean isCreateRoom() {
            return createRoom;
        }

        public int getVideoRecvMode() {
            return videoRecvMode;
        }

        public boolean isAutoRotateVideo() {
            return autoRotateVideo;
        }

        public boolean isEnableMic() {
            return enableMic;
        }

        public boolean isEnableSpeaker() {
            return enableSpeaker;
        }

        public boolean isEnableHdAudio() {
            return enableHdAudio;
        }


        public static class Builder {
            private int relationId;
            private long authBits = -1L;
            private byte[] authBuffer = null;
            private String controlRole = "";
            private int audioCategory = 0;
            private boolean autoCreateRoom = true;
            private int videoRecvMode = 0;
            private boolean autoRotateVideo = false;
            private boolean enableMic = false;
            private boolean enableSpeaker = false;
            private boolean enableHdAudio = false;

            public Builder(int var1) {
                this.relationId = var1;
            }

            public AVRoomMulti.EnterParam.Builder auth(long var1, byte[] var3) {
                this.authBits = var1;
                this.authBuffer = var3;
                return this;
            }

            public AVRoomMulti.EnterParam.Builder avControlRole(String var1) {
                this.controlRole = var1;
                return this;
            }

            public AVRoomMulti.EnterParam.Builder audioCategory(int var1) {
                this.audioCategory = var1;
                return this;
            }

            public AVRoomMulti.EnterParam.Builder autoCreateRoom(boolean var1) {
                this.autoCreateRoom = var1;
                return this;
            }

            public AVRoomMulti.EnterParam.Builder videoRecvMode(int var1) {
                this.videoRecvMode = var1;
                return this;
            }

            public AVRoomMulti.EnterParam.Builder isDegreeFixed(boolean var1) {
                this.autoRotateVideo = var1;
                return this;
            }

            public AVRoomMulti.EnterParam.Builder isEnableMic(boolean var1) {
                this.enableMic = var1;
                return this;
            }

            public AVRoomMulti.EnterParam.Builder isEnableSpeaker(boolean var1) {
                this.enableSpeaker = var1;
                return this;
            }

            public AVRoomMulti.EnterParam.Builder isEnableHdAudio(boolean var1) {
                this.enableHdAudio = var1;
                return this;
            }

            public AVRoomMulti.EnterParam build() {
                return new AVRoomMulti.EnterParam(this);
            }
        }
    }
//
//    public interface ChangeAVControlRoleCompleteCallback {
//        void OnComplete(int var1);
//    }
//
//    public static class ChangeAuthorityCallback {
//        static final String TAG = "SdkJni";
//
//        public ChangeAuthorityCallback() {
//        }
//
//        protected void onChangeAuthority(int var1) {
//        }
//    }
}
