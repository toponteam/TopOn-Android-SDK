package com.anythink.network.adcolony;

import com.anythink.core.api.ATMediationSetting;

@Deprecated
public class AdColonyRewardedVideoSetting implements ATMediationSetting {

    boolean enableConfirmationDialog;
    boolean enableResultsDialog;

    public boolean isEnableConfirmationDialog() {
        return enableConfirmationDialog;
    }

    public void setEnableConfirmationDialog(boolean enableConfirmationDialog) {
        this.enableConfirmationDialog = enableConfirmationDialog;
    }

    public boolean isEnableResultsDialog() {
        return enableResultsDialog;
    }

    public void setEnableResultsDialog(boolean enableResultsDialog) {
        this.enableResultsDialog = enableResultsDialog;
    }

    @Override
    public int getNetworkType() {
        return AdColonyATConst.NETWORK_FIRM_ID;
    }
}
