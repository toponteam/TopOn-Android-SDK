package com.anythink.core.api;

/**
 * Created by Z on 2018/5/16.
 */

public interface ATSDKInitListener {

    public void onSuccess();

    public void onFail(String errorMsg);
}
