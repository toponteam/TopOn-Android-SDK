package com.anythink.network.mintegral;

import com.anythink.core.api.ATMediationRequestInfo;

import java.util.HashMap;
import java.util.Map;

public class MintegralATRequestInfo extends ATMediationRequestInfo {

    HashMap<String, Object> paramMap;

    public MintegralATRequestInfo(String appId, String appKey, String placementId, String unitId) {
        this.networkFirmId = MintegralATConst.NETWORK_FIRM_ID;
        paramMap = new HashMap<>();
        paramMap.put("appid", appId);
        paramMap.put("placement_id", placementId);
        paramMap.put("appkey", appKey);
        paramMap.put("unitid", unitId);
    }


    @Override
    public void setFormat(String format) {
        switch (format) {
            case "4":
                className = MintegralATSplashAdapter.class.getName();
                break;
        }
    }

    @Override
    public Map<String, Object> getRequestParamMap() {
        return paramMap;
    }
}
