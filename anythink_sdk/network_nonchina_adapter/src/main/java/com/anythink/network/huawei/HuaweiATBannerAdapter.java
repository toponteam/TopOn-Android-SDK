package com.anythink.network.huawei;

import android.content.Context;
import android.view.View;

import com.anythink.banner.unitgroup.api.CustomBannerAdapter;
import com.huawei.hms.ads.AdListener;
import com.huawei.hms.ads.AdParam;
import com.huawei.hms.ads.BannerAdSize;
import com.huawei.hms.ads.banner.BannerView;

import java.util.Map;

public class HuaweiATBannerAdapter extends CustomBannerAdapter {

    String mAdId;
    String mBannerSize;

    BannerView mBannerView;

    @Override
    public View getBannerView() {
        return mBannerView;
    }

    @Override
    public void loadCustomNetworkAd(Context context, Map<String, Object> serverExtras, Map<String, Object> localExtras) {
        if (serverExtras.containsKey("ad_id")) {
            mAdId = (String) serverExtras.get("ad_id");

        } else {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "AdId is empty.");
            }
            return;
        }

        if (serverExtras.containsKey("size")) {
            mBannerSize = (String) serverExtras.get("size");
        }

        final BannerView bannerView = new BannerView(context);
        bannerView.setAdId(mAdId);

        switch (mBannerSize) {
            case "320x50":
                bannerView.setBannerAdSize(BannerAdSize.BANNER_SIZE_320_50);
                break;
            case "320x100":
                bannerView.setBannerAdSize(BannerAdSize.BANNER_SIZE_320_100);
                break;
            case "300x250":
                bannerView.setBannerAdSize(BannerAdSize.BANNER_SIZE_300_250);
                break;
            case "360x57":
                bannerView.setBannerAdSize(BannerAdSize.BANNER_SIZE_360_57);
                break;
            case "360x144":
                bannerView.setBannerAdSize(BannerAdSize.BANNER_SIZE_360_144);
                break;
            default:
                bannerView.setBannerAdSize(BannerAdSize.BANNER_SIZE_SMART);
                break;
        }

        bannerView.setAdListener(new AdListener() {
            public void onAdClosed() {
                if (mImpressionEventListener != null) {
                    mImpressionEventListener.onBannerAdClose();
                }
            }

            public void onAdFailed(int errorCode) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError(String.valueOf(errorCode), "");
                }
            }

            public void onAdLeave() {
            }

            public void onAdOpened() {
            }

            public void onAdLoaded() {
                mBannerView = bannerView;
                if (mLoadListener != null) {
                    mLoadListener.onAdCacheLoaded();
                }
            }

            public void onAdClicked() {
                if (mImpressionEventListener != null) {
                    mImpressionEventListener.onBannerAdClicked();
                }
            }

            public void onAdImpression() {
                if (mImpressionEventListener != null) {
                    mImpressionEventListener.onBannerAdShow();
                }
            }
        });

        AdParam adParam = new AdParam.Builder().build();
        bannerView.loadAd(adParam);
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
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        return HuaweiATInitManager.getInstance().setUserDataConsent(context, isConsent, isEUTraffic);
    }

    @Override
    public String getNetworkPlacementId() {
        return mAdId;
    }

    @Override
    public String getNetworkSDKVersion() {
        return HuaweiATInitManager.getInstance().getNetworkSDKVersion();
    }

    @Override
    public String getNetworkName() {
        return HuaweiATInitManager.getInstance().getNetworkName();
    }
}
