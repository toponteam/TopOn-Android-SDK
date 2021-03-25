/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.helium;

import android.text.TextUtils;

import com.anythink.core.api.ATBiddingListener;
import com.anythink.core.api.ATBiddingResult;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialEventListener;
import com.chartboost.heliumsdk.ad.HeliumAdError;
import com.chartboost.heliumsdk.ad.HeliumInterstitialAdListener;

import java.util.HashMap;

public abstract class HeliumCustomInterstitialListener implements HeliumInterstitialAdListener {
    private ATBiddingListener mBidListener;
    private CustomInterstitialEventListener mImpressionListener;

    public void setBidListener(ATBiddingListener bidListener) {
        mBidListener = bidListener;
    }

    public void setImpressionListener(CustomInterstitialEventListener impressionListener) {
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
            mImpressionListener.onInterstitialAdShow();
        }
    }

    @Override
    public void didClose(String s, HeliumAdError heliumAdError) {
        if (mImpressionListener != null) {
            mImpressionListener.onInterstitialAdClose();
        }
    }

    public abstract void bidSuccess(String bidId);

    protected void destory() {
        mImpressionListener = null;
    }
}
