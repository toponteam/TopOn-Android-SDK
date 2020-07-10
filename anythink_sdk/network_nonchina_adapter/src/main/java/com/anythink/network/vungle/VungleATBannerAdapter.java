package com.anythink.network.vungle;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;

import com.anythink.banner.api.ATBannerView;
import com.anythink.banner.unitgroup.api.CustomBannerAdapter;
import com.anythink.banner.unitgroup.api.CustomBannerListener;
import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.ErrorCode;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialListener;
import com.vungle.warren.AdConfig;
import com.vungle.warren.Banners;
import com.vungle.warren.LoadAdCallback;
import com.vungle.warren.PlayAdCallback;
import com.vungle.warren.Vungle;
import com.vungle.warren.VungleBanner;
import com.vungle.warren.VungleNativeAd;
import com.vungle.warren.error.VungleException;

import java.util.Map;

public class VungleATBannerAdapter extends CustomBannerAdapter {

    private final String TAG = VungleATBannerAdapter.class.getSimpleName();
    String mPlacementId;
    AdConfig mAdConfig;
    String unitType = "";
    String sizeType = "";

    CustomBannerListener mBannerListener;

    View mVungleBannerView;

    PlayAdCallback playAdCallback = new PlayAdCallback() {
        @Override
        public void onAdStart(String placementReferenceId) {
        }

        @Override
        public void onAdEnd(String placementReferenceId, boolean completed, boolean isCTAClicked) {
            // Calling finishDisplayingAd when you want to finish displaying In-Feed Ad
            // will trigger onAdEnd and will tell you when you can remove the child
            // In-Feed view container vungleNativeAd.finishDisplayingAd();

            // And removing empty ad view from container
            if (isCTAClicked) {
                mBannerListener.onBannerAdClicked(VungleATBannerAdapter.this);
            }

//            if (completed) {
//                mBannerListener.onBannerAdClose(VungleATBannerAdapter.this);
//            }
        }

        @Override
        public void onError(String placementReferenceId, VungleException e) {
            // Play ad error occurred - e.getLocalizedMessage() contains error message
        }
    };

    @Override
    public void loadBannerAd(ATBannerView bannerView, final Context activity, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, CustomBannerListener customBannerListener) {

//        serverExtras.put("app_id","5ad59a853d927044ac75263a");

//        serverExtras.put("placement_id", "ANDROID_MREC_300X250-7831245");
//        serverExtras.put("unit_type", "1");
//        serverExtras.put("size_type", "0");

//        serverExtras.put("placement_id", "ANDROID_BANNER_320X50-5191270");
//        serverExtras.put("unit_type", "0");
//        serverExtras.put("size_type", "2");


        String mAppId = (String) serverExtras.get("app_id");
        mPlacementId = (String) serverExtras.get("placement_id");

        if (serverExtras.containsKey("unit_type")) {
            unitType = (String) serverExtras.get("unit_type");
        }

        if (serverExtras.containsKey("size_type")) {
            sizeType = (String) serverExtras.get("size_type");
        }


        mBannerListener = customBannerListener;

        if (TextUtils.isEmpty(mAppId) || TextUtils.isEmpty(mPlacementId)) {
            if (mBannerListener != null) {
                mBannerListener.onBannerAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "vungle appid & placementId is empty."));
            }
            return;
        }

        mAdConfig = new AdConfig();


        VungleATInitManager.getInstance().initSDK(activity.getApplicationContext(), serverExtras, new VungleATInitManager.InitListener() {
            @Override
            public void onSuccess() {
                startLoadAd(activity, playAdCallback);
            }

            @Override
            public void onError(Throwable throwable) {
                if (mBannerListener != null) {
                    mBannerListener.onBannerAdLoadFail(VungleATBannerAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, "", throwable.getMessage()));
                }
            }
        });
    }

    private void startLoadAd(Context context, PlayAdCallback playAdCallback) {
        if (!TextUtils.isEmpty(unitType)) {
            switch (unitType) {
                case "1": //MREC
                    loadMrecAd(context, playAdCallback);
                    break;
                default:
                    loadBannerAd(playAdCallback);
                    break;
            }
        } else {
            loadBannerAd(playAdCallback);
        }
    }

    private void loadMrecAd(final Context context, final PlayAdCallback playAdCallback) {
        mAdConfig.setAdSize(AdConfig.AdSize.VUNGLE_MREC);
        Vungle.loadAd(mPlacementId, mAdConfig, new LoadAdCallback() {
            @Override
            public void onAdLoad(String s) {
                VungleNativeAd vungleNativeAd = Vungle.getNativeAd(mPlacementId, mAdConfig, playAdCallback);
                RelativeLayout relativeLayout = new RelativeLayout(context);
                relativeLayout.addView(vungleNativeAd.renderNativeView());
                mVungleBannerView = relativeLayout;

                if (mBannerListener != null) {
                    mBannerListener.onBannerAdLoaded(VungleATBannerAdapter.this);
                }
            }

            @Override
            public void onError(String s, VungleException e) {
                if (mBannerListener != null) {
                    mBannerListener.onBannerAdLoadFail(VungleATBannerAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, "", e.toString()));
                }
            }
        });
    }

    private void loadBannerAd(final PlayAdCallback playAdCallback) {
        if (!TextUtils.isEmpty(sizeType)) {
            switch (sizeType) {
                case "2":
                    mAdConfig.setAdSize(AdConfig.AdSize.BANNER);
                    break;
                case "3":
                    mAdConfig.setAdSize(AdConfig.AdSize.BANNER_SHORT);
                    break;
                case "4":
                    mAdConfig.setAdSize(AdConfig.AdSize.BANNER_LEADERBOARD);
                    break;
                default:
                    mAdConfig.setAdSize(AdConfig.AdSize.BANNER);
                    break;
            }
        } else {
            mAdConfig.setAdSize(AdConfig.AdSize.BANNER);
        }

        Banners.loadBanner(mPlacementId, mAdConfig.getAdSize(), new LoadAdCallback() {
            @Override
            public void onAdLoad(String s) {
                if (Banners.canPlayAd(mPlacementId, mAdConfig.getAdSize())) {
                    VungleBanner vungleBanner = Banners.getBanner(mPlacementId, mAdConfig.getAdSize(), playAdCallback);
                    mVungleBannerView = vungleBanner;
                    if (mBannerListener != null) {
                        mBannerListener.onBannerAdLoaded(VungleATBannerAdapter.this);
                    }
                } else {
                    if (mBannerListener != null) {
                        mBannerListener.onBannerAdLoadFail(VungleATBannerAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "Load success but couldn't play banner"));
                    }
                }

            }

            @Override
            public void onError(String s, VungleException e) {
                if (mBannerListener != null) {
                    mBannerListener.onBannerAdLoadFail(VungleATBannerAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, "", e.toString()));
                }
            }
        });
    }


    @Override
    public String getSDKVersion() {
        return "";
    }


    @Override
    public void clean() {
        if (mVungleBannerView instanceof VungleBanner) {
            ((VungleBanner) mVungleBannerView).destroyAd();
        }
    }

    @Override
    public String getNetworkName() {
        return VungleATInitManager.getInstance().getNetworkName();
    }


    @Override
    public View getBannerView() {
        return mVungleBannerView;
    }
}
