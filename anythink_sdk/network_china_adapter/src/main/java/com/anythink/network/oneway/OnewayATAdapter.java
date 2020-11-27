/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.oneway;

import android.content.Context;
import android.text.TextUtils;

import com.anythink.nativead.unitgroup.api.CustomNativeAd;
import com.anythink.nativead.unitgroup.api.CustomNativeAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mobi.oneway.export.Ad.OWFeedAd;
import mobi.oneway.export.AdListener.feed.OWFeedAdListener;
import mobi.oneway.export.enums.OnewaySdkError;
import mobi.oneway.export.feed.IFeedAd;

public class OnewayATAdapter extends CustomNativeAdapter {

    String mPublishId;
    String mSlotId;


    @Override
    public void loadCustomNetworkAd(Context context, Map<String, Object> serverExtra, Map<String, Object> localExtra) {

        String publishId = "";
        String slotId = "";

        if (serverExtra.containsKey("publisher_id")) {
            publishId = serverExtra.get("publisher_id").toString();
        }
        if (serverExtra.containsKey("slot_id")) {
            slotId = serverExtra.get("slot_id").toString();
        }

        if (TextUtils.isEmpty(publishId) || TextUtils.isEmpty(slotId)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", " publishId or slotId is empty.");
            }
            return;
        }

        mPublishId = publishId;
        mSlotId = slotId;
        OnewayATInitManager.getInstance().initSDK(context, serverExtra);

        OWFeedAd owFeedAd = new OWFeedAd(context, mSlotId);
        owFeedAd.load(new OWFeedAdListener() {
            @Override
            public void onError(OnewaySdkError onewaySdkError, String s) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError(onewaySdkError.name(), s);
                }
            }

            @Override
            public void onAdLoad(List<IFeedAd> list) {
                List<CustomNativeAd> resultList = new ArrayList<>();
                for (IFeedAd iFeedAd : list) {
                    OnewayATNativeAd atNativeAd = new OnewayATNativeAd(iFeedAd);
                    resultList.add(atNativeAd);
                }

                if (mLoadListener != null) {
                    CustomNativeAd[] customNativeAds = new CustomNativeAd[resultList.size()];
                    customNativeAds = resultList.toArray(customNativeAds);
                    mLoadListener.onAdCacheLoaded(customNativeAds);
                }
            }
        });

    }

    @Override
    public boolean initNetworkObjectByPlacementId(Context context, Map<String, Object> serverExtra, Map<String, Object> localExtra) {
        if (serverExtra.containsKey("publisher_id") && serverExtra.containsKey("slot_id")) {
            mPublishId = serverExtra.get("publisher_id").toString();
            mSlotId = serverExtra.get("slot_id").toString();
            return true;
        }
        return false;
    }

    @Override
    public void destory() {

    }

    @Override
    public String getNetworkPlacementId() {
        return mSlotId;
    }

    @Override
    public String getNetworkSDKVersion() {
        return OnewayATConst.getNetworkVersion();
    }

    @Override
    public String getNetworkName() {
        return OnewayATInitManager.getInstance().getNetworkName();
    }
}
