/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.helium;

import android.app.Activity;
import android.content.Context;

import com.anythink.core.api.ATBiddingListener;
import com.anythink.core.api.ATBiddingResult;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;
import com.chartboost.heliumsdk.HeliumSdk;
import com.chartboost.heliumsdk.ad.HeliumAd;
import com.chartboost.heliumsdk.ad.HeliumAdError;
import com.chartboost.heliumsdk.ad.HeliumInterstitialAd;
import com.chartboost.heliumsdk.ad.HeliumInterstitialAdListener;

import java.util.HashMap;
import java.util.Map;

public class HeliumATInterstitialAdapter extends CustomInterstitialAdapter {

    HeliumInterstitialAd mHeliumInterstitialAd;
    String mPlacementName;
    String mPayload;

    @Override
    public void loadCustomNetworkAd(Context context, Map<String, Object> serverExtra, Map<String, Object> localExtra) {
        mPlacementName = serverExtra.get("placement_name").toString();
        mPayload = serverExtra.get("payload").toString();

        if (mPayload != null) {
            HeliumAd ad = HeliumATInitManager.getInstance().getBidAdObject(mPayload);
            if (ad instanceof HeliumInterstitialAd) {
                mHeliumInterstitialAd = (HeliumInterstitialAd) ad;
            }
            HeliumATInitManager.getInstance().removeBidAdObject(mPayload);
        }

        if (mHeliumInterstitialAd != null) {
            if (mHeliumInterstitialAd.readyToShow()) {
                if (mLoadListener != null) {
                    mLoadListener.onAdCacheLoaded();
                }
                return;
            }
        }

        HeliumATInitManager.getInstance().initSDK(context, serverExtra, new HeliumATInitManager.InitCallback() {
            @Override
            public void initSuccess() {
                mHeliumInterstitialAd = new HeliumInterstitialAd(mPlacementName, new HeliumInterstitialAdListener() {
                    @Override
                    public void didReceiveWinningBid(String s, HashMap<String, String> hashMap) {

                    }

                    @Override
                    public void didCache(String s, HeliumAdError heliumAdError) {
                        if (mLoadListener != null) {
                            if (heliumAdError != null) {
                                mLoadListener.onAdLoadError(heliumAdError.getCode() + "", heliumAdError.getMessage());
                            } else {
                                mLoadListener.onAdCacheLoaded();
                            }
                        }
                    }

                    @Override
                    public void didShow(String s, HeliumAdError heliumAdError) {
                        if (mImpressListener != null && heliumAdError == null) {
                            mImpressListener.onInterstitialAdShow();
                        }
                    }

                    @Override
                    public void didClose(String s, HeliumAdError heliumAdError) {
                        if (mImpressListener != null) {
                            mImpressListener.onInterstitialAdClose();
                        }
                    }
                });

                mHeliumInterstitialAd.load();
            }

            @Override
            public void initError(String code, String msg) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError(code, msg);
                }
            }
        });

    }

    @Override
    public void destory() {
        if (mHeliumInterstitialAd != null) {
            if (mHeliumInterstitialAd.heliumInterstitialAdListener instanceof HeliumCustomInterstitialListener) {
                ((HeliumCustomInterstitialListener) mHeliumInterstitialAd.heliumInterstitialAdListener).setImpressionListener(null);
            }
        }
        mHeliumInterstitialAd = null;
    }

    @Override
    public String getNetworkPlacementId() {
        return mPlacementName;
    }

    @Override
    public String getNetworkSDKVersion() {
        return HeliumATInitManager.getInstance().getNetworkVersion();
    }

    @Override
    public String getNetworkName() {
        return HeliumATInitManager.getInstance().getNetworkName();
    }

    @Override
    public boolean isAdReady() {
        if (mHeliumInterstitialAd != null) {
            return mHeliumInterstitialAd.readyToShow();
        }
        return false;
    }

    @Override
    public void show(Activity activity) {
        if (mHeliumInterstitialAd != null) {
            if (mHeliumInterstitialAd.heliumInterstitialAdListener instanceof HeliumCustomInterstitialListener) {
                ((HeliumCustomInterstitialListener) mHeliumInterstitialAd.heliumInterstitialAdListener).setImpressionListener(mImpressListener);
            }
            HeliumSdk.show(mHeliumInterstitialAd);
        }
    }

    @Override
    public boolean startBiddingRequest(Context applicationContext, Map<String, Object> serverExtra, final ATBiddingListener biddingListener) {
        mPlacementName = serverExtra.get("placement_name").toString();
        HeliumATInitManager.getInstance().initSDK(applicationContext, serverExtra, new HeliumATInitManager.InitCallback() {
            @Override
            public void initSuccess() {
                HeliumCustomInterstitialListener listener = new HeliumCustomInterstitialListener() {
                    @Override
                    public void bidSuccess(String bidId) {
                        HeliumATInitManager.getInstance().putBidAdObject(bidId, mHeliumInterstitialAd);
                    }
                };
                listener.setBidListener(biddingListener);

                mHeliumInterstitialAd = new HeliumInterstitialAd(mPlacementName, listener);

                mHeliumInterstitialAd.load();
            }

            @Override
            public void initError(String code, String msg) {
                if (biddingListener != null) {
                    biddingListener.onC2SBidResult(ATBiddingResult.fail(msg));
                }
            }
        });
        return true;
    }


}
