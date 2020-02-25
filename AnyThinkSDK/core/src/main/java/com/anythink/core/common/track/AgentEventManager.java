package com.anythink.core.common.track;

import android.text.TextUtils;

import com.anythink.core.api.AdError;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.entity.AgentInfoBean;
import com.anythink.core.common.utils.task.TaskManager;
import com.anythink.core.strategy.AppStrategy;
import com.anythink.core.strategy.AppStrategyManager;
import com.anythink.core.strategy.PlaceStrategy;
import com.anythink.core.strategy.PlaceStrategyManager;

public class AgentEventManager {

    public static final int NO_READY_ADSOURCE_REASON = 1;
    public static final int PLACEMENT_CAPPING_REASON = 2;
    public static final int PLACEMENT_PACCING_REASON = 3;
    public static final int PLACEMENT_STRATEGY_NULL_REASON = 4;


    public static void onAgentForATToAppLoadFail(String requestId, String placementId, String psid, String sessionId, int groupId, int refresh, String allReason) {
        AgentInfoBean agentInfoBean = new AgentInfoBean();
        agentInfoBean.key = "1004630";
        agentInfoBean.requestId = requestId;
        agentInfoBean.unitId = placementId;
        agentInfoBean.psid = psid;
        agentInfoBean.sessionId = sessionId;
        agentInfoBean.groupId = String.valueOf(groupId);
        agentInfoBean.refresh = String.valueOf(refresh);

        PlaceStrategy placeStrategy = PlaceStrategyManager.getInstance(SDKContext.getInstance().getContext()).getPlaceStrategyByAppIdAndPlaceId(placementId);
        agentInfoBean.asid = placeStrategy != null ? placeStrategy.getAsid() : "";
        agentInfoBean.msg = allReason;

        handleEventSend(agentInfoBean);
    }

    public static void onAdsourceLoadFail(String requestId, String placementId, String psid, String sessionId, int groupId, int refresh, int networkType, String adSourceId, int level, int reason, AdError adError, int bidType, double bidPrice, long failTime) {

        AgentInfoBean agentInfoBean = new AgentInfoBean();
        agentInfoBean.key = "1004631";
        agentInfoBean.requestId = requestId;
        agentInfoBean.unitId = placementId;
        agentInfoBean.psid = psid;
        agentInfoBean.sessionId = sessionId;
        agentInfoBean.groupId = String.valueOf(groupId);
        agentInfoBean.refresh = String.valueOf(refresh);
        agentInfoBean.msg = String.valueOf(networkType);
        agentInfoBean.msg1 = adSourceId;
        agentInfoBean.msg2 = String.valueOf(level);
        agentInfoBean.msg3 = String.valueOf(reason);
        agentInfoBean.msg4 = adError != null ? adError.getPlatformCode() : "";
        agentInfoBean.msg5 = adError != null ? adError.getPlatformMSG() : "";
        agentInfoBean.msg6 = String.valueOf(bidType);
        agentInfoBean.msg7 = String.valueOf(bidPrice);
        if(reason == 0) {
            agentInfoBean.msg8 = String.valueOf(failTime);
        }

        PlaceStrategy placeStrategy = PlaceStrategyManager.getInstance(SDKContext.getInstance().getContext()).getPlaceStrategyByAppIdAndPlaceId(placementId);
        agentInfoBean.asid = placeStrategy != null ? placeStrategy.getAsid() : "";

        handleEventSend(agentInfoBean);
    }

    public static void onAdShowFail(String requestId, String placementId, String psid, String sessionId, int groupId, int refresh
            , int allReason, String resultArrayJson, String currentRequestId) {

        AgentInfoBean agentInfoBean = new AgentInfoBean();
        agentInfoBean.key = "1004633";
        agentInfoBean.requestId = requestId;
        agentInfoBean.unitId = placementId;
        agentInfoBean.psid = psid;
        agentInfoBean.sessionId = sessionId;
        agentInfoBean.groupId = String.valueOf(groupId);
        agentInfoBean.refresh = String.valueOf(refresh);
        agentInfoBean.msg = String.valueOf(allReason);
        agentInfoBean.msg1 = resultArrayJson;

        agentInfoBean.msg4 = currentRequestId;

        if (TextUtils.equals(currentRequestId, requestId)) {
            agentInfoBean.msg5 = "0";
        } else {
            agentInfoBean.msg5 = "1";
        }

        PlaceStrategy placeStrategy = PlaceStrategyManager.getInstance(SDKContext.getInstance().getContext()).getPlaceStrategyByAppIdAndPlaceId(placementId);
        agentInfoBean.asid = placeStrategy != null ? placeStrategy.getAsid() : "";

        handleEventSend(agentInfoBean);
    }

    public static void onAdCloseAgent(String requestId, String placementId, String psid, String sessionId, int groupId, int refresh
            , int networkType, String adSourceId, int level, boolean isReward, int offerType) {
        AgentInfoBean agentInfoBean = new AgentInfoBean();
        agentInfoBean.key = "1004634";
        agentInfoBean.requestId = requestId;
        agentInfoBean.unitId = placementId;
        agentInfoBean.psid = psid;
        agentInfoBean.sessionId = sessionId;
        agentInfoBean.groupId = String.valueOf(groupId);
        agentInfoBean.refresh = String.valueOf(refresh);
        agentInfoBean.msg = String.valueOf(networkType);
        agentInfoBean.msg1 = adSourceId;
        agentInfoBean.msg2 = String.valueOf(level);
        agentInfoBean.msg3 = isReward ? "1" : "0";
        agentInfoBean.msg4 = String.valueOf(offerType);

        PlaceStrategy placeStrategy = PlaceStrategyManager.getInstance(SDKContext.getInstance().getContext()).getPlaceStrategyByAppIdAndPlaceId(placementId);
        agentInfoBean.asid = placeStrategy != null ? placeStrategy.getAsid() : "";

        handleEventSend(agentInfoBean);
    }


    public static void isReadyEventAgentForATToApp(final String requestId, final String placementId, final String psid, final String sessionId, final int groupId, final int refresh
            , final boolean isSuccess, final int allReason, final int level, final String adSourceId, final int networkType, final String networkVersion, final String resultArrayJson, final String currentRequestId, final boolean isCacheFroIsReady, final String adSourceContent) {

        TaskManager.getInstance().run_proxy(new Runnable() {
            @Override
            public void run() {
                AgentInfoBean agentInfoBean = new AgentInfoBean();
                agentInfoBean.key = "1004632";
                agentInfoBean.requestId = requestId;
                agentInfoBean.unitId = placementId;
                agentInfoBean.psid = psid;
                agentInfoBean.sessionId = sessionId;
                agentInfoBean.groupId = String.valueOf(groupId);
                agentInfoBean.refresh = String.valueOf(refresh);
                agentInfoBean.msg = isSuccess ? "1" : "0";
                agentInfoBean.msg1 = String.valueOf(allReason);
                agentInfoBean.msg2 = String.valueOf(level);
                agentInfoBean.msg3 = adSourceId;
                agentInfoBean.msg4 = String.valueOf(networkType);
                agentInfoBean.msg5 = networkVersion;
                agentInfoBean.msg6 = resultArrayJson;
                agentInfoBean.msg7 = currentRequestId;


                if (TextUtils.equals(currentRequestId, requestId)) {
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

                PlaceStrategy placeStrategy = PlaceStrategyManager.getInstance(SDKContext.getInstance().getContext()).getPlaceStrategyByAppIdAndPlaceId(placementId);
                agentInfoBean.asid = placeStrategy != null ? placeStrategy.getAsid() : "";

                handleEventSend(agentInfoBean);
            }
        });

    }

    public static void rewardedVideoPlayFail(String requestId, String placementId, String psid, String sessionId, int groupId, int refresh
            , int networtType, String adSourceId, int level, String sdkErrorCode, String networkErrorCode, String networkErrorMsg) {
        AgentInfoBean agentInfoBean = new AgentInfoBean();
        agentInfoBean.key = "1004636";
        agentInfoBean.requestId = requestId;
        agentInfoBean.unitId = placementId;
        agentInfoBean.psid = psid;
        agentInfoBean.sessionId = sessionId;
        agentInfoBean.groupId = String.valueOf(groupId);
        agentInfoBean.refresh = String.valueOf(refresh);
        agentInfoBean.msg = String.valueOf(networtType);
        agentInfoBean.msg1 = adSourceId;
        agentInfoBean.msg2 = String.valueOf(level);
        agentInfoBean.msg3 = sdkErrorCode;
        agentInfoBean.msg4 = networkErrorCode;
        agentInfoBean.msg5 = networkErrorMsg;

        PlaceStrategy placeStrategy = PlaceStrategyManager.getInstance(SDKContext.getInstance().getContext()).getPlaceStrategyByAppIdAndPlaceId(placementId);
        agentInfoBean.asid = placeStrategy != null ? placeStrategy.getAsid() : "";

        handleEventSend(agentInfoBean);
    }


    public static void sendErrorAgent(String type, String errorCode, String errorMsg, String address, String psid, String sessionId, String placementId, String failCount) {
        AgentInfoBean agentInfoBean = new AgentInfoBean();
        agentInfoBean.key = "1004616";
        agentInfoBean.msg = type;
        agentInfoBean.msg1 = errorCode;
        agentInfoBean.msg2 = errorMsg;
        agentInfoBean.msg3 = address;
        agentInfoBean.msg4 = failCount;
        agentInfoBean.psid = psid;
        agentInfoBean.sessionId = sessionId;
        agentInfoBean.unitId = placementId;

        handleEventSend(agentInfoBean);
    }

    public static void sentHostCallbackTime(String type, long requestTime, long responseTime) {
        AgentInfoBean agentInfoBean = new AgentInfoBean();
        agentInfoBean.key = "1004635";
        agentInfoBean.msg = type;
        agentInfoBean.msg1 = String.valueOf(requestTime);
        agentInfoBean.msg2 = String.valueOf(responseTime);
        agentInfoBean.msg3 = String.valueOf(responseTime - requestTime);


        handleEventSend(agentInfoBean);

    }

    public static void sdkInitEvent(String unitId, String psid, String sessionId, String eventType, String randomString, String initTimestamp) {
        AgentInfoBean agentInfoBean = new AgentInfoBean();
        agentInfoBean.key = "1004637";
        agentInfoBean.unitId = unitId;
        agentInfoBean.psid = psid;
        agentInfoBean.sessionId = sessionId;
        agentInfoBean.msg = eventType;
        agentInfoBean.msg1 = randomString;
        agentInfoBean.msg2 = initTimestamp;

        handleEventSend(agentInfoBean);
    }

    public static void myOfferVideoUrlDownloadEvent(String offerId, String downloadUrl, String downloadResult, long fileSize, String failMsg, long downloadStartTime, long downloadEndTime) {
        AgentInfoBean agentInfoBean = new AgentInfoBean();
        agentInfoBean.key = "1004638";
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

    public static void adDataFillEvent(String requestId, int networkType, String adSourceId, int level, long adDataLoadedTime, long adLoadedTime) {
        AgentInfoBean agentInfoBean = new AgentInfoBean();
        agentInfoBean.key = "1004640";
        agentInfoBean.requestId = requestId;
        agentInfoBean.msg = String.valueOf(networkType);
        agentInfoBean.msg1 = adSourceId;
        agentInfoBean.msg2 = String.valueOf(level);
        agentInfoBean.msg3 = String.valueOf(adDataLoadedTime);
        agentInfoBean.msg4 = String.valueOf(adLoadedTime);

        handleEventSend(agentInfoBean);
    }

    public static void upStatusAvaibleCache(String requestId, String placementId, String psid, String sessionId, int groupId, int networkFirmId, String adsourceId, int level, String originRequestId) {
        AgentInfoBean agentInfoBean = new AgentInfoBean();
        agentInfoBean.key = "1004639";
        agentInfoBean.requestId = requestId;
        agentInfoBean.unitId = placementId;
        agentInfoBean.psid = psid;
        agentInfoBean.sessionId = sessionId;
        agentInfoBean.groupId = String.valueOf(groupId);

        agentInfoBean.msg = String.valueOf(networkFirmId);
        agentInfoBean.msg1 = adsourceId;
        agentInfoBean.msg2 = String.valueOf(level);
        agentInfoBean.msg3 = originRequestId;

        handleEventSend(agentInfoBean);
    }

    public static void appSettingGDPRUpdate(int uploadLevel, int userUploadLevel, int appSettingGDPR_ia) {
        AgentInfoBean agentInfoBean = new AgentInfoBean();
        agentInfoBean.key = "1004641";
        agentInfoBean.msg = String.valueOf(uploadLevel);
        agentInfoBean.msg1 = String.valueOf(userUploadLevel);
        agentInfoBean.msg2 = String.valueOf(appSettingGDPR_ia);

        handleEventSend(agentInfoBean);
    }


    private static void handleEventSend(final AgentInfoBean agentInfoBean) {
        TaskManager.getInstance().run_proxy(new Runnable() {
            @Override
            public void run() {
                agentInfoBean.timestamp = String.valueOf(System.currentTimeMillis());
                AppStrategy appStrategy = AppStrategyManager.getInstance(SDKContext.getInstance().getContext()).getAppStrategyByAppId(SDKContext.getInstance().getAppId());
                if (appStrategy != null && !TextUtils.isEmpty(appStrategy.getDaNotKeys()) && appStrategy.getDaNotKeys().contains(agentInfoBean.key)) {
                    return;
                }

                if (appStrategy != null && !TextUtils.isEmpty(appStrategy.getDaRealTimeKeys()) && appStrategy.getDaRealTimeKeys().contains(agentInfoBean.key)) {
                    AgentInstantManager.getInstance(SDKContext.getInstance().getContext()).addLoggerInfo(agentInfoBean);
                    return;
                }

                Agent.onEvent(agentInfoBean);
            }
        });
    }

}
