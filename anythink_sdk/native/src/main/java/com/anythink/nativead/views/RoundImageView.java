/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.nativead.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.util.AttributeSet;

import com.anythink.core.common.utils.CommonUtil;
import com.anythink.nativead.api.ATNativeImageView;


public class RoundImageView extends ATNativeImageView {

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

    @Override
    protected void dispatchDraw(Canvas canvas) {
        try {
            if (mIsRadiu) {
                int width = getWidth();
                int height = getHeight();

                Path path = new Path();
                path.moveTo(mRadiu, 0);

                path.lineTo(width - mRadiu, 0);
                path.quadTo(width, 0, width, mRadiu);

                path.lineTo(width, height - mRadiu);
                path.quadTo(width, height, width - mRadiu, height);

                path.lineTo(mRadiu, height);
                path.quadTo(0, height, 0, height - mRadiu);

                path.lineTo(0, mRadiu);
                path.quadTo(0, 0, mRadiu, 0);
                canvas.clipPath(path);
            }
            super.dispatchDraw(canvas);
        } catch (Exception e) {

        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        try {
            if (mIsRadiu) {
                int width = getWidth();
                int height = getHeight();

                Path path = new Path();
                path.moveTo(mRadiu, 0);

                path.lineTo(width - mRadiu, 0);
                path.quadTo(width, 0, width, mRadiu);

                path.lineTo(width, height - mRadiu);
                path.quadTo(width, height, width - mRadiu, height);

                path.lineTo(mRadiu, height);
                path.quadTo(0, height, 0, height - mRadiu);

                path.lineTo(0, mRadiu);
                path.quadTo(0, 0, mRadiu, 0);
                canvas.clipPath(path);
            }
            super.onDraw(canvas);
        } catch (Exception e) {

        }
    }


}
