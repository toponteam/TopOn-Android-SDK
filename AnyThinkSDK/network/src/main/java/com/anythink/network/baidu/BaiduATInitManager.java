package com.anythink.network.baidu;

import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.ATInitMediation;
import com.baidu.mobads.utils.XAdSDKFoundationFacade;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BaiduATInitManager extends ATInitMediation {

    private static final String TAG = BaiduATInitManager.class.getSimpleName();
    private String mAppId;
    private static BaiduATInitManager sInstance;

    private BaiduATInitManager() {

    }

    public static BaiduATInitManager getInstance() {
        if (sInstance == null) {
            sInstance = new BaiduATInitManager();
        }
        return sInstance;
    }

    @Override
    public void initSDK(Context context, Map<String, Object> serviceExtras) {
        String app_id = (String) serviceExtras.get("app_id");
        if (TextUtils.isEmpty(mAppId) || !TextUtils.equals(mAppId, app_id)) {
            XAdSDKFoundationFacade.getInstance().getCommonUtils().setAppId(app_id);
            mAppId = app_id;
        }
    }

    @Override
    public String getNetworkName() {
        return "Baidu";
    }

    @Override
    public String getNetworkSDKClass() {
        return "com.baidu.mobads.utils.XAdSDKFoundationFacade";
    }

    @Override
    public List getActivityStatus() {
        ArrayList<String> list = new ArrayList<>();
        list.add("com.baidu.mobads.AppActivity");
        list.add("com.baidu.mobads.production.rewardvideo.MobRewardVideoActivity");
        return list;
    }

    @Override
    public List getProviderStatus() {
        ArrayList<String> list = new ArrayList<>();
        list.add("com.baidu.mobads.openad.FileProvider");
        return list;
    }
}
