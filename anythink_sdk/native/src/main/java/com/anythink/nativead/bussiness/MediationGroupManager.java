package com.anythink.nativead.bussiness;

import android.content.Context;

import com.anythink.core.api.AdError;
import com.anythink.core.common.CommonMediationManager;
import com.anythink.core.common.base.AnyThinkBaseAdapter;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.entity.AdTrackingInfo;
import com.anythink.core.common.utils.CommonMD5;
import com.anythink.core.strategy.PlaceStrategy;
import com.anythink.nativead.api.ATNativeNetworkListener;
import com.anythink.nativead.bussiness.utils.CustomNativeAdapterParser;
import com.anythink.nativead.unitgroup.api.CustomNativeAd;
import com.anythink.nativead.unitgroup.api.CustomNativeAdapter;
import com.anythink.nativead.unitgroup.api.CustomNativeListener;

import java.util.List;
import java.util.Map;

/**
 * NativeAd Mediation Manager
 */
public class MediationGroupManager extends CommonMediationManager {
    ATNativeNetworkListener mCallbackListener;

    Map<String, Object> mLocalMap;


    private CustomNativeListener mCustomNativeListener = new CustomNativeListener() {
        @Override
        public void onNativeAdLoaded(CustomNativeAdapter adapter, List<CustomNativeAd> nativeAdList) {
            if (adapter != null && nativeAdList != null && nativeAdList.size()>0 && adapter.getTrackingInfo() != null) {

                AdTrackingInfo adTrackingInfo = adapter.getTrackingInfo();

                for (CustomNativeAd nativeAd : nativeAdList) {
                    nativeAd.setNetworkType(adTrackingInfo.getmNetworkType());
                }

            }
            onAdLoaded(adapter, nativeAdList);
        }

        @Override
        public void onNativeAdFailed(CustomNativeAdapter adapter, AdError error) {
            onAdError(adapter, error);
        }

    };


    protected MediationGroupManager(Context context) {
        super(context);
    }

    public void setLocalMap(Map<String, Object> localMap) {
        mLocalMap = localMap;
    }

    protected void loadNativeAd(String placementId, String requestid, PlaceStrategy placeStrategy, List<PlaceStrategy.UnitGroupInfo> list) {
        super.loadAd(placementId, requestid, placeStrategy, list);
    }

    @Override
    public void startLoadAd(AnyThinkBaseAdapter baseAdapter, PlaceStrategy.UnitGroupInfo unitGroupInfo, Map<String, Object> serviceExtras) {
        if (baseAdapter instanceof CustomNativeAdapter) {
            CustomNativeAdapterParser.loadNativeAd(mActivityRef.get(), (CustomNativeAdapter) baseAdapter, mCurrentStrategy, unitGroupInfo, serviceExtras, mLocalMap, mCustomNativeListener);
        }
    }


    public void setCallbackListener(ATNativeNetworkListener listener) {
        mCallbackListener = listener;
    }


    @Override
    public void onDevelopLoaded() {
        SDKContext.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (mCallbackListener != null) {
                    mCallbackListener.onNativeAdLoaded();
                }
            }
        });
    }

    @Override
    public void onDeveloLoadFail(final AdError adError) {
        SDKContext.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (mCallbackListener != null) {
                    mCallbackListener.onNativeAdLoadFail(adError);
                }
            }
        });
    }


}
