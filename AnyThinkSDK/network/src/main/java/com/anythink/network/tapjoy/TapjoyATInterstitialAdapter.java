package com.anythink.network.tapjoy;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.ErrorCode;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialListener;
import com.tapjoy.TJActionRequest;
import com.tapjoy.TJConnectListener;
import com.tapjoy.TJError;
import com.tapjoy.TJPlacement;
import com.tapjoy.TJPlacementListener;
import com.tapjoy.TJPlacementVideoListener;
import com.tapjoy.Tapjoy;

import java.util.Map;

/**
 * Created by Z on 2018/6/27.
 */


public class TapjoyATInterstitialAdapter extends CustomInterstitialAdapter {
    private static final String TAG = TapjoyATInterstitialAdapter.class.getSimpleName();

    String unitid = "";
    private TJPlacement directPlayPlacement;
    boolean isRewared = false, isConnonted = false;

    /***
     * init and load
     */
    private void initAndLoad(Activity activity, Map<String, Object> serverExtras) {
        isRewared = false;

        Tapjoy.setActivity(activity);
        //init
        TapjoyATInitManager.getInstance().initSDK(activity.getApplicationContext(), serverExtras, new TJConnectListener() {
            @Override
            public void onConnectSuccess() {
                isConnonted = Tapjoy.isConnected();
                directPlayPlacement = Tapjoy.getPlacement(unitid, new TJPlacementListener() {

                    @Override
                    public void onRequestSuccess(TJPlacement pTJPlacement) {
                        if (!pTJPlacement.isContentAvailable()) {
                            if (mLoadResultListener != null) {
                                mLoadResultListener.onInterstitialAdLoadFail(TapjoyATInterstitialAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "No content available for placement " + pTJPlacement.getName()));
                            }
                        } else {
                            if (mLoadResultListener != null) {
                                mLoadResultListener.onInterstitialAdDataLoaded(TapjoyATInterstitialAdapter.this);
                            }
                        }
                    }

                    @Override
                    public void onRequestFailure(TJPlacement pTJPlacement, TJError pTJError) {
                        if (mLoadResultListener != null) {
                            mLoadResultListener.onInterstitialAdLoadFail(TapjoyATInterstitialAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, "" + pTJError.code, " " + pTJError.message));
                        }
                    }

                    @Override
                    public void onContentReady(TJPlacement pTJPlacement) {
                        if (mLoadResultListener != null) {
                            mLoadResultListener.onInterstitialAdLoaded(TapjoyATInterstitialAdapter.this);
                        }
                    }

                    @Override
                    public void onContentShow(TJPlacement pTJPlacement) {
                        if (mImpressListener != null) {
                            mImpressListener.onInterstitialAdShow(TapjoyATInterstitialAdapter.this);
                        }

                    }

                    @Override
                    public void onContentDismiss(TJPlacement pTJPlacement) {
                        if (mImpressListener != null) {
                            mImpressListener.onInterstitialAdClose(TapjoyATInterstitialAdapter.this);
                        }
                    }

                    @Override
                    public void onPurchaseRequest(TJPlacement pTJPlacement, TJActionRequest pTJActionRequest, String pS) {
                    }

                    @Override
                    public void onRewardRequest(TJPlacement pTJPlacement, TJActionRequest pTJActionRequest, String pS, int pI) {
                    }

                    @Override
                    public void onClick(TJPlacement tjPlacement) {
                        if (mImpressListener != null) {
                            mImpressListener.onInterstitialAdClicked(TapjoyATInterstitialAdapter.this);
                        }
                    }
                });

                // Set Video Listener to anonymous callback
                directPlayPlacement.setVideoListener(new TJPlacementVideoListener() {
                    @Override
                    public void onVideoStart(TJPlacement placement) {
                        if (mImpressListener != null) {
                            mImpressListener.onInterstitialAdVideoStart(TapjoyATInterstitialAdapter.this);
                        }
                    }

                    @Override
                    public void onVideoError(TJPlacement placement, String message) {
                        if (mImpressListener != null) {
                            mImpressListener.onInterstitialAdVideoError(TapjoyATInterstitialAdapter.this, ErrorCode.getErrorCode(ErrorCode.rewardedVideoPlayError, "", " " + message));
                        }
                    }

                    @Override
                    public void onVideoComplete(TJPlacement placement) {
                        if (mImpressListener != null) {
                            mImpressListener.onInterstitialAdVideoEnd(TapjoyATInterstitialAdapter.this);
                        }
                    }

                });

                //load ad
                if (directPlayPlacement != null) {
                    directPlayPlacement.requestContent();
                }
            }

            @Override
            public void onConnectFailure() {
                isConnonted = false;
            }
        });

    }

    @Override
    public void loadInterstitialAd(Context context, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, CustomInterstitialListener customInterstitialListener) {
        mLoadResultListener = customInterstitialListener;
        if (context == null) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onInterstitialAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "activity is null."));
            }
            return;
        }

        if (serverExtras == null) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onInterstitialAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "This placement's params in server is null!"));
            }
            return;
        } else {

            String appkey = (String) serverExtras.get("sdk_key");
            unitid = (String) serverExtras.get("placement_name");

            if (TextUtils.isEmpty(appkey) || TextUtils.isEmpty(unitid)) {
                if (mLoadResultListener != null) {
                    mLoadResultListener.onInterstitialAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "tapjoy sdk_key or placement_name is empty!"));
                }
                return;
            }
        }

        if (!(context instanceof Activity)) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onInterstitialAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "context must be acticity"));
            }
            return;
        }
        //init and load
        initAndLoad((Activity) context, serverExtras);
    }

    @Override
    public void clean() {
    }

    @Override
    public String getNetworkName() {
        return TapjoyATInitManager.getInstance().getNetworkName();
    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public boolean isAdReady() {
        if (directPlayPlacement != null) {
            return directPlayPlacement.isContentReady();
        }
        return false;
    }

    @Override
    public String getSDKVersion() {
        return TapjoyATConst.getNetworkVersion();
    }

    @Override
    public void show(Context context) {
        if (directPlayPlacement != null) {
            directPlayPlacement.showContent();
        }
    }

}