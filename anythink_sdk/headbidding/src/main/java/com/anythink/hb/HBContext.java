package com.anythink.hb;

import android.os.Handler;
import android.os.Looper;

public class HBContext {

    private Handler mHandler;

    static class Holder {
        static final HBContext sInstance = new HBContext();
    }

    public static HBContext getInstance() {
        return Holder.sInstance;
    }

    private HBContext() {
        mHandler = new Handler(Looper.getMainLooper());
    }

    public void runOnMainThread(Runnable runnable) {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            runnable.run();
        } else {
            mHandler.post(runnable);
        }
    }

    public void runOnMainThreadDelayed(Runnable runnable, long delayMillis) {
        mHandler.postDelayed(runnable, delayMillis);
    }

    public void removeMainThreadRunnable(Runnable runnable) {
        mHandler.removeCallbacks(runnable);
    }

    public void removeMainThreadCackbacksAndMessages() {
        mHandler.removeCallbacksAndMessages(null);
    }


}
