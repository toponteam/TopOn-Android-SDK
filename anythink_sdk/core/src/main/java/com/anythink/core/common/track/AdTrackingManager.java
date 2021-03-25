/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.common.track;

import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.common.InstantUpLoadManager;
import com.anythink.core.common.MonitoringPlatformManager;
import com.anythink.core.common.MsgManager;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.entity.AdTrackingInfo;
import com.anythink.core.common.entity.AdTrackingLogBean;
import com.anythink.core.common.entity.TrackerInfo;
import com.anythink.core.common.net.TrackingV2Loader;
import com.anythink.core.common.net.socket.TrackingSocketData;
import com.anythink.core.common.utils.task.TaskManager;
import com.anythink.core.strategy.AppStrategy;
import com.anythink.core.strategy.AppStrategyManager;

import org.json.JSONArray;

import java.util.List;
import java.util.Map;

public class AdTrackingManager extends InstantUpLoadManager<AdTrackingLogBean> {

    private static AdTrackingManager sIntance;


    private AdTrackingManager(Context context) {
        super(context);
    }

    public synchronized static AdTrackingManager getInstance(Context context) {
        if (sIntance == null) {
            sIntance = new AdTrackingManager(context);
        }
        return sIntance;
    }

    public synchronized void addAdTrackingInfo(int businessType, TrackerInfo adTrackingInfo) {
        this.addAdTrackingInfo(businessType, adTrackingInfo, -1);
    }

    public synchronized void addAdTrackingInfo(final int businessType, final TrackerInfo adTrackingInfo, final long timeStamp) {

        TaskManager.getInstance().run_proxy(new Runnable() {
            @Override
            public void run() {
                //Get tracking type in AppSetting
                AppStrategy appStrategy = AppStrategyManager.getInstance(SDKContext.getInstance().getContext())
                        .getAppStrategyByAppId(SDKContext.getInstance().getAppId());

                AdTrackingLogBean logBean = new AdTrackingLogBean();
                logBean.businessType = businessType;
                logBean.adTrackingInfo = adTrackingInfo;
                logBean.time = timeStamp > 0 ? timeStamp : System.currentTimeMillis();

                MsgManager.getInstance(SDKContext.getInstance().getContext()).handleTK(businessType, logBean, appStrategy);

                //check network firm id
                String tkNoTrackingNetworkFirmId = appStrategy.getTkNoTrackingNetworkFirmId();
                if (!TextUtils.isEmpty(tkNoTrackingNetworkFirmId)) {
                    if (adTrackingInfo instanceof AdTrackingInfo) {
                        try {
                            JSONArray jsonArray = new JSONArray(tkNoTrackingNetworkFirmId);
                            int length = jsonArray.length();
                            String networkFirmId = String.valueOf(((AdTrackingInfo) adTrackingInfo).getmNetworkType());
                            for (int i = 0; i < length; i++) {
                                if (TextUtils.equals(networkFirmId, jsonArray.optString(i))) {
                                    //do not upload tracking
                                    return;
                                }
                            }
                        } catch (Throwable e) {

                        }
                    }
                }

                //check format
                Map<String, String> tkNoTFtMap = appStrategy.getTkNoTFtMap();
                if (tkNoTFtMap != null && tkNoTFtMap.containsKey(String.valueOf(businessType))) {
                    String formatArrays = tkNoTFtMap.get(String.valueOf(businessType));

                    if (!TextUtils.isEmpty(formatArrays) && formatArrays.contains(adTrackingInfo.getmAdType())) {
                        //do not upload tracking
                        return;
                    }
                }

                // report impression revenue
                if (TrackingV2Loader.AD_SHOW_TYPE == businessType && adTrackingInfo instanceof AdTrackingInfo) {
                    MonitoringPlatformManager.getInstance().reportImpressionRevenue((AdTrackingInfo) adTrackingInfo);
                }


                if (Const.DEBUG) {
                    SDKContext.getInstance().printJson("AnyThinkTracking", logBean.toJSONObject().toString());
                }

                AdTrackingManager.super.addLoggerInfo(logBean);
            }
        });

    }


    @Override
    protected void sendLoggerToServer(List<AdTrackingLogBean> sendInfo) {
        AppStrategy appStrategy = AppStrategyManager.getInstance(SDKContext.getInstance().getContext()).getAppStrategyByAppId(SDKContext.getInstance().getAppId());
        if (appStrategy != null) {
            switch (appStrategy.getTcpSwitchType()) {
                case 1: //Only TCP
                    TrackingSocketData trackingSocketData = new TrackingSocketData(sendInfo);
                    trackingSocketData.setTcpInfo(1, appStrategy.getTcpRate());
                    trackingSocketData.startToUpload(null);
                    break;
                case 2: //HTTP(s) & TCP
                    new TrackingV2Loader(mApplicationContext, appStrategy.getTcpSwitchType(), sendInfo).start(0, null);

                    TrackingSocketData trackingSocketData2 = new TrackingSocketData(sendInfo);
                    trackingSocketData2.setTcpInfo(2, appStrategy.getTcpRate());
                    trackingSocketData2.startToUpload(null);
                    break;
                default: //HTTP(s)
                    new TrackingV2Loader(mApplicationContext, appStrategy.getTcpSwitchType(), sendInfo).start(0, null);
                    break;
            }
        } else {
            new TrackingV2Loader(mApplicationContext, 0, sendInfo).start(0, null);
        }


    }

//    public void sendClickLoggerToServerDelay(final AdTrackingLogBean sendInfo, int delay) {
//        SDKContext.getInstance().runOnMainThreadDelayed(new Runnable() {
//            @Override
//            public void run() {
//                new TrackingV2Loader(mApplicationContext, sendInfo).start(0, null);
//            }
//        }, delay);
//    }


}
