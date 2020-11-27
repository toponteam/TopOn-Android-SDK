/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.nativead.api;


import android.content.Context;
import android.view.View;

import com.anythink.nativead.unitgroup.BaseNativeAd;

/**
 * Created by Z on 2018/1/8.
 */

public interface ATNativeAdRenderer<T extends BaseNativeAd> {

    /**
     *
     * @param context
     * @param networkType network firm id
     * @return
     */
    public View createView(Context context, int networkType);

    /**
     * Render NativeAd View
     **/
    public void renderAdView(View view, T ad);
}
