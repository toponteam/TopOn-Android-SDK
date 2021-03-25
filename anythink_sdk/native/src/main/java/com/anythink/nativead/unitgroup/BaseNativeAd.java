/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.nativead.unitgroup;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.anythink.core.api.ATNetworkConfirmInfo;
import com.anythink.core.common.entity.AdTrackingInfo;
import com.anythink.core.api.BaseAd;
import com.anythink.core.common.utils.CommonLogUtil;
import com.anythink.nativead.api.NativeAd;

import org.json.JSONObject;

import java.util.List;


public abstract class BaseNativeAd extends BaseAd {
    private final static String TAG = BaseNativeAd.class.getSimpleName();
    /**
     * Ad Source Type
     */
    protected final String VIDEO_TYPE = "1";
    protected final String IMAGE_TYPE = "2";
    protected final String UNKNOW_TYPE = "0";

    public final int NETWORK_UNKNOW = -1;

    public interface NativeEventListener {
        void onAdImpressed();

        void onAdClicked(View childView);

        void onAdVideoStart();

        void onAdVideoEnd();

        void onAdVideoProgress(int progress);

        void onAdDislikeButtonClick();

        void onDeeplinkCallback(boolean isSuccess);

        void onDownloadConfirmCallback(Context context, View clickView, ATNetworkConfirmInfo networkConfirmInfo);
    }


    private NativeEventListener mNativeEventListener;

    protected AdTrackingInfo mAdTrackingInfo;

    private String mAdCacheId;

    protected String mAdSourceType = UNKNOW_TYPE;

    protected int mNetworkType = NETWORK_UNKNOW;

    protected BaseNativeAd() {
    }

    /**
     * Tracking Info
     */
    @Override
    public final void setTrackingInfo(AdTrackingInfo adTrackingInfo) {
        mAdTrackingInfo = adTrackingInfo;
    }

    @Override
    public final AdTrackingInfo getDetail() {
        return mAdTrackingInfo;
    }

//    public final void setAdCacheId(String cacheAdId) {
//        mAdCacheId = cacheAdId;
//    }
//
//    public final String getAdCacheId() {
//        return mAdCacheId;
//    }

    // Lifecycle Handlers

    /**
     * Check if it's template ad
     */
    public abstract boolean isNativeExpress();


    /**
     * Returns the ad's MediaView
     *
     * @param object
     * @return
     */
    public abstract View getAdMediaView(Object... object);

    public abstract View getAdIconView();

    /**
     * Your {@link BaseNativeAd} subclass should implement this method if the network requires the developer
     * to prepare state for recording an impression or click before a view is rendered to screen.
     * <p>
     * This method is optional.
     */
    public abstract void prepare(final View view, final FrameLayout.LayoutParams layoutParams);

    public abstract void prepare(final View view, List<View> clickViewList, final FrameLayout.LayoutParams layoutParams);

    public abstract void bindDislikeListener(View.OnClickListener onClickListener);

    /**
     * Your {@link BaseNativeAd} subclass should implement this method if the network requires the developer
     * to reset or clear state of the native ad after it goes off screen and before it is rendered
     * again.
     * <p>
     * This method is optional.
     */
    public abstract void clear(final View view);


    public abstract ViewGroup getCustomAdContainer();

    public void setNativeEventListener(
            final NativeEventListener nativeEventListener) {
        mNativeEventListener = nativeEventListener;
    }


    /**
     * Notifies the SDK that the user has clicked the ad.
     */
    public final void notifyAdClicked() {
        CommonLogUtil.d(TAG, "notifyAdClicked...");
        if (mNativeEventListener != null) {
            mNativeEventListener.onAdClicked(null);
        }
    }

    /**
     * Notifies the SDK that the user has impressoned the ad.
     */
    public final void notifyAdImpression() {
        CommonLogUtil.d(TAG, "notifyAdImpression...");
        if (mNativeEventListener != null) {
            mNativeEventListener.onAdImpressed();
        }
    }

    /**
     * Notifies the SDK that the user has started ad video.
     */
    public final void notifyAdVideoStart() {
        CommonLogUtil.d(TAG, "notifyAdVideoStart...");
        if (mNativeEventListener != null) {
            mNativeEventListener.onAdVideoStart();
        }
    }

    /**
     * Notifies the SDK that the user has ended ad video.
     */
    public final void notifyAdVideoEnd() {
        CommonLogUtil.d(TAG, "notifyAdVideoEnd...");
        if (mNativeEventListener != null) {
            mNativeEventListener.onAdVideoEnd();
        }
    }

    /**
     * Notifies the SDK that the Ad Deeplink callback status.
     */
    public final void notifyDeeplinkCallback(boolean isSuccess) {
        CommonLogUtil.d(TAG, "notifyDeeplinkCallback...");
        if (mNativeEventListener != null) {
            mNativeEventListener.onDeeplinkCallback(isSuccess);
        }
    }

    /**
     * Notifies the SDK that the user the video progress.
     */
    public final void notifyAdVideoPlayProgress(int progress) {
        CommonLogUtil.d(TAG, "notifyAdVideoPlayProgress...");
        if (mNativeEventListener != null) {
            mNativeEventListener.onAdVideoProgress(progress);
        }
    }

    /**
     * Notifies the SDk that the user click dislike button.
     *
     * @return
     */
    public final void notifyAdDislikeClick() {
        CommonLogUtil.d(TAG, "notifyAdDislikeClick...");
        if (mNativeEventListener != null) {
            mNativeEventListener.onAdDislikeButtonClick();
        }
    }

    public final void notifyDownloadConfirm(Context context, View clickView, ATNetworkConfirmInfo atNetworkConfirmInfo) {
        CommonLogUtil.d(TAG, "notifyDownloadConfirm...");
        if (mNativeEventListener != null) {
            mNativeEventListener.onDownloadConfirmCallback(context, clickView, atNetworkConfirmInfo);
        }
    }

    public final String getAdType() {
        return mAdSourceType;
    }


    public abstract void onPause();

    public abstract void onResume();

    public NativeAd.DownLoadProgressListener mDownLoadProgressListener;

    public final void setDownLoadProgressListener(NativeAd.DownLoadProgressListener pDownLoadProgressListener) {
        mDownLoadProgressListener = pDownLoadProgressListener;
    }

//    public void log(String action, String status, String extraMsg) {
//        if (ATSDK.isNetworkLogDebug()) {
//            if (mAdTrackingInfo != null) {
//                try {
//                    JSONObject jsonObject = new JSONObject();
//                    if (mAdTrackingInfo.ismIsDefaultNetwork()) {
//                        jsonObject.put("isDefault", true);
//                    }
//                    jsonObject.put("placementId", mAdTrackingInfo.getmPlacementId());
//                    jsonObject.put("adType", mAdTrackingInfo.getAdTypeString());
//                    jsonObject.put("action", action);
//                    jsonObject.put("refresh", mAdTrackingInfo.getmRefresh());
//                    jsonObject.put("result", status);
//                    jsonObject.put("position", mAdTrackingInfo.getRequestLevel());
//                    jsonObject.put("networkType", mAdTrackingInfo.getmNetworkType());
//                    jsonObject.put("networkName", mAdTrackingInfo.getNetworkName());
//                    jsonObject.put("networkVersion", mAdTrackingInfo.getmNetworkVersion());
//                    jsonObject.put("networkUnit", mAdTrackingInfo.getmNetworkContent());
//                    jsonObject.put("isHB", mAdTrackingInfo.getmBidType());
//                    jsonObject.put("msg", extraMsg);
//                    jsonObject.put("hourly_frequency", mAdTrackingInfo.getmHourlyFrequency());
//                    jsonObject.put("daily_frequency", mAdTrackingInfo.getmDailyFrequency());
//                    jsonObject.put("network_list", mAdTrackingInfo.getmNetworkList());
//                    jsonObject.put("request_network_num", mAdTrackingInfo.getmRequestNetworkNum());
//                    jsonObject.put("handle_class", getClass().getName());
//                    SDKContext.getInstance().printJson(Const.RESOURCE_HEAD + "_network", jsonObject.toString());
//                } catch (Exception e) {
//
//                }
//            }
//        }
//    }


}
