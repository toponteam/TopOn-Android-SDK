package com.anythink.network.inmobi;

import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.AdError;
import com.anythink.core.api.ErrorCode;
import com.anythink.nativead.unitgroup.api.CustomNativeAd;
import com.anythink.nativead.unitgroup.api.CustomNativeAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Z on 2018/1/12.
 */

public class InmobiATAdapter extends CustomNativeAdapter {

    private String unitId;

    @Override
    public void loadCustomNetworkAd(final Context context, final Map<String, Object> serverExtras, final Map<String, Object> localExtras) {
        String accountId = "";
        unitId = "";
        try {
            if (serverExtras.containsKey("app_id")) {
                accountId = serverExtras.get("app_id").toString();//inmob account id
            }
            if (serverExtras.containsKey("unit_id")) {
                unitId = serverExtras.get("unit_id").toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (TextUtils.isEmpty(accountId) || TextUtils.isEmpty(unitId)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "inmobi accountId or unitid is empty");
            }
            return;
        }

        boolean isAutoPlay = false;
        try {
            if (serverExtras != null) {
                isAutoPlay = Boolean.parseBoolean(serverExtras.get(CustomNativeAd.IS_AUTO_PLAY_KEY).toString());
            }
        } catch (Exception e) {

        }


        final boolean finalIsAutoPlay = isAutoPlay;


        final InmobiATNativeAd.LoadCallbackListener selfListener = new InmobiATNativeAd.LoadCallbackListener() {
            @Override
            public void onSuccess(CustomNativeAd nativeAd) {
                if (mLoadListener != null) {
                    mLoadListener.onAdCacheLoaded(nativeAd);
                }

            }

            @Override
            public void onFail(String errorCode, String errorMsg) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError(errorCode, errorMsg);
                }
            }

        };


        InmobiATInitManager.getInstance().initSDK(context, serverExtras, new InmobiATInitManager.OnInitCallback() {
            @Override
            public void onSuccess() {
                try {
                    InmobiATNativeAd inmobiNativeAd = new InmobiATNativeAd(context, selfListener, unitId, localExtras);
                    inmobiNativeAd.setIsAutoPlay(finalIsAutoPlay);
                    inmobiNativeAd.loadAd();
                } catch (Throwable e) {
                    e.printStackTrace();
                    if (mLoadListener != null) {
                        mLoadListener.onAdLoadError("", e.getMessage());
                    }
                }
            }

            @Override
            public void onError(String errorMsg) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError("", errorMsg);
                }
            }
        });


    }


    @Override
    public String getNetworkSDKVersion() {
        return InmobiATConst.getNetworkVersion();
    }

    @Override
    public void destory() {

    }

    @Override
    public String getNetworkName() {
        return InmobiATInitManager.getInstance().getNetworkName();
    }

    @Override
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        return InmobiATInitManager.getInstance().setUserDataConsent(context, isConsent, isEUTraffic);
    }

    @Override
    public String getNetworkPlacementId() {
        return unitId;
    }
}
