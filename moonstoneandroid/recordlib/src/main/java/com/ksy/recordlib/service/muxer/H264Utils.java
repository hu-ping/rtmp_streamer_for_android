package com.ksy.recordlib.service.muxer;

import android.media.MediaCodec;
import android.util.Log;

import com.ksy.recordlib.service.util.Constants;

import java.nio.ByteBuffer;

/**
 * utils functions from srs.
 */
public class H264Utils {
    private final static String TAG = "Muxer";

    public boolean srs_bytes_equals(byte[] a, byte[] b) {
        if ((a == null || b == null) && (a != null || b != null)) {
            return false;
        }

        if (a.length != b.length) {
            return false;
        }

        for (int i = 0; i < a.length && i < b.length; i++) {
            if (a[i] != b[i]) {
                return false;
            }
        }

        return true;
    }

    public H264AnnexbSearch srs_avc_startswith_annexb(ByteBuffer bb, MediaCodec.BufferInfo bi) {
        H264AnnexbSearch h264AnnexbSearch = new H264AnnexbSearch();
        h264AnnexbSearch.match = false;

        int pos = bb.position();
        while (pos < bi.size - 3) {
            // not match.
            //Returns the byte at the specified index and does not change the position.

//            StringBuilder sb = new StringBuilder();
//            Log.e(Constants.LOG_TAG, "position:" + pos);
//                    + sb.append(String.format("0x%s ", Integer.toHexString(bb.get(pos) & 0xFF))));
//            H264Utils.srs_print_bytes(Constants.LOG_TAG, bb, pos);

            if (bb.get(pos) != 0x00 || bb.get(pos + 1) != 0x00) {
                break;
            }

            // match N[00] 00 00 01, where N>=0
            if (bb.get(pos + 2) == 0x01) {
                h264AnnexbSearch.match = true;
                h264AnnexbSearch.nb_start_code = pos + 3 - bb.position();
                break;
            }
            pos++;
        }

        return h264AnnexbSearch;
    }

    public boolean srs_aac_startswith_adts(ByteBuffer bb, MediaCodec.BufferInfo bi) {
        int pos = bb.position();
        if (bi.size - pos < 2) {
            return false;
        }

        // matched 12bits 0xFFF,
        // @remark, we must cast the 0xff to char to compare.
        if (bb.get(pos) != (byte) 0xff || (byte) (bb.get(pos + 1) & 0xf0) != (byte) 0xf0) {
            return false;
        }

        return true;
    }

    public int srs_codec_aac_ts2rtmp(int profile) {
        switch (profile) {
            case SrsAacProfile.Main:
                return SrsAacObjectType.AacMain;
            case SrsAacProfile.LC:
                return SrsAacObjectType.AacLC;
            case SrsAacProfile.SSR:
                return SrsAacObjectType.AacSSR;
            default:
                return SrsAacObjectType.Reserved;
        }
    }

    public int srs_codec_aac_rtmp2ts(int object_type) {
        switch (object_type) {
            case SrsAacObjectType.AacMain:
                return SrsAacProfile.Main;
            case SrsAacObjectType.AacHE:
            case SrsAacObjectType.AacHEV2:
            case SrsAacObjectType.AacLC:
                return SrsAacProfile.LC;
            case SrsAacObjectType.AacSSR:
                return SrsAacProfile.SSR;
            default:
                return SrsAacProfile.Reserved;
        }
    }
    /**
     * print the size of bytes in bb
     *
     * @param bb   the bytes to print.
     * @param size the total size of bytes to print.
     */
    public static void srs_print_bytes(String tag, ByteBuffer bb, int size) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        int bytes_in_line = 16;
        int max = bb.remaining();
        for (i = 0; i < size && i < max; i++) {
            sb.append(String.format("0x%s ", Integer.toHexString(bb.get(i) & 0xFF)));
            if (((i + 1) % bytes_in_line) == 0) {
                Log.i(tag, String.format("%03d-%03d: %s", i / bytes_in_line * bytes_in_line, i, sb.toString()));
                sb = new StringBuilder();
            }
        }
        if (sb.length() > 0) {
            Log.i(tag, String.format("%03d-%03d: %s", size / bytes_in_line * bytes_in_line, i - 1, sb.toString()));
        }
    }

    public static void srs_print_bytes(String tag, byte[] bb, int size) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        int bytes_in_line = 16;
        int max = bb.length;
        for (i = 0; i < size && i < max; i++) {
            sb.append(String.format("0x%s ", Integer.toHexString(bb[i] & 0xFF)));
            if (((i + 1) % bytes_in_line) == 0) {
                Log.i(tag, String.format("%03d-%03d: %s", i / bytes_in_line * bytes_in_line, i, sb.toString()));
                sb = new StringBuilder();
            }
        }
        if (sb.length() > 0) {
            Log.i(tag, String.format("%03d-%03d: %s", size / bytes_in_line * bytes_in_line, i - 1, sb.toString()));
        }
    }

    class SrsAacObjectType {
        public final static int Reserved = 0;

        /*
         *Table 1.1 – Audio Object Type definition
         *@see @see aac-mp4a-format-ISO_IEC_14496-3+2001.pdf, page 23
        */
        public final static int AacMain = 1;
        public final static int AacLC = 2;
        public final static int AacSSR = 3;

        // AAC HE = LC+SBR
        public final static int AacHE = 5;
        // AAC HEv2 = LC+SBR+PS
        public final static int AacHEV2 = 29;
    }

    /**
     * the aac profile, for ADTS(HLS/TS)
     * //@see https://github.com/simple-rtmp-server/srs/issues/310
     */
    class SrsAacProfile {
        public final static int Reserved = 3;

        // @see 7.1 Profiles, aac-iso-13818-7.pdf, page 40
        public final static int Main = 0;
        public final static int LC = 1;
        public final static int SSR = 2;
    }
}