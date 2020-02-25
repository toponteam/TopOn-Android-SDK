package com.anythink.core.common;

import android.app.Activity;
import android.content.Context;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.AdError;
import com.anythink.core.api.ErrorCode;
import com.anythink.core.common.base.AnyThinkBaseAdapter;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.entity.AdCacheInfo;
import com.anythink.core.common.entity.AdTrackingInfo;
import com.anythink.core.common.entity.BaseAd;
import com.anythink.core.common.entity.TemplateStrategy;
import com.anythink.core.common.entity.UnitgroupCacheInfo;
import com.anythink.core.common.hb.HeadBiddingCacheManager;
import com.anythink.core.common.net.TrackingV2Loader;
import com.anythink.core.common.track.AdTrackingManager;
import com.anythink.core.common.track.AgentEventManager;
import com.anythink.core.common.utils.CommonDeviceUtil;
import com.anythink.core.common.utils.CommonLogUtil;
import com.anythink.core.common.utils.CustomAdapterFactory;
import com.anythink.core.common.utils.TrackingInfoUtil;
import com.anythink.core.strategy.PlaceStrategy;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class CommonMediationManager {
    protected Context mApplcationContext;
    protected WeakReference<Context> mActivityRef;
    protected HashMap<String, Runnable> mOverTimeRunnableMap; //Timeout Runnable of UnitGroup
    protected HashMap<AnyThinkBaseAdapter, Boolean> mUnitGroupReturnStatus; //Return status of UnitGroup
    protected HashMap<String, Runnable> mAdDataOverTimeRunnableMap; //AdData Timeout Runnable of UnitGroup

    protected HashMap<AnyThinkBaseAdapter, Boolean> mOverLoadMap; //Short Timeout status of UnitGroup
    protected boolean mHasReturnAdStatus; //Return staus
    protected int mLoadCount; //Request Count

    protected boolean mIsRelease; //Release status

    protected HashMap<String, Long> mUnitGroupLoadTimeMap;

    protected boolean mHasFinishLoad; //Finish loading status
    protected boolean mHasCancelCacheOffer; //Cancel Cached offer status

    protected String mUserId = "";
    protected String mCustomData = "";

    protected boolean mIsRefresh;

    private long mStartLoadTime;

    private Runnable mLongOverTimeRunnable = new Runnable() {
        @Override
        public void run() {

            /**Check the UnitGroup which don't return the resulr**/
            for (AnyThinkBaseAdapter adapter : mUnitGroupReturnStatus.keySet()) {
                boolean isReturn = mUnitGroupReturnStatus.get(adapter);
                if (!isReturn) {
                    AdTrackingInfo adTrackingInfo = adapter.getTrackingInfo();
                    adTrackingInfo.setLoadStatus(AdTrackingInfo.LONG_OVERTIME_ERROR_CALLBACK);
                    if (mUnitGroupReturnStatus.containsKey(adapter)
                            && !mUnitGroupReturnStatus.get(adapter)) {
                        AdError adError = ErrorCode.getErrorCode(ErrorCode.timeOutError, "", "");

                        mUnitGroupReturnStatus.put(adapter, true);

                        adapter.log(Const.LOGKEY.REQUEST_RESULT, Const.LOGKEY.FAIL, adError.printStackTrace());

                        AgentEventManager.onAdsourceLoadFail(adTrackingInfo.getmRequestId(), mCurrentPlacementId, adTrackingInfo.getmPsid()
                                , adTrackingInfo.getmSessionId(), adTrackingInfo.getmGroupId(), adTrackingInfo.getmRefresh()
                                , adTrackingInfo.getmNetworkType(), adTrackingInfo.getmUnitGroupUnitId()
                                , adTrackingInfo.getmLevel(), 1, adError, adTrackingInfo.getmBidType(), adTrackingInfo.getmBidPrice(), 0);

                    }
                }
            }

            if (!mHasReturnAdStatus) {
                mHasReturnAdStatus = true;
                mHasFinishLoad = true;
                AdError adError = ErrorCode.getErrorCode(ErrorCode.timeOutError, "", "");

                AgentEventManager.onAgentForATToAppLoadFail(mCurrentReqeustId, mCurrentPlacementId, SDKContext.getInstance().getPsid(), SDKContext.getInstance().getSessionId(mCurrentPlacementId)
                        , mCurrentStrategy.getGroupId(), mIsRefresh ? 1 : 0, adError.printStackTrace());

                mCurrentStrategy.updateSortUnitgroupList(mCurrentUnitGroupInfoList);

                onDeveloLoadFail(adError);
            }

            CommonAdManager adLoadManager = CommonAdManager.getInstance(mCurrentPlacementId);
            if (adLoadManager != null) {
                adLoadManager.removeMediationManager(mCurrentReqeustId);
            }
        }
    };

    protected CommonMediationManager(Context context) {
        mActivityRef = new WeakReference<>(context);
        mApplcationContext = SDKContext.getInstance().getContext();
        mOverTimeRunnableMap = new HashMap<>();
        mAdDataOverTimeRunnableMap = new HashMap<>();
        mUnitGroupLoadTimeMap = new HashMap<>();
        mOverLoadMap = new HashMap<>();
        mIsRelease = false;
    }

    public void setRefresh(boolean isRefresh) {
        mIsRefresh = isRefresh;
    }

    /**
     * AD Data Loaded Callback (Only use by RewardedVideo)
     */
    public void onAdDataLoaded(AnyThinkBaseAdapter baseAdapter) {
        if (mIsRelease) {
            return;
        }
        AdTrackingInfo adTrackingInfo = baseAdapter.getTrackingInfo();
        long starttime = mUnitGroupLoadTimeMap.get(adTrackingInfo.getmUnitGroupUnitId());
        adTrackingInfo.setDataFillTime(System.currentTimeMillis() - starttime);
    }

    /**
     * Ad Request Success Callback
     *
     * @param baseAdapter
     * @param adObjectList
     */
    public void onAdLoaded(AnyThinkBaseAdapter baseAdapter, List<? extends BaseAd> adObjectList) {
        if (mIsRelease) {
            return;
        }

        AdTrackingInfo adTrackingInfo = baseAdapter.getTrackingInfo();
        long starttime = mUnitGroupLoadTimeMap.get(adTrackingInfo.getmUnitGroupUnitId());
        adTrackingInfo.setFillTime(System.currentTimeMillis() - starttime);

        if (baseAdapter.getmUnitgroupInfo().getNetworkAdDataLoadTimeOut() != -1) {
            if (adTrackingInfo.getDataFillTime() > 0) {
                AgentEventManager.adDataFillEvent(adTrackingInfo.getmRequestId(), adTrackingInfo.getmNetworkType(), adTrackingInfo.getmUnitGroupUnitId(), adTrackingInfo.getmLevel(),
                        adTrackingInfo.getDataFillTime(), adTrackingInfo.getFillTime());
            }
        }


        if (!adTrackingInfo.ismIsDefaultNetwork()) { /**Normal request**/
            mLoadCount++;
            Runnable runnable = mOverTimeRunnableMap.get(adTrackingInfo.getmUnitGroupUnitId());
            if (runnable != null) {
                SDKContext.getInstance().removeMainThreadRunnable(runnable);
            }
            runnable = mAdDataOverTimeRunnableMap.get(adTrackingInfo.getmUnitGroupUnitId());
            if (runnable != null) {
                SDKContext.getInstance().removeMainThreadRunnable(runnable);
            }

            if (mUnitGroupReturnStatus.containsKey(baseAdapter)
                    && !mUnitGroupReturnStatus.get(baseAdapter)) {
                if (mOverLoadMap.containsKey(baseAdapter) && mOverLoadMap.get(baseAdapter)) {
                    adTrackingInfo.setLoadStatus(AdTrackingInfo.SHORT_OVERTIME_CALLBACK);
                }
                mUnitGroupReturnStatus.put(baseAdapter, true);
            }
        }

        AdCacheInfo showCacheInfo = AdCacheManager.getInstance().getHasShowAdCache(mCurrentPlacementId);
        if (showCacheInfo != null) {
            /**Record Flag**/
            if (showCacheInfo.getLevel() > adTrackingInfo.getmLevel()) {
                adTrackingInfo.setFlag(AdTrackingInfo.SHOW_LOW_LEVEL_CACHE);
            } else {
                adTrackingInfo.setFlag(AdTrackingInfo.SHOW_HIGH_LEVEL_CACHE);
            }
        } else {
            adTrackingInfo.setFlag(AdTrackingInfo.NO_SHOW_CACHE);
        }

        AdTrackingManager.getInstance(mApplcationContext).addAdTrackingInfo(TrackingV2Loader.AD_REQUEST_SUCCESS_TYPE, adTrackingInfo);
        baseAdapter.log(Const.LOGKEY.REQUEST_RESULT, Const.LOGKEY.SUCCESS, "");


        /**Save Offer to Caches**/
        if (!mHasCancelCacheOffer) {
            long adCaheTime = baseAdapter.getmUnitgroupInfo().getUnitADCacheTime();

            /**Splash Ad would not be putting in the cache**/
            if (mCurrentStrategy.getFormat() != 4) {
                AdCacheManager.getInstance().addCache(mCurrentPlacementId, adTrackingInfo.getmLevel(), baseAdapter, adObjectList, adCaheTime, mCurrentStrategy);
                CommonAdManager adManager = CommonAdManager.getInstance(mCurrentPlacementId);
                if (adManager != null && mCurrentStrategy.getAutoRequestUnitgroupAd() >= 1) {
                    adManager.prepareCountdown(baseAdapter, mCurrentReqeustId, adTrackingInfo.getmLevel());
                }
            }
        } else {
            if (baseAdapter != null) {
                baseAdapter.clean();
            }
        }

        if (!adTrackingInfo.ismIsDefaultNetwork()) {  /**Normal request**/
            if (isAllReturn()) {
                /**Cancel the timer of long-timeout runnable**/
                if (CommonAdManager.getInstance(mCurrentPlacementId) != null) {
                    CommonAdManager.getInstance(mCurrentPlacementId).removeMediationManager(mCurrentReqeustId);
                }
            }
        }

        if (!mHasReturnAdStatus) { //Handle to callback
            onLoadedCallbackToDeveloper(adTrackingInfo, false);
        }
    }

    /**
     * Handle the Cache whick exists
     **/
    private void onCacheAdLoaded(AnyThinkBaseAdapter baseAdapter, AdTrackingInfo adTrackingInfo, PlaceStrategy.UnitGroupInfo unitGroupInfo) {
        mLoadCount++; //Record load count
        if (!mHasReturnAdStatus) { //Check the callback status

            AdTrackingInfo ugAdTrackingInfo = TrackingInfoUtil.initTrackingInfo(mCurrentReqeustId, mCurrentPlacementId, mUserId, mCurrentStrategy, mUnitGroupList, mRequestCount, mIsRefresh);
            ugAdTrackingInfo = TrackingInfoUtil.initPlacementUnitGroupTrackingInfo(baseAdapter, ugAdTrackingInfo, unitGroupInfo);
            onLoadedCallbackToDeveloper(ugAdTrackingInfo, true);
        }
    }


    /**
     * Callback of Request Success
     *
     * @param adTrackingInfo
     */
    private void onLoadedCallbackToDeveloper(AdTrackingInfo adTrackingInfo, boolean isCache) {
        mHasReturnAdStatus = true;
        mHasFinishLoad = true;

        long loadDuration = System.currentTimeMillis() - mStartLoadTime;
        AdTrackingInfo anythinkTrackingInfo = new AdTrackingInfo();
        anythinkTrackingInfo.setmAdType(adTrackingInfo.getmAdType());
        anythinkTrackingInfo.setmPlacementId(adTrackingInfo.getmPlacementId());
        anythinkTrackingInfo.setmRequestId(adTrackingInfo.getmRequestId());
        anythinkTrackingInfo.setAsid(mCurrentStrategy.getAsid());
        anythinkTrackingInfo.setmPsid(SDKContext.getInstance().getPsid());
        anythinkTrackingInfo.setmSessionId(SDKContext.getInstance().getSessionId(mCurrentPlacementId));
        anythinkTrackingInfo.setmIsLoad(true);
        anythinkTrackingInfo.setmRefresh(mIsRefresh ? 1 : 0);
        anythinkTrackingInfo.setFillTime(loadDuration);
        anythinkTrackingInfo.setmTrafficGroupId(adTrackingInfo.getmTrafficGroupId());
        anythinkTrackingInfo.setmGroupId(adTrackingInfo.getmGroupId());
        if (isCache) {
            anythinkTrackingInfo.setmReason(5);
        }


        AdTrackingManager.getInstance(mApplcationContext).addAdTrackingInfo(TrackingV2Loader.AD_SDK_LOAD_SUCCESS_TYPE, anythinkTrackingInfo);

        CommonAdManager commonAdManager = CommonAdManager.getInstance(mCurrentPlacementId);
        if (commonAdManager != null) {
            commonAdManager.setLoaded();
        }

        if (!Const.FORMAT.SPLASH_FORMAT.equals(mCurrentStrategy.getFormat() + "")) {
            AdCacheManager.getInstance().refreshCacheInfo(mCurrentUnitGroupInfoList, mCurrentPlacementId, mCurrentStrategy
                    , TrackingInfoUtil.initTrackingInfo(mCurrentReqeustId, mCurrentPlacementId, mUserId, mCurrentStrategy, adTrackingInfo.getmNetworkList(), mRequestCount, mIsRefresh));

        }

        mCurrentStrategy.updateSortUnitgroupList(mCurrentUnitGroupInfoList);
        onDevelopLoaded();
    }


    /**
     * Callback of Request Fail
     *
     * @param baseAdapter
     * @param adError
     */
    public void onAdError(AnyThinkBaseAdapter baseAdapter, AdError adError) {
        if (mIsRelease) {
            return;
        }

        if (baseAdapter != null) {
            AdTrackingInfo adTrackingInfo = baseAdapter.getTrackingInfo();
            long starttime = mUnitGroupLoadTimeMap.get(adTrackingInfo.getmUnitGroupUnitId());
            if (!adTrackingInfo.ismIsDefaultNetwork()) {

                Runnable runnable = mOverTimeRunnableMap.get(adTrackingInfo.getmUnitGroupUnitId());
                if (runnable != null) {
                    SDKContext.getInstance().removeMainThreadRunnable(runnable);
                }
                runnable = mAdDataOverTimeRunnableMap.get(adTrackingInfo.getmUnitGroupUnitId());
                if (runnable != null) {
                    SDKContext.getInstance().removeMainThreadRunnable(runnable);
                }


                if (mOverLoadMap.containsKey(baseAdapter) && mOverLoadMap.get(baseAdapter)) {
                    adTrackingInfo.setLoadStatus(AdTrackingInfo.SHORT_OVERTIME_CALLBACK);
                }

                if (mUnitGroupReturnStatus.containsKey(baseAdapter)
                        && !mUnitGroupReturnStatus.get(baseAdapter)) {
                    String nwErrorCode = "";
                    String nwErrorMsg = "";
                    if (adError != null) {
                        nwErrorCode = adError.getPlatformCode() == null ? "" : adError.getPlatformCode();
                        nwErrorMsg = adError.getPlatformMSG() == null ? "" : adError.getPlatformMSG();
                    }

                    //发送统计
//                    new TrackingLoader(mApplcationContext, adTrackingInfo, TrackingLoader.AD_REQUEST_FAIL_TYPE, 0).start(0, null);

                    mUnitGroupReturnStatus.put(baseAdapter, true);

                    baseAdapter.log(Const.LOGKEY.REQUEST_RESULT, Const.LOGKEY.FAIL, adError.printStackTrace());

                    AgentEventManager.onAdsourceLoadFail(adTrackingInfo.getmRequestId(), mCurrentPlacementId, adTrackingInfo.getmPsid()
                            , adTrackingInfo.getmSessionId(), adTrackingInfo.getmGroupId(), adTrackingInfo.getmRefresh()
                            , adTrackingInfo.getmNetworkType(), adTrackingInfo.getmUnitGroupUnitId()
                            , adTrackingInfo.getmLevel(), 0, adError, adTrackingInfo.getmBidType(), adTrackingInfo.getmBidPrice()
                            , System.currentTimeMillis() - starttime);

                }


            } else {
                String nwErrorCode = "";
                String nwErrorMsg = "";
                if (adError != null) {
                    nwErrorCode = adError.getPlatformCode() == null ? "" : adError.getPlatformCode();
                    nwErrorMsg = adError.getPlatformMSG() == null ? "" : adError.getPlatformMSG();
                }

                AgentEventManager.onAdsourceLoadFail(adTrackingInfo.getmRequestId(), mCurrentPlacementId, adTrackingInfo.getmPsid()
                        , adTrackingInfo.getmSessionId(), adTrackingInfo.getmGroupId(), adTrackingInfo.getmRefresh()
                        , adTrackingInfo.getmNetworkType(), adTrackingInfo.getmUnitGroupUnitId()
                        , adTrackingInfo.getmLevel(), 0, adError, adTrackingInfo.getmBidType(), adTrackingInfo.getmBidPrice()
                        , System.currentTimeMillis() - starttime);

                baseAdapter.log(Const.LOGKEY.REQUEST_RESULT, Const.LOGKEY.FAIL, adError.printStackTrace());


                return;
            }

        }


        synchronized (this) {
            mLoadCount++; //Record LoadCount
            boolean isAllReturn = false;
            if (isAllReturn()) {
                /**Release current MediationManager**/
                if (CommonAdManager.getInstance(mCurrentPlacementId) != null && mLoadCount >= mCurrentUnitGroupInfoList.size()) {
                    CommonAdManager.getInstance(mCurrentPlacementId).removeMediationManager(mCurrentReqeustId);
                }
                isAllReturn = true;
            }
            if (!mHasReturnAdStatus) {
                //Over LoadCount
                if (mLoadCount >= mCurrentIndex + mRequestCount || mLoadCount >= mCurrentUnitGroupInfoList.size()) {
                    //Check the UnitGroups has been finished.
                    if (mLoadCount < mCurrentUnitGroupInfoList.size()) {
                        //Continue to request
                        loadNetworkAd(mCurrentStrategy, mCurrentPlacementId, mCurrentReqeustId, mCurrentIndex + mRequestCount, mRequestCount);
                    } else {
                        mHasFinishLoad = true;
                        if (!isAllReturn) {
                            return;
                        }
                        mHasReturnAdStatus = true;

                        AgentEventManager.onAgentForATToAppLoadFail(mCurrentReqeustId, mCurrentPlacementId, SDKContext.getInstance().getPsid(), SDKContext.getInstance().getSessionId(mCurrentPlacementId), mCurrentStrategy.getGroupId()
                                , mIsRefresh ? 1 : 0, adError != null ? adError.printStackTrace() : "");

                        mCurrentStrategy.updateSortUnitgroupList(mCurrentUnitGroupInfoList);
                        if (adError != null) {
                            onDeveloLoadFail(adError);
                        } else {
                            onDeveloLoadFail(ErrorCode.getErrorCode(ErrorCode.noADError, "", ""));
                        }
                    }
                }
            }

        }
    }


    public boolean isAllReturn() {
        Collection<Boolean> valueSet = mUnitGroupReturnStatus.values();
        for (Boolean hasReturn : valueSet) {
            //只要有一个没有返回，则直接return，不回调结果
            if (!hasReturn) {
                return false;
            }
        }
        return true;
    }


    protected void loadAd(String placementId, String requestid, PlaceStrategy placeStrategy, List<PlaceStrategy.UnitGroupInfo> list) {

        mCurrentStrategy = placeStrategy;
        mCurrentPlacementId = placementId;
        mCurrentReqeustId = requestid;
        mCurrentUnitGroupInfoList = list;

        int index = 0;

        int requestCount = Math.min(placeStrategy.getRequestUnitGroupNumber(), list.size());

        if (mUnitGroupReturnStatus != null) {
            mUnitGroupReturnStatus.clear();
        } else {
            mUnitGroupReturnStatus = new HashMap<>();
        }

        //Start the long-timeout timer
        networkLongOverTimeLoad(mCurrentStrategy.getLongOverLoadTime());

        loadNetworkAd(placeStrategy, placementId, requestid, index, requestCount);

        startLoadDefaultNetwork(mCurrentUnitGroupInfoList, requestCount); //兜底network请求逻辑
    }


    /**
     * Default Network Request
     **/
    protected void startLoadDefaultNetwork(final List<PlaceStrategy.UnitGroupInfo> list, final int requestCount) {
        TemplateStrategy templateStrategy = mCurrentStrategy.getTemplateStrategy();
        if (templateStrategy == null) {
            return;
        }

        int defaultNetworkFirmId = templateStrategy.defaultNetworkFirmId;
        PlaceStrategy.UnitGroupInfo unitGroupInfo = null;
        for (PlaceStrategy.UnitGroupInfo itemInfo : list) {
            if (itemInfo.networkType == defaultNetworkFirmId) {
                unitGroupInfo = itemInfo;
                break;
            }
        }

        if (unitGroupInfo == null || mUnitGroupLoadTimeMap.containsKey(unitGroupInfo.unitId)) {
            return;
        }


        final PlaceStrategy.UnitGroupInfo defaultUnitGroupInfo = unitGroupInfo;
        SDKContext.getInstance().runOnMainThreadDelayed(new Runnable() {
            @Override
            public void run() {
                if (!mHasReturnAdStatus && !mUnitGroupLoadTimeMap.containsKey(defaultUnitGroupInfo.unitId)) {
                    final AnyThinkBaseAdapter adapter = CustomAdapterFactory.createAdapter(defaultUnitGroupInfo);

                    if (adapter == null) {
                        return;
                    }

                    if ((mActivityRef.get() == null)) {
                        return;
                    }

                    String unitGroupList = "";
                    for (int i = 0; i < mCurrentUnitGroupInfoList.size(); i++) {
                        if (i > 0) {
                            unitGroupList = unitGroupList + ",";
                        }
                        String networkName = mCurrentUnitGroupInfoList.get(i).networkType + "";
                        unitGroupList = unitGroupList + networkName;
                    }

                    AdTrackingInfo adTrackingInfo = TrackingInfoUtil.initTrackingInfo(mCurrentReqeustId, mCurrentPlacementId, mUserId, mCurrentStrategy, unitGroupList, mRequestCount, mIsRefresh);

                    UnitgroupCacheInfo unitgroupCacheInfo = AdCacheManager.getInstance().getUnitgroupCacheInfoByAdSourceId(mCurrentPlacementId, defaultUnitGroupInfo.unitId);
                    AdCacheInfo adCacheInfo = unitgroupCacheInfo != null ? unitgroupCacheInfo.getAdCacheInfo() : null;
                    if (adCacheInfo != null && adCacheInfo.isUpStatusAvaiable() && adCacheInfo.isNetworkAdReady()) {
                        onCacheAdLoaded(adCacheInfo.getBaseAdapter(), adTrackingInfo, defaultUnitGroupInfo);
                        return;
                    }

                    CommonDeviceUtil.putNetworkSDKVersion(defaultUnitGroupInfo.networkType, adapter.getSDKVersion());

                    mUnitGroupLoadTimeMap.put(defaultUnitGroupInfo.unitId, System.currentTimeMillis());

                    adTrackingInfo = TrackingInfoUtil.initPlacementUnitGroupTrackingInfo(adapter, adTrackingInfo, defaultUnitGroupInfo);
                    adTrackingInfo.setmIsDefaultNetwork(true);

                    AdTrackingManager.getInstance(mApplcationContext).addAdTrackingInfo(TrackingV2Loader.AD_REQUEST_TYPE, adTrackingInfo);

                    adapter.log(Const.LOGKEY.REQUEST, Const.LOGKEY.START, "");


                    startLoadAd(adapter, defaultUnitGroupInfo, PlaceStrategy.getServerExtrasMap(mCurrentPlacementId, defaultUnitGroupInfo, mCurrentStrategy.getMyOfferSetting()));

                }
            }
        }, templateStrategy.defaultDelayTime);

    }

    protected int mCurrentIndex;
    protected int mRequestCount;
    protected List<PlaceStrategy.UnitGroupInfo> mCurrentUnitGroupInfoList;
    protected PlaceStrategy mCurrentStrategy;
    protected String mCurrentPlacementId;
    protected String mCurrentReqeustId;
    protected String mUnitGroupList;

    private void loadNetworkAd(final PlaceStrategy strategy
            , final String mPlacementId
            , final String mRequestId
            , final int index
            , final int requestCount) {

        mCurrentIndex = index;
        mRequestCount = requestCount;

        mStartLoadTime = System.currentTimeMillis();

        mUnitGroupList = "";
        for (int i = 0; i < mCurrentUnitGroupInfoList.size(); i++) {
            if (i > 0) {
                mUnitGroupList = mUnitGroupList + ",";
            }
            String networkName = mCurrentUnitGroupInfoList.get(i).networkType + "";
            mUnitGroupList = mUnitGroupList + networkName;
        }

        /**
         * Request by the request's num
         */
        for (int i = index; i < index + requestCount; i++) {
            try {
                if (i >= mCurrentUnitGroupInfoList.size()) {
                    break;
                }
                final PlaceStrategy.UnitGroupInfo unitGroupInfo = mCurrentUnitGroupInfoList.get(i);
                if (unitGroupInfo == null) {
                    continue;
                }

                AdTrackingInfo adTrackingInfo = TrackingInfoUtil.initTrackingInfo(mCurrentReqeustId, mCurrentPlacementId, mUserId, mCurrentStrategy, mUnitGroupList, mRequestCount, mIsRefresh);

                UnitgroupCacheInfo unitgroupCacheInfo = AdCacheManager.getInstance().getUnitgroupCacheInfoByAdSourceId(mCurrentPlacementId, unitGroupInfo.unitId);
                AdCacheInfo adCacheInfo = unitgroupCacheInfo != null ? unitgroupCacheInfo.getAdCacheInfo() : null;
                if (adCacheInfo != null && adCacheInfo.isUpStatusAvaiable() && adCacheInfo.isNetworkAdReady()) {
                    onCacheAdLoaded(adCacheInfo.getBaseAdapter(), adTrackingInfo, unitGroupInfo);
                    continue;
                }

                AnyThinkBaseAdapter adapter = CustomAdapterFactory.createAdapter(unitGroupInfo);
                if (adapter == null) {
                    AdError adError = ErrorCode.getErrorCode(ErrorCode.adapterNotExistError, "", unitGroupInfo.adapterClassName + " does not exit!");
                    onAdError(null, adError);
                    continue;
                }

                CommonDeviceUtil.putNetworkSDKVersion(unitGroupInfo.networkType, adapter.getSDKVersion());

                mUnitGroupReturnStatus.put(adapter, false);

                adTrackingInfo = TrackingInfoUtil.initPlacementUnitGroupTrackingInfo(adapter, adTrackingInfo, unitGroupInfo);


                AdTrackingManager.getInstance(mApplcationContext).addAdTrackingInfo(TrackingV2Loader.AD_REQUEST_TYPE, adTrackingInfo);

                adapter.log(Const.LOGKEY.REQUEST, Const.LOGKEY.START, "");

                /**
                 * Start the timer to request other group
                 */
                Runnable overTimeRunnable = networkAdOvertimeLoad(adapter, unitGroupInfo, unitGroupInfo.getUnitADRequestOutTime());
                if (overTimeRunnable != null) {
                    mOverTimeRunnableMap.put(unitGroupInfo.unitId, overTimeRunnable);
                }

                Runnable adDataOverTimeRunnable = networkAdOvertimeLoad(adapter, unitGroupInfo, unitGroupInfo.getNetworkAdDataLoadTimeOut());
                if (adDataOverTimeRunnable != null) {
                    mAdDataOverTimeRunnableMap.put(unitGroupInfo.unitId, adDataOverTimeRunnable); //存储
                }

                mUnitGroupLoadTimeMap.put(unitGroupInfo.unitId, System.currentTimeMillis());


                if ((mActivityRef.get() == null)) {
                    onAdError(adapter, ErrorCode.getErrorCode(ErrorCode.contextDestoryError, "", ""));
                    return;
                }

                if (mActivityRef.get() instanceof Activity) {
                    adapter.refreshActivityContext((Activity) mActivityRef.get());
                }

                try {
                    //Remove the HB cache because it has been used
                    if (unitGroupInfo.bidType == 1) {
                        HeadBiddingCacheManager.getInstance().removeCache(unitGroupInfo.unitId);
                    }
                } catch (Throwable e) {

                }

                startLoadAd(adapter, unitGroupInfo, PlaceStrategy.getServerExtrasMap(mCurrentPlacementId, unitGroupInfo, mCurrentStrategy.getMyOfferSetting()));
            } catch (Exception e) {
                if (Const.DEBUG) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Timeout Handle
     *
     * @param unitGroupInfo
     */
    private Runnable networkAdOvertimeLoad(final AnyThinkBaseAdapter adapter, final PlaceStrategy.UnitGroupInfo unitGroupInfo, final long overTime) {
        if (overTime == -1) {
            return null;
        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (CommonMediationManager.this) {
                    /**Cacnel the short-timeout runnable**/
                    SDKContext.getInstance().removeMainThreadRunnable(mOverTimeRunnableMap.get(unitGroupInfo.unitId));
                    SDKContext.getInstance().removeMainThreadRunnable(mAdDataOverTimeRunnableMap.get(unitGroupInfo.unitId));
                    boolean isReturn = mUnitGroupReturnStatus.get(adapter);
                    if (isReturn) {
                        return;
                    }
                    mOverLoadMap.put(adapter, true);
                    mLoadCount++;
                    if (!mHasReturnAdStatus) {
                        if (mLoadCount >= mCurrentIndex + mRequestCount || mLoadCount >= mCurrentUnitGroupInfoList.size()) {
                            if (mLoadCount < mCurrentUnitGroupInfoList.size()) {
                                loadNetworkAd(mCurrentStrategy, mCurrentPlacementId, mCurrentReqeustId, mCurrentIndex + mRequestCount, mRequestCount);
                            } else {
                                mHasFinishLoad = true;
                            }
                        }
                    }

                }
            }
        };
        SDKContext.getInstance().runOnMainThreadDelayed(runnable, overTime);
        return runnable;
    }

    /**
     * Long-Timeout
     *
     * @param overTime
     */
    private void networkLongOverTimeLoad(long overTime) {
        SDKContext.getInstance().runOnMainThreadDelayed(mLongOverTimeRunnable, overTime);
    }


    public boolean hasFinishLoad() {
        return mHasFinishLoad;
    }

    /**
     * Cancel callback
     **/
    public void cancelCallback() {
        mHasReturnAdStatus = true;
    }

    /**
     * Cancel cache offer
     **/
    public void cancelCacheOffer() {
        mHasCancelCacheOffer = true;
    }


    public void release() {
        CommonLogUtil.i("CommonMediationManager", "finish load, release source!");
        mIsRelease = true;
        try {
            if (mOverTimeRunnableMap != null) {
                for (Runnable runnable : mOverTimeRunnableMap.values()) {
                    SDKContext.getInstance().removeMainThreadRunnable(runnable);
                }
                mOverTimeRunnableMap.clear();
//                mOverTimeRunnableMap = null;
            }
            if (mAdDataOverTimeRunnableMap != null) {
                for (Runnable runnable : mAdDataOverTimeRunnableMap.values()) {
                    SDKContext.getInstance().removeMainThreadRunnable(runnable);
                }
                mAdDataOverTimeRunnableMap.clear();
//                mAdDataOverTimeRunnableMap = null;

            }
        } catch (Exception e) {
            if (Const.DEBUG) {
                e.printStackTrace();
            }
        }

        if (mUnitGroupReturnStatus != null) {
            mUnitGroupReturnStatus.clear();
//            mUnitGroupReturnStatus = null;
        }


        /**Cancel long-timeout runnable**/
        SDKContext.getInstance().removeMainThreadRunnable(mLongOverTimeRunnable);

        if (mUnitGroupLoadTimeMap != null) {
            mUnitGroupLoadTimeMap.clear();
//            mUnitGroupLoadTimeMap = null;
        }

    }

    protected Map<Integer, ATMediationSetting> mSettingMap;

    public void setNetworkSettingMap(Map<Integer, ATMediationSetting> settingMap) {
        mSettingMap = settingMap;
    }

    public abstract void onDevelopLoaded();

    public abstract void onDeveloLoadFail(AdError adError);

    public abstract void startLoadAd(AnyThinkBaseAdapter baseAdapter, PlaceStrategy.UnitGroupInfo unitGroupInfo, Map<String, Object> serverExtras);

}
