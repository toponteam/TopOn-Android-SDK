package com.anythink.core.api;

public interface NetTrafficeCallback {
    void onResultCallback(boolean isEU);
    void onErrorCallback(String errorMsg);
}
