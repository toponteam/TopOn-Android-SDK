/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.hb;

import android.text.TextUtils;

import com.anythink.core.api.ATBaseAdAdapter;
import com.anythink.core.api.AdError;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.entity.ATHeadBiddingRequest;
import com.anythink.core.common.entity.BaseBiddingResult;
import com.anythink.core.common.entity.BiddingResult;
import com.anythink.core.common.net.OnHttpLoaderListener;
import com.anythink.core.common.utils.CommonSDKUtil;
import com.anythink.core.common.utils.CustomAdapterFactory;
import com.anythink.core.hb.adx.BidRequest;
import com.anythink.core.hb.adx.network.AdxBidRequestInfo;
import com.anythink.core.hb.adx.network.BaseNetworkInfo;
import com.anythink.core.hb.adx.network.MtgBidRequestInfo;
import com.anythink.core.hb.callback.BiddingCallback;
import com.anythink.core.strategy.PlaceStrategy;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ATS2SHeadBiddingHandler extends BaseHeadBiddingHandler {
    final String TAG = "HeadBidding";
    ConcurrentHashMap<String, PlaceStrategy.UnitGroupInfo> requestHBMap = new ConcurrentHashMap<>(); //Key : network_firm_id + network_unit_id
    List<BaseNetworkInfo> baseNetworkInfoList = new ArrayList<>();
    List<PlaceStrategy.UnitGroupInfo> noFilterUnitGroupInfo = new ArrayList<>();
    List<PlaceStrategy.UnitGroupInfo> successUnitGroupList = new ArrayList<>();

    String requestId;
    String placementId;
    String extraInfo;
    String bidRequestUrl;

    boolean isFinishHB;

    BiddingCallback mBiddingCallback;

    public ATS2SHeadBiddingHandler(ATHeadBiddingRequest request) {
        super(request);
        isFinishHB = false;

        init(request);
    }

    @Override
    public void setTestMode(boolean isTest) {
        this.mIsTestMode = isTest;
    }

    @Override
    protected void startBidRequest(final BiddingCallback biddingCallback) {
        mBiddingCallback = biddingCallback;
        if (baseNetworkInfoList.size() == 0) {
            finishCallback(false);
            return;
        }

        final long startBidTime = System.currentTimeMillis();

        new BidRequest(bidRequestUrl, placementId, requestId, baseNetworkInfoList, extraInfo).start(0, new OnHttpLoaderListener() {
            @Override
            public void onLoadStart(int reqCode) {

            }

            @Override
            public void onLoadFinish(int reqCode, Object result) {
                long useTime = System.currentTimeMillis() - startBidTime;
                List<BiddingResult> responseList = parseS2SResponseList(result);
                if (responseList.size() == 0) {
                    finishCallback(false);
                    return;
                }

                for (BiddingResult ATS2SBiddingResult : responseList) {
                    PlaceStrategy.UnitGroupInfo unitGroupInfo = requestHBMap.get(ATS2SBiddingResult.networkFirmId + ATS2SBiddingResult.networkUnitId);

                    ATS2SHeadBiddingHandler.this.processUnitGrouInfo(unitGroupInfo, ATS2SBiddingResult, useTime);
                }

                Collections.sort(successUnitGroupList);
                finishCallback(true);

            }

            @Override
            public void onLoadError(int reqCode, String msg, AdError errorCode) {
                finishCallback(false);
            }

            @Override
            public void onLoadCanceled(int reqCode) {
                finishCallback(false);
            }
        });

    }


    public void init(ATHeadBiddingRequest request) {
        this.requestId = request.requestId;
        this.placementId = request.placementId;

        bidRequestUrl = request.s2sBidUrl;
        List<PlaceStrategy.UnitGroupInfo> hbList = request.hbList;
        int format = request.format;

        this.extraInfo = CommonSDKUtil.createRequestCustomData(request.context, requestId, placementId, format, 0).toString();
        if (this.mIsTestMode) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("Start HeadBidding List", parseHBLogJSONArray(hbList));
            } catch (Exception e) {

            }
            SDKContext.getInstance().printJson(TAG, jsonObject.toString());
        }

        for (PlaceStrategy.UnitGroupInfo unitGroupInfo : hbList) {
            switch (unitGroupInfo.networkType) {
                case 6: //Mintegral

                    ATBaseAdAdapter adapter = CustomAdapterFactory.createAdapter(unitGroupInfo);
                    if (adapter != null) {
                        MtgBidRequestInfo mtgBidRequestInfo = new MtgBidRequestInfo(mRequest.context, String.valueOf(format), unitGroupInfo, adapter);
                        requestHBMap.put(unitGroupInfo.networkType + mtgBidRequestInfo.getRequestInfoId(), unitGroupInfo);
                        baseNetworkInfoList.add(mtgBidRequestInfo);
                    } else {
                        processFailedUnitGrouInfo(unitGroupInfo, "There is no Network SDK.");
                    }

                    break;
                case 66: //Adx
                    AdxBidRequestInfo adxBidRequestInfo = new AdxBidRequestInfo(String.valueOf(format), unitGroupInfo, SDKContext.getInstance().getExcludeMyOfferPkgList());
                    requestHBMap.put(unitGroupInfo.networkType + adxBidRequestInfo.getRequestInfoId(), unitGroupInfo);
                    baseNetworkInfoList.add(adxBidRequestInfo);
                    break;
                default:
                    processFailedUnitGrouInfo(unitGroupInfo, "This network don't support head bidding in current TopOn's version.");
                    break;
            }
        }

    }

    private synchronized void finishCallback(boolean isSuccess) {
        if (!isFinishHB) {
            isFinishHB = true;

            if (!isSuccess) {
                for (PlaceStrategy.UnitGroupInfo unitGroupInfo : requestHBMap.values()) {
                    processFailedUnitGrouInfo(unitGroupInfo, "Bid request error.");
                }
            }


            if (this.mIsTestMode) {
                JSONObject jsonObject = new JSONObject();

                try {
                    jsonObject.put("HeadBidding Success List", parseHBLogJSONArray(successUnitGroupList));
                    jsonObject.put("HeadBidding Fail List", parseHBLogJSONArray(noFilterUnitGroupInfo));
                } catch (Exception e) {

                }

                SDKContext.getInstance().printJson(TAG, jsonObject.toString());
            }

            if (successUnitGroupList.size() > 0) {
                mBiddingCallback.onBiddingSuccess(successUnitGroupList);
            }
            mBiddingCallback.onBiddingFailed(noFilterUnitGroupInfo);
            mBiddingCallback.onBiddingFinished();

        }

    }

    private List<BiddingResult> parseS2SResponseList(Object result) {
        List<BiddingResult> ATS2SBiddingResultList = new ArrayList<>();
        if (result instanceof JSONArray) {
            JSONArray responseArray = (JSONArray) result;
            for (int i = 0; i < responseArray.length(); i++) {
                BiddingResult response = BiddingResult.parseJSONString(responseArray.optString(i));
                ATS2SBiddingResultList.add(response);
            }

        }
        return ATS2SBiddingResultList;
    }

    private JSONArray parseHBLogJSONArray(List<PlaceStrategy.UnitGroupInfo> unitGroupInfos) {
        JSONArray jsonArray = new JSONArray();
        try {
            for (PlaceStrategy.UnitGroupInfo unitGroupInfo : unitGroupInfos) {
                JSONObject itemObject = new JSONObject();
                itemObject.put("network_firm_id", unitGroupInfo.networkType);
                itemObject.put("ad_source_id", unitGroupInfo.unitId);
                itemObject.put("content", unitGroupInfo.content);

                if (unitGroupInfo.ecpm != 0) {
                    itemObject.put("price", unitGroupInfo.ecpm);
                }
                if (!TextUtils.isEmpty(unitGroupInfo.errorMsg)) {
                    itemObject.put("error", unitGroupInfo.errorMsg);
                }
                jsonArray.put(itemObject);
            }
        } catch (Exception e) {

        }

        return jsonArray;
    }

    @Override
    protected void processUnitGrouInfo(PlaceStrategy.UnitGroupInfo unitGroupInfo, BaseBiddingResult biddingResult, long bidUseTime) {
        if (biddingResult instanceof BiddingResult) {
            BiddingResult s2sBiddingResult = ((BiddingResult) biddingResult);
            if (s2sBiddingResult.isSuccess) {
                unitGroupInfo.payload = s2sBiddingResult.token;
                unitGroupInfo.ecpm = s2sBiddingResult.price;
                unitGroupInfo.sortType = 0;
                unitGroupInfo.bidUseTime = bidUseTime;
                successUnitGroupList.add(unitGroupInfo);

                if (s2sBiddingResult.networkFirmId == Const.NETWORK_FIRM.ADX_NETWORK_FIRM_ID) {
                    s2sBiddingResult.outDateTime = s2sBiddingResult.expireTime + System.currentTimeMillis();
                } else {
                    s2sBiddingResult.outDateTime = unitGroupInfo.bidTokenAvailTime + System.currentTimeMillis();
                }

                BiddingCacheManager.getInstance().addCache(unitGroupInfo.unitId, s2sBiddingResult);
                //TODO Save Adx Adsource Bid Info to File
            } else {
                processFailedUnitGrouInfo(unitGroupInfo, "errorCode:[" + s2sBiddingResult.errorCode + "],errorMsg:[" + s2sBiddingResult.errorMsg + "]");
            }
        }
    }

    @Override
    protected void onTimeout() {
        finishCallback(false);
    }

    private void processFailedUnitGrouInfo(PlaceStrategy.UnitGroupInfo unitGroupInfo, String errorMsg) {
        unitGroupInfo.ecpm = 0;
        unitGroupInfo.sortType = -1;
        unitGroupInfo.level = -1;
        unitGroupInfo.errorMsg = errorMsg;
        noFilterUnitGroupInfo.add(unitGroupInfo);
    }
}
