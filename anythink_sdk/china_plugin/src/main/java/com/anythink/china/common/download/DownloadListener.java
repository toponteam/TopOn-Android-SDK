package com.anythink.china.common.download;

public interface DownloadListener {

    void onSuccess(String url);

    void onProgress(int progress);

    void onFailed(String url);
}
