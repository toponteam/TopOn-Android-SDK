package com.anythink.network.toutiao;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.ErrorCode;
import com.anythink.splashad.unitgroup.api.CustomSplashAdapter;
import com.anythink.splashad.unitgroup.api.CustomSplashListener;
import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTSplashAd;

import java.util.Map;

public class TTATSplashAdapter extends CustomSplashAdapter implements TTSplashAd.AdInteractionListener {
    private final String TAG = getClass().getSimpleName();

    String appId = "";
    String slotId = "";
    String personalizedTemplate = "";

    CustomSplashListener mListener;

    Activity activity;
    View skipView;

    @Override
    public void loadSplashAd(final Activity activity, final ViewGroup container, View skipView, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, CustomSplashListener customSplashListener) {

        mListener = customSplashListener;

        this.activity = activity;
        this.skipView = skipView;

        if (serverExtras == null) {
            if (mListener != null) {
                mListener.onSplashAdFailed(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "This placement's params in server is null!"));
            }
            return;
        }

        if (serverExtras.containsKey("app_id") && serverExtras.containsKey("slot_id")) {
            appId = (String) serverExtras.get("app_id");
            slotId = (String) serverExtras.get("slot_id");

        } else {
            if (mListener != null) {
                mListener.onSplashAdFailed(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "app_id or slot_id is empty!"));
            }
            return;
        }

        personalizedTemplate = "0";
        if (serverExtras.containsKey("personalized_template")) {
            personalizedTemplate = (String) serverExtras.get("personalized_template");
        }

        if (activity != null) {
            TTATInitManager.getInstance().initSDK(activity, serverExtras, true, new TTATInitManager.InitCallback() {
                @Override
                public void onFinish() {
                    startLoad(activity, container);
                }
            });
        }
    }

    private void startLoad(Activity activity, final ViewGroup container) {
        TTAdManager ttAdManager = TTAdSdk.getAdManager();

        TTAdNative mTTAdNative = ttAdManager.createAdNative(activity);//baseContext is recommended for activity
        AdSlot.Builder adSlotBuilder = new AdSlot.Builder().setCodeId(slotId);

        int width = 0;
        int height = 0;
        ViewGroup.LayoutParams layoutParams = container.getLayoutParams();
        if (layoutParams != null) {
            width = layoutParams.width;
            height = layoutParams.height;
        }
        if (width <= 0) {
            width = activity.getResources().getDisplayMetrics().widthPixels;
        }
        if (height <= 0) {
            height = activity.getResources().getDisplayMetrics().heightPixels;
        }

        adSlotBuilder.setImageAcceptedSize(width, height); //Must be set

        if (TextUtils.equals("1", personalizedTemplate)) {// Native Express
            adSlotBuilder.setExpressViewAcceptedSize(width, height);
        }

        AdSlot adSlot = adSlotBuilder.build();
        mTTAdNative.loadSplashAd(adSlot, new TTAdNative.SplashAdListener() {
            @Override
            public void onError(int i, String s) {
                if (mListener != null) {
                    mListener.onSplashAdFailed(TTATSplashAdapter.this
                            , ErrorCode.getErrorCode(ErrorCode.noADError, i + "", s));
                }
            }

            @Override
            public void onTimeout() {
                if (mListener != null) {
                    mListener.onSplashAdFailed(TTATSplashAdapter.this
                            , ErrorCode.getErrorCode(ErrorCode.timeOutError, "", ""));
                }
            }

            @Override
            public void onSplashAdLoad(TTSplashAd ttSplashAd) {
                if (ttSplashAd != null) {
//                    ttSplashAd.setNotAllowSdkCountdown();
                    ttSplashAd.setSplashInteractionListener(TTATSplashAdapter.this);
                    View splashView = ttSplashAd.getSplashView();
                    if (splashView != null) {
                        if (mListener != null) {
                            mListener.onSplashAdLoaded(TTATSplashAdapter.this);
                        }
                        container.removeAllViews();
                        container.addView(splashView);
                    } else {
                        if (mListener != null) {
                            mListener.onSplashAdFailed(TTATSplashAdapter.this
                                    , ErrorCode.getErrorCode(ErrorCode.noADError, "", ""));
                        }
                    }

                } else {
                    if (mListener != null) {
                        mListener.onSplashAdFailed(TTATSplashAdapter.this
                                , ErrorCode.getErrorCode(ErrorCode.noADError, "", ""));
                    }
                }
            }
        });
    }

    @Override
    public String getNetworkName() {
        return TTATInitManager.getInstance().getNetworkName();
    }


    @Override
    public void clean() {
        activity = null;
        skipView = null;
    }


    @Override
    public void onAdClicked(View view, int i) {
        if (mListener != null) {
            mListener.onSplashAdClicked(this);
        }

    }

    @Override
    public void onAdShow(View view, int i) {
        if (mListener != null) {
            mListener.onSplashAdShow(this);
        }

    }

    @Override
    public void onAdSkip() {
        if (mListener != null) {
            mListener.onSplashAdDismiss(TTATSplashAdapter.this);
        }
    }

    @Override
    public void onAdTimeOver() {
        if (mListener != null) {
            mListener.onSplashAdDismiss(TTATSplashAdapter.this);
        }
    }

    @Override
    public String getSDKVersion() {
        return TTATConst.getNetworkVersion();
    }
}
