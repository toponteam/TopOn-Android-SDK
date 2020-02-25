package com.anythink.network.ogury;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.AdError;
import com.anythink.core.api.ErrorCode;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoListener;

import java.util.Map;

import io.presage.common.AdConfig;
import io.presage.common.network.models.RewardItem;
import io.presage.interstitial.optinvideo.PresageOptinVideo;
import io.presage.interstitial.optinvideo.PresageOptinVideoCallback;

public class OguryATRewardedVideoAdapter extends CustomRewardVideoAdapter {

    String mUnitId;
    boolean mIsReward;

    private PresageOptinVideo mPresageOptinVideo;

    @Override
    public void loadRewardVideoAd(final Activity activity, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, CustomRewardVideoListener customRewardVideoListener) {
        mLoadResultListener = customRewardVideoListener;

        String assetKey = "";
        String unitId = "";
        if (serverExtras == null) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onRewardedVideoAdFailed(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "service params is empty."));
            }
            return;
        } else {
            if (serverExtras.containsKey("key")) {
                assetKey = serverExtras.get("key").toString();
            }
            if (serverExtras.containsKey("unit_id")) {
                unitId = serverExtras.get("unit_id").toString();
            }

            if (TextUtils.isEmpty(assetKey) || TextUtils.isEmpty(unitId)) {
                if (mLoadResultListener != null) {
                    AdError adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", "asset_key、unit_id could not be null.");
                    mLoadResultListener.onRewardedVideoAdFailed(this, adError);
                }
                return;
            }
        }
        mUnitId = unitId;

        OguryATInitManager.getInstance().initSDK(activity, serverExtras, new OguryATInitManager.Callback() {
            @Override
            public void onSuccess() {
                init(activity);
            }
        });
    }

    private void init(Context context) {
        AdConfig adConfig = new AdConfig(mUnitId);
        mPresageOptinVideo = new PresageOptinVideo(context, adConfig);
        if(!TextUtils.isEmpty(mUserId)) {
            mPresageOptinVideo.setUserId(mUserId);
        }
        mPresageOptinVideo.setOptinVideoCallback(new PresageOptinVideoCallback() {
            @Override
            public void onAdAvailable() {
                if(mLoadResultListener != null) {
                    mLoadResultListener.onRewardedVideoAdDataLoaded(OguryATRewardedVideoAdapter.this);
                }
            }

            @Override
            public void onAdNotAvailable() {
                if(mLoadResultListener != null) {
                    mLoadResultListener.onRewardedVideoAdFailed(OguryATRewardedVideoAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "no ad available"));
                }
            }

            @Override
            public void onAdLoaded() {
                if(mLoadResultListener != null) {
                    mLoadResultListener.onRewardedVideoAdLoaded(OguryATRewardedVideoAdapter.this);
                }
            }

            @Override
            public void onAdNotLoaded () {
            }

            @Override
            public void onAdDisplayed() {
                if(mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayStart(OguryATRewardedVideoAdapter.this);
                }
            }

            @Override
            public void onAdClosed() {
                if(mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayEnd(OguryATRewardedVideoAdapter.this);
                    if(mIsReward) {
                        mImpressionListener.onReward(OguryATRewardedVideoAdapter.this);
                    }
                    mImpressionListener.onRewardedVideoAdClosed(OguryATRewardedVideoAdapter.this);
                }
            }

            @Override
            public void onAdError(int code) {
                /*
                code 0: load failed
                code 1: phone not connected to internet
                code 2: ad disabled
                code 3: various error (configuration file not synced)
                code 4: ad expires in 4 hours if it was not shown
                code 5: start method not called
                */
                if(mLoadResultListener != null) {
                    mLoadResultListener.onRewardedVideoAdFailed(OguryATRewardedVideoAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, "" + code, OguryATInitManager.getErrorMsg(code)));
                }
            }

            @Override
            public void onAdRewarded(RewardItem rewardItem) {
                mIsReward = true;
            }
        });

        mPresageOptinVideo.load();
    }

    @Override
    public void show(Activity activity) {
        if (isAdReady()) {
            mPresageOptinVideo.show();
        }
    }

    @Override
    public void onResume(Activity activity) {

    }

    @Override
    public void onPause(Activity activity) {

    }

    @Override
    public boolean isAdReady() {
        return mPresageOptinVideo != null && mPresageOptinVideo.isLoaded();
    }

    @Override
    public String getSDKVersion() {
        return OguryATConst.getSDKVersion();
    }

    @Override
    public void clean() {

    }

    @Override
    public String getNetworkName() {
        return OguryATInitManager.getInstance().getNetworkName();
    }
}
