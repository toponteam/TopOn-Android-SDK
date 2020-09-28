package com.anythink.network.sigmob;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.anythink.splashad.unitgroup.api.CustomSplashAdapter;
import com.sigmob.windad.Splash.WindSplashAD;
import com.sigmob.windad.Splash.WindSplashADListener;
import com.sigmob.windad.Splash.WindSplashAdRequest;
import com.sigmob.windad.WindAdError;

import java.util.Map;

public class SigmobATSplashAdapter extends CustomSplashAdapter {

    private static final String TAG = SigmobATSplashAdapter.class.getSimpleName();
    private String mPlacementId = "";

    @Override
    public String getNetworkName() {
        return SigmobATInitManager.getInstance().getNetworkName();
    }

    @Override
    public void loadCustomNetworkAd(final Context context, Map<String, Object> serverExtra, Map<String, Object> localExtra) {
        String appId = "";
        String appKey = "";
        if (serverExtra.containsKey("app_id")) {
            appId = serverExtra.get("app_id").toString();
        }
        if (serverExtra.containsKey("app_key")) {
            appKey = serverExtra.get("app_key").toString();
        }
        if (serverExtra.containsKey("placement_id")) {
            mPlacementId = serverExtra.get("placement_id").toString();
        }

        if (TextUtils.isEmpty(appId) || TextUtils.isEmpty(appKey) || TextUtils.isEmpty(mPlacementId)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "app_id、app_key、placement_id could not be null.");
            }
            return;
        }

        SigmobATInitManager.getInstance().initSDK(context, serverExtra, new SigmobATInitManager.InitCallback() {
            @Override
            public void onFinish() {
                WindSplashAdRequest splashAdRequest = new WindSplashAdRequest(mPlacementId, "", null);
                splashAdRequest.setDisableAutoHideAd(true);

                //show ad
                new WindSplashAD((Activity) context, mContainer, splashAdRequest, new WindSplashADListener() {
                    @Override
                    public void onSplashAdSuccessPresentScreen() {
                        if (mLoadListener != null) {
                            mLoadListener.onAdCacheLoaded();
                        }
                        if (mImpressionListener != null) {
                            mImpressionListener.onSplashAdShow();
                        }

                    }

                    @Override
                    public void onSplashAdFailToPresent(WindAdError windAdError, String s) {
                        if (mLoadListener != null) {
                            mLoadListener.onAdLoadError("" + windAdError.getErrorCode(), windAdError.toString());
                        }
                    }

                    @Override
                    public void onSplashAdClicked() {
                        if (mImpressionListener != null) {
                            mImpressionListener.onSplashAdClicked();
                        }
                    }

                    @Override
                    public void onSplashClosed() {
                        if (mImpressionListener != null) {
                            mImpressionListener.onSplashAdDismiss();
                        }
                    }
                });
            }
        });
    }

    @Override
    public void destory() {

    }

    @Override
    public String getNetworkPlacementId() {
        return mPlacementId;
    }

    @Override
    public String getNetworkSDKVersion() {
        return SigmobATConst.getSDKVersion();
    }

}
