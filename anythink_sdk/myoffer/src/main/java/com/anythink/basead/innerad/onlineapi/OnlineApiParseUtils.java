/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.innerad.onlineapi;

import com.anythink.core.common.base.Const;
import com.anythink.core.common.entity.BaseAdRequestInfo;
import com.anythink.core.common.entity.OnlineApiOffer;
import com.anythink.core.common.entity.OwnBaseAdSetting;
import com.anythink.core.common.entity.OwnBaseAdTrackObject;

import org.json.JSONArray;
import org.json.JSONObject;

public class OnlineApiParseUtils {
    public static final String SDK_UPDATE_TIME_KEY = "sdk_updatetime";

    public static final OnlineApiOffer parseOffer(BaseAdRequestInfo onlineApiRequestInfo, JSONObject jsonObject) {
        try {
            JSONObject dataObject = jsonObject.optJSONObject(Const.API.JSON_DATA);
            if (dataObject == null) {
                return null;
            }


            JSONArray offerArray = dataObject.optJSONArray("offers");
            JSONObject offerObject = offerArray.optJSONObject(0);


            OnlineApiOffer onlineApiOffer = new OnlineApiOffer();
            onlineApiOffer.setNetworkFirmId(onlineApiRequestInfo.networkFirmId);
            onlineApiOffer.setOfferId(offerObject.optString("oid"));
            onlineApiOffer.setCreativeId(offerObject.optString("c_id"));
            onlineApiOffer.setPkgName(offerObject.optString("pkg"));
            onlineApiOffer.setTitle(offerObject.optString("title"));
            onlineApiOffer.setDesc(offerObject.optString("desc"));
            onlineApiOffer.setRating(offerObject.optInt("rating"));
            onlineApiOffer.setIconUrl(offerObject.optString("icon_u"));
            onlineApiOffer.setEndCardImageUrl(offerObject.optString("full_u"));
            onlineApiOffer.setUnitType(offerObject.optInt("unit_type"));
            onlineApiOffer.setAdChoiceUrl(offerObject.optString("tp_logo_u"));
            onlineApiOffer.setCtaText(offerObject.optString("cta"));
            onlineApiOffer.setVideoUrl(offerObject.optString("video_u"));
            onlineApiOffer.setVideoLength(offerObject.optInt("video_l"));
            onlineApiOffer.setVideoScreen(offerObject.optString("video_r"));
            onlineApiOffer.setEndcardUrl(offerObject.optString("ec_u"));
            onlineApiOffer.setPreviewUrl(offerObject.optString("store_u"));
            onlineApiOffer.setClickType(offerObject.optInt("link_type"));
            onlineApiOffer.setClickUrl(offerObject.optString("click_u"));
            onlineApiOffer.setDeeplinkUrl(offerObject.optString("deeplink"));
            onlineApiOffer.setDeeplinkTarget(offerObject.optInt("r_target"));
            onlineApiOffer.setExpireTime(offerObject.optLong("expire"));
            onlineApiOffer.setAdLogoTitle(offerObject.optString("ad_logo_title"));

            onlineApiOffer.setCreativeType(offerObject.optInt("crt_type", 1));
            onlineApiOffer.setImageUrlList(offerObject.optString("img_list"));
            onlineApiOffer.setBannerXhtml(offerObject.optString("banner_xhtml"));//
            onlineApiOffer.setUpdateTime(jsonObject.optLong(SDK_UPDATE_TIME_KEY));

            /**5.7.7**/
            onlineApiOffer.setOfferFirmId(offerObject.optInt("offer_firm_id"));
            onlineApiOffer.setJumpUrl(offerObject.optString("jump_url"));

            OwnBaseAdSetting ownBaseAdSetting = OwnBaseAdSetting.parseAdSetting(offerObject.optString("ctrl"));
            onlineApiOffer.setAdSetting(ownBaseAdSetting);

            OwnBaseAdTrackObject trackObject = OwnBaseAdTrackObject.parseAdxTrackObject(offerObject.optString("tk"));
            onlineApiOffer.setTrackObject(trackObject);


            return onlineApiOffer;
        } catch (Throwable e) {
            if (Const.DEBUG) {
                e.printStackTrace();
            }
        }

        return null;
    }

}
