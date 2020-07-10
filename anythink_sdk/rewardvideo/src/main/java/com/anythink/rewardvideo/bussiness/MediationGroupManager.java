package com.anythink.rewardvideo.bussiness;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.AdError;
import com.anythink.core.common.CommonMediationManager;
import com.anythink.core.common.base.AnyThinkBaseAdapter;
import com.anythink.core.strategy.PlaceStrategy;
import com.anythink.rewardvideo.api.ATRewardVideoListener;
import com.anythink.rewardvideo.bussiness.utils.CustomRewardVideoAdapterParser;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoListener;

import java.util.List;
import java.util.Map;

/**
 * RewardedVideo Mediation Manager
 */
public class MediationGroupManager extends CommonMediationManager {
    ATRewardVideoListener mCallbackListener;

    private CustomRewardVideoListener mCustomRewardVideoListener = new CustomRewardVideoListener() {
        @Override
        public void onRewardedVideoAdDataLoaded(CustomRewardVideoAdapter customRewardVideoAd) {
            onAdDataLoaded(customRewardVideoAd);
        }

        @Override
        public void onRewardedVideoAdLoaded(CustomRewardVideoAdapter customRewardVideoAd) {
            if (customRewardVideoAd != null) {
                customRewardVideoAd.clearLoadListener();
            }
            onAdLoaded(customRewardVideoAd, null);
        }

        @Override
        public void onRewardedVideoAdFailed(CustomRewardVideoAdapter customRewardVideoAd, final AdError adError) {
            if (customRewardVideoAd != null) {
                customRewardVideoAd.clearLoadListener();
            }
            onAdError(customRewardVideoAd, adError);
        }


    };

    protected MediationGroupManager(Context context) {
        super(context);
    }


    protected void loadRewardVideoAd(String placementId, String requestid, PlaceStrategy placeStrategy, List<PlaceStrategy.UnitGroupInfo> list) {
        super.loadAd(placementId, requestid, placeStrategy, list);
    }

    @Override
    public void startLoadAd(AnyThinkBaseAdapter baseAdapter, PlaceStrategy.UnitGroupInfo unitGroupInfo, Map<String, Object> serviceExtras) {
        if (baseAdapter instanceof CustomRewardVideoAdapter && mActivityRef.get() instanceof Activity) {
            ATMediationSetting setting = null;
            if (mSettingMap != null) {
                setting = mSettingMap.get(unitGroupInfo.networkType);
            }
            ((CustomRewardVideoAdapter) baseAdapter).setUserId(mUserId);
            ((CustomRewardVideoAdapter) baseAdapter).setUserData(mCustomData);
            CustomRewardVideoAdapterParser.loadRewardVideoAd((Activity) mActivityRef.get(), (CustomRewardVideoAdapter) baseAdapter, unitGroupInfo, serviceExtras, setting, mCustomRewardVideoListener);
        }
    }


    public void setCallbackListener(ATRewardVideoListener listener) {
        mCallbackListener = listener;
    }


    public void setUserData(String userId, String customData) {
        if (TextUtils.isEmpty(userId)) {
            mUserId = "";
        } else {
            mUserId = userId;
        }

        if (TextUtils.isEmpty(customData)) {
            mCustomData = "";
        } else {
            mCustomData = customData;
        }
    }

    @Override
    public void onDevelopLoaded() {
        if(mIsRefresh) {
            return;
        }
        if (mCallbackListener != null) {
            mCallbackListener.onRewardedVideoAdLoaded();
        }

        mCallbackListener = null;
    }

    @Override
    public void onDeveloLoadFail(AdError adError) {
        if (mIsRefresh) {
            return;
        }
        if (mCallbackListener != null) {
            mCallbackListener.onRewardedVideoAdFailed(adError);
        }

        mCallbackListener = null;
    }


}
