package com.anythink.network.myoffer;

import android.content.Context;

import com.anythink.core.api.BaseAd;
import com.anythink.core.common.MyOfferAPIProxy;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.entity.MyOfferSetting;
import com.anythink.myoffer.network.base.MyOfferAdListener;
import com.anythink.myoffer.network.nativead.MyOfferNativeAd;
import com.anythink.nativead.unitgroup.api.CustomNativeAdapter;

import java.util.Map;

public class MyOfferATAdapter extends CustomNativeAdapter {
    private String offer_id = "";
    private MyOfferSetting myofferSetting;
    private String placement_id = "";

    private boolean isDefaultOffer = false; //用于判断兜底offer的

    MyOfferNativeAd mMyOfferNativeAd;

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

        initNativeObject(context);

        mMyOfferNativeAd.load();

    }

    private void initNativeObject(final Context context) {
        mMyOfferNativeAd = new MyOfferNativeAd(context, placement_id, offer_id, myofferSetting, isDefaultOffer);
        mMyOfferNativeAd.setListener(new MyOfferAdListener() {

            @Override
            public void onAdLoaded() {
                if (mLoadListener != null) {
                    MyOfferATNativeAd myOfferATNativeAd = new MyOfferATNativeAd(context, mMyOfferNativeAd);
                    mLoadListener.onAdCacheLoaded(myOfferATNativeAd);
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
            }

            @Override
            public void onAdClosed() {
            }

            @Override
            public void onAdClick() {
            }
        });
    }


    @Override
    public boolean initNetworkObjectByPlacementId(Context context, Map<String, Object> serverExtras, Map<String, Object> localExtra) {
        if (serverExtras.containsKey("my_oid")) {
            offer_id = serverExtras.get("my_oid").toString();
        }
        if (serverExtras.containsKey("myoffer_setting")) {
            myofferSetting = (MyOfferSetting) serverExtras.get("myoffer_setting");
        }
        if (serverExtras.containsKey("topon_placement")) {
            placement_id = serverExtras.get("topon_placement").toString();
        }

        if (serverExtras.containsKey(MyOfferAPIProxy.MYOFFER_DEFAULT_TAG)) {
            isDefaultOffer = (Boolean) serverExtras.get(MyOfferAPIProxy.MYOFFER_DEFAULT_TAG);
        }

        mMyOfferNativeAd = new MyOfferNativeAd(context, placement_id, offer_id, myofferSetting, isDefaultOffer);
        return true;
    }


    @Override
    public BaseAd getBaseAdObject(Context context) {
        if (mMyOfferNativeAd != null && mMyOfferNativeAd.isReady()) {
            MyOfferATNativeAd myOfferATNativeAd = new MyOfferATNativeAd(context, mMyOfferNativeAd);
            return myOfferATNativeAd;
        }
        return null;
    }

    @Override
    public void destory() {
        if (mMyOfferNativeAd != null) {
            mMyOfferNativeAd.setListener(null);
            mMyOfferNativeAd = null;
        }
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
