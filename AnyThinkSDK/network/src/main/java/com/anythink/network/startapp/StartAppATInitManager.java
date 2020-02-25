package com.anythink.network.startapp;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.ATInitMediation;
import com.startapp.android.publish.adsCommon.StartAppSDK;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StartAppATInitManager extends ATInitMediation {

    private static final String TAG = StartAppATInitManager.class.getSimpleName();
    private String mAppId;
    private static StartAppATInitManager sIntance;

    private StartAppATInitManager() {

    }

    public static StartAppATInitManager getInstance() {
        if (sIntance == null) {
            sIntance = new StartAppATInitManager();
        }
        return sIntance;
    }


    @Override
    public void initSDK(Context context, Map<String, Object> serviceExtras) {
        if (!(context instanceof Activity)) {
            return;
        }

        String appId = (String) serviceExtras.get("app_id");

        if (!TextUtils.isEmpty(appId)) {
            if (TextUtils.isEmpty(mAppId) || !TextUtils.equals(mAppId, appId)) {
                StartAppSDK.init(((Activity) context), appId, false);
                mAppId = appId;
            }
        }
    }

    @Override
    public String getNetworkName() {
        return "StartApp";
    }

    @Override
    public String getNetworkSDKClass() {
        return "com.startapp.android.publish.adsCommon.StartAppSDK";
    }

    @Override
    public List getActivityStatus() {
        ArrayList<String> list = new ArrayList<>();
        list.add("com.startapp.android.publish.ads.list3d.List3DActivity");
        list.add("com.startapp.android.publish.adsCommon.activities.OverlayActivity");
        list.add("com.startapp.android.publish.adsCommon.activities.FullScreenActivity");
        return list;
    }

    @Override
    public List getServiceStatus() {
        ArrayList<String> list = new ArrayList<>();
        list.add("com.startapp.android.publish.common.metaData.PeriodicMetaDataService");
        list.add("com.startapp.android.publish.common.metaData.InfoEventService");
        list.add("com.startapp.android.publish.common.metaData.PeriodicJobService");
        return list;
    }
}
