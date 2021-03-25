/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.facebook;

import android.content.Context;
import android.util.Log;

import com.anythink.core.api.ATSDK;
import com.anythink.core.api.MediationBidManager;
import com.anythink.core.common.base.Const;
import com.anythink.core.strategy.PlaceStrategy;
import com.facebook.ads.BidderTokenProvider;
import com.facebook.biddingkit.auction.Auction;
import com.facebook.biddingkit.auction.AuctionListener;
import com.facebook.biddingkit.bidders.Bidder;
import com.facebook.biddingkit.facebook.bidder.FacebookBidder;
import com.facebook.biddingkit.gen.Bid;
import com.facebook.biddingkit.gen.FacebookAdBidFormat;
import com.facebook.biddingkit.gen.biddingConstants;
import com.facebook.biddingkit.waterfall.Waterfall;
import com.facebook.biddingkit.waterfall.WaterfallEntry;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

public class FacebookBidkitAuction {
    private final String TAG = getClass().getSimpleName();
    Context mContext;
    int mFormat;
    List<PlaceStrategy.UnitGroupInfo> mBidUnitGroupInfos;
    List<PlaceStrategy.UnitGroupInfo> mNormalUnitGroupInfos;

    ConcurrentHashMap<String, WaterfallEntry> mWaterfallEntryMap;

    Auction mAuction;

    protected FacebookBidkitAuction(Context context, int format, List<PlaceStrategy.UnitGroupInfo> bidUnitGroupInfos, List<PlaceStrategy.UnitGroupInfo> normalUnitGroupInofs) {
        mContext = context;
        mFormat = format;
        mBidUnitGroupInfos = bidUnitGroupInfos;
        mNormalUnitGroupInfos = normalUnitGroupInofs;
    }

    public void startBidding(String requestUrl, final MediationBidManager.BidListener bidListener) {
        final Map<String, PlaceStrategy.UnitGroupInfo> fbInfoTempMap = new HashMap<>();
        Auction.Builder auctionBuilder = new Auction.Builder();
        for (PlaceStrategy.UnitGroupInfo fbUnitGroupInfo : mBidUnitGroupInfos) {
            try {
                if (fbUnitGroupInfo.networkType == 1) {
                    String content = fbUnitGroupInfo.content;
                    JSONObject jsonObject = new JSONObject(content);
                    String appId = jsonObject.optString("app_id");
                    String unitId = jsonObject.optString("unit_id");
                    String unitType = jsonObject.optString("unit_type");
                    String size = jsonObject.optString("size");
                    FacebookAdBidFormat facebookAdBidFormat = null;
                    switch (String.valueOf(mFormat)) {
                        case Const.FORMAT.BANNER_FORMAT:
                            if ("320x50".equals(size)) {
                                facebookAdBidFormat = FacebookAdBidFormat.BANNER_HEIGHT_50;
                            }

                            if ("320x90".equals(size)) {
                                facebookAdBidFormat = FacebookAdBidFormat.BANNER_HEIGHT_90;
                            }

                            if ("300x250".equals(size) || "320x250".equals(size)) {
                                facebookAdBidFormat = FacebookAdBidFormat.BANNER_HEIGHT_250;
                            }

                            if (facebookAdBidFormat == null) {
                                facebookAdBidFormat = FacebookAdBidFormat.BANNER_HEIGHT_50;
                            }

                            break;
                        case Const.FORMAT.NATIVE_FORMAT:
                            if ("1".equals(unitType)) {
                                facebookAdBidFormat = FacebookAdBidFormat.NATIVE_BANNER;
                            } else {
                                facebookAdBidFormat = FacebookAdBidFormat.NATIVE;
                            }
                            break;
                        case Const.FORMAT.INTERSTITIAL_FORMAT:
                            facebookAdBidFormat = FacebookAdBidFormat.INTERSTITIAL;
                            break;
                        case Const.FORMAT.REWARDEDVIDEO_FORMAT:
                            facebookAdBidFormat = FacebookAdBidFormat.REWARDED_VIDEO;
                            break;
                    }
                    Bidder facebookBidder = new FacebookBidder.Builder(appId, unitId
                            , facebookAdBidFormat, BidderTokenProvider.getBidderToken(mContext)).setTestMode(false).build();
                    auctionBuilder.addBidder(facebookBidder);
                    //Add Facebook UnitGroupInfo
                    fbInfoTempMap.put(unitId, fbUnitGroupInfo);
                }

            } catch (Throwable e) {

            }
        }

        Waterfall waterfall = new WaterfallImpl();
        for (PlaceStrategy.UnitGroupInfo unitGroupInfo : mNormalUnitGroupInfos) {
            waterfall.insert(new WaterfallEntryImpl(null, unitGroupInfo.ecpm * 100, unitGroupInfo.unitId));
        }

        mAuction = auctionBuilder.build();
        mAuction.startRemoteAuction(requestUrl, waterfall, new AuctionListener() {
            @Override
            public void onAuctionCompleted(Waterfall waterfall) {
                handleAuctionResult(fbInfoTempMap, waterfall, bidListener);
            }
        });


    }

    private synchronized void handleAuctionResult(Map<String, PlaceStrategy.UnitGroupInfo> fbMap, Waterfall waterfall, MediationBidManager.BidListener bidListener) {
        Iterator<WaterfallEntry> iterable = waterfall.entries().iterator();
        List<PlaceStrategy.UnitGroupInfo> successInfoList = new ArrayList<>();

        if (mWaterfallEntryMap == null) {
            mWaterfallEntryMap = new ConcurrentHashMap<>();
        }
        while (iterable.hasNext()) {
            WaterfallEntry waterfallEntry = iterable.next();
            String entryName = waterfallEntry.getEntryName();
            Bid entryBid = waterfallEntry.getBid();
            if (entryBid != null) {
                //Only return Facebook Result
                if (biddingConstants.FACEBOOK_BIDDER.equals(entryName)) {
                    PlaceStrategy.UnitGroupInfo unitGroupInfo = fbMap.get(entryBid.getPlacementId());
                    unitGroupInfo.payload = entryBid.getPayload();
                    unitGroupInfo.ecpm = entryBid.getPrice() / 100;
                    successInfoList.add(unitGroupInfo);
                    mWaterfallEntryMap.put(unitGroupInfo.unitId, waterfallEntry);
                }
            } else {
                mWaterfallEntryMap.put(waterfallEntry.getEntryName(), waterfallEntry);
            }
        }

        if (bidListener != null) {
            bidListener.onBidSuccess(successInfoList);
        }

    }

    protected synchronized void notifyWinnerDisplay(PlaceStrategy.UnitGroupInfo unitGroupInfo) {
        if (mWaterfallEntryMap != null) {
            WaterfallEntry waterfallEntry = mWaterfallEntryMap.get(unitGroupInfo.unitId);
            if (waterfallEntry != null && mAuction != null) {
                if (ATSDK.isNetworkLogDebug()) {
                    Log.i(TAG, "notifyWinnerDisplay:" + waterfallEntry.getEntryName());
                }
                mAuction.notifyDisplayWinner(waterfallEntry);
            }
        }
    }

    /**
     * Waterfall
     */
    class WaterfallImpl implements Waterfall {
        SortedSet<WaterfallEntry> waterfallEntries;

        public WaterfallImpl() {
            waterfallEntries = new TreeSet<>();
        }

        @Override
        public Waterfall createWaterfallCopy() {
            Waterfall copy = new WaterfallImpl();
            for (WaterfallEntry waterfallEntry : waterfallEntries) {
                copy.insert(waterfallEntry);
            }
            return copy;
        }

        @Override
        public void insert(WaterfallEntry waterfallEntry) {
            waterfallEntries.add(waterfallEntry);
        }

        @Override
        public void insert(Bid bid) {
            waterfallEntries.add(new WaterfallEntryImpl(bid, bid.getPrice(), bid.getBidderName()));
        }

        @Override
        public Iterable<WaterfallEntry> entries() {
            return waterfallEntries;
        }

        public WaterfallEntry getFirst() {
            return waterfallEntries.first();
        }

        public int size() {
            return waterfallEntries.size();
        }
    }

    /**
     * WaterfallEntry
     */
    class WaterfallEntryImpl implements WaterfallEntry, Comparable<WaterfallEntryImpl> {
        private Bid mBid;
        private double mCpm;
        private String mBidderName;

        public WaterfallEntryImpl(Bid bid, double cpm, String bidderName) {
            mBid = bid;
            mCpm = cpm;
            mBidderName = bidderName;
        }

        @Override
        public Bid getBid() {
            return mBid;
        }

        @Override
        public double getCPMCents() {
            return mCpm;
        }

        @Override
        public String getEntryName() {
            return mBidderName;
        }

        @Override
        public int compareTo(WaterfallEntryImpl obj) {
            return obj.getCPMCents() > this.getCPMCents() ? 1 : -1;
        }
    }

}
