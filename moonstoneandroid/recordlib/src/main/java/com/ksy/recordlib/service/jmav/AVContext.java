package com.ksy.recordlib.service.jmav;

import android.content.Context;

/**
 * Created by huping on 2016/9/23.
 */
public abstract class AVContext {

    /* 持有私有静态实例，防止被引用，此处赋值为null，目的是实现延迟加载 */
    private static AVContext instance = null;

    protected AVContext() {
    }

    public static AVContext createInstance(Context var0) {
        if (instance == null) {
            instance = new AVContextImpl(var0);
        }

        return instance;
    }

    public static AVContext createInstance(Context var0, boolean var1) {
        if (instance == null) {
            instance = new AVContextImpl(var0);
        }

        return instance;
    }

    public abstract int start(AVContext.StartParam var1, AVCallback var2);

    public abstract int enterRoom(AVRoomMulti.EventListener var1, AVRoomMulti.EnterParam var2);

    public abstract void setMagicView(AVImageView view);

    public abstract AVAudioCtrl getAudioCtrl();

    public abstract AVVideoCtrl getVideoCtrl();

    public abstract int exitRoom();

    public abstract int stop();
//
    public abstract void destroy();



//    public static int getSoExtractError() {
//        return AVContextImpl.getSoExtractError();
//    }

//        public static String getVersion() {
//        return AVContextImpl.getVersion();
//    }

//
//    public abstract AVRoomMulti getRoom();
//
//
//
//    public abstract void setSpearEngineMode(int var1);
//
////    public abstract AVWebCloudSpearEngineCtrl getWebCloudSpearEngineCtrl();
////
////    public abstract AVCustomSpearEngineCtrl getCustomSpearEngineCtrl();
//
//    public abstract void setRenderMgrAndHolder(GraphicRendererMgr var1, SurfaceHolder var2);


//    public static class StartParam {
//        public int sdkAppId = 0;
//        public String accountType = "";
//        public String appIdAt3rd = "";
//        public String identifier = "";
//
//        public StartParam() {
//        }
//    }

    public static class StartParam {
        public String device_type; //硬件类型
        public String os; //系统版本
        public String app_version; //应用信息
        public String sdk_version; //使用的sdk版本
        public String user_id; //用户唯一标识
    }

    public interface AVCallback {
        void onComplete(int var1, String var2);
    }
}
