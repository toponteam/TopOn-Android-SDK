/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.adx.utils;

import com.anythink.core.common.base.Const;
import com.anythink.core.common.entity.AdxAdSetting;
import com.anythink.core.common.entity.AdxOffer;
import com.anythink.core.common.entity.AdxTrackObject;

import org.json.JSONArray;
import org.json.JSONObject;

public class AdxOfferParseUtil {

    public static final AdxOffer parseOffer(String bidId, JSONObject jsonObject) {
        try {
            JSONObject dataObject = jsonObject.optJSONObject(Const.API.JSON_DATA);
            if (dataObject == null) {
                return null;
            }


            JSONArray adxArray = dataObject.optJSONArray("seatbid");
            JSONObject adxObject = adxArray.optJSONObject(0);


            AdxOffer adxOffer = new AdxOffer();
            adxOffer.setBidId(bidId);
            adxOffer.setOfferId(adxObject.optString("oid"));
            adxOffer.setCreativeId(adxObject.optString("c_id"));
            adxOffer.setPkgName(adxObject.optString("pkg"));
            adxOffer.setTitle(adxObject.optString("title"));
            adxOffer.setDesc(adxObject.optString("desc"));
            adxOffer.setRating(adxObject.optInt("rating"));
            adxOffer.setIconUrl(adxObject.optString("icon_u"));
            adxOffer.setEndCardImageUrl(adxObject.optString("full_u"));
            adxOffer.setResourceType(adxObject.optInt("unit_type"));
            adxOffer.setAdChoiceUrl(adxObject.optString("tp_logo_u"));
            adxOffer.setCtaText(adxObject.optString("cta"));
            adxOffer.setVideoUrl(adxObject.optString("video_u"));
            adxOffer.setVideoLength(adxObject.optInt("video_l"));
            adxOffer.setVideoScreen(adxObject.optString("video_r"));
            adxOffer.setEndcardUrl(adxObject.optString("ec_u"));
            adxOffer.setPreviewUrl(adxObject.optString("store_u"));
            adxOffer.setClickType(adxObject.optInt("link_type"));
            adxOffer.setClickUrl(adxObject.optString("click_u"));
            adxOffer.setDeeplinkUrl(adxObject.optString("deeplink"));

            AdxAdSetting adxAdSetting = AdxAdSetting.parseAdxSetting(adxObject.optString("ctrl"));
            adxOffer.setAdxAdSetting(adxAdSetting);

            AdxTrackObject adxTrackObject = AdxTrackObject.parseAdxTrackObject(adxObject.optString("tk"));
            adxOffer.setAdxTrackObject(adxTrackObject);


            return adxOffer;
        } catch (Throwable e) {
            if (Const.DEBUG) {
                e.printStackTrace();
            }
        }

        return null;
    }
}
