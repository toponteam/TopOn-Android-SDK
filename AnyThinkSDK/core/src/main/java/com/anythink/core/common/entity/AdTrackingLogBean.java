package com.anythink.core.common.entity;

import org.json.JSONObject;

public class AdTrackingLogBean extends LoggerInfoInterface {
    public int businessType;
    public TrackerInfo adTrackingInfo;
    public long time;

    public JSONObject toJSONObject() {
        JSONObject jsonObject = adTrackingInfo.toJSONObject(businessType);
        try {
            if (jsonObject != null) {
                jsonObject.put("sdk_time", time);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return jsonObject;
    }
}
