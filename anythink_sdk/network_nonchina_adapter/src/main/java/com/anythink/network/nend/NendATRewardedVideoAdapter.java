package com.anythink.network.nend;

import android.app.Activity;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.ErrorCode;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoListener;

import net.nend.android.NendAdRewardItem;
import net.nend.android.NendAdRewardedListener;
import net.nend.android.NendAdRewardedVideo;
import net.nend.android.NendAdVideo;

import java.util.Map;

public class NendATRewardedVideoAdapter extends CustomRewardVideoAdapter {

    String mApiKey;
    int mSpotId;
    NendAdRewardedVideo mNendAdRewardedVideo;

    @Override
    public void loadRewardVideoAd(Activity activity, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, CustomRewardVideoListener customRewardVideoListener) {
        mLoadResultListener = customRewardVideoListener;
        if (serverExtras == null) {
            if (customRewardVideoListener != null) {
                customRewardVideoListener.onRewardedVideoAdFailed(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "This placement's params in server is null!"));
            }
            return;
        }

        if (serverExtras.containsKey("api_key") && serverExtras.containsKey("spot_id")) {
            mApiKey = (String) serverExtras.get("api_key");
            mSpotId = Integer.parseInt((String) serverExtras.get("spot_id"));

        } else {
            if (customRewardVideoListener != null) {
                customRewardVideoListener.onRewardedVideoAdFailed(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "app_id or slot_id is empty!"));
            }
            return;
        }

        mNendAdRewardedVideo = new NendAdRewardedVideo(activity, mSpotId, mApiKey);
        mNendAdRewardedVideo.setUserId(mUserId);
        mNendAdRewardedVideo.setAdListener(new NendAdRewardedListener() {
            @Override
            public void onRewarded(NendAdVideo nendAdVideo, NendAdRewardItem nendAdRewardItem) {
                if (mImpressionListener != null) {
                    mImpressionListener.onReward(NendATRewardedVideoAdapter.this);
                }
            }

            @Override
            public void onLoaded(NendAdVideo nendAdVideo) {
                if (mLoadResultListener != null) {
                    mLoadResultListener.onRewardedVideoAdLoaded(NendATRewardedVideoAdapter.this);
                }
            }

            @Override
            public void onFailedToLoad(NendAdVideo nendAdVideo, int i) {
                if (mLoadResultListener != null) {
                    mLoadResultListener.onRewardedVideoAdFailed(NendATRewardedVideoAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, i + "", ""));
                }
            }

            @Override
            public void onFailedToPlay(NendAdVideo nendAdVideo) {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayFailed(NendATRewardedVideoAdapter.this, ErrorCode.getErrorCode(ErrorCode.rewardedVideoPlayError, "", ""));
                }
            }

            @Override
            public void onShown(NendAdVideo nendAdVideo) {

            }

            @Override
            public void onClosed(NendAdVideo nendAdVideo) {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdClosed(NendATRewardedVideoAdapter.this);
                }
            }

            @Override
            public void onStarted(NendAdVideo nendAdVideo) {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayStart(NendATRewardedVideoAdapter.this);
                }
            }

            @Override
            public void onStopped(NendAdVideo nendAdVideo) {

            }

            @Override
            public void onCompleted(NendAdVideo nendAdVideo) {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayEnd(NendATRewardedVideoAdapter.this);
                }
            }

            @Override
            public void onAdClicked(NendAdVideo nendAdVideo) {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayClicked(NendATRewardedVideoAdapter.this);
                }
            }

            @Override
            public void onInformationClicked(NendAdVideo nendAdVideo) {

            }
        });

        mNendAdRewardedVideo.loadAd();
    }

    @Override
    public void show(Activity activity) {
        if (mNendAdRewardedVideo != null && mNendAdRewardedVideo.isLoaded()) {
            mNendAdRewardedVideo.showAd(activity);
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
        if (mNendAdRewardedVideo != null) {
            return mNendAdRewardedVideo.isLoaded();
        }
        return false;
    }

    @Override
    public void clean() {

    }
    @Override
    public String getSDKVersion() {
        return "";
    }

    @Override
    public String getNetworkName() {
        return NendATInitManager.getInstance().getNetworkName();
    }

}
