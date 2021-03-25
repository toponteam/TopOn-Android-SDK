/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.buiness;

import com.anythink.basead.entity.OfferClickResult;

import java.util.concurrent.ConcurrentHashMap;

public class OfferClickResultManager {
//    public static final int MYOFFER_AD = 1;
//    public static final int ADX_AD = 2;
//    public static final int ONLINE_AD = 3;

    public static final String TAG = OfferClickResultManager.class.getSimpleName();
    private static OfferClickResultManager sInstance;

    ConcurrentHashMap<String, OfferClickResult> mOfferClickResultMap;

    private OfferClickResultManager() {
        mOfferClickResultMap = new ConcurrentHashMap<>();
    }

    public synchronized static OfferClickResultManager getInstance() {
        if (sInstance == null) {
            sInstance = new OfferClickResultManager();
        }
        return sInstance;
    }

    public void putOfferClickResult(int offerType, String offerId, OfferClickResult offerClickResult) {
        mOfferClickResultMap.put(offerType + offerId, offerClickResult);
    }

    public OfferClickResult getOfferClickResult(int offerType, String offerId) {
        return mOfferClickResultMap.get(offerType + offerId);
    }

}
