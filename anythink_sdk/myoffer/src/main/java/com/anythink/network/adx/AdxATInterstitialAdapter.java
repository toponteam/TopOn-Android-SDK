/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.adx;

import android.app.Activity;
import android.content.Context;

import com.anythink.basead.adx.AdxAdConfig;
import com.anythink.basead.adx.AdxInterstitialAd;
import com.anythink.basead.entity.OfferError;
import com.anythink.basead.listeners.VideoAdListener;
import com.anythink.basead.myoffer.MyOfferBaseAd;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.entity.AdxRequestInfo;
import com.anythink.core.common.utils.CommonDeviceUtil;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;

import java.util.HashMap;
import java.util.Map;

public class AdxATInterstitialAdapter extends CustomInterstitialAdapter {

    AdxRequestInfo mAdxRequestInfo;
    AdxInterstitialAd mAdxInterstitialAd;

    @Override
    public void show(Activity activity) {
        int orientation = CommonDeviceUtil.orientation(activity);
        Map<String, Object> extra = new HashMap<>(1);
        extra.put(MyOfferBaseAd.EXTRA_SCENARIO, mScenario);
        extra.put(MyOfferBaseAd.EXTRA_ORIENTATION, orientation);
        if (mAdxInterstitialAd != null) {
            mAdxInterstitialAd.show(extra);
        }
    }

    @Override
    public void loadCustomNetworkAd(Context context, final Map<String, Object> serverExtra, Map<String, Object> localExtra) {

        initInterstitialAdObject(context, serverExtra);

        mAdxInterstitialAd.load();
    }

    @Override
        public boolean initNetworkObjectByPlacementId(Context context, Map<String, Object> serverExtras, Map<String, Object> localExtra) {
            initInterstitialAdObject(context, serverExtras);
        return true;
    }

    private void initInterstitialAdObject(Context context, Map<String, Object> serverExtra) {
        int videoMute = 0;
        int showCloseButtonTime = -1;

        if (serverExtra.containsKey("v_m")) {
            Object v_m = serverExtra.get("v_m");
            if (v_m != null) {
                videoMute = Integer.parseInt(v_m.toString());
            }
        }

        if (serverExtra.containsKey("s_c_t")) {
            Object s_c_t = serverExtra.get("s_c_t");
            if (s_c_t != null) {
                showCloseButtonTime = Integer.parseInt(s_c_t.toString());
            }
        }

        mAdxRequestInfo = (AdxRequestInfo) serverExtra.get(Const.NETWORK_REQUEST_PARAMS_KEY.ADX_PARAMS_KEY);
        mAdxInterstitialAd = new AdxInterstitialAd(context, mAdxRequestInfo);
        mAdxInterstitialAd.setAdxAdConfig(new AdxAdConfig.Builder()
                .isMute(videoMute)
                .showCloseButtonTime(showCloseButtonTime)
                .build()
        );
        mAdxInterstitialAd.setListener(new VideoAdListener() {
            @Override
            public void onVideoAdPlayStart() {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdVideoStart();
                }
            }

            @Override
            public void onVideoAdPlayEnd() {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdVideoEnd();
                }
            }

            @Override
            public void onVideoShowFailed(OfferError error) {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdVideoError(error.getCode(), error.getDesc());
                }
            }

            @Override
            public void onRewarded() {
            }

            @Override
            public void onAdDataLoaded() {
                if (mLoadListener != null) {
                    mLoadListener.onAdDataLoaded();
                }
            }

            @Override
            public void onAdCacheLoaded() {
                if (mLoadListener != null) {
                    mLoadListener.onAdCacheLoaded();
                }
            }

            @Override
            public void onAdLoadFailed(OfferError error) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError(error.getCode(), error.getDesc());
                }
            }

            @Override
            public void onAdShow() {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdShow();
                }
            }

            @Override
            public void onAdClosed() {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdClose();
                }
            }

            @Override
            public void onAdClick() {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdClicked();
                }
            }
        });
    }

    @Override
    public void destory() {
        if (mAdxInterstitialAd != null) {
            mAdxInterstitialAd.destory();
            mAdxInterstitialAd = null;
        }
    }

    @Override
    public String getNetworkPlacementId() {
        return mAdxRequestInfo.placementId;
    }

    @Override
    public String getNetworkSDKVersion() {
        return "";
    }

    @Override
    public String getNetworkName() {
        return "Adx";
    }

    @Override
    public boolean isAdReady() {
        if (mAdxInterstitialAd != null) {
            return mAdxInterstitialAd.isReady();
        }
        return false;
    }
}
