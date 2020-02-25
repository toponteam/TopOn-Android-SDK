package com.anythink.myoffer.network.interstitial;

import com.anythink.myoffer.network.base.MyOfferAdListener;
import com.anythink.network.myoffer.MyOfferError;

public interface MyOfferInterstitialAdListener extends MyOfferAdListener {

    void onVideoAdPlayStart();

    void onVideoAdPlayEnd();

    void onVideoShowFailed(MyOfferError error);

}
