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
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;
import com.chartboost.heliumsdk.ad.HeliumAd;
import com.chartboost.heliumsdk.ad.HeliumAdError;
import com.chartboost.heliumsdk.ad.HeliumRewardedAd;
import com.chartboost.heliumsdk.ad.HeliumRewardedAdListener;

import java.util.HashMap;
import java.util.Map;

public class HeliumATRewardedVideoAdapter extends CustomRewardVideoAdapter {

    HeliumRewardedAd mHeliumRewardedAd;
    String mPlacementName;
    String mPayload;

    @Override
    public void loadCustomNetworkAd(Context context, Map<String, Object> serverExtra, Map<String, Object> localExtra) {
        mPlacementName = serverExtra.get("placement_name").toString();
        mPayload = serverExtra.get("payload").toString();

        if (mPayload != null) {
            HeliumAd ad = HeliumATInitManager.getInstance().getBidAdObject(mPayload);
            if (ad instanceof HeliumRewardedAd) {
                mHeliumRewardedAd = (HeliumRewardedAd) ad;
            }
            HeliumATInitManager.getInstance().removeBidAdObject(mPayload);
        }

        if (mHeliumRewardedAd != null) {
            if (mHeliumRewardedAd.readyToShow()) {
                if (mLoadListener != null) {
                    mLoadListener.onAdCacheLoaded();
                }
                return;
            }
        }


        HeliumATInitManager.getInstance().initSDK(context, serverExtra, new HeliumATInitManager.InitCallback() {
            @Override
            public void initSuccess() {
                mHeliumRewardedAd = new HeliumRewardedAd(mPlacementName, new HeliumRewardedAdListener() {
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
                        if (mImpressionListener != null) {
                            if (heliumAdError == null) {
                                mImpressionListener.onRewardedVideoAdPlayStart();
                            } else {
                                mImpressionListener.onRewardedVideoAdPlayFailed(heliumAdError.getCode() + "", heliumAdError.getMessage());
                            }

                        }
                    }

                    @Override
                    public void didClose(String s, HeliumAdError heliumAdError) {
                        if (mImpressionListener != null) {
                            mImpressionListener.onRewardedVideoAdClosed();
                        }
                    }

                    @Override
                    public void didReceiveReward(String s, String s1) {
                        if (mImpressionListener != null) {
                            mImpressionListener.onRewardedVideoAdPlayEnd();
                            mImpressionListener.onReward();
                        }
                    }
                });

                mHeliumRewardedAd.load();
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
        if (mHeliumRewardedAd != null) {
            if (mHeliumRewardedAd.heliumRewardedAdListener instanceof HeliumCustomRewardedVideoListener) {
                ((HeliumCustomRewardedVideoListener) mHeliumRewardedAd.heliumRewardedAdListener).setImpressionListener(null);
            }
        }
        mHeliumRewardedAd = null;
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
        if (mHeliumRewardedAd != null) {
            return mHeliumRewardedAd.readyToShow();
        }
        return false;
    }

    @Override
    public void show(Activity activity) {
        if (mHeliumRewardedAd != null) {
            if (mHeliumRewardedAd.heliumRewardedAdListener instanceof HeliumCustomRewardedVideoListener) {
                ((HeliumCustomRewardedVideoListener) mHeliumRewardedAd.heliumRewardedAdListener).setImpressionListener(mImpressionListener);
            }
            mHeliumRewardedAd.show();
        }
    }

    @Override
    public boolean startBiddingRequest(Context applicationContext, Map<String, Object> serverExtra, final ATBiddingListener biddingListener) {
        mPlacementName = serverExtra.get("placement_name").toString();
        HeliumATInitManager.getInstance().initSDK(applicationContext, serverExtra, new HeliumATInitManager.InitCallback() {
            @Override
            public void initSuccess() {
                HeliumCustomRewardedVideoListener heliumCustomRewardedVideoListener = new HeliumCustomRewardedVideoListener() {
                    @Override
                    public void bidSuccess(String bidId) {
                        HeliumATInitManager.getInstance().putBidAdObject(bidId, mHeliumRewardedAd);
                    }
                };
                heliumCustomRewardedVideoListener.setBidListener(biddingListener);

                mHeliumRewardedAd = new HeliumRewardedAd(mPlacementName, heliumCustomRewardedVideoListener);

                mHeliumRewardedAd.load();
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
