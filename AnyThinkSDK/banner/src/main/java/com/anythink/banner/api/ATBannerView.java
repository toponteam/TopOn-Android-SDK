package com.anythink.banner.api;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.anythink.banner.business.AdLoadManager;
import com.anythink.banner.business.InnerBannerListener;
import com.anythink.banner.unitgroup.api.CustomBannerAdapter;
import com.anythink.core.api.ATAdInfo;
import com.anythink.core.api.ATSDK;
import com.anythink.core.api.AdError;
import com.anythink.core.api.ErrorCode;
import com.anythink.core.common.AdCacheManager;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.entity.AdCacheInfo;
import com.anythink.core.common.utils.CommonLogUtil;
import com.anythink.core.strategy.PlaceStrategy;
import com.anythink.core.strategy.PlaceStrategyManager;

import java.util.Map;

public class ATBannerView extends FrameLayout {
    private final String TAG = ATBannerView.class.getSimpleName();

    private ATBannerListener mListener;
    private String mUnitId;

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
                            mCustomBannerAd.clean();
                        }

                        AdCacheInfo adCacheInfo = AdCacheManager.getInstance().getCache(getContext(), mUnitId, null);

                        CustomBannerAdapter bannerAdapter = null;
                        if (adCacheInfo != null && adCacheInfo.getBaseAdapter() instanceof CustomBannerAdapter) {
                            bannerAdapter = (CustomBannerAdapter) adCacheInfo.getBaseAdapter();
                        }

                        hasCallbackShow = false; //reset the mark of impression

                        if (bannerAdapter != null) {
                            if (!hasCallbackShow && isInView() && getVisibility() == VISIBLE) {
                                hasCallbackShow = true;
                                mCustomBannerAd = bannerAdapter;
                                //Add Banner Ad to ATBannerView
                                int index = indexOfChild(bannerAdapter.getBannerView());
                                if (index < 0) {
                                    removeAllViews();
                                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                    params.gravity = Gravity.CENTER;
                                    addView(bannerAdapter.getBannerView(), params);
                                } else {
                                    for (int i = index - 1; i >= 0; i--) {
                                        removeViewAt(i);
                                    }
                                }
                                bannerAdapter.notfiyShow(getContext().getApplicationContext());
                                if (mListener != null) {
                                    if (bannerAdapter.isRefresh()) {
                                        mListener.onBannerAutoRefreshed(ATAdInfo.fromAdapter(mCustomBannerAd));
                                    } else {
                                        mListener.onBannerLoaded();
                                        mListener.onBannerShow(ATAdInfo.fromAdapter(mCustomBannerAd));
                                    }
                                }

                                //Save Impression
                                AdCacheManager.getInstance().saveShowTime(getContext().getApplicationContext(), adCacheInfo);
                                mAdLoadManager.cancelReturnCache(adCacheInfo);


                                if (mAdLoadManager != null) {
                                    CommonLogUtil.i(TAG, "in window load success to countDown refresh!");
                                    startAutoRefresh(mRefreshRunnable);
                                }
                            } else {
                                hasCallbackShow = false;
                                if (mListener != null && !bannerAdapter.isRefresh()) {
                                    mListener.onBannerLoaded();
                                }
                            }
                        } else {
                            if (mListener != null && !isRefresh) {
                                mListener.onBannerFailed(ErrorCode.getErrorCode(ErrorCode.noADError, "", ""));
                            }
                        }
                    }
                }
            });
        }

        @Override
        public void onBannerFailed(final boolean isRefresh, final AdError adError) {
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
                    if (mAdLoadManager != null && !hasCallbackShow && isInView() && getVisibility() == VISIBLE) {
                        CommonLogUtil.i(TAG, "in window load fail to countDown refresh!");
                        if (mAdLoadManager != null && !mAdLoadManager.isLoading()) { //Start timer to refresh banner
                            startAutoRefresh(mRefreshRunnable);
                        }

                    }
                }
            });
        }

        @Override
        public void onBannerClicked(boolean isRefresh) {
            SDKContext.getInstance().runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    if (mListener != null) {
                        mListener.onBannerClicked(ATAdInfo.fromAdapter(mCustomBannerAd));
                    }
                }
            });
        }

        @Override
        public void onBannerShow(boolean isRefresh) {

        }

        @Override
        public void onBannerClose(boolean isRefresh) {
            SDKContext.getInstance().runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    if (mListener != null) {
                        mListener.onBannerClose();
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

    public void setUnitId(String unitId) {
        mAdLoadManager = AdLoadManager.getInstance(getContext(), unitId);
        mUnitId = unitId;
    }

    @Deprecated
    public void setCustomMap(Map<String, String> customMap) {
    }

    public void loadAd() {
        ATSDK.apiLog(mUnitId, Const.LOGKEY.API_BANNER, Const.LOGKEY.API_LOAD, Const.LOGKEY.START, "");
        loadAd(false);
    }


    private void loadAd(boolean isRefresh) {
        /**Stop timer**/
        if (mAdLoadManager != null) {
            CommonLogUtil.i(TAG, "start to load to stop countdown refresh!");
            stopAutoRefresh(mRefreshRunnable);
        }

        if (mAdLoadManager != null) {
            mAdLoadManager.startLoadAd(this, isRefresh, SDKContext.getInstance().getCustomMap(), mInnerBannerListener);
        } else {
            mInnerBannerListener.onBannerFailed(isRefresh, ErrorCode.getErrorCode(ErrorCode.placeStrategyError, "", ""));
        }
    }

    public void setBannerAdListener(ATBannerListener listener) {
        mListener = listener;
    }

    public void clean() {
        if (mCustomBannerAd != null) {
            mCustomBannerAd.clean();
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
            AdCacheInfo adCacheInfo = AdCacheManager.getInstance().getCache(getContext(), mUnitId, null);

            CustomBannerAdapter bannerAdapter = null;
            if (adCacheInfo != null && adCacheInfo.getBaseAdapter() instanceof CustomBannerAdapter) {
                bannerAdapter = (CustomBannerAdapter) adCacheInfo.getBaseAdapter();
            }

            /**Refeshing the ad if exist Ad in ATBannerView**/
            if (bannerAdapter != null || mCustomBannerAd != null) {
                /**Remove the timer if ATBanner is invisible**/
                if (visibility != VISIBLE || !hasTouchWindow || getVisibility() != VISIBLE) {
                    if (mAdLoadManager != null) {
                        CommonLogUtil.i(TAG, "no in window to stop refresh!");
                        stopAutoRefresh(mRefreshRunnable);
                    }
                } else {
                    /**Start the timer if ATBanner is visible**/
                    if (mAdLoadManager != null && !mAdLoadManager.isLoading()) {
                        CommonLogUtil.i(TAG, "first add in window to countDown refresh!");
                        startAutoRefresh(mRefreshRunnable);
                    }
                }
            }


            if (!hasCallbackShow && isInView() && bannerAdapter != null && getVisibility() == VISIBLE) {
                View bannerView = bannerAdapter.getBannerView();
                if (bannerView.getParent() != null && bannerView.getParent() != this) {
                    Log.i(TAG, "Banner View already add in other parent!");
                    return;
                }

                mCustomBannerAd = bannerAdapter;
                int index = indexOfChild(bannerView);
                if (index < 0) {
                    removeAllViews();
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.gravity = Gravity.CENTER;
                    addView(bannerAdapter.getBannerView(), params);
                } else {
                    for (int i = index - 1; i >= 0; i--) {
                        removeViewAt(i);
                    }
                }
                bannerAdapter.notfiyShow(getContext().getApplicationContext());
                if (mListener != null) {
                    if (bannerAdapter != null && bannerAdapter.isRefresh()) {
                        mListener.onBannerAutoRefreshed(ATAdInfo.fromAdapter(mCustomBannerAd));
                    } else {
                        mListener.onBannerShow(ATAdInfo.fromAdapter(mCustomBannerAd));
                    }
                }
                //保存展示
                AdCacheManager.getInstance().saveShowTime(getContext().getApplicationContext(), adCacheInfo);
                mAdLoadManager.cancelReturnCache(adCacheInfo);

                hasCallbackShow = true;
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
        PlaceStrategy placeStrategy = PlaceStrategyManager.getInstance(getContext().getApplicationContext()).getPlaceStrategyByAppIdAndPlaceId(mUnitId);
        if (placeStrategy != null && placeStrategy.getAutoRefresh() == 1) { //Start to refresh
            SDKContext.getInstance().runOnMainThreadDelayed(runnable, placeStrategy.getAutoRefreshTime());
        }
    }

    private void stopAutoRefresh(Runnable runnable) {
        SDKContext.getInstance().removeMainThreadRunnable(runnable);
    }

}
