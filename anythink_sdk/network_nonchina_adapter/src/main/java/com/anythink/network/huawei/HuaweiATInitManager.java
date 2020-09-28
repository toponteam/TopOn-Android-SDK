package com.anythink.network.huawei;

import android.content.Context;

import com.anythink.core.api.ATInitMediation;
import com.huawei.hms.ads.HwAds;
import com.huawei.hms.ads.RequestOptions;

import java.util.Map;

import static com.huawei.hms.ads.NonPersonalizedAd.ALLOW_ALL;
import static com.huawei.hms.ads.TagForChild.TAG_FOR_CHILD_PROTECTION_TRUE;
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
        if (isInit) {
            return;
        }
        HwAds.init(context.getApplicationContext());

        isInit = true;
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

    public String getNetworkSDKVersion() {
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
}
