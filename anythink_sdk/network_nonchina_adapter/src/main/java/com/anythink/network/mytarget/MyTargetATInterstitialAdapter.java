/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 *
 */

package com.anythink.network.mytarget;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;
import com.my.target.ads.InterstitialAd;
import com.my.target.common.MyTargetManager;

import java.util.Map;

public class MyTargetATInterstitialAdapter extends CustomInterstitialAdapter {

    private static final String TAG = MyTargetATInterstitialAdapter.class.getSimpleName();

    private int mSlotId = -1;
    private boolean mIsReady;
    private InterstitialAd mInterstitialAd;
    private String mPayload;

    @Override
    public void show(Activity activity) {
        if (isAdReady()) {
            mIsReady = false;
            mInterstitialAd.show();
        }
    }

    @Override
    public void loadCustomNetworkAd(final Context context, Map<String, Object> serverExtra, Map<String, Object> localExtra) {

        String slotId = (String) serverExtra.get("slot_id");

        if (TextUtils.isEmpty(slotId)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "MyTarget slot_id = null");
            }
            return;
        }

        mSlotId = Integer.parseInt(slotId);

        mPayload = (String) serverExtra.get("payload");

        startLoadAd((context));
    }

    private void startLoadAd(Context context) {
        mInterstitialAd = new InterstitialAd(mSlotId, context);
        mInterstitialAd.setListener(new InterstitialAd.InterstitialAdListener() {
            @Override
            public void onLoad(@NonNull InterstitialAd interstitialAd) {
                mIsReady = true;
                if (mLoadListener != null) {
                    mLoadListener.onAdCacheLoaded();
                }
            }

            @Override
            public void onNoAd(@NonNull String reason, @NonNull InterstitialAd interstitialAd) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError("", "MyTarget " + reason);
                }
            }

            @Override
            public void onClick(@NonNull InterstitialAd interstitialAd) {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdClicked();
                }
            }

            @Override
            public void onDismiss(@NonNull InterstitialAd interstitialAd) {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdClose();
                }
            }

            @Override
            public void onVideoCompleted(@NonNull InterstitialAd interstitialAd) {
//                if (mImpressListener != null) {
//                    mImpressListener.onInterstitialAdVideoEnd();
//                }
            }

            @Override
            public void onDisplay(@NonNull InterstitialAd interstitialAd) {
                mIsReady = false;
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdShow();
                }

//                if (mImpressListener != null) {
//                    mImpressListener.onInterstitialAdVideoStart();
//                }

            }
        });

        if (!TextUtils.isEmpty(mPayload)) {
            mInterstitialAd.loadFromBid(mPayload);
        } else {
            mInterstitialAd.load();
        }
    }

    @Override
    public void destory() {
        if (mInterstitialAd != null) {
            mInterstitialAd.setListener(null);
            mInterstitialAd.destroy();
            mInterstitialAd = null;
        }
    }

    @Override
    public String getNetworkPlacementId() {
        return String.valueOf(mSlotId);
    }

    @Override
    public String getNetworkSDKVersion() {
        return MyTargetATInitManager.getInstance().getNetworkVersion();
    }

    @Override
    public String getNetworkName() {
        return MyTargetATInitManager.getInstance().getNetworkName();
    }

    @Override
    public boolean isAdReady() {
        return mInterstitialAd != null && mIsReady;
    }

    @Override
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        return MyTargetATInitManager.getInstance().setUserDataConsent(context, isConsent, isEUTraffic);
    }

    @Override
    public String getBiddingToken(Context context) {
        return MyTargetManager.getBidderToken(context);
    }

}
