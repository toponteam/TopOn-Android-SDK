package com.anythink.network.toutiao;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.ErrorCode;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialListener;
import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTFullScreenVideoAd;
import com.bytedance.sdk.openadsdk.TTInteractionAd;
import com.bytedance.sdk.openadsdk.TTNativeExpressAd;

import java.util.List;
import java.util.Map;

public class TTATInterstitialAdapter extends CustomInterstitialAdapter {
    private final String TAG = getClass().getSimpleName();

    String slotId = "";
    boolean isVideo = false;

    private TTInteractionAd mttInterstitialAd;
    private TTFullScreenVideoAd mTTFullScreenVideoAd;
    private TTNativeExpressAd mTTNativeExpressAd;


    //TT Ad load listener
    TTAdNative.InteractionAdListener ttInterstitialAdListener = new TTAdNative.InteractionAdListener() {
        @Override
        public void onError(int code, String message) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onInterstitialAdLoadFail(TTATInterstitialAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, String.valueOf(code), message));
            }
        }

        @Override
        public void onInteractionAdLoad(TTInteractionAd ttInteractionAd) {
            mttInterstitialAd = ttInteractionAd;
            if (mLoadResultListener != null) {
                mLoadResultListener.onInterstitialAdLoaded(TTATInterstitialAdapter.this);
            }
        }

    };

    //TT Advertising event listener
    TTInteractionAd.AdInteractionListener interactionListener = new TTInteractionAd.AdInteractionListener() {

        @Override
        public void onAdClicked() {
            if (mImpressListener != null) {
                mImpressListener.onInterstitialAdClicked(TTATInterstitialAdapter.this);
            }
        }

        @Override
        public void onAdShow() {
            if (mImpressListener != null) {
                mImpressListener.onInterstitialAdShow(TTATInterstitialAdapter.this);
            }
        }

        @Override
        public void onAdDismiss() {
            if (mImpressListener != null) {
                mImpressListener.onInterstitialAdClose(TTATInterstitialAdapter.this);
            }
        }

    };


    TTAdNative.FullScreenVideoAdListener ttFullScrenAdListener = new TTAdNative.FullScreenVideoAdListener() {
        @Override
        public void onError(int code, String message) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onInterstitialAdLoadFail(TTATInterstitialAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, String.valueOf(code), message));
            }
        }

        @Override
        public void onFullScreenVideoAdLoad(TTFullScreenVideoAd ad) {
            mTTFullScreenVideoAd = ad;

            if (mLoadResultListener != null) {
                mLoadResultListener.onInterstitialAdDataLoaded(TTATInterstitialAdapter.this);
            }
        }

        @Override
        public void onFullScreenVideoCached() {
            if (mLoadResultListener != null) {
                mLoadResultListener.onInterstitialAdLoaded(TTATInterstitialAdapter.this);
            }
            try {
                TTATInitManager.getInstance().put(getTrackingInfo().getmUnitGroupUnitId(), mTTFullScreenVideoAd);
            } catch (Exception e) {

            }
        }

    };

    TTFullScreenVideoAd.FullScreenVideoAdInteractionListener ttFullScreenEventListener = new TTFullScreenVideoAd.FullScreenVideoAdInteractionListener() {

        @Override
        public void onAdShow() {
            if (mImpressListener != null) {
                mImpressListener.onInterstitialAdShow(TTATInterstitialAdapter.this);
                mImpressListener.onInterstitialAdVideoStart(TTATInterstitialAdapter.this);
            }
        }

        @Override
        public void onAdVideoBarClick() {
            if (mImpressListener != null) {
                mImpressListener.onInterstitialAdClicked(TTATInterstitialAdapter.this);
            }
        }

        @Override
        public void onAdClose() {
            if (mImpressListener != null) {
                mImpressListener.onInterstitialAdClose(TTATInterstitialAdapter.this);
            }
            try {
                TTATInitManager.getInstance().remove(getTrackingInfo().getmUnitGroupUnitId());
            } catch (Exception e) {

            }
        }

        @Override
        public void onVideoComplete() {
            if (mImpressListener != null) {
                mImpressListener.onInterstitialAdVideoEnd(TTATInterstitialAdapter.this);
            }
        }

        @Override
        public void onSkippedVideo() {
        }

    };


    TTAdNative.NativeExpressAdListener expressAdListener = new TTAdNative.NativeExpressAdListener() {
        @Override
        public void onError(int i, String s) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onInterstitialAdLoadFail(TTATInterstitialAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, String.valueOf(i), s));
            }
        }

        @Override
        public void onNativeExpressAdLoad(List<TTNativeExpressAd> list) {
            mTTNativeExpressAd = list.get(0);
            mTTNativeExpressAd.render();
            if (mLoadResultListener != null) {
                mLoadResultListener.onInterstitialAdLoaded(TTATInterstitialAdapter.this);
            }
            try {
                TTATInitManager.getInstance().put(getTrackingInfo().getmUnitGroupUnitId(), mTTNativeExpressAd);
            } catch (Exception e) {

            }
        }
    };


    TTNativeExpressAd.AdInteractionListener adExpressInteractionListener = new TTNativeExpressAd.AdInteractionListener() {
        @Override
        public void onAdDismiss() {
            if (mImpressListener != null) {
                mImpressListener.onInterstitialAdClose(TTATInterstitialAdapter.this);
            }
            if (mTTNativeExpressAd != null) {
                mTTNativeExpressAd.destroy();
            }
            try {
                TTATInitManager.getInstance().remove(getTrackingInfo().getmUnitGroupUnitId());
            } catch (Exception e) {

            }
        }

        @Override
        public void onAdClicked(View view, int i) {
            if (mImpressListener != null) {
                mImpressListener.onInterstitialAdClicked(TTATInterstitialAdapter.this);
            }
        }

        @Override
        public void onAdShow(View view, int i) {
            if (mImpressListener != null) {
                mImpressListener.onInterstitialAdShow(TTATInterstitialAdapter.this);
            }
        }

        @Override
        public void onRenderFail(View view, String s, int i) {
        }

        @Override
        public void onRenderSuccess(View view, float v, float v1) {

        }
    };

    @Override
    public void loadInterstitialAd(final Context context, Map<String, Object> serverExtras, final ATMediationSetting mediationSetting, final CustomInterstitialListener customRewardVideoListener) {

        mLoadResultListener = customRewardVideoListener;

        if (serverExtras == null) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onInterstitialAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "This placement's params in server is null!"));
            }
            return;
        }

        String appId = (String) serverExtras.get("app_id");
        slotId = (String) serverExtras.get("slot_id");

        if (TextUtils.isEmpty(appId) || TextUtils.isEmpty(slotId)) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onInterstitialAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "app_id or slot_id is empty!"));
            }
            return;
        }

        if (serverExtras.containsKey("is_video")) {
            if (serverExtras.get("is_video").toString().equals("1")) {
                isVideo = true;
            }
        }

        if (!(context instanceof Activity)) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onInterstitialAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "context must be activity"));
            }
            return;
        }

        int layoutType = 0;
        if (serverExtras.containsKey("layout_type")) {
            layoutType = Integer.parseInt(serverExtras.get("layout_type").toString());
        }

        final String personalized_template = (String) serverExtras.get("personalized_template");

        final int finalLayoutType = layoutType;
        TTATInitManager.getInstance().initSDK(context, serverExtras, new TTATInitManager.InitCallback() {
            @Override
            public void onFinish() {
                startLoad(context, mediationSetting, finalLayoutType, personalized_template);
            }
        });
    }

    private void startLoad(Context context, ATMediationSetting mediationSetting, int layoutType, String personalized_template) {
        TTAdManager ttAdManager = TTAdSdk.getAdManager();

        /**Get the width set by the developer**/
        TTATInterstitialSetting setting;
        int developerSetExpressWidth = 0;
        if (mediationSetting instanceof TTATInterstitialSetting) {
            setting = (TTATInterstitialSetting) mediationSetting;
            developerSetExpressWidth = setting.getInterstitialWidth();
        }


        TTAdNative mTTAdNative = ttAdManager.createAdNative(context);//baseContext is recommended for Activity
        AdSlot.Builder adSlotBuilder = new AdSlot.Builder().setCodeId(slotId);
        int width = context.getResources().getDisplayMetrics().widthPixels;
        int height = context.getResources().getDisplayMetrics().heightPixels;
        adSlotBuilder.setImageAcceptedSize(width, height); //must be set
        adSlotBuilder.setAdCount(1);


        if (isVideo) {

            try {
                if (!TextUtils.isEmpty(personalized_template) && TextUtils.equals("1", personalized_template)) {
                    adSlotBuilder.setExpressViewAcceptedSize(px2dip(context, width), px2dip(context, height));
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }

            AdSlot adSlot = adSlotBuilder.build();
            mTTAdNative.loadFullScreenVideoAd(adSlot, ttFullScrenAdListener);
        } else {
            if (layoutType == 1) { //Native Express Interstitial
                float density = context.getResources().getDisplayMetrics().density;
                /**If developer width is set to 0, the default width is used**/
                int expressWidth = developerSetExpressWidth <= 0 ? (int) ((Math.min(width, height) - 30 * density) / density) : (int) (developerSetExpressWidth / density);
                adSlotBuilder.setExpressViewAcceptedSize(expressWidth, 0);
                AdSlot adSlot = adSlotBuilder.build();
                mTTAdNative.loadInteractionExpressAd(adSlot, expressAdListener);
            } else {
                AdSlot adSlot = adSlotBuilder.build();
                mTTAdNative.loadInteractionAd(adSlot, ttInterstitialAdListener);
            }

        }
    }

    @Override
    public boolean isAdReady() {
        return mttInterstitialAd != null || mTTFullScreenVideoAd != null || mTTNativeExpressAd != null;
    }

    @Override
    public void show(Context context) {
        try {
            if (mttInterstitialAd != null && context instanceof Activity) {
                mttInterstitialAd.setAdInteractionListener(interactionListener);
                mttInterstitialAd.showInteractionAd((Activity) context);
            }

            if (mTTFullScreenVideoAd != null && context instanceof Activity) {
                mTTFullScreenVideoAd.setFullScreenVideoAdInteractionListener(ttFullScreenEventListener);
                mTTFullScreenVideoAd.showFullScreenVideoAd((Activity) context);
            }

            if (mTTNativeExpressAd != null && context instanceof Activity) {
                mTTNativeExpressAd.setExpressInteractionListener(adExpressInteractionListener);
                mTTNativeExpressAd.showInteractionExpressAd((Activity) context);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void clean() {
    }

    @Override
    public void onResume() {
    }

    @Override
    public void onPause() {

    }

    @Override
    public String getSDKVersion() {
        return TTATConst.getNetworkVersion();
    }

    @Override
    public String getNetworkName() {
        return TTATInitManager.getInstance().getNetworkName();
    }

    private static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / (scale <= 0 ? 1 : scale) + 0.5f);
    }
}
