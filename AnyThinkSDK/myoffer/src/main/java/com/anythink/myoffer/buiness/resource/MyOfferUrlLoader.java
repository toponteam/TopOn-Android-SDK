package com.anythink.myoffer.buiness.resource;

import com.anythink.core.common.track.AgentEventManager;
import com.anythink.network.myoffer.MyOfferError;

import java.util.Map;

/**
 * MyOffer URL Resource Loader
 */
class MyOfferUrlLoader extends MyOfferBaseUrlLoader {

    private boolean mIsPreLoad;
    private boolean mIsVideo;
    private String mOfferId;

    public MyOfferUrlLoader(boolean isPreLoad, String offerId, String url, boolean isVideo) {
        super(url);
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

    /**
     * Preload use single thread
     */
    protected boolean isUseSingleThread() {
        return mIsPreLoad;
    }

    @Override
    protected void onLoadFinishCallback() {
        super.onLoadFinishCallback();
        if (mIsVideo) {
            AgentEventManager.myOfferVideoUrlDownloadEvent(mOfferId, mURL, "1"
                    , downloadSize, null, downloadStartTime, downloadEndTime);
        }
    }

    @Override
    protected void onLoadFailedCallback(MyOfferError error) {
        super.onLoadFailedCallback(error);
        if (mIsVideo) {
            AgentEventManager.myOfferVideoUrlDownloadEvent(mOfferId, mURL, "0"
                    , downloadSize, error.printStackTrace(), downloadStartTime, 0);
        }
    }
}
