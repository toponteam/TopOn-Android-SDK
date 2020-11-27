package com.anythink.network.flurry;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.anythink.banner.unitgroup.api.CustomBannerAdapter;
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
                if (mLoadListener != null) {
                    mLoadListener.onAdCacheLoaded();
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
                if (mImpressionEventListener != null) {
                    mImpressionEventListener.onBannerAdClose();
                }
            }

            @Override
            public void onAppExit(FlurryAdBanner flurryAdBanner) {

            }

            @Override
            public void onClicked(FlurryAdBanner flurryAdBanner) {
                if (mImpressionEventListener != null) {
                    mImpressionEventListener.onBannerAdClicked();
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
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError(i + "", flurryAdErrorType.toString());
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
    public void destory() {
        mBannerView = null;
        if (mFlurryAdBanner != null) {
            mFlurryAdBanner.setListener(null);
            mFlurryAdBanner.destroy();
            mFlurryAdBanner = null;
        }
    }


    @Override
    public void loadCustomNetworkAd(Context activity, Map<String, Object> serverExtras, Map<String, Object> localExtras) {

        String sdkKey = "";

        sdkKey = ((String) serverExtras.get("sdk_key"));
        placeid = ((String) serverExtras.get("ad_space"));

        if (TextUtils.isEmpty(sdkKey) || TextUtils.isEmpty(placeid)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "sdkkey is empty.");
            }
            return;
        }
        FlurryATInitManager.getInstance().initSDK(activity, serverExtras);
        startLoad(activity);
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
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        return FlurryATInitManager.getInstance().setUserDataConsent(context, isConsent, isEUTraffic);
    }

    @Override
    public String getNetworkPlacementId() {
        return placeid;
    }
}