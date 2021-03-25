/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.innerad.onlineapi;

import android.content.Context;

import com.anythink.core.common.base.Const;
import com.anythink.core.common.entity.BaseAdRequestInfo;
import com.anythink.core.common.utils.SPUtil;

public class OnlineApiAdCacheManager {
    private static OnlineApiAdCacheManager sInstance;

    private OnlineApiAdCacheManager() {

    }

    public synchronized static OnlineApiAdCacheManager getInstance() {
        if (sInstance == null) {
            sInstance = new OnlineApiAdCacheManager();
        }
        return sInstance;
    }

    public void saveOnlineApiOffer(Context context, String onelinapiId, String adxOfferData) {
        SPUtil.putString(context, Const.ONLINEAPI_SPU_FILE_NAME, onelinapiId, adxOfferData);
    }

    public String getOnlineApiOffer(Context context, String onelinapiId) {
        return SPUtil.getString(context, Const.ONLINEAPI_SPU_FILE_NAME, onelinapiId, "");
    }

    public void removeOnlineApiOfferInfo(Context context, String onelinapiId) {
        SPUtil.remove(context, Const.ONLINEAPI_SPU_FILE_NAME, onelinapiId);
    }

    /**
     * Placement + Adsource + NetworkFirmId
     *
     * @param onlineApiRequestInfo
     * @return
     */
    public String getOnlineApiSaveId(BaseAdRequestInfo onlineApiRequestInfo) {
        return onlineApiRequestInfo.placementId + "_" + onlineApiRequestInfo.adsourceId + "_" + onlineApiRequestInfo.networkFirmId;
    }
}
