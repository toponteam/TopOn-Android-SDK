package com.anythink.network.gdt;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.AdError;
import com.anythink.core.api.ErrorCode;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialListener;
import com.qq.e.ads.cfg.VideoOption;
import com.qq.e.ads.interstitial.InterstitialAD;
import com.qq.e.ads.interstitial.InterstitialADListener;
import com.qq.e.ads.interstitial2.UnifiedInterstitialAD;
import com.qq.e.ads.interstitial2.UnifiedInterstitialADListener;
import com.qq.e.ads.interstitial2.UnifiedInterstitialMediaListener;

import java.util.Map;

/**
 * Copyright (C) 2018 {XX} Science and Technology Co., Ltd.
 *
 * @version V{XX_XX}
 * @Author ：Created by zhoushubin on 2018/9/20.
 * @Email: zhoushubin@salmonads.com
 */
public class GDTATInterstitialAdapter extends CustomInterstitialAdapter implements UnifiedInterstitialMediaListener {
    public static String TAG = "GDTInterstitialAdapter";
    InterstitialAD mInterstitialAD;
    UnifiedInterstitialAD mUnifiedInterstitialAd;


    String mAppId;
    String mUnitId;
    boolean isReady = false;

    int mUnitVersion = 0;
    String mIsFullScreen;// 0： normal， 1：full screen

    @Override
    public void loadInterstitialAd(Context context, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, CustomInterstitialListener customInterstitialListener) {

        String posId = "";
        String appid = "";

        if (serverExtras.containsKey("app_id")) {
            appid = serverExtras.get("app_id").toString();
        }

        if (serverExtras.containsKey("unit_id")) {
            posId = serverExtras.get("unit_id").toString();
        }

        if (serverExtras.containsKey("unit_version")) {
            mUnitVersion = Integer.parseInt(serverExtras.get("unit_version").toString());
        }

        mLoadResultListener = customInterstitialListener;
        if (TextUtils.isEmpty(appid) || TextUtils.isEmpty(posId)) {
            if (mLoadResultListener != null) {
                AdError adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", "GTD appid or unitId is empty.");
                mLoadResultListener.onInterstitialAdLoadFail(this, adError);

            }
            return;
        }

        if (!(context instanceof Activity)) {
            if (mLoadResultListener != null) {
                AdError adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", "context must be activity.");
                mLoadResultListener.onInterstitialAdLoadFail(this, adError);
            }
            return;
        }

        mAppId = appid;
        mUnitId = posId;
        isReady = false;


        if (mUnitVersion != 2) {
            mInterstitialAD = new InterstitialAD((Activity) context, mAppId, mUnitId);

            mInterstitialAD.setADListener(new InterstitialADListener() {
                @Override
                public void onADReceive() {
                    isReady = true;
                    if (mLoadResultListener != null) {
                        mLoadResultListener.onInterstitialAdLoaded(GDTATInterstitialAdapter.this);
                    }
                }

                @Override
                public void onNoAD(com.qq.e.comm.util.AdError pAdError) {

                    if (mLoadResultListener != null) {
                        mLoadResultListener.onInterstitialAdLoadFail(GDTATInterstitialAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, String.valueOf(pAdError.getErrorCode()), pAdError.getErrorMsg()));
                    }

                }

                @Override
                public void onADOpened() {
                    if (mImpressListener != null) {
                        mImpressListener.onInterstitialAdShow(GDTATInterstitialAdapter.this);
                    }
                }

                @Override
                public void onADExposure() {
                }

                @Override
                public void onADClicked() {
                    if (mImpressListener != null) {
                        mImpressListener.onInterstitialAdClicked(GDTATInterstitialAdapter.this);
                    }
                }

                @Override
                public void onADLeftApplication() {
                }

                @Override
                public void onADClosed() {
                    isReady = false;
                    if (mImpressListener != null) {
                        mImpressListener.onInterstitialAdClose(GDTATInterstitialAdapter.this);
                    }
                }
            });

            mInterstitialAD.loadAD();
        } else { //2.0

            mIsFullScreen = "0";
            if(serverExtras.containsKey("is_fullscreen")) {
                mIsFullScreen = (String) serverExtras.get("is_fullscreen");
            }

            mUnifiedInterstitialAd = new UnifiedInterstitialAD((Activity) context, mAppId, mUnitId, new UnifiedInterstitialADListener() {
                @Override
                public void onADReceive() {
                    isReady = true;
                    // Interstitial video or full screen
                    mUnifiedInterstitialAd.setMediaListener(GDTATInterstitialAdapter.this);

                    if (mLoadResultListener != null) {
                        mLoadResultListener.onInterstitialAdLoaded(GDTATInterstitialAdapter.this);
                    }
                }

                @Override
                public void onVideoCached() {

                }

                @Override
                public void onNoAD(com.qq.e.comm.util.AdError adError) {
                    if (mLoadResultListener != null) {
                        mLoadResultListener.onInterstitialAdLoadFail(GDTATInterstitialAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, String.valueOf(adError.getErrorCode()), adError.getErrorMsg()));
                    }
                }

                @Override
                public void onADOpened() {
                    if (mImpressListener != null) {
                        mImpressListener.onInterstitialAdShow(GDTATInterstitialAdapter.this);
                    }
                }

                @Override
                public void onADExposure() {

                }

                @Override
                public void onADClicked() {
                    if (mImpressListener != null) {
                        mImpressListener.onInterstitialAdClicked(GDTATInterstitialAdapter.this);
                    }
                }

                @Override
                public void onADLeftApplication() {

                }

                @Override
                public void onADClosed() {
                    isReady = false;
                    if (mImpressListener != null) {
                        mImpressListener.onInterstitialAdClose(GDTATInterstitialAdapter.this);
                    }
                }
            });

            // set video option
            setVideoOption(context, serverExtras);

            if (TextUtils.equals("1", mIsFullScreen)) {//full screen
                mUnifiedInterstitialAd.loadFullScreenAD();
            } else {
                mUnifiedInterstitialAd.loadAD();
            }
        }

    }

    @Override
    public boolean isAdReady() {
        return isReady;
    }

    @Override
    public void show(Context context) {
        if (mInterstitialAD != null) {
            if (context instanceof Activity) {
                mInterstitialAD.show((Activity) context);
            } else {
                mInterstitialAD.show();
            }

        }
        if (mUnifiedInterstitialAd != null) {
            if(TextUtils.equals("1", mIsFullScreen)) {//full screen
                if (context instanceof Activity) {
                    mUnifiedInterstitialAd.showFullScreenAD(((Activity) context));
                } else {
                    Log.e(TAG, "Gdt (Full Screen) show fail: context need be Activity");
                }
            } else {
                if (context instanceof Activity) {
                    mUnifiedInterstitialAd.show(((Activity) context));
                } else {
                    mUnifiedInterstitialAd.show();
                }
                mUnifiedInterstitialAd.show();
            }
        }
    }

    @Override
    public void clean() {

    }

    @Override
    public void onResume() {
    }

    @Override
    public void onPause() {

    }

    @Override
    public String getSDKVersion() {
        return GDTATConst.getNetworkVersion();
    }

    @Override
    public String getNetworkName() {
        return GDTATInitManager.getInstance().getNetworkName();
    }

    /**
     * set video option
     */
    private void setVideoOption(Context context, Map<String, Object> serverExtras) {
        if (mUnifiedInterstitialAd == null) {
            return;
        }

        int isVideoMuted = 1;
        int isVideoAutoPlay = 1;
        String videoDuration = "";
        if (serverExtras.containsKey("video_muted")) {
            isVideoMuted = Integer.parseInt(serverExtras.get("video_muted").toString());
        }
        if (serverExtras.containsKey("video_autoplay")) {
            isVideoAutoPlay = Integer.parseInt(serverExtras.get("video_autoplay").toString());
        }
        if (serverExtras.containsKey("video_duration")) {
            videoDuration = serverExtras.get("video_duration").toString();
        }

        VideoOption option = new VideoOption.Builder()
                .setAutoPlayMuted(isVideoMuted == 1)
                .setAutoPlayPolicy(isVideoAutoPlay)
                .build();
        mUnifiedInterstitialAd.setVideoOption(option);
        if (!TextUtils.isEmpty(videoDuration)) {
            mUnifiedInterstitialAd.setMaxVideoDuration(Integer.parseInt(videoDuration));
        }

        mUnifiedInterstitialAd.setVideoPlayPolicy(getVideoPlayPolicy(option.getAutoPlayPolicy(), context));
    }

    private static int getVideoPlayPolicy(int autoPlayPolicy, Context context) {
        if (autoPlayPolicy == VideoOption.AutoPlayPolicy.ALWAYS) {
            return VideoOption.VideoPlayPolicy.AUTO;
        } else if (autoPlayPolicy == VideoOption.AutoPlayPolicy.WIFI) {
            try {
                ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo wifiNetworkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                return wifiNetworkInfo != null && wifiNetworkInfo.isConnected() ? VideoOption.VideoPlayPolicy.AUTO
                        : VideoOption.VideoPlayPolicy.MANUAL;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (autoPlayPolicy == VideoOption.AutoPlayPolicy.NEVER) {
            return VideoOption.VideoPlayPolicy.MANUAL;
        }
        return VideoOption.VideoPlayPolicy.UNKNOWN;
    }


    @Override
    public void onVideoInit() {

    }

    @Override
    public void onVideoLoading() {

    }

    @Override
    public void onVideoReady(long l) {
    }

    @Override
    public void onVideoStart() {
        if (mImpressListener != null) {
            mImpressListener.onInterstitialAdVideoStart(GDTATInterstitialAdapter.this);
        }
    }

    @Override
    public void onVideoPause() {

    }

    @Override
    public void onVideoComplete() {
        if (mImpressListener != null) {
            mImpressListener.onInterstitialAdVideoEnd(GDTATInterstitialAdapter.this);
        }
    }

    @Override
    public void onVideoError(com.qq.e.comm.util.AdError adError) {
        if (mImpressListener != null) {
            mImpressListener.onInterstitialAdVideoError(GDTATInterstitialAdapter.this, ErrorCode.getErrorCode(ErrorCode.rewardedVideoPlayError, adError.getErrorCode() + "", adError.getErrorMsg()));
        }
    }

    @Override
    public void onVideoPageOpen() {

    }

    @Override
    public void onVideoPageClose() {

    }
}
