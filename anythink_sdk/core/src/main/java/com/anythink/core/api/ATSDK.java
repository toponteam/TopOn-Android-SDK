package com.anythink.core.api;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.anythink.core.common.OffLineTkManager;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.base.UploadDataLevelManager;
import com.anythink.core.common.utils.task.TaskManager;

import org.json.JSONObject;

import java.util.Map;
import java.util.regex.Pattern;


/**
 * SDK open api
 */

public class ATSDK {

    /**
     * GDPR LEVEL
     */
    public static final int PERSONALIZED = 0;
    public static final int NONPERSONALIZED = 1;
    public static final int UNKNOWN = 2;


    /**
     * Mark of SDK init
     */
    private static boolean HAS_INIT = false;

    private ATSDK() {

    }

    /**
     * sdk初始化
     *
     * @param context
     * @param appId
     * @param appKey
     */
    public static void init(Context context, String appId, String appKey) {
        init(context, appId, appKey, null);

    }

    /**
     * SDK init
     *
     * @param context
     * @param appId
     * @param appKey
     */
    public static void init(Context context, String appId, String appKey, ATSDKInitListener listener) {

        try {
            if (context == null) {
                if (listener != null) {
                    listener.onFail("init: Context is null!");
                }
                Log.e(Const.RESOURCE_HEAD, "init: Context is null!");
                return;
            }


            if (!HAS_INIT) {
                HAS_INIT = true;
                SDKContext.getInstance().init(context, appId, appKey);
            }

            if (listener != null) {
                listener.onSuccess();
            }

            TaskManager.getInstance().run_proxy(new Runnable() {
                @Override
                public void run() {
                    OffLineTkManager.getInstance().tryToReSendRequest();
                }
            });


        } catch (Exception ex) {
            if (Const.DEBUG) {
                ex.printStackTrace();
            }

        } catch (Error e) {

        }
    }

    /**
     * Check SDK Area
     *
     * @return
     */
    public static boolean isChinaSDK() {
        return SDKContext.getInstance().getChinaHandler() != null;
    }

    /**
     * Before initSDK
     **/
    public static void setChannel(String channel) {
        String regex = "^[A-Za-z0-9_]+$";
        if (!TextUtils.isEmpty(channel) && channel.length() <= 128) {
            boolean isMatch = Pattern.matches(regex, channel);
            if (isMatch) {
                SDKContext.getInstance().setChannel(channel);
            } else {
                Log.e(Const.RESOURCE_HEAD, "Invail Channel(" + channel + "):Channel contains some characters that are not in the [A-Za-z0-9_]");
            }

        } else {
            Log.e(Const.RESOURCE_HEAD, "Invail Channel(" + channel + "):Channel'length over 128");
        }

    }

    public static void setSubChannel(String subChannel) {
        String regex = "^[A-Za-z0-9_]+$";
        if (!TextUtils.isEmpty(subChannel) && subChannel.length() <= 128) {
            boolean isMatch = Pattern.matches(regex, subChannel);
            if (isMatch) {
                SDKContext.getInstance().setSubChannel(subChannel);
            } else {
                Log.e(Const.RESOURCE_HEAD, "Invail SubChannel(" + subChannel + "):SubChannel contains some characters that are not in the [A-Za-z0-9_]");
            }

        } else {
            Log.e(Const.RESOURCE_HEAD, "Invail SubChannel(" + subChannel + "):SubChannel'length over 128");
        }
    }

    /**
     * init custom key-value
     **/
    public static void initCustomMap(Map<String, Object> customMap) {
        SDKContext.getInstance().setAppCustomMap(customMap);
    }

    /**
     * init placement custom key-value
     */
    public static void initPlacementCustomMap(String placmentId, Map<String, Object> customMap) {
        SDKContext.getInstance().setPlacementCustomMap(placmentId, customMap);
    }

    /**
     * GDPR LEVEL Setting
     */
    public static void setGDPRUploadDataLevel(Context context, int level) {
        if (context == null) {
            Log.e(Const.RESOURCE_HEAD, "setGDPRUploadDataLevel: context should not be null");
            return;
        }

        /**Can't not set without PERSONALIZED and NONPERSONALIZED **/
        if (level == PERSONALIZED || level == NONPERSONALIZED) {
            UploadDataLevelManager.getInstance(context).setUploadDataLevel(level);
        } else {
            Log.e(Const.RESOURCE_HEAD, "GDPR level setting error!!! Level must be PERSONALIZED or NONPERSONALIZED.");
        }

    }


    @Deprecated
    public static void addNetworkGDPRInfo(Context context, int networkType, Map<String, Object> gdprInfo) {
    }

    @Deprecated
    public static Map<String, Object> getNetworkGDPRInfo(Context context, int networkType) {
        return null;
    }

    /**
     * Get GDPR LEVEL
     */
    public static int getGDPRDataLevel(Context context) {
        return UploadDataLevelManager.getInstance(context).getUploadDataLevel();
    }

    /**
     * Check current area is EU-Traffic
     */
    public static boolean isEUTraffic(Context context) {
        return UploadDataLevelManager.getInstance(context).isEUTraffic();
    }

    public static void checkIsEuTraffic(Context context, NetTrafficeCallback netTrafficeCallback) {
        UploadDataLevelManager.getInstance(context).checkIsEuTraffic(netTrafficeCallback);
    }

    /**
     * Show GDPR Activity
     *
     * @param context
     */
    public static void showGdprAuth(Context context) {
        UploadDataLevelManager.getInstance(context).showUploadDataNotifyDialog(context, null);
    }

    /**
     * Show GDPR Activity with callback
     *
     * @param context
     */
    public static void showGdprAuth(Context context, ATGDPRAuthCallback callback) {
        UploadDataLevelManager.getInstance(context).showUploadDataNotifyDialog(context, callback);
    }


    /**
     * SDK Version
     *
     * @return
     */
    public static String getSDKVersionName() {
        return Const.SDK_VERSION_NAME;
    }


    /**
     * Open Debug log switch
     */
    public static void setNetworkLogDebug(boolean debug) {
        SDKContext.getInstance().setNetworkLogDebug(debug);
    }

    public static boolean isNetworkLogDebug() {
        return SDKContext.getInstance().isNetworkLogDebug();
    }

    /**
     * Check the correctness of SDK-init
     */
    public static void integrationChecking(Context context) {
        SDKContext.getInstance().integrationChecking(context);
    }


    public static void apiLog(String placementId, String adType, String apiStr, String result, String extra) {
        if (ATSDK.isNetworkLogDebug()) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("placementId", placementId);
                jsonObject.put("adtype", adType);
                jsonObject.put("api", apiStr);
                jsonObject.put("result", result);
                jsonObject.put("reason", extra);
                Log.i(Const.RESOURCE_HEAD + "_network", jsonObject.toString());
            } catch (Throwable e) {

            }
        }
    }

}
