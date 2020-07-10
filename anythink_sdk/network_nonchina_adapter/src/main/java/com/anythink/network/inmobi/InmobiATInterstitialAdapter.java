package com.anythink.network.inmobi;

import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.ErrorCode;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialListener;
import com.inmobi.ads.AdMetaInfo;
import com.inmobi.ads.InMobiAdRequestStatus;
import com.inmobi.ads.InMobiInterstitial;
import com.inmobi.ads.listeners.InterstitialAdEventListener;

import java.util.Map;

/**
 * Created by zhou on 2018/6/27.
 */

public class InmobiATInterstitialAdapter extends CustomInterstitialAdapter {
    private static final String TAG = InmobiATInterstitialAdapter.class.getSimpleName();

    InMobiInterstitial interstitialAd;
    Long placeId;
    int mClickCallbackType;

    /***
     * init and load
     */
    private void initAndLoad(final Context context, final Map<String, Object> serverExtras) {
        InmobiATInitManager.getInstance().initSDK(context.getApplicationContext(), serverExtras, new InmobiATInitManager.OnInitCallback() {
            @Override
            public void onSuccess() {
                startLoadAd(context);
            }

            @Override
            public void onError(String errorMsg) {
                if (mLoadResultListener != null) {
                    mLoadResultListener.onInterstitialAdLoadFail(InmobiATInterstitialAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "Inmobi " + errorMsg));
                }
            }
        });
    }


    @Override
    public void loadInterstitialAd(Context context, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, CustomInterstitialListener customInterstitialListener) {
        mLoadResultListener = customInterstitialListener;
        if (context == null) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onInterstitialAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "activity is null."));
            }
            return;
        }
        if (serverExtras == null) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onInterstitialAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "inmobi serverExtras is null!"));
            }
            return;
        } else {

            String accountId = (String) serverExtras.get("app_id");
            String unitId = (String) serverExtras.get("unit_id");

            if (TextUtils.isEmpty(accountId) || TextUtils.isEmpty(unitId)) {
                if (mLoadResultListener != null) {
                    mLoadResultListener.onInterstitialAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "inmobi account_id or unit_id is empty!"));
                }
                return;
            }
            placeId = Long.parseLong(unitId);
        }
        mClickCallbackType = 0;
        initAndLoad(context, serverExtras);
    }

    @Override
    public boolean isAdReady() {
        if (interstitialAd != null) {
            return interstitialAd.isReady();
        }
        return false;
    }

    @Override
    public void show(Context context) {
        if (interstitialAd != null && isAdReady()) {
            interstitialAd.show();
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
        return InmobiATConst.getNetworkVersion();
    }

    @Override
    public String getNetworkName() {
        return InmobiATInitManager.getInstance().getNetworkName();
    }

    private void startLoadAd(Context context) {
        interstitialAd = new InMobiInterstitial(context, placeId, new InterstitialAdEventListener() {
            @Override
            public void onAdLoadFailed(InMobiInterstitial inMobiInterstitial, InMobiAdRequestStatus inMobiAdRequestStatus) {
                if (mLoadResultListener != null) {
                    mLoadResultListener.onInterstitialAdLoadFail(InmobiATInterstitialAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, "" + inMobiAdRequestStatus.getStatusCode(), inMobiAdRequestStatus.getMessage()));
                }
            }

            @Override
            public void onAdFetchSuccessful(InMobiInterstitial inMobiInterstitial, AdMetaInfo adMetaInfo) {
                if (mLoadResultListener != null) {
                    mLoadResultListener.onInterstitialAdDataLoaded(InmobiATInterstitialAdapter.this);
                }
            }

            @Override
            public void onAdLoadSucceeded(InMobiInterstitial inMobiInterstitial, AdMetaInfo adMetaInfo) {
                if (mLoadResultListener != null) {
                    mLoadResultListener.onInterstitialAdLoaded(InmobiATInterstitialAdapter.this);
                }
            }

            @Override
            public void onAdDisplayFailed(InMobiInterstitial inMobiInterstitial) {
            }

            @Override
            public void onAdWillDisplay(InMobiInterstitial inMobiInterstitial) {
            }

            @Override
            public void onAdDisplayed(InMobiInterstitial inMobiInterstitial, AdMetaInfo adMetaInfo) {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdShow(InmobiATInterstitialAdapter.this);
                }
            }

            @Override
            public void onAdDismissed(InMobiInterstitial inMobiInterstitial) {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdClose(InmobiATInterstitialAdapter.this);
                }
            }

            @Override
            public void onAdClicked(InMobiInterstitial inMobiInterstitial, Map<Object, Object> map) {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdClicked(InmobiATInterstitialAdapter.this);
                }
            }
        });
        interstitialAd.load();
    }
}