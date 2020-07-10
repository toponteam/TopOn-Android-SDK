package com.anythink.interstitial.unitgroup.api;

import android.content.Context;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.common.base.AnyThinkBaseAdapter;

import java.util.Map;

public abstract class CustomInterstitialAdapter extends AnyThinkBaseAdapter {

    protected CustomInterstitialListener mLoadResultListener;
    protected CustomInterstitialEventListener mImpressListener;

    public abstract void loadInterstitialAd(final Context context,
                                            final Map<String, Object> serverExtras,
                                            final ATMediationSetting mediationSetting,
                                            final CustomInterstitialListener customInterstitialListener);

    public abstract void show(Context context);

    public abstract void onResume();

    public abstract void onPause();

    public void setCustomInterstitialEventListener(CustomInterstitialEventListener listener) {
        mImpressListener = listener;
    }

    public void clearLoadListener() {
        mLoadResultListener = null;
    }

    public void clearImpressionListener() {
        mImpressListener = null;
    }

}
