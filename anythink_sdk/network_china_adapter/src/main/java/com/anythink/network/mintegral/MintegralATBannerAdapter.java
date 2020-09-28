package com.anythink.network.mintegral;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import com.anythink.banner.unitgroup.api.CustomBannerAdapter;
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
                if (mATBannerView != null) {
                    mATBannerView.removeView(mMTGBannerView);
                }
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError("", s);
                }
            }

            @Override
            public void onLoadSuccessed() {
                if (mLoadListener != null) {
                    mLoadListener.onAdCacheLoaded();
                }

                if (mImpressionEventListener != null) {
                    mImpressionEventListener.onBannerAdShow();
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

//        if (bannerView != null) {
//            bannerView.addView(mMTGBannerView, lp);
//        }

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
    public String getNetworkName() {
        return MintegralATInitManager.getInstance().getNetworkName();
    }

    @Override
    public void loadCustomNetworkAd(final Context context, Map<String, Object> serverExtra, Map<String, Object> localExtra) {
        String appid = "";
        String appkey = "";
        if (serverExtra.containsKey("appid")) {
            appid = serverExtra.get("appid").toString();
        }
        if (serverExtra.containsKey("appkey")) {
            appkey = serverExtra.get("appkey").toString();
        }
        if (serverExtra.containsKey("unitid")) {
            unitId = serverExtra.get("unitid").toString();
        }
        if (serverExtra.containsKey("size")) {
            size = serverExtra.get("size").toString();
        }
        if (serverExtra.containsKey("payload")) {
            mPayload = serverExtra.get("payload").toString();
        }
        if (serverExtra.containsKey("placement_id")) {
            placementId = serverExtra.get("placement_id").toString();
        }

        if (serverExtra.containsKey("tp_info")) {
            mCustomData = serverExtra.get("tp_info").toString();
        }

        if (TextUtils.isEmpty(appid) || TextUtils.isEmpty(appkey) || TextUtils.isEmpty(unitId)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "appid、appkey or unitid is empty.");
            }
            return;
        }

        if (!(context instanceof Activity)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "Context must be activity.");
            }
            return;
        }

        mRefreshTime = 0;
        try {
            if (serverExtra.containsKey("nw_rft")) {
                mRefreshTime = Integer.valueOf((String) serverExtra.get("nw_rft"));
                mRefreshTime /= 1000f;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        MintegralATInitManager.getInstance().initSDK(context, serverExtra, new MintegralATInitManager.InitCallback() {
            @Override
            public void onSuccess() {
                startLoad(context);
            }

            @Override
            public void onError(Throwable e) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError("", e.getMessage());
                }
            }
        });
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
    public String getNetworkPlacementId() {
        return unitId;
    }

    @Override
    public String getNetworkSDKVersion() {
        return MintegralATConst.getNetworkVersion();
    }
}
