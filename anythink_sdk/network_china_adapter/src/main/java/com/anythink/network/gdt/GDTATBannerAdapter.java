package com.anythink.network.gdt;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import com.anythink.banner.api.ATBannerView;
import com.anythink.banner.unitgroup.api.CustomBannerAdapter;
import com.anythink.banner.unitgroup.api.CustomBannerListener;
import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.AdError;
import com.anythink.core.api.ErrorCode;
import com.qq.e.ads.banner.ADSize;
import com.qq.e.ads.banner.BannerADListener;
import com.qq.e.ads.banner.BannerView;
import com.qq.e.ads.banner2.UnifiedBannerADListener;
import com.qq.e.ads.banner2.UnifiedBannerView;

import java.util.Map;

public class GDTATBannerAdapter extends CustomBannerAdapter {
    private final String TAG = GDTATBannerAdapter.class.getSimpleName();

    String mAppId;
    String mUnitId;

    CustomBannerListener mCustomBannerListener;

    View mBannerView;

    int mUnitVersion = 0;
    int mRefreshTime;

    @Override
    public void loadBannerAd(ATBannerView anythinkBannerView, final Context activity, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, CustomBannerListener customBannerListener) {
        String appid = "";
        String unitId = "";

        mCustomBannerListener = customBannerListener;
        if (serverExtras.containsKey("app_id")) {
            appid = serverExtras.get("app_id").toString();
        }
        if (serverExtras.containsKey("unit_id")) {
            unitId = serverExtras.get("unit_id").toString();
        }
        if (serverExtras.containsKey("unit_version")) {
            mUnitVersion = Integer.parseInt(serverExtras.get("unit_version").toString());
        }

        if (TextUtils.isEmpty(appid) || TextUtils.isEmpty(unitId)) {
            if (mCustomBannerListener != null) {
                AdError adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", "GTD appid or unitId is empty.");
                mCustomBannerListener.onBannerAdLoadFail(this, adError);
            }
            return;
        }

        if (!(activity instanceof Activity)) {
            if (mCustomBannerListener != null) {
                AdError adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", "Context must be activity.");
                mCustomBannerListener.onBannerAdLoadFail(this, adError);
            }
            return;
        }

        mRefreshTime = 0;
        try {
            if (serverExtras.containsKey("nw_rft")) {
                mRefreshTime = Integer.valueOf((String) serverExtras.get("nw_rft"));
                mRefreshTime /= 1000f;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }


        mAppId = appid;
        mUnitId = unitId;

        GDTATInitManager.getInstance().initSDK(activity, serverExtras, new GDTATInitManager.OnInitCallback() {
            @Override
            public void onSuccess() {
                startLoadAd((Activity) activity);
            }

            @Override
            public void onError() {
                if (mCustomBannerListener != null) {
                    mCustomBannerListener.onBannerAdLoadFail(GDTATBannerAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "GDT initSDK failed."));
                }
            }
        });
    }

    private void startLoadAd(Activity activity) {
        if (mUnitVersion != 2) {
            final BannerView bannerView = new BannerView(activity, ADSize.BANNER, mAppId, mUnitId);
            if (mRefreshTime > 0) {
                bannerView.setRefresh(mRefreshTime);
            } else {
                bannerView.setRefresh(0);
            }
            bannerView.setADListener(new BannerADListener() {
                @Override
                public void onNoAD(com.qq.e.comm.util.AdError adError) {
                    if (mCustomBannerListener != null) {
                        mCustomBannerListener.onBannerAdLoadFail(GDTATBannerAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, String.valueOf(adError.getErrorCode()), adError.getErrorMsg()));
                    }
                }

                @Override
                public void onADReceiv() {
                    if (mCustomBannerListener != null) {
                        mBannerView = bannerView;
                        mCustomBannerListener.onBannerAdLoaded(GDTATBannerAdapter.this);
                    }
                }

                @Override
                public void onADExposure() {
                    if (mCustomBannerListener != null) {
                        mCustomBannerListener.onBannerAdShow(GDTATBannerAdapter.this);
                    }
                }

                @Override
                public void onADClosed() {
                    if (mCustomBannerListener != null) {
                        mCustomBannerListener.onBannerAdClose(GDTATBannerAdapter.this);
                    }
                }

                @Override
                public void onADClicked() {
                    if (mCustomBannerListener != null) {
                        mCustomBannerListener.onBannerAdClicked(GDTATBannerAdapter.this);
                    }
                }

                @Override
                public void onADLeftApplication() {
                    log(TAG, "onADLeftApplication");
                }

                @Override
                public void onADOpenOverlay() {
                    log(TAG, "onADOpenOverlay");
                }

                @Override
                public void onADCloseOverlay() {
                    log(TAG, "onADCloseOverlay");
                }
            });
            bannerView.loadAD();
        } else { //2.0
            final UnifiedBannerView unifiedBannerView = new UnifiedBannerView(activity, mUnitId, new UnifiedBannerADListener() {
                @Override
                public void onNoAD(com.qq.e.comm.util.AdError adError) {
                    mBannerView = null;
                    if (mCustomBannerListener != null) {
                        mCustomBannerListener.onBannerAdLoadFail(GDTATBannerAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, String.valueOf(adError.getErrorCode()), adError.getErrorMsg()));
                    }
                }

                @Override
                public void onADReceive() {
                    if (mCustomBannerListener != null) {
                        mCustomBannerListener.onBannerAdLoaded(GDTATBannerAdapter.this);
                    }
                }

                @Override
                public void onADExposure() {
                    if (mCustomBannerListener != null) {
                        mCustomBannerListener.onBannerAdShow(GDTATBannerAdapter.this);
                    }
                }

                @Override
                public void onADClosed() {
                    if (mCustomBannerListener != null) {
                        mCustomBannerListener.onBannerAdClose(GDTATBannerAdapter.this);
                    }
                }

                @Override
                public void onADClicked() {
                    if (mCustomBannerListener != null) {
                        mCustomBannerListener.onBannerAdClicked(GDTATBannerAdapter.this);
                    }
                }

                @Override
                public void onADLeftApplication() {

                }

                @Override
                public void onADOpenOverlay() {

                }

                @Override
                public void onADCloseOverlay() {

                }
            });
            if (mRefreshTime > 0) {
                unifiedBannerView.setRefresh(mRefreshTime);
            } else {
                unifiedBannerView.setRefresh(0);
            }
            mBannerView = unifiedBannerView;
            unifiedBannerView.loadAD();
        }
    }

    @Override
    public View getBannerView() {
        return mBannerView;
    }

    @Override
    public void clean() {
        mBannerView = null;
    }

    @Override
    public String getSDKVersion() {
        return GDTATConst.getNetworkVersion();
    }

    @Override
    public String getNetworkName() {
        return GDTATInitManager.getInstance().getNetworkName();
    }
}
