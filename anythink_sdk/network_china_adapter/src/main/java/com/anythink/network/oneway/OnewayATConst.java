package com.anythink.network.oneway;


import mobi.oneway.export.Ad.OnewaySdk;

public class OnewayATConst {
    public static final int NETWORK_FIRM_ID = 17;

    public static String getNetworkVersion() {
        try {
            return OnewaySdk.getVersion();
        } catch (Throwable e) {

        }
        return "";
    }

}
