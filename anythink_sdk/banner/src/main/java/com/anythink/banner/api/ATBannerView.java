/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.banner.api;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.anythink.banner.business.AdLoadManager;
import com.anythink.banner.business.BannerEventListener;
import com.anythink.banner.business.InnerBannerListener;
import com.anythink.banner.unitgroup.api.CustomBannerAdapter;
import com.anythink.core.api.ATAdInfo;
import com.anythink.core.api.ATBaseAdAdapter;
import com.anythink.core.api.ATSDK;
import com.anythink.core.api.AdError;
import com.anythink.core.api.ErrorCode;
import com.anythink.core.common.AdCacheManager;
import com.anythink.core.common.PlacementAdManager;
import com.anythink.core.common.ShowWaterfallManager;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.entity.AdCacheInfo;
import com.anythink.core.common.entity.AdTrackingInfo;
import com.anythink.core.common.net.TrackingV2Loader;
import com.anythink.core.common.track.AdTrackingManager;
import com.anythink.core.common.utils.CommonLogUtil;
import com.anythink.core.common.utils.CommonSDKUtil;
import com.anythink.core.common.utils.TrackingInfoUtil;
import com.anythink.core.common.utils.task.TaskManager;
import com.anythink.core.strategy.PlaceStrategy;
import com.anythink.core.strategy.PlaceStrategyManager;

import java.util.Map;

public class ATBannerView extends FrameLayout {
    private final String TAG = ATBannerView.class.getSimpleName();

    private ATBannerListener mListener;
    private String mPlacementId;

    private AdLoadManager mAdLoadManager;

    boolean hasTouchWindow = false;
    int visibility = 0;

    boolean hasCallbackShow = false;

    CustomBannerAdapter mCustomBannerAd;

    Runnable mRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            loadAd(true);
        }
    };


    private InnerBannerListener mInnerBannerListener = new InnerBannerListener() {

        @Override
        public void onBannerLoaded(final boolean isRefresh) {
            SDKContext.getInstance().runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    synchronized (mAdLoadManager) {
                        //clean previous ad
                        if (mCustomBannerAd != null) {
                            mCustomBannerAd.destory();
                        }

                        AdCacheInfo adCacheInfo = AdCacheManager.getInstance().getCache(getContext(), mPlacementId);

                        CustomBannerAdapter bannerAdapter = null;
                        if (adCacheInfo != null && adCacheInfo.getBaseAdapter() instanceof CustomBannerAdapter) {
                            bannerAdapter = (CustomBannerAdapter) adCacheInfo.getBaseAdapter();
                        }

                        hasCallbackShow = false; //reset the mark of impression

                        if (bannerAdapter != null) {
                            if (isInView() && getVisibility() == VISIBLE) {
                                hasCallbackShow = true;
                                mCustomBannerAd = bannerAdapter;

                                /**Mark ad has been showed**/
                                adCacheInfo.setShowTime(adCacheInfo.getShowTime() + 1);

                                //Add Banner Ad to ATBannerView
                                View networkBannerView = bannerAdapter.getBannerView();
                                int index = indexOfChild(networkBannerView);

                                if (index < 0) {
                                    removeAllViews();
                                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                    params.gravity = Gravity.CENTER;
                                    if (networkBannerView.getParent() != null && networkBannerView.getParent() != ATBannerView.this) {
                                        ((ViewGroup) networkBannerView.getParent()).removeView(networkBannerView);
                                    }
                                    networkBannerView.setLayoutParams(params);
                                    addView(networkBannerView, params);
                                } else {
                                    for (int i = index - 1; i >= 0; i--) {
                                        removeViewAt(i);
                                    }
                                }


                                notifyBannerShow(getContext().getApplicationContext(), adCacheInfo);
                                mCustomBannerAd.setAdEventListener(new BannerEventListener(mInnerBannerListener, mCustomBannerAd, isRefresh));
                                if (mListener != null) {
                                    if (isRefresh) {
                                        mListener.onBannerAutoRefreshed(ATAdInfo.fromAdapter(mCustomBannerAd));
                                    } else {
                                        mListener.onBannerLoaded();
                                        mListener.onBannerShow(ATAdInfo.fromAdapter(mCustomBannerAd));
                                    }
                                }

                                mAdLoadManager.notifyNewestCacheHasBeenShow(adCacheInfo);


                                if (mAdLoadManager != null) {
                                    CommonLogUtil.i(TAG, "in window load success to countDown refresh!");
                                    startAutoRefresh(mRefreshRunnable);
                                }
                            } else {
                                hasCallbackShow = false;
                                if (mListener != null && !isRefresh) {
                                    mListener.onBannerLoaded();
                                }
                            }
                        } else {
                            onBannerFailed(isRefresh, ErrorCode.getErrorCode(ErrorCode.noADError, "", ""));
                        }
                    }
                }
            });
        }

        @Override
        public void onBannerFailed(final boolean isRefresh, final AdError adError) {
            if (mAdLoadManager != null) {
                mAdLoadManager.setLoadFail(adError);
            }
            SDKContext.getInstance().runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    if (mListener != null) {
                        if (isRefresh) {
                            mListener.onBannerAutoRefreshFail(adError);
                        } else {
                            mListener.onBannerFailed(adError);
                        }
                    }
                    if (mAdLoadManager != null && isInView() && getVisibility() == VISIBLE) {
                        CommonLogUtil.i(TAG, "in window load fail to countDown refresh!");
                        if (mAdLoadManager != null && !mAdLoadManager.isLoading()) { //Start timer to refresh banner
                            startAutoRefresh(mRefreshRunnable);
                        }

                    }
                }
            });
        }

        @Override
        public void onBannerClicked(boolean isRefresh, final CustomBannerAdapter customBannerAdapter) {
            SDKContext.getInstance().runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    if (mListener != null) {
                        mListener.onBannerClicked(ATAdInfo.fromAdapter(customBannerAdapter));
                    }
                }
            });
        }

        @Override
        public void onBannerShow(boolean isRefresh) {

        }

        @Override
        public void onBannerClose(boolean isRefresh, final CustomBannerAdapter customBannerAdapter) {
            SDKContext.getInstance().runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    if (mListener != null) {
                        mListener.onBannerClose(ATAdInfo.fromAdapter(customBannerAdapter));
                    }
                }
            });
            //Refresh after closed
            loadAd(true);
        }
    };


    public ATBannerView(Context context) {
        super(context);
    }

    public ATBannerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ATBannerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Deprecated
    public void setUnitId(String placementId) {
        setPlacementId(placementId);
    }

    public void setPlacementId(String placementId) {
        mAdLoadManager = AdLoadManager.getInstance(getContext(), placementId);
        mPlacementId = placementId;
    }

    @Deprecated
    public void setCustomMap(Map<String, String> customMap) {
    }

    /**
     * Mediation Setting Map
     *
     * @param map
     */
    public void setLocalExtra(Map<String, Object> map) {
        if (TextUtils.isEmpty(mPlacementId)) {
            Log.e(TAG, "You must set unit Id first.");
            return;
        }
        PlacementAdManager.getInstance().putPlacementLocalSettingMap(mPlacementId, map);
    }

    public void loadAd() {
        ATSDK.apiLog(mPlacementId, Const.LOGKEY.API_BANNER, Const.LOGKEY.API_LOAD, Const.LOGKEY.START, "");
        loadAd(false);
    }


    boolean mIsRefresh = false;

    private void loadAd(boolean isRefresh) {
        /**Stop timer**/
        mIsRefresh = isRefresh;
        if (mAdLoadManager != null) {
            CommonLogUtil.i(TAG, "start to load to stop countdown refresh!");
            stopAutoRefresh(mRefreshRunnable);
        }

        if (mAdLoadManager != null) {
            mAdLoadManager.startLoadAd(getContext(), this, isRefresh, mInnerBannerListener);
        } else {
            mInnerBannerListener.onBannerFailed(isRefresh, ErrorCode.getErrorCode(ErrorCode.placeStrategyError, "", ""));
        }
    }

    public void setBannerAdListener(ATBannerListener listener) {
        mListener = listener;
    }

    @Deprecated
    public void clean() {
        this.destroy();
    }

    public void destroy() {
        if (mCustomBannerAd != null) {
            mCustomBannerAd.destory();
        }
        if (mAdLoadManager != null) {
            mAdLoadManager.clean();
        }
//        AdCacheManager.getInstance().forceCleanCache(mUnitId);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        hasTouchWindow = true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        hasTouchWindow = false;
        stopAutoRefresh(mRefreshRunnable);
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        controlShow(visibility);
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        controlShow(visibility);
    }

    private void controlShow(int visibility) {
        this.visibility = visibility;
        if (mAdLoadManager == null) {
            return;
        }

        synchronized (mAdLoadManager) {

            if (visibility != VISIBLE || !hasTouchWindow || getVisibility() != VISIBLE) {
                /**Remove the timer if ATBanner is invisible**/
                CommonLogUtil.i(TAG, "no in window to stop refresh!");
                stopAutoRefresh(mRefreshRunnable);
            } else {
                AdCacheInfo adCacheInfo = AdCacheManager.getInstance().getCache(getContext(), mPlacementId);

                CustomBannerAdapter bannerAdapter = null;
                if (adCacheInfo != null && adCacheInfo.getBaseAdapter() instanceof CustomBannerAdapter) {
                    bannerAdapter = (CustomBannerAdapter) adCacheInfo.getBaseAdapter();
                }

                /**Refeshing the ad if exist Ad in ATBannerView**/
                if (bannerAdapter != null || mCustomBannerAd != null) {
                    /**Start the timer if ATBanner is visible**/
                    if (mAdLoadManager != null && !mAdLoadManager.isLoading()) {
                        CommonLogUtil.i(TAG, "first add in window to countDown refresh!");
                        startAutoRefresh(mRefreshRunnable);
                    }
                }

                if (!hasCallbackShow && isInView() && bannerAdapter != null && getVisibility() == VISIBLE) {
                    /**Mark ad has been showed**/
                    adCacheInfo.setShowTime(adCacheInfo.getShowTime() + 1);


                    View bannerView = bannerAdapter.getBannerView();
                    if (bannerView.getParent() != null && bannerView.getParent() != this) {
                        Log.i(TAG, "Banner View already add in other parent!");
                        ((ViewGroup) bannerView.getParent()).removeView(bannerView);
                    }

                    mCustomBannerAd = bannerAdapter;
                    int index = indexOfChild(bannerView);
                    if (index < 0) {
                        removeAllViews();
                        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        params.gravity = Gravity.CENTER;
                        bannerView.setLayoutParams(params);
                        addView(bannerView, params);
                    } else {
                        for (int i = index - 1; i >= 0; i--) {
                            removeViewAt(i);
                        }
                    }

                    notifyBannerShow(getContext().getApplicationContext(), adCacheInfo);
                    /**Set Banner Event Listener**/
                    bannerAdapter.setAdEventListener(new BannerEventListener(mInnerBannerListener, bannerAdapter, mIsRefresh));

                    if (mListener != null) {
                        if (bannerAdapter != null && mIsRefresh) {
                            mListener.onBannerAutoRefreshed(ATAdInfo.fromAdapter(mCustomBannerAd));
                        } else {
                            mListener.onBannerShow(ATAdInfo.fromAdapter(mCustomBannerAd));
                        }
                    }

                    mAdLoadManager.notifyNewestCacheHasBeenShow(adCacheInfo);

                    hasCallbackShow = true;
                }
            }
        }

    }


    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        /**Remove the timer if ATBanner is invisible**/
        if (visibility != VISIBLE || !hasTouchWindow || getVisibility() != VISIBLE || !hasWindowFocus) {
            if (mAdLoadManager != null) {
                CommonLogUtil.i(TAG, "onWindowFocusChanged no in window to stop refresh!");
                stopAutoRefresh(mRefreshRunnable);
            }
        } else {
            /**Start the timer if ATBanner is visible**/
            if (mAdLoadManager != null && !mAdLoadManager.isLoading()) {
                CommonLogUtil.i(TAG, "onWindowFocusChanged first add in window to countDown refresh!");
                startAutoRefresh(mRefreshRunnable);
            }
        }
    }

    private boolean isInView() {
        if (hasTouchWindow && visibility == VISIBLE) {
            return true;
        }
        return false;
    }

    private void startAutoRefresh(Runnable runnable) {
        stopAutoRefresh(runnable);
        PlaceStrategy placeStrategy = PlaceStrategyManager.getInstance(getContext().getApplicationContext()).getPlaceStrategyByAppIdAndPlaceId(mPlacementId);
        if (placeStrategy != null && placeStrategy.getAutoRefresh() == 1) { //Start to refresh
            SDKContext.getInstance().runOnMainThreadDelayed(runnable, placeStrategy.getAutoRefreshTime());
        }
    }

    private void stopAutoRefresh(Runnable runnable) {
        SDKContext.getInstance().removeMainThreadRunnable(runnable);
    }

    private void notifyBannerShow(final Context context, final AdCacheInfo adCacheInfo) {
        ATBaseAdAdapter baseAdAdapter = adCacheInfo.getBaseAdapter();
        final AdTrackingInfo adTrackingInfo = baseAdAdapter.getTrackingInfo();
        final long timestamp = System.currentTimeMillis();
        if (adTrackingInfo != null) {
            adTrackingInfo.setmShowId(CommonSDKUtil.creatImpressionId(adTrackingInfo.getmRequestId(), adTrackingInfo.getmUnitGroupUnitId(), timestamp));
        }

        TaskManager.getInstance().run_proxy(new Runnable() {
            @Override
            public void run() {
                if (adTrackingInfo != null) {
                    /**Debug log**/
                    CommonSDKUtil.printAdTrackingInfoStatusLog(adTrackingInfo, Const.LOGKEY.IMPRESSION, Const.LOGKEY.SUCCESS, "");

                    String placementId = adTrackingInfo.getmPlacementId();
                    String currentRequestId = ShowWaterfallManager.getInstance().getWaterFallNewestRequestId(placementId);
                    adTrackingInfo.setCurrentRequestId(currentRequestId);


                    /**Must set before AdCacheManager.saveShowTime()，don't suggest to do it in UI-Thread**/
                    TrackingInfoUtil.fillTrackingInfoShowTime(context, adTrackingInfo);
                    //Ad Tracking
                    AdTrackingManager.getInstance(context).addAdTrackingInfo(TrackingV2Loader.AD_SDK_SHOW_TYPE, adTrackingInfo, timestamp);
                    AdTrackingManager.getInstance(context).addAdTrackingInfo(TrackingV2Loader.AD_SHOW_TYPE, adTrackingInfo);

                    //保存展示
                    AdCacheManager.getInstance().saveShowTimeToDisk(context.getApplicationContext(), adCacheInfo.getBaseAdapter(), adCacheInfo.isLast());
                }
            }
        });
    }


}
