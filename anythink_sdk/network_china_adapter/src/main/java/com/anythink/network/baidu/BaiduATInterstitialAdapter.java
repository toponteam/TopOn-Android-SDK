package com.anythink.network.baidu;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.ErrorCode;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialListener;
import com.baidu.mobads.InterstitialAd;
import com.baidu.mobads.InterstitialAdListener;

import java.util.Map;

public class BaiduATInterstitialAdapter extends CustomInterstitialAdapter {

    private static final String TAG = BaiduATInterstitialAdapter.class.getSimpleName();

    InterstitialAd mInterstitialAd;
    private String mAdPlaceId = "";


    @Override
    public void loadInterstitialAd(final Context context, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, CustomInterstitialListener customInterstitialListener) {
        mLoadResultListener = customInterstitialListener;

        if (context == null) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onInterstitialAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "activity is null."));
            }
            return;
        }

        if (serverExtras == null) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onInterstitialAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", " appid or unitid  is empty."));
            }
            return;
        } else {
            String mAppId = (String) serverExtras.get("app_id");
            mAdPlaceId = (String) serverExtras.get("ad_place_id");

            if (TextUtils.isEmpty(mAppId) || TextUtils.isEmpty(mAdPlaceId)) {
                if (mLoadResultListener != null) {
                    mLoadResultListener.onInterstitialAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", " app_id ,ad_place_id is empty."));
                }
                return;
            }
        }

        if (!(context instanceof Activity)) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onInterstitialAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "context must be activity."));
            }
            return;
        }
        BaiduATInitManager.getInstance().initSDK(context, serverExtras, new BaiduATInitManager.InitCallback() {
            @Override
            public void onSuccess() {
                startLoadAd(context);
            }

            @Override
            public void onError(Throwable e) {
                if (mLoadResultListener != null) {
                    mLoadResultListener.onInterstitialAdLoadFail(BaiduATInterstitialAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, "", e.getMessage()));
                }
            }
        });

    }

    private void startLoadAd(Context context) {
        mInterstitialAd = new InterstitialAd(context, mAdPlaceId);
        mInterstitialAd.setListener(new InterstitialAdListener() {
            @Override
            public void onAdReady() {
                if (mLoadResultListener != null) {
                    mLoadResultListener.onInterstitialAdLoaded(BaiduATInterstitialAdapter.this);

                }
            }

            @Override
            public void onAdPresent() {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdShow(BaiduATInterstitialAdapter.this);

                }
            }

            @Override
            public void onAdClick(InterstitialAd interstitialAd) {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdClicked(BaiduATInterstitialAdapter.this);

                }
            }

            @Override
            public void onAdDismissed() {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdClose(BaiduATInterstitialAdapter.this);

                }
            }

            @Override
            public void onAdFailed(String s) {
                if (mLoadResultListener != null) {
                    mLoadResultListener.onInterstitialAdLoadFail(BaiduATInterstitialAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, "", s));
                }
            }
        });
        mInterstitialAd.loadAd();
    }

    @Override
    public boolean isAdReady() {
        if (mInterstitialAd != null) {
            return mInterstitialAd.isAdReady();
        }
        return false;
    }

    @Override
    public void show(Context context) {
        try {
            if (mInterstitialAd != null && context instanceof Activity) {
                mInterstitialAd.showAd((Activity) context);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void clean() {
        if (mInterstitialAd != null) {
            mInterstitialAd.destroy();
        }
    }

    @Override
    public void onResume() {
    }

    @Override
    public void onPause() {

    }

    @Override
    public String getSDKVersion() {
        return BaiduATConst.getNetworkVersion();
    }

    @Override
    public String getNetworkName() {
        return BaiduATInitManager.getInstance().getNetworkName();
    }
}
