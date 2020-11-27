/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.hb;

import com.anythink.core.common.HeadBiddingFactory;
import com.anythink.core.common.entity.ATHeadBiddingRequest;
import com.anythink.core.common.utils.AbsTimerHandler;
import com.anythink.core.hb.callback.BiddingCallback;
import com.anythink.core.strategy.PlaceStrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ATHeadBiddingHandler extends AbsTimerHandler implements HeadBiddingFactory.IHeadBiddingHandler {

    public static final String TAG = ATHeadBiddingHandler.class.getSimpleName();

    private List<PlaceStrategy.UnitGroupInfo> processSuccessList = Collections.synchronizedList(new ArrayList<PlaceStrategy.UnitGroupInfo>());
    private List<PlaceStrategy.UnitGroupInfo> processFailedList = Collections.synchronizedList(new ArrayList<PlaceStrategy.UnitGroupInfo>());

    private HeadBiddingFactory.IHeadBiddingCallback mResultCallback;

    private ATS2SHeadBiddingHandler mATS2SHeadBiddingHandler;
    private ATC2SHeadBiddingHandler mATC2SHeadBiddingHandler;

    private boolean isC2SFinish;
    private boolean isS2SFinish;

    private String mRequestId;
    private long mHBWaitingToReqeustTime;
    private long mHBBidMaxTime;

    private boolean isTestMode;

    public ATHeadBiddingHandler(ATHeadBiddingRequest request) {

        mRequestId = request.requestId;
        mHBWaitingToReqeustTime = request.hbWaitingToReqeustTime;
        mHBBidMaxTime = request.hbBidMaxTimeOut;

        List<PlaceStrategy.UnitGroupInfo> hbList = request.hbList;

        int size = hbList.size();
        List<PlaceStrategy.UnitGroupInfo> s2sHbList = null;
        List<PlaceStrategy.UnitGroupInfo> c2sHbList = null;

        //split to s2sHbList、c2sHbList
        PlaceStrategy.UnitGroupInfo unitGroupInfo;
        for (int i = 0; i < size; i++) {
            unitGroupInfo = hbList.get(i);

            if (unitGroupInfo.adsourceType == PlaceStrategy.UnitGroupInfo.TYPE_HB_S2S
                    || unitGroupInfo.adsourceType == PlaceStrategy.UnitGroupInfo.TYPE_ADX) {//s2s
                if (s2sHbList == null) {
                    s2sHbList = new ArrayList<>(size);
                }
                s2sHbList.add(unitGroupInfo);
            } else if (unitGroupInfo.adsourceType == PlaceStrategy.UnitGroupInfo.TYPE_HB_C2S) {//c2s
                if (c2sHbList == null) {
                    c2sHbList = new ArrayList<>(size);
                }
                c2sHbList.add(unitGroupInfo);
            }
        }

        //create handler for s2s and c2s
        if (s2sHbList != null && s2sHbList.size() > 0) {
            mATS2SHeadBiddingHandler = new ATS2SHeadBiddingHandler(request.createS2SRequest(s2sHbList));
        } else {
            isS2SFinish = true;
        }

        if (c2sHbList != null && c2sHbList.size() > 0) {
            mATC2SHeadBiddingHandler = new ATC2SHeadBiddingHandler(request.createC2SRequest(c2sHbList));
        } else {
            isC2SFinish = true;
        }
    }

    @Override
    public void setTestMode(boolean isTest) {
        this.isTestMode = isTest;
    }

    @Override
    public void startHeadBiddingRequest(HeadBiddingFactory.IHeadBiddingCallback callback) {
        mResultCallback = callback;

        //start waiting timer
        startWaitingTimer();

        //start bid max timer
        super.startTimer(mHBBidMaxTime);

        if (mATS2SHeadBiddingHandler != null) {
            //start s2s request
            mATS2SHeadBiddingHandler.setTestMode(this.isTestMode);
            mATS2SHeadBiddingHandler.startBidRequest(new BiddingCallback() {
                @Override
                public void onBiddingSuccess(List<PlaceStrategy.UnitGroupInfo> successList) {
                    handleResult(true, successList);
                }

                @Override
                public void onBiddingFailed(List<PlaceStrategy.UnitGroupInfo> failedList) {
                    handleResult(false, failedList);
                }

                @Override
                public void onBiddingFinished() {
                    isS2SFinish = true;
                    checkToNotifyFinished();
                }
            });
        }

        if (mATC2SHeadBiddingHandler != null) {
            //start c2s request
            mATC2SHeadBiddingHandler.setTestMode(this.isTestMode);
            mATC2SHeadBiddingHandler.startBidRequest(new BiddingCallback() {
                @Override
                public void onBiddingSuccess(List<PlaceStrategy.UnitGroupInfo> successList) {
                    handleResult(true, successList);
                }

                @Override
                public void onBiddingFailed(List<PlaceStrategy.UnitGroupInfo> failedList) {
                    handleResult(false, failedList);
                }

                @Override
                public void onBiddingFinished() {
                    isC2SFinish = true;
                    checkToNotifyFinished();
                }
            });
        }
    }

    private boolean mIsWaitingTimerUp;
    private Timer waitingTimer;

    private void startWaitingTimer() {
        waitingTimer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                mIsWaitingTimerUp = true;
                callbackResult();
            }
        };
        waitingTimer.schedule(timerTask, getWaitingToRequestTime());
    }

    private synchronized void callbackResult() {
        int successSize = processSuccessList.size();
        int failedSize = processFailedList.size();

        //callback result
        if (successSize > 0 || failedSize > 0) {
            List<PlaceStrategy.UnitGroupInfo> successObjectList = new ArrayList<>();
            List<PlaceStrategy.UnitGroupInfo> failObjectList = new ArrayList<>();

            synchronized (ATHeadBiddingHandler.this) {
                if (successSize > 0) {
                    successObjectList.addAll(processSuccessList);
                    processSuccessList.clear();
                }

                if (failedSize > 0) {
                    failObjectList.addAll(processFailedList);
                    processFailedList.clear();
                }
            }

            if (mResultCallback != null) {
                if (successObjectList.size() > 0) {
                    Collections.sort(successObjectList);
                    mResultCallback.onSuccess(this.mRequestId, successObjectList);
                }
                if (failObjectList.size() > 0) {
                    mResultCallback.onFailed(this.mRequestId, failObjectList);
                }
            }
        }
    }

    private long getWaitingToRequestTime() {
        long hbWaitingToReqeustTime = mHBWaitingToReqeustTime;
        if (hbWaitingToReqeustTime <= 0) {
            hbWaitingToReqeustTime = 2000;
        }
        return hbWaitingToReqeustTime;
    }

    private void handleResult(boolean isSuccess, List<PlaceStrategy.UnitGroupInfo> list) {

        synchronized (ATHeadBiddingHandler.this) {
            //add to list according to isSuccess
            if (isSuccess) {
                processSuccessList.addAll(list);
            } else {
                processFailedList.addAll(list);
            }
        }

        //after waiting timer up, callback when handle each bid result
        if (!mIsWaitingTimerUp) {
            callbackResult();
        }
    }

    @Override
    protected void onTimerUp() {
        if (!isS2SFinish) {
            if (mATS2SHeadBiddingHandler != null) {
                mATS2SHeadBiddingHandler.onTimeout();
            }
        }

        if (!isC2SFinish) {
            if (mATC2SHeadBiddingHandler != null) {
                mATC2SHeadBiddingHandler.onTimeout();
            }
        }
    }

    private void checkToNotifyFinished() {
        //after all bid request has finished, callback finish
        if (isS2SFinish && isC2SFinish) {

            if (waitingTimer != null) {
                waitingTimer.cancel();
                waitingTimer = null;
            }

            this.cancelTimer();

            callbackResult();

            if (mResultCallback != null) {
                mResultCallback.onFinished(this.mRequestId);
            }

            this.release();
        }
    }

    private void release() {
        if (mATS2SHeadBiddingHandler != null) {
            mATS2SHeadBiddingHandler = null;
        }
        if (mATC2SHeadBiddingHandler != null) {
            mATC2SHeadBiddingHandler = null;
        }
        mResultCallback = null;
    }


}
