package com.anythink.nativead.api;

import com.anythink.core.api.AdError;

/**
 * Created by Z on 2018/1/8.
 */

public interface ATNativeNetworkListener {
    /**
     * Ad Request Success Callback
     */
    void onNativeAdLoaded();

    /**
     * Ad Request Fail Callback
     */
    void onNativeAdLoadFail(AdError error);

}
