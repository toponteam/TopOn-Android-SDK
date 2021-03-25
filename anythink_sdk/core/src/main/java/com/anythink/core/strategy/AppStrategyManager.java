/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.strategy;

import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.ATCustomRuleKeys;
import com.anythink.core.api.ATInitMediation;
import com.anythink.core.api.AdError;
import com.anythink.core.common.MsgManager;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.db.CommonSDKDBHelper;
import com.anythink.core.common.db.ConfigInfoDao;
import com.anythink.core.common.entity.SDKConfigInfo;
import com.anythink.core.common.net.AppStrategyLoader;
import com.anythink.core.common.net.OnHttpLoaderListener;
import com.anythink.core.common.utils.CommonLogUtil;
import com.anythink.core.common.utils.CommonUtil;
import com.anythink.core.common.utils.SPUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


public class AppStrategyManager {
    public static final String TAG = AppStrategyManager.class.getSimpleName();
    private static AppStrategyManager mInstance = null;
    private static AppStrategy appStrategy = null;
    private Context mContext;
    private boolean isLoading;

    List<AppStragtegyRequestListener> mRegisterListeners;

    private Object listenerSynchronizedObject = new Object();

    private AppStrategyManager(Context context) {
        mContext = context;
        isLoading = false;
        mRegisterListeners = Collections.synchronizedList(new ArrayList<AppStragtegyRequestListener>(3));
    }

    public synchronized static AppStrategyManager getInstance(Context context) {
        if (mInstance == null) {
            synchronized (AppStrategyManager.class) {
                if (mInstance == null) {
                    mInstance = new AppStrategyManager(context);
                }
            }
        }
        return mInstance;
    }

    private Context getContext() {
        return mContext;
    }

    private void setContext(Context context) {
        mContext = context;
    }

    public void registerAppStrategyListener(AppStragtegyRequestListener listener) {
        synchronized (listenerSynchronizedObject) {
            if (listenerSynchronizedObject != null) {
                mRegisterListeners.add(listener);
            }
        }
    }

    private void notifyAppStrategyListener(String errorMsg) {
        synchronized (listenerSynchronizedObject) {
            for (AppStragtegyRequestListener listener : mRegisterListeners) {
                if (listener != null) {
                    if (errorMsg == null) {
                        listener.onSuccess();
                    } else {
                        listener.onFail(errorMsg);
                    }

                }
            }
            mRegisterListeners.clear();
        }
    }

    public void unRegisterAppStrategyListener(AppStragtegyRequestListener listener) {
        synchronized (listenerSynchronizedObject) {
            if (listener != null) {
                mRegisterListeners.remove(listener);
            }
        }
    }

    /**
     * @param appId
     * @return
     */
    public boolean isTimeToGetAppStrategy(String appId) {
        AppStrategy strategy = getAppStrategyByAppId(appId);
        if (strategy != null) {
            OfmStrategy ofmStrategy = strategy.getOfmStrategy();
            long interval = strategy.getStrategyOutTime();
            long currentTime = System.currentTimeMillis();
            long settingNextRequestTime = strategy.getUpdateTime() + interval;

            boolean isStrategyOutdate = settingNextRequestTime <= currentTime;

            boolean isOfmStrategyOutdate = false;
            if (ofmStrategy != null) {
                isOfmStrategyOutdate = (strategy.getUpdateTime() + ofmStrategy.getAvailTime()) <= currentTime;
            }

            boolean isCustomChange = false;
            Map<String, Object> strategyCustomMap = strategy.getAppCustomMap();
            Map<String, Object> newestCustomMap = SDKContext.getInstance().getCustomMap();
            if (strategyCustomMap != null) {
                isCustomChange = !strategyCustomMap.equals(newestCustomMap);
            } else if (newestCustomMap != null) {
                isCustomChange = true;
            }

            if (!isStrategyOutdate && !isOfmStrategyOutdate && !isCustomChange) {
                return false;
            }
        }
        CommonLogUtil.i(TAG, "app Settings timeout or not exists");
        return true;
    }

    /**
     * get app strategy from db
     *
     * @param appid
     * @return
     */
    public synchronized AppStrategy getAppStrategyByAppId(String appid) {
        if (appStrategy == null) {
            try {
                if (mContext == null) {
                    mContext = SDKContext.getInstance().getContext();
                }
                appStrategy = getDBStrategy(mContext, appid);
                if (appStrategy == null) {
                    appStrategy = getLocalStrategy();
                }

            } catch (Exception e) {
                if (Const.DEBUG) {
                    e.printStackTrace();
                }

            }
        }
        return appStrategy;
    }

    /**
     * @return
     */
    public long getMyOfferCacheSize() {
        if (appStrategy == null || appStrategy.getOfferCacheSize() == 0) {
            return 50 * 1024;
        }
        return appStrategy.getOfferCacheSize();
    }


    /***
     * @return
     */
    public static AppStrategy getLocalStrategy() {
        AppStrategy appStrategy = new AppStrategy();
        appStrategy.isLocalStrategy = true;
        appStrategy.setStrategyOutTime(Const.DEFAULT_SDK_KEY.APPSTRATEGY_DEFAULT_OUTTIME);
        appStrategy.setReq_ver("0");
        appStrategy.setUpdateTime(0);
        appStrategy.setPlacementTimeOut(5000L);

        appStrategy.setTkMaxAmount(1);
        appStrategy.setTkInterval(0);
        appStrategy.setTkAddress("");

        appStrategy.setDaMaxAmount(1);
        appStrategy.setDaInterval(0);
        appStrategy.setDaAddress("");

        appStrategy.setPsidTimeOut(30 * 1000L);
        appStrategy.setOfferCacheSize(50 * 1024); //50MB

        appStrategy.setRecreatePsidIntervalWhenHotBoot(30000);
        appStrategy.setUseCountDownSwitchAfterLeaveApp(0);

        appStrategy.setCrashList("[\"com.anythink\"]");
        appStrategy.setCrashSwitch(1);
        return appStrategy;
    }

    /**
     * @param mContext
     * @param appid
     * @return
     */
    public static AppStrategy getDBStrategy(Context mContext, String appid) {
        List<SDKConfigInfo> sdkConfigInfoList = ConfigInfoDao.getInstance(CommonSDKDBHelper.getInstance(mContext)).queryAllByKey(appid, Const.SPUKEY.SPU_APP_STRATEGY_TYPE);
        SDKConfigInfo temp;
        if (sdkConfigInfoList != null && sdkConfigInfoList.size() > 0) {
            temp = sdkConfigInfoList.get(0);
            if (temp != null) {
                String json = temp.getValue();
                AppStrategy appStrategy = AppStrategy.parseStrategy(json);
                if (appStrategy != null) {
                    appStrategy.setUpdateTime(Long.parseLong(temp.getUpdatetime()));
                }
                return appStrategy;
            }
            return null;
        }
        return null;
    }

    /***
     * @param appid
     */
    public static AppStrategy saveStrategy(Context mContext, String appid, String json) {
        ConfigInfoDao.getInstance(CommonSDKDBHelper.getInstance(mContext)).insertOrUpdate(appid, json, Const.SPUKEY.SPU_APP_STRATEGY_TYPE);
        AppStrategy appStrategy = AppStrategy.parseStrategy(json);
        appStrategy.setUpdateTime(System.currentTimeMillis());
        SPUtil.putInt(mContext, Const.SPU_NAME, Const.SPUKEY.SPU_EU_INFO, appStrategy.getGdprIa());
        return appStrategy;
    }

    public boolean isAppStrategyRequesting() {
        return isLoading;
    }

    /***
     * Appsetting request
     */
    public synchronized void startRequest(final String appid, final String appkey) {
        if (isLoading) {
            return;
        }
        isLoading = true;
        AppStrategyLoader appStrategyLoader = new AppStrategyLoader(mContext, appid, appkey);
        appStrategyLoader.start(0, new OnHttpLoaderListener() {
            @Override
            public void onLoadStart(int reqCode) {
            }

            @Override
            public void onLoadFinish(int reqCode, Object result) {
                isLoading = false;
                if (result != null) {
                    String json = result.toString();
                    appStrategy = AppStrategyManager.saveStrategy(mContext, appid, json);
                    if (appStrategy != null) {
                        String sysId = appStrategy.getSystemId();
                        if (!TextUtils.isEmpty(sysId) && TextUtils.isEmpty(SDKContext.getInstance().getSysId())) {
                            SDKContext.getInstance().saveSysId(sysId);
                        }
                        MsgManager.getInstance(mContext).handleInit(appStrategy);
                        preInit(mContext, appStrategy);
                    }
                    notifyAppStrategyListener(null);
                } else {
                    CommonLogUtil.e(TAG, "app strg f!");
                }
            }

            @Override
            public void onLoadError(int reqCode, String msg, AdError errorBean) {
                isLoading = false;
                CommonLogUtil.e(TAG, "app strg f!" + msg);
                notifyAppStrategyListener(msg != null ? msg : "Request Strategy error.");
            }

            @Override
            public void onLoadCanceled(int reqCode) {
                isLoading = false;
                notifyAppStrategyListener("Request cancel");
            }
        });
    }

    /**
     * Pre-init
     */
    public void preInit(final Context context, final AppStrategy strategy) {
        if (strategy == null || TextUtils.isEmpty(strategy.getPreinitStr())) {
            return;
        }

        SDKContext.getInstance().runOnThreadPool(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONArray jsonArray = new JSONArray(strategy.getPreinitStr());
                    int length = jsonArray.length();

                    int adapterLength;
                    JSONObject jsonObject;
                    String adapterName;
                    String content;
                    Map<String, Object> serverExtrasMap;
                    JSONArray adapters;
                    for (int i = 0; i < length; i++) {
                        jsonObject = jsonArray.getJSONObject(i);

                        content = jsonObject.optString("content");
                        if (TextUtils.isEmpty(content)) {
                            continue;
                        }

                        serverExtrasMap = CommonUtil.jsonObjectToMap(content);
                        boolean isAgeLess13 = false;
                        try {
                            Map<String, Object> customMap = SDKContext.getInstance().getCustomMap();
                            if (customMap != null && customMap.containsKey(ATCustomRuleKeys.AGE)) {
                                int age = Integer.parseInt(customMap.get(ATCustomRuleKeys.AGE).toString());
                                if (age <= 13) {
                                    isAgeLess13 = true;
                                }
                            }
                        } catch (Throwable e) {

                        }
                        serverExtrasMap.put(Const.NETWORK_REQUEST_PARAMS_KEY.APP_CCPA_SWITCH_KEY, strategy.getCcpaSwitch() == 3 ? true : false);
                        serverExtrasMap.put(Const.NETWORK_REQUEST_PARAMS_KEY.APP_COPPA_SWITCH_KEY, (strategy.getCoppaSwitch() == 2 && isAgeLess13) ? true : false);

                        adapters = jsonObject.optJSONArray("adapter");

                        adapterLength = adapters.length();

                        for (int j = 0; j < adapterLength; j++) {
                            adapterName = adapters.getString(j);

                            try {
                                Class<?> aClass = Class.forName(adapterName);
                                Method getInstance = aClass.getDeclaredMethod("getInstance");
                                Object instance = getInstance.invoke(null);

                                if (instance instanceof ATInitMediation) {
                                    ((ATInitMediation) instance).initSDK(context, serverExtrasMap);
                                }
                            } catch (Throwable e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public interface AppStragtegyRequestListener {
        void onSuccess();

        void onFail(String errorMsg);
    }

}
