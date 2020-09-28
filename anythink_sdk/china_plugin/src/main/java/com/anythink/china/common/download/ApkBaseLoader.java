package com.anythink.china.common.download;

import android.text.TextUtils;
import android.util.Log;

import com.anythink.china.api.ApkError;
import com.anythink.china.api.ApkErrorCode;
import com.anythink.china.common.download.task.TaskManager;
import com.anythink.china.common.resource.FileUtils;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.utils.CommonLogUtil;
import com.anythink.core.common.utils.task.Worker;

import org.apache.http.conn.ConnectTimeoutException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

public abstract class ApkBaseLoader {

    private static final String TAG = ApkBaseLoader.class.getSimpleName();

    public interface DownloadListener {

        void onStartBefore(ApkRequest apkRequest, long progress, long all);

        void onSuccess(ApkRequest apkRequest, long downloadTime);

        void onProgress(ApkRequest apkRequest, long progress, long all);

        void onFailed(ApkRequest apkRequest, String msg);

        void onCancel(ApkRequest apkRequest, long progress, long all, int status);
    }

    private DownloadListener mDownloadListener;
    private ApkRequest mApkRequest;

    protected String mURL;


    protected boolean mIsStop;
    protected boolean mIsPause;

    public static final String SUFFIX_TEMP = ".temp";
    public static final String SUFFIX_LOG = ".log";
    public static final String SUFFIX_APK = ".apk";

    public static final int NORMAL = 0;
    public static final int SUCCESS = 1;
    public static final int PAUSE = 2;
    public static final int STOP = 3;
    public static final int FAIL = 4;
    public int mStatus = NORMAL;

    private String mFailMsg;

    public ApkBaseLoader(ApkRequest apkRequest) {
        this.mApkRequest = apkRequest;
        this.mURL = apkRequest.url;
    }

    public void start(DownloadListener downloadListener) {
        this.mDownloadListener = downloadListener;
        mIsStop = false;
        load();
    }


    public void stop() {
        mIsStop = true;
    }

    public void pause() {
        mIsPause = true;
    }

    protected long mStartPos;
    protected long downloadStartTime;
    protected long downloadEndTime;
    protected long downloadSize;
    protected long writeLength;


    private void load() {

        Worker worker = new Worker() {

            @Override
            public void work() {
                CommonLogUtil.d(TAG, "start download url -> " + mURL);

                try {

                    readLogFile();
                    doUrlConnect(mURL);

                } catch (OutOfMemoryError | StackOverflowError e) {
                    System.gc();
                    CommonLogUtil.e(TAG, e.getMessage());

                    String msg = ApkErrorCode.fail_connect;
                    if (e.getMessage() != null) {
                        msg = e.getMessage();
                    }
                    onLoadFailedCallback(ApkErrorCode.get(ApkErrorCode.exception, msg));
                } catch (Exception e) {
                    CommonLogUtil.e(TAG, e.getMessage());

                    String msg = ApkErrorCode.fail_connect;
                    if (e.getMessage() != null) {
                        msg = e.getMessage();
                    }
                    onLoadFailedCallback(ApkErrorCode.get(ApkErrorCode.exception, msg));

                }
            }

            private void doUrlConnect(String urlStr) {

                downloadStartTime = System.currentTimeMillis();

                HttpURLConnection httpConn = null;
                try {
                    CommonLogUtil.i(TAG, "REQUEST URL: " + urlStr);

                    URL url = new URL(urlStr);

                    httpConn = (HttpURLConnection) url.openConnection();

                    httpConn.setInstanceFollowRedirects(false);

                    //Header
                    if (mStartPos > 0) {
                        CommonLogUtil.i(TAG, "Range: startPos -> " + mStartPos + "  ,  endPos -> " + downloadSize);
                        httpConn.setRequestProperty("Range", "bytes=" + mStartPos + "-");
                    } else {
                        downloadSize = httpConn.getContentLength();
                    }


                    if (downloadSize <= 0) {
                        CommonLogUtil.e(TAG, "downloadSize <= 0!");
                        onErrorCallback(ApkErrorCode.get(ApkErrorCode.exception, "downloadSize <= 0"));
                        return;
                    }

                    if (mIsStop) {
                        if (mApkRequest != null) {
                            mApkRequest.stop();
                        }
                        mStatus = STOP;
                        onCancelCallback();
                        return;
                    }

                    httpConn.setConnectTimeout(getConnectTimeout());
                    httpConn.connect();

                    final int statusCode = httpConn.getResponseCode();
                    if (statusCode != HttpURLConnection.HTTP_OK && statusCode != HttpURLConnection.HTTP_PARTIAL) {

                        CommonLogUtil.e(TAG, "http respond status code is " + statusCode + " ! url=" + urlStr);
                        onErrorCallback(ApkErrorCode.get(ApkErrorCode.httpStatuException, httpConn.getResponseMessage()));
                        return;
                    }

                    if (mIsStop) {
                        if (mApkRequest != null) {
                            mApkRequest.stop();
                        }
                        mStatus = STOP;
                        onCancelCallback();
                        return;
                    }

                    InputStream inputStream = httpConn.getInputStream();

                    if (mApkRequest != null) {
                        mApkRequest.start();
                    }
                    mApkRequest.apkSize = downloadSize;
                    //callback
                    if (mDownloadListener != null) {
                        mDownloadListener.onStartBefore(mApkRequest, mStartPos, downloadSize);
                    }

                    int status = writeToLocal(mURL, inputStream);
                    mStatus = status;

                    if (inputStream != null) {
                        inputStream.close();
                    }

                    downloadEndTime = System.currentTimeMillis();
                    mApkRequest.downloadTime = downloadEndTime - downloadStartTime;

                    switch (status) {
                        case SUCCESS:
                            CommonLogUtil.d(TAG, "download success --> " + mURL);
                            onLoadFinishCallback();
                            break;
                        case PAUSE:
                        case STOP:
                            onCancelCallback();
                            break;
                        case FAIL:
                        default:
                            CommonLogUtil.d(TAG, "download fail --> " + mURL);
                            onLoadFailedCallback(ApkErrorCode.get(ApkErrorCode.exception, ApkErrorCode.fail_save + "(" + mFailMsg + ")"));
                            break;
                    }
                } catch (SocketTimeoutException e) {
                    onErrorCallback(ApkErrorCode.get(ApkErrorCode.timeOutError, e.getMessage()));
                    CommonLogUtil.e(TAG, e.toString());
                } catch (ConnectTimeoutException e) {
                    onConnectTimeout(e);
                } catch (OutOfMemoryError e) {
                    System.gc();
                    CommonLogUtil.e(TAG, e.getMessage());

                    String msg = ApkErrorCode.fail_connect;
                    if (e.getMessage() != null) {
                        msg = e.getMessage();
                    }
                    onErrorCallback(ApkErrorCode.get(ApkErrorCode.exception, msg));
                } catch (StackOverflowError e) {
                    System.gc();
                    CommonLogUtil.e(TAG, e.getMessage());

                    String msg = ApkErrorCode.fail_connect;
                    if (e.getMessage() != null) {
                        msg = e.getMessage();
                    }
                    onErrorCallback(ApkErrorCode.get(ApkErrorCode.exception, msg));
                } catch (Error e) {
                    System.gc();
                    CommonLogUtil.e(TAG, e.getMessage());

                    String msg = ApkErrorCode.fail_connect;
                    if (e.getMessage() != null) {
                        msg = e.getMessage();
                    }
                    onErrorCallback(ApkErrorCode.get(ApkErrorCode.exception, msg));
                } catch (Exception e) {
                    CommonLogUtil.e(TAG, e.getMessage());

                    String msg = ApkErrorCode.fail_connect;
                    if (e.getMessage() != null) {
                        msg = e.getMessage();
                    }
                    if (Const.DEBUG) {
                        e.printStackTrace();
                    }

                    onErrorCallback(ApkErrorCode.get(ApkErrorCode.exception, msg));

                } finally {
                    if (httpConn != null) {
                        httpConn.disconnect();
                    }
                }

            }

        };

        if (isUseSingleThread()) {
            TaskManager.getInstance().run(worker, TaskManager.TYPE_SINGLE);
        } else {
            TaskManager.getInstance().run(worker);
        }

    }

    private int writeToLocal(String url, InputStream inputStream) {

        String resourcePath = FileUtils.getResourcePath(url);
        if (TextUtils.isEmpty(resourcePath)) {
            return FAIL;
        }
        File tempFile = new File(resourcePath + SUFFIX_TEMP);
        File logFile = new File(resourcePath + SUFFIX_LOG);

        RandomAccessFile tempRaf = null;
        RandomAccessFile logRaf = null;
        try {
            if (!tempFile.exists()) {
                boolean newTempFile = tempFile.createNewFile();
                boolean newLogFile = logFile.createNewFile();
                if (!newTempFile || !newLogFile) {
                    return FAIL;
                }
            }

            tempRaf = new RandomAccessFile(tempFile, "rws");
            logRaf = new RandomAccessFile(logFile, "rws");

            if (mStartPos > 0) {
                Log.i(TAG, "(" + mApkRequest.title + ")  seek to -> " + mStartPos);
                tempRaf.seek(mStartPos);
            } else {
                Log.i(TAG, "(" + mApkRequest.title + ")  set temp file size -> " + downloadSize);
                tempRaf.setLength(downloadSize);
            }
            byte[] buffer = new byte[1024 * 1024];
            int num = 0;
            writeLength = mStartPos;//update progress
            while (-1 != (num = inputStream.read(buffer))) {

                if (mIsPause) {
                    if (mApkRequest != null) {
                        mApkRequest.pause();
                    }
                    return PAUSE;
                }
                if (mIsStop) {
                    if (mApkRequest != null) {
                        mApkRequest.stop();
                    }
                    return STOP;
                }
                // write data to temp file
                tempRaf.write(buffer, 0, num);
                writeLength += num;

                if (mApkRequest != null) {
                    mApkRequest.progress = writeLength;
                }
                // write progress to log file
                logRaf.setLength(0);
                logRaf.write(String.valueOf(writeLength).getBytes());
                // callback
                if (mDownloadListener != null) {
                    mDownloadListener.onProgress(mApkRequest, writeLength, downloadSize);
                }
            }

            //download success
            tempFile.renameTo(new File(resourcePath + SUFFIX_APK));
            //delete log file
            if (logFile.exists()) {
                logFile.delete();
            }

            return SUCCESS;
        } catch (Throwable e) {
            e.printStackTrace();
            mFailMsg = e.getMessage();

        } finally {

            try {
                if (tempRaf != null) {
                    tempRaf.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (logRaf != null) {
                    logRaf.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return FAIL;
    }

    private void readLogFile() {
        File logFile = new File(FileUtils.getResourcePath(mURL) + SUFFIX_LOG);
        File tempFile = new File(FileUtils.getResourcePath(mURL) + SUFFIX_TEMP);
        if (!logFile.exists() || !tempFile.exists()) {
            try {
                logFile.delete();
                tempFile.delete();
            } catch (Throwable e) {
            }
            return;
        }

        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {
            fis = new FileInputStream(logFile);
            isr = new InputStreamReader(fis);
            br = new BufferedReader(isr);
            String content = br.readLine();

            if (!TextUtils.isEmpty(content)) {//
                mStartPos = Long.valueOf(content);

                if (mStartPos > tempFile.length()) {
                    mStartPos = 0;
                } else {
                    downloadSize = tempFile.length();
                }
                CommonLogUtil.i(TAG, "readLogFile: startPost -> " + mStartPos + ", downloadSize -> " + downloadSize);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
                if (isr != null) {
                    isr.close();
                }
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    protected boolean isUseSingleThread() {
        return true;
    }

    protected int getConnectTimeout() {
        return 60000;
    }

    protected int getReadTimeout() {
        return 20000;
    }


    protected void onConnectTimeout(ConnectTimeoutException e) {

        onLoadFailedCallback(ApkErrorCode.get(ApkErrorCode.exception, e.getMessage()));
    }

    protected void onErrorCallback(ApkError error) {
        CommonLogUtil.d(TAG, "url: " + mURL);
        onLoadFailedCallback(error);
    }

    protected void onCancelCallback() {
        CommonLogUtil.d(TAG, "url: " + mURL);
        if (mDownloadListener != null) {
            mDownloadListener.onCancel(mApkRequest, writeLength, downloadSize, mStatus);
        }
    }

    protected void onLoadFinishCallback() {
        CommonLogUtil.d(TAG, "url: " + mURL);
        //callback
        if (mDownloadListener != null) {
            mDownloadListener.onSuccess(mApkRequest, mApkRequest.downloadTime);
        }
    }

    protected void onLoadFailedCallback(ApkError error) {
        CommonLogUtil.d(TAG, "download failed --> " + mURL + "(" + error.getDesc() + ")");
        //callback
        if (mDownloadListener != null) {
            mDownloadListener.onFailed(mApkRequest, error.printStackTrace());
        }
    }

}
