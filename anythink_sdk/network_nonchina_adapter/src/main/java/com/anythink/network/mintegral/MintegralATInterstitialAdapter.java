package com.anythink.network.mintegral;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.AdError;
import com.anythink.core.api.ErrorCode;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;
import com.mintegral.msdk.MIntegralConstans;
import com.mintegral.msdk.interstitialvideo.out.InterstitialVideoListener;
import com.mintegral.msdk.interstitialvideo.out.MTGBidInterstitialVideoHandler;
import com.mintegral.msdk.interstitialvideo.out.MTGInterstitialVideoHandler;
import com.mintegral.msdk.out.CustomInfoManager;
import com.mintegral.msdk.out.InterstitialListener;
import com.mintegral.msdk.out.MTGInterstitialHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Z on 2018/6/27.
 */


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
                        if (isCompleteView) {
                            mImpressListener.onInterstitialAdVideoEnd();
                        }
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

    @Override
    public void loadCustomNetworkAd(final Context context, Map<String, Object> serverExtras, Map<String, Object> localExtras) {
        mIsReady = false;
        isVideo = false;
        // appid,appkey,unitid
        String appid = (String) serverExtras.get("appid");
        String appkey = (String) serverExtras.get("appkey");
        unitId = (String) serverExtras.get("unitid");

        if (TextUtils.isEmpty(appid) || TextUtils.isEmpty(appkey) || TextUtils.isEmpty(unitId)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "mintegral appid, appkey or unitid is empty!");
            }
            return;
        }

        if (serverExtras.containsKey("is_video")) {
            if (serverExtras.get("is_video").toString().equals("1")) {
                isVideo = true;
            }
        }

        if (serverExtras.containsKey("payload")) {
            mPayload = serverExtras.get("payload").toString();
        }

        if (serverExtras.containsKey("tp_info")) {
            mCustomData = serverExtras.get("tp_info").toString();
        }

        if (serverExtras.containsKey("placement_id")) {
            placementId = serverExtras.get("placement_id").toString();
        }

        MintegralATInitManager.getInstance().initSDK(context, serverExtras, new MintegralATInitManager.InitCallback() {
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
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        return MintegralATInitManager.getInstance().setUserDataConsent(context, isConsent, isEUTraffic);
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
    public String getNetworkSDKVersion() {
        return MintegralATConst.getNetworkVersion();
    }

    @Override
    public String getNetworkName() {
        return MintegralATInitManager.getInstance().getNetworkName();
    }

    @Override
    public String getNetworkPlacementId() {
        return unitId;
    }
}