/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.listeners;

import com.anythink.basead.entity.OfferError;

public interface AdLoadListener {
    void onAdDataLoaded();

    void onAdCacheLoaded();

    void onAdLoadFailed(OfferError error);
}
