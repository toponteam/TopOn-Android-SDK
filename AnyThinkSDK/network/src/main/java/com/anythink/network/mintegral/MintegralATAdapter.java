package com.anythink.network.mintegral;

import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.AdError;
import com.anythink.core.api.ErrorCode;
import com.anythink.nativead.unitgroup.api.CustomNativeAd;
import com.anythink.nativead.unitgroup.api.CustomNativeAdapter;
import com.anythink.nativead.unitgroup.api.CustomNativeListener;
import com.mintegral.msdk.MIntegralConstans;
import com.mintegral.msdk.out.Campaign;
import com.mintegral.msdk.out.Frame;
import com.mintegral.msdk.out.MtgBidNativeHandler;
import com.mintegral.msdk.out.MtgNativeHandler;
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

    @Override
    public void loadNativeAd(final Context context, final CustomNativeListener customNativeListener, final Map<String, Object> serverExtras, Map<String, Object> localExtras) {
        try {
            String appid = "";
            String unitId = "";
            String sdkKey = "";
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
                if (serverExtras.containsKey("appkey")) {
                    sdkKey = serverExtras.get("appkey").toString();
                }

                if (serverExtras.containsKey("payload")) {
                    mPayload = serverExtras.get("payload").toString();
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

            final String finalUnitId = unitId;
            final boolean finalSuportVideo = suportVideo;
            final int finalRequestNum = requestNum;
            MintegralATInitManager.getInstance().initSDK(context, serverExtras, new MintegralATInitManager.InitCallback() {
                @Override
                public void onSuccess() {
                    startLoad(context, customNativeListener, serverExtras, finalUnitId, finalSuportVideo, finalRequestNum);
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

    private void startLoad(Context context, CustomNativeListener customNativeListener, Map<String, Object> serverExtras, String unitId, boolean suportVideo, int requestNum) {

        boolean isAutoPlay = false;
        try {
            if (serverExtras != null) {
                isAutoPlay = Boolean.parseBoolean(serverExtras.get(CustomNativeAd.IS_AUTO_PLAY_KEY).toString());
            }
        } catch (Exception e) {

        }

        loadAd(context, requestNum, unitId, suportVideo, customNativeListener, isAutoPlay);
    }

    private void loadAd(final Context context, final int adnum, final String unitId, boolean supportVideo, final CustomNativeListener customNativeListener, final boolean isAutoPlay) {
        Map<String, Object> properties = MtgNativeHandler
                .getNativeProperties(unitId);

        properties.put(MIntegralConstans.PROPERTIES_AD_NUM, adnum);
        properties.put(MIntegralConstans.PROPERTIES_LAYOUT_TYPE,
                MIntegralConstans.LAYOUT_NATIVE);
        //MV 广告位 ID 必传
        properties.put(MIntegralConstans.PROPERTIES_UNIT_ID, unitId);

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
                        MintegralATNativeAd mintegralNativeAd = new MintegralATNativeAd(context, unitId, campaign, isHB);
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
            final MtgNativeHandler mvNativeHandler = new MtgNativeHandler(properties, context.getApplicationContext());
            mvNativeHandler.setAdListener(listener);
            mvNativeHandler.load();
        } else {
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
