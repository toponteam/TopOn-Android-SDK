/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.hb;

import android.os.SystemClock;
import android.util.Log;

import com.anythink.core.api.ATBaseAdAdapter;
import com.anythink.core.api.MediationBidManager;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.entity.ATHeadBiddingRequest;
import com.anythink.core.common.entity.BaseBiddingResult;
import com.anythink.core.common.entity.BiddingResult;
import com.anythink.core.common.utils.CustomAdapterFactory;
import com.anythink.core.hb.callback.BiddingCallback;
import com.anythink.core.strategy.PlaceStrategy;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FBInHouseBiddingHandler extends BaseHeadBiddingHandler {
    private String TAG = "IH Bidding";
    private long mStartBiddingTime;
    private BiddingCallback mCallback;
    boolean isFinish = false;

    public FBInHouseBiddingHandler(ATHeadBiddingRequest request) {
        super(request);
    }

    @Override
    protected void startBidRequest(final BiddingCallback biddingCallback) {
        mCallback = biddingCallback;
        isFinish = false;
        mStartBiddingTime = SystemClock.elapsedRealtime();

        List<PlaceStrategy.UnitGroupInfo> hbList = mRequest.hbList;

        if (this.mIsTestMode) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("Start IH Bidding List", parseHBLogJSONArray(hbList));
            } catch (Exception e) {

            }
            SDKContext.getInstance().printJson(TAG, jsonObject.toString());
        }

        if (BiddingCacheManager.getInstance().getMediationBidManager() == null) {
            for (PlaceStrategy.UnitGroupInfo unitGroupInfo : hbList) {
                if (unitGroupInfo.networkType != 1) { //Only for facebook
                    continue;
                }
                ATBaseAdAdapter atBaseAdAdapter = CustomAdapterFactory.createAdapter(unitGroupInfo);
                if (atBaseAdAdapter != null) {
                    MediationBidManager mediationBidManager = atBaseAdAdapter.getBidManager();
                    if (mediationBidManager != null) {
                        BiddingCacheManager.getInstance().setMediationBidManager(mediationBidManager);
                    }
                }
            }
        }

        MediationBidManager mediationBidManager = BiddingCacheManager.getInstance().getMediationBidManager();
        if (mediationBidManager == null) {
            Log.i(TAG, "No BidManager.");
            handleResult(null);
            return;
        }

        mediationBidManager.setBidRequestUrl(Const.API.URL_FACEBOOK_INHOUSE_DOMAIN);
        mediationBidManager.startBid(mRequest.context, mRequest.format, mRequest.placementId, hbList, mRequest.normalList, new MediationBidManager.BidListener() {
            @Override
            public void onBidSuccess(List<PlaceStrategy.UnitGroupInfo> bidUnitGroupInfos) {
                handleResult(bidUnitGroupInfos);
            }

            @Override
            public void onBidFail(String errorMsg) {

            }
        }, mRequest.fbInHouseTimeOut);

    }

    private synchronized void handleResult(List<PlaceStrategy.UnitGroupInfo> bidUnitGroupInfos) {
        if (isFinish) {
            return;
        }

        if (bidUnitGroupInfos == null) {
            bidUnitGroupInfos = new ArrayList<>();
        }

        long delayTime = SystemClock.elapsedRealtime() - mStartBiddingTime;
        List<PlaceStrategy.UnitGroupInfo> failList = new ArrayList<>();
        for (PlaceStrategy.UnitGroupInfo unitGroupInfo : mRequest.hbList) {
            boolean isSuccess = false;
            for (PlaceStrategy.UnitGroupInfo bidUnitGroupInfo : bidUnitGroupInfos) {
                if (unitGroupInfo.unitId.equals(bidUnitGroupInfo.unitId)) {
                    bidUnitGroupInfo.bidUseTime = delayTime;
                    bidUnitGroupInfo.sortType = 0;
                    addBidResult(bidUnitGroupInfo);
                    isSuccess = true;
                    break;
                }
            }
            if (!isSuccess) {
                processFailedUnitGrouInfo(unitGroupInfo, "No Bid Info.");
                failList.add(unitGroupInfo);
            }
        }

        if (this.mIsTestMode) {
            JSONObject jsonObject = new JSONObject();

            try {
                jsonObject.put("IH Bidding Success List", parseHBLogJSONArray(bidUnitGroupInfos));
                jsonObject.put("IH Bidding Fail List", parseHBLogJSONArray(failList));
            } catch (Exception e) {

            }

            SDKContext.getInstance().printJson(TAG, jsonObject.toString());
        }

        if (mCallback != null) {
            if (bidUnitGroupInfos.size() > 0) {
                mCallback.onBiddingSuccess(bidUnitGroupInfos);
            }

            mCallback.onBiddingFailed(failList);
            mCallback.onBiddingFinished();
        }

        isFinish = true;

    }

    @Override
    protected void processUnitGrouInfo(PlaceStrategy.UnitGroupInfo unitGroupInfo, BaseBiddingResult biddingResult, long bidUseTime) {

    }

    @Override
    protected void onTimeout() {
        handleResult(null);
    }

    private void processFailedUnitGrouInfo(PlaceStrategy.UnitGroupInfo unitGroupInfo, String errorMsg) {
        unitGroupInfo.ecpm = 0;
        unitGroupInfo.sortType = -1;
        unitGroupInfo.level = -1;
        unitGroupInfo.errorMsg = errorMsg;
    }

    private void addBidResult(PlaceStrategy.UnitGroupInfo unitGroupInfo) {
        BiddingResult inhouseBiddingResult = new BiddingResult(true, unitGroupInfo.ecpm, unitGroupInfo.payload, "", "", "");
        inhouseBiddingResult.outDateTime = unitGroupInfo.getBidTokenAvailTime() + System.currentTimeMillis();
        inhouseBiddingResult.expireTime = unitGroupInfo.getBidTokenAvailTime();

        //save hb cache
        BiddingCacheManager.getInstance().addCache(unitGroupInfo.unitId, inhouseBiddingResult);
    }
}
