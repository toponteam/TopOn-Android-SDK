package com.anythink.network.nend;

import android.app.Activity;
import android.content.Context;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.ErrorCode;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;

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
    public void loadCustomNetworkAd(Context context, Map<String, Object> serverExtras, Map<String, Object> localExtras) {

        if (serverExtras.containsKey("api_key") && serverExtras.containsKey("spot_id")) {
            mApiKey = (String) serverExtras.get("api_key");
            mSpotId = Integer.parseInt((String) serverExtras.get("spot_id"));

        } else {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "app_id or slot_id is empty!");
            }
            return;
        }

        mNendAdRewardedVideo = new NendAdRewardedVideo(context.getApplicationContext(), mSpotId, mApiKey);
        mNendAdRewardedVideo.setUserId(mUserId);
        mNendAdRewardedVideo.setAdListener(new NendAdRewardedListener() {
            @Override
            public void onRewarded(NendAdVideo nendAdVideo, NendAdRewardItem nendAdRewardItem) {
                if (mImpressionListener != null) {
                    mImpressionListener.onReward();
                }
            }

            @Override
            public void onLoaded(NendAdVideo nendAdVideo) {
                if (mLoadListener != null) {
                    mLoadListener.onAdCacheLoaded();
                }
            }

            @Override
            public void onFailedToLoad(NendAdVideo nendAdVideo, int i) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError(i + "", "");
                }
            }

            @Override
            public void onFailedToPlay(NendAdVideo nendAdVideo) {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayFailed("", "onFailedToPlay");
                }
            }

            @Override
            public void onShown(NendAdVideo nendAdVideo) {

            }

            @Override
            public void onClosed(NendAdVideo nendAdVideo) {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdClosed();
                }
            }

            @Override
            public void onStarted(NendAdVideo nendAdVideo) {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayStart();
                }
            }

            @Override
            public void onStopped(NendAdVideo nendAdVideo) {

            }

            @Override
            public void onCompleted(NendAdVideo nendAdVideo) {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayEnd();
                }
            }

            @Override
            public void onAdClicked(NendAdVideo nendAdVideo) {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayClicked();
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
        if (activity != null) {
            if (mNendAdRewardedVideo != null && mNendAdRewardedVideo.isLoaded()) {
                mNendAdRewardedVideo.showAd((activity));
            }
        }
    }

    @Override
    public boolean isAdReady() {
        if (mNendAdRewardedVideo != null) {
            return mNendAdRewardedVideo.isLoaded();
        }
        return false;
    }

    @Override
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        return false;
    }

    @Override
    public void destory() {
        if (mNendAdRewardedVideo != null) {
            mNendAdRewardedVideo.setAdListener(null);
            mNendAdRewardedVideo = null;
        }
    }

    @Override
    public String getNetworkSDKVersion() {
        return "";
    }

    @Override
    public String getNetworkName() {
        return NendATInitManager.getInstance().getNetworkName();
    }

    @Override
    public String getNetworkPlacementId() {
        try {
            return String.valueOf(mSpotId);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

}
