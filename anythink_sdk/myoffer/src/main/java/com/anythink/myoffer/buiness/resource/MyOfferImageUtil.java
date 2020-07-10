package com.anythink.myoffer.buiness.resource;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

import java.io.FileDescriptor;

public class MyOfferImageUtil {

    public static Bitmap getBitmap(String path, int maxWidth, int maxHeight) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            BitmapFactory.decodeFile(path, options);
            options.inSampleSize = calculateSampleSize(options.outWidth, options.outHeight, maxWidth, maxHeight);
            options.inJustDecodeBounds = false;

            return BitmapFactory.decodeFile(path, options);
        } catch (OutOfMemoryError oom) {

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    public static Bitmap getBitmap(FileDescriptor fd, int maxWidth, int maxHeight) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            BitmapFactory.decodeFileDescriptor(fd, null, options);
            options.inSampleSize = calculateSampleSize(options.outWidth, options.outHeight, maxWidth, maxHeight);
            options.inJustDecodeBounds = false;

            return BitmapFactory.decodeFileDescriptor(fd, null, options);
        } catch (OutOfMemoryError oom) {
            oom.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }


    public static int calculateSampleSize(int originWidth, int originHeight, int desWidth, int desHeight) {
        int sampleSize = 1;
        int width = originWidth;
        int height = originHeight;
        while((width / sampleSize) > desWidth && (height / sampleSize) > desHeight) {
            sampleSize *= 2;
        }
        return sampleSize;
    }

    public static Bitmap blurBitmap(Context context, Bitmap bitmap) {
        try {
            Bitmap outBitmap = Bitmap.createBitmap(bitmap.getWidth()/3, bitmap.getHeight()/3, Bitmap.Config.ARGB_8888);
            RenderScript rs = RenderScript.create(context);
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
            e.printStackTrace();
        }

        return null;

    }
}
