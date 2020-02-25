package com.anythink.network.luomi;

import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.ATInitMediation;
import com.anythink.core.api.ATSDK;
import com.hz.yl.b.McLogUtil;
import com.hz.yl.b.mian.XMain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LuomiATInitManager extends ATInitMediation {

    private static final String TAG = LuomiATInitManager.class.getSimpleName();
    private String mAppKey;
    private static LuomiATInitManager sIntance;

    private LuomiATInitManager() {

    }

    public static LuomiATInitManager getInstance() {
        if (sIntance == null) {
            sIntance = new LuomiATInitManager();
        }
        return sIntance;
    }

    @Override
    public void initSDK(Context context, Map<String, Object> serviceExtras) {
        String appKey = serviceExtras.get("app_key").toString();
        if (!TextUtils.isEmpty(appKey)) {
            if (TextUtils.isEmpty(mAppKey) || !TextUtils.equals(mAppKey, appKey)) {
                XMain.getInstance().setAppKey(context.getApplicationContext(), appKey);
                McLogUtil.setENABLE_LOGCAT(ATSDK.NETWORK_LOG_DEBUG);
                mAppKey = appKey;
            }
        }
    }

    @Override
    public String getNetworkName() {
        return "Luomi";
    }

    @Override
    public String getNetworkSDKClass() {
        return "com.hz.yl.b.mian.XMain";
    }

    @Override
    public List getActivityStatus() {
        ArrayList<String> list = new ArrayList<>();
        list.add("com.hz.yl.b.HHActivity");
        list.add("com.hz.yl.b.HHVideoActivity");
        return list;
    }

    @Override
    public List getProviderStatus() {
        ArrayList<String> list = new ArrayList<>();
        list.add("com.anythink.network.luomi.LuomiATFileProvider");
        return list;
    }
}
