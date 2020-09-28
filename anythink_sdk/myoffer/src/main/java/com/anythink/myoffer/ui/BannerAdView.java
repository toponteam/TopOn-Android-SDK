package com.anythink.myoffer.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.anythink.core.common.entity.MyOfferAd;
import com.anythink.core.common.entity.MyOfferSetting;
import com.anythink.core.common.res.ImageLoader;
import com.anythink.core.common.res.ResourceEntry;
import com.anythink.core.common.res.image.RecycleImageView;
import com.anythink.core.common.utils.BitmapUtil;
import com.anythink.core.common.utils.CommonLogUtil;
import com.anythink.core.common.utils.CommonUtil;
import com.anythink.myoffer.buiness.MyOfferAdManager;
import com.anythink.myoffer.buiness.MyOfferImpressionRecordManager;
import com.anythink.myoffer.buiness.OfferClickController;
import com.anythink.myoffer.buiness.resource.MyOfferResourceState;
import com.anythink.myoffer.net.MyOfferTkLoader;
import com.anythink.myoffer.network.base.MyOfferAdListener;
import com.anythink.myoffer.ui.component.RoundImageView;

public class BannerAdView extends RelativeLayout {

    public static final String TAG = BannerAdView.class.getSimpleName();

    String mPlacementId;
    String mRequestId;
    MyOfferAd mMyOfferAd;
    MyOfferSetting mMyOfferSettings;
    MyOfferAdListener mMyOfferAdListener;

    boolean mNeedShowMainImage;
    String mBannerSize;
    int mWidth;
    int mHeight;


    OfferClickController mOfferClickControl;
    private View mCloseView;

    boolean hasUseImpressionPlugin;
    boolean hasRecordImpression;

    public BannerAdView(Context context, String placementId, String requestId, MyOfferAd myOfferAd, MyOfferSetting myOfferSettings, MyOfferAdListener myOfferAdListener) {
        super(context);

        mMyOfferAd = myOfferAd;
        mMyOfferSettings = myOfferSettings;
        mMyOfferAdListener = myOfferAdListener;
        mRequestId = requestId;
        mPlacementId = placementId;

        String bannerSize = myOfferSettings.getBannerSize();

        String bannerUrl;
        String layoutName;
        switch (bannerSize) {
            case MyOfferSetting.BANNER_SIZE_320x90:
                mBannerSize = MyOfferSetting.BANNER_SIZE_320x90;
                mWidth = 320;
                mHeight = 90;
                layoutName = "myoffer_banner_ad_layout_320x90";
                bannerUrl = myOfferAd.getBanner320x90Url();
                mNeedShowMainImage = true;
                break;

            case MyOfferSetting.BANNER_SIZE_300x250:
                mBannerSize = MyOfferSetting.BANNER_SIZE_300x250;
                mWidth = 300;
                mHeight = 250;
                layoutName = "myoffer_banner_ad_layout_300x250";
                bannerUrl = myOfferAd.getBanner300x250Url();
                mNeedShowMainImage = true;
                break;

            case MyOfferSetting.BANNER_SIZE_728x90:
                mBannerSize = MyOfferSetting.BANNER_SIZE_728x90;
                mWidth = 728;
                mHeight = 90;
                layoutName = "myoffer_banner_ad_layout_728x90";
                bannerUrl = myOfferAd.getBanner728x90Url();
                mNeedShowMainImage = true;
                break;

            case MyOfferSetting.BANNER_SIZE_320x50:
            default:
                mBannerSize = MyOfferSetting.BANNER_SIZE_320x50;
                mWidth = 320;
                mHeight = 50;
                layoutName = "myoffer_banner_ad_layout_320x50";
                bannerUrl = myOfferAd.getBanner320x50Url();
                break;
        }


        if (!TextUtils.isEmpty(bannerUrl) && MyOfferResourceState.isExist(bannerUrl)) {//Only picture
            CommonLogUtil.d(TAG, "mode: pure picture");
            LayoutInflater.from(context).inflate(CommonUtil.getResId(getContext(), "myoffer_banner_ad_layout_pure_picture", "layout"), this);
            initPurePictureBannerView(bannerUrl);
        } else {//assemble banner view
            CommonLogUtil.d(TAG, "mode: assemble banner");
            LayoutInflater.from(context).inflate(CommonUtil.getResId(getContext(), layoutName, "layout"), this);
            assembleBannerView();
        }

        registerListener();
    }

    private void initPurePictureBannerView(final String bannerUrl) {

        RelativeLayout rootView = (RelativeLayout) findViewById(CommonUtil.getResId(getContext(), "myoffer_banner_root", "id"));
        mCloseView = (ImageView) findViewById(CommonUtil.getResId(getContext(), "myoffer_banner_close", "id"));

        if (0 == mMyOfferSettings.getIsShowCloseButton()) {//show close button
            mCloseView.setVisibility(VISIBLE);

            if (TextUtils.equals(MyOfferSetting.BANNER_SIZE_728x90, mBannerSize)) {
                ViewGroup.LayoutParams layoutParams = mCloseView.getLayoutParams();
                layoutParams.width = CommonUtil.dip2px(getContext(), 23);
                layoutParams.height = CommonUtil.dip2px(getContext(), 23);
                mCloseView.setLayoutParams(layoutParams);
            }
        } else {//hide close button
            mCloseView.setVisibility(GONE);
        }

        int width = CommonUtil.dip2px(getContext(), mWidth);
        int height = CommonUtil.dip2px(getContext(), mHeight);

        //limit parent's size
        LayoutParams lp = (LayoutParams) rootView.getLayoutParams();
        lp.width = width;
        lp.height = height;
        rootView.setLayoutParams(lp);


        final RecycleImageView bgIv = new RecycleImageView(getContext());
        bgIv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        addView(bgIv, 0, new RelativeLayout.LayoutParams(width, height));

        //picture
        final RecycleImageView imageView = new RecycleImageView(getContext());
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        ImageLoader.getInstance(getContext()).load(new ResourceEntry(ResourceEntry.INTERNAL_CACHE_TYPE, bannerUrl), width, height, new ImageLoader.ImageLoaderListener() {
            @Override
            public void onSuccess(String url, Bitmap bitmap) {
                if (TextUtils.equals(bannerUrl, url)) {
                    imageView.setImageBitmap(bitmap);

                    Bitmap blurBitmap = BitmapUtil.blurBitmap(getContext(), bitmap);
                    bgIv.setImageBitmap(blurBitmap);
                }
            }

            @Override
            public void onFail(String url, String errorMsg) {

            }
        });
        RelativeLayout.LayoutParams imageLayoutParams = new RelativeLayout.LayoutParams(width, height);
        imageLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        addView(imageView, 1, imageLayoutParams);

        if (!TextUtils.isEmpty(mMyOfferAd.getAdChoiceUrl())) {
            final ImageView adChoiceView = (ImageView) findViewById(CommonUtil.getResId(getContext(), "myoffer_banner_self_ad_logo", "id"));
            ImageLoader.getInstance(getContext()).load(new ResourceEntry(ResourceEntry.INTERNAL_CACHE_TYPE, mMyOfferAd.getAdChoiceUrl()), new ImageLoader.ImageLoaderListener() {
                @Override
                public void onSuccess(String url, Bitmap bitmap) {
                    if (TextUtils.equals(mMyOfferAd.getAdChoiceUrl(), url)) {
                        adChoiceView.setImageBitmap(bitmap);
                    }
                }

                @Override
                public void onFail(String url, String errorMsg) {

                }
            });
        }

    }

    private void assembleBannerView() {
        final RoundImageView iconView = (RoundImageView) findViewById(CommonUtil.getResId(getContext(), "myoffer_banner_icon", "id"));
        TextView titleView = (TextView) findViewById(CommonUtil.getResId(getContext(), "myoffer_banner_ad_title", "id"));
        TextView destView = (TextView) findViewById(CommonUtil.getResId(getContext(), "myoffer_banner_desc", "id"));
        TextView ctaView = (TextView) findViewById(CommonUtil.getResId(getContext(), "myoffer_banner_ad_install_btn", "id"));
        mCloseView = (ImageView) findViewById(CommonUtil.getResId(getContext(), "myoffer_banner_close", "id"));

        ViewGroup.LayoutParams layoutParams;
        if (0 == mMyOfferSettings.getIsShowCloseButton()) {//show close button
            mCloseView.setVisibility(VISIBLE);
        } else {//hide close button
            mCloseView.setVisibility(GONE);

            RelativeLayout.LayoutParams lp;
            switch (mBannerSize) {
                case MyOfferSetting.BANNER_SIZE_320x50:
                    lp = ((RelativeLayout.LayoutParams) ctaView.getLayoutParams());
                    lp.rightMargin = CommonUtil.dip2px(getContext(), 10);
                    ctaView.setLayoutParams(lp);
                    break;
                case MyOfferSetting.BANNER_SIZE_320x90:
                    lp = ((RelativeLayout.LayoutParams) titleView.getLayoutParams());
                    lp.rightMargin = CommonUtil.dip2px(getContext(), 10);
                    titleView.setLayoutParams(lp);
                    break;
                case MyOfferSetting.BANNER_SIZE_728x90:
                    lp = ((RelativeLayout.LayoutParams) ctaView.getLayoutParams());
                    lp.rightMargin = CommonUtil.dip2px(getContext(), 46);
                    ctaView.setLayoutParams(lp);
                    break;
            }
        }

        if (!TextUtils.isEmpty(mMyOfferAd.getIconUrl())) {
            layoutParams = iconView.getLayoutParams();
            iconView.setRadiusInDip(3);
            iconView.setNeedRadiu(true);
            ImageLoader.getInstance(getContext()).load(new ResourceEntry(ResourceEntry.INTERNAL_CACHE_TYPE, mMyOfferAd.getIconUrl()), layoutParams.width, layoutParams.height, new ImageLoader.ImageLoaderListener(){

                @Override
                public void onSuccess(String url, Bitmap bitmap) {
                    if (TextUtils.equals(mMyOfferAd.getIconUrl(), url)) {
                        iconView.setImageBitmap(bitmap);
                    }
                }

                @Override
                public void onFail(String url, String errorMsg) {

                }
            });
        }
        titleView.setText(mMyOfferAd.getTitle());
        destView.setText(mMyOfferAd.getDesc());
        ctaView.setText(mMyOfferAd.getCtaText());

        if (!TextUtils.isEmpty(mMyOfferAd.getAdChoiceUrl())) {
            final ImageView adChoiceView = (ImageView) findViewById(CommonUtil.getResId(getContext(), "myoffer_banner_self_ad_logo", "id"));
            ImageLoader.getInstance(getContext()).load(new ResourceEntry(ResourceEntry.INTERNAL_CACHE_TYPE, mMyOfferAd.getAdChoiceUrl()), new ImageLoader.ImageLoaderListener() {
                @Override
                public void onSuccess(String url, Bitmap bitmap) {
                    if (TextUtils.equals(mMyOfferAd.getAdChoiceUrl(), url)) {
                        adChoiceView.setImageBitmap(bitmap);
                    }
                }

                @Override
                public void onFail(String url, String errorMsg) {

                }
            });
        }

        if (mNeedShowMainImage) {
            final RoundImageView MainImageView = (RoundImageView) findViewById(CommonUtil.getResId(getContext(), "myoffer_banner_main_image", "id"));
            if (!TextUtils.isEmpty(mMyOfferAd.getEndCardImageUrl())) {
                layoutParams = MainImageView.getLayoutParams();
                MainImageView.setRadiusInDip(3);
                MainImageView.setNeedRadiu(true);
                ImageLoader.getInstance(getContext()).load(new ResourceEntry(ResourceEntry.INTERNAL_CACHE_TYPE, mMyOfferAd.getEndCardImageUrl()), new ImageLoader.ImageLoaderListener() {
                    @Override
                    public void onSuccess(String url, Bitmap bitmap) {
                        if (TextUtils.equals(mMyOfferAd.getEndCardImageUrl(), url)) {
                            MainImageView.setImageBitmap(bitmap);
                        }
                    }

                    @Override
                    public void onFail(String url, String errorMsg) {

                    }
                });
            }
        }

    }

    private void registerListener() {
        mCloseView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMyOfferAdListener != null) {
                    mMyOfferAdListener.onAdClosed();
                }
            }
        });
    }

    public void onClickBannerView() {
        if (mOfferClickControl == null) {
            mOfferClickControl = new OfferClickController(getContext(), mPlacementId, mMyOfferAd);
        }

        mOfferClickControl.startClick(mRequestId, new OfferClickController.ClickStatusCallback() {
            @Override
            public void clickStart() {

            }

            @Override
            public void clickEnd() {

            }

            @Override
            public void downloadApp(String url) {
                MyOfferAdManager.getInstance(getContext()).startDownloadApp(mRequestId, mMyOfferSettings, mMyOfferAd, url);
            }
        });

        if (mMyOfferAdListener != null) {
            mMyOfferAdListener.onAdClick();
        }

    }


    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == VISIBLE && !hasUseImpressionPlugin) {
            notifyShow();
        }
    }

    private synchronized void notifyShow() {
        if (hasRecordImpression) {
            return;
        }
        hasRecordImpression = true;
        MyOfferImpressionRecordManager.getInstance(getContext()).recordImpression(mMyOfferAd);
        MyOfferAdManager.getInstance(getContext()).sendAdTracking(mRequestId, mMyOfferAd, MyOfferTkLoader.IMPRESSION_TYPE, "");

        if (mMyOfferAdListener != null) {
            mMyOfferAdListener.onAdShow();
        }
    }

}
