/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.appnext;

import android.content.Context;
import android.view.View;

import com.anythink.banner.unitgroup.api.CustomBannerAdapter;
import com.appnext.banners.BannerAdRequest;
import com.appnext.banners.BannerListener;
import com.appnext.banners.BannerSize;
import com.appnext.banners.BannerView;
import com.appnext.core.AppnextAdCreativeType;
import com.appnext.core.AppnextError;

import java.util.Map;

public class AppnextATBannerAdapter extends CustomBannerAdapter {
    private final String TAG = AppnextATBannerAdapter.class.getSimpleName();

    String mPlacementId;

    BannerView mBannerView;

    @Override
    public void loadCustomNetworkAd(Context activity, Map<String, Object> serverExtras, Map<String, Object> localExtras) {

        if (serverExtras.containsKey("placement_id")) {
            mPlacementId = (String) serverExtras.get("placement_id");

        } else {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "placement_id is empty!");
            }
            return;
        }

        String size = "";
        if (serverExtras.containsKey("size")) {
            size = serverExtras.get("size").toString();
        }

        AppnextATInitManager.getInstance().initSDK(activity, serverExtras);

        final BannerView banner = new BannerView(activity);
        banner.setPlacementId(mPlacementId);

        switch (size) {
            case "320x50":
                banner.setBannerSize(BannerSize.BANNER);
                break;
            case "320x100":
                banner.setBannerSize(BannerSize.LARGE_BANNER);
                break;
            case "300x250":
                banner.setBannerSize(BannerSize.MEDIUM_RECTANGLE);
                break;
            default:
                banner.setBannerSize(BannerSize.BANNER);
                break;
        }


        banner.setBannerListener(new BannerListener() {

            @Override
            public void onAdLoaded(String s, AppnextAdCreativeType appnextAdCreativeType) {
                mBannerView = banner;
                if (mLoadListener != null) {
                    mLoadListener.onAdCacheLoaded();
                }
            }

            @Override
            public void onError(AppnextError appnextError) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError("", appnextError.getErrorMessage());
                }
            }

            @Override
            public void adImpression() {
                if (mImpressionEventListener != null) {
                    mImpressionEventListener.onBannerAdShow();
                }
            }

            @Override
            public void onAdClicked() {
                if (mImpressionEventListener != null) {
                    mImpressionEventListener.onBannerAdClicked();
                }
            }
        });
        banner.loadAd(new BannerAdRequest());

    }

    @Override
    public View getBannerView() {
        return mBannerView;
    }

    @Override
    public void destory() {
        if (mBannerView != null) {
            mBannerView.setBannerListener(null);
            mBannerView.destroy();
            mBannerView = null;
        }
    }

    @Override
    public String getNetworkSDKVersion() {
        return "";
    }

    @Override
    public String getNetworkName() {
        return AppnextATInitManager.getInstance().getNetworkName();
    }

    @Override
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        return AppnextATInitManager.getInstance().setUserDataConsent(context, isConsent, isEUTraffic);
    }

    @Override
    public String getNetworkPlacementId() {
        return mPlacementId;
    }

}
