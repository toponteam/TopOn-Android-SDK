package com.anythink.nativead.splash.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.anythink.core.common.res.ImageLoader;
import com.anythink.core.common.res.ResourceEntry;
import com.anythink.core.common.utils.BitmapUtil;
import com.anythink.core.common.utils.CommonUtil;
import com.anythink.nativead.api.ATNativeAdRenderer;
import com.anythink.nativead.unitgroup.api.CustomNativeAd;
import com.anythink.nativead.views.AppRatingView;
import com.anythink.nativead.views.RoundImageView;

/**
 * Created by Z on 2018/1/18.
 */

public class ATSplashRender implements ATNativeAdRenderer<CustomNativeAd> {

    private Context mContext;

    public ATSplashRender(Context context) {
        mContext = context.getApplicationContext();
    }

    View mDevelopView;

    int mNetworkType;


    boolean logonImageFinish = false;
    boolean mainImageFinish = false;


    @Override
    public View createView(Context context, int networkType) {
        if (mDevelopView == null) {
            mDevelopView = LayoutInflater.from(context).inflate(CommonUtil.getResId(context, "plugin_splash_ad_layout", "layout"), null);
        }
        mNetworkType = networkType;
        return mDevelopView;
    }

    @Override
    public void renderAdView(final View view, CustomNativeAd ad) {
        final Context context = view.getContext();
        TextView titleView = (TextView) view.findViewById(CommonUtil.getResId(context, "plugin_splash_ad_title", "id"));
        TextView ctaView = (TextView) view.findViewById(CommonUtil.getResId(context, "plugin_splash_ad_install_btn", "id"));
        TextView descView = (TextView) view.findViewById(CommonUtil.getResId(context, "plugin_splash_desc", "id"));
        TextView adFromView = (TextView) view.findViewById(CommonUtil.getResId(context, "plugin_splash_ad_from", "id"));
        TextView selfAdLogo = (TextView) view.findViewById(CommonUtil.getResId(context, "plugin_splash_self_ad_logo", "id"));

        FrameLayout contentArea = (FrameLayout) view.findViewById(CommonUtil.getResId(context, "plugin_splash_ad_content_image_area", "id"));
        FrameLayout expressArea = (FrameLayout) view.findViewById(CommonUtil.getResId(context, "plugin_splash_ad_express_area", "id"));
        View mediaView = ad.getAdMediaView(contentArea, contentArea.getWidth());

        final RoundImageView bgView = (RoundImageView) view.findViewById(CommonUtil.getResId(context, "plugin_splash_bg", "id"));


        final AppRatingView appRatingView = (AppRatingView) view.findViewById(CommonUtil.getResId(context, "plugin_rating_view", "id"));
        appRatingView.setStarNum(5);
        appRatingView.setRating(ad.getStarRating() == 0 ? 5 : ad.getStarRating().intValue());

        final RoundImageView logoView = (RoundImageView) view.findViewById(CommonUtil.getResId(context, "plugin_splash_ad_logo", "id"));
        if (!TextUtils.isEmpty(ad.getAdChoiceIconUrl())) {
            logoView.setVisibility(View.VISIBLE);
            int logoSize = logoView.getLayoutParams().width;
            ImageLoader.getInstance(mContext).load(new ResourceEntry(ResourceEntry.CUSTOM_IMAGE_CACHE_TYPE, ad.getAdChoiceIconUrl()), logoSize, logoSize, new ImageLoader.ImageLoaderListener() {
                @Override
                public void onSuccess(String key, Bitmap bitmap) {
                    logoView.setImageBitmap(bitmap);
                    logonImageFinish = true;
                    imageFinish();
                }

                @Override
                public void onFail(String key, String errorMsg) {
                    logonImageFinish = true;
                    imageFinish();
                }
            });
        } else {
            logonImageFinish = true;
            logoView.setVisibility(View.GONE);
            imageFinish();
        }


        contentArea.removeAllViews();
        contentArea.setVisibility(View.GONE);
        expressArea.setVisibility(View.GONE);
        int bigSize = contentArea.getLayoutParams().width;

        selfAdLogo.setVisibility(View.VISIBLE);
        if (mediaView != null) {
            if (ad.isNativeExpress()) {// 个性化模板
                selfAdLogo.setVisibility(View.GONE);
                titleView.setVisibility(View.GONE);
                ctaView.setVisibility(View.GONE);
                if (descView != null) {
                    descView.setVisibility(View.GONE);
                }
                appRatingView.setVisibility(View.GONE);
                logoView.setVisibility(View.GONE);
                if (adFromView != null) {
                    adFromView.setVisibility(View.GONE);
                }

                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                lp.gravity = Gravity.CENTER;
                expressArea.addView(mediaView, lp);
                expressArea.setVisibility(View.VISIBLE);
            } else {
                contentArea.addView(mediaView, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
                contentArea.setVisibility(View.VISIBLE);
            }
            mainImageFinish = true;
            Bitmap defaultBg = BitmapFactory.decodeResource(context.getResources(), CommonUtil.getResId(context, "plugin_splash_default_bg", "drawable"));
            bgView.setImageBitmap(BitmapUtil.blurBitmap(context, defaultBg));
            if (defaultBg != null) {
                defaultBg.recycle();
            }
            imageFinish();
        } else {
            final RoundImageView imageView = new RoundImageView(context);
            ViewGroup.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
            imageView.setLayoutParams(params);
            imageView.setNeedRadiu(true);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            contentArea.addView(imageView, params);
            contentArea.setVisibility(View.VISIBLE);

            ImageLoader.getInstance(mContext).load(new ResourceEntry(ResourceEntry.CUSTOM_IMAGE_CACHE_TYPE, ad.getMainImageUrl()), bigSize, bigSize, new ImageLoader.ImageLoaderListener() {
                @Override
                public void onSuccess(String key,Bitmap bitmap) {
                    imageView.setImageBitmap(bitmap);
                    Bitmap blurBitmap = BitmapUtil.blurBitmap(context, bitmap);
                    bgView.setImageBitmap(blurBitmap);
                    mainImageFinish = true;
                    imageFinish();
                }

                @Override
                public void onFail(String key, String errorMsg) {
                    Bitmap defaultBg = BitmapFactory.decodeResource(context.getResources(), CommonUtil.getResId(context, "plugin_splash_default_bg", "drawable"));
                    bgView.setImageBitmap(BitmapUtil.blurBitmap(context, defaultBg));
                    if (defaultBg != null) {
                        defaultBg.recycle();
                    }
                    mainImageFinish = true;
                    imageFinish();
                }
            });
        }


        if (!TextUtils.isEmpty(ad.getTitle())) {
            titleView.setText(ad.getTitle());
            titleView.setVisibility(View.VISIBLE);
        } else {
            titleView.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(ad.getCallToActionText())) {
            ctaView.setText(ad.getCallToActionText());
            ctaView.setVisibility(View.VISIBLE);
        } else {
            ctaView.setVisibility(View.GONE);
        }

        if (descView != null) {
            if (!TextUtils.isEmpty(ad.getDescriptionText())) {
                descView.setText(ad.getDescriptionText());
                descView.setVisibility(View.VISIBLE);
            } else {
                descView.setVisibility(View.GONE);
            }
        }

        if (adFromView != null) {
            if (!TextUtils.isEmpty(ad.getAdFrom())) {
                adFromView.setText(ad.getAdFrom());
                adFromView.setVisibility(View.VISIBLE);
            } else {
                adFromView.setVisibility(View.GONE);
            }
        }
    }


    private void imageFinish() {
        if (logonImageFinish && mainImageFinish) {
            if (mCallback != null) {
                mCallback.finish();
            }
        }
    }

    ImageFinishCallback mCallback;

    public void setCallback(ImageFinishCallback callback) {
        mCallback = callback;
    }

    public interface ImageFinishCallback {
        public void finish();
    }
}
