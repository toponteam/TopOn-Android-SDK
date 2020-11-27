/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.common.res;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;

import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.res.image.ImageUrlLoader;
import com.anythink.core.common.utils.BitmapUtil;
import com.anythink.core.common.utils.CommonLogUtil;
import com.anythink.core.common.utils.FileUtil;
import com.anythink.core.common.utils.task.TaskManager;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.lang.ref.SoftReference;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

public class ImageLoader {
    private static final String TAG = "ImageLoader";

    private static ImageLoader mInstance;

    /**
     * Lru Image Map
     */
    private ImageLruCache<String, SoftReference<Bitmap>> mMemoryCache;

    private final Object mDiskCacheLock = new Object();

    Context mContext;
    // Download callback listener
    private LinkedHashMap<String, List<ImageLoaderListener>> mListenerMap = new LinkedHashMap<String, List<ImageLoaderListener>>();

    private void onCallbackSuccess(final String url, final Bitmap bitmap) {
        SDKContext.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                LinkedList<ImageLoaderListener> list = (LinkedList<ImageLoaderListener>) mListenerMap.get(url);
                if (list != null) {
                    for (ImageLoaderListener listener : list) {
                        if (listener != null) {
                            if (bitmap != null) {
                                listener.onSuccess(url, bitmap);
                            } else {
                                listener.onFail(url, "Bitmap load fail");
                            }

                        }
                    }
                }
                mListenerMap.remove(url);
            }
        });
    }

    private void onCallbackError(final String url, final String errorMsg) {
        SDKContext.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                LinkedList<ImageLoaderListener> list = (LinkedList<ImageLoaderListener>) mListenerMap.get(url);
                if (list != null) {
                    for (ImageLoaderListener listener : list) {
                        if (listener != null) {
                            listener.onFail(url, errorMsg);
                        }
                    }
                }
                mListenerMap.remove(url);
            }
        });

    }


    /**
     * Add Bitmap to Memory Cache
     *
     * @param key
     * @param bitmap
     */
    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null && bitmap != null) {
            mMemoryCache.put(key, new SoftReference<Bitmap>(bitmap));
        }
    }


    /**
     * Get Bitmap from Memory Cache
     *
     * @param key
     * @return
     */
    public Bitmap getBitmapFromMemCache(String key) {
        SoftReference<Bitmap> bitmapWeakRef = mMemoryCache.get(key);
        return bitmapWeakRef != null ? bitmapWeakRef.get() : null;
    }

    /**
     * Get Bitmap from Disk Cache
     */
    public Bitmap getBitmapFromDiskCache(ResourceEntry entry, int width, int height) {
        if (entry == null || TextUtils.isEmpty(entry.resourceUrl)) {
            return null;
        }
        final String fileName = FileUtil.hashKeyForDisk(entry.resourceUrl);
        Bitmap bitmap = null;

        synchronized (mDiskCacheLock) {
//            while (mDiskCacheStarting) {
//                try {
//                    mDiskCacheLock.wait();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }

            FileInputStream fileInputStream = ResourceDiskCacheManager.getInstance(mContext).getFileInputStream(entry.resourceType, fileName);
            if (fileInputStream == null) {
                return null;
            }

            try {
                FileDescriptor fd = ((FileInputStream) fileInputStream).getFD();
                bitmap = BitmapUtil.getBitmap(fd, width, height);
            } catch (Throwable e) {

            } finally {
                try {
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                } catch (Exception e) {
                }
            }

            return bitmap;
        }
    }

    private ImageLoader(Context context) {
        mContext = context.getApplicationContext();
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int mCacheSize = maxMemory / 5;

        CommonLogUtil.e(TAG, "ImageLoad init cache size: " + mCacheSize + "B");
        mMemoryCache = new ImageLruCache<String, SoftReference<Bitmap>>(mCacheSize) {

            @Override
            protected int sizeOf(String key, SoftReference<Bitmap> value) {
                Bitmap bitmap = value != null ? value.get() : null;
                int size = bitmap != null ? bitmap.getRowBytes() * bitmap.getHeight() : 0;
                CommonLogUtil.e(TAG, "sizeOf: Bitmap size:" + size + "B.");
                return size;
            }

            @Override
            protected void entryRemoved(boolean evicted, String key, SoftReference<Bitmap> oldValue, SoftReference<Bitmap> newValue) {
                super.entryRemoved(evicted, key, oldValue, newValue);
                try {
                    Bitmap oldBitmap = oldValue != null ? oldValue.get() : null;
                    if (oldBitmap == null) {
                        CommonLogUtil.e(TAG, "entryRemoved: Bitmap has been release.");
                    }
                    if (oldValue != null && !oldValue.equals(newValue) && oldBitmap != null && !oldBitmap.isRecycled()) {
                        // The removed entry is a recycling drawable, so notify it
                        // that it has been removed from the memory cache
                        oldBitmap.recycle();
                        oldBitmap = null;
                        oldValue = null;
                        CommonLogUtil.e(TAG, "entryRemoved: Bitmap recycle.");
                    }
                } catch (Exception ex) {
                    if (Const.DEBUG) {
                        ex.printStackTrace();
                    }
                }

            }
        };
    }


    public synchronized static ImageLoader getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new ImageLoader(context);
        }
        return mInstance;
    }


    /**
     * Load Bitmap
     */
    public void load(ResourceEntry resourceEntry, ImageLoaderListener listener) {
        load(resourceEntry, -1, -1, listener);
    }

    public void load(final ResourceEntry resourceEntry, final int width, final int height, final ImageLoaderListener listener) {
        if (resourceEntry == null || TextUtils.isEmpty(resourceEntry.resourceUrl)) {
            if (listener != null) {
                listener.onFail("", "No url info.");
            }
            return;
        }

        Bitmap cacheBitmap = getBitmapFromMemCache(resourceEntry.resourceUrl);
        if (cacheBitmap != null) {
            listener.onSuccess(resourceEntry.resourceUrl, cacheBitmap);
            return;
        } else {
            TaskManager.getInstance().run_proxy(new Runnable() {
                @Override
                public void run() {
                    final Bitmap bitmap = getBitmapFromDiskCache(resourceEntry, width, height);
                    if (bitmap != null) {
                        CommonLogUtil.d(TAG, "url image [" + resourceEntry.resourceUrl + "] is downloaded");
                        addBitmapToMemoryCache(resourceEntry.resourceUrl, bitmap);
                        LinkedList<ImageLoaderListener> list = new LinkedList<ImageLoaderListener>();
                        list.add(listener);
                        mListenerMap.put(resourceEntry.resourceUrl, list);

                        onCallbackSuccess(resourceEntry.resourceUrl, bitmap);
                    } else {
                        loadFormUrl(resourceEntry, width, height, listener);
                    }
                }
            });
        }
    }


    /**
     * Load Bitmap from Net
     */
    private void loadFormUrl(final ResourceEntry resourceEntry, final int width, final int height,
                             ImageLoaderListener listener) {

        if (!mListenerMap.containsKey(resourceEntry.resourceUrl)) {
            LinkedList<ImageLoaderListener> list = new LinkedList<ImageLoaderListener>();
            list.add(listener);
            mListenerMap.put(resourceEntry.resourceUrl, list);
            ImageUrlLoader urlLoader = new ImageUrlLoader(resourceEntry);
            urlLoader.setListener(new ImageUrlLoader.HttpLoadListener() {
                @Override
                public void onLoadSuccess(ResourceEntry entry) {
                    CommonLogUtil.e(TAG, "Load Success:" + entry.resourceUrl);

                    final Bitmap bitmap = getBitmapFromDiskCache(entry, width, height);
                    if (bitmap != null) {
                        addBitmapToMemoryCache(entry.resourceUrl, bitmap);
                    }

                    onCallbackSuccess(resourceEntry.resourceUrl, bitmap);
                }

                @Override
                public void onLoadFail(ResourceEntry entry, String errorMsg) {
                    onCallbackError(entry.resourceUrl, errorMsg);
                }
            });

            urlLoader.start();
        } else {
            LinkedList<ImageLoaderListener> list = (LinkedList<ImageLoaderListener>) mListenerMap.get(resourceEntry.resourceUrl);
            if (list != null && !list.contains(listener)) {
                list.add(listener);
            }
        }
    }


    public void recycle() {
        try {
            if (mMemoryCache != null) {
                mMemoryCache.evictAll();
            }
            if (mListenerMap != null) {
                mListenerMap.clear();
            }
//            //每天清理一次缓存
//            long lastClearTime = SharedPreferencesUtils.getLong(SDKInitManager.getInstance().getContext(), Const.SHAREPERFENCE_KEY.FILE_NAME, "imagecache_clear_time", 0L);
//            if ((System.currentTimeMillis() - lastClearTime) > 24 * 60 * 60 * 1000) {
//                TaskManager.getInstance().run(new Worker() {
//                    @Override
//                    public void work() {
//                        LogUtil.i(TAG, "clearDiskCache");
////                        CommonSDCardUtil.clearCache();
//                        clearDishCache(); //通过DishLruCache对象来清除
//                    }
//                });
//                SharedPreferencesUtils.putLong(SDKInitManager.getInstance().getContext(), Const.SHAREPERFENCE_KEY.FILE_NAME, "imagecache_clear_time", System.currentTimeMillis());
//            }
        } catch (Exception ex) {
            if (Const.DEBUG) {
                ex.printStackTrace();
            }
        }

    }


    public interface ImageLoaderListener {
        void onSuccess(String url, Bitmap bitmap);

        void onFail(String url, String errorMsg);
    }

}

