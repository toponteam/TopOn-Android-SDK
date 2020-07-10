package com.anythink.core.common;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.ATSDK;
import com.anythink.core.api.AdError;
import com.anythink.core.api.ErrorCode;
import com.anythink.core.cap.AdCapV2Manager;
import com.anythink.core.cap.AdPacingManager;
import com.anythink.core.common.base.AnyThinkBaseAdapter;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.entity.AdCacheInfo;
import com.anythink.core.common.entity.AdTrackingInfo;
import com.anythink.core.common.entity.PlacementImpressionInfo;
import com.anythink.core.common.entity.UnitgroupCacheInfo;
import com.anythink.core.common.net.TrackingV2Loader;
import com.anythink.core.common.track.AdTrackingManager;
import com.anythink.core.common.track.AgentEventManager;
import com.anythink.core.common.utils.CommonDeviceUtil;
import com.anythink.core.common.utils.CommonLogUtil;
import com.anythink.core.common.utils.CommonMD5;
import com.anythink.core.common.utils.CommonSDKUtil;
import com.anythink.core.common.utils.CommonUtil;
import com.anythink.core.common.utils.NetworkLogUtil;
import com.anythink.core.common.utils.TrackingInfoUtil;
import com.anythink.core.common.utils.task.TaskManager;
import com.anythink.core.strategy.PlaceStrategy;
import com.anythink.core.strategy.PlaceStrategyManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

public abstract class CommonAdManager {
    private static final String TAG = "CommonAdManager";

    private static HashMap<String, CommonAdManager> sAdMangerMap = new HashMap<>();

    protected HashMap<Integer, ATMediationSetting> mSettings;

    public static CommonAdManager getInstance(String placementId) {
        return sAdMangerMap.get(placementId);
    }

    public static void addAdManager(String placementId, CommonAdManager adManager) {
        sAdMangerMap.put(placementId, adManager);
    }


    protected Context mApplicationContext;
    protected WeakReference<Context> mActivityRef;
    protected String mPlacementId;

    protected HashMap<String, CommonMediationManager> mHistoryMediationManager;

    protected CommonMediationManager mCurrentManager; //Mediation Request Manager

    protected int mUpStatus = 0;

    protected boolean mIsLoading;

    private long mUpStatusSetTime;

    private long mUpStatusOverTime; //upstatus's out-date time

    protected String mRequestId;


    public CommonAdManager(Context context, String placementId) {
        mActivityRef = new WeakReference<>(context);
        mApplicationContext = context.getApplicationContext();
        mPlacementId = placementId;

        mHistoryMediationManager = new HashMap<>(5);

        if (SDKContext.getInstance().getContext() == null) {
            SDKContext.getInstance().setContext(mApplicationContext);
        }
    }

    public String getCurrentRequestId() {
        return mRequestId;
    }

    /**
     * Set Loaded Status
     */
    public void setLoaded() {
        mUpStatus = 1;
        mUpStatusSetTime = System.currentTimeMillis();
    }


    public Context getContext() {
        return mActivityRef.get();
    }

    /**
     * Refresh Context
     *
     * @param context
     */
    public void refreshContext(Context context) {
        mActivityRef = new WeakReference<>(context);
    }

    /**
     * Remove MediationManager by RequestId
     *
     * @param requestId
     */
    public void removeMediationManager(String requestId) {
        CommonMediationManager mediationManager = mHistoryMediationManager.get(requestId);
        if (mediationManager != null) {
            mediationManager.release();
        }
        mHistoryMediationManager.remove(requestId);
    }

    /**
     * Check upstatus is out of date
     */
    public boolean isUpStatusOverTime() {
        if (System.currentTimeMillis() - mUpStatusSetTime >= mUpStatusOverTime) {
            return true;
        }
        return false;
    }

    /**
     * Check loading status
     *
     * @return
     */
    public boolean isLoading() {
        return mIsLoading || (mCurrentManager != null && !mCurrentManager.hasFinishLoad());
    }

    /**
     * Request Placement Setting
     *
     * @param context
     * @param mPlacementId
     * @param placementCallback
     */
    public void loadStragety(final Context context, final String format, final String mPlacementId, final boolean isRefresh, final PlacementCallback placementCallback) {
        TaskManager.getInstance().run_proxy(new Runnable() {
            @Override
            public void run() {
                synchronized (this) {
                    SDKContext.getInstance().checkAppStrategy(context, SDKContext.getInstance().getAppId(), SDKContext.getInstance().getAppKey());

                    final String requestId = createRequestId(context);
                    //Create RequestId
                    mRequestId = requestId;

                    final Context mApplicationContext = context.getApplicationContext();
                    final String mAppId = SDKContext.getInstance().getAppId();
                    final String mAppKey = SDKContext.getInstance().getAppKey();
                    final String mPsid = SDKContext.getInstance().getPsid();
                    final String mSessionId = SDKContext.getInstance().getSessionId(mPlacementId);

                    final PlaceStrategy placeStrategy = PlaceStrategyManager.getInstance(mApplicationContext).getPlaceStrategyByAppIdAndPlaceId(mPlacementId);

                    if (mIsLoading || (mCurrentManager != null && !mCurrentManager.hasFinishLoad())) {
                        //正处于加载中
                        SDKContext.getInstance().runOnMainThread(new Runnable() {
                            @Override
                            public void run() {
                                AdError adError = ErrorCode.getErrorCode(ErrorCode.loadingError, "", "");
                                if (placementCallback != null) {
                                    placementCallback.onLoadError(mPlacementId, requestId, adError);
                                }
                                /**Send tracking**/
                                String psid = mPsid;
                                String sessionid = mSessionId;
                                String asid = placeStrategy != null ? placeStrategy.getAsid() : "";


                                AdTrackingInfo adTrackingInfo = TrackingInfoUtil.getTrackingInfoForAgent(format, requestId, mPlacementId, psid, sessionid, placeStrategy, isRefresh ? 1 : 0);
                                adTrackingInfo.setmIsLoad(false);
                                adTrackingInfo.setmReason(AdTrackingInfo.LOADING_REASON);

                                AdTrackingManager.getInstance(mApplicationContext).addAdTrackingInfo(TrackingV2Loader.AD_SDK_LOAD_TYPE, adTrackingInfo);
                                //Fail Agent
                                AgentEventManager.onAgentForATToAppLoadFail(adTrackingInfo, adError.printStackTrace());
                            }
                        });
                        return;
                    }


                    if (SDKContext.getInstance().getContext() == null
                            || TextUtils.isEmpty(SDKContext.getInstance().getAppId())
                            || TextUtils.isEmpty(SDKContext.getInstance().getAppKey())
                            || CommonUtil.isNullOrEmpty(mPlacementId)) {
                        SDKContext.getInstance().runOnMainThread(new Runnable() {
                            @Override
                            public void run() {
                                if (placementCallback != null) {
                                    AdError adError = ErrorCode.getErrorCode(ErrorCode.appIdOrPlaceIdEmpty, "", "");
                                    placementCallback.onLoadError(mPlacementId, requestId, adError);
                                }
                            }
                        });
                        Log.e("CommonAdLoadManager", "SDK init error!");
                        mIsLoading = false;
                        return;
                    }


                    mIsLoading = true;


                    /**Cancel the callback of old MediationManager*/
                    if (mCurrentManager != null) {
                        mCurrentManager.cancelCallback();
                        mCurrentManager.cancelCacheOffer();
                    }


                    PlaceStrategyManager.getInstance(context).requestStrategy(placeStrategy, mAppId, mAppKey, mPlacementId, new PlaceStrategyManager.StrategyloadListener() {
                        @Override
                        public void loadStrategySuccess(final PlaceStrategy placeStrategy) {
                            mUpStatusOverTime = placeStrategy.getUpStatusOverTime();

                            String psid = mPsid;
                            String sessionid = mSessionId;
                            String asid = placeStrategy.getAsid();

                            /**tracking**/
                            final AdTrackingInfo adTrackingInfo = TrackingInfoUtil.getTrackingInfoForAgent(format, requestId, mPlacementId, psid, sessionid, placeStrategy, isRefresh ? 1 : 0);

                            //Check if the placement matches the ad format
                            if (!TextUtils.equals(String.valueOf(placeStrategy.getFormat()), format)) {
                                SDKContext.getInstance().runOnMainThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (placementCallback != null) {
                                            AdError adError = ErrorCode.getErrorCode(ErrorCode.formatError, "", "");
                                            placementCallback.onLoadError(mPlacementId, requestId, adError);
                                        }
                                    }
                                });

                                adTrackingInfo.setmIsLoad(false);
//                                adTrackingInfo.setmReason(AdTrackingInfo.MISMATCHED_FORMAT_REASON);
//                                AdTrackingManager.getInstance(mApplicationContext).addAdTrackingInfo(TrackingV2Loader.AD_SDK_LOAD_TYPE, adTrackingInfo);

                                AgentEventManager.onAgentForATToAppLoadFail(adTrackingInfo, "Mismatched ad placement and ad format.");
                                mIsLoading = false;
                                return;
                            }

                            TaskManager.getInstance().run_proxy(new Runnable() {
                                @Override
                                public void run() {
//                                    checkPacingAndCappingToGetUnitgroup(mApplicationContext, mPlacementId, requestId, placeStrategy, adTrackingInfo, placementCallback);
                                    checkToGetWaterfallList(mApplicationContext, mPlacementId, requestId, placeStrategy, adTrackingInfo, placementCallback);
                                }
                            });

                        }

                        @Override
                        public void loadStrategyFailed(AdError errorBean) {
                            NetworkLogUtil.strategyLog(Const.LOGKEY.FAIL, mPlacementId, CommonSDKUtil.getFormatString(format), errorBean.printStackTrace());

                            final AdError adError1 = ErrorCode.getErrorCode(ErrorCode.placeStrategyError, errorBean.getPlatformCode(), errorBean.getPlatformMSG());

                            final AdTrackingInfo adTrackingInfo = TrackingInfoUtil.getTrackingInfoForAgent(format, requestId, mPlacementId, mPsid, mSessionId, null, isRefresh ? 1 : 0);
                            adTrackingInfo.setmReason(AdTrackingInfo.PLACEMENT_STRATEGY_REASON);

                            onCallbackFail(adTrackingInfo, adError1, adError1.printStackTrace(), placementCallback);
                        }
                    });
                }
            }
        });
    }

    /**
     * Check Cap and Pacing
     *
     * @param context
     * @param placementId
     * @param requestId
     * @param placeStrategy
     * @param callback
     */
    private void checkPacingAndCappingToGetUnitgroup(final Context context, final String placementId, final String requestId
            , final PlaceStrategy placeStrategy, final AdTrackingInfo adTrackingInfo, final PlacementCallback callback) {

        List<PlaceStrategy.UnitGroupInfo> resultUnitGroupList = placeStrategy.parseUnitGroupInfoList(placeStrategy.getNormalUnitGroupListStr());
        List<PlaceStrategy.UnitGroupInfo> headbiddingList = placeStrategy.parseUnitGroupInfoList(placeStrategy.getHeadbiddingUnitGroupListStr());

        //Headbidding Tracking status
        boolean tempNeedTrackHb = false;
        if (headbiddingList != null && headbiddingList.size() > 0) {
            tempNeedTrackHb = true;
        }
        final boolean needTrackHb = tempNeedTrackHb;


        /**Unqualified UnitGroup List**/
        final List<PlaceStrategy.UnitGroupInfo> noFilterGroupList = new ArrayList<>();

        final String psid = SDKContext.getInstance().getPsid();
        final String sessionid = SDKContext.getInstance().getSessionId(placementId);
        final int groupid = placeStrategy != null ? placeStrategy.getGroupId() : 0;

        if (!placeStrategy.isAdOpen()) {
            SDKContext.getInstance().runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    if (callback != null) {
                        AdError adError = ErrorCode.getErrorCode(ErrorCode.placementAdClose, "", "");
                        callback.onLoadError(placementId, requestId, adError);
                    }
                }
            });

            adTrackingInfo.setmIsLoad(false);
            adTrackingInfo.setmReason(AdTrackingInfo.PLACEMENT_STRATEGY_REASON);
            AdTrackingManager.getInstance(mApplicationContext).addAdTrackingInfo(TrackingV2Loader.AD_SDK_LOAD_TYPE, adTrackingInfo);

            AgentEventManager.onAgentForATToAppLoadFail(adTrackingInfo, "Strategy is close.");
            mIsLoading = false;
            return;
        }


        if ((resultUnitGroupList == null || resultUnitGroupList.size() == 0) && (headbiddingList == null || headbiddingList.size() == 0)) {
            CommonLogUtil.i(TAG, "unitgroup list is null");
            SDKContext.getInstance().runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    if (callback != null) {
                        AdError adError = ErrorCode.getErrorCode(ErrorCode.noAdsourceConfig, "", "");
                        callback.onLoadError(placementId, requestId, adError);
                    }
                }
            });
            mIsLoading = false;

            adTrackingInfo.setmIsLoad(false);
            adTrackingInfo.setmReason(AdTrackingInfo.PLACEMENT_STRATEGY_REASON);
            AdTrackingManager.getInstance(mApplicationContext).addAdTrackingInfo(TrackingV2Loader.AD_SDK_LOAD_TYPE, adTrackingInfo);

            AgentEventManager.onAgentForATToAppLoadFail(adTrackingInfo, "No Adsource.");
            return;
        }


        long placementDayCap = placeStrategy.getUnitCapsDayNumber();
        long placementHourCap = placeStrategy.getUnitCapsHourNumber();

        PlacementImpressionInfo placementImpressionInfo = AdCapV2Manager.getInstance(context).getPlacementImpressionInfo(placementId);
        int placementHourShowCount = placementImpressionInfo != null ? placementImpressionInfo.hourShowCount : 0;
        int placementDayShowCount = placementImpressionInfo != null ? placementImpressionInfo.dayShowCount : 0;

        //-1 mean no limit
        /**placement cap**/
        if ((placementDayCap != -1 && placementDayShowCount >= placementDayCap)
                || (placementHourCap != -1 && placementHourShowCount >= placementHourCap)) {
            CommonLogUtil.i(TAG, "placement capping error");
            SDKContext.getInstance().runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    if (callback != null) {
                        AdError adError = ErrorCode.getErrorCode(ErrorCode.outOfCapError, "", "");
                        callback.onLoadError(placementId, requestId, adError);
                    }
                }
            });
            mIsLoading = false;

            adTrackingInfo.setmIsLoad(false);
            adTrackingInfo.setmReason(AdTrackingInfo.CAPPING_REASON);
            AdTrackingManager.getInstance(mApplicationContext).addAdTrackingInfo(TrackingV2Loader.AD_SDK_LOAD_TYPE, adTrackingInfo);
            //失败埋点
            AgentEventManager.onAgentForATToAppLoadFail(adTrackingInfo, "Capping.");
            return;
        }

        if (AdPacingManager.getInstance().isPlacementInPacing(placementId, placeStrategy)) {
            CommonLogUtil.i(TAG, "placement pacing error");
            SDKContext.getInstance().runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    if (callback != null) {
                        AdError adError = ErrorCode.getErrorCode(ErrorCode.inPacingError, "", "");
                        callback.onLoadError(placementId, requestId, adError);
                    }
                }
            });
            mIsLoading = false;
            adTrackingInfo.setmIsLoad(false);
            adTrackingInfo.setmReason(AdTrackingInfo.PACCING_REASON);
            AdTrackingManager.getInstance(mApplicationContext).addAdTrackingInfo(TrackingV2Loader.AD_SDK_LOAD_TYPE, adTrackingInfo);

            AgentEventManager.onAgentForATToAppLoadFail(adTrackingInfo, "Pacing.");
            return;
        }


        /**Qualified UnitGroup List**/
        final List<PlaceStrategy.UnitGroupInfo> filterUnitgroupList = new ArrayList<>();
        /**Qualified HeadBidding list**/
        final List<PlaceStrategy.UnitGroupInfo> headBiddingFilterList = new ArrayList<>();

        //Check UnitGroup's cap,pacing,request status --- Normal UnitGroup
        for (PlaceStrategy.UnitGroupInfo unitGroupInfo : resultUnitGroupList) {
            if (isRequestLimit(placementId, adTrackingInfo, placementImpressionInfo, unitGroupInfo)) {
                noFilterGroupList.add(unitGroupInfo);
                continue;
            }

            filterUnitgroupList.add(unitGroupInfo);
        }


        //Check UnitGroup's cap,pacing,request status --- Headbidding UnitGroup
        for (PlaceStrategy.UnitGroupInfo unitGroupInfo : headbiddingList) {

            if (isRequestLimit(placementId, adTrackingInfo, placementImpressionInfo, unitGroupInfo)) {
                noFilterGroupList.add(unitGroupInfo);
                continue;
            }
            headBiddingFilterList.add(unitGroupInfo);
        }

        if (filterUnitgroupList.size() <= 0 && headBiddingFilterList.size() <= 0) {
            CommonLogUtil.i(TAG, "no vail adsource");
            final AdError adError = ErrorCode.getErrorCode(ErrorCode.noVailAdsource, "", "");
            SDKContext.getInstance().runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    if (callback != null) {
                        callback.onLoadError(placementId, requestId, adError);
                    }
                }
            });
            mIsLoading = false;

            adTrackingInfo.setmIsLoad(false);
            adTrackingInfo.setmReason(AdTrackingInfo.NO_VAIL_ADSOURCE_REASON);
            AdTrackingManager.getInstance(mApplicationContext).addAdTrackingInfo(TrackingV2Loader.AD_SDK_LOAD_TYPE, adTrackingInfo);

            AgentEventManager.onAgentForATToAppLoadFail(adTrackingInfo, adError.printStackTrace());
            mIsLoading = false;
            return;
        }


        SDKContext.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (mUpStatus == 1 && !isUpStatusOverTime() && AdCacheManager.getInstance().getCache(context, mPlacementId, mSettings) != null) {
                    if (callback != null) {
                        callback.onAdLoaded(placementId, requestId);
                    }
                    adTrackingInfo.setmIsLoad(false);
                    adTrackingInfo.setmReason(AdTrackingInfo.HAS_OFFER_REASON);
                    AdTrackingManager.getInstance(mApplicationContext).addAdTrackingInfo(TrackingV2Loader.AD_SDK_LOAD_TYPE, adTrackingInfo);
                    AdTrackingManager.getInstance(mApplicationContext).addAdTrackingInfo(TrackingV2Loader.AD_SDK_LOAD_SUCCESS_TYPE, adTrackingInfo);
                    mIsLoading = false;
                } else {
                    TaskManager.getInstance().run_proxy(new Runnable() {
                        @Override
                        public void run() {

                            /**Check HeadBiddingFilterList if has offer in caches**/
                            for (int i = headBiddingFilterList.size() - 1; i >= 0; i--) {
                                PlaceStrategy.UnitGroupInfo hbUgInfo = headBiddingFilterList.get(i);
                                UnitgroupCacheInfo unitgroupCacheInfo = AdCacheManager.getInstance().getUnitgroupCacheInfoByAdSourceId(placementId, hbUgInfo.getUnitId());
                                AdCacheInfo adCacheInfo = unitgroupCacheInfo != null ? unitgroupCacheInfo.getAdCacheInfo() : null;
                                if (adCacheInfo != null && adCacheInfo.isUpStatusAvaiable() && adCacheInfo.isNetworkAdReady()) {
                                    try {
                                        double oldBidEcpm = adCacheInfo.getBaseAdapter().getTrackingInfo().getmBidPrice();
                                        hbUgInfo.setEcpm(oldBidEcpm);
                                        hbUgInfo.setSortType(3); //标记广告源up_status有效并没发起bid request
                                        headBiddingFilterList.remove(hbUgInfo);

                                        /**If cache exist, adding the HeadBidding info to normar list**/
                                        if (filterUnitgroupList.size() == 0) {
                                            filterUnitgroupList.add(hbUgInfo);
                                        } else {
                                            for (int filtIndex = 0; i < filterUnitgroupList.size(); filtIndex++) {
                                                PlaceStrategy.UnitGroupInfo unitGroupInfo = filterUnitgroupList.get(filtIndex);
                                                if (unitGroupInfo.getEcpm() <= hbUgInfo.getEcpm()) {
                                                    filterUnitgroupList.add(filtIndex, hbUgInfo);
                                                    break;
                                                } else {
                                                    if (filtIndex == filterUnitgroupList.size() - 1) {
                                                        filterUnitgroupList.add(hbUgInfo);
                                                    }
                                                }
                                            }
                                        }

                                    } catch (Exception e) {
                                        Log.e(TAG, "Cache Error, need to bid.");
                                    }
                                }
                            }


                            final long startTime = System.currentTimeMillis();
                            try {
                                /**reset unitgroup info level**/
//                                if (filterUnitgroupList != null) {
//                                    for (int i = 0; i < filterUnitgroupList.size(); i++) {
//                                        PlaceStrategy.UnitGroupInfo unitGroupInfo = filterUnitgroupList.get(i);
//                                        unitGroupInfo.level = i;
//                                    }
//                                }

                                HeadBiddingFactory.IHeadBiddingHandler hbHandler = HeadBiddingFactory.createHeadBiddingHandler();
                                if (hbHandler == null) {
                                    throw new Exception("anythink_headbidding.aar doesn't exist");
                                }

                                /**
                                 * Start to Headbidding of UnitGroup
                                 */
                                hbHandler.setTestMode(ATSDK.isNetworkLogDebug());
                                hbHandler.initHbInfo(context, requestId, placementId, placeStrategy.getFormat(), filterUnitgroupList, headBiddingFilterList);
                                hbHandler.startHeadBiddingRequest(new HeadBiddingFactory.IHeadBiddingCallback() {

                                    @Override
                                    public void onResultCallback(List<PlaceStrategy.UnitGroupInfo> resultList, List<PlaceStrategy.UnitGroupInfo> failList) {

                                        if (needTrackHb) {
                                            handleHBTracking(requestId, placeStrategy, startTime, adTrackingInfo.getmRefresh(), resultList, failList, noFilterGroupList);
                                        }


                                        if (resultList == null || resultList.size() == 0) {
                                            AdError adError = ErrorCode.getErrorCode(ErrorCode.noAdsourceConfig, "", "");
                                            callback.onLoadError(placementId, requestId, adError);
                                            adTrackingInfo.setmIsLoad(false);
                                            adTrackingInfo.setmReason(AdTrackingInfo.PLACEMENT_STRATEGY_REASON);

                                            AdTrackingManager.getInstance(mApplicationContext).addAdTrackingInfo(TrackingV2Loader.AD_SDK_LOAD_TYPE, adTrackingInfo);
                                            AgentEventManager.onAgentForATToAppLoadFail(adTrackingInfo, "After Headbidding fail no adsource.");
                                            mIsLoading = false;
                                            return;
                                        }

                                        if (failList != null) {
                                            noFilterGroupList.addAll(failList);
                                        }

                                        placeStrategy.setNoFilterList(noFilterGroupList);

                                        adTrackingInfo.setmIsLoad(true);
                                        AdTrackingManager.getInstance(mApplicationContext).addAdTrackingInfo(TrackingV2Loader.AD_SDK_LOAD_TYPE, adTrackingInfo);

                                        List<PlaceStrategy.UnitGroupInfo> finalAdSourceList = randomTheSameLayerAdsource(placeStrategy, requestId, adTrackingInfo.getmRefresh(), resultList);
                                        if (callback != null) {
                                            callback.onSuccess(placementId, requestId, placeStrategy, finalAdSourceList);
                                        }
                                        mIsLoading = false;
                                    }
                                });

                            } catch (Throwable e) {

                                if (needTrackHb) {
                                    handleHBTracking(requestId, placeStrategy, startTime, adTrackingInfo.getmRefresh(), filterUnitgroupList, headBiddingFilterList, noFilterGroupList);
                                }

                                /**Adding HBList to the noFilterGroupList**/
                                noFilterGroupList.addAll(headBiddingFilterList);
                                placeStrategy.setNoFilterList(noFilterGroupList);

                                adTrackingInfo.setmIsLoad(true);
                                AdTrackingManager.getInstance(mApplicationContext).addAdTrackingInfo(TrackingV2Loader.AD_SDK_LOAD_TYPE, adTrackingInfo);

                                List<PlaceStrategy.UnitGroupInfo> finalAdSourceList = randomTheSameLayerAdsource(placeStrategy, requestId, adTrackingInfo.getmRefresh(), filterUnitgroupList);
                                if (callback != null) {
                                    callback.onSuccess(placementId, requestId, placeStrategy, finalAdSourceList);
                                }

                                mIsLoading = false;
                            }
                        }
                    });
                }
            }
        });
    }


    /**
     * Check: <p>
     * 1、the status of ad placement <p>
     * 2、adsource in waterfall <p>
     * 3、cap for ad placement <p>
     * 4、pacing for ad placement <p>
     * 5、has offer <p>
     * 7、cap and pacing for adsource <p>
     * <p>
     * 8、headbidding <p>
     * 9、get final waterfall <p>
     */
    private void checkToGetWaterfallList(final Context context, final String placementId, final String requestId
            , final PlaceStrategy placeStrategy, final AdTrackingInfo adTrackingInfo, final PlacementCallback callback) {

        List<PlaceStrategy.UnitGroupInfo> originNormalList = PlaceStrategy.parseUnitGroupInfoList(placeStrategy.getNormalUnitGroupListStr());
        List<PlaceStrategy.UnitGroupInfo> originHbList = PlaceStrategy.parseUnitGroupInfoList(placeStrategy.getHeadbiddingUnitGroupListStr());

        /**Unqualified UnitGroup List**/
        final List<PlaceStrategy.UnitGroupInfo> noFilterGroupList;
        /**Qualified UnitGroup List**/
        final List<PlaceStrategy.UnitGroupInfo> resultFilterList;
        /**Qualified HeadBidding list**/
        final List<PlaceStrategy.UnitGroupInfo> hbFilterList;

        try {
            checkIsAdOpen(placeStrategy, adTrackingInfo);
            checkHasAdsource(originNormalList, originHbList, adTrackingInfo);

            PlacementImpressionInfo placementImpressionInfo = AdCapV2Manager.getInstance(context).getPlacementImpressionInfo(placementId);
            checkCapForAdPlacement(placeStrategy, placementImpressionInfo, adTrackingInfo);
            checkPacingForAdPlacement(placeStrategy, adTrackingInfo);

            if (mUpStatus == 1 && !isUpStatusOverTime() && AdCacheManager.getInstance().getCache(context, mPlacementId, mSettings) != null) {
                if (callback != null) {
                    callback.onAdLoaded(placementId, requestId);
                }
                adTrackingInfo.setmIsLoad(false);
                adTrackingInfo.setmReason(AdTrackingInfo.HAS_OFFER_REASON);
                AdTrackingManager.getInstance(mApplicationContext).addAdTrackingInfo(TrackingV2Loader.AD_SDK_LOAD_TYPE, adTrackingInfo);
                AdTrackingManager.getInstance(mApplicationContext).addAdTrackingInfo(TrackingV2Loader.AD_SDK_LOAD_SUCCESS_TYPE, adTrackingInfo);
                mIsLoading = false;
                return;
            }

            noFilterGroupList = new ArrayList<>();
            resultFilterList = new ArrayList<>();
            hbFilterList = new ArrayList<>();

            checkCapAndPacingForAdsource(originNormalList, originHbList,
                    resultFilterList, hbFilterList, noFilterGroupList,
                    placementImpressionInfo, adTrackingInfo);

        } catch (AdStatusException e) {
            onCallbackFail(adTrackingInfo, e, callback);
            return;
        } catch (Throwable e) {
            onCallbackFail(adTrackingInfo, e, callback);
            return;
        }

        //Headbidding Tracking status
        boolean tempNeedTrackHb = false;
        if (originHbList != null && originHbList.size() > 0) {
            tempNeedTrackHb = true;
        }
        final boolean needTrackHb = tempNeedTrackHb;

        final long[] startTime = new long[1];
        try {
            insertHBUnitInfoToNormalList(resultFilterList, hbFilterList, placementId);
            startTime[0] = System.currentTimeMillis();

            HeadBiddingFactory.IHeadBiddingHandler hbHandler = HeadBiddingFactory.createHeadBiddingHandler();
            if (hbHandler == null) {
                throw new Exception("anythink_headbidding.aar doesn't exist");
            }

            /**
             * Start to Headbidding of UnitGroup
             */
            hbHandler.setTestMode(ATSDK.isNetworkLogDebug());
            hbHandler.initHbInfo(context, requestId, placementId, placeStrategy.getFormat(), resultFilterList, hbFilterList);
            hbHandler.startHeadBiddingRequest(new HeadBiddingFactory.IHeadBiddingCallback() {

                @Override
                public void onResultCallback(List<PlaceStrategy.UnitGroupInfo> resultList, List<PlaceStrategy.UnitGroupInfo> failList) {

                    handleResult(startTime[0], needTrackHb, resultList, failList, noFilterGroupList, placeStrategy, adTrackingInfo, callback);
                }
            });

        } catch (Throwable e) {
            handleResult(startTime[0], needTrackHb, resultFilterList, hbFilterList, noFilterGroupList, placeStrategy, adTrackingInfo, callback);
        }
    }

    /**
     * Check whether the status of placement is open
     */
    private void checkIsAdOpen(PlaceStrategy placeStrategy, AdTrackingInfo adTrackingInfo) {
        if (!placeStrategy.isAdOpen()) {
            adTrackingInfo.setmReason(AdTrackingInfo.PLACEMENT_STRATEGY_REASON);

            throw new AdStatusException(ErrorCode.getErrorCode(ErrorCode.placementAdClose, "", ""), "Strategy is close.");
        }
    }

    /**
     * Check whether has any adsource
     */
    private void checkHasAdsource(List normalList, List hbList, AdTrackingInfo adTrackingInfo) {
        if ((normalList == null || normalList.size() == 0) && (hbList == null || hbList.size() == 0)) {
            CommonLogUtil.i(TAG, "unitgroup list is null");
            adTrackingInfo.setmReason(AdTrackingInfo.PLACEMENT_STRATEGY_REASON);

            throw new AdStatusException(ErrorCode.getErrorCode(ErrorCode.noAdsourceConfig, "", ""), "No Adsource.");
        }
    }

    /**
     * Check cap for placement
     */
    private void checkCapForAdPlacement(PlaceStrategy placeStrategy, PlacementImpressionInfo placementImpressionInfo, AdTrackingInfo adTrackingInfo) {
        long placementDayCap = placeStrategy.getUnitCapsDayNumber();
        long placementHourCap = placeStrategy.getUnitCapsHourNumber();

        int placementHourShowCount = placementImpressionInfo != null ? placementImpressionInfo.hourShowCount : 0;
        int placementDayShowCount = placementImpressionInfo != null ? placementImpressionInfo.dayShowCount : 0;

        //-1 mean no limit
        /**placement cap**/
        if ((placementDayCap != -1 && placementDayShowCount >= placementDayCap)
                || (placementHourCap != -1 && placementHourShowCount >= placementHourCap)) {
            CommonLogUtil.i(TAG, "placement capping error");

            adTrackingInfo.setmReason(AdTrackingInfo.CAPPING_REASON);

            throw new AdStatusException(ErrorCode.getErrorCode(ErrorCode.outOfCapError, "", ""), "Capping.");
        }
    }

    /**
     * Check pacing for placement
     */
    private void checkPacingForAdPlacement(PlaceStrategy placeStrategy, AdTrackingInfo adTrackingInfo) {
        if (AdPacingManager.getInstance().isPlacementInPacing(adTrackingInfo.getmPlacementId(), placeStrategy)) {
            CommonLogUtil.i(TAG, "placement pacing error");
            adTrackingInfo.setmReason(AdTrackingInfo.PACCING_REASON);

            throw new AdStatusException(ErrorCode.getErrorCode(ErrorCode.inPacingError, "", ""), "Pacing.");
        }
    }

    /**
     * Check cap and pacing for adsource
     */
    private void checkCapAndPacingForAdsource(List<PlaceStrategy.UnitGroupInfo> originNormalList, List<PlaceStrategy.UnitGroupInfo> originHbList,
                                              List<PlaceStrategy.UnitGroupInfo> filterList, List<PlaceStrategy.UnitGroupInfo> hbFilterList,
                                              List<PlaceStrategy.UnitGroupInfo> noFilterGroupList, PlacementImpressionInfo placementImpressionInfo,
                                              AdTrackingInfo adTrackingInfo) {

        //Check UnitGroup's cap,pacing,request status --- Normal UnitGroup
        for (PlaceStrategy.UnitGroupInfo unitGroupInfo : originNormalList) {
            if (isRequestLimit(adTrackingInfo.getmPlacementId(), adTrackingInfo, placementImpressionInfo, unitGroupInfo)) {
                noFilterGroupList.add(unitGroupInfo);
                continue;
            }

            filterList.add(unitGroupInfo);
        }


        //Check UnitGroup's cap,pacing,request status --- Headbidding UnitGroup
        for (PlaceStrategy.UnitGroupInfo unitGroupInfo : originHbList) {

            if (isRequestLimit(adTrackingInfo.getmPlacementId(), adTrackingInfo, placementImpressionInfo, unitGroupInfo)) {
                noFilterGroupList.add(unitGroupInfo);
                continue;
            }
            hbFilterList.add(unitGroupInfo);
        }

        if (filterList.size() <= 0 && hbFilterList.size() <= 0) {
            CommonLogUtil.i(TAG, "no vail adsource");

            adTrackingInfo.setmReason(AdTrackingInfo.NO_VAIL_ADSOURCE_REASON);

            AdError adError = ErrorCode.getErrorCode(ErrorCode.noVailAdsource, "", "");
            throw new AdStatusException(adError, adError.printStackTrace());
        }
    }

    private void insertHBUnitInfoToNormalList(List<PlaceStrategy.UnitGroupInfo> filterUnitgroupList, List<PlaceStrategy.UnitGroupInfo> headBiddingFilterList, String placementId) {

        if (filterUnitgroupList == null || headBiddingFilterList == null) {
            return;
        }


        /**Check HeadBiddingFilterList if has offer in caches**/
        for (int i = headBiddingFilterList.size() - 1; i >= 0; i--) {
            PlaceStrategy.UnitGroupInfo hbUgInfo = headBiddingFilterList.get(i);
            UnitgroupCacheInfo unitgroupCacheInfo = AdCacheManager.getInstance().getUnitgroupCacheInfoByAdSourceId(placementId, hbUgInfo.getUnitId());
            AdCacheInfo adCacheInfo = unitgroupCacheInfo != null ? unitgroupCacheInfo.getAdCacheInfo() : null;
            if (adCacheInfo != null && adCacheInfo.isUpStatusAvaiable() && adCacheInfo.isNetworkAdReady()) {
                try {
                    double oldBidEcpm = adCacheInfo.getBaseAdapter().getTrackingInfo().getmBidPrice();
                    hbUgInfo.setEcpm(oldBidEcpm);
                    hbUgInfo.setSortType(3); //标记广告源up_status有效并没发起bid request
                    headBiddingFilterList.remove(hbUgInfo);

                    /**If cache exist, adding the HeadBidding info to normar list**/
                    if (filterUnitgroupList.size() == 0) {
                        filterUnitgroupList.add(hbUgInfo);
                    } else {
                        for (int filtIndex = 0; i < filterUnitgroupList.size(); filtIndex++) {
                            PlaceStrategy.UnitGroupInfo unitGroupInfo = filterUnitgroupList.get(filtIndex);
                            if (unitGroupInfo.getEcpm() <= hbUgInfo.getEcpm()) {
                                filterUnitgroupList.add(filtIndex, hbUgInfo);
                                break;
                            } else {
                                if (filtIndex == filterUnitgroupList.size() - 1) {
                                    filterUnitgroupList.add(hbUgInfo);
                                }
                            }
                        }
                    }

                } catch (Exception e) {
                    Log.e(TAG, "Cache Error, need to bid.");
                }
            }
        }
    }

    private void handleResult(long startTime, boolean needTrackHb,
                              List<PlaceStrategy.UnitGroupInfo> resultList, List<PlaceStrategy.UnitGroupInfo> failList,
                              List<PlaceStrategy.UnitGroupInfo> noFilterGroupList, PlaceStrategy placeStrategy,
                              AdTrackingInfo adTrackingInfo, PlacementCallback callback) {

        String requestId = adTrackingInfo.getmRequestId();
        String placementId = adTrackingInfo.getmPlacementId();

        if (needTrackHb) {
            handleHBTracking(requestId, placeStrategy, startTime, adTrackingInfo.getmRefresh(), resultList, failList, noFilterGroupList);
        }

        if (resultList == null || resultList.size() == 0) {
            AdError adError = ErrorCode.getErrorCode(ErrorCode.noVailAdsource, "", "");
            adTrackingInfo.setmReason(AdTrackingInfo.NO_VAIL_ADSOURCE_REASON);

            onCallbackFail(adTrackingInfo, adError, "After Headbidding fail no adsource.", callback);
            return;
        }

        if (failList != null) {
            noFilterGroupList.addAll(failList);
        }
        placeStrategy.setNoFilterList(noFilterGroupList);

        List<PlaceStrategy.UnitGroupInfo> finalAdSourceList = randomTheSameLayerAdsource(placeStrategy, requestId, adTrackingInfo.getmRefresh(), resultList);

        placeStrategy.updateSortUnitgroupList(finalAdSourceList);

        AdCacheManager.getInstance().putPlacementStrategy(placementId, placeStrategy);

        adTrackingInfo.setmIsLoad(true);
        AdTrackingManager.getInstance(mApplicationContext).addAdTrackingInfo(TrackingV2Loader.AD_SDK_LOAD_TYPE, adTrackingInfo);

        if (callback != null) {
            callback.onSuccess(placementId, requestId, placeStrategy, finalAdSourceList);
        }

        mIsLoading = false;
    }


    private void onCallbackFail(final AdTrackingInfo adTrackingInfo, Throwable e, final PlacementCallback callback) {

        final AdError adError;
        final String reason;
        if (e instanceof AdStatusException) {
            adError = ((AdStatusException) e).adError;
            reason = ((AdStatusException) e).reason;
        } else {
            adError = ErrorCode.getErrorCode(ErrorCode.exception, "", e.getMessage());
            reason = e.getMessage();
        }

        onCallbackFail(adTrackingInfo, adError, reason, callback);
    }

    private void onCallbackFail(final AdTrackingInfo adTrackingInfo, final AdError adError, String reason, final PlacementCallback callback) {
        SDKContext.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (callback != null) {
                    callback.onLoadError(adTrackingInfo.getmPlacementId(), adTrackingInfo.getmRequestId(), adError);
                }
            }
        });
        mIsLoading = false;
        adTrackingInfo.setmIsLoad(false);
        AdTrackingManager.getInstance(mApplicationContext).addAdTrackingInfo(TrackingV2Loader.AD_SDK_LOAD_TYPE, adTrackingInfo);

        AgentEventManager.onAgentForATToAppLoadFail(adTrackingInfo, reason);
    }

    /**
     * Check Unitgroup status
     *
     * @param placementId
     * @param adTrackingInfo
     * @param placementImpressionInfo
     * @param unitGroupInfo
     * @return
     */
    private boolean isRequestLimit(String placementId, AdTrackingInfo adTrackingInfo, PlacementImpressionInfo placementImpressionInfo, PlaceStrategy.UnitGroupInfo unitGroupInfo) {
        PlacementImpressionInfo.AdSourceImpressionInfo adSourceImpressionInfo = placementImpressionInfo != null ? placementImpressionInfo.getAdSourceImpressInfo(unitGroupInfo.unitId) : null;

        int adsourceHourShowTime = adSourceImpressionInfo != null ? adSourceImpressionInfo.hourShowCount : 0;
        int adsourceDayShowTime = adSourceImpressionInfo != null ? adSourceImpressionInfo.dayShowCount : 0;

        if (unitGroupInfo.capsByDay != -1 && adsourceDayShowTime >= unitGroupInfo.capsByDay) {
            unitGroupInfo.setErrorMsg("Out of Cap");
            NetworkLogUtil.adsourceLog(placementId, adTrackingInfo, "Out of Cap", unitGroupInfo, adsourceHourShowTime, adsourceDayShowTime);
            AgentEventManager.onAdsourceLoadFail(adTrackingInfo, unitGroupInfo, -1, 2, ErrorCode.getErrorCode(ErrorCode.outOfCapError, "", "Out of Cap"), 0);
            return true;
        }

        if (unitGroupInfo.capsByHour != -1 && adsourceHourShowTime >= unitGroupInfo.capsByHour) {
            unitGroupInfo.setErrorMsg("Out of Cap");
            NetworkLogUtil.adsourceLog(placementId, adTrackingInfo, "Out of Cap", unitGroupInfo, adsourceHourShowTime, adsourceDayShowTime);
            AgentEventManager.onAdsourceLoadFail(adTrackingInfo, unitGroupInfo, -1, 2, ErrorCode.getErrorCode(ErrorCode.outOfCapError, "", "Out of Cap"), 0);
            return true;
        }

        if (AdPacingManager.getInstance().isUnitGroupInPacing(placementId, unitGroupInfo)) {
            unitGroupInfo.setErrorMsg("Out of Pacing");
            NetworkLogUtil.adsourceLog(placementId, adTrackingInfo, "Out of Pacing", unitGroupInfo, adsourceHourShowTime, adsourceDayShowTime);
            AgentEventManager.onAdsourceLoadFail(adTrackingInfo, unitGroupInfo, -1, 3, ErrorCode.getErrorCode(ErrorCode.inPacingError, "", "Out of Pacing"), 0);
            return true;
        }

        if (AdSourceRequestFailManager.getInstance().isInRequestFailInterval(unitGroupInfo)) {
            unitGroupInfo.setErrorMsg("Request fail in pacing");
            NetworkLogUtil.adsourceLog(placementId, adTrackingInfo, "Request fail in pacing", unitGroupInfo, adsourceHourShowTime, adsourceDayShowTime);
            AgentEventManager.onAdsourceLoadFail(adTrackingInfo, unitGroupInfo, -1, 4, ErrorCode.getErrorCode(ErrorCode.inRequestFailPacing, "", "Request fail in pacing"), 0);
            return true;
        }
        return false;
    }

    /**
     * Randowm to sort the same ecpm adsource
     *
     * @param placeStrategy
     * @param requestId
     * @param isRefresh
     * @param unitGroupInfos
     * @return
     */
    private List<PlaceStrategy.UnitGroupInfo> randomTheSameLayerAdsource(PlaceStrategy placeStrategy, String requestId, int isRefresh, List<PlaceStrategy.UnitGroupInfo> unitGroupInfos) {

        JSONArray jsonArray = new JSONArray();

        AdTrackingInfo adTrackingInfo = new AdTrackingInfo();
        adTrackingInfo.setmPlacementId(mPlacementId);
        adTrackingInfo.setmRequestId(requestId);
        adTrackingInfo.setmAdType(placeStrategy.getFormat() + "");
        adTrackingInfo.setAsid(placeStrategy.getAsid());
        adTrackingInfo.setmRefresh(isRefresh);
        adTrackingInfo.setmTrafficGroupId(placeStrategy.getTracfficGroupId());
        adTrackingInfo.setmGroupId(placeStrategy.getGroupId());

        List<PlaceStrategy.UnitGroupInfo> randomResultList = new ArrayList<>();
        LinkedHashMap<String, List<PlaceStrategy.UnitGroupInfo>> layerAdSourceMap = new LinkedHashMap<>();
        for (PlaceStrategy.UnitGroupInfo unitGroupInfo : unitGroupInfos) {
            double ecpm = unitGroupInfo.getEcpm();
            List<PlaceStrategy.UnitGroupInfo> layerList = layerAdSourceMap.get(String.valueOf(ecpm));
            if (layerList == null) {
                layerList = new ArrayList<PlaceStrategy.UnitGroupInfo>();
                layerAdSourceMap.put(String.valueOf(ecpm), layerList);
            }
            layerList.add(unitGroupInfo);
        }

        for (List<PlaceStrategy.UnitGroupInfo> layerList : layerAdSourceMap.values()) {
            Collections.shuffle(layerList);
        }

        CommonLogUtil.e(TAG, "Request UnitGroup's Number:" + placeStrategy.getRequestUnitGroupNumber());
        int level = 0;
        for (String key : layerAdSourceMap.keySet()) {
            List<PlaceStrategy.UnitGroupInfo> layerList = layerAdSourceMap.get(key);
            for (PlaceStrategy.UnitGroupInfo unitGroupInfo : layerList) {
                unitGroupInfo.level = level;
                unitGroupInfo.setRequestLayerLevel(level / placeStrategy.getRequestUnitGroupNumber() + 1);
                CommonLogUtil.e(TAG, "UnitGroupInfo level:" + level + " || " + "layer:" + unitGroupInfo.getRequestLayLevel());
                level++;
                randomResultList.add(unitGroupInfo);

                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("sortpriority", unitGroupInfo.level);
                    jsonObject.put("sorttype", unitGroupInfo.bidType == 1 ? unitGroupInfo.sortType : 1); //如果是非Bid的，标记sortType为1，Bid的AdSource使用自身的sortType
                    jsonObject.put("unit_id", unitGroupInfo.getUnitId());
                    jsonObject.put("bidresult", 1);
                    jsonObject.put("bidprice", unitGroupInfo.getEcpm());
                    jsonArray.put(jsonObject);
                } catch (Exception e) {

                }
            }
        }

        adTrackingInfo.setmHbResultList(jsonArray.toString());

        AdTrackingManager.getInstance(mApplicationContext).addAdTrackingInfo(TrackingV2Loader.ADSOURCE_SORT_TYPE, adTrackingInfo);

        return randomResultList;

    }


    protected CommonCacheCountdownTimer mCacheCountdownTimer;
    int mCurrentLevel = -1;
    String mCountDownRequestId = "";

    /**
     * Cache's countdown
     *
     * @param baseAdapter
     * @param requestId
     * @param level
     */
    public void prepareCountdown(final AnyThinkBaseAdapter baseAdapter, final String requestId, final int level) {

        SDKContext.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                AdTrackingInfo adTrackingInfo = baseAdapter.getTrackingInfo();
                PlaceStrategy.UnitGroupInfo unitGroupInfo = baseAdapter.getmUnitgroupInfo();

                /**Only the height level unitgoup will be countdown**/
                if (mCurrentLevel != -1 && mCurrentLevel < level && mCountDownRequestId.equals(requestId)) {
                    return;
                }

                if (adTrackingInfo == null || unitGroupInfo == null) {
                    return;
                }

                if (adTrackingInfo.getmNetworkType() == 35) { //MyOffer shouldn't to countdown
                    return;
                }

                mCurrentLevel = level;
                mCountDownRequestId = requestId;

                if (mCacheCountdownTimer != null) {
                    mCacheCountdownTimer.cancel();
                    mCacheCountdownTimer = null;
                }

                startCountdown(unitGroupInfo, adTrackingInfo);
            }
        });

    }

    public void cancelCountdown() {
        SDKContext.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (mCacheCountdownTimer != null) {
                    mCacheCountdownTimer.cancel();
                    mCacheCountdownTimer = null;
                }
            }
        });
    }

    /**
     * Override by AdType
     **/
    public abstract void startCountdown(PlaceStrategy.UnitGroupInfo unitGroupInfo, AdTrackingInfo adTrackingInfo);


    /**
     * Create Requestid
     *
     * @return
     */
    public String createRequestId(Context context) {
        String upId = SDKContext.getInstance().getUpId();

        StringBuffer requestIdBuffer = new StringBuffer();
        requestIdBuffer.append(CommonDeviceUtil.getAndroidID(context));
        requestIdBuffer.append("&");
        requestIdBuffer.append(CommonDeviceUtil.getGoogleAdId());
        requestIdBuffer.append("&");
        requestIdBuffer.append(upId);
        requestIdBuffer.append("&");
        requestIdBuffer.append(System.currentTimeMillis());
        requestIdBuffer.append("&");
        requestIdBuffer.append(new Random().nextInt(10000));

        String requestId = CommonMD5.getMD5(requestIdBuffer.toString());

        return requestId;
    }

    public void cancelReturnCache(AdCacheInfo cacheInfo) {
        if (cacheInfo.isLast()) {
            if (mCurrentManager != null) {
                mCurrentManager.cancelCacheOffer();
            }
            mUpStatus = 0;
        }
    }

    @Deprecated
    public void clean() {
    }

    public boolean isAdReady(Context context) {
        AdCacheInfo adCacheInfo = isAdReady(context, false);
        return adCacheInfo != null;
    }

    protected AdCacheInfo isAdReady(Context context, boolean isShowCall) {

        PlaceStrategy placeStrategy = AdCacheManager.getInstance().getCachePlacementStrategy(mPlacementId);

        CommonAdManager adManager = CommonAdManager.getInstance(mPlacementId);
        String currentRequestId = adManager != null ? adManager.getCurrentRequestId() : "";

        if (placeStrategy == null) {
            placeStrategy = PlaceStrategyManager.getInstance(mApplicationContext).getPlaceStrategyByAppIdAndPlaceId(mPlacementId);
        }

        if (placeStrategy == null) {

            AdTrackingInfo trackingInfoForAgent = TrackingInfoUtil.getTrackingInfoForAgent("", "", mPlacementId, "", "", null, 0);
            if (isShowCall) {
                AgentEventManager.onAdShowFail(trackingInfoForAgent, AgentEventManager.PLACEMENT_STRATEGY_NULL_REASON, "", currentRequestId);
            } else {
                AgentEventManager.isReadyEventAgentForATToApp(trackingInfoForAgent, false, AgentEventManager.PLACEMENT_STRATEGY_NULL_REASON, -1, "", -1, "", "", currentRequestId, false, "");
            }
            return null;
        }

        //paccing
        if (AdPacingManager.getInstance().isPlacementInPacing(mPlacementId, placeStrategy)) {

            AdTrackingInfo trackingInfoForAgent = TrackingInfoUtil.getTrackingInfoForAgent(String.valueOf(placeStrategy.getFormat()), "", mPlacementId, SDKContext.getInstance().getPsid(), SDKContext.getInstance().getSessionId(mPlacementId), placeStrategy, 0);
            if (isShowCall) {
                AgentEventManager.onAdShowFail(trackingInfoForAgent, AgentEventManager.PLACEMENT_PACCING_REASON, "", currentRequestId);
            } else {
                AgentEventManager.isReadyEventAgentForATToApp(trackingInfoForAgent, false, AgentEventManager.PLACEMENT_PACCING_REASON, -1, "", -1, "", "", currentRequestId, false, "");
            }
            return null;
        }

        //capping
        if (AdCapV2Manager.getInstance(mApplicationContext).isPlacementOutOfCap(placeStrategy, mPlacementId)) {

            AdTrackingInfo trackingInfoForAgent = TrackingInfoUtil.getTrackingInfoForAgent(String.valueOf(placeStrategy.getFormat()), "", mPlacementId, SDKContext.getInstance().getPsid(), SDKContext.getInstance().getSessionId(mPlacementId), placeStrategy, 0);
            if (isShowCall) {
                AgentEventManager.onAdShowFail(trackingInfoForAgent, AgentEventManager.PLACEMENT_CAPPING_REASON, "", currentRequestId);
            } else {
                AgentEventManager.isReadyEventAgentForATToApp(trackingInfoForAgent, false, AgentEventManager.PLACEMENT_CAPPING_REASON, -1, "", -1, "", "", currentRequestId, false, "");
            }
            return null;
        }

        AdCacheInfo adCacheInfo = AdCacheManager.getInstance().checkCache(context, mPlacementId, true, isShowCall, mSettings);

        return adCacheInfo;
    }


    /**
     * Bidding Tracking
     *
     * @param successList
     * @param headbiddingFailList
     * @param noFilterList
     */
    protected void handleHBTracking(String requestId, PlaceStrategy strategy, long startTime, int isRefresh, List<PlaceStrategy.UnitGroupInfo> successList, List<PlaceStrategy.UnitGroupInfo> headbiddingFailList, List<PlaceStrategy.UnitGroupInfo> noFilterList) {
        JSONArray jsonArray = new JSONArray();
        AdTrackingInfo adTrackingInfo = new AdTrackingInfo();
        adTrackingInfo.setmPlacementId(mPlacementId);
        adTrackingInfo.setmRequestId(requestId);
        adTrackingInfo.setmAdType(strategy.getFormat() + "");
        adTrackingInfo.setAsid(strategy.getAsid());
        adTrackingInfo.setmRefresh(isRefresh);
        adTrackingInfo.setmHbStartTime(startTime);
        adTrackingInfo.setmHbEndTime(System.currentTimeMillis());
        adTrackingInfo.setmTrafficGroupId(strategy.getTracfficGroupId());
        adTrackingInfo.setmGroupId(strategy.getGroupId());

        try {
            if (successList != null) {
                for (int i = 0; i < successList.size(); i++) {
                    PlaceStrategy.UnitGroupInfo unitGroupInfo = successList.get(i);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("sortpriority", i);
                    jsonObject.put("sorttype", unitGroupInfo.bidType == 1 ? unitGroupInfo.sortType : 1); //如果是非Bid的，标记sortType为1，Bid的AdSource使用自身的sortType
                    jsonObject.put("unit_id", unitGroupInfo.getUnitId());
                    jsonObject.put("bidresult", 1);
                    jsonObject.put("bidprice", unitGroupInfo.getEcpm());
                    jsonArray.put(jsonObject);
                }
            }

            if (headbiddingFailList != null) {
                for (int i = 0; i < headbiddingFailList.size(); i++) {
                    PlaceStrategy.UnitGroupInfo unitGroupInfo = headbiddingFailList.get(i);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("sortpriority", -1);
                    jsonObject.put("sorttype", -1);
                    jsonObject.put("unit_id", unitGroupInfo.getUnitId());
                    jsonObject.put("bidresult", 0);
                    jsonObject.put("bidprice", unitGroupInfo.bidType == 1 ? 0 : unitGroupInfo.getEcpm());
                    jsonObject.put("errormsg", unitGroupInfo.getErrorMsg());
                    jsonArray.put(jsonObject);
                }
            }

            if (noFilterList != null) {
                for (int i = 0; i < noFilterList.size(); i++) {
                    PlaceStrategy.UnitGroupInfo unitGroupInfo = noFilterList.get(i);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("sortpriority", -1);
                    jsonObject.put("sorttype", -1);
                    jsonObject.put("unit_id", unitGroupInfo.getUnitId());
                    jsonObject.put("bidresult", 0);
                    jsonObject.put("bidprice", unitGroupInfo.bidType == 1 ? 0 : unitGroupInfo.getEcpm());
                    jsonObject.put("errormsg", unitGroupInfo.getErrorMsg());
                    jsonArray.put(jsonObject);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        adTrackingInfo.setmHbResultList(jsonArray.toString());

        AdTrackingManager.getInstance(mApplicationContext).addAdTrackingInfo(TrackingV2Loader.AD_HEADERBIDDING_TYPE, adTrackingInfo);

    }

    /**
     * Extra Setting
     **/
    public synchronized void addSetting(int networkType, ATMediationSetting setting) {
        if (mSettings == null) {
            mSettings = new HashMap<>();
        }
        mSettings.put(networkType, setting);
    }

    public interface PlacementCallback {
        void onSuccess(String placementId, String requestId, PlaceStrategy placeStrategy, List<PlaceStrategy.UnitGroupInfo> unitGroupInfoList);

        void onAdLoaded(String placementId, String requestId);

        void onLoadError(String placementId, String requestId, AdError adError);

    }
}
