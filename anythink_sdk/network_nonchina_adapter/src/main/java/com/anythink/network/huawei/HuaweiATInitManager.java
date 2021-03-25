/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.huawei;

import android.content.Context;

import com.anythink.core.api.ATInitMediation;
import com.anythink.core.common.base.Const;
import com.huawei.hms.ads.HwAds;
import com.huawei.hms.ads.RequestOptions;
import com.huawei.hms.ads.TagForChild;

import java.util.Map;

import static com.huawei.hms.ads.NonPersonalizedAd.ALLOW_ALL;
import static com.huawei.hms.ads.UnderAge.PROMISE_FALSE;
import static com.huawei.hms.ads.UnderAge.PROMISE_TRUE;

public class HuaweiATInitManager extends ATInitMediation {
    boolean isInit = false;
    private static HuaweiATInitManager sInstance;

    private HuaweiATInitManager() {

    }

    public synchronized static HuaweiATInitManager getInstance() {
        if (sInstance == null) {
            sInstance = new HuaweiATInitManager();
        }
        return sInstance;
    }

    @Override
    public void initSDK(Context context, Map<String, Object> serviceExtras) {
        this.initSDK(context, serviceExtras, null);
    }

    public void initSDK(Context context, Map<String, Object> serviceExtras, InitListener listener) {
        if (isInit) {
            if (listener != null) {
                listener.onSuccess();
            }
            return;
        }

        try {
            boolean coppaSwitch = (boolean) serviceExtras.get(Const.NETWORK_REQUEST_PARAMS_KEY.APP_COPPA_SWITCH_KEY);
            if (coppaSwitch) {
                RequestOptions requestOptions = HwAds.getRequestOptions().toBuilder().setTagForChildProtection(TagForChild.TAG_FOR_CHILD_PROTECTION_TRUE).build();
                HwAds.setRequestOptions(requestOptions);
            }
        } catch (Throwable e) {

        }

        HwAds.init(context.getApplicationContext());

        isInit = true;

        if (listener != null) {
            listener.onSuccess();
        }

    }

    @Override
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        // Add the child-directed setting.
        RequestOptions requestOptions = HwAds.getRequestOptions().toBuilder().setTagForUnderAgeOfPromise(isConsent ? PROMISE_FALSE : PROMISE_TRUE).
                setNonPersonalizedAd(ALLOW_ALL).build();
        HwAds.setRequestOptions(requestOptions);
        return true;
    }

    @Override
    public String getNetworkName() {
        return "Huawei(HMS)";
    }

    public String getNetworkVersion() {
        try {
            return HwAds.getSDKVersion();
        } catch (Throwable e) {

        }
        return "";

    }

    @Override
    public String getNetworkSDKClass() {
        try {
            return HwAds.class.getName();
        } catch (Throwable e) {

        }
        return "";

    }

    public interface InitListener {
        void onSuccess();
    }
}
