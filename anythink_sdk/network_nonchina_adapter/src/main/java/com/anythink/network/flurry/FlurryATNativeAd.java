package com.anythink.network.flurry;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

import com.anythink.core.api.AdError;
import com.anythink.core.api.ErrorCode;
import com.anythink.nativead.unitgroup.api.CustomNativeAd;
import com.flurry.android.FlurryAgent;
import com.flurry.android.ads.FlurryAdErrorType;
import com.flurry.android.ads.FlurryAdNative;
import com.flurry.android.ads.FlurryAdNativeListener;

import java.util.List;
import java.util.Map;

/**
 * Created by Z on 2018/1/12.
 */

public class FlurryATNativeAd extends CustomNativeAd implements FlurryAdNativeListener {
    public static final String TAG = FlurryATNativeAd.class.getSimpleName();
    private FlurryAdNative mFlurryAdNative;
    // Assets documented here: https://developer.yahoo.com/flurry/docs/publisher/code/android/
    private static final String AD_ASSET_SUMMARY = "summary";
    private static final String AD_ASSET_HEADLINE = "headline";
    private static final String AD_ASSET_SOURCE = "source";
    private static final String AD_ASSET_SEC_HQ_BRANDING_LOGO = "secHqBrandingLogo";
    private static final String AD_ASSET_SEC_HQ_RATING_IMAGE = "secHqRatingImg";
    private static final String AD_ASSET_SHOW_RATING = "showRating";
    private static final String AD_ASSET_SEC_HQ_IMAGE = "secHqImage";
    private static final String AD_ASSET_SEC_IMAGE = "secImage";
    private static final String AD_ASSET_VIDEO_URL = "videoUrl";
    private static final String AD_ASSET_CALL_TO_ACTION = "callToAction";

    Context mContext;
    LoadCallbackListener mCustonNativeListener;
    String mAdSpace;

    public FlurryATNativeAd(Context context
            , LoadCallbackListener customNativeListener
            , String adSpace
            , Map<String, Object> localExtras) {
        mContext = context.getApplicationContext();
        mCustonNativeListener = customNativeListener;
        mAdSpace = adSpace;
        mFlurryAdNative = new FlurryAdNative(context, adSpace);
        mFlurryAdNative.setListener(this);

//                FlurryAdTargeting adTargeting = new FlurryAdTargeting();
//                //do not release the app with the test mode enabled
//                adTargeting.setEnableTestAds(true);
//                mFlurryAdNative.setTargeting(adTargeting);

    }

    public void loadAd() {
        log(TAG, "loadAd");
        if (mContext != null) {
            FlurryAgent.onStartSession(mContext.getApplicationContext());
        }
        mFlurryAdNative.fetchAd();

    }

    // Lifecycle Handlers
    @Override
    public void prepare(final View view, FrameLayout.LayoutParams layoutParams) {
        if (view == null) {
            return;
        }
        log(TAG, "prepare");
        if (mFlurryAdNative != null) {
            mFlurryAdNative.setTrackingView(view);
        }
    }

    @Override
    public void prepare(View view, List<View> clickViewList, FrameLayout.LayoutParams layoutParams) {
        prepare(view, layoutParams);
    }

    @Override
    public void clear(final View view) {
        log(TAG, "clear");
        if (mFlurryAdNative != null) {
            mFlurryAdNative.removeTrackingView();
        }

    }


    @Override
    public View getAdMediaView(Object... object) {
        try {
            if (mFlurryAdNative != null && mFlurryAdNative.isVideoAd()) {
                FrameLayout mMediaView = new FrameLayout(mContext);
                mFlurryAdNative.getAsset(AD_ASSET_VIDEO_URL).loadAssetIntoView(mMediaView);
                return mMediaView;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void destroy() {
        log(TAG, "destory");
        if (mFlurryAdNative != null) {
            mFlurryAdNative.destroy();
        }
    }

    /**
     * flurry listener--------------------------------------------------------------------------------
     **/


    @Override
    public void onFetched(FlurryAdNative flurryAdNative) {
        log(TAG, "FlurryAdNative.......(" + (flurryAdNative == null) + "");

        if (mContext != null) {
            FlurryAgent.onEndSession(mContext.getApplicationContext());
        }
        mFlurryAdNative = flurryAdNative;

        setTitle(mFlurryAdNative.getAsset(AD_ASSET_HEADLINE).getValue());
        setDescriptionText(mFlurryAdNative.getAsset(AD_ASSET_SUMMARY).getValue());
        setAdChoiceIconUrl(mFlurryAdNative.getAsset(AD_ASSET_SEC_HQ_BRANDING_LOGO).getValue());

        setAdFrom(mFlurryAdNative.getAsset(AD_ASSET_SOURCE).getValue());

        if (mFlurryAdNative.getAsset(AD_ASSET_SEC_HQ_IMAGE) != null) {
            setMainImageUrl(mFlurryAdNative.getAsset(AD_ASSET_SEC_HQ_IMAGE).getValue());
        } else if (mFlurryAdNative.getAsset(AD_ASSET_SEC_IMAGE) != null) {
            setMainImageUrl(mFlurryAdNative.getAsset(AD_ASSET_SEC_IMAGE).getValue());
        }

        if (mFlurryAdNative.isVideoAd()) {
            mAdSourceType = VIDEO_TYPE;
            setVideoUrl(mFlurryAdNative.getAsset(AD_ASSET_VIDEO_URL).getValue());
        } else {
            mAdSourceType = IMAGE_TYPE;
        }

        setCallToActionText(mFlurryAdNative.getAsset(AD_ASSET_CALL_TO_ACTION).getValue());

        mCustonNativeListener.onSuccess(this);
    }

    @Override
    public void onShowFullscreen(FlurryAdNative flurryAdNative) {
        log(TAG, "onShowFullscreen-------------");
    }

    @Override
    public void onCloseFullscreen(FlurryAdNative flurryAdNative) {
        log(TAG, "onCloseFullscreen-------------");
    }

    @Override
    public void onAppExit(FlurryAdNative flurryAdNative) {
        log(TAG, "onAppExit-------------");
    }

    @Override
    public void onClicked(FlurryAdNative flurryAdNative) {
        log(TAG, "onClicked-------------");
        notifyAdClicked();
    }

    @Override
    public void onImpressionLogged(FlurryAdNative flurryAdNative) {
        log(TAG, "onImpressionLogged-------------");
    }

    @Override
    public void onExpanded(FlurryAdNative flurryAdNative) {
        log(TAG, "onExpanded-------------");
    }

    @Override
    public void onCollapsed(FlurryAdNative flurryAdNative) {
        log(TAG, "onCollapsed-------------");
    }

    @Override
    public void onError(FlurryAdNative flurryAdNative, FlurryAdErrorType flurryAdErrorType, int i) {
        log(TAG, "onError:code:" + i + "," + flurryAdErrorType);
        if (mContext != null) {
            FlurryAgent.onEndSession(mContext.getApplicationContext());
        }
        AdError adError = ErrorCode.getErrorCode(ErrorCode.noADError, i + "", flurryAdErrorType.name());
        mCustonNativeListener.onFail(adError);
    }


    boolean mIsAutoPlay;

    public void setIsAutoPlay(boolean isAutoPlay) {
        mIsAutoPlay = isAutoPlay;
    }


    interface LoadCallbackListener {
        public void onSuccess(CustomNativeAd customNativeAd);

        public void onFail(AdError adError);
    }
}
