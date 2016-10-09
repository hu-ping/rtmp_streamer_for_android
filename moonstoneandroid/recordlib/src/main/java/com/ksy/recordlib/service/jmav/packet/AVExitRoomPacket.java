package com.ksy.recordlib.service.jmav.packet;

import android.util.Log;


import com.ksy.recordlib.service.jmav.AVError;
import com.ksy.recordlib.service.jmav.util.Assertions;
import com.ksy.recordlib.service.recoder.RtmpContainer;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by huping on 2016/9/28.
 */
public class AVExitRoomPacket extends AVHttpPacket{
    public String mUserId; //用户唯一标识
    public int mRoomId = -1; //房间唯一标识

    @Override
    protected int write(RtmpContainer<String> input) {
        int ret = AVError.AV_OK;

        Assertions.checkArgument(mUserId != null);

        JSONObject obj = new JSONObject();
        try {
            obj.put("user_id", mUserId);
            obj.put("room_id", mRoomId);
        } catch (JSONException e) {
            e.printStackTrace();
            return AVError.AV_ERR_JSON;
        }

        input.setObject(obj.toString());

        Log.v(TAG, "write  JSONObject success.");

        return ret;
    }

    @Override
    protected int read(String output) {
        return AVError.AV_OK;
    }

}
