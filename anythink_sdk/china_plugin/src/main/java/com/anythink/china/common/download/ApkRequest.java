package com.anythink.china.common.download;

import android.graphics.Bitmap;

import java.util.concurrent.atomic.AtomicBoolean;

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
