package com.anythink.network.gdt;

import com.anythink.core.api.ATMediationRequestInfo;

import java.util.HashMap;
import java.util.Map;

public class GDTATRequestInfo extends ATMediationRequestInfo {

    HashMap<String, Object> paramMap;

    public GDTATRequestInfo(String appId, String unitId) {
        this.networkFirmId = GDTATConst.NETWORK_FIRM_ID;
        paramMap = new HashMap<>();
        paramMap.put("app_id", appId);
        paramMap.put("unit_id", unitId);
    }


    @Override
    public void setFormat(String format) {
        switch (format) {
            case "4":
                className = GDTATSplashAdapter.class.getName();
                break;
        }
    }

    @Override
    public Map<String, Object> getRequestParamMap() {
        return paramMap;
    }
}
