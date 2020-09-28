package com.anythink.network.mopub;

import android.location.Location;
import android.text.TextUtils;

import com.anythink.core.api.ATMediationSetting;
import com.mopub.mobileads.MoPubRewardedVideoManager;

/**
 * Created by zhou on 2018/6/27.
 */

public class MopubRewardedVideoSetting implements ATMediationSetting {

    private String requestParameters_keywords;
    private String requestParameters_userDataKeywords;
    private Location requestParameters_location;

    @Override
    public int getNetworkType() {
        return MopubATConst.NETWORK_FIRM_ID;
    }


    public String getRequestParameters_keywords() {
        return requestParameters_keywords;
    }

    public void setRequestParameters_keywords(String pRequestParameters_keywords) {
        requestParameters_keywords = pRequestParameters_keywords;
    }

    public String getRequestParameters_userDataKeywords() {
        return requestParameters_userDataKeywords;
    }

    public void setRequestParameters_userDataKeywords(String pRequestParameters_userDataKeywords) {
        requestParameters_userDataKeywords = pRequestParameters_userDataKeywords;
    }

    public Location getRequestParameters_location() {
        return requestParameters_location;
    }

    public void setRequestParameters_location(Location pRequestParameters_location) {
        requestParameters_location = pRequestParameters_location;
    }



}
