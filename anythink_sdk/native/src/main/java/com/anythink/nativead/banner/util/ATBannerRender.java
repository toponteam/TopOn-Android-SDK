package com.anythink.nativead.banner.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.anythink.core.common.utils.CommonUtil;
import com.anythink.nativead.api.ATNativeAdRenderer;
import com.anythink.nativead.banner.api.ATNativeBannerConfig;
import com.anythink.nativead.banner.api.ATNativeBannerSize;
import com.anythink.nativead.bussiness.CommonImageLoader;
import com.anythink.nativead.unitgroup.api.CustomNativeAd;
import com.anythink.nativead.views.RoundImageView;

/**
 * Created by Z on 2018/1/18.
 */

public class ATBannerRender implements ATNativeAdRenderer<CustomNativeAd> {

    Context mContext;
    ATNativeBannerConfig config;

    public ATBannerRender(Context context, ATNativeBannerConfig config) {
        mContext = context;
        this.config = config;
    }

    public void setConfig(ATNativeBannerConfig config) {
        this.config = config;
    }

    public ATNativeBannerSize getBannerSize() {
        return config.bannerSize;
    }

    int mNetworkType;


    @Override
    public View createView(Context context, int networkType) {
        View developView = null;
        if (config.bannerSize == ATNativeBannerSize.BANNER_SIZE_320x50) {
            developView = LayoutInflater.from(context).inflate(CommonUtil.getResId(mContext, "plugin_banner_320x50", "layout"), null);
        }
        if (config.bannerSize == ATNativeBannerSize.BANNER_SIZE_640x150) {
            developView = LayoutInflater.from(context).inflate(CommonUtil.getResId(mContext, "plugin_banner_640x150", "layout"), null);
        }

        if (config.bannerSize == ATNativeBannerSize.BANNER_SIZE_AUTO) {
            developView = LayoutInflater.from(context).inflate(CommonUtil.getResId(mContext, "plugin_banner_auto", "layout"), null);
        }
        //兜底320x50
        if (developView == null) {
            config.bannerSize = ATNativeBannerSize.BANNER_SIZE_AUTO;
            developView = LayoutInflater.from(context).inflate(CommonUtil.getResId(mContext, "plugin_banner_auto", "layout"), null);
        }
        mNetworkType = networkType;
        developView.setVisibility(View.GONE);
        return developView;
    }

    @Override
    public void renderAdView(final View view, CustomNativeAd ad) {
        if(view instanceof ViewGroup) {
            ViewGroup develop = (ViewGroup) view;

            if (develop.getChildCount() > 5) {
                for (int i = develop.getChildCount(); i >= 5; i++) {
                    develop.removeViewAt(i);
                }
            }
            if(ad.getAdMediaView() != null && ad.isNativeExpress()) {
                for (int i = 0; i < 5; i++) {
                    develop.getChildAt(i).setVisibility(View.GONE);
                }
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                lp.addRule(RelativeLayout.CENTER_IN_PARENT);
                develop.addView(ad.getAdMediaView(), lp);
                view.setVisibility(View.VISIBLE);
                return;
            }
        }

        if (config.bannerSize == ATNativeBannerSize.BANNER_SIZE_320x50) {
            final RoundImageView roundImageView = (RoundImageView) view.findViewById(CommonUtil.getResId(mContext, "plugin_320_banner_icon", "id"));
            roundImageView.setNeedRadiu(true);
            TextView ctaTextView = (TextView) view.findViewById(CommonUtil.getResId(mContext, "plugin_320_banner_cta", "id"));
            TextView titleTextView = (TextView) view.findViewById(CommonUtil.getResId(mContext, "plugin_320_banner_title", "id"));
            TextView descTextView = (TextView) view.findViewById(CommonUtil.getResId(mContext, "plugin_320_banner_desc", "id"));
            TextView adFromTextView = (TextView) view.findViewById(CommonUtil.getResId(mContext, "plugin_320_banner_adfrom_view", "id"));
            final RoundImageView adChoiceImageView = (RoundImageView) view.findViewById(CommonUtil.getResId(mContext, "plugin_320_banner_adchoice_icon", "id"));


            CommonImageLoader.getInstance().startLoadImage(ad.getIconImageUrl(), dip2px(mContext, 40), new CommonImageLoader.ImageCallback() {
                @Override
                public void onSuccess(Bitmap bitmap, String url) {
                    roundImageView.setVisibility(View.VISIBLE);
                    roundImageView.setImageBitmap(bitmap);
                }

                @Override
                public void onFail() {
                    roundImageView.setVisibility(View.GONE);
                }
            });

            if (!TextUtils.isEmpty(ad.getAdFrom())) {
                adFromTextView.setText(ad.getAdFrom());
            } else {
                adFromTextView.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(ad.getCallToActionText()) && config.isCtaBtnShow) {
                ctaTextView.setText(ad.getCallToActionText());
                ctaTextView.setVisibility(View.VISIBLE);
            } else {
                ctaTextView.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(ad.getTitle())) {
                titleTextView.setText(ad.getTitle());
                titleTextView.setVisibility(View.VISIBLE);
                titleTextView.setSelected(true);
            } else {
                titleTextView.setVisibility(View.GONE);
            }


            if (!TextUtils.isEmpty(ad.getDescriptionText())) {
                descTextView.setText(ad.getDescriptionText());
                descTextView.setVisibility(View.VISIBLE);
                descTextView.setSelected(true);
            } else {
                descTextView.setVisibility(View.GONE);
            }


            titleTextView.setTextColor(config.titleColor);
            descTextView.setTextColor(config.descColor);

            GradientDrawable gradientDrawable = new GradientDrawable();
            gradientDrawable.setCornerRadius(dip2px(mContext, 20));
            gradientDrawable.setColor(config.ctaBgColor);
            ctaTextView.setTextColor(config.ctaColor);
            ctaTextView.setBackgroundDrawable(gradientDrawable);

            CommonImageLoader.getInstance().startLoadImage(ad.getAdChoiceIconUrl(), dip2px(mContext, 10), new CommonImageLoader.ImageCallback() {
                @Override
                public void onSuccess(Bitmap bitmap, String url) {
                    adChoiceImageView.setImageBitmap(bitmap);
                    adChoiceImageView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onFail() {
                    adChoiceImageView.setVisibility(View.GONE);
                }
            });

        }

        if (config.bannerSize == ATNativeBannerSize.BANNER_SIZE_640x150) {
            final FrameLayout frameLayout = view.findViewById(CommonUtil.getResId(mContext, "plugin_640_image_area", "id"));
            View mediaView = ad.getAdMediaView(frameLayout, frameLayout.getWidth());
            if (mediaView != null) {
                ViewParent viewParent = mediaView.getParent();
                if (viewParent instanceof ViewGroup) {
                    ((ViewGroup) viewParent).removeView(mediaView);
                }
                frameLayout.addView(mediaView);
            } else {
                final RoundImageView roundImageView = new RoundImageView(mContext);
                roundImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                frameLayout.addView(roundImageView);
                CommonImageLoader.getInstance().startLoadImage(ad.getMainImageUrl(), dip2px(mContext, 300), new CommonImageLoader.ImageCallback() {
                    @Override
                    public void onSuccess(Bitmap bitmap, String url) {
                        frameLayout.setVisibility(View.VISIBLE);
                        roundImageView.setImageBitmap(bitmap);
                    }

                    @Override
                    public void onFail() {
                        frameLayout.setVisibility(View.GONE);
                    }
                });
            }

            TextView ctaTextView = (TextView) view.findViewById(CommonUtil.getResId(mContext, "plugin_640_banner_cta", "id"));
            TextView titleTextView = (TextView) view.findViewById(CommonUtil.getResId(mContext, "plugin_640_banner_title", "id"));
            TextView descTextView = (TextView) view.findViewById(CommonUtil.getResId(mContext, "plugin_640_banner_desc", "id"));
            TextView fromTextView = (TextView) view.findViewById(CommonUtil.getResId(mContext, "plugin_640_banner_from", "id"));
            TextView adFromTextView = (TextView) view.findViewById(CommonUtil.getResId(mContext, "plugin_640_banner_adfrom_view", "id"));
            final RoundImageView adChoiceImageView = (RoundImageView) view.findViewById(CommonUtil.getResId(mContext, "plugin_640_banner_adchoice_icon", "id"));

            if (!TextUtils.isEmpty(ad.getAdFrom())) {
                adFromTextView.setText(ad.getAdFrom());
            } else {
                adFromTextView.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(ad.getCallToActionText()) && config.isCtaBtnShow) {
                ctaTextView.setText(ad.getCallToActionText());
                ctaTextView.setVisibility(View.VISIBLE);
            } else {
                ctaTextView.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(ad.getTitle())) {
                titleTextView.setText(ad.getTitle());
                titleTextView.setVisibility(View.VISIBLE);
                titleTextView.setSelected(true);
            } else {
                titleTextView.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(ad.getDescriptionText())) {
                descTextView.setText(ad.getDescriptionText());
                descTextView.setVisibility(View.VISIBLE);
                descTextView.setSelected(true);
            } else {
                descTextView.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(ad.getAdFrom())) {
                fromTextView.setText(ad.getAdFrom());
                fromTextView.setVisibility(View.VISIBLE);
            } else {
                fromTextView.setVisibility(View.GONE);
            }

            CommonImageLoader.getInstance().startLoadImage(ad.getAdChoiceIconUrl(), dip2px(mContext, 10), new CommonImageLoader.ImageCallback() {
                @Override
                public void onSuccess(Bitmap bitmap, String url) {
                    adChoiceImageView.setImageBitmap(bitmap);
                    adChoiceImageView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onFail() {
                    adChoiceImageView.setVisibility(View.GONE);
                }
            });

            titleTextView.setTextColor(config.titleColor);
            descTextView.setTextColor(config.descColor);

            GradientDrawable gradientDrawable = new GradientDrawable();
            gradientDrawable.setCornerRadius(dip2px(mContext, 20));
            gradientDrawable.setColor(config.ctaBgColor);
            ctaTextView.setTextColor(config.ctaColor);
            ctaTextView.setBackgroundDrawable(gradientDrawable);
        }


        if (config.bannerSize == ATNativeBannerSize.BANNER_SIZE_AUTO) {
            final RoundImageView roundImageView = (RoundImageView) view.findViewById(CommonUtil.getResId(mContext, "plugin_auto_banner_icon", "id"));
            roundImageView.setNeedRadiu(true);
            final TextView titleTextView = (TextView) view.findViewById(CommonUtil.getResId(mContext, "plugin_auto_banner_title", "id"));
            final TextView descTextView = (TextView) view.findViewById(CommonUtil.getResId(mContext, "plugin_auto_banner_desc", "id"));
            final TextView ctaTextView = (TextView) view.findViewById(CommonUtil.getResId(mContext, "plugin_auto_banner_cta", "id"));
            final RoundImageView adChoiceImageView = (RoundImageView) view.findViewById(CommonUtil.getResId(mContext, "plugin_auto_banner_adchoice_icon", "id"));
            TextView adFromTextView = (TextView) view.findViewById(CommonUtil.getResId(mContext, "plugin_auto_banner_adfrom_view", "id"));

            final int parentWidth = dip2px(mContext, 320);
            final int parentHeight = dip2px(mContext, 50);
            final int iconHeight = dip2px(mContext, 40);

            final int titleSize = dip2px(mContext, 15);
            final int descSize = dip2px(mContext, 12);
            final int ctaSize = dip2px(mContext, 13);

            CommonImageLoader.getInstance().startLoadImage(ad.getIconImageUrl(), dip2px(mContext, 40), new CommonImageLoader.ImageCallback() {
                @Override
                public void onSuccess(Bitmap bitmap, String url) {
                    roundImageView.setVisibility(View.VISIBLE);
                    roundImageView.setImageBitmap(bitmap);
                }

                @Override
                public void onFail() {
                    roundImageView.setVisibility(View.GONE);
                }
            });

            if (!TextUtils.isEmpty(ad.getCallToActionText()) && config.isCtaBtnShow) {
                ctaTextView.setText(ad.getCallToActionText());
                ctaTextView.setVisibility(View.VISIBLE);
            } else {
                ctaTextView.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(ad.getAdFrom())) {
                adFromTextView.setText(ad.getAdFrom());
            } else {
                adFromTextView.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(ad.getTitle())) {
                titleTextView.setText(ad.getTitle());
                titleTextView.setVisibility(View.VISIBLE);
                titleTextView.setSelected(true);
            } else {
                titleTextView.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(ad.getDescriptionText())) {
                descTextView.setText(ad.getDescriptionText());
                descTextView.setVisibility(View.VISIBLE);
                descTextView.setSelected(true);
            } else {
                descTextView.setVisibility(View.GONE);
            }

            final ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    int width = view.getMeasuredWidth();
                    int height = view.getMeasuredHeight();

                    double scaleParam = height * 1d / parentHeight; //Math.min(height * 1d / parentHeight, width * 1d / parentWidth);
                    int scaleIconHeight = (int) (iconHeight * scaleParam);
                    roundImageView.getLayoutParams().width = scaleIconHeight;
                    roundImageView.getLayoutParams().height = scaleIconHeight;

                    int scaleTitleSize = (int) (titleSize * scaleParam);
                    int scaleDescSize = (int) (descSize * scaleParam);
                    int scaleCtaSize = (int) (ctaSize * scaleParam);

                    titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, scaleTitleSize);
                    descTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, scaleDescSize);
                    ctaTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, scaleCtaSize);

                }
            };

            titleTextView.setTextColor(config.titleColor);
            descTextView.setTextColor(config.descColor);

            GradientDrawable gradientDrawable = new GradientDrawable();
            gradientDrawable.setCornerRadius(dip2px(mContext, 20));
            gradientDrawable.setColor(config.ctaBgColor);
            ctaTextView.setTextColor(config.ctaColor);
            ctaTextView.setBackgroundDrawable(gradientDrawable);

            view.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);

            CommonImageLoader.getInstance().startLoadImage(ad.getAdChoiceIconUrl(), dip2px(mContext, 10), new CommonImageLoader.ImageCallback() {
                @Override
                public void onSuccess(Bitmap bitmap, String url) {
                    adChoiceImageView.setImageBitmap(bitmap);
                    adChoiceImageView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onFail() {
                    adChoiceImageView.setVisibility(View.GONE);
                }
            });

        }

        view.setVisibility(View.VISIBLE);
    }

    public int dip2px(Context context, float dipValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }


}
