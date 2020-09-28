package com.anythink.network.myoffer;

import android.content.Context;

import com.anythink.core.common.base.Const;
import com.anythink.core.common.entity.MyOfferSetting;
import com.anythink.myoffer.network.base.MyOfferAdListener;
import com.anythink.myoffer.network.splash.MyOfferSplashAd;
import com.anythink.splashad.unitgroup.api.CustomSplashAdapter;

import java.util.Map;

public class MyOfferATSplashAdapter extends CustomSplashAdapter {
    String offer_id;
    MyOfferSetting myofferSetting;
    String placement_id;

    MyOfferSplashAd mMyOfferSplashAd;

    @Override
    public void loadCustomNetworkAd(Context context, Map<String, Object> serverExtras, Map<String, Object> localExtra) {
        if (serverExtras.containsKey("my_oid")) {
            offer_id = serverExtras.get("my_oid").toString();
        }
        if (serverExtras.containsKey("myoffer_setting")) {
            myofferSetting = (MyOfferSetting) serverExtras.get("myoffer_setting");
        }
        if (serverExtras.containsKey("topon_placement")) {
            placement_id = serverExtras.get("topon_placement").toString();
        }

        initSplashObject(context);

        mMyOfferSplashAd.load();
    }

    @Override
    public void destory() {
        if (mMyOfferSplashAd != null) {
            mMyOfferSplashAd.destory();
            mMyOfferSplashAd = null;
        }

        myofferSetting = null;
    }

    private void initSplashObject(Context context) {
        mMyOfferSplashAd = new MyOfferSplashAd(context, placement_id, offer_id, myofferSetting, getTrackingInfo().getmRequestId(), false);
        mMyOfferSplashAd.setListener(new MyOfferAdListener() {
            @Override
            public void onAdLoaded() {
                if (mContainer != null) {
                    if (mLoadListener != null) {
                        mLoadListener.onAdCacheLoaded();
                    }
                    mMyOfferSplashAd.show(mContainer);
                } else {
                    if (mLoadListener != null) {
                        mLoadListener.onAdLoadError("", "Splash Container has been released.");
                    }
                }
            }

            @Override
            public void onAdLoadFailed(MyOfferError error) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError(error.getCode(), error.getDesc());
                }
            }

            @Override
            public void onAdShow() {
                if (mImpressionListener != null) {
                    mImpressionListener.onSplashAdShow();
                }
            }

            @Override
            public void onAdClosed() {
                if (mImpressionListener != null) {
                    mImpressionListener.onSplashAdDismiss();
                }
            }

            @Override
            public void onAdClick() {
                if (mImpressionListener != null) {
                    mImpressionListener.onSplashAdClicked();
                }
            }
        });
    }

    @Override
    public String getNetworkName() {
        return "MyOffer";
    }

    @Override
    public String getNetworkPlacementId() {
        return offer_id;
    }


    @Override
    public String getNetworkSDKVersion() {
        return Const.SDK_VERSION_NAME;
    }


}
