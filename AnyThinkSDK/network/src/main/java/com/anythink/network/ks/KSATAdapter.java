package com.anythink.network.ks;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.anythink.core.api.ErrorCode;
import com.anythink.nativead.unitgroup.api.CustomNativeAd;
import com.anythink.nativead.unitgroup.api.CustomNativeAdapter;
import com.anythink.nativead.unitgroup.api.CustomNativeListener;
import com.kwad.sdk.KsAdSDK;
import com.kwad.sdk.export.i.IAdRequestManager;
import com.kwad.sdk.export.i.KsFeedAd;
import com.kwad.sdk.export.i.KsNativeAd;
import com.kwad.sdk.protocol.model.AdScene;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KSATAdapter extends CustomNativeAdapter {

    long posId;
    CustomNativeListener mLoadResultListener;

    @Override
    public void loadNativeAd(final Context context, CustomNativeListener customNativeListener, Map<String, Object> serverExtras, Map<String, Object> localExtras) {

        mLoadResultListener = customNativeListener;

        String appId = (String) serverExtras.get("app_id");
        String appName = (String) serverExtras.get("app_name");
        String position_id = (String) serverExtras.get("position_id");

        if (TextUtils.isEmpty(appId) || TextUtils.isEmpty(appName) || TextUtils.isEmpty(position_id)) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onNativeAdFailed(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "kuaishou app_id、 app_name or position_id is empty."));
            }
            return;
        }
        posId = Long.parseLong(position_id);


        String layout_type = "0";
        if (serverExtras != null && serverExtras.containsKey("layout_type")) {// 0：self-rendering  1：native express
            layout_type = (String) serverExtras.get("layout_type");
        }

        boolean isVideoSoundEnable = false;
        if (serverExtras != null && serverExtras.containsKey("video_sound")) {// 0：mute  1: non-silent
            isVideoSoundEnable = TextUtils.equals("1", (String) serverExtras.get("video_sound"));
        }

        int requestNum = 1;
        try {
            if (serverExtras != null) {
                requestNum = Integer.parseInt(serverExtras.get(CustomNativeAd.AD_REQUEST_NUM).toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        KSATInitManager.getInstance().initSDK(context, serverExtras);

        AdScene adScene = new AdScene(posId);
        adScene.adNum = requestNum;

        final boolean finalIsVideoSoundEnable = isVideoSoundEnable;
        if(TextUtils.equals("1", layout_type)) {//native express
            KsAdSDK.getAdManager().loadFeedAd(adScene, new IAdRequestManager.FeedAdListener() {
                @Override
                public void onError(int i, String s) {
                    if(mLoadResultListener != null) {
                        mLoadResultListener.onNativeAdFailed(KSATAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, i + "", s));
                    }
                }

                @Override
                public void onFeedAdLoad(@Nullable List<KsFeedAd> list) {
                    if(list == null || list.size() == 0) {
                        if (mLoadResultListener != null) {
                            mLoadResultListener.onNativeAdFailed(KSATAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "kuaishou no fill"));
                        }
                    } else {
                        final List<CustomNativeAd> customNativeAds = new ArrayList<>();
                        for (final KsFeedAd ksFeedAd : list) {
                            KSATFeedAd ksatFeedAd = new KSATFeedAd(context, ksFeedAd, finalIsVideoSoundEnable);
                            customNativeAds.add(ksatFeedAd);
                        }

                        if (mLoadResultListener != null) {
                            mLoadResultListener.onNativeAdLoaded(KSATAdapter.this, customNativeAds);
                        }
                    }
                }
            });
        } else {//self-rendering
            KsAdSDK.getAdManager().loadNativeAd(adScene, new IAdRequestManager.NativeAdListener() {
                @Override
                public void onError(int i, String s) {
                    if(mLoadResultListener != null) {
                        mLoadResultListener.onNativeAdFailed(KSATAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, i + "", s));
                    }
                }

                @Override
                public void onNativeAdLoad(@Nullable List<KsNativeAd> list) {
                    if(list == null || list.size() == 0) {
                        if (mLoadResultListener != null) {
                            mLoadResultListener.onNativeAdFailed(KSATAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "kuaishou no fill"));
                        }
                    } else {
                        final List<CustomNativeAd> customNativeAds = new ArrayList<>();
                        for (final KsNativeAd ksNativeAd : list) {
                            KSATNativeAd ksatNativeAd = new KSATNativeAd(context, ksNativeAd, finalIsVideoSoundEnable);
                            customNativeAds.add(ksatNativeAd);
                        }

                        if (mLoadResultListener != null) {
                            mLoadResultListener.onNativeAdLoaded(KSATAdapter.this, customNativeAds);
                        }
                    }
                }
            });
        }
    }

    @Override
    public String getSDKVersion() {
        return KSATConst.getSDKVersion();
    }

    @Override
    public void clean() {
        mLoadResultListener = null;
    }

    @Override
    public String getNetworkName() {
        return KSATInitManager.getInstance().getNetworkName();
    }
}
