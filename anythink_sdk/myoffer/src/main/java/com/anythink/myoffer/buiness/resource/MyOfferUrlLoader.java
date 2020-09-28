package com.anythink.myoffer.buiness.resource;

import com.anythink.core.common.track.AgentEventManager;
import com.anythink.core.common.res.image.ResourceDownloadBaseUrlLoader;
import com.anythink.core.common.utils.task.TaskManager;
import com.anythink.core.common.utils.task.Worker;
import com.anythink.myoffer.buiness.MyOfferResourceManager;
import com.anythink.network.myoffer.MyOfferErrorCode;

import java.io.InputStream;
import java.util.Map;

/**
 * MyOffer URL Resource Loader
 */
class MyOfferUrlLoader extends ResourceDownloadBaseUrlLoader {

    private String mPlacementId;
    private boolean mIsPreLoad;
    private boolean mIsVideo;
    private String mOfferId;

    public MyOfferUrlLoader(String placementId, boolean isPreLoad, String offerId, String url, boolean isVideo) {
        super(url);
        this.mPlacementId = placementId;
        this.mIsPreLoad = isPreLoad;
        this.mIsVideo = isVideo;
        this.mOfferId = offerId;
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
        return MyOfferResourceManager.getInstance().writeToDiskLruCache(mURL, inputStream);
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
            AgentEventManager.myOfferVideoUrlDownloadEvent(mPlacementId, mOfferId, mURL, "1"
                    , downloadSize, null, downloadStartTime, downloadEndTime);
        }
        MyOfferUrlLoadManager.getInstance().notifyDownloadSuccess(mURL);
    }

    @Override
    protected void onLoadFailedCallback(String errorCode, String erroMsg) {
        if (mIsVideo) {
            AgentEventManager.myOfferVideoUrlDownloadEvent(mPlacementId, mOfferId, mURL, "0"
                    , downloadSize, erroMsg, downloadStartTime, 0);
        }
        MyOfferUrlLoadManager.getInstance().notifyDownloadFailed(mURL, MyOfferErrorCode.get(errorCode, erroMsg));
    }
}
