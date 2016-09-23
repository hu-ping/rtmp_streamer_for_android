package com.ksy.recordlib.service.recoder;

/**
 * Created by huping on 2016/8/31.
 */
public interface IVideoEncoder {
    void initialize();
    void start();
    void stop();
    void release();
    Object feedRawData();
}
