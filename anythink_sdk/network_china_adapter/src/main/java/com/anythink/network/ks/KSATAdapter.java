package com.anythink.network.ks;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.anythink.nativead.unitgroup.api.CustomNativeAd;
import com.anythink.nativead.unitgroup.api.CustomNativeAdapter;
import com.kwad.sdk.api.KsAdSDK;
import com.kwad.sdk.api.KsDrawAd;
import com.kwad.sdk.api.KsFeedAd;
import com.kwad.sdk.api.KsLoadManager;
import com.kwad.sdk.api.KsNativeAd;
import com.kwad.sdk.api.KsScene;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KSATAdapter extends CustomNativeAdapter {

    long posId;

    @Override
    public String getNetworkName() {
        return KSATInitManager.getInstance().getNetworkName();
    }

    @Override
    public void loadCustomNetworkAd(final Context context, Map<String, Object> serverExtra, Map<String, Object> localExtra) {

        String appId = (String) serverExtra.get("app_id");
        String position_id = (String) serverExtra.get("position_id");

        if (TextUtils.isEmpty(appId) || TextUtils.isEmpty(position_id)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "kuaishou app_id or position_id is empty.");
            }
            return;
        }
        posId = Long.parseLong(position_id);


        String layout_type = "0";
        if (serverExtra.containsKey("layout_type")) {// 0：self-rendering  1：native express
            layout_type = (String) serverExtra.get("layout_type");
        }

        boolean isVideoSoundEnable = false;
        if (serverExtra.containsKey("video_sound")) {// 0：mute  1: non-silent
            isVideoSoundEnable = TextUtils.equals("1", (String) serverExtra.get("video_sound"));
        }

        String unit_type = "0";
        if (serverExtra.containsKey("unit_type")) {// 0：native or feed  1: draw
            unit_type = (String) serverExtra.get("unit_type");
        }

        int requestNum = 1;
        try {
            requestNum = Integer.parseInt(serverExtra.get(CustomNativeAd.AD_REQUEST_NUM).toString());
        } catch (Exception e) {
        }

        KSATInitManager.getInstance().initSDK(context, serverExtra);

        KsScene adScene = new KsScene.Builder(posId)
                .adNum(requestNum)
                .build();

        if (TextUtils.equals("1", unit_type)) {//draw
            KsAdSDK.getLoadManager().loadDrawAd(adScene, new KsLoadManager.DrawAdListener() {
                @Override
                public void onError(int i, String s) {
                    if (mLoadListener != null) {
                        mLoadListener.onAdLoadError(i + "", s);
                    }
                }

                @Override
                public void onDrawAdLoad(@Nullable List<KsDrawAd> list) {
                    if (list == null || list.size() == 0) {
                        if (mLoadListener != null) {
                            mLoadListener.onAdLoadError("", "kuaishou no fill");
                        }
                    } else {
                        final List<CustomNativeAd> resultList = new ArrayList<>();
                        for (final KsDrawAd ksDrawAd : list) {
                            KSATDrawAd ksatDrawAd = new KSATDrawAd(context, ksDrawAd);
                            resultList.add(ksatDrawAd);
                        }

                        CustomNativeAd[] customNativeAds = new CustomNativeAd[resultList.size()];
                        customNativeAds = resultList.toArray(customNativeAds);
                        if (mLoadListener != null) {
                            mLoadListener.onAdCacheLoaded(customNativeAds);
                        }
                    }
                }
            });
            return;
        }

        final boolean finalIsVideoSoundEnable = isVideoSoundEnable;
        if (TextUtils.equals("1", layout_type)) {//native express
            KsAdSDK.getLoadManager().loadFeedAd(adScene, new KsLoadManager.FeedAdListener() {
                @Override
                public void onError(int i, String s) {
                    if (mLoadListener != null) {
                        mLoadListener.onAdLoadError(i + "", s);
                    }
                }

                @Override
                public void onFeedAdLoad(@Nullable List<KsFeedAd> list) {
                    if (list == null || list.size() == 0) {
                        if (mLoadListener != null) {
                            mLoadListener.onAdLoadError("", "kuaishou no fill");
                        }
                    } else {
                        final List<CustomNativeAd> resultList = new ArrayList<>();
                        for (final KsFeedAd ksFeedAd : list) {
                            KSATFeedAd ksatFeedAd = new KSATFeedAd(context, ksFeedAd, finalIsVideoSoundEnable);
                            resultList.add(ksatFeedAd);
                        }

                        CustomNativeAd[] customNativeAds = new CustomNativeAd[resultList.size()];
                        customNativeAds = resultList.toArray(customNativeAds);
                        if (mLoadListener != null) {
                            mLoadListener.onAdCacheLoaded(customNativeAds);
                        }
                    }
                }
            });
        } else {//self-rendering
            KsAdSDK.getLoadManager().loadNativeAd(adScene, new KsLoadManager.NativeAdListener() {
                @Override
                public void onError(int i, String s) {
                    if (mLoadListener != null) {
                        mLoadListener.onAdLoadError(i + "", s);
                    }
                }

                @Override
                public void onNativeAdLoad(@Nullable List<KsNativeAd> list) {
                    if (list == null || list.size() == 0) {
                        if (mLoadListener != null) {
                            mLoadListener.onAdLoadError("", "kuaishou no fill");
                        }
                    } else {
                        final List<CustomNativeAd> resultList = new ArrayList<>();
                        for (final KsNativeAd ksNativeAd : list) {
                            KSATNativeAd ksatNativeAd = new KSATNativeAd(context, ksNativeAd, finalIsVideoSoundEnable);
                            resultList.add(ksatNativeAd);
                        }

                        CustomNativeAd[] customNativeAds = new CustomNativeAd[resultList.size()];
                        customNativeAds = resultList.toArray(customNativeAds);
                        if (mLoadListener != null) {
                            mLoadListener.onAdCacheLoaded(customNativeAds);
                        }
                    }
                }
            });
        }
    }

    @Override
    public void destory() {

    }

    @Override
    public String getNetworkPlacementId() {
        try {
            return String.valueOf(posId);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    @Override
    public String getNetworkSDKVersion() {
        return KSATConst.getSDKVersion();
    }
}
