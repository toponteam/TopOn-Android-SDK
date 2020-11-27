package com.anythink.network.huawei;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.ViewGroup;

import com.anythink.splashad.unitgroup.api.CustomSplashAdapter;
import com.huawei.hms.ads.AdParam;
import com.huawei.hms.ads.AudioFocusType;
import com.huawei.hms.ads.splash.SplashAdDisplayListener;
import com.huawei.hms.ads.splash.SplashView;

import java.util.Map;

public class HuaweiATSplashAdapter extends CustomSplashAdapter {
    String mAdId;
    SplashView mSplashView;
    Context mApplicationContext;

    SplashActivityLifeCycle mSplashActivityLifeCycleListener;

    @Override
    public void loadCustomNetworkAd(final Context context, Map<String, Object> serverExtras, Map<String, Object> localExtras) {
        if (serverExtras.containsKey("ad_id")) {
            mAdId = (String) serverExtras.get("ad_id");

        } else {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "AdId is empty.");
            }
            return;
        }

        mApplicationContext = context.getApplicationContext();


        int orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        if (serverExtras.containsKey("orientation")) {
            int oriServer = Integer.parseInt(serverExtras.get("orientation").toString());
            switch (oriServer) {
                case 1:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
                case 2:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
            }
        }

        final SplashAdDisplayListener adDisplayListener = new SplashAdDisplayListener() {
            @Override
            public void onAdShowed() {
                // Called when an ad is displayed.
                if (mImpressionListener != null) {
                    mImpressionListener.onSplashAdShow();
                }
            }

            @Override
            public void onAdClick() {
                // Called when an ad is clicked.
                if (mImpressionListener != null) {
                    mImpressionListener.onSplashAdClicked();
                }

                if (mImpressionListener != null) {
                    mImpressionListener.onSplashAdDismiss();
                }
            }
        };


        SplashView.SplashAdLoadListener splashAdLoadListener = new SplashView.SplashAdLoadListener() {
            @Override
            public void onAdLoaded() {
                if (mSplashView != null && mContainer != null) {
                    if (mLoadListener != null) {
                        mLoadListener.onAdCacheLoaded();
                    }

                } else {
                    //Remove HMS Splash View
                    if (mContainer != null && mSplashView != null) {
                        mContainer.addView(mSplashView);
                    }
                    if (mLoadListener != null) {
                        mLoadListener.onAdLoadError("", "Splash View had been released.");
                    }
                }
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                //Remove HMS Splash View
                postOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mContainer != null && mSplashView != null) {
                            mContainer.removeView(mSplashView);
                        }
                    }
                });


                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError(String.valueOf(errorCode), "");
                }
            }

            @Override
            public void onAdDismissed() {
                // Called when the display of an ad is complete. The app home screen is then displayed.
                if (mImpressionListener != null) {
                    mImpressionListener.onSplashAdDismiss();
                }
            }
        };

        if (context instanceof Activity) {
            mSplashActivityLifeCycleListener = new SplashActivityLifeCycle((Activity) context);
            ((Application) mApplicationContext).registerActivityLifecycleCallbacks(mSplashActivityLifeCycleListener);
        }

        // Obtain SplashView.
        mSplashView = new SplashView(context);
        // Set the audio focus preemption policy for a video splash ad.
        mSplashView.setAudioFocusType(AudioFocusType.NOT_GAIN_AUDIO_FOCUS_WHEN_MUTE);
        // Load the ad. AD_ID indicates the ad slot ID.

        mSplashView.setAdDisplayListener(adDisplayListener);
        if (mContainer != null) {
            mContainer.addView(mSplashView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
        AdParam adParam = new AdParam.Builder().build();
        mSplashView.load(mAdId, orientation, adParam, splashAdLoadListener);

        mSplashView.resumeView();


    }

    @Override
    public void destory() {
        try {
            if (mSplashView != null) {
                mSplashView.pauseView();
                mSplashView.destroyView();
                mSplashView = null;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            mSplashView = null;
        }


        try {
            if (mSplashActivityLifeCycleListener != null) {
                ((Application) mApplicationContext).unregisterActivityLifecycleCallbacks(mSplashActivityLifeCycleListener);
                mSplashActivityLifeCycleListener = null;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            mSplashActivityLifeCycleListener = null;
        }

    }

    @Override
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        return HuaweiATInitManager.getInstance().setUserDataConsent(context, isConsent, isEUTraffic);
    }

    @Override
    public String getNetworkPlacementId() {
        return mAdId;
    }

    @Override
    public String getNetworkSDKVersion() {
        return HuaweiATInitManager.getInstance().getNetworkSDKVersion();
    }

    @Override
    public String getNetworkName() {
        return HuaweiATInitManager.getInstance().getNetworkName();
    }

    class SplashActivityLifeCycle implements Application.ActivityLifecycleCallbacks {
        private Activity mActivity;

        private SplashActivityLifeCycle(Activity activity) {
            mActivity = activity;
        }

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

        }

        @Override
        public void onActivityStarted(Activity activity) {

        }

        @Override
        public void onActivityResumed(Activity activity) {
            try {
                if (mSplashView != null && mActivity == activity) {
                    mSplashView.resumeView();
                }
            } catch (Throwable e) {

            }

        }

        @Override
        public void onActivityPaused(Activity activity) {
            try {
                if (mSplashView != null && mActivity == activity) {
                    mSplashView.pauseView();
                }
            } catch (Throwable e) {

            }
        }

        @Override
        public void onActivityStopped(Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            if (activity == mActivity) {
                destory();
            }
        }
    }
}
