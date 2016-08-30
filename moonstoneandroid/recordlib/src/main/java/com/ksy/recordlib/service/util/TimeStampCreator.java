package com.ksy.recordlib.service.util;

import android.util.Log;

/**
 * Created by gezhaoyou on 16/5/28.
 */
public class TimeStampCreator {

    private long baseRealTime;
    private long lastRealTime;
    private static TimeStampCreator timeStampCreator;

    private TimeStampCreator() {
        baseRealTime = System.currentTimeMillis();
        lastRealTime = baseRealTime;
    }

    public static TimeStampCreator getTimeStampCreator() {
        if (timeStampCreator == null) {
            timeStampCreator = new TimeStampCreator();
        }
        return timeStampCreator;
    }

    public int createAbsoluteTimestamp() {
        lastRealTime = System.currentTimeMillis();
        int timestamp = (int)(lastRealTime - baseRealTime);
//        Log.d(Constants.LOG_TAG, "timestamp: " + timestamp);
        return timestamp;
    }

    public int createTimestampDetal() {
        long currentRealTime = System.currentTimeMillis();
        long timestampDetal = currentRealTime - lastRealTime;
        lastRealTime = currentRealTime;
        return (int)timestampDetal;
    }

    public static void release() {
        timeStampCreator = null;
    }
}
