package com.anythink.network.oneway;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.AdError;
import com.anythink.core.api.ErrorCode;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialListener;

import java.util.Map;

import mobi.oneway.export.Ad.OWInterstitialAd;
import mobi.oneway.export.Ad.OnewaySdk;
import mobi.oneway.export.AdListener.OWInterstitialAdListener;
import mobi.oneway.export.enums.OnewayAdCloseType;
import mobi.oneway.export.enums.OnewaySdkError;


/**
 * Copyright (C) 2018 {XX} Science and Technology Co., Ltd.
 *
 * @version V{XX_XX}
 * @Author ：Created by zhoushubin on 2018/9/20.
 * @Email: zhoushubin@salmonads.com
 */
public class OnewayATInterstitialAdapter extends CustomInterstitialAdapter {
    public static String TAG = "oneway";


    String mPublishId;

    OWInterstitialAdListener mListener = new OWInterstitialAdListener() {
        @Override
        public void onAdReady() {
            // The ad is ready, you can call the show () method to play the ad
            if (mLoadResultListener != null) {
                mLoadResultListener.onInterstitialAdLoaded(OnewayATInterstitialAdapter.this);
            }
        }

        @Override
        public void onAdShow(String tag) {
            // Advertising has begun
            if (mImpressListener != null) {

                mImpressListener.onInterstitialAdShow(OnewayATInterstitialAdapter.this);
                mImpressListener.onInterstitialAdVideoStart(OnewayATInterstitialAdapter.this);
            }
        }

        @Override
        public void onAdClick(String tag) {
            // Ad click event
            if (mImpressListener != null) {
                mImpressListener.onInterstitialAdClicked(OnewayATInterstitialAdapter.this);
            }
        }

        @Override
        public void onAdClose(String tag, OnewayAdCloseType type) {
            if (mImpressListener != null) {
                mImpressListener.onInterstitialAdClose(OnewayATInterstitialAdapter.this);
            }
        }

        @Override
        public void onAdFinish(String s, OnewayAdCloseType onewayAdCloseType, String s1) {
            if (mImpressListener != null) {
                mImpressListener.onInterstitialAdVideoEnd(OnewayATInterstitialAdapter.this);
            }
        }


        @Override
        public void onSdkError(OnewaySdkError error, String message) {
            // An error occurred during SDK initialization or advertisement playback. You can save the error log here to troubleshoot the cause of the error.
            if (mLoadResultListener != null) {
                mLoadResultListener.onInterstitialAdLoadFail(OnewayATInterstitialAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, String.valueOf(error.name()), message));
            }
        }
    };

    @Override
    public void loadInterstitialAd(Context context, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, CustomInterstitialListener customInterstitialListener) {
        String publishId = "";

        if (serverExtras.containsKey("publisher_id")) {
            publishId = serverExtras.get("publisher_id").toString();
        }

        mLoadResultListener = customInterstitialListener;
        if (TextUtils.isEmpty(publishId)) {
            if (mLoadResultListener != null) {
                AdError adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", " publishId  is empty.");
                mLoadResultListener.onInterstitialAdLoadFail(this, adError);

            }
            return;
        }

        if (!(context instanceof Activity)) {
            if (mLoadResultListener != null) {
                AdError adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", "context must be activity.");
                mLoadResultListener.onInterstitialAdLoadFail(this, adError);
            }
            return;
        }

        mPublishId = publishId;
        OnewayATInitManager.getInstance().initSDK(context, serverExtras);
        OnewaySdk.configure(((Activity) context), publishId);

        if (OnewayATInitManager.getInstance().isInitInterstitial()) {
            if (OWInterstitialAd.isReady()) {
                OWInterstitialAd.setListener(mListener);
                if (mLoadResultListener != null) {
                    mLoadResultListener.onInterstitialAdLoaded(OnewayATInterstitialAdapter.this);
                }
            } else {
                if (mLoadResultListener != null) {
                    mLoadResultListener.onInterstitialAdLoadFail(OnewayATInterstitialAdapter.this,
                            ErrorCode.getErrorCode(ErrorCode.noADError, "", "No Fill"));
                }
            }
        } else {
            OnewayATInitManager.getInstance().initInterstitial((Activity) context, mListener);
        }


    }

    @Override
    public boolean initNetworkObjectByPlacementId(Context context, Map<String, Object> serverExtras, ATMediationSetting mediationSetting) {
        if (serverExtras.containsKey("publisher_id")) {
            mPublishId = serverExtras.get("publisher_id").toString();
            OWInterstitialAd.setListener(mListener);
            return true;
        }
        return false;
    }

    @Override
    public boolean isAdReady() {
        return OWInterstitialAd.isReady();
    }

    @Override
    public String getSDKVersion() {
        return OnewayATConst.getNetworkVersion();
    }

    @Override
    public void show(Context context) {
        if (context instanceof Activity) {
            if (OWInterstitialAd.isReady()) {
                OWInterstitialAd.show((Activity) context);
            }
        }
    }

    @Override
    public void clean() {

    }

    @Override
    public String getNetworkName() {
        return OnewayATInitManager.getInstance().getNetworkName();
    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

}
