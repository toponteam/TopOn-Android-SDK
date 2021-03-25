/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.nativead.api;


import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.anythink.core.api.ATAdInfo;
import com.anythink.core.api.ATBaseAdAdapter;
import com.anythink.core.api.ATNetworkConfirmInfo;
import com.anythink.core.api.ATSDK;
import com.anythink.core.common.AdCacheManager;
import com.anythink.core.common.CommonAdManager;
import com.anythink.core.common.PlacementAdManager;
import com.anythink.core.common.ShowWaterfallManager;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.entity.AdCacheInfo;
import com.anythink.core.common.entity.AdTrackingInfo;
import com.anythink.core.common.net.TrackingV2Loader;
import com.anythink.core.common.track.AdTrackingManager;
import com.anythink.core.common.utils.CommonSDKUtil;
import com.anythink.core.common.utils.TrackingInfoUtil;
import com.anythink.core.common.utils.task.TaskManager;
import com.anythink.nativead.unitgroup.BaseNativeAd;
import com.anythink.nativead.unitgroup.api.CustomNativeAd;

import java.util.List;

public class NativeAd {
    private Context mContext;
    protected BaseNativeAd mBaseNativeAd;
    private ATNativeAdRenderer mAdRender;
    private String mPlacementId;
    private ATNativeEventListener mNativeEventListener;
    private ATNativeDislikeListener mDislikeListener;

    private boolean mRecordedShow;
    private boolean mRecordedImpression;
    private boolean mIsDestroyed;

    private boolean hasSetShowTkDetail;

    private AdCacheInfo mAdCacheInfo;

    protected NativeAd(Context context,
                       final String placementId,
                       AdCacheInfo cacheInfo) {
        mContext = context.getApplicationContext();

        mPlacementId = placementId;

        mAdCacheInfo = cacheInfo;

        mBaseNativeAd = (BaseNativeAd) mAdCacheInfo.getAdObject();
        mBaseNativeAd.setNativeEventListener(new BaseNativeAd.NativeEventListener() {
            @Override
            public void onAdDislikeButtonClick() {
                handleAdDislikeButtonClick(mNativeView);
            }

            @Override
            public void onDeeplinkCallback(boolean isSuccess) {
                handleDeeplinkCallback(mNativeView, isSuccess);
            }

            @Override
            public void onDownloadConfirmCallback(Context context, View clickView, ATNetworkConfirmInfo networkConfirmInfo) {
                handleDownloadConfirm(context, clickView, networkConfirmInfo);
            }

            @Override
            public void onAdImpressed() {
                handleImpression(mNativeView);
            }

            @Override
            public void onAdClicked(View clickView) {
                handleClick(mNativeView, clickView);
            }

            @Override
            public void onAdVideoStart() {
                handleVideoStart(mNativeView);
            }

            @Override
            public void onAdVideoEnd() {
                handleVideoEnd(mNativeView);
            }

            @Override
            public void onAdVideoProgress(int progress) {
                handleVideoProgress(mNativeView, progress);
            }
        });

    }


    ATNativeAdView mNativeView;

    public synchronized void renderAdView(ATNativeAdView view, ATNativeAdRenderer adRender) throws Exception {
        if (mIsDestroyed) {
            return;
        }
        mAdRender = adRender;
        if (mAdRender == null) {
            throw new Exception("Render cannot be null!");
        }

        //clear old info
        try {
            if (mBaseNativeAd != null) {
                mBaseNativeAd.clear(mNativeView);
            }
        } catch (Exception e) {
            if (Const.DEBUG) {
                e.printStackTrace();
            }
        }

        mNativeView = view;
        final AdTrackingInfo adTrackingInfo = mBaseNativeAd.getDetail();
        final View developerView = mAdRender.createView(mContext, adTrackingInfo != null ? adTrackingInfo.getmNetworkType() : 0);

        if (developerView == null) {
            throw new Exception("not set render view!");
        }

        renderViewToWindow(developerView);

    }

    /**
     * Ad Interaction Type
     *
     * @return
     */
    public int getAdInteractionType() {
        if (mBaseNativeAd != null && mBaseNativeAd instanceof CustomNativeAd) {
            return ((CustomNativeAd) mBaseNativeAd).getNativeAdInteractionType();
        }
        return NativeAdInteractionType.UNKNOW;
    }

    private void renderViewToWindow(final View developerView) {
        ATSDK.apiLog(mPlacementId, Const.LOGKEY.API_NATIVE, Const.LOGKEY.API_SHOW, Const.LOGKEY.START, "");

        //Clear previous Ad before render current Ad
//        try {
//            if (mBaseNativeAd != null) {
//                mBaseNativeAd.clear(mNativeView);
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        ViewGroup mCustomAdView = mBaseNativeAd.getCustomAdContainer();

        int hashCode = this.hashCode();

        if (mCustomAdView != null) {
            mCustomAdView.addView(developerView);
        }

        View adView = mCustomAdView == null ? developerView : mCustomAdView;


        mNativeView.renderView(hashCode, adView, new ImpressionEventListener() {
            @Override
            public void onImpression() {
                recordShow(mNativeView);
            }
        });

        mAdRender.renderAdView(developerView, mBaseNativeAd);
    }


    public synchronized void prepare(ATNativeAdView view) {
        prepare(view, null);
    }

    public synchronized void prepare(ATNativeAdView view, FrameLayout.LayoutParams layoutParams) {
        if (mIsDestroyed) {
            return;
        }
        if (view != null) {
            this.prepare(view, null, layoutParams);
        }
    }

    public synchronized void prepare(ATNativeAdView view, List<View> clickViewList, FrameLayout.LayoutParams layoutParams) {
        if (mIsDestroyed) {
            return;
        }
        if (view != null) {
            if (clickViewList != null && clickViewList.size() > 0) {
                mBaseNativeAd.prepare(view, clickViewList, layoutParams);
                bindListener();
            } else {
                mBaseNativeAd.prepare(view, layoutParams);
                bindListener();
            }
        }
    }

    View.OnClickListener mDefaultCloseViewListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mBaseNativeAd != null) {
                mBaseNativeAd.notifyAdDislikeClick();
            }
        }
    };

    private void bindListener() {
        if (mBaseNativeAd instanceof CustomNativeAd) {
            final CustomNativeAd customNativeAd = (CustomNativeAd) this.mBaseNativeAd;

            if (customNativeAd.checkHasCloseViewListener()) {
                return;
            }

            CustomNativeAd.ExtraInfo extraInfo = customNativeAd.getExtraInfo();
            if (extraInfo != null) {
                View closeView = extraInfo.getCloseView();
                if (closeView != null) {
                    closeView.setOnClickListener(mDefaultCloseViewListener);
                }
            }
        }
    }

    public void setNativeEventListener(ATNativeEventListener listener) {
        if (mIsDestroyed) {
            return;
        }
        mNativeEventListener = listener;
    }

    public void setDislikeCallbackListener(ATNativeDislikeListener listener) {
        if (mIsDestroyed) {
            return;
        }
        mDislikeListener = listener;
    }


    DownloadConfirmListener mConfirmListener;

    public void setDownloadConfirmListener(DownloadConfirmListener downloadConfirmListener) {
        if (downloadConfirmListener != null) {
            if (mBaseNativeAd instanceof CustomNativeAd) {
                ((CustomNativeAd) mBaseNativeAd).registerDownloadConfirmListener();
            }
        } else {
            if (mBaseNativeAd instanceof CustomNativeAd) {
                ((CustomNativeAd) mBaseNativeAd).unregeisterDownloadConfirmListener();
            }
        }
        mConfirmListener = downloadConfirmListener;
    }

    public synchronized void clear(ATNativeAdView view) {
        if (mIsDestroyed) {
            return;
        }
        if (mNativeView != null) {
            mNativeView.clearImpressionListener(hashCode());
            mNativeView = null;
        }
        mBaseNativeAd.clear(view);

    }

    public synchronized void destory() {
        if (mIsDestroyed) {
            return;
        }
        clear(mNativeView);
        mIsDestroyed = true;
        mNativeEventListener = null;
        mDislikeListener = null;
        mDefaultCloseViewListener = null;
        mNativeView = null;

        if (mBaseNativeAd != null) {
            mBaseNativeAd.destroy();
        }

    }

    private synchronized void fillShowTrackingInfo(AdTrackingInfo adTrackingInfo) {
        final long timestamp = System.currentTimeMillis();

        if (adTrackingInfo != null && TextUtils.isEmpty(adTrackingInfo.getmShowId())) {
            adTrackingInfo.setmShowId(CommonSDKUtil.creatImpressionId(adTrackingInfo.getmRequestId(), adTrackingInfo.getmUnitGroupUnitId(), timestamp));
        }

        if (!hasSetShowTkDetail) {
            String currentRequestId = ShowWaterfallManager.getInstance().getWaterFallNewestRequestId(mPlacementId);
            hasSetShowTkDetail = true;
            if (adTrackingInfo != null) {
                adTrackingInfo.setCurrentRequestId(currentRequestId);
                /**Must set before AdCacheManager.saveShowTime()，don't suggest to do it in UI-Thread**/
                TrackingInfoUtil.fillTrackingInfoShowTime(mContext, adTrackingInfo);
            }
        }

    }

    // Event Handlers
    synchronized void recordShow(final ATNativeAdView view) {
        if (!mRecordedShow) { //To save show time and send show tracking
            final AdTrackingInfo adTrackingInfo = mBaseNativeAd.getDetail();
            mRecordedShow = true;
            if (mAdCacheInfo != null) {
                /**Mark ad has been showed**/
                mAdCacheInfo.setShowTime(mAdCacheInfo.getShowTime() + 1);

                CommonAdManager adManager = PlacementAdManager.getInstance().getAdManager(mPlacementId);
                if (adManager != null) {
                    adManager.notifyNewestCacheHasBeenShow(mAdCacheInfo);
                    adManager.cancelCountdown();
                }
            }


            TaskManager.getInstance().run_proxy(new Runnable() {
                @Override
                public void run() {
                    if (mIsDestroyed) {
                        return;
                    }
                    if (mAdCacheInfo != null) {
                        /**synchronized to fill show time**/
                        fillShowTrackingInfo(adTrackingInfo);

                        long timestamp = System.currentTimeMillis();

                        try {
                            String[] showIdArray = adTrackingInfo.getmShowId().split("_");
                            timestamp = Long.parseLong(showIdArray[showIdArray.length - 1]);
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }

                        /**Show Tracking**/
                        AdTrackingManager.getInstance(mContext).addAdTrackingInfo(TrackingV2Loader.AD_SDK_SHOW_TYPE, adTrackingInfo, timestamp);


                        AdCacheManager.getInstance().saveShowTimeToDisk(mContext.getApplicationContext(), mAdCacheInfo);
                    }
                }
            });
            ATBaseAdAdapter baseAdAdapter = mAdCacheInfo.getBaseAdapter();
            if (baseAdAdapter != null && !baseAdAdapter.supportImpressionCallback()) {
                if (mBaseNativeAd instanceof CustomNativeAd) {
                    ((CustomNativeAd) mBaseNativeAd).impressionTrack(view);
                }
                handleImpression(view);
            }
        }

    }

    synchronized void handleImpression(final ATNativeAdView view) {
        if (mRecordedImpression || mIsDestroyed) {
            return;
        }

        mRecordedImpression = true;

        TaskManager.getInstance().run_proxy(new Runnable() {
            @Override
            public void run() {
                if (mIsDestroyed) {
                    return;
                }
                try {
                    if (mBaseNativeAd != null) {
                        AdTrackingInfo adTrackingInfo = null;

                        adTrackingInfo = mBaseNativeAd.getDetail();
                        CommonSDKUtil.printAdTrackingInfoStatusLog(adTrackingInfo, Const.LOGKEY.IMPRESSION, Const.LOGKEY.SUCCESS, "");

                        /**synchronized to fill show time**/
                        fillShowTrackingInfo(adTrackingInfo);

                        /**Impression Tracking**/
                        AdTrackingManager.getInstance(mContext.getApplicationContext()).addAdTrackingInfo(TrackingV2Loader.AD_SHOW_TYPE, adTrackingInfo);


                        SDKContext.getInstance().runOnMainThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mNativeEventListener != null) {
                                    mNativeEventListener.onAdImpressed(view,
                                            ATAdInfo.fromBaseAd(mBaseNativeAd));
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.e("NativeAd", "BaseNativeAd has been destotyed.");
                }
            }
        });
    }

    synchronized void handleDeeplinkCallback(ATNativeAdView view, boolean isSuccess) {
        if (mIsDestroyed) {
            return;
        }

        if (mNativeEventListener != null && mNativeEventListener instanceof ATNativeEventExListener) {
            ((ATNativeEventExListener) mNativeEventListener).onDeeplinkCallback(view, ATAdInfo.fromBaseAd(mBaseNativeAd), isSuccess);
        }

    }


    synchronized void handleClick(ATNativeAdView view, View clickView) {
        if (mIsDestroyed) {
            return;
        }

        if (mBaseNativeAd != null) {
            AdTrackingInfo adTrackingInfo = mBaseNativeAd.getDetail();

            CommonSDKUtil.printAdTrackingInfoStatusLog(adTrackingInfo, Const.LOGKEY.CLICK, Const.LOGKEY.SUCCESS, "");

            AdTrackingManager.getInstance(mContext.getApplicationContext()).addAdTrackingInfo(TrackingV2Loader.AD_CLICK_TYPE, adTrackingInfo);
        }

        if (mNativeEventListener != null) {
            mNativeEventListener.onAdClicked(view, ATAdInfo.fromBaseAd(mBaseNativeAd));
        }
    }


    synchronized void handleVideoStart(ATNativeAdView view) {
        if (mIsDestroyed) {
            return;
        }

        if (mBaseNativeAd != null) {
            AdTrackingInfo adTrackingInfo = mBaseNativeAd.getDetail();
            adTrackingInfo.setmProgress(0);
            AdTrackingManager.getInstance(mContext.getApplicationContext()).addAdTrackingInfo(TrackingV2Loader.AD_RV_START_TYPE, adTrackingInfo);
        }


        if (mNativeEventListener != null) {
            mNativeEventListener.onAdVideoStart(view);
        }

    }

    synchronized void handleAdDislikeButtonClick(ATNativeAdView view) {
        if (mIsDestroyed) {
            return;
        }


        if (mDislikeListener != null) {
            mDislikeListener.onAdCloseButtonClick(view, ATAdInfo.fromBaseAd(mBaseNativeAd));
        }

    }

    synchronized void handleVideoEnd(ATNativeAdView view) {
        if (mIsDestroyed) {
            return;
        }

        if (mBaseNativeAd != null) {
            AdTrackingInfo adTrackingInfo = mBaseNativeAd.getDetail();
            adTrackingInfo.setmProgress(100);
            AdTrackingManager.getInstance(mContext.getApplicationContext()).addAdTrackingInfo(TrackingV2Loader.AD_RV_CLOSE_TYPE, adTrackingInfo);
        }

        if (mNativeEventListener != null) {
            mNativeEventListener.onAdVideoEnd(view);
        }

    }

    synchronized void handleVideoProgress(ATNativeAdView view, int progress) {
        if (mIsDestroyed) {
            return;
        }

        if (mNativeEventListener != null) {
            mNativeEventListener.onAdVideoProgress(view, progress);
        }

    }

    synchronized void handleDownloadConfirm(Context context, View clickView, ATNetworkConfirmInfo
            networkConfirmInfo) {
        if (mIsDestroyed) {
            return;
        }

        if (mConfirmListener != null && mBaseNativeAd != null) {
            mConfirmListener.onDownloadConfirm(context != null ? context : mContext, ATAdInfo.fromBaseAd(mBaseNativeAd), clickView, networkConfirmInfo);
        }
    }

    public final void setDownLoadProgressListener(DownLoadProgressListener
                                                          pDownLoadProgressListener) {
        mDownLoadProgressListener = pDownLoadProgressListener;
        mBaseNativeAd.setDownLoadProgressListener(mDownLoadProgressListener);
    }

    public DownLoadProgressListener mDownLoadProgressListener;

    public interface DownLoadProgressListener {
        /***
         * @param status
         * @param description
         * @param progrees
         */
        void onDwonLoadProprees(int status, String description, int progrees);
    }

    public interface DownloadConfirmListener {
        void onDownloadConfirm(Context context, ATAdInfo atAdInfo, View clickView, ATNetworkConfirmInfo networkConfirmInfo);
    }


    public interface ImpressionEventListener {
        void onImpression();
    }

    public void onPause() {
        if (mIsDestroyed) {
            return;
        }
        if (mBaseNativeAd != null) {
            mBaseNativeAd.onPause();
        }
    }

    public void onResume() {
        if (mIsDestroyed) {
            return;
        }
        if (mBaseNativeAd != null) {
            mBaseNativeAd.onResume();
        }
    }

    public ATAdInfo getAdInfo() {
        return ATAdInfo.fromBaseAd(mBaseNativeAd);
    }
}
