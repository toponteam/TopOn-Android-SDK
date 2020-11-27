package com.anythink.network.unityads;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import com.anythink.banner.unitgroup.api.CustomBannerAdapter;
import com.unity3d.services.banners.BannerErrorInfo;
import com.unity3d.services.banners.BannerView;
import com.unity3d.services.banners.UnityBannerSize;

import java.util.Map;

public class UnityAdsATBannerAdapter extends CustomBannerAdapter {

    String placement_id = "";

    BannerView mBannerView;

    @Override
    public View getBannerView() {
        return mBannerView;
    }

    @Override
    public void loadCustomNetworkAd(final Context context, final Map<String, Object> serverExtras, Map<String, Object> localExtra) {
        String game_id = (String) serverExtras.get("game_id");
        placement_id = (String) serverExtras.get("placement_id");

        if (TextUtils.isEmpty(game_id) || TextUtils.isEmpty(placement_id)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "unityads game_id, placement_id is empty!");
            }
            return;
        }

        if (!(context instanceof Activity)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "UnityAds context must be activity.");
            }
            return;
        }

        UnityAdsATInitManager.getInstance().initSDK(context, serverExtras, new UnityAdsATInitManager.InitListener() {
            @Override
            public void onSuccess() {
                startLoadAd((Activity) context, serverExtras);
            }

            @Override
            public void onError(String error, String msg) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError(error, msg);
                }
            }
        });

    }

    private void startLoadAd(Activity context, Map<String, Object> serverExtras) {
        String size;
        if (serverExtras.containsKey("size")) {
            size = (String) serverExtras.get("size");
        } else {
            size = "";
        }

        UnityBannerSize unityBannerSize;

        switch (size) {
            case "468x60":
                unityBannerSize = new UnityBannerSize(468, 60);
                break;
            case "728x90":
                unityBannerSize = new UnityBannerSize(728, 90);
                break;
            case "320x50":
            default:
                unityBannerSize = new UnityBannerSize(320, 50);
                break;
        }

        // Create the top banner view object:
        final BannerView bannerView = new BannerView(context, placement_id, unityBannerSize);
        // Set the listener for banner lifcycle events:
        bannerView.setListener(new BannerView.IListener() {
            @Override
            public void onBannerLoaded(BannerView bannerView) {
                mBannerView = bannerView;
                if (mLoadListener != null) {
                    mLoadListener.onAdCacheLoaded();
                }
            }

            @Override
            public void onBannerClick(BannerView bannerView) {
                if (mImpressionEventListener != null) {
                    mImpressionEventListener.onBannerAdClicked();
                }
            }

            @Override
            public void onBannerFailedToLoad(BannerView bannerView, BannerErrorInfo bannerErrorInfo) {
                if (mATBannerView != null) {
                    mATBannerView.removeView(mBannerView);
                }

                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError(bannerErrorInfo.errorCode.name(), bannerErrorInfo.errorMessage);
                }
            }

            @Override
            public void onBannerLeftApplication(BannerView bannerView) {

            }
        });

//                if (mATBannerView != null) {
//                    mATBannerView.addView(mBannerView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//                }

        bannerView.load();
    }

    @Override
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        return UnityAdsATInitManager.getInstance().setUserDataConsent(context, isConsent, isEUTraffic);
    }

    @Override
    public void destory() {
        if (mBannerView != null) {
            mBannerView.setListener(null);
            mBannerView.destroy();
            mBannerView = null;
        }
    }

    @Override
    public String getNetworkPlacementId() {
        return placement_id;
    }

    @Override
    public String getNetworkSDKVersion() {
        return UnityAdsATConst.getNetworkVersion();
    }

    @Override
    public String getNetworkName() {
        return UnityAdsATInitManager.getInstance().getNetworkName();
    }
}
