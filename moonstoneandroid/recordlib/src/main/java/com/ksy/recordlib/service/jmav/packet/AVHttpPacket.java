package com.ksy.recordlib.service.jmav.packet;

import com.ksy.recordlib.service.jmav.AVError;
import com.ksy.recordlib.service.jmav.network.AVHttpClient;
import com.ksy.recordlib.service.jmav.util.AVConstants;
import com.ksy.recordlib.service.recoder.RtmpContainer;

/**
 * Created by huping on 2016/9/28.
 */
public abstract class AVHttpPacket {
    protected static final String TAG = "RTMP";

    public int doHttpConnect() {
        int ret = AVError.AV_OK;

        RtmpContainer<String> input = new RtmpContainer<>();
        if ((ret = write(input)) != AVError.AV_OK) {
            return ret;
        }

        RtmpContainer<String> output = new RtmpContainer<>();
        if ((ret = AVHttpClient.post(AVConstants.HTTP_SERVER_URL, input.getObject(), output)) != AVError.AV_OK) {
            return ret;
        }

        if ((ret = read(output.getObject())) != AVError.AV_OK) {
            return ret;
        }

        return ret;

    }

    protected int write(RtmpContainer<String> input) {
        return AVError.AV_OK;
    }

    protected int read(String output) {
        return AVError.AV_OK;
    }
}
