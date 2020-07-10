package com.anythink.network.flurry;

import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.ErrorCode;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialListener;
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
                if (mLoadResultListener != null) {
                    mLoadResultListener.onInterstitialAdLoaded(FlurryATInterstitialAdapter.this);
                }
            }

            @Override
            public void onRendered(FlurryAdInterstitial pFlurryAdInterstitial) {

            }

            @Override
            public void onDisplay(FlurryAdInterstitial pFlurryAdInterstitial) {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdShow(FlurryATInterstitialAdapter.this);
                }

            }

            @Override
            public void onClose(FlurryAdInterstitial pFlurryAdInterstitial) {
                if (mImpressListener != null && mIsShow) {
                    mIsShow = false;
                    mImpressListener.onInterstitialAdClose(FlurryATInterstitialAdapter.this);
                }
            }

            @Override
            public void onAppExit(FlurryAdInterstitial pFlurryAdInterstitial) {
            }

            @Override
            public void onClicked(FlurryAdInterstitial pFlurryAdInterstitial) {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdClicked(FlurryATInterstitialAdapter.this);
                }
            }

            @Override
            public void onVideoCompleted(FlurryAdInterstitial pFlurryAdInterstitial) {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdVideoEnd(FlurryATInterstitialAdapter.this);
                }
            }

            @Override
            public void onError(FlurryAdInterstitial pFlurryAdInterstitial, FlurryAdErrorType pFlurryAdErrorType, int pI) {
                if (applicationContext != null) {
                    FlurryAgent.onEndSession(applicationContext);
                }
                if (mLoadResultListener != null) {
                    mLoadResultListener.onInterstitialAdLoadFail(FlurryATInterstitialAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, pI + "", pFlurryAdErrorType.toString()));
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
    public void clean() {
        if (check()) {
            mFlurryAdInterstitial.destroy();
        }
    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }


    @Override
    public void loadInterstitialAd(Context context, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, CustomInterstitialListener customInterstitialListener) {
        mLoadResultListener = customInterstitialListener;
        if (context == null) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onInterstitialAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "activity is null."));
            }
            return;
        }

        String sdkKey = "";
        if (serverExtras == null) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onInterstitialAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", " appid or unitid  is empty."));
            }
            return;
        } else {
            sdkKey = ((String) serverExtras.get("sdk_key"));
            placeid = ((String) serverExtras.get("ad_space"));

            if (TextUtils.isEmpty(sdkKey) || TextUtils.isEmpty(placeid)) {
                if (mLoadResultListener != null) {
                    mLoadResultListener.onInterstitialAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "  sdkkey is empty."));
                }
                return;
            }
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

    /***
     * Whether to show
     */
    boolean mIsShow;

    @Override
    public void show(Context context) {
        if (check() && isAdReady()) {
            mIsShow = true;
            mFlurryAdInterstitial.displayAd();
        }
    }


    private boolean check() {
        return mFlurryAdInterstitial != null;
    }

    @Override
    public String getSDKVersion() {
        return FlurryATConst.getNetworkVersion();
    }

    @Override
    public String getNetworkName() {
        return FlurryATInitManager.getInstance().getNetworkName();
    }
}