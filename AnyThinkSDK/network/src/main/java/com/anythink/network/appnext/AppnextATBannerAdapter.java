package com.anythink.network.appnext;

import android.content.Context;
import android.view.View;

import com.anythink.banner.api.ATBannerView;
import com.anythink.banner.unitgroup.api.CustomBannerAdapter;
import com.anythink.banner.unitgroup.api.CustomBannerListener;
import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.ErrorCode;
import com.appnext.banners.BannerAdRequest;
import com.appnext.banners.BannerListener;
import com.appnext.banners.BannerSize;
import com.appnext.banners.BannerView;
import com.appnext.core.AppnextAdCreativeType;
import com.appnext.core.AppnextError;

import java.util.Map;

public class AppnextATBannerAdapter extends CustomBannerAdapter {
    private final String TAG = AppnextATBannerAdapter.class.getSimpleName();


    CustomBannerListener mListener;

    String mPlacementId;

    BannerView mBannerView;

    @Override
    public void loadBannerAd(ATBannerView bannerView, Context activity, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, CustomBannerListener customBannerListener) {

        mListener = customBannerListener;

        if (serverExtras == null) {
            if (mListener != null) {
                mListener.onBannerAdLoadFail(AppnextATBannerAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "This placement's params in server is null!"));
            }
            return;
        }

        if (serverExtras.containsKey("placement_id")) {
            mPlacementId = (String) serverExtras.get("placement_id");

        } else {
            if (mListener != null) {
                mListener.onBannerAdLoadFail(AppnextATBannerAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "placement_id is empty!"));
            }
            return;
        }

        String size = "";
        if (serverExtras.containsKey("size")) {
            size = serverExtras.get("size").toString();
        }

        AppnextATInitManager.getInstance().initSDK(activity, serverExtras);

        final BannerView banner = new BannerView(activity);
        banner.setPlacementId(mPlacementId);

        switch (size) {
            case "320x50":
                banner.setBannerSize(BannerSize.BANNER);
                break;
            case "320x100":
                banner.setBannerSize(BannerSize.LARGE_BANNER);
                break;
            case "300x250":
                banner.setBannerSize(BannerSize.MEDIUM_RECTANGLE);
                break;
            default:
                banner.setBannerSize(BannerSize.BANNER);
                break;
        }


        banner.setBannerListener(new BannerListener() {

            @Override
            public void onAdLoaded(String s, AppnextAdCreativeType appnextAdCreativeType) {
                mBannerView = banner;
                if (mListener != null) {
                    mListener.onBannerAdLoaded(AppnextATBannerAdapter.this);
                }
            }

            @Override
            public void onError(AppnextError appnextError) {
                if (mListener != null) {
                    mListener.onBannerAdLoadFail(AppnextATBannerAdapter.this
                            , ErrorCode.getErrorCode(ErrorCode.noADError, "", appnextError.getErrorMessage()));
                }
            }

            @Override
            public void adImpression() {
                if (mListener != null) {
                    mListener.onBannerAdShow(AppnextATBannerAdapter.this);
                }
            }

            @Override
            public void onAdClicked() {
                if (mListener != null) {
                    mListener.onBannerAdClicked(AppnextATBannerAdapter.this);
                }
            }
        });
        banner.loadAd(new BannerAdRequest());

    }

    @Override
    public View getBannerView() {
        return mBannerView;
    }

    @Override
    public void clean() {
        if (mBannerView != null) {
            mBannerView.destroy();
        }
    }

    @Override
    public String getSDKVersion() {
        return "";
    }

    @Override
    public String getNetworkName() {
        return AppnextATInitManager.getInstance().getNetworkName();
    }

}
