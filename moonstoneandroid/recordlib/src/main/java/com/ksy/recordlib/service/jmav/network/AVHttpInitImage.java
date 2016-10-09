package com.ksy.recordlib.service.jmav.network;

import android.util.Log;

import com.ksy.recordlib.service.jmav.AVContext;
import com.ksy.recordlib.service.jmav.AVError;
import com.ksy.recordlib.service.jmav.media.AVImageConfig;
import com.ksy.recordlib.service.jmav.util.AVConstants;
import com.ksy.recordlib.service.jmav.util.Assertions;
import com.ksy.recordlib.service.recoder.RtmpContainer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by huping on 2016/9/26.
 */
public class AVHttpInitImage {

    private static final String TAG = "RTMP";

    /* 持有私有静态实例，防止被引用，此处赋值为null，目的是实现延迟加载 */
    private static AVHttpInitImage instance = null;

    private AVHttpInitImage() {}

    public static AVHttpInitImage getInstance() {
        if (instance == null) {
            instance = new AVHttpInitImage();
        }
        return instance;
    }

    public int getAVConfiguration(AVContext.StartParam param, RtmpContainer<AVImageConfig> configContainer) {
        int ret = AVError.AV_OK;

        RtmpContainer<String> input = new RtmpContainer<>();
        if ((ret = write(param, input)) != AVError.AV_OK) {
            return ret;
        }

        RtmpContainer<String> output = new RtmpContainer<>();
        if ((ret = AVHttpClient.post(AVConstants.HTTP_SERVER_URL, input.getObject(), output)) != AVError.AV_OK) {
            return ret;
        }

        if ((ret = read(output.getObject(), configContainer)) != AVError.AV_OK) {
            return ret;
        }

        return ret;
    }


    private int read(String jsonData, RtmpContainer<AVImageConfig> configContainer) {
        int ret = AVError.AV_OK;

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(jsonData);
        } catch (JSONException e) {
            e.printStackTrace();
            return AVError.AV_ERR_JSON;
        }

        AVImageConfig config = new AVImageConfig();

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

            config.encoderConfigArray.add(encoderConfig);
        }

        configContainer.setObject(config);

        return ret;
    }

    private int write(AVContext.StartParam param, RtmpContainer<String> buf) {
        int ret = AVError.AV_OK;

        Assertions.checkArgument(param != null);

        JSONObject obj = new JSONObject();
        try {
            obj.put("device_type", param.device_type);
            obj.put("os", param.os);
            obj.put("app_version", param.app_version);
            obj.put("sdk_version", param.sdk_version);
            obj.put("userId", param.user_id);
        } catch (JSONException e) {
            e.printStackTrace();
            return AVError.AV_ERR_JSON;
        }

        buf.setObject(obj.toString());

        Log.v(TAG, "write  JSONObject success.");

        return ret;
    }
}
