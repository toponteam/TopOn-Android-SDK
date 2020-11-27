/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.common.net.socket;

import com.anythink.core.common.net.ApiRequestParam;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPOutputStream;

public abstract class SocketUploadData {
    private final int TCP_VERSION = 0;

    public static final int TK_TYPE = 1;
    public static final int AGENT_TYPE = 2;

    public static final int NORMAL_DATA_TYPE = 2;
    public static final int GZIP_DATA_TYPE = 3;
    public static final int ZIP_DATA_TYPE = 4;

    protected int reportType;
    protected String reportRate;

    public abstract int getApiType();

    public abstract int getDataType();

    public abstract byte[] getContentData();

    public abstract boolean isOfflineData();

    public void handleLogToRequestNextTime(String errorCode, String errorMsg, String domain, int port) {

    }

//    public byte[] getRequestData() {
//
//        int length = 0;
//        byte[] contentData = getContentData();
//        if (contentData != null) {
//            length = contentData.length;
//        } else {
//            contentData = new byte[0];
//        }
//
//        byte[] headData = new byte[7];
//        headData[0] = TCP_VERSION;
//        headData[1] = (byte) getDataType();
//        headData[2] = (byte) getApiType();
//        headData[3] = (byte) ((length >>> 24) & 0xff);
//        headData[4] = (byte) ((length >>> 16) & 0xff);
//        headData[5] = (byte) ((length >>> 8) & 0xff);
//        headData[6] = (byte) ((length >>> 0) & 0xff);
//
//
//        byte[] requestData = new byte[headData.length + contentData.length];
//
//        System.arraycopy(headData, 0, requestData, 0, headData.length);
//        System.arraycopy(contentData, 0, requestData, headData.length, contentData.length);
//
//        return requestData;
//    }

    public void setTcpInfo(int reportType, String reportRate) {
        this.reportType = reportType;
        this.reportRate = reportRate;
    }

    protected byte[] compress(String str) {
        if (str == null || str.length() == 0) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip;
        try {
            gzip = new GZIPOutputStream(out);
            gzip.write(str.getBytes("utf-8"));
            gzip.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }

    protected JSONObject getCommonDataObject() {
        return ApiRequestParam.getBaseInfoObject();
    }

    /**
     * Extra Info
     *
     * @return
     */
    protected JSONObject getDeviceDataObject() {
        return ApiRequestParam.getMainInfoObject();
    }


    public void startToUpload(SocketListener socketListener) {
        TcpSocketManager.getInstance().sendRequestData(this, socketListener);
    }

    public interface SocketListener {
        public void onSuccess(Object result);

        public void onError(Throwable throwable);
    }


}
