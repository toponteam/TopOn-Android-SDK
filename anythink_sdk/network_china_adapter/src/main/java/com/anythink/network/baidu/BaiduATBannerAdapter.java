package com.anythink.network.baidu;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import com.anythink.banner.api.ATBannerView;
import com.anythink.banner.unitgroup.api.CustomBannerAdapter;
import com.anythink.banner.unitgroup.api.CustomBannerListener;
import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.AdError;
import com.anythink.core.api.ErrorCode;
import com.baidu.mobads.AdView;
import com.baidu.mobads.AdViewListener;

import org.json.JSONObject;

import java.util.Map;

public class BaiduATBannerAdapter extends CustomBannerAdapter {

    String mAdPlaceId;

    CustomBannerListener mCustomBannerListener;

    AdView mBannerView;

    @Override
    public void loadBannerAd(final ATBannerView bannerView, final Context activity, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, CustomBannerListener customBannerListener) {
        mCustomBannerListener = customBannerListener;
        String mAppId = "";
        if (serverExtras.containsKey("app_id")) {
            mAppId = serverExtras.get("app_id").toString();
        }
        if (serverExtras.containsKey("ad_place_id")) {
            mAdPlaceId = serverExtras.get("ad_place_id").toString();
        }

        if (TextUtils.isEmpty(mAppId) || TextUtils.isEmpty(mAdPlaceId)) {
            if (mCustomBannerListener != null) {
                AdError adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", "app_id or ad_place_id is empty.");
                mCustomBannerListener.onBannerAdLoadFail(this, adError);
            }
            return;
        }
        BaiduATInitManager.getInstance().initSDK(activity, serverExtras, new BaiduATInitManager.InitCallback() {
            @Override
            public void onSuccess() {
                startLoadAd(bannerView, activity);
            }

            @Override
            public void onError(Throwable e) {
                if (mCustomBannerListener != null) {
                    AdError adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", e.getMessage());
                    mCustomBannerListener.onBannerAdLoadFail(BaiduATBannerAdapter.this, adError);
                }
            }
        });
    }

    private void startLoadAd(final ATBannerView bannerView, Context activity) {
        mBannerView = new AdView(activity, mAdPlaceId);
        mBannerView.setListener(new AdViewListener() {
            @Override
            public void onAdReady(AdView adView) {

            }

            @Override
            public void onAdShow(JSONObject jsonObject) {
                if (mCustomBannerListener != null) {
                    mCustomBannerListener.onBannerAdLoaded(BaiduATBannerAdapter.this);
                    mCustomBannerListener.onBannerAdShow(BaiduATBannerAdapter.this);
                }
            }

            @Override
            public void onAdClick(JSONObject jsonObject) {
                if (mCustomBannerListener != null) {
                    mCustomBannerListener.onBannerAdClicked(BaiduATBannerAdapter.this);
                }
            }

            @Override
            public void onAdFailed(String s) {
                if (mCustomBannerListener != null) {
                    mCustomBannerListener.onBannerAdLoadFail(BaiduATBannerAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, "", s));
                }
                if (bannerView != null) {
                    bannerView.removeView(mBannerView);
                }
            }

            @Override
            public void onAdSwitch() {

            }

            @Override
            public void onAdClose(JSONObject jsonObject) {
                if (mCustomBannerListener != null) {
                    mCustomBannerListener.onBannerAdClose(BaiduATBannerAdapter.this);
                }
            }
        });

        if (bannerView != null) {
            bannerView.addView(mBannerView);
        }
    }

    @Override
    public View getBannerView() {
        return mBannerView;
    }

    @Override
    public void clean() {
        if (mBannerView != null) {
            mBannerView.destroy();
        }
    }

    @Override
    public String getSDKVersion() {
        return BaiduATConst.getNetworkVersion();
    }

    @Override
    public String getNetworkName() {
        return BaiduATInitManager.getInstance().getNetworkName();
    }
}
