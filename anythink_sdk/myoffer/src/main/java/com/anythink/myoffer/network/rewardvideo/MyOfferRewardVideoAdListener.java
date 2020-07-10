package com.anythink.myoffer.network.rewardvideo;

import com.anythink.myoffer.network.base.MyOfferAdListener;
import com.anythink.network.myoffer.MyOfferError;

public interface MyOfferRewardVideoAdListener extends MyOfferAdListener {

    void onVideoAdPlayStart();

    void onVideoAdPlayEnd();

    void onVideoShowFailed(MyOfferError error);

    void onRewarded();

}
