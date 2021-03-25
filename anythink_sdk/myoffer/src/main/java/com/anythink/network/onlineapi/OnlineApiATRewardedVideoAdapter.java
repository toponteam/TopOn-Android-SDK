/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.onlineapi;

import android.app.Activity;
import android.content.Context;

import com.anythink.basead.BaseAdUtils;
import com.anythink.basead.entity.OfferError;
import com.anythink.basead.innerad.OwnBaseAd;
import com.anythink.basead.innerad.OwnBaseAdConfig;
import com.anythink.basead.innerad.OwnRewardVideoAd;
import com.anythink.basead.listeners.AdLoadListener;
import com.anythink.basead.listeners.VideoAdEventListener;
import com.anythink.basead.myoffer.MyOfferBaseAd;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.entity.BaseAdRequestInfo;
import com.anythink.core.common.utils.CommonDeviceUtil;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;

import java.util.HashMap;
import java.util.Map;

public class OnlineApiATRewardedVideoAdapter extends CustomRewardVideoAdapter {

    BaseAdRequestInfo mRequestInfo;
    OwnRewardVideoAd mRewardVideoAd;
    String mUnitId;

    Map<String, Object> mCustomMap;

    @Override
    public void show(Activity activity) {
        int orientation = CommonDeviceUtil.orientation(activity);
        Map<String, Object> extra = new HashMap<>(1);
        extra.put(MyOfferBaseAd.EXTRA_SCENARIO, mScenario);
        extra.put(MyOfferBaseAd.EXTRA_ORIENTATION, orientation);

        mRewardVideoAd.setListener(new VideoAdEventListener() {
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

            @Override
            public void onDeeplinkCallback(boolean isSuccess) {
                if (mImpressionListener != null) {
                    mImpressionListener.onDeeplinkCallback(isSuccess);
                }
            }
        });
        if (mRewardVideoAd != null) {
            mRewardVideoAd.show(extra);
        }
    }

    @Override
    public void loadCustomNetworkAd(Context context, final Map<String, Object> serverExtra, Map<String, Object> localExtra) {

        initRewardedVideoAdObject(context, serverExtra);

        mRewardVideoAd.load(new AdLoadListener() {
            @Override
            public void onAdDataLoaded() {
                if (mLoadListener != null) {
                    mLoadListener.onAdDataLoaded();
                }
            }

            @Override
            public void onAdCacheLoaded() {
                mCustomMap = BaseAdUtils.fillBaseAdCustomMap(mRewardVideoAd);
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
        });
    }

    @Override
    public boolean initNetworkObjectByPlacementId(Context context, Map<String, Object> serverExtras, Map<String, Object> localExtra) {
        initRewardedVideoAdObject(context, serverExtras);
        return true;
    }

    private void initRewardedVideoAdObject(Context context, Map<String, Object> serverExtra) {

        mUnitId = serverExtra.get("unit_id") != null ? serverExtra.get("unit_id").toString() : "";

        int videoMute = 0;
        int showCloseButtonTime = -1;

        if (serverExtra.containsKey("v_m")) {
            Object v_m = serverExtra.get("v_m");
            if (v_m != null) {
                videoMute = Integer.parseInt(v_m.toString());
            }
        }

        if (serverExtra.containsKey("s_c_t")) {
            Object s_c_t = serverExtra.get("s_c_t");
            if (s_c_t != null) {
                showCloseButtonTime = Integer.parseInt(s_c_t.toString());
            }
        }

        mRequestInfo = (BaseAdRequestInfo) serverExtra.get(Const.NETWORK_REQUEST_PARAMS_KEY.BASE_AD_PARAMS_KEY);
        mRewardVideoAd = new OwnRewardVideoAd(context, OwnBaseAd.OFFER_TYPE.ONLINE_API_OFFER_REQUEST_TYPE, mRequestInfo);
        mRewardVideoAd.setAdConfig(new OwnBaseAdConfig.Builder()
                .isMute(videoMute)
                .showCloseButtonTime(showCloseButtonTime)
                .build()
        );
    }

    @Override
    public void destory() {
        if (mRewardVideoAd != null) {
            mRewardVideoAd.destroy();
            mRewardVideoAd = null;
        }
    }

    @Override
    public String getNetworkPlacementId() {
        return mUnitId;
    }

    @Override
    public String getNetworkSDKVersion() {
        return "";
    }

    @Override
    public String getNetworkName() {
        return "";
    }

    @Override
    public boolean isAdReady() {
        if (mRewardVideoAd != null) {
            mCustomMap = BaseAdUtils.fillBaseAdCustomMap(mRewardVideoAd);
            return mRewardVideoAd.isAdReady();
        }
        return false;
    }

    @Override
    public Map<String, Object> getNetworkInfoMap() {
        return mCustomMap;
    }
}
