package com.anythink.network.toutiao;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.ErrorCode;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;
import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTFullScreenVideoAd;
import com.bytedance.sdk.openadsdk.TTInteractionAd;

import java.util.Map;

public class TTATInterstitialAdapter extends CustomInterstitialAdapter {
    private final String TAG = getClass().getSimpleName();

    String slotId = "";
    boolean isVideo = false;

    private TTFullScreenVideoAd mTTFullScreenVideoAd;


    //TT Advertising event listener
//    TTInteractionAd.AdInteractionListener interactionListener = new TTInteractionAd.AdInteractionListener() {
//
//        @Override
//        public void onAdClicked() {
//            if (mImpressListener != null) {
//                mImpressListener.onInterstitialAdClicked();
//            }
//        }
//
//        @Override
//        public void onAdShow() {
//            if (mImpressListener != null) {
//                mImpressListener.onInterstitialAdShow();
//            }
//        }
//
//        @Override
//        public void onAdDismiss() {
//            if (mImpressListener != null) {
//                mImpressListener.onInterstitialAdClose();
//            }
//        }
//
//    };


    TTAdNative.FullScreenVideoAdListener ttFullScrenAdListener = new TTAdNative.FullScreenVideoAdListener() {
        @Override
        public void onError(int code, String message) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError(String.valueOf(code), message);
            }
        }

        @Override
        public void onFullScreenVideoAdLoad(TTFullScreenVideoAd ad) {
            mTTFullScreenVideoAd = ad;

            if (mLoadListener != null) {
                mLoadListener.onAdDataLoaded();
            }
        }

        @Override
        public void onFullScreenVideoCached() {
            if (mLoadListener != null) {
                mLoadListener.onAdCacheLoaded();
            }
        }

    };

    TTFullScreenVideoAd.FullScreenVideoAdInteractionListener ttFullScreenEventListener = new TTFullScreenVideoAd.FullScreenVideoAdInteractionListener() {

        @Override
        public void onAdShow() {
            if (mImpressListener != null) {
                mImpressListener.onInterstitialAdShow();
                mImpressListener.onInterstitialAdVideoStart();
            }
        }

        @Override
        public void onAdVideoBarClick() {
            if (mImpressListener != null) {
                mImpressListener.onInterstitialAdClicked();
            }
        }

        @Override
        public void onAdClose() {
            if (mImpressListener != null) {
                mImpressListener.onInterstitialAdClose();
            }
        }

        @Override
        public void onVideoComplete() {
            if (mImpressListener != null) {
                mImpressListener.onInterstitialAdVideoEnd();
            }
        }

        @Override
        public void onSkippedVideo() {
        }

    };


    @Override
    public void loadCustomNetworkAd(final Context context, Map<String, Object> serverExtras, Map<String, Object> localExtras) {


        String appId = (String) serverExtras.get("app_id");
        slotId = (String) serverExtras.get("slot_id");

        if (TextUtils.isEmpty(appId) || TextUtils.isEmpty(slotId)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "app_id or slot_id is empty!");
            }
            return;
        }

        if (serverExtras.containsKey("is_video")) {
            if (serverExtras.get("is_video").toString().equals("1")) {
                isVideo = true;
            }
        }

        int layoutType = 0;
        if (serverExtras.containsKey("layout_type")) {
            layoutType = Integer.parseInt(serverExtras.get("layout_type").toString());
        }


        final int finalLayoutType = layoutType;
        TTATInitManager.getInstance().initSDK(context, serverExtras, new TTATInitManager.InitCallback() {
            @Override
            public void onFinish() {
                try {
                    startLoad(context, finalLayoutType);
                } catch (Throwable e) {
                    if (mLoadListener != null) {
                        mLoadListener.onAdLoadError("", e.getMessage());
                    }
                }
            }
        });
    }

    private void startLoad(Context context, int layoutType) {
        TTAdManager ttAdManager = TTAdSdk.getAdManager();

        TTAdNative mTTAdNative = ttAdManager.createAdNative(context);//baseContext is recommended for Activity
        AdSlot.Builder adSlotBuilder = new AdSlot.Builder().setCodeId(slotId);
        int width = context.getResources().getDisplayMetrics().widthPixels;
        int height = context.getResources().getDisplayMetrics().heightPixels;
        adSlotBuilder.setImageAcceptedSize(width, height); //must be set
        adSlotBuilder.setAdCount(1);

        //Only support fullscreen video
        AdSlot adSlot = adSlotBuilder.build();
        mTTAdNative.loadFullScreenVideoAd(adSlot, ttFullScrenAdListener);
    }

    @Override
    public boolean isAdReady() {
        return mTTFullScreenVideoAd != null;
    }

    @Override
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        return TTATInitManager.getInstance().setUserDataConsent(context, isConsent, isEUTraffic);
    }

    @Override
    public void show(Activity activity) {
        try {
            if (mTTFullScreenVideoAd != null && activity != null) {
                mTTFullScreenVideoAd.setFullScreenVideoAdInteractionListener(ttFullScreenEventListener);
                mTTFullScreenVideoAd.showFullScreenVideoAd(activity);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void destory() {
        if (mTTFullScreenVideoAd != null) {
            mTTFullScreenVideoAd.setFullScreenVideoAdInteractionListener(null);
            mTTFullScreenVideoAd = null;
        }

        ttFullScreenEventListener = null;
        ttFullScrenAdListener = null;
    }


    @Override
    public String getNetworkSDKVersion() {
        return TTATConst.getNetworkVersion();
    }

    @Override
    public String getNetworkName() {
        return TTATInitManager.getInstance().getNetworkName();
    }

    @Override
    public String getNetworkPlacementId() {
        return slotId;
    }
}
