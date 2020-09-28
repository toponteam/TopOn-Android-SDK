package com.anythink.core.common;

import android.content.Context;
import android.os.CountDownTimer;

import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.entity.LoggerInfoInterface;
import com.anythink.core.strategy.AppStrategy;
import com.anythink.core.strategy.AppStrategyManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Instant Tracking
 *
 * @param <T>
 */
public abstract class InstantUpLoadManager<T extends LoggerInfoInterface> {

    ArrayList<T> mTrackingQueue = new ArrayList<>();

    protected CountDownTimer mCountDownTimer;

    String mAppId;
    protected Context mApplicationContext;


    protected InstantUpLoadManager(Context context) {
        mApplicationContext = context.getApplicationContext();
        mAppId = SDKContext.getInstance().getAppId();

    }

    public synchronized void addLoggerInfo(T loggerBean) {
        final AppStrategy appStrategy = AppStrategyManager.getInstance(mApplicationContext).getAppStrategyByAppId(mAppId);

        boolean isTimeUp = false;
        if (mTrackingQueue.isEmpty()) {
            if (appStrategy.getTkInterval() > 0) {
                SDKContext.getInstance().runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        mCountDownTimer = new CountDownTimer(appStrategy.getTkInterval(), appStrategy.getTkInterval()) {
                            @Override
                            public void onTick(long l) {

                            }

                            @Override
                            public void onFinish() {
                                tryToSendTrackingInfo(true);
                            }
                        };
                        mCountDownTimer.start();
                    }
                });

            } else {
                isTimeUp = true;
            }
        }
        mTrackingQueue.add(loggerBean);
        tryToSendTrackingInfo(isTimeUp);
    }

    private synchronized void tryToSendTrackingInfo(boolean isTimeUp) {

        AppStrategy appStrategy = AppStrategyManager.getInstance(mApplicationContext).getAppStrategyByAppId(mAppId);

        if (isTimeUp) {
            ArrayList<T> sendInfo = new ArrayList<>();
            sendInfo.addAll(mTrackingQueue);
            if (sendInfo.size() > 0) {
                sendLoggerToServer(sendInfo);
            }

            mTrackingQueue.clear();
        } else {
            ArrayList<T> sendInfo = new ArrayList<>();
            if (mTrackingQueue.size() >= appStrategy.getTkMaxAmount()) {
                for (int i = appStrategy.getTkMaxAmount() - 1; i >= 0; i--) {
                    sendInfo.add(mTrackingQueue.get(i));
                    mTrackingQueue.remove(i);
                }
                if (sendInfo.size() > 0) {
                    sendLoggerToServer(sendInfo);
                }
            }
        }


        SDKContext.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (mTrackingQueue.isEmpty()) {
                    if (mCountDownTimer != null) {
                        mCountDownTimer.cancel();
                    }
                }
            }
        });

    }

    protected abstract void sendLoggerToServer(List<T> sendInfo);
}
