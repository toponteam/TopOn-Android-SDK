package com.anythink.myoffer.buiness.resource;

import com.anythink.core.common.base.Const;
import com.anythink.core.common.utils.CommonLogUtil;
import com.anythink.myoffer.buiness.MyOfferResourceManager;
import com.anythink.myoffer.buiness.resource.task.TaskManager;
import com.anythink.myoffer.buiness.resource.task.Worker;
import com.anythink.network.myoffer.MyOfferError;
import com.anythink.network.myoffer.MyOfferErrorCode;

import org.apache.http.conn.ConnectTimeoutException;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Map;


/**
 * MyOffer Downloader
 */
abstract class MyOfferBaseUrlLoader {

    private static final String TAG = "MyOfferBaseUrlLoader";

    protected String mURL;

    protected boolean mIsStop;

    public MyOfferBaseUrlLoader(String url) {
        this.mURL = url;
    }

    public void start() {
        mIsStop = false;
        load();
    }


    public void stop() {
        mIsStop = true;
    }

    protected long downloadStartTime;
    protected long downloadEndTime;
    protected long downloadSize;

    protected abstract Map<String, String> onPrepareHeaders();


    protected abstract void onErrorAgent(String mURL, String msg);


    private void load() {

        //写worker
        Worker worker = new Worker() {

            @Override
            public void work() {
                CommonLogUtil.d(TAG, "start download url -> " + mURL);

                try {
                    doUrlConnect(mURL);

                } catch (OutOfMemoryError | StackOverflowError e) {
                    System.gc();
                    CommonLogUtil.e(TAG, e.getMessage());

                    String msg = MyOfferErrorCode.fail_connect;
                    if (e.getMessage() != null) {
                        msg = e.getMessage();
                    }
                    onLoadFailedCallback(MyOfferErrorCode.get(MyOfferErrorCode.exception, msg));
                } catch (Exception e) {
                    CommonLogUtil.e(TAG, e.getMessage());

                    String msg = MyOfferErrorCode.fail_connect;
                    if (e.getMessage() != null) {
                        msg = e.getMessage();
                    }
                    onLoadFailedCallback(MyOfferErrorCode.get(MyOfferErrorCode.exception, msg));

                }
            }

            /**
             * @param urlStr
             */
            private void doUrlConnect(String urlStr) {

                downloadStartTime = System.currentTimeMillis();

                HttpURLConnection httpConn = null;
                try {
                    CommonLogUtil.i(TAG, "REQUEST URL: " + urlStr);

                    URL url = new URL(urlStr);

                    httpConn = (HttpURLConnection) url.openConnection();

                    httpConn.setInstanceFollowRedirects(false);

                    //Header
                    Map<String, String> headers = onPrepareHeaders();
                    if (headers != null && headers.size() > 0) {
                        for (String key : headers.keySet()) {
                            httpConn.addRequestProperty(key, headers.get(key));
                            CommonLogUtil.i(TAG, "REQUEST ADDED HEADER: \n" + key + "  :  " + headers.get(key));
                        }
                    }

                    if (mIsStop) {
                        onCancelCallback();
                        return;
                    }

                    httpConn.setConnectTimeout(getConnectTimeout());
                    httpConn.connect();

                    final int statusCode = httpConn.getResponseCode();
                    if (statusCode != HttpURLConnection.HTTP_OK) {

                        CommonLogUtil.e(TAG, "http respond status code is " + statusCode + " ! url=" + urlStr);
                        if (statusCode == HttpURLConnection.HTTP_MOVED_TEMP) {

                            if (!mIsStop) {
                                String location = httpConn.getHeaderField("Location");
                                if (location != null) {
                                    if (!location.startsWith("http")) {
                                        location = urlStr + location;
                                    }

                                    doUrlConnect(location);
                                }

                            } else {
                                onCancelCallback();
                            }
                            return;
                        } else {
                            onErrorCallback(MyOfferErrorCode.get(MyOfferErrorCode.httpStatuException, httpConn.getResponseMessage()));
                            return;
                        }

                    }

                    if (mIsStop) {
                        onCancelCallback();
                        return;
                    }
                    downloadSize = httpConn.getContentLength();
                    InputStream inputStream = httpConn.getInputStream();
                    boolean save_success = MyOfferResourceManager.getInstance().writeToDiskLruCache(mURL, inputStream);

                    if (inputStream != null) {
                        inputStream.close();
                    }

                    downloadEndTime = System.currentTimeMillis();

                    if (save_success) {
                        CommonLogUtil.d(TAG, "download success --> " + mURL);
                        onLoadFinishCallback();
                    } else {
                        CommonLogUtil.d(TAG, "download fail --> " + mURL);
                        onLoadFailedCallback(MyOfferErrorCode.get(MyOfferErrorCode.exception, MyOfferErrorCode.fail_save));
                    }

                } catch (SocketTimeoutException e) {
                    onErrorCallback(MyOfferErrorCode.get(MyOfferErrorCode.timeOutError, e.getMessage()));
                    CommonLogUtil.e(TAG, e.toString());
                } catch (ConnectTimeoutException e) {
                    onConnectTimeout(e);
                } catch (OutOfMemoryError e) {
                    System.gc();
                    CommonLogUtil.e(TAG, e.getMessage());

                    String msg = MyOfferErrorCode.fail_connect;
                    if (e.getMessage() != null) {
                        msg = e.getMessage();
                    }
                    onErrorCallback(MyOfferErrorCode.get(MyOfferErrorCode.exception, msg));
                } catch (StackOverflowError e) {
                    System.gc();
                    CommonLogUtil.e(TAG, e.getMessage());

                    String msg = MyOfferErrorCode.fail_connect;
                    if (e.getMessage() != null) {
                        msg = e.getMessage();
                    }
                    onErrorCallback(MyOfferErrorCode.get(MyOfferErrorCode.exception, msg));
                } catch (Error e) {
                    System.gc();
                    CommonLogUtil.e(TAG, e.getMessage());

                    String msg = MyOfferErrorCode.fail_connect;
                    if (e.getMessage() != null) {
                        msg = e.getMessage();
                    }
                    onErrorCallback(MyOfferErrorCode.get(MyOfferErrorCode.exception, msg));
                } catch (Exception e) {
                    CommonLogUtil.e(TAG, e.getMessage());

                    String msg = MyOfferErrorCode.fail_connect;
                    if (e.getMessage() != null) {
                        msg = e.getMessage();
                    }
                    if (Const.DEBUG) {
                        e.printStackTrace();
                    }

                    onErrorCallback(MyOfferErrorCode.get(MyOfferErrorCode.exception, msg));

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

        onLoadFailedCallback(MyOfferErrorCode.get(MyOfferErrorCode.exception, e.getMessage()));
        onErrorAgent(mURL, e.getMessage());
    }

    protected void onErrorCallback(MyOfferError error) {
        CommonLogUtil.d(TAG, "thread - " + Thread.currentThread().getId() + ", url: " + mURL);
        onLoadFailedCallback(error);
        onErrorAgent(mURL, error.getDesc());
    }

    protected void onCancelCallback() {
        CommonLogUtil.d(TAG, "thread - " + Thread.currentThread().getId() + ", url: " + mURL);
        onLoadFailedCallback(MyOfferErrorCode.get(MyOfferErrorCode.exception, MyOfferErrorCode.fail_load_cannel));
    }

    protected void onLoadFinishCallback() {
        CommonLogUtil.d(TAG, "thread - " + Thread.currentThread().getId() + ", url: " + mURL);
        MyOfferUrlLoadManager.getInstance().notifyDownloadSuccess(mURL);
    }

    protected void onLoadFailedCallback(MyOfferError error) {
        CommonLogUtil.d(TAG, "download failed --> " + mURL + "(" + error.getDesc() + ")");
        MyOfferUrlLoadManager.getInstance().notifyDownloadFailed(mURL, error);
    }

}
