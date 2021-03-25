/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 *
 */

package com.anythink.network.kidoz;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.anythink.banner.unitgroup.api.CustomBannerAdapter;
import com.kidoz.sdk.api.KidozSDK;
import com.kidoz.sdk.api.ui_views.kidoz_banner.KidozBannerListener;
import com.kidoz.sdk.api.ui_views.new_kidoz_banner.KidozBannerView;

import java.util.Map;

public class KidozATBannerAdapter extends CustomBannerAdapter {

    private static final String TAG = KidozATBannerAdapter.class.getSimpleName();

    private KidozBannerView mKidozBannerView;

    @Override
    public View getBannerView() {
        return mKidozBannerView;
    }

    @Override
    public void loadCustomNetworkAd(final Context context, Map<String, Object> serverExtra, Map<String, Object> localExtra) {

        if (!(context instanceof Activity)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "Kidoz context must be activity.");
            }
            return;
        }

        if (!serverExtra.containsKey("publisher_id")) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "Kidoz publisher_id = null");
            }
            return;
        }

        if (!serverExtra.containsKey("security_token")) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "Kidoz security_token = null");
            }
            return;
        }

        KidozATInitManager.getInstance().initSDK(context, serverExtra, new KidozATInitManager.InitListener() {
            @Override
            public void onSuccess() {
                startLoadAd(((Activity) context));
            }

            @Override
            public void onError(String errorMsg) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError("", "Kidoz " + errorMsg);
                }
            }
        });

    }

    private void startLoadAd(Activity activity) {
        final KidozBannerView kidozBannerView = KidozSDK.getKidozBanner(activity);
        kidozBannerView.setLayoutWithoutShowing();
        kidozBannerView.setKidozBannerListener(new KidozBannerListener() {
            @Override
            public void onBannerViewAdded() {
                //onBannerViewAdded
            }

            @Override
            public void onBannerReady() {
                //onBannerReady
                mKidozBannerView = kidozBannerView;
                mKidozBannerView.show();
                if (mLoadListener != null) {
                    mLoadListener.onAdCacheLoaded();
                }
            }

            @Override
            public void onBannerError(String errorMsg) {
                //onBannerError
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError("", "Kidoz " + errorMsg);
                }
            }

            @Override
            public void onBannerClose() {
                //onBannerClose
                if (mImpressionEventListener != null) {
                    mImpressionEventListener.onBannerAdClose();
                }
            }

            @Override
            public void onBannerNoOffers() {
                //onBannerNoOffers
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError("", "Kidoz no offers");
                }
            }
        });

        kidozBannerView.load();
    }


    @Override
    public void destory() {
        if (mKidozBannerView != null) {
            mKidozBannerView.setKidozBannerListener(null);
            mKidozBannerView.destroy();
            mKidozBannerView = null;
        }
    }

    @Override
    public String getNetworkPlacementId() {
        return "";
    }

    @Override
    public String getNetworkSDKVersion() {
        return KidozATInitManager.getInstance().getNetworkVersion();
    }

    @Override
    public String getNetworkName() {
        return KidozATInitManager.getInstance().getNetworkName();
    }

    @Override
    public boolean supportImpressionCallback() {
        return false;
    }
}
