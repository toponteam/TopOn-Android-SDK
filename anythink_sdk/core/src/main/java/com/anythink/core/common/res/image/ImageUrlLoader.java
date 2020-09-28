package com.anythink.core.common.res.image;

import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.res.ResourceDiskCacheManager;
import com.anythink.core.common.res.ResourceEntry;
import com.anythink.core.common.utils.FileUtil;
import com.anythink.core.common.utils.task.TaskManager;
import com.anythink.core.common.utils.task.Worker;

import java.io.InputStream;
import java.util.Map;

public class ImageUrlLoader extends ResourceDownloadBaseUrlLoader {
    ResourceEntry entry;
    HttpLoadListener listener;

    public ImageUrlLoader(ResourceEntry entry) {
        super(entry.resourceUrl);
        this.entry = entry;
    }

    public void setListener(HttpLoadListener listener) {
        this.listener = listener;
    }

    @Override
    protected Map<String, String> onPrepareHeaders() {
        return null;
    }

    @Override
    protected void onErrorAgent(String mURL, String msg) {

    }

    @Override
    protected boolean saveHttpResource(InputStream inputStream) {
        return ResourceDiskCacheManager.getInstance(SDKContext.getInstance().getContext()).saveNetworkInputStreamToFile(entry.resourceType, FileUtil.hashKeyForDisk(entry.resourceUrl), inputStream);
    }

    @Override
    protected void startWorker(Worker worker) {
        TaskManager.getInstance().run(worker, TaskManager.TYPE_IMAGE_TYPE);
    }

    @Override
    protected void onLoadFinishCallback() {
        if (listener != null) {
            listener.onLoadSuccess(entry);
        }
    }

    @Override
    protected void onLoadFailedCallback(String errorCode, String errorMsg) {
        if (listener != null) {
            listener.onLoadFail(entry, errorMsg);
        }
    }


    public interface HttpLoadListener {
        void onLoadSuccess(ResourceEntry entry);

        void onLoadFail(ResourceEntry entry, String errorMsg);
    }
}
