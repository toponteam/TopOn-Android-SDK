package com.anythink.network.mopub;

import android.app.Activity;
import android.content.Context;

import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;
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
                if (mLoadListener != null) {
                    mLoadListener.onAdCacheLoaded();
                }
            }

            @Override
            public void onInterstitialFailed(MoPubInterstitial interstitial, MoPubErrorCode errorCode) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError(errorCode.getIntCode() + "", errorCode.toString());
                }
            }

            @Override
            public void onInterstitialShown(MoPubInterstitial interstitial) {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdShow();
                }
            }

            @Override
            public void onInterstitialClicked(MoPubInterstitial interstitial) {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdClicked();
                }
            }

            @Override
            public void onInterstitialDismissed(MoPubInterstitial interstitial) {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdClose();
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
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        return MopubATInitManager.getInstance().setUserDataConsent(context, isConsent, isEUTraffic);
    }

    @Override
    public void show(Activity activity) {
        if (mInterstitial != null) {
            mInterstitial.show();
        }
    }

    @Override
    public void destory() {
        if (mInterstitial != null) {
            mInterstitial.destroy();
            mInterstitial = null;
        }
    }


    @Override
    public void loadCustomNetworkAd(final Context context, final Map<String, Object> serverExtras, final Map<String, Object> localExtras) {
        if (serverExtras.containsKey("unitid")) {
            adUnitId = (String) serverExtras.get("unitid");

        } else {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "unitid is empty!");
            }
            return;
        }

        if (!(context instanceof Activity)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "Mopub context must be activity!");
            }
            return;
        }

        postOnMainThread(new Runnable() {
            @Override
            public void run() {
                try {
                    MopubATInitManager.getInstance().initSDK(context.getApplicationContext(), serverExtras, new MopubATInitManager.InitListener() {
                        @Override
                        public void initSuccess() {
                            try {
                                startLoad(context);
                            } catch (Throwable e) {
                                if (mLoadListener != null) {
                                    mLoadListener.onAdLoadError("", e.getMessage());
                                }
                            }
                        }
                    });
                } catch (Throwable e) {
                    if (mLoadListener != null) {
                        mLoadListener.onAdLoadError("", e.getMessage());
                    }
                }
            }
        });
    }

    @Override
    public String getNetworkSDKVersion() {
        return MopubATConst.getNetworkVersion();
    }

    @Override
    public String getNetworkName() {
        return MopubATInitManager.getInstance().getNetworkName();
    }

    @Override
    public String getNetworkPlacementId() {
        return adUnitId;
    }

}