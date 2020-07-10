package com.anythink.network.inmobi;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import com.anythink.banner.api.ATBannerView;
import com.anythink.banner.unitgroup.api.CustomBannerAdapter;
import com.anythink.banner.unitgroup.api.CustomBannerListener;
import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.ErrorCode;
import com.inmobi.ads.AdMetaInfo;
import com.inmobi.ads.InMobiAdRequestStatus;
import com.inmobi.ads.InMobiBanner;
import com.inmobi.ads.listeners.BannerAdEventListener;

import java.util.Map;

/**
 * Created by zhou on 2018/6/27.
 */

public class InmobiATBannerAdapter extends CustomBannerAdapter {
    private static final String TAG = InmobiATBannerAdapter.class.getSimpleName();

    CustomBannerListener mListener;
    Long placeId;
    View mBannerView;

    final int INTERACTION = 1;
    final int LEFTAPPLICATION = 2;
    int mClickCallbackType;
    int mRefreshTime;

    InMobiBanner bannerAdLoader;

    /***
     * init and load
     */
    private void initAndLoad(final Context context, final Map<String, Object> serverExtras) {
        InmobiATInitManager.getInstance().initSDK(context.getApplicationContext(), serverExtras, new InmobiATInitManager.OnInitCallback() {
            @Override
            public void onSuccess() {
                startLoadAd(context);
            }

            @Override
            public void onError(String errorMsg) {
                if (mListener != null) {
                    mListener.onBannerAdLoadFail(InmobiATBannerAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "Inmobi " + errorMsg));
                }
            }
        });
    }

    private void startLoadAd(Context context) {
        bannerAdLoader = new InMobiBanner(context, placeId);

        if (mRefreshTime > 0) {
            bannerAdLoader.setEnableAutoRefresh(true);
            bannerAdLoader.setRefreshInterval(mRefreshTime);
        } else {
            bannerAdLoader.setEnableAutoRefresh(false);
            bannerAdLoader.setRefreshInterval(0);
        }

        bannerAdLoader.setBannerSize(dip2px(context, 320), dip2px(context, 50));
        bannerAdLoader.setListener(new BannerAdEventListener() {

            @Override
            public void onAdLoadSucceeded(InMobiBanner inMobiBanner, AdMetaInfo adMetaInfo) {
                mBannerView = inMobiBanner;
                if (mListener != null) {
                    mListener.onBannerAdLoaded(InmobiATBannerAdapter.this);
                }
            }

            @Override
            public void onAdLoadFailed(InMobiBanner inMobiBanner, InMobiAdRequestStatus inMobiAdRequestStatus) {
                if (mListener != null) {
                    mListener.onBannerAdLoadFail(InmobiATBannerAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, inMobiAdRequestStatus.getStatusCode().name(), inMobiAdRequestStatus.getMessage()));
                }
            }

            @Override
            public void onAdDisplayed(InMobiBanner inMobiBanner) {
                if (mListener != null) {
                    mListener.onBannerAdShow(InmobiATBannerAdapter.this);

                }
            }

            @Override
            public void onAdDismissed(InMobiBanner inMobiBanner) {
                if (mListener != null) {
                    mListener.onBannerAdClose(InmobiATBannerAdapter.this);
                }
            }

            @Override
            public void onAdClicked(InMobiBanner inMobiBanner, Map<Object, Object> map) {
                if (mListener != null) {
                    mListener.onBannerAdClicked(InmobiATBannerAdapter.this);
                }
            }

        });

        bannerAdLoader.load();
    }


    @Override
    public void loadBannerAd(ATBannerView anythinkBannerView, Context activity, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, CustomBannerListener customBannerListener) {
        mListener = customBannerListener;
        if (activity == null) {
            if (mListener != null) {
                mListener.onBannerAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "activity is null."));
            }
            return;
        }
        if (serverExtras == null) {
            if (mListener != null) {
                mListener.onBannerAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "inmobi serverExtras is null!"));
            }
            return;
        } else {

            String accountId = (String) serverExtras.get("app_id");
            String unitId = (String) serverExtras.get("unit_id");

            if (TextUtils.isEmpty(accountId) || TextUtils.isEmpty(unitId)) {
                if (mListener != null) {
                    mListener.onBannerAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "inmobi account_id or unit_id is empty!"));
                }
                return;
            }
            placeId = Long.parseLong(unitId);

            mRefreshTime = 0;
            try {
                if (serverExtras.containsKey("nw_rft")) {
                    mRefreshTime = Integer.valueOf((String) serverExtras.get("nw_rft"));
                    mRefreshTime /= 1000f;
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        mClickCallbackType = 0;
        initAndLoad(activity, serverExtras);
    }


    @Override
    public View getBannerView() {
        return mBannerView;
    }

    @Override
    public void clean() {
        mBannerView = null;
    }


    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    @Override
    public String getSDKVersion() {
        return InmobiATConst.getNetworkVersion();
    }

    @Override
    public String getNetworkName() {
        return InmobiATInitManager.getInstance().getNetworkName();
    }
}