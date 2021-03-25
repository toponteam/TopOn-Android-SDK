/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.helium;

import android.content.Context;

import com.anythink.core.api.ATInitMediation;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.utils.task.TaskManager;
import com.chartboost.heliumsdk.HeliumSdk;
import com.chartboost.heliumsdk.ad.HeliumAd;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HeliumATInitManager extends ATInitMediation {
    private static HeliumATInitManager sInstance;

    boolean isStartingInit = false;
    boolean isInitSuccess;
    ConcurrentHashMap<String, HeliumAd> mBidAdObject = new ConcurrentHashMap<>();

    private HeliumATInitManager() {
        isInitSuccess = false;
    }

    public synchronized static HeliumATInitManager getInstance() {
        if (sInstance == null) {
            sInstance = new HeliumATInitManager();
        }
        return sInstance;
    }

    protected void putBidAdObject(String bidId, HeliumAd adObject) {
        mBidAdObject.put(bidId, adObject);
    }

    protected void removeBidAdObject(String bidId) {
        mBidAdObject.remove(bidId);
    }

    protected HeliumAd getBidAdObject(String bidId) {
        return mBidAdObject.get(bidId);
    }

    @Override
    public void initSDK(Context context, Map<String, Object> serviceExtras) {
        initSDK(context, serviceExtras, null);
    }

    public void initSDK(final Context context, final Map<String, Object> serviceExtras, final InitCallback initCallback) {
        TaskManager.getInstance().run_proxy(new Runnable() {
            @Override
            public void run() {
                synchronized (HeliumATInitManager.this) {
                    try {
                        if (isStartingInit) {
                            lockInit();
                        }

                        if (isInitSuccess) {
                            if (initCallback != null) {
                                initCallback.initSuccess();
                            }
                            return;
                        }

                        isStartingInit = true;

                        String appId = serviceExtras.get("app_id").toString();
                        String appSignature = serviceExtras.get("app_signature").toString();

                        try {
                            boolean ccpaSwitch = (boolean) serviceExtras.get(Const.NETWORK_REQUEST_PARAMS_KEY.APP_CCPA_SWITCH_KEY);
                            if (ccpaSwitch) {
                                HeliumSdk.setCCPAConsent(false);
                            }
                        } catch (Throwable e) {

                        }

                        try {
                            boolean coppaSwitch = (boolean) serviceExtras.get(Const.NETWORK_REQUEST_PARAMS_KEY.APP_COPPA_SWITCH_KEY);
                            if (coppaSwitch) {
                                HeliumSdk.setSubjectToCoppa(true);
                            }
                        } catch (Throwable e) {

                        }

                        HeliumSdk.start(context, appId, appSignature, new HeliumSdk.HeliumSdkListener() {

                            @Override
                            public void didInitialize(Error error) {
                                isStartingInit = false;
                                if (error == null) {
                                    isInitSuccess = true;
                                } else {
                                    isInitSuccess = false;
                                }
                                try {
                                    initNotifyAll();
                                } catch (Exception e) {

                                }

                                if (error == null) {
                                    if (initCallback != null) {
                                        initCallback.initSuccess();
                                    }
                                } else {
                                    if (initCallback != null) {
                                        initCallback.initError("", error.getMessage());
                                    }
                                }
                            }
                        });
                    } catch (Exception e) {
                        if (initCallback != null) {
                            initCallback.initError("", e.getMessage());
                        }
                    }
                }
            }
        });

    }

    private void lockInit() throws Exception {
        synchronized (this) {
            wait();
        }
    }

    private void initNotifyAll() throws Exception {
        synchronized (this) {
            notifyAll();
        }
    }

    @Override
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        HeliumSdk.setUserHasGivenConsent(!isConsent);
        return true;
    }

    public String getNetworkName() {
        return "Helium";
    }

    public String getNetworkSDKClass() {
        return "com.chartboost.heliumsdk.HeliumSdk";
    }

    public String getNetworkVersion() {
        return HeliumSdk.getVersion();
    }


    public interface InitCallback {
        void initSuccess();

        void initError(String code, String msg);
    }

}
