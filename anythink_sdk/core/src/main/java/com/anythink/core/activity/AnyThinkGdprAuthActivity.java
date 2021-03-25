/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.activity;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.telecom.Call;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;

import com.anythink.core.activity.component.PrivacyPolicyView;
import com.anythink.core.api.ATGDPRAuthCallback;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.strategy.AppStrategy;
import com.anythink.core.strategy.AppStrategyManager;


public class AnyThinkGdprAuthActivity extends Activity {

    String mCurrentUrl;
    PrivacyPolicyView mPrivacyPolicyView;

    public static ATGDPRAuthCallback mCallback;

    boolean allowBackPress = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppStrategy appStrategy = AppStrategyManager.getInstance(getApplicationContext()).getAppStrategyByAppId(SDKContext.getInstance().getAppId());
        if (appStrategy != null) {
            mCurrentUrl = appStrategy.getGdprNu();
        }

        if (TextUtils.isEmpty(mCurrentUrl)) {
            mCurrentUrl = Const.URL.GDPR_URL;
        }

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }
        try {
            mPrivacyPolicyView = new PrivacyPolicyView(this);
            mPrivacyPolicyView.setResultCallbackListener(new PrivacyPolicyView.CallbackListener() {
                @Override
                public void onLevelSelect(int level) {
                    if (mCallback != null) {
                        mCallback.onAuthResult(level);
                        mCallback = null;
                    }
                    finish();
                }

                @Override
                public void onPageLoadFail() {
                    allowBackPress = true;
                    if (mCallback != null) {
                        mCallback.onPageLoadFail();
                    }
                }

                @Override
                public void onPageLoadSuccess() {
                    allowBackPress = false;
                }
            });
            setContentView(mPrivacyPolicyView);
            mPrivacyPolicyView.loadPolicyUrl(mCurrentUrl);

        } catch (Exception e) {
            if (Const.DEBUG) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (allowBackPress) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (mPrivacyPolicyView != null) {
            mPrivacyPolicyView.destory();
        }
        mCallback = null;
        super.onDestroy();

    }
}
