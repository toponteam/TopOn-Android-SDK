package com.anythink.core.common.track;

import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.common.InstantUpLoadManager;
import com.anythink.core.common.MsgManager;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.entity.AdTrackingLogBean;
import com.anythink.core.common.entity.TrackerInfo;
import com.anythink.core.common.net.TrackingV2Loader;
import com.anythink.core.strategy.AppStrategy;
import com.anythink.core.strategy.AppStrategyManager;

import java.util.List;
import java.util.Random;

public class AdTrackingManager extends InstantUpLoadManager<AdTrackingLogBean> {

    private static AdTrackingManager sIntance;


    private AdTrackingManager(Context context) {
        super(context);
    }

    public static AdTrackingManager getInstance(Context context) {
        if (sIntance == null) {
            sIntance = new AdTrackingManager(context);
        }
        return sIntance;
    }

    public synchronized void addAdTrackingInfo(int businessType, TrackerInfo adTrackingInfo) {

        //Get tracking type in AppSetting
        AppStrategy appStrategy = AppStrategyManager.getInstance(SDKContext.getInstance().getContext())
                .getAppStrategyByAppId(SDKContext.getInstance().getAppId());

        int[] tkNoTrackingType = appStrategy.getTkNoTrackingType();
        if (tkNoTrackingType != null) {// No Tracking Type
            for (int no_tk_type : tkNoTrackingType) {
                if (no_tk_type == businessType) {
                    //不上报
                    return;
                }
            }
        }

        AdTrackingLogBean logBean = new AdTrackingLogBean();
        logBean.businessType = businessType;
        logBean.adTrackingInfo = adTrackingInfo;
        logBean.time = System.currentTimeMillis();

        if (Const.DEBUG) {
            SDKContext.getInstance().printJson("AnyThinkTracking", logBean.toJSONObject().toString());
        }

        if (TrackingV2Loader.AD_CLICK_TYPE == businessType) {
            int clickTkDelayMinTime = logBean.adTrackingInfo.getmClickTkDelayMinTime();
            int clickTkDelayMaxTime = logBean.adTrackingInfo.getmClickTkDelayMaxTime();
            if (clickTkDelayMinTime == -1 && clickTkDelayMaxTime == -1) {
                return;
            }
            int randomDelayTime;
            if (!TextUtils.isEmpty(logBean.adTrackingInfo.getmClickTkUrl())) {
                if (clickTkDelayMinTime == 0 && clickTkDelayMaxTime == 0) {
                    randomDelayTime = 0;
                } else if (clickTkDelayMinTime == clickTkDelayMaxTime) {
                    randomDelayTime = clickTkDelayMinTime;
                } else {
                    randomDelayTime = new Random().nextInt(clickTkDelayMaxTime - clickTkDelayMinTime) + clickTkDelayMinTime;
                }
                sendClickLoggerToServerDelay(logBean, randomDelayTime);
            }
        } else {
            super.addLoggerInfo(logBean);
        }

        MsgManager.getInstance(SDKContext.getInstance().getContext()).handleTK(businessType, logBean, appStrategy);
    }


    @Override
    protected void sendLoggerToServer(List<AdTrackingLogBean> sendInfo) {
        new TrackingV2Loader(mApplicationContext, sendInfo).start(0, null);
    }

    public void sendClickLoggerToServerDelay(final AdTrackingLogBean sendInfo, int delay) {
        SDKContext.getInstance().runOnMainThreadDelayed(new Runnable() {
            @Override
            public void run() {
                new TrackingV2Loader(mApplicationContext, sendInfo).start(0, null);
            }
        }, delay);
    }


}
