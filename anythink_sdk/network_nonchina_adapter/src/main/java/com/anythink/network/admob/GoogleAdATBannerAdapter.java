package com.anythink.network.admob;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.anythink.banner.unitgroup.api.CustomBannerAdapter;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;

import java.util.Map;

/**
 * Banner Adapter for Google Ad Manager
 */

public class GoogleAdATBannerAdapter extends CustomBannerAdapter {

    private String unitid = "";


    PublisherAdView mBannerView;


    @Override
    public void loadCustomNetworkAd(Context activity, Map<String, Object> serverExtras, Map<String, Object> localExtras) {

        if (serverExtras.containsKey("unit_id")) {
            unitid = (String) serverExtras.get("unit_id");

        } else {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "unitid is empty.");
            }
            return;
        }


//        Bundle persionalBundle = AdMobATInitManager.getInstance().getRequestBundle(activity.getApplicationContext());

        final PublisherAdView adView = new PublisherAdView(activity);
        AdSize adSize = null;
        if (localExtras.containsKey(AdmobATConst.INLINE_ADAPTIVE_ORIENTATION) && localExtras.containsKey(AdmobATConst.INLINE_ADAPTIVE_WIDTH)) {
            try {
                int orientation = Integer.parseInt(localExtras.get(AdmobATConst.INLINE_ADAPTIVE_ORIENTATION).toString());
                int width = Integer.parseInt(localExtras.get(AdmobATConst.INLINE_ADAPTIVE_WIDTH).toString());
                width = px2dip(activity, width);
                switch (orientation) {
                    case AdmobATConst.ORIENTATION_PORTRAIT:
                        adSize = AdSize.getPortraitInlineAdaptiveBannerAdSize(activity, width);
                        break;
                    case AdmobATConst.ORIENTATION_LANDSCAPE:
                        adSize = AdSize.getLandscapeInlineAdaptiveBannerAdSize(activity, width);
                        break;
                    default:
                        adSize = AdSize.getCurrentOrientationInlineAdaptiveBannerAdSize(activity, width);
                        break;
                }
            } catch (Throwable e) {
                adSize = null;
            }
        }

        if (adSize == null) {
            String size = "";
            if (serverExtras.containsKey("size")) {
                size = serverExtras.get("size").toString();
            }

            switch (size) {
                case "320x50":
                    adSize = AdSize.BANNER;
                    break;
                case "320x100":
                    adSize = AdSize.LARGE_BANNER;
                    break;
                case "300x250":
                    adSize = AdSize.MEDIUM_RECTANGLE;
                    break;
                case "468x60":
                    adSize = AdSize.FULL_BANNER;
                    break;
                case "728x90":
                    adSize = AdSize.LEADERBOARD;
                    break;
                default:
                    adSize = AdSize.SMART_BANNER;
                    break;
            }
        }

        adView.setAdSizes(adSize);
        adView.setAdUnitId(unitid);
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                mBannerView = adView;
                if (mLoadListener != null) {
                    mLoadListener.onAdCacheLoaded();
                }
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError(errorCode + "", "");

                }
            }

            @Override
            public void onAdOpened() {
                if (mImpressionEventListener != null) {
                    mImpressionEventListener.onBannerAdShow();
                }
            }

            @Override
            public void onAdLeftApplication() {
                if (mImpressionEventListener != null) {
                    mImpressionEventListener.onBannerAdClicked();
                }
            }

            @Override
            public void onAdClosed() {
            }
        });

        PublisherAdRequest adRequest = new PublisherAdRequest.Builder().build();
        adView.loadAd(adRequest);


    }

    @Override
    public View getBannerView() {
        return mBannerView;
    }


    @Override
    public void destory() {
        if (mBannerView != null) {
            mBannerView.setAdListener(null);
            mBannerView.destroy();
            mBannerView = null;
        }
    }

    @Override
    public String getNetworkSDKVersion() {
        return AdmobATConst.getNetworkVersion();
    }

    @Override
    public String getNetworkName() {
        return AdMobATInitManager.getInstance().getGoogleAdManagerName();
    }

    @Override
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        return AdMobATInitManager.getInstance().setUserDataConsent(context, isConsent, isEUTraffic);
    }

    @Override
    public String getNetworkPlacementId() {
        return unitid;
    }

    private static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / (scale <= 0 ? 1 : scale) + 0.5f);
    }

}