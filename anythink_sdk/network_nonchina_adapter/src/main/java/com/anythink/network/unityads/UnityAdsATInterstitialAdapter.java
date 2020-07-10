package com.anythink.network.unityads;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.ErrorCode;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialListener;
import com.unity3d.ads.UnityAds;

import java.util.Map;

public class UnityAdsATInterstitialAdapter extends CustomInterstitialAdapter {
    private static final String TAG = UnityAdsATInterstitialAdapter.class.getSimpleName();

    String placement_id = "";

    @Override
    public void loadInterstitialAd(Context context, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, CustomInterstitialListener customInterstitialListener) {
        mLoadResultListener = customInterstitialListener;
        if (context == null) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onInterstitialAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "context is null."));
            }
            return;
        }

        if (!(context instanceof Activity)) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onInterstitialAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "context must be activity."));
            }
            return;
        }


        if (serverExtras == null) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onInterstitialAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "This placement's params in server is null!"));
            }
            return;
        } else {
            String game_id = (String) serverExtras.get("game_id");
            placement_id = (String) serverExtras.get("placement_id");

            if (TextUtils.isEmpty(game_id) || TextUtils.isEmpty(placement_id)) {
                if (mLoadResultListener != null) {
                    mLoadResultListener.onInterstitialAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "unityads game_id, placement_id is empty!"));
                }
                return;
            }
        }

        UnityAds.PlacementState placementState = UnityAds.getPlacementState(placement_id);
        if (UnityAds.PlacementState.READY == placementState) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onInterstitialAdLoaded(this);
            }
        } else {
            UnityAdsATInitManager.getInstance().putLoadResultAdapter(placement_id, this);
            UnityAdsATInitManager.getInstance().initSDK(context, serverExtras);
            UnityAds.load(placement_id);
        }
    }

    @Override
    public boolean isAdReady() {
        return UnityAds.isReady(placement_id);
    }

    @Override
    public String getSDKVersion() {
        return UnityAdsATConst.getNetworkVersion();
    }

    @Override
    public void show(Context context) {
        UnityAdsATInitManager.getInstance().putAdapter(placement_id, this);
        if (context instanceof Activity) {
            UnityAds.show(((Activity) context), placement_id);
        }
    }

    @Override
    public boolean initNetworkObjectByPlacementId(Context context, Map<String, Object> serverExtras, ATMediationSetting mediationSetting) {
        if (serverExtras != null) {
            if (serverExtras.containsKey("game_id") && serverExtras.containsKey("placement_id")) {
                placement_id = (String) serverExtras.get("placement_id");
                return true;
            }
        }
        return false;
    }

    void notifyLoaded(String placementId) {
        if (placementId.equals(placement_id)) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onInterstitialAdLoaded(UnityAdsATInterstitialAdapter.this);
            }
        }
    }

    void notifyLoadFail(String code, String msg) {
        if (mLoadResultListener != null) {
            mLoadResultListener.onInterstitialAdLoadFail(UnityAdsATInterstitialAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, code, msg));
        }
    }

    void notifyStart(String placementId) {
        if (mImpressListener != null && placement_id.equals(placementId)) {
            mImpressListener.onInterstitialAdShow(UnityAdsATInterstitialAdapter.this);
        }
    }

    void notifyFinish(String placementId, UnityAds.FinishState finishState) {
        if (mImpressListener != null && placement_id.equals(placementId)) {
            mImpressListener.onInterstitialAdClose(UnityAdsATInterstitialAdapter.this);
        }
    }

    void notifyClick(String placementId) {
        if (mImpressListener != null && placement_id.equals(placementId)) {
            mImpressListener.onInterstitialAdClicked(UnityAdsATInterstitialAdapter.this);
        }
    }

    @Override
    public void clean() {

    }

    @Override
    public String getNetworkName() {
        return UnityAdsATInitManager.getInstance().getNetworkName();
    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }
}
