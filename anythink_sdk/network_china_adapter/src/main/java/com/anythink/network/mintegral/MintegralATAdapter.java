package com.anythink.network.mintegral;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.anythink.core.api.AdError;
import com.anythink.core.api.ErrorCode;
import com.anythink.nativead.unitgroup.api.CustomNativeAd;
import com.anythink.nativead.unitgroup.api.CustomNativeAdapter;
import com.anythink.nativead.unitgroup.api.CustomNativeListener;
import com.mintegral.msdk.MIntegralConstans;
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

    @Override
    public void loadNativeAd(final Context context, final CustomNativeListener customNativeListener, final Map<String, Object> serverExtras, Map<String, Object> localExtras) {
        try {
            String appid = "";
            String unitId = "";
            String sdkKey = "";
            String placementId = "";
            String suportVideo_str = "1";
            //支持视频
            boolean suportVideo = false;

            try {
                if (serverExtras.containsKey("appid")) {
                    appid = serverExtras.get("appid").toString();
                }
                if (serverExtras.containsKey("unitid")) {
                    unitId = serverExtras.get("unitid").toString();
                }

                if (serverExtras.containsKey("placement_id")) {
                    placementId = serverExtras.get("placement_id").toString();
                }
                if (serverExtras.containsKey("appkey")) {
                    sdkKey = serverExtras.get("appkey").toString();
                }

                if (serverExtras.containsKey("payload")) {
                    mPayload = serverExtras.get("payload").toString();
                }

                if (serverExtras.containsKey("tp_info")) {
                    mCustomData = serverExtras.get("tp_info").toString();
                }

                if (serverExtras.containsKey("unit_type")) {
                    mUnitType = serverExtras.get("unit_type").toString();
                }

                if (serverExtras.containsKey("video_muted")) {
                    videoMuted = serverExtras.get("video_muted").toString();
                }

                if (serverExtras.containsKey("video_autoplay")) {
                    videoAutoPlay = serverExtras.get("video_autoplay").toString();
                }

                if (serverExtras.containsKey("close_button")) {
                    closeButton = serverExtras.get("close_button").toString();
                }


            } catch (Exception e) {
                e.printStackTrace();
            }

            if (TextUtils.isEmpty(appid) || TextUtils.isEmpty(unitId) || TextUtils.isEmpty(sdkKey)) {
                if (customNativeListener != null) {
                    AdError adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", "mintegral appid ,unitid or sdkkey is empty.");
                    customNativeListener.onNativeAdFailed(this, adError);
                }
                return;
            }

            int requestNum = 1;
            try {
                if (serverExtras != null) {
                    requestNum = Integer.parseInt(serverExtras.get(CustomNativeAd.AD_REQUEST_NUM).toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (serverExtras.containsKey("suport_video")) {
                suportVideo_str = serverExtras.get("suport_video").toString();
                if ("1".equals(suportVideo_str)) {
                    suportVideo = true;
                }
            } else {
                suportVideo = false;
            }

            try{
                expressWidth = Integer.parseInt(localExtras.get(MintegralATConst.AUTO_RENDER_NATIVE_WIDTH).toString());
                expressHeight = Integer.parseInt(localExtras.get(MintegralATConst.AUTO_RENDER_NATIVE_HEIGHT).toString());
            }catch (Exception e){
                Log.e(TAG, "Mintegral AdvancedNative size is empty.");
            }

            final String finalUnitId = unitId;
            final boolean finalSuportVideo = suportVideo;
            final int finalRequestNum = requestNum;
            final String finalPlacementId = placementId;

            MintegralATInitManager.getInstance().initSDK(context, serverExtras, new MintegralATInitManager.InitCallback() {
                @Override
                public void onSuccess() {
                    startLoad(context, customNativeListener, serverExtras, finalPlacementId, finalUnitId, finalSuportVideo, finalRequestNum);
                }

                @Override
                public void onError(Throwable e) {
                    if (customNativeListener != null) {
                        AdError adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", e.getMessage());
                        customNativeListener.onNativeAdFailed(MintegralATAdapter.this, adError);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            if (customNativeListener != null) {
                AdError adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", e.getMessage());
                customNativeListener.onNativeAdFailed(MintegralATAdapter.this, adError);
            }
        }
    }

    private void startLoad(Context context, CustomNativeListener customNativeListener, Map<String, Object> serverExtras, String placementId, String unitId, boolean suportVideo, int requestNum) {

        boolean isAutoPlay = false;
        try {
            if (serverExtras != null) {
                isAutoPlay = Boolean.parseBoolean(serverExtras.get(CustomNativeAd.IS_AUTO_PLAY_KEY).toString());
            }
        } catch (Exception e) {

        }

        if (TextUtils.equals(mUnitType, "1")) {
            loadExpressAd(context, placementId, unitId, customNativeListener);
        } else {
            loadAd(context, requestNum, placementId, unitId, suportVideo, customNativeListener, isAutoPlay);
        }

    }

    private void loadExpressAd(final Context context, final String placementId, final String unitId, final CustomNativeListener customNativeListener) {
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
                if (customNativeListener != null) {
                    AdError adError = ErrorCode.getErrorCode(ErrorCode.noADError, s, s);
                    customNativeListener.onNativeAdFailed(MintegralATAdapter.this, adError);
                }
            }

            @Override
            public void onLoadSuccessed() {
                MintegralATExpressNativeAd mintegralATExpressNativeAd = new MintegralATExpressNativeAd(context, mtgNativeAdvancedHandler, false);
                if (customNativeListener != null) {
                    List<CustomNativeAd> customNativeAdList = new ArrayList<>();
                    customNativeAdList.add(mintegralATExpressNativeAd);
                    customNativeListener.onNativeAdLoaded(MintegralATAdapter.this, customNativeAdList);
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


    private void loadAd(final Context context, final int adnum, final String placementId, final String unitId, boolean supportVideo, final CustomNativeListener customNativeListener, final boolean isAutoPlay) {
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

        NativeListener.NativeAdListener listener = new NativeListener.NativeAdListener() {

            @Override
            public void onAdLoaded(List<Campaign> list, int i) {
                if (list == null || list.size() <= 0) {
                    if (customNativeListener != null) {
                        AdError adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", " no ad return ");
                        customNativeListener.onNativeAdFailed(MintegralATAdapter.this, adError);
                    }
                    return;
                }

                boolean hasReturn = false;
                List<CustomNativeAd> customNativeAds = new ArrayList<>();
                for (Campaign campaign : list) {
                    if (campaign != null) {
                        hasReturn = true;
                        boolean isHB = !TextUtils.isEmpty(mPayload);
                        MintegralATNativeAd mintegralNativeAd = new MintegralATNativeAd(context, placementId, unitId, campaign, isHB);
                        mintegralNativeAd.setIsAutoPlay(isAutoPlay);
                        customNativeAds.add(mintegralNativeAd);
                    }
                }

                if (!hasReturn) {
                    if (customNativeListener != null) {
                        AdError adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", " no ad return ");
                        customNativeListener.onNativeAdFailed(MintegralATAdapter.this, adError);

                    }
                    return;
                } else {
                    if (customNativeListener != null) {
                        customNativeListener.onNativeAdLoaded(MintegralATAdapter.this, customNativeAds);
                    }
                }

            }

            @Override
            public void onAdLoadError(String s) {
                if (customNativeListener != null) {

                    AdError adError = ErrorCode.getErrorCode(ErrorCode.noADError, s, s);
                    customNativeListener.onNativeAdFailed(MintegralATAdapter.this, adError);
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

        if (TextUtils.isEmpty(mPayload)) {
            try {
                CustomInfoManager.getInstance().setCustomInfo(unitId, CustomInfoManager.TYPE_LOAD, mCustomData);
            } catch (Throwable e) {
            }

            final MtgNativeHandler mvNativeHandler = new MtgNativeHandler(properties, context.getApplicationContext());
            mvNativeHandler.setAdListener(listener);
            mvNativeHandler.load();
        } else {
            try {
                CustomInfoManager.getInstance().setCustomInfo(unitId, CustomInfoManager.TYPE_BIDLOAD, mCustomData);
            } catch (Throwable e) {
            }

            final MtgBidNativeHandler mtgBidNativeHandler = new MtgBidNativeHandler(properties, context.getApplicationContext());
            mtgBidNativeHandler.setAdListener(listener);
            mtgBidNativeHandler.bidLoad(mPayload);
        }
    }


    @Override
    public String getSDKVersion() {
        return MintegralATConst.getNetworkVersion();
    }

    @Override
    public void clean() {

    }

    @Override
    public String getNetworkName() {
        return MintegralATInitManager.getInstance().getNetworkName();
    }
}
