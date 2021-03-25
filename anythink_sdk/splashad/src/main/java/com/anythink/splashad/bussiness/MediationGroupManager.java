/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.splashad.bussiness;

import android.content.Context;

import com.anythink.core.api.ATBaseAdAdapter;
import com.anythink.core.api.AdError;
import com.anythink.core.api.ErrorCode;
import com.anythink.core.common.CommonMediationManager;
import com.anythink.core.common.entity.AdTrackingInfo;
import com.anythink.core.common.track.AgentEventManager;
import com.anythink.core.common.utils.TrackingInfoUtil;
import com.anythink.splashad.unitgroup.api.CustomSplashAdapter;

/**
 * Splash Mediation Manager
 */
public class MediationGroupManager extends CommonMediationManager {


//    boolean hasDismiss;

    int timeout;

//    public void onSplashAdShowHandle(final CustomSplashAdapter customSplashAd) {
//        if (isRelease) {
//            return;
//        }
//        TaskManager.getInstance().run_proxy(new Runnable() {
//            @Override
//            public void run() {
//                AdTrackingInfo adTrackingInfo = null;
//                PlaceStrategy.UnitGroupInfo unitGroupInfo = null;
//                if (customSplashAd != null) {
//
//                    adTrackingInfo = customSplashAd.getTrackingInfo();
//                    long timestamp = System.currentTimeMillis();
//                    adTrackingInfo.setmShowId(CommonSDKUtil.creatImpressionId(adTrackingInfo.getmRequestId(), adTrackingInfo.getmUnitGroupUnitId(), timestamp));
//
//                    /**Must set before AdCacheManager.saveShowTime()，don't suggest to do it in UI-Thread**/
//                    TrackingInfoUtil.fillTrackingInfoShowTime(mApplcationContext, adTrackingInfo);
//
//                    AdTrackingManager.getInstance(mApplcationContext).addAdTrackingInfo(TrackingV2Loader.AD_SHOW_TYPE, adTrackingInfo, timestamp);
//
//                    CommonSDKUtil.printAdTrackingInfoStatusLog(adTrackingInfo, Const.LOGKEY.IMPRESSION, Const.LOGKEY.SUCCESS, "");
//
//                    if (adTrackingInfo != null) {
//                        adTrackingInfo.setCurrentRequestId(mRequestId);
//                    }
//
//                }
//
//                if (mCallbackListener != null) {
//                    mCallbackListener.onAdShow(ATAdInfo.fromAdapter(customSplashAd));
//                }
//
//                //Save AdSource show time
////                AdCacheManager.getInstance().saveShowTimeToDisk(mApplcationContext, customSplashAd, true);
////                if (adTrackingInfo != null) {
////                    //Update impression
////                    AdCapV2Manager.getInstance(mApplcationContext).saveOneCap(adTrackingInfo.getmAdType(), mPlacementId, adTrackingInfo.getmUnitGroupUnitId());
////                    //Record impression time
////                    AdPacingManager.getInstance().savePlacementShowTime(mPlacementId);
////                    AdPacingManager.getInstance().saveUnitGropuShowTime(mPlacementId, adTrackingInfo.getmUnitGroupUnitId());
////                    notifyImpression(unitGroupInfo != null ? unitGroupInfo.ecpm : 0);
////                }
//            }
//        });
//
//    }
//
//    public void onSplashAdClickedHandle(CustomSplashAdapter customSplashAd) {
//        if (isRelease) {
//            return;
//        }
//        if (customSplashAd != null) {
//            AdTrackingInfo adTrackingInfo = customSplashAd.getTrackingInfo();
//
//            AdTrackingManager.getInstance(mApplcationContext).addAdTrackingInfo(TrackingV2Loader.AD_CLICK_TYPE, adTrackingInfo);
//
//            CommonSDKUtil.printAdTrackingInfoStatusLog(adTrackingInfo, Const.LOGKEY.CLICK, Const.LOGKEY.SUCCESS, "");
//
//        }
//
//        if (mCallbackListener != null) {
//            mCallbackListener.onAdClick(ATAdInfo.fromAdapter(customSplashAd));
//        }
//    }
//
//    public void onSplashAdDismissHandle(CustomSplashAdapter customSplashAd) {
//        if (isRelease) {
//            return;
//        }
//        callbackDismiss(customSplashAd);
//    }


    protected MediationGroupManager(Context context) {
        super(context);
    }

    public void setFetchAdTimeout(int timeout) {
        this.timeout = timeout;
    }

    @Override
    protected void onAdError(ATBaseAdAdapter baseAdapter, AdError adError) {
        super.onAdError(baseAdapter, adError);
        if (baseAdapter instanceof CustomSplashAdapter) {
            ((CustomSplashAdapter) baseAdapter).cleanImpressionListener();
        }
    }

    @Override
    public void onDevelopLoaded() {
        if (mCallbackListener != null) {
            mCallbackListener.onCallbackAdLoaded();
        }
        mCallbackListener = null;
    }

    @Override
    public void onDeveloLoadFail(final AdError adError) {
        if (mCallbackListener != null) {
            mCallbackListener.onCallbackNoAdError(adError);
        }
        mCallbackListener = null;
    }

//    private void callbackDismiss(CustomSplashAdapter splashAdapter) {
//        if (!hasDismiss) {
//            hasDismiss = true;
//            if (splashAdapter != null && splashAdapter.getTrackingInfo() != null) {
//                CommonSDKUtil.printAdTrackingInfoStatusLog(splashAdapter.getTrackingInfo(), Const.LOGKEY.CLOSE, Const.LOGKEY.SUCCESS, "");
//            }
//
//            if (mCallbackListener != null) {
//                mCallbackListener.onAdDismiss(ATAdInfo.fromAdapter(splashAdapter));
//            }
//
//            setCallbackListener(null);//Only fail, dismiss or release would set CallbackListener to null.
//
//            if (splashAdapter != null) {
//                splashAdapter.destory();
//            }
//        }
//    }

    @Override
    public void prepareFormatAdapter(ATBaseAdAdapter baseAdapter) {
        if (baseAdapter instanceof CustomSplashAdapter) {
            ((CustomSplashAdapter) baseAdapter).setFetchAdTimeout(timeout);
        }
    }

    AdLoadListener mCallbackListener;

    public void setCallbackListener(AdLoadListener listener) {
        mCallbackListener = listener;
    }


    @Override
    public void release() {
        super.release();
        mCallbackListener = null; //Only fail, dismiss or release would set CallbackListener to null.
    }


    @Override
    public void removeFormatCallback() {
        mCallbackListener = null;
    }

    /**
     * Callback timeout in ATSplash
     */
    public void callbackTimeout() {
        hasLongTimeout = true; //stop waterfall request
        hasReturnResult = true; //stop the result callback tracking
        AdTrackingInfo adTrackingInfo = TrackingInfoUtil.initTrackingInfo(mRequestId, mPlacementId, mUserId, mStrategy, "", mStrategy.getRequestUnitGroupNumber(), mIsRefresh, 0);
        AgentEventManager.onAgentForATToAppLoadFail(adTrackingInfo, ErrorCode.getErrorCode(ErrorCode.timeOutError, "", "Splash FetchAd Timeout."));
    }

//    private class SplashEventListener implements CustomSplashEventListener {
//        CustomSplashAdapter splashAdapter;
//
//        public SplashEventListener(CustomSplashAdapter splashAdapter) {
//            this.splashAdapter = splashAdapter;
//        }
//
//        @Override
//        public void onSplashAdShow() {
//            onSplashAdShowHandle(splashAdapter);
//        }
//
//        @Override
//        public void onSplashAdClicked() {
//            onSplashAdClickedHandle(splashAdapter);
//        }
//
//        @Override
//        public void onSplashAdDismiss() {
//            onSplashAdDismissHandle(splashAdapter);
//            if (splashAdapter != null) {
//                splashAdapter.cleanImpressionListener();
//            }
//            setCallbackListener(null); //Only fail or dismiss would set CallbackListener to null.
//        }
//    }

}
