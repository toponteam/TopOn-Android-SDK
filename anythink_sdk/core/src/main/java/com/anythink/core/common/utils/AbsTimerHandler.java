/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.common.utils;

import java.util.Timer;
import java.util.TimerTask;

public abstract class AbsTimerHandler {

    private boolean mIsTimerUp;
    private Timer mTimer;

    protected void startTimer(long delay) {
        mTimer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                synchronized (this) {
                    if (!mIsTimerUp) {
                        mIsTimerUp = true;
                        onTimerUp();
                    }
                }
            }
        };

        mTimer.schedule(task, delay);
    }

    public void cancelTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    protected boolean isTimerUp() {
        return this.mIsTimerUp;
    }

    protected abstract void onTimerUp();
}
