/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.buiness.resource;

import android.text.TextUtils;

import com.anythink.basead.buiness.OfferAdFunctionUtil;
import com.anythink.basead.entity.UserOperateRecord;
import com.anythink.core.common.entity.BaseAdContent;
import com.anythink.core.common.track.AgentEventManager;
import com.anythink.core.common.res.image.ResourceDownloadBaseUrlLoader;
import com.anythink.core.common.utils.task.TaskManager;
import com.anythink.core.common.utils.task.Worker;
import com.anythink.basead.buiness.OfferResourceManager;
import com.anythink.basead.entity.OfferErrorCode;

import java.io.InputStream;
import java.util.Map;

/**
 * MyOffer URL Resource Loader
 */
class OfferUrlLoader extends ResourceDownloadBaseUrlLoader {

    private String mPlacementId;
    private boolean mIsPreLoad;
    private boolean mIsVideo;
    private String mOfferId;
    private int mOfferType;
    private BaseAdContent baseAdContent;

    public OfferUrlLoader(String placementId, boolean isPreLoad, BaseAdContent baseAdContent, String url) {
        super(url);
        this.baseAdContent = baseAdContent;
        this.mPlacementId = placementId;
        this.mIsPreLoad = isPreLoad;
        this.mIsVideo = TextUtils.equals(baseAdContent.getVideoUrl(), url);
        this.mOfferId = baseAdContent.getOfferId();
        this.mOfferType = baseAdContent.getOfferSourceType();
    }

    @Override
    protected Map<String, String> onPrepareHeaders() {
        return null;
    }

    @Override
    protected void onErrorAgent(String mURL, String msg) {

    }

    @Override
    protected boolean saveHttpResource(InputStream inputStream) {
        return OfferResourceManager.getInstance().writeToDiskLruCache(mURL, inputStream);
    }

    @Override
    protected void startWorker(Worker worker) {
        if (mIsPreLoad) {
            TaskManager.getInstance().run(worker, TaskManager.TYPE_PRELOAD_TASK);
        } else {
            TaskManager.getInstance().run(worker, TaskManager.TYPE_IMAGE_TYPE);
        }
    }


    @Override
    protected void onLoadFinishCallback() {
        if (mIsVideo) {
            OfferAdFunctionUtil.sendAdTracking(OfferAdFunctionUtil.VIDEO_DOWNLOAD_SUCCESS_TYPE, baseAdContent, new UserOperateRecord("", ""));
            AgentEventManager.offerVideoUrlDownloadEvent(mPlacementId, mOfferId, mURL, "1"
                    , downloadSize, null, downloadStartTime, downloadEndTime, mOfferType, downloadRealEndTime - downloadRealStartTime);
        }
        OfferUrlLoadManager.getInstance().notifyDownloadSuccess(mURL);
    }

    @Override
    protected void onLoadFailedCallback(String errorCode, String erroMsg) {
        if (mIsVideo) {
            AgentEventManager.offerVideoUrlDownloadEvent(mPlacementId, mOfferId, mURL, "0"
                    , downloadSize, erroMsg, downloadStartTime, 0, mOfferType, downloadRealEndTime - downloadRealStartTime);
        }
        OfferUrlLoadManager.getInstance().notifyDownloadFailed(mURL, OfferErrorCode.get(errorCode, erroMsg));
    }
}
