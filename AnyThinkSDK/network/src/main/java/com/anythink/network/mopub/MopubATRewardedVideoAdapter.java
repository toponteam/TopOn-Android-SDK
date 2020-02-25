package com.anythink.network.mopub;

import android.app.Activity;
import android.support.annotation.NonNull;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.ErrorCode;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoListener;
import com.mopub.common.MoPub;
import com.mopub.common.MoPubReward;
import com.mopub.common.SdkConfiguration;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubRewardedVideoListener;
import com.mopub.mobileads.MoPubRewardedVideos;

import java.util.Map;
import java.util.Set;

/**
 * Created by zhou on 2018/6/27.
 */

public class MopubATRewardedVideoAdapter extends CustomRewardVideoAdapter {
    private final String TAG = MopubATRewardedVideoAdapter.class.getSimpleName();

    MopubRewardedVideoSetting mMopubMediationSetting;
    String adUnitId;
    MoPubRewardedVideoListener mMoPubRewardedVideoListener;

    private void startLoad(Activity activity) {
        mMoPubRewardedVideoListener = new MoPubRewardedVideoListener() {
            @Override
            public void onRewardedVideoLoadSuccess(@NonNull String adUnitId) {
                if (mLoadResultListener != null) {
                    mLoadResultListener.onRewardedVideoAdLoaded(MopubATRewardedVideoAdapter.this);
                }
            }

            @Override
            public void onRewardedVideoLoadFailure(@NonNull String adUnitId, @NonNull MoPubErrorCode errorCode) {
                if (mLoadResultListener != null) {
                    mLoadResultListener.onRewardedVideoAdFailed(MopubATRewardedVideoAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, "", errorCode.toString()));
                }
            }

            @Override
            public void onRewardedVideoStarted(@NonNull String adUnitId) {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayStart(MopubATRewardedVideoAdapter.this);
                }
            }

            @Override
            public void onRewardedVideoPlaybackError(@NonNull String adUnitId, @NonNull MoPubErrorCode errorCode) {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayFailed(MopubATRewardedVideoAdapter.this, ErrorCode.getErrorCode(ErrorCode.rewardedVideoPlayError, "", errorCode.toString()));
                }
            }

            @Override
            public void onRewardedVideoClicked(@NonNull String adUnitId) {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayClicked(MopubATRewardedVideoAdapter.this);
                }
            }

            @Override
            public void onRewardedVideoClosed(@NonNull String adUnitId) {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdClosed(MopubATRewardedVideoAdapter.this);
                }
                if (mActivityRef != null) {
                    MoPub.onStop(mActivityRef.get());
                    MoPub.onDestroy(mActivityRef.get());
                }
            }

            @Override
            public void onRewardedVideoCompleted(@NonNull Set<String> adUnitIds, @NonNull MoPubReward reward) {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayEnd(MopubATRewardedVideoAdapter.this);
                }

                if (mImpressionListener != null) {
                    mImpressionListener.onReward(MopubATRewardedVideoAdapter.this);
                }
            }
        };
        //RewardVideo needs initialization
        SdkConfiguration sdkConfiguration = new SdkConfiguration.Builder(adUnitId).build();
        MoPub.initializeSdk(activity, sdkConfiguration, null);

        MoPubRewardedVideos.setRewardedVideoListener(mMoPubRewardedVideoListener);
        MoPub.onCreate(activity);
        MoPub.onStart(activity);
        MoPubRewardedVideos.loadRewardedVideo(adUnitId, mMopubMediationSetting != null ? mMopubMediationSetting.getRequestParameters(mUserId) : null);
    }

    @Override
    public void clean() {

    }

    @Override
    public void onResume(Activity activity) {
        MoPub.onResume(activity);
    }

    @Override
    public void onPause(Activity activity) {
        MoPub.onPause(activity);
    }


    @Override
    public void loadRewardVideoAd(final Activity activity, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, CustomRewardVideoListener customRewardVideoListener) {
        mLoadResultListener = customRewardVideoListener;

        if (activity == null) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onRewardedVideoAdFailed(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "activity is null."));
            }
            return;
        }
        if (mediationSetting != null && mediationSetting instanceof MopubRewardedVideoSetting) {
            mMopubMediationSetting = (MopubRewardedVideoSetting) mediationSetting;
        }

        if (serverExtras == null) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onRewardedVideoAdFailed(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "This placement's params in server is null!"));
            }
            return;
        } else {
            if (serverExtras.containsKey("unitid")) {
                adUnitId = (String) serverExtras.get("unitid");

            } else {
                if (mLoadResultListener != null) {
                    mLoadResultListener.onRewardedVideoAdFailed(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "unitid is empty!"));
                }
                return;
            }
        }

        MopubATInitManager.getInstance().initSDK(activity.getApplicationContext(), serverExtras, new MopubATInitManager.InitListener() {
            @Override
            public void initSuccess() {
                startLoad(activity);
            }
        });
    }

    @Override
    public boolean isAdReady() {
        return MoPubRewardedVideos.hasRewardedVideo(adUnitId);
    }

    @Override
    public void show(Activity activity) {
        if (isAdReady()) {
            MoPubRewardedVideos.showRewardedVideo(adUnitId, mUserId);
        }
    }

    @Override
    public String getSDKVersion() {
        return MopubATConst.getNetworkVersion();
    }

    @Override
    public String getNetworkName() {
        return MopubATInitManager.getInstance().getNetworkName();
    }
}