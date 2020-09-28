package com.anythink.myoffer.utils.impression;

import android.view.View;

/**
 * Created by Z on 2018/2/27.
 * 用于控制Native广告展示时机
 */

public abstract class ImpressionController implements ImpressionInterface {

    private static final int DEFAULT_IMPRESSION_MIN_TIME_VIEWED_MS = 1000;
    private static final int DEFAULT_IMPRESSION_MIN_PERCENTAGE_VIEWED = 50; //默认展示50%以上才调用展示

    private boolean mImpressionRecorded;
    private int mImpressionMinTimeViewed;
    private int mImpressionMinPercentageViewed;
    private Integer mImpressionMinVisiblePx;


    public ImpressionController() {
        mImpressionMinTimeViewed = DEFAULT_IMPRESSION_MIN_TIME_VIEWED_MS;
        mImpressionMinPercentageViewed = DEFAULT_IMPRESSION_MIN_PERCENTAGE_VIEWED;
        mImpressionMinVisiblePx = null;
    }

    @Override
    public abstract void recordImpression( final View view) ;

    @Override
    final public int getImpressionMinPercentageViewed() {
        return mImpressionMinPercentageViewed;
    }

    @Override
    final public int getImpressionMinTimeViewed() {
        return mImpressionMinTimeViewed;
    }

    @Override
    final public Integer getImpressionMinVisiblePx() {
        return mImpressionMinVisiblePx;
    }

    @Override
    final public boolean isImpressionRecorded() {
        return mImpressionRecorded;
    }

    @Override
    final public void setImpressionRecorded() {
        mImpressionRecorded = true;
    }


}
