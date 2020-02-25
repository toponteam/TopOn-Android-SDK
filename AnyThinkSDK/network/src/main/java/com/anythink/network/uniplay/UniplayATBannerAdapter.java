package com.anythink.network.uniplay;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.anythink.banner.api.ATBannerView;
import com.anythink.banner.unitgroup.api.CustomBannerAdapter;
import com.anythink.banner.unitgroup.api.CustomBannerListener;
import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.ErrorCode;
import com.uniplay.adsdk.AdBannerListener;
import com.uniplay.adsdk.AdSize;
import com.uniplay.adsdk.AdView;

import java.util.Map;

public class UniplayATBannerAdapter extends CustomBannerAdapter {

    private final String TAG = UniplayATBannerAdapter.class.getSimpleName();
    String mAppId;
    CustomBannerListener mListener;
    View mBannerView;

    @Override
    public void loadBannerAd(final ATBannerView anyThinkBannerView, Context activity, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, CustomBannerListener customBannerListener) {

        mListener = customBannerListener;

        if (serverExtras == null) {
            if (mListener != null) {
                mListener.onBannerAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "This placement's params in server is null!"));
            }
            return;
        }

        if (serverExtras.containsKey("app_id")) {
            mAppId = (String) serverExtras.get("app_id");

        } else {
            if (mListener != null) {
                mListener.onBannerAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "app_id is empty!"));
            }
            return;
        }

        String size = "";
        if (serverExtras.containsKey("size")) {
            size = serverExtras.get("size").toString();
        }

        AdView adView = null;

        switch (size) {
            case "320x50":
                adView = new AdView(activity, AdSize.Size_320X50, mAppId);
                break;
            case "640x100":
                adView = new AdView(activity, AdSize.Size_640X100, mAppId);
                break;
            case "960x150":
                adView = new AdView(activity, AdSize.Size_960X150, mAppId);
                break;
            case "480x75":
                adView = new AdView(activity, AdSize.Size_480X75, mAppId);
                break;
            case "728x90":
                adView = new AdView(activity, AdSize.Size_728X90, mAppId);
                break;
            default:
                adView = new AdView(activity, AdSize.Size_320X50, mAppId);
                break;
        }


        adView.setAdListener(new AdBannerListener() {
            @Override
            public void onAdShow(Object o) {
                if (mListener != null) {
                    mListener.onBannerAdLoaded(UniplayATBannerAdapter.this);
                }
            }

            @Override
            public void onAdClick() {
                if (mListener != null) {
                    mListener.onBannerAdClicked(UniplayATBannerAdapter.this);
                }
            }

            @Override
            public void onAdError(String s) {
                if (mListener != null) {
                    mListener.onBannerAdLoadFail(UniplayATBannerAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, "", s));
                }
                if (anyThinkBannerView != null) {
                    anyThinkBannerView.removeView(mBannerView);
                }
            }
        });

        mBannerView = adView;

        if (anyThinkBannerView != null) {
            anyThinkBannerView.addView(mBannerView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
    }

    @Override
    public View getBannerView() {
        return mBannerView;
    }

    @Override
    public String getSDKVersion() {
        return "";
    }

    @Override
    public void clean() {
        mBannerView = null;
    }

    @Override
    public String getNetworkName() {
        return UniplayATInitManager.getInstance().getNetworkName();
    }

}
