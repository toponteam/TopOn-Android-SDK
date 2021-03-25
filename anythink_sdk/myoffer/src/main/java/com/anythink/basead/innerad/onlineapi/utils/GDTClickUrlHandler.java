/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.innerad.onlineapi.utils;

import android.net.Uri;

import com.anythink.basead.entity.AdClickRecord;
import com.anythink.basead.entity.OfferClickResult;
import com.anythink.core.common.entity.BaseAdContent;
import com.anythink.core.common.entity.BaseAdRequestInfo;
import com.anythink.core.common.track.AgentEventManager;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GDTClickUrlHandler {

    public static OfferClickResult handleApkClickUrlResult(BaseAdRequestInfo requestInfo, BaseAdContent baseAdContent, String startUrl) {
        HttpURLConnection conn = null;
        try {
            URL serverUrl = new URL(startUrl);
            conn = (HttpURLConnection) serverUrl
                    .openConnection();
            conn.setRequestMethod("GET");
            // Must set to false. If not, it will auto redirect to Location
            conn.setInstanceFollowRedirects(false);

            conn.setConnectTimeout(30 * 1000);
            conn.connect();
            int responseCode = conn.getResponseCode();

            OfferClickResult offerClickResult = null;
            if (responseCode == 200) {
                InputStream inputStream = conn.getInputStream();
                InputStreamReader reader = new InputStreamReader(inputStream);
                BufferedReader input = new BufferedReader(reader);
                String s;
                StringBuilder sb = new StringBuilder();
                while ((s = input.readLine()) != null) {
                    sb.append(s);
                }

                JSONObject jsonObject = new JSONObject(sb.toString());
                JSONObject dataObject = jsonObject.optJSONObject("data");
                String dslink = dataObject.optString("dstlink");
                String clickId = dataObject.optString("clickid");
                offerClickResult = new OfferClickResult(dslink, "", clickId);
                if (input != null) {
                    input.close();
                }
                if (reader != null) {
                    reader.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }

                return offerClickResult;
            } else {
                AgentEventManager.sendClickFailAgent(requestInfo.placementId, baseAdContent.getOfferId(), baseAdContent.getOfferSourceType(), baseAdContent.getClickUrl(), startUrl, responseCode + "", "");
            }

        } catch (Exception e) {
            AgentEventManager.sendClickFailAgent(requestInfo.placementId, baseAdContent.getOfferId(), baseAdContent.getOfferSourceType(), baseAdContent.getClickUrl(), startUrl, "", e.getMessage());

        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return null;
    }

    public static String getBrandAdUrlResultClickId(String resultUrl) {
        try {
            Uri uri = Uri.parse(resultUrl);
            String clickId = uri.getQueryParameter("qz_gdt");
            return clickId;
        } catch (Throwable e) {

        }
        return null;
    }

//    public static String replaceClickTrackingInfo(String trackingUrl, AdClickRecord adClickRecord) {
//        if (adClickRecord == null) {
//            return trackingUrl;
//        }
//        String finishReplaceUrl = trackingUrl.replaceAll("\\{__REQ_WIDTH__\\}", adClickRecord.requestWidth == 0 ? "__REQ_WIDTH__" : adClickRecord.requestWidth + "")
//                .replaceAll("\\{__REQ_HEIGHT__\\}", adClickRecord.requestHeight == 0 ? "__REQ_HEIGHT__" : adClickRecord.requestHeight + "")
//                .replaceAll("\\{__WIDTH__\\}", adClickRecord.realWidth + "")
//                .replaceAll("\\{__HEIGHT__\\}", adClickRecord.realHeight + "")
//                .replaceAll("\\{__DOWN_X__\\}", adClickRecord.clickDownX + "")
//                .replaceAll("\\{__DOWN_Y__\\}", adClickRecord.clickDownY + "")
//                .replaceAll("\\{__UP_X__\\}", adClickRecord.clickUpX + "")
//                .replaceAll("\\{__UP_Y__\\}", adClickRecord.clickUpY + "");
//        return finishReplaceUrl;
//    }
}
