package com.anythink.core.common.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.anythink.core.cap.AdCapV2Manager;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.entity.PlacementImpressionInfo;
import com.anythink.core.strategy.PlaceStrategy;

import org.json.JSONObject;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by Ivan on 2015/1/7.
 */
public class CommonSDKUtil {

    public static class AppStoreUtils {
        /**
         * URI format: {scheme}://{host}/{path}?{params}
         */
        public static final String PACKAGE_NAME_GOOGLE_PLAY = "com.android.vending";//

    }

    public static boolean isVailScenario(String scenario) {
        String regex = "^[A-Za-z0-9]+$";
        if (!TextUtils.isEmpty(scenario) && scenario.length() == 14) {
            boolean isMatch = Pattern.matches(regex, scenario);
            if (isMatch) {
                return true;
            } else {
                Log.e(Const.RESOURCE_HEAD, "Invail Scenario(" + scenario + "):Scenario contains some characters that are not in the [A-Za-z0-9]");
                return false;
            }

        } else {
            Log.e(Const.RESOURCE_HEAD, "Invail Scenario(" + scenario + "):Scenario'length isn't 14");
            return false;
        }
    }

    /**
     * create impression id
     */
    public static String creatImpressionId(String requestId, String adsourceId, long timeStamp) {
        return CommonMD5.getMD5(requestId + adsourceId + timeStamp);
    }


    public static String getFormatString(String adFormat) {
        switch (adFormat) {
            case Const.FORMAT.NATIVE_FORMAT:
                return Const.FORMAT_STRING.NATIVE;
            case Const.FORMAT.REWARDEDVIDEO_FORMAT:
                return Const.FORMAT_STRING.REWARDEDVIDEO;
            case Const.FORMAT.BANNER_FORMAT:
                return Const.FORMAT_STRING.BANNER;
            case Const.FORMAT.INTERSTITIAL_FORMAT:
                return Const.FORMAT_STRING.INTERSTITIAL;
            case Const.FORMAT.SPLASH_FORMAT:
                return Const.FORMAT_STRING.SPLASH;
        }
        return "";
    }

    /**
     * Create topon custom info
     *
     * @param context
     * @param requestId
     * @param placementId
     * @param format
     * @param unitGroupInfo
     * @return
     */
    public static JSONObject createRequestCustomData(Context context, String requestId, String placementId, int format, PlaceStrategy.UnitGroupInfo unitGroupInfo) {
        Map<String, PlacementImpressionInfo> impressionInfoMap = AdCapV2Manager.getInstance(context).getFormatShowTime(format);
        int formatDayShowTime = 0;
        int formatHourShowTime = 0;

        if (impressionInfoMap != null) {
            for (PlacementImpressionInfo impressionInfo : impressionInfoMap.values()) {
                formatDayShowTime += impressionInfo.dayShowCount;
                formatHourShowTime += impressionInfo.hourShowCount;
            }
        }

        PlacementImpressionInfo placementImpressionInfo = impressionInfoMap.get(placementId);

        /**TopOn Info to network**/
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("sr", "tp");
            jsonObject.put("rid", requestId);
            jsonObject.put("ads", formatDayShowTime);
            jsonObject.put("ahs", formatHourShowTime);
            jsonObject.put("pds", placementImpressionInfo != null ? placementImpressionInfo.dayShowCount : 0);
            jsonObject.put("phs", placementImpressionInfo != null ? placementImpressionInfo.hourShowCount : 0);
            jsonObject.put("ap", unitGroupInfo.getRequestLayLevel());
            jsonObject.put("tpl", placementId);

        } catch (Exception e) {

        }
        return jsonObject;
    }
}
