package com.anythink.myoffer.network.base;

import com.anythink.network.myoffer.MyOfferError;

public interface MyOfferAdListener {

    void onAdLoaded();

    void onAdLoadFailed(MyOfferError error);

    void onAdShow();

    void onAdClosed();

    void onAdClick();

}
