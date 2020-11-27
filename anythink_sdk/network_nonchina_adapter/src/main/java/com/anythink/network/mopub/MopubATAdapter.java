package com.anythink.network.mopub;

import android.content.Context;
import android.text.TextUtils;

import com.anythink.nativead.unitgroup.api.CustomNativeAd;
import com.anythink.nativead.unitgroup.api.CustomNativeAdapter;

import java.util.Map;

/**
 * Created by Z on 2018/3/13.
 */

public class MopubATAdapter extends CustomNativeAdapter {
    private final String TAG = MopubATAdapter.class.getSimpleName();

    private String unitId;
    private int requestNum = 1;
    private boolean isAutoPlay = false;

    @Override
    public void loadCustomNetworkAd(final Context context, final Map<String, Object> serverExtras, final Map<String, Object> localExtras) {

        unitId = "";
        try {
            if (serverExtras.containsKey("unitid")) {
                unitId = serverExtras.get("unitid").toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (TextUtils.isEmpty(unitId)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "mopub unitId is empty.");
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


        postOnMainThread(new Runnable() {
            @Override
            public void run() {
                try {
                    MopubATInitManager.getInstance().initSDK(context, serverExtras, new MopubATInitManager.InitListener() {

                        @Override
                        public void initSuccess() {
                            try {
                                startLoad(context, localExtras, unitId, requestNum, isAutoPlay);
                            } catch (Throwable e) {
                                if (mLoadListener != null) {
                                    mLoadListener.onAdLoadError("", e.getMessage());
                                }
                            }
                        }
                    });
                } catch (Throwable e) {
                    if (mLoadListener != null) {
                        mLoadListener.onAdLoadError("", e.getMessage());
                    }
                }
            }
        });


    }

    private void startLoad(Context context, Map<String, Object> localExtras, String unitId, int requestNum, boolean isAutoPlay) {
        final int finalRequestNum = requestNum;

        MopubATNativeAd.LoadCallbackListener selfListener = new MopubATNativeAd.LoadCallbackListener() {
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

        try {
            MopubATNativeAd mopubNativeAd = new MopubATNativeAd(context, selfListener, unitId, localExtras);
            mopubNativeAd.setIsAutoPlay(isAutoPlay);
            mopubNativeAd.loadAd();
        } catch (Exception e) {
            e.printStackTrace();
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", e.getMessage());
            }
        }
    }

    @Override
    public String getNetworkSDKVersion() {
        return MopubATConst.getNetworkVersion();
    }

    @Override
    public void destory() {

    }

    @Override
    public String getNetworkName() {
        return MopubATInitManager.getInstance().getNetworkName();
    }

    @Override
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        return MopubATInitManager.getInstance().setUserDataConsent(context, isConsent, isEUTraffic);
    }

    @Override
    public String getNetworkPlacementId() {
        return unitId;
    }
}
