package com.anythink.nativead.bussiness;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;

import com.anythink.core.common.utils.task.TaskManager;
import com.anythink.nativead.bussiness.utils.CommonBitmapUtil;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class CommonImageLoader {

    private static CommonImageLoader sInstance;

    Handler handler = new Handler(Looper.getMainLooper());

    HashMap<String, SoftReference<Bitmap>> cacheMap = new HashMap<>();

    SoftReference<Bitmap> blurBitmap;

    public static CommonImageLoader getInstance() {
        if (sInstance == null) {
            sInstance = new CommonImageLoader();
        }

        return sInstance;
    }


    public void startLoadImage(final String urlStr, final int size, final ImageCallback callback) {
        SoftReference<Bitmap> bitmapWeakReference = cacheMap.get(urlStr);
        if (bitmapWeakReference != null && bitmapWeakReference.get() != null) {
            if (callback != null) {
                callback.onSuccess(bitmapWeakReference.get(), urlStr);
            }
            return;
        }

        TaskManager.getInstance().run_proxy(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                InputStream is = null;
                try {
                    URL url = new URL(urlStr);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setReadTimeout(30 * 1000);
                    connection.setConnectTimeout(30 * 1000);

                    connection.connect();

                    int statusCode = connection.getResponseCode();

                    //处理返回
                    if (statusCode == 200) {
                        is = connection.getInputStream();
                        byte[] buffer = new byte[2048];
                        int ch = 0;
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        while ((ch = is.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, ch);
                        }
                        byte[] bitmapBuffer = outputStream.toByteArray();
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;

                        BitmapFactory.decodeByteArray(bitmapBuffer, 0, bitmapBuffer.length, options);
                        // Calculate inSampleSize
                        options.inSampleSize = CommonBitmapUtil.calculateInSampleSize(options, size, size);
                        // Decode bitmap with inSampleSize set
                        options.inJustDecodeBounds = false;

                        Bitmap map = BitmapFactory.decodeByteArray(bitmapBuffer, 0, bitmapBuffer.length, options);
                        final SoftReference<Bitmap> bitmaps = new SoftReference<>(map);
                        cacheMap.put(urlStr, bitmaps);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (callback != null) {
                                    callback.onSuccess(bitmaps.get(), urlStr);
                                }
                            }
                        });
                        if (outputStream != null) {
                            outputStream.close();
                            outputStream = null;
                        }

                        return;
                    }
                } catch (Throwable e) {

                } finally {
                    try {
                        if (is != null) {

                            is.close();
                            is = null;
                        }
                    } catch (Exception e) {

                    }
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null) {
                            callback.onFail();
                        }
                    }
                });

            }
        });
    }

    public Bitmap getBlurBitmap(Context context, Bitmap map) {
        try {
            blurBitmap = new SoftReference<>(CommonBitmapUtil.blurBitmap(context, map));
            return blurBitmap.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }


    public interface ImageCallback {
        public void onSuccess(Bitmap bitmap, String url);

        public void onFail();
    }
}
