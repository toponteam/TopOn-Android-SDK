/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.test.ad.demo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.anythink.core.api.ATAdConst;
import com.anythink.core.api.ATAdInfo;
import com.anythink.core.api.ATAdStatusInfo;
import com.anythink.core.api.AdError;
import com.anythink.network.baidu.BaiduATConst;
import com.anythink.splashad.api.ATSplashAd;
import com.anythink.splashad.api.ATSplashExListener;

import java.util.HashMap;
import java.util.Map;

public class SplashAdShowInCurrentActivity extends Activity {

    private static final String TAG = SplashAdShowInCurrentActivity.class.getSimpleName();

    ATSplashAd splashAd;
    FrameLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_ad_show_in_current);

        String placementId = getIntent().getStringExtra("placementId");
        container = findViewById(R.id.splash_ad_container);

        initSplash(placementId);


        findViewById(R.id.show_ad_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (splashAd.isAdReady()) {
                    if (container != null) {
                        container.setVisibility(View.VISIBLE);
                    }
                    splashAd.show(SplashAdShowInCurrentActivity.this, container);
                } else {
                    Toast.makeText(SplashAdShowInCurrentActivity.this, "splash no cache.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.loadAd_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String, Object> localMap = new HashMap<>();
                localMap.put(ATAdConst.KEY.AD_WIDTH, getResources().getDisplayMetrics().widthPixels);
                localMap.put(ATAdConst.KEY.AD_HEIGHT, getResources().getDisplayMetrics().heightPixels);

                // Only for GDT (true: open download dialog, false: download directly)
                localMap.put(ATAdConst.KEY.AD_CLICK_CONFIRM_STATUS, true);

                splashAd.setLocalExtra(localMap);

                splashAd.loadAd();
            }
        });

        findViewById(R.id.is_ad_ready_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                boolean isReady = splashAd.isAdReady();
                ATAdStatusInfo atAdStatusInfo = splashAd.checkAdStatus();
                Toast.makeText(SplashAdShowInCurrentActivity.this, "splash ad ready status:" + atAdStatusInfo.isReady(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initSplash(String placementId) {
        splashAd = new ATSplashAd(this, placementId, null, new ATSplashExListener() {

            @Override
            public void onDeeplinkCallback(ATAdInfo adInfo, boolean isSuccess) {
                Log.i(TAG, "onDeeplinkCallback:" + adInfo.toString() + "--status:" + isSuccess);
            }

            @Override
            public void onAdLoaded() {
                Log.i(TAG, "onAdLoaded---------");
                Toast.makeText(SplashAdShowInCurrentActivity.this, "onAdLoaded", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNoAdError(AdError adError) {
                Log.i(TAG, "onNoAdError---------:" + adError.getFullErrorInfo());
                Toast.makeText(SplashAdShowInCurrentActivity.this, "onNoAdError: " + adError.getFullErrorInfo(), Toast.LENGTH_SHORT).show();
                if (container != null) {
                    container.removeAllViews();
                    container.setVisibility(View.GONE);
                }
            }

            @Override
            public void onAdShow(ATAdInfo entity) {
                Log.i(TAG, "onAdShow:\n" + entity.toString());
                Toast.makeText(SplashAdShowInCurrentActivity.this, "onAdShow", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdClick(ATAdInfo entity) {
                Log.i(TAG, "onAdClick:\n" + entity.toString());
                Toast.makeText(SplashAdShowInCurrentActivity.this, "onAdClick", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdDismiss(ATAdInfo entity) {
                Log.i(TAG, "onAdDismiss---------:" + entity.toString());
                Toast.makeText(SplashAdShowInCurrentActivity.this, "onAdDismiss", Toast.LENGTH_SHORT).show();
                if (container != null) {
                    container.removeAllViews();
                    container.setVisibility(View.GONE);
                }
            }

        }, 5000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        splashAd = null;
        container = null;
    }
}