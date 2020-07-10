package com.anythink.myoffer.buiness.resource;

import android.graphics.Bitmap;
import android.text.TextUtils;

import com.anythink.myoffer.buiness.MyOfferResourceManager;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;

public class MyOfferResourceUtil {

    /**
     * Get Bitmap by width and height
     */
    public static Bitmap getBitmap(String url, int width, int height) {
        if (TextUtils.isEmpty(url) || width <= 0 || height <= 0) {
            return null;
        }
        Bitmap result = null;
        FileInputStream fis = MyOfferResourceManager.getInstance().getInputStream(url);
        if (fis != null) {
            try {
                FileDescriptor fd = fis.getFD();
                if (fd != null) {
                    result = MyOfferImageUtil.getBitmap(fd, width, height);
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }
}
