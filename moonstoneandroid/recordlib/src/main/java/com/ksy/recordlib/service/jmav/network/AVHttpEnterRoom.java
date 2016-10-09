package com.ksy.recordlib.service.jmav.network;

import android.util.Log;

import com.ksy.recordlib.service.jmav.AVEndpoint;
import com.ksy.recordlib.service.jmav.AVError;
import com.ksy.recordlib.service.jmav.AVRoomMulti;
import com.ksy.recordlib.service.jmav.util.AVConstants;
import com.ksy.recordlib.service.jmav.util.Assertions;
import com.ksy.recordlib.service.recoder.RtmpContainer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by huping on 2016/9/27.
 */
public class AVHttpEnterRoom {
    private static final String TAG = "RTMP";

    /* 持有私有静态实例，防止被引用，此处赋值为null，目的是实现延迟加载 */
    private static AVHttpEnterRoom instance = null;

    private AVHttpEnterRoom() {}

    public static AVHttpEnterRoom getInstance() {
        if (instance == null) {
            instance = new AVHttpEnterRoom();
        }
        return instance;
    }

    public int enterRoom(AVRoomMulti.EnterParam param, ArrayList<AVEndpoint> endpointArray) {
        int ret = AVError.AV_OK;

        RtmpContainer<String> input = new RtmpContainer<>();
        if ((ret = write(param, input)) != AVError.AV_OK) {
            return ret;
        }

        RtmpContainer<String> output = new RtmpContainer<>();
        if ((ret = AVHttpClient.post(AVConstants.HTTP_SERVER_URL, input.getObject(), output)) != AVError.AV_OK) {
            return ret;
        }

        if ((ret = read(output.getObject(), endpointArray)) != AVError.AV_OK) {
            return ret;
        }

        return ret;
    }


    private int read(String jsonData, ArrayList<AVEndpoint> endpointArray) {
        int ret = AVError.AV_OK;

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(jsonData);
        } catch (JSONException e) {
            e.printStackTrace();
            return AVError.AV_ERR_JSON;
        }

        JSONArray endpointsJson = null;
        try {
            endpointsJson = jsonObject.getJSONArray("endpoints");
        } catch (JSONException e) {
            e.printStackTrace();
            return AVError.AV_ERR_JSON;
        }

        for (int i = 0; i < endpointsJson.length(); i++) {
            AVEndpoint endpoint = new AVEndpoint();
            try {
                endpoint.user_id = endpointsJson.getJSONObject(i).getString("userId");
                endpoint.is_audio = endpointsJson.getJSONObject(i).getBoolean("is_audio");
                endpoint.is_camera_video = endpointsJson.getJSONObject(i).getBoolean("is_camera_video");
                endpoint.is_screen_video = endpointsJson.getJSONObject(i).getBoolean("is_screen_video");
                endpoint.last_audio_stamp_recv = endpointsJson.getJSONObject(i).getLong("last_audio_stamp_recv");
                endpoint.last_audio_stamp_send = endpointsJson.getJSONObject(i).getLong("last_audio_stamp_send");
                endpoint.last_video_stamp_recv = endpointsJson.getJSONObject(i).getLong("last_video_stamp_recv");
                endpoint.last_video_stamp_send = endpointsJson.getJSONObject(i).getLong("last_video_stamp_send");
                endpoint.stream_role = endpointsJson.getJSONObject(i).getString("stream_role");
                endpoint.stream_address = endpointsJson.getJSONObject(i).getString("stream_address");
            } catch (JSONException e) {
                e.printStackTrace();
                return AVError.AV_ERR_JSON;
            }

            endpointArray.add(endpoint);
        }

        return ret;
    }

    private int write(AVRoomMulti.EnterParam param, RtmpContainer<String> buf) {
        int ret = AVError.AV_OK;

        Assertions.checkArgument(param != null);

        JSONObject obj = new JSONObject();
        try {
            obj.put("relationId", param.getRelationId());
            obj.put("authBits", param.getAuthBits());
            obj.put("controlRole", param.getControlRole());
            obj.put("audioCategory", param.getAudioCategory());
            obj.put("createRoom", param.isCreateRoom());
        } catch (JSONException e) {
            e.printStackTrace();
            return AVError.AV_ERR_JSON;
        }

        buf.setObject(obj.toString());

        Log.v(TAG, "write  JSONObject success.");

        return ret;
    }
}
