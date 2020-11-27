/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.buiness;


import com.anythink.basead.buiness.resource.OfferResourceLoader;
import com.anythink.basead.buiness.resource.OfferResourceState;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.entity.BaseAdContent;
import com.anythink.core.common.entity.BaseAdSetting;
import com.anythink.core.common.entity.MyOfferAd;
import com.anythink.core.common.entity.MyOfferSetting;
import com.anythink.core.common.res.ResourceDiskCacheManager;
import com.anythink.core.common.res.ResourceEntry;
import com.anythink.core.common.utils.FileUtil;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

public class OfferResourceManager {

    public static final String TAG = OfferResourceManager.class.getSimpleName();
    private static OfferResourceManager sInstance;

    private OfferResourceManager() {
    }

    public synchronized static OfferResourceManager getInstance() {
        if (sInstance == null) {
            sInstance = new OfferResourceManager();
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
    public void load(String placementId, BaseAdContent myOfferAd, BaseAdSetting myOfferSetting, final OfferResourceLoader.ResourceLoaderListener listener) {
        load(placementId, false, myOfferAd, myOfferSetting, listener);
    }

    /**
     * download myoffer resource
     */
    public void load(String placementId, boolean isPreLoad, BaseAdContent myOfferAd, BaseAdSetting myOfferSetting, final OfferResourceLoader.ResourceLoaderListener listener) {
        OfferResourceLoader myOfferLoader = new OfferResourceLoader(placementId, isPreLoad, myOfferSetting.getOfferTimeout());
        myOfferLoader.load(myOfferAd, myOfferSetting, listener);
    }

    /**
     * Check if resource ready
     */
    public boolean isExist(MyOfferAd myOfferAd, MyOfferSetting myOfferSetting) {
        return OfferResourceState.isExist(myOfferAd, myOfferSetting);
    }


}
