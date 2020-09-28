package com.anythink.network.toutiao;

import com.anythink.core.api.ATMediationRequestInfo;

import java.util.HashMap;
import java.util.Map;

public class TTATRequestInfo extends ATMediationRequestInfo {

    HashMap<String, Object> paramMap;

    public TTATRequestInfo(String appId, String slotId, boolean isTemplate) {
        this.networkFirmId = TTATConst.NETWORK_FIRM_ID;
        paramMap = new HashMap<>();
        paramMap.put("app_id", appId);
        paramMap.put("slot_id", slotId);
        paramMap.put("personalized_template", isTemplate ? "1" : "0");
    }


    @Override
    public void setFormat(String format) {
        switch (format) {
            case "4":
                className = TTATSplashAdapter.class.getName();
                break;
        }
    }

    @Override
    public Map<String, Object> getRequestParamMap() {
        return paramMap;
    }
}
