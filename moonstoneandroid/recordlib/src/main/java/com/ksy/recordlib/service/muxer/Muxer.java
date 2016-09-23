package com.ksy.recordlib.service.muxer;

import android.media.MediaCodec;
import android.media.MediaFormat;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by Administrator on 2016/8/9.
 */
public interface Muxer {

    // start to the remote SRS for remux.
    void start() throws IOException;
    // stop the muxer, stopRtmp HTTP or Rtmp connection from SRS.
    void stop();
    // release any resources
    void release();

    // send the annexb frame to media server(for example:SRS) over HTTP FLV Or Rtmp.
    void writeSampleData(int trackIndex, ByteBuffer byteBuf, MediaCodec.BufferInfo bufferInfo) throws Exception;

    // Adds a track with the specified format.
//    int addTrack(MediaFormat format);
}
