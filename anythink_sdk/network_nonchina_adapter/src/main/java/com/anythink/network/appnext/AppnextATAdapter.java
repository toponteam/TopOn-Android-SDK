package com.anythink.network.appnext;

import android.content.Context;

import com.anythink.core.api.AdError;
import com.anythink.core.api.ErrorCode;
import com.anythink.nativead.unitgroup.api.CustomNativeAd;
import com.anythink.nativead.unitgroup.api.CustomNativeAdapter;
import com.anythink.nativead.unitgroup.api.CustomNativeListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AppnextATAdapter extends CustomNativeAdapter {
    private final String TAG = AppnextATAdapter.class.getSimpleName();
    CustomNativeListener mListener;
    String mPlacementId;

    int mCallbackCount;

    List<CustomNativeAd> adList = new ArrayList<>();
    @Override
    public void loadNativeAd(Context context, final CustomNativeListener customNativeListener, Map<String, Object> serverExtras, Map<String, Object> localExtras) {

        mListener = customNativeListener;

        mCallbackCount = 0;

        if (serverExtras == null) {
            log(TAG, "This placement's params in server is null!");
            if (mListener != null) {
                mListener.onNativeAdFailed(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "This placement's params in server is null!"));
            }
            return;
        }

        if (serverExtras.containsKey("placement_id")) {
            mPlacementId = (String) serverExtras.get("placement_id");

        } else {
            log(TAG, "placement_id is empty!");
            if (mListener != null) {
                mListener.onNativeAdFailed(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "placement_id is empty!"));
            }
            return;
        }

        AppnextATInitManager.getInstance().initSDK(context, serverExtras);

        int requestNum = 1;
        try {
            if (serverExtras != null) {
                requestNum = Integer.parseInt(serverExtras.get(CustomNativeAd.AD_REQUEST_NUM).toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        final int finalRequestNum = requestNum;

        AppnextATNativeAd.LoadCallbackListener selfListener = new AppnextATNativeAd.LoadCallbackListener() {
            @Override
            public void onSuccess(CustomNativeAd nativeAd) {
                synchronized (AppnextATAdapter.this) {
                    mCallbackCount++;
                    adList.add(nativeAd);
                    finishLoad(null);
                }

            }

            @Override
            public void onFail(AdError error) {
                synchronized (AppnextATAdapter.this) {
                    mCallbackCount++;
                    finishLoad(error);
                }
            }

            private void finishLoad(AdError adError) {
                if (mCallbackCount >= finalRequestNum) {
                    if (adList.size() > 0) {
                        if (customNativeListener != null) {
                            customNativeListener.onNativeAdLoaded(AppnextATAdapter.this, adList);
                        }
                    } else {
                        if (mCallbackCount >= finalRequestNum) {
                            if (adError == null) {
                                adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", "");
                            }
                            customNativeListener.onNativeAdFailed(AppnextATAdapter.this, adError);
                        }
                    }
                }

            }
        };


        try {
            for (int i = 0; i < requestNum; i++) {
                AppnextATNativeAd appnextNativeAd = new AppnextATNativeAd(context, mPlacementId, selfListener);
                appnextNativeAd.loadAd();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (customNativeListener != null) {
                AdError adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", e.getMessage());
                customNativeListener.onNativeAdFailed(AppnextATAdapter.this, adError);
            }
        }
    }


    @Override
    public String getSDKVersion() {
        return "";
    }

    @Override
    public void clean() {

    }

    @Override
    public String getNetworkName() {
        return AppnextATInitManager.getInstance().getNetworkName();
    }
}
