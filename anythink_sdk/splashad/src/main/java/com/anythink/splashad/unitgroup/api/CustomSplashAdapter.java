package com.anythink.splashad.unitgroup.api;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.common.base.AnyThinkBaseAdapter;

import java.util.Map;

public abstract class CustomSplashAdapter extends AnyThinkBaseAdapter {

    public abstract void loadSplashAd(final Activity activity
            , final ViewGroup constainer, final View skipView
            , final Map<String, Object> serverExtras
            , final ATMediationSetting mediationSetting
            , final CustomSplashListener customSplashListener);


    public abstract void clean();

    @Override
    public boolean isAdReady() {
        return false;
    }
}
