package com.anythink.splashad.bussiness;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.anythink.core.api.ATAdInfo;
import com.anythink.core.api.ATBaseAdAdapter;
import com.anythink.core.api.AdError;
import com.anythink.core.cap.AdCapV2Manager;
import com.anythink.core.cap.AdPacingManager;
import com.anythink.core.common.CommonMediationManager;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.entity.AdTrackingInfo;
import com.anythink.core.common.net.TrackingV2Loader;
import com.anythink.core.common.track.AdTrackingManager;
import com.anythink.core.common.utils.CommonSDKUtil;
import com.anythink.core.common.utils.TrackingInfoUtil;
import com.anythink.core.common.utils.task.TaskManager;
import com.anythink.splashad.api.ATSplashAdListener;
import com.anythink.splashad.unitgroup.api.CustomSplashAdapter;
import com.anythink.splashad.unitgroup.api.CustomSplashEventListener;

/**
 * Splash Mediation Manager
 */
public class MediationGroupManager extends CommonMediationManager {
    private ViewGroup mContainer;// splash container
//    private View mSkipView; //skip view


    boolean hasDismiss;
    boolean isRelease;

    public void onSplashAdShowHandle(final CustomSplashAdapter customSplashAd) {
        if (isRelease) {
            return;
        }
        TaskManager.getInstance().run_proxy(new Runnable() {
            @Override
            public void run() {
                AdTrackingInfo adTrackingInfo = null;
                if (customSplashAd != null) {

                    adTrackingInfo = customSplashAd.getTrackingInfo();
                    long timestamp = System.currentTimeMillis();
                    adTrackingInfo.setmShowId(CommonSDKUtil.creatImpressionId(adTrackingInfo.getmRequestId(), adTrackingInfo.getmUnitGroupUnitId(), timestamp));

                    /**Must set before AdCacheManager.saveShowTime()，don't suggest to do it in UI-Thread**/
                    TrackingInfoUtil.fillTrackingInfoShowTime(mApplcationContext, adTrackingInfo);

                    AdTrackingManager.getInstance(mApplcationContext).addAdTrackingInfo(TrackingV2Loader.AD_SHOW_TYPE, adTrackingInfo, timestamp);

                    customSplashAd.log(Const.LOGKEY.IMPRESSION, Const.LOGKEY.SUCCESS, "");

                    if (adTrackingInfo != null) {
                        adTrackingInfo.setCurrentRequestId(mRequestId);
                    }

                }

                if (mCallbackListener != null) {
                    mCallbackListener.onAdShow(ATAdInfo.fromAdapter(customSplashAd));
                }


                if (adTrackingInfo != null) {
                    //Update impression
                    AdCapV2Manager.getInstance(mApplcationContext).saveOneCap(adTrackingInfo.getmAdType(), mPlacementId, adTrackingInfo.getmUnitGroupUnitId());
                    //Record impression time
                    AdPacingManager.getInstance().savePlacementShowTime(mPlacementId);
                    AdPacingManager.getInstance().saveUnitGropuShowTime(mPlacementId, adTrackingInfo.getmUnitGroupUnitId());
                }
            }
        });

    }

    public void onSplashAdClickedHandle(CustomSplashAdapter customSplashAd) {
        if (isRelease) {
            return;
        }
        if (customSplashAd != null) {
            AdTrackingInfo adTrackingInfo = customSplashAd.getTrackingInfo();

            AdTrackingManager.getInstance(mApplcationContext).addAdTrackingInfo(TrackingV2Loader.AD_CLICK_TYPE, adTrackingInfo);

            customSplashAd.log(Const.LOGKEY.CLICK, Const.LOGKEY.SUCCESS, "");

        }

        if (mCallbackListener != null) {
            mCallbackListener.onAdClick(ATAdInfo.fromAdapter(customSplashAd));
        }
    }

    public void onSplashAdDismissHandle(CustomSplashAdapter customSplashAd) {
        if (isRelease) {
            return;
        }
        callbackDismiss(customSplashAd);
    }


    protected MediationGroupManager(Context context) {
        super(context);
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
        SDKContext.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (mCallbackListener != null) {
                    mCallbackListener.onAdLoaded();
                }
                mContainer = null;
            }
        });
    }

    @Override
    public void onDeveloLoadFail(final AdError adError) {
        SDKContext.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (mCallbackListener != null) {
                    mCallbackListener.onNoAdError(adError);
                }

                if (mContainer != null) {
                    mContainer.setVisibility(View.INVISIBLE);
                }
                mContainer = null;
                mCallbackListener = null; //Only fail, dismiss or release would set CallbackListener to null.
            }
        });
    }

    private void callbackDismiss(CustomSplashAdapter splashAdapter) {
        if (!hasDismiss) {
            hasDismiss = true;
            if (splashAdapter != null && splashAdapter.getTrackingInfo() != null) {
                splashAdapter.log(Const.LOGKEY.CLOSE, Const.LOGKEY.SUCCESS, "");
            }
            mContainer = null;

            if (mCallbackListener != null) {
                mCallbackListener.onAdDismiss(ATAdInfo.fromAdapter(splashAdapter));
            }

            setCallbackListener(null);//Only fail, dismiss or release would set CallbackListener to null.

            if (splashAdapter != null) {
                splashAdapter.destory();
            }
        }
    }

    @Override
    public void prepareFormatAdapter(ATBaseAdAdapter baseAdapter) {
        if (baseAdapter instanceof CustomSplashAdapter) {
            ((CustomSplashAdapter) baseAdapter).initAdContainer(mContainer);
            ((CustomSplashAdapter) baseAdapter).initSplashImpressionListener(new SplashEventListener((CustomSplashAdapter) baseAdapter));
        }
    }

    ATSplashAdListener mCallbackListener;

    public void setCallbackListener(ATSplashAdListener listener) {
        mCallbackListener = listener;
    }

    public void setContainerView(ViewGroup containerView) {
        mContainer = containerView;
    }


    @Override
    public void release() {
        isRelease = true;
        hasDismiss = true;
        hasReturnResult = true;

        mCallbackListener = null; //Only fail, dismiss or release would set CallbackListener to null.
        mContainer = null;
    }


    @Override
    public void removeFormatCallback() {
    }

    private class SplashEventListener implements CustomSplashEventListener {
        CustomSplashAdapter splashAdapter;

        public SplashEventListener(CustomSplashAdapter splashAdapter) {
            this.splashAdapter = splashAdapter;
        }

        @Override
        public void onSplashAdShow() {
            onSplashAdShowHandle(splashAdapter);
        }

        @Override
        public void onSplashAdClicked() {
            onSplashAdClickedHandle(splashAdapter);
        }

        @Override
        public void onSplashAdDismiss() {
            onSplashAdDismissHandle(splashAdapter);
            if (splashAdapter != null) {
                splashAdapter.cleanImpressionListener();
            }
            setCallbackListener(null); //Only fail or dismiss would set CallbackListener to null.
        }
    }

}
