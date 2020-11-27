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

import java.util.Map;

/**
 * Banner adapter admob
 * Created by Z on 2018/6/27.
 */

public class AdmobATBannerAdapter extends CustomBannerAdapter {
    private static final String TAG = AdmobATBannerAdapter.class.getSimpleName();

    AdRequest mAdRequest = null;
    private String unitid = "";


    AdView mBannerView;


    @Override
    public void loadCustomNetworkAd(Context activity, Map<String, Object> serverExtras, Map<String, Object> localExtras) {

        String appid = "";
        if (serverExtras.containsKey("app_id") && serverExtras.containsKey("unit_id")) {
            appid = (String) serverExtras.get("app_id");
            unitid = (String) serverExtras.get("unit_id");

        } else {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "appid or unitId is empty.");
            }
            return;
        }

        AdMobATInitManager.getInstance().initSDK(activity.getApplicationContext(), serverExtras);

        Bundle persionalBundle = AdMobATInitManager.getInstance().getRequestBundle(activity.getApplicationContext());

        final AdView adView = new AdView(activity);
        AdSize adSize = null;

        if (localExtras.containsKey(AdmobATConst.ADAPTIVE_TYPE) && localExtras.containsKey(AdmobATConst.ADAPTIVE_ORIENTATION) && localExtras.containsKey(AdmobATConst.ADAPTIVE_WIDTH)) {
            try {
                int adaptiveType = Integer.parseInt(localExtras.get(AdmobATConst.ADAPTIVE_TYPE).toString());
                int orientation = Integer.parseInt(localExtras.get(AdmobATConst.ADAPTIVE_ORIENTATION).toString());
                int width = Integer.parseInt(localExtras.get(AdmobATConst.ADAPTIVE_WIDTH).toString());
                width = px2dip(activity, width);
                switch (orientation) {
                    case AdmobATConst.ORIENTATION_PORTRAIT:
                        if (adaptiveType == AdmobATConst.ADAPTIVE_INLINE) {
                            adSize = AdSize.getPortraitInlineAdaptiveBannerAdSize(activity, width);
                        } else {
                            adSize = AdSize.getPortraitAnchoredAdaptiveBannerAdSize(activity, width);
                        }

                        break;
                    case AdmobATConst.ORIENTATION_LANDSCAPE:
                        if (adaptiveType == AdmobATConst.ADAPTIVE_INLINE) {
                            adSize = AdSize.getLandscapeInlineAdaptiveBannerAdSize(activity, width);
                        } else {
                            adSize = AdSize.getLandscapeAnchoredAdaptiveBannerAdSize(activity, width);
                        }

                        break;
                    default:
                        if (adaptiveType == AdmobATConst.ADAPTIVE_INLINE) {
                            adSize = AdSize.getCurrentOrientationInlineAdaptiveBannerAdSize(activity, width);
                        } else {
                            adSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, width);
                        }

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

        adView.setAdSize(adSize);
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
        return AdMobATInitManager.getInstance().getNetworkName();
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