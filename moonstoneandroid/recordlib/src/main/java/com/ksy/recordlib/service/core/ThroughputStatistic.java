package com.ksy.recordlib.service.core;

import android.util.Log;

import com.ksy.recordlib.service.util.Constants;

/**
 * Created by huping on 2016/8/17.
 */
public class ThroughputStatistic {
    public final static int WEIGHT_LEVEL_MINIMUM = -100;
    public final static int WEIGHT_LEVEL_NEGATIVE_7 = -7;
    public final static int WEIGHT_LEVEL_NEGATIVE_5 = -5;
    public final static int WEIGHT_LEVEL_NEGATIVE_3 = -3;
    public final static int WEIGHT_LEVEL_NEGATIVE_1 = -1;
    public final static int WEIGHT_LEVEL_0 = 0;
    public final static int WEIGHT_LEVEL_1 = 1;
    public final static int WEIGHT_LEVEL_2 = 2;

    public static final int MAX_STATISTIC_SIZE = 3;

    public int LEVEL1_QUEUE_SIZE = 0;
    public int LEVEL2_QUEUE_SIZE = 0;
    public int QUEUE_SIZE = 0;
    public int MAX_QUEUE_SIZE = 0;

    public int[] bufferLengthS = new int[MAX_STATISTIC_SIZE];
    public int[] averageBufferTimeMsS = new int[MAX_STATISTIC_SIZE];
    public int statisticSize = 0;

    public int weight;
    public int rateGrowthAvg;
    public int averageBufferLength;
    public int averageBufferTimeMs;
    public boolean isCompleteMonitor;


    public ThroughputStatistic() {
        this.isCompleteMonitor = false;
        this.weight = WEIGHT_LEVEL_0;
        this.rateGrowthAvg = 0;
    }

    public void calcQueueLevelLimit(KsyRecordClientConfig mConfig) {
        QUEUE_SIZE = mConfig.getVideoFrameRate() * 4;
        LEVEL1_QUEUE_SIZE = QUEUE_SIZE /4;
        LEVEL2_QUEUE_SIZE = QUEUE_SIZE /4 * 3;
        MAX_QUEUE_SIZE = QUEUE_SIZE * 2;
    }

    public void calcWeight() {
        if (statisticSize < 3) {
            weight = 0;
            averageBufferLength = 0;
            return;
        }

        int total = bufferLengthS[0] + bufferLengthS[1] + bufferLengthS[2];
        averageBufferLength = total / MAX_STATISTIC_SIZE;

//        rateGrowthAvg = 0;
//        int prevValue = bufferLengthS[0];
//        for(int i = 1; i < bufferLengthS.length; i++) {
//            rateGrowthAvg += (bufferLengthS[i] > prevValue) ?
//                    -1 : (bufferLengthS[i] < prevValue ? 1 : 0);
//            prevValue = bufferLengthS[i];
//        }

        rateGrowthAvg = 0;
        int prevValue = bufferLengthS[0];
        for (int i = 1; i < bufferLengthS.length; i++) {
            rateGrowthAvg +=  bufferLengthS[i] - prevValue;
            prevValue = bufferLengthS[i];
        }

        weight = 0;
        if (rateGrowthAvg < 0) {
            int abs = Math.abs(rateGrowthAvg);
            if (abs <= LEVEL1_QUEUE_SIZE && averageBufferLength > LEVEL1_QUEUE_SIZE) {
                weight = WEIGHT_LEVEL_NEGATIVE_1;
            } else if (abs > LEVEL1_QUEUE_SIZE  && abs <= LEVEL2_QUEUE_SIZE) {
                weight = WEIGHT_LEVEL_0;
            } else if (abs > LEVEL2_QUEUE_SIZE && abs <= QUEUE_SIZE) {
                weight = WEIGHT_LEVEL_0;
            } else if (abs > QUEUE_SIZE) {
                weight = WEIGHT_LEVEL_1;
            }

        } else if ( rateGrowthAvg > 0) {
            if (bufferLengthS[2] > MAX_QUEUE_SIZE) {
                weight = WEIGHT_LEVEL_MINIMUM;

            } else if (bufferLengthS[2] > QUEUE_SIZE && bufferLengthS[2] <= MAX_QUEUE_SIZE) {
                weight = WEIGHT_LEVEL_NEGATIVE_7;

            } else if(bufferLengthS[2] > LEVEL2_QUEUE_SIZE && bufferLengthS[2] <= QUEUE_SIZE) {
                weight = WEIGHT_LEVEL_NEGATIVE_5;

            } else if (rateGrowthAvg > LEVEL1_QUEUE_SIZE && rateGrowthAvg <= LEVEL2_QUEUE_SIZE) {
                weight = WEIGHT_LEVEL_NEGATIVE_3;

            } else if (rateGrowthAvg > 0 && rateGrowthAvg <= LEVEL1_QUEUE_SIZE) {
                weight = WEIGHT_LEVEL_NEGATIVE_1;
            }
        } else {
            if (averageBufferLength == 0) {
                weight = WEIGHT_LEVEL_1;
            } else {
                weight = WEIGHT_LEVEL_0;
            }
        }

//        if(averageBufferLength > LEVEL2_QUEUE_SIZE) {
//            weight = -2;
//        } else if (averageBufferLength > LEVEL1_QUEUE_SIZE && averageBufferLength <= LEVEL2_QUEUE_SIZE) {
//            weight = -1;
//        } else if (averageBufferLength > 0 && averageBufferLength <= LEVEL1_QUEUE_SIZE) {
//            weight = 0;
//        } else if (averageBufferLength == 0) {
//            weight = 1;
//        }


        Log.i(Constants.LOG_TAG, MAX_QUEUE_SIZE
                + ":" + QUEUE_SIZE
                + ", LEVEL1:" +LEVEL1_QUEUE_SIZE
                + ", LEVEL2:" + LEVEL2_QUEUE_SIZE
                + ", average:" + averageBufferLength
                + ", WEIGHT:" + weight + ", RATEGROWTHAVG:" + rateGrowthAvg);
    }

}
