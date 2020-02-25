package com.anythink.core.common.utils;

import com.anythink.core.cap.AdCapManager;
import com.anythink.core.common.base.AnyThinkBaseAdapter;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.entity.AdTrackingInfo;
import com.anythink.core.strategy.PlaceStrategy;

/**
 * Tracking Info Utils
 */
public class TrackingInfoUtil {

    /**
     * init placement tracking info
     *
     * @param mCurrentReqeustId
     * @param mCurrentPlacementId
     * @param mUserId
     * @param mCurrentStrategy
     * @param unitGroupList
     * @param requestCount
     * @param mIsRefresh
     * @return
     */
    public static AdTrackingInfo initTrackingInfo(String mCurrentReqeustId, String mCurrentPlacementId, String mUserId, PlaceStrategy mCurrentStrategy, String unitGroupList, int requestCount, boolean mIsRefresh) {
        AdTrackingInfo adTrackingInfo = new AdTrackingInfo();
        adTrackingInfo.setmPlacementId(mCurrentPlacementId);
        adTrackingInfo.setmRequestId(mCurrentReqeustId);
        adTrackingInfo.setmGroupId(mCurrentStrategy.getGroupId());


        if (mCurrentStrategy.getFormat() == 1) {
            adTrackingInfo.setmSourceType("1");
        } else {
            adTrackingInfo.setmSourceType("0");
        }

        adTrackingInfo.setmAdType(String.valueOf(mCurrentStrategy.getFormat()));
        adTrackingInfo.setUserInfo(mUserId);

        adTrackingInfo.setmPsid(SDKContext.getInstance().getPsid());
        adTrackingInfo.setmSessionId(SDKContext.getInstance().getSessionId(mCurrentPlacementId));
        adTrackingInfo.setmTrafficGroupId(mCurrentStrategy.getTracfficGroupId());

        adTrackingInfo.setmNetworkList(unitGroupList);
        adTrackingInfo.setmRequestNetworkNum(requestCount);
        adTrackingInfo.setmRefresh(mIsRefresh ? 1 : 0);

        adTrackingInfo.setLoadStatus(AdTrackingInfo.NORMAL_CALLBACK);
        adTrackingInfo.setRequestType(AdTrackingInfo.HANDLE_REQUEST);
        adTrackingInfo.setAsid(mCurrentStrategy.getAsid());
        adTrackingInfo.setFlag(AdTrackingInfo.NO_SHOW_CACHE);

        return adTrackingInfo;
    }


    /**
     * placement+unitgroup tacking info
     *
     * @param baseAdapter
     * @param adTrackingInfo
     * @param unitGroupInfo
     * @return
     */
    public static AdTrackingInfo initPlacementUnitGroupTrackingInfo(AnyThinkBaseAdapter baseAdapter, AdTrackingInfo adTrackingInfo, PlaceStrategy.UnitGroupInfo unitGroupInfo) {

        AdCapManager.CapInfo capInfo = AdCapManager.getInstance(SDKContext.getInstance().getContext()).getCapByPlaceIdAndUnitGroupId(adTrackingInfo.getmPlacementId(), unitGroupInfo.unitId);

        adTrackingInfo.setmNetworkType(unitGroupInfo.networkType);
        adTrackingInfo.setmUnitGroupUnitId(unitGroupInfo.unitId);
        adTrackingInfo.setmShowTkSwitch(unitGroupInfo.showTkSwitch);
        adTrackingInfo.setmClickTkSwtich(unitGroupInfo.clickTkSwitch);
        adTrackingInfo.setmLevel(unitGroupInfo.level);
        adTrackingInfo.setmNetworkContent(unitGroupInfo.content);
        adTrackingInfo.setmHourlyFrequency(capInfo.getUnitGroupHourCatById(unitGroupInfo.unitId));
        adTrackingInfo.setmDailyFrequency(capInfo.getUnitGroupDayCatById(unitGroupInfo.unitId));
        adTrackingInfo.setmLevel(unitGroupInfo.level);
        adTrackingInfo.setmBidPrice(unitGroupInfo.getEcpm());
        adTrackingInfo.setmBidType(unitGroupInfo.bidType);
        adTrackingInfo.setmClickTkUrl(unitGroupInfo.getClickTkUrl());
        adTrackingInfo.setmClickTkDelayMinTime(unitGroupInfo.getClickTkDelayMinTime());
        adTrackingInfo.setmClickTkDelayMaxTime(unitGroupInfo.getClickTkDelayMaxTime());

        adTrackingInfo.setmNetworkVersion(baseAdapter.getSDKVersion());
        adTrackingInfo.setNetworkName(baseAdapter.getNetworkName());

        baseAdapter.setmUnitgroupInfo(unitGroupInfo);
        baseAdapter.setRefresh(adTrackingInfo.getmRefresh() == 1);

        baseAdapter.setTrackingInfo(adTrackingInfo);

        return adTrackingInfo;
    }

}
