/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.rewardvideo.unitgroup.api;

import android.app.Activity;

import com.anythink.core.api.ATBaseAdAdapter;

public abstract class CustomRewardVideoAdapter extends ATBaseAdAdapter {

    protected CustomRewardedVideoEventListener mImpressionListener;

    public abstract void show(Activity activity);

    final public void internalShow(Activity activity, CustomRewardedVideoEventListener listener) {
        mImpressionListener = listener;
        show(activity);
    }

    public void clearImpressionListener() {
        mImpressionListener = null;
    }

}
