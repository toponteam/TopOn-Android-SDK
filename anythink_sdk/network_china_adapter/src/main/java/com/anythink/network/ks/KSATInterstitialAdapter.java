/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.ks;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;
import com.kwad.sdk.api.KsAdSDK;
import com.kwad.sdk.api.KsFullScreenVideoAd;
import com.kwad.sdk.api.KsLoadManager;
import com.kwad.sdk.api.KsScene;
import com.kwad.sdk.api.KsVideoPlayConfig;

import java.util.List;
import java.util.Map;

public class KSATInterstitialAdapter extends CustomInterstitialAdapter {

    long posId;
    KsFullScreenVideoAd mKsFullScreenVideoAd;
    int orientation;
    boolean isVideoSoundEnable;

    @Override
    public void loadCustomNetworkAd(Context context, Map<String, Object> serverExtra, Map<String, Object> localExtra) {
        String appId = (String) serverExtra.get("app_id");
        String position_id = (String) serverExtra.get("position_id");

        if (TextUtils.isEmpty(appId) || TextUtils.isEmpty(position_id)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "kuaishou app_id or position_id is empty.");
            }
            return;
        }
        posId = Long.parseLong(position_id);

        isVideoSoundEnable = true;
        if (serverExtra.containsKey("video_muted")) {
            isVideoSoundEnable = TextUtils.equals("0", serverExtra.get("video_muted").toString());
        }

        if (serverExtra.containsKey("orientation")) {
            orientation = Integer.parseInt(serverExtra.get("orientation").toString());
        }

        KSATInitManager.getInstance().initSDK(context, serverExtra, new KSATInitManager.InitCallback() {
            @Override
            public void onFinish() {
                startLoadAd();
            }
        });
    }

    private void startLoadAd() {
        KsScene adScene = new KsScene.Builder(posId)
                .adNum(1)
                .build();
        KsAdSDK.getLoadManager().loadFullScreenVideoAd(adScene, new KsLoadManager.FullScreenVideoAdListener() {
            @Override
            public void onError(int code, String msg) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError(code + "", msg);
                }
            }

            @Override
            public void onRequestResult(int i) {
                if (mLoadListener != null) {
                    mLoadListener.onAdDataLoaded();
                }
            }

            @Override
            public void onFullScreenVideoAdLoad(@Nullable List<KsFullScreenVideoAd> list) {
                if (list != null && list.size() > 0) {
                    mKsFullScreenVideoAd = list.get(0);
                    if (mLoadListener != null) {
                        mLoadListener.onAdCacheLoaded();
                    }
                }
                try {
                    KSATInitManager.getInstance().put(getTrackingInfo().getmUnitGroupUnitId(), mKsFullScreenVideoAd);
                } catch (Exception e) {

                }
            }
        });
    }

    @Override
    public void show(Activity activity) {
        if (mKsFullScreenVideoAd != null && activity != null) {
            mKsFullScreenVideoAd.setFullScreenVideoAdInteractionListener(new KsFullScreenVideoAd.FullScreenVideoAdInteractionListener() {
                @Override
                public void onAdClicked() {
                    if (mImpressListener != null) {
                        mImpressListener.onInterstitialAdClicked();
                    }
                }

                @Override
                public void onPageDismiss() {
                    if (mImpressListener != null) {
                        mImpressListener.onInterstitialAdClose();
                    }
                    try {
                        KSATInitManager.getInstance().remove(getTrackingInfo().getmUnitGroupUnitId());
                    } catch (Exception e) {

                    }

                }

                @Override
                public void onVideoPlayError(int code, int extra) {
                    if (mImpressListener != null) {
                        mImpressListener.onInterstitialAdVideoError(code + "", "");
                    }
                }

                @Override
                public void onVideoPlayEnd() {
                    if (mImpressListener != null) {
                        mImpressListener.onInterstitialAdVideoEnd();
                    }
                }

                @Override
                public void onVideoPlayStart() {
                    if (mImpressListener != null) {
                        mImpressListener.onInterstitialAdShow();
                        mImpressListener.onInterstitialAdVideoStart();
                    }
                }

                @Override
                public void onSkippedVideo() {

                }
            });

            KsVideoPlayConfig videoPlayConfig = new KsVideoPlayConfig.Builder()
                    .showLandscape(orientation == 2)//1:Portrait screen，2:Landscape, default portrait
                    .skipThirtySecond(false)//Optional. After 30s, it can be turned off (interstitial video is effective) After playing for 30s, the close button is displayed
                    .videoSoundEnable(isVideoSoundEnable)
                    .build();

            mKsFullScreenVideoAd.showFullScreenVideoAd(activity, videoPlayConfig);
        }
    }


    @Override
    public boolean isAdReady() {
        return mKsFullScreenVideoAd != null && mKsFullScreenVideoAd.isAdEnable();
    }

    @Override
    public String getNetworkName() {
        return KSATInitManager.getInstance().getNetworkName();
    }

    @Override
    public void destory() {
        if (mKsFullScreenVideoAd != null) {
            mKsFullScreenVideoAd.setFullScreenVideoAdInteractionListener(null);
            mKsFullScreenVideoAd = null;
        }
    }

    @Override
    public String getNetworkPlacementId() {
        try {
            return String.valueOf(posId);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    @Override
    public String getNetworkSDKVersion() {
        return KSATInitManager.getInstance().getNetworkVersion();
    }
}
