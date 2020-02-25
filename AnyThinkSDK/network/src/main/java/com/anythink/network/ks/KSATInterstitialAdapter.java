package com.anythink.network.ks;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.ErrorCode;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialListener;
import com.kwad.sdk.KsAdSDK;
import com.kwad.sdk.export.i.IAdRequestManager;
import com.kwad.sdk.export.i.KsFullScreenVideoAd;
import com.kwad.sdk.protocol.model.AdScene;
import com.kwad.sdk.video.VideoPlayConfig;

import java.util.List;
import java.util.Map;

public class KSATInterstitialAdapter extends CustomInterstitialAdapter {

    long posId;
    KsFullScreenVideoAd mKsFullScreenVideoAd;
    int orientation;

    @Override
    public void loadInterstitialAd(Context context, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, CustomInterstitialListener customInterstitialListener) {
        mLoadResultListener = customInterstitialListener;

        if (serverExtras == null) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onInterstitialAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "service params is empty."));
            }
            return;
        } else {

            String appId = (String) serverExtras.get("app_id");
            String appName = (String) serverExtras.get("app_name");
            String position_id = (String) serverExtras.get("position_id");

            if (TextUtils.isEmpty(appId) || TextUtils.isEmpty(appName) || TextUtils.isEmpty(position_id)) {
                if (mLoadResultListener != null) {
                    mLoadResultListener.onInterstitialAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "kuaishou app_id、 app_name or position_id is empty."));
                }
                return;
            }
            posId = Long.parseLong(position_id);
        }

        if (serverExtras != null && serverExtras.containsKey("orientation")) {
            orientation = Integer.parseInt(serverExtras.get("orientation").toString());
        }

        KSATInitManager.getInstance().initSDK(context, serverExtras);

        AdScene adScene = new AdScene(posId);
        adScene.adNum = 1;

        KsAdSDK.getAdManager().loadFullScreenVideoAd(adScene, new IAdRequestManager.FullScreenVideoAdListener() {
            @Override
            public void onError(int code, String msg) {
                if (mLoadResultListener != null) {
                    mLoadResultListener.onInterstitialAdLoadFail(KSATInterstitialAdapter.this,
                            ErrorCode.getErrorCode(ErrorCode.noADError, code + "", msg));
                }

            }

            @Override
            public void onFullScreenVideoAdLoad(@Nullable List<KsFullScreenVideoAd> list) {
                if (list != null && list.size() > 0) {
                    mKsFullScreenVideoAd = list.get(0);
                    if (mLoadResultListener != null) {
                        mLoadResultListener.onInterstitialAdLoaded(KSATInterstitialAdapter.this);
                    }
                }
            }
        });
    }

    @Override
    public void show(Context context) {
        if (mKsFullScreenVideoAd != null && context instanceof Activity) {
            mKsFullScreenVideoAd.setFullScreenVideoAdInteractionListener(new KsFullScreenVideoAd.FullScreenVideoAdInteractionListener() {
                @Override
                public void onAdClicked() {
                    if (mImpressListener != null) {
                        mImpressListener.onInterstitialAdClicked(KSATInterstitialAdapter.this);
                    }
                }

                @Override
                public void onPageDismiss() {
                    if (mImpressListener != null) {
                        mImpressListener.onInterstitialAdClose(KSATInterstitialAdapter.this);
                    }
                }

                @Override
                public void onVideoPlayError(int code, int extra) {
                    if (mImpressListener != null) {
                        mImpressListener.onInterstitialAdVideoError(KSATInterstitialAdapter.this,
                                ErrorCode.getErrorCode(ErrorCode.noADError, code + "", ""));
                    }
                }

                @Override
                public void onVideoPlayEnd() {
                    if (mImpressListener != null) {
                        mImpressListener.onInterstitialAdVideoEnd(KSATInterstitialAdapter.this);
                    }
                }

                @Override
                public void onVideoPlayStart() {
                    if (mImpressListener != null) {
                        mImpressListener.onInterstitialAdShow(KSATInterstitialAdapter.this);
                        mImpressListener.onInterstitialAdVideoStart(KSATInterstitialAdapter.this);
                    }
                }

                @Override
                public void onSkippedVideo() {

                }
            });

            VideoPlayConfig videoPlayConfig = new VideoPlayConfig.Builder()
                    .showLandscape(orientation == 2)//1:Portrait screen，2:Landscape, default portrait
                    .skipThirtySecond(false)//Optional. After 30s, it can be turned off (interstitial video is effective) After playing for 30s, the close button is displayed
                    .build();

            mKsFullScreenVideoAd.showFullScreenVideoAd((Activity) context, videoPlayConfig);
        }
    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public boolean isAdReady() {
        return mKsFullScreenVideoAd != null && mKsFullScreenVideoAd.isAdEnable();
    }

    @Override
    public String getSDKVersion() {
        return KSATConst.getSDKVersion();
    }

    @Override
    public void clean() {

    }

    @Override
    public String getNetworkName() {
        return KSATInitManager.getInstance().getNetworkName();
    }
}
