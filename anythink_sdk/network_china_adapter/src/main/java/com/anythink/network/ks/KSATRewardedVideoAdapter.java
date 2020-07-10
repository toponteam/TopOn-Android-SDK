package com.anythink.network.ks;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.ErrorCode;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoListener;
import com.kwad.sdk.KsAdSDK;
import com.kwad.sdk.export.i.IAdRequestManager;
import com.kwad.sdk.export.i.KsRewardVideoAd;
import com.kwad.sdk.protocol.model.AdScene;
import com.kwad.sdk.video.VideoPlayConfig;

import java.util.List;
import java.util.Map;

public class KSATRewardedVideoAdapter extends CustomRewardVideoAdapter {

    long posId;
    KsRewardVideoAd mKsRewardVideoAd;
    int orientation;

    @Override
    public void loadRewardVideoAd(Activity activity, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, CustomRewardVideoListener customRewardVideoListener) {
        mLoadResultListener = customRewardVideoListener;
        if (activity == null) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onRewardedVideoAdFailed(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "activity is null."));
            }
            return;
        }

        if (serverExtras == null) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onRewardedVideoAdFailed(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "service params is empty."));
            }
            return;
        } else {

            String appId = (String) serverExtras.get("app_id");
            String appName = (String) serverExtras.get("app_name");
            String position_id = (String) serverExtras.get("position_id");

            if (TextUtils.isEmpty(appId) || TextUtils.isEmpty(appName) || TextUtils.isEmpty(position_id)) {
                if (mLoadResultListener != null) {
                    mLoadResultListener.onRewardedVideoAdFailed(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "kuaishou app_id、 app_name or position_id is empty."));
                }
                return;
            }
            posId = Long.parseLong(position_id);
        }

        if (serverExtras != null && serverExtras.containsKey("orientation")) {
            orientation = Integer.parseInt(serverExtras.get("orientation").toString());
        }

        KSATInitManager.getInstance().initSDK(activity, serverExtras);

        AdScene adScene = new AdScene(posId);
        adScene.adNum = 1;
        KsAdSDK.getAdManager().loadRewardVideoAd(adScene, new IAdRequestManager.RewardVideoAdListener() {
            @Override
            public void onError(int code, String msg) {
                mLoadResultListener.onRewardedVideoAdFailed(KSATRewardedVideoAdapter.this,
                        ErrorCode.getErrorCode(ErrorCode.noADError, code + "", msg));
            }


            @Override
            public void onRewardVideoAdLoad(@Nullable List<KsRewardVideoAd> list) {
                if (list != null && list.size() > 0) {
                    mKsRewardVideoAd = list.get(0);
                    mLoadResultListener.onRewardedVideoAdLoaded(KSATRewardedVideoAdapter.this);
                }
                try {
                    KSATInitManager.getInstance().put(getTrackingInfo().getmUnitGroupUnitId(), mKsRewardVideoAd);
                } catch (Exception e) {

                }

            }
        });
    }

    @Override
    public void show(Activity activity) {
        if (mKsRewardVideoAd != null) {
            mKsRewardVideoAd.setRewardAdInteractionListener(new KsRewardVideoAd.RewardAdInteractionListener() {
                @Override
                public void onAdClicked() {
                    if (mImpressionListener != null) {
                        mImpressionListener.onRewardedVideoAdPlayClicked(KSATRewardedVideoAdapter.this);
                    }
                }

                @Override
                public void onPageDismiss() {
                    if (mImpressionListener != null) {
                        mImpressionListener.onRewardedVideoAdClosed(KSATRewardedVideoAdapter.this);
                    }

                    try {
                        KSATInitManager.getInstance().remove(getTrackingInfo().getmUnitGroupUnitId());
                    } catch (Exception e) {

                    }

                }

                @Override
                public void onVideoPlayError(int code, int extra) {
                    if (mImpressionListener != null) {
                        mImpressionListener.onRewardedVideoAdPlayFailed(KSATRewardedVideoAdapter.this,
                                ErrorCode.getErrorCode(ErrorCode.noADError, code + "", ""));
                    }
                }

                @Override
                public void onVideoPlayEnd() {
                    if (mImpressionListener != null) {
                        mImpressionListener.onRewardedVideoAdPlayEnd(KSATRewardedVideoAdapter.this);
                    }
                }

                @Override
                public void onVideoPlayStart() {
                    if (mImpressionListener != null) {
                        mImpressionListener.onRewardedVideoAdPlayStart(KSATRewardedVideoAdapter.this);
                    }
                }

                @Override
                public void onRewardVerify() {
                    if (mImpressionListener != null) {
                        mImpressionListener.onReward(KSATRewardedVideoAdapter.this);
                    }
                }
            });

            VideoPlayConfig videoPlayConfig = new VideoPlayConfig.Builder()
                    .showLandscape(orientation == 2)//1:Portrait screen，2:Landscape, default portrait
                    .skipThirtySecond(false)//Optional. After 30s, it can be turned off (interstitial video is effective) After playing for 30s, the close button is displayed
                    .build();

            mKsRewardVideoAd.showRewardVideoAd(activity, videoPlayConfig);
        }
    }

    @Override
    public void onResume(Activity activity) {

    }

    @Override
    public void onPause(Activity activity) {

    }

    @Override
    public boolean isAdReady() {
        return mKsRewardVideoAd != null && mKsRewardVideoAd.isAdEnable();
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
