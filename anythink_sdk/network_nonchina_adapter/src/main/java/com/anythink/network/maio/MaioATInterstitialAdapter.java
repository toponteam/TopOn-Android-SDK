package com.anythink.network.maio;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.AdError;
import com.anythink.core.api.ErrorCode;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialListener;

import java.util.Map;

import jp.maio.sdk.android.MaioAds;

public class MaioATInterstitialAdapter extends CustomInterstitialAdapter implements MaioATNotify {

    String mZoneId;

    @Override
    public void loadInterstitialAd(Context context, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, CustomInterstitialListener customInterstitialListener) {

        String mMediaId = "";
        if (serverExtras.containsKey("media_id")) {
            mMediaId = serverExtras.get("media_id").toString();
        }

        if (serverExtras.containsKey("zone_id")) {
            mZoneId = serverExtras.get("zone_id").toString();
        }

        mLoadResultListener = customInterstitialListener;
        if (TextUtils.isEmpty(mMediaId) || TextUtils.isEmpty(mZoneId)) {
            if (mLoadResultListener != null) {
                AdError adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", " mediaid or zoneid  is empty.");
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

        if (MaioAds.canShow(mZoneId)) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onInterstitialAdLoaded(this);
            }
            return;
        }
        MaioATInitManager.getInstance().addLoadResultListener(mZoneId, this);
        MaioATInitManager.getInstance().initSDK(context, serverExtras);
    }

    @Override
    public boolean initNetworkObjectByPlacementId(Context context, Map<String, Object> serverExtras, ATMediationSetting mediationSetting) {
        if (serverExtras != null) {
            if (serverExtras.containsKey("zone_id")) {
                mZoneId = serverExtras.get("zone_id").toString();
            }
            return true;
        }
        return false;
    }

    @Override
    public void show(Context context) {
        if (MaioAds.canShow(mZoneId)) {
            MaioATInitManager.getInstance().addListener(mZoneId, this);
            MaioAds.show(mZoneId);
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
        return MaioAds.canShow(mZoneId);
    }

    @Override
    public String getSDKVersion() {
        return MaioATConst.getNetworkVersion();
    }

    @Override
    public void clean() {

    }

    @Override
    public String getNetworkName() {
        return MaioATInitManager.getInstance().getNetworkName();
    }


    //internal callback
    @Override
    public void notifyLoaded() {
        if (mLoadResultListener != null) {
            mLoadResultListener.onInterstitialAdLoaded(this);
        }
    }

    @Override
    public void notifyLoadFail(String code, String msg) {
        if (mLoadResultListener != null) {
            mLoadResultListener.onInterstitialAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, code, msg));
        }
    }

    @Override
    public void notifyPlayStart() {
        if (mImpressListener != null) {
            mImpressListener.onInterstitialAdShow(this);
        }
    }

    @Override
    public void notifyClick() {
        if (mImpressListener != null) {
            mImpressListener.onInterstitialAdClicked(this);
        }
    }

    @Override
    public void notifyClose() {
        if (mImpressListener != null) {
            mImpressListener.onInterstitialAdClose(this);
        }
    }

    @Override
    public void notifyPlayEnd(boolean isReward) {

    }

    @Override
    public void notifyPlayFail(String code, String msg) {

    }
}
