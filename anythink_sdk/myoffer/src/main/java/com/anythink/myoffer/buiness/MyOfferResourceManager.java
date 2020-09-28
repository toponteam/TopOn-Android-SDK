package com.anythink.myoffer.buiness;


import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.entity.MyOfferAd;
import com.anythink.core.common.entity.MyOfferSetting;
import com.anythink.core.common.res.ResourceDiskCacheManager;
import com.anythink.core.common.res.ResourceEntry;
import com.anythink.core.common.utils.FileUtil;
import com.anythink.myoffer.buiness.resource.MyOfferLoader;
import com.anythink.myoffer.buiness.resource.MyOfferResourceState;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

public class MyOfferResourceManager {

    public static final String TAG = MyOfferResourceManager.class.getSimpleName();
    private static MyOfferResourceManager sInstance;

    private MyOfferResourceManager() {
    }

    public synchronized static MyOfferResourceManager getInstance() {
        if (sInstance == null) {
            sInstance = new MyOfferResourceManager();
        }
        return sInstance;
    }


    public boolean writeToDiskLruCache(String url, InputStream inputStream) {
        if (url == null || inputStream == null) {
            return false;
        }

        String resFileName = FileUtil.hashKeyForDisk(url);
        return ResourceDiskCacheManager.getInstance(SDKContext.getInstance().getContext()).saveNetworkInputStreamToFile(ResourceEntry.INTERNAL_CACHE_TYPE, resFileName, inputStream);


    }

    public FileInputStream getInputStream(String url) {
        String resFileName = FileUtil.hashKeyForDisk(url);
        return ResourceDiskCacheManager.getInstance(SDKContext.getInstance().getContext())
                .getFileInputStream(ResourceEntry.INTERNAL_CACHE_TYPE, resFileName);
    }


    /**
     * preload
     */
    public void preLoadOfferList(String placementId, List<MyOfferAd> myOfferAds, MyOfferSetting myOfferSetting) {
        if (myOfferAds == null) {
            return;
        }
        int size = myOfferAds.size();
        for (int i = 0; i < size; i++) {
            load(placementId, true, myOfferAds.get(i), myOfferSetting, null);
        }

    }

    /**
     * download myoffer resource
     */
    public void load(String placementId, MyOfferAd myOfferAd, MyOfferSetting myOfferSetting, final MyOfferLoader.MyOfferLoaderListener listener) {
        load(placementId, false, myOfferAd, myOfferSetting, listener);
    }

    /**
     * download myoffer resource
     */
    public void load(String placementId, boolean isPreLoad, MyOfferAd myOfferAd, MyOfferSetting myOfferSetting, final MyOfferLoader.MyOfferLoaderListener listener) {
        MyOfferLoader myOfferLoader = new MyOfferLoader(placementId, isPreLoad, myOfferSetting.getOfferTimeout());
        myOfferLoader.load(myOfferAd, myOfferSetting, listener);
    }

    /**
     * Check if resource ready
     */
    public boolean isExist(MyOfferAd myOfferAd, MyOfferSetting myOfferSetting) {
        return MyOfferResourceState.isExist(myOfferAd, myOfferSetting);
    }


}
