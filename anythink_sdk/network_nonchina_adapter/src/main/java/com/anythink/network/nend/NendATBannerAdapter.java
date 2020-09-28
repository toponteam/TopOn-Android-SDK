package com.anythink.network.nend;

import android.content.Context;
import android.view.View;

import com.anythink.banner.api.ATBannerView;
import com.anythink.banner.unitgroup.api.CustomBannerAdapter;
import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.ErrorCode;

import net.nend.android.NendAdInformationListener;
import net.nend.android.NendAdView;

import java.util.Map;

public class NendATBannerAdapter extends CustomBannerAdapter {

    NendAdView mBannerView;
    String mApiKey;
    int mSpotId;

    @Override
    public void loadCustomNetworkAd(Context activity, Map<String, Object> serverExtras, Map<String, Object> localExtras) {


        if (serverExtras.containsKey("api_key") && serverExtras.containsKey("spot_id")) {
            mApiKey = (String) serverExtras.get("api_key");
            mSpotId = Integer.parseInt((String) serverExtras.get("spot_id"));

        } else {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "app_id or slot_id is empty!");
            }
            return;
        }


        // 1 Instantiate NendAdView
        NendAdView nendAdView = new NendAdView(activity, mSpotId, mApiKey);
        // 3 Start loading ads
        nendAdView.setListener(new NendAdInformationListener() {
            @Override
            public void onInformationButtonClick(NendAdView nendAdView) {

            }

            @Override
            public void onReceiveAd(NendAdView nendAdView) {
                mBannerView = nendAdView;
                if (mLoadListener != null) {
                    mLoadListener.onAdCacheLoaded();
                }

            }

            @Override
            public void onFailedToReceiveAd(NendAdView nendAdView) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError("", "onFailedToReceiveAd");
                }
            }

            @Override
            public void onClick(NendAdView nendAdView) {
                if (mImpressionEventListener != null) {
                    mImpressionEventListener.onBannerAdClicked();
                }
            }

            @Override
            public void onDismissScreen(NendAdView nendAdView) {
            }
        });
        nendAdView.loadAd();
    }

    @Override
    public String getNetworkSDKVersion() {
        return "";
    }

    @Override
    public View getBannerView() {
        return mBannerView;
    }

    @Override
    public void destory() {
        if (mBannerView != null) {
            mBannerView.setListener(null);
            mBannerView = null;
        }
    }

    @Override
    public String getNetworkName() {
        return NendATInitManager.getInstance().getNetworkName();
    }

    @Override
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        return false;
    }

    @Override
    public String getNetworkPlacementId() {
        try {
            return String.valueOf(mSpotId);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
