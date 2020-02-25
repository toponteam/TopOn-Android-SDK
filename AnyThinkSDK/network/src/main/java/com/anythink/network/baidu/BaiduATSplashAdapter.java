package com.anythink.network.baidu;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.ErrorCode;
import com.anythink.splashad.unitgroup.api.CustomSplashAdapter;
import com.anythink.splashad.unitgroup.api.CustomSplashListener;
import com.baidu.mobads.SplashAd;
import com.baidu.mobads.SplashAdListener;

import java.util.Map;

public class BaiduATSplashAdapter extends CustomSplashAdapter {
    private final String TAG = BaiduATSplashAdapter.class.getSimpleName();
    String mAdPlaceId = "";

    CustomSplashListener mListener;

    SplashAd mSplashAd;

    @Override
    public void loadSplashAd(Activity activity, ViewGroup constainer, View skipView, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, CustomSplashListener customSplashListener) {

        mListener = customSplashListener;

        if (serverExtras == null) {
            if (mListener != null) {
                mListener.onSplashAdFailed(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "This placement's params in server is null!"));
            }
            return;
        }

        String mAppId = (String) serverExtras.get("app_id");
        mAdPlaceId = (String) serverExtras.get("ad_place_id");
        if (TextUtils.isEmpty(mAppId) || TextUtils.isEmpty(mAdPlaceId)) {
            if (mListener != null) {
                mListener.onSplashAdFailed(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", " app_id ,ad_place_id is empty."));
            }
            return;
        }

        BaiduATInitManager.getInstance().initSDK(activity, serverExtras);
        // the observer of AD
        SplashAdListener listener = new SplashAdListener() {
            @Override
            public void onAdDismissed() {
            }

            @Override
            public void onAdFailed(String arg0) {
                if (mListener != null) {
                    mListener.onSplashAdFailed(BaiduATSplashAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, "", arg0));
                }
            }

            @Override
            public void onAdPresent() {
                if (mListener != null) {
                    mListener.onSplashAdLoaded(BaiduATSplashAdapter.this);
                    mListener.onSplashAdShow(BaiduATSplashAdapter.this);
                }
            }

            @Override
            public void onAdClick() {
                if (mListener != null) {
                    mListener.onSplashAdClicked(BaiduATSplashAdapter.this);
                }
            }
        };
        mSplashAd = new SplashAd(activity, constainer, listener, mAdPlaceId, true);
    }

    @Override
    public void clean() {
        if (mSplashAd != null) {
            mSplashAd.destroy();
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
