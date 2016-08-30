package com.ksy.recordlib.service.util;

/**
 * Created by Administrator on 2016/8/9.
 */
public class YunManager {

    // for the vbuffer for YV12(android YUV), @see below:
    // https://developer.android.com/reference/android/hardware/Camera.Parameters.html#setPreviewFormat(int)
    // https://developer.android.com/reference/android/graphics/ImageFormat.html#YV12
    public static int getYuvBuffer(int width, int height) {
        // stride = ALIGN(width, 16)
        int stride = (int) Math.ceil(width / 16.0) * 16;
        // y_size = stride * height
        int y_size = stride * height;
        // c_stride = ALIGN(stride/2, 16)
        int c_stride = (int) Math.ceil(width / 32.0) * 16;
        // c_size = c_stride * height/2
        int c_size = c_stride * height / 2;
        // size = y_size + c_size * 2
        return y_size + c_size * 2;
    }

    // the color transform, @see http://stackoverflow.com/questions/15739684/mediacodec-and-camera-color-space-incorrect
    public static byte[] YV12toYUV420PackedSemiPlanar(final byte[] input, final byte[] output, final int width, final int height) {
        /*
         * COLOR_TI_FormatYUV420PackedSemiPlanar is NV12
         * We convert by putting the corresponding U and V bytes together (interleaved).
         */

        final int frameSize = width * height;
        final int qFrameSize = frameSize / 4;

        System.arraycopy(input, 0, output, 0, frameSize); // Y

        for (int i = 0; i < qFrameSize; i++) {
            output[frameSize + i * 2] = input[frameSize + i + qFrameSize]; // Cb (U)
            output[frameSize + i * 2 + 1] = input[frameSize + i]; // Cr (V)
        }

        return output;
    }

    // NV21: YYYYYYYY VUVU     =>YUV420SP
    public static byte[] YV12toNV21(final byte[] input, final byte[] output, final int width, final int height) {
        final int frameSize = width * height;
        final int qFrameSize = frameSize / 4;

        System.arraycopy(input, 0, output, 0, frameSize); // Y

        for (int i = 0; i < qFrameSize; i++) {
            output[frameSize + i * 2] = input[frameSize + i ]; // Cb (V)
            output[frameSize + i * 2 + 1] = input[frameSize + i + qFrameSize]; // Cr (U)
        }

        return output;
    }

    public static byte[] YUV420PackedSemiPlanartoYV12(final byte[] input, final byte[] output, final int width, final int height) {
        final int frameSize = width * height;
        final int qFrameSize = frameSize / 4;

        System.arraycopy(input, 0, output, 0, frameSize); // Y

        for (int i = 0; i < qFrameSize; i++) {
            output[frameSize + i + qFrameSize] = input[frameSize + i * 2]; // Cb (U)
            output[frameSize + i] = input[frameSize + i * 2 + 1]; // Cr (V)
        }

        return output;
    }

    public static byte[] YV12toYUV420Planar(byte[] input, byte[] output, int width, int height) {
        /*
         * COLOR_FormatYUV420Planar is I420 which is like YV12, but with U and V reversed.
         * So we just have to reverse U and V.
         */
        final int frameSize = width * height;
        final int qFrameSize = frameSize / 4;

        System.arraycopy(input, 0, output, 0, frameSize); // Y
        System.arraycopy(input, frameSize, output, frameSize + qFrameSize, qFrameSize); // Cr (V)
        System.arraycopy(input, frameSize + qFrameSize, output, frameSize, qFrameSize); // Cb (U)

        return output;
    }


    public static void rotateYV12(byte[] input, byte[] output, int imageWidth, int imageHeight, int rotation, int cameraType){
        if (rotation == 0) {
            System.arraycopy(input, 0, output, 0, input.length);
            return;
        }

        if (cameraType == Constants.CONFIG_CAMERA_TYPE_BACK) {
            if (rotation == 90) {
                rotateYV12Degree90(input, output, imageWidth, imageHeight);
            } else if (rotation == 180) {
                rotateYV12Degree180(input, output, imageWidth, imageHeight);
            }
        }

        if (cameraType == Constants.CONFIG_CAMERA_TYPE_FRONT) {
            if (rotation == 90) {
                rotateYV12Degree270(input, output, imageWidth, imageHeight);
            } else if (rotation ==180) {
                rotateYV12Degree180(input, output, imageWidth, imageHeight);
            }
        }
    }

    private static void rotateYV12Degree90(byte[] input, byte[] output, int imageWidth, int imageHeight) {
        final int frameSize = imageWidth * imageHeight;
        final int qFrameSize = frameSize / 4;

        // Rotate the Y luma
        int i = 0;
        for (int x = 0;x < imageWidth;x++) {
            for(int y = imageHeight-1; y >= 0; y--)
            {
                output[i] = input[y*imageWidth+x];
                i++;
            }
        }

        //Rotate the V and U color components
        for (int x = 0; x < imageWidth/2; x++) {
            for(int y = imageHeight/4 - 1; y >= 0; y--) {
                //Rotate the V color components
                output[i] = input[frameSize + y * imageWidth + imageWidth/2 + x];
                //Rotate the U color components
                output[qFrameSize + i] = input[frameSize + qFrameSize + y * imageWidth + imageWidth/2 + x];
                i++;

                //Rotate the V color components
                output[i] = input[frameSize + y * imageWidth + x];
                //Rotate the U color components
                output[qFrameSize + i] = input[frameSize + qFrameSize + y * imageWidth + x];
                i++;
            }
        }
    }

    private static void rotateYV12Degree180(byte[] input, byte[] output, int imageWidth, int imageHeight) {
        final int frameSize = imageWidth * imageHeight;
        final int qFrameSize = frameSize / 4;

        // Rotate the Y luma
        int i = 0;
        for (int y = imageHeight - 1; y >= 0; y--) {
            for (int x = imageWidth - 1; x >= 0; x --) {
                output[i] = input[y*imageWidth + x];
                i++;
            }
        }

        //Rotate the V and U color components
        for (int y = imageHeight/2 - 1; y >= 0; y--) {
            for (int x = imageWidth/2 - 1; x >= 0; x--) {
                //Rotate the V color components
                output[i] = input[frameSize + y * imageWidth/2 + x];
                //Rotate the U color components
                output[qFrameSize + i] = input[frameSize + qFrameSize + y * imageWidth/2 + x];
                i++;
            }
        }
    }

    private static void rotateYV12Degree270(byte[] input, byte[] output, int imageWidth, int imageHeight) {
        final int frameSize = imageWidth * imageHeight;
        final int qFrameSize = frameSize / 4;

        //Rotate the Y luma
        int i = 0;
        for (int x = 1; x <= imageWidth; x++) {
            for (int y = 1; y <= imageHeight; y++) {
                output[i++] = input[y * imageWidth - x];
            }
        }

        //Rotate the V and U color components
        for (int x = 1; x <= imageWidth/2; x++) {
            for (int y = 1; y <= imageHeight/2; y++) {
                output[i] = input[frameSize + y * imageWidth/2 - x];
                output[qFrameSize + i] = input[frameSize + qFrameSize + y * imageWidth/2 - x];
                i++;
            }
        }
    }
}
