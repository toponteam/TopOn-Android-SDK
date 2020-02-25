package com.anythink.network.toutiao;

import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdSdk;

public class TTATConst {
    public static final String NATIVE_AD_IMAGE_WIDTH = "tt_image_width";
    public static final String NATIVE_AD_IMAGE_HEIGHT = "tt_image_height";
    public static final String NATIVE_AD_INTERRUPT_VIDEOPLAY = "tt_can_interrupt_video";
    public static final String NATIVE_AD_VIDEOPLAY_BTN_BITMAP = "tt_video_play_btn_bitmap";
    public static final String NATIVE_AD_VIDEOPLAY_BTN_SIZE = "tt_video_play_btn_SIZE";

    public static boolean hasRequestPermission = false;

    public static final int NETWORK_FIRM_ID = 15;

    public static String getNetworkVersion() {
        try {
            TTAdManager ttAdManager = TTAdSdk.getAdManager();
            return ttAdManager.getSDKVersion();
        } catch (Throwable e) {

        }
        return "";
    }
}
