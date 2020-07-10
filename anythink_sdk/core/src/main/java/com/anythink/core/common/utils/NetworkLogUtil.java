package com.anythink.core.common.utils;

import android.text.TextUtils;
import android.util.Log;

import com.anythink.core.api.ATSDK;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.entity.AdTrackingInfo;
import com.anythink.core.strategy.PlaceStrategy;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NetworkLogUtil {


    public static void adsourceLog(String placementId, AdTrackingInfo adTrackingInfo, String reason, PlaceStrategy.UnitGroupInfo unitGroupInfo,
                                   int hourlyFrequency, int dailyFrequency) {
        if (ATSDK.isNetworkLogDebug()) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("reason", reason);
                jsonObject.put("placementId", placementId);
                jsonObject.put("adtype", adTrackingInfo.getAdTypeString());
                jsonObject.put("networkFirmId", unitGroupInfo.networkType);
                jsonObject.put("content", adTrackingInfo.getmNetworkContent());
                jsonObject.put("hourly_frequency", hourlyFrequency);
                jsonObject.put("hourly_limit", unitGroupInfo.capsByHour);
                jsonObject.put("daily_frequency", dailyFrequency);
                jsonObject.put("daily_limit", unitGroupInfo.capsByDay);
                jsonObject.put("pacing_limit", unitGroupInfo.pacing);
                jsonObject.put("request_fail_interval", unitGroupInfo.requetFailInterval);
                printJson(Const.RESOURCE_HEAD + "_network", jsonObject.toString(), true);

            } catch (Throwable e) {

            }
        }
    }


    public static void headbidingLog(String result, String placementId, String formatStr, PlaceStrategy.UnitGroupInfo unitGroupInfo) {
        if (ATSDK.isNetworkLogDebug()) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("action", Const.LOGKEY.HEADBIDDING);
                jsonObject.put("result", result);
                jsonObject.put("placementId", placementId);
                jsonObject.put("adtype", formatStr);
                jsonObject.put("networkFirmId", unitGroupInfo.networkType);
                jsonObject.put("content", unitGroupInfo.content);
                jsonObject.put("bidPrice", unitGroupInfo.ecpm);
                jsonObject.put("msg", unitGroupInfo.errorMsg);
                printJson(Const.RESOURCE_HEAD + "_network", jsonObject.toString(), TextUtils.equals(Const.LOGKEY.FAIL, result));

            } catch (Throwable e) {

            }
        }
    }

    public static void strategyLog(String result, String placementId, String formatStr, String errorMsg) {
        if (ATSDK.isNetworkLogDebug()) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("action", Const.LOGKEY.STRATEGY);
                jsonObject.put("result", result);
                jsonObject.put("placementId", placementId);
                jsonObject.put("adtype", formatStr);
                jsonObject.put("errorMsg", errorMsg);
                printJson(Const.RESOURCE_HEAD + "_network", jsonObject.toString(), TextUtils.equals(Const.LOGKEY.FAIL, result));

            } catch (Throwable e) {

            }
        }
    }



    /**
     * Formatting the JSON String
     *
     * @param tag
     * @param msg
     */
    public static void printJson(String tag, String msg, boolean error) {
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

        if (error) {
            Log.e(tag, " \n" + jsonPrint);
        } else {
            Log.i(tag, " \n" + jsonPrint);
        }
    }

    public void printJson(String tag, String msg) {
        printJson(tag, msg, false);
    }

}
