/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.mintegral;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;
import com.mintegral.msdk.mtgbid.out.BidManager;
import com.mintegral.msdk.out.CustomInfoManager;
import com.mintegral.msdk.out.MTGBidRewardVideoHandler;
import com.mintegral.msdk.out.MTGRewardVideoHandler;
import com.mintegral.msdk.out.RewardVideoListener;

import java.util.Map;

/**
 * Created by zhou on 2018/6/27.
 */
public class MintegralATRewardedVideoAdapter extends CustomRewardVideoAdapter {
    private final String TAG = MintegralATRewardedVideoAdapter.class.getSimpleName();

    MTGRewardVideoHandler mMvRewardVideoHandler;
    MTGBidRewardVideoHandler mMvBidRewardVideoHandler;
    String placementId = "";
    String unitId = "";
    String mPayload;
    String mCustomData = "{}";

    Context context;

    @Override
    public void loadCustomNetworkAd(final Context context, Map<String, Object> serverExtra, Map<String, Object> localExtra) {

        String appid = (String) serverExtra.get("appid");
        String appkey = (String) serverExtra.get("appkey");
        unitId = (String) serverExtra.get("unitid");

        if (TextUtils.isEmpty(appid) || TextUtils.isEmpty(appkey) || TextUtils.isEmpty(unitId)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "mintegral appid, appkey or unitid is empty!");
            }
            return;
        }

        if (serverExtra.containsKey("payload")) {
            mPayload = serverExtra.get("payload").toString();
        }

        if (serverExtra.containsKey("tp_info")) {
            mCustomData = serverExtra.get("tp_info").toString();
        }

        if (serverExtra.containsKey("placement_id")) {
            placementId = serverExtra.get("placement_id").toString();
        }

        MintegralATInitManager.getInstance().initSDK(context.getApplicationContext(), serverExtra, new MintegralATInitManager.InitCallback() {
            @Override
            public void onSuccess() {
                //init
                init(context);
                //load ad
                startLoad();
            }

            @Override
            public void onError(Throwable e) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError("", e.getMessage());
                }
            }
        });
    }

    /***
     * init
     */
    private void init(Context context) {
        this.context = context;
        RewardVideoListener videoListener = new RewardVideoListener() {

            @Override
            public void onVideoLoadSuccess(String placementId, String unitId) {
                try {
                    if (mMvRewardVideoHandler != null) {
                        MintegralATInitManager.getInstance().put(getTrackingInfo().getmUnitGroupUnitId(), mMvRewardVideoHandler);
                    }

                    if (mMvBidRewardVideoHandler != null) {
                        MintegralATInitManager.getInstance().put(getTrackingInfo().getmUnitGroupUnitId(), mMvBidRewardVideoHandler);
                    }
                } catch (Exception e) {

                }

                if (mLoadListener != null) {
                    mLoadListener.onAdCacheLoaded();
                }
            }

            @Override
            public void onLoadSuccess(String placementId, String unitId) {
                if (mLoadListener != null) {
                    mLoadListener.onAdDataLoaded();
                }
            }

            @Override
            public void onVideoLoadFail(String pErrorMSG) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError("", pErrorMSG);
                }
            }

            @Override
            public void onAdShow() {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayStart();
                }
            }

            @Override
            public void onAdClose(boolean pIsCompleteView, String pRewardName,
                                  float pRewardAmout) {
                if (mImpressionListener != null) {
                    if (pIsCompleteView) {
                        mImpressionListener.onReward();
                    }
                    mImpressionListener.onRewardedVideoAdClosed();
                }

                try {
                    MintegralATInitManager.getInstance().remove(getTrackingInfo().getmUnitGroupUnitId());
                } catch (Exception e) {

                }
            }

            @Override
            public void onShowFail(String pErrorMSG) {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayFailed("", pErrorMSG);
                }

            }

            @Override
            public void onVideoAdClicked(String placementId, String unitId) {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayClicked();
                }
            }

            @Override
            public void onVideoComplete(String placementId, String unitId) {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayEnd();
                }
            }

            @Override
            public void onEndcardShow(String placementId, String unitId) {

            }
        };

        if (TextUtils.isEmpty(mPayload)) {
            mMvRewardVideoHandler = new MTGRewardVideoHandler(context.getApplicationContext(), placementId, unitId);
            mMvRewardVideoHandler.setRewardVideoListener(videoListener);
        } else {
            mMvBidRewardVideoHandler = new MTGBidRewardVideoHandler(context.getApplicationContext(), placementId, unitId);
            mMvBidRewardVideoHandler.setRewardVideoListener(videoListener);
        }
    }

    @Override
    public boolean initNetworkObjectByPlacementId(Context context, Map<String, Object> serverExtra, Map<String, Object> localExtra) {
        if (serverExtra != null) {
            if (serverExtra.containsKey("appid") && serverExtra.containsKey("appkey") && serverExtra.containsKey("unitid")) {
                unitId = serverExtra.get("unitid").toString();
                if (serverExtra.containsKey("placement_id")) {
                    placementId = serverExtra.get("placement_id").toString();
                }
                init(context);
                return true;
            }
        }
        return false;
    }

    /***
     * load ad
     */
    public void startLoad() {
        if (mMvRewardVideoHandler != null) {
            try {
                CustomInfoManager.getInstance().setCustomInfo(unitId, CustomInfoManager.TYPE_LOAD, mCustomData);
            } catch (Throwable e) {
            }
            mMvRewardVideoHandler.load();
        }

        if (mMvBidRewardVideoHandler != null) {
            try {
                CustomInfoManager.getInstance().setCustomInfo(unitId, CustomInfoManager.TYPE_BIDLOAD, mCustomData);
            } catch (Throwable e) {
            }
            mMvBidRewardVideoHandler.loadFromBid(mPayload);
        }
    }

    @Override
    public boolean isAdReady() {
        if (mMvRewardVideoHandler != null) {
            return mMvRewardVideoHandler.isReady();
        }

        if (mMvBidRewardVideoHandler != null) {
            return mMvBidRewardVideoHandler.isBidReady();
        }
        return false;
    }

    @Override
    public void show(Activity activity) {
        if (mMvRewardVideoHandler != null) {
            mMvRewardVideoHandler.show("1", mUserId);
        }

        if (mMvBidRewardVideoHandler != null) {
            mMvBidRewardVideoHandler.showFromBid("1", mUserId);
        }
    }

    @Override
    public String getNetworkName() {
        return MintegralATInitManager.getInstance().getNetworkName();
    }

    @Override
    public void destory() {
        if (mMvBidRewardVideoHandler != null) {
            mMvBidRewardVideoHandler.setRewardVideoListener(null);
            mMvBidRewardVideoHandler = null;
        }

        if (mMvRewardVideoHandler != null) {
            mMvRewardVideoHandler.setRewardVideoListener(null);
            mMvRewardVideoHandler = null;
        }
    }

    @Override
    public String getNetworkPlacementId() {
        return unitId;
    }

    @Override
    public String getNetworkSDKVersion() {
        return MintegralATConst.getNetworkVersion();
    }

    @Override
    public String getBiddingToken(Context context) {
        return BidManager.getBuyerUid(context);
    }
}