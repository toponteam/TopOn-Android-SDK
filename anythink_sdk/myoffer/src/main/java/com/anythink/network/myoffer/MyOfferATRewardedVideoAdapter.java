/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.myoffer;

import android.app.Activity;
import android.content.Context;

import com.anythink.basead.entity.OfferError;
import com.anythink.basead.listeners.VideoAdListener;
import com.anythink.core.common.MyOfferAPIProxy;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.entity.MyOfferRequestInfo;
import com.anythink.core.common.utils.CommonDeviceUtil;
import com.anythink.basead.myoffer.MyOfferBaseAd;
import com.anythink.basead.myoffer.MyOfferRewardVideoAd;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;

import java.util.HashMap;
import java.util.Map;

public class MyOfferATRewardedVideoAdapter extends CustomRewardVideoAdapter {

    private String offer_id = "";
    private MyOfferRewardVideoAd mMyOfferRewardVideoAd;
    private boolean isDefaultOffer = false; //用于判断兜底offer的

    MyOfferRequestInfo mMyOfferRequestInfo;

    /**
     * @param context
     * @param serverExtras key: myoffer_setting(Play Setting)，topon_placement(PlacementId)，my_oid(MyOfferId)
     * @param localExtras
     */
    @Override
    public void loadCustomNetworkAd(Context context, Map<String, Object> serverExtras, Map<String, Object> localExtras) {

        if (serverExtras.containsKey("my_oid")) {
            offer_id = serverExtras.get("my_oid").toString();
        }
        if (serverExtras.containsKey(Const.NETWORK_REQUEST_PARAMS_KEY.MYOFFER_PARAMS_KEY)) {
            mMyOfferRequestInfo = (MyOfferRequestInfo) serverExtras.get(Const.NETWORK_REQUEST_PARAMS_KEY.MYOFFER_PARAMS_KEY);
        }

        initRewardedVideoObject(context);

        mMyOfferRewardVideoAd.load();
    }

    private void initRewardedVideoObject(Context context) {
        mMyOfferRewardVideoAd = new MyOfferRewardVideoAd(context, mMyOfferRequestInfo.placementId, offer_id, mMyOfferRequestInfo.myOfferSetting, isDefaultOffer);
        mMyOfferRewardVideoAd.setListener(new VideoAdListener() {
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
            public void onVideoShowFailed(OfferError error) {
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
            public void onAdDataLoaded() {

            }

            @Override
            public void onAdCacheLoaded() {
                if (mLoadListener != null) {
                    mLoadListener.onAdCacheLoaded();
                }
            }

            @Override
            public void onAdLoadFailed(OfferError error) {
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
     * @param serverExtras key: myoffer_setting(Play Setting)，topon_placement(PlacementId)，my_oid(MyOfferId)
     * @param localExtras
     * @return
     */
    @Override
    public boolean initNetworkObjectByPlacementId(Context context, Map<String, Object> serverExtras, Map<String, Object> localExtras) {
        if (serverExtras.containsKey("my_oid")) {
            offer_id = serverExtras.get("my_oid").toString();
        }
        if (serverExtras.containsKey(Const.NETWORK_REQUEST_PARAMS_KEY.MYOFFER_PARAMS_KEY)) {
            mMyOfferRequestInfo = (MyOfferRequestInfo) serverExtras.get(Const.NETWORK_REQUEST_PARAMS_KEY.MYOFFER_PARAMS_KEY);
        }
        if (serverExtras.containsKey(MyOfferAPIProxy.MYOFFER_DEFAULT_TAG)) {
            isDefaultOffer = (Boolean) serverExtras.get(MyOfferAPIProxy.MYOFFER_DEFAULT_TAG);
        }

        initRewardedVideoObject(context);
        return true;
    }

    @Override
    public void show(Activity activity) {
        int orientation = CommonDeviceUtil.orientation(activity);
        if (isAdReady()) {
            Map<String, Object> extra = new HashMap<>(1);
            extra.put(MyOfferBaseAd.EXTRA_REQUEST_ID, mMyOfferRequestInfo.requestId);
            extra.put(MyOfferBaseAd.EXTRA_SCENARIO, mScenario);
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
