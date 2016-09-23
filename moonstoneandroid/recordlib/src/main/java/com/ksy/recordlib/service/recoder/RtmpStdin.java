package com.ksy.recordlib.service.recoder;

/**
 * Created by 1 on 2016/2/3.
 */
public class RtmpStdin {
    public static final int ERROR_SUCCESS                             = 0;
    public static final int ERROR_SOCKET_CREATE                       = 1000;
    public static final int ERROR_RTMFP_CHECKSUM_INVALID              = 1009;
    public static final int ERROR_RTMFP_CHUNK_INVALID                 = 1012;
    public static final int ERROR_RTMFP_PACKET_INVALID                = 1015;
    public static final int ERROR_ST_SENDTO                           = 1016;
    public static final int ERROR_RTMFP_BUFFER_OVERFLOW               = 1034;
    public static final int ERROR_RTMFP_SEQUENCE_NUMBER_WRAP          = 1035;
    public static final int ERROR_ST_RECVFROM                         = 1036;
    public static final int ERROR_ST_COND_TIMEOUT                     = 1037;
    public static final int ERROR_NOT_IMPLEMENT                       = 1038;
    public static final int ERROR_RTMFP_SESSION_INVALID               = 1039;
    public static final int ERROR_RTMFP_ADDR_ERROR                    = 1040;
    public static final int ERROR_RTMFP_PEER_CLOSED                   = 1041;
    public static final int ERROR_RTMFP_PACKET_TOO_SHORT              = 1043;
    public static final int ERROR_RTMFP_OVERLOAD_DROP                 = 1044;
    public static final int ERROR_RTMFP_SESSION_CLOSE                 = 1047;

    public static final int ERROR_AES_NO_SUCH_ALGORITHM               = 1100;
    public static final int ERROR_AES_NO_SUCH_PADDING                 = 1101;
    public static final int ERROR_AES_INVALID_KEY                     = 1102;
    public static final int ERROR_AES_INVALID_ALGORITHM_PARAMETER     = 1103;
    public static final int ERROR_AES_ILLEGAL_BLOCK_SIZE              = 1104;
    public static final int ERROR_AES_BAD_PADDING                     = 1105;

    public static final int ERROR_DH_NO_SUCH_ALGORITHM                = 1200;
    public static final int ERROR_DH_INVALID_KEY_SPEC                 = 1201;
    public static final int ERROR_DH_INVALID_KEY                      = 1202;

    public static final int ERROR_HMAC_NO_SUCH_ALGORITHM              = 1300;
    public static final int ERROR_HMAC_UNSUPPORTED_ENCODING           = 1301;
    public static final int ERROR_HMAC_INVALID_KEY                    = 1302;

    public static final int ERROR_WIFI_INVALID                        = 1400;
    public static final int ERROR_MOBILE_INVALID                      = 1401;
    public static final int ERROR_NETWORK_INVALID                     = 1402;
    public static final int ERROR_MALFORMED_URL                       = 1403;
    public static final int ERROR_IO                                  = 1404;
    public static final int ERROR_PROTOCOL                            = 1405;
    public static final int ERROR_JSON                                = 1406;

    public static final int ERROR_PIECE_ALREADY_EXISTS                = 1500;
    public static final int ERROR_HTTP_NO_SUCH_DATA                   = 1501;
    public static final int ERROR_PEER_NOT_MATCH                      = 1502;
    public static final int ERROR_P2P_SEGMENT_PIECE_NOT_FOUND         = 1503;
    public static final int ERROR_NON_EXISTS                          = 1504;
    public static final int ERROR_FILE_ALREADY_EXISTS                 = 1505;
    public static final int ERROR_MAKE_FILE                           = 1506;
    public static final int ERROR_UPDATE_M3U8                         = 1507;
    public static final int ERROR_READ_OR_WRITE_STREAM                = 1508;
    public static final int ERROR_INVALID_INTERNAL_COMMAND            = 1509;
    public static final int ERROR_INVALID_CHUNK_LENGTH                = 1510;
    public static final int ERROR_COMMAND_PROCESSED                   = 1511;



    ///////////////////////////////////////////////////////
    // RTMP protocol error.
    ///////////////////////////////////////////////////////
    public static final int ERROR_RTMP_PLAIN_REQUIRED                  = 2000;
    public static final int ERROR_RTMP_CHUNK_START                     = 2001;
    public static final int ERROR_RTMP_MSG_INVLIAD_SIZE                = 2002;
    public static final int ERROR_RTMP_AMF0_DECODE                     = 2003;
    public static final int ERROR_RTMP_AMF0_INVALID                    = 2004;
    public static final int ERROR_RTMP_AMF0_ENCODE                     = 2009;

    ///////////////////////////////////////////////////////
    // SRS application level
    ///////////////////////////////////////////////////////
    public static final int ERROR_KERNEL_STREAM_INIT                   = 3000;

    public static final int ERROR_RTMFP_HANDLER_EXISTS                 = 9133;
    public static final int ERROR_RTMFP_COOKIE_INVALID                 = 9118;

    public static boolean rtmfp_is_gracefully_closed(int ret) {
        // when peer not found, same as gracefully closed,
        // to avoid lots of logs.
        if (ret == ERROR_RTMFP_SESSION_INVALID) {
            return true;
        }

        // when stage wrap, that is we have ack the sequence number,
        // but we got an old sequence number, see RtmfpConnectionFlow::interpret,
        // to avoid lots of logs.
        if (ret == ERROR_RTMFP_SEQUENCE_NUMBER_WRAP) {
            return true;
        }

        // closed by close request/ack.
        if (ret == ERROR_RTMFP_PEER_CLOSED) {
            return true;
        }

        // when overload, use drop strategy to drop the packet,
        // when redirect/drop, use this code indicates the packet is dropped,
        // client will got timeout and retry.
        // to avoid lots of logs.
        if (ret == ERROR_RTMFP_OVERLOAD_DROP) {
            return true;
        }

        // when server close the session.
        if (ret == ERROR_RTMFP_SESSION_CLOSE) {
            return true;
        }

        return false;
    }

    public static final class RtmfpException extends Exception {
        private final int status;

        public RtmfpException(int status, String message) {
            super(message);
            this.status = status;
        }

        public RtmfpException(int status, String message, Exception e) {
            super(message, e);
            this.status = status;
        }

        public String getDescription() {
            String message = "EXCEPTION: " + getMessage() + " STATUS: " + status;
            return message;
        }
    }
}
