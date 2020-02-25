package com.anythink.interstitial.business;

import android.content.Context;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.AdError;
import com.anythink.core.common.CommonMediationManager;
import com.anythink.core.common.base.AnyThinkBaseAdapter;
import com.anythink.core.strategy.PlaceStrategy;
import com.anythink.interstitial.api.ATInterstitialListener;
import com.anythink.interstitial.business.utils.CustomInterstitialAdapterParser;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialListener;

import java.util.List;
import java.util.Map;

/**
 * 第三方广告平台管理类，每次加载广告都会重新创建
 */
public class MediationGroupManager extends CommonMediationManager {
    ATInterstitialListener mCallbackListener;

    private CustomInterstitialListener mCustomInterstitialListener = new CustomInterstitialListener() {
        @Override
        public void onInterstitialAdDataLoaded(CustomInterstitialAdapter adapter) {
            onAdDataLoaded(adapter);
        }

        @Override
        public void onInterstitialAdLoaded(CustomInterstitialAdapter adapter) {
            /**加载后清除监听，临时方案清理**/
            if (adapter != null) {
                adapter.clearLoadListener();
            }
            onAdLoaded(adapter, null);
        }

        @Override
        public void onInterstitialAdLoadFail(CustomInterstitialAdapter adapter, final AdError adError) {
            /**加载后清除监听，临时方案清理**/
            if (adapter != null) {
                adapter.clearLoadListener();
            }
            onAdError(adapter, adError);
        }


    };


    protected MediationGroupManager(Context context) {
        super(context);
    }


    protected void loadInterstitialAd(String placementId, String requestid, PlaceStrategy placeStrategy, List<PlaceStrategy.UnitGroupInfo> list) {
        super.loadAd(placementId, requestid, placeStrategy, list);
    }

    @Override
    public void startLoadAd(AnyThinkBaseAdapter baseAdapter, PlaceStrategy.UnitGroupInfo unitGroupInfo, Map<String, Object> serviceExtras) {
        if (baseAdapter instanceof CustomInterstitialAdapter) {
            ATMediationSetting setting = null;
            if (mSettingMap != null) {
                setting = mSettingMap.get(unitGroupInfo.networkType);
            }
            CustomInterstitialAdapterParser.loadInterstitialAd(mActivityRef.get(), (CustomInterstitialAdapter) baseAdapter, unitGroupInfo, serviceExtras, setting, mCustomInterstitialListener);
        }
    }


    public void setCallbackListener(ATInterstitialListener listener) {
        mCallbackListener = listener;
    }


    @Override
    public void onDevelopLoaded() {
        if (mIsRefresh) {
            return;
        }
        if (mCallbackListener != null) {
            mCallbackListener.onInterstitialAdLoaded();
        }
        mCallbackListener = null;
    }

    @Override
    public void onDeveloLoadFail(AdError adError) {
        if (mIsRefresh) {
            return;
        }
        if (mCallbackListener != null) {
            mCallbackListener.onInterstitialAdLoadFail(adError);
        }
        mCallbackListener = null;
    }


}
