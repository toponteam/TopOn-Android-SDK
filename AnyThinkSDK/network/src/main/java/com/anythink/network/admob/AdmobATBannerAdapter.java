package com.anythink.network.admob;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.anythink.banner.api.ATBannerView;
import com.anythink.banner.unitgroup.api.CustomBannerAdapter;
import com.anythink.banner.unitgroup.api.CustomBannerListener;
import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.ErrorCode;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import java.util.Map;

/**
 * Banner adapter admob
 * Created by Z on 2018/6/27.
 */

public class AdmobATBannerAdapter extends CustomBannerAdapter {
    private static final String TAG = AdmobATBannerAdapter.class.getSimpleName();

    AdRequest mAdRequest = null;
    private String unitid = "";


    CustomBannerListener mListener;

    View mBannerView;


    @Override
    public void loadBannerAd(ATBannerView bannerView, Context activity, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, CustomBannerListener customBannerListener) {
        mListener = customBannerListener;

        if (activity == null) {
            log(TAG, "activity is null!");
            if (mListener != null) {
                mListener.onBannerAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "activity is null."));
            }
            return;
        }


        if (serverExtras == null) {
            log(TAG, "This placement's params in server is null!");
            if (mListener != null) {
                mListener.onBannerAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", " serverExtras is empty."));
            }
            return;
        } else {
            String appid = "";
            if (serverExtras.containsKey("app_id") && serverExtras.containsKey("unit_id")) {
                appid = (String) serverExtras.get("app_id");
                unitid = (String) serverExtras.get("unit_id");

            } else {
                log(TAG, "app_id or unit_id is empty!");
                if (mListener != null) {
                    mListener.onBannerAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", " appid ,unitid or sdkkey is empty."));

                }
                return;
            }
        }
        
        AdMobATInitManager.getInstance().initSDK(activity.getApplicationContext(), serverExtras);

        Bundle persionalBundle = AdMobATInitManager.getInstance().getRequestBundle(activity.getApplicationContext());

        String size = "";
        if (serverExtras.containsKey("size")) {
            size = serverExtras.get("size").toString();
        }

        final AdView adView = new AdView(activity);
        switch (size) {
            case "320x50":
                adView.setAdSize(AdSize.BANNER);
                break;
            case "320x100":
                adView.setAdSize(AdSize.LARGE_BANNER);
                break;
            case "300x250":
                adView.setAdSize(AdSize.MEDIUM_RECTANGLE);
                break;
            case "468x60":
                adView.setAdSize(AdSize.FULL_BANNER);
                break;
            case "728x90":
                adView.setAdSize(AdSize.LEADERBOARD);
                break;
            default:
                adView.setAdSize(AdSize.SMART_BANNER);
                break;
        }


        adView.setAdUnitId(unitid);
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                log(TAG, "onAdLoaded");
                mBannerView = adView;
                if (mListener != null) {
                    mListener.onBannerAdLoaded(AdmobATBannerAdapter.this);
                }
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                log(TAG, "onAdFailedToLoad");
                if (mListener != null) {
                    mListener.onBannerAdLoadFail(AdmobATBannerAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, errorCode + "", ""));

                }
            }

            @Override
            public void onAdOpened() {
                log(TAG, "onAdOpened");
                if (mListener != null) {
                    mListener.onBannerAdShow(AdmobATBannerAdapter.this);
                }
            }

            @Override
            public void onAdLeftApplication() {
                log(TAG, "onAdLeftApplication");
                if (mListener != null) {
                    mListener.onBannerAdClicked(AdmobATBannerAdapter.this);
                }
            }

            @Override
            public void onAdClosed() {
                log(TAG, "onAdClosed");
            }
        });
        mAdRequest = new AdRequest.Builder()
                .addNetworkExtrasBundle(AdMobAdapter.class, persionalBundle)
                //                .addKeyword("")
                //                .addNetworkExtras("")
                .build();
        adView.loadAd(mAdRequest);


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
        return "";
    }

    @Override
    public String getNetworkName() {
        return AdMobATInitManager.getInstance().getNetworkName();
    }

}