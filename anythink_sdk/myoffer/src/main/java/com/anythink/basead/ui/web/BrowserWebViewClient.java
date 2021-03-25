/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.ui.web;


import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.anythink.basead.buiness.OfferUrlHandler;


class BrowserWebViewClient extends WebViewClient {


    private WebLandPageActivity mWebBrowerActivity;

    public BrowserWebViewClient(final WebLandPageActivity webBrowerActivity) {
        mWebBrowerActivity = webBrowerActivity;
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description,
                                String failingUrl) {
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (TextUtils.isEmpty(url) || "about:blank".equals(url)) {
            return false;
        }
        boolean isSuccess = OfferUrlHandler.handleInAppOpenUrl(view.getContext(), url, false);

        if (isSuccess) {
            return true;
        }

        try {
            Uri uri = Uri.parse(url);
            if (uri.getScheme().equals("intent")) {
                try {
                    Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                    String fallbackUrl = intent.getStringExtra("browser_fallback_url");
                    if (!TextUtils.isEmpty(fallbackUrl) && fallbackUrl.startsWith("http")) {
                        view.loadUrl(fallbackUrl);
                        return true;
                    }
                } catch (Throwable e) {

                }
            }

            if (!uri.getScheme().equals("http") && !uri.getScheme().equals("https")) {
                Log.i("", "The App does not exist.");
                return true;
            }
            return false;
        } catch (Throwable e) {

        }
        return false;

    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);

        WebProgressBarView webProgressBarView = mWebBrowerActivity.getWebProgressBarView();
        if (webProgressBarView != null) {
            webProgressBarView.setVisibility(View.VISIBLE);
            webProgressBarView.setProgress(0);
        }
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);

        mWebBrowerActivity.setBackButtomClickable(view.canGoBack());
        mWebBrowerActivity.setForwardButtomClickable(view.canGoForward());
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Override
    public boolean onRenderProcessGone(@Nullable final WebView view, @Nullable final RenderProcessGoneDetail detail) {
        mWebBrowerActivity.finish();
        return true;
    }
}
