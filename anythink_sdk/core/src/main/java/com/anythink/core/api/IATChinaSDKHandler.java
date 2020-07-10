package com.anythink.core.api;

import android.content.Context;

import com.anythink.core.strategy.AppStrategy;

import org.json.JSONObject;

public interface IATChinaSDKHandler {
    void initDeviceInfo(Context context);

    void fillRequestData(JSONObject jsonObject, AppStrategy appStrategy);
}
