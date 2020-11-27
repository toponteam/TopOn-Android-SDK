/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.ks;

import com.anythink.core.api.ATMediationRequestInfo;

import java.util.HashMap;
import java.util.Map;

public class KSATRequestInfo extends ATMediationRequestInfo {

    HashMap<String, Object> paramMap;

    public KSATRequestInfo(String appId, String placementId) {
        this.networkFirmId = KSATConst.NETWORK_FIRM_ID;
        paramMap = new HashMap<>();
        paramMap.put("app_id", appId);
        paramMap.put("position_id", placementId);
    }

    @Override
    public void setFormat(String format) {
        switch (format) {
            case "4":
                className = KSATSplashAdapter.class.getName();
                break;
        }
    }

    @Override
    public Map<String, Object> getRequestParamMap() {
        return paramMap;
    }
}
