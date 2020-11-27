/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.uniplay;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.anythink.banner.unitgroup.api.CustomBannerAdapter;
import com.uniplay.adsdk.AdBannerListener;
import com.uniplay.adsdk.AdSize;
import com.uniplay.adsdk.AdView;

import java.util.Map;

public class UniplayATBannerAdapter extends CustomBannerAdapter {

    private final String TAG = UniplayATBannerAdapter.class.getSimpleName();
    String mAppId;
    AdView mBannerView;
    int mRefreshTime;

    @Override
    public View getBannerView() {
        return mBannerView;
    }

    @Override
    public String getNetworkName() {
        return UniplayATInitManager.getInstance().getNetworkName();
    }

    @Override
    public void loadCustomNetworkAd(final Context context, final Map<String, Object> serverExtra, Map<String, Object> localExtra) {
        if (serverExtra.containsKey("app_id")) {
            mAppId = (String) serverExtra.get("app_id");

        } else {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "app_id is empty!");
            }
            return;
        }

        String size = "";
        if (serverExtra.containsKey("size")) {
            size = serverExtra.get("size").toString();
        }

        mRefreshTime = 0;
        try {
            if (serverExtra.containsKey("nw_rft")) {
                mRefreshTime = Integer.valueOf((String) serverExtra.get("nw_rft"));
                mRefreshTime /= 1000f;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        AdView adView = null;

        switch (size) {// unit: px
            case "320x50":
                adView = new AdView(context, AdSize.Size_320X50, mAppId);
                break;
            case "640x100":
                adView = new AdView(context, AdSize.Size_640X100, mAppId);
                break;
            case "960x150":
                adView = new AdView(context, AdSize.Size_960X150, mAppId);
                break;
            case "480x75":
                adView = new AdView(context, AdSize.Size_480X75, mAppId);
                break;
            case "728x90":
                adView = new AdView(context, AdSize.Size_728X90, mAppId);
                break;
            default:
                adView = new AdView(context, AdSize.Size_320X50, mAppId);
                break;
        }

        adView.setAdListener(new AdBannerListener() {
            @Override
            public void onAdShow(Object o) {
                if (mLoadListener != null) {
                    mLoadListener.onAdCacheLoaded();
                }
            }

            @Override
            public void onAdClick() {
                if (mImpressionEventListener != null) {
                    mImpressionEventListener.onBannerAdClicked();
                }
            }

            @Override
            public void onAdError(String s) {
                if (mATBannerView != null) {
                    mATBannerView.removeView(mBannerView);
                }

                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError("", s);
                }
            }
        });

        mBannerView = adView;

        if (mATBannerView != null) {
            mATBannerView.addView(mBannerView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }


        if (mRefreshTime > 0) {// setRefreshInterval must be called after addView, otherwise will not send ad request
            adView.setRefreshInterval(mRefreshTime);
        } else {
            adView.setRefreshInterval(0);
        }
    }

    @Override
    public void destory() {
        if (mBannerView != null) {
            mBannerView.setAdListener(null);
            mBannerView = null;
        }
    }

    @Override
    public String getNetworkPlacementId() {
        return mAppId;
    }

    @Override
    public String getNetworkSDKVersion() {
        return "";
    }

}
