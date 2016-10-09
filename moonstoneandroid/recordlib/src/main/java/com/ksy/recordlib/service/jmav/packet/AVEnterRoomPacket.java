package com.ksy.recordlib.service.jmav.packet;

import android.util.Log;

import com.ksy.recordlib.service.jmav.AVEndpoint;
import com.ksy.recordlib.service.jmav.AVError;
import com.ksy.recordlib.service.jmav.AVRoomMulti;
import com.ksy.recordlib.service.jmav.util.Assertions;
import com.ksy.recordlib.service.recoder.RtmpContainer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by huping on 2016/9/28.
 */
public class AVEnterRoomPacket extends AVHttpPacket{
    private static final String TAG = "RTMP";

    public AVRoomMulti.EnterParam mEnterParam;
    public ArrayList<AVEndpoint> mEndpointArray = new ArrayList<>();

    @Override
    protected int write(RtmpContainer<String> input) {
        int ret = AVError.AV_OK;

        Assertions.checkArgument(mEnterParam != null);

        JSONObject obj = new JSONObject();
        try {
            obj.put("relationId", mEnterParam.getRelationId());
            obj.put("authBits", mEnterParam.getAuthBits());
            obj.put("controlRole", mEnterParam.getControlRole());
            obj.put("audioCategory", mEnterParam.getAudioCategory());
            obj.put("createRoom", mEnterParam.isCreateRoom());
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

            mEndpointArray.add(endpoint);
        }

        return ret;
    }
}
