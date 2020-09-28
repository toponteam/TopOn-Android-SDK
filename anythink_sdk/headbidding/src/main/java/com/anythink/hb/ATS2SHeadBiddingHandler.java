package com.anythink.hb;

import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.AdError;
import com.anythink.core.common.HeadBiddingFactory;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.entity.S2SHBResponse;
import com.anythink.core.common.hb.HBS2SCacheManager;
import com.anythink.core.common.net.OnHttpLoaderListener;
import com.anythink.core.common.utils.CommonSDKUtil;
import com.anythink.core.common.utils.task.TaskManager;
import com.anythink.core.strategy.PlaceStrategy;
import com.anythink.hb.adx.BidRequest;
import com.anythink.hb.adx.network.BaseNetworkInfo;
import com.anythink.hb.adx.network.MtgBidRequestInfo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ATS2SHeadBiddingHandler implements HeadBiddingFactory.IHeadBiddingS2SHandler {
    final String TAG = "HeadBidding";
    ConcurrentHashMap<String, PlaceStrategy.UnitGroupInfo> requestHBMap = new ConcurrentHashMap<>(); //Key : network_firm_id + network_unit_id
    List<BaseNetworkInfo> baseNetworkInfoList = new ArrayList<>();
    List<PlaceStrategy.UnitGroupInfo> noFilterUnitGroupInfo = new ArrayList<>();
    List<PlaceStrategy.UnitGroupInfo> successUnitGroupList = new ArrayList<>();

    String requestId;
    String placementId;
    String extraInfo;


    boolean isFinishHB;
    long hbTimeOut = 10 * 1000L;

    public ATS2SHeadBiddingHandler() {
        isFinishHB = false;
    }

    @Override
    public void setTestMode(boolean isTest) {
        HeaderBiddingAggregator.setDebugMode(isTest);
    }

    public void initS2SHbInfo(Context context, String requestId, String placementId, int format, long hbBidTimeout, List<PlaceStrategy.UnitGroupInfo> hbUnitInfoList) {
        this.requestId = requestId;
        this.placementId = placementId;
        hbTimeOut = hbBidTimeout;

        this.extraInfo = CommonSDKUtil.createRequestCustomData(context, requestId, placementId, format, 0).toString();

        if (HeaderBiddingAggregator.isDebugMode()) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("Start HeadBidding List", parseHBLogJSONArray(hbUnitInfoList));
            } catch (Exception e) {

            }
            SDKContext.getInstance().printJson(TAG, jsonObject.toString());
        }

        for (PlaceStrategy.UnitGroupInfo unitGroupInfo : hbUnitInfoList) {
            switch (unitGroupInfo.networkType) {
                case 6: //Mintegral
                    MtgBidRequestInfo mtgBidRequestInfo = new MtgBidRequestInfo(context, String.valueOf(format), unitGroupInfo);
                    if (mtgBidRequestInfo.checkNetworkSDK()) {
                        requestHBMap.put(unitGroupInfo.networkType + mtgBidRequestInfo.getUnitId(), unitGroupInfo);
                        baseNetworkInfoList.add(mtgBidRequestInfo);
                    } else {
                        unitGroupInfo.ecpm = 0;
                        unitGroupInfo.setErrorMsg("There is no Network SDK.");
                        noFilterUnitGroupInfo.add(unitGroupInfo);
                    }

                    break;
                default:
                    String errorMsg = "This network don't support head bidding in current TopOn's version.";
                    unitGroupInfo.setErrorMsg(errorMsg);
                    unitGroupInfo.ecpm = 0;
                    unitGroupInfo.sortType = -1;
                    unitGroupInfo.level = -1;
                    noFilterUnitGroupInfo.add(unitGroupInfo);
                    break;
            }
        }

    }

    public void startS2SHbInfo(String bidRequestUrl, final HeadBiddingFactory.IHeadBiddingCallback callback) {
        if (baseNetworkInfoList.size() == 0) {
            finishCallback(callback, false);
            return;
        }

        TaskManager.getInstance().run_proxyDelayed(new Runnable() {
            @Override
            public void run() {
                finishCallback(callback, false);
            }
        }, hbTimeOut);

        final long startBidTime = System.currentTimeMillis();

        new BidRequest(bidRequestUrl, placementId, requestId, baseNetworkInfoList, extraInfo).start(0, new OnHttpLoaderListener() {
            @Override
            public void onLoadStart(int reqCode) {

            }

            @Override
            public void onLoadFinish(int reqCode, Object result) {
                long useTime = System.currentTimeMillis() - startBidTime;
                List<S2SHBResponse> responseList = parseS2SResponseList(result);
                if (responseList.size() == 0) {
                    finishCallback(callback, false);
                    return;
                }

                for (S2SHBResponse s2SHBResponse : responseList) {
                    PlaceStrategy.UnitGroupInfo unitGroupInfo = requestHBMap.get(s2SHBResponse.networkFirmId + s2SHBResponse.networkUnitId);
                    if (s2SHBResponse.isSuccess == 1) {
                        unitGroupInfo.payload = s2SHBResponse.bidId;
                        unitGroupInfo.ecpm = s2SHBResponse.price;
                        unitGroupInfo.sortType = 0;
                        unitGroupInfo.bidUseTime = useTime;
                        successUnitGroupList.add(unitGroupInfo);

                        s2SHBResponse.outDateTime = unitGroupInfo.bidTokenAvailTime + System.currentTimeMillis();
                        HBS2SCacheManager.getInstance().addCache(unitGroupInfo.unitId, s2SHBResponse);
                    } else {
                        unitGroupInfo.ecpm = 0;
                        unitGroupInfo.sortType = -1;
                        unitGroupInfo.level = -1;
                        unitGroupInfo.errorMsg = "errorCode:[" + s2SHBResponse.errorCode + "],errorMsg:[" + s2SHBResponse.errorMsg + "]";
                        noFilterUnitGroupInfo.add(unitGroupInfo);
                    }
                }

                Collections.sort(successUnitGroupList);
                finishCallback(callback, true);

            }


            @Override
            public void onLoadError(int reqCode, String msg, AdError errorCode) {
                finishCallback(callback, false);
            }

            @Override
            public void onLoadCanceled(int reqCode) {
                finishCallback(callback, false);
            }
        });

    }

    private synchronized void finishCallback(HeadBiddingFactory.IHeadBiddingCallback callback, boolean isSuccess) {
        if (!isFinishHB) {
            isFinishHB = true;
            if (!isSuccess) {
                for(PlaceStrategy.UnitGroupInfo unitGroupInfo: requestHBMap.values()) {
                    unitGroupInfo.sortType = -1;
                    unitGroupInfo.level = -1;
                    unitGroupInfo.errorMsg = "Bid request error.";
                }
                noFilterUnitGroupInfo.addAll(requestHBMap.values());
            }



            if (HeaderBiddingAggregator.isDebugMode()) {
                JSONObject jsonObject = new JSONObject();

                try {
                    jsonObject.put("HeadBidding Success List", parseHBLogJSONArray(successUnitGroupList));
                    jsonObject.put("HeadBidding Fail List", parseHBLogJSONArray(noFilterUnitGroupInfo));
                } catch (Exception e) {

                }

                SDKContext.getInstance().printJson(TAG, jsonObject.toString());
            }

            callback.onSuccess(requestId, successUnitGroupList);
            callback.onFailed(requestId, noFilterUnitGroupInfo);
            callback.onFinished(requestId);

        }

    }

    private List<S2SHBResponse> parseS2SResponseList(Object result) {
        List<S2SHBResponse> s2SHBResponseList = new ArrayList<>();
        if (result instanceof JSONArray) {
            JSONArray responseArray = (JSONArray) result;
            for (int i = 0; i < responseArray.length(); i++) {
                JSONObject itemObject = responseArray.optJSONObject(i);
                S2SHBResponse response = new S2SHBResponse();
                response.bidId = itemObject.optString("bid_id");
                response.curreny = itemObject.optString("cur");
                response.price = itemObject.optDouble("price");
                response.winNoticeUrl = itemObject.optString("nurl");
                response.lossNoticeUrl = itemObject.optString("lurl");
                response.networkUnitId = itemObject.optString("unit_id");
                response.networkFirmId = itemObject.optString("nw_firm_id");
                response.isSuccess = itemObject.optInt("is_success");
                response.errorCode = itemObject.optInt("err_code");
                response.errorMsg = itemObject.optString("err_msg");

                s2SHBResponseList.add(response);
            }

        }
        return s2SHBResponseList;
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
}
