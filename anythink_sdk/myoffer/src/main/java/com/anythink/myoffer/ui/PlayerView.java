package com.anythink.myoffer.ui;

import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.anythink.core.common.entity.MyOfferSetting;
import com.anythink.core.common.utils.CommonLogUtil;
import com.anythink.core.common.utils.CommonUtil;
import com.anythink.myoffer.buiness.MyOfferResourceManager;
import com.anythink.myoffer.buiness.resource.MyOfferVideoUtil;
import com.anythink.myoffer.ui.util.ViewUtil;
import com.anythink.network.myoffer.MyOfferError;
import com.anythink.network.myoffer.MyOfferErrorCode;

import java.io.FileDescriptor;
import java.io.FileInputStream;


public class PlayerView extends RelativeLayout implements TextureView.SurfaceTextureListener {

    public static final String TAG = PlayerView.class.getSimpleName();

    private MediaPlayer mMediaPlayer;
    private SurfaceTexture mSurfaceTexture;
    private TextureView mTextureView;
    private Surface mSurface;

    private FileInputStream mFileInputStream;
    private FileDescriptor mSourceFD;
    private String mSourcePath;

    private int mVideoWidth;
    private int mVideoHeight;

    private int mCurrentPosition = -1;//Current progress
    private int mDuration;
    private int mVideoProgress25;//progress==25
    private int mVideoProgress50;//progress==50
    private int mVideoProgress75;//progress==75
    private boolean mVideoPlay25;
    private boolean mVideoPlay50;
    private boolean mVideoPlay75;

    private boolean mFlag = false;//Loop read mark for playback progress
    private boolean mIsVideoStart = false;
    private boolean mIsVideoPlayCompletion = false;
    private boolean mIsMediaPlayerPrepared = false;

    private OnPlayerListener mListener;
    private Handler mMainHandler;


    private int mViewSizeDp = 29;//dp
    private int mViewMarginDp = 19;//dp
    private int mLeftMarginDp = 19;//dp
    private int mTopMarginDp = 30;//dp
    private int mViewSize;
    private int mViewMargin;
    private int mLeftMargin;
    private int mTopMargin;

    private int mMuteResId;
    private int mNoMuteResId;
    private int mCloseResId;
    private CountDownView mCountDownView;
    private ImageView mMuteBtn;
    private ImageView mCloseBtn;

    private final int mCountDownViewIndex = 1;
    private final int mMuteButtonIndex = 2;
    private final int mCloseButtonIndex = 3;

    private boolean mIsMute;
    private long mShowCloseTime;
    private Thread mProgressThread;


    public PlayerView(ViewGroup container, OnPlayerListener listener) {
        super(container.getContext());
        this.mListener = listener;

        setId(CommonUtil.getResId(getContext(), "myoffer_player_view_id", "id"));
        setSaveEnabled(true);
        attachTo(container);

        mMainHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {

                mCurrentPosition = msg.what;

                if (mCurrentPosition <= 0) {
                    return;
                }

                //Control to show close button
                if (mCloseBtn == null && mShowCloseTime >= 0 && mCurrentPosition >= mShowCloseTime) {
                    showCloseButton();
                }

                if (!mIsVideoStart && !mIsVideoPlayCompletion) {
                    mIsVideoStart = true;
                    if (mListener != null) {
                        mListener.onVideoPlayStart();
                    }
                }

                if (mListener != null) {
                    mListener.onVideoUpdateProgress(mCurrentPosition);
                }

                if (!mVideoPlay25 && mCurrentPosition >= mVideoProgress25) {
                    mVideoPlay25 = true;
                    if (mListener != null) {
                        mListener.onVideoPlayProgress(25);
                    }
                } else if (!mVideoPlay50 && mCurrentPosition >= mVideoProgress50) {
                    mVideoPlay50 = true;
                    if (mListener != null) {
                        mListener.onVideoPlayProgress(50);
                    }
                } else if (!mVideoPlay75 && mCurrentPosition >= mVideoProgress75) {
                    mVideoPlay75 = true;
                    if (mListener != null) {
                        mListener.onVideoPlayProgress(75);
                    }
                }

                showView();
                if (mCountDownView != null && mCountDownView.isShown()) {
                    mCountDownView.refresh(mCurrentPosition);
                }
            }
        };
    }

    private void attachTo(ViewGroup container) {
        RelativeLayout.LayoutParams rl = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        container.addView(this, 0, rl);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        CommonLogUtil.d(TAG, "onSaveInstanceState...");
        Parcelable parcelable = super.onSaveInstanceState();
        SavedState ss = new SavedState(parcelable);
        //Current video state
        ss.savePosition = mCurrentPosition;
        ss.saveVideoPlay25 = mVideoPlay25;
        ss.saveVideoPlay50 = mVideoPlay50;
        ss.saveVideoPlay75 = mVideoPlay75;
        ss.saveIsVideoStart = mIsVideoStart;
        ss.saveIsVideoPlayCompletion = mIsVideoPlayCompletion;
        ss.saveIsMute = mIsMute;

        CommonLogUtil.d(TAG, "onSaveInstanceState..." + ss.print());
        return ss;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        CommonLogUtil.d(TAG, "onRestoreInstanceState...");
        SavedState ss = (SavedState) state;
        CommonLogUtil.d(TAG, "onRestoreInstanceState..." + ss.print());
        super.onRestoreInstanceState(ss.getSuperState());
        mCurrentPosition = ss.savePosition;
        mVideoPlay25 = ss.saveVideoPlay25;
        mVideoPlay50 = ss.saveVideoPlay50;
        mVideoPlay75 = ss.saveVideoPlay75;
        mIsVideoStart = ss.saveIsVideoStart;
        mIsVideoPlayCompletion = ss.saveIsVideoPlayCompletion;
        mIsMute = ss.saveIsMute;

        if (mMediaPlayer != null) {
            mMediaPlayer.setVolume(mIsMute ? 0 : 1, mIsMute ? 0 : 1);
        }

    }

    static class SavedState extends BaseSavedState {

        int savePosition;
        boolean saveVideoPlay25;
        boolean saveVideoPlay50;
        boolean saveVideoPlay75;
        boolean saveIsVideoStart;
        boolean saveIsVideoPlayCompletion;
        boolean saveIsMute;

        public SavedState(Parcel source) {
            super(source);
            savePosition = source.readInt();
            boolean[] booleans = new boolean[6];
            source.readBooleanArray(booleans);
            saveVideoPlay25 = booleans[0];
            saveVideoPlay50 = booleans[1];
            saveVideoPlay75 = booleans[2];
            saveIsVideoStart = booleans[3];
            saveIsVideoPlayCompletion = booleans[4];
            saveIsMute = booleans[5];
        }


        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(savePosition);
            boolean[] booleans = new boolean[6];
            booleans[0] = saveVideoPlay25;
            booleans[1] = saveVideoPlay50;
            booleans[2] = saveVideoPlay75;
            booleans[3] = saveIsVideoStart;
            booleans[4] = saveIsVideoPlayCompletion;
            booleans[5] = saveIsMute;
            out.writeBooleanArray(booleans);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {

            @Override
            public SavedState createFromParcel(Parcel source) {
                return new SavedState(source);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };

        public String print() {
            return "SavedState(\n" +
                    "savePosition - " + savePosition + "\n" +
                    "saveVideoPlay25 - " + saveVideoPlay25 + "\n" +
                    "saveVideoPlay50 - " + saveVideoPlay50 + "\n" +
                    "saveVideoPlay75 - " + saveVideoPlay75 + "\n" +
                    "saveIsVideoStart - " + saveIsVideoStart + "\n" +
                    "saveIsVideoPlayCompletion - " + saveIsVideoPlayCompletion + "\n" +
                    "saveIsMute - " + saveIsMute + "\n)";
        }
    }


    public void setSetting(MyOfferSetting myOfferSetting) {
        if (myOfferSetting == null) {
            return;
        }

        mIsMute = myOfferSetting.getVideoMute() == 0;
        mShowCloseTime = myOfferSetting.getShowCloseTime() * 1000;
        CommonLogUtil.d(TAG, "isMute - " + mIsMute);
        CommonLogUtil.d(TAG, "showCloseTime - " + mShowCloseTime);
    }

    private void init() {
        CommonLogUtil.d(TAG, "init...");
        boolean error = checkValid();
        if (error) {
            if (mListener != null) {
                mListener.onVideoShowFailed(MyOfferErrorCode.get(MyOfferErrorCode.rewardedVideoPlayError, MyOfferErrorCode.fail_video_file_error_));
            }
            return;
        }

        initParams();
        computeVideoSize();

        initTextureView();
        initMediaPlayer();
        initCountDownView();
        initMutebutton();
    }

    private void initParams() {
        mViewSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mViewSizeDp, getContext().getResources().getDisplayMetrics());
        mViewMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mViewMarginDp, getContext().getResources().getDisplayMetrics());
        mLeftMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mLeftMarginDp, getContext().getResources().getDisplayMetrics());
        mTopMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mTopMarginDp, getContext().getResources().getDisplayMetrics());

        mMuteResId = CommonUtil.getResId(getContext(), "myoffer_video_mute", "drawable");
        mNoMuteResId = CommonUtil.getResId(getContext(), "myoffer_video_no_mute", "drawable");
        mCloseResId = CommonUtil.getResId(getContext(), "myoffer_video_close", "drawable");
    }

    private void computeVideoSize() {
        if (mVideoWidth != 0 && mVideoHeight != 0) {
            return;
        }
        try {
            DisplayMetrics dm = getResources().getDisplayMetrics();
            MyOfferVideoUtil.Size videoSize = MyOfferVideoUtil.getAdaptiveVideoSize(mSourceFD, dm.widthPixels, dm.heightPixels);

            if (videoSize != null) {
                mVideoWidth = videoSize.width;
                mVideoHeight = videoSize.height;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void initCountDownView() {
        if (getChildAt(mCountDownViewIndex) != null) {
            removeViewAt(mCountDownViewIndex);
        }

        mCountDownView = new CountDownView(getContext());
        mCountDownView.setId(CommonUtil.getResId(getContext(), "myoffer_count_down_view_id", "id"));
        RelativeLayout.LayoutParams rl = new RelativeLayout.LayoutParams(mViewSize, mViewSize);
        rl.leftMargin = mLeftMargin;
        rl.topMargin = mTopMargin;
        mCountDownView.setVisibility(View.INVISIBLE);
        addView(mCountDownView, mCountDownViewIndex, rl);
    }

    private void initMutebutton() {
        if (getChildAt(mMuteButtonIndex) != null) {
            removeViewAt(mMuteButtonIndex);
        }

        mMuteBtn = new ImageView(getContext());
        mMuteBtn.setId(CommonUtil.getResId(getContext(), "myoffer_btn_mute_id", "id"));
        RelativeLayout.LayoutParams rl = new RelativeLayout.LayoutParams(mViewSize, mViewSize);
        rl.addRule(RelativeLayout.RIGHT_OF, mCountDownView.getId());
        rl.leftMargin = mViewMargin;
        rl.addRule(RelativeLayout.ALIGN_TOP, mCountDownView.getId());
        rl.addRule(RelativeLayout.ALIGN_BOTTOM, mCountDownView.getId());
        mMuteBtn.setVisibility(View.INVISIBLE);
        addView(mMuteBtn, mMuteButtonIndex, rl);

        if (mIsMute) {
            mMuteBtn.setBackgroundResource(mMuteResId);
        } else {
            mMuteBtn.setBackgroundResource(mNoMuteResId);
        }

        mMuteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsVideoPlayCompletion) {
                    return;
                }

                mIsMute = !mIsMute;
                if (mIsMute) {//静音
                    mMuteBtn.setBackgroundResource(mMuteResId);
                    if (mMediaPlayer != null) {
                        mMediaPlayer.setVolume(0f, 0f);
                    }
                } else {
                    mMuteBtn.setBackgroundResource(mNoMuteResId);
                    if (mMediaPlayer != null) {
                        mMediaPlayer.setVolume(1f, 1f);
                    }
                }

            }
        });
    }

    private void initCloseButton() {
        if (getChildAt(mCloseButtonIndex) != null) {
            removeViewAt(mCloseButtonIndex);
        }

        mCloseBtn = new ImageView(getContext());
        mCloseBtn.setId(CommonUtil.getResId(getContext(), "myoffer_btn_close_id", "id"));
        RelativeLayout.LayoutParams rl = new RelativeLayout.LayoutParams(mViewSize, mViewSize);
        rl.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        rl.rightMargin = mViewMargin;
        rl.addRule(RelativeLayout.ALIGN_TOP, mCountDownView.getId());
        rl.addRule(RelativeLayout.ALIGN_BOTTOM, mCountDownView.getId());
        addView(mCloseBtn, mCloseButtonIndex, rl);

        mCloseBtn.setImageResource(mCloseResId);

        ViewUtil.expandTouchArea(mCloseBtn, mViewSize / 2);

        mCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onVideoCloseClick();
                }
            }
        });
    }

    private void showView() {
        showCountDownView();
        showMuteButton();
    }

    private void showCountDownView() {
        if (mCountDownView != null && !mCountDownView.isShown()) {
            mCountDownView.setVisibility(View.VISIBLE);
        }
    }

    private void showMuteButton() {
        if (mMuteBtn != null && !mMuteBtn.isShown()) {
            mMuteBtn.setVisibility(View.VISIBLE);
        }
    }


    private void startProgressThread() {
        if (mProgressThread != null) {
            return;
        }
        mFlag = true;
        mProgressThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (mFlag) {
                    if (!mIsVideoPlayCompletion && mMediaPlayer != null && mMediaPlayer.isPlaying() && mMainHandler != null) {

                        mMainHandler.sendEmptyMessage(mMediaPlayer.getCurrentPosition());
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        mProgressThread.start();
    }

    private void stopProgressThread() {
        mFlag = false;
        mProgressThread = null;
    }

    public void load(String url) {
        this.mSourcePath = url;

        init();
    }

    private boolean checkValid() {
        mFileInputStream = MyOfferResourceManager.getInstance().getInputStream(mSourcePath);
        boolean error = false;

        try {
            if (mFileInputStream == null) {
                error = true;
            } else {
                mSourceFD = mFileInputStream.getFD();
            }

        } catch (Throwable e) {
            e.printStackTrace();
            error = true;
        }
        if (error) {
            if (mFileInputStream != null) {
                try {
                    mFileInputStream.close();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
        return error;
    }


    private void initMediaPlayer() {
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setVolume(mIsMute ? 0 : 1, mIsMute ? 0 : 1);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    CommonLogUtil.d(TAG, "MediaPlayer onPrepared()...");

                    mIsMediaPlayerPrepared = true;
                    mDuration = mMediaPlayer.getDuration();
                    if (mCountDownView != null) {
                        mCountDownView.setDuration(mDuration);
                    }
                    mVideoProgress25 = Math.round(0.25f * mDuration);
                    mVideoProgress50 = Math.round(0.5f * mDuration);
                    mVideoProgress75 = Math.round(0.75f * mDuration);


                    if (mCurrentPosition > 0) {
                        mMediaPlayer.seekTo(mCurrentPosition);
                    } else {

                        start();
                    }
                }
            });

            mMediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                @Override
                public void onSeekComplete(MediaPlayer mp) {
                    start();
                }
            });

            if (!mIsVideoPlayCompletion) {
                mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        stopProgressThread();
                        mIsVideoPlayCompletion = true;
                        mCurrentPosition = mDuration;

                        if (mListener != null) {
                            mListener.onVideoPlayCompletion();
                        }
                    }
                });
            }

            mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    if (mListener != null) {
                        mListener.onVideoShowFailed(MyOfferErrorCode.get(MyOfferErrorCode.rewardedVideoPlayError, MyOfferErrorCode.fail_player));
                    }
                    return true;//false will call OnCompletionListener
                }
            });

        }
    }

    private void initTextureView() {
        if (mTextureView == null) {
            mTextureView = new TextureView(getContext());
            mTextureView.setSurfaceTextureListener(this);
            mTextureView.setKeepScreenOn(true);

            RelativeLayout.LayoutParams rl = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            if (mVideoWidth != 0 && mVideoHeight != 0) {
                rl.width = mVideoWidth;
                rl.height = mVideoHeight;
            }
            rl.addRule(RelativeLayout.CENTER_IN_PARENT);
            removeAllViews();
            addView(mTextureView, rl);

            mTextureView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onVideoClick();
                    }
                }
            });
        }
    }


    private void openPlayer() {
        init();
        try {
            mMediaPlayer.reset();

            if (!mSourceFD.valid()) {
                throw new IllegalStateException("MyOffer video resource is valid");
            } else {
                CommonLogUtil.d(TAG, "video resource valid - " + mSourceFD.valid());
            }

            mMediaPlayer.setDataSource(this.mSourceFD);
            try {
                if (mFileInputStream != null) {
                    mFileInputStream.close();
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
            if (mSurface == null) {
                mSurface = new Surface(mSurfaceTexture);
            }
            mMediaPlayer.setSurface(mSurface);
            mMediaPlayer.prepareAsync();

        } catch (Throwable e) {
            e.printStackTrace();
            if (mListener != null) {
                mListener.onVideoShowFailed(MyOfferErrorCode.get(MyOfferErrorCode.rewardedVideoPlayError, e.getMessage()));
            }
        }
    }

    public void showCloseButton() {
        initCloseButton();
    }


    public void start() {
        CommonLogUtil.d(TAG, "start()");
        if (mMediaPlayer != null && mIsMediaPlayerPrepared) {
            mMediaPlayer.start();
        }
        startProgressThread();
    }

    public void pause() {
        CommonLogUtil.d(TAG, "pause()");
        stopProgressThread();
        if (isPlaying()) {
            mMediaPlayer.pause();
        }
    }

    public void stop() {
        CommonLogUtil.d(TAG, "stop()");
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
        }

        if (mListener != null) {
            mListener.onVideoPlayEnd();
        }
    }

    public void release() {
        if (!mIsMediaPlayerPrepared) {
            return;
        }
        CommonLogUtil.d(TAG, "release...");
        stopProgressThread();
        mSurfaceTexture = null;
        mSurface = null;
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        if (mMainHandler != null) {
            mMainHandler.removeCallbacksAndMessages(null);
        }
        mIsMediaPlayerPrepared = false;
    }

    public boolean isPlaying() {
        if (mMediaPlayer != null && mIsMediaPlayerPrepared) {
            return mMediaPlayer.isPlaying();
        }
        return false;
    }


    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        CommonLogUtil.d(TAG, "onSurfaceTextureAvailable()...");
        mSurfaceTexture = surface;
        openPlayer();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        CommonLogUtil.d(TAG, "onSurfaceTextureDestroyed()...");
        this.release();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        CommonLogUtil.d(TAG, "onDetachedFromWindow()...");
        this.release();
    }

    public interface OnPlayerListener {
        void onVideoPlayStart();

        void onVideoUpdateProgress(int progress);

        void onVideoPlayEnd();

        void onVideoPlayCompletion();

        void onVideoShowFailed(MyOfferError error);

        void onVideoPlayProgress(int progressArea);

        void onVideoCloseClick();

        void onVideoClick();
    }
}
