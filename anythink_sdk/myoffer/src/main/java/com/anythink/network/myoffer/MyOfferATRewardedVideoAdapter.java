package com.anythink.network.myoffer;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.ErrorCode;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.entity.AdTrackingInfo;
import com.anythink.core.common.utils.CommonDeviceUtil;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoListener;
import com.anythink.core.common.MyOfferAPIProxy;
import com.anythink.myoffer.network.base.MyOfferBaseAd;
import com.anythink.myoffer.network.rewardvideo.MyOfferRewardVideoAd;
import com.anythink.myoffer.network.rewardvideo.MyOfferRewardVideoAdListener;

import java.util.HashMap;
import java.util.Map;

public class MyOfferATRewardedVideoAdapter extends CustomRewardVideoAdapter {

    private String offer_id = "";
    private String myoffer_setting = "";
    private String placement_id = "";
    private MyOfferRewardVideoAd mMyOfferRewardVideoAd;
    private boolean isDefaultOffer = false; //用于判断兜底offer的

    /**
     * @param activity
     * @param serverExtras   key: myoffer_setting(Play Setting)，topon_placement(PlacementId)，my_oid(MyOfferId)
     * @param mediationSetting
     * @param customRewardVideoListener
     */
    @Override
    public void loadRewardVideoAd(Activity activity, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, CustomRewardVideoListener customRewardVideoListener) {

        mLoadResultListener = customRewardVideoListener;

        if (serverExtras.containsKey("my_oid")) {
            offer_id = serverExtras.get("my_oid").toString();
        }
        if (serverExtras.containsKey("myoffer_setting")) {
            myoffer_setting = serverExtras.get("myoffer_setting").toString();
        }
        if (serverExtras.containsKey("topon_placement")) {
            placement_id = serverExtras.get("topon_placement").toString();
        }


        if (TextUtils.isEmpty(offer_id) || TextUtils.isEmpty(placement_id)) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onRewardedVideoAdFailed(this,
                        ErrorCode.getErrorCode(ErrorCode.noADError, "", "my_oid、topon_placement can not be null!"));
            }
            return;
        }

        initRewardedVideoObject(activity);

        mMyOfferRewardVideoAd.load();
    }

    private void initRewardedVideoObject(Context context) {
        mMyOfferRewardVideoAd = new MyOfferRewardVideoAd(context, placement_id, offer_id, myoffer_setting, isDefaultOffer);
        mMyOfferRewardVideoAd.setListener(new MyOfferRewardVideoAdListener() {
            @Override
            public void onVideoAdPlayStart() {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayStart(MyOfferATRewardedVideoAdapter.this);
                }
            }

            @Override
            public void onVideoAdPlayEnd() {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayEnd(MyOfferATRewardedVideoAdapter.this);
                }
            }

            @Override
            public void onVideoShowFailed(MyOfferError error) {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayFailed(MyOfferATRewardedVideoAdapter.this,
                            ErrorCode.getErrorCode(ErrorCode.rewardedVideoPlayError, error.getCode(), error.getDesc()));
                }
            }

            @Override
            public void onRewarded() {
                if (mImpressionListener != null) {
                    mImpressionListener.onReward(MyOfferATRewardedVideoAdapter.this);
                }
            }

            @Override
            public void onAdLoaded() {
                if (mLoadResultListener != null) {
                    mLoadResultListener.onRewardedVideoAdLoaded(MyOfferATRewardedVideoAdapter.this);
                }
            }

            @Override
            public void onAdLoadFailed(MyOfferError error) {
                if (mLoadResultListener != null) {
                    mLoadResultListener.onRewardedVideoAdFailed(MyOfferATRewardedVideoAdapter.this,
                            ErrorCode.getErrorCode(ErrorCode.noADError, error.getCode(), error.getDesc()));
                }
            }

            @Override
            public void onAdShow() {
            }

            @Override
            public void onAdClosed() {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdClosed(MyOfferATRewardedVideoAdapter.this);
                }
            }

            @Override
            public void onAdClick() {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayClicked(MyOfferATRewardedVideoAdapter.this);
                }
            }
        });
    }

    /**
     * @param context
     * @param serverExtras  key: myoffer_setting(Play Setting)，topon_placement(PlacementId)，my_oid(MyOfferId)
     * @param mediationSetting
     * @return
     */
    @Override
    public boolean initNetworkObjectByPlacementId(Context context, Map<String, Object> serverExtras, ATMediationSetting mediationSetting) {
        if (serverExtras.containsKey("my_oid")) {
            offer_id = serverExtras.get("my_oid").toString();
        }
        if (serverExtras.containsKey("myoffer_setting")) {
            myoffer_setting = serverExtras.get("myoffer_setting").toString();
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
    public void onResume(Activity activity) {

    }

    @Override
    public void onPause(Activity activity) {

    }

    @Override
    public boolean isAdReady() {
        if (mMyOfferRewardVideoAd != null) {
            return mMyOfferRewardVideoAd.isReady();
        }
        return false;
    }

    @Override
    public String getSDKVersion() {
        return Const.SDK_VERSION_NAME;
    }

    @Override
    public void clean() {

    }

    @Override
    public String getNetworkName() {
        return "MyOffer";
    }
}
