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
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.anythink.basead.BaseAdConst;
import com.anythink.basead.adx.manager.AdxApkManager;
import com.anythink.basead.buiness.OfferAdFunctionUtil;
import com.anythink.basead.buiness.OfferClickController;
import com.anythink.basead.entity.AdActivityStartParams;
import com.anythink.basead.entity.OfferError;
import com.anythink.basead.entity.OfferErrorCode;
import com.anythink.basead.listeners.AdEventMessager;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.entity.AdxOffer;
import com.anythink.core.common.entity.BaseAdContent;
import com.anythink.core.common.entity.BaseAdSetting;
import com.anythink.core.common.utils.CommonLogUtil;
import com.anythink.core.common.utils.CommonUtil;

public class BaseAdActivity extends Activity {

    public static final String TAG = BaseAdActivity.class.getSimpleName();

    // ad type： 0 =Native，1= RV，2=banner ，3=inter ，4=splash
    public static final int FORMAT_REWARD_VIDEO = 1;
    public static final int FORMAT_INTERSTITIAL = 3;

    private String mRequestId;
    private String mScenario;
    private int mAdFormat;
    private BaseAdContent mBaseAdContent;
    private String mPlacementId;
    private BaseAdSetting mMyOfferSetting;
    private String mEventId;

//    public static final String EXTRA_ORIENTATION = "extra_orientation";


    private boolean mIsShowEndCard;
    private AdEventMessager.OnEventListener mListener;

    private RelativeLayout mRoot;
    private PlayerView mPlayerView;
    private BannerView mBannerView;
    private EndCardView mEndCardView;
    private LoadingView mLoadingView;//Loading

    private long mShowBannerTime;

    private int mScreenWidth;
    private int mScreenHeight;
    private boolean mIsClicking;
    private OfferClickController mOfferClickController;

    private int mOrientation;

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

        intent.putExtra(BaseAdConst.AcitvityParamsKey.EXTRA_REQUEST_ID, adActivityStartParams.requestId);
        intent.putExtra(BaseAdConst.AcitvityParamsKey.EXTRA_SCENARIO, adActivityStartParams.scenario);
        intent.putExtra(BaseAdConst.AcitvityParamsKey.EXTRA_AD_FORMAT, adActivityStartParams.format);
        intent.putExtra(BaseAdConst.AcitvityParamsKey.EXTRA_BASE_AD, adActivityStartParams.baseAdContent);
        intent.putExtra(BaseAdConst.AcitvityParamsKey.EXTRA_PLACEMENT_ID, adActivityStartParams.placementId);
        intent.putExtra(BaseAdConst.AcitvityParamsKey.EXTRA_BASEAD_SETTING, adActivityStartParams.baseAdSetting);
        intent.putExtra(BaseAdConst.AcitvityParamsKey.EXTRA_EVENT_ID, adActivityStartParams.eventId);

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private void parseExtra() {
        Intent intent = getIntent();
        try {
            if (intent != null) {
                mRequestId = intent.getStringExtra(BaseAdConst.AcitvityParamsKey.EXTRA_REQUEST_ID);
                mScenario = intent.getStringExtra(BaseAdConst.AcitvityParamsKey.EXTRA_SCENARIO);
                mAdFormat = intent.getIntExtra(BaseAdConst.AcitvityParamsKey.EXTRA_AD_FORMAT, BaseAdActivity.FORMAT_REWARD_VIDEO);
                mBaseAdContent = (BaseAdContent) intent.getSerializableExtra(BaseAdConst.AcitvityParamsKey.EXTRA_BASE_AD);
                mPlacementId = intent.getStringExtra(BaseAdConst.AcitvityParamsKey.EXTRA_PLACEMENT_ID);
                mMyOfferSetting = (BaseAdSetting) intent.getSerializableExtra(BaseAdConst.AcitvityParamsKey.EXTRA_BASEAD_SETTING);
                mEventId = intent.getStringExtra(BaseAdConst.AcitvityParamsKey.EXTRA_EVENT_ID);

                if (mMyOfferSetting != null) {
                    mShowBannerTime = mMyOfferSetting.getShowBannerTime() > 0 ? mMyOfferSetting.getShowBannerTime() * 1000 : mMyOfferSetting.getShowBannerTime();
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
    private int getLayoutIdByAdFormat() {
        switch (mAdFormat) {
            case FORMAT_INTERSTITIAL:
            case FORMAT_REWARD_VIDEO:
            default:
                return CommonUtil.getResId(this, "myoffer_activity_ad", "layout");
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

        if (mBaseAdContent == null) {
            Log.e(Const.RESOURCE_HEAD, TAG + " onCreate: OfferAd = null, format=" + mAdFormat);
            finish();
            return;
        }
        readSaveInstance(savedInstanceState);
        setContentView(getLayoutIdByAdFormat());

        init();
    }

    private void readSaveInstance(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mIsShowEndCard = savedInstanceState.getBoolean(BaseAdConst.AcitvityParamsKey.EXTRA_IS_SHOW_END_CARD);
        }
    }

    private void init() {
        getScreenParams();

        mRoot = findViewById(CommonUtil.getResId(this, "myoffer_rl_root", "id"));

        mListener = AdEventMessager.getInstance().getListener(mEventId);

        if (mIsShowEndCard) {
            showEndCard();
        } else if (mBaseAdContent.isVideo()) {
            initPlayer();
        } else if (FORMAT_REWARD_VIDEO == mAdFormat) {
            notifyShowFailedAndFinish(OfferErrorCode.get(OfferErrorCode.rewardedVideoPlayError, OfferErrorCode.fail_no_video_url));
            return;
        } else if (FORMAT_INTERSTITIAL == mAdFormat) {
//            mIsShow = true;
            showEndCard();
            notifyShow();
        }

    }

    private void getScreenParams() {
        DisplayMetrics dm = this.getResources().getDisplayMetrics();
        mScreenWidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;

//        if (mScreenWidth > mScreenHeight) {
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
//        } else {
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        }

    }

    private void initBannerView() {
        if (mBannerView != null) {
            return;
        }
        int childCount = mRoot.getChildCount();
        if (childCount > 1) {
            for (int i = childCount - 1; i >= 1; i--) {
                mRoot.removeViewAt(i);
            }
        }
        mBannerView = new BannerView(mRoot, mBaseAdContent, mOrientation, new BannerView.OnBannerListener() {
            @Override
            public void onClickCTA() {
                onClick();
            }

            @Override
            public void onClickBanner() {
                if (mMyOfferSetting != null && mMyOfferSetting.getEndCardClickArea() != 1) {
                    onClick();
                }
            }
        });
    }

    private void initPlayer() {
        mPlayerView = new PlayerView(mRoot, new PlayerView.OnPlayerListener() {
            @Override
            public void onVideoPlayStart() {
                CommonLogUtil.d(TAG, "onVideoPlayStart...");
//                mIsShow = true;
                notifyShow();
                notifyVideoPlayStart();
            }

            @Override
            public void onVideoUpdateProgress(int progress) {
//                if(!mIsShow) {
//                    mIsShow = true;
//                }
                if (mBannerView == null && mShowBannerTime >= 0 && progress >= mShowBannerTime) {
                    showBannerView();
                }
            }

            @Override
            public void onVideoPlayEnd() {
                CommonLogUtil.d(TAG, "onVideoPlayEnd...");

            }

            @Override
            public void onVideoPlayCompletion() {
                CommonLogUtil.d(TAG, "onVideoPlayCompletion...");
                OfferAdFunctionUtil.sendAdTracking(mRequestId, mBaseAdContent, OfferAdFunctionUtil.VIDEO_FINISH_TYPE, mScenario);

                if (mListener != null) {
                    mListener.onVideoPlayEnd();
                }

                if (mListener != null) {
                    mListener.onReward();
                }

                showEndCard();
            }

            @Override
            public void onVideoShowFailed(OfferError error) {
                notifyShowFailedAndFinish(error);
            }

            @Override
            public void onVideoPlayProgress(int progressArea) {
                switch (progressArea) {
                    case 25:
                        CommonLogUtil.d(TAG, "onVideoProgress25.......");
                        OfferAdFunctionUtil.sendAdTracking(mRequestId, mBaseAdContent, OfferAdFunctionUtil.VIDEO_PROGRESS25_TYPE, mScenario);
                        break;
                    case 50:
                        CommonLogUtil.d(TAG, "onVideoProgress50.......");
                        OfferAdFunctionUtil.sendAdTracking(mRequestId, mBaseAdContent, OfferAdFunctionUtil.VIDEO_PROGRESS50_TYPE, mScenario);
                        break;
                    case 75:
                        CommonLogUtil.d(TAG, "onVideoProgress75.......");
                        OfferAdFunctionUtil.sendAdTracking(mRequestId, mBaseAdContent, OfferAdFunctionUtil.VIDEO_PROGRESS75_TYPE, mScenario);
                        break;
                }
            }

            @Override
            public void onVideoCloseClick() {
                if (mPlayerView != null) {
                    mPlayerView.stop();
                }
                showEndCard();
            }

            @Override
            public void onVideoClick() {
                if (mShowBannerTime == -1) {
                    showBannerView();
                }

                OfferAdFunctionUtil.sendAdxAdTracking(OfferAdFunctionUtil.VIDEO_CLICK_TYPE, mBaseAdContent);

                if (mMyOfferSetting != null && mMyOfferSetting.getVideoClick() == 1) {
                    onClick();
                }
            }

            @Override
            public void onVideoMute() {
                CommonLogUtil.d(TAG, "onVideoMute...");
                OfferAdFunctionUtil.sendAdxAdTracking(OfferAdFunctionUtil.VIDEO_MUTE_TYPE, mBaseAdContent);
            }

            @Override
            public void onVideoNoMute() {
                CommonLogUtil.d(TAG, "onVideoNoMute...");
                OfferAdFunctionUtil.sendAdxAdTracking(OfferAdFunctionUtil.VIDEO_NO_MUTE_TYPE, mBaseAdContent);
            }
        });
        mPlayerView.setSetting(mMyOfferSetting);
        mPlayerView.load(mBaseAdContent.getVideoUrl());
    }

    private void notifyVideoPlayStart() {
        if (mListener != null) {
            mListener.onVideoPlayStart();
        }
        OfferAdFunctionUtil.sendAdTracking(mRequestId, mBaseAdContent, OfferAdFunctionUtil.VIDEO_START_TYPE, mScenario);
    }

    private void notifyShow() {
        if (mListener != null) {
            mListener.onShow();
        }
        OfferAdFunctionUtil.sendAdTracking(mRequestId, mBaseAdContent, OfferAdFunctionUtil.IMPRESSION_TYPE, mScenario);
    }

    private void notifyShowFailedAndFinish(OfferError error) {
        if (mListener != null) {
            mListener.onVideoShowFailed(error);
        }
        finish();
    }


    private void showEndCard() {
        CommonLogUtil.d(TAG, "showEndCard.......");
        mIsShowEndCard = true;
        mEndCardView = new EndCardView(mRoot, mScreenWidth, mScreenHeight, mBaseAdContent, new EndCardView.OnEndCardListener() {
            @Override
            public void onClickEndCard() {
                Log.d(TAG, "onClickEndCard: ");

                if (mMyOfferSetting != null && mMyOfferSetting.getEndCardClickArea() == 0) {
                    onClick();
                }
            }

            @Override
            public void onCloseEndCard() {
                CommonLogUtil.d(TAG, "onCloseEndCard.......");
                OfferAdFunctionUtil.sendAdTracking(mRequestId, mBaseAdContent, OfferAdFunctionUtil.ENDCARD_CLOSE_TYPE, mScenario);
                finish();
                if (mListener != null) {
                    mListener.onClose();
                }
            }
        });

        showBannerView();

        if (mPlayerView != null) {
            mRoot.removeView(mPlayerView);
            mPlayerView = null;
        }

        OfferAdFunctionUtil.sendAdTracking(mRequestId, mBaseAdContent, OfferAdFunctionUtil.ENDCARD_SHOW_TYPE, mScenario);
    }

    private void showBannerView() {
        initBannerView();
    }

    private void onClick() {
        CommonLogUtil.d(TAG, "click 。。。。。");

        if (mIsClicking) {
            CommonLogUtil.d(TAG, "during click 。。。。。");
            return;
        }
        if (mBaseAdContent == null) {
            return;
        }

        if (mListener != null) {
            mListener.onClick();
        }
        OfferAdFunctionUtil.sendAdTracking(mRequestId, mBaseAdContent, OfferAdFunctionUtil.CLICK_TYPE, mScenario);

        mOfferClickController = new OfferClickController(this, mPlacementId, mBaseAdContent,
                mMyOfferSetting);
        mOfferClickController.startClick(mRequestId, new OfferClickController.ClickStatusCallback() {
            @Override
            public void clickStart() {
                mIsClicking = true;
                showLoading();
            }

            @Override
            public void clickEnd() {
                mIsClicking = false;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideLoading();
                    }
                });
            }

            @Override
            public void downloadApp(final String url) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideLoading();

                        if (mBaseAdContent instanceof AdxOffer) {
                            AdxApkManager.getInstance(getApplicationContext()).registerAdxApkBroadcastReceiver();
                            AdxApkManager.getInstance(getApplicationContext()).register(mBaseAdContent.getOfferId(), ((AdxOffer) mBaseAdContent));
                        }

                        OfferAdFunctionUtil.startDownloadApp(getApplicationContext(), mRequestId, mMyOfferSetting, mBaseAdContent, url);
                    }
                });
            }
        });

    }

    private void showLoading() {
        if (mLoadingView == null) {
            mLoadingView = new LoadingView(mRoot);
        }
        mLoadingView.startLoading();
    }

    private void hideLoading() {
        if (mLoadingView != null) {
            mLoadingView.hide();
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        CommonLogUtil.d(TAG, "onSaveInstanceState...");
        if (mIsShowEndCard) {
            CommonLogUtil.d(TAG, "onSaveInstanceState... mIsShowEndCard - true");
            outState.putBoolean(BaseAdConst.AcitvityParamsKey.EXTRA_IS_SHOW_END_CARD, true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            if (mPlayerView != null && !mPlayerView.isPlaying()) {
                mPlayerView.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mPlayerView != null && mPlayerView.isPlaying()) {
            OfferAdFunctionUtil.sendAdxAdTracking(OfferAdFunctionUtil.VIDEO_PAUSE_TYPE, mBaseAdContent);
            mPlayerView.pause();
        }

    }

    @Override
    protected void onDestroy() {

        if (mOfferClickController != null) {
            mOfferClickController.cancelClick();
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
