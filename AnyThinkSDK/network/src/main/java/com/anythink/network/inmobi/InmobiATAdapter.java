package com.anythink.network.inmobi;

import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.AdError;
import com.anythink.core.api.ErrorCode;
import com.anythink.nativead.unitgroup.api.CustomNativeAd;
import com.anythink.nativead.unitgroup.api.CustomNativeAdapter;
import com.anythink.nativead.unitgroup.api.CustomNativeListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Z on 2018/1/12.
 */

public class InmobiATAdapter extends CustomNativeAdapter {

    private final String TAG = InmobiATAdapter.class.getSimpleName();
    int mCallbackCount;

    HashMap<String, InmobiATNativeAd> mNativeAdMap;
    List<CustomNativeAd> adList = new ArrayList<>();
    @Override
    public void loadNativeAd(final Context context, final CustomNativeListener customNativeListener, final Map<String, Object> serverExtras, final Map<String, Object> localExtras) {
        String accountId = "";
        String unitId = "";
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
            if (customNativeListener != null) {
                AdError adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", "inmobi accountId or unitid is empty");
                customNativeListener.onNativeAdFailed(this, adError);
            }
            return;
        }

        int requestNum = 1;
        try {
            if (serverExtras != null) {
                requestNum = Integer.parseInt(serverExtras.get(CustomNativeAd.AD_REQUEST_NUM).toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        boolean isAutoPlay = false;
        try {
            if (serverExtras != null) {
                isAutoPlay = Boolean.parseBoolean(serverExtras.get(CustomNativeAd.IS_AUTO_PLAY_KEY).toString());
            }
        } catch (Exception e) {

        }


        mNativeAdMap = new HashMap<>();

        final boolean finalIsAutoPlay = isAutoPlay;

        final int finalRequestNum = requestNum;

        final InmobiATNativeAd.LoadCallbackListener selfListener = new InmobiATNativeAd.LoadCallbackListener() {
            @Override
            public void onSuccess(CustomNativeAd nativeAd) {
                synchronized (InmobiATAdapter.this) {
                    mCallbackCount++;
                    adList.add(nativeAd);
                    finishLoad(null);
                }

            }

            @Override
            public void onFail(AdError error) {
                synchronized (InmobiATAdapter.this) {
                    mCallbackCount++;
                    finishLoad(error);
                }
            }

            private void finishLoad(AdError adError) {
                if (mCallbackCount >= finalRequestNum) {
                    if (adList.size() > 0) {
                        if (customNativeListener != null) {
                            customNativeListener.onNativeAdLoaded(InmobiATAdapter.this, adList);
                        }
                    } else {
                        if (mCallbackCount >= finalRequestNum) {
                            if (adError == null) {
                                adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", "");
                            }
                            customNativeListener.onNativeAdFailed(InmobiATAdapter.this, adError);
                        }
                    }
                }

            }
        };

        try {

            final String tempUnitId = unitId;
            InmobiATInitManager.getInstance().initSDK(context, serverExtras, new InmobiATInitManager.OnInitCallback() {
                @Override
                public void onFinish() {
                    for (int i = 0; i < finalRequestNum; i++) {
                        InmobiATNativeAd inmobiNativeAd = new InmobiATNativeAd(context, selfListener, tempUnitId, localExtras);
                        mNativeAdMap.put(tempUnitId, inmobiNativeAd);
                        inmobiNativeAd.setIsAutoPlay(finalIsAutoPlay);
                        inmobiNativeAd.loadAd();
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            if (customNativeListener != null) {
                AdError adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", e.getMessage());
                customNativeListener.onNativeAdFailed(this, adError);
            }
        }


    }


    @Override
    public String getSDKVersion() {
        return InmobiATConst.getNetworkVersion();
    }

    @Override
    public void clean() {

    }

    @Override
    public String getNetworkName() {
        return InmobiATInitManager.getInstance().getNetworkName();
    }
}
