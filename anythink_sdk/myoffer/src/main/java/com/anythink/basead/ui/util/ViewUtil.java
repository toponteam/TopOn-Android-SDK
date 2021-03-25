/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.ui.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.view.TouchDelegate;
import android.view.View;

public class ViewUtil {

    public static void expandTouchArea(final View view, final int size) {
        final View parentView = (View) view.getParent();
        parentView.post(new Runnable() {
            @Override
            public void run() {
                Rect rect = new Rect();
                view.getHitRect(rect);

                rect.top -= size;
                rect.bottom += size;
                rect.left -= size;
                rect.right += size;

                parentView.setTouchDelegate(new TouchDelegate(rect, view));
            }
        });
    }

    public static Path getRadiusPath(int radius, int width, int height) {
        Path path = new Path();
        path.moveTo(radius, 0);

        path.lineTo(width - radius, 0);
        path.quadTo(width, 0, width, radius);

        path.lineTo(width, height - radius);
        path.quadTo(width, height, width - radius, height);

        path.lineTo(radius, height);
        path.quadTo(0, height, 0, height - radius);

        path.lineTo(0, radius);
        path.quadTo(0, 0, radius, 0);

        path.close();

        return path;
    }

    public static void drawRadiusMask(Canvas canvas, int width, int height, int radius) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);
        Bitmap maskBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas1 = new Canvas(maskBitmap);
        canvas1.drawPath(ViewUtil.getRadiusPath(radius, width, height), paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));


        canvas.drawBitmap(maskBitmap, 0, 0, paint);
//        canvas.restoreToCount(saveCount);
    }

    public static int[] getFitSize(int viewWidth, int viewHeight, float destRatio) {

        int width;
        int height;

        float viewRatio = (float)viewWidth / viewHeight;

        if (destRatio > viewRatio) {
            width = viewWidth;
            height = (int) (width / destRatio);
        } else {
            height = viewHeight;
            width = (int) (height * (destRatio));
        }

        return new int[] {
                width,
                height
        };
    }

}
