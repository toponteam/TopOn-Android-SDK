/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.mintegral;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;
import com.mintegral.msdk.MIntegralConstans;
import com.mintegral.msdk.interstitialvideo.out.InterstitialVideoListener;
import com.mintegral.msdk.interstitialvideo.out.MTGBidInterstitialVideoHandler;
import com.mintegral.msdk.interstitialvideo.out.MTGInterstitialVideoHandler;
import com.mintegral.msdk.mtgbid.out.BidManager;
import com.mintegral.msdk.out.CustomInfoManager;
import com.mintegral.msdk.out.InterstitialListener;
import com.mintegral.msdk.out.MTGInterstitialHandler;

import java.util.HashMap;
import java.util.Map;



public class MintegralATInterstitialAdapter extends CustomInterstitialAdapter {
    private final String TAG = MintegralATInterstitialAdapter.class.getSimpleName();

    MTGBidInterstitialVideoHandler mMvBidIntersititialVideoHandler;
    MTGInterstitialHandler mMvInterstitialHandler;
    MTGInterstitialVideoHandler mMvInterstitialVideoHandler;
    String placementId = "";
    String unitId = "";
    boolean isVideo;
    boolean mIsReady;
    String mPayload;
    String mCustomData = "{}";

    @Override
    public void loadCustomNetworkAd(final Context context, Map<String, Object> serverExtra, Map<String, Object> localExtra) {
        mIsReady = false;
        isVideo = false;

        // appid,appkey,unitid
        String appid = (String) serverExtra.get("appid");
        String appkey = (String) serverExtra.get("appkey");
        unitId = (String) serverExtra.get("unitid");

        if (TextUtils.isEmpty(appid) || TextUtils.isEmpty(appkey) || TextUtils.isEmpty(unitId)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "mintegral appid, appkey or unitid is empty!");
            }
            return;
        }

        if (serverExtra.containsKey("is_video")) {
            if (serverExtra.get("is_video").toString().equals("1")) {
                isVideo = true;
            }
        }

        if (serverExtra.containsKey("payload")) {
            mPayload = serverExtra.get("payload").toString();
        }

        if (serverExtra.containsKey("tp_info")) {
            mCustomData = serverExtra.get("tp_info").toString();
        }

        if (serverExtra.containsKey("placement_id")) {
            placementId = serverExtra.get("placement_id").toString();
        }

        MintegralATInitManager.getInstance().initSDK(context, serverExtra, new MintegralATInitManager.InitCallback() {
            @Override
            public void onSuccess() {
                //init
                init(context);
                //load ad
                startLoad();
            }

            @Override
            public void onError(Throwable e) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError("", e.getMessage());
                }
            }
        });
    }


    /***
     * init
     */
    private void init(Context context) {

        if (isVideo) {
            InterstitialVideoListener videoListener = new InterstitialVideoListener() {

                @Override
                public void onLoadSuccess(String placementId, String unitId) {
                    if (mLoadListener != null) {
                        mLoadListener.onAdDataLoaded();
                    }
                }

                @Override
                public void onVideoLoadSuccess(String placementId, String unitId) {
                    if (mLoadListener != null) {
                        mLoadListener.onAdCacheLoaded();
                    }

                }

                @Override
                public void onVideoLoadFail(String errorMsg) {
                    if (mLoadListener != null) {
                        mLoadListener.onAdLoadError("", errorMsg);
                    }
                }

                @Override
                public void onShowFail(String errorMsg) {
                    if (mImpressListener != null) {
                        mImpressListener.onInterstitialAdVideoError("", errorMsg);
                    }
                }

                @Override
                public void onAdShow() {
                    if (mImpressListener != null) {
                        mImpressListener.onInterstitialAdShow();
                        mImpressListener.onInterstitialAdVideoStart();
                    }
                }

                @Override
                public void onAdClose(boolean isCompleteView) {
                    if (mImpressListener != null) {
                        mImpressListener.onInterstitialAdClose();
                    }
                }

                @Override
                public void onVideoAdClicked(String placementId, String unitId) {
                    if (mImpressListener != null) {
                        mImpressListener.onInterstitialAdClicked();
                    }
                }

                @Override
                public void onVideoComplete(String placementId, String unitId) {
                    if (mImpressListener != null) {
                        mImpressListener.onInterstitialAdVideoEnd();
                    }
                }

                @Override
                public void onAdCloseWithIVReward(boolean b, int i) {

                }

                @Override
                public void onEndcardShow(String placementId, String unitId) {

                }

            };

            if (TextUtils.isEmpty(mPayload)) {
                mMvInterstitialVideoHandler = new MTGInterstitialVideoHandler(context.getApplicationContext(), placementId, unitId);
                // Please use this method"mMtgInterstitalVideoHandler.setRewardVideoListener()" ,if the SDK version is below 9.0.2
                mMvInterstitialVideoHandler.setInterstitialVideoListener(videoListener);
            } else {
                mMvBidIntersititialVideoHandler = new MTGBidInterstitialVideoHandler(context.getApplicationContext(), placementId, unitId);
                mMvBidIntersititialVideoHandler.setInterstitialVideoListener(videoListener);
            }


        } else {
            HashMap<String, Object> hashMap = new HashMap<String, Object>();
            hashMap.put(MIntegralConstans.PROPERTIES_UNIT_ID, unitId);
            hashMap.put(MIntegralConstans.PLACEMENT_ID, placementId);
            mMvInterstitialHandler = new MTGInterstitialHandler(context.getApplicationContext(), hashMap);
            mMvInterstitialHandler.setInterstitialListener(new InterstitialListener() {

                @Override
                public void onInterstitialLoadSuccess() {
                    mIsReady = true;
                    if (mLoadListener != null) {
                        mLoadListener.onAdCacheLoaded();
                    }
                }

                @Override
                public void onInterstitialLoadFail(String s) {
                    if (mLoadListener != null) {
                        mLoadListener.onAdLoadError("", s);
                    }
                }

                @Override
                public void onInterstitialShowSuccess() {
                    mIsReady = false;
                    if (mImpressListener != null) {
                        mImpressListener.onInterstitialAdShow();
                    }
                }

                @Override
                public void onInterstitialShowFail(String s) {
                    Log.e(TAG, "onInterstitialShowFail");
                }

                @Override
                public void onInterstitialClosed() {
                    if (mImpressListener != null) {
                        mImpressListener.onInterstitialAdClose();
                    }
                }

                @Override
                public void onInterstitialAdClick() {
                    if (mImpressListener != null) {
                        mImpressListener.onInterstitialAdClicked();
                    }
                }
            });
        }
    }

    /***
     * load ad
     */
    public void startLoad() {
        if (mMvInterstitialHandler != null) {
            try {
                CustomInfoManager.getInstance().setCustomInfo(unitId, CustomInfoManager.TYPE_LOAD, mCustomData);
            } catch (Throwable e) {
            }
            mMvInterstitialHandler.preload();
        }
        if (mMvInterstitialVideoHandler != null) {
            try {
                CustomInfoManager.getInstance().setCustomInfo(unitId, CustomInfoManager.TYPE_LOAD, mCustomData);
            } catch (Throwable e) {
            }
            mMvInterstitialVideoHandler.load();
        }
        if (mMvBidIntersititialVideoHandler != null) {
            try {
                CustomInfoManager.getInstance().setCustomInfo(unitId, CustomInfoManager.TYPE_BIDLOAD, mCustomData);
            } catch (Throwable e) {
            }
            mMvBidIntersititialVideoHandler.loadFromBid(mPayload);
        }
    }

    @Override
    public boolean isAdReady() {
        if (mMvInterstitialVideoHandler != null) {
            return mMvInterstitialVideoHandler.isReady();
        }

        if (mMvBidIntersititialVideoHandler != null) {
            return mMvBidIntersititialVideoHandler.isBidReady();
        }

        return mIsReady;
    }

    @Override
    public void show(Activity activity) {
        if (mMvInterstitialHandler != null) {
            mMvInterstitialHandler.show();
        }

        if (mMvInterstitialVideoHandler != null) {
            mMvInterstitialVideoHandler.show();
        }

        if (mMvBidIntersititialVideoHandler != null) {
            mMvBidIntersititialVideoHandler.showFromBid();
        }
    }

    @Override
    public String getNetworkName() {
        return MintegralATInitManager.getInstance().getNetworkName();
    }

    @Override
    public void destory() {
        if (mMvBidIntersititialVideoHandler != null) {
            mMvBidIntersititialVideoHandler.setInterstitialVideoListener(null);
            mMvBidIntersititialVideoHandler = null;
        }

        if (mMvInterstitialHandler != null) {
            mMvInterstitialHandler.setInterstitialListener(null);
            mMvInterstitialHandler = null;
        }

        if (mMvInterstitialVideoHandler != null) {
            mMvInterstitialVideoHandler.setInterstitialVideoListener(null);
            mMvInterstitialVideoHandler = null;
        }
    }

    @Override
    public String getNetworkPlacementId() {
        return unitId;
    }

    @Override
    public String getNetworkSDKVersion() {
        return MintegralATInitManager.getInstance().getNetworkVersion();
    }

    @Override
    public String getBiddingToken(Context context) {
        return BidManager.getBuyerUid(context);
    }

}