/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.common.entity;

import com.anythink.core.common.base.Const;

import org.json.JSONObject;

public class AdLoadCapBean {
    public int number;
    public long loadTime;


    public void readCache(String cacheInfo) {
        try {
            JSONObject jsonObject = new JSONObject(cacheInfo);
            number = jsonObject.optInt("number");
            loadTime = jsonObject.optLong("loadTime");
        } catch (Exception e) {
            if (Const.DEBUG) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String toString() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("number", number);
            jsonObject.put("loadTime", loadTime);
        } catch (Exception e) {
            if (Const.DEBUG) {
                e.printStackTrace();
            }
        }
        return jsonObject.toString();
    }
}
