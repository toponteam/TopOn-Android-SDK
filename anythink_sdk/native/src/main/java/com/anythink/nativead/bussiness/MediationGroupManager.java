package com.anythink.nativead.bussiness;

import android.content.Context;

import com.anythink.core.api.ATBaseAdAdapter;
import com.anythink.core.api.AdError;
import com.anythink.core.api.BaseAd;
import com.anythink.core.common.CommonMediationManager;
import com.anythink.core.common.entity.AdTrackingInfo;
import com.anythink.nativead.api.ATNativeNetworkListener;
import com.anythink.nativead.unitgroup.api.CustomNativeAd;

import java.util.List;

/**
 * NativeAd Mediation Manager
 */
public class MediationGroupManager extends CommonMediationManager {
    ATNativeNetworkListener mCallbackListener;

    @Override
    public synchronized void onAdLoaded(ATBaseAdAdapter baseAdapter, List<? extends BaseAd> adObjectList) {
        if (baseAdapter != null && adObjectList != null && adObjectList.size() > 0 && baseAdapter.getTrackingInfo() != null) {

            AdTrackingInfo adTrackingInfo = baseAdapter.getTrackingInfo();

            for (BaseAd nativeAd : adObjectList) {
                if (nativeAd instanceof CustomNativeAd) {
                    ((CustomNativeAd) nativeAd).setTrackingInfo(adTrackingInfo);
                }

            }

        }
        super.onAdLoaded(baseAdapter, adObjectList);
    }

    @Override
    public void prepareFormatAdapter(ATBaseAdAdapter baseAdapter) {
        //Nothing to do!
    }


    protected MediationGroupManager(Context context) {
        super(context);
    }


    public void setCallbackListener(ATNativeNetworkListener listener) {
        mCallbackListener = listener;
    }


    @Override
    public void onDevelopLoaded() {
        if (mCallbackListener != null) {
            mCallbackListener.onNativeAdLoaded();
        }
    }

    @Override
    public void onDeveloLoadFail(final AdError adError) {
        if (mCallbackListener != null) {
            mCallbackListener.onNativeAdLoadFail(adError);
        }
    }

    @Override
    public void removeFormatCallback() {
        mCallbackListener = null;
    }

}
