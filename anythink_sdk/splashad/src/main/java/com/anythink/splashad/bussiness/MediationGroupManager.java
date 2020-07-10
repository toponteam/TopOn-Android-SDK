package com.anythink.splashad.bussiness;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.anythink.core.api.ATAdInfo;
import com.anythink.core.api.AdError;
import com.anythink.core.cap.AdCapV2Manager;
import com.anythink.core.cap.AdPacingManager;
import com.anythink.core.common.CommonAdManager;
import com.anythink.core.common.CommonMediationManager;
import com.anythink.core.common.base.AnyThinkBaseAdapter;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.entity.AdTrackingInfo;
import com.anythink.core.common.net.TrackingV2Loader;
import com.anythink.core.common.track.AdTrackingManager;
import com.anythink.core.common.utils.CommonSDKUtil;
import com.anythink.core.common.utils.TrackingInfoUtil;
import com.anythink.core.common.utils.task.TaskManager;
import com.anythink.core.strategy.PlaceStrategy;
import com.anythink.splashad.api.ATSplashAdListener;
import com.anythink.splashad.bussiness.utils.CustomSplashAdapterParser;
import com.anythink.splashad.unitgroup.api.CustomSplashAdapter;
import com.anythink.splashad.unitgroup.api.CustomSplashListener;

import java.util.List;
import java.util.Map;

/**
 * Splash Mediation Manager
 */
public class MediationGroupManager extends CommonMediationManager {
    private ViewGroup mContainer;// splash container
    private View mSkipView; //skip view


    private CustomSplashAdapter mAdapter;


    boolean hasDismiss;
//    Runnable mCountdownRunnable = new Runnable() {
//        @Override
//        public void run() {
//            mFetchDelay = mFetchDelay - 1000;
//            if (mFetchDelay > 0) {
//                if (mCallbackListener != null) {
//                    mCallbackListener.onAdTick(mFetchDelay);
//                }
//                long delay = 1000L;
//                if (mFetchDelay < 1000) {
//                    delay = mFetchDelay;
//                }
//                SDKContext.getInstance().runOnMainThreadDelayed(mCountdownRunnable, delay);
//            } else {
//                callbackDismiss();
//            }
//        }
//    };

    private void callbackDismiss() {
        if (!hasDismiss) {
            hasDismiss = true;
            if (mAdapter != null && mAdapter.getTrackingInfo() != null) {
                mAdapter.log(Const.LOGKEY.CLOSE, Const.LOGKEY.SUCCESS, "");
            }
            mContainer = null;
            mSkipView = null;

            if (mAdapter != null) {
                mAdapter.clean();
            }

            if (mCallbackListener != null) {
                mCallbackListener.onAdDismiss(ATAdInfo.fromAdapter(mAdapter));
            }


            mCallbackListener = null;
        }
    }

//    private void startCountDown() {
//        if (mSkipView == null) {
//            return;
//        }
//
//        SDKContext.getInstance().runOnMainThread(new Runnable() {
//            @Override
//            public void run() {
//                if (mCallbackListener != null) {
//                    mCallbackListener.onAdTick(mFetchDelay);
//                }
//            }
//        });
//
//        long delay = 1000L;
//        if (mFetchDelay < 1000) {
//            delay = mFetchDelay;
//        }
//        SDKContext.getInstance().runOnMainThreadDelayed(mCountdownRunnable, delay);
//    }


    private CustomSplashListener mCustomSplashListener = new CustomSplashListener() {
        @Override
        public void onSplashAdLoaded(CustomSplashAdapter customSplashAd) {

            mAdapter = customSplashAd;

            onAdLoaded(customSplashAd, null);

        }

        @Override
        public void onSplashAdFailed(CustomSplashAdapter adapter, final AdError adError) {
            onAdError(adapter, adError);
        }

        @Override
        public void onSplashAdShow(final CustomSplashAdapter customSplashAd) {

            TaskManager.getInstance().run_proxy(new Runnable() {
                @Override
                public void run() {
                    if (mIsRelease) {
                        return;
                    }
                    AdTrackingInfo adTrackingInfo = null;
                    if (customSplashAd != null) {

                        adTrackingInfo = customSplashAd.getTrackingInfo();
                        long timestamp = System.currentTimeMillis();
                        adTrackingInfo.setmShowId(CommonSDKUtil.creatImpressionId(adTrackingInfo.getmRequestId(), adTrackingInfo.getmUnitGroupUnitId(), timestamp));

                        /**Must set before AdCacheManager.saveShowTime()，don't suggest to do it in UI-Thread**/
                        TrackingInfoUtil.fillTrackingInfoShowTime(mApplcationContext, adTrackingInfo);

                        AdTrackingManager.getInstance(mApplcationContext).addAdTrackingInfo(TrackingV2Loader.AD_SHOW_TYPE, adTrackingInfo, timestamp);

                        customSplashAd.log(Const.LOGKEY.IMPRESSION, Const.LOGKEY.SUCCESS, "");

                        CommonAdManager commonAdManager = CommonAdManager.getInstance(mCurrentPlacementId);
                        String currentRequestId = commonAdManager != null ? commonAdManager.getCurrentRequestId() : "";

                        if (adTrackingInfo != null) {
                            adTrackingInfo.setCurrentRequestId(currentRequestId);
                        }

//                        AdTrackingManager.getInstance(mApplcationContext).addAdTrackingInfo(TrackingV2Loader.AD_SDK_SHOW_TYPE, adTrackingInfo);
                    }

                    if (mCallbackListener != null) {
                        mCallbackListener.onAdShow(ATAdInfo.fromAdapter(mAdapter));
                    }

//                    startCountDown();

                    if (adTrackingInfo != null) {
                        //Update impression
                        AdCapV2Manager.getInstance(mApplcationContext).saveOneCap(adTrackingInfo.getmAdType(), mCurrentPlacementId, adTrackingInfo.getmUnitGroupUnitId());
                        //Record impression time
                        AdPacingManager.getInstance().savePlacementShowTime(mCurrentPlacementId);
                        AdPacingManager.getInstance().saveUnitGropuShowTime(mCurrentPlacementId, adTrackingInfo.getmUnitGroupUnitId());
                    }
                }
            });

        }

        @Override
        public void onSplashAdClicked(CustomSplashAdapter customSplashAd) {
            if (mIsRelease) {
                return;
            }
            if (customSplashAd != null) {
                AdTrackingInfo adTrackingInfo = customSplashAd.getTrackingInfo();

                AdTrackingManager.getInstance(mApplcationContext).addAdTrackingInfo(TrackingV2Loader.AD_CLICK_TYPE, adTrackingInfo);

                customSplashAd.log(Const.LOGKEY.CLICK, Const.LOGKEY.SUCCESS, "");

            }

            if (mCallbackListener != null) {
                mCallbackListener.onAdClick(ATAdInfo.fromAdapter(mAdapter));
            }
        }

        @Override
        public void onSplashAdDismiss(CustomSplashAdapter customSplashAd) {
            callbackDismiss();
        }

    };

    protected MediationGroupManager(Context context) {
        super(context);
    }

    @Override
    public void onDevelopLoaded() {
        SDKContext.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (mCallbackListener != null) {
                    mCallbackListener.onAdLoaded();
                }
            }
        });

    }

    @Override
    public void onDeveloLoadFail(final AdError adError) {
        SDKContext.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                clean();

                if (mCallbackListener != null) {
                    mCallbackListener.onNoAdError(adError);
                }
                mCallbackListener = null;
            }
        });
    }

    @Override
    public void startLoadAd(AnyThinkBaseAdapter baseAdapter, PlaceStrategy.UnitGroupInfo unitGroupInfo, Map<String, Object> serviceExtras) {
        if (baseAdapter instanceof CustomSplashAdapter && mActivityRef.get() instanceof Activity) {
            CustomSplashAdapterParser.loadSplashAd((Activity) mActivityRef.get(), mContainer, mSkipView, (CustomSplashAdapter) baseAdapter, unitGroupInfo, serviceExtras, null, mCustomSplashListener);
        }
    }


    ATSplashAdListener mCallbackListener;

    public void setCallbackListener(ATSplashAdListener listener) {
        mCallbackListener = listener;
    }


    protected void loadSplashAd(ViewGroup container, View skipView, String placementId, String requestid, PlaceStrategy placeStrategy, List<PlaceStrategy.UnitGroupInfo> list) {

        mContainer = container;
        mSkipView = skipView;
        super.loadAd(placementId, requestid, placeStrategy, list);
    }


    protected void clean() {
        if (mContainer != null) {
            mContainer.setVisibility(View.INVISIBLE);
        }

        if (mAdapter != null) {
            mAdapter.clean();
        }

        mContainer = null;
        mSkipView = null;
    }

    @Override
    public void release() {
        clean();
        super.release();
    }
}
