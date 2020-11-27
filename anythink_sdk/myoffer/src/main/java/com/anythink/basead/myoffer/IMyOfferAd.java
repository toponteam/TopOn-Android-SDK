/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.myoffer;

import java.util.Map;

public interface IMyOfferAd {

    void load();
    void show(Map<String, Object> extraMap);
    boolean isReady();
}
