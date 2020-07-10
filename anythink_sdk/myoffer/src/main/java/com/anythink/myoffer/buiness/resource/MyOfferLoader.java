package com.anythink.myoffer.buiness.resource;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.anythink.core.common.utils.CommonLogUtil;
import com.anythink.myoffer.entity.MyOfferAd;
import com.anythink.network.myoffer.MyOfferError;
import com.anythink.network.myoffer.MyOfferErrorCode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * OfferLoader
 */
public class MyOfferLoader implements MyOfferUrlLoadManager.ResourceLoadResult {

    public static final String TAG = MyOfferLoader.class.getSimpleName();

    private String mPlacementId;
    private boolean mIsPreLoad;
    private int mMyOfferTimeout;
    private String mOfferId;
    private List<String> mUrlList;
    private MyOfferLoaderListener mListener;

    private Handler mMainHandler;
    private AtomicBoolean mHasCallback = new AtomicBoolean(false);//Load callback flag

    public MyOfferLoader(String placementId, boolean isPreLoad, int myOfferTimeout) {
        this.mPlacementId = placementId;
        this.mIsPreLoad = isPreLoad;
        this.mMyOfferTimeout = myOfferTimeout;
    }

    public interface MyOfferLoaderListener {
        /**
         * MyOffer load success
         */
        void onSuccess();
        /**
         * MyOffer load failed
         */
        void onFailed(MyOfferError msg);
    }

    /**
     * load MyOffer
     */
    public void load(MyOfferAd myOfferAd, MyOfferLoaderListener listener) {
        this.mOfferId = myOfferAd.getOfferId();

        mListener = listener;

        List<String> urlList = myOfferAd.getUrlList();
        int size = urlList.size();
        String url;
        if(size == 0) {
            notifySuccess();
            return;
        }

        mUrlList = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            url = urlList.get(i);

            if (!MyOfferResourceState.isExist(url)) {
                mUrlList.add(url);
            }
        }

        int url_size = mUrlList.size();
        if(url_size == 0) {
            CommonLogUtil.d(TAG, "Offer(" + mOfferId + "), all files have already exist");
            notifySuccess();
            return;
        }


        MyOfferUrlLoadManager.getInstance().register(this);
        startLoadTimer();

        synchronized (MyOfferLoader.this) {
            for (int i = 0; i < url_size; i++) {
                url = mUrlList.get(i);

                if(TextUtils.isEmpty(url)) {
                    continue;
                } else if(MyOfferResourceState.isLoading(url)) {
                    CommonLogUtil.d(TAG, "file is loading -> " + url);
                    continue;
                }
                else if (MyOfferResourceState.isExist(url)) {
                    CommonLogUtil.d(TAG, "file exist -> " + url);
                    MyOfferResourceState.setState(url, MyOfferResourceState.NORMAL);
                    MyOfferUrlLoadManager.getInstance().notifyDownloadSuccess(url);
                    continue;
                }
                MyOfferResourceState.setState(url, MyOfferResourceState.LOADING);
                CommonLogUtil.d(TAG, "file not exist -> " + url);
                MyOfferUrlLoader myOfferUrlLoader = new MyOfferUrlLoader(mPlacementId, mIsPreLoad, myOfferAd.getOfferId(), url, TextUtils.equals(url, myOfferAd.getVideoUrl()));
                myOfferUrlLoader.start();
            }
        }
    }

    @Override
    public void onResourceLoadSuccess(String url) {
        synchronized (MyOfferLoader.this) {
            MyOfferResourceState.setState(url, MyOfferResourceState.NORMAL);
            if (mUrlList != null) {
                mUrlList.remove(url);
                if (mUrlList.size() == 0) {
                    if (!mHasCallback.get()) {//Load success before timeout
                        notifySuccess();
                    }
                }
            }
        }
    }

    @Override
    public void onResourceLoadFailed(String url, MyOfferError error) {
        MyOfferResourceState.setState(url, MyOfferResourceState.NORMAL);
        notifyFailed(error);
    }

    private void notifySuccess() {
        mHasCallback.set(true);
        if(mListener != null) {
            CommonLogUtil.d(TAG, "Offer load success, OfferId -> " + mOfferId);
            mListener.onSuccess();
        }
        this.release();
    }
    private void notifyFailed(MyOfferError error) {
        mHasCallback.set(true);
        if(mListener != null) {
            CommonLogUtil.d(TAG, "Offer load failed, OfferId -> " + mOfferId);
            mListener.onFailed(error);
        }
        this.release();
    }
    private void release() {
        MyOfferUrlLoadManager.getInstance().unRegister(this);
        if(mMainHandler != null) {
            mMainHandler.removeCallbacksAndMessages(null);
            mMainHandler = null;
        }
    }

    private void startLoadTimer() {
        if(mMainHandler == null) {
            mMainHandler = new Handler(Looper.getMainLooper());
            mMainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!mHasCallback.get()) {
                        notifyFailed(MyOfferErrorCode.get(MyOfferErrorCode.timeOutError, MyOfferErrorCode.fail_load_timeout));
                    }
                }
            }, mMyOfferTimeout);
        }
    }
}
