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
