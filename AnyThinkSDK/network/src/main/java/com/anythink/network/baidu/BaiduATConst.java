package com.anythink.network.baidu;

import com.baidu.mobads.utils.XAdSDKFoundationFacade;

public class BaiduATConst {
    public static final int NETWORK_FIRM_ID = 22;

    public static String getNetworkVersion() {
        try {
            return XAdSDKFoundationFacade.getInstance().getProxyVer();
        } catch (Throwable e) {

        }
        return "";
    }
}
