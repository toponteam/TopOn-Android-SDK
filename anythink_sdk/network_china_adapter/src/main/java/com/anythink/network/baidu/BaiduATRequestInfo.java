package com.anythink.network.baidu;

import com.anythink.core.api.ATMediationRequestInfo;

import java.util.HashMap;
import java.util.Map;

public class BaiduATRequestInfo extends ATMediationRequestInfo {

    HashMap<String, Object> paramMap;

    public BaiduATRequestInfo(String appId, String adpPlaceId) {
        this.networkFirmId = BaiduATConst.NETWORK_FIRM_ID;
        paramMap = new HashMap<>();
        paramMap.put("app_id", appId);
        paramMap.put("ad_place_id", adpPlaceId);
    }


    @Override
    public void setFormat(String format) {
        switch (format) {
            case "4":
                className = BaiduATSplashAdapter.class.getName();
                break;
        }
    }

    @Override
    public Map<String, Object> getRequestParamMap() {
        return paramMap;
    }
}
