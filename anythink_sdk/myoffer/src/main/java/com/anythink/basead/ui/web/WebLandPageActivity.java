/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.ui.web;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.CookieSyncManager;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.anythink.basead.BaseAdConst;
import com.anythink.basead.buiness.OfferAdFunctionUtil;
import com.anythink.basead.buiness.OfferClickResultManager;
import com.anythink.basead.buiness.OfferUrlHandler;
import com.anythink.basead.entity.AdActivityStartParams;
import com.anythink.basead.entity.OfferClickResult;
import com.anythink.basead.ui.util.WebViews;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.entity.BaseAdContent;
import com.anythink.core.common.entity.BaseAdRequestInfo;
import com.anythink.core.common.utils.CommonUtil;

import java.lang.reflect.Method;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class WebLandPageActivity extends Activity {
    private static final int INNER_LAYOUT_ID = 343452;

    private WebProgressBarView mProgressBarView;
    private WebView mWebView;
    private ImageButton mBackButton;
    private ImageButton mForwardButton;
    private ImageButton mRefreshButton;
    private ImageButton mCloseButton;

    private boolean mProgressBarAvailable;


    /**
     * 开启页面
     */
    public static void start(Context context, AdActivityStartParams adActivityStartParams) {
        Intent intent = new Intent();
        intent.setClass(context, WebLandPageActivity.class);

        intent.putExtra(BaseAdConst.AcitvityParamsKey.EXTRA_BASE_AD, adActivityStartParams.baseAdContent);
        intent.putExtra(BaseAdConst.AcitvityParamsKey.EXTRA_BASE_REQUEST_INFO, adActivityStartParams.baseAdRequestInfo);
        intent.putExtra(BaseAdConst.AcitvityParamsKey.EXTRA_TARGET_URL, adActivityStartParams.targetUrl);

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private BaseAdContent mBaseAdContent;
    private BaseAdRequestInfo mBaseAdRequestInfo;
    private String mUrl;

    private void parseExtra() {
        Intent intent = getIntent();
        try {
            if (intent != null) {
                mBaseAdContent = (BaseAdContent) intent.getSerializableExtra(BaseAdConst.AcitvityParamsKey.EXTRA_BASE_AD);
                mBaseAdRequestInfo = (BaseAdRequestInfo) intent.getSerializableExtra(BaseAdConst.AcitvityParamsKey.EXTRA_BASE_REQUEST_INFO);
                mUrl = intent.getStringExtra(BaseAdConst.AcitvityParamsKey.EXTRA_TARGET_URL);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setForwardButtomClickable(boolean enable) {
        mForwardButton.setImageResource(enable ? CommonUtil.getResId(this, "browser_right_icon"
                , "drawable") : CommonUtil.getResId(this, "browser_unright_icon", "drawable"));
    }

    public void setBackButtomClickable(boolean enable) {
        mBackButton.setImageResource(enable ? CommonUtil.getResId(this, "browser_left_icon"
                , "drawable") : CommonUtil.getResId(this, "browser_unleft_icon", "drawable"));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setResult(Activity.RESULT_OK);

        mProgressBarAvailable = getWindow().requestFeature(Window.FEATURE_PROGRESS);
        if (mProgressBarAvailable) {
            getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);
        }

        parseExtra();

        if (mBaseAdContent == null || mBaseAdRequestInfo == null) {
            finish();
            return;
        }
        setContentView(getBrowserView());

        initializeWebView();
        initializeButtons();
        enableCookies();
    }

    @Override
    protected void onStart() {
        super.onStart();
        CommonUtil.hideNavigationBar(this);
    }


    private void initializeWebView() {
        WebSettings webSettings = mWebView.getSettings();

        /*
         * Pinch to zoom is apparently not enabled by default on all devices, so
         * declare zoom support explicitly.
         * https://stackoverflow.com/questions/5125851/enable-disable-zoom-in-android-webview
         */
//        webSettings.setSupportZoom(true);
//        webSettings.setBuiltInZoomControls(true);
//        webSettings.setUseWideViewPort(true);

        mWebView.setHorizontalScrollBarEnabled(false);
        mWebView.setVerticalScrollBarEnabled(false);
        mWebView.setVerticalScrollBarEnabled(false);
        mWebView.requestFocus();

        webSettings.setJavaScriptEnabled(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setAppCacheMaxSize(5242880L);
        webSettings.setAllowFileAccess(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setSupportZoom(false);
        webSettings.setSavePassword(false);
        webSettings.setDatabaseEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);

        if (Build.VERSION.SDK_INT >= 17) {
            webSettings.setMediaPlaybackRequiresUserGesture(false);
        }

        try {
            if (Build.VERSION.SDK_INT >= 16) {
                webSettings.setAllowUniversalAccessFromFileURLs(true);
            }
        } catch (Throwable var6) {
            var6.printStackTrace();
        }

//        webSettings.setDatabaseEnabled(true);
//        String var2 = getDir("AT_DB", 0).getPath();
//        webSettings.setDatabasePath(var2);
//        webSettings.setGeolocationEnabled(true);
//        webSettings.setGeolocationDatabasePath(var2);

        try {
            if (Build.VERSION.SDK_INT >= 21) {
                webSettings.setMixedContentMode(0);
            }
        } catch (Exception var5) {
            var5.printStackTrace();
        }

        if (Build.VERSION.SDK_INT >= 11) {
            try {
                Method var7 = WebSettings.class.getDeclaredMethod("setDisplayZoomControls", Boolean.TYPE);
                var7.setAccessible(true);
                var7.invoke(webSettings, false);
            } catch (Exception var4) {
            }
        }

        if (TextUtils.isEmpty(mUrl)) {
            mWebView.loadUrl(mBaseAdContent.getClickUrl());
        } else {
            mWebView.loadUrl(mUrl);
        }


        mWebView.setWebViewClient(new BrowserWebViewClient(this));

        mWebView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(final String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                if (url.contains(".apk")) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
                    if (!OfferAdFunctionUtil.startDownloadApp(getApplicationContext(), mBaseAdRequestInfo, mBaseAdContent, getOfferClickResult(), url)) {
                        OfferUrlHandler.openBrowserUrl(WebLandPageActivity.this, url);
                    }
//                        }
//                    });
                } else {
                    OfferUrlHandler.openBrowserUrl(WebLandPageActivity.this, url);
                }

            }
        });
    }

    private OfferClickResult getOfferClickResult() {
        return OfferClickResultManager.getInstance().getOfferClickResult(mBaseAdContent.getOfferSourceType(), mBaseAdContent.getOfferId());
    }

    private void initializeButtons() {
        mBackButton.setBackgroundColor(Color.TRANSPARENT);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mWebView.canGoBack()) {
                    mWebView.goBack();
                }
            }
        });

        mForwardButton.setBackgroundColor(Color.TRANSPARENT);
        mForwardButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mWebView.canGoForward()) {
                    mWebView.goForward();
                }
            }
        });

        mRefreshButton.setBackgroundColor(Color.TRANSPARENT);
        mRefreshButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mWebView.reload();
            }
        });

        mCloseButton.setBackgroundColor(Color.TRANSPARENT);
        mCloseButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                WebLandPageActivity.this.finish();
            }
        });
    }

    private void enableCookies() {
        CookieSyncManager.createInstance(this);
        CookieSyncManager.getInstance().startSync();
    }

    public WebProgressBarView getWebProgressBarView() {
        return mProgressBarView;
    }

    @Override
    protected void onPause() {
        super.onPause();
        CookieSyncManager.getInstance().stopSync();
        mWebView.setWebChromeClient(null);
        WebViews.onPause(mWebView, isFinishing());
    }

    @Override
    protected void onResume() {
        super.onResume();
        CookieSyncManager.getInstance().startSync();
        mWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView webView, int newProgress) {
                if (mProgressBarView != null) {
                    mProgressBarView.setProgress(newProgress);
                    if (newProgress == 100) {
                        SDKContext.getInstance().runOnMainThreadDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mProgressBarView.setVisibility(View.GONE);
                            }
                        }, 200);
                    }
                }

            }
        });

        mWebView.onResume();
    }

    @Override
    public void finish() {
        // ZoomButtonController adds buttons to the window's decorview. If they're still visible
        // when finish() is called, they need to be removed or a Window object will be leaked.
        ViewGroup decorView = (ViewGroup) getWindow().getDecorView();
        decorView.removeAllViews();
        super.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mWebView != null) {
            mWebView.destroy();
        }
        mWebView = null;
    }

    private View getBrowserView() {

        RelativeLayout moPubBrowserView = new RelativeLayout(this);
        moPubBrowserView.setBackgroundColor(0xff000000);
        LinearLayout.LayoutParams outerLayoutParams = new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        moPubBrowserView.setLayoutParams(outerLayoutParams);

        LinearLayout innerLayout = new LinearLayout(this);
        innerLayout.setId(INNER_LAYOUT_ID);
        RelativeLayout.LayoutParams innerLayoutParams = new RelativeLayout.LayoutParams(MATCH_PARENT, CommonUtil.dip2px(this, 55));
        innerLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        innerLayout.setLayoutParams(innerLayoutParams);
        innerLayout.setBackgroundDrawable(new ColorDrawable(0xfff6f6f6));
        int horizontalPadding = CommonUtil.dip2px(this, 20);
        innerLayout.setPadding(horizontalPadding, 0, horizontalPadding, 0);
        moPubBrowserView.addView(innerLayout);

        mBackButton = getButton(getResources().getDrawable(CommonUtil.getResId(this, "browser_unleft_icon"
                , "drawable")));
        mForwardButton = getButton(getResources().getDrawable(CommonUtil.getResId(this, "browser_unright_icon"
                , "drawable")));
        mRefreshButton = getButton(getResources().getDrawable(CommonUtil.getResId(this, "browser_refresh_icon"
                , "drawable")));
        mCloseButton = getButton(getResources().getDrawable(CommonUtil.getResId(this, "browser_close_icon"
                , "drawable")));

        innerLayout.addView(mBackButton);
        innerLayout.addView(mForwardButton);
        innerLayout.addView(mRefreshButton);
        innerLayout.addView(mCloseButton);

        mWebView = new BaseWebView(this);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        layoutParams.addRule(RelativeLayout.ABOVE, INNER_LAYOUT_ID);
        mWebView.setLayoutParams(layoutParams);
        moPubBrowserView.addView(mWebView);

        View view = new View(this);
        view.setBackgroundColor(0xffdadada);
        RelativeLayout.LayoutParams linelayoutParams = new RelativeLayout.LayoutParams(MATCH_PARENT, CommonUtil.dip2px(this, 1));
        linelayoutParams.addRule(RelativeLayout.ABOVE, INNER_LAYOUT_ID);
        view.setLayoutParams(linelayoutParams);
        moPubBrowserView.addView(view);

        mProgressBarView = new WebProgressBarView(this);
        mProgressBarView.setProgress(0);
        moPubBrowserView.addView(mProgressBarView, new RelativeLayout.LayoutParams(MATCH_PARENT, CommonUtil.dip2px(this, 2)));

        return moPubBrowserView;
    }

    private ImageButton getButton(final Drawable drawable) {
        ImageButton imageButton = new ImageButton(this);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(WRAP_CONTENT, CommonUtil.dip2px(this, 35), 1f);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        imageButton.setLayoutParams(layoutParams);
        imageButton.setBackgroundColor(0x00000000);

        imageButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imageButton.setImageDrawable(drawable);

        return imageButton;
    }

}

