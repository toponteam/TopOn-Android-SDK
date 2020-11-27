/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.common.entity;

import android.text.TextUtils;

import com.anythink.core.common.base.Const;
import com.anythink.core.common.net.CommonNoticeUrlLoader;

import org.json.JSONObject;

public class BiddingResult extends BaseBiddingResult {

    public int errorCode;
    public String curreny;
    public String networkUnitId;
    public int networkFirmId;
    public long expireTime;
    public long outDateTime;

    public boolean hasSendWinUrl;

    public BiddingResult(boolean isSuccess, double price, String token, String winUrl, String lossUrl, String errorMsg) {
        super(isSuccess, price, token, winUrl, lossUrl, errorMsg);
    }

    public boolean isExpire() {
        return outDateTime < System.currentTimeMillis();
    }

    public void sendWinNotice() {
        if (hasSendWinUrl) {
            return;
        }
        hasSendWinUrl = true;
        if (!TextUtils.isEmpty(winNoticeUrl)) {
            new CommonNoticeUrlLoader(winNoticeUrl).start(0, null);
        }
    }

    public String toFileCacheString() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("bid_id", token);
            jsonObject.put("cur", curreny);
            jsonObject.put("price", price);
            jsonObject.put("nurl", winNoticeUrl);
            jsonObject.put("lurl", loseNoticeUrl);
            jsonObject.put("unit_id", networkUnitId);
            jsonObject.put("nw_firm_id", networkFirmId);
            jsonObject.put("is_success", isSuccess ? 1 : 0);
            jsonObject.put("err_code", errorCode);
            jsonObject.put("err_msg", errorMsg);
            jsonObject.put("expire", expireTime);
            jsonObject.put("out_data_time", outDateTime);
            jsonObject.put("is_send_winurl", hasSendWinUrl);
        } catch (Throwable e) {

        }

        return jsonObject.toString();

    }

    public static BiddingResult parseJSONString(String jsonString) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            boolean is_success = jsonObject.optInt("is_success") == 1;
            String bid_id = jsonObject.optString("bid_id");
            double price = jsonObject.optDouble("price");
            String winNoticeUrl = jsonObject.optString("nurl");
            String loseNoticeUrl = jsonObject.optString("lurl");
            String err_msg = jsonObject.optString("err_msg");

            BiddingResult response = new BiddingResult(is_success, price, bid_id, winNoticeUrl, loseNoticeUrl, err_msg);
            response.curreny = jsonObject.optString("cur");
            response.networkUnitId = jsonObject.optString("unit_id");
            response.networkFirmId = jsonObject.optInt("nw_firm_id");
            response.errorCode = jsonObject.optInt("err_code");
            response.expireTime = jsonObject.optLong("expire");
            response.outDateTime = jsonObject.optLong("out_data_time");
            response.hasSendWinUrl = jsonObject.optBoolean("is_send_winurl");
            return response;
        } catch (Throwable e) {
            if (Const.DEBUG) {
                e.printStackTrace();
            }
        }
        return null;

    }
}
