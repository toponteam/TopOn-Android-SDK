/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

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
