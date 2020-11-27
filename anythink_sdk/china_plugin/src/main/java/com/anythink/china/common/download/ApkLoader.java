/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.china.common.download;

public class ApkLoader extends ApkBaseLoader{

    public ApkLoader(ApkRequest apkRequest) {
        super(apkRequest);
    }

    @Override
    protected boolean isUseSingleThread() {
        return true;
    }
}
