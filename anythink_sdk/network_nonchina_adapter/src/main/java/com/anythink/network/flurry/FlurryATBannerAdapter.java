package com.anythink.network.flurry;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.anythink.banner.api.ATBannerView;
import com.anythink.banner.unitgroup.api.CustomBannerAdapter;
import com.anythink.banner.unitgroup.api.CustomBannerListener;
import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.ErrorCode;
import com.flurry.android.FlurryAgent;
import com.flurry.android.ads.FlurryAdBanner;
import com.flurry.android.ads.FlurryAdBannerListener;
import com.flurry.android.ads.FlurryAdErrorType;

import java.util.Map;

/**
 * Created by zhou on 2018/6/27.
 */

public class FlurryATBannerAdapter extends CustomBannerAdapter {
    private static final String TAG = FlurryATBannerAdapter.class.getSimpleName();

    CustomBannerListener mListener;
    String placeid = "";

    View mBannerView;
    FlurryAdBanner mFlurryAdBanner;

    /***
     * load ad
     */
    private void startLoad(final Context context) {

        final Context applicationContext = context.getApplicationContext();

        final RelativeLayout relativeLayout = new RelativeLayout(context);
        relativeLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 100));
        mFlurryAdBanner = new FlurryAdBanner(context, relativeLayout, placeid);
        mFlurryAdBanner.setListener(new FlurryAdBannerListener() {
            @Override
            public void onFetched(FlurryAdBanner flurryAdBanner) {
                mBannerView = relativeLayout;
                if (mListener != null) {
                    mListener.onBannerAdLoaded(FlurryATBannerAdapter.this);
                }
                mFlurryAdBanner.displayAd();

                if (applicationContext != null) {
                    FlurryAgent.onEndSession(applicationContext);
                }
            }

            @Override
            public void onRendered(FlurryAdBanner flurryAdBanner) {
            }

            @Override
            public void onShowFullscreen(FlurryAdBanner flurryAdBanner) {

            }

            @Override
            public void onCloseFullscreen(FlurryAdBanner flurryAdBanner) {
                if (mListener != null) {
                    mListener.onBannerAdClose(FlurryATBannerAdapter.this);
                }
            }

            @Override
            public void onAppExit(FlurryAdBanner flurryAdBanner) {

            }

            @Override
            public void onClicked(FlurryAdBanner flurryAdBanner) {
                if (mListener != null) {
                    mListener.onBannerAdClicked(FlurryATBannerAdapter.this);
                }
            }

            @Override
            public void onVideoCompleted(FlurryAdBanner flurryAdBanner) {

            }

            @Override
            public void onError(FlurryAdBanner flurryAdBanner, FlurryAdErrorType flurryAdErrorType, int i) {
                if (applicationContext != null) {
                    FlurryAgent.onEndSession(applicationContext);
                }
                if (mListener != null) {
                    mListener.onBannerAdLoadFail(FlurryATBannerAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, i + "", flurryAdErrorType.toString()));
                }
            }
        });

        if (applicationContext != null) {
            FlurryAgent.onStartSession(applicationContext);
        }
        mFlurryAdBanner.fetchAd();
    }

    @Override
    public View getBannerView() {
        return mBannerView;
    }

    @Override
    public void clean() {
        mBannerView = null;
        if (mFlurryAdBanner != null) {
            mFlurryAdBanner.destroy();
            mFlurryAdBanner = null;
        }
    }


    @Override
    public void loadBannerAd(ATBannerView bannerView, Context activity, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, CustomBannerListener customBannerListener) {
        mListener = customBannerListener;
        if (activity == null) {
            if (mListener != null) {
                mListener.onBannerAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "activity is null."));
            }
            return;
        }

        String sdkKey = "";
        if (serverExtras == null) {
            if (mListener != null) {
                mListener.onBannerAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", " serverExtras  is empty."));
            }
            return;
        } else {

            sdkKey = ((String) serverExtras.get("sdk_key"));
            placeid = ((String) serverExtras.get("ad_space"));

            if (TextUtils.isEmpty(sdkKey) || TextUtils.isEmpty(placeid)) {
                if (mListener != null) {
                    mListener.onBannerAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "  sdkkey is empty."));
                }
                return;
            }
        }
        FlurryATInitManager.getInstance().initSDK(activity, serverExtras);
        startLoad(activity);
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