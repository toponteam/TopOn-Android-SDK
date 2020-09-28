package com.anythink.network.gdt;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import com.anythink.banner.unitgroup.api.CustomBannerAdapter;
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
    View mBannerView;

    int mUnitVersion = 0;
    int mRefreshTime;

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
                    if (mLoadListener != null) {
                        mLoadListener.onAdLoadError(String.valueOf(adError.getErrorCode()), adError.getErrorMsg());
                    }
                }

                @Override
                public void onADReceiv() {
                    if (mLoadListener != null) {
                        mBannerView = bannerView;
                        mLoadListener.onAdCacheLoaded();
                    }
                }

                @Override
                public void onADExposure() {
                    if (mImpressionEventListener != null) {
                        mImpressionEventListener.onBannerAdShow();
                    }
                }

                @Override
                public void onADClosed() {
                    if (mImpressionEventListener != null) {
                        mImpressionEventListener.onBannerAdClose();
                    }
                }

                @Override
                public void onADClicked() {
                    if (mImpressionEventListener != null) {
                        mImpressionEventListener.onBannerAdClicked();
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
            bannerView.loadAD();
        } else { //2.0
            final UnifiedBannerView unifiedBannerView = new UnifiedBannerView(activity, mUnitId, new UnifiedBannerADListener() {
                @Override
                public void onNoAD(com.qq.e.comm.util.AdError adError) {
                    mBannerView = null;
                    if (mLoadListener != null) {
                        mLoadListener.onAdLoadError(String.valueOf(adError.getErrorCode()), adError.getErrorMsg());
                    }
                }

                @Override
                public void onADReceive() {
                    if (mLoadListener != null) {
                        mLoadListener.onAdCacheLoaded();
                    }
                }

                @Override
                public void onADExposure() {
                    if (mImpressionEventListener != null) {
                        mImpressionEventListener.onBannerAdShow();
                    }
                }

                @Override
                public void onADClosed() {
                    if (mImpressionEventListener != null) {
                        mImpressionEventListener.onBannerAdClose();
                    }
                }

                @Override
                public void onADClicked() {
                    if (mImpressionEventListener != null) {
                        mImpressionEventListener.onBannerAdClicked();
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
    public String getNetworkName() {
        return GDTATInitManager.getInstance().getNetworkName();
    }

    @Override
    public void loadCustomNetworkAd(final Context context, Map<String, Object> serverExtra, Map<String, Object> localExtra) {
        String appid = "";
        String unitId = "";

        if (serverExtra.containsKey("app_id")) {
            appid = serverExtra.get("app_id").toString();
        }
        if (serverExtra.containsKey("unit_id")) {
            unitId = serverExtra.get("unit_id").toString();
        }
        if (serverExtra.containsKey("unit_version")) {
            mUnitVersion = Integer.parseInt(serverExtra.get("unit_version").toString());
        }

        if (TextUtils.isEmpty(appid) || TextUtils.isEmpty(unitId)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "GTD appid or unitId is empty.");
            }
            return;
        }

        if (!(context instanceof Activity)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "Context must be activity.");
            }
            return;
        }

        mRefreshTime = 0;
        try {
            if (serverExtra.containsKey("nw_rft")) {
                mRefreshTime = Integer.valueOf((String) serverExtra.get("nw_rft"));
                mRefreshTime /= 1000f;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        mAppId = appid;
        mUnitId = unitId;

        GDTATInitManager.getInstance().initSDK(context, serverExtra, new GDTATInitManager.OnInitCallback() {
            @Override
            public void onSuccess() {
                startLoadAd((Activity) context);
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
        if (mBannerView != null) {
            if (mBannerView instanceof BannerView) {
                ((BannerView) mBannerView).setADListener(null);
                ((BannerView) mBannerView).destroy();
            } else if (mBannerView instanceof UnifiedBannerView) {
                ((UnifiedBannerView) mBannerView).destroy();
            }
            mBannerView = null;
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
}
