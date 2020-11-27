/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.common.base;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.anythink.BuildConfig;
import com.anythink.core.api.ATCustomRuleKeys;
import com.anythink.core.api.ATInitMediation;
import com.anythink.core.api.AdError;
import com.anythink.core.api.ErrorCode;
import com.anythink.core.api.IATChinaSDKHandler;
import com.anythink.core.cap.AdCapV2Manager;
import com.anythink.core.common.MsgManager;
import com.anythink.core.common.OffLineTkManager;
import com.anythink.core.common.net.OnHttpLoaderListener;
import com.anythink.core.common.net.PlaceStrategyLoader;
import com.anythink.core.common.track.Agent;
import com.anythink.core.common.track.AgentEventManager;
import com.anythink.core.common.utils.CommonDeviceUtil;
import com.anythink.core.common.utils.CommonLogUtil;
import com.anythink.core.common.utils.CommonMD5;
import com.anythink.core.common.utils.CommonUtil;
import com.anythink.core.common.utils.SPUtil;
import com.anythink.core.common.utils.task.TaskManager;
import com.anythink.core.strategy.AppStrategy;
import com.anythink.core.strategy.AppStrategyManager;
import com.anythink.core.strategy.PlaceStrategy;
import com.anythink.core.strategy.PlaceStrategyManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import dalvik.system.DexFile;

/**
 * Created by Z on 2017-1-20.
 */

public class SDKContext {
    private final String TAG = "SDK.init";
    private final String CHINA_HANDLER_CLASS = BuildConfig.CHINA_PLUGIN_NAME;
    private boolean isCheckChinaPlugin = false;

    private static SDKContext instance;

    private Context mContext;
    private String mAppId;
    private String mAppKey;
    private Handler mHandler;

    private ConcurrentHashMap<String, Object> mCustomMap;
    private ConcurrentHashMap<String, Map<String, Object>> mPlacementCustomMap;

    private Map<String, Boolean> mDeniedDeviceInfoUpLoadMap;

    private String mPsid;
    private JSONObject mSessionIdObject;

    private BroadcastReceiver mNetworkChangeReceiver;


    private String mUpId;
    private String mSysId;
    private String mBkId;

    private IATChinaSDKHandler mChinaHandler;
    private final String mLogPath;
    private boolean NETWORK_LOG_FILE_EXIST = false;
    private boolean DEVELOPER_NETWORK_LOG_DEBUG = false;

    private long firstInitTime = 0;
    private long initDays = 0;

    private List<String> mPackageList;

    public synchronized static SDKContext getInstance() {
        if (instance == null) {
            synchronized (SDKContext.class) {
                instance = new SDKContext();
            }
        }
        return instance;
    }

    public synchronized IATChinaSDKHandler getChinaHandler() {
        if (isCheckChinaPlugin) {
            return mChinaHandler;
        }

        try {
            Class<? extends IATChinaSDKHandler> chinaHandlerClass = Class.forName(CHINA_HANDLER_CLASS)
                    .asSubclass(IATChinaSDKHandler.class);
            final Constructor<?> chinaHandlerConstructor = chinaHandlerClass.getDeclaredConstructor((Class[]) null);
            chinaHandlerConstructor.setAccessible(true);
            mChinaHandler = (IATChinaSDKHandler) chinaHandlerConstructor.newInstance();
        } catch (Exception e) {

        }

        isCheckChinaPlugin = true;
        return mChinaHandler;
    }

    private SDKContext() {
        mHandler = new Handler(Looper.getMainLooper());
        mPlacementCustomMap = new ConcurrentHashMap<>();
        mCustomMap = new ConcurrentHashMap<>();

        mLogPath = File.separator + "anythink.test";
    }

    public synchronized void deniedUploadDeviceInfo(String... deviceInfos) {
        if (deviceInfos != null) {
            mDeniedDeviceInfoUpLoadMap = new HashMap<>();
            for (String deviceKey : deviceInfos) {
                mDeniedDeviceInfoUpLoadMap.put(deviceKey, true);
            }
        } else {
            mDeniedDeviceInfoUpLoadMap = null;
        }
    }

    public synchronized boolean containDeniedDeviceKey(String deviceKey) {
        if (mDeniedDeviceInfoUpLoadMap == null) {
            return false;
        }

        return mDeniedDeviceInfoUpLoadMap.containsKey(deviceKey);
    }

    public void setContext(Context context) {
        mContext = context;
    }

    public Context getContext() {
        return mContext;
    }

    public long getFirstInitTime() {
        return firstInitTime;
    }

    public long getInitDays() {
        return initDays;
    }


    public void setAppCustomMap(Map<String, Object> customMap) {
        mCustomMap.clear();
        if (customMap != null) {
            mCustomMap.putAll(customMap);
        }
    }

    public void setPlacementCustomMap(String placementId, Map<String, Object> customMap) {
        mPlacementCustomMap.put(placementId, customMap);
    }

    public void setExcludeMyOfferPkgList(List<String> packageList) {
        mPackageList = packageList;
    }

    public List<String> getExcludeMyOfferPkgList() {
        return mPackageList;
    }

    public Map<String, Object> getCustomMap() {
        return mCustomMap;
    }

    public Map<String, Object> getCustomMap(String placmentId) {
        Map<String, Object> customMap = new HashMap<>();
        Map<String, Object> placementMap = mPlacementCustomMap.get(placmentId);
        if (mCustomMap != null) {
            customMap.putAll(mCustomMap);
        }
        if (placementMap != null) {
            customMap.putAll(placementMap);
        }

        //Reset channel and subchannel by custom rule of app
        customMap.remove(ATCustomRuleKeys.CHANNEL);
        customMap.remove(ATCustomRuleKeys.SUB_CHANNEL);

        Object channelObject = mCustomMap.get(ATCustomRuleKeys.CHANNEL);
        Object subChannelObject = mCustomMap.get(ATCustomRuleKeys.SUB_CHANNEL);

        if (channelObject != null) {
            customMap.put(ATCustomRuleKeys.CHANNEL, channelObject);
        }

        if (subChannelObject != null) {
            customMap.put(ATCustomRuleKeys.SUB_CHANNEL, subChannelObject);
        }

        return customMap;
    }

    public String getChannel() {
        Object channelObject = mCustomMap.get(ATCustomRuleKeys.CHANNEL);
        return channelObject != null ? channelObject.toString() : "";
    }

    public void setChannel(String channel) {
        mCustomMap.put(ATCustomRuleKeys.CHANNEL, channel);
    }

    public String getSubChannel() {
        Object subChannelObject = mCustomMap.get(ATCustomRuleKeys.SUB_CHANNEL);
        return subChannelObject != null ? subChannelObject.toString() : "";
    }

    public void setSubChannel(String subChannel) {
        mCustomMap.put(ATCustomRuleKeys.SUB_CHANNEL, subChannel);
    }

    public String getAppId() {
        if (TextUtils.isEmpty(mAppId)) {
            mAppId = SPUtil.getString(mContext, Const.SPU_NAME, Const.SPU_APPID, "");
        }
        return mAppId;
    }

    public void setAppId(String appId) {
        this.mAppId = appId;
        SPUtil.putString(mContext, Const.SPU_NAME, Const.SPU_APPID, appId);
    }

    public String getAppKey() {
        if (TextUtils.isEmpty(mAppKey)) {
            mAppKey = SPUtil.getString(mContext, Const.SPU_NAME, Const.SPU_APPKEY, "");
        }
        return mAppKey;
    }

    public void setAppKey(String appKey) {
        this.mAppKey = appKey;
        SPUtil.putString(mContext, Const.SPU_NAME, Const.SPU_APPKEY, appKey);
    }


    public String getPsid() {
        try {
            if (TextUtils.isEmpty(mPsid)) {
                createPsid(getContext(), getAppId(), ApplicationLifecycleListener.COLD_LAUNCH_MODE);
            }
        } catch (Exception e) {

        }
        return mPsid;
    }


    /**
     * 获取SessionId
     *
     * @param placementId
     * @return
     */
    public synchronized String getSessionId(String placementId) {
        if (mSessionIdObject == null) {
            mSessionIdObject = new JSONObject();
        }

        String placementSessionId = mSessionIdObject.optString(placementId);

        if (TextUtils.isEmpty(placementSessionId)) {
            placementSessionId = createSessionId(placementId);
        } else {
            CommonLogUtil.i(TAG, placementId + ": sessionid exists.");
            CommonLogUtil.i(TAG, "placementSessionId :" + placementSessionId);
        }

        return placementSessionId;
    }


    public void initGlobalCommonPara() {

        TaskManager.getInstance().run_proxy(new Runnable() {

            @Override
            public void run() {

                try {
                    CommonDeviceUtil.initCommonDeviceInfo(mContext);// Init Device info
//                    try {
//                        //Get gaid
//                        Class clz = Class.forName("com.google.android.gms.ads.identifier.AdvertisingIdClient");
//                        Class clzInfo = Class.forName("com.google.android.gms.ads.identifier.AdvertisingIdClient$Info");
//                        Method m = clz.getMethod("getAdvertisingIdInfo", Context.class);
//                        Object o = m.invoke(null, mContext);
////                                Class<? extends Object> infoClass = o.getClass();
//
//                        Method m2 = clzInfo.getMethod("getId");
//                        String googleAdvertisingId = (String) m2.invoke(o);
//                        CommonDeviceUtil.setGoogleAdId(googleAdvertisingId);
//
//                    } catch (Exception e) {
////                                e.printStackTrace();
//                        // try to get from google play app library
//                        try {
//                            AdvertisingIdClient.AdInfo adInfo = new AdvertisingIdClient().getAdvertisingIdInfo(mContext);
//                            CommonDeviceUtil.setGoogleAdId(adInfo.getId());
//                        } catch (Exception e1) {
//                        }
//                    }
                } catch (Exception e) {
                }

                if (SDKContext.this.isNetworkLogDebug()) {
                    Log.i(Const.RESOURCE_HEAD, "********************************** " + Const.SDK_VERSION_NAME + " *************************************");
                    Log.i(Const.RESOURCE_HEAD, "GAID(ADID): " + CommonDeviceUtil.getGoogleAdId() + " , AndroidID: " + CommonDeviceUtil.getAndroidID(mContext));
                    Log.i(Const.RESOURCE_HEAD, "********************************** " + Const.SDK_VERSION_NAME + " *************************************");
                }
            }
        });
    }

    /**
     * init
     *
     * @param context
     * @param appId
     * @param appKey
     */
    public synchronized void init(final Context context, final String appId, final String appKey) {
        if (context == null) {
            return;
        }

        try {
            long currentTime = System.currentTimeMillis();
            firstInitTime = SPUtil.getLong(context, Const.SPU_NAME, Const.SPUKEY.SPU_FIRST_INIT_TIME, 0L);
            if (firstInitTime == 0) {
                /**Record first init time**/
                firstInitTime = currentTime;
                SPUtil.putLong(context, Const.SPU_NAME, Const.SPUKEY.SPU_FIRST_INIT_TIME, firstInitTime);
            }

            long currentDateTime = getDateTimeMillis(currentTime);
            long initDateTime = getDateTimeMillis(firstInitTime);

            /**first init days**/
            initDays = (currentDateTime - initDateTime) / (24 * 60 * 60 * 1000L) + 1;

            coldModeCreatePsidTime = 0;
            AdCapV2Manager.getInstance(context.getApplicationContext()).cleanUseLessData();

            final Context applicationContext = context.getApplicationContext();
            setContext(applicationContext);
            setAppId(appId);
            setAppKey(appKey);
            setNetworkLogFileExist();

            registerNetworkChange();


            TaskManager.getInstance().run_proxy(new Runnable() {
                @Override
                public void run() {
                    try {
                        Agent.getInstance().init(applicationContext);
                        createPsid(applicationContext, appId, ApplicationLifecycleListener.COLD_LAUNCH_MODE);
                        registerApplicationLifecycle(context);

                        CrashUtil.getInstance(applicationContext).init();//init Crash Util
                    } catch (Exception e) {
                        if (Const.DEBUG) {
                            e.printStackTrace();
                        }
                    }
                }
            });


            //Delay to get UA
            runOnMainThreadDelayed(new Runnable() {
                @Override
                public void run() {
                    CommonDeviceUtil.getDefaultUserAgent_UI(applicationContext);
//                    }
                }
            }, 5000);

            initGlobalCommonPara();

            TaskManager.getInstance().run_proxy(new Runnable() {
                @Override
                public void run() {
                    getChinaHandler();
                    /**If china-plugin exist, init the china-plugin info**/
                    if (mChinaHandler != null) {
                        mChinaHandler.initDeviceInfo(context);
                    }
                    PlaceStrategyManager.getInstance(applicationContext).cleanPlaceStrategyCheck();
                }
            });

            doInitWork(context, appId, appKey);

        } catch (Exception e) {

        }
    }

    /**
     * register activity lifecycle
     *
     * @param context
     */
    private void registerApplicationLifecycle(Context context) {
        long startTime = coldModeCreatePsidTime;
        try {
            String recordJSON = SPUtil.getString(SDKContext.getInstance().getContext(), Const.SPU_NAME, SDKContext.getInstance().getAppId() + "playRecord", "");

            if (!TextUtils.isEmpty(recordJSON)) {
                JSONObject jsonObject = new JSONObject(recordJSON);
                long recordStartTime = jsonObject.optLong(ApplicationLifecycleListener.JSON_START_TIME_KEY);
                long endTime = jsonObject.optLong(ApplicationLifecycleListener.JSON_END_TIME_KEY);
                String psid = jsonObject.optString(ApplicationLifecycleListener.JSON_PSID_KEY);
                int launchMode = jsonObject.optInt(ApplicationLifecycleListener.JSON_LAUNCH_MODE_KEY);

                if (startTime != 0) { //Psid create time is not 0, send the lastest playtime
                    AgentEventManager.sendApplicationPlayTime(launchMode == ApplicationLifecycleListener.HOT_LAUNCH_MODE ? 4 : 2, recordStartTime, endTime, psid);
                    CommonLogUtil.e(TAG, "Create new psid, SDKContext.init to send playTime:" + (endTime - recordStartTime) / 1000);
                } else {
                    startTime = recordStartTime; //psid is old, use the pervious start time
                    CommonLogUtil.e(TAG, "Psid is old, use pervioud statime，close before:" + (endTime - recordStartTime) / 1000);
                }
                SPUtil.putString(SDKContext.getInstance().getContext(), Const.SPU_NAME, SDKContext.getInstance().getAppId() + "playRecord", "");
            }

        } catch (Exception e) {
            SPUtil.putString(SDKContext.getInstance().getContext(), Const.SPU_NAME, SDKContext.getInstance().getAppId() + "playRecord", "");
        }

        if (startTime == 0) { //If start time is 0,
            startTime = SPUtil.getLong(context, Const.SPU_NAME, Const.SPUKEY.SPU_INIT_TIME_KEY, 0L);
        }

        if (startTime == 0) { //If start time is 0,
            startTime = System.currentTimeMillis();
        }

        /**Register the application lifecycle**/
        ((Application) context.getApplicationContext())
                .registerActivityLifecycleCallbacks(new ApplicationLifecycleListener(ApplicationLifecycleListener.COLD_LAUNCH_MODE, startTime));
    }

    /**
     * Register network changet
     */
    private void registerNetworkChange() {
        try {
            if (mNetworkChangeReceiver != null) {
                mContext.unregisterReceiver(mNetworkChangeReceiver);
            }
            mNetworkChangeReceiver = null;
        } catch (Throwable e) {

        }

        try {
            mNetworkChangeReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (CommonUtil.isNetConnect(context)) {
                        OffLineTkManager.getInstance().tryToReSendRequest();
                        Agent.getInstance().sendLogByTime();
                        checkAppStrategy(context, getAppId(), getAppKey());
                    }
                }
            };
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            mContext.registerReceiver(mNetworkChangeReceiver, intentFilter);
        } catch (Throwable e) {

        }
    }

    long coldModeCreatePsidTime = 0; //Psid create time in init's medthod

    /**
     * Psid: record the life of SDK-Starting
     *
     * @param context
     * @param appId
     * @return create time, if no create new psid, it will return 0
     * @throws JSONException
     */
    protected synchronized long createPsid(Context context, String appId, int launchMode) throws JSONException {
        AppStrategy appStrategy = AppStrategyManager.getInstance(context).getAppStrategyByAppId(appId);

        String psid = SPUtil.getString(context, Const.SPU_NAME, Const.SPUKEY.SPU_PSID_KEY, "");
        String sessionId = SPUtil.getString(context, Const.SPU_NAME, Const.SPUKEY.SPU_SESSIONID_KEY, "");
        long time = SPUtil.getLong(context, Const.SPU_NAME, Const.SPUKEY.SPU_INIT_TIME_KEY, 0L);

        long currentTime = System.currentTimeMillis();
        //做层保护，如果开发者调整过时间会导致时间间隔<0,则将init的更新时间设置为0
        if (currentTime - time < 0) {
            time = 0;
        }

        /**Psid and SessionId create**/
        if (currentTime - time <= (launchMode == ApplicationLifecycleListener.COLD_LAUNCH_MODE
                ? appStrategy.getPsidTimeOut() : appStrategy.getRecreatePsidIntervalWhenHotBoot())) {
            CommonLogUtil.i(TAG, "psid updataTime<=" + appStrategy.getPsidTimeOut());
            mPsid = psid;
            if (!TextUtils.isEmpty(sessionId)) {
                mSessionIdObject = new JSONObject(sessionId);
            }
            CommonLogUtil.i(TAG, "psid :" + mPsid);
            return 0;
        } else {
            CommonLogUtil.i(TAG, "psid updataTime>" + appStrategy.getPsidTimeOut());
            String deviceId = getUpId();
            String randomString = "";
            if (TextUtils.isEmpty(deviceId)) {
                deviceId = CommonDeviceUtil.getAndroidID(context) + CommonDeviceUtil.getGoogleAdId();
                randomString = String.valueOf(new Random().nextInt(10000000));
            }

            mPsid = CommonMD5.getMD5(deviceId + appId + randomString + currentTime);

            //Reset session id
            mSessionIdObject = new JSONObject();

            SPUtil.putString(context, Const.SPU_NAME, Const.SPUKEY.SPU_PSID_KEY, mPsid); //update psid
            SPUtil.putString(context, Const.SPU_NAME, Const.SPUKEY.SPU_SESSIONID_KEY, ""); //clean all sessionid
            SPUtil.putLong(context, Const.SPU_NAME, Const.SPUKEY.SPU_INIT_TIME_KEY, currentTime); //update SDK-init time

            CommonLogUtil.i(TAG, "psid :" + mPsid);
            AgentEventManager.sdkInitEvent(null, "1", randomString, currentTime + "");
            if (launchMode == ApplicationLifecycleListener.COLD_LAUNCH_MODE) {
                coldModeCreatePsidTime = currentTime;
            }
            return currentTime;
        }
    }

    /**
     * Create SessionId
     *
     * @param placementId
     * @return
     */
    private String createSessionId(String placementId) {
        String placementSessionId;
        CommonLogUtil.i(TAG, placementId + ": sessionid is empty.");
        String deviceId = getUpId();
        String randomString = "";
        if (TextUtils.isEmpty(deviceId)) {
            deviceId = CommonDeviceUtil.getAndroidID(mContext) + CommonDeviceUtil.getGoogleAdId();
            randomString = new Random().nextInt(10000000) + "";
        }
        long currentTime = System.currentTimeMillis();
        placementSessionId = CommonMD5.getMD5(deviceId + placementId + randomString + currentTime);
        try {
            mSessionIdObject.put(placementId, placementSessionId);
        } catch (Exception e) {
        }

        SPUtil.putString(mContext, Const.SPU_NAME, Const.SPUKEY.SPU_SESSIONID_KEY, mSessionIdObject.toString());

        CommonLogUtil.i(TAG, "placementSessionId :" + placementSessionId);
        AgentEventManager.sdkInitEvent(placementId, "2", TextUtils.isEmpty(getUpId()) ? randomString : null, currentTime + "");
        return placementSessionId;
    }


    private void doInitWork(final Context context, final String mAppId, final String appKey) {
        // Check AppSetting
        checkAppStrategy(context.getApplicationContext(), mAppId, appKey);

        TaskManager.getInstance().run_proxy(new Runnable() {
            @Override
            public void run() {
                AppStrategy appStrategy = AppStrategyManager.getInstance(context.getApplicationContext()).getAppStrategyByAppId(mAppId);
                if (appStrategy != null) {
                    if (!appStrategy.isLocalStrategy()) {
                        MsgManager.getInstance(mContext).handleInit(appStrategy);
                    }

                    if (!AppStrategyManager.getInstance(context.getApplicationContext()).isTimeToGetAppStrategy(mAppId)) {
                        // Pre-init some Mediation
                        AppStrategyManager.getInstance(context.getApplicationContext()).preInit(context, appStrategy);
                    }
                }
            }
        });


    }

    public void integrationChecking(final Context context) {
        TaskManager.getInstance().run_proxy(new Runnable() {
            @Override
            public void run() {
                //init check
                try {
                    List<String> initClassNames = new ArrayList<>();
                    String packageName = "com.anythink.network";
                    DexFile dexFile = new DexFile(context.getPackageCodePath());
                    Enumeration<String> enumeration = dexFile.entries();
                    while (enumeration.hasMoreElements()) {//遍历
                        String className = enumeration.nextElement();

//                        Log.e("test", className);
                        if (className.contains(packageName) && className.contains("InitManager") && !className.contains("$")) {
                            initClassNames.add(className);
                        }
                    }

                    Log.i(Const.RESOURCE_HEAD, "********************************** Network Integration Status *************************************");
                    if (initClassNames.size() != 0) {
                        Log.i(Const.RESOURCE_HEAD, "----------------------------------------");
                    }
                    for (String className : initClassNames) {
                        try {
                            Class<?> aClass = Class.forName(className);
                            Method getInstance = aClass.getDeclaredMethod("getInstance");
                            Object instance = null;
                            try {
                                instance = getInstance.invoke(null);
                            } catch (Throwable e) {
                                Log.e(Const.RESOURCE_HEAD, "Cannot instantiate " + aClass.getName() + ", please check if a third-party SDK is imported");
                                Log.i(Const.RESOURCE_HEAD, "----------------------------------------");
                            }

                            if (instance != null && instance instanceof ATInitMediation) {
//                                    CommonLogUtil.d(TAG, "initSDK - > " + adapterName);
                                ATInitMediation networkSDKInitManager = ((ATInitMediation) instance);
                                String networkName = networkSDKInitManager.getNetworkName();
                                if (TextUtils.isEmpty(networkName)) {
                                    continue;
                                }
                                Log.i(Const.RESOURCE_HEAD, "NetworkName: " + networkName);
                                boolean isNetworkClassExist = isNetworkClassExist(networkSDKInitManager.getNetworkSDKClass());
                                boolean isPluginClassExist = isPluginClassExist(networkSDKInitManager.getPluginClassStatus());
                                boolean isActivityExist = isActivityVertify(context, networkSDKInitManager.getActivityStatus());
                                boolean isServiceExist = isServicesValid(context, networkSDKInitManager.getServiceStatus());
                                boolean isProviderExist = isProviderValid(context, networkSDKInitManager.getProviderStatus());

                                boolean status = isNetworkClassExist && isPluginClassExist && isActivityExist && isServiceExist && isProviderExist;

                                if (status) {
                                    Log.i(Const.RESOURCE_HEAD, "Status: Success");
                                } else {
                                    Log.e(Const.RESOURCE_HEAD, "Status: Fail");
                                }


                                Log.i(Const.RESOURCE_HEAD, "----------------------------------------");

                            }
                        } catch (Throwable e) {
                        }

                    }
                    Log.i(Const.RESOURCE_HEAD, "********************************** Network Integration Status *************************************");

                } catch (Exception e) {

                }

            }
        });
    }

    public void checkSplashDefaultConfig(Context context, final String splashPlacementId, Map<String, Object> customMap) {
        if (isNetworkLogDebug()) {
            Log.i(Const.RESOURCE_HEAD, "Requesting placement(" + splashPlacementId + ") setting Info，please wait a moment.");
            PlaceStrategyLoader placeStrategyLoader = new PlaceStrategyLoader(context, SDKContext.getInstance().getAppId(), SDKContext.getInstance().getAppKey(), splashPlacementId, "", customMap);
            placeStrategyLoader.start(0, new OnHttpLoaderListener() {
                @Override
                public void onLoadStart(int reqCode) {

                }

                @Override
                public void onLoadFinish(int reqCode, Object result) {
                    String json = (String) result;

                    final PlaceStrategy curr = PlaceStrategy.parseStrategy(json);
                    if (!Const.FORMAT.SPLASH_FORMAT.equals(String.valueOf(curr.getFormat()))) {
                        Log.i(Const.RESOURCE_HEAD, "********************************** Get Splash Config Start(" + splashPlacementId + ") *************************************");
                        Log.i(Const.RESOURCE_HEAD, "This placement(" + splashPlacementId + ") does not belong to Splash!");
                        Log.i(Const.RESOURCE_HEAD, "********************************** Get Splash Config End(" + splashPlacementId + ") *************************************");
                    } else {
                        Log.i(Const.RESOURCE_HEAD, "********************************** Get Splash Config Start(" + splashPlacementId + ") *************************************");
                        List<PlaceStrategy.UnitGroupInfo> unitGroupInfoList = curr.getNormalUnitGroupList();
                        if (unitGroupInfoList == null || unitGroupInfoList.size() == 0) {
                            Log.i(Const.RESOURCE_HEAD, ErrorCode.getErrorCode(ErrorCode.noAdsourceConfig, "", "").getDesc());
                        } else {
                            for (PlaceStrategy.UnitGroupInfo unitGroupInfo : unitGroupInfoList) {
                                Log.i(Const.RESOURCE_HEAD, "------------------------------------------------");
                                Log.i(Const.RESOURCE_HEAD, "Network Firm Id:" + unitGroupInfo.networkType);
                                Log.i(Const.RESOURCE_HEAD, "AdSource Id:" + unitGroupInfo.unitId);
                                Log.i(Const.RESOURCE_HEAD, "Network Content:" + unitGroupInfo.content);
                                Log.i(Const.RESOURCE_HEAD, "------------------------------------------------");
                            }
                        }
                        Log.i(Const.RESOURCE_HEAD, "********************************** Get Splash Config End(" + splashPlacementId + ") *************************************");
                    }
                }

                @Override
                public void onLoadError(int reqCode, String msg, AdError errorCode) {
                    Log.i(Const.RESOURCE_HEAD, "********************************** Get Splash Config Start(" + splashPlacementId + ") *************************************");
                    Log.i(Const.RESOURCE_HEAD, "This placement(" + splashPlacementId + ") request error:" + errorCode.printStackTrace());
                    Log.i(Const.RESOURCE_HEAD, "********************************** Get Splash Config End(" + splashPlacementId + ") *************************************");
                }

                @Override
                public void onLoadCanceled(int reqCode) {

                }
            });
        } else {
            Log.i(Const.RESOURCE_HEAD, "********************************** Get Splash Config Start(" + splashPlacementId + ") *************************************");
            Log.i(Const.RESOURCE_HEAD, "Only use in debug mode!");
            Log.i(Const.RESOURCE_HEAD, "********************************** Get Splash Config End(" + splashPlacementId + ") *************************************");

        }
    }


    boolean isNetworkClassExist(String className) {
        try {
            Class.forName(className);
            Log.i(Const.RESOURCE_HEAD, "SDK: VERIFIED");
            return true;
        } catch (Throwable e) {

        }
        Log.i(Const.RESOURCE_HEAD, "SDK: NOT VERIFIED");
        return false;
    }

    boolean isPluginClassExist(Map<String, Boolean> pluginMap) {
        if (pluginMap == null) {
            return true;
        }

        boolean isExist = true;
        StringBuilder missTips = new StringBuilder();
        for (String key : pluginMap.keySet()) {
            if (!pluginMap.get(key)) {
                isExist = false;
                missTips.append(", ").append(key);
            }
        }
        if (missTips.length() > 2) {
            missTips.delete(0, 2);
        }

        if (isExist) {
            Log.i(Const.RESOURCE_HEAD, "Dependence Plugin: VERIFIED");
        } else {
            Log.e(Const.RESOURCE_HEAD, "Dependence Plugin: Missing " + missTips);
        }

        return isExist;
    }

    boolean isActivityVertify(Context context, List<String> list) {
        if (list == null) {
            return true;
        }
        boolean isVertify = true;
        StringBuilder reason = new StringBuilder();
        for (String activityClass : list) {
            try {
                if (context.getPackageManager().queryIntentActivities(new Intent(context, Class.forName(activityClass)), PackageManager.MATCH_ALL).size() > 0) {

                } else {
                    isVertify = false;
                    reason.append(", ").append(activityClass);
                }
            } catch (Throwable e) {
                isVertify = false;
                reason.append("error: ").append(e.getMessage());
            }
        }
        if (reason.length() > 2) {
            reason.delete(0, 2);
        }

        if (isVertify) {
            Log.i(Const.RESOURCE_HEAD, "Activities : VERIFIED");
        } else {
            Log.e(Const.RESOURCE_HEAD, "Activities : Missing " + reason.toString() + " declare in AndroidManifest");
        }

        return isVertify;

    }

    boolean isServicesValid(Context context, List<String> list) {
        if (list == null) {
            return true;
        }
        boolean isVertify = true;
        StringBuilder reason = new StringBuilder();
        PackageManager packageManager = context.getPackageManager();
        for (String service : list) {
            try {
                if (packageManager.queryIntentServices(new Intent(context, Class.forName(service)), PackageManager.MATCH_ALL).size() > 0) {
                } else {
                    isVertify = false;
                    reason.append(", ").append(service);
                }
            } catch (Throwable e) {
                isVertify = false;
                reason.append("error: ").append(e.getMessage());
            }
        }
        if (reason.length() > 2) {
            reason.delete(0, 2);
        }

        if (isVertify) {
            Log.i(Const.RESOURCE_HEAD, "Services : VERIFIED");
        } else {
            Log.e(Const.RESOURCE_HEAD, "Services : Missing " + reason.toString() + " declare in AndroidManifest");
        }

        return isVertify;
    }

    boolean isProviderValid(Context context, List<String> list) {
        if (list == null) {
            return true;
        }
        boolean isVertify = true;
        StringBuilder reason = new StringBuilder();

        PackageManager packageManager = context.getPackageManager();

        PackageInfo info = null;
        try {
            info = packageManager.getPackageInfo(context.getPackageName(),
                    PackageManager.GET_PROVIDERS);
        } catch (Throwable e) {
            isVertify = false;
            reason.append("error: ").append(e.getMessage());
        }

        if (info == null) {
            return false;
        }

        ProviderInfo[] providers = info.providers;
        boolean find;
        for (String provider : list) {
            find = false;
            for (ProviderInfo providerInfo : providers) {
                if (TextUtils.equals(providerInfo.name, provider)) {
                    find = true;
                    break;
                }
            }
            if (!find) {
                isVertify = false;
                reason.append(", ").append(provider);
            }
        }
        if (reason.length() > 2) {
            reason.delete(0, 2);
        }

        if (isVertify) {
            Log.i(Const.RESOURCE_HEAD, "Providers : VERIFIED");
        } else {
            Log.e(Const.RESOURCE_HEAD, "Providers : Missing " + reason.toString() + " declare in AndroidManifest");
        }

        return isVertify;
    }


    /**
     * Check AppSetting
     *
     * @param context
     * @param mAppId
     * @param appKey
     */
    public void checkAppStrategy(final Context context, final String mAppId, final String appKey) {
        //Update the AppSetting which is out of date
        TaskManager.getInstance().run_proxy(new Runnable() {
            @Override
            public void run() {
                if (AppStrategyManager.getInstance(context).isTimeToGetAppStrategy(mAppId)) {
                    AppStrategyManager.getInstance(context).startRequest(mAppId, appKey);
                }
            }
        });
    }

    public void runOnMainThread(Runnable runnable) {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            runnable.run();
        } else {
            mHandler.post(runnable);
        }
    }

    public void runOnThreadPool(Runnable runnable) {
        TaskManager.getInstance().run_proxy(runnable);
    }

    public void runOnMainThreadDelayed(Runnable runnable, long delayMillis) {
        mHandler.postDelayed(runnable, delayMillis);
    }

    public void removeMainThreadRunnable(Runnable runnable) {
        mHandler.removeCallbacks(runnable);
    }

    public void runOnThreadPoolsDelayed(Runnable runnable, long delayMillis) {
        TaskManager.getInstance().run_proxyDelayed(runnable, delayMillis);
    }


    public String getUpId() {
        if (TextUtils.isEmpty(mUpId)) {
            mUpId = SPUtil.getString(mContext, Const.SPU_NAME, Const.SPUKEY.SPU_UP_ID_KEY, "");
        }
        return mUpId;
    }


    public String getSysId() {
        if (TextUtils.isEmpty(mSysId)) {
            mSysId = SPUtil.getString(mContext, Const.SPU_EXC_LOG_NAME, Const.SPUKEY.SPU_EXC_SYS, "");
        }
        return mSysId;
    }

    public String getBkId() {
        if (TextUtils.isEmpty(mBkId)) {
            mBkId = SPUtil.getString(mContext, Const.SPU_EXC_LOG_NAME, Const.SPUKEY.SPU_EXC_BK, "");
        }
        return mBkId;
    }

    public void saveSysId(String sysId) {
        mSysId = sysId;
        SPUtil.putString(mContext, Const.SPU_EXC_LOG_NAME, Const.SPUKEY.SPU_EXC_SYS, sysId);
    }

    public void saveBkId(String bkId) {
        mBkId = bkId;
        SPUtil.putString(mContext, Const.SPU_EXC_LOG_NAME, Const.SPUKEY.SPU_EXC_BK, bkId);
    }


    public void setUpId(String upId) {
        SPUtil.putString(mContext, Const.SPU_NAME, Const.SPUKEY.SPU_UP_ID_KEY, upId);
        mUpId = upId;
    }

    public void setNetworkLogDebug(boolean debug) {
        this.DEVELOPER_NETWORK_LOG_DEBUG = debug;
    }

    private void setNetworkLogFileExist() {
        boolean exists = false;
        if (mContext != null) {
            try {
                exists = new File(mContext.getExternalFilesDir(null), mLogPath).exists();
                if (!exists) {
                    exists = new File(mContext.getFilesDir(), mLogPath).exists();
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        NETWORK_LOG_FILE_EXIST = exists;
    }

    public boolean isNetworkLogDebug() {
        return this.NETWORK_LOG_FILE_EXIST || this.DEVELOPER_NETWORK_LOG_DEBUG;
    }

    /**
     * Formatting the JSON String
     *
     * @param tag
     * @param msg
     */
    public void printJson(String tag, String msg) {
        String LINE_SEPARATOR = System.getProperty("line.separator");
        String message;

        try {
            if (msg.startsWith("{")) {
                JSONObject jsonObject = new JSONObject(msg);
                message = jsonObject.toString(4);
            } else if (msg.startsWith("[")) {
                JSONArray jsonArray = new JSONArray(msg);
                message = jsonArray.toString(4);
            } else {
                message = msg;
            }
        } catch (JSONException e) {
            message = msg;
        }

//        printLine(tag, true);
        String jsonPrint = "";
        jsonPrint = "╔═══════════════════════════════════════════════════════════════════════════════════════";

        message = LINE_SEPARATOR + message;
        String[] lines = message.split(LINE_SEPARATOR);
        for (String line : lines) {
            jsonPrint = jsonPrint + "\n";
            jsonPrint = jsonPrint + "║ " + line;
        }
        jsonPrint = jsonPrint + "\n╚═══════════════════════════════════════════════════════════════════════════════════════";
//        printLine(tag, false);
        Log.i(tag, " \n" + jsonPrint);

    }

    public long getDateTimeMillis(long timeMillis) {
        Date date = new Date(timeMillis);
        return new Date(date.getYear(), date.getMonth(), date.getDate()).getTime();
    }


}