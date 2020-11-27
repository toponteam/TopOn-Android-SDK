/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

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
