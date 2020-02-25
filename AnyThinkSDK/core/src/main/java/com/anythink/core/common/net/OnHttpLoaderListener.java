package com.anythink.core.common.net;

import com.anythink.core.api.AdError;

public interface OnHttpLoaderListener {

    void onLoadStart(int reqCode);

    void onLoadFinish(int reqCode, Object result);

    void onLoadError(int reqCode, String msg, AdError errorCode);

    void onLoadCanceled(int reqCode);
}
