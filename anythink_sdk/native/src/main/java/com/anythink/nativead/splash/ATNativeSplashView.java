/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.nativead.splash;

import android.content.Context;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.anythink.core.api.ATAdInfo;
import com.anythink.core.common.entity.TemplateStrategy;
import com.anythink.core.common.utils.CommonUtil;
import com.anythink.core.strategy.PlaceStrategy;
import com.anythink.core.strategy.PlaceStrategyManager;
import com.anythink.nativead.api.ATNativeAdView;
import com.anythink.nativead.api.ATNativeEventListener;
import com.anythink.nativead.api.NativeAd;
import com.anythink.nativead.splash.api.ATNativeSplashListener;
import com.anythink.nativead.splash.util.ATSplashRender;

public class ATNativeSplashView extends RelativeLayout {

    ATSplashRender mRender;
    ATNativeAdView mNativeAdView;

    View mDevelopSkipView;
    TextView mSelfSkipView;

    long mFetchDelay;

    public ATNativeSplashView(Context context) {
        super(context);
        init();
    }

    public ATNativeSplashView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ATNativeSplashView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    String mSkipString = "";

    private void init() {
        LayoutInflater.from(getContext()).inflate(CommonUtil.getResId(getContext(), "plugin_splash_view_layout", "layout"), this);

        mNativeAdView = (ATNativeAdView) findViewById(CommonUtil.getResId(getContext(), "plugin_splash_native", "id"));
        mRender = new ATSplashRender(getContext());

        mSelfSkipView = (TextView) findViewById(CommonUtil.getResId(getContext(), "plugin_splash_skip", "id"));
        mSelfSkipView.setVisibility(GONE);

        mSkipString = getContext().getString(CommonUtil.getResId(getContext(), "plugin_splash_skip_text", "string"));
    }


    public void renderAd(final ViewGroup parenet, NativeAd nativeAd, final String unitId) {
        PlaceStrategy placeStrategy = PlaceStrategyManager.getInstance(getContext()).getPlaceStrategyByAppIdAndPlaceId(unitId);
        final TemplateStrategy templateStrategy = placeStrategy != null ? placeStrategy.getTemplateStrategy() : null;
        if (templateStrategy != null && templateStrategy.isUseNetConfig) {
            mFetchDelay = templateStrategy.countDownTime;
        }
        nativeAd.setNativeEventListener(new ATNativeEventListener() {
            @Override
            public void onAdImpressed(ATNativeAdView view, ATAdInfo entity) {
                if (mListener != null) {
                    mListener.onAdShow(entity);
                }

            }

            @Override
            public void onAdClicked(ATNativeAdView view, ATAdInfo entity) {
                if (mListener != null) {
                    mListener.onAdClick(entity);
                }
            }

            @Override
            public void onAdVideoStart(ATNativeAdView view) {
            }

            @Override
            public void onAdVideoEnd(ATNativeAdView view) {
            }

            @Override
            public void onAdVideoProgress(ATNativeAdView view, int progress) {
            }
        });

        mRender.setCallback(new ATSplashRender.ImageFinishCallback() {
            @Override
            public void finish() {
                parenet.addView(ATNativeSplashView.this, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                startCountDown(templateStrategy != null && templateStrategy.canSkip);
            }
        });

        try {
            nativeAd.renderAdView(mNativeAdView, mRender);
        } catch (Exception e) {
            e.printStackTrace();
        }

        nativeAd.prepare(mNativeAdView);
    }

    CountDownTimer countDownTimer;
    boolean isFinishCountDown;

    private void startCountDown(final boolean isShowSkip) {

        if (mDevelopSkipView != null) {
            mDevelopSkipView.setVisibility(VISIBLE);
            mDevelopSkipView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isShowSkip || isFinishCountDown) {
                        if (mListener != null) {
                            mListener.onAdSkip();
                        }
                        if (countDownTimer != null) {
                            countDownTimer.cancel();
                        }
                    }

                }
            });
        } else {
            mSelfSkipView.setVisibility(VISIBLE);
            mSelfSkipView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isShowSkip || isFinishCountDown) {
                        if (mListener != null) {
                            mListener.onAdSkip();
                        }
                        if (countDownTimer != null) {
                            countDownTimer.cancel();
                        }
                    }

                }
            });
        }

        isFinishCountDown = false;

        countDownTimer = new CountDownTimer(mFetchDelay, 1000L) {
            @Override
            public void onTick(long l) {
                if (mListener != null) {
                    mListener.onAdTick(l);
                }

                if (mDevelopSkipView == null) {
                    if (isShowSkip) {
                        mSelfSkipView.setText((l / 1000) + "s " + mSkipString);
                    } else {
                        mSelfSkipView.setText((l / 1000) + " s");
                    }

                }
            }

            @Override
            public void onFinish() {
                mSelfSkipView.setText(mSkipString);
                if (mListener != null) {
                    mListener.onAdTimeOver();
                }
                isFinishCountDown = true;
            }
        };

        countDownTimer.start();
    }

    ATNativeSplashListener mListener;

    public void setNativeSplashListener(ATNativeSplashListener listener) {
        mListener = listener;
    }

    public void setDevelopSkipView(View skipView, long fetchDelay) {
        mFetchDelay = fetchDelay;
        mDevelopSkipView = skipView;
    }

}
