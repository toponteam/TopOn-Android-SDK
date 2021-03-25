/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.listeners;

import com.anythink.basead.entity.OfferError;

public interface VideoAdEventListener extends AdEventListener {

    void onVideoAdPlayStart();

    void onVideoAdPlayEnd();

    void onVideoShowFailed(OfferError error);

    void onRewarded();

}
