/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.baidu;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.anythink.splashad.unitgroup.api.CustomSplashAdapter;
import com.baidu.mobads.SplashAd;
import com.baidu.mobads.SplashAdListener;

import java.util.Map;

public class BaiduATSplashAdapter extends CustomSplashAdapter {
    private final String TAG = BaiduATSplashAdapter.class.getSimpleName();
    String mAdPlaceId = "";
    SplashAd mSplashAd;

    FrameLayout mContainer;

    private void startLoadAd(final Context context, final FrameLayout constainer) {
        // the observer of AD
        SplashAdListener listener = new SplashAdListener() {
            @Override
            public void onAdDismissed() {
                if (mImpressionListener != null) {
                    mImpressionListener.onSplashAdDismiss();
                }
            }

            @Override
            public void onADLoaded() {
                mContainer = constainer;
                if (mLoadListener != null) {
                    mLoadListener.onAdCacheLoaded();
                }
            }

            @Override
            public void onAdFailed(String arg0) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError("", arg0);
                }
            }

            @Override
            public void onAdPresent() {
                if (mImpressionListener != null) {
                    mImpressionListener.onSplashAdShow();
                }

            }

            @Override
            public void onAdClick() {
                if (mImpressionListener != null) {
                    mImpressionListener.onSplashAdClicked();
                }
            }
        };

        mSplashAd = new SplashAd(context, constainer, listener, mAdPlaceId, true);
        mSplashAd.load();
    }

    @Override
    public String getNetworkName() {
        return BaiduATInitManager.getInstance().getNetworkName();
    }

    @Override
    public boolean isAdReady() {
        return mContainer != null;
    }

    @Override
    public void loadCustomNetworkAd(final Context context, Map<String, Object> serverExtra, Map<String, Object> localExtra) {

        if (!(context instanceof Activity)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "Baidu: context must be activity");
            }
            return;
        }

        String mAppId = (String) serverExtra.get("app_id");
        mAdPlaceId = (String) serverExtra.get("ad_place_id");
        if (TextUtils.isEmpty(mAppId) || TextUtils.isEmpty(mAdPlaceId)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", " app_id ,ad_place_id is empty.");
            }
            return;
        }

        final FrameLayout container = new FrameLayout(context);
        BaiduATInitManager.getInstance().initSDK(context, serverExtra, new BaiduATInitManager.InitCallback() {
            @Override
            public void onSuccess() {
                startLoadAd(context, container);
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
        if (mSplashAd != null) {
            mSplashAd.destroy();
            mSplashAd = null;
        }

        mContainer = null;
    }

    @Override
    public String getNetworkPlacementId() {
        return mAdPlaceId;
    }

    @Override
    public String getNetworkSDKVersion() {
        return BaiduATInitManager.getInstance().getNetworkVersion();
    }

    @Override
    public void show(Activity activity, ViewGroup container) {
        if (mContainer != null) {
            container.addView(mContainer, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            mSplashAd.show();
        }
    }
}
