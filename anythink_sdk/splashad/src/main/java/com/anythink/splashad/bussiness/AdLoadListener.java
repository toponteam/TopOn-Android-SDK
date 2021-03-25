/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.splashad.bussiness;

import com.anythink.core.api.AdError;

import java.util.Timer;
import java.util.TimerTask;

public abstract class AdLoadListener {

    String mRequestId;
    Timer mTimer;
    boolean mHasReturn = false;

    TimerTask mTimerTask = new TimerTask() {
        @Override
        public void run() {
            if (!mHasReturn) {
                mHasReturn = true;

                onTimeout(mRequestId);
            }
        }
    };

    public void startCountDown(int timeout) {
        if (mTimer == null) {
            mTimer = new Timer();
        }

        mTimer.schedule(mTimerTask, timeout);
    }


    public void setRequestId(String requestId) {
        this.mRequestId = requestId;
    }

    public void onCallbackAdLoaded() {
        if (mTimer != null) {
            mTimer.cancel();
        }

        if (!mHasReturn) {
            mHasReturn = true;

            onAdLoaded(mRequestId);
        }
    }


    public void onCallbackNoAdError(AdError adError) {
        if (mTimer != null) {
            mTimer.cancel();
        }

        if (!mHasReturn) {
            mHasReturn = true;

            onNoAdError(mRequestId, adError);
        }
    }

    public abstract void onAdLoaded(String requestId);

    public abstract void onNoAdError(String requestId, AdError adError);

    public abstract void onTimeout(String requestId);

}
