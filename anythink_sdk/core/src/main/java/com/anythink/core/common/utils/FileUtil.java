package com.anythink.core.common.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;

import java.io.File;
import java.util.UUID;

public class FileUtil {

    public static File getFileSaveFile(Context context) {
        File baseFile = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            try {
                File fileDir = context.getExternalFilesDir(null);

                if (fileDir != null) {
                    baseFile = getRandomFileDir(fileDir);

                    if (baseFile != null) {
                        return baseFile;
                    }
                }
            } catch (Throwable t) {
            }
        }

        if (baseFile == null && hasSDCardPermission(context)) {
            String rootPath = Environment.getExternalStorageDirectory().getPath() + File.separator
                    + context.getPackageName();
            baseFile = getRandomFileDir(new File(rootPath));
            if (!hasEnoughSpace()) {
                baseFile = null;
            }
        }
        if (baseFile == null) {
            String saveDirectory = context.getFilesDir().getAbsoluteFile().getAbsolutePath();
            baseFile = new File(saveDirectory);
        }

        return baseFile;
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

    private static boolean hasSDCardPermission(Context context) {
        boolean mHasPermission = false;
        if (context == null) {
            return mHasPermission;
        }
        try {
            PackageManager pm = context.getPackageManager();
            int hasPerm = pm.checkPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    context.getPackageName());
            mHasPermission = hasPerm == PackageManager.PERMISSION_GRANTED;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mHasPermission;
    }

    private static boolean hasEnoughSpace() {
        return getAvailableExternalMemorySize() > 30 * 1024 * 1024;
    }

    /**
     * @return byte
     */
    @SuppressWarnings("deprecation")
    private static long getAvailableExternalMemorySize() {
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

    private static boolean hasSDCard() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    public static String hashKeyForDisk(String url) {
        return CommonMD5.getMD5(url);
    }
}
