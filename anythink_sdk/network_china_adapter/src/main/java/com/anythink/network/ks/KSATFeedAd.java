package com.anythink.network.ks;

import android.content.Context;
import android.view.View;

import com.anythink.nativead.unitgroup.api.CustomNativeAd;
import com.kwad.sdk.export.i.KsFeedAd;

/**
 * Feed native express
 */
public class KSATFeedAd extends CustomNativeAd {

    Context context;
    KsFeedAd ksFeedAd;


    public KSATFeedAd(Context context, KsFeedAd ksFeedAd, boolean isVideoSoundEnable) {
        this.context = context;
        this.ksFeedAd = ksFeedAd;

        setAdData(isVideoSoundEnable);
    }

    private void setAdData(boolean isVideoSoundEnable) {
        ksFeedAd.setVideoSoundEnable(isVideoSoundEnable);

        ksFeedAd.setAdInteractionListener(new KsFeedAd.AdInteractionListener() {
            @Override
            public void onAdClicked() {
                notifyAdClicked();
            }

            @Override
            public void onAdShow() {

            }

            @Override
            public void onDislikeClicked() {
                notifyAdDislikeClick();
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
            return ksFeedAd.getFeedView(context);
        } catch (Exception e) {

        }
        return null;
    }
}
