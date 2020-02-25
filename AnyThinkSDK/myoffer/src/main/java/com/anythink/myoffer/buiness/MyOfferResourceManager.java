package com.anythink.myoffer.buiness;


import android.text.TextUtils;
import android.util.Log;

import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.utils.CommonLogUtil;
import com.anythink.core.strategy.AppStrategyManager;
import com.anythink.myoffer.buiness.resource.DiskLruCache;
import com.anythink.myoffer.buiness.resource.MyOfferLoader;
import com.anythink.myoffer.buiness.resource.MyOfferResourceState;
import com.anythink.myoffer.entity.MyOfferAd;
import com.anythink.myoffer.entity.MyOfferSetting;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class MyOfferResourceManager {

    public static final String TAG = MyOfferResourceManager.class.getSimpleName();

    private MyOfferResourceManager() {
        initDiskCache(AppStrategyManager.getInstance(SDKContext.getInstance().getContext()).getMyOfferCacheSize());
    }

    public static MyOfferResourceManager getInstance() {
        return Holder.sInstance;
    }

    private static class Holder {
        private static MyOfferResourceManager sInstance = new MyOfferResourceManager();
    }

    private DiskLruCache mDiskLruCache;
//    private final Object mDiskCacheLock = new Object();

    private void initDiskCache(long maxSize) {//maxSize 单位 KB
        if (mDiskLruCache != null) {
            return;
        }
        String saveDirectory = MyOfferResourceState.getSaveDirectory();
        if (TextUtils.isEmpty(saveDirectory)) {
            return;
        }
        File diskCacheDir = new File(saveDirectory);
        if (diskCacheDir != null) {
            if (!diskCacheDir.exists()) {
                diskCacheDir.mkdirs();
            }
            try {
                mDiskLruCache = DiskLruCache.open(new File(saveDirectory), 1, 1, maxSize * 1024);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean writeToDiskLruCache(String url, InputStream inputStream) {
        if (url == null || inputStream == null) {
            return false;
        }
        String saveDirectory = MyOfferResourceState.getSaveDirectory();
        if (TextUtils.isEmpty(saveDirectory)) {
            return false;
        }
        File diskCacheDir = new File(saveDirectory);
        if (!diskCacheDir.exists()) {
            diskCacheDir.mkdirs();
        }

        boolean result = false;
//        synchronized (mDiskCacheLock) {
        // Add to disk cache
        if (mDiskLruCache != null) {
            final String key = MyOfferResourceState.getResourceName(url);
            OutputStream out = null;
            DiskLruCache.Editor editor = null;
            try {
                DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
                if (snapshot == null) {
                    editor = mDiskLruCache.edit(key);
                    if (editor != null) {
                        out = editor.newOutputStream(0);
                        // 读取数据
                        byte[] buffer = new byte[2048];
                        int ch = 0;
                        while ((ch = inputStream.read(buffer)) != -1) {
                            out.write(buffer, 0, ch);
                        }
                        editor.commit();
                        out.close();
                    }
                } else {
                    snapshot.getInputStream(0).close();
                }
                result = true;
            } catch (Exception e) {
                CommonLogUtil.e(TAG, "writeToDiskLruCache - " + e);
                try {
                    if (editor != null) {
                        editor.abort();
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                }
            }
        }
//        }
        return result;
    }

    public FileInputStream getInputStream(String url) {
        try {
            if (mDiskLruCache != null) {
                InputStream inputStream = null;
                DiskLruCache.Snapshot snapshot = mDiskLruCache.get(MyOfferResourceState.getResourceName(url));

                if (snapshot != null) {
                    inputStream = snapshot.getInputStream(0);
                    if (inputStream != null) {
                        return ((FileInputStream) inputStream);
                    }
                }
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * preload
     */
    public void preLoadOfferList(List<MyOfferAd> myOfferAds, MyOfferSetting myOfferSetting) {
        if (myOfferAds == null) {
            return;
        }
        int size = myOfferAds.size();
        for (int i = 0; i < size; i++) {
            load(true, myOfferAds.get(i), myOfferSetting, null);
        }

    }

    /**
     * download myoffer resource
     */
    public void load(MyOfferAd myOfferAd, MyOfferSetting myOfferSetting, final MyOfferLoader.MyOfferLoaderListener listener) {
        load(false, myOfferAd, myOfferSetting, listener);
    }

    /**
     * download myoffer resource
     */
    public void load(boolean isPreLoad, MyOfferAd myOfferAd, MyOfferSetting myOfferSetting, final MyOfferLoader.MyOfferLoaderListener listener) {
        MyOfferLoader myOfferLoader = new MyOfferLoader(isPreLoad, myOfferSetting.getOfferTimeout());
        myOfferLoader.load(myOfferAd, listener);
    }

    /**
     * Check if resource ready
     */
    public boolean isExist(MyOfferAd myOfferAd) {
        return MyOfferResourceState.isExist(myOfferAd);
    }


}
