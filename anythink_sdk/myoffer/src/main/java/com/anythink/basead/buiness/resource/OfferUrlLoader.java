/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.buiness.resource;

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

    public OfferUrlLoader(String placementId, boolean isPreLoad, String offerId, String url, boolean isVideo, int offerType) {
        super(url);
        this.mPlacementId = placementId;
        this.mIsPreLoad = isPreLoad;
        this.mIsVideo = isVideo;
        this.mOfferId = offerId;
        this.mOfferType = offerType;
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
            AgentEventManager.offerVideoUrlDownloadEvent(mPlacementId, mOfferId, mURL, "1"
                    , downloadSize, null, downloadStartTime, downloadEndTime, mOfferType);
        }
        OfferUrlLoadManager.getInstance().notifyDownloadSuccess(mURL);
    }

    @Override
    protected void onLoadFailedCallback(String errorCode, String erroMsg) {
        if (mIsVideo) {
            AgentEventManager.offerVideoUrlDownloadEvent(mPlacementId, mOfferId, mURL, "0"
                    , downloadSize, erroMsg, downloadStartTime, 0, mOfferType);
        }
        OfferUrlLoadManager.getInstance().notifyDownloadFailed(mURL, OfferErrorCode.get(errorCode, erroMsg));
    }
}
