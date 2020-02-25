package com.anythink.network.oneway;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.AdError;
import com.anythink.core.api.ErrorCode;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoListener;

import java.util.Map;

import mobi.oneway.export.Ad.OWRewardedAd;
import mobi.oneway.export.Ad.OnewaySdk;
import mobi.oneway.export.AdListener.OWRewardedAdListener;
import mobi.oneway.export.enums.OnewayAdCloseType;
import mobi.oneway.export.enums.OnewaySdkError;


public class OnewayATRewardedVideoAdapter extends CustomRewardVideoAdapter {
    private final String TAG = getClass().getSimpleName();

    String mPublishId;

    OWRewardedAdListener mListener = new OWRewardedAdListener() {
        @Override
        public void onAdReady() {
            if (mLoadResultListener != null) {
                mLoadResultListener.onRewardedVideoAdLoaded(OnewayATRewardedVideoAdapter.this);
            }
        }

        @Override
        public void onAdShow(String s) {
            if (mImpressionListener != null) {
                mImpressionListener.onRewardedVideoAdPlayStart(OnewayATRewardedVideoAdapter.this);
            }
        }

        @Override
        public void onAdClick(String s) {
            if (mImpressionListener != null) {
                mImpressionListener.onRewardedVideoAdPlayClicked(OnewayATRewardedVideoAdapter.this);
            }
        }

        @Override
        public void onAdClose(String s, OnewayAdCloseType onewayAdCloseType) {

            if (mImpressionListener != null) {
                if (onewayAdCloseType == OnewayAdCloseType.COMPLETED) {
                    mImpressionListener.onReward(OnewayATRewardedVideoAdapter.this);
                }
                mImpressionListener.onRewardedVideoAdClosed(OnewayATRewardedVideoAdapter.this);
            }
        }

        @Override
        public void onAdFinish(String s, OnewayAdCloseType onewayAdCloseType, String s1) {
            if (mImpressionListener != null) {
                mImpressionListener.onRewardedVideoAdPlayEnd(OnewayATRewardedVideoAdapter.this);
            }
        }

        @Override
        public void onSdkError(OnewaySdkError onewaySdkError, String s) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onRewardedVideoAdFailed(OnewayATRewardedVideoAdapter.this
                        , ErrorCode.getErrorCode(ErrorCode.noADError, "", s));
            }
        }
    };

    @Override
    public void loadRewardVideoAd(Activity pActivity, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, CustomRewardVideoListener customRewardVideoListener) {
        String publishId = "";

        if (serverExtras.containsKey("publisher_id")) {
            publishId = serverExtras.get("publisher_id").toString();
        }


        mLoadResultListener = customRewardVideoListener;
        if (TextUtils.isEmpty(publishId)) {
            Log.e(TAG, "publishId is empty, place check once more....");
            if (mLoadResultListener != null) {
                AdError adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", " publishId  is empty.");
                mLoadResultListener.onRewardedVideoAdFailed(this, adError);

            }
            return;
        }

        mPublishId = publishId;
        OnewayATInitManager.getInstance().initSDK(pActivity, serverExtras);
        OnewaySdk.configure(pActivity, publishId);

        if (OnewayATInitManager.getInstance().isInitRewardVideo()) {
            if (OWRewardedAd.isReady()) {
                OWRewardedAd.setListener(mListener);
                if (mLoadResultListener != null) {
                    mLoadResultListener.onRewardedVideoAdLoaded(OnewayATRewardedVideoAdapter.this);
                }
            } else {
                if (mLoadResultListener != null) {
                    mLoadResultListener.onRewardedVideoAdFailed(OnewayATRewardedVideoAdapter.this,
                            ErrorCode.getErrorCode(ErrorCode.noADError, "", "No Fill"));
                }
            }
        } else {
            OnewayATInitManager.getInstance().initRewardVideo(pActivity, mListener);
        }

    }

    @Override
    public boolean initNetworkObjectByPlacementId(Context context, Map<String, Object> serverExtras, ATMediationSetting mediationSetting) {
        if (serverExtras.containsKey("publisher_id")) {
            mPublishId = serverExtras.get("publisher_id").toString();
            OWRewardedAd.setListener(mListener);
            return true;
        }
        return false;
    }

    @Override
    public boolean isAdReady() {
        return OWRewardedAd.isReady();
    }

    @Override
    public void show(Activity activity) {
        if (OWRewardedAd.isReady()) {
            OWRewardedAd.show(activity);
        }
    }

    @Override
    public void clean() {

    }

    @Override
    public void onResume(Activity activity) {
    }

    @Override
    public void onPause(Activity activity) {

    }

    @Override
    public String getSDKVersion() {
        return OnewayATConst.getNetworkVersion();
    }

    @Override
    public String getNetworkName() {
        return OnewayATInitManager.getInstance().getNetworkName();
    }
}
