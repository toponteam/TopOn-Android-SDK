package com.anythink.network.baidu;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.anythink.nativead.unitgroup.api.CustomNativeAd;
import com.baidu.mobad.feeds.NativeResponse;
import com.baidu.mobads.component.XNativeView;

import java.util.List;
import java.util.Map;

public class BaiduATNativeAd extends CustomNativeAd {

    private NativeResponse mNativeResponse;
    private Context mContext;

    public BaiduATNativeAd(Context context, NativeResponse nativeResponse) {

        mContext = context.getApplicationContext();
        mNativeResponse = nativeResponse;

        setData(mNativeResponse);

    }

    public void setData(NativeResponse nativeResponse) {
        setIconImageUrl(nativeResponse.getIconUrl());
        setMainImageUrl(nativeResponse.getImageUrl());
        setAdChoiceIconUrl(nativeResponse.getBaiduLogoUrl());
        setTitle(nativeResponse.getTitle());
        setDescriptionText(nativeResponse.getDesc());
        setCallToActionText(nativeResponse.isDownloadApp() ? "下载" : "查看");
        setAdFrom(nativeResponse.getBrandName());

    }

    @Override
    public ViewGroup getCustomAdContainer() {
        return null;
    }

    @Override
    public void prepare(final View view, FrameLayout.LayoutParams layoutParams) {
        if (view == null) {
            return;
        }
        registerView(view, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mNativeResponse != null) {
                    mNativeResponse.handleClick(view);
                    notifyAdClicked();
                }
            }
        });
        mNativeResponse.recordImpression(view);
        if (mMediaView != null) {
            mMediaView.render();
        }
    }

    @Override
    public void prepare(View view, List<View> clickViewList, FrameLayout.LayoutParams layoutParams) {
        if (view == null) {
            return;
        }

        for (View childView : clickViewList) {
            if (childView != null && childView != mMediaView) {
                childView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mNativeResponse != null) {
                            mNativeResponse.handleClick(view);
                        }
                        notifyAdClicked();
                    }
                });
            }
        }
        mNativeResponse.recordImpression(view);
        if (mMediaView != null) {
            mMediaView.render();
        }

    }

    private void registerView(View view, View.OnClickListener clickListener) {
        if (view instanceof ViewGroup && view != mMediaView) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                registerView(child, clickListener);
            }
        } else {
            view.setOnClickListener(clickListener);
        }
    }

    private void unregisterView(View view) {
        if (view == null) {
            return;
        }
        if (view instanceof ViewGroup && view != mMediaView) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                unregisterView(child);
            }
        } else {
            view.setOnClickListener(null);
        }
    }


    @Override
    public void clear(final View view) {
        unregisterView(view);
        if (mMediaView != null) {
            mMediaView.setNativeItem(null);
            mMediaView.setNativeViewClickListener(null);
            mMediaView = null;
        }

    }


    XNativeView mMediaView;

    @Override
    public View getAdMediaView(Object... object) {
        if (mNativeResponse != null && mNativeResponse.getMaterialType() == NativeResponse.MaterialType.VIDEO) {
            mMediaView = new XNativeView(mContext);
            mMediaView.setNativeItem(mNativeResponse);
            mMediaView.setNativeViewClickListener(new XNativeView.INativeViewClickListener() {
                @Override
                public void onNativeViewClick(XNativeView xNativeView) {
                    notifyAdClicked();
                }
            });
            return mMediaView;
        }
        return null;
    }

    @Override
    public void destroy() {
        mNativeResponse = null;

        if (mMediaView != null) {
            mMediaView.setNativeItem(null);
            mMediaView.setNativeViewClickListener(null);
            mMediaView = null;
        }
        mContext = null;
    }

}
