package com.ksy.recordlib.service.jmav.media;

import java.util.ArrayList;

/**
 * Created by huping on 2016/9/26.
 */
public class AVImageConfig {
    public ArrayList<AVCodecConfig> encoderConfigArray = new ArrayList<>();


    public static class AVCodecConfig {
        public String name;
        public int encoding_bit_rate = 900;
        public int v_resolution_w = 640;
        public int v_resolution_h = 480;
        public int v_fps = 20;
        public int v_gop = 2;
        public int a_sampling_rate = 44100;
    }
}
