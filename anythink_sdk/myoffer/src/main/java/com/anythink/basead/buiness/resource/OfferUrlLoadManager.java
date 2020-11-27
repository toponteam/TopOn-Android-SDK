/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.buiness.resource;

import com.anythink.basead.entity.OfferError;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Url resource state register
 */
class OfferUrlLoadManager {

    public static final String TAG = OfferUrlLoadManager.class.getSimpleName();

    private static OfferUrlLoadManager sInstance;
    private OfferUrlLoadManager() {

    }

    public synchronized static OfferUrlLoadManager getInstance() {
        if(sInstance == null){
            sInstance = new OfferUrlLoadManager();
        }
        return sInstance;
    }

    private List<ResourceLoadResult> mResourceLoadResultList = new CopyOnWriteArrayList<>();

    public interface ResourceLoadResult {
        /**
         * Success Callback
         */
        void onResourceLoadSuccess(String url);
        /**
         * Fail Callback
         */
        void onResourceLoadFailed(String url, OfferError error);
    }

    /**
     * Download Url Register
     */
    public synchronized void register(ResourceLoadResult result) {
        this.mResourceLoadResultList.add(result);
    }

    /**
     * Download Url Unregister
     */
    public synchronized void unRegister(ResourceLoadResult result) {
        int size = mResourceLoadResultList.size();
        int removeIndex = -1;
        for (int i = 0; i < size; i++) {
            if(result == mResourceLoadResultList.get(i)) {
                removeIndex = i;
                break;
            }
        }
        if(removeIndex != -1) {
            this.mResourceLoadResultList.remove(removeIndex);
        }
    }

    public void notifyDownloadSuccess(String url) {
        if (mResourceLoadResultList != null) {
            for (ResourceLoadResult resourceLoadResult : mResourceLoadResultList) {
                resourceLoadResult.onResourceLoadSuccess(url);
            }
        }
    }

    public void notifyDownloadFailed(String url, OfferError error) {
        if (mResourceLoadResultList != null) {
            for (ResourceLoadResult resourceLoadResult : mResourceLoadResultList) {
                resourceLoadResult.onResourceLoadFailed(url, error);
            }
        }
    }

}
