package com.anythink.network.mintegral;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import com.anythink.banner.api.ATBannerView;
import com.anythink.banner.unitgroup.api.CustomBannerAdapter;
import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.AdError;
import com.anythink.core.api.ErrorCode;
import com.mintegral.msdk.out.BannerAdListener;
import com.mintegral.msdk.out.BannerSize;
import com.mintegral.msdk.out.CustomInfoManager;
import com.mintegral.msdk.out.MTGBannerView;

import java.util.Map;

public class MintegralATBannerAdapter extends CustomBannerAdapter {

    MTGBannerView mMTGBannerView;

    String unitId = "";
    String placementId = "";
    String size;
    String mPayload;
    String mCustomData = "{}";
    int mRefreshTime;

    @Override
    public void loadCustomNetworkAd(final Context activity, Map<String, Object> serverExtras, Map<String, Object> localExtras) {

        String appid = "";
        String appkey = "";
        if (serverExtras.containsKey("appid")) {
            appid = serverExtras.get("appid").toString();
        }
        if (serverExtras.containsKey("appkey")) {
            appkey = serverExtras.get("appkey").toString();
        }
        if (serverExtras.containsKey("unitid")) {
            unitId = serverExtras.get("unitid").toString();
        }
        if (serverExtras.containsKey("size")) {
            size = serverExtras.get("size").toString();
        }
        if (serverExtras.containsKey("payload")) {
            mPayload = serverExtras.get("payload").toString();
        }
        if (serverExtras.containsKey("placement_id")) {
            placementId = serverExtras.get("placement_id").toString();
        }

        if (serverExtras.containsKey("tp_info")) {
            mCustomData = serverExtras.get("tp_info").toString();
        }

        if (TextUtils.isEmpty(appid) || TextUtils.isEmpty(appkey) || TextUtils.isEmpty(unitId)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "appid、appkey or unitid is empty.");
            }
            return;
        }

        if (!(activity instanceof Activity)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "Context must be activity.");
            }
            return;
        }

        mRefreshTime = 0;
        try {
            if (serverExtras.containsKey("nw_rft")) {
                mRefreshTime = Integer.valueOf((String) serverExtras.get("nw_rft"));
                mRefreshTime /= 1000f;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        MintegralATInitManager.getInstance().initSDK(activity, serverExtras, new MintegralATInitManager.InitCallback() {
            @Override
            public void onSuccess() {
                startLoad(activity);
            }

            @Override
            public void onError(Throwable e) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError("", e.getMessage());
                }
            }
        });
    }

    private void startLoad(Context activity) {
        mMTGBannerView = new MTGBannerView(activity);
        int bannerSize;
        int width;
        int height;
        switch (size) {
            case "320x90":
                bannerSize = BannerSize.LARGE_TYPE;
                width = 320;
                height = 90;
                break;
            case "300x250":
                bannerSize = BannerSize.MEDIUM_TYPE;
                width = 300;
                height = 250;
                break;
            case "smart":
                bannerSize = BannerSize.SMART_TYPE;
                width = FrameLayout.LayoutParams.MATCH_PARENT;
                height = FrameLayout.LayoutParams.MATCH_PARENT;
                break;
            case "320x50":
            default:
                bannerSize = BannerSize.STANDARD_TYPE;
                width = 320;
                height = 50;
                break;
        }
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        lp.gravity = Gravity.CENTER;

        mMTGBannerView.init(new BannerSize(bannerSize, 0, 0), placementId, unitId);

        mMTGBannerView.setBannerAdListener(new BannerAdListener() {
            @Override
            public void onLoadFailed(String s) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError("", s);
                }
            }

            @Override
            public void onLoadSuccessed() {
                if (mLoadListener != null) {
                    mLoadListener.onAdCacheLoaded();
                }
            }

            @Override
            public void onLogImpression() {

            }

            @Override
            public void onClick() {
                if (mImpressionEventListener != null) {
                    mImpressionEventListener.onBannerAdClicked();
                }
            }

            @Override
            public void onLeaveApp() {

            }

            @Override
            public void showFullScreen() {

            }

            @Override
            public void closeFullScreen() {

            }

            @Override
            public void onCloseBanner() {
                if (mImpressionEventListener != null) {
                    mImpressionEventListener.onBannerAdClose();
                }
            }
        });

        if (mRefreshTime > 0) {
            mMTGBannerView.setRefreshTime(mRefreshTime);
        } else {
            mMTGBannerView.setRefreshTime(0);
        }

        if (!TextUtils.isEmpty(mPayload)) {
            try {
                CustomInfoManager.getInstance().setCustomInfo(unitId, CustomInfoManager.TYPE_BIDLOAD, mCustomData);
            } catch (Throwable e) {
            }
            mMTGBannerView.loadFromBid(mPayload);
        } else {
            try {
                CustomInfoManager.getInstance().setCustomInfo(unitId, CustomInfoManager.TYPE_LOAD, mCustomData);
            } catch (Throwable e) {
            }
            mMTGBannerView.load();
        }
    }


    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    @Override
    public View getBannerView() {
        return mMTGBannerView;
    }

    @Override
    public String getNetworkSDKVersion() {
        return MintegralATConst.getNetworkVersion();
    }

    @Override
    public void destory() {
        if (mMTGBannerView != null) {
            mMTGBannerView.setBannerAdListener(null);
            mMTGBannerView.release();
            mMTGBannerView = null;
        }
    }

    @Override
    public String getNetworkName() {
        return MintegralATInitManager.getInstance().getNetworkName();
    }

    @Override
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        return MintegralATInitManager.getInstance().setUserDataConsent(context, isConsent, isEUTraffic);
    }

    @Override
    public String getNetworkPlacementId() {
        return unitId;
    }
}
