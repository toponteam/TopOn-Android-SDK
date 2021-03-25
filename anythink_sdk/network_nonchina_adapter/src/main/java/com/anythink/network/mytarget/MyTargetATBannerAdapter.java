/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 *
 */

package com.anythink.network.mytarget;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;

import com.anythink.banner.unitgroup.api.CustomBannerAdapter;
import com.my.target.ads.MyTargetView;
import com.my.target.common.MyTargetManager;

import java.util.Map;

public class MyTargetATBannerAdapter extends CustomBannerAdapter {

    private static final String TAG = MyTargetATBannerAdapter.class.getSimpleName();

    private int mSlotId = -1;
    private String mSize;
    private MyTargetView mMyTargetView;
    private String mPayload;

    @Override
    public View getBannerView() {
        return mMyTargetView;
    }

    @Override
    public void loadCustomNetworkAd(final Context context, Map<String, Object> serverExtra, Map<String, Object> localExtra) {

        String slotId = (String) serverExtra.get("slot_id");

        if (TextUtils.isEmpty(slotId)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "MyTarget slot_id = null");
            }
            return;
        }

        mSlotId = Integer.parseInt(slotId);
        mSize = (String) serverExtra.get("size");

        mPayload = (String) serverExtra.get("payload");

        startLoadAd(context);
    }

    private void startLoadAd(Context context) {

        MyTargetView myTargetView = new MyTargetView(context);

        MyTargetView.AdSize bannerSize;
        switch (mSize) {
            case "300x250":
                bannerSize = MyTargetView.AdSize.ADSIZE_300x250;
                break;
            case "728x90":
                bannerSize = MyTargetView.AdSize.ADSIZE_728x90;
                break;
            case "320x50":
            default:
                bannerSize = MyTargetView.AdSize.ADSIZE_320x50;
                break;
        }

        myTargetView.setSlotId(mSlotId);
        myTargetView.setAdSize(bannerSize);
        myTargetView.setRefreshAd(false);

        myTargetView.setListener(new MyTargetView.MyTargetViewListener() {
            @Override
            public void onLoad(@NonNull MyTargetView myTargetView) {
                mMyTargetView = myTargetView;

                if (mLoadListener != null) {
                    mLoadListener.onAdCacheLoaded();
                }
            }

            @Override
            public void onNoAd(@NonNull String reason, @NonNull MyTargetView myTargetView) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError("", "MyTarget " + reason);
                }
            }

            @Override
            public void onShow(@NonNull MyTargetView myTargetView) {
                if (mImpressionEventListener != null) {
                    mImpressionEventListener.onBannerAdShow();
                }
            }

            @Override
            public void onClick(@NonNull MyTargetView myTargetView) {
                if (mImpressionEventListener != null) {
                    mImpressionEventListener.onBannerAdClicked();
                }
            }
        });

        if (!TextUtils.isEmpty(mPayload)) {
            myTargetView.loadFromBid(mPayload);
        } else {
            myTargetView.load();
        }

    }


    @Override
    public void destory() {
        if (mMyTargetView != null) {
            mMyTargetView.setListener(null);
            mMyTargetView.destroy();
            mMyTargetView = null;
        }
    }

    @Override
    public String getNetworkPlacementId() {
        return String.valueOf(mSlotId);
    }

    @Override
    public String getNetworkSDKVersion() {
        return MyTargetATInitManager.getInstance().getNetworkVersion();
    }

    @Override
    public String getNetworkName() {
        return MyTargetATInitManager.getInstance().getNetworkName();
    }

    @Override
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        return MyTargetATInitManager.getInstance().setUserDataConsent(context, isConsent, isEUTraffic);
    }

    @Override
    public String getBiddingToken(Context context) {
        return MyTargetManager.getBidderToken(context);
    }

}
