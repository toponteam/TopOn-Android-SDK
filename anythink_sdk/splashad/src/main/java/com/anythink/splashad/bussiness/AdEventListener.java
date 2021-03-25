/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.splashad.bussiness;

import android.content.Context;

import com.anythink.core.api.ATAdInfo;
import com.anythink.core.api.ATNetworkConfirmInfo;

public abstract class AdEventListener {

    boolean mHasDismiss;

    public void onCallbackAdDismiss(ATAdInfo entity) {
        if (!mHasDismiss) {
            mHasDismiss = true;

            onAdDismiss(entity);
        }
    }

    public abstract void onAdShow(ATAdInfo entity);

    public abstract void onAdClick(ATAdInfo entity);

    public abstract void onAdDismiss(ATAdInfo entity);

    public abstract void onDeeplinkCallback(ATAdInfo entity, boolean isSuccess);

    public abstract void onDownloadConfirm(Context context, ATAdInfo adInfo, ATNetworkConfirmInfo networkConfirmInfo);


}
