package com.anythink.network.luomi;

import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.AdError;
import com.anythink.core.api.ErrorCode;
import com.anythink.nativead.unitgroup.api.CustomNativeAd;
import com.anythink.nativead.unitgroup.api.CustomNativeAdapter;
import com.anythink.nativead.unitgroup.api.CustomNativeListener;
import com.hz.yl.b.HhInfo;
import com.hz.yl.b.mian.HmNative;
import com.hz.yl.b.mian.NativeListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LuomiATAdapter extends CustomNativeAdapter {

    String mSize = String.valueOf(36);

    @Override
    public void loadNativeAd(final Context context, final CustomNativeListener customNativeListener, Map<String, Object> serverExtras, Map<String, Object> localExtras) {
        try {
            String appKey = "";
            try {
                if (serverExtras.containsKey("app_key")) {
                    appKey = serverExtras.get("app_key").toString();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (serverExtras.containsKey("size")) {
                    String size = serverExtras.get("size").toString();
                    switch (size) {
                        case "800x1200":
                            mSize = "38";
                            break;
                        case "640x400":
                            mSize = "36";
                            break;
                        case "720x1280":
                            mSize = "7";
                            break;
                        case "150x150":
                            mSize = "34";
                            break;
                        case "214x140":
                            mSize = "4";
                            break;
                        case "640x100":
                            mSize = "30";
                            break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (TextUtils.isEmpty(appKey)) {
                if (customNativeListener != null) {
                    AdError adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", "luomi appkey is empty.");
                    customNativeListener.onNativeAdFailed(this, adError);
                }
                return;
            }

            LuomiATInitManager.getInstance().initSDK(context, serverExtras);
            int requestNum = 1;
            try {
                if (serverExtras != null) {
                    requestNum = Integer.parseInt(serverExtras.get(CustomNativeAd.AD_REQUEST_NUM).toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            HmNative hmNative = new HmNative(context, mSize, new NativeListener() {
                @Override
                public void LoadSuccess(List<HhInfo> list) {
                    List<CustomNativeAd> customNativeAds = new ArrayList<>();
                    for (HhInfo hhInfo : list) {
                        LuomiATNativeAd luomiNativeAd = new LuomiATNativeAd(context, customNativeListener, hhInfo);
                        customNativeAds.add(luomiNativeAd);
                    }
                    if (customNativeListener != null) {
                        customNativeListener.onNativeAdLoaded(LuomiATAdapter.this, customNativeAds);
                    }
                }

                @Override
                public void LoadError(String s) {
                    if (customNativeListener != null) {
                        customNativeListener.onNativeAdFailed(LuomiATAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, "", s));
                    }
                }
            }, requestNum);

        } catch (Exception e) {
            e.printStackTrace();
            if (customNativeListener != null) {
                AdError adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", e.getMessage());
                customNativeListener.onNativeAdFailed(this, adError);
            }
        }
    }

    @Override
    public String getSDKVersion() {
        return LuomiATConst.getSDKVersion();
    }

    @Override
    public void clean() {

    }

    @Override
    public String getNetworkName() {
        return LuomiATInitManager.getInstance().getNetworkName();
    }
}
