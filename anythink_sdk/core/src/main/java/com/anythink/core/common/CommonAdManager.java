package com.anythink.core.common;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.anythink.core.api.ATBaseAdAdapter;
import com.anythink.core.api.ATSDK;
import com.anythink.core.api.AdError;
import com.anythink.core.api.ErrorCode;
import com.anythink.core.cap.AdCapV2Manager;
import com.anythink.core.cap.AdLoadCapManager;
import com.anythink.core.cap.AdPacingManager;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.entity.AdCacheInfo;
import com.anythink.core.common.entity.AdTrackingInfo;
import com.anythink.core.common.entity.PlacementImpressionInfo;
import com.anythink.core.common.entity.S2SHBResponse;
import com.anythink.core.common.entity.UnitgroupCacheInfo;
import com.anythink.core.common.hb.HBS2SCacheManager;
import com.anythink.core.common.net.TrackingV2Loader;
import com.anythink.core.common.track.AdTrackingManager;
import com.anythink.core.common.track.AgentEventManager;
import com.anythink.core.common.utils.CommonLogUtil;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public abstract class CommonAdManager<T extends FormatLoadParams> {
    private final String TAG = getClass().getSimpleName();

    protected Context mApplicationContext;
    protected WeakReference<Context> mActivityRef;
    protected String mPlacementId;

    protected ConcurrentHashMap<String, CommonMediationManager> mHistoryMediationManager;

    protected int mUpStatus = 0;

    protected boolean mIsLoading;

    private long mUpStatusSetTime;

    private long mUpStatusOverTime; //upstatus's out-date time

    protected String mRequestId = ""; //Only use for get the newest MediationManager

//    boolean forceCanceRequestAdSource = false; //Only for Splash

    private boolean isInLoadFailInterval;

    private long loadFailStartTime;

    private boolean hasSendLoadFailIntervalLog = false;


    public CommonAdManager(Context context, String placementId) {
        mActivityRef = new WeakReference<>(context);
        mApplicationContext = context.getApplicationContext();
        mPlacementId = placementId;

        mHistoryMediationManager = new ConcurrentHashMap<>(5);

        if (SDKContext.getInstance().getContext() == null) {
            SDKContext.getInstance().setContext(mApplicationContext);
        }
    }


    /**
     * Set Loaded Status
     */
    public void setLoaded(String asid) {
        PlaceStrategy placeStrategy = PlaceStrategyManager.getInstance(mApplicationContext).getPlaceStrategyByAppIdAndPlaceId(mPlacementId);
        if (TextUtils.equals(asid, placeStrategy.getAsid())) {
            mUpStatus = 1;
            mUpStatusSetTime = System.currentTimeMillis();
            //Reset load fail error limit info
            isInLoadFailInterval = false;
            loadFailStartTime = 0;
            hasSendLoadFailIntervalLog = false;
        } else {
            mUpStatus = 0;
        }

    }

    /**
     * Set Load Fail Status（Only for Placement Strategy exist）
     *
     * @return
     */
    public void setLoadFail(AdError adError) {
        final PlaceStrategy placeStrategy = PlaceStrategyManager.getInstance(mApplicationContext).getPlaceStrategyInCache(mPlacementId);
        if (!isInLoadFailInterval && placeStrategy != null) {
            isInLoadFailInterval = true;
            loadFailStartTime = System.currentTimeMillis();
        }
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
        mHistoryMediationManager.remove(requestId);
    }


    /**
     *
     */
    public void notifyMediationManagerImpression(String requestId, double impressionEcpm) {
        CommonMediationManager mediationManager = mHistoryMediationManager.get(requestId);
        if (mediationManager != null) {
            mediationManager.notifyImpression(impressionEcpm);
        }
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
        if (mIsLoading) {
            return true;
        }
        if (!TextUtils.isEmpty(mRequestId)) {
            CommonMediationManager mediationManager = mHistoryMediationManager.get(mRequestId);
            if (mediationManager != null && !mediationManager.hasFinishLoad()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Request Placement Setting
     *
     * @param context
     * @param mPlacementId
     */
    public void startLoadAd(final Context context, final String format, final String mPlacementId, final T formatLoadParams) {
        TaskManager.getInstance().run_proxy(new Runnable() {
            @Override
            public void run() {
                synchronized (CommonAdManager.this) {
                    SDKContext.getInstance().checkAppStrategy(context, SDKContext.getInstance().getAppId(), SDKContext.getInstance().getAppKey());
                    //Create RequestId
                    final String requestId = CommonSDKUtil.createRequestId(context);

                    //If init error, callback error and don't to send tk and agent.
                    if (SDKContext.getInstance().getContext() == null
                            || TextUtils.isEmpty(SDKContext.getInstance().getAppId())
                            || TextUtils.isEmpty(SDKContext.getInstance().getAppKey())
                            || CommonUtil.isNullOrEmpty(mPlacementId)) {
                        SDKContext.getInstance().runOnMainThread(new Runnable() {
                            @Override
                            public void run() {
                                //It would not to send tk and agent, so it use onCallbacInternalError to callback error
                                AdError adError = ErrorCode.getErrorCode(ErrorCode.appIdOrPlaceIdEmpty, "", "");
                                onCallbacInternalError(formatLoadParams, mPlacementId, requestId, adError);
                            }
                        });

                        if (SDKContext.getInstance().isNetworkLogDebug()) {
                            Log.e(Const.RESOURCE_HEAD, "Please check these params in your code (AppId: " + SDKContext.getInstance().getAppId() + ", AppKey: " + SDKContext.getInstance().getAppKey() + ", PlacementId: " + mPlacementId + ")");
                        }
                        mIsLoading = false;
                        return;
                    }

                    /**If default network is loading, it would callback error.**/
                    if (isInDefaultAdSourceLoading()) {
                        AdError adError = ErrorCode.getErrorCode(ErrorCode.loadingError, "", "");
                        onCallbacInternalError(formatLoadParams, mPlacementId, requestId, adError);
                        return;
                    }

                    final Context mApplicationContext = context.getApplicationContext();
                    final String mAppId = SDKContext.getInstance().getAppId();
                    final String mAppKey = SDKContext.getInstance().getAppKey();
                    final String mPsid = SDKContext.getInstance().getPsid();
                    final String mSessionId = SDKContext.getInstance().getSessionId(mPlacementId);

                    final PlaceStrategy placeStrategy = PlaceStrategyManager.getInstance(mApplicationContext).getPlaceStrategyByAppIdAndPlaceId(mPlacementId);

                    final String oldAsid = placeStrategy != null ? placeStrategy.getAsid() : "";

                    final AdTrackingInfo adTrackingInfo = TrackingInfoUtil.getTrackingInfoForAgent(format, requestId, mPlacementId, mPsid, mSessionId, placeStrategy, formatLoadParams.isRefresh ? 1 : 0);


                    /**No placement strategy to request default AdSource.**/
                    if (placeStrategy == null && formatLoadParams.defaultRequestInfo != null) {
                        Log.i(Const.RESOURCE_HEAD, "request default adsource for splash.");
                        if (startDefaultAdSourceLoading(mPlacementId, requestId, formatLoadParams)) {
                            PlaceStrategyManager.getInstance(context).requestStrategy(null, mAppId, mAppKey, mPlacementId, null);
                            return;
                        }
                    }


                    /**Before request to check upstatus and offer.**/
                    if ((!format.equals(Const.FORMAT.SPLASH_FORMAT)) && mUpStatus == 1 && !isUpStatusOverTime() && AdCacheManager.getInstance().getCache(context, mPlacementId) != null) {
                        ShowWaterfallManager.getInstance().finishFinalWaterFall(mPlacementId, requestId);
                        onCallbackOfferHasExist(formatLoadParams, mPlacementId, requestId);
                        adTrackingInfo.setmIsLoad(false);
                        adTrackingInfo.setmReason(AdTrackingInfo.HAS_OFFER_REASON);
                        AdTrackingManager.getInstance(mApplicationContext).addAdTrackingInfo(TrackingV2Loader.AD_SDK_LOAD_TYPE, adTrackingInfo);
                        AdTrackingManager.getInstance(mApplicationContext).addAdTrackingInfo(TrackingV2Loader.AD_SDK_LOAD_SUCCESS_TYPE, adTrackingInfo);
                        mIsLoading = false;
                        return;
                    }

                    //If placement in load fail pacing, it would return error.
                    if (placeStrategy != null && isInLoadFailInterval) {
                        long interval = System.currentTimeMillis() - loadFailStartTime;
                        if (interval > 0 && interval < placeStrategy.getLoadFailWaitTime()) {
                            AdError adError = ErrorCode.getErrorCode(ErrorCode.loadFailInPacingError, "", "");
                            adTrackingInfo.setmReason(AdTrackingInfo.LOAD_FAIL_PACING_REASON);
                            boolean neeoToSendTK = !hasSendLoadFailIntervalLog;
                            onCallbackFail(neeoToSendTK, adTrackingInfo, new AdStatusException(adError, adError.printStackTrace()), formatLoadParams);
                            hasSendLoadFailIntervalLog = true;
                            return;
                        }
                    }

                    //Reset load fail error limit info
                    isInLoadFailInterval = false;
                    loadFailStartTime = 0;
                    hasSendLoadFailIntervalLog = false;

                    //Load Cap limit
                    if (placeStrategy != null && AdLoadCapManager.getInstance().isInLoadCapping(mApplicationContext, mPlacementId, placeStrategy)) {
                        AdError adError = ErrorCode.getErrorCode(ErrorCode.loadCappingError, "", "");
                        adTrackingInfo.setmReason(AdTrackingInfo.LOAD_CAPPING_REASON);
                        onCallbackFail(true, adTrackingInfo, new AdStatusException(adError, adError.printStackTrace()), formatLoadParams);
                        return;
                    }

                    if (isLoading()) {
                        AdError adError = ErrorCode.getErrorCode(ErrorCode.loadingError, "", "");
                        adTrackingInfo.setmReason(AdTrackingInfo.LOADING_REASON);

                        onCallbackFail(true, adTrackingInfo, new AdStatusException(adError, adError.printStackTrace()), formatLoadParams);
                        return;
                    }

                    mIsLoading = true;

                    /**Cancel return callback to deveploper**/
                    for (CommonMediationManager mediationManager : mHistoryMediationManager.values()) {
                        mediationManager.notifyCancelReturnResult();
                    }

                    PlaceStrategyManager.getInstance(context).requestStrategy(placeStrategy, mAppId, mAppKey, mPlacementId, new PlaceStrategyManager.StrategyloadListener() {
                        @Override
                        public void loadStrategySuccess(final PlaceStrategy placeStrategy) {

                            TaskManager.getInstance().run_proxy(new Runnable() {
                                @Override
                                public void run() {
                                    //synchronized to handle waterfall udate
                                    synchronized (CommonAdManager.this) {
                                        mUpStatusOverTime = placeStrategy.getUpStatusOverTime();
                                        adTrackingInfo.setAsid(placeStrategy.getAsid());
                                        //Check if the placement matches the ad format
                                        //It would not to send tk, so it use onCallbacInternalError to callback error
                                        if (!TextUtils.equals(String.valueOf(placeStrategy.getFormat()), format)) {
                                            final AdError adError = ErrorCode.getErrorCode(ErrorCode.formatError, "", "Format corresponding to API: " +
                                                    CommonSDKUtil.getFormatString(format) +
                                                    ", Format corresponding to placement strategy: " + CommonSDKUtil.getFormatString(String.valueOf(placeStrategy.getFormat())));
                                            SDKContext.getInstance().runOnMainThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    onCallbacInternalError(formatLoadParams, mPlacementId, requestId, adError);
                                                }
                                            });

                                            adTrackingInfo.setmIsLoad(false);


                                            AgentEventManager.onAgentForATToAppLoadFail(adTrackingInfo, adError);
                                            mIsLoading = false;
                                            return;
                                        }

                                        checkToGetWaterfallList(mApplicationContext, mPlacementId, requestId, placeStrategy, adTrackingInfo, formatLoadParams);
                                    }

                                }
                            });

                        }

                        @Override
                        public void loadStrategyFailed(AdError errorBean) {
                            NetworkLogUtil.strategyLog(Const.LOGKEY.FAIL, mPlacementId, CommonSDKUtil.getFormatString(format), errorBean.printStackTrace());

                            final AdError adError1 = ErrorCode.getErrorCode(ErrorCode.placeStrategyError, errorBean.getPlatformCode(), errorBean.getPlatformMSG());

                            final AdTrackingInfo adTrackingInfo = TrackingInfoUtil.getTrackingInfoForAgent(format, requestId, mPlacementId, mPsid, mSessionId, null, formatLoadParams.isRefresh ? 1 : 0);
                            adTrackingInfo.setmReason(AdTrackingInfo.PLACEMENT_STRATEGY_REASON);

                            onCallbackFail(true, adTrackingInfo, adError1, formatLoadParams);
                        }

                        @Override
                        public void overTimeSuccessStrategy(final PlaceStrategy placeStrategy) {
                            if (!TextUtils.equals(oldAsid, placeStrategy.getAsid())) {
                                mUpStatus = 0; //Reset upstatus
                            }

                            TaskManager.getInstance().run_proxy(new Runnable() {
                                @Override
                                public void run() {
                                    synchronized (CommonAdManager.this) {
                                        List<PlaceStrategy.UnitGroupInfo> originNormalList = PlaceStrategy.parseUnitGroupInfoList(placeStrategy.getNormalUnitGroupListStr(), false);
                                        List<PlaceStrategy.UnitGroupInfo> originHbList = PlaceStrategy.parseUnitGroupInfoList(placeStrategy.getHeadbiddingUnitGroupListStr(), true);
                                        insertHBUnitInfoToNormalList(originNormalList, originHbList, mPlacementId);

                                        ShowWaterfallManager.getInstance().refreshPlacementWaterFall(mPlacementId, requestId, placeStrategy, originNormalList);

                                        if (originHbList.size() == 0) {
                                            ShowWaterfallManager.getInstance().finishFinalWaterFall(mPlacementId, requestId);
                                        }
                                    }
                                }
                            });

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
            , final PlaceStrategy placeStrategy, final AdTrackingInfo adTrackingInfo, final T formatLoadParams) {

        /**Create New Adsource Object to Request**/
        List<PlaceStrategy.UnitGroupInfo> originNormalList = PlaceStrategy.parseUnitGroupInfoList(placeStrategy.getNormalUnitGroupListStr(), false);
        List<PlaceStrategy.UnitGroupInfo> originHbList = PlaceStrategy.parseUnitGroupInfoList(placeStrategy.getHeadbiddingUnitGroupListStr(), true);

        /**It will refresh waterfall before start to filter AdSource List.**/
        ShowWaterfallManager.getInstance().refreshPlacementWaterFall(placementId, requestId, placeStrategy, originNormalList);

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

            noFilterGroupList = new ArrayList<>();
            resultFilterList = new ArrayList<>();
            hbFilterList = new ArrayList<>();

            checkCapAndPacingForAdsource(originNormalList, originHbList,
                    resultFilterList, hbFilterList, noFilterGroupList,
                    placementImpressionInfo, adTrackingInfo);

        } catch (AdStatusException e) {

            onCallbackFail(true, adTrackingInfo, e, formatLoadParams);
            return;
        } catch (Throwable e) {
            onCallbackFail(true, adTrackingInfo, e, formatLoadParams);
            return;
        }

        insertHBUnitInfoToNormalList(resultFilterList, hbFilterList, placementId);


        //Random Adsource by ecpm, use for requesting
        List<PlaceStrategy.UnitGroupInfo> finalAdSourceList = randomTheSameLayerAdsource(placeStrategy, requestId, adTrackingInfo.getmRefresh(), resultFilterList);

        /**Add no filter adsource**/
        List<PlaceStrategy.UnitGroupInfo> updateWaterFallList = new ArrayList<>();
        updateWaterFallList.addAll(finalAdSourceList);
        updateWaterFallList.addAll(noFilterGroupList);
        /**refresh final waterfall**/
        ShowWaterfallManager.getInstance().refreshPlacementWaterFall(placementId, requestId, placeStrategy, updateWaterFallList);

        boolean isFinalWaterFall = false;
        HeadBiddingFactory.IHeadBiddingS2SHandler hbHandler = HeadBiddingFactory.createS2SHeadBiddingHandler();
        if (hbHandler == null || hbFilterList == null || hbFilterList.size() == 0) {
            ShowWaterfallManager.getInstance().finishFinalWaterFall(placementId, requestId);
            isFinalWaterFall = true;
        }

        //Do not have any adsource (Final)
        if (isFinalWaterFall && (finalAdSourceList == null || finalAdSourceList.size() == 0)) {
            AdError adError = ErrorCode.getErrorCode(ErrorCode.noVailAdsource, "", "");
            adTrackingInfo.setmReason(AdTrackingInfo.NO_VAIL_ADSOURCE_REASON);

            onCallbackFail(true, adTrackingInfo, adError, formatLoadParams);
            mIsLoading = false;
            return;
        }

        AdLoadCapManager.getInstance().saveOneLoadTime(mApplicationContext, placementId, placeStrategy);
        adTrackingInfo.setmIsLoad(true);
        AdTrackingManager.getInstance(mApplicationContext).addAdTrackingInfo(TrackingV2Loader.AD_SDK_LOAD_TYPE, adTrackingInfo);


        CommonMediationManager commonMediationManager = createFormatMediationManager(formatLoadParams);
        mRequestId = requestId;
        mHistoryMediationManager.put(requestId, commonMediationManager);

        commonMediationManager.startToRequestMediationAd(placementId, requestId, placeStrategy, finalAdSourceList, isFinalWaterFall);

        mIsLoading = false;

        /**
         * Start to Headbidding of UnitGroup
         */
        if (!isFinalWaterFall) {
            try {
                final long[] startTime = new long[1];
                startTime[0] = System.currentTimeMillis();
                /**
                 * Start to Headbidding of UnitGroup
                 */
                hbHandler.setTestMode(ATSDK.isNetworkLogDebug());
                hbHandler.initS2SHbInfo(context, requestId, placementId, placeStrategy.getFormat(), placeStrategy.getHbBidTimeout(), hbFilterList);
                hbHandler.startS2SHbInfo(placeStrategy.getHbRequestUrl(), new HeadBiddingFactory.IHeadBiddingCallback() {

                    @Override
                    public void onSuccess(String requestId, List<PlaceStrategy.UnitGroupInfo> successList) {
                        CommonLogUtil.i("HeadBidding", "onSuccess: ----------------------------------------------");
                        CommonMediationManager requestMediationManager = mHistoryMediationManager.get(requestId);

                        /**Check if exist AdSource in newest PlaceSetting**/
                        PlaceStrategy newestStrategy = PlaceStrategyManager.getInstance(mApplicationContext).getPlaceStrategyByAppIdAndPlaceId(mPlacementId);

                        List<PlaceStrategy.UnitGroupInfo> filterSuccessList = new ArrayList<>();
                        for (PlaceStrategy.UnitGroupInfo hbInfo : successList) {
                            if (newestStrategy.containHBUnitGroupInfo(hbInfo.unitId)) {
                                filterSuccessList.add(hbInfo);
                            }
                        }

                        ShowWaterfallManager.getInstance().addAdSourceToWaterFall(placementId, requestId, filterSuccessList);
                        if (requestMediationManager != null) {
                            requestMediationManager.handleHeadBiddingAdSource(filterSuccessList);
                        }

                    }

                    @Override
                    public void onFailed(String requestId, List<PlaceStrategy.UnitGroupInfo> failList) {
                        PlaceStrategy newestStrategy = PlaceStrategyManager.getInstance(mApplicationContext).getPlaceStrategyByAppIdAndPlaceId(mPlacementId);

                        List<PlaceStrategy.UnitGroupInfo> filterFailList = new ArrayList<>();
                        for (PlaceStrategy.UnitGroupInfo hbInfo : failList) {
                            if (newestStrategy.containHBUnitGroupInfo(hbInfo.unitId)) {
                                filterFailList.add(hbInfo);
                            }
                            AdSourceRequestFailManager.getInstance().putAdSourceBidFailTime(hbInfo.unitId, System.currentTimeMillis());
                        }
                        CommonLogUtil.i("HeadBidding", "onFailed: ----------------------------------------------");
                        ShowWaterfallManager.getInstance().addAdSourceToWaterFall(placementId, requestId, filterFailList);

                    }

                    @Override
                    public void onFinished(String requestId) {
                        CommonLogUtil.i("HeadBidding", "onFinished: ----------------------------------------------");

                        ShowWaterfallManager.getInstance().finishFinalWaterFall(placementId, requestId);
                        CommonMediationManager requestMediationManager = mHistoryMediationManager.get(requestId);
                        if (requestMediationManager != null) {
                            requestMediationManager.notifyBiddingFinish();
                        }
                        sendFinishHBTracking(placeStrategy, requestId, formatLoadParams.isRefresh ? 1 : 0, ShowWaterfallManager.getInstance().getNewestWaterFallForPlacementId(placementId), startTime[0]);

                    }

                });
            } catch (Throwable e) {
                CommonMediationManager requestMediationManager = mHistoryMediationManager.get(requestId);
                if (requestMediationManager != null) {
                    requestMediationManager.notifyBiddingFinish();
                }
            }

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
                unitGroupInfo.level = -1;
                noFilterGroupList.add(unitGroupInfo);
                continue;
            }

            filterList.add(unitGroupInfo);
        }


        //Check UnitGroup's cap,pacing,request status --- Headbidding UnitGroup
        for (PlaceStrategy.UnitGroupInfo unitGroupInfo : originHbList) {

            if (isRequestLimit(adTrackingInfo.getmPlacementId(), adTrackingInfo, placementImpressionInfo, unitGroupInfo)) {
                unitGroupInfo.level = -1;
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

            if ((adCacheInfo != null && adCacheInfo.isUpStatusAvaiable() && adCacheInfo.isNetworkAdReady())) {
                try {
                    double oldBidEcpm = adCacheInfo.getBaseAdapter().getTrackingInfo().getmBidPrice();
                    hbUgInfo.setEcpm(oldBidEcpm);
                    hbUgInfo.setSortType(3); //标记广告源up_status有效并没发起bid request
                    headBiddingFilterList.remove(i);
                    CommonSDKUtil.insertAdSourceByOrderEcpm(filterUnitgroupList, hbUgInfo);
                    continue;
                } catch (Exception e) {
                }
            }

            /**Check HB Bidding Cache**/
            if (checkAdSourceHBTokenIsReady(hbUgInfo)) {
                headBiddingFilterList.remove(i);
                CommonSDKUtil.insertAdSourceByOrderEcpm(filterUnitgroupList, hbUgInfo);
                continue;
            }
        }
    }


    /**
     * Check HB Bidding Cache is ready?
     *
     * @param unitGroupInfo
     * @return
     */
    private boolean checkAdSourceHBTokenIsReady(PlaceStrategy.UnitGroupInfo unitGroupInfo) {
        try {
            S2SHBResponse hiBidCache = HBS2SCacheManager.getInstance().getCache(unitGroupInfo.unitId);
            if (hiBidCache != null) {
                unitGroupInfo.ecpm = hiBidCache.price;
                unitGroupInfo.payload = hiBidCache.bidId;
                unitGroupInfo.sortType = 2; //use cache token
                return true;
            }

        } catch (Throwable e) {

        }
        return false;
    }


    private void onCallbackFail(boolean needToSendTk, final AdTrackingInfo adTrackingInfo, Throwable e, T formatLoadParams) {

        final AdError adError;
        if (e instanceof AdStatusException) {
            adError = ((AdStatusException) e).adError;
        } else {
            adError = ErrorCode.getErrorCode(ErrorCode.exception, "", e.getMessage());
        }

        onCallbackFail(needToSendTk, adTrackingInfo, adError, formatLoadParams);
    }

    private void onCallbackFail(boolean needToSendTk, final AdTrackingInfo adTrackingInfo, final AdError adError, final T formatLoadParams) {
        mIsLoading = false;

        SDKContext.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                onCallbacInternalError(formatLoadParams, mPlacementId, adTrackingInfo.getmRequestId(), adError);
            }
        });
        adTrackingInfo.setmIsLoad(false);
        if (needToSendTk) {
            AdTrackingManager.getInstance(mApplicationContext).addAdTrackingInfo(TrackingV2Loader.AD_SDK_LOAD_TYPE, adTrackingInfo);

            AgentEventManager.onAgentForATToAppLoadFail(adTrackingInfo, adError);
        }

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

        if (unitGroupInfo.bidType == 1) {
            if (AdSourceRequestFailManager.getInstance().isInBidFailInterval(unitGroupInfo)) {
                unitGroupInfo.setErrorMsg("Bid fail in pacing");
                NetworkLogUtil.adsourceLog(placementId, adTrackingInfo, "Bid fail in pacing", unitGroupInfo, adsourceHourShowTime, adsourceDayShowTime);
                AgentEventManager.onAdsourceLoadFail(adTrackingInfo, unitGroupInfo, -1, 4, ErrorCode.getErrorCode(ErrorCode.inRequestFailPacing, "", "Bid fail in pacing"), 0);
                return true;
            }
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

        /**Send TK 15**/
        CommonLogUtil.e(TAG, "Request UnitGroup's Number:" + placeStrategy.getRequestUnitGroupNumber());
        int level = 0;
        for (String key : layerAdSourceMap.keySet()) {
            List<PlaceStrategy.UnitGroupInfo> layerList = layerAdSourceMap.get(key);
            for (PlaceStrategy.UnitGroupInfo unitGroupInfo : layerList) {
//                unitGroupInfo.level = level;
                unitGroupInfo.setRequestLayerLevel(level / placeStrategy.getRequestUnitGroupNumber() + 1);
                CommonLogUtil.e(TAG, "UnitGroupInfo requestLevel:" + level + " || " + "layer:" + unitGroupInfo.getRequestLayLevel());

                randomResultList.add(unitGroupInfo);
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("sortpriority", level);
                    jsonObject.put("sorttype", unitGroupInfo.bidType == 1 ? unitGroupInfo.sortType : 1); //如果是非Bid的，标记sortType为1，Bid的AdSource使用自身的sortType
                    jsonObject.put("unit_id", unitGroupInfo.getUnitId());
                    jsonObject.put("bidresult", 1);
                    jsonObject.put("bidprice", unitGroupInfo.getEcpm());
                    jsonArray.put(jsonObject);
                } catch (Exception e) {

                }
                level++;
            }
        }

        adTrackingInfo.setmHbResultList(jsonArray.toString());

        AdTrackingManager.getInstance(mApplicationContext).addAdTrackingInfo(TrackingV2Loader.ADSOURCE_SORT_TYPE, adTrackingInfo);

        return randomResultList;

    }

    private void sendFinishHBTracking(final PlaceStrategy placeStrategy, final String requestId, final int isRefresh, final List<PlaceStrategy.UnitGroupInfo> unitGroupInfos, final long startTime) {
        TaskManager.getInstance().run_proxy(new Runnable() {
            @Override
            public void run() {
                CommonLogUtil.e(TAG, "UnitGroupInfo Finish HeadBidding Tracking");
                JSONArray jsonArray = new JSONArray();

                AdTrackingInfo adTrackingInfo = new AdTrackingInfo();
                adTrackingInfo.setmPlacementId(mPlacementId);
                adTrackingInfo.setmRequestId(requestId);
                adTrackingInfo.setmAdType(placeStrategy.getFormat() + "");
                adTrackingInfo.setAsid(placeStrategy.getAsid());
                adTrackingInfo.setmRefresh(isRefresh);
                adTrackingInfo.setmHbStartTime(startTime);
                adTrackingInfo.setmHbEndTime(System.currentTimeMillis());
                adTrackingInfo.setmTrafficGroupId(placeStrategy.getTracfficGroupId());
                adTrackingInfo.setmGroupId(placeStrategy.getGroupId());


                /**Send TK 11**/
                for (int i = 0; i < unitGroupInfos.size(); i++) {
//                unitGroupInfo.level = level;
                    PlaceStrategy.UnitGroupInfo unitGroupInfo = unitGroupInfos.get(i);
                    CommonLogUtil.e(TAG, "UnitGroupInfo requestLevel:" + i + " || " + "layer:" + unitGroupInfo.getRequestLayLevel());

                    try {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("sortpriority", unitGroupInfo.level != -1 ? i : -1);
                        jsonObject.put("sorttype", unitGroupInfo.bidType == 1 ? unitGroupInfo.sortType : 1); //如果是非Bid的，标记sortType为1，Bid的AdSource使用自身的sortType
                        jsonObject.put("unit_id", unitGroupInfo.getUnitId());
                        jsonObject.put("bidresult", unitGroupInfo.ecpm > 0 ? 1 : 0);
                        jsonObject.put("bidprice", unitGroupInfo.getEcpm());
                        jsonObject.put("errormsg", unitGroupInfo.getErrorMsg());
                        jsonArray.put(jsonObject);
                    } catch (Exception e) {

                    }
                }

                adTrackingInfo.setmHbResultList(jsonArray.toString());

                AdTrackingManager.getInstance(mApplicationContext).addAdTrackingInfo(TrackingV2Loader.AD_HEADERBIDDING_TYPE, adTrackingInfo);
            }
        });


    }


    protected CommonCacheCountdownTimer mCacheCountdownTimer;
    double mAdSourceEcpm = 0;
    String mCountDownRequestId = "";

    /**
     * Cache's countdown
     *
     * @param baseAdapter
     * @param requestId
     */
    public void prepareCountdown(final ATBaseAdAdapter baseAdapter, final String requestId, final double adSourceEcpm) {

        SDKContext.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                AdTrackingInfo adTrackingInfo = baseAdapter.getTrackingInfo();
                PlaceStrategy.UnitGroupInfo unitGroupInfo = baseAdapter.getmUnitgroupInfo();

                if (adTrackingInfo == null || unitGroupInfo == null) {
                    return;
                }

                if (unitGroupInfo.bidType == 1) {
                    return;
                }

                //Banner and Splash would not to refresh request
                if (TextUtils.equals(adTrackingInfo.getmAdType(), Const.FORMAT.BANNER_FORMAT) || TextUtils.equals(adTrackingInfo.getmAdType(), Const.FORMAT.SPLASH_FORMAT)) {
                    return;
                }

                /**Only the height ecpm unitgroup will be countdown**/
                if (mAdSourceEcpm > adSourceEcpm && mCountDownRequestId.equals(requestId)) {
                    return;
                }

                if (adTrackingInfo.getmNetworkType() == 35) { //MyOffer shouldn't to countdown
                    return;
                }

                mAdSourceEcpm = adSourceEcpm;
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
    private void startCountdown(PlaceStrategy.UnitGroupInfo unitGroupInfo, AdTrackingInfo adTrackingInfo) {
        CommonCacheCountdownTimer cacheCountdownTimer = new CommonCacheCountdownTimer(unitGroupInfo.getUnitADCacheTime(), unitGroupInfo.getUnitADCacheTime(), unitGroupInfo, adTrackingInfo);
        mCacheCountdownTimer = cacheCountdownTimer;
        mCacheCountdownTimer.start();
    }


    public void notifyNewestCacheHasBeenShow(AdCacheInfo cacheInfo) {
        if (cacheInfo.isLast()) {
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

        PlaceStrategy placeStrategy = PlaceStrategyManager.getInstance(SDKContext.getInstance().getContext()).getPlaceStrategyByAppIdAndPlaceId(mPlacementId);

        String currentRequestId = TextUtils.isEmpty(mRequestId) ? "" : mRequestId;

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

        AdCacheInfo adCacheInfo = AdCacheManager.getInstance().checkCache(context, mPlacementId, true, isShowCall);

        return adCacheInfo;
    }


    public boolean isInDefaultAdSourceLoading() {
        return false;
    }

    public boolean startDefaultAdSourceLoading(String placementId, String requestId, T loadParam) {
        return false;
    }

    public abstract CommonMediationManager createFormatMediationManager(T formatLoadParams);

    public abstract void onCallbackOfferHasExist(T formatLoadParams, String placementId, String requestId);

    public abstract void onCallbacInternalError(T formatLoadParams, String placementId, String requestId, AdError adError);
}
