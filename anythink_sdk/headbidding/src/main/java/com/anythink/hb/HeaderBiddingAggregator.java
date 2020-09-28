/*
 * Copyright (C) 2019 Mintegral, Inc. All rights reserved.
 */
package com.anythink.hb;

import android.content.Context;
import android.text.TextUtils;

import com.anythink.hb.callback.BidRequestCallback;
import com.anythink.hb.constants.Constants;
import com.anythink.hb.data.BidRequestInfo;
import com.anythink.hb.data.HBDataContext;
import com.anythink.hb.exception.BidRequestException;
import com.anythink.hb.exception.SdkIntegratedException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Entrance class which init bidders and send bid request.
 */
public class HeaderBiddingAggregator {
    private static final String TAG = HeaderBiddingAggregator.class.getName();

    /**
     * switch for developers to enable log or not
     */
    private static boolean mIsDebugMode = false;

    private static ExecutorService executor = Executors.newCachedThreadPool();
    private static Context context;

    static boolean isDebugMode() {
        return mIsDebugMode;
    }

    /**
     * enable log or not
     *
     * @param isDebugMode
     */
    static void setDebugMode(boolean isDebugMode) {
        mIsDebugMode = isDebugMode;
    }


    /**
     * passed in context
     *
     * @param context
     */
    static void init(final Context context) {
        HeaderBiddingAggregator.context = context;
    }

    /**
     * @param bidReqs            bid request for openrtb
     * @param timeOutMS          time out in microseconds
     * @param bidRequestCallback bid request callback
     * @return
     * @throws BidRequestException
     */
    public static HeaderBiddingTransaction requestBid(final List<BidRequestInfo> bidReqs, final String requestId,
                                                      final String placementId, final String adType, long waitingTimeOutMS, long timeOutMS,
                                                      BidRequestCallback bidRequestCallback) throws BidRequestException, SdkIntegratedException {
        if (context == null) {
            throw new BidRequestException("Context is null or empty!");
        }

        if (bidReqs == null || bidReqs.size() == 0) {
            throw new BidRequestException("Bidders is null or empty!");
        }

        if (TextUtils.isEmpty(placementId.trim())) {
            throw new BidRequestException("placementId is null or empty!");
        }

        if (TextUtils.isEmpty(adType.trim())) {
            throw new BidRequestException("adType is null or empty!");
        }

        if (bidRequestCallback == null) {
            throw new BidRequestException("bidRequestCallback is null");
        }

        if (timeOutMS <= 0) {
            timeOutMS = Constants.DEFAULT_TIME_OUT_MS;
        }

        //new and init bidders
        Map<Bidder, BidRequestInfo> bidders = new HashMap<>();
        for (int i = 0; i < bidReqs.size(); i++) {
            try {
                Class bidderClass = bidReqs.get(i).getBidderClass();
                if (bidderClass != null) {
                    Object newInstance = bidderClass.newInstance();
                    if (newInstance instanceof Bidder) {
                        Bidder bidder = (Bidder) newInstance;
                        bidder.init(new HBDataContext(context, bidReqs.get(i).getAppId(), bidReqs.get(i).getAppKey()));
                        bidders.put(bidder, bidReqs.get(i));
                    }
                }
            } catch (Throwable ex) {
                bidRequestCallback.onError(placementId, bidReqs.get(i), ex);
            }
        }

        if (bidders == null || bidders.size() == 0) {
            throw new BidRequestException("No vail bid request.");
        }


        //do runtime bidding
        HeaderBiddingTransaction transaction =
                new HeaderBiddingTransaction(executor, requestId, placementId, adType, bidRequestCallback);
        transaction.startTransaction(bidders, waitingTimeOutMS, timeOutMS);

        return transaction;

    }

}
