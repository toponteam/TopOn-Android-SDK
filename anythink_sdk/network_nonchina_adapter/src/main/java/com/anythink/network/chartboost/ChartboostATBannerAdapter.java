package com.anythink.network.chartboost;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;

import com.anythink.banner.unitgroup.api.CustomBannerAdapter;
import com.chartboost.sdk.Banner.BannerSize;
import com.chartboost.sdk.CBLocation;
import com.chartboost.sdk.ChartboostBanner;
import com.chartboost.sdk.ChartboostBannerListener;
import com.chartboost.sdk.Events.ChartboostCacheError;
import com.chartboost.sdk.Events.ChartboostCacheEvent;
import com.chartboost.sdk.Events.ChartboostClickError;
import com.chartboost.sdk.Events.ChartboostClickEvent;
import com.chartboost.sdk.Events.ChartboostShowError;
import com.chartboost.sdk.Events.ChartboostShowEvent;

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
    private void initAndLoad(final Context activity, Map<String, Object> serverExtras) {
        ChartboostATInitManager.getInstance().initSDK(activity, serverExtras, new ChartboostATInitManager.InitCallback() {
            @Override
            public void didInitialize() {
                try {
                    startload(activity);
                } catch (Throwable e) {
                    if (mLoadListener != null) {
                        mLoadListener.onAdLoadError("", e.getMessage());
                    }
                }
            }
        });
    }


    ChartboostBanner mChartboosBanner;

    /***
     * load ad
     */
    public void startload(Context context) {
        ChartboostBannerListener chartboostBannerListener = new ChartboostBannerListener() {
            @Override
            public void onAdCached(ChartboostCacheEvent chartboostCacheEvent, ChartboostCacheError chartboostCacheError) {
                if (chartboostCacheError == null) {
                    if (mLoadListener != null) {
                        mLoadListener.onAdCacheLoaded();
                    }
                } else {
                    mChartboosBanner = null;
                    if (mLoadListener != null) {
                        mLoadListener.onAdLoadError(chartboostCacheError.code + "", chartboostCacheError.toString());
                    }
                }
            }

            @Override
            public void onAdShown(ChartboostShowEvent chartboostShowEvent, ChartboostShowError chartboostShowError) {
                if (mImpressionEventListener != null) {
                    mImpressionEventListener.onBannerAdShow();
                }

                //Only for Chartboost old version（Banner bug）
                if (chartboostShowError != null) {
                    mChartboosBanner = null;
                    if (mLoadListener != null) {
                        mLoadListener.onAdLoadError(chartboostShowError.code + "", chartboostShowError.toString());
                    }
                }
            }

            @Override
            public void onAdClicked(ChartboostClickEvent chartboostClickEvent, ChartboostClickError chartboostClickError) {
                if (mImpressionEventListener != null) {
                    mImpressionEventListener.onBannerAdClicked();
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
    public void destory() {
        if (mChartboosBanner != null) {
            mChartboosBanner.setListener(null);
            mChartboosBanner = null;
        }
    }

    @Override
    public String getNetworkName() {
        return ChartboostATInitManager.getInstance().getNetworkName();
    }

    @Override
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        return ChartboostATInitManager.getInstance().setUserDataConsent(context, isConsent, isEUTraffic);
    }

    @Override
    public String getNetworkPlacementId() {
        return location;
    }


    @Override
    public void loadCustomNetworkAd(Context activity, Map<String, Object> serverExtras, Map<String, Object> localExtras) {

//        serverExtras.put("app_id", "4f7b433509b6025804000002");
//        serverExtras.put("app_signature", "dd2d41b69ac01b80f443f5b6cf06096d457f82bd");
//        serverExtras.put("location", CBLocation.LOCATION_DEFAULT);
//        serverExtras.put("size", "320x50");
//        serverExtras.put("size", "300x250");
//        serverExtras.put("size", "728x90");


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
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", " app_id ,app_signature or location is empty.");
            }
            return;
        }

        if (!(activity instanceof Activity)) {
            mLoadListener.onAdLoadError("", " context must be activity.");
            return;
        }


        initAndLoad(activity, serverExtras);

    }


    @Override
    public String getNetworkSDKVersion() {
        return ChartboostATConst.getNetworkVersion();
    }


    @Override
    public View getBannerView() {
        return mChartboosBanner;
    }
}