package com.anythink.hb;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.anythink.core.common.HeadBiddingFactory;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.utils.CommonSDKUtil;
import com.anythink.core.common.utils.NetworkLogUtil;
import com.anythink.core.strategy.PlaceStrategy;
import com.anythink.hb.bidder.FacebookBidder;
import com.anythink.hb.bidder.MtgBidder;
import com.anythink.hb.callback.BidRequestCallback;
import com.anythink.hb.constants.ADType;
import com.anythink.hb.data.AuctionResult;
import com.anythink.hb.data.BidRequestInfo;
import com.anythink.hb.data.BiddingResponse;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * HeadBidding Handle class
 * p.s: Must keep the code path
 */
public class ATHeadBiddingHandler implements HeadBiddingFactory.IHeadBiddingHandler {

    Context mApplicationContext;
    List<PlaceStrategy.UnitGroupInfo> mHBUnitInfoList = new ArrayList<>();
    List<PlaceStrategy.UnitGroupInfo> mFailedUnitInfoList;

    ConcurrentHashMap<BidRequestInfo, PlaceStrategy.UnitGroupInfo> mRequestUnitObjectMap = new ConcurrentHashMap<>();

    String mPlacementId;
    int mFormat;
    String mAdType;
    String mRequestId;
    long mHBWaitingToRequestTime;
    long mHBBidTimeout;

    @Override
    public void setTestMode(boolean isTest) {
        HeaderBiddingAggregator.setDebugMode(isTest);
    }

    @Override
    public void initHbInfo(Context context, String requestId, String placementId, int format, long hbWaitingToReqeustTime, long hbBidTimeout, List<PlaceStrategy.UnitGroupInfo> hbUnitInfoList) {
        mApplicationContext = context.getApplicationContext();

        mFormat = format;
        mRequestId = requestId;
        mHBWaitingToRequestTime = hbWaitingToReqeustTime;
        mHBBidTimeout = hbBidTimeout;

        if (hbUnitInfoList != null) {
            mHBUnitInfoList.addAll(hbUnitInfoList);
        }


        HeaderBiddingAggregator.init(mApplicationContext);

        mPlacementId = placementId;
        switch (format) {
            case 0:  //native
                mAdType = ADType.NATIVE;
                break;
            case 1:  //rv
                mAdType = ADType.REWARDED_VIDEO;
                break;
            case 2:  //banner
                mAdType = ADType.BANNER;
                break;
            case 3:  //interstitial
                mAdType = ADType.INTERSTITIAL;
                break;
        }

    }

    @Override
    public void startHeadBiddingRequest(final HeadBiddingFactory.IHeadBiddingCallback callback) {

        HBContext.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                List<BidRequestInfo> bidderReqs = new ArrayList<>();

                long waitingToRequestTime = 0;
                long biddingTimeOut = 0;

                if (mHBWaitingToRequestTime > waitingToRequestTime) {
                    waitingToRequestTime = mHBWaitingToRequestTime;
                }

                if (mHBBidTimeout > biddingTimeOut) {
                    biddingTimeOut = mHBBidTimeout;
                }

                LogUtil.i("HeadBidding", "hbStartTime: " + waitingToRequestTime + ", hbBidTimeout: " + biddingTimeOut);

                try {

                    for (PlaceStrategy.UnitGroupInfo headBiddingObject : mHBUnitInfoList) {
                        JSONObject contentObject = new JSONObject(headBiddingObject.content);

                        String adsourceId = headBiddingObject.unitId;
                        long bidTokenAvailTime = headBiddingObject.getBidTokenAvailTime();

                        BidRequestInfo requestInfo = new BidRequestInfo();
                        requestInfo.put(BidRequestInfo.KEY_ADSOURCE_ID, adsourceId);
                        requestInfo.put(BidRequestInfo.KEY_BID_TOKEN_AVAIL_TIME, bidTokenAvailTime);


                        if (headBiddingObject.networkType == 6) { //Mintegral
                            String appid = contentObject.optString("appid");
                            String unitid = contentObject.optString("unitid");
                            String appkey = contentObject.optString("appkey");
                            String placementId = contentObject.optString("placement_id");

                            requestInfo.put(BidRequestInfo.KEY_APP_ID, appid);
                            requestInfo.put(BidRequestInfo.KEY_APP_KEY, appkey);
                            requestInfo.put(BidRequestInfo.KEY_UNIT_PLACEMENT_ID, placementId);
                            requestInfo.put(BidRequestInfo.KEY_PLACEMENT_ID, unitid);
                            requestInfo.put(BidRequestInfo.KEY_CUSTOM_INFO, CommonSDKUtil.createRequestCustomData(mApplicationContext, mRequestId, mPlacementId, mFormat, 0).toString());
                            requestInfo.put(BidRequestInfo.KEY_BIDDER_CLASS, MtgBidder.class);
                            if (TextUtils.equals(mAdType, ADType.BANNER)) { //Banner
                                String size = contentObject.optString("size");
                                requestInfo.put(BidRequestInfo.KEY_BANNER_SIZE, size);
                            }

                            bidderReqs.add(requestInfo);

                            mRequestUnitObjectMap.put(requestInfo, headBiddingObject);

                        } else if (headBiddingObject.networkType == 1) { //Facebook
                            String unitid = contentObject.optString("unit_id");
                            String appid = contentObject.optString("app_id");

                            requestInfo.put(BidRequestInfo.KEY_APP_ID, appid);
                            requestInfo.put(BidRequestInfo.KEY_PLACEMENT_ID, unitid);
                            requestInfo.put(BidRequestInfo.KEY_BIDDER_CLASS, FacebookBidder.class);
                            requestInfo.put(BidRequestInfo.KEY_PLATFORM_ID, appid);
                            if (TextUtils.equals(mAdType, ADType.BANNER)) { //Banner
                                String size = contentObject.optString("size");
                                requestInfo.put(BidRequestInfo.KEY_BANNER_SIZE, size);
                            }
//                            fb.put("isTest", true);
                            bidderReqs.add(requestInfo);

                            mRequestUnitObjectMap.put(requestInfo, headBiddingObject);
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                //If no bid list, it will return the normal list
                if (bidderReqs.size() == 0) {
                    if (callback != null) {
                        callback.onFailed(mRequestId, mHBUnitInfoList);
                        callback.onFinished(mRequestId);
                    }
                    return;
                }


                List<HeaderBiddingTransaction> transactionList = new ArrayList<>();

                try {
                    HeaderBiddingTransaction transaction = HeaderBiddingAggregator.requestBid(bidderReqs, mRequestId,
                            mPlacementId, mAdType, waitingToRequestTime, biddingTimeOut, new BidRequestCallback() {
                                @Override
                                public void onError(String placementId, BidRequestInfo bidRequestInfo, Throwable e) {
                                    Log.e("HeadBidding", "onError: " + e.getMessage());
                                    if (mFailedUnitInfoList == null) {
                                        mFailedUnitInfoList = new ArrayList<>();
                                    }

                                    PlaceStrategy.UnitGroupInfo unitGroupInfo = mRequestUnitObjectMap.get(bidRequestInfo);
                                    if (unitGroupInfo != null) {
                                        unitGroupInfo.setErrorMsg(e.getMessage());
                                        mFailedUnitInfoList.add(unitGroupInfo);
                                    }
                                }

                                @Override
                                public void onBidResultWhenWaitingTimeout(String placementId, AuctionResult auctionResult) {
                                    handleResult(auctionResult, callback);
                                }

                                @Override
                                public void onBidEachResult(String placementId, AuctionResult auctionResult) {
                                    handleResult(auctionResult, callback);
                                }

                                @Override
                                public void onBidRequestFinished(String placementId, AuctionResult auctionResult) {
                                    handleResult(auctionResult, callback);

                                    if (callback != null) {
                                        callback.onFinished(mRequestId);
                                    }
                                }
                            });
                    transactionList.add(transaction);
                } catch (Exception e) {
                    e.printStackTrace();
                    if (callback != null) {
                        callback.onFailed(mRequestId, mHBUnitInfoList);
                        callback.onFinished(mRequestId);
                    }
                }
            }
        });
    }

    private void handleResult(AuctionResult auctionResult, HeadBiddingFactory.IHeadBiddingCallback callback) {
        List<PlaceStrategy.UnitGroupInfo> successObjectList = new ArrayList<>();
        List<PlaceStrategy.UnitGroupInfo> failObjectList = new ArrayList<>();

        if (mFailedUnitInfoList != null && mFailedUnitInfoList.size() > 0) {
            failObjectList.addAll(mFailedUnitInfoList);
            mFailedUnitInfoList.clear();
        }

        List<BiddingResponse> successBidders = auctionResult.getSuccessBidders();
        List<BiddingResponse> failedBidders = auctionResult.getFailedBidders();

        int successSize = successBidders != null ? successBidders.size() : 0;
        int failedSize = failedBidders != null ? failedBidders.size() : 0;

        BiddingResponse biddingResponse;
        BidRequestInfo bidRequestInfo;
        PlaceStrategy.UnitGroupInfo unitGroupInfo;
        for (int i = 0; i < successSize; i++) {
            biddingResponse = successBidders.get(i);

            if (biddingResponse != null) {
                bidRequestInfo = biddingResponse.getBidRequestInfo();

                unitGroupInfo = mRequestUnitObjectMap.get(bidRequestInfo);

                if (unitGroupInfo != null) {
                    try {
                        unitGroupInfo.bidEndTime = biddingResponse.getBiddingEndTime();//bid end time
                        unitGroupInfo.bidUseTime = biddingResponse.getBiddingEndTime() - biddingResponse.getBiddingStartTime();//bid use time
                        unitGroupInfo.ecpm = biddingResponse.getBiddingPriceUSD();
                        unitGroupInfo.payload = biddingResponse.getPayload().toString();
                        unitGroupInfo.sortType = 0;//normal bidrequest

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    NetworkLogUtil.headbidingLog(Const.LOGKEY.SUCCESS, mPlacementId, CommonSDKUtil.getFormatString(String.valueOf(mFormat)), unitGroupInfo);

                    successObjectList.add(unitGroupInfo);
                }
            }
        }
        for (int i = 0; i < failedSize; i++) {
            biddingResponse = failedBidders.get(i);

            if (biddingResponse != null && biddingResponse.getBiddingPriceUSD() == 0) {
                bidRequestInfo = biddingResponse.getBidRequestInfo();

                unitGroupInfo = mRequestUnitObjectMap.get(bidRequestInfo);

                if (unitGroupInfo != null) {
                    try {
                        unitGroupInfo.bidEndTime = biddingResponse.getBiddingEndTime();//bid end time
                        unitGroupInfo.bidUseTime = biddingResponse.getBiddingEndTime() - biddingResponse.getBiddingStartTime();//bid use time
                        unitGroupInfo.ecpm = 0;
                        unitGroupInfo.level = -1;
                        unitGroupInfo.setErrorMsg(biddingResponse.getErrorMessage());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    NetworkLogUtil.headbidingLog(Const.LOGKEY.FAIL, mPlacementId, CommonSDKUtil.getFormatString(String.valueOf(mFormat)), unitGroupInfo);

                    failObjectList.add(unitGroupInfo);
                }
            }
        }

        if (callback != null) {
            if (successObjectList.size() > 0) {
                Collections.sort(successObjectList);
                callback.onSuccess(mRequestId, successObjectList);
            }
            if (failObjectList.size() > 0) {
                callback.onFailed(mRequestId, failObjectList);
            }
        }
    }

}
