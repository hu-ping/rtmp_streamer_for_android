package com.ksy.recordlib.service.recoder;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.EGLContext;
import android.util.Log;

import com.ksy.recordlib.service.core.KsyRecordClient;
import com.ksy.recordlib.service.core.SMRecordClientConfig;
import com.ksy.recordlib.service.magicfilter.encoder.gles.EglCore;
import com.ksy.recordlib.service.magicfilter.encoder.video.WindowSurface;
import com.ksy.recordlib.service.magicfilter.filter.base.MagicCameraInputFilter;
import com.ksy.recordlib.service.magicfilter.filter.base.gpuimage.GPUImageFilter;
import com.ksy.recordlib.service.util.Constants;

import java.nio.FloatBuffer;

/**
 * Created by huping on 2016/8/31.
 */
public class SMTextureVideoEncoder implements IVideoEncoder{

    /**
     * MediaCodec module
     */
    private final KsyRecordClient.RecordHandler mainHandler;
    private final Context mContext;
    private SMRecordClientConfig mVideoConfig;
    private SMTextureVideoEncoderCore videoEncoderCore = null;
//    private VideoEncoderCore videoEncoderCore = null;

    /**
     * Filter module
     */
    private EGLContext sharedEglContext = null;
    private WindowSurface mInputWindowSurface;
    private EglCore mEglCore;
    private MagicCameraInputFilter mInput;
    private int mTextureId;
    private GPUImageFilter filter;
    private FloatBuffer gLCubeBuffer;
    private FloatBuffer gLTextureBuffer;

    /**
     * Threader module
     */
    private Thread worker = null;

    private static final int MSG_START_RECORDING = 0;
    private static final int MSG_STOP_RECORDING = 1;
    private static final int MSG_FRAME_AVAILABLE = 2;
    private static final int MSG_SET_TEXTURE_ID = 3;
    private static final int MSG_UPDATE_SHARED_CONTEXT = 4;
    private static final int MSG_QUIT = 5;

    // packet to process.
    private RtmpBlockingQueue<RtmpMessage> queue = new RtmpBlockingQueue(1);

    public SMTextureVideoEncoder(SMRecordClientConfig videoConfig, EGLContext sharedEglContext,
                                 KsyRecordClient.RecordHandler mRecordHandler, Context mContext) {
        this.mVideoConfig = videoConfig;
        this.sharedEglContext = sharedEglContext;
        this.mainHandler = mRecordHandler;
        this.mContext = mContext;

        this.videoEncoderCore = new SMTextureVideoEncoderCore(mVideoConfig, null);

//        try {
//            this.videoEncoderCore = new VideoEncoderCore(640, 480,500, new File(MagicParams.videoPath,MagicParams.videoName));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }


    @Override
    public void initialize() {
    }

    @Override
    public void start() {

        worker = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    cycle();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        worker.start();

        startRecording();
    }

    private int cycle() throws Exception {
        int ret = RtmpStdin.ERROR_SUCCESS;

        for (;;) {
            RtmpContainer<RtmpMessage> ppkt = new RtmpContainer<>();
            if ((ret = queue.dequeue(ppkt)) != RtmpStdin.ERROR_SUCCESS) {
                // timeout, return to close session.
                //return ret;
                break;
            }

            RtmpMessage message = ppkt.getObject();
            handleMessage(message);

            if(message.what == MSG_STOP_RECORDING) {
                break;
            }
        }

        return ret;
    }

    private int feedRtmpMessage(RtmpMessage message) {
        return queue.enqueue(message);
    }

    private int feedRtmpMessage(int what) {
        RtmpMessage message = new RtmpMessage();
        message.what = what;

        return feedRtmpMessage(message);
    }

    private int feedRtmpMessage(int what, Object obj) {
        RtmpMessage message = new RtmpMessage();
        message.what = what;
        message.obj = obj;

        return feedRtmpMessage(message);
    }

    private int feedRtmpMessage(int what, int arg1, int arg2, Object obj) {
        RtmpMessage message = new RtmpMessage();
        message.what = what;
        message.arg1 = arg1;
        message.arg2 = arg2;
        message.obj = obj;

        message.startTime = System.currentTimeMillis();

        return feedRtmpMessage(message);
    }

    private int feedRtmpMessage(int what, int arg1, int arg2, int arg3,  Object obj) {
        RtmpMessage message = new RtmpMessage();
        message.what = what;
        message.arg1 = arg1;
        message.arg2 = arg2;
        message.arg3 = arg3;
        message.obj = obj;

        message.startTime = System.currentTimeMillis();

        return feedRtmpMessage(message);
    }

    /**
     * Tells the video recorder to start recording.  (Call from non-encoder thread.)
     * <p>
     * Creates a new thread, which will create an encoder using the provided configuration.
     * <p>
     * Returns after the recorder thread has started and is ready to accept Messages.  The
     * encoder may not yet be fully configured.
     */
    private void startRecording() {
        Log.d(Constants.LOG_TAG, "Encoder: startRecording()");

//        handleStartRecording();

        feedRtmpMessage(MSG_START_RECORDING);
    }
    /**
     * Tells the video recorder to stop recording.  (Call from non-encoder thread.)
     * <p>
     * Returns immediately; the encoder/muxer may not yet be finished creating the movie.
     * <p>
     * TODO: have the encoder thread invoke a callback on the UI thread just before it shuts down
     * so we can provide reasonable status UI (and let the caller know that movie encoding
     * has completed).
     */
    public void stopRecording() {
        Log.e(Constants.LOG_TAG, "stopRecording");
        Log.e(Constants.LOG_TAG, "" + queue.count());

        feedRtmpMessage(MSG_STOP_RECORDING);
        feedRtmpMessage(MSG_QUIT);
        // We don't know when these will actually finish (or even start).  We don't want to
        // delay the UI thread though, so we return immediately.
    }

    /**
     * Tells the video recorder that a new frame is available.  (Call from non-encoder thread.)
     * <p>
     * This function sends a message and returns immediately.  This isn't sufficient -- we
     * don't want the caller to latch a new frame until we're done with this one -- but we
     * can get away with it so long as the input frame rate is reasonable and the encoder
     * thread doesn't stall.
     * <p>
     * TODO: either block here until the texture has been rendered onto the encoder surface,
     * or have a separate "block if still busy" method that the caller can execute immediately
     * before it calls updateTexImage().  The latter is preferred because we don't want to
     * stall the caller while this thread does work.
     */
    public void frameAvailable(int id, SurfaceTexture st) {
        float[] transform = new float[16];      // TODO - avoid alloc every frame
        st.getTransformMatrix(transform);
        long timestamp = st.getTimestamp();
        if (timestamp == 0) {
            // Seeing this after device is toggled off/on with power button.  The
            // first frame back has a zero timestamp.
            //
            // MPEG4Writer thinks this is cause to abort() in native code, so it's very
            // important that we just ignore the frame.
            Log.w(Constants.LOG_TAG, "HEY: got SurfaceTexture with timestamp of zero");
            return;
        }


//        handleFrameAvailable(transform, timestamp, System.currentTimeMillis());

        feedRtmpMessage(MSG_FRAME_AVAILABLE, (int) (timestamp >> 32), (int) timestamp, id, transform);
    }

    /**
     * Tells the video recorder what texture name to use.  This is the external texture that
     * we're receiving camera previews in.  (Call from non-encoder thread.)
     * <p>
     * TODO: do something less clumsy
     */
    public void setTextureId(int id) {
//        handleSetTexture(id);

        feedRtmpMessage(MSG_SET_TEXTURE_ID, id, 0, null);
    }

    /**
     * Tells the video recorder to refresh its EGL surface.  (Call from non-encoder thread.)
     */
    public void updateSharedContext(EGLContext sharedContext) {
//        handleUpdateSharedContext(sharedContext);

        feedRtmpMessage(MSG_UPDATE_SHARED_CONTEXT, sharedContext);
    }

    private void handleMessage(RtmpMessage msg) {
        int what = msg.what;
        Object obj = msg.obj;

        switch (what) {
            case MSG_START_RECORDING:
                handleStartRecording();
                break;
            case MSG_STOP_RECORDING:
                handleStopRecording();
                break;
            case MSG_FRAME_AVAILABLE:
                long timestamp = (((long) msg.arg1) << 32) |
                        (((long) msg.arg2) & 0xffffffffL);
                int id = msg.arg3;
                handleFrameAvailable((float[]) obj, timestamp, id,  msg.startTime);
                break;
            case MSG_SET_TEXTURE_ID:
                handleSetTexture(msg.arg1);
                break;
            case MSG_UPDATE_SHARED_CONTEXT:
                handleUpdateSharedContext((EGLContext) msg.obj);
                break;
            case MSG_QUIT:
//                Looper.myLooper().quit();
                break;
            default:
                throw new RuntimeException("Unhandled msg what=" + what);
        }

    }


    /**
     * Starts recording.
     */
    private void handleStartRecording() {
        Log.d(Constants.LOG_TAG, "handleStartRecording");

        videoEncoderCore.initialize();
        videoEncoderCore.start();

//        mainHandler.sendEmptyMessage(Constants.MESSAGE_SWITCH_CAMERA_FINISH);

        mEglCore = new EglCore(sharedEglContext, EglCore.FLAG_RECORDABLE);
        mInputWindowSurface = new WindowSurface(mEglCore, videoEncoderCore.getInputSurface(), true);
        mInputWindowSurface.makeCurrent();

        mInput = new MagicCameraInputFilter();
        mInput.init();
        mInput.onBeautyLevelChanged();

//        filter = MagicFilterFactory.initFilters(type);
//        if(filter != null){
//            filter.init();
//            filter.onInputSizeChanged(mPreviewWidth, mPreviewHeight);
//            filter.onDisplaySizeChanged(mVideoWidth, mVideoHeight);
//        }
    }

    /**
     * Handles a request to stop encoding.
     */
    private void handleStopRecording() {
        Log.d(Constants.LOG_TAG, "handleStopRecording");
//        mVideoEncoder.drainEncoder(true);
        releaseEncoder();
    }

    /**
     * Sets the texture name that SurfaceTexture will use when frames are received.
     */
    private void handleSetTexture(int id) {
        //Log.d(TAG, "handleSetTexture " + id);
        mTextureId = id;
    }


    /**
     * Handles notification of an available frame.
     * <p>
     * The texture is rendered onto the encoder's input surface, along with a moving
     * box (just because we can).
     * <p>
     * @param transform The texture transform, from SurfaceTexture.
     * @param timestampNanos The frame's timestamp, from SurfaceTexture.
     */
    private void handleFrameAvailable(float[] transform, long timestampNanos, int id, long startTime) {
//        if (VERBOSE) Log.d(TAG, "handleFrameAvailable tr=" + transform);

//        long endTime = System.currentTimeMillis();
//        Log.i(Constants.LOG_TAG, "" + startTime);
//        Log.i(Constants.LOG_TAG, "" + endTime);
//        Log.i(Constants.LOG_TAG, "dequeue executed time, ms:" + (endTime - startTime) + ", queue size:" + queue.count());

        mTextureId = id;

        videoEncoderCore.drainEncoder(false);

        mInput.setTextureTransformMatrix(transform);
        if(filter == null) {
            mInput.onDrawFrame(mTextureId, gLCubeBuffer, gLTextureBuffer);
        }else {
            filter.onDrawFrame(mTextureId, gLCubeBuffer, gLTextureBuffer);
        }

        mInputWindowSurface.setPresentationTime(timestampNanos);
        mInputWindowSurface.swapBuffers();


//        long endDrawTime = System.currentTimeMillis();
//        Log.i(Constants.LOG_TAG, "draw executed time, ms: " + (endDrawTime - endTime));
    }

    /**
     * Tears down the EGL surface and context we've been using to feed the MediaCodec input
     * surface, and replaces it with a new one that shares with the new context.
     * <p>
     * This is useful if the old context we were sharing with went away (maybe a GLSurfaceView
     * that got torn down) and we need to hook up with the new one.
     */
    private void handleUpdateSharedContext(EGLContext newSharedContext) {
        Log.d(Constants.LOG_TAG, "handleUpdatedSharedContext " + newSharedContext);

        // Release the EGLSurface and EGLContext.
        mInputWindowSurface.releaseEglSurface();
        mInput.destroy();
        mEglCore.release();

        // Create a new EGLContext and recreate the window surface.
        mEglCore = new EglCore(newSharedContext, EglCore.FLAG_RECORDABLE);
        mInputWindowSurface.recreate(mEglCore);
        mInputWindowSurface.makeCurrent();

        // Create new programs and such for the new context.
        mInput = new MagicCameraInputFilter();
        mInput.init();
//        mInput.onBeautyLevelChanged();

//        filter = MagicFilterFactory.initFilters(type);
//        if(filter != null){
//            filter.init();
//            filter.onInputSizeChanged(mPreviewWidth, mPreviewHeight);
//            filter.onDisplaySizeChanged(mVideoWidth, mVideoHeight);
//        }
    }

    public void setTextureBuffer(FloatBuffer gLTextureBuffer) {
        this.gLTextureBuffer = gLTextureBuffer;
    }

    public void setCubeBuffer(FloatBuffer gLCubeBuffer) {
        this.gLCubeBuffer = gLCubeBuffer;
    }

    private void releaseEncoder() {
       videoEncoderCore.release();

        if (mInputWindowSurface != null) {
            mInputWindowSurface.release();
            mInputWindowSurface = null;
        }
        if (mInput != null) {
            mInput.destroy();
            mInput = null;
        }
        if (mEglCore != null) {
            mEglCore.release();
            mEglCore = null;
        }
        if(filter != null){
            filter.destroy();
            filter = null;
//            type = MagicFilterType.NONE;
        }
    }

    @Override
    public void stop() {
        stopRecording();
    }

    @Override
    public void release() {
        stopRecording();
    }

    @Override
    public Object feedRawData() {
        return null;
    }

}
