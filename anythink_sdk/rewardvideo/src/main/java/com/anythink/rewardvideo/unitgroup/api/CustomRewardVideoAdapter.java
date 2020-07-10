package com.anythink.rewardvideo.unitgroup.api;

import android.app.Activity;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.common.base.AnyThinkBaseAdapter;

import java.util.Map;

public abstract class CustomRewardVideoAdapter extends AnyThinkBaseAdapter {

    protected CustomRewardVideoListener mLoadResultListener;
    protected CustomRewardedVideoEventListener mImpressionListener;
    protected String mUserId = "";
    protected String mUserData = "";

    public abstract void loadRewardVideoAd(final Activity activity
            , final Map<String, Object> serverExtras
            , final ATMediationSetting mediationSetting
            , final CustomRewardVideoListener customRewardVideoListener);

    public abstract void show(Activity activity);

    public abstract void onResume(Activity activity);

    public abstract void onPause(Activity activity);

    public void setUserId(String userId) {
        mUserId = userId;
    }

     public void setUserData(String userData) {
        mUserData = userData;
    }

    public void setAdImpressionListener(CustomRewardedVideoEventListener listener) {
        mImpressionListener = listener;
    }

    public void clearLoadListener() {
        mLoadResultListener = null;
    }

    public void clearImpressionListener() {
        mImpressionListener = null;
    }

}
