package com.anythink.core.activity;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.anythink.core.activity.component.PrivacyPolicyView;
import com.anythink.core.api.ATGDPRAuthCallback;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.strategy.AppStrategy;
import com.anythink.core.strategy.AppStrategyManager;

/**
 * Created by Z on 2018/5/18.
 * GDPR Activity
 */

public class AnyThinkGdprAuthActivity extends Activity {

    String mCurrentUrl;
    PrivacyPolicyView mPrivacyPolicyView;

    public static ATGDPRAuthCallback mCallback;

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
            mPrivacyPolicyView.setClickCallbackListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int resultLevel = (int) (v.getTag());
                    if (mCallback != null) {
                        mCallback.onAuthResult(resultLevel);
                        mCallback = null;
                    }
                    finish();
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
