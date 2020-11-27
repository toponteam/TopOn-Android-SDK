/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.common;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.ATBaseAdAdapter;
import com.anythink.core.api.ATCustomLoadListener;
import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.ATSDK;
import com.anythink.core.api.AdError;
import com.anythink.core.api.BaseAd;
import com.anythink.core.api.ErrorCode;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.base.UploadDataLevelManager;
import com.anythink.core.common.entity.AdCacheInfo;
import com.anythink.core.common.entity.AdTrackingInfo;
import com.anythink.core.common.entity.BiddingResult;
import com.anythink.core.common.entity.TemplateStrategy;
import com.anythink.core.common.entity.UnitgroupCacheInfo;
import com.anythink.core.hb.BiddingCacheManager;
import com.anythink.core.common.net.TrackingV2Loader;
import com.anythink.core.common.track.AdTrackingManager;
import com.anythink.core.common.track.AgentEventManager;
import com.anythink.core.common.utils.CommonDeviceUtil;
import com.anythink.core.common.utils.CommonLogUtil;
import com.anythink.core.common.utils.CommonSDKUtil;
import com.anythink.core.common.utils.CustomAdapterFactory;
import com.anythink.core.common.utils.TrackingInfoUtil;
import com.anythink.core.common.utils.task.TaskManager;
import com.anythink.core.strategy.AppStrategy;
import com.anythink.core.strategy.PlaceStrategy;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class CommonMediationManager {
    private final String TAG = getClass().getSimpleName();
    protected Context mApplcationContext;
    protected WeakReference<Context> mActivityRef;

    protected String mUserId = "";
    protected String mCustomData = "";

    boolean isFinishBidding = false;
    protected boolean hasReturnResult = false; //Result return
    boolean hasLongTimeout = false; //Long timeout
    boolean requestHasShow = false;

    boolean isResultSuccess = false; //1:Success, 2:Fail

    private List<PlaceStrategy.UnitGroupInfo> startLoadUgList; //Current Normal RequestList

    List<PlaceStrategy.UnitGroupInfo> requestWaitingPool;
    List<PlaceStrategy.UnitGroupInfo> requestingPool;

    List<PlaceStrategy.UnitGroupInfo> hbRequestWaitingPool;
    List<PlaceStrategy.UnitGroupInfo> hbRequestingPool;

    protected PlaceStrategy mStrategy;
    protected String mRequestId;
    protected String mPlacementId;

    protected boolean mIsRefresh; //If loading for refresh
    String mUnitGroupList; //Only for agent or tracking

    AdError mLoadError;

    long mStartLoadTime;

    int requestPriority; //Only for tk (Normal)
    int hbRequestPriority; //Only for tk (HB)

    int currentCacheNum; //Cache number in this request

    Object bidResultSyncObject = new Object();

    protected HashMap<String, Long> mUnitGroupLoadTimeMap;//Record AdSource start to request time
    protected HashMap<String, Runnable> mOverTimeRunnableMap; //Timeout Runnable of UnitGroup
    protected HashMap<String, Runnable> mAdDataOverTimeRunnableMap; //AdData Timeout Runnable of UnitGroup
    protected Map<String, RequestStatus> mUnitGroupReturnStatus; //Return status of UnitGroup

    ConcurrentHashMap<String, ATBaseAdAdapter> mLoadingMap;

    private Runnable mLongOverTimeRunnable = new Runnable() {
        @Override
        public void run() {

            synchronized (CommonMediationManager.this) {
                hasLongTimeout = true;

                /**Check the UnitGroup which don't return the resulr**/
                for (String adsourceId : mUnitGroupReturnStatus.keySet()) {
                    mLoadingMap.remove(adsourceId);
                    RequestStatus requestStatus = mUnitGroupReturnStatus.get(adsourceId);
                    if (!requestStatus.isReturnResult) {
                        AdTrackingInfo adTrackingInfo = requestStatus.adTrackingInfo;
                        adTrackingInfo.setLoadStatus(AdTrackingInfo.LONG_OVERTIME_ERROR_CALLBACK);
                        AdError adError = ErrorCode.getErrorCode(ErrorCode.timeOutError, "", "");
                        mLoadError.putNetworkErrorMsg(adTrackingInfo.getmNetworkType(), adTrackingInfo.getNetworkName(), adError);

                        requestStatus.isReturnResult = true;

                        CommonSDKUtil.printAdTrackingInfoStatusLog(requestStatus.adTrackingInfo, Const.LOGKEY.REQUEST_RESULT, Const.LOGKEY.FAIL, adError.printStackTrace());

                        AgentEventManager.onAdsourceLoadFail(adTrackingInfo, 1, adError, 0);

                    }
                }

                if (!hasReturnResult) {
                    hasReturnResult = true;
                    onErrorCallbackToDeveloper();
                }

//            CommonAdManager adLoadManager = CommonAdManager.getInstance(mCurrentPlacementId);
//            if (adLoadManager != null) {
//                adLoadManager.removeMediationManager(mCurrentReqeustId);
//            }
            }
        }
    };

    /**
     * Init Ad Load Status and Collection Object
     *
     * @param context
     */
    public CommonMediationManager(Context context) {
        mActivityRef = new WeakReference<>(context);
        mApplcationContext = SDKContext.getInstance().getContext();

        requestWaitingPool = Collections.synchronizedList(new ArrayList<PlaceStrategy.UnitGroupInfo>());
        requestingPool = Collections.synchronizedList(new ArrayList<PlaceStrategy.UnitGroupInfo>());

        hbRequestWaitingPool = Collections.synchronizedList(new ArrayList<PlaceStrategy.UnitGroupInfo>());
        hbRequestingPool = Collections.synchronizedList(new ArrayList<PlaceStrategy.UnitGroupInfo>());

        mUnitGroupLoadTimeMap = new HashMap<>();

        mOverTimeRunnableMap = new HashMap<>();
        mAdDataOverTimeRunnableMap = new HashMap<>();

        mUnitGroupReturnStatus = new ConcurrentHashMap<>();

        mLoadingMap = new ConcurrentHashMap<>(5);

        mLoadError = ErrorCode.getErrorCode(ErrorCode.noADError, "", "");

    }


    /**---------------------------------------------------------------Request Pool Handle Start----------------------------------------------------------------------------------------**/
    /**
     * Add Default AdSource to Request
     **/
    private void addDefaultAdSourceToRequestingPool(final List<PlaceStrategy.UnitGroupInfo> list) {
        TemplateStrategy templateStrategy = mStrategy.getTemplateStrategy();
        if (templateStrategy == null) {
            return;
        }

        int defaultNetworkFirmId = templateStrategy.defaultNetworkFirmId;
        final PlaceStrategy.UnitGroupInfo[] unitGroupInfoArray = new PlaceStrategy.UnitGroupInfo[1];
        for (PlaceStrategy.UnitGroupInfo itemInfo : list) {
            if (itemInfo.networkType == defaultNetworkFirmId) {
                unitGroupInfoArray[0] = itemInfo;
                break;
            }
        }

        if (unitGroupInfoArray[0] == null) {
            return;
        }

        CommonLogUtil.i(TAG, "addDefaultAdSourceToRequestingPool: Default UnitGroupInfo:" + unitGroupInfoArray[0].networkType + "--content:" + unitGroupInfoArray[0].content);
        CommonLogUtil.i(TAG, "addDefaultAdSourceToRequestingPool delay:" + templateStrategy.defaultDelayTime);
        SDKContext.getInstance().runOnMainThreadDelayed(new Runnable() {
            @Override
            public void run() {
                CommonLogUtil.i(TAG, "addDefaultAdSourceToRequestingPool: startLoadDefaultAdSource:" + unitGroupInfoArray[0].networkType + "--content:" + unitGroupInfoArray[0].content);
                if (!hasReturnResult && requestWaitingPool.contains(unitGroupInfoArray[0])) {
                    requestWaitingPool.remove(unitGroupInfoArray[0]);
                    requestingPool.add(unitGroupInfoArray[0]);

                    CommonLogUtil.i(TAG, "addDefaultAdSourceToRequestingPool:start to request: waiting size:" + requestWaitingPool.size() + "; requesting size:" + requestingPool.size());
                    startAdSourceRequest(unitGroupInfoArray[0], true, false, false);

                }
            }
        }, templateStrategy.defaultDelayTime);

    }

    /**
     * Add AdSource to Request
     *
     * @param num
     * @param waitingList
     * @param requestingList
     */
    private synchronized void addAdSourceToRequestingPool(final int num, final List<PlaceStrategy.UnitGroupInfo> waitingList, final List<PlaceStrategy.UnitGroupInfo> requestingList, final boolean isFromHbPool) {
        // if long timeout, do nothing
        if (hasLongTimeout) {
            return;
        }

        TaskManager.getInstance().run_proxy(new Runnable() {
            @Override
            public void run() {
                synchronized (CommonMediationManager.this) {
                    if (!hasReturnResult) {
                        if (!hasFinishAllRequest()) {
                            List<PlaceStrategy.UnitGroupInfo> startRequestList = new ArrayList<>();
                            startRequestList.addAll(waitingList.subList(0, Math.min(num, waitingList.size())));
                            waitingList.removeAll(startRequestList);
                            requestingList.addAll(startRequestList);

                            CommonLogUtil.i(TAG, "addAdSourceToRequestingPool:start to request: waiting size:" + waitingList.size() + "; requesting size:" + requestingList.size());
                            for (PlaceStrategy.UnitGroupInfo unitGroupInfo : startRequestList) {
                                startAdSourceRequest(unitGroupInfo, false, isFromHbPool, false);
                            }
                        } else {
                            onErrorCallbackToDeveloper();
                        }
                    } else {
                        CommonLogUtil.i(TAG, "addAdSourceToRequestingPool(Has been returned):start to request: waiting size:" + waitingList.size() + "; requesting size:" + requestingList.size());
                        //If return result, check hb list or normal need to request
                        //If it is splash, it would not continue to request ad.
                        if (Const.FORMAT.SPLASH_FORMAT.equals(String.valueOf(mStrategy.getFormat()))) {
                            return;
                        }
                        if (waitingList.size() > 0) {
                            PlaceStrategy.UnitGroupInfo unitGroupInfo = waitingList.get(0);
                            //Check whether the number of caches meets the requirements
                            if (currentCacheNum < mStrategy.getCachedOffersNum()) {
                                waitingList.remove(unitGroupInfo);
                                requestingList.add(unitGroupInfo);
                                startAdSourceRequest(unitGroupInfo, false, isFromHbPool, true);
                            } else if (isFromHbPool) {
                                AdCacheInfo adCacheInfo = AdCacheManager.getInstance().getCache(SDKContext.getInstance().getContext(), mPlacementId);
                                if (adCacheInfo == null || unitGroupInfo.ecpm > adCacheInfo.getBaseAdapter().getmUnitgroupInfo().ecpm) {
                                    waitingList.remove(unitGroupInfo);
                                    requestingList.add(unitGroupInfo);
                                    startAdSourceRequest(unitGroupInfo, false, isFromHbPool, false);
                                }
                            }

                        }

                    }
                }
            }
        });


    }

    /**
     * Add AdSource to the Waiting request pool
     *
     * @param waitingPool
     * @param unitGroupInfo
     */
    private synchronized void addAdSourceToRequestWaitingPool(List<PlaceStrategy.UnitGroupInfo> waitingPool, PlaceStrategy.UnitGroupInfo unitGroupInfo, boolean isAddToHBPool) {
        if (waitingPool.size() == 0) {
            waitingPool.add(unitGroupInfo);

            /**If no AdSource is reuqesting or the last AdSource had been short-timeout, it will go to request the next AdSource.(Distinguish HB AdSource and Non-HB AdSource)**/
            if (isAddToHBPool) {
                if (hbRequestingPool.size() == 0 || !mOverTimeRunnableMap.containsKey(hbRequestingPool.get(hbRequestingPool.size() - 1).unitId)) {
                    addAdSourceToRequestingPool(1, hbRequestWaitingPool, hbRequestingPool, true);
                }
            } else {
                if (requestingPool.size() == 0 || !mOverTimeRunnableMap.containsKey(requestingPool.get(requestingPool.size() - 1).unitId)) {
                    addAdSourceToRequestingPool(1, requestWaitingPool, requestingPool, true);
                }
            }
            return;
        }

        synchronized (waitingPool) {
            CommonSDKUtil.insertAdSourceByOrderEcpm(waitingPool, unitGroupInfo);
//            for (int i = 0; i < waitingPool.size(); i++) {
//                PlaceStrategy.UnitGroupInfo waitingItem = waitingPool.get(i);
//                if (unitGroupInfo.ecpm >= waitingItem.ecpm) {
//                    waitingPool.add(i, unitGroupInfo);
//                    break;
//                } else {
//                    if (i == waitingPool.size() - 1) {
//                        waitingPool.add(unitGroupInfo);
//                        break;
//                    }
//                }
//            }
        }

    }


    /**
     * Remove AdSource from Requesting Pool
     *
     * @param unitGroupInfo
     */
    private synchronized void removeAdSourceFromRequestingPool(PlaceStrategy.UnitGroupInfo unitGroupInfo) {
        requestingPool.remove(unitGroupInfo);
        hbRequestingPool.remove(unitGroupInfo);
    }


    /**
     * Handle Bid Result
     *
     * @param unitGroupInfos
     */
    public void handleHeadBiddingAdSource(List<PlaceStrategy.UnitGroupInfo> unitGroupInfos) {
        synchronized (bidResultSyncObject) {
            if (unitGroupInfos == null || unitGroupInfos.size() == 0) {
                return;
            }

            int loadStatus = 0; //Only for Agent
            long useTime = 0; //Only for Agent
            JSONArray hbHandleResultArray = new JSONArray(); //Only for Agent

            PlaceStrategy.UnitGroupInfo lowestUnitGroupInfo = null;
            if (requestingPool.size() > 0) {
                lowestUnitGroupInfo = requestingPool.get(requestingPool.size() - 1);
            }

            List<PlaceStrategy.UnitGroupInfo> lowerAdSourcesList = new ArrayList<>();
            List<PlaceStrategy.UnitGroupInfo> higerAdSourcesList = new ArrayList<>();


            if (hasReturnResult) {
                AdCacheInfo adCacheInfo = null;


                if (isResultSuccess) { //Only for Agent
                    loadStatus = 2; //Only for Agent
                    adCacheInfo = AdCacheManager.getInstance().getCache(SDKContext.getInstance().getContext(), mPlacementId);
                } else {
                    loadStatus = 3; //Only for Agent
                }

                for (PlaceStrategy.UnitGroupInfo hbItemUnitGroupInfo : unitGroupInfos) {

                    useTime = hbItemUnitGroupInfo.bidUseTime; //Only for Agent
                    double compareEcpm = 0; //Only for Agent
                    int compareResult = 0; //Only for Agent

                    /**Select the high price AdSource to request.**/

                    if (isResultSuccess) {
                        if (adCacheInfo != null) {
                            compareEcpm = adCacheInfo.getBaseAdapter().getmUnitgroupInfo().ecpm;
                            if (hbItemUnitGroupInfo.ecpm > adCacheInfo.getBaseAdapter().getmUnitgroupInfo().ecpm) {
                                higerAdSourcesList.add(hbItemUnitGroupInfo);
//                            AgentEventManager.headBiddingAddToRequestPoolAgent(mRequestId
//                                    , mPlacementId, mStrategy
//                                    , hbItemUnitGroupInfo.networkType
//                                    , hbItemUnitGroupInfo.unitId
//                                    , hbItemUnitGroupInfo.ecpm
//                                    , hbItemUnitGroupInfo.bidUseTime
//                                    , isResultSuccess ? 2 : 3
//                                    , adCacheInfo.getBaseAdapter().getmUnitgroupInfo().ecpm, 1);
                                compareResult = 1; //Only for Agent

                            } else {
//                            AgentEventManager.headBiddingAddToRequestPoolAgent(mRequestId
//                                    , mPlacementId, mStrategy
//                                    , hbItemUnitGroupInfo.networkType
//                                    , hbItemUnitGroupInfo.unitId
//                                    , hbItemUnitGroupInfo.ecpm
//                                    , hbItemUnitGroupInfo.bidUseTime
//                                    , isResultSuccess ? 2 : 3
//                                    , adCacheInfo.getBaseAdapter().getmUnitgroupInfo().ecpm, 3);
                                compareResult = 3; //Only for Agent
                            }
                        } else {
                            higerAdSourcesList.add(hbItemUnitGroupInfo);
//                        AgentEventManager.headBiddingAddToRequestPoolAgent(mRequestId
//                                , mPlacementId, mStrategy
//                                , hbItemUnitGroupInfo.networkType
//                                , hbItemUnitGroupInfo.unitId
//                                , hbItemUnitGroupInfo.ecpm
//                                , hbItemUnitGroupInfo.bidUseTime
//                                , isResultSuccess ? 2 : 3
//                                , 0, 1);
                            compareResult = 1; //Only for Agent
                        }
                    } else {
                        compareResult = 3; //Only for Agent
                    }

                    fillHBS2SResultAgentLog(hbHandleResultArray, hbItemUnitGroupInfo, compareEcpm, compareResult); //Only for Agent
                }

            } else {
                loadStatus = 1; //Only for Agent
                double compareEcpm = 0; //Only for Agent
                int compareResult = 0; //Only for Agent

                for (PlaceStrategy.UnitGroupInfo hbItemUnitGroupInfo : unitGroupInfos) {
                    useTime = hbItemUnitGroupInfo.bidUseTime;
                    if (lowestUnitGroupInfo == null || hbItemUnitGroupInfo.ecpm > lowestUnitGroupInfo.ecpm) {
                        higerAdSourcesList.add(hbItemUnitGroupInfo);
//                        AgentEventManager.headBiddingAddToRequestPoolAgent(mRequestId
//                                , mPlacementId, mStrategy
//                                , hbItemUnitGroupInfo.networkType
//                                , hbItemUnitGroupInfo.unitId
//                                , hbItemUnitGroupInfo.ecpm
//                                , hbItemUnitGroupInfo.bidUseTime
//                                , 1
//                                , lowestUnitGroupInfo != null ? lowestUnitGroupInfo.ecpm : 0, 1);

                        compareEcpm = lowestUnitGroupInfo != null ? lowestUnitGroupInfo.ecpm : 0;
                        compareResult = 1; //Only for Agent
                    } else {
                        lowerAdSourcesList.add(hbItemUnitGroupInfo);
//                        AgentEventManager.headBiddingAddToRequestPoolAgent(mRequestId
//                                , mPlacementId, mStrategy
//                                , hbItemUnitGroupInfo.networkType
//                                , hbItemUnitGroupInfo.unitId
//                                , hbItemUnitGroupInfo.ecpm
//                                , hbItemUnitGroupInfo.bidUseTime
//                                , 1
//                                , lowestUnitGroupInfo != null ? lowestUnitGroupInfo.ecpm : 0, 2);

                        compareEcpm = lowestUnitGroupInfo != null ? lowestUnitGroupInfo.ecpm : 0;
                        compareResult = 2; //Only for Agent
                    }

                    fillHBS2SResultAgentLog(hbHandleResultArray, hbItemUnitGroupInfo, compareEcpm, compareResult); //Only for Agent
                }
            }

            /**HB Result Agent**/
            try {
                JSONObject resultObject = new JSONObject();
                resultObject.put("load_status", loadStatus);
                resultObject.put("bid_time", useTime);
                resultObject.put("result_list", hbHandleResultArray);
                AgentEventManager.headBiddingS2SAddToRequestPoolAgent(mRequestId, mPlacementId, mStrategy, resultObject.toString());
            } catch (Exception e) {
                if (Const.DEBUG) {
                    e.printStackTrace();
                }
            }


            /**Add to Normal Waiting Pool**/
            for (PlaceStrategy.UnitGroupInfo lowerItem : lowerAdSourcesList) {
                addAdSourceToRequestWaitingPool(requestWaitingPool, lowerItem, false);
            }

            /**Add to HB Waiting Pool**/
            for (PlaceStrategy.UnitGroupInfo higherItem : higerAdSourcesList) {
                addAdSourceToRequestWaitingPool(hbRequestWaitingPool, higherItem, true);
            }
        }
    }

    private void fillHBS2SResultAgentLog(JSONArray hbHandleResultArray, PlaceStrategy.UnitGroupInfo hbItemUnitGroupInfo, double compareEcpm, int compareResult) {
        try {
            JSONObject hbResultObject = new JSONObject();
            hbResultObject.put("unit_id", hbItemUnitGroupInfo.unitId);
            hbResultObject.put("nw_firm_id", hbItemUnitGroupInfo.networkType);
            hbResultObject.put("bidprice", hbItemUnitGroupInfo.ecpm);
            hbResultObject.put("ctype", compareEcpm);
            hbResultObject.put("result", compareResult);

            hbHandleResultArray.put(hbResultObject);
        } catch (Exception e) {
            if (Const.DEBUG) {
                e.printStackTrace();
            }
        }

    }

    /**---------------------------------------------------------------Request Pool Handle End----------------------------------------------------------------------------------------**/


    /**---------------------------------------------------------------Mediation Request Handle Start----------------------------------------------------------------------------------------**/

    /**
     * Start to WaterFall Request
     *
     * @param placementId
     * @param requestid
     * @param placeStrategy
     * @param normalList
     * @param hasFinsihBidding
     */
    protected void startToRequestMediationAd(String placementId, String requestid, PlaceStrategy placeStrategy, List<PlaceStrategy.UnitGroupInfo> normalList, boolean hasFinsihBidding) {
        isFinishBidding = hasFinsihBidding;
        requestWaitingPool.addAll(normalList);
        mRequestId = requestid;
        mPlacementId = placementId;
        mStrategy = placeStrategy;
        mUnitGroupList = "";

        startLoadUgList = normalList;

        for (int i = 0; i < normalList.size(); i++) {
            if (i > 0) {
                mUnitGroupList = mUnitGroupList + ",";
            }
            String networkName = normalList.get(i).networkType + "";
            mUnitGroupList = mUnitGroupList + networkName;
        }

        mStartLoadTime = System.currentTimeMillis();

        //Start long-timeour runnable
        networkLongOverTimeLoad(mStrategy.getLongOverLoadTime());

        addAdSourceToRequestingPool(placeStrategy.getRequestUnitGroupNumber(), requestWaitingPool, requestingPool, false);

        addDefaultAdSourceToRequestingPool(normalList);
    }


    /**
     * Start AdSource Request
     *
     * @param unitGroupInfo
     * @param isDefault
     * @param isFromHBPool
     */
    private void startAdSourceRequest(PlaceStrategy.UnitGroupInfo unitGroupInfo, boolean isDefault, boolean isFromHBPool, boolean isOfferCacheNoInRequestToLoad) {
        CommonLogUtil.i(TAG, "startAdSourceRequest: NetworkFirmId:" + unitGroupInfo.networkType + "---content:" + unitGroupInfo.content + "----Default:" + isDefault + "-----fromHBPool:" + isFromHBPool);
        try {
            //Remove the HB cache because it has been return result.
            if (unitGroupInfo.bidType == 1) {
                CommonLogUtil.i(TAG, "hb request send win notice url, remove cache");
                BiddingResult cacheResponse = BiddingCacheManager.getInstance().getCache(unitGroupInfo.unitId, unitGroupInfo.networkType);
                if (cacheResponse != null) {
                    cacheResponse.sendWinNotice();
                    //Refresh s2s status
                    BiddingCacheManager.getInstance().addCache(unitGroupInfo.unitId, cacheResponse);
                }
                /**If not Adx Adsource, it will remove the hb cache**/
                if (unitGroupInfo.networkType != Const.NETWORK_FIRM.ADX_NETWORK_FIRM_ID) {
                    BiddingCacheManager.getInstance().removeCache(unitGroupInfo.unitId, unitGroupInfo.networkType);
                }


            }
        } catch (Throwable e) {

        }


        if (isFromHBPool) {
            hbRequestPriority++;
        } else {
            requestPriority++;
        }

        AdTrackingInfo adTrackingInfo = TrackingInfoUtil.initTrackingInfo(mRequestId, mPlacementId, mUserId, mStrategy, isFromHBPool ? (unitGroupInfo.networkType + "") : mUnitGroupList, mStrategy.getRequestUnitGroupNumber(), mIsRefresh);

        UnitgroupCacheInfo unitgroupCacheInfo = AdCacheManager.getInstance().getUnitgroupCacheInfoByAdSourceId(mPlacementId, unitGroupInfo.unitId);
        AdCacheInfo adCacheInfo = unitgroupCacheInfo != null ? unitgroupCacheInfo.getAdCacheInfo() : null;
        if (adCacheInfo != null && adCacheInfo.isUpStatusAvaiable() && adCacheInfo.isNetworkAdReady()) {
            onCacheAdLoaded(unitGroupInfo);
            return;
        }

        ATBaseAdAdapter adapter = CustomAdapterFactory.createAdapter(unitGroupInfo);
        if (adapter == null) {
            AdError adError = ErrorCode.getErrorCode(ErrorCode.adapterNotExistError, "", unitGroupInfo.adapterClassName + " does not exist!");
            mLoadError.putNetworkErrorMsg(unitGroupInfo.networkType, "", adError);
            onAdError(unitGroupInfo, null, adError);
            return;
        }

        try {
            CommonDeviceUtil.putNetworkSDKVersion(unitGroupInfo.networkType, adapter.getNetworkSDKVersion());
        } catch (Throwable e) {

        }


        adTrackingInfo = TrackingInfoUtil.initPlacementUnitGroupTrackingInfo(adapter, adTrackingInfo, unitGroupInfo, isFromHBPool ? hbRequestPriority - 1 : requestPriority - 1);
        adTrackingInfo.setmIsDefaultNetwork(isDefault);

        if (isOfferCacheNoInRequestToLoad) { //The number of caches does not meet the requirements
            adTrackingInfo.setRequestType(AdTrackingInfo.CACHE_NUM_REQUEST);
        }

        AdTrackingManager.getInstance(mApplcationContext).addAdTrackingInfo(TrackingV2Loader.AD_REQUEST_TYPE, adTrackingInfo);

        CommonSDKUtil.printAdTrackingInfoStatusLog(adTrackingInfo, Const.LOGKEY.REQUEST, Const.LOGKEY.START, "");

        /**
         * Start AdSource's Short-Timeout Runnable
         */
        if (!isDefault) {
            Runnable overTimeRunnable = networkAdOvertimeLoad(unitGroupInfo, unitGroupInfo.getUnitADRequestOutTime(), isFromHBPool);
            if (overTimeRunnable != null) {
                mOverTimeRunnableMap.put(unitGroupInfo.unitId, overTimeRunnable);
            }

            Runnable adDataOverTimeRunnable = networkAdOvertimeLoad(unitGroupInfo, unitGroupInfo.getNetworkAdDataLoadTimeOut(), isFromHBPool);
            if (adDataOverTimeRunnable != null) {
                mAdDataOverTimeRunnableMap.put(unitGroupInfo.unitId, adDataOverTimeRunnable); //存储
            }
        }

        recordAdSourceReturnStatus(adTrackingInfo.getmUnitGroupUnitId(), adTrackingInfo, false);

        mUnitGroupLoadTimeMap.put(unitGroupInfo.unitId, System.currentTimeMillis());


        if ((mActivityRef.get() == null)) {
            onAdError(adapter, ErrorCode.getErrorCode(ErrorCode.contextDestoryError, "", ""));
            return;
        }

        if (mActivityRef.get() instanceof Activity) {
            adapter.refreshActivityContext((Activity) mActivityRef.get());
        }

        handleLoadAd(adapter, unitGroupInfo, mStrategy.getServerExtrasMap(mPlacementId, mRequestId, unitGroupInfo), isFromHBPool);
    }

    /**
     * Handle Adapter to Load Ad
     *
     * @param baseAdapter
     * @param unitGroupInfo
     * @param serverExtras
     */
    private void handleLoadAd(final ATBaseAdAdapter baseAdapter, final PlaceStrategy.UnitGroupInfo unitGroupInfo, final Map<String, Object> serverExtras, boolean isFromHBPool) {
        if (unitGroupInfo.networkType == 6) {
            JSONObject jsonObject = CommonSDKUtil.createRequestCustomData(mApplcationContext, mRequestId, mPlacementId, mStrategy.getFormat(), isFromHBPool ? hbRequestPriority : requestPriority);
            serverExtras.put("tp_info", jsonObject.toString());
        }

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                prepareFormatAdapter(baseAdapter);

                Context requestContext = mActivityRef.get();

                if (requestContext == null) {
                    onAdError(baseAdapter, ErrorCode.getErrorCode(ErrorCode.adapterInnerError, "", "Request Context is null! Please check the Ad init Context."));
                    return;
                }

                if (AppStrategy.needToSetNetworkGDPR()) {
                    UploadDataLevelManager uploadDataLevelManager = UploadDataLevelManager.getInstance(SDKContext.getInstance().getContext());
                    //If set data consent is true, it will log the gdpr info
                    try {
                        if (!uploadDataLevelManager.hasSetGDPR(unitGroupInfo.networkType)
                                && baseAdapter.setUserDataConsent(requestContext, uploadDataLevelManager.isNetworkGDPRConsent(), ATSDK.isEUTraffic(mApplcationContext))) {
                            uploadDataLevelManager.logGDPRSetting(unitGroupInfo.networkType);
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
                try {
                    Map<String, Object> localMap = PlacementAdManager.getInstance().getPlacementLocalSettingMap(mPlacementId);
                    /**Keep Adapter Global, Avoid adapter recycling. (Except Splash)**/
                    if (!Const.FORMAT.SPLASH_FORMAT.equals(mStrategy.getFormat())) {
                        mLoadingMap.put(unitGroupInfo.unitId, baseAdapter);
                    }

                    baseAdapter.internalLoad(requestContext, serverExtras, localMap, new CustomAdapterLoadListener(baseAdapter));
                } catch (Throwable e) {
                    onAdError(baseAdapter, ErrorCode.getErrorCode(ErrorCode.adapterInnerError, "", e.getMessage()));
                }

            }
        };

        if (TextUtils.equals(String.valueOf(mStrategy.getFormat()), Const.FORMAT.BANNER_FORMAT) || TextUtils.equals(String.valueOf(mStrategy.getFormat()), Const.FORMAT.SPLASH_FORMAT)) {
            SDKContext.getInstance().runOnMainThread(runnable); //Banner's request need to run on main thread
        } else {
            TaskManager.getInstance().runNetworkRequest(runnable);
        }


    }

    /**---------------------------------------------------------------Mediation Request Handle End----------------------------------------------------------------------------------------**/


    /**--------------------------------------------------------------------Ad Result Handle Start---------------------------------------------------------------------------------**/
    /**
     * Ad Data Loaded Callback to invoke
     *
     * @param baseAdapter
     */
    public void onAdDataLoaded(ATBaseAdAdapter baseAdapter) {
        AdTrackingInfo adTrackingInfo = baseAdapter.getTrackingInfo();
        long starttime = mUnitGroupLoadTimeMap.get(adTrackingInfo.getmUnitGroupUnitId());
        adTrackingInfo.setDataFillTime(System.currentTimeMillis() - starttime);
    }

    /**
     * Ad Cache Loaded Callback to invoke
     *
     * @param unitGroupInfo
     */
    private void onCacheAdLoaded(PlaceStrategy.UnitGroupInfo unitGroupInfo) {
        removeAdSourceFromRequestingPool(unitGroupInfo);
        CommonLogUtil.i(TAG, "onCacheAdLoaded: NetworkFirmId:" + unitGroupInfo.networkType + "---content:" + unitGroupInfo.content);
        currentCacheNum++;
        if (!hasReturnResult) {
            onLoadedCallbackToDeveloper(true);
        }
    }

    /**
     * Ad Loaded Success Callback to invoke
     *
     * @param baseAdapter
     * @param adObjectList
     */
    public synchronized void onAdLoaded(ATBaseAdAdapter baseAdapter, List<? extends BaseAd> adObjectList) {

        //Check adapter had returned result in this request. If true, it would not continue to do next step.
        String adsourceId = baseAdapter != null ? baseAdapter.getTrackingInfo().getmUnitGroupUnitId() : "";

        if (isAdSourceReturnResult(adsourceId)) {
            return;
        }

        CommonLogUtil.i(TAG, "onAdLoaded: NetworkFirmId:" + baseAdapter.getmUnitgroupInfo().networkType + "---content:" + baseAdapter.getmUnitgroupInfo().content);
        AdTrackingInfo adTrackingInfo = baseAdapter.getTrackingInfo();

        long starttime = mUnitGroupLoadTimeMap.get(adsourceId);
        adTrackingInfo.setFillTime(System.currentTimeMillis() - starttime);

        //set placement id for network
        adTrackingInfo.setmNetworkPlacementId(baseAdapter.getNetworkPlacementId());

        removeAdSourceFromRequestingPool(baseAdapter.getmUnitgroupInfo());

//        try {
//            //Remove the HB cache because it has been return result.
//            if (baseAdapter.getmUnitgroupInfo().bidType == 1) {
//                HeadBiddingCacheManager.getInstance().removeCache(baseAdapter.getmUnitgroupInfo().unitId);
//            }
//        } catch (Throwable e) {
//
//        }

        if (baseAdapter.getmUnitgroupInfo().getNetworkAdDataLoadTimeOut() != -1) {
            if (adTrackingInfo.getDataFillTime() > 0) {
                AgentEventManager.adDataFillEvent(adTrackingInfo);
            }
        }


        boolean isInShortTime = false;

        Runnable runnable = mOverTimeRunnableMap.get(adsourceId);
        if (runnable != null) {
            SDKContext.getInstance().removeMainThreadRunnable(runnable);
            mOverTimeRunnableMap.remove(adsourceId);
            isInShortTime = true;
        }
        runnable = mAdDataOverTimeRunnableMap.get(adsourceId);
        if (runnable != null) {
            SDKContext.getInstance().removeMainThreadRunnable(runnable);
            mAdDataOverTimeRunnableMap.remove(adsourceId);
            isInShortTime = true;
        }

        if (isInShortTime) {
            adTrackingInfo.setLoadStatus(AdTrackingInfo.SHORT_OVERTIME_CALLBACK);
        }

        recordAdSourceReturnStatus(adsourceId, adTrackingInfo, true);

        if (mImpressionMaxEcpm > 0) {
            /**Record Flag**/
            if (mImpressionMaxEcpm < baseAdapter.getmUnitgroupInfo().ecpm) {
                adTrackingInfo.setFlag(AdTrackingInfo.SHOW_LOW_LEVEL_CACHE);
            } else {
                adTrackingInfo.setFlag(AdTrackingInfo.SHOW_HIGH_LEVEL_CACHE);
            }
        } else {
            adTrackingInfo.setFlag(AdTrackingInfo.NO_SHOW_CACHE);
        }

        AdTrackingManager.getInstance(mApplcationContext).addAdTrackingInfo(TrackingV2Loader.AD_REQUEST_SUCCESS_TYPE, adTrackingInfo);
        CommonSDKUtil.printAdTrackingInfoStatusLog(adTrackingInfo, Const.LOGKEY.REQUEST_RESULT, Const.LOGKEY.SUCCESS, "");


        /**Save Offer to Caches**/
        long adCaheTime = baseAdapter.getmUnitgroupInfo().getUnitADCacheTime();

        /**Splash Ad would not be putting in the cache**/
        if (mStrategy.getFormat() != Integer.parseInt(Const.FORMAT.SPLASH_FORMAT)) {
            AdCacheManager.getInstance().addCache(mPlacementId, adTrackingInfo.getRequestLevel(), baseAdapter, adObjectList, adCaheTime, mStrategy);

            //If result callback had been canceled or had been showed, it would not to start the countdown.
            if (!hasCancelReturnResult && !requestHasShow) {
                CommonAdManager adManager = PlacementAdManager.getInstance().getAdManager(mPlacementId);
                if (adManager != null && mStrategy.getAutoRequestUnitgroupAd() >= 1) {
                    adManager.prepareCountdown(baseAdapter, mRequestId, baseAdapter.getmUnitgroupInfo().ecpm);
                }
            }

        }

        currentCacheNum++;

        if (adsourceId != null) {
            mLoadingMap.remove(adsourceId);
        }

        if (!hasReturnResult) { //Handle to callback
            onLoadedCallbackToDeveloper(false);
        }
    }

    /**
     * Callback to Deveploper Success
     */
    private void onLoadedCallbackToDeveloper(boolean isCache) {
        CommonLogUtil.i(TAG, "onLoadedCallbackToDeveloper: isCache:" + isCache);
        hasReturnResult = true;
        isResultSuccess = true;

        if (mLongOverTimeRunnable != null) {
            /**Cancel long-timeout runnable**/
            SDKContext.getInstance().removeMainThreadRunnable(mLongOverTimeRunnable);
        }

        long loadDuration = System.currentTimeMillis() - mStartLoadTime;
        AdTrackingInfo anythinkTrackingInfo = TrackingInfoUtil.initTrackingInfo(mRequestId, mPlacementId, mUserId, mStrategy, mUnitGroupList, mStrategy.getRequestUnitGroupNumber(), mIsRefresh);
        anythinkTrackingInfo.setmIsLoad(true);
        anythinkTrackingInfo.setFillTime(loadDuration);
        if (isCache) {
            anythinkTrackingInfo.setmReason(5);
        }


        AdTrackingManager.getInstance(mApplcationContext).addAdTrackingInfo(TrackingV2Loader.AD_SDK_LOAD_SUCCESS_TYPE, anythinkTrackingInfo);

        CommonAdManager commonAdManager = PlacementAdManager.getInstance().getAdManager(mPlacementId);
        if (commonAdManager != null) {
            commonAdManager.setLoaded(mStrategy.getAsid());
        }

        if (!Const.FORMAT.SPLASH_FORMAT.equals(mStrategy.getFormat() + "")) {
            AdCacheManager.getInstance().refreshCacheInfo(startLoadUgList, mPlacementId, mStrategy
                    , mRequestId, mUserId, mUnitGroupList, mIsRefresh);

        }

        if (!hasCancelReturnResult) {
            onDevelopLoaded();
        }

        removeFormatCallback();

        handleToRemoveTheRequestMediation();
    }


    /**
     * AdSource Load Fail Callback to invoke
     *
     * @param baseAdapter
     * @param adError
     */
    private synchronized void onAdError(PlaceStrategy.UnitGroupInfo unitGroupInfo, final ATBaseAdAdapter baseAdapter, AdError adError) {

        boolean isFromHBPool = hbRequestingPool.contains(unitGroupInfo);
        /**Remove the reuqest in requesting pool**/
        removeAdSourceFromRequestingPool(unitGroupInfo);

        if (unitGroupInfo.unitId != null) {
            mLoadingMap.remove(unitGroupInfo.unitId);
        }

        if (baseAdapter != null) {
            //Check adapter had returned result in this request. If true, it would not continue to do next step.
            SDKContext.getInstance().runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    if (baseAdapter != null) {
                        baseAdapter.destory();
                    }
                }
            });

            AdTrackingInfo adTrackingInfo = baseAdapter.getTrackingInfo();
            String adsourceId = adTrackingInfo.getmUnitGroupUnitId();

            if (isAdSourceReturnResult(adsourceId)) {
                return;
            }


            mLoadError.putNetworkErrorMsg(adTrackingInfo.getmNetworkType(), baseAdapter.getNetworkName(), adError);
            //Record request fail time
            AdSourceRequestFailManager.getInstance().putAdSourceRequestFailTime(adsourceId, System.currentTimeMillis());

            long starttime = mUnitGroupLoadTimeMap.get(adsourceId);

            boolean isInShortTime = false;

            Runnable runnable = mOverTimeRunnableMap.get(adsourceId);
            if (runnable != null) {
                SDKContext.getInstance().removeMainThreadRunnable(runnable);
                mOverTimeRunnableMap.remove(adsourceId);
                isInShortTime = true;
            }

            runnable = mAdDataOverTimeRunnableMap.get(adsourceId);
            if (runnable != null) {
                SDKContext.getInstance().removeMainThreadRunnable(runnable);
                mAdDataOverTimeRunnableMap.remove(adsourceId);
                isInShortTime = true;
            }


            if (isInShortTime) {
                adTrackingInfo.setLoadStatus(AdTrackingInfo.SHORT_OVERTIME_CALLBACK);
            }

            recordAdSourceReturnStatus(adsourceId, adTrackingInfo, true);

            CommonSDKUtil.printAdTrackingInfoStatusLog(adTrackingInfo, Const.LOGKEY.REQUEST_RESULT, Const.LOGKEY.FAIL, adError.printStackTrace());

            AgentEventManager.onAdsourceLoadFail(adTrackingInfo, 0, adError, System.currentTimeMillis() - starttime);

        }

        /**If default adsource request fail, it would not to request the next adsource.**/
        if (baseAdapter != null && baseAdapter.getTrackingInfo().ismIsDefaultNetwork()) {
            if (hasFinishAllRequest() && !hasReturnResult) {
                onErrorCallbackToDeveloper();
            }
        } else {
            /**Start to next request**/
            if (isFromHBPool) {
                addAdSourceToRequestingPool(1, hbRequestWaitingPool, hbRequestingPool, true);
            } else {
                addAdSourceToRequestingPool(1, requestWaitingPool, requestingPool, false);
            }
        }
    }

    protected void onAdError(ATBaseAdAdapter baseAdapter, AdError adError) {
        onAdError(baseAdapter.getmUnitgroupInfo(), baseAdapter, adError);
    }

    /**
     * Final Error Callback to Deveploper
     */
    private void onErrorCallbackToDeveloper() {
        hasReturnResult = true;
        isResultSuccess = false;

        if (mLongOverTimeRunnable != null) {
            /**Cancel long-timeout runnable**/
            SDKContext.getInstance().removeMainThreadRunnable(mLongOverTimeRunnable);
        }

        AdTrackingInfo adTrackingInfo = TrackingInfoUtil.initTrackingInfo(mRequestId, mPlacementId, mUserId, mStrategy, mUnitGroupList, mStrategy.getRequestUnitGroupNumber(), mIsRefresh);
        AgentEventManager.onAgentForATToAppLoadFail(adTrackingInfo, mLoadError);

        if (!hasCancelReturnResult) {
            onDeveloLoadFail(mLoadError);
        }


        removeFormatCallback();

        handleToRemoveTheRequestMediation();
    }

    /**--------------------------------------------------------------------Ad Result Handle End---------------------------------------------------------------------------------**/


    /**
     * Short Timeout Handle
     *
     * @param unitGroupInfo
     */
    private Runnable networkAdOvertimeLoad(final PlaceStrategy.UnitGroupInfo unitGroupInfo, final long overTime, final boolean isFromHBPool) {
        if (overTime == -1) {
            return null;
        }

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (CommonMediationManager.this) {
                    // if long timeout, do nothing
                    if (hasLongTimeout) {
                        return;
                    }

                    /**Cacnel the short-timeout runnable**/
                    Runnable overTimeRunnable = mOverTimeRunnableMap.get(unitGroupInfo.unitId);
                    Runnable adDataOverTimeRunnable = mAdDataOverTimeRunnableMap.get(unitGroupInfo.unitId);

                    if (overTimeRunnable != null) {
                        SDKContext.getInstance().removeMainThreadRunnable(overTimeRunnable);
                    }

                    if (adDataOverTimeRunnable != null) {
                        SDKContext.getInstance().removeMainThreadRunnable(adDataOverTimeRunnable);
                    }

                    //Remove short time-out runnable in Map
                    mOverTimeRunnableMap.remove(unitGroupInfo.unitId);
                    mAdDataOverTimeRunnableMap.remove(unitGroupInfo.unitId);


                    if (isFromHBPool) {
                        addAdSourceToRequestingPool(1, hbRequestWaitingPool, hbRequestingPool, true);
                    } else {
                        addAdSourceToRequestingPool(1, requestWaitingPool, requestingPool, false);
                    }

                }
            }
        };
        SDKContext.getInstance().runOnMainThreadDelayed(runnable, overTime);
        return runnable;
    }

    private synchronized boolean hasFinishAllRequest() {
        CommonLogUtil.i(TAG, "hasFinishAllRequest:isFinishBidding: " + isFinishBidding);
        CommonLogUtil.i(TAG, "hasFinishAllRequest:requestWaitingPool: " + requestWaitingPool.size());
        CommonLogUtil.i(TAG, "hasFinishAllRequest:requestingPool: " + requestingPool.size());
        CommonLogUtil.i(TAG, "hasFinishAllRequest:hbRequestWaitingPool: " + hbRequestWaitingPool.size());
        CommonLogUtil.i(TAG, "hasFinishAllRequest:hbRequestingPool: " + hbRequestingPool.size());

        return isFinishBidding && requestWaitingPool.size() == 0 && requestingPool.size() == 0 && hbRequestWaitingPool.size() == 0 && hbRequestingPool.size() == 0;
    }

    /**
     * Long-Timeout
     *
     * @param overTime
     */
    private void networkLongOverTimeLoad(long overTime) {
        SDKContext.getInstance().runOnMainThreadDelayed(mLongOverTimeRunnable, overTime);
    }

    public void setRefresh(boolean isRefresh) {
        mIsRefresh = isRefresh;
    }

    protected Map<Integer, ATMediationSetting> mSettingMap;

    public boolean hasFinishLoad() {
        /**Return result or All AdSource had been Short-Timeout**/
        return hasReturnResult || (isFinishBidding && requestWaitingPool.size() == 0 && hbRequestWaitingPool.size() == 0 && mOverTimeRunnableMap.size() == 0);
    }

    /**
     * Notify this request has been showed
     */
    double mImpressionMaxEcpm;

    public void notifyImpression(double impressionEcpm) {
        requestHasShow = true;
        if (impressionEcpm > mImpressionMaxEcpm) {
            mImpressionMaxEcpm = impressionEcpm;
        }
        handleToRemoveTheRequestMediation();
    }

    /**
     * Notify this request bidding finish
     */
    public void notifyBiddingFinish() {
        synchronized (bidResultSyncObject) {
            isFinishBidding = true;
            /**If finising all request and no result callback after bidding over, it will return errror to deveploper**/
            if (!hasReturnResult && hasFinishAllRequest()) {
                onErrorCallbackToDeveloper();
            }

            handleToRemoveTheRequestMediation();
        }
    }

    private void handleToRemoveTheRequestMediation() {
        if (isFinishBidding && hasReturnResult) {
            if ((isResultSuccess && requestHasShow) || !isResultSuccess) {
                PlacementAdManager.getInstance().getAdManager(mPlacementId).removeMediationManager(mRequestId);
            }
        }
    }

    /**
     * Set User Data
     **/
    public void setUserData(String userId, String customData) {
        if (TextUtils.isEmpty(userId)) {
            mUserId = "";
        } else {
            mUserId = userId;
        }

        if (TextUtils.isEmpty(customData)) {
            mCustomData = "";
        } else {
            mCustomData = customData;
        }
    }

    boolean hasCancelReturnResult = false;

    public void notifyCancelReturnResult() {
        hasCancelReturnResult = true;
    }

    public void release() {
        if (mLongOverTimeRunnable != null) {
            /**Cancel long-timeout runnable**/
            SDKContext.getInstance().removeMainThreadRunnable(mLongOverTimeRunnable);
        }
    }

    public abstract void prepareFormatAdapter(ATBaseAdAdapter baseAdapter);

    public abstract void onDevelopLoaded();

    public abstract void onDeveloLoadFail(AdError adError);

    public abstract void removeFormatCallback();


    public class CustomAdapterLoadListener implements ATCustomLoadListener {
        ATBaseAdAdapter baseAdAdapter;

        private CustomAdapterLoadListener(ATBaseAdAdapter baseAdAdapter) {
            this.baseAdAdapter = baseAdAdapter;
        }

        @Override
        public void onAdDataLoaded() {
            CommonMediationManager.this.onAdDataLoaded(baseAdAdapter);
        }

        @Override
        public void onAdCacheLoaded(BaseAd... baseAds) {
            CommonMediationManager.this.onAdLoaded(baseAdAdapter, baseAds != null ? Arrays.asList(baseAds) : null);
            if (baseAdAdapter != null) {
                baseAdAdapter.releaseLoadResource();
            }
        }

        @Override
        public void onAdLoadError(String errorCode, String errorMsg) {
            CommonMediationManager.this.onAdError(baseAdAdapter, ErrorCode.getErrorCode(ErrorCode.noADError, errorCode, errorMsg));
            if (baseAdAdapter != null) {
                baseAdAdapter.releaseLoadResource();
            }
        }
    }

    /**
     * Check Adsource has been return result
     *
     * @param adsourceId
     * @return
     */
    private boolean isAdSourceReturnResult(String adsourceId) {
        if (mUnitGroupReturnStatus.containsKey(adsourceId)) {
            RequestStatus requestStatus = mUnitGroupReturnStatus.get(adsourceId);
            if (requestStatus != null && requestStatus.isReturnResult) {
                return true;
            }
        }
        return false;
    }

    /**
     * Change Adsource return result status
     *
     * @param adsourceId
     * @param adTrackingInfo
     * @param isReturnResult
     */
    private void recordAdSourceReturnStatus(String adsourceId, AdTrackingInfo adTrackingInfo, boolean isReturnResult) {
        RequestStatus requestStatus = mUnitGroupReturnStatus.get(adsourceId);
        if (requestStatus == null) {
            requestStatus = new RequestStatus(adTrackingInfo, isReturnResult);
            mUnitGroupReturnStatus.put(adsourceId, requestStatus);
        } else {
            requestStatus.adTrackingInfo = adTrackingInfo;
            requestStatus.isReturnResult = isReturnResult;
        }
    }

    class RequestStatus {
        AdTrackingInfo adTrackingInfo;
        boolean isReturnResult;

        RequestStatus(AdTrackingInfo adTrackingInfo, boolean isReturnResult) {
            this.adTrackingInfo = adTrackingInfo;
            this.isReturnResult = isReturnResult;
        }
    }
}
