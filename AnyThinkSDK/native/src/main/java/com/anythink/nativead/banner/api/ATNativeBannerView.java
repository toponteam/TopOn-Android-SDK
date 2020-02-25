package com.anythink.nativead.banner.api;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.anythink.core.api.ATAdInfo;
import com.anythink.core.api.AdError;
import com.anythink.core.common.utils.CommonUtil;
import com.anythink.nativead.api.ATNative;
import com.anythink.nativead.api.ATNativeAdView;
import com.anythink.nativead.api.ATNativeEventListener;
import com.anythink.nativead.api.ATNativeNetworkListener;
import com.anythink.nativead.api.ATNativeOpenSetting;
import com.anythink.nativead.api.NativeAd;
import com.anythink.nativead.banner.util.ATBannerRender;
import com.anythink.nativead.bussiness.utils.CommonSDCardUtil;

import java.util.Map;

public class ATNativeBannerView extends RelativeLayout {

    ATNativeBannerListener mListener;
    String mUnitId;

    ATNative mATNative;
    ATNativeAdView mNativeAdView;

    Handler mHandler = new Handler();

    TextView mAdLogoTextView;
    ImageView mCloseView;

    ATNativeBannerConfig mConfig = new ATNativeBannerConfig();

    boolean mIsRefresh = false;

    ATNativeNetworkListener mATNativeNetworkListener = new ATNativeNetworkListener() {
        @Override
        public void onNativeAdLoaded() {
            mIsLoading = false;

            if (mListener != null && !mIsRefresh) {
                mListener.onAdLoaded();
            }

            if (visibility == VISIBLE && getVisibility() == VISIBLE && hasTouchWindow) {
                addNativeView();

                stopAutoRefresh();
                startAutoRefresh();
            }


        }

        @Override
        public void onNativeAdLoadFail(AdError adError) {
            mIsLoading = false;
            if (mListener != null) {
                if (!mIsRefresh) {
                    mListener.onAdError(adError.printStackTrace());
                } else {
                    mListener.onAutoRefreshFail(adError.printStackTrace());
                }

            }
        }
    };

    ATBannerRender mRender;
    Map<String, Object> mLocalExtra;

    private void addNativeView() {
        if (mATNative != null) {
            NativeAd nativeAd = mATNative.getNativeAd();
            if (nativeAd != null) {
                if (mNativeAdView != null) {
                    removeView(mNativeAdView);
                    mNativeAdView = null;
                }

                nativeAd.setNativeEventListener(eventListener);
                mNativeAdView = new ATNativeAdView(getContext());
                if (mRender == null) {
                    mRender = new ATBannerRender(getContext(), mConfig);
                }
                mRender.setConfig(mConfig);
                try {
                    nativeAd.renderAdView(mNativeAdView, mRender);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                nativeAd.prepare(mNativeAdView);

                LayoutParams params = null;
                if (mRender.getBannerSize() == ATNativeBannerSize.BANNER_SIZE_640x150) {
                    params = new LayoutParams(dip2px(getContext(), 360), dip2px(getContext(), 75));
                    params.addRule(CENTER_IN_PARENT);
                }

                if (mRender.getBannerSize() == ATNativeBannerSize.BANNER_SIZE_320x50) {
                    params = new LayoutParams(dip2px(getContext(), 320), dip2px(getContext(), 50));
                    params.addRule(CENTER_IN_PARENT);
                }

                if (mRender.getBannerSize() == ATNativeBannerSize.BANNER_SIZE_AUTO) {
                    params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                    params.addRule(CENTER_IN_PARENT);
                }

                if (params != null) {
                    addView(mNativeAdView, 0, params);
                } else {
                    addView(mNativeAdView, 0);
                }

                if (mConfig.isCloseBtnShow) {
                    mCloseView.setVisibility(VISIBLE);
                } else {
                    mCloseView.setVisibility(GONE);
                }

                if (mConfig.backgroupResId != 0) {
                    try {
                        setBackgroundResource(mConfig.backgroupResId);
                    } catch (Exception e) {

                    }
                }
                mAdLogoTextView.setVisibility(VISIBLE);
            }
        }
    }

    ATNativeEventListener eventListener = new ATNativeEventListener() {
        @Override
        public void onAdImpressed(ATNativeAdView nativeAdView, ATAdInfo entity) {
            if (mListener != null) {
                if (mIsRefresh) {
                    mListener.onAutoRefresh(entity);
                } else {
                    mListener.onAdShow(entity);
                }

            }
        }

        @Override
        public void onAdClicked(ATNativeAdView nativeAdView, ATAdInfo entity) {
            if (mListener != null) {
                mListener.onAdClick(entity);
            }
        }

        @Override
        public void onAdVideoStart(ATNativeAdView nativeAdView) {
        }

        @Override
        public void onAdVideoEnd(ATNativeAdView nativeAdView) {
        }

        @Override
        public void onAdVideoProgress(ATNativeAdView nativeAdView, int i) {
        }
    };

    public ATNativeBannerView(Context context) {
        super(context);
        init();
    }

    public ATNativeBannerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ATNativeBannerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setUnitId(String unitId) {
        mUnitId = unitId;
        mATNative = new ATNative(getContext().getApplicationContext(), mUnitId, mATNativeNetworkListener);
        if (mLocalExtra != null) {
            mATNative.setLocalExtra(mLocalExtra);
        }
    }

    public void setBannerConfig(ATNativeBannerConfig config) {
        if (config == null) {
            return;
        }
        mConfig = config;
    }

    public void setLocalExtra(Map<String, Object> localExtra) {
        configMap = localExtra;
    }

    public void setAdListener(ATNativeBannerListener listener) {
        mListener = listener;
    }

    Map<String, Object> configMap;
    Map<String, String> customRequestMap;

    boolean mIsLoading;

    public void loadAd(Map<String, String> customRequestMap) {

        this.customRequestMap = customRequestMap;

        loadAd(false);
    }

    private void loadAd(boolean refresh) {
        if (mATNative == null) {
            if (mListener != null) {
                if (!refresh) {
                    mListener.onAdError("Unit id is empty");
                }
            }
            return;
        }

        mIsRefresh = refresh;

        if (mIsLoading) {
            if (mListener != null) {
                if (!refresh) {
                    mListener.onAdError("Banner is loading");
                }
            }
            return;
        }

        mIsLoading = true;
        stopAutoRefresh();
        mATNative.setLocalExtra(configMap);
        mATNative.makeAdRequest(customRequestMap);
    }

    private void init() {
        CommonSDCardUtil.init(getContext().getApplicationContext());

        mAdLogoTextView = new TextView(getContext());
        mAdLogoTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 7);
        mAdLogoTextView.setText("AD");
        mAdLogoTextView.setTextColor(0xffffffff);
        mAdLogoTextView.setIncludeFontPadding(false);
        mAdLogoTextView.setGravity(Gravity.CENTER);
        mAdLogoTextView.setPadding(dip2px(getContext(), 3), dip2px(getContext(), 1), dip2px(getContext(), 3), dip2px(getContext(), 1));
        mAdLogoTextView.setBackgroundResource(CommonUtil.getResId(getContext(), "plugin_banner_ad_bg", "drawable"));

        LayoutParams adLogoParam = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        adLogoParam.addRule(ALIGN_PARENT_LEFT);
        adLogoParam.addRule(ALIGN_PARENT_TOP);
        adLogoParam.topMargin = dip2px(getContext(), 3);
        adLogoParam.leftMargin = dip2px(getContext(), 3);
        addView(mAdLogoTextView, adLogoParam);
        mAdLogoTextView.setVisibility(INVISIBLE);

        mCloseView = new ImageView(getContext());
        mCloseView.setImageResource(CommonUtil.getResId(getContext(), "plugin_banner_icon_close", "drawable"));
        LayoutParams closeParam = new LayoutParams(dip2px(getContext(), 15), dip2px(getContext(), 15));
        closeParam.rightMargin = dip2px(getContext(), 2);
        closeParam.topMargin = dip2px(getContext(), 2);
        closeParam.addRule(ALIGN_PARENT_RIGHT);
        addView(mCloseView, closeParam);
        mCloseView.setVisibility(INVISIBLE);

        mCloseView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onAdClose();
                }
            }
        });

    }

    boolean hasTouchWindow;
    int visibility;

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        hasTouchWindow = true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        hasTouchWindow = false;
        stopAutoRefresh();
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        controlShow(visibility);
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        controlShow(visibility);
    }

    private void controlShow(int visibility) {
        this.visibility = visibility;
        if (mATNative == null) {
            return;
        }
        if (visibility != VISIBLE || !hasTouchWindow || getVisibility() != VISIBLE) {
            stopAutoRefresh();
        } else {
            addNativeView();
            stopAutoRefresh();
            startAutoRefresh();

        }
    }


    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (visibility != VISIBLE || !hasTouchWindow || getVisibility() != VISIBLE || !hasWindowFocus) {
            stopAutoRefresh();
        } else {
            stopAutoRefresh();
            startAutoRefresh();
        }
    }

    Runnable mRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            loadAd(true);
        }
    };

    private void startAutoRefresh() {
        try {
            ATNativeOpenSetting atSetting = null;
            if (mATNative != null) {
                atSetting = mATNative.getOpenSetting();
            }
            if (mConfig.refreshTime == -1 && atSetting != null) {
                if (atSetting.isAutoRefresh) {
                    mHandler.postDelayed(mRefreshRunnable, atSetting.autoRefreshTime);
                }
            } else {
                if (mConfig.refreshTime > 0) {
                    mHandler.postDelayed(mRefreshRunnable, mConfig.refreshTime);
                }
            }
        } catch (Throwable e) {
            stopAutoRefresh();
            if (mConfig.refreshTime > 0) {
                mHandler.postDelayed(mRefreshRunnable, mConfig.refreshTime);
            }
        }
    }

    private void stopAutoRefresh() {
        mHandler.removeCallbacks(mRefreshRunnable);
    }


    public int dip2px(Context context, float dipValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }
}
