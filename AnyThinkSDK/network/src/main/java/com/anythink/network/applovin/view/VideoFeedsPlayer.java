package com.anythink.network.applovin.view;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.anythink.core.common.utils.CommonUtil;

import java.util.Timer;
import java.util.TimerTask;

/**
 * base player
 *
 * @author simon
 */
public class VideoFeedsPlayer implements OnCompletionListener, OnErrorListener, OnPreparedListener, OnInfoListener, OnBufferingUpdateListener {

    public static final String TAG = "VideoFeedsPlayer";

    /**
     * Whether to finish playing
     */
    private boolean mIsComplete = false;
    private boolean mIsPlaying = false;
    private boolean mHasPrepare = false;

    /**
     * Is buffering
     */
    private boolean mIsBuffering = false;

    /**
     * Whether buffer timeout is required
     */
    private boolean mIsNeedBufferingTimeout = false;

    /**
     * If prepare is complete, if it is in the background, the start method is not called.
     */
    private boolean mIsFrontDesk = true;
    ;

    /**
     * Buffer time
     */
    private int mBufferTime = 5;

    /**
     * Current playback progress in milliseconds
     */
    private int mCurrentPosition;

    public static final int INTERVAL_TIME_PLAY_TIME_CD_THREAD = 1 * 1000;

    private Timer mPlayProgressTimer;
    private Timer mBufferTimeoutTimer;

    private Context mAppContext;


    /**
     * internal listener
     */
    private VideoFeedsPlayerListener mInnerVFPLisener;

    private Object mLock = new Object();
    private String mPlayUrl;
    private MediaPlayer mMediaPlayer;
    private View mLoadingView;
    private TextView mpAdcountDwon;
    private ImageView mpAdclose;
    private ImageView mpAdsoundclose;
    private ImageView mpReplay;

    /**
     * true is silent, default is not silent
     */
    private boolean mIsSilent;


    private boolean visibikityClose;//Whether to show the close button


    private boolean mIsClickSoundBtn;//Records whether the user has clicked the sound control button
    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
        }

    };

    public void setShowClose(boolean canshow) {
        visibikityClose = canshow;
    }

    /**
     * Init MediaPlayer
     *
     * @param loadingView
     * @param videoListener
     * @return
     */
    public boolean initMediaPlayer(Context context, ImageView mpReplay, View loadingView, TextView mp_adcountDwon, ImageView mp_adclose, ImageView mp_adsoundclose, boolean showCloseButton, boolean isSilent, VideoFeedsPlayerListener videoListener) {

        Log.d(TAG, "initMediaPlayer-----");
        boolean isInitSuccess = false;
        mAppContext = context;

        isSilent = true;

        try {
            synchronized (mLock) {
                if (this.mMediaPlayer == null) {
                    this.mMediaPlayer = new MediaPlayer();
                    this.mMediaPlayer.reset();

                } else {
                    this.mMediaPlayer.release();
                    this.mMediaPlayer = new MediaPlayer();
                    this.mMediaPlayer.reset();
                }
                if (loadingView == null) {
                    Log.i(TAG, "loadingView = null");
                    postOnPlayErrorOnMainThread(PlayerErrorConstant.MEDIAPLAYER_INIT_FAILED);
                    return false;
                }

                this.mInnerVFPLisener = videoListener;

                this.mLoadingView = loadingView;
                this.mpReplay = mpReplay;
                this.mpAdcountDwon = mp_adcountDwon;
                this.mpAdclose = mp_adclose;
                this.mpAdsoundclose = mp_adsoundclose;
                this.mIsSilent = isSilent;
                initShowSound(context, isSilent);
                this.mpAdsoundclose.setOnClickListener(new View.OnClickListener() {//声音控制
                    @Override
                    public void onClick(View view) {
                        Log.i(TAG, "mpAdsoundclose....." + mIsSilent);
                        doChangeShowSound();

                    }
                });


                this.mpAdclose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.i(TAG, "mpAdclose.....");
                        postOnPlayCloseOnMainThread();
                    }
                });

                this.visibikityClose = showCloseButton;
                this.mMediaPlayer.setOnCompletionListener(this);
                this.mMediaPlayer.setOnErrorListener(this);
                this.mMediaPlayer.setOnPreparedListener(this);
                this.mMediaPlayer.setOnInfoListener(this);
                this.mMediaPlayer.setOnBufferingUpdateListener(this);
                isInitSuccess = true;
                setDataSource();
            }
        } catch (Throwable e) {
            e.printStackTrace();
            String errStr = "";
            if (e != null) {
                errStr = e.toString();
            }
            isInitSuccess = false;
            postOnPlayErrorOnMainThread(errStr);
        }
        return isInitSuccess;
    }


    public int getDuration() {
        return this.mMediaPlayer.getDuration();
    }

    public void setDisplay(SurfaceHolder surfaceHolder) {

        try {
            if (mMediaPlayer != null) {
                this.mMediaPlayer.setDisplay(surfaceHolder);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    /***
     * init
     */
    private void initShowSound(Context context, boolean _isSilent) {
        if (_isSilent) {
            closeSound();
        } else {

            float streamVolumeLeft = 1f;
            float streamVolumeRight = 1f;
            Log.d(TAG, "streamVolumeLeft--" + streamVolumeLeft);
            openSound(streamVolumeLeft, streamVolumeRight);

        }
        changeCloseImg();
    }

    private void doChangeShowSound() {
        if (mIsSilent) {
            openSound();
        } else {
            closeSound();
        }
        changeCloseImg();
    }

    /**
     * play video
     */
    public void play(String playUrl, int curPositon) {

        try {
            synchronized (mLock) {

                Log.e(TAG, "currentionPosition:" + mCurrentPosition);

                if (curPositon > 0) {
                    mCurrentPosition = curPositon;
                }
                if (TextUtils.isEmpty(playUrl)) {
                    postOnPlayErrorOnMainThread(PlayerErrorConstant.PLAY_URL_ILLEGAL);
                    return;
                }

                this.mPlayUrl = playUrl;
                mHasPrepare = false;
                mIsFrontDesk = true;
                showLoading();
                setDataSource();
                if (this.mMediaPlayer != null && mHasPrepare) {
                    postOnReStartOnMainThread(mCurrentPosition, this.mMediaPlayer.getDuration() / 1000);

                }

                Log.i(TAG, "mPlayUrl:" + mPlayUrl);
            }
        } catch (Exception e) {

            e.printStackTrace();

            releasePlayer();
            hideLoading();
            postOnPlayErrorOnMainThread(PlayerErrorConstant.PLAY_CANNOT_PALY);
        }
    }

    private class PlayProgressTask extends TimerTask {

        @Override
        public void run() {

            try {
                if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {

                    mCurrentPosition = mMediaPlayer.getCurrentPosition();
                    int currentPosition = mCurrentPosition / 1000;

                    Log.i(TAG, "currentPosition:" + currentPosition);

                    int allDurSecond = 0;
                    if (mMediaPlayer != null && mMediaPlayer.getDuration() > 0) {
                        allDurSecond = mMediaPlayer.getDuration() / 1000;
                    }
                    if (currentPosition >= 0 && allDurSecond > 0 && mMediaPlayer.isPlaying()) {
                        postOnPlayProgressOnMainThread(currentPosition, allDurSecond);
                        setPlayCountDwon(currentPosition, allDurSecond);
                    }

                    mIsComplete = false;
                    if (!mIsBuffering) {
                        Log.e(TAG, "mIsBuffering=false hideloading");
                        hideLoading();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    private void startPlayProgressTimer() {

        try {
            cancelPlayProgressTimer();
            mPlayProgressTimer = new Timer();
            PlayProgressTask playProgressTask = new PlayProgressTask();
            mPlayProgressTimer.schedule(playProgressTask, 0, INTERVAL_TIME_PLAY_TIME_CD_THREAD);
        } catch (Exception e) {

            e.printStackTrace();

        }
    }

    private void cancelPlayProgressTimer() {

        try {
            if (mPlayProgressTimer != null) {
                mPlayProgressTimer.cancel();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cancelBufferTimeoutTimer() {

        try {
            if (mBufferTimeoutTimer != null) {
                mBufferTimeoutTimer.cancel();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Enable the buffer timeout timer
     *
     * @param bufferMsg
     */
    private void startBufferIngTimer(final String bufferMsg) {

        if (!mIsNeedBufferingTimeout) {
            Log.e(TAG, "No buffer timeout required");
            return;
        }

        cancelBufferTimeoutTimer();

        mBufferTimeoutTimer = new Timer();
        mBufferTimeoutTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    if (!mHasPrepare || mIsBuffering) {
                        Log.e(TAG, "Buffer timeout");
                        postOnBufferingStarOnMainThread(bufferMsg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, mBufferTime * 1000);

    }


    /**
     * init buffer
     */
    public void initBufferIngParam(int maxBufferTime) {
        if (maxBufferTime > 0) {
            mBufferTime = maxBufferTime;
        }
        mIsNeedBufferingTimeout = true;
        Log.i(TAG, "mIsNeedBufferingTimeout:" + mIsNeedBufferingTimeout + "  mMaxBufferTime:" + mBufferTime);
    }


    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        try {
            mIsComplete = true;
            mIsPlaying = false;
            mCurrentPosition = 0;
            hideLoading();
            postOnPlayCompletedOnMainThread();
            Log.i(TAG, "======onCompletion");

            mpReplay.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPrepared(final MediaPlayer mediaPlayer) {

        try {
            Log.i(TAG, "on prepar listener");
            if (mpReplay != null && mpReplay.getVisibility() == View.VISIBLE) {
                return;
            }

            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                Log.i(TAG, "on prepare, playing now ");
                return;
            }

            Log.i(TAG, "onPrepared:" + mHasPrepare);
            if (mIsFrontDesk) {

                mMediaPlayer.seekTo(mCurrentPosition);
                mMediaPlayer.setOnSeekCompleteListener(new OnSeekCompleteListener() {
                    @Override
                    public void onSeekComplete(MediaPlayer mp) {
                        try {
                            hideLoading();
                            mHasPrepare = true;
                            if (mMediaPlayer != null && mpReplay.getVisibility() != View.VISIBLE) {
                                mMediaPlayer.start();
                                mIsPlaying = true;
                                if (mMediaPlayer.getCurrentPosition() == 0) {
                                    postOnPlayStartOnMainThread(mMediaPlayer.getDuration());
                                    Log.i(TAG, "onPlayStarted()");
                                }
                            }
                            postOnBufferinEndOnMainThread();
                            startPlayProgressTimer();
                            Log.i(TAG, "onprepare mCurrentPosition:" + mCurrentPosition + " onp repare start play, mHasPrepare：" + mHasPrepare);
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                });

            } else {
                Log.i(TAG, "No processing at this time in the background");
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * show loading
     */
    public void showLoading() {
        Log.i(TAG, "showLoading.................");
        try {

            if (mHandler == null) {
                return;
            }
            mHandler.post(new Runnable() {
                @Override
                public void run() {

                    if (mLoadingView != null) {
                        mLoadingView.setVisibility(View.VISIBLE);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showPlayStatView() {
        Log.i(TAG, "showPlayStatView.................");
        try {

            if (mHandler == null) {
                return;
            }
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mpAdcountDwon != null) {
                        mpAdcountDwon.setVisibility(View.GONE);
                    }
                    if (visibikityClose && mpAdclose != null) {
                        mpAdclose.setVisibility(View.VISIBLE);
                    }
                    if (mpAdsoundclose != null) {
                        mpAdsoundclose.setVisibility(View.VISIBLE);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***
     * set countdown
     */
    private void setPlayCountDwon(final int curr, final int all) {
        try {
            if (mHandler == null) {
                return;
            }
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mpAdcountDwon != null) {
                        mpAdcountDwon.setText((all - curr) + "");
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void hidePlayStatView() {
        Log.i(TAG, "hidePlayStatView.................");
        try {
            if (mHandler == null) {
                return;
            }
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mpAdcountDwon != null) {
                        mpAdcountDwon.setVisibility(View.GONE);
                    }
                    if (mpAdclose != null) {
                        mpAdclose.setVisibility(View.GONE);
                    }
                    if (mpAdsoundclose != null) {
                        mpAdsoundclose.setVisibility(View.GONE);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /***
     * Modify close button's picture
     */
    private void changeCloseImg() {

        try {
            if (mHandler == null) {
                return;
            }
            mHandler.post(new Runnable() {
                @Override
                public void run() {


                    if (mpAdsoundclose != null) {
                        if (isSilent()) {
                            mpAdsoundclose.setImageResource(CommonUtil.getResId(mpAdsoundclose.getContext(), "video_soundclose_close", "mipmap"));
                        } else {
                            mpAdsoundclose.setImageResource(CommonUtil.getResId(mpAdsoundclose.getContext(), "video_soundclose_open", "mipmap"));
                        }
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**
     * hide loading
     */
    private void hideLoading() {
        try {
            if (mHandler == null) {
                return;
            }
            mHandler.post(new Runnable() {
                @Override
                public void run() {

                    if (mLoadingView != null) {
                        mLoadingView.setVisibility(View.GONE);
                    }
                    if (mpAdcountDwon != null) {
                        mpAdcountDwon.setVisibility(View.GONE);
                    }
                    if (visibikityClose && mpAdclose != null) {
                        mpAdclose.setVisibility(View.VISIBLE);
                    }
                    if (mpAdsoundclose != null) {
                        mpAdsoundclose.setVisibility(View.VISIBLE);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * PlayClose callback
     */
    private void postOnPlayCloseOnMainThread() {
        try {
            if (mHandler != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mInnerVFPLisener != null) {
                            mInnerVFPLisener.onPlayClose();
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * onPlayProgress callback
     */
    private void postOnPlayProgressOnMainThread(final int _curPlayPosition, final int _allDurationSecond) {
        Log.d(TAG, "postOnPlayProgressOnMainThread---" + _curPlayPosition + ":" + _allDurationSecond);
        try {
            if (mHandler != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mInnerVFPLisener != null) {
                            mInnerVFPLisener.onPlayProgress(_curPlayPosition, _allDurationSecond);
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 在OnBufferingStart callback
     */
    private void postOnBufferingStarOnMainThread(final String bufferMsg) {
        try {
            if (mHandler != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {


                        if (mInnerVFPLisener != null) {
                            mInnerVFPLisener.OnBufferingStart(bufferMsg);
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * OnBufferingEnd callback
     */
    private void postOnBufferinEndOnMainThread() {
        try {
            if (mHandler != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {


                        if (mInnerVFPLisener != null) {
                            mInnerVFPLisener.OnBufferingEnd();
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * onPlayStart callback
     */
    private void postOnPlayStartOnMainThread(final int allDuration) {
        try {
            if (mHandler != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {


                        if (mInnerVFPLisener != null) {
                            mInnerVFPLisener.onPlayStarted(allDuration);
                        }
                    }
                });
            }
        } catch (Exception e) {

            e.printStackTrace();

        }
    }

    /**
     * onPlayError callback
     *
     * @param errStr
     */
    private void postOnPlayErrorOnMainThread(final String errStr) {
        try {
            if (mHandler != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {


                        if (mInnerVFPLisener != null) {
                            mInnerVFPLisener.onPlayError(errStr);
                        }
                    }
                });
            }
        } catch (Exception e) {

            e.printStackTrace();

        }
    }

    /**
     * onPlaySetDataSourceError callback
     *
     * @param errStr
     */
    private void postOnPlaySetDataSourceError2MainThread(final String errStr) {
        try {
            if (mHandler != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        if (mInnerVFPLisener != null) {
                            mInnerVFPLisener.onPlaySetDataSourceError(errStr);
                        }
                    }
                });
            }
        } catch (Exception e) {

            e.printStackTrace();

        }
    }

    /**
     * onPlayCompleted callback
     */
    private void postOnPlayCompletedOnMainThread() {
        try {
            if (mHandler != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mInnerVFPLisener != null) {
                            mInnerVFPLisener.onPlayCompleted();
                        }
                    }
                });
            }
        } catch (Exception e) {

            e.printStackTrace();

        }
    }

    private void postOnPlayPauseOnMainThread(final int curPlayPosition) {
        try {
            if (mHandler != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mInnerVFPLisener != null) {
                            mInnerVFPLisener.onPalyPause(curPlayPosition);
                        }
                    }
                });
            }
        } catch (Exception e) {

            e.printStackTrace();
        }

    }

    private void postOnReStartOnMainThread(final int curPlayPosition, final int allPlayPosition) {
        try {
            if (mHandler != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mInnerVFPLisener != null) {
                            mInnerVFPLisener.onPalyRestart(curPlayPosition, allPlayPosition);
                        }
                    }
                });
            }
        } catch (Exception e) {

            e.printStackTrace();

        }
    }

    private void postOnSoundStatOnMainThread(final boolean openSound) {
        try {
            if (mHandler != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mInnerVFPLisener != null) {
                            mInnerVFPLisener.onSoundStat(openSound);
                        }
                    }
                });
            }
        } catch (Exception e) {

            e.printStackTrace();

        }
    }

    /**
     * pause playback
     */
    public void pause() {
        pause(true);
    }

    /**
     * pause playback
     */
    public void pause(boolean needCallBack) {
        try {
            if (!mHasPrepare) {
                return;
            }
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                Log.i(TAG, "pause isPalying:" + mMediaPlayer.isPlaying() + " mIsPlaying:" + mIsPlaying);
                hideLoading();
                mMediaPlayer.pause();
                mIsPlaying = false;
            }
        } catch (Exception e) {
        }
    }

    /**
     * stop playback
     */
    public void stop() {
        try {
            if (!mHasPrepare) {
                return;
            }
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                hideLoading();
                mMediaPlayer.stop();
                mHasPrepare = false;
                mIsPlaying = false;
            }
        } catch (Exception e) {

            e.printStackTrace();

        }
    }

    /**
     * start play
     */
    public void start() {
        try {
            if (!mHasPrepare) {
                Log.i(TAG, "!mHasPrepare");
                return;
            }
            if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
                showLoading();
                mMediaPlayer.start();
                mIsPlaying = true;

                Log.i(TAG, "start");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * start play
     */
    public void start(final int curPosition) {
        try {

            if (!mHasPrepare) {
                return;
            }

            if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {

                if (curPosition > 0) {
                    mMediaPlayer.seekTo(curPosition);
                    mMediaPlayer.setOnSeekCompleteListener(new OnSeekCompleteListener() {
                        @Override
                        public void onSeekComplete(MediaPlayer mp) {
                            mMediaPlayer.start();
                            mIsPlaying = true;
                            Log.i(TAG, "==================start curposition:" + curPosition);
                        }
                    });
                } else {
                    mMediaPlayer.start();
                    mIsPlaying = true;
                    Log.i(TAG, "=========start ");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * set DataSource
     */
    public void setDataSource() {
        try {
            Log.i(TAG, "setDataSource");
            if (mMediaPlayer != null) {
                mMediaPlayer.reset();
                mMediaPlayer.setDataSource(mAppContext, Uri.parse(mPlayUrl));
                mHasPrepare = false;
                mMediaPlayer.prepareAsync();
                startBufferIngTimer(PlayerErrorConstant.PREPARE_TIMEOUT);
            }
        } catch (Exception e) {
            hideLoading();
            postOnPlaySetDataSourceError2MainThread(PlayerErrorConstant.ILLEGAL_VIDEO_ADDRESS);
        }
    }

    /**
     * set listener
     */
    public void setSelfVideoFeedsPlayerListener(VideoFeedsPlayerListener selfVFPLisener) {
        mInnerVFPLisener = selfVFPLisener;
    }

    /**
     * Release Player
     */
    public void releasePlayer() {

        try {
            Log.i(TAG, "release");
            cancelPlayProgressTimer();
            cancelBufferTimeoutTimer();
            if (mMediaPlayer != null) {
                stop();
                mMediaPlayer.reset();

                mMediaPlayer.release();
                mMediaPlayer = null;
                mIsPlaying = false;
            }
            hideLoading();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * close sound
     */
    public void closeSound() {

        try {
            if (mMediaPlayer == null) {
                return;
            }
            mMediaPlayer.setVolume(0, 0);
            mIsSilent = true;
            postOnSoundStatOnMainThread(false);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * open sound
     */
    public void openSound() {
        openSound(1f, 1f);
    }


    /**
     * open sound
     */
    public void openSound(float streamVolumeLeft, float streamVolumeRight) {

        try {
            if (mMediaPlayer == null) {
                return;
            }
            mMediaPlayer.setVolume(streamVolumeLeft, streamVolumeRight);
            mIsSilent = false;
            postOnSoundStatOnMainThread(true);
        } catch (Exception e) {

            e.printStackTrace();

        }
    }

    /**
     * get current progress
     *
     * @return
     */
    public int getCurPosition() {
        return mCurrentPosition;
    }

    /**
     * is playing
     *
     * @return
     */
    public boolean isPlayIng() {

        try {
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
        try {
            Log.e(TAG, "onError what:" + what + " extra:" + extra);
            if (what != -38) {
                mHasPrepare = false;
                postOnPlayErrorOnMainThread(PlayerErrorConstant.UNKNOW_ERROR);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {

        try {
            Log.e(TAG, "onInfo what:" + what);
            switch (what) {

                case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                    Log.e(TAG, "BUFFERING_START:" + what);
                    mIsBuffering = true;
                    showLoading();

                    startBufferIngTimer("play buffering tiemout");
                    break;

                case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                    Log.e(TAG, "BUFFERING_END:" + what);
                    mIsBuffering = false;
                    hideLoading();
                    postOnBufferinEndOnMainThread();
                    break;
                default:
                    ;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int bufferingProgress) {

    }


    /**
     * Whether it has been prepared
     */
    public boolean hasPrepare() {
        return mHasPrepare;
    }

    public void setisFrontDesk(boolean isFrontDesk) {
        try {
            mIsFrontDesk = isFrontDesk;
            Log.e(TAG, "isFrontDesk:" + isFrontDesk);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isComplete() {
        return mIsComplete;
    }

    /**
     * true is mute, the default is not mute
     */
    public boolean isSilent() {
        return mIsSilent;
    }
}
