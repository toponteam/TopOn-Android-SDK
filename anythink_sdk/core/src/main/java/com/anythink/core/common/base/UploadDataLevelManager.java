/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.common.base;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.anythink.core.activity.AnyThinkGdprAuthActivity;
import com.anythink.core.api.ATGDPRAuthCallback;
import com.anythink.core.api.ATSDK;
import com.anythink.core.api.AdError;
import com.anythink.core.api.NetTrafficeCallback;
import com.anythink.core.common.net.NetTrafficCheckLoader;
import com.anythink.core.common.net.OnHttpLoaderListener;
import com.anythink.core.common.track.AgentEventManager;
import com.anythink.core.common.utils.SPUtil;
import com.anythink.core.common.utils.task.TaskManager;
import com.anythink.core.strategy.AppStrategy;
import com.anythink.core.strategy.AppStrategyManager;

import org.json.JSONObject;

import java.util.concurrent.ConcurrentHashMap;


public class UploadDataLevelManager {

    final int EU_DEFAULT_CODE = -100;

    Context mContext;
    private static UploadDataLevelManager sInstance;

    int mLevel = ATSDK.UNKNOWN;

    private ConcurrentHashMap<Integer, Boolean> networkGDPRSettingStatus = new ConcurrentHashMap<>(5);

    private UploadDataLevelManager(Context context) {
        if (context != null) {
            mContext = context.getApplicationContext();
        }
        mLevel = SPUtil.getInt(mContext, Const.SPU_NAME, Const.SPUKEY.SPU_UPLOAD_DATA_LEVEL, ATSDK.UNKNOWN);

    }

    public synchronized static UploadDataLevelManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new UploadDataLevelManager(context);
        }
        return sInstance;
    }

    public void setUploadDataLevel(int level) {
        mLevel = level;
        SPUtil.putInt(mContext, Const.SPU_NAME, Const.SPUKEY.SPU_UPLOAD_DATA_LEVEL, level);
    }

    //Return the level User set
    public int getUploadDataLevel() {
        return mLevel;
    }

    /**
     * Switch of Anythink's DeviceInfo
     *
     * @return
     */
    public boolean canUpLoadDeviceData() {
        AppStrategy appStrategy = AppStrategyManager.getInstance(mContext).getAppStrategyByAppId(SDKContext.getInstance().getAppId());

        /**If Appseting doesn't exist, return result by user's setting**/
        if (appStrategy == null || appStrategy.isLocalStrategy()) {
            if (mLevel == ATSDK.NONPERSONALIZED) {
                return false;
            } else {
                return true;
            }
        }

        //Not EU-traffic
        if (appStrategy.getGdprIa() == 0) {
            return true;
        } else {
            /**EU-traffic**/
            int level = mLevel;

            /**Return result by AppSetting**/
            if (appStrategy.getGdprSo() == 1) {
                level = appStrategy.getGdprSdcs();
            }

            if (level == ATSDK.PERSONALIZED) {
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * Switch of Mediation's DeviceInfo
     *
     * @return
     */
    public boolean isNetworkGDPRConsent() {
        AppStrategy appStrategy = AppStrategyManager.getInstance(mContext).getAppStrategyByAppId(SDKContext.getInstance().getAppId());

        /**If Appseting doesn't exist, return result by user's setting**/
        if (appStrategy == null || appStrategy.isLocalStrategy()) {
            if (mLevel == ATSDK.NONPERSONALIZED) {
                return false;
            } else {
                return true;
            }
        }

        if (mLevel == ATSDK.UNKNOWN) {
            /**Return result by AppSetting**/
            if (appStrategy.getGdprIa() == 0) {
                return true;
            }
            return false;
        }

        if (appStrategy.getGdprSo() == 1) {
            /**Return result by AppSetting**/
            int level = appStrategy.getGdprSdcs();
            if (level == ATSDK.PERSONALIZED) {
                return true;
            } else {
                return false;
            }
        } else {
            if (mLevel == ATSDK.PERSONALIZED) {
                return true;
            }

            if (appStrategy.getGdprIa() == 0) {
                return true;
            } else {
                return false;
            }

        }
    }

    /**
     * Check current area is EU-Traffic.(If Appsetting doesn't exist, reutnr false)
     *
     * @return
     */
    public boolean isEUTraffic() {
        int eu_info = SPUtil.getInt(mContext, Const.SPU_NAME, Const.SPUKEY.SPU_EU_INFO, EU_DEFAULT_CODE);
        return eu_info == 1;
    }


    public void showUploadDataNotifyDialog(final Context context, final ATGDPRAuthCallback callback) {
        SDKContext.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                AnyThinkGdprAuthActivity.mCallback = callback;
                Intent intent = new Intent(context, AnyThinkGdprAuthActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
    }

    /**
     * check the eu-traffic
     *
     * @param callback
     */
    public void checkIsEuTraffic(final NetTrafficeCallback callback) {
        int eu_info = SPUtil.getInt(mContext, Const.SPU_NAME, Const.SPUKEY.SPU_EU_INFO, EU_DEFAULT_CODE);
        if (eu_info == -100) {
            new NetTrafficCheckLoader().start(0, new OnHttpLoaderListener() {
                @Override
                public void onLoadStart(int reqCode) {
                }

                @Override
                public void onLoadFinish(int reqCode, Object result) {
                    try {
                        if (result == null) {
                            if (callback != null) {
                                callback.onErrorCallback("There is no result.");
                            }

                            return;
                        }

                        JSONObject resultObject = (JSONObject) result;

                        if (!resultObject.has("is_eu")) {
                            if (callback != null) {
                                callback.onErrorCallback("There is no result.");
                            }
                            return;
                        }

                        int isEU = ((JSONObject) result).optInt("is_eu");
//                        SPUtil.putInt(mContext, Const.SPU_NAME, Const.SPUKEY.SPU_EU_INFO, isEU);
                        if (isEU == 1) {
                            if (callback != null) {
                                callback.onResultCallback(true);
                            }
                        } else {
                            if (callback != null) {
                                callback.onResultCallback(false);
                            }
                        }
                    } catch (Throwable e) {
                        if (callback != null) {
                            callback.onErrorCallback("Internal error");
                        }
                    }

                }

                @Override
                public void onLoadError(int reqCode, String msg, AdError errorCode) {
                    if (callback != null) {
                        callback.onErrorCallback(errorCode.printStackTrace());
                    }
                }

                @Override
                public void onLoadCanceled(int reqCode) {
                }
            });
        } else {
            if (eu_info == 1) {
                if (callback != null) {
                    callback.onResultCallback(true);
                }
            } else {
                if (callback != null) {
                    callback.onResultCallback(false);
                }
            }
        }
    }

    public void logGDPRSetting(final int networkFirmId) {
        TaskManager.getInstance().run_proxy(new Runnable() {
            @Override
            public void run() {
                if (!hasSetGDPR(networkFirmId)) {
                    UploadDataLevelManager levelManager = UploadDataLevelManager.getInstance(SDKContext.getInstance().getContext());

                    AppStrategy appStrategy = AppStrategyManager.getInstance(SDKContext.getInstance().getContext()).getAppStrategyByAppId(SDKContext.getInstance().getAppId());

                    //DataConcent=Unknown，gdpr_ia=true，set to NonPersonalized
                    if (levelManager.getUploadDataLevel() == ATSDK.UNKNOWN && appStrategy.getGdprIa() == 1 && appStrategy.getUseNetworkDefaultGDPR() == 0) {
                        AgentEventManager.appSettingGDPRUpdate(1, levelManager.getUploadDataLevel(), appStrategy.getGdprIa(), networkFirmId);
                    }

                    //DataConcent=Nonpersonalized，gdpr_ia=false，gdpr_so=0
                    if (levelManager.getUploadDataLevel() == ATSDK.NONPERSONALIZED && appStrategy.getGdprSo() == 0 && appStrategy.getGdprIa() == 0) {
                        AgentEventManager.appSettingGDPRUpdate(2, levelManager.getUploadDataLevel(), appStrategy.getGdprIa(), networkFirmId);
                    }
                    networkGDPRSettingStatus.put(networkFirmId, true);
                }
            }
        });

    }

    public boolean hasSetGDPR(int networkFirmId) {
        if (networkGDPRSettingStatus.get(networkFirmId) == null || !networkGDPRSettingStatus.get(networkFirmId)) {
            return false;
        }
        return true;
    }


}
