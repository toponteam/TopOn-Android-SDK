package com.anythink.network.applovin;

import android.content.Context;
import android.view.View;

import com.anythink.banner.api.ATBannerView;
import com.anythink.banner.unitgroup.api.CustomBannerAdapter;
import com.anythink.banner.unitgroup.api.CustomBannerListener;
import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.ErrorCode;
import com.applovin.adview.AppLovinAdView;
import com.applovin.sdk.AppLovinAd;
import com.applovin.sdk.AppLovinAdClickListener;
import com.applovin.sdk.AppLovinAdDisplayListener;
import com.applovin.sdk.AppLovinAdLoadListener;
import com.applovin.sdk.AppLovinAdSize;
import com.applovin.sdk.AppLovinSdk;

import java.util.Map;

/**
 * Created by Z on 2018/6/27.
 */

public class ApplovinATBannerAdapter extends CustomBannerAdapter {
    private static final String TAG = ApplovinATBannerAdapter.class.getSimpleName();


    String sdkkey = "", zoneid = "";

    String size = "";

    CustomBannerListener mListener;

    AppLovinAdView mBannerView;

    /***
     * init and load
     *
     */
    private void initAndLoad(Context activity, Map<String, Object> serverExtras) {

        AppLovinSdk applovinSdk = ApplovinATInitManager.getInstance().initSDK(activity, sdkkey, serverExtras);

        AppLovinAdView appLovinAdView = null;
        switch (size) {
            case "320x50":
                appLovinAdView = new AppLovinAdView(applovinSdk, AppLovinAdSize.BANNER, activity);
                break;
            case "300x250":
                appLovinAdView = new AppLovinAdView(applovinSdk, AppLovinAdSize.MREC, activity);
                break;
            default:
                appLovinAdView = new AppLovinAdView(applovinSdk, AppLovinAdSize.BANNER, activity);
                break;
        }

        final AppLovinAdView finalAdView = appLovinAdView;



        finalAdView.setAdDisplayListener(new AppLovinAdDisplayListener() {
            @Override
            public void adDisplayed(AppLovinAd appLovinAd) {
                if (mListener != null) {
                    mListener.onBannerAdShow(ApplovinATBannerAdapter.this);
                }

            }

            @Override
            public void adHidden(AppLovinAd appLovinAd) {
            }
        });

        finalAdView.setAdClickListener(new AppLovinAdClickListener() {
            @Override
            public void adClicked(AppLovinAd appLovinAd) {
                if (mListener != null) {
                    mListener.onBannerAdClicked(ApplovinATBannerAdapter.this);
                }
            }
        });


        AppLovinAdLoadListener adLoadListener = new AppLovinAdLoadListener() {
            public void adReceived(final AppLovinAd ad) {
                finalAdView.renderAd(ad);
                mBannerView = finalAdView;
                if (mListener != null) {
                    mListener.onBannerAdLoaded(ApplovinATBannerAdapter.this);
                }
            }

            @Override
            public void failedToReceiveAd(int i) {
                if (mListener != null) {
                    mListener.onBannerAdLoadFail(ApplovinATBannerAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, i + "", ""));
                }
            }
        };

        applovinSdk.getAdService().loadNextAdForZoneId(zoneid, adLoadListener);

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
    public void loadBannerAd(ATBannerView bannerView, Context activity, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, CustomBannerListener customBannerListener) {
        mListener = customBannerListener;
        if (activity == null) {
            if (mListener != null) {
                mListener.onBannerAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "activity is null."));
            }
            return;
        }

        if (serverExtras == null) {
            if (mListener != null) {
                mListener.onBannerAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "service info is empty."));
            }
            return;
        } else {
            if (serverExtras.containsKey("sdkkey") && serverExtras.containsKey("zone_id")) {
                sdkkey = (String) serverExtras.get("sdkkey");
                zoneid = (String) serverExtras.get("zone_id");
            } else {
                if (mListener != null) {
                    mListener.onBannerAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "sdkkey or zone_id is empty!"));
                }
                return;
            }
        }

        if (serverExtras.containsKey("size")) {
            size = serverExtras.get("size").toString();
        }

        initAndLoad(activity, serverExtras);

    }


    @Override
    public String getSDKVersion() {
        return ApplovinATConst.getNetworkVersion();
    }

    @Override
    public String getNetworkName() {
        return ApplovinATInitManager.getInstance().getNetworkName();
    }
}