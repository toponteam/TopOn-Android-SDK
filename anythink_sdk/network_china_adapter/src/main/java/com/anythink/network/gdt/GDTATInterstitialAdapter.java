package com.anythink.network.gdt;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;
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
    public static String TAG = GDTATInterstitialAdapter.class.getSimpleName();
    InterstitialAD mInterstitialAD;
    UnifiedInterstitialAD mUnifiedInterstitialAd;


    String mAppId;
    String mUnitId;
    boolean isReady = false;

    int mUnitVersion = 0;
    String mIsFullScreen;// 0： normal， 1：full screen

    private void startLoadAd(Context context, Map<String, Object> serverExtra) {
        if (mUnitVersion != 2) {
            mInterstitialAD = new InterstitialAD((Activity) context, mAppId, mUnitId);

            mInterstitialAD.setADListener(new InterstitialADListener() {
                @Override
                public void onADReceive() {
                    isReady = true;
                    if (mLoadListener != null) {
                        mLoadListener.onAdCacheLoaded();
                    }
                }

                @Override
                public void onNoAD(com.qq.e.comm.util.AdError pAdError) {

                    if (mLoadListener != null) {
                        mLoadListener.onAdLoadError(String.valueOf(pAdError.getErrorCode()), pAdError.getErrorMsg());
                    }

                }

                @Override
                public void onADOpened() {
                    if (mImpressListener != null) {
                        mImpressListener.onInterstitialAdShow();
                    }
                }

                @Override
                public void onADExposure() {
                }

                @Override
                public void onADClicked() {
                    if (mImpressListener != null) {
                        mImpressListener.onInterstitialAdClicked();
                    }
                }

                @Override
                public void onADLeftApplication() {
                }

                @Override
                public void onADClosed() {
                    isReady = false;
                    if (mImpressListener != null) {
                        mImpressListener.onInterstitialAdClose();
                    }
                    if (mInterstitialAD != null) {
                        mInterstitialAD.destroy();
                    }
                }
            });

            mInterstitialAD.loadAD();
        } else { //2.0

            mIsFullScreen = "0";
            if (serverExtra.containsKey("is_fullscreen")) {
                mIsFullScreen = (String) serverExtra.get("is_fullscreen");
            }

            mUnifiedInterstitialAd = new UnifiedInterstitialAD((Activity) context, mUnitId, new UnifiedInterstitialADListener() {
                @Override
                public void onADReceive() {
                    isReady = true;

                    if (mLoadListener != null) {
                        mLoadListener.onAdCacheLoaded();
                    }
                    try {
                        GDTATInitManager.getInstance().put(getTrackingInfo().getmUnitGroupUnitId(), mUnifiedInterstitialAd);
                    } catch (Exception e) {

                    }
                }

                @Override
                public void onVideoCached() {

                }

                @Override
                public void onNoAD(com.qq.e.comm.util.AdError adError) {
                    if (mLoadListener != null) {
                        mLoadListener.onAdLoadError(String.valueOf(adError.getErrorCode()), adError.getErrorMsg());
                    }
                }

                @Override
                public void onADOpened() {
                    if (mImpressListener != null) {
                        mImpressListener.onInterstitialAdShow();
                    }
                }

                @Override
                public void onADExposure() {

                }

                @Override
                public void onADClicked() {
                    if (mImpressListener != null) {
                        mImpressListener.onInterstitialAdClicked();
                    }
                }

                @Override
                public void onADLeftApplication() {

                }

                @Override
                public void onADClosed() {
                    isReady = false;
                    if (mImpressListener != null) {
                        mImpressListener.onInterstitialAdClose();
                    }
                    if (mUnifiedInterstitialAd != null) {
                        mUnifiedInterstitialAd.destroy();
                    }
                    try {
                        GDTATInitManager.getInstance().remove(getTrackingInfo().getmUnitGroupUnitId());
                    } catch (Exception e) {

                    }
                }
            });

            // set video option
            setVideoOption(context, serverExtra);

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
    public void show(Activity activity) {
        if (mInterstitialAD != null) {
            if (activity != null) {
                mInterstitialAD.show(activity);
            } else {
                mInterstitialAD.show();
            }

        }
        if (mUnifiedInterstitialAd != null) {
            // Interstitial video or full screen
            mUnifiedInterstitialAd.setMediaListener(GDTATInterstitialAdapter.this);

            if (TextUtils.equals("1", mIsFullScreen)) {//full screen
                if (activity != null) {
                    mUnifiedInterstitialAd.showFullScreenAD(activity);
                } else {
                    Log.e(TAG, "Gdt (Full Screen) show fail: context need be Activity");
                }
            } else {
                if (activity != null) {
                    mUnifiedInterstitialAd.show(activity);
                } else {
                    mUnifiedInterstitialAd.show();
                }
            }
        }
    }

    @Override
    public String getNetworkName() {
        return GDTATInitManager.getInstance().getNetworkName();
    }

    @Override
    public void loadCustomNetworkAd(final Context context, final Map<String, Object> serverExtra, Map<String, Object> localExtra) {
        String posId = "";
        String appid = "";

        if (serverExtra.containsKey("app_id")) {
            appid = serverExtra.get("app_id").toString();
        }

        if (serverExtra.containsKey("unit_id")) {
            posId = serverExtra.get("unit_id").toString();
        }

        if (serverExtra.containsKey("unit_version")) {
            mUnitVersion = Integer.parseInt(serverExtra.get("unit_version").toString());
        }

        if (TextUtils.isEmpty(appid) || TextUtils.isEmpty(posId)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "GDT appid or unitId is empty.");
            }
            return;
        }

        if (!(context instanceof Activity)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "GDT context must be activity.");
            }
            return;
        }

        mAppId = appid;
        mUnitId = posId;
        isReady = false;


        GDTATInitManager.getInstance().initSDK(context, serverExtra, new GDTATInitManager.OnInitCallback() {
            @Override
            public void onSuccess() {
                startLoadAd(context, serverExtra);
            }

            @Override
            public void onError() {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError("", "GDT initSDK failed.");
                }
            }
        });
    }

    @Override
    public void destory() {
        if (mInterstitialAD != null) {
            mInterstitialAD.setADListener(null);
            mInterstitialAD.destroy();
            mInterstitialAD = null;
        }

        if (mUnifiedInterstitialAd != null) {
            mUnifiedInterstitialAd.setMediaListener(null);
            mUnifiedInterstitialAd.destroy();
            mUnifiedInterstitialAd = null;
        }
    }

    @Override
    public String getNetworkPlacementId() {
        return mUnitId;
    }

    @Override
    public String getNetworkSDKVersion() {
        return GDTATConst.getNetworkVersion();
    }

    /**
     * set video option
     */
    private void setVideoOption(Context context, Map<String, Object> serverExtra) {
        if (mUnifiedInterstitialAd == null) {
            return;
        }

        int isVideoMuted = 1;
        int isVideoAutoPlay = 1;
        int videoDuration = -1;
        if (serverExtra.containsKey("video_muted")) {
            isVideoMuted = Integer.parseInt(serverExtra.get("video_muted").toString());
        }
        if (serverExtra.containsKey("video_autoplay")) {
            isVideoAutoPlay = Integer.parseInt(serverExtra.get("video_autoplay").toString());
        }
        if (serverExtra.containsKey("video_duration")) {
            videoDuration = Integer.parseInt(serverExtra.get("video_duration").toString());
        }

        VideoOption option = new VideoOption.Builder()
                .setAutoPlayMuted(isVideoMuted == 1)
                .setAutoPlayPolicy(isVideoAutoPlay)
                .build();
        mUnifiedInterstitialAd.setVideoOption(option);
        if (videoDuration != -1) {
            mUnifiedInterstitialAd.setMaxVideoDuration(videoDuration);
        }

        mUnifiedInterstitialAd.setVideoPlayPolicy(GDTATInitManager.getInstance().getVideoPlayPolicy(context, isVideoAutoPlay));
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
            mImpressListener.onInterstitialAdVideoStart();
        }
    }

    @Override
    public void onVideoPause() {

    }

    @Override
    public void onVideoComplete() {
        if (mImpressListener != null) {
            mImpressListener.onInterstitialAdVideoEnd();
        }
    }

    @Override
    public void onVideoError(com.qq.e.comm.util.AdError adError) {
        if (mImpressListener != null) {
            mImpressListener.onInterstitialAdVideoError(adError.getErrorCode() + "", adError.getErrorMsg());
        }
    }

    @Override
    public void onVideoPageOpen() {

    }

    @Override
    public void onVideoPageClose() {

    }
}
