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
import android.widget.ImageView;

import com.anythink.basead.ui.util.ViewUtil;
import com.anythink.core.common.res.image.RecycleImageView;
import com.anythink.core.common.utils.CommonUtil;
import com.anythink.core.common.utils.task.TaskManager;


public class RoundImageView extends RecycleImageView {

    int mRadiu;
    boolean mIsRadiu;

    public RoundImageView(Context context) {
        super(context);
        mRadiu = CommonUtil.dip2px(getContext(), 5);
    }

    public RoundImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRadiu = CommonUtil.dip2px(getContext(), 5);
    }

    public RoundImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mRadiu = CommonUtil.dip2px(getContext(), 5);
    }

    public void setNeedRadiu(boolean isRadiu) {
        mIsRadiu = isRadiu;
    }

    public void setRadiusInDip(int dip) {
        this.mRadiu = CommonUtil.dip2px(getContext(), dip);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        try {
            if (mIsRadiu) {
                int saveCount = canvas.saveLayer(0, 0, getWidth(), getHeight(), null, Canvas.ALL_SAVE_FLAG);
                super.dispatchDraw(canvas);
                ViewUtil.drawRadiusMask(canvas, getWidth(), getHeight(), mRadiu);
                canvas.restoreToCount(saveCount);
                return;
            }

        } catch (Exception e) {

        }

        super.dispatchDraw(canvas);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        try {
            if (mIsRadiu) {
                int saveCount = canvas.saveLayer(0, 0, getWidth(), getHeight(), null, Canvas.ALL_SAVE_FLAG);
                super.onDraw(canvas);
                ViewUtil.drawRadiusMask(canvas, getWidth(), getHeight(), mRadiu);
                canvas.restoreToCount(saveCount);
                return;
            }

        } catch (Exception e) {

        }
        super.onDraw(canvas);
    }


}
