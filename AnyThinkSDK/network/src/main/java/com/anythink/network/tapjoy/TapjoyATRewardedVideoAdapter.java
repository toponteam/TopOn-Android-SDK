package com.anythink.network.tapjoy;

import android.app.Activity;
import android.text.TextUtils;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.ATSDK;
import com.anythink.core.api.ErrorCode;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoListener;
import com.tapjoy.TJActionRequest;
import com.tapjoy.TJConnectListener;
import com.tapjoy.TJError;
import com.tapjoy.TJPlacement;
import com.tapjoy.TJPlacementListener;
import com.tapjoy.TJPlacementVideoListener;
import com.tapjoy.Tapjoy;
import com.tapjoy.TapjoyConnectFlag;
import com.tapjoy.TapjoyLog;

import java.util.Hashtable;
import java.util.Map;

/**
 * Created by zhou on 2018/6/27.
 */


public class TapjoyATRewardedVideoAdapter extends CustomRewardVideoAdapter {
    private static final String TAG = TapjoyATRewardedVideoAdapter.class.getSimpleName();

    TapjoyRewardedVideoSetting mTapjoyMediationSetting;
    String unitid = "";
    private TJPlacement directPlayPlacement;
    boolean isConnonted = false;

    /***
     * init and load
     */
    private void initAndLoad(Activity activity, Map<String, Object> serverExtras) {
        Hashtable<String, Object> connectFlags = new Hashtable<String, Object>();

        connectFlags.put(TapjoyConnectFlag.ENABLE_LOGGING, "true");

        TapjoyLog.setDebugEnabled(ATSDK.NETWORK_LOG_DEBUG);

        if (mTapjoyMediationSetting != null) {
            Tapjoy.setGcmSender(mTapjoyMediationSetting.getGcmSender());
        }
        connectFlags.put(TapjoyConnectFlag.USER_ID, mUserId);

        Tapjoy.setActivity(activity);
        //init
        TapjoyATInitManager.getInstance().initSDK(activity.getApplicationContext(), serverExtras, new TJConnectListener() {
            @Override
            public void onConnectSuccess() {
                isConnonted = Tapjoy.isConnected();
                Tapjoy.setUserID(mUserId);
                directPlayPlacement = Tapjoy.getPlacement(unitid, new TJPlacementListener() {

                    @Override
                    public void onRequestSuccess(TJPlacement pTJPlacement) {
                        if (!pTJPlacement.isContentAvailable()) {
                            if (mLoadResultListener != null) {
                                mLoadResultListener.onRewardedVideoAdFailed(TapjoyATRewardedVideoAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "No content available for placement " + pTJPlacement.getName()));
                            }
                        } else {
                            if (mLoadResultListener != null) {
                                mLoadResultListener.onRewardedVideoAdDataLoaded(TapjoyATRewardedVideoAdapter.this);
                            }
                        }
                    }

                    @Override
                    public void onRequestFailure(TJPlacement pTJPlacement, TJError pTJError) {
                        if (mLoadResultListener != null) {
                            mLoadResultListener.onRewardedVideoAdFailed(TapjoyATRewardedVideoAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, "" + pTJError.code, " " + pTJError.message));
                        }
                    }

                    @Override
                    public void onContentReady(TJPlacement pTJPlacement) {
                        if (mLoadResultListener != null) {
                            mLoadResultListener.onRewardedVideoAdLoaded(TapjoyATRewardedVideoAdapter.this);
                        }
                    }

                    @Override
                    public void onContentShow(TJPlacement pTJPlacement) {

                    }

                    @Override
                    public void onContentDismiss(TJPlacement pTJPlacement) {
                        if (mImpressionListener != null) {
                            mImpressionListener.onRewardedVideoAdClosed(TapjoyATRewardedVideoAdapter.this);
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
                        if (mImpressionListener != null) {
                            mImpressionListener.onRewardedVideoAdPlayClicked(TapjoyATRewardedVideoAdapter.this);
                        }
                    }
                });

                // Set Video Listener to anonymous callback
                directPlayPlacement.setVideoListener(new TJPlacementVideoListener() {
                    @Override
                    public void onVideoStart(TJPlacement placement) {
                        if (mImpressionListener != null) {
                            mImpressionListener.onRewardedVideoAdPlayStart(TapjoyATRewardedVideoAdapter.this);
                        }
                    }

                    @Override
                    public void onVideoError(TJPlacement placement, String message) {
                        if (mImpressionListener != null) {
                            mImpressionListener.onRewardedVideoAdPlayFailed(TapjoyATRewardedVideoAdapter.this, ErrorCode.getErrorCode(ErrorCode.rewardedVideoPlayError, "", " " + message));
                        }
                    }

                    @Override
                    public void onVideoComplete(TJPlacement placement) {
                        if (mImpressionListener != null) {
                            mImpressionListener.onRewardedVideoAdPlayEnd(TapjoyATRewardedVideoAdapter.this);
                        }

                        if (mImpressionListener != null) {
                            mImpressionListener.onReward(TapjoyATRewardedVideoAdapter.this);
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
    public void loadRewardVideoAd(Activity activity, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, CustomRewardVideoListener customRewardVideoListener) {
        mLoadResultListener = customRewardVideoListener;
        if (activity == null) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onRewardedVideoAdFailed(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "activity is null."));
            }
            return;
        }
        if (mediationSetting != null && mediationSetting instanceof TapjoyRewardedVideoSetting) {
            mTapjoyMediationSetting = (TapjoyRewardedVideoSetting) mediationSetting;

        }

        if (serverExtras == null) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onRewardedVideoAdFailed(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "This placement's params in server is null!"));
            }
            return;
        } else {

            String appkey = (String) serverExtras.get("sdk_key");
            unitid = (String) serverExtras.get("placement_name");

            if (TextUtils.isEmpty(appkey) || TextUtils.isEmpty(unitid)) {
                if (mLoadResultListener != null) {
                    mLoadResultListener.onRewardedVideoAdFailed(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "tapjoy sdk_key or placement_name is empty!"));
                }
                return;
            }
        }

        //init and load
        initAndLoad(activity, serverExtras);
    }

    @Override
    public void clean() {
    }

    @Override
    public void onResume(Activity activity) {

    }

    @Override
    public void onPause(Activity activity) {

    }

    @Override
    public boolean isAdReady() {
        if (directPlayPlacement != null) {

            return directPlayPlacement.isContentReady();
        }
        return false;
    }

    @Override
    public void show(Activity activity) {
        if (directPlayPlacement != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    directPlayPlacement.showContent();
                }
            });

        } else if (directPlayPlacement != null) {
            directPlayPlacement.showContent();
        }

    }

    @Override
    public String getSDKVersion() {
        return TapjoyATConst.getNetworkVersion();
    }

    @Override
    public String getNetworkName() {
        return TapjoyATInitManager.getInstance().getNetworkName();
    }
}