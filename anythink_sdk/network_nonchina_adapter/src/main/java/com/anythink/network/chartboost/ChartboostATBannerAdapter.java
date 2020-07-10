package com.anythink.network.chartboost;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.anythink.banner.api.ATBannerView;
import com.anythink.banner.unitgroup.api.CustomBannerAdapter;
import com.anythink.banner.unitgroup.api.CustomBannerListener;
import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.ErrorCode;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialListener;
import com.chartboost.sdk.Banner.BannerSize;
import com.chartboost.sdk.CBLocation;
import com.chartboost.sdk.Chartboost;
import com.chartboost.sdk.ChartboostBanner;
import com.chartboost.sdk.ChartboostBannerListener;
import com.chartboost.sdk.Events.ChartboostCacheError;
import com.chartboost.sdk.Events.ChartboostCacheEvent;
import com.chartboost.sdk.Events.ChartboostClickError;
import com.chartboost.sdk.Events.ChartboostClickEvent;
import com.chartboost.sdk.Events.ChartboostShowError;
import com.chartboost.sdk.Events.ChartboostShowEvent;
import com.chartboost.sdk.Libraries.CBLogging;
import com.chartboost.sdk.Model.CBError;

import java.util.Map;

/**
 * Created by zhou on 2018/6/27.
 */


public class ChartboostATBannerAdapter extends CustomBannerAdapter {
    private final String TAG = ChartboostATBannerAdapter.class.getSimpleName();

    String location = CBLocation.LOCATION_DEFAULT;
    String size = "";
    boolean isAutoRefresh = false;

    /***
     * init and load
     */
    private void initAndLoad(final Context activity, final ATBannerView bannerView, Map<String, Object> serverExtras, final CustomBannerListener customBannerListener) {
        ChartboostATInitManager.getInstance().initSDK(activity, serverExtras, new ChartboostATInitManager.InitCallback() {
            @Override
            public void didInitialize() {
                startload(activity, bannerView, customBannerListener);
            }
        });
    }


    ChartboostBanner mChartboosBanner;

    /***
     * load ad
     */
    public void startload(Context context, ATBannerView atBannerView, final CustomBannerListener customBannerListener) {
        ChartboostBannerListener chartboostBannerListener = new ChartboostBannerListener() {
            @Override
            public void onAdCached(ChartboostCacheEvent chartboostCacheEvent, ChartboostCacheError chartboostCacheError) {
                if (chartboostCacheError == null) {
                    if (customBannerListener != null) {
                        customBannerListener.onBannerAdLoaded(ChartboostATBannerAdapter.this);
                    }
                } else {
                    mChartboosBanner = null;
                    if (customBannerListener != null) {
                        customBannerListener.onBannerAdLoadFail(ChartboostATBannerAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, chartboostCacheError.code + "", chartboostCacheError.toString()));
                    }
                }
            }

            @Override
            public void onAdShown(ChartboostShowEvent chartboostShowEvent, ChartboostShowError chartboostShowError) {
                if (customBannerListener != null) {
                    customBannerListener.onBannerAdShow(ChartboostATBannerAdapter.this);
                }

                //Only for Chartboost old version（Banner bug）
                if (chartboostShowError != null) {
                    mChartboosBanner = null;
                    if (customBannerListener != null) {
                        customBannerListener.onBannerAdLoadFail(ChartboostATBannerAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, chartboostShowError.code + "", chartboostShowError.toString()));
                    }
                }
            }

            @Override
            public void onAdClicked(ChartboostClickEvent chartboostClickEvent, ChartboostClickError chartboostClickError) {
                if (customBannerListener != null) {
                    customBannerListener.onBannerAdClicked(ChartboostATBannerAdapter.this);
                }
            }
        };

        BannerSize bannerSize = BannerSize.STANDARD;
        if (!TextUtils.isEmpty(size)) {
            switch (size) {
                case "320x50":
                    bannerSize = BannerSize.STANDARD;
                    break;
                case "300x250":
                    bannerSize = BannerSize.MEDIUM;
                    break;
                case "728x90":
                    bannerSize = BannerSize.LEADERBOARD;
                    break;
            }
        }
        mChartboosBanner = new ChartboostBanner(context, location, bannerSize, null);
        mChartboosBanner.setListener(chartboostBannerListener);
        mChartboosBanner.setAutomaticallyRefreshesContent(isAutoRefresh);
        mChartboosBanner.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT));

        mChartboosBanner.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                mChartboosBanner.show();
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
            }
        });

        mChartboosBanner.cache();
    }

    @Override
    public void clean() {
    }

    @Override
    public String getNetworkName() {
        return ChartboostATInitManager.getInstance().getNetworkName();
    }


    @Override
    public void loadBannerAd(ATBannerView bannerView, Context activity, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, CustomBannerListener customBannerListener) {
        if (activity == null) {
            if (customBannerListener != null) {
                customBannerListener.onBannerAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "activity is null."));
            }
            return;
        }

//        serverExtras.put("app_id", "4f7b433509b6025804000002");
//        serverExtras.put("app_signature", "dd2d41b69ac01b80f443f5b6cf06096d457f82bd");
//        serverExtras.put("location", CBLocation.LOCATION_DEFAULT);
//        serverExtras.put("size", "320x50");
//        serverExtras.put("size", "300x250");
//        serverExtras.put("size", "728x90");

        if (serverExtras == null) {
            if (customBannerListener != null) {
                customBannerListener.onBannerAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", " serverExtras is empty."));
            }
            return;
        } else {

            String appid = (String) serverExtras.get("app_id");
            String appkey = (String) serverExtras.get("app_signature");

            if (serverExtras.containsKey("location")) {
                location = TextUtils.isEmpty(serverExtras.get("location").toString()) ? "start" : serverExtras.get("location").toString();
            }

            if (serverExtras.containsKey("size")) {
                size = serverExtras.get("size").toString();
            }

            try {
                if (serverExtras.containsKey("nw_rft")) {
                    isAutoRefresh = Integer.parseInt(serverExtras.get("nw_rft").toString()) > 0;
                }
            } catch (Exception e) {

            }


            if (TextUtils.isEmpty(appid) || TextUtils.isEmpty(appkey) || TextUtils.isEmpty(location)) {
                if (customBannerListener != null) {
                    customBannerListener.onBannerAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", " app_id ,app_signature or location is empty."));
                }
                return;
            }
        }

        if (!(activity instanceof Activity)) {
            customBannerListener.onBannerAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", " context must be activity."));
            return;
        }


        initAndLoad(activity, bannerView, serverExtras, customBannerListener);

    }


    @Override
    public String getSDKVersion() {
        return ChartboostATConst.getNetworkVersion();
    }


    @Override
    public View getBannerView() {
        return mChartboosBanner;
    }
}