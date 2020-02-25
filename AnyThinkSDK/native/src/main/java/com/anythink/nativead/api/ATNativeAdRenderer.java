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
