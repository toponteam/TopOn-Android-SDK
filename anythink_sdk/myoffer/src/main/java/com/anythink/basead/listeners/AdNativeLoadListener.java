/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.listeners;

import com.anythink.basead.entity.OfferError;
import com.anythink.basead.innerad.OwnUnifiedAd;

public interface AdNativeLoadListener {
    public void onNativeAdLoaded(OwnUnifiedAd... ownNativeAds);

    public void onNativeAdLoadError(OfferError offerError);
}
