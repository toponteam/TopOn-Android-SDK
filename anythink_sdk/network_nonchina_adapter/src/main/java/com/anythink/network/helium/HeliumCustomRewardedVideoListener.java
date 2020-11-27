package com.anythink.network.helium;

import android.text.TextUtils;

import com.anythink.core.api.ATBiddingListener;
import com.anythink.core.api.ATBiddingResult;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialEventListener;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardedVideoEventListener;
import com.chartboost.heliumsdk.ad.HeliumAdError;
import com.chartboost.heliumsdk.ad.HeliumInterstitialAdListener;
import com.chartboost.heliumsdk.ad.HeliumRewardedAdListener;

import java.util.HashMap;

public abstract class HeliumCustomRewardedVideoListener implements HeliumRewardedAdListener {
    private ATBiddingListener mBidListener;
    private CustomRewardedVideoEventListener mImpressionListener;

    public void setBidListener(ATBiddingListener bidListener) {
        mBidListener = bidListener;
    }

    public void setImpressionListener(CustomRewardedVideoEventListener impressionListener) {
        mImpressionListener = impressionListener;
    }

    @Override
    public void didReceiveWinningBid(String s, HashMap<String, String> hashMap) {
        if (mBidListener != null) {
            double price = 0;
            try {
                price = Double.parseDouble(hashMap.get("price"));
            } catch (Throwable e) {

            }
            String bidId = hashMap.get("auction-id");
            if (TextUtils.isEmpty(bidId)) {
                mBidListener.onC2SBidResult(ATBiddingResult.fail("auction-id is empty."));
            } else {
                bidSuccess(bidId);
                mBidListener.onC2SBidResult(ATBiddingResult.success(price, bidId, "", ""));
            }
            mBidListener = null;
        }
    }

    @Override
    public void didCache(String s, HeliumAdError heliumAdError) {
        if (heliumAdError != null) {
            if (mBidListener != null) {
                mBidListener.onC2SBidResult(ATBiddingResult.fail(heliumAdError.getMessage()));
            }
            mBidListener = null;
        }
    }

    @Override
    public void didShow(String s, HeliumAdError heliumAdError) {
        if (mImpressionListener != null && heliumAdError == null) {
            mImpressionListener.onRewardedVideoAdPlayStart();
        }
    }

    @Override
    public void didReceiveReward(String var1, String var2) {
        if (mImpressionListener != null) {
            mImpressionListener.onRewardedVideoAdPlayEnd();
            mImpressionListener.onReward();
        }
    }

    @Override
    public void didClose(String s, HeliumAdError heliumAdError) {
        if (mImpressionListener != null) {
            mImpressionListener.onRewardedVideoAdClosed();
        }
    }

    public abstract void bidSuccess(String bidId);

    protected void destory() {
        mImpressionListener = null;
    }
}
