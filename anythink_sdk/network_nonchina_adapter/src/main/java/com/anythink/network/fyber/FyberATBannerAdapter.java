package com.anythink.network.fyber;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.anythink.banner.api.ATBannerView;
import com.anythink.banner.unitgroup.api.CustomBannerAdapter;
import com.anythink.banner.unitgroup.api.CustomBannerListener;
import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.AdError;
import com.anythink.core.api.ErrorCode;
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

    @Override
    public void loadBannerAd(final ATBannerView bannerView, final Context activity, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, final CustomBannerListener customBannerListener) {

        //todo  mock data
//        serverExtras.put("app_id", "102960");
//        serverExtras.put("spot_id", "150942");
//        serverExtras.put("spot_id", "150943");//RECTANGLE

//        serverExtras.put("app_id", "112759");
//        serverExtras.put("spot_id", "217617");
        //end  mock data

        String appId = "";
        String spotId = "";
        if (serverExtras.containsKey("app_id")) {
            appId = (String) serverExtras.get("app_id");
        }

        if (serverExtras.containsKey("spot_id")) {
            spotId = (String) serverExtras.get("spot_id");
        }

        if (TextUtils.isEmpty(appId) || TextUtils.isEmpty(spotId)) {
            if (customBannerListener != null) {
                AdError adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", "Fyber app_id、spot_id could not be null.");
                customBannerListener.onBannerAdLoadFail(this, adError);
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

                    }

                    @Override
                    public void onAdClicked(InneractiveAdSpot inneractiveAdSpot) {
                        if (customBannerListener != null) {
                            customBannerListener.onBannerAdClicked(FyberATBannerAdapter.this);
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

                    if (customBannerListener != null) {
                        customBannerListener.onBannerAdLoaded(FyberATBannerAdapter.this);
                        customBannerListener.onBannerAdShow(FyberATBannerAdapter.this);
                    }
                } else {
                    if (customBannerListener != null) {
                        customBannerListener.onBannerAdLoadFail(FyberATBannerAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "No fill"));
                    }
                }


            }

            @Override
            public void onInneractiveFailedAdRequest(InneractiveAdSpot inneractiveAdSpot, InneractiveErrorCode inneractiveErrorCode) {
                if (customBannerListener != null) {
                    customBannerListener.onBannerAdLoadFail(FyberATBannerAdapter.this,
                            ErrorCode.getErrorCode(ErrorCode.noADError, "", inneractiveErrorCode.name() + ", " + inneractiveErrorCode.getMetricable()));
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
    public String getSDKVersion() {
        return FyberATConst.getNetworkVersion();
    }

    @Override
    public void clean() {
        if (mSpot != null) {
            mSpot.destroy();
            mSpot = null;
        }
        mContainer = null;
    }

    @Override
    public String getNetworkName() {
        return FyberATInitManager.getInstance().getNetworkName();
    }
}
