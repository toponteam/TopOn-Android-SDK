/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.nativead.api;

import com.anythink.core.api.ATAdInfo;

public interface ATNativeEventListener {
    /**
     * Impression Callback
     *
     * @param view
     */
    public void onAdImpressed(ATNativeAdView view, ATAdInfo entity);

    /**
     * Click Callback
     *
     * @param view
     */
    public void onAdClicked(ATNativeAdView view, ATAdInfo entity);

    /**
     * Video Start Callback
     *
     * @param view
     */
    public void onAdVideoStart(ATNativeAdView view);

    /**
     * Video End Callback
     *
     * @param view
     */
    public void onAdVideoEnd(ATNativeAdView view);

    /**
     * Video Progress Callback
     *
     * @param view
     */
    public void onAdVideoProgress(ATNativeAdView view, int progress);


}
