package com.anythink.china.common.service;

public interface IApkDownloadBinder {

    void pause(String url);

    void stop(String url);

    boolean canStopSelf();

}
