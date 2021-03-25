/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

public class AutoResizeTextView extends TextView {

    private static final int MAX_SIZE = 1000;

    private static final int MIN_SIZE = 5;

    private TextPaint mTextPaint;

    private float mSpacingMult = 1.0f;

    private float mSpacingAdd = 0.0f;

    private boolean needAdaptive = false;

    private boolean adapting = false;

    public AutoResizeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initAutoResizeTextView();
    }

    public AutoResizeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAutoResizeTextView();
    }

    public AutoResizeTextView(Context context) {
        super(context);
        initAutoResizeTextView();
    }

    private void initAutoResizeTextView() {
        mTextPaint = new TextPaint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (adapting) {
            return;
        }
        if (needAdaptive) {
            adaptTextSize();
        } else {
            super.onDraw(canvas);
        }
    }

    private void adaptTextSize() {
        CharSequence text = getText();
        int viewWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        int viewHeight = getHeight() - getPaddingTop() - getPaddingBottom();

        if (viewWidth == 0 || viewHeight == 0 || TextUtils.isEmpty(text)) {
            return;
        }

        adapting = true;
        float textSize = getTextSize();

        int textMeasureWidth;
        int textMeasureHeight;

        int min = MIN_SIZE;
        int max = (int) textSize;
        int cur = max;
        while (cur >= min) {
            mTextPaint.setTextSize(cur);

            textMeasureWidth = (int) mTextPaint.measureText(text, 0, text.length());
            textMeasureHeight = getTextHeight(text, viewWidth);

            if (textMeasureWidth < viewWidth && textMeasureHeight < viewHeight) {
                break;
            }

            cur--;
        }
        setTextSize(TypedValue.COMPLEX_UNIT_PX, cur);

        adapting = false;
        needAdaptive = false;

        invalidate();
    }

    private int getTextHeight(CharSequence text, int targetWidth) {
        StaticLayout layout = new StaticLayout(text, mTextPaint, targetWidth,
                Alignment.ALIGN_NORMAL, mSpacingMult, mSpacingAdd, true);
        return layout.getHeight();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        needAdaptive = true;
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        needAdaptive = true;
    }

    @Override
    public void setLineSpacing(float add, float mult) {
        super.setLineSpacing(add, mult);
        mSpacingMult = mult;
        mSpacingAdd = add;
    }
}