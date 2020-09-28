package com.anythink.banner.unitgroup.api;


/**
 * Used by Mediation
 */
public interface CustomBannerEventListener {

    void onBannerAdClicked();//Callback of Ad click

    void onBannerAdShow();////Callback of Ad impression

    void onBannerAdClose();////Callback of Ad close

}
