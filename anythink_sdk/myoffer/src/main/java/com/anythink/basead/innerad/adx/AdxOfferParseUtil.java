/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.innerad.adx;

import com.anythink.core.common.base.Const;
import com.anythink.core.common.entity.AdxOffer;
import com.anythink.core.common.entity.OwnBaseAdSetting;
import com.anythink.core.common.entity.OwnBaseAdTrackObject;

import org.json.JSONArray;
import org.json.JSONObject;

public class AdxOfferParseUtil {

    public static final AdxOffer parseOffer(String bidId, JSONObject jsonObject) {
        try {
            JSONObject dataObject = jsonObject.optJSONObject(Const.API.JSON_DATA);
            if (dataObject == null) {
                if (!jsonObject.has("seatbid")) {
                    return null;
                } else {
                    dataObject = jsonObject;
                }
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
            adxOffer.setUnitType(adxObject.optInt("unit_type"));
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

            /**v5.7.3**/
            adxOffer.setCreativeType(adxObject.optInt("crt_type", 1));
            adxOffer.setImageUrlList(adxObject.optString("img_list"));
            adxOffer.setBannerXhtml(adxObject.optString("banner_xhtml"));//

            /**5.7.7**/
            adxOffer.setOfferFirmId(adxObject.optInt("offer_firm_id"));
            adxOffer.setJumpUrl(adxObject.optString("jump_url"));


            OwnBaseAdSetting adxAdSetting = OwnBaseAdSetting.parseAdSetting(adxObject.optString("ctrl"));
            adxOffer.setAdSetting(adxAdSetting);

            OwnBaseAdTrackObject adxTrackObject = OwnBaseAdTrackObject.parseAdxTrackObject(adxObject.optString("tk"));
            adxOffer.setTrackObject(adxTrackObject);


            return adxOffer;
        } catch (Throwable e) {
            if (Const.DEBUG) {
                e.printStackTrace();
            }
        }

        return null;
    }
}
