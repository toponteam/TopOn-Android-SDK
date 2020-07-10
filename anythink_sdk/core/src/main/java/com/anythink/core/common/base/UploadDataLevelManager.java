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
import com.anythink.core.common.utils.SPUtil;
import com.anythink.core.strategy.AppStrategy;
import com.anythink.core.strategy.AppStrategyManager;

import org.json.JSONObject;

/**
 * Created by Z on 2018/4/27.
 */

public class UploadDataLevelManager {

    final int EU_DEFAULT_CODE = -100;

    Context mContext;
    private static UploadDataLevelManager sInstance;

    int mLevel = ATSDK.UNKNOWN;

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


}
