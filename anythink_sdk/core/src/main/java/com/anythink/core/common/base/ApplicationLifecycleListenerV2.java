/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.common.base;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;

import com.anythink.core.common.track.AgentEventManager;
import com.anythink.core.common.utils.CommonLogUtil;
import com.anythink.core.common.utils.CommonSDKUtil;
import com.anythink.core.common.utils.SPUtil;
import com.anythink.core.strategy.AppStrategy;
import com.anythink.core.strategy.AppStrategyManager;

import org.json.JSONObject;

public class ApplicationLifecycleListenerV2 implements Application.ActivityLifecycleCallbacks {
    /**
     * Use for recording application leave time
     **/
    public final static String SPU_RECORD_NAME = "APP_LAUNCHER_INFO";

    public final static String JSON_START_TIME_KEY = "start_time";
    public final static String JSON_END_TIME_KEY = "end_time";
    public final static String JSON_PSID_KEY = "psid";
    public final static String JSON_LAUNCHER_ID_KEY = "launcher_id";
    public final static String JSON_LAUNCH_MODE_KEY = "launch_mode";


    private final String TAG = ApplicationLifecycleListenerV2.class.getName();
    public static final int COLD_LAUNCH_MODE = 0;
    public static final int HOT_LAUNCH_MODE = 1;

    Context mApplicationContext;
    long startTime;
    int launchMode;

    JSONObject recordObject;

    Handler handler = new Handler(Looper.getMainLooper());

    String launcherId;

    Runnable recordPlayTimeRunnable = new Runnable() {
        @Override
        public void run() {
            if (recordObject != null) {
                /**clean playtime's record**/
                SPUtil.putString(SDKContext.getInstance().getContext(), Const.SPU_NAME, SPU_RECORD_NAME, "");
                startTime = 0; //reset application start time
                JSONObject jsonObject = recordObject;
                long startTime = jsonObject.optLong(JSON_START_TIME_KEY);
                long endTime = jsonObject.optLong(JSON_END_TIME_KEY);
                String psid = jsonObject.optString(JSON_PSID_KEY);
                int launchMode = jsonObject.optInt(JSON_LAUNCH_MODE_KEY);
                String launcherId = jsonObject.optString(JSON_LAUNCHER_ID_KEY);

                recordObject = null; //set null
                //Send playtime agent
                AgentEventManager.sendApplicationPlayTimeV2(launchMode == HOT_LAUNCH_MODE ? 3 : 1, startTime, endTime, psid, launcherId);
                CommonLogUtil.e(TAG, "Time up to send application playTime, reset playStartTime and send agent, playtime:" + (endTime - startTime) / 1000);
            } else {
                CommonLogUtil.e(TAG, "Time up to send application playTime, but recordObject is null.");
            }
        }
    };

    public ApplicationLifecycleListenerV2(Context context, long realStartTime, int launchMode, String launcherId) {
        this.launchMode = launchMode;
        if (realStartTime != 0) {
            startTime = realStartTime;
        } else {
            startTime = SystemClock.elapsedRealtime();
        }

        if (!TextUtils.isEmpty(launcherId)) {
            this.launcherId = launcherId;
        } else {
            this.launcherId = CommonSDKUtil.createLaunchId(context);
        }

        this.mApplicationContext = context.getApplicationContext();
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        long methodStatTime = SystemClock.elapsedRealtime();

        handler.removeCallbacks(recordPlayTimeRunnable);

        AppStrategy appStrategy = AppStrategyManager.getInstance(activity.getApplicationContext()).getAppStrategyByAppId(SDKContext.getInstance().getAppId());
        if (recordObject != null) {
            CommonLogUtil.e(TAG, "onActivityResumed : Time countdown is closed, check the time is up?");
            JSONObject jsonObject = recordObject;
            long recordStartTime = jsonObject.optLong(JSON_START_TIME_KEY);
            long endTime = jsonObject.optLong(JSON_END_TIME_KEY);
            String psid = jsonObject.optString(JSON_PSID_KEY);
            int launchMode = jsonObject.optInt(JSON_LAUNCH_MODE_KEY);
            String launcherId = jsonObject.optString(JSON_LAUNCHER_ID_KEY);

            if (SystemClock.elapsedRealtime() - endTime > appStrategy.getRecreatePsidIntervalWhenHotBoot() || SystemClock.elapsedRealtime() - endTime < 0) {
                CommonLogUtil.e(TAG, "onActivityResumed : Time countdown is closed, time up to send agent and create new psid, playtime:" + (endTime - recordStartTime) / 1000);
                /**clean playtime's record**/
                SPUtil.putString(SDKContext.getInstance().getContext(), Const.SPU_NAME, SPU_RECORD_NAME, "");
                //Send playtime agent
                AgentEventManager.sendApplicationPlayTimeV2(launchMode == HOT_LAUNCH_MODE ? 3 : 1, recordStartTime, endTime, psid, launcherId);
                this.startTime = 0;
            } else {
                this.launcherId = launcherId;
                CommonLogUtil.e(TAG, "onActivityResumed : Time countdown is closed, continue to record pervious start time");
            }
        } else {
            CommonLogUtil.e(TAG, "onActivityResumed : Time countdown is opened or doesn't start countdown");
        }
        recordObject = null;

        if (startTime == 0) {
            launchMode = HOT_LAUNCH_MODE;
            CommonLogUtil.e(TAG, "onActivityResumed : restart to record starttime");
            startTime = SystemClock.elapsedRealtime();
            launcherId = CommonSDKUtil.createLaunchId(mApplicationContext);
        }
//
//        else {
//            //clear play record
//            String appid = SDKContext.getInstance().getAppId();
//            SPUtil.putString(activity.getApplicationContext(), Const.SPU_NAME, appid + "playRecord", "");
//            CommonLogUtil.e(TAG, "onActivityResumed : Continue to record the pervious start time");
//        }

        CommonLogUtil.e(TAG, "onActivityResumed: Method use time:" + (SystemClock.elapsedRealtime() - methodStatTime));
    }

    @Override
    public void onActivityPaused(final Activity activity) {
        long methodStatTime = SystemClock.elapsedRealtime();

        final String appid = SDKContext.getInstance().getAppId();
        try {
            JSONObject recordObject = new JSONObject();
            recordObject.put(JSON_PSID_KEY, SDKContext.getInstance().getPsid());
            recordObject.put(JSON_START_TIME_KEY, startTime);
            recordObject.put(JSON_END_TIME_KEY, SystemClock.elapsedRealtime());
            recordObject.put(JSON_LAUNCH_MODE_KEY, launchMode);
            recordObject.put(JSON_LAUNCHER_ID_KEY, launcherId);
            this.recordObject = recordObject;
            SPUtil.putString(activity.getApplicationContext(), Const.SPU_NAME, SPU_RECORD_NAME, recordObject.toString());
            CommonLogUtil.e(TAG, "onActivityPaused: record leave time:" + recordObject.toString());
        } catch (Exception e) {
            if (Const.DEBUG) {
                e.printStackTrace();
            }
        }

        AppStrategy appStrategy = AppStrategyManager.getInstance(activity.getApplicationContext()).getAppStrategyByAppId(appid);
        if (appStrategy.getUseCountDownSwitchAfterLeaveApp() == 1) {
            handler.postDelayed(recordPlayTimeRunnable, appStrategy.getRecreatePsidIntervalWhenHotBoot());
            CommonLogUtil.e(TAG, "onActivityPaused : Start to leave application countdown.");
        }

        CommonLogUtil.e(TAG, "onActivityPaused: Method use time:" + (SystemClock.elapsedRealtime() - methodStatTime));
    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}
