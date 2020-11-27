/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.common;

import com.anythink.core.api.AdError;

public class AdStatusException extends IllegalStateException {

    public AdError adError;
    public String reason;

    public AdStatusException(AdError adError, String reason) {
        this.adError = adError;
        this.reason = reason;
    }
}
