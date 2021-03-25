/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.common.adx;

import android.content.Context;

import com.anythink.core.common.base.Const;
import com.anythink.core.common.utils.SPUtil;

public class AdxCacheController {
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
        SPUtil.putString(context, Const.SPU_ADX_FILE_NAME, bidId, adxOfferData);
    }

    public String getAdxOffer(Context context, String bidId) {
        return SPUtil.getString(context, Const.SPU_ADX_FILE_NAME, bidId, "");
    }

    public void removeAdxOfferInfo(Context context, String bidId) {
        SPUtil.remove(context, Const.SPU_ADX_FILE_NAME, bidId);
        SPUtil.remove(context, Const.SPU_ADX_FILE_NAME, bidId + Const.SPUKEY.SPU_OWN_AD_SUFFIX_WIN_NOTICE);
    }

    public void saveWinNotice(Context context, String bidId) {
        SPUtil.putInt(context, Const.SPU_ADX_FILE_NAME, bidId + Const.SPUKEY.SPU_OWN_AD_SUFFIX_WIN_NOTICE, 1);
    }

    public boolean isSendWinNotice(Context context, String bidId) {
        return SPUtil.getInt(context, Const.SPU_ADX_FILE_NAME, bidId + Const.SPUKEY.SPU_OWN_AD_SUFFIX_WIN_NOTICE, 0) == 1 ;
    }
}
