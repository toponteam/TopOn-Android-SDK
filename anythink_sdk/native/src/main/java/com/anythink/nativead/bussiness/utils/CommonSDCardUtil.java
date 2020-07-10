
package com.anythink.nativead.bussiness.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;

import com.anythink.core.common.utils.task.TaskManager;

import java.io.File;
import java.util.UUID;

/**
 * 描述: SDCard工具类
 *
 * @author chenys
 * @since 2013-7-11 下午4:25:27
 */
public class CommonSDCardUtil {


    static boolean mHasPermission = false;
    static String mCachePath = "";
    static String mFilePath = "";


    public static void init(final Context c) {

        TaskManager.getInstance().run_proxy(new Runnable() {
            @Override
            public void run() {
                try {
                    mCachePath = c.getFilesDir().getAbsolutePath() + File.separator;
                    PackageManager pm = c.getPackageManager();
                    int hasPerm = pm.checkPermission(
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            c.getPackageName());
                    if (hasPerm == PackageManager.PERMISSION_GRANTED) {
                        mHasPermission = true;
                    } else {
                        mHasPermission = false;
                    }

                    mFilePath = getRootPath(c);
                } catch (Exception e) {
                    mCachePath = c.getFilesDir().getAbsolutePath() + File.separator;
                }
            }
        });
    }


    /**
     * 判断是否存在SDCard
     *
     * @return
     */
    public static boolean hasSDCard() {
        String status = Environment.getExternalStorageState();
        if (!status.equals(Environment.MEDIA_MOUNTED)) {
            return false;
        }
        return true;
    }


    /**
     * SDCard剩余大小
     *
     * @return 字节
     */
    @SuppressWarnings("deprecation")
    public static long getAvailableExternalMemorySize() {
        if (hasSDCard()) {
            try {
                File path = Environment.getExternalStorageDirectory();
                StatFs stat = new StatFs(path.getPath());
                long blockSize = stat.getBlockSize();
                long availableBlocks = stat.getAvailableBlocks();
                return availableBlocks * blockSize;
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            } catch (Error e) {
            }
        }
        return 0;
    }


//    static Status sStatus = new Status();

    static class Status {
        public long usertime;
        public long nicetime;
        public long systemtime;
        public long idletime;
        public long iowaittime;
        public long irqtime;
        public long softirqtime;

        public float getTotalTime() {
            return (usertime + nicetime + systemtime + idletime + iowaittime
                    + irqtime + softirqtime);
        }
    }

    /**
     *
     * @param context
     * @return
     */
    public static String getRootPath(Context context) {
        File baseFile = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            try {
                File fileDir = context.getExternalFilesDir(null);
                if (fileDir != null) {
                    baseFile = getRandomFileDir(fileDir);
                }
            } catch (Throwable t) {
            }
        }

        if (mHasPermission) {
            if (baseFile == null) {
                String rootPath = Environment.getExternalStorageDirectory().getPath() + File.separator + "Android" + File.separator + "data" + File.separator + context.getPackageName();
                baseFile = getRandomFileDir(new File(rootPath));
            }
            if (!hasEnoughSpace()) {
                baseFile = null;
            }
        }

        if (baseFile == null || !baseFile.exists()) {
            baseFile = context.getFilesDir().getAbsoluteFile();
        }
        return baseFile.getAbsolutePath();
    }

    private static File getRandomFileDir(File fileDir) {
        File baseFile = null;
        File temp = new File(fileDir, UUID.randomUUID() + "");
        if (temp.exists()) {
            temp.delete();
        }
        if (temp.mkdirs()) {
            temp.delete();
            baseFile = fileDir.getAbsoluteFile();
        }
        return baseFile;
    }


    public static boolean hasEnoughSpace() {
        return getAvailableExternalMemorySize() > 30 * 1024 * 1024;
    }


}
