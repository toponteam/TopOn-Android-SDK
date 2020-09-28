package com.anythink.rewardvideo.api;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.anythink.core.api.ATAdConst;
import com.anythink.core.api.ATAdInfo;
import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.ATSDK;
import com.anythink.core.api.AdError;
import com.anythink.core.api.ErrorCode;
import com.anythink.core.common.PlacementAdManager;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.utils.CommonSDKUtil;
import com.anythink.core.strategy.PlaceStrategy;
import com.anythink.core.strategy.PlaceStrategyManager;
import com.anythink.rewardvideo.bussiness.AdLoadManager;

import java.util.Map;

public class ATRewardVideoAd {
    final String TAG = getClass().getSimpleName();
    String mPlacementId;
    ATRewardVideoListener mListener;
    AdLoadManager mAdLoadManager;

    Context mContext;

    private ATRewardVideoListener mInterListener = new ATRewardVideoListener() {
        @Override
        public void onRewardedVideoAdLoaded() {
            SDKContext.getInstance().runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    if (mListener != null) {
                        mListener.onRewardedVideoAdLoaded();
                    }
                }
            });

        }

        @Override
        public void onRewardedVideoAdFailed(final AdError errorCode) {
            if (mAdLoadManager != null) {
                mAdLoadManager.setLoadFail(errorCode);
            }
            SDKContext.getInstance().runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    if (mListener != null) {
                        mListener.onRewardedVideoAdFailed(errorCode);
                    }
                }
            });

        }

        @Override
        public void onRewardedVideoAdPlayStart(final ATAdInfo entity) {
            SDKContext.getInstance().runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    if (mListener != null) {
                        mListener.onRewardedVideoAdPlayStart(entity);
                    }
                }
            });

        }

        @Override
        public void onRewardedVideoAdPlayEnd(final ATAdInfo entity) {
            SDKContext.getInstance().runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    if (mListener != null) {
                        mListener.onRewardedVideoAdPlayEnd(entity);
                    }
                }
            });

        }

        @Override
        public void onRewardedVideoAdPlayFailed(final AdError errorCode, final ATAdInfo entity) {
            SDKContext.getInstance().runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    if (mListener != null) {
                        mListener.onRewardedVideoAdPlayFailed(errorCode, entity);
                    }
                }
            });

        }

        @Override
        public void onRewardedVideoAdClosed(final ATAdInfo entity) {
            SDKContext.getInstance().runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    if (mListener != null) {
                        mListener.onRewardedVideoAdClosed(entity);
                    }
                }
            });

            if (isNeedAutoLoadAfterClose()) {
                load(true);
            }
        }

        @Override
        public void onRewardedVideoAdPlayClicked(final ATAdInfo entity) {
            SDKContext.getInstance().runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    if (mListener != null) {
                        mListener.onRewardedVideoAdPlayClicked(entity);
                    }
                }
            });

        }

        @Override
        public void onReward(final ATAdInfo adInfo) {
            SDKContext.getInstance().runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    if (mListener != null) {
                        mListener.onReward(adInfo);
                    }
                }
            });
        }
    };

    public ATRewardVideoAd(Context context, String placementId) {
        mPlacementId = placementId;
        mContext = context;
        mAdLoadManager = AdLoadManager.getInstance(context, placementId);
    }

    @Deprecated
    public void setCustomExtra(Map<String, String> map) {
    }

    public void load() {
        load(false);
    }

    private void load(final boolean isAutoRefresh) {
        ATSDK.apiLog(mPlacementId, Const.LOGKEY.API_REWARD, Const.LOGKEY.API_LOAD, Const.LOGKEY.START, "");
        mAdLoadManager.refreshContext(mContext);
        mAdLoadManager.startLoadAd(mContext, isAutoRefresh, mInterListener);
    }

    private boolean isNeedAutoLoadAfterClose() {
        PlaceStrategy placeStrategy = PlaceStrategyManager.getInstance(SDKContext.getInstance().getContext()).getPlaceStrategyByAppIdAndPlaceId(mPlacementId);
        if (placeStrategy != null) {
            return placeStrategy.getAutoRefresh() == 1 && !mAdLoadManager.isLoading();
        }
        return false;
    }

    @Deprecated
    public void addSetting(int networkType, ATMediationSetting setting) {
    }

    public void setLocalExtra(Map<String, Object> map) {
        PlacementAdManager.getInstance().putPlacementLocalSettingMap(mPlacementId, map);
    }

    public void setAdListener(ATRewardVideoListener listener) {
        mListener = listener;
    }

    public boolean isAdReady() {
        if (SDKContext.getInstance().getContext() == null
                || TextUtils.isEmpty(SDKContext.getInstance().getAppId())
                || TextUtils.isEmpty(SDKContext.getInstance().getAppKey())) {
            Log.e(TAG, "SDK init error!");
            return false;
        }

        boolean isAdReady = mAdLoadManager.isAdReady(mContext);
        ATSDK.apiLog(mPlacementId, Const.LOGKEY.API_REWARD, Const.LOGKEY.API_ISREADY, String.valueOf(isAdReady), "");
        return isAdReady;
    }

    @Deprecated
    public void setUserData(String userId, String customData) {
        PlacementAdManager.getInstance().addExtraInfoToLocalMap(mPlacementId, ATAdConst.KEY.USER_ID, userId);
        PlacementAdManager.getInstance().addExtraInfoToLocalMap(mPlacementId, ATAdConst.KEY.USER_CUSTOM_DATA, customData);
    }

    public void show(Activity activity, String scenario) {
        String realScenario = "";
        if (CommonSDKUtil.isVailScenario(scenario)) {
            realScenario = scenario;
        }
        controlShow(activity, realScenario);
    }

    public void show(Activity activity) {
        controlShow(activity, "");
    }

    @Deprecated
    public void show(String scenario) {
        String realScenario = "";
        if (CommonSDKUtil.isVailScenario(scenario)) {
            realScenario = scenario;
        }
        controlShow(null, realScenario);
    }

    @Deprecated
    public void show() {
        controlShow(null, "");
    }


    private void controlShow(Activity activity, String scenario) {

        ATSDK.apiLog(mPlacementId, Const.LOGKEY.API_REWARD, Const.LOGKEY.API_SHOW, Const.LOGKEY.START, "");
        if (SDKContext.getInstance().getContext() == null
                || TextUtils.isEmpty(SDKContext.getInstance().getAppId())
                || TextUtils.isEmpty(SDKContext.getInstance().getAppKey())) {
            AdError error = ErrorCode.getErrorCode(ErrorCode.exception, "", "sdk init error");
            if (mListener != null) {
                mListener.onRewardedVideoAdPlayFailed(error, ATAdInfo.fromAdapter(null));
            }
            Log.e(TAG, "SDK init error!");
            return;
        }

        Activity showActivity = activity;
        if (showActivity == null && mContext instanceof Activity) {
            showActivity = (Activity) mContext;
        }

        if (showActivity == null) {
            Log.e(TAG, "RewardedVideo Show Activity is null.");
        }

        mAdLoadManager.show(showActivity, scenario, mInterListener);
    }

    @Deprecated
    public void clean() {
//        mAdLoadManager.clean();
    }

    @Deprecated
    public void onPause() {
    }

    @Deprecated
    public void onResume() {
    }

    @Deprecated
    public void onDestory() {
    }

}
