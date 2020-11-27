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
import android.util.Log;

import com.anythink.core.api.ATAdConst;
import com.anythink.nativead.unitgroup.api.CustomNativeAd;
import com.anythink.nativead.unitgroup.api.CustomNativeAdapter;
import com.mintegral.msdk.MIntegralConstans;
import com.mintegral.msdk.mtgbid.out.BidManager;
import com.mintegral.msdk.out.AutoPlayMode;
import com.mintegral.msdk.out.Campaign;
import com.mintegral.msdk.out.CustomInfoManager;
import com.mintegral.msdk.out.Frame;
import com.mintegral.msdk.out.MTGMultiStateEnum;
import com.mintegral.msdk.out.MTGNativeAdvancedHandler;
import com.mintegral.msdk.out.MtgBidNativeHandler;
import com.mintegral.msdk.out.MtgNativeHandler;
import com.mintegral.msdk.out.NativeAdvancedAdListener;
import com.mintegral.msdk.out.NativeListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * @author zhou
 * @date 2018/1/17
 */

public class MintegralATAdapter extends CustomNativeAdapter {

    private static final String TAG = MintegralATAdapter.class.getSimpleName();
    String mPayload;
    String mCustomData = "{}";
    String mUnitType;
    String videoMuted;
    String videoAutoPlay;
    String closeButton;

    int expressWidth;
    int expressHeight;
    String mUnitId;

    private void startLoad(Context context, Map<String, Object> serverExtra, String placementId, String unitId, boolean suportVideo, int requestNum) {

        boolean isAutoPlay = false;
        try {
            if (serverExtra != null) {
                isAutoPlay = Boolean.parseBoolean(serverExtra.get(CustomNativeAd.IS_AUTO_PLAY_KEY).toString());
            }
        } catch (Exception e) {

        }

        if (TextUtils.equals(mUnitType, "1")) {
            loadExpressAd(context, placementId, unitId);
        } else {
            loadAd(context, requestNum, placementId, unitId, suportVideo, isAutoPlay);
        }

    }

    private void loadExpressAd(final Context context, final String placementId, final String unitId) {
        final MTGNativeAdvancedHandler mtgNativeAdvancedHandler = new MTGNativeAdvancedHandler((Activity) context, placementId, unitId);
        if (!TextUtils.isEmpty(videoMuted)) {
            switch (videoMuted) {
                case "0":
                    mtgNativeAdvancedHandler.setPlayMuteState(MIntegralConstans.REWARD_VIDEO_PLAY_MUTE);
                    break;
                case "1":
                    mtgNativeAdvancedHandler.setPlayMuteState(MIntegralConstans.REWARD_VIDEO_PLAY_NOT_MUTE);
                    break;
            }
        }

        if (!TextUtils.isEmpty(videoAutoPlay)) {
            switch (videoAutoPlay) {
                case "1":
                    mtgNativeAdvancedHandler.autoLoopPlay(AutoPlayMode.PLAY_WHEN_NETWORK_IS_WIFI);
                    break;
                case "2":
                    mtgNativeAdvancedHandler.autoLoopPlay(AutoPlayMode.PLAY_WHEN_USER_CLICK);
                    break;
                case "3":
                    mtgNativeAdvancedHandler.autoLoopPlay(AutoPlayMode.PLAY_WHEN_NETWORK_IS_AVAILABLE);
                    break;
            }
        }

        if (!TextUtils.isEmpty(closeButton)) {
            switch (closeButton) {
                case "0":
                    mtgNativeAdvancedHandler.setCloseButtonState(MTGMultiStateEnum.positive);
                    break;
                case "1":
                    mtgNativeAdvancedHandler.setCloseButtonState(MTGMultiStateEnum.negative);
                    break;
            }
        }

        mtgNativeAdvancedHandler.setNativeViewSize(expressWidth, expressHeight);

        mtgNativeAdvancedHandler.setAdListener(new NativeAdvancedAdListener() {
            @Override
            public void onLoadFailed(String s) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError(s, s);
                }
                mtgNativeAdvancedHandler.setAdListener(null);
            }

            @Override
            public void onLoadSuccessed() {
                MintegralATExpressNativeAd mintegralATExpressNativeAd = new MintegralATExpressNativeAd(context, mtgNativeAdvancedHandler, false);
                if (mLoadListener != null) {
                    List<CustomNativeAd> resultList = new ArrayList<>();
                    resultList.add(mintegralATExpressNativeAd);

                    CustomNativeAd[] customNativeAds = new CustomNativeAd[resultList.size()];
                    customNativeAds = resultList.toArray(customNativeAds);
                    mLoadListener.onAdCacheLoaded(customNativeAds);
                }
            }

            @Override
            public void onLogImpression() {

            }

            @Override
            public void onClick() {

            }

            @Override
            public void onLeaveApp() {

            }

            @Override
            public void showFullScreen() {

            }

            @Override
            public void closeFullScreen() {

            }

            @Override
            public void onClose() {

            }
        });

        if (TextUtils.isEmpty(mPayload)) {
            try {
                CustomInfoManager.getInstance().setCustomInfo(unitId, CustomInfoManager.TYPE_LOAD, mCustomData);
            } catch (Throwable e) {
            }

            mtgNativeAdvancedHandler.load();
        } else {
            try {
                CustomInfoManager.getInstance().setCustomInfo(unitId, CustomInfoManager.TYPE_BIDLOAD, mCustomData);
            } catch (Throwable e) {
            }

            mtgNativeAdvancedHandler.loadByToken(mPayload);
        }

    }


    private void loadAd(final Context context, final int adnum, final String placementId, final String unitId, boolean supportVideo, final boolean isAutoPlay) {
        Map<String, Object> properties = MtgNativeHandler
                .getNativeProperties(placementId, unitId);

        properties.put(MIntegralConstans.PROPERTIES_AD_NUM, adnum);
        properties.put(MIntegralConstans.PROPERTIES_LAYOUT_TYPE,
                MIntegralConstans.LAYOUT_NATIVE);
//        properties.put(MIntegralConstans.PLACEMENT_ID, placementId);
//        //MV 广告位 ID 必传
//        properties.put(MIntegralConstans.PROPERTIES_UNIT_ID, unitId);

        //设置是否支持视频
        properties.put(MIntegralConstans.NATIVE_VIDEO_SUPPORT, supportVideo);

        MtgNativeHandler mvNativeHandler = null;
        MtgBidNativeHandler mtgBidNativeHandler = null;

        if (TextUtils.isEmpty(mPayload)) {
            try {
                CustomInfoManager.getInstance().setCustomInfo(unitId, CustomInfoManager.TYPE_LOAD, mCustomData);
            } catch (Throwable e) {
            }

            mvNativeHandler = new MtgNativeHandler(properties, context.getApplicationContext());

        } else {
            try {
                CustomInfoManager.getInstance().setCustomInfo(unitId, CustomInfoManager.TYPE_BIDLOAD, mCustomData);
            } catch (Throwable e) {
            }

            mtgBidNativeHandler = new MtgBidNativeHandler(properties, context.getApplicationContext());
        }

        final MtgNativeHandler finalMvNativeHandler = mvNativeHandler;
        final MtgBidNativeHandler finalMtgBidNativeHandler = mtgBidNativeHandler;
        NativeListener.NativeAdListener listener = new NativeListener.NativeAdListener() {

            @Override
            public void onAdLoaded(List<Campaign> list, int i) {
                if (list == null || list.size() <= 0) {
                    if (mLoadListener != null) {
                        mLoadListener.onAdLoadError("", "Mintegral no ad return ");
                    }

                    if (finalMvNativeHandler != null) {
                        finalMvNativeHandler.setAdListener(null);
                        finalMvNativeHandler.release();
                    } else if (finalMtgBidNativeHandler != null) {
                        finalMtgBidNativeHandler.setAdListener(null);
                        finalMtgBidNativeHandler.bidRelease();
                    }
                    return;
                }

                boolean hasReturn = false;
                List<CustomNativeAd> resultList = new ArrayList<>();
                for (Campaign campaign : list) {
                    if (campaign != null) {
                        hasReturn = true;
                        boolean isHB = !TextUtils.isEmpty(mPayload);
                        MintegralATNativeAd mintegralNativeAd = new MintegralATNativeAd(context, placementId, unitId, campaign, isHB);
                        mintegralNativeAd.setIsAutoPlay(isAutoPlay);
                        resultList.add(mintegralNativeAd);
                    }
                }

                if (!hasReturn) {
                    if (mLoadListener != null) {
                        mLoadListener.onAdLoadError("", "Mintegral no ad return ");
                    }
                } else {
                    if (mLoadListener != null) {
                        CustomNativeAd[] customNativeAds = new CustomNativeAd[resultList.size()];
                        customNativeAds = resultList.toArray(customNativeAds);
                        mLoadListener.onAdCacheLoaded(customNativeAds);
                    }
                }

                if (finalMvNativeHandler != null) {
                    finalMvNativeHandler.setAdListener(null);
                    finalMvNativeHandler.release();
                } else if (finalMtgBidNativeHandler != null) {
                    finalMtgBidNativeHandler.setAdListener(null);
                    finalMtgBidNativeHandler.bidRelease();
                }
            }

            @Override
            public void onAdLoadError(String s) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError(s, s);
                }

                if (finalMvNativeHandler != null) {
                    finalMvNativeHandler.setAdListener(null);
                    finalMvNativeHandler.release();
                } else if (finalMtgBidNativeHandler != null) {
                    finalMtgBidNativeHandler.setAdListener(null);
                    finalMtgBidNativeHandler.bidRelease();
                }
            }

            @Override
            public void onAdClick(Campaign campaign) {
            }

            @Override
            public void onAdFramesLoaded(List<Frame> list) {
            }

            @Override
            public void onLoggingImpression(int i) {
            }
        };

        if (finalMvNativeHandler != null) {
            finalMvNativeHandler.setAdListener(listener);
            finalMvNativeHandler.load();
        } else if (finalMtgBidNativeHandler != null) {
            finalMtgBidNativeHandler.setAdListener(listener);
            finalMtgBidNativeHandler.bidLoad(mPayload);
        }
    }

    @Override
    public String getNetworkName() {
        return MintegralATInitManager.getInstance().getNetworkName();
    }

    @Override
    public void loadCustomNetworkAd(final Context context, final Map<String, Object> serverExtra, Map<String, Object> localExtra) {
        try {
            String appid = "";
            String unitId = "";
            String sdkKey = "";
            String placementId = "";
            String suportVideo_str = "1";
            //支持视频
            boolean suportVideo = false;

            try {
                if (serverExtra.containsKey("appid")) {
                    appid = serverExtra.get("appid").toString();
                }
                if (serverExtra.containsKey("unitid")) {
                    unitId = serverExtra.get("unitid").toString();
                }

                if (serverExtra.containsKey("placement_id")) {
                    placementId = serverExtra.get("placement_id").toString();
                }
                if (serverExtra.containsKey("appkey")) {
                    sdkKey = serverExtra.get("appkey").toString();
                }

                if (serverExtra.containsKey("payload")) {
                    mPayload = serverExtra.get("payload").toString();
                }

                if (serverExtra.containsKey("tp_info")) {
                    mCustomData = serverExtra.get("tp_info").toString();
                }

                if (serverExtra.containsKey("unit_type")) {
                    mUnitType = serverExtra.get("unit_type").toString();
                }

                if (serverExtra.containsKey("video_muted")) {
                    videoMuted = serverExtra.get("video_muted").toString();
                }

                if (serverExtra.containsKey("video_autoplay")) {
                    videoAutoPlay = serverExtra.get("video_autoplay").toString();
                }

                if (serverExtra.containsKey("close_button")) {
                    closeButton = serverExtra.get("close_button").toString();
                }


            } catch (Exception e) {
                e.printStackTrace();
            }

            if (TextUtils.isEmpty(appid) || TextUtils.isEmpty(unitId) || TextUtils.isEmpty(sdkKey)) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError("", "mintegral appid ,unitid or sdkkey is empty.");
                }
                return;
            }

            int requestNum = 1;
            try {
                if (serverExtra != null) {
                    requestNum = Integer.parseInt(serverExtra.get(CustomNativeAd.AD_REQUEST_NUM).toString());
                }
            } catch (Exception e) {
            }

            if (serverExtra.containsKey("suport_video")) {
                suportVideo_str = serverExtra.get("suport_video").toString();
                if ("1".equals(suportVideo_str)) {
                    suportVideo = true;
                }
            } else {
                suportVideo = false;
            }

            try {
                expressWidth = Integer.parseInt(localExtra.get(MintegralATConst.AUTO_RENDER_NATIVE_WIDTH).toString());
                expressHeight = Integer.parseInt(localExtra.get(MintegralATConst.AUTO_RENDER_NATIVE_HEIGHT).toString());
            } catch (Throwable e) {
            }

            if (expressWidth <= 0 && expressHeight <= 0) {
                try {
                    expressWidth = Integer.parseInt(localExtra.get(ATAdConst.KEY.AD_WIDTH).toString());
                    expressHeight = Integer.parseInt(localExtra.get(ATAdConst.KEY.AD_HEIGHT).toString());
                } catch (Throwable e) {
                    Log.e(TAG, "Mintegral AdvancedNative size is empty.");
                }

            }

            mUnitId = unitId;
            final boolean finalSuportVideo = suportVideo;
            final int finalRequestNum = requestNum;
            final String finalPlacementId = placementId;

            MintegralATInitManager.getInstance().initSDK(context, serverExtra, new MintegralATInitManager.InitCallback() {
                @Override
                public void onSuccess() {
                    startLoad(context, serverExtra, finalPlacementId, mUnitId, finalSuportVideo, finalRequestNum);
                }

                @Override
                public void onError(Throwable e) {
                    if (mLoadListener != null) {
                        mLoadListener.onAdLoadError("", e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", e.getMessage());
            }
        }
    }

    @Override
    public void destory() {

    }

    @Override
    public String getNetworkPlacementId() {
        return mUnitId;
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
