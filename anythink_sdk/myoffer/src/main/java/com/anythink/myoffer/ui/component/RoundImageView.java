package com.anythink.myoffer.ui.component;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.util.AttributeSet;
import android.widget.ImageView;

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
