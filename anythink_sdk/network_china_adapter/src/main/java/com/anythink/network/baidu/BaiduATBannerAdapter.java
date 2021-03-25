/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.baidu;

import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.anythink.banner.unitgroup.api.CustomBannerAdapter;
import com.baidu.mobads.AdView;
import com.baidu.mobads.AdViewListener;

import org.json.JSONObject;

import java.util.Map;

public class BaiduATBannerAdapter extends CustomBannerAdapter {

    String mAdPlaceId;
    AdView mBannerView;

    private void startLoadAd(Context context) {
        mBannerView = new AdView(context, mAdPlaceId);
        mBannerView.setListener(new AdViewListener() {
            @Override
            public void onAdReady(AdView adView) {

            }

            @Override
            public void onAdShow(JSONObject jsonObject) {
                if (mLoadListener != null) {
                    mLoadListener.onAdCacheLoaded();
                }
                if (mImpressionEventListener != null) {
                    mImpressionEventListener.onBannerAdShow();
                }
            }

            @Override
            public void onAdClick(JSONObject jsonObject) {
                if (mImpressionEventListener != null) {
                    mImpressionEventListener.onBannerAdClicked();
                }
            }

            @Override
            public void onAdFailed(String s) {
                if (mATBannerView != null) {
                    mATBannerView.removeView(mBannerView);
                }

                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError("", s);
                }

            }

            @Override
            public void onAdSwitch() {

            }

            @Override
            public void onAdClose(JSONObject jsonObject) {
                if (mImpressionEventListener != null) {
                    mImpressionEventListener.onBannerAdClose();
                }
            }
        });

        postOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (mATBannerView != null) {
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.gravity = Gravity.CENTER;
                    mATBannerView.addView(mBannerView, params);
                }
            }
        });

    }

    @Override
    public View getBannerView() {
        return mBannerView;
    }

    @Override
    public String getNetworkName() {
        return BaiduATInitManager.getInstance().getNetworkName();
    }

    @Override
    public void loadCustomNetworkAd(final Context context, Map<String, Object> serverExtra, Map<String, Object> localExtra) {
        String mAppId = "";
        if (serverExtra.containsKey("app_id")) {
            mAppId = serverExtra.get("app_id").toString();
        }
        if (serverExtra.containsKey("ad_place_id")) {
            mAdPlaceId = serverExtra.get("ad_place_id").toString();
        }

        if (TextUtils.isEmpty(mAppId) || TextUtils.isEmpty(mAdPlaceId)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "app_id or ad_place_id is empty.");
            }
            return;
        }
        BaiduATInitManager.getInstance().initSDK(context, serverExtra, new BaiduATInitManager.InitCallback() {
            @Override
            public void onSuccess() {
                startLoadAd(context);
            }

            @Override
            public void onError(Throwable e) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError("", e.getMessage());
                }
            }
        });
    }

    @Override
    public void destory() {
        if (mBannerView != null) {
            mBannerView.setListener(null);
            mBannerView.destroy();
            mBannerView = null;
        }
    }

    @Override
    public String getNetworkPlacementId() {
        return mAdPlaceId;
    }

    @Override
    public String getNetworkSDKVersion() {
        return BaiduATInitManager.getInstance().getNetworkVersion();
    }
}
