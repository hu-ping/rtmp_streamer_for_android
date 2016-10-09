package com.ksy.recordlib.service.jmav.packet;

import android.util.Log;


import com.ksy.recordlib.service.jmav.AVContext;
import com.ksy.recordlib.service.jmav.AVError;
import com.ksy.recordlib.service.jmav.media.AVImageConfig;
import com.ksy.recordlib.service.jmav.util.Assertions;
import com.ksy.recordlib.service.recoder.RtmpContainer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by huping on 2016/9/28.
 */
public class AVInitImagePacket extends AVHttpPacket{
    private static final String TAG = "RTMP";

    public AVContext.StartParam mStartParam;
    public AVImageConfig mImageConfig;

    @Override
    protected int write(RtmpContainer<String> input) {
        int ret = AVError.AV_OK;

        Assertions.checkArgument(mStartParam != null);

        JSONObject obj = new JSONObject();
        try {
            obj.put("device_type", mStartParam.device_type);
            obj.put("os", mStartParam.os);
            obj.put("app_version", mStartParam.app_version);
            obj.put("sdk_version", mStartParam.sdk_version);
            obj.put("userId", mStartParam.user_id);
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
        int ret = AVError.AV_OK;

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(output);
        } catch (JSONException e) {
            e.printStackTrace();
            return AVError.AV_ERR_JSON;
        }

        JSONArray encoderJson = null;
        try {
            encoderJson = jsonObject.getJSONArray("role_list");
        } catch (JSONException e) {
            e.printStackTrace();
            return AVError.AV_ERR_JSON;
        }

        for (int i = 0; i < encoderJson.length(); i++) {
            AVImageConfig.AVCodecConfig encoderConfig = new AVImageConfig.AVCodecConfig();
            try {
                encoderConfig.name = encoderJson.getJSONObject(i).getString("name");
                encoderConfig.encoding_bit_rate = encoderJson.getJSONObject(i).getInt("encoding_bit_rate");
                encoderConfig.v_resolution_w = encoderJson.getJSONObject(i).getInt("v_resolution_w");
                encoderConfig.v_resolution_h = encoderJson.getJSONObject(i).getInt("v_resolution_h");
                encoderConfig.v_fps = encoderJson.getJSONObject(i).getInt("v_fps");
                encoderConfig.v_gop = encoderJson.getJSONObject(i).getInt("v_gop");
                encoderConfig.a_sampling_rate = encoderJson.getJSONObject(i).getInt("a_sampling_rate");
            } catch (JSONException e) {
                e.printStackTrace();
                return AVError.AV_ERR_JSON;
            }

            mImageConfig.encoderConfigArray.add(encoderConfig);
        }

        return ret;

    }

}
