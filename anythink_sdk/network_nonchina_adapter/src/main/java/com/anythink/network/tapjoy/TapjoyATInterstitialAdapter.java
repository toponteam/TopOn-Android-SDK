package com.anythink.network.tapjoy;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;
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
                try {
                    isConnonted = Tapjoy.isConnected();
                    directPlayPlacement = Tapjoy.getPlacement(unitid, new TJPlacementListener() {

                        @Override
                        public void onRequestSuccess(TJPlacement pTJPlacement) {
                            if (!pTJPlacement.isContentAvailable()) {
                                if (mLoadListener != null) {
                                    mLoadListener.onAdLoadError("", "No content available for placement " + pTJPlacement.getName());
                                }
                            } else {
                                if (mLoadListener != null) {
                                    mLoadListener.onAdDataLoaded();
                                }
                            }
                        }

                        @Override
                        public void onRequestFailure(TJPlacement pTJPlacement, TJError pTJError) {
                            if (mLoadListener != null) {
                                mLoadListener.onAdLoadError("" + pTJError.code, " " + pTJError.message);
                            }
                        }

                        @Override
                        public void onContentReady(TJPlacement pTJPlacement) {
                            if (mLoadListener != null) {
                                mLoadListener.onAdCacheLoaded();
                            }
                        }

                        @Override
                        public void onContentShow(TJPlacement pTJPlacement) {
                            if (mImpressListener != null) {
                                mImpressListener.onInterstitialAdShow();
                            }

                        }

                        @Override
                        public void onContentDismiss(TJPlacement pTJPlacement) {
                            if (mImpressListener != null) {
                                mImpressListener.onInterstitialAdClose();
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
                                mImpressListener.onInterstitialAdClicked();
                            }
                        }
                    });

                    // Set Video Listener to anonymous callback
                    directPlayPlacement.setVideoListener(new TJPlacementVideoListener() {
                        @Override
                        public void onVideoStart(TJPlacement placement) {
                            if (mImpressListener != null) {
                                mImpressListener.onInterstitialAdVideoStart();
                            }
                        }

                        @Override
                        public void onVideoError(TJPlacement placement, String message) {
                            if (mImpressListener != null) {
                                mImpressListener.onInterstitialAdVideoError("", " " + message);
                            }
                        }

                        @Override
                        public void onVideoComplete(TJPlacement placement) {
                            if (mImpressListener != null) {
                                mImpressListener.onInterstitialAdVideoEnd();
                            }
                        }

                    });

                    //load ad
                    if (directPlayPlacement != null) {
                        directPlayPlacement.requestContent();
                    }
                } catch (Throwable e) {
                    if (mLoadListener != null) {
                        mLoadListener.onAdLoadError("", e.getMessage());
                    }
                }
            }

            @Override
            public void onConnectFailure() {
                isConnonted = false;
            }
        });

    }

    @Override
    public void loadCustomNetworkAd(Context context, Map<String, Object> serverExtras, Map<String, Object> localExtras) {

        String appkey = (String) serverExtras.get("sdk_key");
        unitid = (String) serverExtras.get("placement_name");

        if (TextUtils.isEmpty(appkey) || TextUtils.isEmpty(unitid)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "tapjoy sdk_key or placement_name is empty!");
            }
            return;
        }

        if (!(context instanceof Activity)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "Tapjoy context must be acticity");
            }
            return;
        }
        //init and load
        initAndLoad((Activity) context, serverExtras);
    }

    @Override
    public void destory() {
        if (directPlayPlacement != null) {
            directPlayPlacement.setVideoListener(null);
            directPlayPlacement = null;
        }
    }

    @Override
    public String getNetworkName() {
        return TapjoyATInitManager.getInstance().getNetworkName();
    }

    @Override
    public String getNetworkPlacementId() {
        return unitid;
    }


    @Override
    public boolean isAdReady() {
        if (directPlayPlacement != null) {
            return directPlayPlacement.isContentReady();
        }
        return false;
    }

    @Override
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        return TapjoyATInitManager.getInstance().setUserDataConsent(context, isConsent, isEUTraffic);
    }

    @Override
    public String getNetworkSDKVersion() {
        return TapjoyATConst.getNetworkVersion();
    }

    @Override
    public void show(Activity activity) {
        if (activity != null) {
            Tapjoy.setActivity(activity);
        }
        if (directPlayPlacement != null) {
            directPlayPlacement.showContent();
        }
    }

}