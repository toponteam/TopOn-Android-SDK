/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.inmobi;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.ATBiddingListener;
import com.anythink.core.api.ATBiddingResult;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;
import com.inmobi.ads.AdMetaInfo;
import com.inmobi.ads.InMobiAdRequestStatus;
import com.inmobi.ads.InMobiInterstitial;
import com.inmobi.ads.listeners.InterstitialAdEventListener;

import java.util.Map;


public class InmobiATInterstitialAdapter extends CustomInterstitialAdapter {
    private static final String TAG = InmobiATInterstitialAdapter.class.getSimpleName();

    InMobiInterstitial interstitialAd;
    Long placeId;

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


    @Override
    public void loadCustomNetworkAd(Context context, Map<String, Object> serverExtras, Map<String, Object> localExtras) {

        String accountId = (String) serverExtras.get("app_id");
        String unitId = (String) serverExtras.get("unit_id");

        if (TextUtils.isEmpty(accountId) || TextUtils.isEmpty(unitId)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "inmobi account_id or unit_id is empty!");
            }
            return;
        }
        placeId = Long.parseLong(unitId);

        String payload = (String) serverExtras.get("payload");

        if (!TextUtils.isEmpty(payload)) {
            startLoadBidAd(payload);
        } else {
            initAndLoad(context, serverExtras);
        }
    }

    @Override
    public boolean isAdReady() {
        if (interstitialAd != null) {
            return interstitialAd.isReady();
        }
        return false;
    }

    @Override
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        return InmobiATInitManager.getInstance().setUserDataConsent(context, isConsent, isEUTraffic);
    }

    @Override
    public void show(Activity activity) {
        if (interstitialAd != null && isAdReady()) {
            interstitialAd.show();
        }

    }

    @Override
    public void destory() {
        if (interstitialAd != null) {
            try {
                interstitialAd.setListener(null);
            } catch (Throwable e) {
            }
            interstitialAd = null;
        }
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
    public String getNetworkPlacementId() {
        try {
            return String.valueOf(placeId);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private void startLoadAd(Context context) {
        interstitialAd = new InMobiInterstitial(context.getApplicationContext(), placeId, this.getInterstitialAdEventListener());
        InmobiATInitManager.getInstance().addInmobiAd(interstitialAd);
        interstitialAd.load();
    }

    private InterstitialAdEventListener getInterstitialAdEventListener() {
        return new InterstitialAdEventListener() {
            @Override
            public void onAdLoadFailed(InMobiInterstitial inMobiInterstitial, InMobiAdRequestStatus inMobiAdRequestStatus) {
                InmobiATInitManager.getInstance().removeInmobiAd(interstitialAd);
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError("" + inMobiAdRequestStatus.getStatusCode(), inMobiAdRequestStatus.getMessage());
                }
            }

            @Override
            public void onAdFetchSuccessful(InMobiInterstitial inMobiInterstitial, AdMetaInfo adMetaInfo) {
                if (mLoadListener != null) {
                    mLoadListener.onAdDataLoaded();
                }
            }

            @Override
            public void onAdLoadSucceeded(InMobiInterstitial inMobiInterstitial, AdMetaInfo adMetaInfo) {
                if (mLoadListener != null) {
                    mLoadListener.onAdCacheLoaded();
                }
            }

            @Override
            public void onAdDisplayFailed(InMobiInterstitial inMobiInterstitial) {
                InmobiATInitManager.getInstance().removeInmobiAd(interstitialAd);
            }

            @Override
            public void onAdWillDisplay(InMobiInterstitial inMobiInterstitial) {
            }

            @Override
            public void onAdDisplayed(InMobiInterstitial inMobiInterstitial, AdMetaInfo adMetaInfo) {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdShow();
                }
            }

            @Override
            public void onAdDismissed(InMobiInterstitial inMobiInterstitial) {
                InmobiATInitManager.getInstance().removeInmobiAd(interstitialAd);
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdClose();
                }
            }

            @Override
            public void onAdClicked(InMobiInterstitial inMobiInterstitial, Map<Object, Object> map) {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdClicked();
                }
            }
        };
    }

    private void startLoadBidAd(String payload) {
        Object object = InmobiATInitManager.getInstance().getBidAdObject(payload);
        if (object instanceof InMobiInterstitial) {
            interstitialAd = (InMobiInterstitial) object;
        }
        InmobiATInitManager.getInstance().removeBidAdObject(payload);


        interstitialAd.setListener(this.getInterstitialAdEventListener());

        InmobiATInitManager.getInstance().addInmobiAd(interstitialAd);
        interstitialAd.getPreloadManager().load();
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
        final InMobiInterstitial interstitialAd = new InMobiInterstitial(context.getApplicationContext(), placeId, new InterstitialAdEventListener() {

            @Override
            public void onAdFetchSuccessful(InMobiInterstitial inMobiInterstitial, AdMetaInfo adMetaInfo) {
                InmobiATInitManager.getInstance().putBidAdObject(adMetaInfo.getCreativeID(), inMobiInterstitial);

                if (biddingListener != null) {
                    biddingListener.onC2SBidResult(ATBiddingResult.success(adMetaInfo.getBid(), adMetaInfo.getCreativeID(), null, null));
                }
            }

            @Override
            public void onAdFetchFailed(InMobiInterstitial inMobiInterstitial, InMobiAdRequestStatus status) {
                if (biddingListener != null) {
                    biddingListener.onC2SBidResult(ATBiddingResult.fail(status.getMessage()));
                }
            }
        });

        InmobiATInitManager.getInstance().addInmobiAd(interstitialAd);
        interstitialAd.getPreloadManager().preload();
    }

}