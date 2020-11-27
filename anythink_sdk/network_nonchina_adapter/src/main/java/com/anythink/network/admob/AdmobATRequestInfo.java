package com.anythink.network.admob;

import com.anythink.core.api.ATMediationRequestInfo;
import com.google.android.gms.ads.appopen.AppOpenAd;

import java.util.HashMap;
import java.util.Map;

public class AdmobATRequestInfo extends ATMediationRequestInfo {

    public static String ORIENTATION_PORTRAIT = String.valueOf(AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT);
    public static String ORIENTATION_LANDSCAPE = String.valueOf(AppOpenAd.APP_OPEN_AD_ORIENTATION_LANDSCAPE);

    HashMap<String, Object> paramMap;

    public AdmobATRequestInfo(String appId, String unitId, String orientation) {
        this.networkFirmId = AdmobATConst.NETWORK_FIRM_ID;
        paramMap = new HashMap<>();
        paramMap.put("app_id", appId);
        paramMap.put("unit_id", unitId);
        paramMap.put("orientation", orientation);
    }


    @Override
    public void setFormat(String format) {
        switch (format) {
            case "4":
                className = AdmobATSplashAdapter.class.getName();
                break;
        }
    }

    @Override
    public Map<String, Object> getRequestParamMap() {
        return paramMap;
    }
}
