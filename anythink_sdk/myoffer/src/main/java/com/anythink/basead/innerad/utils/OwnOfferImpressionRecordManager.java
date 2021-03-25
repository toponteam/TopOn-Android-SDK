/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.innerad.utils;

import android.content.Context;

import com.anythink.core.common.base.Const;
import com.anythink.core.common.entity.BaseAdContent;
import com.anythink.core.common.entity.BaseAdSetting;
import com.anythink.core.common.entity.OwnBaseAdSetting;
import com.anythink.core.common.utils.SPUtil;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Record the impression offer
 */
public class OwnOfferImpressionRecordManager {

    private static OwnOfferImpressionRecordManager sInstance;

    ConcurrentHashMap<String, ArrayList<String>> impressionRecordMap;

    private OwnOfferImpressionRecordManager() {
        impressionRecordMap = new ConcurrentHashMap<>();
    }

    public synchronized static OwnOfferImpressionRecordManager getInstance() {
        if (sInstance == null) {
            sInstance = new OwnOfferImpressionRecordManager();
        }
        return sInstance;
    }

    public synchronized void recordOfferImpression(Context context, String recordId, BaseAdContent baseAdContent, BaseAdSetting baseAdSetting) {
        /**Only for Online Api Offer**/
        if (baseAdContent.getOfferSourceType() == BaseAdContent.ONLINEAPI_TYPE && baseAdSetting instanceof OwnBaseAdSetting) {
            if (((OwnBaseAdSetting) baseAdSetting).getRecordImpressAdNum() <= 0) {
                return;
            }

            OwnBaseAdSetting ownBaseAdSetting = (OwnBaseAdSetting) baseAdSetting;
            ArrayList<String> recordList = impressionRecordMap.get(recordId);
            if (recordList == null) {
                recordList = new ArrayList<>();
                String recordArrayList = SPUtil.getString(context, Const.SPU_OWN_OFFER_IMPRESSION_RECORD_FILE_NAME, recordId, "");
                try {
                    JSONArray jsonArray = new JSONArray(recordArrayList);
                    if (jsonArray.length() > 0) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            recordList.add(jsonArray.optString(i));
                        }
                    }
                } catch (Exception e) {

                }
                impressionRecordMap.put(recordId, recordList);
            }

            if (recordList.size() >= ownBaseAdSetting.getRecordImpressAdNum()) {
                recordList.remove(recordList.size() - 1);
            }

            recordList.add(0, baseAdContent.getOfferId());
            JSONArray jsonArray = new JSONArray(recordList);

            SPUtil.putString(context, Const.SPU_OWN_OFFER_IMPRESSION_RECORD_FILE_NAME, recordId, jsonArray.toString());
        }

    }

    public String[] getOfferImpressionList(Context context, String recordId) {
        ArrayList<String> recordList = impressionRecordMap.get(recordId);
        if (recordList == null) {
            String recordArrayList = SPUtil.getString(context, Const.SPU_OWN_OFFER_IMPRESSION_RECORD_FILE_NAME, recordId, "");
            try {
                JSONArray jsonArray = new JSONArray(recordArrayList);
                if (jsonArray.length() > 0) {
                    recordList = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        recordList.add(jsonArray.optString(i));
                    }
                }
            } catch (Exception e) {

            }
        }

        if (recordList != null) {
            impressionRecordMap.put(recordId, recordList);
            String[] recordStringArray = new String[recordList.size()];
            recordList.toArray(recordStringArray);
            return recordStringArray;
        }

        return null;
    }

    public static String getRecordId(String placementId, String adSourceId) {
        return placementId + adSourceId;
    }


}
