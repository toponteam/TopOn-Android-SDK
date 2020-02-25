package com.anythink.network.unityads;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.ErrorCode;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.services.monetization.UnityMonetization;
import com.unity3d.services.monetization.placementcontent.ads.ShowAdListenerAdapter;
import com.unity3d.services.monetization.placementcontent.ads.ShowAdPlacementContent;
import com.unity3d.services.monetization.placementcontent.core.PlacementContent;
import com.unity3d.services.monetization.placementcontent.purchasing.PromoAdPlacementContent;

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

        PlacementContent placementContent = UnityMonetization.getPlacementContent(placement_id);
        if (placementContent != null && placementContent.isReady()) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onInterstitialAdLoaded(this);
            }
        } else {
            UnityAdsATInitManager.getInstance().putLoadResultAdapter(placement_id, this);
            UnityAdsATInitManager.getInstance().initSDK(context, serverExtras);
        }

    }

    @Override
    public boolean isAdReady() {
        PlacementContent placementContent = UnityMonetization.getPlacementContent(placement_id);
        return placementContent != null && placementContent.isReady();
    }

    @Override
    public String getSDKVersion() {
        return UnityAdsATConst.getNetworkVersion();
    }

    @Override
    public void show(Context context) {
        PlacementContent placementContent = UnityMonetization.getPlacementContent(placement_id);

        if (placementContent instanceof PromoAdPlacementContent && context instanceof Activity) {
            ((PromoAdPlacementContent) placementContent).show((Activity) context, new ShowAdListenerAdapter() {
                @Override
                public void onAdStarted(String placementId) {
                    if (mImpressListener != null) {
                        mImpressListener.onInterstitialAdShow(UnityAdsATInterstitialAdapter.this);
                    }
                }

                @Override
                public void onAdFinished(String placementId, UnityAds.FinishState withState) {
                    if (mImpressListener != null) {
                        mImpressListener.onInterstitialAdClose(UnityAdsATInterstitialAdapter.this);
                    }
                }
            });
        } else if (placementContent instanceof ShowAdPlacementContent && context instanceof Activity) {
            ((ShowAdPlacementContent) placementContent).show((Activity) context, new ShowAdListenerAdapter() {
                @Override
                public void onAdStarted(String placementId) {
                    if (mImpressListener != null) {
                        mImpressListener.onInterstitialAdShow(UnityAdsATInterstitialAdapter.this);
                    }
                }

                @Override
                public void onAdFinished(String placementId, UnityAds.FinishState withState) {
                    if (mImpressListener != null) {
                        mImpressListener.onInterstitialAdClose(UnityAdsATInterstitialAdapter.this);
                    }
                }
            });
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

    public void notifyLoaded(String placementId) {
        if (placementId.equals(placement_id)) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onInterstitialAdLoaded(UnityAdsATInterstitialAdapter.this);
            }
        }
    }

    public void notifyLoadFail(String code, String msg) {
        if (mLoadResultListener != null) {
            mLoadResultListener.onInterstitialAdLoadFail(UnityAdsATInterstitialAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, code, msg));
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
