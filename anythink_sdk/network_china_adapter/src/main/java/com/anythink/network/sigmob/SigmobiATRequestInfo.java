package com.anythink.network.sigmob;

import com.anythink.core.api.ATMediationRequestInfo;
import com.sigmob.windad.Adapter.SigmobSplashAdAdapter;

import java.util.HashMap;
import java.util.Map;

public class SigmobiATRequestInfo extends ATMediationRequestInfo {

    HashMap<String, Object> paramMap;

    public SigmobiATRequestInfo(String appId, String appKey, String placementId) {
        this.networkFirmId = SigmobATConst.NETWORK_FIRM_ID;
        paramMap = new HashMap<>();
        paramMap.put("app_id", appId);
        paramMap.put("app_key", appKey);
        paramMap.put("placement_id", placementId);
    }


    @Override
    public void setFormat(String format) {
        switch (format) {
            case "4":
                className = SigmobATSplashAdapter.class.getName();
                break;
        }
    }

    @Override
    public Map<String, Object> getRequestParamMap() {
        return paramMap;
    }
}
