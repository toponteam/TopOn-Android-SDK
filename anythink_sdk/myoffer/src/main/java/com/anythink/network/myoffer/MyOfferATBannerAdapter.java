package com.anythink.network.myoffer;

import android.content.Context;
import android.view.View;

import com.anythink.banner.unitgroup.api.CustomBannerAdapter;
import com.anythink.core.common.MyOfferAPIProxy;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.entity.MyOfferSetting;
import com.anythink.myoffer.network.banner.MyOfferBannerAd;
import com.anythink.myoffer.network.base.MyOfferAdListener;

import java.util.Map;

public class MyOfferATBannerAdapter extends CustomBannerAdapter {

    String offer_id;
    private MyOfferSetting myOfferSetting;
    private String placement_id = "";
    private MyOfferBannerAd mMyOfferBannerAd;
    private View mBannerView;

    private boolean isDefaultOffer = false; //用于判断兜底offer的

    @Override
    public View getBannerView() {
        if (mBannerView == null) {
            if (mMyOfferBannerAd != null && mMyOfferBannerAd.isReady()) {
                mBannerView = mMyOfferBannerAd.getBannerView(getTrackingInfo().getmRequestId());
            }
        }
        return mBannerView;
    }

    @Override
    public void loadCustomNetworkAd(Context context, Map<String, Object> serverExtra, Map<String, Object> localExtra) {
        if (serverExtra.containsKey("my_oid")) {
            offer_id = serverExtra.get("my_oid").toString();
        }
        if (serverExtra.containsKey("myoffer_setting")) {
            myOfferSetting = (MyOfferSetting) serverExtra.get("myoffer_setting");
        }
        if (serverExtra.containsKey("topon_placement")) {
            placement_id = serverExtra.get("topon_placement").toString();
        }

        initBannerAdObject(context);

        mMyOfferBannerAd.load();
    }

    private void initBannerAdObject(Context context) {
        mMyOfferBannerAd = new MyOfferBannerAd(context, placement_id, offer_id, myOfferSetting, isDefaultOffer);
        mMyOfferBannerAd.setListener(new MyOfferAdListener() {
            @Override
            public void onAdLoaded() {
                mBannerView = mMyOfferBannerAd.getBannerView(getTrackingInfo().getmRequestId());

                if (mLoadListener != null) {
                    if (mBannerView != null) {
                        mLoadListener.onAdCacheLoaded();
                    } else {
                        mLoadListener.onAdLoadError("", "MyOffer bannerView = null");
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
                if (mImpressionEventListener != null) {
                    mImpressionEventListener.onBannerAdShow();
                }
            }

            @Override
            public void onAdClosed() {
                if (mImpressionEventListener != null) {
                    mImpressionEventListener.onBannerAdClose();
                }
            }

            @Override
            public void onAdClick() {
                if (mImpressionEventListener != null) {
                    mImpressionEventListener.onBannerAdClicked();
                }
            }
        });
    }

    @Override
    public boolean initNetworkObjectByPlacementId(Context context, Map<String, Object> serverExtras, Map<String, Object> localExtra) {
        if (serverExtras.containsKey("my_oid")) {
            offer_id = serverExtras.get("my_oid").toString();
        }

        if (serverExtras.containsKey("myoffer_setting")) {
            myOfferSetting = (MyOfferSetting) serverExtras.get("myoffer_setting");
        }

        if (serverExtras.containsKey("topon_placement")) {
            placement_id = serverExtras.get("topon_placement").toString();
        }

        if (serverExtras.containsKey(MyOfferAPIProxy.MYOFFER_DEFAULT_TAG)) {
            isDefaultOffer = (Boolean) serverExtras.get(MyOfferAPIProxy.MYOFFER_DEFAULT_TAG);
        }

        initBannerAdObject(context);
        return true;
    }

    @Override
    public void destory() {
        mBannerView = null;
        if (mMyOfferBannerAd != null) {
            mMyOfferBannerAd.setListener(null);
            mMyOfferBannerAd.destroy();
            mMyOfferBannerAd = null;
        }
    }

    @Override
    public String getNetworkPlacementId() {
        return offer_id;
    }

    @Override
    public String getNetworkSDKVersion() {
        return Const.SDK_VERSION_NAME;
    }

    @Override
    public String getNetworkName() {
        return "MyOffer";
    }
}
