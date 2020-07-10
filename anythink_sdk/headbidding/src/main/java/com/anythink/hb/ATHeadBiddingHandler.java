package com.anythink.hb;

import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.common.HeadBiddingFactory;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.entity.HiBidCache;
import com.anythink.core.common.hb.HeadBiddingCacheManager;
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
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * HeadBidding Handle class
 * p.s: Must keep the code path
 */
public class ATHeadBiddingHandler implements HeadBiddingFactory.IHeadBiddingHandler {

    Context mApplicationContext;
    List<PlaceStrategy.UnitGroupInfo> mNormalUnitInfoList = new ArrayList<>();
    List<PlaceStrategy.UnitGroupInfo> mHBUnitInfoList = new ArrayList<>();

    ConcurrentHashMap<BidRequestInfo, PlaceStrategy.UnitGroupInfo> mRequestUnitObjectMap = new ConcurrentHashMap<>();

    String mUnitId;
    int mFormat;
    String mAdType;
    String mRequestId;

    @Override
    public void setTestMode(boolean isTest) {
        HeaderBiddingAggregator.setDebugMode(isTest);
    }

    @Override
    public void initHbInfo(Context context, String requestId, String unitId, int format, List<PlaceStrategy.UnitGroupInfo> normalUnitInfoList, List<PlaceStrategy.UnitGroupInfo> hbUnitInfoList) {
        mApplicationContext = context.getApplicationContext();

        mFormat = format;
        mRequestId = requestId;
        if (normalUnitInfoList != null) {
            mNormalUnitInfoList.addAll(normalUnitInfoList);
        }

        if (hbUnitInfoList != null) {
            mHBUnitInfoList.addAll(hbUnitInfoList);
        }


        HeaderBiddingAggregator.init(mApplicationContext);

        mUnitId = unitId;
        if (format == 0) { //native
            mAdType = ADType.NATIVE;
        }
        if (format == 1) { //rv
            mAdType = ADType.REWARDED_VIDEO;
        }
        if (format == 2) { //banner
            mAdType = ADType.BANNER;
        }
        if (format == 3) { //interstitial
            mAdType = ADType.INTERSTITIAL;
        }

    }

    @Override
    public void startHeadBiddingRequest(final HeadBiddingFactory.IHeadBiddingCallback callback) {

        HBContext.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                List<BidRequestInfo> bidderReqs = new ArrayList<BidRequestInfo>();

                long timeOut = 0;
                try {

                    for (PlaceStrategy.UnitGroupInfo headBiddingObject : mHBUnitInfoList) {
                        JSONObject contentObject = new JSONObject(headBiddingObject.content);

                        long hbTimeOut = headBiddingObject.hbTimeout;
                        if (hbTimeOut > timeOut) {
                            timeOut = hbTimeOut;
                        }

                        if (headBiddingObject.networkType == 6) { //Mintegral
                            String appid = contentObject.optString("appid");
                            String unitid = contentObject.optString("unitid");
                            String appkey = contentObject.optString("appkey");
                            String placementId = contentObject.optString("placement_id");

                            BidRequestInfo mtg = new BidRequestInfo();
                            mtg.put(BidRequestInfo.KEY_APP_ID, appid);
                            mtg.put(BidRequestInfo.KEY_APP_KEY, appkey);
                            mtg.put(BidRequestInfo.KEY_UNIT_PLACEMENT_ID, placementId);
                            mtg.put(BidRequestInfo.KEY_PLACEMENT_ID, unitid);
                            mtg.put(BidRequestInfo.KEY_CUSTOM_INFO, CommonSDKUtil.createRequestCustomData(mApplicationContext, mRequestId, mUnitId, mFormat, headBiddingObject).toString());
                            mtg.put(BidRequestInfo.KEY_BIDDER_CLASS, MtgBidder.class);
                            if (TextUtils.equals(mAdType, ADType.BANNER)) { //Banner
                                String size = contentObject.optString("size");
                                mtg.put(BidRequestInfo.KEY_BANNER_SIZE, size);
                            }

                            bidderReqs.add(mtg);

                            mRequestUnitObjectMap.put(mtg, headBiddingObject);
                        }

                        if (headBiddingObject.networkType == 1) { //Facebook
                            String unitid = contentObject.optString("unit_id");
                            String appid = contentObject.optString("app_id");
                            BidRequestInfo fb = new BidRequestInfo();
                            fb.put(BidRequestInfo.KEY_APP_ID, appid);
                            fb.put(BidRequestInfo.KEY_PLACEMENT_ID, unitid);
                            fb.put(BidRequestInfo.KEY_BIDDER_CLASS, FacebookBidder.class);
                            fb.put(BidRequestInfo.KEY_PLATFORM_ID, appid);
                            if (TextUtils.equals(mAdType, ADType.BANNER)) { //Banner
                                String size = contentObject.optString("size");
                                fb.put(BidRequestInfo.KEY_BANNER_SIZE, size);
                            }
//                            fb.put("isTest", true);
                            bidderReqs.add(fb);

                            mRequestUnitObjectMap.put(fb, headBiddingObject);
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                //If no bid list, it will return the normal list
                if (bidderReqs.size() == 0) {
                    if (callback != null) {
                        List<PlaceStrategy.UnitGroupInfo> resultList = new ArrayList<>();
                        resultList.addAll(mNormalUnitInfoList);
                        callback.onResultCallback(resultList, null);
                    }
                    return;
                }


                List<HeaderBiddingTransaction> transactionList = new ArrayList<HeaderBiddingTransaction>();

                try {
                    HeaderBiddingTransaction transaction = HeaderBiddingAggregator.requestBid(bidderReqs,
                            mUnitId, mAdType, timeOut, new BidRequestCallback() {
                                @Override
                                public void onBidRequestCallback(String unitId, AuctionResult auctionResult) {
                                    handleResult(auctionResult, callback);
                                }
                            });
                    transactionList.add(transaction);
                } catch (Exception e) {
                    e.printStackTrace();
                    if (callback != null) {
                        callback.onResultCallback(mNormalUnitInfoList, null);
                    }
                }
            }
        });
    }

    private void handleResult(AuctionResult auctionResult, HeadBiddingFactory.IHeadBiddingCallback callback) {
        List<PlaceStrategy.UnitGroupInfo> successObjectList = new ArrayList<>();
        List<PlaceStrategy.UnitGroupInfo> failObjectList = new ArrayList<>();

        BiddingResponse winner = auctionResult.getWinner();

        if (auctionResult.getWinner() != null) {

            BidRequestInfo bidRequestInfo = winner.getBidRequestInfo();
            PlaceStrategy.UnitGroupInfo winnerUnitInfo = mRequestUnitObjectMap.get(bidRequestInfo);
            if (winnerUnitInfo != null) {
                try {
                    winnerUnitInfo.ecpm = winner.getBiddingPriceUSD();
                    winnerUnitInfo.payload = winner.getPayload().toString();
                    winnerUnitInfo.sortType = 0;//normal bidrequest

                } catch (Exception e) {
                    e.printStackTrace();
                }

                String adSourceId = winnerUnitInfo.unitId;
                /**Add Bid Cache**/
                HiBidCache hiBidCache = new HiBidCache();
                hiBidCache.payLoad = winner.getPayload().toString();
                hiBidCache.price = winner.getBiddingPriceUSD();
                hiBidCache.outDateTime = winnerUnitInfo.getBidTokenAvailTime() + System.currentTimeMillis();
                //save hb cache
                HeadBiddingCacheManager.getInstance().addCache(adSourceId, hiBidCache);

                successObjectList.add(winnerUnitInfo);
            }

            LogUtil.i("bidding", "winner bidding succsess......：" + winner.getPayload());
        } else {
            LogUtil.i("bidding", "bidding fail......");
        }

        List<BiddingResponse> otherBidders = auctionResult.getOtherBidders();

        if (otherBidders != null) {
            for (BiddingResponse biddingResponse : otherBidders) {
                BidRequestInfo otherBidRequestInfo = biddingResponse.getBidRequestInfo();
                PlaceStrategy.UnitGroupInfo otherUnitInfo = mRequestUnitObjectMap.get(otherBidRequestInfo);

                if (biddingResponse.getBiddingPriceUSD() == 0) {

                    String adsourceId = otherUnitInfo.unitId;
                    /**HB Cache exist？**/
                    HiBidCache bidCache = HeadBiddingCacheManager.getInstance().getCache(adsourceId);
                    if (bidCache != null) {
                        try {
                            otherUnitInfo.ecpm = bidCache.price;
                            otherUnitInfo.payload = bidCache.payLoad;
                            otherUnitInfo.sortType = 2; //use cache token

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        successObjectList.add(otherUnitInfo);
                        LogUtil.i("bidding", "use cache......payload:" + biddingResponse.getErrorMessage());
                    } else {
                        try {
                            otherUnitInfo.setErrorMsg(biddingResponse.getErrorMessage());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        NetworkLogUtil.headbidingLog(Const.LOGKEY.FAIL, mUnitId, CommonSDKUtil.getFormatString(String.valueOf(mFormat)), otherUnitInfo);

                        failObjectList.add(otherUnitInfo);
                        LogUtil.i("bidding", "bidding fail......payload:" + biddingResponse.getErrorMessage());
                    }

                } else {
                    LogUtil.i("bidding", "other bidding succsess......：" + biddingResponse.getPayload());
                    try {
//                        otherObject.put(unitGroup_bidtype, 1);
                        otherUnitInfo.ecpm = biddingResponse.getBiddingPriceUSD();
                        otherUnitInfo.payload = biddingResponse.getPayload().toString();
                        otherUnitInfo.sortType = 0;

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    successObjectList.add(otherUnitInfo);

                    String adSourceId = otherUnitInfo.unitId;
                    /**Add HB Cache**/
                    HiBidCache hiBidCache = new HiBidCache();
                    hiBidCache.payLoad = winner.getPayload().toString();
                    hiBidCache.price = winner.getBiddingPriceUSD();
                    hiBidCache.outDateTime = otherUnitInfo.getBidTokenAvailTime() + System.currentTimeMillis();
                    HeadBiddingCacheManager.getInstance().addCache(adSourceId, hiBidCache);

                }
            }
        }

        for (PlaceStrategy.UnitGroupInfo successObject : successObjectList) {
            if (mNormalUnitInfoList.size() == 0) {
                NetworkLogUtil.headbidingLog(Const.LOGKEY.SUCCESS, mUnitId, CommonSDKUtil.getFormatString(String.valueOf(mFormat)), successObject);
                mNormalUnitInfoList.add(successObject);
                continue;
            }

            for (int i = 0; i < mNormalUnitInfoList.size(); i++) {
                PlaceStrategy.UnitGroupInfo normalObject = mNormalUnitInfoList.get(i);
                try {
                    if (successObject.ecpm >= normalObject.ecpm) {
                        NetworkLogUtil.headbidingLog(Const.LOGKEY.SUCCESS, mUnitId, CommonSDKUtil.getFormatString(String.valueOf(mFormat)), successObject);
                        mNormalUnitInfoList.add(i, successObject);
                        break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (mNormalUnitInfoList.indexOf(successObject) < 0) {
                NetworkLogUtil.headbidingLog(Const.LOGKEY.SUCCESS, mUnitId, CommonSDKUtil.getFormatString(String.valueOf(mFormat)), successObject);
                mNormalUnitInfoList.add(successObject);
            }
        }

        List<PlaceStrategy.UnitGroupInfo> finalResultList = new ArrayList<>();
        for (int i = 0; i < mNormalUnitInfoList.size(); i++) {
            PlaceStrategy.UnitGroupInfo unitGroupInfo = mNormalUnitInfoList.get(i);
//            unitGroupInfo.level = i;
            finalResultList.add(unitGroupInfo);
        }

        if (callback != null) {
            callback.onResultCallback(finalResultList, failObjectList);
        }

    }
}
