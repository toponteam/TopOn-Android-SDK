/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.ui.util;


import android.os.Build;
import android.webkit.CookieManager;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.base.UploadDataLevelManager;

public class WebViews {
    public static void onPause(final WebView webView, boolean isFinishing) {
        // XXX
        // We need to call WebView#stopLoading and WebView#loadUrl here due to an Android
        // bug where the audio of an HTML5 video will continue to play after the activity has been
        // destroyed. The web view must stop then load an invalid url during the onPause lifecycle
        // event in order to stop the audio.
        if (isFinishing) {
            webView.stopLoading();
            webView.loadUrl("");
        }

        webView.onPause();
    }

    public static void setDisableJSChromeClient(final WebView webView) {
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(final WebView view, final String url,
                                     final String message, final JsResult result) {
                result.confirm();
                return true;
            }

            @Override
            public boolean onJsConfirm(final WebView view, final String url,
                                       final String message, final JsResult result) {
                result.confirm();
                return true;
            }

            @Override
            public boolean onJsPrompt(final WebView view, final String url,
                                      final String message, final String defaultValue,
                                      final JsPromptResult result) {
                result.confirm();
                return true;
            }

            @Override
            public boolean onJsBeforeUnload(final WebView view, final String url,
                                            final String message, final JsResult result) {
                result.confirm();
                return true;
            }
        });
    }

    public static void manageWebCookies() {
        CookieManager cookieManager = CookieManager.getInstance();
        if (UploadDataLevelManager.getInstance(SDKContext.getInstance().getContext()).canUpLoadDeviceData()) {
            cookieManager.setAcceptCookie(true);
            CookieManager.setAcceptFileSchemeCookies(true);
            return;
        }

        // remove all cookies
        cookieManager.setAcceptCookie(false);
        CookieManager.setAcceptFileSchemeCookies(false);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.removeSessionCookies(null);
            cookieManager.removeAllCookies(null);
            cookieManager.flush();
        } else {
            cookieManager.removeSessionCookie();
            cookieManager.removeAllCookie();
        }
    }

    //
    public static void manageThirdPartyCookies(final WebView webView) {
        if (webView == null) {
            return;
        }
        CookieManager cookieManager = CookieManager.getInstance();
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.setAcceptThirdPartyCookies(webView, UploadDataLevelManager.getInstance(webView.getContext()).canUpLoadDeviceData());
        }
    }
}
