package com.ksy.recordlib.service.jmav.network;

import android.util.Log;


import com.ksy.recordlib.service.jmav.AVError;
import com.ksy.recordlib.service.recoder.RtmpByteBuffer;
import com.ksy.recordlib.service.recoder.RtmpContainer;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;


/**
 * Created by 1 on 2016/3/15.
 */
public class AVHttpClient {
    private final static  String TAG = "RTMF";
    private final static int TIMEOUT_MS = 18000;

    public static int get(String address, RtmpContainer<String> obj) {
        int ret = AVError.AV_OK;

        HttpURLConnection connection = null;
        StringBuilder response = null;
        try {
            URL url = null;
            try {
                url = new URL(address);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return AVError.AV_ERR_MALFORMED_URL;
            }
            try {
                connection = (HttpURLConnection) url.openConnection();
            } catch (IOException e) {
                e.printStackTrace();
                return AVError.AV_ERR_IO;
            }
            try {
                connection.setRequestMethod("GET");
            } catch (ProtocolException e) {
                e.printStackTrace();
                return AVError.AV_ERR_PROTOCOL;
            }
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setDoInput(true);
            try {
                connection.connect();
            } catch (IOException e) {
                e.printStackTrace();
                return AVError.AV_ERR_IO;
            }

            try {
                int code;
                if ((code = connection.getResponseCode()) != HttpURLConnection.HTTP_OK) {
                    Log.e(TAG, "connect tracker server error! error value: " +  code);
                }
            } catch (IOException e) {
                e.printStackTrace();
                return AVError.AV_ERR_IO;
            }

            InputStream in = null;
            try {
                in = connection.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
                return AVError.AV_ERR_IO;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            response = new StringBuilder();
            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
                return AVError.AV_ERR_IO;
            }
        } finally {
            connection.disconnect();
        }

//        {"peers": [{"nearID": "f5000d6e86ca918c1d384d4b5e43ddf92d2f5aeb9088c22a4f90baac7d390d7f", "id": 100}], "code": 0}
        connection.disconnect();
        obj.setObject(response.toString());

        return ret;
    }

    public static int getBytes(String address, RtmpContainer<byte[]> obj) {
        int ret = AVError.AV_OK;

        HttpURLConnection connection = null;
        RtmpByteBuffer bytes = new RtmpByteBuffer(1024);

        try {
            URL url = null;
            try {
                url = new URL(address);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return AVError.AV_ERR_MALFORMED_URL;
            }
            try {
                connection = (HttpURLConnection) url.openConnection();
            } catch (IOException e) {
                e.printStackTrace();
                return AVError.AV_ERR_IO;
            }
            try {
                connection.setRequestMethod("GET");
            } catch (ProtocolException e) {
                e.printStackTrace();
                return AVError.AV_ERR_PROTOCOL;
            }
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setDoInput(true);
            try {
                connection.connect();
            } catch (IOException e) {
                e.printStackTrace();
                return AVError.AV_ERR_IO;
            }

            try {
                int code;
                if ((code = connection.getResponseCode()) != HttpURLConnection.HTTP_OK) {
                    Log.e(TAG, "connect http server error! error value: " +  code);
                }
            } catch (IOException e) {
                e.printStackTrace();
                return AVError.AV_ERR_IO;
            }

            InputStream in = null;
            try {
                in = connection.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
                return AVError.AV_ERR_HTTP_NO_SUCH_DATA;
            }

            byte[] buffer = new byte[1024];
            int hasRead = 0;
            try {
                hasRead = in.read(buffer);
                if (hasRead <=  0) {
                    return AVError.AV_ERR_HTTP_NO_SUCH_DATA;
                }
                bytes.append(buffer, hasRead);

                while ((hasRead = in.read(buffer)) > 0) {
                    bytes.append(buffer, hasRead);
                }

            } catch (IOException e) {
                e.printStackTrace();
                return AVError.AV_ERR_IO;
            }
        } finally {
            connection.disconnect();
        }

//        {"peers": [{"nearID": "f5000d6e86ca918c1d384d4b5e43ddf92d2f5aeb9088c22a4f90baac7d390d7f", "id": 100}], "code": 0}
        connection.disconnect();
        obj.setObject(bytes.toBytes());

        return ret;
    }

    public static int post(String address, String buf) {
        int ret = AVError.AV_OK;

        HttpURLConnection connection = null;
        StringBuilder response = null;
        try {
            URL url = null;
            try {
                url = new URL(address);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return AVError.AV_ERR_MALFORMED_URL;
            }
            try {
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
            } catch (IOException e) {
                e.printStackTrace();
                return AVError.AV_ERR_IO;
            }
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setRequestProperty("Content-Type", "application / xml");
            try {
                connection.connect();
            } catch (IOException e) {
                e.printStackTrace();
                return AVError.AV_ERR_IO;
            }

            //The next code must be commented.
            //See details: http://stackoverflow.com/questions/11413028/cannot-write-output-after-reading-input
            /*try {
                int code;
                if ((code = connection.getResponseCode()) != HttpURLConnection.HTTP_OK) {
                    Log.e(TAG, "connect tracker server error! error value: " +  code);
                }
            } catch (IOException e) {
                e.printStackTrace();
                return RtmfpStdin.ERROR_IO;
            }*/

            DataOutputStream out = null;
            try {
                out = new DataOutputStream(connection.getOutputStream());
                out.writeBytes(buf);
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //Read is necessary. If not, the tracker server wouldn't print "post success" to console.
            BufferedReader in = null;
            try {
                in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String receive = "";
                do {
                    String line = in.readLine();
                    if (line == null)
                        break;
                    receive += line;
                } while (true);
                in.close();
                Log.i(TAG, "After Post, read from http server is: " + receive);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } finally {
            connection.disconnect();
        }

        return ret;
    }

    public static int post(String address, String intput, RtmpContainer<String> output) {
        int ret = AVError.AV_OK;

        HttpURLConnection connection = null;
        StringBuilder response = null;
        try {
            URL url = null;
            try {
                url = new URL(address);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return AVError.AV_ERR_MALFORMED_URL;
            }
            try {
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
            } catch (IOException e) {
                e.printStackTrace();
                return AVError.AV_ERR_IO;
            }
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setRequestProperty("Content-Type", "application / xml");
            try {
                connection.connect();
            } catch (IOException e) {
                e.printStackTrace();
                return AVError.AV_ERR_IO;
            }

            //The next code must be commented.
            //See details: http://stackoverflow.com/questions/11413028/cannot-write-output-after-reading-input
            /*try {
                int code;
                if ((code = connection.getResponseCode()) != HttpURLConnection.HTTP_OK) {
                    Log.e(TAG, "connect tracker server error! error value: " +  code);
                }
            } catch (IOException e) {
                e.printStackTrace();
                return RtmfpStdin.ERROR_IO;
            }*/

            DataOutputStream out = null;
            try {
                out = new DataOutputStream(connection.getOutputStream());
                out.writeBytes(intput);
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //Read is necessary. If not, the tracker server wouldn't print "post success" to console.
            BufferedReader in = null;
            try {
                in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String receive = "";
                do {
                    String line = in.readLine();
                    if (line == null)
                        break;
                    receive += line;
                } while (true);
                in.close();
                Log.i(TAG, "After Post, read from http server is: " + receive);
                output.setObject(receive);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } finally {
            connection.disconnect();
        }

        return ret;
    }
}
