package com.anythink.network.toutiao;

import com.anythink.core.api.ATMediationSetting;

/**
 * Created by zhou on 2018/6/27.
 */

public class TTRewardedVideoSetting implements ATMediationSetting {


    private boolean supportDeepLink = true; //Default is true
    private String rewardName;
    private int rewardCount;
    private int orientation; //1:Vertial 2:Horizontal
    private boolean isRequirePermission = false; // Whether to apply for permission

    public void setSupportDeepLink(boolean supportDeepLink) {
        this.supportDeepLink = supportDeepLink;
    }

    public void setRewardName(String rewardName) {
        this.rewardName = rewardName;
    }

    public void setRewardAmount(int rewardCount) {
        this.rewardCount = rewardCount;
    }

    public void setVideoOrientation(int orientation) {
        this.orientation = orientation;
    }

    public boolean getSoupportDeepLink() {
        return supportDeepLink;
    }

    public String getRewardName() {
        return rewardName;
    }

    public int getRewardCount() {
        return rewardCount;
    }

    public int getVideoOrientation() {
        return orientation;
    }

    public boolean isRequirePermission() {
        return isRequirePermission;
    }

    public void setRequirePermission(boolean requirePermission) {
        isRequirePermission = requirePermission;
    }


    @Override
    public int getNetworkType() {
        return TTATConst.NETWORK_FIRM_ID;
    }

}
