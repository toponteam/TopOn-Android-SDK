package com.anythink.network.facebook;

import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.AdError;
import com.anythink.core.api.ErrorCode;
import com.anythink.nativead.unitgroup.api.CustomNativeAd;
import com.anythink.nativead.unitgroup.api.CustomNativeAdapter;
import com.anythink.nativead.unitgroup.api.CustomNativeListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Z on 2018/1/12.
 */

public class FacebookATAdapter extends CustomNativeAdapter {

    List<CustomNativeAd> adList = new ArrayList<>();

    String mPayload;
    String unitId = "";
    boolean isAutoPlay = false;

    @Override
    public void loadNativeAd(final Context context, final CustomNativeListener customNativeListener, final Map<String, Object> serverExtras, final Map<String, Object> localExtras) {

        try {
            if (serverExtras.containsKey("unit_id")) {
                unitId = serverExtras.get("unit_id").toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (TextUtils.isEmpty(unitId)) {
            if (customNativeListener != null) {
                AdError adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", "facebook unitId is empty.");
                customNativeListener.onNativeAdFailed(this, adError);
            }
            return;
        }


        try {
            if (serverExtras != null) {
                isAutoPlay = Boolean.parseBoolean(serverExtras.get(CustomNativeAd.IS_AUTO_PLAY_KEY).toString());
            }
        } catch (Exception e) {

        }

        FacebookATInitManager.getInstance().initSDK(context.getApplicationContext(), serverExtras);

        if (serverExtras.containsKey("payload")) {
            mPayload = serverExtras.get("payload").toString();
        }


        final FacebookATNativeAd.LoadCallbackListener selfListener = new FacebookATNativeAd.LoadCallbackListener() {
            @Override
            public void onSuccess(CustomNativeAd nativeAd) {
                synchronized (FacebookATAdapter.this) {
                    adList.add(nativeAd);
                    finishLoad(null);
                }

            }

            @Override
            public void onFail(AdError error) {
                synchronized (FacebookATAdapter.this) {
                    finishLoad(error);
                }
            }

            private void finishLoad(AdError adError) {
                if (adList.size() > 0) {
                    if (customNativeListener != null) {
                        customNativeListener.onNativeAdLoaded(FacebookATAdapter.this, adList);
                    }
                } else {
                    if (adError == null) {
                        adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", "");
                    }
                    if (customNativeListener != null) {
                        customNativeListener.onNativeAdFailed(FacebookATAdapter.this, adError);
                    }
                }
            }
        };

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    FacebookATNativeAd facebookNativeAd = new FacebookATNativeAd(context, selfListener, unitId, localExtras);
                    facebookNativeAd.setIsAutoPlay(isAutoPlay);
                    facebookNativeAd.loadAd(mPayload);
                } catch (Exception e) {
                    e.printStackTrace();
                    if (customNativeListener != null) {
                        AdError adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", e.getMessage());
                        customNativeListener.onNativeAdFailed(FacebookATAdapter.this, adError);
                    }
                }
            }
        }).start();
    }

    @Override
    public void clean() {

    }

    @Override
    public String getNetworkName() {
        return FacebookATInitManager.getInstance().getNetworkName();
    }

    @Override
    public String getSDKVersion() {
        return FacebookATConst.getNetworkVersion();
    }
}
