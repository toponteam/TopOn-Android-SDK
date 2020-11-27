/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.api;

/**
 * Created by Z on 2018/5/16.
 */

public interface ATSDKInitListener {

    public void onSuccess();

    public void onFail(String errorMsg);
}
