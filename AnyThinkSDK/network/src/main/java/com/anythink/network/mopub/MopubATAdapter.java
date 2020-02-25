package com.anythink.network.mopub;

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
 * Created by Z on 2018/3/13.
 */

public class MopubATAdapter extends CustomNativeAdapter {
    private final String TAG = MopubATAdapter.class.getSimpleName();

    int mCallbackCount;
    private String unitId;
    private int requestNum = 1;
    private boolean isAutoPlay = false;

    List<CustomNativeAd> adList = new ArrayList<>();
    @Override
    public void loadNativeAd(final Context context, final CustomNativeListener customNativeListener
            , final Map<String, Object> serverExtras, final Map<String, Object> localExtras) {

        unitId = "";
        try {
            if (serverExtras.containsKey("unitid")) {
                unitId = serverExtras.get("unitid").toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (TextUtils.isEmpty(unitId)) {
            if (customNativeListener != null) {
                AdError adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", "mopub unitId is empty.");
                customNativeListener.onNativeAdFailed(this, adError);
            }
            return;
        }

        requestNum = 1;
        try {
            if (serverExtras != null) {
                requestNum = Integer.parseInt(serverExtras.get(CustomNativeAd.AD_REQUEST_NUM).toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        isAutoPlay = false;
        try {
            if (serverExtras != null) {
                isAutoPlay = Boolean.parseBoolean(serverExtras.get(CustomNativeAd.IS_AUTO_PLAY_KEY).toString());
            }
        } catch (Exception e) {

        }


        MopubATInitManager.getInstance().initSDK(context, serverExtras, new MopubATInitManager.InitListener() {

            @Override
            public void initSuccess() {
                startLoad(context, customNativeListener, localExtras, unitId, requestNum, isAutoPlay);
            }
        });


    }

    private void startLoad(Context context, final CustomNativeListener customNativeListener, Map<String, Object> localExtras, String unitId, int requestNum, boolean isAutoPlay) {
        final int finalRequestNum = requestNum;

        MopubATNativeAd.LoadCallbackListener selfListener = new MopubATNativeAd.LoadCallbackListener() {
            @Override
            public void onSuccess(CustomNativeAd nativeAd) {
                synchronized (MopubATAdapter.this) {
                    mCallbackCount++;
                    adList.add(nativeAd);
                    finishLoad(null);
                }

            }

            @Override
            public void onFail(AdError error) {
                synchronized (MopubATAdapter.this) {
                    mCallbackCount++;
                    finishLoad(error);
                }
            }

            private void finishLoad(AdError adError) {
                if (mCallbackCount >= finalRequestNum) {
                    if (adList.size() > 0) {
                        if (customNativeListener != null) {
                            customNativeListener.onNativeAdLoaded(MopubATAdapter.this, adList);
                        }
                    } else {
                        if (mCallbackCount >= finalRequestNum) {
                            if (adError == null) {
                                adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", "");
                            }
                            customNativeListener.onNativeAdFailed(MopubATAdapter.this, adError);
                        }
                    }
                }
            }
        };

        try {
            for (int i = 0; i < requestNum; i++) {
                MopubATNativeAd mopubNativeAd = new MopubATNativeAd(context, selfListener, unitId, localExtras);
                mopubNativeAd.setIsAutoPlay(isAutoPlay);
                mopubNativeAd.loadAd();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (customNativeListener != null) {
                AdError adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", e.getMessage());
                customNativeListener.onNativeAdFailed(MopubATAdapter.this, adError);
            }
        }
    }

    @Override
    public String getSDKVersion() {
        return MopubATConst.getNetworkVersion();
    }

    @Override
    public void clean() {

    }

    @Override
    public String getNetworkName() {
        return MopubATInitManager.getInstance().getNetworkName();
    }
}
