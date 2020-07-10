package com.anythink.network.nend;

import android.content.Context;
import android.view.View;

import com.anythink.banner.api.ATBannerView;
import com.anythink.banner.unitgroup.api.CustomBannerAdapter;
import com.anythink.banner.unitgroup.api.CustomBannerListener;
import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.ErrorCode;

import net.nend.android.NendAdInformationListener;
import net.nend.android.NendAdView;

import java.util.Map;

public class NendATBannerAdapter extends CustomBannerAdapter {

    CustomBannerListener mListener;
    View mBannerView;
    String mApiKey;
    int mSpotId;

    @Override
    public void loadBannerAd(ATBannerView bannerView, Context activity, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, CustomBannerListener customBannerListener) {

        mListener = customBannerListener;

        if (serverExtras == null) {
            if (mListener != null) {
                mListener.onBannerAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "This placement's params in server is null!"));
            }
            return;
        }

        if (serverExtras.containsKey("api_key") && serverExtras.containsKey("spot_id")) {
            mApiKey = (String) serverExtras.get("api_key");
            mSpotId = Integer.parseInt((String) serverExtras.get("spot_id"));

        } else {
            if (customBannerListener != null) {
                customBannerListener.onBannerAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "app_id or slot_id is empty!"));
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
                if (mListener != null) {
                    mListener.onBannerAdLoaded(NendATBannerAdapter.this);
                }

            }

            @Override
            public void onFailedToReceiveAd(NendAdView nendAdView) {
                if (mListener != null) {
                    mListener.onBannerAdLoadFail(NendATBannerAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, "", ""));
                }
            }

            @Override
            public void onClick(NendAdView nendAdView) {
                if (mListener != null) {
                    mListener.onBannerAdClicked(NendATBannerAdapter.this);
                }
            }

            @Override
            public void onDismissScreen(NendAdView nendAdView) {
            }
        });
        nendAdView.loadAd();
    }

    @Override
    public String getSDKVersion() {
        return "";
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
    public String getNetworkName() {
        return NendATInitManager.getInstance().getNetworkName();
    }
}
