package com.anythink.network.vungle;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.ATAdConst;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;
import com.vungle.warren.AdConfig;
import com.vungle.warren.LoadAdCallback;
import com.vungle.warren.PlayAdCallback;
import com.vungle.warren.Vungle;
import com.vungle.warren.error.VungleException;

import java.util.Map;

public class VungleATRewardedVideoAdapter extends CustomRewardVideoAdapter {

    private final String TAG = VungleATRewardedVideoAdapter.class.getSimpleName();
    String mPlacementId;
    AdConfig mAdConfig;

    private final LoadAdCallback loadAdCallback = new LoadAdCallback() {
        @Override
        public void onAdLoad(String s) {
            if (mLoadListener != null) {
                mLoadListener.onAdCacheLoaded();
            }
        }

        @Override
        public void onError(String s, VungleException throwable) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", throwable.toString());
            }
        }
    };

    private PlayAdCallback vungleDefaultListener = new PlayAdCallback() {

        @Override
        public void onAdEnd(String placementReferenceId, boolean wasSuccessFulView, boolean wasCallToActionClicked) {
            // Called when user exits the ad and control is returned to your application
            // if wasSuccessfulView is true, the user watched the ad and could be rewarded
            // if wasCallToActionClicked is true, the user clicked the call to action button in the ad.

//            if (mImpressionListener != null) {
//                if (wasCallToActionClicked) {
//                    mImpressionListener.onRewardedVideoAdPlayClicked();
//                }
//                mImpressionListener.onRewardedVideoAdPlayEnd();
//                if (wasSuccessFulView) {
//                    mImpressionListener.onReward();
//                }
//                mImpressionListener.onRewardedVideoAdClosed();
//
//            }

        }

        @Override
        public void onAdEnd(String s) {
            if (mImpressionListener != null) {
                mImpressionListener.onRewardedVideoAdPlayEnd();
                mImpressionListener.onRewardedVideoAdClosed();
            }
        }

        @Override
        public void onAdClick(String s) {
            if (mImpressionListener != null) {
                mImpressionListener.onRewardedVideoAdPlayClicked();
            }
        }

        @Override
        public void onAdRewarded(String s) {
            if (mImpressionListener != null) {
                mImpressionListener.onReward();

            }
        }

        @Override
        public void onAdLeftApplication(String s) {

        }

        @Override
        public void onAdStart(String placementReferenceId) {
            // Called before playing an ad
            if (mImpressionListener != null) {
                mImpressionListener.onRewardedVideoAdPlayStart();
            }
        }

        @Override
        public void onError(String placementReferenceId, VungleException reason) {
            // Called after playAd(placementId, adConfig) is unable to play the ad
            if (mImpressionListener != null) {
                mImpressionListener.onRewardedVideoAdPlayFailed("", reason.toString());
            }
        }
    };


    @Override
    public void loadCustomNetworkAd(Context context, Map<String, Object> serverExtras, Map<String, Object> localExtras) {
        String mAppId = (String) serverExtras.get("app_id");
        mPlacementId = (String) serverExtras.get("placement_id");


        if (TextUtils.isEmpty(mAppId) || TextUtils.isEmpty(mPlacementId)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", " appid & placementId is empty.");
            }
            return;
        }


        mAdConfig = new AdConfig();
        mAdConfig.setOrdinal(AdConfig.AUTO_ROTATE);

        try {
            if (localExtras.containsKey(ATAdConst.KEY.AD_ORIENTATION)) {
                int orientation = Integer.parseInt(localExtras.get(ATAdConst.KEY.AD_ORIENTATION).toString());
                switch (orientation) {
                    case ATAdConst.ORIENTATION_HORIZONTAL:
                        mAdConfig.setOrdinal(AdConfig.LANDSCAPE);
                        break;
                    case ATAdConst.ORIENTATION_VERTICAL:
                        mAdConfig.setOrdinal(AdConfig.PORTRAIT);
                        break;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            if (localExtras.containsKey(ATAdConst.KEY.AD_SOUND)) {
                boolean isSoundEnable = Boolean.parseBoolean(localExtras.get(ATAdConst.KEY.AD_SOUND).toString());
                if (isSoundEnable) {
                    mAdConfig.setMuted(false);
                } else {
                    mAdConfig.setMuted(true);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        VungleATInitManager.getInstance().initSDK(context.getApplicationContext(), serverExtras, new VungleATInitManager.InitListener() {
            @Override
            public void onSuccess() {
                try {
                    Vungle.loadAd(mPlacementId, loadAdCallback);
                } catch (Throwable e) {
                    if (mLoadListener != null) {
                        mLoadListener.onAdLoadError("", e.getMessage());
                    }
                }
            }

            @Override
            public void onError(Throwable throwable) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError("", throwable.toString());
                }
            }
        });
    }

    @Override
    public boolean isAdReady() {
        return Vungle.canPlayAd(mPlacementId);
    }

    @Override
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        return VungleATInitManager.getInstance().setUserDataConsent(context, isConsent, isEUTraffic);
    }

    @Override
    public void show(Activity activity) {
        // Play a Placement ad with Placement ID, you can pass AdConfig to customize your ad
        Vungle.setIncentivizedFields(mUserId, "", "", "", "");
        Vungle.playAd(mPlacementId, mAdConfig, vungleDefaultListener);
    }

    @Override
    public String getNetworkName() {
        return VungleATInitManager.getInstance().getNetworkName();
    }

    @Override
    public String getNetworkPlacementId() {
        return mPlacementId;
    }

    @Override
    public void destory() {
        mAdConfig = null;
        vungleDefaultListener = null;
    }


    @Override
    public String getNetworkSDKVersion() {
        return VungleATConst.getNetworkVersion();
    }

}
