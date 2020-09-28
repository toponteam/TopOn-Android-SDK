package com.anythink.network.baidu;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;
import com.baidu.mobads.InterstitialAd;
import com.baidu.mobads.InterstitialAdListener;

import java.util.Map;

public class BaiduATInterstitialAdapter extends CustomInterstitialAdapter {

    private static final String TAG = BaiduATInterstitialAdapter.class.getSimpleName();

    InterstitialAd mInterstitialAd;
    private String mAdPlaceId = "";

    private void startLoadAd(Context context) {
        mInterstitialAd = new InterstitialAd(context, mAdPlaceId);
        mInterstitialAd.setListener(new InterstitialAdListener() {
            @Override
            public void onAdReady() {
                if (mLoadListener != null) {
                    mLoadListener.onAdCacheLoaded();

                }
            }

            @Override
            public void onAdPresent() {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdShow();

                }
            }

            @Override
            public void onAdClick(InterstitialAd interstitialAd) {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdClicked();

                }
            }

            @Override
            public void onAdDismissed() {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdClose();

                }
            }

            @Override
            public void onAdFailed(String s) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError("", s);
                }
            }
        });
        mInterstitialAd.loadAd();
    }

    @Override
    public boolean isAdReady() {
        if (mInterstitialAd != null) {
            return mInterstitialAd.isAdReady();
        }
        return false;
    }

    @Override
    public void show(Activity activity) {
        try {
            if (mInterstitialAd != null) {
                mInterstitialAd.showAd(activity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

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

        if (!(context instanceof Activity)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "Baidu context must be activity.");
            }
            return;
        }

        BaiduATInitManager.getInstance().initSDK(context, serverExtra, new BaiduATInitManager.InitCallback() {
            @Override
            public void onSuccess() {
                startLoadAd(context);
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
        if (mInterstitialAd != null) {
            mInterstitialAd.setListener(null);
            mInterstitialAd.destroy();
            mInterstitialAd = null;
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
