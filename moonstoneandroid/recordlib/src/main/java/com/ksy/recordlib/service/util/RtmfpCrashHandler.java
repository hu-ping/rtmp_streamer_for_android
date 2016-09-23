package com.ksy.recordlib.service.util;

import android.content.Context;
import android.util.Log;

/**
 * Created by 1 on 2016/4/29.
 */
public class RtmfpCrashHandler implements Thread.UncaughtExceptionHandler {
    private static final String TAG = "RTMFP";
    private static RtmfpCrashHandler instance;  //单例引用，这里我们做成单例的，因为我们一个应用程序里面只需要一个UncaughtExceptionHandler实例

    private RtmfpCrashHandler(){}

    public synchronized static RtmfpCrashHandler getInstance(){  //同步方法，以免单例多线程环境下出现异常
        if (instance == null){
            instance = new RtmfpCrashHandler();
        }
        return instance;
    }

    public void init(Context ctx){  //初始化，把当前对象设置成UncaughtExceptionHandler处理器
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {  //当有未处理的异常发生时，就会来到这里。。
        ex.printStackTrace();
        Log.e(TAG, "uncaughtException, thread: " + thread
                + " name: " + thread.getName() + " id: "
                + thread.getId() + "exception: " + ex);
    }
}
