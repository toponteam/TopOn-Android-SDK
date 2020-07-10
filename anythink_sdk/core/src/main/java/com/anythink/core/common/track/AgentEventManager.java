package com.anythink.core.common.track;

import android.text.TextUtils;

import com.anythink.core.api.AdError;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.entity.AdTrackingInfo;
import com.anythink.core.common.entity.AgentInfoBean;
import com.anythink.core.common.utils.task.TaskManager;
import com.anythink.core.strategy.AppStrategy;
import com.anythink.core.strategy.AppStrategyManager;
import com.anythink.core.strategy.PlaceStrategy;
import com.anythink.core.strategy.PlaceStrategyManager;

import java.util.Map;

public class AgentEventManager {

    public static final int NO_READY_ADSOURCE_REASON = 1;
    public static final int PLACEMENT_CAPPING_REASON = 2;
    public static final int PLACEMENT_PACCING_REASON = 3;
    public static final int PLACEMENT_STRATEGY_NULL_REASON = 4;


    public static void onAgentForATToAppLoadFail(AdTrackingInfo adTrackingInfo, String allReason) {
        try {
            AgentInfoBean agentInfoBean = new AgentInfoBean();
            agentInfoBean.key = "1004630";
            agentInfoBean.requestId = adTrackingInfo.getmRequestId();
            agentInfoBean.unitId = adTrackingInfo.getmPlacementId();
//            agentInfoBean.psid = SDKContext.getInstance().getPsid();
//            agentInfoBean.sessionId = adTrackingInfo.getmSessionId();
            agentInfoBean.trafficGroupId = String.valueOf(adTrackingInfo.getmTrafficGroupId());
            agentInfoBean.groupId = String.valueOf(adTrackingInfo.getmGroupId());
            agentInfoBean.refresh = String.valueOf(adTrackingInfo.getmRefresh());

            PlaceStrategy placeStrategy = PlaceStrategyManager.getInstance(SDKContext.getInstance().getContext()).getPlaceStrategyByAppIdAndPlaceId(adTrackingInfo.getmPlacementId());
            agentInfoBean.asid = placeStrategy != null ? placeStrategy.getAsid() : "";
            agentInfoBean.msg = allReason;

            agentInfoBean.format = adTrackingInfo.getmAdType();

            handleEventSend(agentInfoBean);
        } catch (Throwable e) {
            if (Const.DEBUG) {
                e.printStackTrace();
            }
        }
    }

    private static void onAdsourceLoadFail(String requestId, String placementId, int groupId, int refresh, int networkType, String adSourceId, String format, int level, int reason, AdError adError, int bidType, double bidPrice, long failTime, int trafficGroupId) {

        AgentInfoBean agentInfoBean = new AgentInfoBean();
        agentInfoBean.key = "1004631";
        agentInfoBean.requestId = requestId;
        agentInfoBean.unitId = placementId;
//        agentInfoBean.psid = SDKContext.getInstance().getPsid();
//        agentInfoBean.sessionId = SDKContext.getInstance().getSessionId(placementId);
        agentInfoBean.groupId = String.valueOf(groupId);
        agentInfoBean.refresh = String.valueOf(refresh);
        agentInfoBean.trafficGroupId = String.valueOf(trafficGroupId);
        agentInfoBean.msg = String.valueOf(networkType);
        agentInfoBean.msg1 = adSourceId;
        agentInfoBean.msg2 = String.valueOf(level);
        agentInfoBean.msg3 = String.valueOf(reason);
        agentInfoBean.msg4 = adError != null ? adError.getPlatformCode() : "";
        agentInfoBean.msg5 = adError != null ? adError.getPlatformMSG() : "";
        agentInfoBean.msg6 = String.valueOf(bidType);
        agentInfoBean.msg7 = String.valueOf(bidPrice);
        if (reason == 0) {
            agentInfoBean.msg8 = String.valueOf(failTime);
        }

        PlaceStrategy placeStrategy = PlaceStrategyManager.getInstance(SDKContext.getInstance().getContext()).getPlaceStrategyByAppIdAndPlaceId(placementId);
        agentInfoBean.asid = placeStrategy != null ? placeStrategy.getAsid() : "";

        agentInfoBean.format = format;

        handleEventSend(agentInfoBean);
    }

    public static void onAdsourceLoadFail(AdTrackingInfo adTrackingInfo, PlaceStrategy.UnitGroupInfo unitGroupInfo, int level, int reason, AdError adError, long failTime) {
        try {
            onAdsourceLoadFail(
                    adTrackingInfo.getmRequestId(),
                    adTrackingInfo.getmPlacementId(),
                    adTrackingInfo.getmGroupId(),
                    adTrackingInfo.getmRefresh(),
                    unitGroupInfo.networkType,
                    unitGroupInfo.unitId,
                    adTrackingInfo.getmAdType(),
                    level,
                    reason,
                    adError,
                    unitGroupInfo.bidType,
                    unitGroupInfo.getEcpm(),
                    failTime,
                    adTrackingInfo.getmTrafficGroupId()
            );
        } catch (Throwable e) {
            if (Const.DEBUG) {
                e.printStackTrace();
            }
        }
    }

    public static void onAdsourceLoadFail(AdTrackingInfo adTrackingInfo, int reason, AdError adError, long failTime) {
        try {
            onAdsourceLoadFail(
                    adTrackingInfo.getmRequestId(),
                    adTrackingInfo.getmPlacementId(),
                    adTrackingInfo.getmGroupId(),
                    adTrackingInfo.getmRefresh(),
                    adTrackingInfo.getmNetworkType(),
                    adTrackingInfo.getmUnitGroupUnitId(),
                    adTrackingInfo.getmAdType(),
                    adTrackingInfo.getmLevel(),
                    reason,
                    adError,
                    adTrackingInfo.getmBidType(),
                    adTrackingInfo.getmBidPrice(),
                    failTime,
                    adTrackingInfo.getmTrafficGroupId()
            );
        } catch (Throwable e) {
            if (Const.DEBUG) {
                e.printStackTrace();
            }
        }
    }

    public static void onAdShowFail(AdTrackingInfo adTrackingInfo, int allReason, String resultArrayJson, String currentRequestId) {

        try {
            AgentInfoBean agentInfoBean = new AgentInfoBean();
            agentInfoBean.key = "1004633";
            agentInfoBean.requestId = adTrackingInfo.getmRequestId();
            agentInfoBean.unitId = adTrackingInfo.getmPlacementId();
//            agentInfoBean.psid = SDKContext.getInstance().getPsid();
//            agentInfoBean.sessionId = SDKContext.getInstance().getSessionId(adTrackingInfo.getmPlacementId());
            agentInfoBean.groupId = String.valueOf(adTrackingInfo.getmGroupId());
            agentInfoBean.refresh = String.valueOf(adTrackingInfo.getmRefresh());
            agentInfoBean.trafficGroupId = String.valueOf(adTrackingInfo.getmTrafficGroupId());
            agentInfoBean.msg = String.valueOf(allReason);
            agentInfoBean.msg1 = resultArrayJson;

            agentInfoBean.msg4 = currentRequestId;

            if (TextUtils.equals(currentRequestId, adTrackingInfo.getmRequestId())) {
                agentInfoBean.msg5 = "0";
            } else {
                agentInfoBean.msg5 = "1";
            }

            PlaceStrategy placeStrategy = PlaceStrategyManager.getInstance(SDKContext.getInstance().getContext()).getPlaceStrategyByAppIdAndPlaceId(adTrackingInfo.getmPlacementId());
            agentInfoBean.asid = placeStrategy != null ? placeStrategy.getAsid() : "";

            agentInfoBean.format = adTrackingInfo.getmAdType();

            handleEventSend(agentInfoBean);

        } catch (Throwable e) {
            if (Const.DEBUG) {
                e.printStackTrace();
            }
        }
    }


    public static void onAdCloseAgent(AdTrackingInfo adTrackingInfo, boolean isReward) {
        AgentInfoBean agentInfoBean = new AgentInfoBean();
        agentInfoBean.key = "1004634";
        agentInfoBean.requestId = adTrackingInfo.getmRequestId();
        agentInfoBean.unitId = adTrackingInfo.getmPlacementId();
//        agentInfoBean.psid = SDKContext.getInstance().getPsid();
//        agentInfoBean.sessionId = SDKContext.getInstance().getSessionId(adTrackingInfo.getmPlacementId());
        agentInfoBean.groupId = String.valueOf(adTrackingInfo.getmGroupId());
        agentInfoBean.refresh = String.valueOf(adTrackingInfo.getmRefresh());
        agentInfoBean.trafficGroupId = String.valueOf(adTrackingInfo.getmTrafficGroupId());
        agentInfoBean.msg = String.valueOf(adTrackingInfo.getmNetworkType());
        agentInfoBean.msg1 = adTrackingInfo.getmUnitGroupUnitId();
        agentInfoBean.msg2 = String.valueOf(adTrackingInfo.getmLevel());
        agentInfoBean.msg3 = isReward ? "1" : "0";
        agentInfoBean.msg4 = String.valueOf(adTrackingInfo.getMyOfferShowType());

        PlaceStrategy placeStrategy = PlaceStrategyManager.getInstance(SDKContext.getInstance().getContext()).getPlaceStrategyByAppIdAndPlaceId(adTrackingInfo.getmPlacementId());
        agentInfoBean.asid = placeStrategy != null ? placeStrategy.getAsid() : "";

        agentInfoBean.format = adTrackingInfo.getmAdType();

        handleEventSend(agentInfoBean);
    }

    public static void isReadyEventAgentForATToApp(final AdTrackingInfo adTrackingInfo, final boolean isSuccess, final int allReason, final int level, final String adSourceId, final int networkType, final String networkVersion, final String resultArrayJson
            , final String currentRequestId, final boolean isCacheFroIsReady, final String adSourceContent) {

        try {
            TaskManager.getInstance().run_proxy(new Runnable() {
                @Override
                public void run() {
                    AgentInfoBean agentInfoBean = new AgentInfoBean();
                    agentInfoBean.key = "1004632";
                    agentInfoBean.requestId = adTrackingInfo.getmRequestId();
                    agentInfoBean.unitId = adTrackingInfo.getmPlacementId();
//                    agentInfoBean.psid = SDKContext.getInstance().getPsid();
//                    agentInfoBean.sessionId = SDKContext.getInstance().getSessionId(adTrackingInfo.getmPlacementId());
                    agentInfoBean.groupId = String.valueOf(adTrackingInfo.getmGroupId());
                    agentInfoBean.refresh = String.valueOf(adTrackingInfo.getmRefresh());
                    agentInfoBean.trafficGroupId = String.valueOf(adTrackingInfo.getmTrafficGroupId());
                    agentInfoBean.msg = isSuccess ? "1" : "0";
                    agentInfoBean.msg1 = String.valueOf(allReason);
                    agentInfoBean.msg2 = String.valueOf(level);
                    agentInfoBean.msg3 = adSourceId;
                    agentInfoBean.msg4 = String.valueOf(networkType);
                    agentInfoBean.msg5 = networkVersion;
                    agentInfoBean.msg6 = resultArrayJson;
                    agentInfoBean.msg7 = currentRequestId;


                    if (TextUtils.equals(currentRequestId, adTrackingInfo.getmRequestId())) {
                        agentInfoBean.msg8 = "0";
                    } else {
                        agentInfoBean.msg8 = "1";
                    }

                    if (isCacheFroIsReady) {
                        agentInfoBean.msg9 = "1";
                    } else {
                        agentInfoBean.msg9 = "0";
                    }

                    agentInfoBean.msg10 = adSourceContent;

                    PlaceStrategy placeStrategy = PlaceStrategyManager.getInstance(SDKContext.getInstance().getContext()).getPlaceStrategyByAppIdAndPlaceId(adTrackingInfo.getmPlacementId());
                    agentInfoBean.asid = placeStrategy != null ? placeStrategy.getAsid() : "";

                    agentInfoBean.format = adTrackingInfo.getmAdType();

                    handleEventSend(agentInfoBean);
                }
            });
        } catch (Exception e) {
            if (Const.DEBUG) {
                e.printStackTrace();
            }
        }
    }


    public static void rewardedVideoPlayFail(AdTrackingInfo adTrackingInfo, AdError adError) {
        AgentInfoBean agentInfoBean = new AgentInfoBean();
        agentInfoBean.key = "1004636";
        agentInfoBean.requestId = adTrackingInfo.getmRequestId();
        agentInfoBean.unitId = adTrackingInfo.getmPlacementId();
//        agentInfoBean.psid = SDKContext.getInstance().getPsid();
//        agentInfoBean.sessionId = SDKContext.getInstance().getSessionId(adTrackingInfo.getmPlacementId());
        agentInfoBean.groupId = String.valueOf(adTrackingInfo.getmGroupId());
        agentInfoBean.refresh = String.valueOf(adTrackingInfo.getmRefresh());
        agentInfoBean.trafficGroupId = String.valueOf(adTrackingInfo.getmTrafficGroupId());
        agentInfoBean.msg = String.valueOf(adTrackingInfo.getmNetworkType());
        agentInfoBean.msg1 = adTrackingInfo.getmUnitGroupUnitId();
        agentInfoBean.msg2 = String.valueOf(adTrackingInfo.getmLevel());
        agentInfoBean.msg3 = adError.getCode();
        agentInfoBean.msg4 = adError.getPlatformCode();
        agentInfoBean.msg5 = adError.getPlatformMSG();

        PlaceStrategy placeStrategy = PlaceStrategyManager.getInstance(SDKContext.getInstance().getContext()).getPlaceStrategyByAppIdAndPlaceId(adTrackingInfo.getmPlacementId());
        agentInfoBean.asid = placeStrategy != null ? placeStrategy.getAsid() : "";

        agentInfoBean.format = adTrackingInfo.getmAdType();

        handleEventSend(agentInfoBean);
    }


    public static void sendErrorAgent(String type, String errorCode, String errorMsg, String address, String placementId, String failCount) {
        AgentInfoBean agentInfoBean = new AgentInfoBean();
        agentInfoBean.key = "1004616";
        agentInfoBean.unitId = placementId;
        agentInfoBean.msg = type;
        agentInfoBean.msg1 = errorCode;
        agentInfoBean.msg2 = errorMsg;
        agentInfoBean.msg3 = address;
        agentInfoBean.msg4 = failCount;
//        agentInfoBean.psid = psid;
//        agentInfoBean.sessionId = sessionId;
        agentInfoBean.unitId = placementId;

        handleEventSend(agentInfoBean);
    }

    public static void sentHostCallbackTime(String type, String placementId, long requestTime, long responseTime) {
        AgentInfoBean agentInfoBean = new AgentInfoBean();
        agentInfoBean.key = "1004635";
        if (!TextUtils.isEmpty(placementId)) {
            agentInfoBean.unitId = placementId;
        }
        agentInfoBean.msg = type;
        agentInfoBean.msg1 = String.valueOf(requestTime);
        agentInfoBean.msg2 = String.valueOf(responseTime);
        agentInfoBean.msg3 = String.valueOf(responseTime - requestTime);


        handleEventSend(agentInfoBean);

    }

    public static void sdkInitEvent(String unitId, String eventType, String randomString, String initTimestamp) {
        AgentInfoBean agentInfoBean = new AgentInfoBean();
        agentInfoBean.key = "1004637";
        agentInfoBean.unitId = unitId;
//        agentInfoBean.psid = psid;
//        agentInfoBean.sessionId = sessionId;
        agentInfoBean.msg = eventType;
        agentInfoBean.msg1 = randomString;
        agentInfoBean.msg2 = initTimestamp;

        handleEventSend(agentInfoBean);
    }

    public static void myOfferVideoUrlDownloadEvent(String placementId, String offerId, String downloadUrl, String downloadResult, long fileSize, String failMsg, long downloadStartTime, long downloadEndTime) {
        AgentInfoBean agentInfoBean = new AgentInfoBean();
        agentInfoBean.key = "1004638";
        agentInfoBean.unitId = placementId;
        agentInfoBean.msg = offerId;
        agentInfoBean.msg1 = downloadUrl;
        agentInfoBean.msg2 = downloadResult;
        agentInfoBean.msg3 = String.valueOf(fileSize);
        agentInfoBean.msg4 = failMsg;
        agentInfoBean.msg5 = String.valueOf(downloadStartTime);
        agentInfoBean.msg6 = String.valueOf(downloadEndTime);
        agentInfoBean.msg7 = "1".equals(downloadResult) ? String.valueOf(downloadEndTime - downloadStartTime) : null;

        handleEventSend(agentInfoBean);
    }

    public static void adDataFillEvent(AdTrackingInfo adTrackingInfo) {
        try {
            AgentInfoBean agentInfoBean = new AgentInfoBean();
            agentInfoBean.key = "1004640";
            agentInfoBean.requestId = adTrackingInfo.getmRequestId();
            agentInfoBean.groupId = String.valueOf(adTrackingInfo.getmGroupId());
            agentInfoBean.trafficGroupId = String.valueOf(adTrackingInfo.getmTrafficGroupId());
            agentInfoBean.unitId = adTrackingInfo.getmPlacementId();
            agentInfoBean.msg = String.valueOf(adTrackingInfo.getmNetworkType());
            agentInfoBean.msg1 = adTrackingInfo.getmUnitGroupUnitId();
            agentInfoBean.msg2 = String.valueOf(adTrackingInfo.getmLevel());
            agentInfoBean.msg3 = String.valueOf(adTrackingInfo.getDataFillTime());
            agentInfoBean.msg4 = String.valueOf(adTrackingInfo.getFillTime());

            agentInfoBean.format = adTrackingInfo.getmAdType();

            handleEventSend(agentInfoBean);
        } catch (Throwable e) {
            if (Const.DEBUG) {
                e.printStackTrace();
            }
        }
    }

    public static void upStatusAvaibleCache(AdTrackingInfo adTrackingInfo, String originRequestId) {
        try {
            AgentInfoBean agentInfoBean = new AgentInfoBean();
            agentInfoBean.key = "1004639";
            agentInfoBean.requestId = adTrackingInfo.getmRequestId();
            agentInfoBean.unitId = adTrackingInfo.getmPlacementId();
            agentInfoBean.trafficGroupId = String.valueOf(adTrackingInfo.getmTrafficGroupId());
//            agentInfoBean.psid = SDKContext.getInstance().getPsid();
//            agentInfoBean.sessionId = SDKContext.getInstance().getSessionId(adTrackingInfo.getmPlacementId());
            agentInfoBean.groupId = String.valueOf(adTrackingInfo.getmGroupId());

            agentInfoBean.msg = String.valueOf(adTrackingInfo.getmNetworkType());
            agentInfoBean.msg1 = adTrackingInfo.getmUnitGroupUnitId();
            agentInfoBean.msg2 = String.valueOf(adTrackingInfo.getmLevel());
            agentInfoBean.msg3 = originRequestId;

            agentInfoBean.format = adTrackingInfo.getmAdType();

            handleEventSend(agentInfoBean);
        } catch (Throwable e) {
            if (Const.DEBUG) {
                e.printStackTrace();
            }
        }
    }

    public static void appSettingGDPRUpdate(int uploadLevel, int userUploadLevel, int appSettingGDPR_ia) {
        AgentInfoBean agentInfoBean = new AgentInfoBean();
        agentInfoBean.key = "1004641";
        agentInfoBean.msg = String.valueOf(uploadLevel);
        agentInfoBean.msg1 = String.valueOf(userUploadLevel);
        agentInfoBean.msg2 = String.valueOf(appSettingGDPR_ia);

        handleEventSend(agentInfoBean);
    }

    //downloadStatus:
    // 1:start
    // 2:success
    // 3:fail
    // 4:install
    // 5:install success
    public static void onApkDownload(String requestId, String offerId, String downloadURL, int downloadStatus, String failMsd, long downloadTime, long apkSize) {
        AgentInfoBean agentInfoBean = new AgentInfoBean();
        agentInfoBean.key = "1004642";
        agentInfoBean.requestId = requestId;
        agentInfoBean.msg = offerId;
        agentInfoBean.msg1 = downloadURL;
        agentInfoBean.msg2 = String.valueOf(downloadStatus);
        if (downloadStatus == 3) {
            agentInfoBean.msg3 = failMsd;
        } else if (downloadStatus == 2) {
            agentInfoBean.msg4 = String.valueOf(downloadTime);
            agentInfoBean.msg5 = String.valueOf(apkSize / 1024f);
        }

        handleEventSend(agentInfoBean);
    }


    /**
     * @param existScenario 1:Cold launch, exist by home 2:Cold launch, kill app  3:Hot launch, exist by home  4:Hot launch, kill app
     * @param startTime
     * @param endTime
     */
    public static void sendApplicationPlayTime(int existScenario, long startTime, long endTime, String psid) {
        AgentInfoBean agentInfoBean = new AgentInfoBean();
        agentInfoBean.key = "1004644";
        agentInfoBean.psid = psid;
        agentInfoBean.msg = String.valueOf(existScenario);
        agentInfoBean.msg1 = String.valueOf(startTime);
        agentInfoBean.msg2 = String.valueOf(endTime);
        agentInfoBean.msg3 = String.valueOf(endTime - startTime);

        handleEventSend(agentInfoBean);
    }

    public static void onAdImpressionTimeAgent(AdTrackingInfo adTrackingInfo, boolean isReward, long startTime, long endTime) {
        try {
            AgentInfoBean agentInfoBean = new AgentInfoBean();
            agentInfoBean.key = "1004643";
            agentInfoBean.requestId = adTrackingInfo.getmRequestId();
            agentInfoBean.unitId = adTrackingInfo.getmPlacementId();
//            agentInfoBean.psid = SDKContext.getInstance().getPsid();
//            agentInfoBean.sessionId = SDKContext.getInstance().getSessionId(adTrackingInfo.getmPlacementId());
            agentInfoBean.groupId = String.valueOf(adTrackingInfo.getmGroupId());
            agentInfoBean.refresh = String.valueOf(adTrackingInfo.getmRefresh());
            agentInfoBean.trafficGroupId = String.valueOf(adTrackingInfo.getmTrafficGroupId());
            agentInfoBean.msg = adTrackingInfo.getmAdType();
            agentInfoBean.msg1 = String.valueOf(startTime);
            agentInfoBean.msg2 = String.valueOf(endTime);
            agentInfoBean.msg3 = String.valueOf(endTime - startTime);
            agentInfoBean.msg4 = String.valueOf(adTrackingInfo.getmNetworkType());
            agentInfoBean.msg5 = adTrackingInfo.getmUnitGroupUnitId();
            agentInfoBean.msg6 = String.valueOf(adTrackingInfo.getmLevel());
            agentInfoBean.msg7 = String.valueOf(adTrackingInfo.getMyOfferShowType());
            agentInfoBean.msg8 = isReward ? "1" : "0";

            PlaceStrategy placeStrategy = PlaceStrategyManager.getInstance(SDKContext.getInstance().getContext()).getPlaceStrategyByAppIdAndPlaceId(adTrackingInfo.getmPlacementId());
            agentInfoBean.asid = placeStrategy != null ? placeStrategy.getAsid() : "";

            agentInfoBean.format = adTrackingInfo.getmAdType();

            handleEventSend(agentInfoBean);
        } catch (Throwable e) {
            if (Const.DEBUG) {
                e.printStackTrace();
            }
        }
    }


    private static void handleEventSend(final AgentInfoBean agentInfoBean) {
        TaskManager.getInstance().run_proxy(new Runnable() {
            @Override
            public void run() {
                if (TextUtils.isEmpty(agentInfoBean.psid)) {
                    agentInfoBean.psid = SDKContext.getInstance().getPsid();
                }

                if (!TextUtils.isEmpty(agentInfoBean.unitId)) {
                    agentInfoBean.sessionId = SDKContext.getInstance().getSessionId(agentInfoBean.unitId);
                }

                agentInfoBean.timestamp = String.valueOf(System.currentTimeMillis());
                AppStrategy appStrategy = AppStrategyManager.getInstance(SDKContext.getInstance().getContext()).getAppStrategyByAppId(SDKContext.getInstance().getAppId());
                if (appStrategy != null) {

                    Map<String, String> daNotKeyFtMap = appStrategy.getDaNotKeyFtMap();
                    if (daNotKeyFtMap != null) {
                        boolean isValid = false;
                        if (TextUtils.isEmpty(agentInfoBean.format)) {
                            isValid = daNotKeyFtMap.containsKey(agentInfoBean.key);
                        } else {
                            if (daNotKeyFtMap.containsKey(agentInfoBean.key)) {
                                String formatArrays = daNotKeyFtMap.get(agentInfoBean.key);
                                if (!TextUtils.isEmpty(formatArrays) && formatArrays.contains(agentInfoBean.format)) {
                                    isValid = true;
                                }
                            }
                        }

                        if (isValid) {//No send da
                            return;
                        }
                    }

                    Map<String, String> daRtKeyFtMap = appStrategy.getDaRtKeyFtMap();
                    if (daRtKeyFtMap != null) {
                        boolean isValid = false;
                        if (TextUtils.isEmpty(agentInfoBean.format)) {
                            isValid = daRtKeyFtMap.containsKey(agentInfoBean.key);
                        } else {
                            if (daRtKeyFtMap.containsKey(agentInfoBean.key)) {
                                String formatArrays = daRtKeyFtMap.get(agentInfoBean.key);
                                if (!TextUtils.isEmpty(formatArrays) && formatArrays.contains(agentInfoBean.format)) {
                                    isValid = true;
                                }
                            }
                        }

                        if (isValid) {//real time send da
                            AgentInstantManager.getInstance(SDKContext.getInstance().getContext()).addLoggerInfo(agentInfoBean);
                            return;
                        }
                    }
                }

                Agent.onEvent(agentInfoBean);
            }
        });
    }

}
