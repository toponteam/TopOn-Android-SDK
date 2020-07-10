package com.anythink.network.applovin.view;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.utils.CommonUtil;


/**
 * PlayerView
 *
 * @author zhou
 */
public class PlayerView extends LinearLayout {
    public static final String TAG = "PlayerView";

    private LinearLayout mLlSurContainer;
    private ProgressBar mLoadingView;
    //count down
    private TextView mpAdcountDwon;
    //close button
    private ImageView mpAdclose;
    //close sound
    private ImageView mpAdsoundclose;


    //replay button
    private ImageView mpReplay;

    private VideoFeedsPlayer mVideoFeedsPlayer;

    private String mPlayUrl;

    private boolean mInitState = false;

    private boolean mIsFirstCreateHolder = true;
    private boolean mIsSurfaceHolderDestoryed = false;

    /**
     * Whether playback is complete
     */
    private boolean mIsComplete = false;

    private SurfaceHolder mSurfaceHolder;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    public PlayerView(Context context) {
        super(context);
        init();
    }

    public PlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {

        try {
            initView();
            initPlayer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void initPlayer() {
        mVideoFeedsPlayer = new VideoFeedsPlayer();
    }

    /**
     * Add surfaceView
     */
    public void addSurfaceView() {

        try {
            Log.i(TAG, "addSurfaceView");
            SurfaceView mSurfaceView = new SurfaceView(getContext());
            mSurfaceHolder = mSurfaceView.getHolder();

            mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            mSurfaceHolder.setFormat(PixelFormat.RGBA_8888);
            mSurfaceHolder.setKeepScreenOn(true);
            mSurfaceHolder.addCallback(new MySurceHoldeCallback());
            mLlSurContainer.addView(mSurfaceView, -1, -1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initView() {

        int resLayoutID = CommonUtil.getResId(getContext(), "video_common_player_view", "layout");
        View rootView = LayoutInflater.from(getContext()).inflate(resLayoutID, null);
        if (rootView != null) {
            mLlSurContainer = (LinearLayout) rootView.findViewById(CommonUtil.getResId(getContext(), "video_playercommon_ll_sur_container", "id"));
            mLoadingView = (ProgressBar) rootView.findViewById(CommonUtil.getResId(getContext(), "video_progressBar", "id"));
            mpAdcountDwon = (TextView) rootView.findViewById(CommonUtil.getResId(getContext(), "video_adcountDwon", "id"));
            mpAdclose = (ImageView) rootView.findViewById(CommonUtil.getResId(getContext(), "video_adclose", "id"));
            mpAdsoundclose = (ImageView) rootView.findViewById(CommonUtil.getResId(getContext(), "video_adsoundclose", "id"));

            mpReplay = (ImageView) rootView.findViewById(CommonUtil.getResId(getContext(), "video_replay", "id"));

            addSurfaceView();
            addView(rootView, -1, -1);
        }
    }

    VideoFeedsPlayerListener mvfpListener;

    private void postOnInitCallBackOnMainThread(final boolean tag) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mvfpListener != null) {
                    mvfpListener.onInitCallBack(tag);
                }
            }
        });
    }

    private void postOnPlaySetDataSourceErrorOnMainThread(final String msg) {

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mvfpListener != null) {
                    mvfpListener.onPlaySetDataSourceError(PlayerErrorConstant.PLAY_FILE_NOT_EXISTS);
                }
            }
        });
    }

    /**
     * Init Video
     *
     * @param videoPlayUrl
     * @param showCloseButton
     * @param vfpListener
     * @return
     */
    public void initVFPData(final String videoPlayUrl, final boolean showCloseButton, final boolean isSilent, final VideoFeedsPlayerListener vfpListener) {
        mvfpListener = vfpListener;
        Log.d(TAG, "initVFPData-----");
        String playUrl = videoPlayUrl;

        if (TextUtils.isEmpty(videoPlayUrl)) {
            Log.i(TAG, "playUrl==null");
            postOnInitCallBackOnMainThread(false);
            return;
        }

        mPlayUrl = playUrl;

        mInitState = true;
        Log.d(TAG, "go.....initMediaPlayer-----");
        mVideoFeedsPlayer.initMediaPlayer(getContext(), mpReplay, mLoadingView, mpAdcountDwon, mpAdclose, mpAdsoundclose, showCloseButton, isSilent, vfpListener);

        postOnInitCallBackOnMainThread(true);

        this.mpReplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mpReplay.setVisibility(GONE);
                start(0);
            }
        });
    }

    /***
     * Set resolution
     * @param  videoWidth 1280
     * @param  videoHeight  720
     */
    public void setVideoLayout(int videoWidth, int videoHeight) {
        Log.d(TAG, "----" + videoWidth + "x" + videoHeight);
        if (mSurfaceHolder != null) {
            mSurfaceHolder.setFixedSize(videoWidth, videoHeight);
        }
    }

    /**
     * play video
     */
    public void playVideo(int curPosition) {
        try {
            if (mVideoFeedsPlayer == null) {
                Log.i(TAG, "player init error");
                return;
            }
            if (!mInitState) {
                Log.i(TAG, "vfp init failed");
                return;
            }

            mVideoFeedsPlayer.play(mPlayUrl, curPosition);
        } catch (Throwable t) {
            Log.e(TAG, t.getMessage(), t);
        }
    }

    public void playVideo() {
        playVideo(0);
    }

    public void onPause() {
        try {
            pause();
            if (mVideoFeedsPlayer != null) {
                mVideoFeedsPlayer.setisFrontDesk(false);
                if (mvfpListener != null) {
                    mvfpListener.onPalyPause(mVideoFeedsPlayer.getCurPosition());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * pause playback
     */
    private void pause() {
        try {
            if (mVideoFeedsPlayer != null) {
                mVideoFeedsPlayer.pause();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void resumeStar() {
        try {
            if (Build.VERSION.SDK_INT >= 24) {
                /**System above 7.0 Switch back to the foreground from the background requires setDataSource*/
                setDataSource();
            } else {
                start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setDataSource() {
        try {

            if (mVideoFeedsPlayer != null) {
                mVideoFeedsPlayer.showLoading();
                mVideoFeedsPlayer.setDataSource();
                mLoadingView.setVisibility(View.VISIBLE);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * play video
     */
    private void start() {
        try {

            if (mVideoFeedsPlayer != null) {
                mVideoFeedsPlayer.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * play video
     */
    private void start(int curPosition) {
        try {

            if (mVideoFeedsPlayer != null) {
                mVideoFeedsPlayer.start(curPosition);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stop() {
        try {
            if (mVideoFeedsPlayer != null) {
                mVideoFeedsPlayer.stop();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * open sound
     */
    public void openSound() {
        if (mVideoFeedsPlayer != null) {
            mVideoFeedsPlayer.openSound();
        }
    }

    /**
     * close sound
     */
    public void closeSound() {
        if (mVideoFeedsPlayer != null) {
            mVideoFeedsPlayer.closeSound();
        }
    }

    /**
     * resume playback
     */
    public void onResume() {
        try {

            mVideoFeedsPlayer.setisFrontDesk(true);

            if (mvfpListener != null) {
                mvfpListener.onPalyResume(mVideoFeedsPlayer.getCurPosition());
            }
            if (mVideoFeedsPlayer != null && !mIsFirstCreateHolder && !mIsSurfaceHolderDestoryed && !mIsComplete) {
                Log.i(TAG, "onresume========");
                if (mVideoFeedsPlayer.hasPrepare()) {
                    resumeStar();
                } else {
                    playVideo(0);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * release player resources
     */
    public void release() {
        try {
            if (mVideoFeedsPlayer != null) {
                mVideoFeedsPlayer.releasePlayer();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private class MySurceHoldeCallback implements SurfaceHolder.Callback {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                Log.i(TAG, "surfaceCreated");
                if (mVideoFeedsPlayer != null && holder != null) {
                    mSurfaceHolder = holder;
                    mVideoFeedsPlayer.setDisplay(holder);
                }

                mIsFirstCreateHolder = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

            try {
                Log.i(TAG, "surfaceDestroyed ");
                mIsSurfaceHolderDestoryed = true;
                mVideoFeedsPlayer.pause(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            try {
                Log.i(TAG, "surfaceChanged");
                // ///////
                if (mIsSurfaceHolderDestoryed && !mIsComplete && mpReplay.getVisibility() != VISIBLE) {

                    if (mVideoFeedsPlayer.hasPrepare()) {
                        Log.i(TAG, "surfaceChanged  start====");
                        resumeStar();
                    } else {
                        Log.i(TAG, "surfaceChanged  PLAY====");
                        playVideo(0);
                    }

                }
                // //////
                mIsSurfaceHolderDestoryed = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


    public int getCurPosition() {
        if (mVideoFeedsPlayer != null) {
            return mVideoFeedsPlayer.getCurPosition();
        }
        return 0;
    }

    /**
     * init buffer
     *
     * @param maxBufferTime
     */
    private void initBufferIngParam(int maxBufferTime) {
        if (mVideoFeedsPlayer != null) {
            mVideoFeedsPlayer.initBufferIngParam(maxBufferTime);
        }
    }

    public boolean isPlayIng() {
        try {
            if (mVideoFeedsPlayer != null) {
                return mVideoFeedsPlayer.isPlayIng();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return false;
    }

    public boolean isComplete() {
        if (mVideoFeedsPlayer != null) {
            return mVideoFeedsPlayer.isComplete();
        }
        return false;
    }
}
