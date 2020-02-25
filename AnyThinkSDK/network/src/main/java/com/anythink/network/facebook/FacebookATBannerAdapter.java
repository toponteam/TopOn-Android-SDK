package com.anythink.network.facebook;

import android.content.Context;
import android.view.View;

import com.anythink.banner.api.ATBannerView;
import com.anythink.banner.unitgroup.api.CustomBannerAdapter;
import com.anythink.banner.unitgroup.api.CustomBannerListener;
import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.ErrorCode;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;

import java.util.Map;

/**
 * Created by Z on 2018/6/27.
 */

public class FacebookATBannerAdapter extends CustomBannerAdapter {

    private String unitid = "";

    CustomBannerListener mListener;

    View mBannerView;

    String size = "";

    @Override
    public void loadBannerAd(ATBannerView bannerView, final Context activity, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, CustomBannerListener customBannerListener) {
        mListener = customBannerListener;

        if (activity == null) {
            if (mListener != null) {
                mListener.onBannerAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "activity is null."));
            }
            return;
        }

        if (serverExtras == null) {
            if (mListener != null) {
                mListener.onBannerAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "facebook serverExtras is empty."));
            }
            return;
        } else {

            if (serverExtras.containsKey("unit_id")) {
                unitid = (String) serverExtras.get("unit_id");

            } else {
                if (mListener != null) {
                    mListener.onBannerAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "facebook unitid is empty."));

                }
                return;
            }
        }

        FacebookATInitManager.getInstance().initSDK(activity.getApplicationContext(), serverExtras);

        if (serverExtras.containsKey("size")) {
            size = serverExtras.get("size").toString();
        }


        final AdListener adListener = new AdListener() {
            @Override
            public void onAdLoaded(Ad ad) {
                mBannerView = (AdView) ad;
                if (mListener != null) {
                    mListener.onBannerAdLoaded(FacebookATBannerAdapter.this);

                }
            }

            @Override
            public void onError(Ad ad, AdError adError) {
                if (mListener != null) {
                    mListener.onBannerAdLoadFail(FacebookATBannerAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, adError.getErrorCode() + "", adError.getErrorMessage()));

                }
            }

            @Override
            public void onAdClicked(Ad ad) {
                if (mListener != null) {
                    mListener.onBannerAdClicked(FacebookATBannerAdapter.this);
                }
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                if (mListener != null) {
                    mListener.onBannerAdShow(FacebookATBannerAdapter.this);
                }
            }
        };

        new Thread(new Runnable() {
            @Override
            public void run() {
                AdView adView = null;
                switch (size) {
                    case "320x50":
                        adView = new AdView(activity, unitid, AdSize.BANNER_HEIGHT_50);
                        break;
                    case "320x90":
                        adView = new AdView(activity, unitid, AdSize.BANNER_HEIGHT_90);
                        break;
                    case "320x250":
                        adView = new AdView(activity, unitid, AdSize.RECTANGLE_HEIGHT_250);
                        break;
                    default:
                        adView = new AdView(activity, unitid, AdSize.BANNER_HEIGHT_50);
                        break;
                }
                adView.loadAd(adView.buildLoadAdConfig().withAdListener(adListener).build());
            }
        }).start();
    }

    @Override
    public View getBannerView() {
        return mBannerView;
    }

    @Override
    public void clean() {
        mBannerView = null;
    }

    @Override
    public String getSDKVersion() {
        return FacebookATConst.getNetworkVersion();
    }

    @Override
    public String getNetworkName() {
        return FacebookATInitManager.getInstance().getNetworkName();
    }

}