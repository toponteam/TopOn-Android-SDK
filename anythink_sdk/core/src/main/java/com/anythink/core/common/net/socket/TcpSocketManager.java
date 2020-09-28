package com.anythink.core.common.net.socket;


import android.util.Log;

import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.utils.task.TaskManager;
import com.anythink.core.common.utils.task.Worker;
import com.anythink.core.strategy.AppStrategy;
import com.anythink.core.strategy.AppStrategyManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

public class TcpSocketManager {
    private final int TCP_VERSION = 0;
    private final int HEAD_LENGTH = 7;

    private final int RESPONSE_SUCCESS = 1;
//    private final int RESPONSE_CLIENT_REQUEST_ERROR = 2;
//    private final int RESPONSE_SERVER_ERROR = 3;

    private final String TAG = getClass().getSimpleName();
    private static TcpSocketManager sIntance;

    private String mDomain;
    private int mPort;

    private Socket mSocket;

    /**
     * Write byte buffer array
     **/
    byte[] requestBufferBytes = null;
    byte[] responseBytes = new byte[1];

    private TcpSocketManager() {
    }

    protected static synchronized TcpSocketManager getInstance() {
        if (sIntance == null) {
            sIntance = new TcpSocketManager();
        }
        return sIntance;
    }

    private void connect() throws Exception {
        synchronized (this) {
            if (mSocket == null) {
                mSocket = new Socket();
                mSocket.setSoTimeout(60 * 1000);
            }

            AppStrategy appStrategy = AppStrategyManager.getInstance(SDKContext.getInstance().getContext()).getAppStrategyByAppId(SDKContext.getInstance().getAppId());
            if (appStrategy != null) {
                if (Const.DEBUG) {
                    Log.e(getClass().getSimpleName(), "connect");
                }
                mDomain = appStrategy.getTcpDomain();
                mPort = appStrategy.getTcpPort();
                mSocket.connect(new InetSocketAddress(appStrategy.getTcpDomain(), appStrategy.getTcpPort()), 30 * 1000);
            }

        }
    }


    private void disConnect() {
        synchronized (this) {
            try {
                if (mSocket != null) {
                    mSocket.close();
                    mSocket = null;
                }
            } catch (Exception e) {
                if (Const.DEBUG) {
                    e.printStackTrace();
                }
            }
            if (Const.DEBUG) {
                Log.e(getClass().getSimpleName(), "disConnect");
            }
        }
    }

    public boolean isConnect() {
        return mSocket != null && mSocket.isConnected() && !mSocket.isClosed();
    }


    public void sendRequestData(final SocketUploadData socketUploadData, final SocketUploadData.SocketListener socketListener) {
        Worker worker = new Worker() {
            @Override
            public void work() {
                Throwable exception = null;
                try {
                    if (Const.DEBUG) {
                        Log.e(TAG, "send data type is offline:" + socketUploadData.isOfflineData());
                    }
                    writeDataToServer(socketUploadData);
                    int responseCode = getResponseCode();
                    if (responseCode == RESPONSE_SUCCESS) {
                        if (socketListener != null) {
                            socketListener.onSuccess(socketUploadData);
                        }
                        if (Const.DEBUG) {
                            Log.e(TAG, "1 send data success:" + socketUploadData.toString() + "，response code :" + responseCode);
                        }
                    } else {
                        if (Const.DEBUG) {
                            Log.e(TAG, "1 send data error:" + responseCode);
                        }
                        throw new Exception("Response Error Code:" + responseCode);
                    }


                    return;
                } catch (SocketException e) {
                    if (Const.DEBUG) {
                        Log.e(TAG, "Throw exception:" + e.getMessage() + ";;;" + socketUploadData.toString());
                    }
                    disConnect();
                    //Re-try to send data
                    try {
                        writeDataToServer(socketUploadData);
                        int responseCode = getResponseCode();
                        if (responseCode == RESPONSE_SUCCESS) {
                            if (socketListener != null) {
                                socketListener.onSuccess(socketUploadData);
                            }
                            if (Const.DEBUG) {
                                Log.e(TAG, "2 send data success:" + socketUploadData.toString() + "，response code :" + responseCode);
                            }
                        } else {
                            if (Const.DEBUG) {
                                Log.e(TAG, "2 send data error:" + responseCode);
                            }
                            throw new Exception("Response Error Code:" + responseCode);
                        }

                        return;
                    } catch (Throwable throwable) {
                        exception = throwable;
                        if (Const.DEBUG) {
                            throwable.printStackTrace();
                        }
                    }

                } catch (Throwable e) {
                    exception = e;
                    if (Const.DEBUG) {
                        e.printStackTrace();
                    }

                }
                if (Const.DEBUG) {
                    Log.e(TAG, "send data fail:" + exception.getMessage() + ";;;" + socketUploadData.toString());
                }

                socketUploadData.handleLogToRequestNextTime("", exception != null ? exception.getMessage() : "", mDomain, mPort);
                if (socketListener != null) {
                    socketListener.onError(exception);
                }

            }
        };

        TaskManager.getInstance().run(worker, TaskManager.TYPE_TCP_LOG);
    }

    private synchronized void writeDataToServer(SocketUploadData socketUploadData) throws Exception {
        if (!isConnect()) {
            if (Const.DEBUG) {
                Log.e(TAG, "Connect is dead, re-connect tcp.");
            }
            connect();

        } else {
            if (Const.DEBUG) {
                Log.e(TAG, "Connect is alive.");
            }
        }

        int contetnLength = 0;
        byte[] contentData = socketUploadData.getContentData();
        if (contentData != null) {
            contetnLength = contentData.length;
        } else {
            return;
        }

        if (contentData.length == 0) {
            return;
        }

        int requestDataLength = HEAD_LENGTH + contetnLength;
        if (requestBufferBytes == null || requestBufferBytes.length < requestDataLength) {
            requestBufferBytes = new byte[requestDataLength];

        }

        /**Header**/
        requestBufferBytes[0] = TCP_VERSION;
        requestBufferBytes[1] = (byte) socketUploadData.getDataType();
        requestBufferBytes[2] = (byte) socketUploadData.getApiType();
        requestBufferBytes[3] = (byte) ((contetnLength >>> 24) & 0xff);
        requestBufferBytes[4] = (byte) ((contetnLength >>> 16) & 0xff);
        requestBufferBytes[5] = (byte) ((contetnLength >>> 8) & 0xff);
        requestBufferBytes[6] = (byte) ((contetnLength >>> 0) & 0xff);

        /**Content**/
        System.arraycopy(contentData, 0, requestBufferBytes, HEAD_LENGTH, contentData.length);

        //Only fot test
//        byte[] test = new byte[contentData.length];
//        System.arraycopy(requestBufferBytes, HEAD_LENGTH, test, 0, contetnLength);
//        byte[] unzipTest = unGZip(test);
//        Log.e(TAG, "upload data string:" + new String(unzipTest));

        OutputStream outputStream = mSocket.getOutputStream();
//        byte[] uploadData = socketUploadData.getRequestData();
        outputStream.write(requestBufferBytes, 0, requestDataLength);
        outputStream.flush();
    }

    private synchronized int getResponseCode() throws Exception {
        InputStream inputStream = mSocket.getInputStream();

        int length = inputStream.read(responseBytes, 0, 1);

        if (length == -1) {
            throw new SocketException("Socket.InputStream read length = -1!");
        }

        int responseCode = responseBytes[0];
        responseBytes[0] = 0;
        return responseCode;

    }


//    public static byte[] unGZip(byte[] data) {
//        byte[] b = null;
//        try {
//            ByteArrayInputStream bis = new ByteArrayInputStream(data);
//            GZIPInputStream gzip = new GZIPInputStream(bis);
//            byte[] buf = new byte[1024];
//            int num = -1;
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            while ((num = gzip.read(buf, 0, buf.length)) != -1) {
//                baos.write(buf, 0, num);
//            }
//            b = baos.toByteArray();
//            baos.flush();
//            baos.close();
//            gzip.close();
//            bis.close();
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        return b;
//    }


}
