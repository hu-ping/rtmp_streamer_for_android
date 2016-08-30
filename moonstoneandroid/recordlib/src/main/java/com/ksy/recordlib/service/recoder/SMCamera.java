package com.ksy.recordlib.service.recoder;

import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceView;
import android.view.TextureView;

import com.ksy.recordlib.service.core.KSYFlvData;
import com.ksy.recordlib.service.core.KsyRecord;
import com.ksy.recordlib.service.core.KsyRecordClientConfig;
import com.ksy.recordlib.service.core.KsyRecordSender;
import com.ksy.recordlib.service.util.CameraUtil;
import com.ksy.recordlib.service.util.Constants;
import com.ksy.recordlib.service.util.OnClientErrorListener;
import com.ksy.recordlib.service.view.CameraTextureView;

import java.io.IOException;
import java.util.LinkedList;

/**
 * Created by huping on 2016/8/30.
 */
public class SMCamera implements OnClientErrorListener{

    private Camera mCamera = null;
    private KsyRecordClientConfig mConfig = null;
    private int currentCameraId = 0;
    private int displayOrientation = 0;
    private SurfaceView mSurfaceView;
    private TextureView mTextureView;
    private OnClientErrorListener onClientErrorListener = null;

    private SurfaceTexture surfaceTexture;

    private static SMCamera SMCameraInstance = new SMCamera();

    private SMCamera() {
    }

    public static SMCamera getSMCameraInstance() {
        return SMCameraInstance;
    }

    public boolean initialize(KsyRecordClientConfig Config,  OnClientErrorListener onClientErrorListener) {
        this.mConfig = Config;
        this.onClientErrorListener = onClientErrorListener;

        try {
            if (mCamera == null) {
                int number = Camera.getNumberOfCameras();
                if (number > 0) {
                    Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                    for (int i = 0; i < number; i++) {
                        Camera.getCameraInfo(i, cameraInfo);
                        if (cameraInfo.facing == mConfig.getCameraType()) {
                            mCamera = Camera.open(i);
                            currentCameraId = i;
                        }
                    }
                } else {
                    mCamera = Camera.open();
                }

                if (mCamera == null) {
                    return false;
                }

                displayOrientation = CameraUtil.getDisplayOrientation(0, currentCameraId);
                KsyRecordClientConfig.previewOrientation = displayOrientation;
                Log.d(Constants.LOG_TAG, "current displayOrientation = " + displayOrientation);
                mCamera.setDisplayOrientation(displayOrientation);

//                if (mCameraSizeChangedListener != null)
//                    mCameraSizeChangedListener.onCameraPreviewSize(parameters.getPreviewSize().width,
//                            parameters.getPreviewSize().height);


                Camera.Parameters parameters = mCamera.getParameters();
                parameters.setRotation(0);
                if (parameters.getSupportedFocusModes().contains(
                        Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                }
                // Camera.Size size = findBestPreviewSize(mCamera, (mSurfaceView == null ? mTextureView : mSurfaceView));

                Camera.Size size = mCamera.new Size(mConfig.getVideoWidth(), mConfig.getVideoHeight()) ;
                if (size != null) {
                    parameters.setPreviewSize(size.width, size.height);
                }

                parameters.setPreviewFormat(ImageFormat.YV12);
                mCamera.setParameters(parameters);
            }

            // Here we reuse camera, just unlock it
//            mCamera.unlock();
        } catch (Exception e) {
            onClientError(SOURCE_CLIENT, ERROR_CAMERA_START_FAILED);
            e.printStackTrace();
            Log.e(Constants.LOG_TAG, "" + e);
            return false;
        }
        return true;
    }

    public void startPreview(SurfaceTexture surfaceTexture) {
        surfaceTexture = surfaceTexture;

        if (mCamera != null) {
            try {
                mCamera.setPreviewTexture(surfaceTexture);
                mCamera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(Constants.LOG_TAG, "" + e);
            }
        }
    }

    public void startPreview() {
        Camera.Size size = mCamera.new Size(mConfig.getVideoWidth(), mConfig.getVideoHeight());

        try {
            if (mSurfaceView != null) {
                mCamera.setPreviewDisplay(mSurfaceView.getHolder());
            } else if (mTextureView != null) {
                ((CameraTextureView) mTextureView).setPreviewSize(size);
                mCamera.setPreviewTexture(mTextureView.getSurfaceTexture());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void setDisplayPreview(SurfaceView surfaceView) {
        if (mConfig == null) {
            throw new IllegalStateException("should set KsyRecordConfig before invoke setDisplayPreview");
        }
        this.mSurfaceView = surfaceView;
        this.mTextureView = null;
    }


    public void setDisplayPreview(TextureView textureView) {
        if (mConfig == null) {
            throw new IllegalStateException("should set KsyRecordConfig before invoke setDisplayPreview");
        }
        this.mTextureView = textureView;
        this.mSurfaceView = null;
    }

    @Override
    public void onClientError(int source, int what) {
        if (onClientErrorListener != null) {
            onClientErrorListener.onClientError(source, what);
        }
        stopCamera();
    }

    public void setOnClientErrorListener(OnClientErrorListener onClientErrorListener) {
        this.onClientErrorListener = onClientErrorListener;
    }

    public void stopCamera() {
        if (mCamera != null) {
            try {
                mCamera.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mCamera = null;
        }
    }

}
