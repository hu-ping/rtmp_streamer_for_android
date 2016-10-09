package com.ksy.recordlib.service.recoder;

import android.util.Log;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by huping on 2016/9/6.
 */
public class RtmpBlockingQueue<T> {

    private static final String TAG = "RTMP";
    private final int capacity;

    // TODO: 2016/5/13 选择使用LinkedList还是ArrayList，需要通过在此情况下测试他们的性能来决定。
    private ArrayBlockingQueue<T> packets;

    public RtmpBlockingQueue(int capacity) {
        this.capacity = capacity;
        this.packets = new ArrayBlockingQueue<>(capacity);
    }

    public int enqueue(T packet) {
        try {
            packets.put(packet);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //Log.i(TAG, String.format("RtmpQueue: enqueue packet, queue size %d", (int) packets.size()));
        return RtmpStdin.ERROR_SUCCESS;
    }


    public int dequeue(long timeoutMs, RtmpContainer<T> ppkt) {
        int ret = RtmpStdin.ERROR_SUCCESS;

        try {
            ppkt.setObject(packets.take());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return ret;
    }

    public int dequeue(RtmpContainer<T> ppkt) {
        int ret = RtmpStdin.ERROR_SUCCESS;

        try {
            ppkt.setObject(packets.take());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return ret;
    }

    public int count() {
        return packets.size();
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
