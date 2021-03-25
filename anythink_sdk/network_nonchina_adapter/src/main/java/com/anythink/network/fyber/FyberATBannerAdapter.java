/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.fyber;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.anythink.banner.unitgroup.api.CustomBannerAdapter;
import com.fyber.inneractive.sdk.external.ImpressionData;
import com.fyber.inneractive.sdk.external.InneractiveAdRequest;
import com.fyber.inneractive.sdk.external.InneractiveAdSpot;
import com.fyber.inneractive.sdk.external.InneractiveAdSpotManager;
import com.fyber.inneractive.sdk.external.InneractiveAdViewEventsListenerWithImpressionData;
import com.fyber.inneractive.sdk.external.InneractiveAdViewUnitController;
import com.fyber.inneractive.sdk.external.InneractiveErrorCode;
import com.fyber.inneractive.sdk.external.InneractiveUnitController;

import java.util.Map;

public class FyberATBannerAdapter extends CustomBannerAdapter {


    InneractiveAdSpot mSpot;
    ViewGroup mContainer;
    private String spotId;

    @Override
    public void loadCustomNetworkAd(final Context activity, Map<String, Object> serverExtras, Map<String, Object> localExtras) {

        String appId = "";
        spotId = "";
        if (serverExtras.containsKey("app_id")) {
            appId = (String) serverExtras.get("app_id");
        }

        if (serverExtras.containsKey("spot_id")) {
            spotId = (String) serverExtras.get("spot_id");
        }

        if (TextUtils.isEmpty(appId) || TextUtils.isEmpty(spotId)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "Fyber app_id、spot_id could not be null.");
            }
            return;
        }

        FyberATInitManager.getInstance().initSDK(activity, serverExtras);

        // spot integration for display Square
        mSpot = InneractiveAdSpotManager.get().createSpot();

        // adding the adview controller
        InneractiveAdViewUnitController controller = new InneractiveAdViewUnitController();
        mSpot.addUnitController(controller);

        InneractiveAdRequest adRequest = new InneractiveAdRequest(spotId);

        // enriching with user and keywords data
        // for gender values see: InneractiveUserConfig.Gender
//        adRequest.setUserParams( new InneractiveUserConfig()
//                .setGender(<gender>)
//                .setZipCode("<zip_code>")
//                .setAge(<age>));
        // Add keywords. Separated by a comma
//        adRequest.setKeywords("pop,rock,music");


        mSpot.setRequestListener(new InneractiveAdSpot.RequestListener() {
            @Override
            public void onInneractiveSuccessfulAdRequest(InneractiveAdSpot inneractiveAdSpot) {

                InneractiveAdViewUnitController controller = (InneractiveAdViewUnitController) mSpot.getSelectedUnitController();
                controller.setEventsListener(new InneractiveAdViewEventsListenerWithImpressionData() {
                    @Override
                    public void onAdImpression(InneractiveAdSpot inneractiveAdSpot, ImpressionData impressionData) {

                    }

                    @Override
                    public void onAdImpression(InneractiveAdSpot inneractiveAdSpot) {
                        if (mImpressionEventListener != null) {
                            mImpressionEventListener.onBannerAdShow();
                        }
                    }

                    @Override
                    public void onAdClicked(InneractiveAdSpot inneractiveAdSpot) {
                        if (mImpressionEventListener != null) {
                            mImpressionEventListener.onBannerAdClicked();
                        }
                    }

                    @Override
                    public void onAdWillCloseInternalBrowser(InneractiveAdSpot inneractiveAdSpot) {

                    }

                    @Override
                    public void onAdWillOpenExternalApp(InneractiveAdSpot inneractiveAdSpot) {

                    }

                    @Override
                    public void onAdEnteredErrorState(InneractiveAdSpot inneractiveAdSpot, InneractiveUnitController.AdDisplayError adDisplayError) {

                    }

                    @Override
                    public void onAdExpanded(InneractiveAdSpot inneractiveAdSpot) {

                    }

                    @Override
                    public void onAdResized(InneractiveAdSpot inneractiveAdSpot) {

                    }

                    @Override
                    public void onAdCollapsed(InneractiveAdSpot inneractiveAdSpot) {

                    }
                });


                // checking if we have ad content
                if (mSpot.isReady()) {

                    mContainer = new FrameLayout(activity);

                    // showing the ad
                    controller.bindView(mContainer);

                    if (mLoadListener != null) {
                        mLoadListener.onAdCacheLoaded();
                    }

                    if (mImpressionEventListener != null) {
                        mImpressionEventListener.onBannerAdShow();
                    }
                } else {
                    if (mLoadListener != null) {
                        mLoadListener.onAdLoadError("", "No fill");
                    }
                }


            }

            @Override
            public void onInneractiveFailedAdRequest(InneractiveAdSpot inneractiveAdSpot, InneractiveErrorCode inneractiveErrorCode) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError("", inneractiveErrorCode.name() + ", " + inneractiveErrorCode.getMetricable());
                }
            }
        });

        //when ready to perform the ad request
        mSpot.requestAd(adRequest);
    }

    @Override
    public View getBannerView() {
        return mContainer;
    }

    @Override
    public String getNetworkSDKVersion() {
        return FyberATInitManager.getInstance().getNetworkVersion();
    }

    @Override
    public void destory() {
        if (mSpot != null) {
            mSpot.setRequestListener(null);
            mSpot.destroy();
            mSpot = null;
        }
        mContainer = null;
    }

    @Override
    public String getNetworkName() {
        return FyberATInitManager.getInstance().getNetworkName();
    }

    @Override
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        return FyberATInitManager.getInstance().setUserDataConsent(context, isConsent, isEUTraffic);
    }

    @Override
    public String getNetworkPlacementId() {
        return spotId;
    }
}
