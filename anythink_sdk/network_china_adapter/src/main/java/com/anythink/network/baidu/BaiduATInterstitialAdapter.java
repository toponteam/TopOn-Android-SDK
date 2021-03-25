/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.baidu;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;
import com.baidu.mobads.InterstitialAd;
import com.baidu.mobads.InterstitialAdListener;
import com.baidu.mobads.rewardvideo.FullScreenVideoAd;

import java.util.Map;

public class BaiduATInterstitialAdapter extends CustomInterstitialAdapter {

    private static final String TAG = BaiduATInterstitialAdapter.class.getSimpleName();

    InterstitialAd mInterstitialAd;
    FullScreenVideoAd mFullScreenVideoAd;
    private String mAdPlaceId = "";
    private boolean mIsVideo;

    FullScreenVideoAd.FullScreenVideoAdListener mFullScreenVideoAdListener;

    private void startLoadAd(Context context) {
        if (mIsVideo) {
            loadFullScreenVideo(context);
        } else {
            loadInterstitial(context);
        }
    }

    private void loadFullScreenVideo(Context context) {
        mFullScreenVideoAdListener = new FullScreenVideoAd.FullScreenVideoAdListener() {
            @Override
            public void onAdShow() {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdShow();
                    mImpressListener.onInterstitialAdVideoStart();
                }
            }

            @Override
            public void onAdClick() {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdClicked();
                }
            }

            @Override
            public void onAdClose(float v) {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdClose();
                }
            }

            @Override
            public void onAdFailed(String s) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError("", "Baidu: " + s);
                }
            }

            @Override
            public void onVideoDownloadSuccess() {
                if (mLoadListener != null) {
                    mLoadListener.onAdCacheLoaded();
                }
            }

            @Override
            public void onVideoDownloadFailed() {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError("", "Baidu: onVideoDownloadFailed()");
                }
            }

            @Override
            public void playCompletion() {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdVideoEnd();
                }
            }

            @Override
            public void onAdSkip(float v) {

            }
        };

        mFullScreenVideoAd = new FullScreenVideoAd(context, mAdPlaceId, mFullScreenVideoAdListener, false);
        mFullScreenVideoAd.load();
    }

    private void loadInterstitial(Context context) {
        mInterstitialAd = new InterstitialAd(context, mAdPlaceId);
        mInterstitialAd.setListener(new InterstitialAdListener() {

            @Override
            public void onAdReady() {
                if (mLoadListener != null) {
                    mLoadListener.onAdCacheLoaded();

                }
            }

            @Override
            public void onAdPresent() {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdShow();

                }
            }

            @Override
            public void onAdClick(InterstitialAd interstitialAd) {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdClicked();

                }
            }

            @Override
            public void onAdDismissed() {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdClose();

                }
            }

            @Override
            public void onAdFailed(String s) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError("", s);
                }
            }
        });
        mInterstitialAd.loadAd();
    }

    @Override
    public boolean isAdReady() {
        if (mIsVideo) {
            if (mFullScreenVideoAd != null) {
                return mFullScreenVideoAd.isReady();
            }
        } else {
            if (mInterstitialAd != null) {
                return mInterstitialAd.isAdReady();
            }
        }
        return false;
    }

    @Override
    public void show(Activity activity) {
        try {
            if (mIsVideo) {
                if (mFullScreenVideoAd != null) {
                    mFullScreenVideoAd.show();
                }
            } else {
                if (mInterstitialAd != null) {
                    mInterstitialAd.showAd(activity);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public String getNetworkName() {
        return BaiduATInitManager.getInstance().getNetworkName();
    }

    @Override
    public void loadCustomNetworkAd(final Context context, Map<String, Object> serverExtra, Map<String, Object> localExtra) {
        String mAppId = (String) serverExtra.get("app_id");
        mAdPlaceId = (String) serverExtra.get("ad_place_id");

        if (TextUtils.isEmpty(mAppId) || TextUtils.isEmpty(mAdPlaceId)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", " app_id ,ad_place_id is empty.");
            }
            return;
        }

        if (!(context instanceof Activity)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "Baidu context must be activity.");
            }
            return;
        }

        Object unit_type = serverExtra.get("unit_type");
        if (unit_type != null) {
            mIsVideo = TextUtils.equals("1", unit_type.toString());
        }

        BaiduATInitManager.getInstance().initSDK(context, serverExtra, new BaiduATInitManager.InitCallback() {
            @Override
            public void onSuccess() {
                startLoadAd(context);
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
        if (mFullScreenVideoAd != null) {
            mFullScreenVideoAd = null;
            mFullScreenVideoAdListener = null;
        }
        if (mInterstitialAd != null) {
            mInterstitialAd.setListener(null);
            mInterstitialAd.destroy();
            mInterstitialAd = null;
        }
    }

    @Override
    public String getNetworkPlacementId() {
        return mAdPlaceId;
    }

    @Override
    public String getNetworkSDKVersion() {
        return BaiduATInitManager.getInstance().getNetworkVersion();
    }
}
