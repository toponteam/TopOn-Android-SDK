package com.anythink.network.myoffer;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.ErrorCode;
import com.anythink.core.common.MyOfferAPIProxy;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.entity.AdTrackingInfo;
import com.anythink.core.common.entity.MyOfferSetting;
import com.anythink.core.common.utils.CommonDeviceUtil;
import com.anythink.myoffer.network.base.MyOfferBaseAd;
import com.anythink.myoffer.network.rewardvideo.MyOfferRewardVideoAd;
import com.anythink.myoffer.network.rewardvideo.MyOfferRewardVideoAdListener;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;

import java.util.HashMap;
import java.util.Map;

public class MyOfferATRewardedVideoAdapter extends CustomRewardVideoAdapter {

    private String offer_id = "";
    private MyOfferSetting myofferSetting = null;
    private String placement_id = "";
    private MyOfferRewardVideoAd mMyOfferRewardVideoAd;
    private boolean isDefaultOffer = false; //用于判断兜底offer的

    /**
     * @param context
     * @param serverExtras  key: myoffer_setting(Play Setting)，topon_placement(PlacementId)，my_oid(MyOfferId)
     * @param localExtras
     */
    @Override
    public void loadCustomNetworkAd(Context context, Map<String, Object> serverExtras, Map<String, Object> localExtras) {

        if (serverExtras.containsKey("my_oid")) {
            offer_id = serverExtras.get("my_oid").toString();
        }
        if (serverExtras.containsKey("myoffer_setting")) {
            myofferSetting = (MyOfferSetting) serverExtras.get("myoffer_setting");
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

        initRewardedVideoObject(context);

        mMyOfferRewardVideoAd.load();
    }

    private void initRewardedVideoObject(Context context) {
        mMyOfferRewardVideoAd = new MyOfferRewardVideoAd(context, placement_id, offer_id, myofferSetting, isDefaultOffer);
        mMyOfferRewardVideoAd.setListener(new MyOfferRewardVideoAdListener() {
            @Override
            public void onVideoAdPlayStart() {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayStart();
                }
            }

            @Override
            public void onVideoAdPlayEnd() {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayEnd();
                }
            }

            @Override
            public void onVideoShowFailed(MyOfferError error) {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayFailed(error.getCode(), error.getDesc());
                }
            }

            @Override
            public void onRewarded() {
                if (mImpressionListener != null) {
                    mImpressionListener.onReward();
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
            }

            @Override
            public void onAdClosed() {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdClosed();
                }
            }

            @Override
            public void onAdClick() {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayClicked();
                }
            }
        });
    }

    /**
     * @param context
     * @param serverExtras     key: myoffer_setting(Play Setting)，topon_placement(PlacementId)，my_oid(MyOfferId)
     * @param localExtras
     * @return
     */
    @Override
    public boolean initNetworkObjectByPlacementId(Context context, Map<String, Object> serverExtras, Map<String,Object> localExtras) {
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

        if (TextUtils.isEmpty(offer_id) || TextUtils.isEmpty(placement_id)) {
            return false;
        }
        initRewardedVideoObject(context);
        return true;
    }

    @Override
    public void show(Activity activity) {
        int orientation = CommonDeviceUtil.orientation(activity);
        if (isAdReady()) {
            Map<String, Object> extra = new HashMap<>(1);
            AdTrackingInfo trackingInfo = getTrackingInfo();
            if (trackingInfo != null) {
                String requestId = trackingInfo.getmRequestId();
                extra.put(MyOfferBaseAd.EXTRA_REQUEST_ID, requestId);
                String scenario = trackingInfo.getmScenario();
                extra.put(MyOfferBaseAd.EXTRA_SCENARIO, scenario);
            }
            extra.put(MyOfferBaseAd.EXTRA_ORIENTATION, orientation);
            mMyOfferRewardVideoAd.show(extra);
        }
    }

    @Override
    public boolean isAdReady() {
        if (mMyOfferRewardVideoAd != null) {
            return mMyOfferRewardVideoAd.isReady();
        }
        return false;
    }

    @Override
    public String getNetworkSDKVersion() {
        return Const.SDK_VERSION_NAME;
    }

    @Override
    public void destory() {
        if (mMyOfferRewardVideoAd != null) {
            mMyOfferRewardVideoAd.setListener(null);
            mMyOfferRewardVideoAd = null;
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
