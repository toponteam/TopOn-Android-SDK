/*
 * Copyright (C) 2019 Mintegral, Inc. All rights reserved.
 */
package com.anythink.hb;

import com.anythink.core.common.entity.HiBidCache;
import com.anythink.core.common.hb.HeadBiddingCacheManager;
import com.anythink.hb.callback.BidRequestCallback;
import com.anythink.hb.callback.BiddingCallback;
import com.anythink.hb.data.AuctionNotification;
import com.anythink.hb.data.AuctionResult;
import com.anythink.hb.data.BidRequestInfo;
import com.anythink.hb.data.BiddingResponse;
import com.anythink.hb.exception.BiddingException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;

/**
 * One transaction for one runtime bidding
 */
public class HeaderBiddingTransaction implements BiddingCallback {
    private static final String TAG = HeaderBiddingTransaction.class.getSimpleName();

    private String requestId;
    private boolean mIsWaitingTimerUp = false;
    private boolean isComplete = false;
    private Timer waitingTimer = new Timer();
    private Timer biddingTimer = new Timer();

    private ExecutorService executor;
    private String placementId;
    private String adType;
    private BidRequestCallback bidRequestCallback;

    /**
     * Map: key:Bidder, value:BidRequestInfo
     */
    private Map<Bidder, BidRequestInfo> bidders = new HashMap<>();
    /**
     * Map: key:Bidder, value:Bidder return status( Bidder is return or not)
     */
    private Map<Bidder, Boolean> returnMap = new HashMap<>();
    /**
     * Map: key:Bidder, value:TimeStamp
     */
    private Map<Bidder, Long> startBidTimeMap = new HashMap<>();

    /**
     * List: BiddingResponse
     */
    private List<BiddingResponse> bidResponses = new ArrayList<>();
    private List<BiddingResponse> processSuccessBidResponses = new ArrayList<>();
    private List<BiddingResponse> processFailedBidResponses = new ArrayList<>();


    HeaderBiddingTransaction(ExecutorService executor, String requestId, String placementId, String adType,
                             BidRequestCallback bidRequestCallback) {
        this.requestId = requestId;
        this.executor = executor;
        this.placementId = placementId;
        this.adType = adType;
        this.bidRequestCallback = bidRequestCallback;
    }

    void startTransaction(final Map<Bidder, BidRequestInfo> bidders, final long waitingTimeOutMS, final long timeOutMS) {

        this.bidders = bidders;

        LogUtil.i(TAG, " requestId = " + requestId + " started time = " + getCurrentTimeStamp());

        if (bidders != null && bidders.size() > 0) {
            for (final Map.Entry<Bidder, BidRequestInfo> entry : bidders.entrySet()) {
                final Bidder bidder = entry.getKey();
                if (bidder != null && !isComplete) {
                    final BidRequestInfo bidRequestInfo = entry.getValue();
                    startBidTimeMap.put(bidder, System.currentTimeMillis());
                    executor.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                bidder.bid(bidRequestInfo, adType, timeOutMS, HeaderBiddingTransaction.this);
                            } catch (BiddingException ex) {
                                LogUtil.e(TAG, bidder + " bidding failed " + ex.getMessage());

                                BiddingResponse biddingResponse = new BiddingResponse(bidder.getBidderClass(), ex.getMessage(), bidder, bidRequestInfo);
                                onBiddingResponse(biddingResponse);

                            } catch (Exception ex) {
                                LogUtil.e(TAG, bidder + " bidding exception " + ex.getMessage());

                                BiddingResponse biddingResponse = new BiddingResponse(bidder.getBidderClass(), ex.getMessage(), bidder, bidRequestInfo);
                                onBiddingResponse(biddingResponse);
                            }
                        }
                    });
                }
            }
        }

        startWaitBiddingTimer(waitingTimeOutMS);
        startBiddingTimer(timeOutMS);
    }

    @Override
    public void onBiddingResponse(BiddingResponse response) {
        synchronized (this) {
            if (response != null && bidResponses != null) {

                LogUtil.i(TAG, "onBiddingResponse");

                //set the time of start to bid
                response.setBiddingStartTime(startBidTimeMap.get(response.getBidder()));

                if (response.isSuccess()) {
                    addHBCache(response);
                }

                if (!isComplete) {

                    bidResponses.add(response);

                    if (response.isSuccess()) {
                        processSuccessBidResponses.add(response);
                    } else {
                        processFailedBidResponses.add(response);
                    }
                    returnMap.put(response.getBidder(), true);

                    if (mIsWaitingTimerUp) {
                        onBidEachResult();
                    }

                    if (bidResponses.size() == bidders.size()) {
                        isComplete = true;

                        LogUtil.i(TAG, " requestId =" + requestId + " -->got all results, return auction result!");
                        onBidRequestFinished();
                    }
                }

            }
        }
    }

    private void addHBCache(BiddingResponse response) {
        LogUtil.i(TAG, "addHBCache");
        BidRequestInfo bidRequestInfo = response.getBidRequestInfo();
        String adsourceId = bidRequestInfo.getString(BidRequestInfo.KEY_ADSOURCE_ID);
        long bidTokenAvailTime = bidRequestInfo.getLong(BidRequestInfo.KEY_BID_TOKEN_AVAIL_TIME);

        //cache token
        /**Add Bid Cache**/
        HiBidCache hiBidCache = new HiBidCache();
        hiBidCache.payLoad = response.getPayload().toString();
        hiBidCache.price = response.getBiddingPriceUSD();
        hiBidCache.outDateTime = bidTokenAvailTime + System.currentTimeMillis();
        //save hb cache
        HeadBiddingCacheManager.getInstance().addCache(adsourceId, hiBidCache);
    }

    private void onBidResultWhenWaitingTimeout() {

        if (processSuccessBidResponses.size() > 0 || processFailedBidResponses.size() > 0) {
            LogUtil.i(TAG, "onBidResultWhenWaitingTimeout");

            AuctionResult auctionResult = new AuctionResult();
            auctionResult.setRequestId(requestId);
            auctionResult.setPlacementId(placementId);

            if (processSuccessBidResponses.size() > 0) {
                auctionResult.setSuccessBidders(new ArrayList<>(processSuccessBidResponses));
            }
            if (processFailedBidResponses.size() > 0) {
                auctionResult.setFailedBidders(new ArrayList<>(processFailedBidResponses));
            }

            bidRequestCallback.onBidResultWhenWaitingTimeout(placementId, auctionResult);

            processSuccessBidResponses.clear();
            processFailedBidResponses.clear();
        }
    }

    private void onBidEachResult() {

        if (processSuccessBidResponses.size() > 0 || processFailedBidResponses.size() > 0) {
            LogUtil.i(TAG, "onBidEachResult");

            AuctionResult auctionResult = new AuctionResult();
            auctionResult.setRequestId(requestId);
            auctionResult.setPlacementId(placementId);

            if (processSuccessBidResponses.size() > 0) {
                auctionResult.setSuccessBidders(new ArrayList<>(processSuccessBidResponses));
            }
            if (processFailedBidResponses.size() > 0) {
                auctionResult.setFailedBidders(new ArrayList<>(processFailedBidResponses));
            }

            bidRequestCallback.onBidEachResult(placementId, auctionResult);

            processSuccessBidResponses.clear();
            processFailedBidResponses.clear();
        }
    }

    private void onBidRequestFinished() {
        LogUtil.i(TAG, "onBidRequestFinished");

        cancelTimer();

        if (bidders != null && bidders.size() > 0) {
            for (Map.Entry<Bidder, BidRequestInfo> bidderEntry : bidders.entrySet()) {
                Bidder bidder = bidderEntry.getKey();
                if (bidder != null && !returnMap.containsKey(bidder)) {
                    //add timeout bidder
                    BiddingResponse biddingResponse = new BiddingResponse(
                            bidder.getBidderClass(),
                            bidder.getBidderClass().getSimpleName() + " timeout",
                            bidder,
                            bidderEntry.getValue()
                            , true);

                    //set the time of start to bid
                    biddingResponse.setBiddingStartTime(startBidTimeMap.get(bidder));

                    returnMap.put(bidder, true);

                    bidResponses.add(biddingResponse);
                    processFailedBidResponses.add(biddingResponse);
                }
            }
        }

        notifyNetworkBiddingResult();


        AuctionResult auctionResult = new AuctionResult();
        auctionResult.setRequestId(requestId);
        auctionResult.setPlacementId(placementId);

        if (processSuccessBidResponses.size() > 0) {
            auctionResult.setSuccessBidders(new ArrayList<>(processSuccessBidResponses));
        }
        if (processFailedBidResponses.size() > 0) {
            auctionResult.setFailedBidders(new ArrayList<>(processFailedBidResponses));
        }

        bidRequestCallback.onBidRequestFinished(placementId, auctionResult);

        processSuccessBidResponses.clear();
        processFailedBidResponses.clear();
    }

    private void notifyNetworkBiddingResult() {
        //notify win, loss, timeout
        AuctionNotification winNotification =
                AuctionNotification.getAuctionNotification(AuctionNotification.ReasonCode.Win);
        AuctionNotification lossNotification =
                AuctionNotification.getAuctionNotification(AuctionNotification.ReasonCode.Loss);
        AuctionNotification timeoutNotification =
                AuctionNotification.getAuctionNotification(AuctionNotification.ReasonCode.Timeout);

        Collections.sort(bidResponses);
        int size = bidResponses.size();
        BiddingResponse biddingResponse;
        Bidder bidder;
        for (int i = 0; i < size; i++) {
            biddingResponse = bidResponses.get(i);
            bidder = biddingResponse.getBidder();
            if (bidder != null) {
                if (i == 0) {// winner
                    bidder.onAuctionNotification(winNotification);
                } else {
                    if (biddingResponse.isTimeout()) {// timeout
                        bidder.onAuctionNotification(timeoutNotification);
                    } else {// failed
                        bidder.onAuctionNotification(lossNotification);
                    }
                }
            }
        }
    }

    private void startWaitBiddingTimer(long timeOutMS) {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                synchronized (this) {
                    if (!mIsWaitingTimerUp) {
                        mIsWaitingTimerUp = true;

                        LogUtil.i(TAG, " requestId = " + requestId + " --> waiting timer up, return auction result!");
                        onBidResultWhenWaitingTimeout();
                    }
                }
            }
        };
        waitingTimer.schedule(task, timeOutMS + 5);
    }

    private void startBiddingTimer(long timeOutMS) {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                synchronized (this) {
                    if (!isComplete) {
                        isComplete = true;

                        LogUtil.i(TAG, " requestId = " + requestId + " --> time out, return auction result!");
                        onBidRequestFinished();
                    }
                }
            }
        };
        biddingTimer.schedule(task, timeOutMS + 5);
    }

    private String getCurrentTimeStamp() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss:SSS");
        return formatter.format(new Date());
    }

    private void cancelTimer() {
        LogUtil.i(TAG, "cancelTimer");
        if (waitingTimer != null) {
            waitingTimer.cancel();
            waitingTimer = null;
        }

        if (biddingTimer != null) {
            biddingTimer.cancel();
            biddingTimer = null;
        }
    }

}
