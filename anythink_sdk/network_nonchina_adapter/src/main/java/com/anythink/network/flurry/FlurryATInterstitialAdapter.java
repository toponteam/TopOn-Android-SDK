package com.anythink.network.flurry;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;
import com.flurry.android.FlurryAgent;
import com.flurry.android.ads.FlurryAdErrorType;
import com.flurry.android.ads.FlurryAdInterstitial;
import com.flurry.android.ads.FlurryAdInterstitialListener;

import java.util.Map;

/**
 * Created by zhou on 2018/6/27.
 */

public class FlurryATInterstitialAdapter extends CustomInterstitialAdapter {
    private static final String TAG = FlurryATInterstitialAdapter.class.getSimpleName();

    FlurryAdInterstitial mFlurryAdInterstitial;

    String placeid = "";

    /***
     * load ad
     */
    private void startLoad(Context context) {

        final Context applicationContext = context.getApplicationContext();
        mIsShow = false;

        mFlurryAdInterstitial = new FlurryAdInterstitial(context, placeid);
        mFlurryAdInterstitial.setListener(new FlurryAdInterstitialListener() {
            @Override
            public void onFetched(FlurryAdInterstitial pFlurryAdInterstitial) {
                if (applicationContext != null) {
                    FlurryAgent.onEndSession(applicationContext);
                }
                if (mLoadListener != null) {
                    mLoadListener.onAdCacheLoaded();
                }
            }

            @Override
            public void onRendered(FlurryAdInterstitial pFlurryAdInterstitial) {

            }

            @Override
            public void onDisplay(FlurryAdInterstitial pFlurryAdInterstitial) {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdShow();
                }

            }

            @Override
            public void onClose(FlurryAdInterstitial pFlurryAdInterstitial) {
                if (mImpressListener != null && mIsShow) {
                    mIsShow = false;
                    mImpressListener.onInterstitialAdClose();
                }
            }

            @Override
            public void onAppExit(FlurryAdInterstitial pFlurryAdInterstitial) {
            }

            @Override
            public void onClicked(FlurryAdInterstitial pFlurryAdInterstitial) {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdClicked();
                }
            }

            @Override
            public void onVideoCompleted(FlurryAdInterstitial pFlurryAdInterstitial) {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdVideoEnd();
                }
            }

            @Override
            public void onError(FlurryAdInterstitial pFlurryAdInterstitial, FlurryAdErrorType pFlurryAdErrorType, int pI) {
                if (applicationContext != null) {
                    FlurryAgent.onEndSession(applicationContext);
                }
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError(pI + "", pFlurryAdErrorType.toString());
                }
            }
        });

        if (context != null) {
            FlurryAgent.onStartSession(context.getApplicationContext());
        }
        if (check()) {
            mFlurryAdInterstitial.fetchAd();
        }
    }

    @Override
    public void destory() {
        if (check()) {
            mFlurryAdInterstitial.setListener(null);
            mFlurryAdInterstitial.destroy();
            mFlurryAdInterstitial = null;
        }
    }


    @Override
    public void loadCustomNetworkAd(Context context, Map<String, Object> serverExtras, Map<String, Object> localExtras) {

        String sdkKey = "";
        sdkKey = ((String) serverExtras.get("sdk_key"));
        placeid = ((String) serverExtras.get("ad_space"));

        if (TextUtils.isEmpty(sdkKey) || TextUtils.isEmpty(placeid)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "sdkkey is empty.");
            }
            return;
        }
        FlurryATInitManager.getInstance().initSDK(context, serverExtras);
        startLoad(context);
    }

    @Override
    public boolean isAdReady() {
        if (check()) {
            return mFlurryAdInterstitial.isReady();
        }
        return false;
    }

    @Override
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        return FlurryATInitManager.getInstance().setUserDataConsent(context, isConsent, isEUTraffic);
    }

    /***
     * Whether to show
     */
    boolean mIsShow;

    @Override
    public void show(Activity activity) {
        if (check() && isAdReady()) {
            mIsShow = true;
            mFlurryAdInterstitial.displayAd();
        }
    }


    private boolean check() {
        return mFlurryAdInterstitial != null;
    }

    @Override
    public String getNetworkSDKVersion() {
        return FlurryATConst.getNetworkVersion();
    }

    @Override
    public String getNetworkName() {
        return FlurryATInitManager.getInstance().getNetworkName();
    }

    @Override
    public String getNetworkPlacementId() {
        return placeid;
    }
}