package com.anythink.network.mintegral;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.AdError;
import com.anythink.core.api.ErrorCode;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialListener;
import com.mintegral.msdk.MIntegralConstans;
import com.mintegral.msdk.interstitialvideo.out.InterstitialVideoListener;
import com.mintegral.msdk.interstitialvideo.out.MTGBidInterstitialVideoHandler;
import com.mintegral.msdk.interstitialvideo.out.MTGInterstitialVideoHandler;
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
    String place_id = "";
    boolean isVideo;
    boolean mIsReady;
    String mPayload;

    /***
     * init
     */
    private void init(Context context) {

        if (isVideo) {
            InterstitialVideoListener videoListener = new InterstitialVideoListener() {

                @Override
                public void onLoadSuccess(String unitId) {
                    if (mLoadResultListener != null) {
                        mLoadResultListener.onInterstitialAdDataLoaded(MintegralATInterstitialAdapter.this);
                    }
                }

                @Override
                public void onVideoLoadSuccess(String unitId) {
                    if (mLoadResultListener != null) {
                        mLoadResultListener.onInterstitialAdLoaded(MintegralATInterstitialAdapter.this);
                    }

                }

                @Override
                public void onVideoLoadFail(String errorMsg) {
                    if (mLoadResultListener != null) {
                        mLoadResultListener.onInterstitialAdLoadFail(MintegralATInterstitialAdapter.this
                                , ErrorCode.getErrorCode(ErrorCode.noADError, "", errorMsg));
                    }
                }

                @Override
                public void onShowFail(String errorMsg) {
                    if (mImpressListener != null) {
                        mImpressListener.onInterstitialAdVideoError(MintegralATInterstitialAdapter.this
                                , ErrorCode.getErrorCode(ErrorCode.noADError, "", errorMsg));
                    }
                }

                @Override
                public void onAdShow() {
                    if (mImpressListener != null) {
                        mImpressListener.onInterstitialAdShow(MintegralATInterstitialAdapter.this);
                        mImpressListener.onInterstitialAdVideoStart(MintegralATInterstitialAdapter.this);
                    }
                }

                @Override
                public void onAdClose(boolean isCompleteView) {
                    if (mImpressListener != null) {
                        if (isCompleteView) {
                            mImpressListener.onInterstitialAdVideoEnd(MintegralATInterstitialAdapter.this);
                        }
                        mImpressListener.onInterstitialAdClose(MintegralATInterstitialAdapter.this);
                    }
                }

                @Override
                public void onVideoAdClicked(String unitId) {
                    if (mImpressListener != null) {
                        mImpressListener.onInterstitialAdClicked(MintegralATInterstitialAdapter.this);
                    }
                }

                @Override
                public void onVideoComplete(String s) {
                    if (mImpressListener != null) {
                        mImpressListener.onInterstitialAdVideoEnd(MintegralATInterstitialAdapter.this);
                    }
                }

                @Override
                public void onEndcardShow(String s) {

                }

            };

            if (TextUtils.isEmpty(mPayload)) {
                mMvInterstitialVideoHandler = new MTGInterstitialVideoHandler((Activity) context, place_id);
                // Please use this method"mMtgInterstitalVideoHandler.setRewardVideoListener()" ,if the SDK version is below 9.0.2
                mMvInterstitialVideoHandler.setInterstitialVideoListener(videoListener);
            } else {
                mMvBidIntersititialVideoHandler = new MTGBidInterstitialVideoHandler(context, place_id);
                mMvBidIntersititialVideoHandler.setInterstitialVideoListener(videoListener);
            }


        } else {
            HashMap<String, Object> hashMap = new HashMap<String, Object>();
            hashMap.put(MIntegralConstans.PROPERTIES_UNIT_ID, place_id);
            mMvInterstitialHandler = new MTGInterstitialHandler(context, hashMap);
            mMvInterstitialHandler.setInterstitialListener(new InterstitialListener() {

                @Override
                public void onInterstitialLoadSuccess() {
                    mIsReady = true;
                    if (mLoadResultListener != null) {
                        mLoadResultListener.onInterstitialAdLoaded(MintegralATInterstitialAdapter.this);
                    }
                }

                @Override
                public void onInterstitialLoadFail(String s) {
                    if (mLoadResultListener != null) {
                        mLoadResultListener.onInterstitialAdLoadFail(MintegralATInterstitialAdapter.this
                                , ErrorCode.getErrorCode(ErrorCode.noADError, "", s));
                    }
                }

                @Override
                public void onInterstitialShowSuccess() {
                    mIsReady = false;
                    if (mImpressListener != null) {
                        mImpressListener.onInterstitialAdShow(MintegralATInterstitialAdapter.this);
                    }
                }

                @Override
                public void onInterstitialShowFail(String s) {
                    log(TAG, "onInterstitialShowFail");
                }

                @Override
                public void onInterstitialClosed() {
                    if (mImpressListener != null) {
                        mImpressListener.onInterstitialAdClose(MintegralATInterstitialAdapter.this);
                    }
                }

                @Override
                public void onInterstitialAdClick() {
                    if (mImpressListener != null) {
                        mImpressListener.onInterstitialAdClicked(MintegralATInterstitialAdapter.this);
                    }
                }
            });
        }
    }

    @Override
    public void loadInterstitialAd(final Context context, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, CustomInterstitialListener customInterstitialListener) {
        mIsReady = false;
        isVideo = false;
        mLoadResultListener = customInterstitialListener;
        if (context == null) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onInterstitialAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "context is null."));
            }
            return;
        }

        if (serverExtras == null) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onInterstitialAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "This placement's params in server is null!"));
            }
            return;
        } else {
            // appid,appkey,unitid
            String appid = (String) serverExtras.get("appid");
            String appkey = (String) serverExtras.get("appkey");
            place_id = (String) serverExtras.get("unitid");

            if (TextUtils.isEmpty(appid) || TextUtils.isEmpty(appkey) || TextUtils.isEmpty(place_id)) {
                if (mLoadResultListener != null) {
                    mLoadResultListener.onInterstitialAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "mintegral appid, appkey or unitid is empty!"));
                }
                return;
            }
        }

        if (serverExtras.containsKey("is_video")) {
            if (serverExtras.get("is_video").toString().equals("1")) {
                isVideo = true;
            }
        }

        if (serverExtras.containsKey("payload")) {
            mPayload = serverExtras.get("payload").toString();
        }

        if (isVideo) {
            if (!(context instanceof Activity)) {
                if (mLoadResultListener != null) {
                    mLoadResultListener.onInterstitialAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "context must be activity."));
                }
                return;
            }
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
                if (mLoadResultListener != null) {
                    AdError adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", e.getMessage());
                    mLoadResultListener.onInterstitialAdLoadFail(MintegralATInterstitialAdapter.this, adError);
                }
            }
        });
    }


    /***
     * load ad
     */
    public void startLoad() {
        if (mMvInterstitialHandler != null) {
            mMvInterstitialHandler.preload();
        }
        if (mMvInterstitialVideoHandler != null) {
            mMvInterstitialVideoHandler.load();
        }
        if (mMvBidIntersititialVideoHandler != null) {
            mMvBidIntersititialVideoHandler.loadFromBid(mPayload);
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
    public void show(Context context) {
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
    public String getSDKVersion() {
        return MintegralATConst.getNetworkVersion();
    }

    @Override
    public String getNetworkName() {
        return MintegralATInitManager.getInstance().getNetworkName();
    }
}