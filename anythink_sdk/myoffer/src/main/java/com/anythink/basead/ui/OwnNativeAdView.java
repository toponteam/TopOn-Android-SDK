/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import com.anythink.basead.entity.AdClickRecord;

public class OwnNativeAdView extends FrameLayout {

    public OwnNativeAdView(@NonNull Context context) {
        super(context);
    }

    public OwnNativeAdView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public OwnNativeAdView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    int recordTouchDownX;
    int recordTouchDownY;
    int recordTouchUpX;
    int recordTouchUpY;

    int recordTouchDownRelateX;
    int recordTouchDownRelateY;
    int recordTouchUpRelateX;
    int recordTouchUpRelateY;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                recordTouchDownX = (int) ev.getRawX();
                recordTouchDownY = (int) ev.getRawY();

                recordTouchDownRelateX = (int) ev.getX();
                recordTouchDownRelateY = (int) ev.getY();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                recordTouchUpX = (int) ev.getRawX();
                recordTouchUpY = (int) ev.getRawY();

                recordTouchUpRelateX = (int) ev.getX();
                recordTouchUpRelateY = (int) ev.getY();

                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    public AdClickRecord getAdClickRecord() {
        AdClickRecord adClickRecord = new AdClickRecord();
        adClickRecord.clickDownX = recordTouchDownX;
        adClickRecord.clickDownY = recordTouchDownY;
        adClickRecord.clickUpX = recordTouchUpX;
        adClickRecord.clickUpY = recordTouchUpY;

        adClickRecord.clickRelateDownX = recordTouchDownRelateX;
        adClickRecord.clickRelateDownY = recordTouchDownRelateY;
        adClickRecord.clickRelateUpX = recordTouchUpRelateX;
        adClickRecord.clickRelateUpY = recordTouchUpRelateY;

        return adClickRecord;
    }
}
