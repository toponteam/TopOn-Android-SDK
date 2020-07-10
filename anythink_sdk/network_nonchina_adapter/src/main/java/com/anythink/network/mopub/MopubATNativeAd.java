package com.anythink.network.mopub;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.anythink.core.api.AdError;
import com.anythink.core.api.ErrorCode;
import com.anythink.nativead.unitgroup.api.CustomNativeAd;
import com.mopub.nativeads.BaseNativeAd;
import com.mopub.nativeads.MediaLayout;
import com.mopub.nativeads.MoPubAdRenderer;
import com.mopub.nativeads.MoPubNative;
import com.mopub.nativeads.NativeAd;
import com.mopub.nativeads.NativeErrorCode;
import com.mopub.nativeads.RequestParameters;
import com.mopub.nativeads.StaticNativeAd;
import com.mopub.nativeads.VideoNativeAd;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;

/**
 * Created by Z on 2018/3/13.
 */

public class MopubATNativeAd extends CustomNativeAd implements MoPubNative.MoPubNativeNetworkListener {
    private final String TAG = MopubATNativeAd.class.getSimpleName();
    Context mContext;
    LoadCallbackListener mCustonNativeListener;

    MoPubNative mMoPubNative;
    RequestParameters mRequestParameters;

    NativeAd mNativeAd;

    public MopubATNativeAd(Context context
            , LoadCallbackListener customNativeListener
            , String unitId
            , Map<String, Object> localExtras) {
        mContext = context.getApplicationContext();
        mCustonNativeListener = customNativeListener;
        mMoPubNative = new MoPubNative(context, unitId, this);
        final EnumSet<RequestParameters.NativeAdAsset> desiredAssets = EnumSet.of(
                RequestParameters.NativeAdAsset.TITLE,
                RequestParameters.NativeAdAsset.TEXT,
                RequestParameters.NativeAdAsset.ICON_IMAGE,
                RequestParameters.NativeAdAsset.MAIN_IMAGE,
                RequestParameters.NativeAdAsset.STAR_RATING,
                RequestParameters.NativeAdAsset.CALL_TO_ACTION_TEXT);

        mRequestParameters = new RequestParameters.Builder()
                .desiredAssets(desiredAssets)
                .build();

        mMoPubNative.registerAdRenderer(new MoPubAdRenderer() {
            @Override
            public View createAdView( Context context,  ViewGroup parent) {
                return null;
            }

            @Override
            public void renderAdView( View view,  BaseNativeAd ad) {

            }

            @Override
            public boolean supports( BaseNativeAd nativeAd) {
                return true;
            }
        });

    }

    public void loadAd() {
        log(TAG, "loadAd");
        mMoPubNative.makeRequest(mRequestParameters);
    }

    // Lifecycle Handlers
    @Override
    public void prepare(final View view, FrameLayout.LayoutParams layoutParams) {
        if (view == null) {
            return;
        }
        log(TAG, "prepare");
        if (mNativeAd != null) {
            mNativeAd.prepare(view);
        }
    }

    @Override
    public void prepare(View view, List<View> clickViewList, FrameLayout.LayoutParams layoutParams) {
        prepare(view, layoutParams);
    }

    @Override
    public void clear(final View view) {
        log(TAG, "clear");
        if (mNativeAd != null) {
            mNativeAd.clear(view);
        }
    }


    @Override
    public View getAdMediaView(Object... object) {
        if (mNativeAd != null && mNativeAd.getBaseNativeAd() instanceof VideoNativeAd) {
            MediaLayout mMediaView = new MediaLayout(mContext);
            ((VideoNativeAd) mNativeAd.getBaseNativeAd()).render(mMediaView); //渲染
            return mMediaView;
        }
        return null;
    }

    @Override
    public void destroy() {
        log(TAG, "destory");
        if (mNativeAd != null) {
            mNativeAd.destroy();
        }
    }

    boolean mIsAutoPlay;

    public void setIsAutoPlay(boolean isAutoPlay) {
        mIsAutoPlay = isAutoPlay;
    }

    @Override
    public void onNativeLoad(NativeAd nativeAd) {
        log(TAG, "onAdloaded");
        mNativeAd = nativeAd;
        if (nativeAd.getBaseNativeAd() instanceof StaticNativeAd) {
            StaticNativeAd staticNativeAd = (StaticNativeAd) nativeAd.getBaseNativeAd();
            setTitle(staticNativeAd.getTitle());
            setDescriptionText(staticNativeAd.getText());
            setIconImageUrl(staticNativeAd.getIconImageUrl());
            setMainImageUrl(staticNativeAd.getMainImageUrl());
            setStarRating(staticNativeAd.getStarRating() == null ? 0 : staticNativeAd.getStarRating());
            setCallToActionText(staticNativeAd.getCallToAction());
            mAdSourceType = IMAGE_TYPE;
        }

        if (nativeAd.getBaseNativeAd() instanceof VideoNativeAd) {
            VideoNativeAd videoNativeAd = (VideoNativeAd) nativeAd.getBaseNativeAd();
            setTitle(videoNativeAd.getTitle());
            setDescriptionText(videoNativeAd.getText());
            setIconImageUrl(videoNativeAd.getIconImageUrl());
            setMainImageUrl(videoNativeAd.getMainImageUrl());
            setVideoUrl(videoNativeAd.getVastVideo());
            setCallToActionText(videoNativeAd.getCallToAction());

            mAdSourceType = VIDEO_TYPE;
        }
        mNativeAd.setMoPubNativeEventListener(new NativeAd.MoPubNativeEventListener() {
            @Override
            public void onImpression(View view) {

            }

            @Override
            public void onClick(View view) {
                notifyAdClicked();
            }
        });

        Log.e(TAG, "onNativeLoad success");

        if (mCustonNativeListener != null) {
            mCustonNativeListener.onSuccess(this);
        }

    }

    @Override
    public void onNativeFail(NativeErrorCode errorCode) {
        log(TAG, "onloadFail:" + errorCode.toString());
        if (mCustonNativeListener != null) {
            AdError adUpError = ErrorCode.getErrorCode(ErrorCode.noADError, errorCode.name() + "", errorCode.toString());
            mCustonNativeListener.onFail(adUpError);
        }

    }

    interface LoadCallbackListener {
        public void onSuccess(CustomNativeAd customNativeAd);
        public void onFail(AdError adError);
    }
}
