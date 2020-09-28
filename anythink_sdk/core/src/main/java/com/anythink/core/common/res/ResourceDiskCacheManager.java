package com.anythink.core.common.res;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.utils.CommonLogUtil;
import com.anythink.core.common.utils.FileUtil;
import com.anythink.core.strategy.AppStrategyManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ConcurrentHashMap;

public class ResourceDiskCacheManager {
    private final String TAG = getClass().getSimpleName();


    private static final String INTERNAL_DIR = Const.RESOURCE_HEAD + "_internal_resouce";
    private static final String CUSTOM_DIR = Const.RESOURCE_HEAD + "_custom_resouce";

    private static ResourceDiskCacheManager sIntance;
    private Context mContext;
    private File mSaveFileDirection;

    ConcurrentHashMap<Integer, DiskLruCache> mFileTypeDiskLruCacheMap = new ConcurrentHashMap<>();

    private ResourceDiskCacheManager(Context context) {
        mContext = context.getApplicationContext();
        mSaveFileDirection = FileUtil.getFileSaveFile(mContext);
    }

    public synchronized static ResourceDiskCacheManager getInstance(Context context) {
        if (sIntance == null) {
            sIntance = new ResourceDiskCacheManager(context);
        }

        return sIntance;
    }

    /**
     * Save File (No suggest to run on main thread.)
     *
     * @param fileType
     * @param fileName
     * @param inputStream
     * @return
     */
    public boolean saveNetworkInputStreamToFile(int fileType, String fileName, InputStream inputStream) {
        if (fileName == null || inputStream == null) {
            return false;
        }
        String saveDirectory = getSaveDirectory(fileType);
        if (TextUtils.isEmpty(saveDirectory)) {
            return false;
        }

        File diskCacheDir = new File(saveDirectory);
        if (!diskCacheDir.exists()) {
            diskCacheDir.mkdirs();
        }

        boolean result = false;

        DiskLruCache diskLruCache = mFileTypeDiskLruCacheMap.get(fileType);
        if (diskLruCache == null) {
            try {
                diskLruCache = DiskLruCache.open(diskCacheDir, 1, 1, getCacheMaxSize(fileType));
                mFileTypeDiskLruCacheMap.put(fileType, diskLruCache);
            } catch (Throwable e) {
                if (SDKContext.getInstance().isNetworkLogDebug()) {
                    Log.e(TAG, "Create DiskCache error.");
                    e.printStackTrace();
                }
            }
        }

//        synchronized (mDiskCacheLock) {
        // Add to disk cache
        if (diskLruCache != null) {
            OutputStream out = null;
            DiskLruCache.Editor editor = null;
            try {
                DiskLruCache.Snapshot snapshot = diskLruCache.get(fileName);
                if (snapshot == null) {
                    editor = diskLruCache.edit(fileName);
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
        return result;
    }

    /**
     * Get File InputStream (No suggest to run on main thread.)
     *
     * @param fileType
     * @param fileName
     * @return
     */
    public FileInputStream getFileInputStream(int fileType, String fileName) {
        String saveDirectory = getSaveDirectory(fileType);
        if (TextUtils.isEmpty(saveDirectory)) {
            return null;
        }

        File diskCacheDir = new File(saveDirectory);
        if (!diskCacheDir.exists()) {
            diskCacheDir.mkdirs();
        }

        DiskLruCache diskLruCache = mFileTypeDiskLruCacheMap.get(fileType);
        if (diskLruCache == null) {
            try {
                diskLruCache = DiskLruCache.open(diskCacheDir, 1, 1, getCacheMaxSize(fileType));
                mFileTypeDiskLruCacheMap.put(fileType, diskLruCache);
            } catch (Throwable e) {
                if (SDKContext.getInstance().isNetworkLogDebug()) {
                    Log.e(TAG, "Create DiskCache error.");
                    e.printStackTrace();
                }
            }
        }


        try {
            if (diskLruCache != null) {
                InputStream inputStream = null;
                DiskLruCache.Snapshot snapshot = diskLruCache.get(fileName);

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
     * Resource save path
     */
    public String getSaveDirectory(int fileType) {
        String fileDirName = CUSTOM_DIR;
        switch (fileType) {
            case ResourceEntry.INTERNAL_CACHE_TYPE:
                fileDirName = INTERNAL_DIR;
                break;
        }

        File file = new File(mSaveFileDirection, fileDirName);

        return file.getAbsolutePath();
    }


    /**
     * Get Disk Cache Size
     *
     * @param fileType
     * @return
     */
    private long getCacheMaxSize(int fileType) {
        switch (fileType) {
            case ResourceEntry.INTERNAL_CACHE_TYPE:
                return AppStrategyManager.getInstance(SDKContext.getInstance().getContext()).getMyOfferCacheSize() * 1024;
        }

        return 25 * 1024 * 1024;
    }

    public boolean isExistFile(int fileType, String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return false;
        }
        File file = new File(getSaveDirectory(fileType) + File.separator + fileName + ".0");
        return file.exists();
    }

}
