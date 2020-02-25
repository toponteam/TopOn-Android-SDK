package com.anythink.core.common.base;

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

import com.anythink.core.api.ATInitMediation;
import com.anythink.core.common.OffLineTkManager;
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
import com.anythink.core.strategy.PlaceStrategyManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Random;

import dalvik.system.DexFile;

/**
 * Created by Z on 2017-1-20.
 */

public class SDKContext {
    private final String TAG = "SDK.init";
    private static SDKContext instance;

    private Context mContext;
    private String mAppId;
    private String mAppKey;
    private Handler mHandler;

    private Map<String, String> mCustomMap;
    private String mChannel;
    private String mSubChannel;

    private String mPsid;
    private JSONObject mSessionIdObject;

    private BroadcastReceiver mNetworkChangeReceiver;


    private String mUpId;


    public static SDKContext getInstance() {
        if (instance == null) {
            synchronized (SDKContext.class) {
                instance = new SDKContext();
            }
        }
        return instance;
    }

    public SDKContext() {
        mHandler = new Handler(Looper.getMainLooper());
    }

    public void setContext(Context context) {
        mContext = context;
    }

    public Context getContext() {
        return mContext;
    }


    public void setCustomMap(Map<String, String> customMap) {
        mCustomMap = customMap;
    }

    public Map<String, String> getCustomMap() {
        return mCustomMap;
    }

    public String getChannel() {
        return mChannel;
    }

    public void setChannel(String channel) {
        this.mChannel = channel;
    }

    public String getSubChannel() {
        return mSubChannel;
    }

    public void setSubChannel(String subChannel) {
        this.mSubChannel = subChannel;
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
                createPsid(getContext(), getAppId());
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
            CommonLogUtil.i(TAG, placementId + ": sessionid exits.");
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
                    try {
                        //Get gaid
                        Class clz = Class.forName("com.google.android.gms.ads.identifier.AdvertisingIdClient");
                        Class clzInfo = Class.forName("com.google.android.gms.ads.identifier.AdvertisingIdClient$Info");
                        Method m = clz.getMethod("getAdvertisingIdInfo", Context.class);
                        Object o = m.invoke(null, mContext);
//                                Class<? extends Object> infoClass = o.getClass();

                        Method m2 = clzInfo.getMethod("getId");
                        String googleAdvertisingId = (String) m2.invoke(o);
                        CommonDeviceUtil.setGoogleAdId(googleAdvertisingId);

                    } catch (Exception e) {
//                                e.printStackTrace();
                        // try to get from google play app library
                        try {
                            AdvertisingIdClient.AdInfo adInfo = new AdvertisingIdClient().getAdvertisingIdInfo(mContext);
                            CommonDeviceUtil.setGoogleAdId(adInfo.getId());
                        } catch (Exception e1) {
                        }
                    }
                } catch (Exception e) {
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
    public void init(final Context context, final String appId, final String appKey) {
        if (context == null) {
            return;
        }

        try {
            final Context applicationContext = context.getApplicationContext();
            setContext(applicationContext);
            setAppId(appId);
            setAppKey(appKey);

            registerNetworkChange();

            TaskManager.getInstance().run_proxy(new Runnable() {
                @Override
                public void run() {
                    try {
                        Agent.init(applicationContext);
                        createPsid(applicationContext, appId);
                    } catch (Exception e) {

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
                    PlaceStrategyManager.getInstance(applicationContext).cleanPlaceStrategyCheck();
                }
            });

            doInitWork(context, appId, appKey);

        } catch (Exception e) {

        }
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
                    }
                }
            };
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            mContext.registerReceiver(mNetworkChangeReceiver, intentFilter);
        } catch (Throwable e) {

        }
    }

    /**
     * Psid: record the life of SDK-Starting
     *
     * @param context
     * @param appId
     * @throws JSONException
     */
    private synchronized void createPsid(Context context, String appId) throws JSONException {
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
        if (currentTime - time <= appStrategy.getPsidTimeOut()) {
            CommonLogUtil.i(TAG, "psid updataTime<=" + appStrategy.getPsidTimeOut());
            mPsid = psid;
            if (!TextUtils.isEmpty(sessionId)) {
                mSessionIdObject = new JSONObject(sessionId);
            }
            CommonLogUtil.i(TAG, "psid :" + mPsid);
        } else {
            CommonLogUtil.i(TAG, "psid updataTime>" + appStrategy.getPsidTimeOut());
            String deviceId = getUpId();
            String randomString = "";
            if (TextUtils.isEmpty(deviceId)) {
                deviceId = CommonDeviceUtil.getAndroidID(context) + CommonDeviceUtil.getGoogleAdId();
                randomString = String.valueOf(new Random().nextInt(10000000));
            }

            mPsid = CommonMD5.getMD5(deviceId + appId + randomString + currentTime);
            SPUtil.putString(context, Const.SPU_NAME, Const.SPUKEY.SPU_PSID_KEY, mPsid); //update psid
            SPUtil.putString(context, Const.SPU_NAME, Const.SPUKEY.SPU_SESSIONID_KEY, ""); //clean all sessionid
            SPUtil.putLong(context, Const.SPU_NAME, Const.SPUKEY.SPU_INIT_TIME_KEY, currentTime); //update SDK-init time

            CommonLogUtil.i(TAG, "psid :" + mPsid);
            AgentEventManager.sdkInitEvent(null, psid, null, "1", randomString, currentTime + "");
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
        AgentEventManager.sdkInitEvent(placementId, mPsid, placementSessionId, "2", TextUtils.isEmpty(getUpId()) ? randomString : null, currentTime + "");
        return placementSessionId;
    }


    private void doInitWork(final Context context, final String mAppId, final String appKey) {
        // Check AppSetting
        checkAppStrategy(context.getApplicationContext(), mAppId, appKey);

        TaskManager.getInstance().run_proxy(new Runnable() {
            @Override
            public void run() {
                AppStrategy appStrategy = AppStrategyManager.getInstance(context.getApplicationContext()).getAppStrategyByAppId(mAppId);
                if (appStrategy != null && !AppStrategyManager.getInstance(context.getApplicationContext()).isTimeToGetAppStrategy(mAppId)) {
                    // Pre-init some Mediation
                    AppStrategyManager.getInstance(context.getApplicationContext()).preInit(context, appStrategy);
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
                                boolean isActivityExist = isActivityVertify(networkSDKInitManager.getActivityStatus());
                                boolean isServiceExist = isServicesValid(networkSDKInitManager.getServiceStatus());
                                boolean isProviderExist = isProviderValid(networkSDKInitManager.getProviderStatus());

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

    boolean isActivityVertify(List<String> list) {
        if (list == null) {
            return true;
        }
        boolean isVertify = true;
        StringBuilder reason = new StringBuilder();
        for (String activityClass : list) {
            try {
                if (mContext.getPackageManager().queryIntentActivities(new Intent(mContext, Class.forName(activityClass)), PackageManager.MATCH_ALL).size() > 0) {

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

    boolean isServicesValid(List<String> list) {
        if (list == null) {
            return true;
        }
        boolean isVertify = true;
        StringBuilder reason = new StringBuilder();
        PackageManager packageManager = mContext.getPackageManager();
        for (String service : list) {
            try {
                if (packageManager.queryIntentServices(new Intent(mContext, Class.forName(service)), PackageManager.MATCH_ALL).size() > 0) {
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

    boolean isProviderValid(List<String> list) {
        if (list == null) {
            return true;
        }
        boolean isVertify = true;
        StringBuilder reason = new StringBuilder();

        PackageManager packageManager = mContext.getPackageManager();

        PackageInfo info = null;
        try {
            info = packageManager.getPackageInfo(mContext.getPackageName(),
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
        mHandler.post(runnable);
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


    public void setUpId(String upId) {
        SPUtil.putString(mContext, Const.SPU_NAME, Const.SPUKEY.SPU_UP_ID_KEY, upId);
        mUpId = upId;
    }


    /**
     * Formatting the JSON String
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


}