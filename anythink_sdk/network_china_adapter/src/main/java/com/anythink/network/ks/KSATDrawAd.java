package com.anythink.network.ks;

import android.content.Context;
import android.view.View;

import com.anythink.nativead.unitgroup.api.CustomNativeAd;
import com.kwad.sdk.export.i.KsDrawAd;
import com.kwad.sdk.export.i.KsFeedAd;

/**
 * Draw
 */
public class KSATDrawAd extends CustomNativeAd {

    Context context;
    KsDrawAd ksDrawAd;


    public KSATDrawAd(Context context, KsDrawAd ksDrawAd) {
        this.context = context;
        this.ksDrawAd = ksDrawAd;

        setAdData();
    }

    private void setAdData() {

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
}
