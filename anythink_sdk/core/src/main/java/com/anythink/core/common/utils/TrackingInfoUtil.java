package com.anythink.core.common.utils;

import android.content.Context;

import com.anythink.core.api.ATBaseAdAdapter;
import com.anythink.core.cap.AdCapV2Manager;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.entity.AdTrackingInfo;
import com.anythink.core.common.entity.PlacementImpressionInfo;
import com.anythink.core.strategy.PlaceStrategy;

import java.util.Map;

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


        if (mCurrentStrategy.getFormat() == Integer.parseInt(Const.FORMAT.REWARDEDVIDEO_FORMAT)) {
            adTrackingInfo.setmSourceType("1");
        } else {
            adTrackingInfo.setmSourceType("0");
        }

        adTrackingInfo.setmAdType(String.valueOf(mCurrentStrategy.getFormat()));
        adTrackingInfo.setUserInfo(mUserId);

        adTrackingInfo.setmTrafficGroupId(mCurrentStrategy.getTracfficGroupId());

        adTrackingInfo.setmNetworkList(unitGroupList);
        adTrackingInfo.setmRequestNetworkNum(requestCount);
        adTrackingInfo.setmRefresh(mIsRefresh ? 1 : 0);

        adTrackingInfo.setLoadStatus(AdTrackingInfo.NORMAL_CALLBACK);
        adTrackingInfo.setRequestType(AdTrackingInfo.HANDLE_REQUEST);
        adTrackingInfo.setAsid(mCurrentStrategy.getAsid());
        adTrackingInfo.setFlag(AdTrackingInfo.NO_SHOW_CACHE);

        adTrackingInfo.setmCountry(mCurrentStrategy.getCountry());
        adTrackingInfo.setmCurrency(mCurrentStrategy.getCurrency());
        adTrackingInfo.setmScenarioRewardMap(mCurrentStrategy.getScenarioRewardMap());
        adTrackingInfo.setmPlacementRewardInfo(mCurrentStrategy.getPlacementRewardInfo());
        adTrackingInfo.setmCustomRule(mCurrentStrategy.getSdkCustomMap());

        adTrackingInfo.setmHBWaitingToRequestTime(mCurrentStrategy.getHbWaitingToRequestTime());
        adTrackingInfo.setmHBBidTimeout(mCurrentStrategy.getHbBidTimeout());

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
    public static AdTrackingInfo initPlacementUnitGroupTrackingInfo(ATBaseAdAdapter baseAdapter, AdTrackingInfo adTrackingInfo, PlaceStrategy.UnitGroupInfo unitGroupInfo, int requestLevel) {

        PlacementImpressionInfo.AdSourceImpressionInfo adSourceImpressionInfo = AdCapV2Manager.getInstance(SDKContext.getInstance().getContext()).getUnitGroupImpressionInfo(adTrackingInfo.getmPlacementId(), unitGroupInfo.unitId);

        adTrackingInfo.setmNetworkType(unitGroupInfo.networkType);
        adTrackingInfo.setmUnitGroupUnitId(unitGroupInfo.unitId);
        adTrackingInfo.setmShowTkSwitch(unitGroupInfo.showTkSwitch);
        adTrackingInfo.setmClickTkSwtich(unitGroupInfo.clickTkSwitch);
        adTrackingInfo.setRequestLevel(requestLevel);
        adTrackingInfo.setmNetworkContent(unitGroupInfo.content);
        adTrackingInfo.setmHourlyFrequency(adSourceImpressionInfo != null ? adSourceImpressionInfo.hourShowCount : 0);
        adTrackingInfo.setmDailyFrequency(adSourceImpressionInfo != null ? adSourceImpressionInfo.dayShowCount : 0);
        adTrackingInfo.setmBidPrice(unitGroupInfo.getEcpm());
        adTrackingInfo.setmBidType(unitGroupInfo.bidType);
        adTrackingInfo.setmClickTkUrl(unitGroupInfo.getClickTkUrl());
        adTrackingInfo.setmClickTkDelayMinTime(unitGroupInfo.getClickTkDelayMinTime());
        adTrackingInfo.setmClickTkDelayMaxTime(unitGroupInfo.getClickTkDelayMaxTime());
        adTrackingInfo.setmEcpmLevel(unitGroupInfo.getEcpmLayLevel());
        adTrackingInfo.setmEcpmPrecision(unitGroupInfo.getEcpmPrecision());

        try {
            adTrackingInfo.setmNetworkVersion(baseAdapter.getNetworkSDKVersion());
        } catch (Throwable e) {

        }

        adTrackingInfo.setNetworkName(baseAdapter.getNetworkName());

        baseAdapter.setmUnitgroupInfo(unitGroupInfo);
        baseAdapter.setRefresh(adTrackingInfo.getmRefresh() == 1);

        baseAdapter.setTrackingInfo(adTrackingInfo);

        return adTrackingInfo;
    }

    public static AdTrackingInfo getTrackingInfoForAgent(String format, String requestId, String placementId, String psid, String sessionId, PlaceStrategy placeStrategy, int isRefresh) {

        AdTrackingInfo adTrackingInfo = new AdTrackingInfo();
        adTrackingInfo.setmAdType(format);
        adTrackingInfo.setmPlacementId(placementId);
        adTrackingInfo.setmRequestId(requestId);
        adTrackingInfo.setmRefresh(isRefresh);
        adTrackingInfo.setmTrafficGroupId(placeStrategy != null ? placeStrategy.getTracfficGroupId() : 0);
        adTrackingInfo.setmGroupId(placeStrategy != null ? placeStrategy.getGroupId() : 0);
        adTrackingInfo.setAsid(placeStrategy != null ? placeStrategy.getAsid() : "");
        return adTrackingInfo;
    }


    /**
     * Impression Tracking fill show time (include current impression)
     *
     * @param context
     * @param adTrackingInfo
     */
    public static void fillTrackingInfoShowTime(Context context, AdTrackingInfo adTrackingInfo) {
        long time = System.currentTimeMillis();
        Map<String, PlacementImpressionInfo> impressionInfoMap = AdCapV2Manager.getInstance(context).getFormatShowTime(Integer.parseInt(adTrackingInfo.getmAdType()));
        int formatDayShowTime = 0;
        int formatHourShowTime = 0;

        PlacementImpressionInfo placementImpressionInfo = null;
        if (impressionInfoMap != null) {
            for (PlacementImpressionInfo impressionInfo : impressionInfoMap.values()) {
                formatDayShowTime += impressionInfo.dayShowCount;
                formatHourShowTime += impressionInfo.hourShowCount;
            }

            placementImpressionInfo = impressionInfoMap.get(adTrackingInfo.getmPlacementId());
        }

        adTrackingInfo.setAdTypeDayShowTime(formatDayShowTime + 1);
        adTrackingInfo.setAdTypeHourShowTime(formatHourShowTime + 1);
        adTrackingInfo.setPlacementDayShowTime((placementImpressionInfo != null ? placementImpressionInfo.dayShowCount : 0) + 1);
        adTrackingInfo.setPlacementHourShowTime((placementImpressionInfo != null ? placementImpressionInfo.hourShowCount : 0) + 1);
        CommonLogUtil.i("anythink", "Check cap waite time:" + (System.currentTimeMillis() - time));
    }

}
