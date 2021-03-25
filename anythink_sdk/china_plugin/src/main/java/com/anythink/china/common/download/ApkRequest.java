/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.china.common.download;

import android.graphics.Bitmap;


public class ApkRequest {
    public String requestId;
    public String url;
    public String title;
    public Bitmap icon;
    public String pkgName;
    public String offerId;
    public long progress;
    public long apkSize;
    public long downloadTime;

    public String clickId; //For Network Agent ClickId

    public String uniqueID;//For download

    private volatile Status status = Status.IDLE;

    public boolean isIdle() {
        return status == Status.IDLE;
    }

    public boolean isPause() {
        return status == Status.PAUSE;
    }

    public boolean isStop() {
        return status == Status.STOP;
    }

    public boolean isLoading() {
        return status == Status.LOADING;
    }

    public void idle() {
        status = Status.IDLE;
    }

    public enum Status {
        IDLE,
        LOADING,
        PAUSE,
        STOP
    }

    public void start() {
        status = Status.LOADING;
    }

    public void stop() {
        status = Status.STOP;
    }

    public void pause() {
        status = Status.PAUSE;
    }


}
