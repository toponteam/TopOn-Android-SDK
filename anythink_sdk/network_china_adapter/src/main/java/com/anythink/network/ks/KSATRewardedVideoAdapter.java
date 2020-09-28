package com.anythink.network.ks;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;
import com.kwad.sdk.api.KsAdSDK;
import com.kwad.sdk.api.KsLoadManager;
import com.kwad.sdk.api.KsRewardVideoAd;
import com.kwad.sdk.api.KsScene;
import com.kwad.sdk.api.KsVideoPlayConfig;

import java.util.List;
import java.util.Map;

public class KSATRewardedVideoAdapter extends CustomRewardVideoAdapter {

    long posId;
    KsRewardVideoAd mKsRewardVideoAd;
    int orientation;

    boolean isSkipAfterThirtySecond = false;
    @Override
    public void show(Activity activity) {
        if (mKsRewardVideoAd != null) {

            try {
                if (activity != null) {
                    mKsRewardVideoAd.setRewardAdInteractionListener(new KsRewardVideoAd.RewardAdInteractionListener() {
                        @Override
                        public void onAdClicked() {
                            if (mImpressionListener != null) {
                                mImpressionListener.onRewardedVideoAdPlayClicked();
                            }
                        }

                        @Override
                        public void onPageDismiss() {
                            if (mImpressionListener != null) {
                                mImpressionListener.onRewardedVideoAdClosed();
                            }

                            try {
                                KSATInitManager.getInstance().remove(getTrackingInfo().getmUnitGroupUnitId());
                            } catch (Exception e) {

                            }

                        }

                        @Override
                        public void onVideoPlayError(int code, int extra) {
                            if (mImpressionListener != null) {
                                mImpressionListener.onRewardedVideoAdPlayFailed(code + "", "");
                            }
                        }

                        @Override
                        public void onVideoPlayEnd() {
                            if (mImpressionListener != null) {
                                mImpressionListener.onRewardedVideoAdPlayEnd();
                            }
                        }

                        @Override
                        public void onVideoPlayStart() {
                            if (mImpressionListener != null) {
                                mImpressionListener.onRewardedVideoAdPlayStart();
                            }
                        }

                        @Override
                        public void onRewardVerify() {
                            if (mImpressionListener != null) {
                                mImpressionListener.onReward();
                            }
                        }
                    });

                    KsVideoPlayConfig videoPlayConfig = new KsVideoPlayConfig.Builder()
                            .showLandscape(orientation == 2)//1:Portrait screen，2:Landscape, default portrait
                            .skipThirtySecond(isSkipAfterThirtySecond)//Optional. After 30s, it can be turned off (interstitial video is effective) After playing for 30s, the close button is displayed
                            .build();

                    mKsRewardVideoAd.showRewardVideoAd(activity, videoPlayConfig);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean isAdReady() {
        return mKsRewardVideoAd != null && mKsRewardVideoAd.isAdEnable();
    }

    @Override
    public String getNetworkName() {
        return KSATInitManager.getInstance().getNetworkName();
    }

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

        if (serverExtra.containsKey("orientation")) {
            orientation = Integer.parseInt(serverExtra.get("orientation").toString());
        }

        if (localExtra.containsKey(KSATConst.REWARDEDVIDEO_SKIP_AFTER_THIRTY_SECOND)) {
            Object isAfterThirtySecond = localExtra.get(KSATConst.REWARDEDVIDEO_SKIP_AFTER_THIRTY_SECOND);
            isSkipAfterThirtySecond = (isAfterThirtySecond instanceof Boolean) ? Boolean.parseBoolean(isAfterThirtySecond.toString()) : false;
        }

        KSATInitManager.getInstance().initSDK(context.getApplicationContext(), serverExtra);

        KsScene adScene = new KsScene.Builder(posId)
                .adNum(1)
                .build();
        KsAdSDK.getLoadManager().loadRewardVideoAd(adScene, new KsLoadManager.RewardVideoAdListener() {
            @Override
            public void onError(int code, String msg) {
                mLoadListener.onAdLoadError(code + "", msg);
            }

            @Override
            public void onRewardVideoAdLoad(@Nullable List<KsRewardVideoAd> list) {
                if (list != null && list.size() > 0) {
                    mKsRewardVideoAd = list.get(0);
                    mLoadListener.onAdCacheLoaded();
                }
                try {
                    KSATInitManager.getInstance().put(getTrackingInfo().getmUnitGroupUnitId(), mKsRewardVideoAd);
                } catch (Exception e) {

                }
            }
        });
    }

    @Override
    public void destory() {
        if (mKsRewardVideoAd != null) {
            mKsRewardVideoAd.setRewardAdInteractionListener(null);
            mKsRewardVideoAd = null;
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
        return KSATConst.getSDKVersion();
    }
}
