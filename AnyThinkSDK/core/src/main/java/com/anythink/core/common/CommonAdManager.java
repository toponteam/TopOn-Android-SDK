package com.anythink.core.common;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.ATSDK;
import com.anythink.core.api.AdError;
import com.anythink.core.api.ErrorCode;
import com.anythink.core.cap.AdCapManager;
import com.anythink.core.cap.AdPacingManager;
import com.anythink.core.common.base.AnyThinkBaseAdapter;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.entity.AdCacheInfo;
import com.anythink.core.common.entity.AdTrackingInfo;
import com.anythink.core.common.entity.UnitgroupCacheInfo;
import com.anythink.core.common.net.TrackingV2Loader;
import com.anythink.core.common.track.AdTrackingManager;
import com.anythink.core.common.track.AgentEventManager;
import com.anythink.core.common.utils.CommonDeviceUtil;
import com.anythink.core.common.utils.CommonLogUtil;
import com.anythink.core.common.utils.CommonMD5;
import com.anythink.core.common.utils.CommonUtil;
import com.anythink.core.common.utils.task.TaskManager;
import com.anythink.core.strategy.PlaceStrategy;
import com.anythink.core.strategy.PlaceStrategyManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
     * @param customExtraMap
     * @param placementCallback
     */
    public void loadStragety(final Context context, final String format, final String mPlacementId, final boolean isRefresh, final Map<String, String> customExtraMap, final PlacementCallback placementCallback) {
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

                                AdTrackingInfo adTrackingInfo = new AdTrackingInfo();
                                adTrackingInfo.setmAdType(format);
                                adTrackingInfo.setmPlacementId(mPlacementId);
                                adTrackingInfo.setmRequestId(requestId);
                                adTrackingInfo.setAsid(asid);
                                adTrackingInfo.setmPsid(psid);
                                adTrackingInfo.setmSessionId(sessionid);
                                adTrackingInfo.setmRefresh(isRefresh ? 1 : 0);
                                adTrackingInfo.setmIsLoad(false);
                                adTrackingInfo.setmReason(AdTrackingInfo.LOADING_REASON);
                                adTrackingInfo.setmTrafficGroupId(placeStrategy != null ? placeStrategy.getTracfficGroupId() : 0);
                                adTrackingInfo.setmGroupId(placeStrategy != null ? placeStrategy.getGroupId() : 0);

                                AdTrackingManager.getInstance(mApplicationContext).addAdTrackingInfo(TrackingV2Loader.AD_SDK_LOAD_TYPE, adTrackingInfo);
                                //Fail Agent
                                AgentEventManager.onAgentForATToAppLoadFail(requestId, mPlacementId, psid, sessionid, placeStrategy != null ? placeStrategy.getGroupId() : 0
                                        , adTrackingInfo.getmRefresh(), adError.printStackTrace());
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


                    PlaceStrategyManager.getInstance(context).requestStrategy(placeStrategy, mAppId, mAppKey, mPlacementId, customExtraMap, new PlaceStrategyManager.StrategyloadListener() {
                        @Override
                        public void loadStrategySuccess(final PlaceStrategy placeStrategy) {
                            mUpStatusOverTime = placeStrategy.getUpStatusOverTime();

                            String psid = mPsid;
                            String sessionid = mSessionId;
                            String asid = placeStrategy.getAsid();

                            /**tracking**/
                            final AdTrackingInfo adTrackingInfo = new AdTrackingInfo();
                            adTrackingInfo.setmAdType(format);
                            adTrackingInfo.setmPlacementId(mPlacementId);
                            adTrackingInfo.setmRequestId(requestId);
                            adTrackingInfo.setAsid(asid);
                            adTrackingInfo.setmPsid(psid);
                            adTrackingInfo.setmSessionId(sessionid);
                            adTrackingInfo.setmRefresh(isRefresh ? 1 : 0);
                            adTrackingInfo.setmTrafficGroupId(placeStrategy.getTracfficGroupId());
                            adTrackingInfo.setmGroupId(placeStrategy.getGroupId());

                            TaskManager.getInstance().run_proxy(new Runnable() {
                                @Override
                                public void run() {
                                    checkPacingAndCappingToGetUnitgroup(mApplicationContext, mPlacementId, requestId, placeStrategy, adTrackingInfo, placementCallback);
                                }
                            });

                        }

                        @Override
                        public void loadStrategyFailed(AdError errorBean) {
                            final AdError adError1 = ErrorCode.getErrorCode(ErrorCode.placeStrategyError, errorBean.getPlatformCode(), errorBean.getPlatformMSG());
                            SDKContext.getInstance().runOnMainThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (placementCallback != null) {
                                        placementCallback.onLoadError(mPlacementId, requestId, adError1);
                                    }
                                }
                            });
                            mIsLoading = false;
                            AdTrackingInfo adTrackingInfo = new AdTrackingInfo();
                            adTrackingInfo.setmAdType(format);
                            adTrackingInfo.setmPlacementId(mPlacementId);
                            adTrackingInfo.setmRequestId(requestId);
                            adTrackingInfo.setAsid(placeStrategy != null ? placeStrategy.getAsid() : "");
                            adTrackingInfo.setmPsid(mPsid);
                            adTrackingInfo.setmSessionId(mSessionId);
                            adTrackingInfo.setmReason(AdTrackingInfo.PLACEMENT_STRATEGY_REASON);
                            adTrackingInfo.setmRefresh(isRefresh ? 1 : 0);
                            adTrackingInfo.setmTrafficGroupId(0);

                            AdTrackingManager.getInstance(mApplicationContext).addAdTrackingInfo(TrackingV2Loader.AD_SDK_LOAD_TYPE, adTrackingInfo);
                            //Fail Agent
                            AgentEventManager.onAgentForATToAppLoadFail(requestId, mPlacementId, "", "", 0, adTrackingInfo.getmRefresh(), adError1.printStackTrace());
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

            AgentEventManager.onAgentForATToAppLoadFail(requestId, mPlacementId, psid, sessionid, groupid, adTrackingInfo.getmRefresh(), "Strategy is close.");
            mIsLoading = false;
            return;
        }

        long placementDayCap = placeStrategy.getUnitCapsDayNumber();
        long placementHourCap = placeStrategy.getUnitCapsHourNumber();

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

            AgentEventManager.onAgentForATToAppLoadFail(requestId, mPlacementId, psid, sessionid, groupid, adTrackingInfo.getmRefresh(), "No Adsource.");
            return;
        }

        /**Qualified UnitGroup List**/
        final List<PlaceStrategy.UnitGroupInfo> filterUnitgroupList = new ArrayList<>();
        /**Qualified HeadBidding list**/
        final List<PlaceStrategy.UnitGroupInfo> headBiddingFilterList = new ArrayList<>();

        String[] unitGroupUnitIds = new String[resultUnitGroupList.size() + headbiddingList.size()];

        for (int i = 0; i < resultUnitGroupList.size() + headbiddingList.size(); i++) {
            if (i <= resultUnitGroupList.size() - 1) {
                unitGroupUnitIds[i] = resultUnitGroupList.get(i).unitId;
            } else {
                unitGroupUnitIds[i] = headbiddingList.get(i - resultUnitGroupList.size()).unitId;
            }
        }


        AdCapManager.CapInfo capInfo = AdCapManager.getInstance(context).getCapByPlaceIdAndUnitGroupId(placementId, unitGroupUnitIds);
        //-1 mean no limit
        /**placement cap**/
        if ((placementDayCap != -1 && capInfo.getAllUnitGroupDayCap() >= placementDayCap)
                || (placementHourCap != -1 && capInfo.getAllUnitGroupHourCap() >= placementHourCap)) {
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
            AgentEventManager.onAgentForATToAppLoadFail(requestId, mPlacementId, psid, sessionid, groupid, adTrackingInfo.getmRefresh(), "Capping.");
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

            AgentEventManager.onAgentForATToAppLoadFail(requestId, mPlacementId, psid, sessionid, groupid, adTrackingInfo.getmRefresh(), "Pacing.");
            return;
        }

        //Check UnitGroup's cap status --- Normal UnitGroup
        for (PlaceStrategy.UnitGroupInfo unitGroupInfo : resultUnitGroupList) {
            if ((unitGroupInfo.capsByDay == -1 || capInfo.getUnitGroupDayCatById(unitGroupInfo.unitId) < unitGroupInfo.capsByDay)
                    && (unitGroupInfo.capsByHour == -1 || capInfo.getUnitGroupHourCatById(unitGroupInfo.unitId) < unitGroupInfo.capsByHour)) {
                filterUnitgroupList.add(unitGroupInfo);
            } else {
                unitGroupInfo.setErrorMsg("Out of Cap");
                noFilterGroupList.add(unitGroupInfo);
                AgentEventManager.onAdsourceLoadFail(requestId, placementId, psid, sessionid, placeStrategy.getGroupId(), adTrackingInfo.getmRefresh(), unitGroupInfo.networkType
                        , unitGroupInfo.unitId, -1, 2, ErrorCode.getErrorCode(ErrorCode.outOfCapError, "", "Out of Cap"), unitGroupInfo.bidType, unitGroupInfo.getEcpm(), 0);
            }
        }


        //Check UnitGroup's cap status --- Headbidding UnitGroup
        for (PlaceStrategy.UnitGroupInfo unitGroupInfo : headbiddingList) {
            if ((unitGroupInfo.capsByDay == -1 || capInfo.getUnitGroupDayCatById(unitGroupInfo.unitId) < unitGroupInfo.capsByDay)
                    && (unitGroupInfo.capsByHour == -1 || capInfo.getUnitGroupHourCatById(unitGroupInfo.unitId) < unitGroupInfo.capsByHour)) {
                headBiddingFilterList.add(unitGroupInfo);
            } else {
                unitGroupInfo.setErrorMsg("Out of Cap");
                AgentEventManager.onAdsourceLoadFail(requestId, placementId, psid, sessionid, placeStrategy.getGroupId(), adTrackingInfo.getmRefresh(), unitGroupInfo.networkType
                        , unitGroupInfo.unitId, -1, 2, ErrorCode.getErrorCode(ErrorCode.outOfCapError, "", "Out of Cap"), unitGroupInfo.bidType, unitGroupInfo.getEcpm(), 0);
                noFilterGroupList.add(unitGroupInfo);
            }
        }

        if (filterUnitgroupList.size() <= 0 && headbiddingList.size() <= 0) {
            CommonLogUtil.i(TAG, "unitgroup capping error");
            SDKContext.getInstance().runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    if (callback != null) {
                        AdError adError = ErrorCode.getErrorCode(ErrorCode.outOfCapError, "", "");
                        callback.onLoadError(placementId, requestId, adError);
                    }
                }
            });

            adTrackingInfo.setmIsLoad(false);
            adTrackingInfo.setmReason(AdTrackingInfo.CAPPING_REASON);
            AdTrackingManager.getInstance(mApplicationContext).addAdTrackingInfo(TrackingV2Loader.AD_SDK_LOAD_TYPE, adTrackingInfo);

            AgentEventManager.onAgentForATToAppLoadFail(requestId, mPlacementId, psid, sessionid, groupid, adTrackingInfo.getmRefresh(), "Capping.");
            mIsLoading = false;
            return;
        }

        //Check UnitGroup's pacing status --- Normal UnitGroup
        for (int i = filterUnitgroupList.size() - 1; i >= 0; i--) {
            PlaceStrategy.UnitGroupInfo unitGroupInfo = filterUnitgroupList.get(i);
            if (AdPacingManager.getInstance().isUnitGroupInPacing(placementId, unitGroupInfo)) {
                /**Add to noFilterGroupList**/
                unitGroupInfo.setErrorMsg("Out of Pacing");
                noFilterGroupList.add(unitGroupInfo);
                filterUnitgroupList.remove(i);
                AgentEventManager.onAdsourceLoadFail(requestId, placementId, psid, sessionid, placeStrategy.getGroupId(), adTrackingInfo.getmRefresh(), unitGroupInfo.networkType
                        , unitGroupInfo.unitId, -1, 3, ErrorCode.getErrorCode(ErrorCode.outOfCapError, "", "Out of Pacing"), unitGroupInfo.bidType, unitGroupInfo.getEcpm(), 0);

            }
        }

        //Check UnitGroup's pacing status --- HeadBidding UnitGroup
        for (int i = headBiddingFilterList.size() - 1; i >= 0; i--) {
            PlaceStrategy.UnitGroupInfo unitGroupInfo = headBiddingFilterList.get(i);
            if (AdPacingManager.getInstance().isUnitGroupInPacing(placementId, unitGroupInfo)) {
                /**Add to noFilterGroupList**/
                unitGroupInfo.setErrorMsg("Out of Pacing");
                noFilterGroupList.add(headBiddingFilterList.get(i));
                headBiddingFilterList.remove(i);
                AgentEventManager.onAdsourceLoadFail(requestId, placementId, psid, sessionid, placeStrategy.getGroupId(), adTrackingInfo.getmRefresh(), unitGroupInfo.networkType
                        , unitGroupInfo.unitId, -1, 3, ErrorCode.getErrorCode(ErrorCode.outOfCapError, "", "Out of Pacing"), unitGroupInfo.bidType, unitGroupInfo.getEcpm(), 0);
            }
        }

        if (filterUnitgroupList.size() <= 0 && headBiddingFilterList.size() <= 0) {
            CommonLogUtil.i(TAG, "unitgroup pacing error");
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

            AgentEventManager.onAgentForATToAppLoadFail(requestId, mPlacementId, psid, sessionid, groupid, adTrackingInfo.getmRefresh(), "Pacing.");
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
                                if (filterUnitgroupList != null) {
                                    for (int i = 0; i < filterUnitgroupList.size(); i++) {
                                        PlaceStrategy.UnitGroupInfo unitGroupInfo = filterUnitgroupList.get(i);
                                        unitGroupInfo.level = i;
                                    }
                                }

                                HeadBiddingFactory.IHeadBiddingHandler hbHandler = HeadBiddingFactory.createHeadBiddingHandler();
                                if (hbHandler == null) {
                                    throw new Exception("anythink_headbidding.aar doesn't exist");
                                }

                                /**
                                 * Start to Headbidding of UnitGroup
                                 */
                                hbHandler.setTestMode(ATSDK.NETWORK_LOG_DEBUG);
                                hbHandler.initHbInfo(context, placementId, placeStrategy.getFormat(), filterUnitgroupList, headBiddingFilterList);
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
                                            AgentEventManager.onAgentForATToAppLoadFail(requestId, mPlacementId, psid, sessionid, groupid, adTrackingInfo.getmRefresh(), "After Headbidding fail no adsource.");
                                            mIsLoading = false;
                                            return;
                                        }

                                        if (failList != null) {
                                            noFilterGroupList.addAll(failList);
                                        }

                                        placeStrategy.setNoFilterList(noFilterGroupList);

                                        adTrackingInfo.setmIsLoad(true);
                                        AdTrackingManager.getInstance(mApplicationContext).addAdTrackingInfo(TrackingV2Loader.AD_SDK_LOAD_TYPE, adTrackingInfo);


                                        if (callback != null) {
                                            callback.onSuccess(placementId, requestId, placeStrategy, resultList);
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


                                if (callback != null) {
                                    callback.onSuccess(placementId, requestId, placeStrategy, filterUnitgroupList);
                                }

                                mIsLoading = false;
                            }
                        }
                    });
                }
            }
        });
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

        PlaceStrategy placeStrategy = AdCacheManager.getInstance().getCacheStrategy(mPlacementId);

        CommonAdManager adManager = CommonAdManager.getInstance(mPlacementId);
        String currentRequestId = adManager != null ? adManager.getCurrentRequestId() : "";

        if (placeStrategy == null) {
            placeStrategy = PlaceStrategyManager.getInstance(mApplicationContext).getPlaceStrategyByAppIdAndPlaceId(mPlacementId);
        }

        if (placeStrategy == null) {
            if (isShowCall) {
                AgentEventManager.onAdShowFail("", mPlacementId, "", "", 0, 0, AgentEventManager.PLACEMENT_STRATEGY_NULL_REASON, "", currentRequestId);
            } else {
                AgentEventManager.isReadyEventAgentForATToApp("", mPlacementId, "", "", 0, 0, false, AgentEventManager.PLACEMENT_STRATEGY_NULL_REASON, -1, "", -1, "", "", currentRequestId, false, "");
            }
            return null;
        }

        //paccing
        if (AdPacingManager.getInstance().isPlacementInPacing(mPlacementId, placeStrategy)) {
            if (isShowCall) {
                AgentEventManager.onAdShowFail("", mPlacementId, "", "", 0, 0, AgentEventManager.PLACEMENT_PACCING_REASON, "", currentRequestId);
            } else {
                AgentEventManager.isReadyEventAgentForATToApp("", mPlacementId, "", "", 0, 0,
                        false, AgentEventManager.PLACEMENT_PACCING_REASON, -1, "", -1, "", "", currentRequestId, false, "");
            }
            return null;
        }

        //capping
        if (AdCapManager.getInstance(mApplicationContext).isPlacementOutOfCap(placeStrategy, mPlacementId)) {
            if (isShowCall) {
                AgentEventManager.onAdShowFail("", mPlacementId, "", "", 0, 0, AgentEventManager.PLACEMENT_CAPPING_REASON, "", currentRequestId);
            } else {
                AgentEventManager.isReadyEventAgentForATToApp("", mPlacementId, "", "", 0, 0,
                        false, AgentEventManager.PLACEMENT_CAPPING_REASON, -1, "", -1, "", "", currentRequestId, false, "");
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
        adTrackingInfo.setmPsid(SDKContext.getInstance().getPsid());
        adTrackingInfo.setmSessionId(SDKContext.getInstance().getSessionId(mPlacementId));
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
