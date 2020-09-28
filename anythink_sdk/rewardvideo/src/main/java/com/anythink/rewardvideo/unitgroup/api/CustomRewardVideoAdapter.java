package com.anythink.rewardvideo.unitgroup.api;

import android.app.Activity;
import android.content.Context;

import com.anythink.core.api.ATBaseAdAdapter;
import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.common.base.AnyThinkBaseAdapter;

import java.util.Map;

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
