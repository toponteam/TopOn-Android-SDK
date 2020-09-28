package com.anythink.core.common.res.image;

import com.anythink.core.common.utils.CommonLogUtil;
import com.anythink.core.common.utils.task.Worker;

import org.apache.http.conn.ConnectTimeoutException;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Map;


/**
 * Downloader
 */
public abstract class ResourceDownloadBaseUrlLoader {

    private final String TAG = getClass().getSimpleName();

    protected String mURL;

    protected boolean mIsStop;

    public ResourceDownloadBaseUrlLoader(String url) {
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
                try {
                    doUrlConnect(mURL);

                } catch (OutOfMemoryError | StackOverflowError e) {
                    System.gc();
                    onLoadFailedCallback(ResourceDownloadError.exception_code, e.getMessage());
                } catch (Exception e) {
                    CommonLogUtil.e(TAG, e.getMessage());
                    onLoadFailedCallback(ResourceDownloadError.exception_code, e.getMessage());

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
                        onLoadFailedCallback(ResourceDownloadError.cancel_code, "Task had been canceled.");
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
                                onLoadFailedCallback(ResourceDownloadError.cancel_code, "Task had been canceled.");
                            }
                            return;
                        } else {
                            onLoadFailedCallback(ResourceDownloadError.exception_code, httpConn.getResponseMessage());
                            return;
                        }

                    }

                    if (mIsStop) {
                        onLoadFailedCallback(ResourceDownloadError.cancel_code, "Task had been canceled.");
                        return;
                    }
                    downloadSize = httpConn.getContentLength();
                    InputStream inputStream = httpConn.getInputStream();
                    boolean save_success = saveHttpResource(inputStream);

                    if (inputStream != null) {
                        inputStream.close();
                    }

                    downloadEndTime = System.currentTimeMillis();

                    if (save_success) {
                        CommonLogUtil.d(TAG, "download success --> " + mURL);
                        onLoadFinishCallback();
                    } else {
                        CommonLogUtil.d(TAG, "download fail --> " + mURL);
                        onLoadFailedCallback(ResourceDownloadError.exception_code, ResourceDownloadError.fail_save_msg);
                    }

                } catch (SocketTimeoutException e) {
                    onLoadFailedCallback(ResourceDownloadError.exception_code, e.getMessage());
                    CommonLogUtil.e(TAG, e.toString());
                } catch (ConnectTimeoutException e) {
                    onLoadFailedCallback(ResourceDownloadError.exception_code, e.getMessage());
                } catch (OutOfMemoryError e) {
                    System.gc();
                    CommonLogUtil.e(TAG, e.getMessage());
                    onLoadFailedCallback(ResourceDownloadError.exception_code, e.getMessage());
                } catch (StackOverflowError e) {
                    System.gc();
                    CommonLogUtil.e(TAG, e.getMessage());

                    onLoadFailedCallback(ResourceDownloadError.exception_code, e.getMessage());
                } catch (Error e) {
                    System.gc();
                    CommonLogUtil.e(TAG, e.getMessage());

                    onLoadFailedCallback(ResourceDownloadError.exception_code, e.getMessage());
                } catch (Exception e) {
                    CommonLogUtil.e(TAG, e.getMessage());
                    onLoadFailedCallback(ResourceDownloadError.exception_code, e.getMessage());
                } finally {
                    if (httpConn != null) {
                        httpConn.disconnect();
                    }
                }

            }

        };

        startWorker(worker);

    }


    protected int getConnectTimeout() {
        return 60000;
    }

    protected int getReadTimeout() {
        return 20000;
    }

    /**Save HttpResource**/
    protected abstract boolean saveHttpResource(InputStream inputStream);
    /**Abstract Run the work**/
    protected abstract void startWorker(Worker worker);
    /**Abstract handle load success**/
    protected abstract void onLoadFinishCallback();
    /**Abstract handle load fail**/
    protected abstract void onLoadFailedCallback(String errorCode, String errorMsg);

}
