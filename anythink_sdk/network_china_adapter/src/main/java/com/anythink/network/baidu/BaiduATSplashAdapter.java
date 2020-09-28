package com.anythink.network.baidu;

import android.content.Context;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.anythink.splashad.unitgroup.api.CustomSplashAdapter;
import com.baidu.mobads.SplashAd;
import com.baidu.mobads.SplashAdListener;

import java.util.Map;

public class BaiduATSplashAdapter extends CustomSplashAdapter {
    private final String TAG = BaiduATSplashAdapter.class.getSimpleName();
    String mAdPlaceId = "";
    SplashAd mSplashAd;

    private void startLoadAd(final Context context, ViewGroup constainer) {
        // the observer of AD
        SplashAdListener listener = new SplashAdListener() {
            @Override
            public void onAdDismissed() {
                if (mImpressionListener != null) {
                    mImpressionListener.onSplashAdDismiss();
                }
            }

            @Override
            public void onAdFailed(String arg0) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError("", arg0);
                }
            }

            @Override
            public void onAdPresent() {
                if (mLoadListener != null) {
                    mLoadListener.onAdCacheLoaded();
                }
                if (mImpressionListener != null) {
                    mImpressionListener.onSplashAdShow();
                }

            }

            @Override
            public void onAdClick() {
                if (mImpressionListener != null) {
                    mImpressionListener.onSplashAdClicked();
                }
            }
        };

        mSplashAd = new SplashAd(context, constainer, listener, mAdPlaceId, true);
    }

    @Override
    public String getNetworkName() {
        return BaiduATInitManager.getInstance().getNetworkName();
    }

    @Override
    public void loadCustomNetworkAd(final Context context, Map<String, Object> serverExtra, Map<String, Object> localExtra) {
        String mAppId = (String) serverExtra.get("app_id");
        mAdPlaceId = (String) serverExtra.get("ad_place_id");
        if (TextUtils.isEmpty(mAppId) || TextUtils.isEmpty(mAdPlaceId)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", " app_id ,ad_place_id is empty.");
            }
            return;
        }

        BaiduATInitManager.getInstance().initSDK(context, serverExtra, new BaiduATInitManager.InitCallback() {
            @Override
            public void onSuccess() {
                startLoadAd(context, mContainer);
            }

            @Override
            public void onError(Throwable e) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError("", e.getMessage());
                }
            }
        });
    }

    @Override
    public void destory() {
        if (mSplashAd != null) {
            mSplashAd.destroy();
            mSplashAd = null;
        }
    }

    @Override
    public String getNetworkPlacementId() {
        return mAdPlaceId;
    }

    @Override
    public String getNetworkSDKVersion() {
        return BaiduATConst.getNetworkVersion();
    }
}
