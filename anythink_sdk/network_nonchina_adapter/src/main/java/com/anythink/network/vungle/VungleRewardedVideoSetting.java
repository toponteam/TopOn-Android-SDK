package com.anythink.network.vungle;

import com.anythink.core.api.ATMediationSetting;

@Deprecated
public class VungleRewardedVideoSetting implements ATMediationSetting {

    int orientation = 2 ; // 1: Automatic rotation based on device orientation  2:Video ads play in the best orientation
    boolean isSoundEnable = true;
    boolean isBackButtonImmediatelyEnable = false; //If true, the user can exit the ad immediately using the back button. If false, the user cannot use the back button to exit the ad until the close button on the screen is displayed。

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public boolean isSoundEnable() {
        return isSoundEnable;
    }

    public void setSoundEnable(boolean soundEnable) {
        isSoundEnable = soundEnable;
    }

    public boolean isBackButtonImmediatelyEnable() {
        return isBackButtonImmediatelyEnable;
    }

    public void setBackButtonImmediatelyEnable(boolean backButtonImmediatelyEnable) {
        isBackButtonImmediatelyEnable = backButtonImmediatelyEnable;
    }

    @Override
    public int getNetworkType() {
        return VungleATConst.NETWORK_FIRM_ID;
    }
}
