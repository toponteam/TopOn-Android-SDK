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
import android.widget.FrameLayout;

import com.anythink.nativead.unitgroup.api.CustomNativeAd;
import com.qq.e.ads.cfg.VideoOption;
import com.qq.e.ads.nativ.ADSize;
import com.qq.e.ads.nativ.NativeExpressAD;
import com.qq.e.ads.nativ.NativeExpressADView;
import com.qq.e.comm.compliance.DownloadConfirmCallBack;
import com.qq.e.comm.compliance.DownloadConfirmListener;

import java.util.List;

public class GDTATNativeExpressAd extends CustomNativeAd {
    NativeExpressAD mNativeExpressAD;
    NativeExpressADView mNativeExpressADView;

    GDTATNativeLoadListener mLoadListener;

    protected GDTATNativeExpressAd(Context context, String unitId, int localWidth, int localHeight,
                                   int videoMuted, int videoAutoPlay, int videoDuration) {

        NativeExpressAD.NativeExpressADListener nativeExpressADListener = new NativeExpressAD.NativeExpressADListener() {

            @Override
            public void onNoAD(com.qq.e.comm.util.AdError pAdError) {
                if (mLoadListener != null) {
                    mLoadListener.notifyError(pAdError.getErrorCode() + "", pAdError.getErrorMsg());
                }
                mLoadListener = null;
            }

            @Override
            public void onADLoaded(List<NativeExpressADView> pList) {
                if (pList.size() > 0) {
                    NativeExpressADView nativeExpressADView = pList.get(0);
                    nativeExpressADView.render();
                } else {
                    if (mLoadListener != null) {
                        mLoadListener.notifyError("", "GDT Ad request success but no Ad return.");
                    }
                    mLoadListener = null;
                }


            }

            @Override
            public void onRenderFail(NativeExpressADView pNativeExpressADView) {
                if (mLoadListener != null) {
                    mLoadListener.notifyError("", "GDT onRenderFail");
                }
                mLoadListener = null;
            }

            @Override
            public void onRenderSuccess(NativeExpressADView pNativeExpressADView) {
                mNativeExpressADView = pNativeExpressADView;
                if (mLoadListener != null) {
                    mLoadListener.notifyLoaded(GDTATNativeExpressAd.this);
                }
                mLoadListener = null;
            }

            @Override
            public void onADExposure(NativeExpressADView pNativeExpressADView) {
                notifyAdImpression();
            }

            @Override
            public void onADClicked(NativeExpressADView pNativeExpressADView) {
                notifyAdClicked();
            }

            @Override
            public void onADClosed(NativeExpressADView pNativeExpressADView) {
                notifyAdDislikeClick();
            }

            @Override
            public void onADLeftApplication(NativeExpressADView pNativeExpressADView) {
            }

            @Override
            public void onADOpenOverlay(NativeExpressADView pNativeExpressADView) {
            }

            @Override
            public void onADCloseOverlay(NativeExpressADView pNativeExpressADView) {
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

        mNativeExpressAD = new NativeExpressAD(context, new ADSize(width, height), unitId, nativeExpressADListener); // Context must be Activity

        VideoOption option = new VideoOption.Builder()
                .setAutoPlayMuted(videoMuted == 1)
                .setDetailPageMuted(videoMuted == 1)
                .setAutoPlayPolicy(videoAutoPlay)
                .build();

        mNativeExpressAD.setVideoOption(option);
        if (videoDuration != -1) {
            mNativeExpressAD.setMaxVideoDuration(videoDuration);
        }

        mNativeExpressAD.setVideoPlayPolicy(GDTATInitManager.getInstance().getVideoPlayPolicy(context, option.getAutoPlayPolicy()));

    }

    protected void loadAD(GDTATNativeLoadListener loadListener) {
        mLoadListener = loadListener;
        mNativeExpressAD.loadAD(1);
    }

    @Override
    public boolean isNativeExpress() {
        return true;
    }

    @Override
    public View getAdMediaView(Object... object) {
        return mNativeExpressADView;
    }

    @Override
    public void registerDownloadConfirmListener() {
        if(mNativeExpressADView != null){
            mNativeExpressADView.setDownloadConfirmListener(new DownloadConfirmListener() {
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
    public void prepare(View view, FrameLayout.LayoutParams layoutParams) {
        super.prepare(view, layoutParams);
//        if (mNativeExpressADView != null) {
//            mNativeExpressADView.render();
//        }
    }

    @Override
    public void prepare(View view, List<View> clickViewList, FrameLayout.LayoutParams layoutParams) {
        super.prepare(view, clickViewList, layoutParams);
//        if (mNativeExpressADView != null) {
//            mNativeExpressADView.render();
//        }
    }

    @Override
    public void destroy() {
        if (mNativeExpressADView != null) {
            mNativeExpressADView.destroy();
        }
        mNativeExpressADView = null;
        mLoadListener = null;
        mNativeExpressAD = null;
    }
}
