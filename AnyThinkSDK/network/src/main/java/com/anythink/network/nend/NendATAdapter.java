package com.anythink.network.nend;

import android.content.Context;

import com.anythink.core.api.AdError;
import com.anythink.core.api.ErrorCode;
import com.anythink.nativead.unitgroup.api.CustomNativeAd;
import com.anythink.nativead.unitgroup.api.CustomNativeAdapter;
import com.anythink.nativead.unitgroup.api.CustomNativeListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NendATAdapter extends CustomNativeAdapter {

    String mApiKey;
    int mSpotId;
    CustomNativeListener mListener;
    int mNativeType;
    int mCallbackCount;
    List<CustomNativeAd> adList = new ArrayList<>();

    @Override
    public void loadNativeAd(Context context, final CustomNativeListener customNativeListener, Map<String, Object> serverExtras, Map<String, Object> localExtras) {
        mListener = customNativeListener;
        if (serverExtras == null) {
            if (customNativeListener != null) {
                customNativeListener.onNativeAdFailed(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "This placement's params in server is null!"));
            }
            return;
        }

        if (serverExtras.containsKey("api_key") && serverExtras.containsKey("spot_id")) {
            mApiKey = (String) serverExtras.get("api_key");
            mSpotId = Integer.parseInt((String) serverExtras.get("spot_id"));

        } else {
            if (customNativeListener != null) {
                customNativeListener.onNativeAdFailed(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "app_id or slot_id is empty!"));
            }
            return;
        }

        if (serverExtras.containsKey("is_video")) {
            mNativeType = Integer.parseInt(serverExtras.get("is_video").toString());
        }

        int requestNum = 1;
        try {
            if (serverExtras != null) {
                requestNum = Integer.parseInt(serverExtras.get(CustomNativeAd.AD_REQUEST_NUM).toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        final int finalRequestNum = requestNum;
        NendATNativeAd.LoadCallbackListener selfListener = new NendATNativeAd.LoadCallbackListener() {
            @Override
            public void onSuccess(CustomNativeAd nativeAd) {
                synchronized (NendATAdapter.this) {
                    mCallbackCount++;
                    adList.add(nativeAd);
                    finishLoad(null);
                }

            }

            @Override
            public void onFail(AdError error) {
                synchronized (NendATAdapter.this) {
                    mCallbackCount++;
                    finishLoad(error);
                }
            }

            private void finishLoad(AdError adError) {
                if (mCallbackCount >= finalRequestNum) {
                    if (adList.size() > 0) {
                        if (customNativeListener != null) {
                            customNativeListener.onNativeAdLoaded(NendATAdapter.this, adList);
                        }
                    } else {
                        if (mCallbackCount >= finalRequestNum) {
                            if (adError == null) {
                                adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", "");
                            }
                            customNativeListener.onNativeAdFailed(NendATAdapter.this, adError);
                        }
                    }
                }
            }
        };

        try {
            for (int i = 0; i < requestNum; i++) {
                NendATNativeAd nendNativeAd = new NendATNativeAd(context, mApiKey, mSpotId, mNativeType, selfListener);
                nendNativeAd.loadAd();
            }
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
        return "";
    }

    @Override
    public void clean() {

    }

    @Override
    public String getNetworkName() {
        return NendATInitManager.getInstance().getNetworkName();
    }
}
