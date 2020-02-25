package com.anythink.banner.unitgroup.api;

import android.content.Context;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.banner.api.ATBannerView;
import com.anythink.banner.business.BaseBannerAdapter;

import java.util.Map;

public abstract class CustomBannerAdapter extends BaseBannerAdapter {

    public abstract void loadBannerAd(final ATBannerView bannerView, final Context activity
            , final Map<String, Object> serverExtras
            , final ATMediationSetting mediationSetting
            , final CustomBannerListener customBannerListener);


    @Override
    public boolean isAdReady() {
        return getBannerView() != null;
    }
}
