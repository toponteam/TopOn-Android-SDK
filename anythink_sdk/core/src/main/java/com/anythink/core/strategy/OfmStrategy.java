/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.strategy;

import com.anythink.core.common.base.Const;
import com.anythink.core.common.utils.CommonUtil;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

//TODO
public class OfmStrategy {
    private boolean isDefault;

    String strategyId;
    private int ofmSystem;
    private int tid;
    private long availTime;

    private int tkSwitch;
    private int daSwitch;
    private String tkAddress;
    private int tkMaxCount;
    private long tkInterval;
    private Map<String, String> tkNoTFtMap;//tk key -> format string array

    public Map<String, Object> mappingSetting;

    public Map<String, Object> getSettingMap(String appIdOrPlacementId) {
        try {
            if (mappingSetting != null) {
                return CommonUtil.jsonObjectToMap(mappingSetting.get(appIdOrPlacementId).toString());
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public OfmStrategy() {
        strategyId = String.valueOf(hashCode());
    }


    public int getOfmSystem() {
        return ofmSystem;
    }

    public int getTid() {
        return tid;
    }

    public long getAvailTime() {
        return availTime;
    }

    public int getTkSwitch() {
        return tkSwitch;
    }

    public int getDaSwitch() {
        return daSwitch;
    }

    public String getTkAddress() {
        return tkAddress;
    }

    public int getTkMaxCount() {
        return tkMaxCount;
    }

    public long getTkInterval() {
        return tkInterval;
    }

    public Map<String, String> getTkNoTFtMap() {
        return tkNoTFtMap;
    }

    public String getId() {
        return strategyId;
    }

    public boolean isDefault() {
        return isDefault;
    }

    private static String ofm_logger = "ofm_logger";
    private static String ofm_tk_sw = "ofm_tk_sw";
    private static String ofm_da_sw = "ofm_da_sw";
    private static String tk_address = "tk_address";
    private static String tk_max_amount = "tk_max_amount";
    private static String tk_interval = "tk_interval";
    private static String da_rt_keys_ft = "da_rt_keys_ft";
    private static String tk_no_t_ft = "tk_no_t_ft";
    private static String da_not_keys_ft = "da_not_keys_ft";

    private static String ofm_system = "ofm_system";
    private static String ofm_tid = "ofm_tid";
    private static String ofm_firm_info = "ofm_firm_info";
    private static String ofm_st_vt = "ofm_st_vt";

    public static OfmStrategy parseOfmStrategy(String jsonStrategy) {
        try {
            OfmStrategy ofmStrategy = new OfmStrategy();
            JSONObject jsonObject = new JSONObject(jsonStrategy);

            ofmStrategy.ofmSystem = jsonObject.optInt(ofm_system);
            ofmStrategy.tid = jsonObject.optInt(ofm_tid);
            ofmStrategy.availTime = jsonObject.optLong(ofm_st_vt);
            ofmStrategy.mappingSetting = CommonUtil.jsonObjectToMap(jsonObject.optString(ofm_firm_info));

            JSONObject loggerObject = jsonObject.optJSONObject(ofm_logger);
            if (loggerObject != null) {
                ofmStrategy.tkSwitch = loggerObject.optInt(ofm_tk_sw);
                ofmStrategy.daSwitch = loggerObject.optInt(ofm_da_sw);
                ofmStrategy.tkAddress = loggerObject.optString(tk_address);
                ofmStrategy.tkMaxCount = loggerObject.optInt(tk_max_amount);
                ofmStrategy.tkInterval = loggerObject.optLong(tk_interval);

                try {
                    JSONObject object = new JSONObject(loggerObject.optString(tk_no_t_ft));

                    Iterator<String> keys = object.keys();
                    Map<String, String> map = new HashMap<>();
                    String key;
                    while (keys.hasNext()) {
                        key = keys.next();
                        map.put(key, object.optString(key));
                    }

                    ofmStrategy.tkNoTFtMap = map;
                } catch (Throwable e) {
                }
            }

            return ofmStrategy;
        } catch (Throwable e) {
            if (Const.DEBUG) {
                e.printStackTrace();
            }
        }
        return null;

    }

    public static OfmStrategy parseDefaultStrategy(String defaultConfig) {
        try {
            OfmStrategy ofmStrategy = new OfmStrategy();
            JSONObject jsonObject = new JSONObject(defaultConfig);
            ofmStrategy.isDefault = true;
            ofmStrategy.ofmSystem = jsonObject.optInt(ofm_system);
            ofmStrategy.mappingSetting = CommonUtil.jsonObjectToMap(jsonObject.optString(ofm_firm_info));

            ofmStrategy.tkSwitch = 1;
            ofmStrategy.daSwitch = 1;

            return ofmStrategy;
        } catch (Throwable e) {
            if (Const.DEBUG) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
