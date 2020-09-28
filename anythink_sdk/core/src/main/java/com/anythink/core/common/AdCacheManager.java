package com.anythink.core.common;

import android.content.Context;
import android.os.Looper;
import android.text.TextUtils;

import com.anythink.core.api.ATBaseAdAdapter;
import com.anythink.core.cap.AdCapV2Manager;
import com.anythink.core.cap.AdPacingManager;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.entity.AdCacheInfo;
import com.anythink.core.common.entity.AdTrackingInfo;
import com.anythink.core.api.BaseAd;
import com.anythink.core.common.entity.TrackerInfo;
import com.anythink.core.common.entity.UnitgroupCacheInfo;
import com.anythink.core.common.track.AgentEventManager;
import com.anythink.core.common.utils.CommonSDKUtil;
import com.anythink.core.common.utils.CustomAdapterFactory;
import com.anythink.core.common.utils.TrackingInfoUtil;
import com.anythink.core.common.utils.task.TaskManager;
import com.anythink.core.strategy.PlaceStrategy;
import com.anythink.core.strategy.PlaceStrategyManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AdCacheManager {

    private static AdCacheManager sIntance;


    private ConcurrentHashMap<String, ConcurrentHashMap<String, UnitgroupCacheInfo>> cacheMap;
    private final HashMap<Integer, Boolean> canInitReadyNetworkMap = new HashMap<>();

    public synchronized static AdCacheManager getInstance() {
        if (sIntance == null) {
            sIntance = new AdCacheManager();
        }
        return sIntance;
    }

    private AdCacheManager() {
        cacheMap = new ConcurrentHashMap<>();
        canInitReadyNetworkMap.put(6, true); //Mintegral
        canInitReadyNetworkMap.put(12, true); //UnityAds
        canInitReadyNetworkMap.put(17, true); //Oneway
        canInitReadyNetworkMap.put(35, true); //MyOffer
    }


    /**
     * Add offer to caches
     *
     * @param placementId
     * @param requestLevel
     * @param adapter
     */
    public UnitgroupCacheInfo addCache(final String placementId, final int requestLevel, final ATBaseAdAdapter adapter, final List<? extends BaseAd> adObjectList, final long cacheTime, final PlaceStrategy placeStrategy) {
        synchronized (AdCacheManager.this) {

            ConcurrentHashMap<String, UnitgroupCacheInfo> unitGroupCacheInfoMap = cacheMap.get(placementId);
            PlaceStrategy.UnitGroupInfo currentUnitGroupInfo = adapter.getmUnitgroupInfo();

            String unitId = adapter.getmUnitgroupInfo().unitId;
            if (unitGroupCacheInfoMap == null) {
                unitGroupCacheInfoMap = new ConcurrentHashMap<>();
                cacheMap.put(placementId, unitGroupCacheInfoMap);
            }

            /**Check the UnitGroup's offer**/
            UnitgroupCacheInfo unitgroupCacheInfo = unitGroupCacheInfoMap.get(unitId);

            if (unitgroupCacheInfo == null) {
                unitgroupCacheInfo = new UnitgroupCacheInfo();
                unitgroupCacheInfo.requestLevel = requestLevel;
                unitgroupCacheInfo.requestId = adapter.getTrackingInfo().getmRequestId();
                unitGroupCacheInfoMap.put(unitId, unitgroupCacheInfo);
            } else {
                unitgroupCacheInfo.requestLevel = requestLevel;
                unitgroupCacheInfo.requestId = adapter.getTrackingInfo().getmRequestId();
            }

            AdCacheInfo cacheInfo = unitgroupCacheInfo.getAdCacheInfo();
            /**If current offer's Request Id equals the newest Request Id, it would not save the cache again.**/
            if (cacheInfo != null && TextUtils.equals(ShowWaterfallManager.getInstance().getWaterFallNewestRequestId(placementId), cacheInfo.getBaseAdapter().getTrackingInfo().getmRequestId())) {
                return unitgroupCacheInfo;
            }

            if (adObjectList != null && adObjectList.size() > 0) {
                List<AdCacheInfo> adCacheInfoLists = new ArrayList<>();
                for (BaseAd adObject : adObjectList) {
                    AdCacheInfo adCacheInfo = new AdCacheInfo();
                    adCacheInfo.setRequestLevel(requestLevel);
                    adCacheInfo.setBaseAdapter(adapter);
                    adCacheInfo.setAdObject(adObject);
                    adCacheInfo.setUpdateTime(System.currentTimeMillis());
                    adCacheInfo.setCacheTime(cacheTime);
                    adCacheInfo.setOriginRequestId(adapter.getTrackingInfo().getmRequestId()); //Origin RequestId
                    adCacheInfo.setUpStatusCacheTime(currentUnitGroupInfo.upStatusTimeOut); //Out-Date time of upstatus

                    adCacheInfoLists.add(adCacheInfo);
                }
                unitgroupCacheInfo.setAdCacheInfoList(adCacheInfoLists);
            } else {
                AdCacheInfo adCacheInfo = new AdCacheInfo();
                adCacheInfo.setRequestLevel(requestLevel);
                adCacheInfo.setBaseAdapter(adapter);
                adCacheInfo.setUpdateTime(System.currentTimeMillis());
                adCacheInfo.setCacheTime(cacheTime);
                adCacheInfo.setOriginRequestId(adapter.getTrackingInfo().getmRequestId()); //Origin RequestId
                adCacheInfo.setUpStatusCacheTime(currentUnitGroupInfo.upStatusTimeOut); //Out-Date time of upstatus

                List<AdCacheInfo> adCacheInfoLists = new ArrayList<>();
                adCacheInfoLists.add(adCacheInfo);
                unitgroupCacheInfo.setAdCacheInfoList(adCacheInfoLists);
            }

            return unitgroupCacheInfo;


        }

    }

    /**
     * Get Offer By PlacementId
     *
     * @param placementId
     * @return
     */
    public AdCacheInfo getCache(Context context, String placementId) {
        synchronized (AdCacheManager.this) {
            return checkCache(context, placementId, false, false);
        }
    }


    /**
     * @param placementId
     * @param isAgent     Need to Agent?
     * @param isShowCall  Show call?
     * @return
     */
    public AdCacheInfo checkCache(Context context, String placementId, boolean isAgent, boolean isShowCall) {

        try {
            if (Looper.myLooper() != Looper.getMainLooper()) {
                Looper.prepare();
            }
        } catch (Throwable e) {
        }

        JSONArray checkArray = new JSONArray();
        List<PlaceStrategy.UnitGroupInfo> myOfferUnitgroupInfoList = new ArrayList<>();
        /**Use the new list object to store the current data, so as to prevent the unitgroup list in the policy from being crashed due to operation**/
        List<PlaceStrategy.UnitGroupInfo> unitGroupInfos = new ArrayList<>();
        synchronized (AdCacheManager.this) {
            PlaceStrategy placeStrategy = PlaceStrategyManager.getInstance(SDKContext.getInstance().getContext()).getPlaceStrategyByAppIdAndPlaceId(placementId);

            if (placeStrategy == null) {
                return null;
            }

            List<PlaceStrategy.UnitGroupInfo> newestUgList = ShowWaterfallManager.getInstance().getNewestWaterFallForPlacementId(placementId);
            String currentRequestId = ShowWaterfallManager.getInstance().getWaterFallNewestRequestId(placementId);


            if (newestUgList != null) {
                unitGroupInfos.addAll(newestUgList);
            }

            ConcurrentHashMap<String, UnitgroupCacheInfo> unitGroupCacheInfoMap = cacheMap.get(placementId);

//            /**Get requestId in the caches**/
//            String requestId = queryRequestIdByOffer(unitGroupInfos, unitGroupCacheInfoMap);

            String psid = SDKContext.getInstance().getPsid();
            String sessionId = SDKContext.getInstance().getSessionId(placementId);
//            int groupId = placeStrategy.getGroupId();

            if (unitGroupInfos != null && unitGroupInfos.size() > 0) {


                for (int i = 0; i < unitGroupInfos.size(); i++) {
                    PlaceStrategy.UnitGroupInfo unitGroupInfo = unitGroupInfos.get(i);
                    if (unitGroupInfo.networkType == MyOfferAPIProxy.MYOFFER_NETWORK_FIRM_ID) {
                        myOfferUnitgroupInfoList.add(unitGroupInfo);
                    }

                    int level = unitGroupInfo.level < 0 ? unitGroupInfo.level : i;

                    if (AdPacingManager.getInstance().isUnitGroupInPacing(placementId, unitGroupInfo)) {
                        addCheckObjectInfo(checkArray, level, unitGroupInfo.unitId, unitGroupInfo.networkType, "", false, TrackerInfo.AD_SOURCE_PACCING_REASON);

                        continue;
                    }

                    if (AdCapV2Manager.getInstance(SDKContext.getInstance().getContext()).isUnitgroupOutOfCap(placementId, unitGroupInfo)) {
                        addCheckObjectInfo(checkArray, level, unitGroupInfo.unitId, unitGroupInfo.networkType, "", false, TrackerInfo.AD_SOURCE_CAPPING_REASON);
                        continue;
                    }

                    boolean isAdReady = false;

                    UnitgroupCacheInfo unitgroupCacheInfo = unitGroupCacheInfoMap != null ?
                            unitGroupCacheInfoMap.get(unitGroupInfo.unitId) : null;

                    AdCacheInfo adCacheInfo = unitgroupCacheInfo != null ? unitgroupCacheInfo.getAdCacheInfo() : null;
                    /**Check the isReady status of Adapter**/
                    if (unitgroupCacheInfo == null || adCacheInfo == null) {
                        ATBaseAdAdapter baseAdapter = null;

                        Boolean canReady = canInitReadyNetworkMap.get(unitGroupInfo.networkType);
                        if (canReady != null && canReady) {
                            baseAdapter = CustomAdapterFactory.createAdapter(unitGroupInfo);
                        }

                        if (baseAdapter != null) {
                            final Map<String, Object> serviceExtras = PlaceStrategy.getServerExtrasMap(placementId, unitGroupInfo, placeStrategy.getMyOfferSetting());
                            final Map<String, Object> localExtras = PlacementAdManager.getInstance().getPlacementLocalSettingMap(placementId);
                            BaseAd baseAdObject = null;
                            try {
                                boolean initSuccess = baseAdapter.internalInitNetworkObjectByPlacementId(context, serviceExtras, localExtras);
                                if (initSuccess) {
                                    currentRequestId = TextUtils.isEmpty(currentRequestId) ? CommonSDKUtil.createRequestId(context) : currentRequestId;
                                    initReadyCacheTrackingInfo(baseAdapter, currentRequestId, placementId, placeStrategy, unitGroupInfo, level);
                                }

                                /**Native Ad Auto Filled**/
                                if (TextUtils.equals(String.valueOf(placeStrategy.getFormat()), Const.FORMAT.NATIVE_FORMAT)) {
                                    isAdReady = initSuccess ? (baseAdObject = baseAdapter.getBaseAdObject(context)) != null : false;
                                } else {
                                    isAdReady = initSuccess ? baseAdapter.isAdReady() : false;
                                }

                            } catch (Throwable e) {
                                if (Const.DEBUG) {
                                    e.printStackTrace();
                                }
                            }

                            if (isAdReady) {

                                List<BaseAd> baseAdList = null;
                                if (baseAdObject != null) {
                                    /**Native Ad Auto Filled**/
                                    baseAdList = new ArrayList<>();
                                    baseAdObject.setTrackingInfo(baseAdapter.getTrackingInfo());
                                    baseAdList.add(baseAdObject);
                                }
                                unitgroupCacheInfo = addCache(placementId, level, baseAdapter, baseAdList, unitGroupInfo.getUnitADCacheTime(), placeStrategy);

                                AdTrackingInfo adTrackingInfo = baseAdapter.getTrackingInfo();
                                adTrackingInfo.setImpressionLevel(level);

                                if (isAgent) {
                                    //Send isReady Agent
                                    AgentEventManager.isReadyEventAgentForATToApp(adTrackingInfo, true, -1, level, unitGroupInfo.unitId, unitGroupInfo.networkType, baseAdapter.getNetworkSDKVersion(), checkArray.toString(), currentRequestId, true, "");
                                }

                                return unitgroupCacheInfo.getAdCacheInfo();
                            } else {
                                addCheckObjectInfo(checkArray, level, unitGroupInfo.unitId, unitGroupInfo.networkType, "", false, TrackerInfo.AD_SOURCE_NO_RESULT_REASON);
                                continue;
                            }
//                            continue;
                        } else {
                            addCheckObjectInfo(checkArray, level, unitGroupInfo.unitId, unitGroupInfo.networkType, "", false, TrackerInfo.AD_SOURCE_NO_RESULT_REASON);
                            continue;
                        }
//                        continue;

                    }


                    if (placeStrategy.getFormat() == Integer.valueOf(Const.FORMAT.NATIVE_FORMAT)) { // Native format
                        isAdReady = adCacheInfo.getBaseAdapter() != null && adCacheInfo.getAdObject() != null;
                    } else {
                        isAdReady = adCacheInfo.getBaseAdapter() != null && adCacheInfo.getBaseAdapter().isAdReady();
                    }

                    if (isAdReady) {
                        if (adCacheInfo.getUpdateTime() + adCacheInfo.getCacheTime() > System.currentTimeMillis()) {
                            //AdCache is available
                            ATBaseAdAdapter baseAdapter = adCacheInfo.getBaseAdapter();

                            addCheckObjectInfo(checkArray, level, unitGroupInfo.unitId, unitGroupInfo.networkType, baseAdapter.getNetworkSDKVersion(), true, -1);

                            AdTrackingInfo adTrackingInfo = baseAdapter.getTrackingInfo();
                            adTrackingInfo.setmAsResult(checkArray.toString()); //Refresh result info

                            adTrackingInfo.setImpressionLevel(level); //Set Impression requestLevel
                            if (isAgent) {
                                AgentEventManager.isReadyEventAgentForATToApp(adTrackingInfo, true, -1, level, unitGroupInfo.unitId, unitGroupInfo.networkType
                                        , baseAdapter.getNetworkSDKVersion(), checkArray.toString(), currentRequestId, adTrackingInfo.getRequestType() == AdTrackingInfo.READY_CHECK
                                        , "");
                            }

                            return adCacheInfo;
                        } else {
                            addCheckObjectInfo(checkArray, level, unitGroupInfo.unitId, unitGroupInfo.networkType, "", false, TrackerInfo.AD_SOURCE_TRUE_BUT_OVERTIME_REASON);
                            continue;
                        }
                    } else {
                        addCheckObjectInfo(checkArray, level, unitGroupInfo.unitId, unitGroupInfo.networkType, "", false, TrackerInfo.AD_SOURCE_RETURN_FALSE_REASON);
                        continue;
                    }
                }

            }

            boolean needUseDefaultMyoffer = false;
            if (placeStrategy.getUseDefaultMyOffer() == 1) {
                needUseDefaultMyoffer = true;
            } else if (placeStrategy.getUseDefaultMyOffer() == 2) {
                needUseDefaultMyoffer = isShowCall;
            }

            /**MyOffer Default Mode**/
            if (myOfferUnitgroupInfoList != null && myOfferUnitgroupInfoList.size() > 0 && needUseDefaultMyoffer) {
                String offerId = MyOfferAPIProxy.getIntance().getDefaultOfferId(context, placementId);
                PlaceStrategy.UnitGroupInfo defaultUnitGroupInfo = null;

                if (!TextUtils.isEmpty(offerId)) {
                    for (PlaceStrategy.UnitGroupInfo myofferInfn : myOfferUnitgroupInfoList) {
                        if (myofferInfn.content != null && myofferInfn.content.contains(offerId)) {
                            defaultUnitGroupInfo = myofferInfn;
                            break;
                        }
                    }
                }

                if (defaultUnitGroupInfo != null) {
                    Map<String, Object> defaultExtras = PlaceStrategy.getServerExtrasMap(placementId, defaultUnitGroupInfo, placeStrategy.getMyOfferSetting());
                    defaultExtras.put(MyOfferAPIProxy.MYOFFER_DEFAULT_TAG, true);
                    int defaultMyOfferLevel = unitGroupInfos.indexOf(defaultUnitGroupInfo);
                    try {
                        ATBaseAdAdapter baseAdapter = CustomAdapterFactory.createAdapter(defaultUnitGroupInfo);
                        boolean initSuccess = baseAdapter.initNetworkObjectByPlacementId(context, defaultExtras, PlacementAdManager.getInstance().getPlacementLocalSettingMap(placementId));
                        if (initSuccess) {
                            /**Save AdCache**/
                            currentRequestId = TextUtils.isEmpty(currentRequestId) ? CommonSDKUtil.createRequestId(context) : currentRequestId;
                            initReadyCacheTrackingInfo(baseAdapter, currentRequestId, placementId, placeStrategy, defaultUnitGroupInfo, defaultMyOfferLevel);
                        }
                        boolean isAdReady = false;
                        BaseAd baseAdObject = null;
                        /**Native Ad Default Filled**/
                        if (TextUtils.equals(String.valueOf(placeStrategy.getFormat()), Const.FORMAT.NATIVE_FORMAT)) {
                            isAdReady = initSuccess ? (baseAdObject = baseAdapter.getBaseAdObject(context)) != null : false;
                        } else {
                            isAdReady = initSuccess ? baseAdapter.isAdReady() : false;
                        }
                        if (isAdReady) {

                            List<BaseAd> baseAdList = null;
                            if (baseAdObject != null) {
                                /**Native Ad Default Filled**/
                                baseAdList = new ArrayList<>();
                                baseAdObject.setTrackingInfo(baseAdapter.getTrackingInfo());
                                baseAdList.add(baseAdObject);
                            }

                            UnitgroupCacheInfo unitgroupCacheInfo = addCache(placementId, defaultMyOfferLevel, baseAdapter, baseAdList, defaultUnitGroupInfo.getUnitADCacheTime(), placeStrategy);

                            AdTrackingInfo adTrackingInfo = baseAdapter.getTrackingInfo();
                            //Default MyOffer Type
                            adTrackingInfo.setMyOfferShowType(1);
                            adTrackingInfo.setImpressionLevel(defaultMyOfferLevel);
                            adTrackingInfo.setmAsResult(checkArray.toString());

                            if (isAgent) {
                                AgentEventManager.isReadyEventAgentForATToApp(adTrackingInfo, true, -1, defaultMyOfferLevel, defaultUnitGroupInfo.unitId, defaultUnitGroupInfo.networkType
                                        , baseAdapter.getNetworkSDKVersion(), checkArray.toString(), currentRequestId, true, defaultUnitGroupInfo.content);
                            }

                            return unitgroupCacheInfo.getAdCacheInfo();
                        }
                    } catch (Throwable e) {

                    }
                }
            }


            if (isAgent) {
                AdTrackingInfo adTrackingInfo = TrackingInfoUtil.getTrackingInfoForAgent(String.valueOf(placeStrategy.getFormat()), currentRequestId, placementId, psid, sessionId, placeStrategy, 0);
                AgentEventManager.isReadyEventAgentForATToApp(adTrackingInfo, false, AgentEventManager.NO_READY_ADSOURCE_REASON
                        , -1, "", -1, "", checkArray.toString(), currentRequestId, false, "");
                if (isShowCall) { //Show Fail Agent
                    AgentEventManager.onAdShowFail(adTrackingInfo, AgentEventManager.NO_READY_ADSOURCE_REASON, checkArray.toString(), currentRequestId);
                }

            }

            return null;
        }
    }

//    /**
//     * Get RequestId by AdCache
//     *
//     * @param currentList
//     * @param placementUnitgrouInfoMap
//     * @return
//     */
//    private String queryRequestIdByOffer(List<PlaceStrategy.UnitGroupInfo> currentList, ConcurrentHashMap<String, UnitgroupCacheInfo> placementUnitgrouInfoMap) {
//        if (currentList != null && currentList.size() > 0 && placementUnitgrouInfoMap != null) {
//            for (PlaceStrategy.UnitGroupInfo unitGroupInfo : currentList) {
//                UnitgroupCacheInfo unitgroupCacheInfo = placementUnitgrouInfoMap.get(unitGroupInfo.unitId);
//                if (unitgroupCacheInfo != null) {
//                    return unitgroupCacheInfo.requestId;
//                }
//            }
//        }
//        return "";
//    }


    private void initReadyCacheTrackingInfo(ATBaseAdAdapter adapter, String requestId, String placementId, PlaceStrategy placeStrategy, PlaceStrategy.UnitGroupInfo unitGroupInfo, int requestLevel) {
        AdTrackingInfo adTrackingInfo = TrackingInfoUtil.initTrackingInfo(requestId, placementId, "", placeStrategy, unitGroupInfo.networkType + "", 1, false);
        TrackingInfoUtil.initPlacementUnitGroupTrackingInfo(adapter, adTrackingInfo, unitGroupInfo, requestLevel);
        adTrackingInfo.setRequestType(AdTrackingInfo.READY_CHECK);
        //set placement id for network
        adTrackingInfo.setmNetworkPlacementId(adapter.getNetworkPlacementId());
        adapter.setRefresh(false);
    }

    private void addCheckObjectInfo(JSONArray jsonArray, int level, String adSourceId, int networkType, String networkVersion, boolean isSuccess, int reason) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("priority", level);
            jsonObject.put("unit_id", adSourceId);
            jsonObject.put("nw_firm_id", networkType);
            jsonObject.put("nw_ver", networkVersion);
            jsonObject.put("result", isSuccess ? 1 : 0);
            if (reason != -1) {
                jsonObject.put("reason", reason);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        jsonArray.put(jsonObject);
    }


    /**
     * Get AdCache had been show
     *
     * @param placementId
     * @return
     */
//    public AdCacheInfo getHasShowAdCache(String placementId, String requestId) {
//        synchronized (AdCacheManager.this) {
//            ConcurrentHashMap<String, UnitgroupCacheInfo> unitGroupCacheInfoMap = cacheMap.get(placementId);
//
//            if (unitGroupCacheInfoMap != null && unitGroupCacheInfoMap.size() > 0) {
//                if (unitGroupCacheInfoMap.values() != null && unitGroupCacheInfoMap.values().size() > 0) {
//                    for (UnitgroupCacheInfo unitgroupCacheInfo : unitGroupCacheInfoMap.values()) {
//                        if (unitgroupCacheInfo != null) {
//                            AdCacheInfo adCacheInfo = unitgroupCacheInfo.getHasShowAdCacheInfo();
//                            if (adCacheInfo != null && TextUtils.equals(adCacheInfo.getBaseAdapter().getTrackingInfo().getmRequestId(), requestId)) {
//                                return adCacheInfo;
//                            }
//                        }
//                    }
//                }
//            }
//            return null;
//        }
//    }
    @Deprecated
    public void cleanNoShowOffer(String placementId) {
    }

    /***
     * Clear UnitGroup's AdCache
     * @param placementId
     * @param unitgroupId
     */
    public void forceCleanCache(String placementId, String unitgroupId) {
        synchronized (this) {
            ConcurrentHashMap<String, UnitgroupCacheInfo> unitGroupCacheInfoMap = cacheMap.get(placementId);
            if (unitGroupCacheInfoMap != null && unitGroupCacheInfoMap.size() > 0) {
                UnitgroupCacheInfo unitgroupCacheInfo = unitGroupCacheInfoMap.remove(unitgroupId);
                if (unitgroupCacheInfo != null) {
                    unitgroupCacheInfo.destoryCache();
                }
            }
        }
    }

    /**
     * @param placementId
     * @return
     */
//    public PlaceStrategy getCacheStrategy(String placementId) {
//        ConcurrentHashMap<String, UnitgroupCacheInfo> unitGroupCacheInfoMap = cacheMap.get(placementId);
//        if (unitGroupCacheInfoMap != null) {
//            Collection<UnitgroupCacheInfo> unitgroupCacheInfoList = unitGroupCacheInfoMap.values();
//            if (unitgroupCacheInfoList != null && unitgroupCacheInfoList.size() > 0) {
//                UnitgroupCacheInfo unitgroupCacheInfo = unitgroupCacheInfoList.iterator().next();
//                return unitgroupCacheInfo != null ? unitgroupCacheInfo.placeStrategy : null;
//            }
//        }
//        return null;
//    }

    /**
     * Save impression
     *
     * @param context
     * @param adCacheInfo
     */
    public void saveShowTime(final Context context, final AdCacheInfo adCacheInfo) {
        synchronized (AdCacheManager.this) {
            final AdTrackingInfo adTrackingInfo = adCacheInfo.getBaseAdapter().getTrackingInfo();

            if (adTrackingInfo != null) {
                /**Being setted in the impression of format**/
//                adCacheInfo.setShowTime(adCacheInfo.getShowTime() + 1);
                TaskManager.getInstance().run_proxy(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (AdCacheManager.this) {
                            CommonAdManager commonAdManager = PlacementAdManager.getInstance().getAdManager(adTrackingInfo.getmPlacementId());
                            commonAdManager.notifyMediationManagerImpression(adTrackingInfo.getmRequestId(), adCacheInfo.getBaseAdapter().getmUnitgroupInfo().ecpm);
                            //Save cap
                            AdCapV2Manager.getInstance(context).saveOneCap(adTrackingInfo.getmAdType(), adTrackingInfo.getmPlacementId(), adTrackingInfo.getmUnitGroupUnitId());
                            //Recore time
                            AdPacingManager.getInstance().savePlacementShowTime(adTrackingInfo.getmPlacementId());
                            AdPacingManager.getInstance().saveUnitGropuShowTime(adTrackingInfo.getmPlacementId(), adTrackingInfo.getmUnitGroupUnitId());

                            if (adCacheInfo.isLast()) {
                                //If the last offer of AdSource had been showed, it would be removed from caches.
                                forceCleanCache(adTrackingInfo.getmPlacementId(), adTrackingInfo.getmUnitGroupUnitId());
                            }

                        }
                    }
                });
            }
        }
    }


    public UnitgroupCacheInfo getUnitgroupCacheInfoByAdSourceId(String placementId, String adsourceId) {
        ConcurrentHashMap<String, UnitgroupCacheInfo> unitGroupCacheInfoMap = cacheMap.get(placementId);
        if (unitGroupCacheInfoMap != null) {
            UnitgroupCacheInfo unitgroupCacheInfo = unitGroupCacheInfoMap.get(adsourceId);
            AdCacheInfo adCacheInfo = unitgroupCacheInfo != null ? unitgroupCacheInfo.getAdCacheInfo() : null;
            if (adCacheInfo != null && adCacheInfo.getUpdateTime() + adCacheInfo.getCacheTime() > System.currentTimeMillis()) {
                return unitgroupCacheInfo;
            }
            return null;
        }
        return null;
    }


    /**
     * Refresh Offer Tracking Info
     **/
    public synchronized void refreshCacheInfo(List<PlaceStrategy.UnitGroupInfo> unitGroupInfoList, String placementId, PlaceStrategy placementStrategy, String mCurrentReqeustId, String mUserId, String mUnitGroupList, boolean mIsRefresh) {
        for (PlaceStrategy.UnitGroupInfo unitGroupInfo : unitGroupInfoList) {
            ConcurrentHashMap<String, UnitgroupCacheInfo> unitGroupCacheInfoMap = cacheMap.get(placementId); //获取Placement下的Cache信息
            if (unitGroupCacheInfoMap != null) {
                UnitgroupCacheInfo unitgroupCacheInfo = unitGroupCacheInfoMap.get(unitGroupInfo.unitId);

                if (unitgroupCacheInfo == null) {
                    continue;
                }

                AdCacheInfo adCacheInfo = unitgroupCacheInfo.getAdCacheInfo();

                if (adCacheInfo != null
                        && adCacheInfo.getUpdateTime() + adCacheInfo.getCacheTime() > System.currentTimeMillis()) {

                    if (adCacheInfo.getOriginRequestId().equals(mCurrentReqeustId)) {
                        continue;
                    }

                    if (adCacheInfo.isUpStatusAvaiable() && adCacheInfo.isNetworkAdReady()) {
                        /**Refresh Adaper's Tracking Info**/
                        AdTrackingInfo ugAdTrackingInfo = TrackingInfoUtil.initTrackingInfo(mCurrentReqeustId, placementId
                                , mUserId, placementStrategy, mUnitGroupList, placementStrategy.getRequestUnitGroupNumber()
                                , mIsRefresh);

                        int requestLevel = unitGroupInfoList.indexOf(unitGroupInfo);
                        TrackingInfoUtil.initPlacementUnitGroupTrackingInfo(adCacheInfo.getBaseAdapter(), ugAdTrackingInfo, unitGroupInfo, requestLevel);
                        ugAdTrackingInfo.setRequestType(AdTrackingInfo.UPSTATUS_REQUEST); //4:upstatus=1 and upstatus is available

                        //Send Agent
                        AgentEventManager.upStatusAvaibleCache(ugAdTrackingInfo, adCacheInfo.getOriginRequestId());


                        unitgroupCacheInfo.refreshCacheInfo(ugAdTrackingInfo, requestLevel); //Refresh AdCache Info
                        continue;
                    }
                }

                unitGroupCacheInfoMap.remove(unitGroupInfo.unitId); //Clear if no available AdCache
            }

        }
    }


}
