package com.anythink.network.toutiao;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.anythink.banner.api.ATBannerView;
import com.anythink.banner.unitgroup.api.CustomBannerAdapter;
import com.anythink.banner.unitgroup.api.CustomBannerListener;
import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.AdError;
import com.anythink.core.api.ErrorCode;
import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdDislike;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTBannerAd;
import com.bytedance.sdk.openadsdk.TTNativeExpressAd;

import java.util.List;
import java.util.Map;

public class TTATBannerAdapter extends CustomBannerAdapter {
    private final String TAG = getClass().getSimpleName();

    String slotId = "";
    private TTBannerAd mttBannerAd;
    private TTNativeExpressAd mTTNativeExpressAd;
    Context mActivity;
    CustomBannerListener mListener;
    View mBannerView;
    int mBannerWidth;
    int mBannerHeight;
    int mRefreshTime;

    //TT Ad load listener
    TTAdNative.BannerAdListener ttBannerAdListener = new TTAdNative.BannerAdListener() {
        @Override
        public void onError(int i, String s) {
            if (mListener != null) {
                mListener.onBannerAdLoadFail(TTATBannerAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, i + "", s));
            }
        }

        @Override
        public void onBannerAdLoad(TTBannerAd ttBannerAd) {
            if (ttBannerAd == null) {
                if (mListener != null) {
                    mListener.onBannerAdLoadFail(TTATBannerAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "TTAD is null!"));
                }
                return;
            }
            View bannerView = ttBannerAd.getBannerView();
            if (bannerView == null) {
                if (mListener != null) {
                    mListener.onBannerAdLoadFail(TTATBannerAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "TTBannerView is null!"));
                }
                return;
            }

            mBannerView = bannerView;
            mBannerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    try {
                        if (mBannerView != null && mBannerView.getParent() != null) {
                            int width = ((ViewGroup) mBannerView.getParent()).getMeasuredWidth();
                            int height = ((ViewGroup) mBannerView.getParent()).getMeasuredHeight();

                            if (mBannerView.getLayoutParams().width != width) {
                                mBannerView.getLayoutParams().width = width;
                                mBannerView.getLayoutParams().height = width * mBannerHeight / mBannerWidth;
                                if (mBannerView.getLayoutParams().height > height) {
                                    mBannerView.getLayoutParams().height = height;
                                    mBannerView.getLayoutParams().width = height * mBannerWidth / mBannerHeight;
                                }
                                ((ViewGroup) mBannerView.getParent()).requestLayout();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    return true;
                }
            });

            ttBannerAd.setBannerInteractionListener(interactionListener);

            if (mListener != null) {
                mListener.onBannerAdLoaded(TTATBannerAdapter.this);
            }
        }
    };

    //TT Advertising event monitoring
    TTBannerAd.AdInteractionListener interactionListener = new TTBannerAd.AdInteractionListener() {

        @Override
        public void onAdClicked(View view, int i) {
            if (mListener != null) {
                mListener.onBannerAdClicked(TTATBannerAdapter.this);
            }
        }

        @Override
        public void onAdShow(View view, int i) {
            if (mListener != null) {
                mListener.onBannerAdShow(TTATBannerAdapter.this);
            }
        }
    };


    //Native Express
    TTAdNative.NativeExpressAdListener expressAdListener = new TTAdNative.NativeExpressAdListener() {
        @Override
        public void onError(int i, String s) {
            if (mListener != null) {
                mListener.onBannerAdLoadFail(TTATBannerAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, i + "", s));
            }
        }

        @Override
        public void onNativeExpressAdLoad(List<TTNativeExpressAd> list) {
            if (list != null && list.size() > 0) {
                mTTNativeExpressAd = list.get(0);
                if (mRefreshTime > 0) {
                    mTTNativeExpressAd.setSlideIntervalTime(mRefreshTime);
                } else {
                    mTTNativeExpressAd.setSlideIntervalTime(0);
                }
                mTTNativeExpressAd.setExpressInteractionListener(expressAdInteractionListener);
                mTTNativeExpressAd.render();

                if (mActivity instanceof Activity) {
                    bindDislike((Activity) mActivity, mTTNativeExpressAd, false);
                }


            } else {
                if (mListener != null) {
                    mListener.onBannerAdLoadFail(TTATBannerAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "Return Ad list is empty."));
                }
            }
        }
    };


    TTNativeExpressAd.ExpressAdInteractionListener expressAdInteractionListener = new TTNativeExpressAd.ExpressAdInteractionListener() {
        @Override
        public void onAdClicked(View view, int type) {
            if (mListener != null) {
                mListener.onBannerAdClicked(TTATBannerAdapter.this);
            }
        }

        @Override
        public void onAdShow(View view, int type) {
            if (mListener != null) {
                mListener.onBannerAdShow(TTATBannerAdapter.this);
            }
        }

        @Override
        public void onRenderFail(View view, String msg, int code) {
            if (mListener != null) {
                mListener.onBannerAdLoadFail(TTATBannerAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, code + "", msg));
            }
        }

        @Override
        public void onRenderSuccess(View view, float width, float height) {
            mBannerView = view;
            mBannerView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(View v) {

                }

                @Override
                public void onViewDetachedFromWindow(View v) {
                    if (mTTNativeExpressAd != null) {
                        mTTNativeExpressAd.destroy();
                    }
                }
            });
            if (mListener != null) {
                mListener.onBannerAdLoaded(TTATBannerAdapter.this);
            }
        }
    };


    @Override
    public void loadBannerAd(final ATBannerView anythinkBannerView, final Context activity, final Map<String, Object> serverExtras, ATMediationSetting mediationSetting, final CustomBannerListener customBannerListener) {
        mActivity = activity;

        mListener = customBannerListener;

        if (serverExtras == null) {
            if (mListener != null) {
                mListener.onBannerAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "This placement's params in server is null!"));
            }
            return;
        }

        String appId = (String) serverExtras.get("app_id");
        slotId = (String) serverExtras.get("slot_id");

        if (TextUtils.isEmpty(appId) || TextUtils.isEmpty(slotId)) {
            if (mListener != null) {
                mListener.onBannerAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "app_id or slot_id is empty!"));
            }
            return;
        }

        if (!(activity instanceof Activity)) {
            if (mListener != null) {
                AdError adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", "Context must be activity.");
                mListener.onBannerAdLoadFail(this, adError);
            }
            return;
        }

        mRefreshTime = 0;
        try {
            if (serverExtras.containsKey("nw_rft")) {
                mRefreshTime = Integer.valueOf((String) serverExtras.get("nw_rft"));
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        TTATInitManager.getInstance().initSDK(activity, serverExtras, new TTATInitManager.InitCallback() {
            @Override
            public void onFinish() {
                startLoadBanner(anythinkBannerView, activity, serverExtras);
            }
        });
    }

    private void startLoadBanner(ATBannerView anythinkBannerView, Context activity, Map<String, Object> serverExtras) {
        TTAdManager ttAdManager = TTAdSdk.getAdManager();

        String size = "";
        if (serverExtras.containsKey("size")) {
            size = serverExtras.get("size").toString();
        }

        int layoutType = 0;
        if (serverExtras.containsKey("layout_type")) {
            layoutType = Integer.parseInt(serverExtras.get("layout_type").toString());
        }

        int mediaSize = 0;
        if (serverExtras.containsKey("media_size")) {
            mediaSize = Integer.parseInt(serverExtras.get("media_size").toString());
        }

        int bannerWidth = 0;
        int bannerHeight = 0;

        //Layout Type
        if (layoutType == 1) {
            switch (mediaSize) {
                case 0:
                    bannerWidth = 600;
                    bannerHeight = 90;
                    break;
                case 1:
                    bannerWidth = 600;
                    bannerHeight = 100;
                    break;
                case 2:
                    bannerWidth = 600;
                    bannerHeight = 150;
                    break;
                case 3:
                    bannerWidth = 600;
                    bannerHeight = 250;
                    break;
                case 4:
                    bannerWidth = 600;
                    bannerHeight = 286;
                    break;
                case 5:
                    bannerWidth = 600;
                    bannerHeight = 200;
                    break;
                case 6:
                    bannerWidth = 600;
                    bannerHeight = 388;
                    break;
                case 7:
                    bannerWidth = 600;
                    bannerHeight = 400;
                    break;
                case 8:
                    bannerWidth = 600;
                    bannerHeight = 500;
                    break;

            }
        } else {
            try {
                if (!TextUtils.isEmpty(size)) {
                    String[] bannerSizes = size.split("x");
                    bannerWidth = Integer.parseInt(bannerSizes[0]); //dip2px(mActivity, Integer.parseInt(bannerSizes[0])) * 3;
                    bannerHeight = Integer.parseInt(bannerSizes[1]); //dip2px(mActivity, Integer.parseInt(bannerSizes[1])) * 3;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        if (bannerWidth == 0 || bannerHeight == 0) {
            bannerWidth = 640;
            bannerHeight = 100;
        }

        mBannerWidth = bannerWidth;
        mBannerHeight = bannerHeight;

        //If BannerView has been configured for width, then use it directly in the template
        int viewWidth = anythinkBannerView.getLayoutParams() != null ? (int) (anythinkBannerView.getLayoutParams().width / activity.getResources().getDisplayMetrics().density) : 0;
        int viewHeight = anythinkBannerView.getLayoutParams() != null ? (int) (anythinkBannerView.getLayoutParams().height / activity.getResources().getDisplayMetrics().density) : 0;

        TTAdNative mTTAdNative = ttAdManager.createAdNative(activity);//baseContext is recommended for Activity
        AdSlot.Builder adSlotBuilder = new AdSlot.Builder().setCodeId(slotId);
        adSlotBuilder.setImageAcceptedSize(bannerWidth, bannerHeight); //must be set
        adSlotBuilder.setAdCount(1);


        if (layoutType == 1) {
            adSlotBuilder.setExpressViewAcceptedSize(viewWidth <= 0 ? bannerWidth / 2 : viewWidth, viewHeight <= 0 ? 0 : viewHeight);
            AdSlot adSlot = adSlotBuilder.build();
            mTTAdNative.loadBannerExpressAd(adSlot, expressAdListener);
        } else {
            AdSlot adSlot = adSlotBuilder.build();
            mTTAdNative.loadBannerAd(adSlot, ttBannerAdListener);
        }
    }

    private void bindDislike(Activity activity, TTNativeExpressAd ad, boolean customStyle) {
        //Use the default dislike popup style in the default personalization template
        ad.setDislikeCallback(activity, new TTAdDislike.DislikeInteractionCallback() {
            @Override
            public void onSelected(int position, String value) {
                if (mListener != null) {
                    mListener.onBannerAdClose(TTATBannerAdapter.this);
                }
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onRefuse() {

            }
        });
    }


    @Override
    public View getBannerView() {
        return mBannerView;
    }


    @Override
    public void clean() {
        mttBannerAd = null;
        mBannerView = null;
        if (mTTNativeExpressAd != null) {
            mTTNativeExpressAd.destroy();
        }
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    @Override
    public String getSDKVersion() {
        return TTATConst.getNetworkVersion();
    }

    @Override
    public String getNetworkName() {
        return TTATInitManager.getInstance().getNetworkName();
    }
}
