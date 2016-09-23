package com.ksy.recordlib.service.recoder;

import android.util.Log;

import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by 1 on 2016/2/22.
 */
public class RtmpQueue<T> {

    private static final String TAG = "RTMP";

    // TODO: 2016/5/13 选择使用LinkedList还是ArrayList，需要通过在此情况下测试他们的性能来决定。
    private LinkedList<T> packets  = new LinkedList<>();

    public int enqueue(T packet) {
        synchronized (this) {
            packets.add(packet);
            //Log.i(TAG, String.format("RtmpQueue: enqueue packet, queue size %d", (int) packets.size()));
            notify();
        }

        return RtmpStdin.ERROR_SUCCESS;
    }


    public int dequeue(long timeoutMs, RtmpObj<T> ppkt) {
        int ret = RtmpStdin.ERROR_SUCCESS;

        synchronized (this) {
            try {
                if (packets.size() == 0) {
                    // @see st-1.9/docs/reference.html#cond_wait
                    // @see st-1.9/docs/reference.html#cond_timedwait
                    try {
                        wait(timeoutMs);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        ret = RtmpStdin.ERROR_ST_COND_TIMEOUT;
                        //Log.e(TAG, String.format("cond wait timeout, timeout =%s, ret=%d",
                        //        timeoutMs , ret));
                        return ret;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if(packets.size() > 0) {
                ppkt.setObject(packets.remove(0));
            } else {
                ret = RtmpStdin.ERROR_ST_COND_TIMEOUT;
                Log.e(TAG, String.format("cond wait timeout, timeout ms=%s, ret=%d", timeoutMs, ret));
//                StackTraceElement st[]= Thread.currentThread().getStackTrace();
//                for(int i=0;i<st.length;i++) {
//                    Log.e(TAG, i + ":" + st[i]);
//                }
            }
        }

        return ret;
    }

    public int dequeue(RtmpObj<T> ppkt) {
        int ret = RtmpStdin.ERROR_SUCCESS;

        synchronized (this) {
            try {
                while (packets.size() == 0) {
                    // @see st-1.9/docs/reference.html#cond_wait
                    // @see st-1.9/docs/reference.html#cond_timedwait
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        ret = RtmpStdin.ERROR_ST_COND_TIMEOUT;
                        Log.e(TAG, String.format("cond wait timeout ret=%d", ret));
                        return ret;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            ppkt.setObject(packets.remove(0));
        }

        return ret;
    }

    public int count() {
        synchronized (this) {
            return packets.size();
        }
    }

    public void clear() {
        packets.clear();
    }

    public boolean got_packet(long timeout)
    {
        if (packets.size() == 0) {
            int code = 0;
            try {
                //The time to sleep in milliseconds.
                 Thread.sleep(timeout);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.i(TAG, String.format("ignore queue test wait, code=%d, timeout=%d", code, timeout));
            }
        }

        return packets.size() > 0;
    }
}
