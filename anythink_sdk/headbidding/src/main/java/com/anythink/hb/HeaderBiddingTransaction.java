/*
 * Copyright (C) 2019 Mintegral, Inc. All rights reserved.
 */
package com.anythink.hb;

import android.text.TextUtils;

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
import java.util.UUID;
import java.util.concurrent.ExecutorService;

/**
 * One transaction for one runtime bidding
 */
public class HeaderBiddingTransaction implements BiddingCallback {
    private static final String TAG = HeaderBiddingTransaction.class.getName();

    private String transId;
    private boolean isComplete = false;
    private Timer biddingTimer = new Timer();

    private ExecutorService executor;
    private String unitId;
    private String adType;
    private BidRequestCallback bidRequestCallback;

    private AuctionResult auctionResult = new AuctionResult();
    /** Map: key:Bidder, value:BidRequestInfo*/
    private Map<Bidder, BidRequestInfo> bidders = new HashMap<Bidder, BidRequestInfo>();
    /** Map: key:Bidder, value:Bidder return status( Bidder is return or not)  */
    private Map<Bidder, Boolean> returnMap = new HashMap<Bidder, Boolean>();
    /** List: BiddingResponse*/
    private List<BiddingResponse> bidResponses = new ArrayList<BiddingResponse>();


    public HeaderBiddingTransaction(ExecutorService executor, String unitId, String adType,
                                    BidRequestCallback bidRequestCallback) {
        this.transId = UUID.randomUUID().toString();
        this.executor = executor;
        this.unitId = unitId;
        this.adType = adType;
        this.bidRequestCallback = bidRequestCallback;
    }

    public String startTransaction(final Map<Bidder, BidRequestInfo> bidders, final long timeOutMS){

        this.bidders = bidders;

        LogUtil.i(TAG, " transId =" + transId + " started time = " + getCurrentTimeStamp());

        if (bidders != null && bidders.size() > 0) {
            for (final Map.Entry<Bidder, BidRequestInfo> entry : bidders.entrySet()) {
                final Bidder bidder = entry.getKey();
                final BidRequestInfo bidRequestInfo = entry.getValue();
                if (bidder != null) {
                    executor.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                bidder.bid(bidRequestInfo, adType, timeOutMS, HeaderBiddingTransaction.this);
                            } catch (BiddingException ex) {
                                LogUtil.e(TAG, bidder + " bidding failed " + ex.getMessage());
                            } catch (Exception ex) {
                                LogUtil.e(TAG, bidder + " bidding exception " + ex.getMessage());
                            }
                        }
                    });
                }
            }
        }

        startBiddingTimer(timeOutMS);

        return transId;
    }

    @Override
    public void onBiddingResponse(BiddingResponse response) {
        synchronized (this) {
            if (!isComplete) {
                if (response != null && bidResponses != null) {

                    bidResponses.add(response);
                    returnMap.put(response.getBidder(), true);

                    if (bidResponses.size() == bidders.size()) {
                        isComplete = true;

                        LogUtil.i(TAG, " transId =" + transId + " -->got all results, return auction result!");
                        onAuctionResult();
                    }
                }
            }
        }
    }


    private void startBiddingTimer(long timeOutMS) {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                synchronized (this) {
                    if (!isComplete) {
                        isComplete = true;

                        LogUtil.i(TAG, " transId =" + transId + " --> time out, return auction result!");
                        LogUtil.i(TAG, "threadName=" + Thread.currentThread().getName()+" threadId="+Thread.currentThread().getId());
                        onAuctionResult();
                    }
                }
            }
        };
        biddingTimer.schedule(task, timeOutMS + 5);
    }

    private String getCurrentTimeStamp() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss:SSS");

        String timeStamp =formatter.format(new Date());

        return timeStamp;
    }

    private void onAuctionResult(){
        LogUtil.i(TAG, " transId =" + transId + " ended time = " + getCurrentTimeStamp());

        if (bidResponses == null || bidResponses.size() == 0){
            onAuctionFail();
        }else {
            onAuctionSuccess();
        }
    }

    private void onAuctionFail(){

        //get timeout bidders
        List<BiddingResponse> timeoutBidders = new ArrayList<BiddingResponse>();
        if (bidders != null && bidders.size() > 0) {
            for (Map.Entry<Bidder, BidRequestInfo> bidderEntry : bidders.entrySet()) {
                Bidder bidder = bidderEntry.getKey();
                if (!returnMap.containsKey(bidder)){
                    //add timeout bidder
                    BiddingResponse biddingResponse = new BiddingResponse(
                            bidder.getBidderClass(),
                            bidder.getBidderClass().getSimpleName() + " timeout",
                            bidder,
                            bidder.getBidderRequestInfo());
                    timeoutBidders.add(biddingResponse);
                }
            }
        }

        //return auction result
        auctionResult.setTransactionId(transId);
        auctionResult.setUnitId(unitId);
        auctionResult.setWinner(null);
        auctionResult.setOtherBidders(timeoutBidders);
        bidRequestCallback.onBidRequestCallback(unitId, auctionResult);

        //notify all timeout
        AuctionNotification timeoutNotification =
                AuctionNotification.getAuctionNotification(AuctionNotification.ReasonCode.Timeout);
        for(int i=0; i < timeoutBidders.size(); i++){
            //notify timeout
            timeoutBidders.get(i).getBidder().onAuctionNotification(timeoutNotification);
        }
    }

    private void onAuctionSuccess(){
        if (bidResponses != null && bidResponses.size() > 0) {

            //get winner and loss bidders
            BiddingResponse winner = null;
            List<BiddingResponse> lossBidders = new ArrayList<BiddingResponse>();
            Collections.sort(bidResponses);
            for(int i=0; i < bidResponses.size(); i++){
                BiddingResponse biddingResponse = bidResponses.get(i);
                /** Winner's price must be greater than zero */
                if(biddingResponse.getBiddingPriceUSD() <= 0.0 || !TextUtils.isEmpty(biddingResponse.getErrorMessage())){
                    lossBidders.add(biddingResponse);
                    continue;
                }

                if (winner == null){
                    winner = biddingResponse;
                }else {
                    lossBidders.add(biddingResponse);
                }
            }


            //get timeout bidders
            List<BiddingResponse> timeoutBidders = new ArrayList<BiddingResponse>();
            if (bidders != null && bidders.size() > 0) {
                for (Map.Entry<Bidder, BidRequestInfo> bidderEntry : bidders.entrySet()) {
                    Bidder bidder = bidderEntry.getKey();
                    if (!returnMap.containsKey(bidder)){
                        //add timeout bidder
                        BiddingResponse biddingResponse = new BiddingResponse(
                                bidder.getBidderClass(),
                                bidder.getBidderClass().getSimpleName() + " timeout",
                                bidder,
                                bidder.getBidderRequestInfo());
                        timeoutBidders.add(biddingResponse);
                    }
                }
            }

            //return auction result
            auctionResult.setTransactionId(transId);
            auctionResult.setUnitId(unitId);
            auctionResult.setWinner(winner);
            List<BiddingResponse> otherBidders = new ArrayList<BiddingResponse>();
            otherBidders.addAll(lossBidders);
            otherBidders.addAll(timeoutBidders);
            auctionResult.setOtherBidders(otherBidders);
            bidRequestCallback.onBidRequestCallback(unitId, auctionResult);

            //notify win, loss, timeout
            AuctionNotification winNotification =
                    AuctionNotification.getAuctionNotification(AuctionNotification.ReasonCode.Win);
            AuctionNotification lossNotification =
                    AuctionNotification.getAuctionNotification(AuctionNotification.ReasonCode.Loss);
            AuctionNotification timeoutNotification =
                    AuctionNotification.getAuctionNotification(AuctionNotification.ReasonCode.Timeout);

            if (winner != null){
                //notify win
                winner.getBidder().onAuctionNotification(winNotification);
            }
            for(int i=0; i < otherBidders.size(); i++){
                //notify loss
                otherBidders.get(i).getBidder().onAuctionNotification(lossNotification);
            }
            for(int i=0; i < timeoutBidders.size(); i++){
                //notify timeout
                timeoutBidders.get(i).getBidder().onAuctionNotification(timeoutNotification);
            }

        }
    }

    public void cancelTimer(){
        biddingTimer.cancel();
        biddingTimer = null;
    }
}
