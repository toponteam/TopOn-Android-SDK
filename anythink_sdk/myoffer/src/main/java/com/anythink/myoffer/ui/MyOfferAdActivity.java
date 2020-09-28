package com.anythink.myoffer.ui;

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

import com.anythink.core.common.base.Const;
import com.anythink.core.common.entity.MyOfferAd;
import com.anythink.core.common.entity.MyOfferSetting;
import com.anythink.core.common.utils.CommonLogUtil;
import com.anythink.core.common.utils.CommonUtil;
import com.anythink.myoffer.buiness.MyOfferAdManager;
import com.anythink.myoffer.buiness.OfferClickController;
import com.anythink.myoffer.net.MyOfferTkLoader;
import com.anythink.myoffer.network.base.MyOfferAdMessager;
import com.anythink.network.myoffer.MyOfferError;
import com.anythink.network.myoffer.MyOfferErrorCode;

public class MyOfferAdActivity extends Activity {

    public static final String TAG = MyOfferAdActivity.class.getSimpleName();

    // ad type： 0 =Native，1= RV，2=banner ，3=inter ，4=splash
    public static final int FORMAT_REWARD_VIDEO = 1;
    public static final int FORMAT_INTERSTITIAL = 3;

    private String mRequestId;
    private String mScenario;
    private int mAdFormat;
    private MyOfferAd mMyOfferAd;
    private String mPlacementId;
    private String mOfferId;
    private MyOfferSetting mMyOfferSetting;
    private long mTimeStamp;

    private static final String EXTRA_REQUEST_ID = "extra_request_id";
    private static final String EXTRA_SCENARIO = "extra_scenario";
    private static final String EXTRA_AD_FORMAT = "extra_ad_format";
    private static final String EXTRA_MYOFFER_AD = "extra_myoffer_ad";
    private static final String EXTRA_PLACEMENT_ID = "extra_placement_id";
    private static final String EXTRA_OFFER_ID = "extra_offer_id";
    private static final String EXTRA_MYOFFER_SETTING = "extra_myoffer_setting";
    private static final String EXTRA_TIMESTAMP = "extra_timestamp";

    private static final String EXTRA_IS_SHOW_END_CARD = "extra_is_show_end_card";

    private boolean mIsShowEndCard;
    private MyOfferAdMessager.OnEventListener mListener;

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

    /**
     * 开启页面
     */
    public static void start(Context context, String requestId, String scenario, int adFormat, MyOfferAd myOfferAd, String placementId,
                             String offerId, MyOfferSetting myOfferSetting, int orientation, long timeStamp) {
        Intent intent = new Intent();

        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            intent.setClass(context, MyOfferLandscapeActivity.class);
        } else {
            intent.setClass(context, MyOfferPortraitActivity.class);
        }

        intent.putExtra(EXTRA_REQUEST_ID, requestId);
        intent.putExtra(EXTRA_SCENARIO, scenario);
        intent.putExtra(EXTRA_AD_FORMAT, adFormat);
        intent.putExtra(EXTRA_MYOFFER_AD, myOfferAd);
        intent.putExtra(EXTRA_PLACEMENT_ID, placementId);
        intent.putExtra(EXTRA_OFFER_ID, offerId);
        intent.putExtra(EXTRA_MYOFFER_SETTING, myOfferSetting);
        intent.putExtra(EXTRA_TIMESTAMP, timeStamp);

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private void parseExtra() {
        Intent intent = getIntent();
        try {
            if (intent != null) {
                mRequestId = intent.getStringExtra(EXTRA_REQUEST_ID);
                mScenario = intent.getStringExtra(EXTRA_SCENARIO);
                mAdFormat = intent.getIntExtra(EXTRA_AD_FORMAT, MyOfferAdActivity.FORMAT_REWARD_VIDEO);
                mMyOfferAd = intent.getParcelableExtra(EXTRA_MYOFFER_AD);
                mPlacementId = intent.getStringExtra(EXTRA_PLACEMENT_ID);
                mOfferId = intent.getStringExtra(EXTRA_OFFER_ID);
                mMyOfferSetting = intent.getParcelableExtra(EXTRA_MYOFFER_SETTING);
                mTimeStamp = intent.getLongExtra(EXTRA_TIMESTAMP, 0L);

                if (mMyOfferSetting != null) {
                    mShowBannerTime = mMyOfferSetting.getShowBannerTime() * 1000;
                }
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

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        parseExtra();

        if (mMyOfferAd == null) {
            Log.e(Const.RESOURCE_HEAD, TAG + " onCreate: mMyofferAd = null");
            finish();
        }
        readSaveInstance(savedInstanceState);
        setContentView(getLayoutIdByAdFormat());

        init();
    }

    private void readSaveInstance(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mIsShowEndCard = savedInstanceState.getBoolean(EXTRA_IS_SHOW_END_CARD);
        }
    }

    private void init() {
        getScreenParams();

        mRoot = findViewById(CommonUtil.getResId(this, "myoffer_rl_root", "id"));

        mListener = MyOfferAdMessager.getInstance().getListener(mPlacementId + mOfferId + mTimeStamp);

        if (mIsShowEndCard) {
            showEndCard();
        } else if (mMyOfferAd.isVideo()) {
            initPlayer();
        } else if (FORMAT_REWARD_VIDEO == mAdFormat) {
            notifyShowFailedAndFinish(MyOfferErrorCode.get(MyOfferErrorCode.rewardedVideoPlayError, MyOfferErrorCode.fail_no_video_url));
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
        mBannerView = new BannerView(mRoot, mMyOfferAd, new BannerView.OnBannerListener() {
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
                MyOfferAdManager.getInstance(getApplicationContext()).sendAdTracking(mRequestId, mMyOfferAd, MyOfferTkLoader.VIDEO_FINISH_TYPE, mScenario);

                if (mListener != null) {
                    mListener.onVideoPlayEnd();
                }

                if (mListener != null) {
                    mListener.onReward();
                }

                showEndCard();
            }

            @Override
            public void onVideoShowFailed(MyOfferError error) {
                notifyShowFailedAndFinish(error);
            }

            @Override
            public void onVideoPlayProgress(int progressArea) {
                switch (progressArea) {
                    case 25:
                        CommonLogUtil.d(TAG, "onVideoProgress25.......");
                        MyOfferAdManager.getInstance(getApplicationContext()).sendAdTracking(mRequestId, mMyOfferAd, MyOfferTkLoader.VIDEO_PROGRESS25_TYPE, mScenario);
                        break;
                    case 50:
                        CommonLogUtil.d(TAG, "onVideoProgress50.......");
                        MyOfferAdManager.getInstance(getApplicationContext()).sendAdTracking(mRequestId, mMyOfferAd, MyOfferTkLoader.VIDEO_PROGRESS50_TYPE, mScenario);
                        break;
                    case 75:
                        CommonLogUtil.d(TAG, "onVideoProgress75.......");
                        MyOfferAdManager.getInstance(getApplicationContext()).sendAdTracking(mRequestId, mMyOfferAd, MyOfferTkLoader.VIDEO_PROGRESS75_TYPE, mScenario);
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

                if (mMyOfferSetting != null && mMyOfferSetting.getVideoClick() == 1) {
                    onClick();
                }
            }
        });
        mPlayerView.setSetting(mMyOfferSetting);
        mPlayerView.load(mMyOfferAd.getVideoUrl());
    }

    private void notifyVideoPlayStart() {
        if (mListener != null) {
            mListener.onVideoPlayStart();
        }
        MyOfferAdManager.getInstance(getApplicationContext()).sendAdTracking(mRequestId, mMyOfferAd, MyOfferTkLoader.VIDEO_START_TYPE, mScenario);
    }

    private void notifyShow() {
        if (mListener != null) {
            mListener.onShow();
        }
        MyOfferAdManager.getInstance(getApplicationContext()).sendAdTracking(mRequestId, mMyOfferAd, MyOfferTkLoader.IMPRESSION_TYPE, mScenario);
    }

    private void notifyShowFailedAndFinish(MyOfferError error) {
        if (mListener != null) {
            mListener.onVideoShowFailed(error);
        }
        finish();
    }


    private void showEndCard() {
        CommonLogUtil.d(TAG, "showEndCard.......");
        mIsShowEndCard = true;
        mEndCardView = new EndCardView(mRoot, mScreenWidth, mScreenHeight, mMyOfferAd, new EndCardView.OnEndCardListener() {
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
                MyOfferAdManager.getInstance(getApplicationContext()).sendAdTracking(mRequestId, mMyOfferAd, MyOfferTkLoader.ENDCARD_CLOSE_TYPE, mScenario);
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

        MyOfferAdManager.getInstance(getApplicationContext()).sendAdTracking(mRequestId, mMyOfferAd, MyOfferTkLoader.ENDCARD_SHOW_TYPE, mScenario);
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
        if (mMyOfferAd == null) {
            return;
        }

        if (mListener != null) {
            mListener.onClick();
        }
        MyOfferAdManager.getInstance(getApplicationContext()).sendAdTracking(mRequestId, mMyOfferAd, MyOfferTkLoader.CLICK_TYPE, mScenario);

        mOfferClickController = new OfferClickController(this, mPlacementId, mMyOfferAd);
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
                        MyOfferAdManager.getInstance(getApplicationContext()).startDownloadApp(mRequestId, mMyOfferSetting, mMyOfferAd, url);
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
            outState.putBoolean(EXTRA_IS_SHOW_END_CARD, true);
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

        if (mPlayerView != null) {
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
