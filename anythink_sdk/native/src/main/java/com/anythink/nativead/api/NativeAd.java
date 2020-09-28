package com.anythink.nativead.api;


import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.anythink.core.api.ATAdInfo;
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

import java.util.List;

/**
 * Created by Z on 2018/1/8.
 */

public class NativeAd {
    private Context mContext;
    protected BaseNativeAd mBaseNativeAd;
    private ATNativeAdRenderer mAdRender;
    private String mAdUnitId;
    private ATNativeEventListener mNativeEventListener;
    private ATNativeDislikeListener mDislikeListener;

    private boolean mRecordedShow;
    private boolean mRecordedImpression;
    private boolean mIsDestroyed;

    private boolean hasSetShowTkDetail;

    private AdCacheInfo mAdCacheInfo;

    protected NativeAd(Context context,
                       final String adUnitId,
                       AdCacheInfo cacheInfo) {
        mContext = context.getApplicationContext();

        mAdUnitId = adUnitId;

        mAdCacheInfo = cacheInfo;

        mBaseNativeAd = (BaseNativeAd) mAdCacheInfo.getAdObject();
        mBaseNativeAd.setNativeEventListener(new BaseNativeAd.NativeEventListener() {
            @Override
            public void onAdDislikeButtonClick() {
                handleAdDislikeButtonClick(mNativeView);
            }

            @Override
            public void onAdClicked() {
                handleClick(mNativeView);
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

        if (!mRecordedShow) { //To save show time and send show tracking
            mRecordedShow = true;
            if (mAdCacheInfo != null) {
                /**Mark ad has been showed**/
                mAdCacheInfo.setShowTime(mAdCacheInfo.getShowTime() + 1);

                CommonAdManager adManager = PlacementAdManager.getInstance().getAdManager(mAdUnitId);
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
                    if (mBaseNativeAd != null) {

                        /**synchronized to fill show time**/
                        String currentRequestId = ShowWaterfallManager.getInstance().getWaterFallNewestRequestId(mAdUnitId);

                        fillNativeTrackinInfoShowTime(adTrackingInfo, currentRequestId);

                        /**Show Tracking**/
                        AdTrackingManager.getInstance(mContext).addAdTrackingInfo(TrackingV2Loader.AD_SDK_SHOW_TYPE, adTrackingInfo);


                        AdCacheManager.getInstance().saveShowTime(mContext.getApplicationContext(), mAdCacheInfo);
                    }
                }
            });
        }

        renderViewToWindow(developerView);

    }

    private synchronized void fillNativeTrackinInfoShowTime(AdTrackingInfo adTrackingInfo, String currentRequestId) {
        if (!hasSetShowTkDetail) {
            hasSetShowTkDetail = true;
            if (adTrackingInfo != null) {
                adTrackingInfo.setCurrentRequestId(currentRequestId);
                /**Must set before AdCacheManager.saveShowTime()，don't suggest to do it in UI-Thread**/
                TrackingInfoUtil.fillTrackingInfoShowTime(mContext, adTrackingInfo);
            }
        }

    }

    private void renderViewToWindow(final View developerView) {
        ATSDK.apiLog(mAdUnitId, Const.LOGKEY.API_NATIVE, Const.LOGKEY.API_SHOW, Const.LOGKEY.START, "");

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
                recordImpression(mNativeView);
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
            mBaseNativeAd.prepare(view, layoutParams);
        }
    }

    public synchronized void prepare(ATNativeAdView view, List<View> clickViewList, FrameLayout.LayoutParams layoutParams) {
        if (mIsDestroyed) {
            return;
        }
        if (view != null) {
            if (clickViewList != null && clickViewList.size() > 0) {
                mBaseNativeAd.prepare(view, clickViewList, layoutParams);
            } else {
                mBaseNativeAd.prepare(view, layoutParams);
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
        mNativeView = null;

        if (mBaseNativeAd != null) {
            mBaseNativeAd.destroy();
            mBaseNativeAd = null;
        }

        mAdCacheInfo = null;
        mContext = null;
    }


    // Event Handlers
    synchronized void recordImpression(final ATNativeAdView view) {
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
                        mBaseNativeAd.log(Const.LOGKEY.IMPRESSION, Const.LOGKEY.SUCCESS, "");

                        /**synchronized to fill show time**/
                        String currentRequestId = ShowWaterfallManager.getInstance().getWaterFallNewestRequestId(mAdUnitId);
                        fillNativeTrackinInfoShowTime(adTrackingInfo, currentRequestId);

                        long timestamp = System.currentTimeMillis();
                        if (adTrackingInfo != null) {
                            adTrackingInfo.setmShowId(CommonSDKUtil.creatImpressionId(adTrackingInfo.getmRequestId(), adTrackingInfo.getmUnitGroupUnitId(), timestamp));
                        }

                        /**Impression Tracking**/
                        AdTrackingManager.getInstance(mContext.getApplicationContext()).addAdTrackingInfo(TrackingV2Loader.AD_SHOW_TYPE, adTrackingInfo, timestamp);

                        SDKContext.getInstance().runOnMainThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mNativeEventListener != null) {
                                    mNativeEventListener.onAdImpressed(view,
                                            ATAdInfo.fromAdTrackingInfo(mBaseNativeAd != null ? mBaseNativeAd.getDetail() : null));
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

    synchronized void handleClick(ATNativeAdView view) {
        if (mIsDestroyed) {
            return;
        }

        if (mBaseNativeAd != null) {
            mBaseNativeAd.log(Const.LOGKEY.CLICK, Const.LOGKEY.SUCCESS, "");
            AdTrackingInfo adTrackingInfo = mBaseNativeAd.getDetail();

            AdTrackingManager.getInstance(mContext.getApplicationContext()).addAdTrackingInfo(TrackingV2Loader.AD_CLICK_TYPE, adTrackingInfo);
        }

        if (mNativeEventListener != null) {
            mNativeEventListener.onAdClicked(view,
                    ATAdInfo.fromAdTrackingInfo(mBaseNativeAd != null ? mBaseNativeAd.getDetail() : null));
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
            mDislikeListener.onAdCloseButtonClick(view, ATAdInfo.fromAdTrackingInfo(mBaseNativeAd != null ? mBaseNativeAd.getDetail() : null));
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

    public final void setDownLoadProgressListener(DownLoadProgressListener pDownLoadProgressListener) {
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
}
