/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.ui.component;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.anythink.basead.ui.util.ViewUtil;
import com.anythink.core.common.utils.CommonUtil;

public class RoundFrameLayout extends FrameLayout {

    int mRadiu;

    public RoundFrameLayout(Context context) {
        super(context);
        mRadiu = dip2px(getContext(), 10);
    }

    public RoundFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRadiu = dip2px(getContext(), 10);
    }

    public RoundFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mRadiu = dip2px(getContext(), 10);
    }


    @Override
    protected void dispatchDraw(Canvas canvas) {
        int saveCount = canvas.saveLayer(0, 0, getWidth(), getHeight(), null, Canvas.ALL_SAVE_FLAG);
        super.dispatchDraw(canvas);
        ViewUtil.drawRadiusMask(canvas, getWidth(), getHeight(), mRadiu);
        canvas.restoreToCount(saveCount);
    }

    public static int dip2px(Context context, float dipValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }
}
