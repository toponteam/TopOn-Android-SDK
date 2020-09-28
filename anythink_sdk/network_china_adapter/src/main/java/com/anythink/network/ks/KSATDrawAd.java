package com.anythink.network.ks;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

import com.anythink.nativead.unitgroup.api.CustomNativeAd;
import com.kwad.sdk.api.KsDrawAd;

import java.util.List;

/**
 * Draw
 */
public class KSATDrawAd extends CustomNativeAd {

    Context context;
    KsDrawAd ksDrawAd;

    public KSATDrawAd(Context context, KsDrawAd ksDrawAd) {
        this.context = context;
        this.ksDrawAd = ksDrawAd;
    }


    @Override
    public boolean isNativeExpress() {
        return true;
    }

    @Override
    public View getAdMediaView(Object... object) {
        try {
            return ksDrawAd.getDrawView(context);
        } catch (Exception e) {

        }
        return null;
    }

    @Override
    public void prepare(View view, List<View> clickViewList, FrameLayout.LayoutParams layoutParams) {
        ksDrawAd.setAdInteractionListener(new KsDrawAd.AdInteractionListener() {
            @Override
            public void onAdClicked() {
                notifyAdClicked();
            }

            @Override
            public void onAdShow() {

            }
        });
    }

    @Override
    public void prepare(View view, FrameLayout.LayoutParams layoutParams) {
        this.prepare(view, null, layoutParams);
    }

    @Override
    public void clear(View view) {
    }

    @Override
    public void destroy() {
        if (ksDrawAd != null) {
            ksDrawAd.setAdInteractionListener(null);
            ksDrawAd = null;
        }
        context = null;
    }
}
