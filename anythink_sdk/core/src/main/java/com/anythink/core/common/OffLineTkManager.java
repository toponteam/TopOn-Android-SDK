package com.anythink.core.common;


import com.anythink.core.api.AdError;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.db.CommonSDKDBHelper;
import com.anythink.core.common.net.OnHttpLoaderListener;
import com.anythink.core.common.net.socket.SocketUploadData;
import com.anythink.core.common.net.socket.TrackingSocketData;
import com.anythink.core.common.utils.CommonLogUtil;
import com.anythink.core.common.utils.CommonMD5;
import com.anythink.core.common.db.FailRequestInfoDao;
import com.anythink.core.common.entity.FailRequestInfo;
import com.anythink.core.common.net.ReSendRequestLoader;

import java.util.List;

public class OffLineTkManager {
    private String TAG = OffLineTkManager.class.getSimpleName();
    private static OffLineTkManager sInstance;


    private OffLineTkManager() {

    }

    public synchronized static OffLineTkManager getInstance() {
        if (sInstance == null) {
            sInstance = new OffLineTkManager();
        }
        return sInstance;
    }

    /**
     * Save Fail-request
     *
     * @param requestType
     * @param requestUrl
     * @param headerString
     * @param content
     */
    public void saveRequestFailInfo(int requestType, String requestUrl, String headerString, String content) {
        FailRequestInfo failRequestInfo = new FailRequestInfo();
        failRequestInfo.requestType = requestType;
        failRequestInfo.requestUrl = requestUrl;
        failRequestInfo.headerJSONString = headerString;
        failRequestInfo.content = content;
        failRequestInfo.time = System.currentTimeMillis();
        failRequestInfo.id = CommonMD5.getMD5(requestUrl + failRequestInfo.time + content != null ? content : "");
        CommonLogUtil.e(TAG, "save request:" + requestUrl + "--content:" + content);
        FailRequestInfoDao.getInstance(CommonSDKDBHelper.getInstance(SDKContext.getInstance().getContext())).insertOrUpdate(failRequestInfo);
    }

    int mRequestCount = 0;

    /**
     * Start to re-send
     */
    public synchronized void tryToReSendRequest() {
        if (mRequestCount == 0) {
            /**Limit 10 Tracking**/
            List<FailRequestInfo> failRequestInfos = FailRequestInfoDao.getInstance(CommonSDKDBHelper.getInstance(SDKContext.getInstance().getContext())).queryRequestInfo(10);

            if (failRequestInfos != null && failRequestInfos.size() > 0) {
                mRequestCount = failRequestInfos.size();
                CommonLogUtil.e(TAG, "neet to send request count:" + mRequestCount);
                for (final FailRequestInfo failRequestInfo : failRequestInfos) {
                    /**Remove the log after 7 day**/
                    if (System.currentTimeMillis() - failRequestInfo.time >= 7 * 24 * 60 * 60 * 1000L) {
                        mRequestCount--;
                        FailRequestInfoDao.getInstance(CommonSDKDBHelper.getInstance(SDKContext.getInstance().getContext())).delete(failRequestInfo);
                        continue;
                    }

                    if (failRequestInfo.requestType == 3) { //TCP
                        TrackingSocketData trackingSocketData = new TrackingSocketData(failRequestInfo.content);
                        trackingSocketData.startToUpload(new SocketUploadData.SocketListener() {
                            @Override
                            public void onSuccess(Object result) {
                                FailRequestInfoDao.getInstance(CommonSDKDBHelper.getInstance(SDKContext.getInstance().getContext())).delete(failRequestInfo);
                                mRequestCount--;
                            }

                            @Override
                            public void onError(Throwable throwable) {
                                mRequestCount--;
                            }
                        });

                    } else {
                        final ReSendRequestLoader reSendRequestLoader = new ReSendRequestLoader(failRequestInfo);
                        reSendRequestLoader.start(0, new OnHttpLoaderListener() {
                            @Override
                            public void onLoadStart(int reqCode) {

                            }

                            @Override
                            public void onLoadFinish(int reqCode, Object result) {
                                FailRequestInfoDao.getInstance(CommonSDKDBHelper.getInstance(SDKContext.getInstance().getContext())).delete(reSendRequestLoader.getRequestInfo());
                                mRequestCount--;
                            }

                            @Override
                            public void onLoadError(int reqCode, String msg, AdError errorCode) {
                                mRequestCount--;
                            }

                            @Override
                            public void onLoadCanceled(int reqCode) {
                                mRequestCount--;
                            }
                        });
                    }

                }
            } else {
                CommonLogUtil.e(TAG, "neet to send request count:" + 0);
            }

        }

    }
}
