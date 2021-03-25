/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.ui;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.anythink.basead.FeedbackDialogController;
import com.anythink.basead.buiness.OfferAdFunctionUtil;
import com.anythink.basead.entity.OfferError;
import com.anythink.basead.entity.OfferErrorCode;
import com.anythink.basead.entity.UserOperateRecord;
import com.anythink.basead.entity.VideoViewRecord;
import com.anythink.basead.listeners.AdEventMessager;
import com.anythink.core.common.entity.BaseAdContent;
import com.anythink.core.common.entity.BaseAdRequestInfo;
import com.anythink.core.common.entity.OwnBaseAdContent;
import com.anythink.core.common.entity.OwnBaseAdTrackObject;
import com.anythink.core.common.utils.CommonLogUtil;
import com.anythink.core.common.utils.CommonUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FullScreenAdView extends BaseAdView {

    public static final String TAG = FullScreenAdView.class.getSimpleName();

    // ad type： 0 =Native，1= RV，2=banner ，3=inter ，4=splash
    public static final int FORMAT_REWARD_VIDEO = 1;
    public static final int FORMAT_INTERSTITIAL = 3;

    private int mAdFormat;
    private int mOrientation;
    private boolean mIsShowEndCard;
    private boolean mNeedHideFeedbackButton;

    private RelativeLayout mRoot;
    private PlayerView mPlayerView;
    private BannerView mBannerView;
    private EndCardView mEndCardView;
    private LoadingView mLoadingView;//Loading

    int mVideoStartPosition; //ms
    private int mScreenWidth;
    private int mScreenHeight;
    private boolean mIsClicking;

    private AdEventMessager.OnEventListener mListener;
    private long mShowBannerTime;
    private boolean mCanShowBannerView;

    private long mVideoPlayStartTime;
    private FeedbackDialogController mFeedbackDialogController;

    public FullScreenAdView(Context context) {
        super(context);
    }

    public FullScreenAdView(Context context, BaseAdRequestInfo baseAdRequestInfo, BaseAdContent baseAdContent, String scenario, int format, int orientation) {
        super(context, baseAdRequestInfo, baseAdContent, scenario);

        this.mAdFormat = format;
        this.mOrientation = orientation;
    }

    public void setListener(AdEventMessager.OnEventListener listener) {
        this.mListener = listener;
    }

    public void setShowBannerTime(long showBannerTime) {
        mShowBannerTime = showBannerTime;
    }

    public boolean isShowEndCard() {
        return this.mIsShowEndCard;
    }

    public void setIsShowEndCard(boolean isShowEndCard) {
        this.mIsShowEndCard = isShowEndCard;
    }

    public void setHideFeedbackButton(boolean hideFeedbackButton) {
        this.mNeedHideFeedbackButton = hideFeedbackButton;
    }

    public boolean needHideFeedbackButton() {
        return this.mNeedHideFeedbackButton;
    }

    @Override
    protected void initContentView() {
        LayoutInflater.from(getContext()).inflate(CommonUtil.getResId(getContext(), "myoffer_activity_ad", "layout"), this);
    }

    public void init() {
        super.removeCache();

        mRoot = findViewById(CommonUtil.getResId(getContext(), "myoffer_rl_root", "id"));
        setId(CommonUtil.getResId(getContext(), "myoffer_full_screen_view_id", "id"));

        getScreenParams();

        mCanShowBannerView = canShowBannerView();

        if (mIsShowEndCard) {
            showEndCard();
        } else if (FORMAT_REWARD_VIDEO == mAdFormat) {//Rewarded Video
            if (mBaseAdContent.hasVideoUrl()) {
                initPlayer();
            } else {
                notifyShowFailed(OfferErrorCode.get(OfferErrorCode.rewardedVideoPlayError, OfferErrorCode.fail_no_video_url));
            }
        } else if (FORMAT_INTERSTITIAL == mAdFormat) {//Interstitial
            if (mBaseAdContent.getUnitType() == BaseAdContent.UNIT_TYPE_VIDEO && mBaseAdContent.hasVideoUrl()) {//Interstitial Video
                initPlayer();
            } else {//Interstitial Ad
                showEndCard();
                FullScreenAdView.super.onShow();
            }
        }
    }


    @Override
    protected UserOperateRecord createUserOperateRecord() {
        UserOperateRecord userOperateRecord = new UserOperateRecord(mBaseAdRequestInfo.requestId, mScenario);
        userOperateRecord.realWidth = getWidth();
        userOperateRecord.realHeight = getHeight();
        if (mPlayerView != null) {
            userOperateRecord.videoViewRecord = fillVideoEndRecord(true);
        }
        return userOperateRecord;
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
        mBannerView = new BannerView(mRoot, mBaseAdContent, mBaseAdRequestInfo.baseAdSetting, mOrientation, new BannerView.OnBannerListener() {
            @Override
            public void onClick() {
                FullScreenAdView.this.onClick();
            }
        });

    }

    private void initPlayer() {
        mPlayerView = new PlayerView(mRoot, new PlayerView.OnPlayerListener() {
            @Override
            public void onVideoPlayStart() {
                CommonLogUtil.d(TAG, "onVideoPlayStart...");
//                mIsShow = true;
                //Record Video play start
                mVideoPlayStartTime = System.currentTimeMillis();
                FullScreenAdView.super.onShow();
                notifyVideoPlayStart();
            }

            @Override
            public void onVideoUpdateProgress(int progress) {
                trackProgress(progress);
                if (mCanShowBannerView && mBannerView == null && mShowBannerTime >= 0 && progress >= mShowBannerTime) {
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

                UserOperateRecord userOperateRecord = createUserOperateRecord();

                OfferAdFunctionUtil.sendAdTracking(OfferAdFunctionUtil.VIDEO_FINISH_TYPE, mBaseAdContent, userOperateRecord);

                OfferAdFunctionUtil.sendAdTracking(OfferAdFunctionUtil.VIDEO_REWARDED_TYPE, mBaseAdContent, userOperateRecord);

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
                UserOperateRecord userOperateRecord = createUserOperateRecord();
                userOperateRecord.videoViewRecord = fillVideoEndRecord(false);
                OfferAdFunctionUtil.sendAdTracking(OfferAdFunctionUtil.VIDEO_ERROR_TYPE, mBaseAdContent, userOperateRecord);
                notifyShowFailed(error);
            }

            @Override
            public void onVideoPlayProgress(int progressArea) {
                UserOperateRecord userOperateRecord = createUserOperateRecord();
                switch (progressArea) {
                    case 25:
                        CommonLogUtil.d(TAG, "onVideoProgress25.......");
                        OfferAdFunctionUtil.sendAdTracking(OfferAdFunctionUtil.VIDEO_PROGRESS25_TYPE, mBaseAdContent, userOperateRecord);
                        break;
                    case 50:
                        CommonLogUtil.d(TAG, "onVideoProgress50.......");
                        OfferAdFunctionUtil.sendAdTracking(OfferAdFunctionUtil.VIDEO_PROGRESS50_TYPE, mBaseAdContent, userOperateRecord);
                        break;
                    case 75:
                        CommonLogUtil.d(TAG, "onVideoProgress75.......");
                        OfferAdFunctionUtil.sendAdTracking(OfferAdFunctionUtil.VIDEO_PROGRESS75_TYPE, mBaseAdContent, userOperateRecord);
                        break;
                }
            }

            @Override
            public void onVideoCloseClick() {
                if (mPlayerView != null) {
                    mPlayerView.stop();
                    UserOperateRecord userOperateRecord = createUserOperateRecord();
                    userOperateRecord.adClickRecord = getAdClickRecord();
                    OfferAdFunctionUtil.sendAdTracking(OfferAdFunctionUtil.VIDEO_SKIP_TYPE, mBaseAdContent, userOperateRecord);
                }

                showEndCard();
            }

            @Override
            public void onVideoClick() {
                if (mCanShowBannerView && mShowBannerTime == -1) {
                    showBannerView();
                }
                UserOperateRecord userOperateRecord = createUserOperateRecord();
                userOperateRecord.adClickRecord = getAdClickRecord();
                OfferAdFunctionUtil.sendAdTracking(OfferAdFunctionUtil.VIDEO_CLICK_TYPE, mBaseAdContent, userOperateRecord);

                if (mBaseAdRequestInfo.baseAdSetting != null && mBaseAdRequestInfo.baseAdSetting.getVideoClick() == 1) {
                    FullScreenAdView.this.onClick();
                }
            }

            @Override
            public void onVideoMute() {
                CommonLogUtil.d(TAG, "onVideoMute...");
                UserOperateRecord userOperateRecord = createUserOperateRecord();
                userOperateRecord.adClickRecord = getAdClickRecord();
                OfferAdFunctionUtil.sendAdTracking(OfferAdFunctionUtil.VIDEO_MUTE_TYPE, mBaseAdContent, userOperateRecord);
            }

            @Override
            public void onVideoNoMute() {
                CommonLogUtil.d(TAG, "onVideoNoMute...");
                UserOperateRecord userOperateRecord = createUserOperateRecord();
                userOperateRecord.adClickRecord = getAdClickRecord();
                OfferAdFunctionUtil.sendAdTracking(OfferAdFunctionUtil.VIDEO_NO_MUTE_TYPE, mBaseAdContent, userOperateRecord);
            }

            @Override
            public void onVideoFeedbackClick() {
                showFeedbackDialog();
            }
        });
        mPlayerView.setSetting(mBaseAdRequestInfo.baseAdSetting);
        mPlayerView.setHideFeedbackButton(mNeedHideFeedbackButton);
        mPlayerView.load(mBaseAdContent.getVideoUrl());
    }

    private void showEndCard() {
        CommonLogUtil.d(TAG, "showEndCard.......");
        mIsShowEndCard = true;

        //todo 判断是否MRAID 调用showMraidEndCard()
//        if () {
//            showMraidEndCard();
//        } else {
        showEndCardView();
//        }


        if (mPlayerView != null) {
            mRoot.removeView(mPlayerView);
            mPlayerView = null;
        }

        UserOperateRecord userOperateRecord = createUserOperateRecord();
        OfferAdFunctionUtil.sendAdTracking(OfferAdFunctionUtil.ENDCARD_SHOW_TYPE, mBaseAdContent, userOperateRecord);
    }

    private void showMraidEndCard() {

    }

    private void showEndCardView() {
        //if can not show banner view, then show ad choice in end card page
        boolean needShowAdChoiceInEndCardView = !mCanShowBannerView;

        boolean needShowLearnMoreButton = false;
        if (!mCanShowBannerView) {
            if (mBaseAdRequestInfo.baseAdSetting != null && mBaseAdRequestInfo.baseAdSetting.getEndCardClickArea() != 0) {
                if (mBaseAdContent instanceof OwnBaseAdContent) {//Adx、OnlineApi
                    if (OwnBaseAdContent.CREATIVE_TYPE_SINGLE_PICTURE == ((OwnBaseAdContent) mBaseAdContent).getCreativeType()) {
                        //need show cta (Learn More)
                        needShowLearnMoreButton = true;
                    }
                }
            }
        }

        mEndCardView = new EndCardView(mRoot, mScreenWidth, mScreenHeight, mBaseAdContent, mBaseAdRequestInfo.baseAdSetting,
                needShowAdChoiceInEndCardView, mNeedHideFeedbackButton, needShowLearnMoreButton, new EndCardView.OnEndCardListener() {
            @Override
            public void onClick() {
                CommonLogUtil.d(TAG, "EndCard onClick: ");
                FullScreenAdView.this.onClick();
            }

            @Override
            public void onCloseEndCard() {
                CommonLogUtil.d(TAG, "onCloseEndCard.......");
                UserOperateRecord userOperateRecord = createUserOperateRecord();
                userOperateRecord.adClickRecord = getAdClickRecord();
                OfferAdFunctionUtil.sendAdTracking(OfferAdFunctionUtil.ENDCARD_CLOSE_TYPE, mBaseAdContent, userOperateRecord);
                if (mListener != null) {
                    mListener.onClose();
                }
            }

            @Override
            public void onClickFeedback() {
                showFeedbackDialog();
            }
        });

        if (mCanShowBannerView) {
            showBannerView();
        }
    }

    private void showLearnMoreButton() {
        TextView learnMoreButton = new TextView(getContext());
        learnMoreButton.setText(CommonUtil.getResId(getContext(), "myoffer_cta_learn_more", "string"));
        learnMoreButton.setTextColor(Color.parseColor("#ffffffff"));
        learnMoreButton.setTextSize(20);
        learnMoreButton.setGravity(Gravity.CENTER);
        learnMoreButton.setBackgroundResource(CommonUtil.getResId(getContext(), "myoffer_splash_btn", "drawable"));

        int width = CommonUtil.dip2px(getContext(), 200);
        int height = CommonUtil.dip2px(getContext(), 70);
        int bottomMargin = CommonUtil.dip2px(getContext(), 23);

        LayoutParams layoutParams = new LayoutParams(width, height);
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        layoutParams.bottomMargin = bottomMargin;

        learnMoreButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                FullScreenAdView.this.onClick();
            }
        });

        addView(learnMoreButton, layoutParams);
    }

    private void showBannerView() {
        initBannerView();
    }

    private boolean canShowBannerView() {

        if (TextUtils.isEmpty(mBaseAdContent.getTitle())) {
            return false;
        }

        if (mBaseAdContent instanceof OwnBaseAdContent && BaseAdContent.UNIT_TYPE_IMAGE == mBaseAdContent.getUnitType()) {//interstitial image
            if (OwnBaseAdContent.CREATIVE_TYPE_SINGLE_PICTURE_AND_TEXT == ((OwnBaseAdContent) mBaseAdContent).getCreativeType()) {
                return true;
            } else {
                return false;
            }
        }

        return true;
    }


    private void showFeedbackDialog() {
        //player pause
        FullScreenAdView.this.onPause();

        if (mFeedbackDialogController == null) {
            mFeedbackDialogController = new FeedbackDialogController();
        }
        mFeedbackDialogController.showDialog(getContext(), mBaseAdContent, mBaseAdRequestInfo, new FeedbackDialogController.FeedbackDialogListener() {

            @Override
            public void onFeedback() {
                hideFeedbackButton();
            }

            @Override
            public void onClosed() {
                //player start
                FullScreenAdView.this.onResume();
                mFeedbackDialogController.destroy();
            }
        });
    }

    private void hideFeedbackButton() {
        mNeedHideFeedbackButton = true;
        if (mPlayerView != null) {
            mPlayerView.removeFeedbackButton();
        }

        if (mIsShowEndCard && mEndCardView != null) {
            mEndCardView.removeFeedbackButton();
        }
    }


    @Override
    protected void onClick() {
        CommonLogUtil.d(TAG, "click 。。。。。");

        if (mIsClicking) {
            CommonLogUtil.d(TAG, "during click 。。。。。");
            return;
        }
        if (mBaseAdContent == null) {
            return;
        }

        super.onClick();
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

    private void notifyVideoPlayStart() {
        OfferAdFunctionUtil.sendAdTracking(OfferAdFunctionUtil.VIDEO_START_TYPE, mBaseAdContent, createUserOperateRecord());

        if (mListener != null) {
            mListener.onVideoPlayStart();
        }
    }

    private void notifyShowFailed(OfferError error) {
        if (mListener != null) {
            mListener.onVideoShowFailed(error);
        }
    }

    @Override
    protected void notifyShow() {

        OfferAdFunctionUtil.sendAdTracking(OfferAdFunctionUtil.IMPRESSION_TYPE, mBaseAdContent, createUserOperateRecord());

        if (mListener != null) {
            mListener.onShow();
        }
    }

    @Override
    protected void notifyClick() {
        UserOperateRecord userOperateRecord = createUserOperateRecord();
        userOperateRecord.adClickRecord = getAdClickRecord();
        OfferAdFunctionUtil.sendAdTracking(OfferAdFunctionUtil.CLICK_TYPE, mBaseAdContent, userOperateRecord);

        if (mListener != null) {
            mListener.onClick();
        }
    }

    @Override
    protected void notifyDeeplinkCallback(boolean isSuccess) {
        if (mListener != null) {
            mListener.onDeeplinkCallback(isSuccess);
        }
    }

    @Override
    protected void onClickStart() {
        mIsClicking = true;
        showLoading();
    }

    @Override
    protected void onClickEnd() {
        mIsClicking = false;
        post(new Runnable() {
            @Override
            public void run() {
                hideLoading();
            }
        });
    }

    protected void onResume() {
        try {
            if (mFeedbackDialogController != null && mFeedbackDialogController.isDialogShowing()) {
                return;
            }

            if (mPlayerView != null && !mPlayerView.isPlaying()) {
                mVideoStartPosition = mPlayerView.getCurrentPosition();
                mPlayerView.start();
                //Record Video play start
                mVideoPlayStartTime = System.currentTimeMillis();
                if (mVideoStartPosition != 0) {
                    OfferAdFunctionUtil.sendAdTracking(OfferAdFunctionUtil.VIDEO_RESUME_TYPE, mBaseAdContent, createUserOperateRecord());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void onPause() {
        if (mPlayerView != null && mPlayerView.isPlaying()) {
            OfferAdFunctionUtil.sendAdTracking(OfferAdFunctionUtil.VIDEO_PAUSE_TYPE, mBaseAdContent, createUserOperateRecord());
            mPlayerView.pause();
        }
    }

    public VideoViewRecord fillVideoEndRecord(boolean successPlay) {
        VideoViewRecord mVideoViewRecord = new VideoViewRecord();
        mVideoViewRecord.viodePlayScence = mOrientation == Configuration.ORIENTATION_LANDSCAPE ? VideoViewRecord.LANDSCAPE_VIDEO_FULLSCREEN : VideoViewRecord.PORTRAIT_VIDEO_FULLSCREEN;
        mVideoViewRecord.videoPlayBehavior = VideoViewRecord.AUTO_PLAY_BEHAVIOR;
        mVideoViewRecord.videoLength = mPlayerView != null ? mPlayerView.getVideoLength() / 1000 : 0;
        mVideoViewRecord.videoStartTime = mVideoStartPosition / 1000;
        mVideoViewRecord.videoEndTime = mPlayerView.getCurrentPosition() / 1000;
        mVideoViewRecord.isVideoPlayInStart = mVideoStartPosition == 0 ? 1 : 0;
        mVideoViewRecord.videoPlayType = mVideoStartPosition == 0 ? VideoViewRecord.FIRST_PLAY_VIDEO_TYPE : VideoViewRecord.RE_PLAY_VIDEO_TYPE;
        mVideoViewRecord.isVideoPlayInEnd = mPlayerView.getCurrentPosition() == mPlayerView.getVideoLength() ? 1 : 0;
        mVideoViewRecord.videoPlayStatus = successPlay ? VideoViewRecord.CORRECT_PLAY_STATUS : VideoViewRecord.ERROR_PLAY_STATUS;
        mVideoViewRecord.videoStartUTCMillTime = mVideoPlayStartTime;
        mVideoViewRecord.videoEndUTCMillTime = System.currentTimeMillis();
        mVideoViewRecord.videoCurrentMillPosition = mPlayerView.getCurrentPosition();

        CommonLogUtil.i(TAG, "Video End Record:" + mVideoViewRecord.toString());
        return mVideoViewRecord;
    }

    ConcurrentHashMap<Integer, Boolean> progressTrackStatus;

    //TODO Test Progress Tracking
    private void trackProgress(int progressMillSecond) {
        if (mBaseAdContent instanceof OwnBaseAdContent) {
            OwnBaseAdTrackObject ownBaseAdTrackObject = ((OwnBaseAdContent) mBaseAdContent).getTrackObject();
            Map<Integer, String[]> progressTrackMap = ownBaseAdTrackObject.getVideoDirectProgressUrls();
            if (progressTrackMap != null && progressTrackMap.size() > 0) {
                if (progressTrackStatus == null) {
                    progressTrackStatus = new ConcurrentHashMap<>();
                }
                int progressSecond = progressMillSecond / 1000;
                for (Integer trackProgress : progressTrackMap.keySet()) {
                    if (progressTrackStatus.get(trackProgress) != null && progressTrackStatus.get(trackProgress)) {
                        continue;
                    }
                    if (progressSecond >= trackProgress) {
                        progressTrackStatus.put(trackProgress, true);
                        UserOperateRecord userOperateRecord = createUserOperateRecord();
                        userOperateRecord.videoViewRecord.videoDirectTrackingProgress = trackProgress;
                        OfferAdFunctionUtil.sendAdTracking(OfferAdFunctionUtil.VIDEO_DIRECT_PROGRESS_TYPE, mBaseAdContent, userOperateRecord);
                    }
                }
            }
        }
    }

    protected void onDestroy() {
        this.destroy();
    }

    @Override
    protected void destroy() {
        super.destroy();

        this.mListener = null;
    }

}
