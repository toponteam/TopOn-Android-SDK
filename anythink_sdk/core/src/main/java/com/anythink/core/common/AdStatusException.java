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
