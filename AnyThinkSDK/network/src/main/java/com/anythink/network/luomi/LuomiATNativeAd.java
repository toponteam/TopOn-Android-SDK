package com.anythink.network.luomi;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.anythink.nativead.unitgroup.api.CustomNativeAd;
import com.anythink.nativead.unitgroup.api.CustomNativeListener;
import com.hz.yl.b.HhInfo;
import com.hz.yl.b.mian.UpLoadPay;

import java.util.List;

/**
 * Created by Z on 2018/1/12.
 */

public class LuomiATNativeAd extends CustomNativeAd {
    private final String TAG = LuomiATNativeAd.class.getSimpleName();

    Context mContext;
    CustomNativeListener mCustonNativeListener;
    HhInfo mHhInfo;

    public LuomiATNativeAd(Context context
            , CustomNativeListener customNativeListener
            , HhInfo hhInfo) {
        mContext = context.getApplicationContext();
        mCustonNativeListener = customNativeListener;

        mHhInfo = hhInfo;

        setTitle(hhInfo.getWenzi());
        setDescriptionText(hhInfo.getWenzi2());
        setIconImageUrl(hhInfo.getGetImageTJ());
        setMainImageUrl(hhInfo.getImgurl());
    }

    // Lifecycle Handlers
    @Override
    public void prepare(final View view, FrameLayout.LayoutParams layoutParams) {
        if (view == null) {
            return;
        }
        try {
            registerView(view, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    UpLoadPay.getInstance().upLoadNativeClick(view.getContext(), mHhInfo);
                    notifyAdClicked();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        UpLoadPay.getInstance().upLoadNativeShow(view.getContext(), mHhInfo);
    }


    @Override
    public void prepare(View view, List<View> clickViewList, FrameLayout.LayoutParams layoutParams) {
        if (view == null) {
            return;
        }
        try {
            for (View childView : clickViewList) {
                childView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        UpLoadPay.getInstance().upLoadNativeClick(view.getContext(), mHhInfo);
                        notifyAdClicked();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        UpLoadPay.getInstance().upLoadNativeShow(view.getContext(), mHhInfo);
    }

    private void registerView(View view, View.OnClickListener clickListener) {
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                registerView(child, clickListener);
            }
        } else {
            view.setOnClickListener(clickListener);
        }
    }


    @Override
    public ViewGroup getCustomAdContainer() {
        return null;
    }

    @Override
    public void clear(final View view) {
        log(TAG, "clear");

    }


    @Override
    public View getAdMediaView(Object... object) {
        try {
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public void destroy() {
        log(TAG, "destory");
    }

}
