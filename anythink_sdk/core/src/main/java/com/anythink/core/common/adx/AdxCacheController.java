/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.common.adx;

import android.content.Context;

import com.anythink.core.common.utils.SPUtil;

public class AdxCacheController {
    private static final String ADX_SPU_FILE_NAME = "adx_file";
    private static AdxCacheController sInstance;

    private AdxCacheController() {

    }

    public synchronized static AdxCacheController getInstance() {
        if (sInstance == null) {
            sInstance = new AdxCacheController();
        }
        return sInstance;
    }

    public void saveAdxOffer(Context context, String bidId, String adxOfferData) {
        SPUtil.putString(context, ADX_SPU_FILE_NAME, bidId, adxOfferData);
    }

    public String getAdxOffer(Context context, String bidId) {
        return SPUtil.getString(context, ADX_SPU_FILE_NAME, bidId, "");
    }

    public void removeAdxOfferInfo(Context context, String bidId) {
        SPUtil.remove(context, ADX_SPU_FILE_NAME, bidId);
    }
}
