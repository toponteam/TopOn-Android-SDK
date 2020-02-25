package com.anythink.network.mintegral;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import com.anythink.banner.api.ATBannerView;
import com.anythink.banner.unitgroup.api.CustomBannerAdapter;
import com.anythink.banner.unitgroup.api.CustomBannerListener;
import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.AdError;
import com.anythink.core.api.ErrorCode;
import com.mintegral.msdk.out.BannerAdListener;
import com.mintegral.msdk.out.BannerSize;
import com.mintegral.msdk.out.MTGBannerView;

import java.util.Map;

public class MintegralATBannerAdapter extends CustomBannerAdapter {

    MTGBannerView mMTGBannerView;

    String place_id = "";
    String size;
    String mPayload;
    CustomBannerListener mCustomBannerListener;

    @Override
    public void loadBannerAd(final ATBannerView bannerView, final Context activity, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, CustomBannerListener customBannerListener) {
        mCustomBannerListener = customBannerListener;

        String appid = "";
        String appkey = "";
        if (serverExtras.containsKey("appid")) {
            appid = serverExtras.get("appid").toString();
        }
        if (serverExtras.containsKey("appkey")) {
            appkey = serverExtras.get("appkey").toString();
        }
        if (serverExtras.containsKey("unitid")) {
            place_id = serverExtras.get("unitid").toString();
        }
        if (serverExtras.containsKey("size")) {
            size = serverExtras.get("size").toString();
        }
        if (serverExtras.containsKey("payload")) {
            mPayload = serverExtras.get("payload").toString();
        }

        if (TextUtils.isEmpty(appid) || TextUtils.isEmpty(appkey) || TextUtils.isEmpty(place_id)) {
            if (mCustomBannerListener != null) {
                AdError adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", "appid、appkey or unitid is empty.");
                mCustomBannerListener.onBannerAdLoadFail(this, adError);
            }
            return;
        }

        if (!(activity instanceof Activity)) {
            if (mCustomBannerListener != null) {
                AdError adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", "Context must be activity.");
                mCustomBannerListener.onBannerAdLoadFail(this, adError);
            }
            return;
        }


        MintegralATInitManager.getInstance().initSDK(activity, serverExtras, new MintegralATInitManager.InitCallback() {
            @Override
            public void onSuccess() {
                startLoad(activity, bannerView);
            }

            @Override
            public void onError(Throwable e) {
                if (mCustomBannerListener != null) {
                    AdError adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", e.getMessage());
                    mCustomBannerListener.onBannerAdLoadFail(MintegralATBannerAdapter.this, adError);
                }
            }
        });
    }

    private void startLoad(Context activity, final ATBannerView bannerView) {
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
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(width > 0 ? dip2px(activity, width) : width, height > 0 ? dip2px(activity, height) : height);
        lp.gravity = Gravity.CENTER;

        mMTGBannerView.init(new BannerSize(bannerSize, 0, 0), place_id);

        mMTGBannerView.setBannerAdListener(new BannerAdListener() {
            @Override
            public void onLoadFailed(String s) {
                if (bannerView != null) {
                    bannerView.removeView(mMTGBannerView);
                }
                if (mCustomBannerListener != null) {
                    mCustomBannerListener.onBannerAdLoadFail(MintegralATBannerAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, "", s));
                }
            }

            @Override
            public void onLoadSuccessed() {
            }

            @Override
            public void onLogImpression() {
                if (mCustomBannerListener != null) {
                    mCustomBannerListener.onBannerAdLoaded(MintegralATBannerAdapter.this);
                    mCustomBannerListener.onBannerAdShow(MintegralATBannerAdapter.this);
                }
            }

            @Override
            public void onClick() {
                if (mCustomBannerListener != null) {
                    mCustomBannerListener.onBannerAdClicked(MintegralATBannerAdapter.this);
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
        });

        if (bannerView != null) {
            bannerView.addView(mMTGBannerView, lp);
        }

        if (!TextUtils.isEmpty(mPayload)) {
            mMTGBannerView.loadFromBid(mPayload);
        } else {
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
    public String getSDKVersion() {
        return MintegralATConst.getNetworkVersion();
    }

    @Override
    public void clean() {
        if (mMTGBannerView != null) {
            mMTGBannerView.release();
            mMTGBannerView = null;
        }
    }

    @Override
    public String getNetworkName() {
        return MintegralATInitManager.getInstance().getNetworkName();
    }
}
