/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.gdt;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.anythink.nativead.unitgroup.api.CustomNativeAd;
import com.qq.e.ads.nativ.ADSize;
import com.qq.e.ads.nativ.express2.AdEventListener;
import com.qq.e.ads.nativ.express2.MediaEventListener;
import com.qq.e.ads.nativ.express2.NativeExpressAD2;
import com.qq.e.ads.nativ.express2.NativeExpressADData2;
import com.qq.e.ads.nativ.express2.VideoOption2;
import com.qq.e.comm.compliance.DownloadConfirmCallBack;
import com.qq.e.comm.compliance.DownloadConfirmListener;
import com.qq.e.comm.util.AdError;

import java.util.List;

public class GDTATNativeExpressAd2 extends CustomNativeAd {

    GDTATNativeLoadListener mLoadListener;
    private NativeExpressADData2 mNativeExpressADData2;

    private AdEventListener adEventListener;
    private MediaEventListener mediaEventListener;
    private NativeExpressAD2 mNativeExpressAD2;

    public GDTATNativeExpressAd2(Context context, final String unitId, int localWidth, int localHeight,
                                 int videoMuted, int videoAutoPlay, int videoDuration) {
        NativeExpressAD2.AdLoadListener adLoadListener = new NativeExpressAD2.AdLoadListener() {
            @Override
            public void onLoadSuccess(List<NativeExpressADData2> list) {
                if (list != null && list.size() > 0) {
                    renderAD(list.get(0));
                } else {
                    if (mLoadListener != null) {
                        mLoadListener.notifyError("", "GDT Ad request success but no Ad return.");
                    }
                    mLoadListener = null;
                }
            }

            @Override
            public void onNoAD(AdError adError) {
                if (mLoadListener != null) {
                    mLoadListener.notifyError(adError.getErrorCode() + "", adError.getErrorMsg());
                }
                mLoadListener = null;
            }
        };

        int width = ADSize.FULL_WIDTH;
        int height = ADSize.AUTO_HEIGHT;
        if (localWidth > 0) {
            width = GDTATInitManager.getInstance().px2dip(context, localWidth);
        }
        if (localHeight > 0) {
            height = GDTATInitManager.getInstance().px2dip(context, localHeight);
        }

        mNativeExpressAD2 = new NativeExpressAD2(context, unitId, adLoadListener);
        mNativeExpressAD2.setAdSize(width, height);

        VideoOption2.AutoPlayPolicy autoPlayPolicy;

        switch (videoAutoPlay) {
            case 1:
                autoPlayPolicy = VideoOption2.AutoPlayPolicy.ALWAYS;
                break;
            case 2:
                autoPlayPolicy = VideoOption2.AutoPlayPolicy.NEVER;
                break;
            default:
                autoPlayPolicy = VideoOption2.AutoPlayPolicy.WIFI;
                break;
        }

        VideoOption2.Builder builder = new VideoOption2.Builder()
                .setAutoPlayMuted(videoMuted == 1)
                .setDetailPageMuted(videoMuted == 1)
                .setAutoPlayPolicy(autoPlayPolicy);

        if (videoDuration != -1) {
            builder.setMaxVideoDuration(videoDuration);
        }

        VideoOption2 option = builder.build();
        mNativeExpressAD2.setVideoOption2(option);

    }


    private void renderAD(final NativeExpressADData2 nativeExpressADData2) {

        adEventListener = new AdEventListener() {
            @Override
            public void onAdClosed() {
                notifyAdDislikeClick();
            }

            @Override
            public void onClick() {
                notifyAdClicked();
            }

            @Override
            public void onExposed() {
                notifyAdImpression();
            }

            @Override
            public void onRenderFail() {
                if (mLoadListener != null) {
                    mLoadListener.notifyError("", "GDT onRenderFail");
                }
                mLoadListener = null;
            }

            @Override
            public void onRenderSuccess() {
                mNativeExpressADData2 = nativeExpressADData2;
                if (mLoadListener != null) {
                    mLoadListener.notifyLoaded(GDTATNativeExpressAd2.this);
                }
                mLoadListener = null;
            }
        };

        mediaEventListener = new MediaEventListener() {
            @Override
            public void onVideoCache() {

            }

            @Override
            public void onVideoComplete() {
                notifyAdVideoEnd();
            }

            @Override
            public void onVideoError() {

            }

            @Override
            public void onVideoPause() {

            }

            @Override
            public void onVideoResume() {

            }

            @Override
            public void onVideoStart() {
                notifyAdVideoStart();
            }
        };

        nativeExpressADData2.setAdEventListener(adEventListener);
        nativeExpressADData2.setMediaListener(mediaEventListener);

        nativeExpressADData2.render();
    }

    protected void loadAD(GDTATNativeLoadListener loadListener) {
        mLoadListener = loadListener;
        if (mNativeExpressAD2 != null) {
            mNativeExpressAD2.loadAd(1);
        }
    }

    @Override
    public void registerDownloadConfirmListener() {
        if (mNativeExpressADData2 != null) {
            mNativeExpressADData2.setDownloadConfirmListener(new DownloadConfirmListener() {
                @Override
                public void onDownloadConfirm(Activity activity, int i, String s, DownloadConfirmCallBack downloadConfirmCallBack) {
                    GDTDownloadFirmInfo gdtDownloadFirmInfo = new GDTDownloadFirmInfo();
                    gdtDownloadFirmInfo.appInfoUrl = s;
                    gdtDownloadFirmInfo.scenes = i;
                    gdtDownloadFirmInfo.confirmCallBack = downloadConfirmCallBack;
                    notifyDownloadConfirm(activity, null, gdtDownloadFirmInfo);
                }
            });
        }
    }


    @Override
    public boolean isNativeExpress() {
        return true;
    }

    @Override
    public View getAdMediaView(Object... object) {
        if (mNativeExpressADData2 != null) {
            return mNativeExpressADData2.getAdView();
        }
        return null;
    }

    @Override
    public void destroy() {
        if (mNativeExpressADData2 != null) {
            mNativeExpressADData2.setMediaListener(null);
            mNativeExpressADData2.setAdEventListener(null);
            mNativeExpressADData2 = null;
        }
        adEventListener = null;
        mediaEventListener = null;
        mNativeExpressAD2 = null;

        mLoadListener = null;
    }
}
