package com.anythink.myoffer.buiness.resource;

import com.anythink.network.myoffer.MyOfferError;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Url resource state register
 */
class MyOfferUrlLoadManager {

    public static final String TAG = MyOfferUrlLoadManager.class.getSimpleName();

    private static MyOfferUrlLoadManager sInstance;
    private MyOfferUrlLoadManager() {

    }

    public synchronized static MyOfferUrlLoadManager getInstance() {
        if(sInstance == null){
            sInstance = new MyOfferUrlLoadManager();
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
        void onResourceLoadFailed(String url, MyOfferError error);
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

    public void notifyDownloadFailed(String url, MyOfferError error) {
        if (mResourceLoadResultList != null) {
            for (ResourceLoadResult resourceLoadResult : mResourceLoadResultList) {
                resourceLoadResult.onResourceLoadFailed(url, error);
            }
        }
    }

}
