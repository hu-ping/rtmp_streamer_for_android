package com.ksy.recordlib.service.util;

/**
 * Created by huping on 2016/8/10.
 */
public class ByteConvert {

    public static byte[] longToByteArray(long ts) {
        byte[] result = new byte[4];
//        result[0] = new Long(timeStamp >> 56 & 0xff).byteValue();
//        result[1] = new Long(timeStamp >> 48 & 0xff).byteValue();
//        result[2] = new Long(timeStamp >> 40 & 0xff).byteValue();
//        result[3] = new Long(timeStamp >> 32 & 0xff).byteValue();
        result[0] = new Long(ts >> 24 & 0xff).byteValue();
        result[1] = new Long(ts >> 16 & 0xff).byteValue();
        result[2] = new Long(ts >> 8 & 0xff).byteValue();
        result[3] = new Long(ts >> 0 & 0xff).byteValue();
        return result;
    }

    public static byte[] intToByteArray(int length) {
        byte[] result = new byte[3];
//        result[0] = (byte) ((length >> 24) & 0xFF);
        result[0] = (byte) ((length >> 16) & 0xFF);
        result[1] = (byte) ((length >> 8) & 0xFF);
        result[2] = (byte) ((length >> 0) & 0xFF);
        return result;
    }

    public static byte[] intToByteArrayTwoByte(int length) {
        byte[] result = new byte[2];
//        result[0] = (byte) ((length >> 24) & 0xFF);
//        result[0] = (byte) ((length >> 16) & 0xFF);
        result[0] = (byte) ((length >> 8) & 0xFF);
        result[1] = (byte) ((length >> 0) & 0xFF);
        return result;
    }

    public static byte[] intToByteArrayFull(int length) {
        byte[] result = new byte[4];
        result[0] = (byte) ((length >> 24) & 0xFF);
        result[1] = (byte) ((length >> 16) & 0xFF);
        result[2] = (byte) ((length >> 8) & 0xFF);
        result[3] = (byte) ((length >> 0) & 0xFF);
        return result;
    }



    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }
}
