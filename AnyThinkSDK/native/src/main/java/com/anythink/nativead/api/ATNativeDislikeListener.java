package com.anythink.nativead.api;


import com.anythink.core.api.ATAdInfo;

/**
 * Created by Z on 2018/1/8.
 * Dislike Callback
 */

public abstract class ATNativeDislikeListener {
    /**
     * Close Callback
     *
     * @param view
     */
    public abstract void onAdCloseButtonClick(ATNativeAdView view, ATAdInfo entity);


}
