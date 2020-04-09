package com.anythink.network.toutiao;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

import com.anythink.core.api.ErrorCode;
import com.anythink.nativead.unitgroup.api.CustomNativeAd;
import com.anythink.nativead.unitgroup.api.CustomNativeAdapter;
import com.anythink.nativead.unitgroup.api.CustomNativeListener;
import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTDrawFeedAd;
import com.bytedance.sdk.openadsdk.TTFeedAd;
import com.bytedance.sdk.openadsdk.TTNativeAd;
import com.bytedance.sdk.openadsdk.TTNativeExpressAd;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TTATAdapter extends CustomNativeAdapter {
    private final String TAG = getClass().getSimpleName();

    String slotId;

    /**
     * //Is Native Feeds? "0"：yes， "1"：no
     */
    String layoutType;

    CustomNativeListener mListener;

    String nativeType;

    @Override
    public void loadNativeAd(final Context context, CustomNativeListener customNativeListener, Map<String, Object> serverExtras, final Map<String, Object> localExtras) {

        mListener = customNativeListener;
        if (serverExtras == null) {
            if (customNativeListener != null) {
                customNativeListener.onNativeAdFailed(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "This placement's params in server is null!"));
            }
            return;
        }

        String appId = (String) serverExtras.get("app_id");
        slotId = (String) serverExtras.get("slot_id");

        if (TextUtils.isEmpty(appId) || TextUtils.isEmpty(slotId)) {
            if (customNativeListener != null) {
                customNativeListener.onNativeAdFailed(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "toutiao app_id or slot_id is empty!"));
            }
            return;
        }

        layoutType = "1";

        if(serverExtras.containsKey("layout_type")) {
            layoutType = (String) serverExtras.get("layout_type");
        }


        int requestNum = 1;
        try {
            if (serverExtras != null) {
                requestNum = Integer.parseInt(serverExtras.get(CustomNativeAd.AD_REQUEST_NUM).toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (serverExtras.containsKey("is_video")) {
            nativeType = serverExtras.get("is_video").toString();
        }

        int mediaSize = 0;
        try {
            if (serverExtras.containsKey("media_size")) {
                mediaSize = Integer.parseInt(serverExtras.get("media_size").toString());
            }
        } catch (Exception e) {

        }

        final int finalRequestNum = requestNum;
        final int finalMediaSize = mediaSize;
        TTATInitManager.getInstance().initSDK(context, serverExtras, new TTATInitManager.InitCallback() {
            @Override
            public void onFinish() {
                startLoad(context, localExtras, finalRequestNum, finalMediaSize);
            }
        });
    }

    private void startLoad(final Context context, Map<String, Object> localExtras, int requestNum, int mediaSize) {
        TTAdManager ttAdManager = TTAdSdk.getAdManager();

        int width = context.getResources().getDisplayMetrics().widthPixels;
        int height = context.getResources().getDisplayMetrics().heightPixels;
        boolean canInterrupt = false;
        Bitmap videoPlayBitmap = null;
        int videoPlaySize = 0;
        if (localExtras != null) {
            Object widthObject = localExtras.get(TTATConst.NATIVE_AD_IMAGE_WIDTH);
            Object heightObject = localExtras.get(TTATConst.NATIVE_AD_IMAGE_HEIGHT);
            Object canInterruptObject = localExtras.get(TTATConst.NATIVE_AD_INTERRUPT_VIDEOPLAY);
            Object videoPlayBitmapObject = localExtras.get(TTATConst.NATIVE_AD_VIDEOPLAY_BTN_BITMAP);
            Object videoPlaySizeObject = localExtras.get(TTATConst.NATIVE_AD_VIDEOPLAY_BTN_SIZE);

            if (mediaSize == 1) { //690*388
                width = 690;
                height = 388;
            } else if (mediaSize == 2) { //228*150
                width = 228;
                height = 150;
            } else {
                try {
                    if (widthObject instanceof Integer || widthObject instanceof String) {
                        width = Integer.parseInt(widthObject.toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    if (heightObject instanceof Integer || heightObject instanceof String) {
                        height = Integer.parseInt(heightObject.toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


            if (canInterruptObject instanceof Boolean) {
                canInterrupt = Boolean.parseBoolean(canInterruptObject.toString());
            }

            if (videoPlayBitmapObject instanceof Bitmap) {
                videoPlayBitmap = (Bitmap) videoPlayBitmapObject;
            }

            if (videoPlaySizeObject instanceof Integer) {
                videoPlaySize = Integer.parseInt(videoPlaySizeObject.toString());
            }
        }

        final boolean canInterruptFinal = canInterrupt;
        final Bitmap videoPlayBitmapFinal = videoPlayBitmap;
        final int videoPlaySizeFinal = videoPlaySize;


        TTAdNative mTTAdNative = ttAdManager.createAdNative(context);//baseContext建议为activity
        AdSlot.Builder adSlotBuilder = new AdSlot.Builder().setCodeId(slotId);

        if(width > 0 && height > 0) {
            adSlotBuilder.setImageAcceptedSize(width, height); //必须设置
        } else {
            adSlotBuilder.setImageAcceptedSize(640, 320); //必须设置
        }
        adSlotBuilder.setAdCount(requestNum);
        adSlotBuilder.setSupportDeepLink(true);


        //Native Express
        if(TextUtils.equals("0", nativeType) && TextUtils.equals("0", layoutType)) {
            Log.i(TAG, "load Native Express Ad");
            // set size, unit: dp
            adSlotBuilder.setExpressViewAcceptedSize(px2dip(context, width), px2dip(context, height)); //Must be set
            mTTAdNative.loadNativeExpressAd(adSlotBuilder.build(), new TTAdNative.NativeExpressAdListener() {
                @Override
                public void onError(int i, String s) {
                    if (mListener != null) {
                        mListener.onNativeAdFailed(TTATAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, String.valueOf(i), s));
                    }
                }

                @Override
                public void onNativeExpressAdLoad(List<TTNativeExpressAd> list) {
                    final List<CustomNativeAd> customNativeAds = new ArrayList<>();
                    for (final TTNativeExpressAd ttNativeExpressAd : list) {
                        TTATNativeExpressAd ttNativeAd = new TTATNativeExpressAd(context, slotId, ttNativeExpressAd, canInterruptFinal, false);
                        customNativeAds.add(ttNativeAd);
                    }

                    if (mListener != null) {
                        mListener.onNativeAdLoaded(TTATAdapter.this, customNativeAds);
                    }
                }
            });
            return;
        }

        //Native Express Video
        if(TextUtils.equals("1", nativeType)) {
            Log.i(TAG, "load Native Express Video");
            // set size, unity: dp
            adSlotBuilder.setExpressViewAcceptedSize(px2dip(context, width), px2dip(context, height)); //Must be set
            mTTAdNative.loadExpressDrawFeedAd(adSlotBuilder.build(), new TTAdNative.NativeExpressAdListener() {
                @Override
                public void onError(int i, String s) {
                    if (mListener != null) {
                        mListener.onNativeAdFailed(TTATAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, String.valueOf(i), s));
                    }
                }

                @Override
                public void onNativeExpressAdLoad(List<TTNativeExpressAd> list) {
                    List<CustomNativeAd> customNativeAds = new ArrayList<>();
                    for (TTNativeExpressAd ttNativeExpressAd : list) {
                        TTATNativeExpressAd ttNativeAd = new TTATNativeExpressAd(context, slotId, ttNativeExpressAd, canInterruptFinal, true);
                        customNativeAds.add(ttNativeAd);
                    }

                    if (mListener != null) {
                        mListener.onNativeAdLoaded(TTATAdapter.this, customNativeAds);
                    }
                }
            });
            return;
        }

        //  Custom rendering-------------------------------------------------------------------------------------------------------------------------------------
        /**Load different ads based on Native type**/
        switch (nativeType) {
            case "0": //Information Flow
                mTTAdNative.loadFeedAd(adSlotBuilder.build(), new TTAdNative.FeedAdListener() {
                    @Override
                    public void onError(int i, String s) {
                        if (mListener != null) {
                            mListener.onNativeAdFailed(TTATAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, String.valueOf(i), s));
                        }

                    }

                    @Override
                    public void onFeedAdLoad(List<TTFeedAd> list) {
                        List<CustomNativeAd> customNativeAds = new ArrayList<>();
                        for (TTFeedAd ttFeedAd : list) {
                            TTATNativeAd ttNativeAd = new TTATNativeAd(context, slotId, ttFeedAd, canInterruptFinal, videoPlayBitmapFinal, videoPlaySizeFinal);
                            customNativeAds.add(ttNativeAd);
                        }

                        if (mListener != null) {
                            mListener.onNativeAdLoaded(TTATAdapter.this, customNativeAds);
                        }
                    }
                });
                break;
            case "1": //Video stream
                mTTAdNative.loadDrawFeedAd(adSlotBuilder.build(), new TTAdNative.DrawFeedAdListener() {
                    @Override
                    public void onError(int i, String s) {
                        if (mListener != null) {
                            mListener.onNativeAdFailed(TTATAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, String.valueOf(i), s));
                        }

                    }

                    @Override
                    public void onDrawFeedAdLoad(List<TTDrawFeedAd> list) {
                        List<CustomNativeAd> customNativeAds = new ArrayList<>();
                        for (TTFeedAd ttFeedAd : list) {
                            TTATNativeAd ttNativeAd = new TTATNativeAd(context, slotId, ttFeedAd, canInterruptFinal, videoPlayBitmapFinal, videoPlaySizeFinal);
                            customNativeAds.add(ttNativeAd);
                        }
                        if (mListener != null) {
                            mListener.onNativeAdLoaded(TTATAdapter.this, customNativeAds);
                        }
                    }
                });
                break;

            case "2": //Native Banner
                adSlotBuilder.setNativeAdType(AdSlot.TYPE_BANNER);
                mTTAdNative.loadNativeAd(adSlotBuilder.build(), new TTAdNative.NativeAdListener() {
                    @Override
                    public void onError(int i, String s) {
                        if (mListener != null) {
                            mListener.onNativeAdFailed(TTATAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, String.valueOf(i), s));
                        }
                    }

                    @Override
                    public void onNativeAdLoad(List<TTNativeAd> list) {
                        List<CustomNativeAd> customNativeAds = new ArrayList<>();
                        for (TTNativeAd ttFeedAd : list) {
                            TTATNativeAd ttNativeAd = new TTATNativeAd(context, slotId, ttFeedAd, canInterruptFinal, videoPlayBitmapFinal, videoPlaySizeFinal);
                            customNativeAds.add(ttNativeAd);
                        }

                        if (mListener != null) {
                            mListener.onNativeAdLoaded(TTATAdapter.this, customNativeAds);
                        }
                    }
                });
                break;
            case "3": //Native Interstitial
                adSlotBuilder.setNativeAdType(AdSlot.TYPE_INTERACTION_AD);
                mTTAdNative.loadNativeAd(adSlotBuilder.build(), new TTAdNative.NativeAdListener() {
                    @Override
                    public void onError(int i, String s) {
                        if (mListener != null) {
                            mListener.onNativeAdFailed(TTATAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, String.valueOf(i), s));
                        }
                    }

                    @Override
                    public void onNativeAdLoad(List<TTNativeAd> list) {
                        List<CustomNativeAd> customNativeAds = new ArrayList<>();
                        for (TTNativeAd ttFeedAd : list) {
                            TTATNativeAd ttNativeAd = new TTATNativeAd(context, slotId, ttFeedAd, canInterruptFinal, videoPlayBitmapFinal, videoPlaySizeFinal);
                            customNativeAds.add(ttNativeAd);
                        }

                        if (mListener != null) {
                            mListener.onNativeAdLoaded(TTATAdapter.this, customNativeAds);
                        }
                    }
                });
                break;
            default:
                if (mListener != null) {
                    mListener.onNativeAdFailed(TTATAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "The Native type is not exit."));
                }
                break;
        }
    }

    private static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / (scale <= 0 ? 1 : scale) + 0.5f);
    }

    @Override
    public String getSDKVersion() {
        return TTATConst.getNetworkVersion();
    }

    @Override
    public void clean() {

    }

    @Override
    public String getNetworkName() {
        return TTATInitManager.getInstance().getNetworkName();
    }
}
