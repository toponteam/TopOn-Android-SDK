package com.anythink.network.mintegral;

import android.app.Activity;
import android.content.res.Configuration;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.AdError;
import com.anythink.core.api.ErrorCode;
import com.anythink.splashad.unitgroup.api.CustomSplashAdapter;
import com.anythink.splashad.unitgroup.api.CustomSplashListener;
import com.mintegral.msdk.out.MTGSplashHandler;
import com.mintegral.msdk.out.MTGSplashLoadListener;
import com.mintegral.msdk.out.MTGSplashShowListener;

import java.util.Map;


/**
 * @author Z
 */

public class MintegralATSplashAdapter extends CustomSplashAdapter {

    private static final String TAG = MintegralATSplashAdapter.class.getSimpleName();
    String mPayload;
    String mCustomData = "{}";
    int countdown = 5;
    int orientation = Configuration.ORIENTATION_PORTRAIT;
    boolean allowSkip = true;

    String appid = "";
    String unitId = "";
    String sdkKey = "";
    String placementId = "";

    @Override
    public String getSDKVersion() {
        return MintegralATConst.getNetworkVersion();
    }

    @Override
    public void loadSplashAd(final Activity activity, final ViewGroup constainer, View skipView, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, final CustomSplashListener customSplashListener) {
        {
            try {

                //支持视频
                boolean suportVideo = false;
                try {
                    if (serverExtras.containsKey("appid")) {
                        appid = serverExtras.get("appid").toString();
                    }
                    if (serverExtras.containsKey("unitid")) {
                        unitId = serverExtras.get("unitid").toString();
                    }

                    if (serverExtras.containsKey("placement_id")) {
                        placementId = serverExtras.get("placement_id").toString();
                    }
                    if (serverExtras.containsKey("appkey")) {
                        sdkKey = serverExtras.get("appkey").toString();
                    }

                    if (serverExtras.containsKey("payload")) {
                        mPayload = serverExtras.get("payload").toString();
                    }

                    if (serverExtras.containsKey("tp_info")) {
                        mCustomData = serverExtras.get("tp_info").toString();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (TextUtils.isEmpty(appid) || TextUtils.isEmpty(unitId) || TextUtils.isEmpty(sdkKey)) {
                    if (customSplashListener != null) {
                        AdError adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", "mintegral appid ,unitid or sdkkey is empty.");
                        customSplashListener.onSplashAdFailed(this, adError);
                    }
                    return;
                }

                if (serverExtras.containsKey("countdown")) {
                    countdown = Integer.parseInt(serverExtras.get("countdown").toString());
                }

                if (serverExtras.containsKey("allows_skip")) {
                    allowSkip = Integer.parseInt(serverExtras.get("allows_skip").toString()) == 1;
                }

                if (serverExtras.containsKey("orientation")) {
                    orientation = TextUtils.equals(serverExtras.get("orientation").toString(), "2") ? Configuration.ORIENTATION_LANDSCAPE : Configuration.ORIENTATION_PORTRAIT;
                }


                MintegralATInitManager.getInstance().initSDK(activity.getApplicationContext(), serverExtras, new MintegralATInitManager.InitCallback() {
                    @Override
                    public void onSuccess() {
                        startLoad(constainer, customSplashListener);
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (customSplashListener != null) {
                            AdError adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", e.getMessage());
                            customSplashListener.onSplashAdFailed(MintegralATSplashAdapter.this, adError);
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                if (customSplashListener != null) {
                    AdError adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", e.getMessage());
                    customSplashListener.onSplashAdFailed(MintegralATSplashAdapter.this, adError);
                }
            }
        }
    }

    MTGSplashHandler splashHandler = null;

    private void startLoad(ViewGroup container, final CustomSplashListener splashListener) {
        splashHandler = new MTGSplashHandler(placementId, unitId, allowSkip, countdown, orientation, 0, 0);
        splashHandler.setSplashLoadListener(new MTGSplashLoadListener() {
            @Override
            public void onLoadSuccessed(int i) {
                if (splashListener != null) {
                    splashListener.onSplashAdLoaded(MintegralATSplashAdapter.this);
                }
            }

            @Override
            public void onLoadFailed(String s, int i) {
                if (splashListener != null) {
                    splashListener.onSplashAdFailed(MintegralATSplashAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, i + "", s));
                }
            }
        });

        splashHandler.setSplashShowListener(new MTGSplashShowListener() {
            @Override
            public void onShowSuccessed() {
                if (splashListener != null) {
                    splashListener.onSplashAdShow(MintegralATSplashAdapter.this);
                }
            }

            @Override
            public void onShowFailed(String s) {

            }

            @Override
            public void onAdClicked() {
                if (splashListener != null) {
                    splashListener.onSplashAdClicked(MintegralATSplashAdapter.this);
                }
            }

            @Override
            public void onDismiss(int i) {
                if (splashListener != null) {
                    splashListener.onSplashAdDismiss(MintegralATSplashAdapter.this);
                }
            }

            @Override
            public void onAdTick(long l) {

            }
        });

        splashHandler.loadAndShow(container);
        splashHandler.onResume();
    }

    @Override
    public void clean() {
        if (splashHandler != null) {
            splashHandler.onPause();
            splashHandler.onDestroy();
        }

    }

    @Override
    public String getNetworkName() {
        return MintegralATInitManager.getInstance().getNetworkName();
    }
}
