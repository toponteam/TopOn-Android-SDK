package com.anythink.network.mopub;

import android.app.Activity;
import android.content.Context;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.ErrorCode;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialListener;
import com.mopub.common.MoPub;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubInterstitial;

import java.util.Map;

/**
 * Created by Z on 2018/6/27.
 */
public class MopubATInterstitialAdapter extends CustomInterstitialAdapter {
    private final String TAG = MopubATInterstitialAdapter.class.getSimpleName();

    String adUnitId;
    MoPubInterstitial mInterstitial;

    private void startLoad(Context context) {
        mInterstitial = new MoPubInterstitial((Activity) context, adUnitId);
        mInterstitial.setInterstitialAdListener(new MoPubInterstitial.InterstitialAdListener() {
            @Override
            public void onInterstitialLoaded(MoPubInterstitial interstitial) {
                if (mLoadResultListener != null) {
                    mLoadResultListener.onInterstitialAdLoaded(MopubATInterstitialAdapter.this);
                }
            }

            @Override
            public void onInterstitialFailed(MoPubInterstitial interstitial, MoPubErrorCode errorCode) {
                if (mLoadResultListener != null) {
                    mLoadResultListener.onInterstitialAdLoadFail(MopubATInterstitialAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, errorCode.getIntCode() + "", errorCode.toString()));
                }
            }

            @Override
            public void onInterstitialShown(MoPubInterstitial interstitial) {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdShow(MopubATInterstitialAdapter.this);
                }
            }

            @Override
            public void onInterstitialClicked(MoPubInterstitial interstitial) {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdClicked(MopubATInterstitialAdapter.this);
                }
            }

            @Override
            public void onInterstitialDismissed(MoPubInterstitial interstitial) {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdClose(MopubATInterstitialAdapter.this);
                }
            }
        });
        mInterstitial.load();
    }


    @Override
    public boolean isAdReady() {
        if (mInterstitial != null) {
            return mInterstitial.isReady();
        }
        return false;
    }

    @Override
    public void show(Context context) {
        if (mInterstitial != null) {
            mInterstitial.show();
        }
    }

    @Override
    public void clean() {
        if (mInterstitial != null) {
            mInterstitial.destroy();
        }
    }

    @Override
    public void onResume() {
        if (mActivityRef.get() != null) {
            MoPub.onResume(mActivityRef.get());
        }
    }

    @Override
    public void onPause() {
        if (mActivityRef.get() != null) {
            MoPub.onPause(mActivityRef.get());
        }
    }

    @Override
    public void loadInterstitialAd(final Context context, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, CustomInterstitialListener customInterstitialListener) {
        mLoadResultListener = customInterstitialListener;

        if (context == null) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onInterstitialAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "activity is null."));
            }
            return;
        }

        if (serverExtras == null) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onInterstitialAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "This placement's params in server is null!"));
            }
            return;
        } else {
            if (serverExtras.containsKey("unitid")) {
                adUnitId = (String) serverExtras.get("unitid");

            } else {
                if (mLoadResultListener != null) {
                    mLoadResultListener.onInterstitialAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "unitid is empty!"));
                }
                return;
            }
        }

        if (!(context instanceof Activity)) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onInterstitialAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "context must be activity!"));
            }
            return;
        }

        MopubATInitManager.getInstance().initSDK(context.getApplicationContext(), serverExtras, new MopubATInitManager.InitListener() {
            @Override
            public void initSuccess() {
                startLoad(context);
            }
        });
    }

    @Override
    public String getSDKVersion() {
        return MopubATConst.getNetworkVersion();
    }

    @Override
    public String getNetworkName() {
        return MopubATInitManager.getInstance().getNetworkName();
    }

}