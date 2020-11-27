/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.baidu;

import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.ATInitMediation;
import com.baidu.mobads.AdView;
import com.baidu.mobads.interfaces.utils.IXAdCommonUtils;
import com.baidu.mobads.utils.XAdSDKFoundationFacade;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BaiduATInitManager extends ATInitMediation {

    private String mAppId;
    private static BaiduATInitManager sInstance;

    private BaiduATInitManager() {

    }

    public synchronized static BaiduATInitManager getInstance() {
        if (sInstance == null) {
            sInstance = new BaiduATInitManager();
        }
        return sInstance;
    }

    @Override
    public synchronized void initSDK(Context context, Map<String, Object> serviceExtras) {
        initSDK(context, serviceExtras, null);
    }

    public synchronized void initSDK(Context context, Map<String, Object> serviceExtras, InitCallback callback) {
        String app_id = (String) serviceExtras.get("app_id");
        if (TextUtils.isEmpty(mAppId) || !TextUtils.equals(mAppId, app_id)) {
            try {
//                Class<?> aClass = Class.forName("com.baidu.mobads.utils.XAdSDKFoundationFacade");
//                XAdSDKFoundationFacade instance = XAdSDKFoundationFacade.getInstance();
//
//                Method getCommonUtils = aClass.getDeclaredMethod("getCommonUtils");
//                Object object = getCommonUtils.invoke(instance);
//
//                if (object instanceof IXAdCommonUtils) {
//                    ((IXAdCommonUtils) object).setAppId(app_id);
//                    mAppId = app_id;
//                } else {
//                    if (callback != null) {
//                        callback.onError(new NoSuchMethodError("No method getCommonUtils()"));
//                    }
//                    return;
//                }

                AdView.setAppSid(context, app_id);
                mAppId = app_id;
            } catch (Throwable e) {
                e.printStackTrace();
                if (callback != null) {
                    callback.onError(e);
                }
                return;
            }
        }
        if (callback != null) {
            callback.onSuccess();
        }
    }


    public interface InitCallback {
        void onSuccess();

        void onError(Throwable e);
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
        list.add("com.baidu.mobads.openad.BdFileProvider");
        return list;
    }
}
