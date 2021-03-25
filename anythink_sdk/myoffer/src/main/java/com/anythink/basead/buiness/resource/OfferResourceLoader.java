/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.buiness.resource;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.anythink.core.common.entity.BaseAdContent;
import com.anythink.core.common.entity.BaseAdSetting;
import com.anythink.core.common.utils.CommonLogUtil;
import com.anythink.basead.entity.OfferError;
import com.anythink.basead.entity.OfferErrorCode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * OfferLoader
 */
public class OfferResourceLoader implements OfferUrlLoadManager.ResourceLoadResult {

    public static final String TAG = OfferResourceLoader.class.getSimpleName();

    private String mPlacementId;
    private boolean mIsPreLoad;
    private int mMyOfferTimeout;
    private String mOfferId;
    private List<String> mUrlList;
    private ResourceLoaderListener mListener;

    private Handler mMainHandler;
    private AtomicBoolean mHasCallback = new AtomicBoolean(false);//Load callback flag

    public OfferResourceLoader(String placementId, boolean isPreLoad, int myOfferTimeout) {
        this.mPlacementId = placementId;
        this.mIsPreLoad = isPreLoad;
        this.mMyOfferTimeout = myOfferTimeout;
    }

    public interface ResourceLoaderListener {
        /**
         * MyOffer load success
         */
        void onSuccess();

        /**
         * MyOffer load failed
         */
        void onFailed(OfferError msg);
    }

    /**
     * load MyOffer
     */
    public void load(BaseAdContent baseAdContent, BaseAdSetting settings, ResourceLoaderListener listener) {
        this.mOfferId = baseAdContent.getOfferId();

        mListener = listener;

        List<String> urlList = baseAdContent.getUrlList(settings);
        if (urlList == null) {
            notifyFailed(OfferErrorCode.get(OfferErrorCode.incompleteResourceError, OfferErrorCode.fail_incomplete_resource));
            return;
        }

        int size = urlList.size();
        String url;
        if (size == 0) {
            notifySuccess();
            return;
        }

        mUrlList = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            url = urlList.get(i);

            if (!OfferResourceState.isExist(url)) {
                mUrlList.add(url);
            }
        }

        int url_size = mUrlList.size();
        if (url_size == 0) {
            CommonLogUtil.d(TAG, "Offer(" + mOfferId + "), all files have already exist");
            notifySuccess();
            return;
        }


        OfferUrlLoadManager.getInstance().register(this);
        startLoadTimer();

        synchronized (OfferResourceLoader.this) {
            for (int i = 0; i < url_size; i++) {
                url = mUrlList.get(i);

                if (TextUtils.isEmpty(url)) {
                    continue;
                } else if (OfferResourceState.isLoading(url)) {
                    CommonLogUtil.d(TAG, "file is loading -> " + url);
                    continue;
                } else if (OfferResourceState.isExist(url)) {
                    CommonLogUtil.d(TAG, "file exist -> " + url);
                    OfferResourceState.setState(url, OfferResourceState.NORMAL);
                    OfferUrlLoadManager.getInstance().notifyDownloadSuccess(url);
                    continue;
                }
                OfferResourceState.setState(url, OfferResourceState.LOADING);
                CommonLogUtil.d(TAG, "file not exist -> " + url);
                OfferUrlLoader myOfferUrlLoader = new OfferUrlLoader(mPlacementId, mIsPreLoad, baseAdContent, url);
                myOfferUrlLoader.start();
            }
        }
    }

    @Override
    public void onResourceLoadSuccess(String url) {
        synchronized (OfferResourceLoader.this) {
            OfferResourceState.setState(url, OfferResourceState.NORMAL);
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
    public void onResourceLoadFailed(String url, OfferError error) {
        OfferResourceState.setState(url, OfferResourceState.NORMAL);
        notifyFailed(error);
    }

    private void notifySuccess() {
        mHasCallback.set(true);
        if (mListener != null) {
            CommonLogUtil.d(TAG, "Offer load success, OfferId -> " + mOfferId);
            mListener.onSuccess();
        }
        this.release();
    }

    private void notifyFailed(OfferError error) {
        mHasCallback.set(true);
        if (mListener != null) {
            CommonLogUtil.d(TAG, "Offer load failed, OfferId -> " + mOfferId);
            mListener.onFailed(error);
        }
        this.release();
    }

    private void release() {
        OfferUrlLoadManager.getInstance().unRegister(this);
        if (mMainHandler != null) {
            mMainHandler.removeCallbacksAndMessages(null);
            mMainHandler = null;
        }
    }

    private void startLoadTimer() {
        if (mMainHandler == null) {
            mMainHandler = new Handler(Looper.getMainLooper());
            mMainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!mHasCallback.get()) {
                        notifyFailed(OfferErrorCode.get(OfferErrorCode.timeOutError, OfferErrorCode.fail_load_timeout));
                    }
                }
            }, mMyOfferTimeout);
        }
    }
}
