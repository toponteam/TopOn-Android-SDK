/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.inmobi;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;

import com.anythink.banner.unitgroup.api.CustomBannerAdapter;
import com.anythink.core.api.ATBiddingListener;
import com.anythink.core.api.ATBiddingResult;
import com.inmobi.ads.AdMetaInfo;
import com.inmobi.ads.InMobiAdRequestStatus;
import com.inmobi.ads.InMobiBanner;
import com.inmobi.ads.listeners.BannerAdEventListener;

import java.util.Map;


public class InmobiATBannerAdapter extends CustomBannerAdapter {
    private static final String TAG = InmobiATBannerAdapter.class.getSimpleName();

    Long placeId;
    View mBannerView;

    int mRefreshTime;

    InMobiBanner inMobiBanner;

    /***
     * init and load
     */
    private void initAndLoad(final Context context, final Map<String, Object> serverExtras) {
        InmobiATInitManager.getInstance().initSDK(context.getApplicationContext(), serverExtras, new InmobiATInitManager.OnInitCallback() {
            @Override
            public void onSuccess() {
                try {
                    startLoadAd(context);
                } catch (Throwable e) {
                    if (mLoadListener != null) {
                        mLoadListener.onAdLoadError("", e.getMessage());
                    }
                }
            }

            @Override
            public void onError(String errorMsg) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError("", "Inmobi " + errorMsg);
                }
            }
        });
    }

    private void startLoadAd(Context context) {
        inMobiBanner = new InMobiBanner(context, placeId);

        if (mRefreshTime > 0) {
            inMobiBanner.setEnableAutoRefresh(true);
            inMobiBanner.setRefreshInterval(mRefreshTime);
        } else {
            inMobiBanner.setEnableAutoRefresh(false);
            inMobiBanner.setRefreshInterval(0);
        }

        inMobiBanner.setBannerSize(dip2px(context, 320), dip2px(context, 50));
        inMobiBanner.setListener(this.getBannerAdEventListener());

        inMobiBanner.load();
    }


    @Override
    public void loadCustomNetworkAd(Context activity, Map<String, Object> serverExtras, Map<String, Object> localExtras) {

        String accountId = (String) serverExtras.get("app_id");
        String unitId = (String) serverExtras.get("unit_id");

        if (TextUtils.isEmpty(accountId) || TextUtils.isEmpty(unitId)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "inmobi account_id or unit_id is empty!");
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

        String payload = (String) serverExtras.get("payload");

        if (!TextUtils.isEmpty(payload)) {
            startLoadBidAd(payload);
        } else {
            initAndLoad(activity, serverExtras);
        }
    }


    @Override
    public View getBannerView() {
        return mBannerView;
    }

    @Override
    public void destory() {
        mBannerView = null;

        if (inMobiBanner != null) {
            try {
                inMobiBanner.setListener(null);
            } catch (Throwable e) {
            }
            inMobiBanner.destroy();
            inMobiBanner = null;
        }
    }


    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    @Override
    public String getNetworkSDKVersion() {
        return InmobiATInitManager.getInstance().getNetworkVersion();
    }

    @Override
    public String getNetworkName() {
        return InmobiATInitManager.getInstance().getNetworkName();
    }

    @Override
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        return InmobiATInitManager.getInstance().setUserDataConsent(context, isConsent, isEUTraffic);
    }

    @Override
    public String getNetworkPlacementId() {
        try {
            return String.valueOf(placeId);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private BannerAdEventListener getBannerAdEventListener() {
        return new BannerAdEventListener() {

            @Override
            public void onAdLoadSucceeded(InMobiBanner inMobiBanner, AdMetaInfo adMetaInfo) {
                mBannerView = inMobiBanner;
                if (mLoadListener != null) {
                    mLoadListener.onAdCacheLoaded();
                }
            }

            @Override
            public void onAdLoadFailed(InMobiBanner inMobiBanner, InMobiAdRequestStatus inMobiAdRequestStatus) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError(inMobiAdRequestStatus.getStatusCode().name(), inMobiAdRequestStatus.getMessage());
                }
            }

            @Override
            public void onAdDisplayed(InMobiBanner inMobiBanner) {
                if (mImpressionEventListener != null) {
                    mImpressionEventListener.onBannerAdShow();

                }
            }

            @Override
            public void onAdDismissed(InMobiBanner inMobiBanner) {
                if (mImpressionEventListener != null) {
                    mImpressionEventListener.onBannerAdClose();
                }
            }

            @Override
            public void onAdClicked(InMobiBanner inMobiBanner, Map<Object, Object> map) {
                if (mImpressionEventListener != null) {
                    mImpressionEventListener.onBannerAdClicked();
                }
            }

        };
    }

    private void startLoadBidAd(String payload) {
        Object object = InmobiATInitManager.getInstance().getBidAdObject(payload);
        if (object instanceof InMobiBanner) {
            inMobiBanner = (InMobiBanner) object;
        }
        InmobiATInitManager.getInstance().removeBidAdObject(payload);


        if (mRefreshTime > 0) {
            inMobiBanner.setEnableAutoRefresh(true);
            inMobiBanner.setRefreshInterval(mRefreshTime);
        } else {
            inMobiBanner.setEnableAutoRefresh(false);
            inMobiBanner.setRefreshInterval(0);
        }

        inMobiBanner.setListener(this.getBannerAdEventListener());

        inMobiBanner.getPreloadManager().load();
    }

    @Override
    public boolean startBiddingRequest(final Context applicationContext, Map<String, Object> serverExtras, final ATBiddingListener biddingListener) {
        String unitId = (String) serverExtras.get("unit_id");
        placeId = Long.parseLong(unitId);

        InmobiATInitManager.getInstance().initSDK(applicationContext, serverExtras, new InmobiATInitManager.OnInitCallback() {
            @Override
            public void onSuccess() {
                try {
                    startBid(applicationContext, biddingListener);
                } catch (Throwable e) {
                    if (biddingListener != null) {
                        biddingListener.onC2SBidResult(ATBiddingResult.fail(e.getMessage()));
                    }
                }
            }

            @Override
            public void onError(String errorMsg) {
                if (biddingListener != null) {
                    biddingListener.onC2SBidResult(ATBiddingResult.fail(errorMsg));
                }
            }
        });

        return true;
    }

    private void startBid(Context context, final ATBiddingListener biddingListener) {
        inMobiBanner = new InMobiBanner(context, placeId);
        inMobiBanner.setBannerSize(dip2px(context, 320), dip2px(context, 50));

        inMobiBanner.setListener(new BannerAdEventListener() {

            @Override
            public void onAdFetchSuccessful(@NonNull InMobiBanner inMobiBanner, @NonNull AdMetaInfo adMetaInfo) {
                InmobiATInitManager.getInstance().putBidAdObject(adMetaInfo.getCreativeID(), inMobiBanner);

                if (biddingListener != null) {
                    biddingListener.onC2SBidResult(ATBiddingResult.success(adMetaInfo.getBid(), adMetaInfo.getCreativeID(), null, null));
                }
            }

            @Override
            public void onAdFetchFailed(@NonNull InMobiBanner inMobiBanner, @NonNull InMobiAdRequestStatus status) {
                if (biddingListener != null) {
                    biddingListener.onC2SBidResult(ATBiddingResult.fail(status.getMessage()));
                }
            }

        });

        inMobiBanner.getPreloadManager().preload();
    }

}