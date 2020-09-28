package com.anythink.network.myoffer;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.ErrorCode;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.entity.AdTrackingInfo;
import com.anythink.core.common.entity.MyOfferSetting;
import com.anythink.core.common.utils.CommonDeviceUtil;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;
import com.anythink.core.common.MyOfferAPIProxy;
import com.anythink.myoffer.network.base.MyOfferBaseAd;
import com.anythink.myoffer.network.interstitial.MyOfferInterstitialAd;
import com.anythink.myoffer.network.interstitial.MyOfferInterstitialAdListener;

import java.util.HashMap;
import java.util.Map;

public class MyOfferATInterstitialAdapter extends CustomInterstitialAdapter {

    private String offer_id = "";
    private MyOfferSetting myOfferSetting;
    private String placement_id = "";
    private MyOfferInterstitialAd mMyOfferInterstitialAd;
    private boolean isDefaultOffer = false; //用于判断兜底offer的

    /**
     * @param context
     * @param serverExtras  key: myoffer_setting(Play Setting)，topon_placement(PlacementId)，my_oid(MyOfferId)
     */
    @Override
    public void loadCustomNetworkAd(Context context, Map<String, Object> serverExtras, Map<String,Object> localMap) {

        if (serverExtras.containsKey("my_oid")) {
            offer_id = serverExtras.get("my_oid").toString();
        }
        if (serverExtras.containsKey("myoffer_setting")) {
            myOfferSetting = (MyOfferSetting) serverExtras.get("myoffer_setting");
        }
        if (serverExtras.containsKey("topon_placement")) {
            placement_id = serverExtras.get("topon_placement").toString();
        }


        if (TextUtils.isEmpty(offer_id) || TextUtils.isEmpty(placement_id)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "my_oid、topon_placement can not be null!");
            }
            return;
        }

        initInterstitialAdObject(context);

        mMyOfferInterstitialAd.load();
    }

    private void initInterstitialAdObject(Context context) {
        mMyOfferInterstitialAd = new MyOfferInterstitialAd(context, placement_id, offer_id, myOfferSetting, isDefaultOffer);
        mMyOfferInterstitialAd.setListener(new MyOfferInterstitialAdListener() {
            @Override
            public void onVideoAdPlayStart() {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdVideoStart();
                }
            }

            @Override
            public void onVideoAdPlayEnd() {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdVideoEnd();
                }
            }

            @Override
            public void onVideoShowFailed(MyOfferError error) {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdVideoError(error.getCode(), error.getDesc());
                }
            }

            @Override
            public void onAdLoaded() {
                if (mLoadListener != null) {
                    mLoadListener.onAdCacheLoaded();
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
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdShow();
                }
            }

            @Override
            public void onAdClosed() {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdClose();
                }
            }

            @Override
            public void onAdClick() {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdClicked();
                }
            }
        });
    }

    /**
     * @param context
     * @param serverExtras     key: myoffer_setting(Play Setting)，topon_placement(PlacementId)，my_oid(MyOfferId)
     * @param localMap
     * @return
     */
    @Override
    public boolean initNetworkObjectByPlacementId(Context context, Map<String, Object> serverExtras, Map<String,Object> localMap) {
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

        if (TextUtils.isEmpty(offer_id) || TextUtils.isEmpty(placement_id)) {
            return false;
        }

        initInterstitialAdObject(context);
        return true;
    }

    @Override
    public void show(Activity activity) {
        if (isAdReady()) {
            Map<String, Object> extraMap = new HashMap<>(1);
            AdTrackingInfo trackingInfo = getTrackingInfo();

            int orientation = CommonDeviceUtil.orientation(activity);
            if (trackingInfo != null) {
                String requestId = trackingInfo.getmRequestId();
                extraMap.put(MyOfferBaseAd.EXTRA_REQUEST_ID, requestId);
                String scenario = trackingInfo.getmScenario();
                extraMap.put(MyOfferBaseAd.EXTRA_SCENARIO, scenario);
            }
            extraMap.put(MyOfferBaseAd.EXTRA_ORIENTATION, orientation);
            mMyOfferInterstitialAd.show(extraMap);
        }
    }


    @Override
    public boolean isAdReady() {
        if (mMyOfferInterstitialAd != null) {
            return mMyOfferInterstitialAd.isReady();
        }
        return false;
    }


    @Override
    public String getNetworkSDKVersion() {
        return Const.SDK_VERSION_NAME;
    }

    @Override
    public void destory() {
        if (mMyOfferInterstitialAd != null) {
            mMyOfferInterstitialAd.setListener(null);
            mMyOfferInterstitialAd = null;
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
}
