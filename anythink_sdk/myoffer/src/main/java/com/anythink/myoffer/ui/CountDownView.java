package com.anythink.myoffer.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

public class CountDownView extends View {

    private Paint mPaint;
    private Paint mPaintBg;
    private Paint mPaintText;
    private float mThick;
    private float mTextSize;

    private int mBgColor;
    private int mUnderRingColor;
    private int mUpProgressColor;

    private int mWidth;
    private int mHeight;
    private int mRadis;
    private RectF mRectF;
    private float mSweepAngle;
    private String mCountDownTimeText;
    private Rect mCountDownTimeTextBounds;

    private int mDuration;
    private float mTextWidth;
    private Paint.FontMetrics mFontMetrics;


    public CountDownView(Context context) {
        this(context, null);
    }

    public CountDownView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    private void init(Context context) {
        mThick = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, context.getResources().getDisplayMetrics());
        mTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 13, context.getResources().getDisplayMetrics());


        mBgColor = Color.parseColor("#cc505050");
        mUnderRingColor = Color.parseColor("#505050");
        mUpProgressColor = Color.WHITE;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mThick);

        mPaintBg = new Paint(mPaint);
        mPaintBg.setColor(mBgColor);
        mPaintBg.setStyle(Paint.Style.FILL);

        mPaintText = new Paint();
        mPaintText.setAntiAlias(true);
        mPaintText.setTextSize(mTextSize);
        mPaintText.setColor(mUpProgressColor);

        mRectF = new RectF();
        mCountDownTimeTextBounds = new Rect();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        this.mWidth = w;
        this.mHeight = h;

        float offset = mThick * 0.5f;

        this.mRectF.set(0 + offset, 0 + offset, mWidth - offset, mHeight - offset);
        this.mRadis = (int) mRectF.width() >> 1;
    }

    public void setDuration(int duration) {
        this.mDuration = duration;
        this.mCountDownTimeText = duration / 1000 + "";
    }

    public void refresh(int currentPosition) {
        mSweepAngle = 360 * (currentPosition * 1f / mDuration);
        mCountDownTimeText = (int) (Math.ceil((mDuration - currentPosition) / 1000D)) + "";
        this.invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawCircle(mRectF.centerX(), mRectF.centerY(), mRadis, mPaintBg);

        mPaint.setColor(mUnderRingColor);
        canvas.drawArc(mRectF, 0, 360, false, mPaint);

        mPaint.setColor(mUpProgressColor);
        canvas.drawArc(mRectF, -90, mSweepAngle, false, mPaint);

        if(!TextUtils.isEmpty(mCountDownTimeText)) {
            mPaintText.getTextBounds(mCountDownTimeText, 0, mCountDownTimeText.length(), mCountDownTimeTextBounds);
            mTextWidth = mPaintText.measureText(mCountDownTimeText);
            mFontMetrics = mPaintText.getFontMetrics();
            canvas.drawText(mCountDownTimeText, mRectF.centerX() - (mTextWidth / 2f),
                    mRectF.centerY() + ((mFontMetrics.bottom - mFontMetrics.top)/2f - mFontMetrics.bottom), mPaintText);
        }
    }
}
