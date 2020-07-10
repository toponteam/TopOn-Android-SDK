package com.anythink.network.baidu;

import android.app.Activity;
import android.text.TextUtils;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.ErrorCode;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoListener;
import com.baidu.mobads.rewardvideo.RewardVideoAd;

import java.util.Map;

public class BaiduATRewardedVideoAdapter extends CustomRewardVideoAdapter {

    private static final String TAG = BaiduATRewardedVideoAdapter.class.getSimpleName();

    RewardVideoAd mRewardVideoAd;
    private String mAdPlaceId = "";


    @Override
    public void loadRewardVideoAd(final Activity activity, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, CustomRewardVideoListener customRewardedVideoListener) {
        mLoadResultListener = customRewardedVideoListener;

        if (activity == null) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onRewardedVideoAdFailed(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "activity is null."));
            }
            return;
        }


        if (serverExtras == null) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onRewardedVideoAdFailed(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", " appid or unitid  is empty."));
            }
            return;
        } else {

            String mAppId = (String) serverExtras.get("app_id");
            mAdPlaceId = (String) serverExtras.get("ad_place_id");
            if (TextUtils.isEmpty(mAppId) || TextUtils.isEmpty(mAdPlaceId)) {
                if (mLoadResultListener != null) {
                    mLoadResultListener.onRewardedVideoAdFailed(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", " app_id ,ad_place_id is empty."));
                }
                return;
            }
        }

        BaiduATInitManager.getInstance().initSDK(activity, serverExtras, new BaiduATInitManager.InitCallback() {
            @Override
            public void onSuccess() {
                startLoadAd(activity);
            }

            @Override
            public void onError(Throwable e) {
                if (mLoadResultListener != null) {
                    mLoadResultListener.onRewardedVideoAdFailed(BaiduATRewardedVideoAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, "", e.getMessage()));
                }
            }
        });
    }

    private void startLoadAd(Activity activity) {
        mRewardVideoAd = new RewardVideoAd(activity.getApplicationContext(), mAdPlaceId, new RewardVideoAd.RewardVideoAdListener() {
            @Override
            public void onAdShow() {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayStart(BaiduATRewardedVideoAdapter.this);
                }
            }

            @Override
            public void onAdClick() {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayClicked(BaiduATRewardedVideoAdapter.this);
                }
            }

            @Override
            public void onAdClose(float v) {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdClosed(BaiduATRewardedVideoAdapter.this);
                }
            }

            @Override
            public void onAdFailed(String s) {
                if (mLoadResultListener != null) {
                    mLoadResultListener.onRewardedVideoAdFailed(BaiduATRewardedVideoAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, "", s));
                }
            }

            @Override
            public void onVideoDownloadSuccess() {
                if (mLoadResultListener != null) {
                    mLoadResultListener.onRewardedVideoAdLoaded(BaiduATRewardedVideoAdapter.this);
                }
            }

            @Override
            public void onVideoDownloadFailed() {

            }

            @Override
            public void playCompletion() {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayEnd(BaiduATRewardedVideoAdapter.this);
                }

                if (mImpressionListener != null) {
                    mImpressionListener.onReward(BaiduATRewardedVideoAdapter.this);
                }
            }
        });
        mRewardVideoAd.load();
    }

    @Override
    public boolean isAdReady() {
        if (mRewardVideoAd != null) {
            return mRewardVideoAd.isReady();
        }
        return false;
    }

    @Override
    public void show(Activity activity) {
        try {
            mRewardVideoAd.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void clean() {
    }

    @Override
    public void onResume(Activity activity) {
        if (mRewardVideoAd != null) {
            mRewardVideoAd.resume();
        }
    }

    @Override
    public void onPause(Activity activity) {
        if (mRewardVideoAd != null) {
            mRewardVideoAd.pause();
        }
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
