
package com.anythink.nativead.bussiness.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.text.TextUtils;

import java.io.File;

/**
 * Bitmap创建类
 *
 * @author chenys
 */
public class CommonBitmapUtil {

    /**
     * Calculate an inSampleSize for use in a {@link Options} object
     * when decoding bitmaps using the decode* methods from {@link BitmapFactory}.
     * This implementation calculates the closest inSampleSize that is a power of 2 and will result
     * in the final decoded bitmap having a width and height equal to or larger than the requested
     * width and height.
     *
     * @param options   An options object with out* params already populated (run through a decode*
     *                  method with inJustDecodeBounds==true
     * @param reqWidth  The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @return The value to be used for inSampleSize
     */
    public static int calculateInSampleSize(Options options, int reqWidth, int reqHeight) {
        // BEGIN_INCLUDE (calculate_sample_size)
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (reqWidth <= 0 && reqHeight <= 0) {
            return inSampleSize;
        }
        if (reqWidth > 0 && reqHeight == 0) {
        }
        if (height > reqHeight || width > reqWidth) {
            if (reqWidth > 0 && reqHeight == 0) {
                reqHeight = height * reqWidth / width;
            }
            if (reqHeight > 0 && reqWidth == 0) {
                reqWidth = width * reqHeight / height;
            }
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }

            // This offers some additional logic in case the image has a strange
            // aspect ratio. For example, a panorama may have a much larger
            // width than height. In these cases the total pixels might still
            // end up being too large to fit comfortably in memory, so we should
            // be more aggressive with sample down the image (=larger inSampleSize).

            long totalPixels = width * height / (inSampleSize * inSampleSize);

            // Anything more than 2x the requested pixels we'll sample down further
            final long totalReqPixelsCap = reqWidth * reqHeight * 4;

            while (totalPixels > totalReqPixelsCap && ((totalPixels / 4) > totalReqPixelsCap)) {
                inSampleSize *= 2;
                totalPixels /= 4;
            }
        }
        return inSampleSize;
    }

    public static boolean isFileExist(String filePath) {
        if (TextUtils.isEmpty(filePath) || TextUtils.isEmpty(filePath.trim())) {
            return false;
        }

        File file = new File(filePath);
        return (file.exists() && file.isFile());
    }

    public static Bitmap blurBitmap(Context context, Bitmap bitmap) {
        try {
            Bitmap outBitmap = Bitmap.createBitmap(bitmap.getWidth()/3, bitmap.getHeight()/3, Config.ARGB_8888);
            RenderScript rs = RenderScript.create(context);
            // 创建高斯模糊对象
            ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
            Allocation allIn = Allocation.createFromBitmap(rs, bitmap);
            Allocation allOut = Allocation.createFromBitmap(rs, outBitmap);
            blurScript.setRadius(25.f);
            blurScript.setInput(allIn);
            blurScript.forEach(allOut);
            allOut.copyTo(outBitmap);

            Canvas c = new Canvas(outBitmap);
            c.drawColor(0x33000000); //如果不设置颜色，默认是透明背景

            rs.destroy();
            return outBitmap;
        } catch (Exception e) {

        }

        return null;

    }
}
