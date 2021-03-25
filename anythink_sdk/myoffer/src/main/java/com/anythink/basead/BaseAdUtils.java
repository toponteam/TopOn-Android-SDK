/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead;

import android.text.TextUtils;

import com.anythink.basead.innerad.OwnBaseAd;
import com.anythink.core.api.ATAdConst;
import com.anythink.core.common.entity.BaseAdContent;

import java.util.HashMap;
import java.util.Map;

public class BaseAdUtils {

    public static Map<String, Object> fillBaseAdCustomMap(OwnBaseAd ownBaseAd) {
        if (ownBaseAd != null) {
            return fillBaseAdCustomMap(ownBaseAd.getBaseAdContent());
        }
        return null;
    }

    public static Map<String, Object> fillBaseAdCustomMap(BaseAdContent baseAdContent) {
        if (baseAdContent != null) {
            Map<String, Object> adDetailMap = new HashMap<>();
            adDetailMap.put(ATAdConst.NETWORK_CUSTOM_KEY.OFFER_ID, baseAdContent.getOfferId());
            adDetailMap.put(ATAdConst.NETWORK_CUSTOM_KEY.CREATIVE_ID, baseAdContent.getCreativeId());
            adDetailMap.put(ATAdConst.NETWORK_CUSTOM_KEY.IS_DEEPLINK_OFFER, (TextUtils.isEmpty(baseAdContent.getJumpUrl()) && TextUtils.isEmpty(baseAdContent.getDeeplinkUrl())) ? 0 : 1);
            return adDetailMap;
        }
        return null;
    }
}
