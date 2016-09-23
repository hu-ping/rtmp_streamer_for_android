package com.ksy.recordlib.service.magicfilter.utils;

import android.content.Context;
import android.os.Environment;

import com.ksy.recordlib.service.magicfilter.widget.base.MagicBaseView;

/**
 * Created by why8222 on 2016/2/26.
 */
public class MagicParams {
    public static Context context;
    public static MagicBaseView magicBaseView;

    public static String videoPath = Environment.getExternalStorageDirectory().getPath();
    public static String videoName = "MagicCamera_test.mp4";
    public static String h264Name = "MagicCamera_test.h264";

    public static int beautyLevel = 5;

    public MagicParams() {

    }
}
