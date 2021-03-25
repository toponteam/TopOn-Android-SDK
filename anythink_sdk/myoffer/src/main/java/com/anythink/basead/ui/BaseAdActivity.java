/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

import com.anythink.basead.BaseAdConst;
import com.anythink.basead.entity.AdActivityStartParams;
import com.anythink.basead.entity.OfferError;
import com.anythink.basead.entity.OfferErrorCode;
import com.anythink.basead.listeners.AdEventMessager;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.entity.BaseAdContent;
import com.anythink.core.common.entity.BaseAdRequestInfo;
import com.anythink.core.common.utils.CommonLogUtil;

public class BaseAdActivity extends Activity {

    public static final String TAG = BaseAdActivity.class.getSimpleName();

    private FullScreenAdView mFullScreenAdView;

    private BaseAdRequestInfo mBaseAdRequestInfo;
    private BaseAdContent mBaseAdContent;

    private String mEventId;
    private AdEventMessager.OnEventListener mListener;

    private String mScenario;
    private int mAdFormat;
    private long mShowBannerTime;
    private int mOrientation;
    private boolean mIsShowEndCard;
    private boolean mNeedHideFeedbackButton;

    /**
     * 开启页面
     */
    public static void start(Context context, AdActivityStartParams adActivityStartParams) {
        Intent intent = new Intent();

        if (adActivityStartParams.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            intent.setClass(context, AdLandscapeActivity.class);
        } else {
            intent.setClass(context, AdPortraitActivity.class);
        }

        intent.putExtra(BaseAdConst.AcitvityParamsKey.EXTRA_SCENARIO, adActivityStartParams.scenario);
        intent.putExtra(BaseAdConst.AcitvityParamsKey.EXTRA_AD_FORMAT, adActivityStartParams.format);
        intent.putExtra(BaseAdConst.AcitvityParamsKey.EXTRA_BASE_AD, adActivityStartParams.baseAdContent);
        intent.putExtra(BaseAdConst.AcitvityParamsKey.EXTRA_EVENT_ID, adActivityStartParams.eventId);
        intent.putExtra(BaseAdConst.AcitvityParamsKey.EXTRA_BASE_REQUEST_INFO, adActivityStartParams.baseAdRequestInfo);

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private void parseExtra() {
        Intent intent = getIntent();
        try {
            if (intent != null) {
                mScenario = intent.getStringExtra(BaseAdConst.AcitvityParamsKey.EXTRA_SCENARIO);
                mAdFormat = intent.getIntExtra(BaseAdConst.AcitvityParamsKey.EXTRA_AD_FORMAT, FullScreenAdView.FORMAT_REWARD_VIDEO);
                mBaseAdContent = (BaseAdContent) intent.getSerializableExtra(BaseAdConst.AcitvityParamsKey.EXTRA_BASE_AD);
                mBaseAdRequestInfo = (BaseAdRequestInfo) intent.getSerializableExtra(BaseAdConst.AcitvityParamsKey.EXTRA_BASE_REQUEST_INFO);
                mEventId = intent.getStringExtra(BaseAdConst.AcitvityParamsKey.EXTRA_EVENT_ID);

                if (mBaseAdRequestInfo != null && mBaseAdRequestInfo.baseAdSetting != null) {
                    mShowBannerTime = mBaseAdRequestInfo.baseAdSetting.getShowBannerTime() > 0 ? mBaseAdRequestInfo.baseAdSetting.getShowBannerTime() * 1000 : mBaseAdRequestInfo.baseAdSetting.getShowBannerTime();
                }
            } else {
                Log.e(Const.RESOURCE_HEAD, TAG + " Intent is null.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ad type： 0 =Native，1= RV，2=banner ，3=inter ，4=splash
     *
     * @return
     */
    private FullScreenAdView getLayoutViewByAdFormat() {
        switch (mAdFormat) {
            case FullScreenAdView.FORMAT_INTERSTITIAL:
            case FullScreenAdView.FORMAT_REWARD_VIDEO:
            default:
                return new FullScreenAdView(this, mBaseAdRequestInfo, mBaseAdContent, mScenario, mAdFormat, mOrientation);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /**Check Application Context exist?**/
        if (SDKContext.getInstance().getContext() == null) {
            SDKContext.getInstance().setContext(getApplicationContext());
        }

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (this instanceof AdLandscapeActivity) {
            mOrientation = Configuration.ORIENTATION_LANDSCAPE;
        } else {
            mOrientation = Configuration.ORIENTATION_PORTRAIT;
        }
        parseExtra();
        mListener = AdEventMessager.getInstance().getListener(mEventId);

        if (mBaseAdRequestInfo == null || mBaseAdRequestInfo.baseAdSetting == null) {
            Log.e(Const.RESOURCE_HEAD, TAG + "Start FullScreen Ad Error.");
            try {
                if (mListener != null) {
                    mListener.onVideoShowFailed(OfferErrorCode.get(OfferErrorCode.rewardedVideoPlayError, TAG + "Start FullScreen Ad Error."));
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
            finish();
            return;
        }

        if (mBaseAdContent == null) {
            Log.e(Const.RESOURCE_HEAD, TAG + " onCreate: OfferAd = null");
            try {
                if (mListener != null) {
                    mListener.onVideoShowFailed(OfferErrorCode.get(OfferErrorCode.rewardedVideoPlayError, TAG + " onCreate: OfferAd = null"));
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
            finish();
            return;
        }

        readSaveInstance(savedInstanceState);
        mFullScreenAdView = getLayoutViewByAdFormat();

        setContentView(mFullScreenAdView);
        init();
    }

    private void readSaveInstance(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mIsShowEndCard = savedInstanceState.getBoolean(BaseAdConst.AcitvityParamsKey.EXTRA_IS_SHOW_END_CARD);
            mNeedHideFeedbackButton = savedInstanceState.getBoolean(BaseAdConst.AcitvityParamsKey.EXTRA_SHOW_FEEDBACK_BUTTON);
        }
    }

    private void init() {

        AdEventMessager.OnEventListener onEventListener = new AdEventMessager.OnEventListener() {
            @Override
            public void onShow() {
                if (mListener != null) {
                    mListener.onShow();
                }
            }

            @Override
            public void onVideoShowFailed(OfferError error) {
                if (mListener != null) {
                    mListener.onVideoShowFailed(error);
                }
                finish();
            }

            @Override
            public void onVideoPlayStart() {
                if (mListener != null) {
                    mListener.onVideoPlayStart();
                }
            }

            @Override
            public void onVideoPlayEnd() {
                if (mListener != null) {
                    mListener.onVideoPlayEnd();
                }
            }

            @Override
            public void onReward() {
                if (mListener != null) {
                    mListener.onReward();
                }
            }

            @Override
            public void onClose() {
                finish();

                if (mListener != null) {
                    mListener.onClose();
                }
            }

            @Override
            public void onClick() {
                if (mListener != null) {
                    mListener.onClick();
                }
            }

            @Override
            public void onDeeplinkCallback(boolean isSuccess) {
                if (mListener != null) {
                    mListener.onDeeplinkCallback(isSuccess);
                }
            }
        };

        mFullScreenAdView.setListener(onEventListener);
        mFullScreenAdView.setShowBannerTime(mShowBannerTime);
        mFullScreenAdView.setIsShowEndCard(mIsShowEndCard);
        mFullScreenAdView.setHideFeedbackButton(mNeedHideFeedbackButton);

        try {
            mFullScreenAdView.init();
        } catch (Throwable e) {
            e.printStackTrace();

            finish();
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mFullScreenAdView != null) {
            if (mFullScreenAdView.isShowEndCard()) {
                CommonLogUtil.d(TAG, "onSaveInstanceState... mFullScreenAdView.isShowEndCard() - true");
                outState.putBoolean(BaseAdConst.AcitvityParamsKey.EXTRA_IS_SHOW_END_CARD, true);
            }

            //Feedback button
            boolean needShowFeedbackButton = mFullScreenAdView.needHideFeedbackButton();
            CommonLogUtil.d(TAG, "onSaveInstanceState... mFullScreenAdView.needShowFeedbackButton() - " + needShowFeedbackButton);
            outState.putBoolean(BaseAdConst.AcitvityParamsKey.EXTRA_SHOW_FEEDBACK_BUTTON, needShowFeedbackButton);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mFullScreenAdView != null) {
            mFullScreenAdView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mFullScreenAdView != null) {
            mFullScreenAdView.onPause();
        }
    }

    @Override
    protected void onDestroy() {

        if (mFullScreenAdView != null) {
            mFullScreenAdView.onDestroy();
        }

        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (KeyEvent.KEYCODE_BACK == keyCode) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
