/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.ui.web;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Z on 2018/6/4.
 */

public class WebProgressBarView extends View {

    int mProgress;
    int mProgressColor;
    Paint mPaint;

    public WebProgressBarView(Context context) {
        super(context);
        init();
    }

    public WebProgressBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WebProgressBarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mProgressColor = 0xff2196F3;
        mPaint = new Paint();
        mPaint.setColor(mProgressColor);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);

        mProgress = 0;

        setBackgroundColor(0x00ffffff);
    }

    public void setProgress(int progress) {
        mProgress = progress;
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        canvas.drawRect(0, 0, getWidth() * mProgress / 100, getHeight(), mPaint);
        canvas.restore();
    }
}
