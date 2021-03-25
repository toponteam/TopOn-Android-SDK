/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.buiness;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.Toast;

import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;

public class OfferUrlHandler {

    public static final String HOST_GOOGLE_PLAY = "play.google.com";
    public static final String HOST_ANDROID_MARKET = "market.android.com";
    public static final String PATH_DETAILS = "details?";
    public static final String SCHEME_MARKET = "market";
    public static final String PROTOCOL_MARKET = SCHEME_MARKET + "://";

    public static boolean handleInAppOpenUrl(Context context, String url, boolean hasToast) {
        if (isGooglePlayUrl(url)) {
            String marketUrl = convertToMarketUrl(url);
            return openAppInPhone(context, marketUrl, hasToast);
        }

        Uri uri = Uri.parse(url);
        String scheme = uri.getScheme();
        if (scheme != null && !scheme.startsWith("http")) {
            return openAppInPhone(context, url, hasToast);
        }

        return false;
    }

    public static void openBrowserUrl(Context context, String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(url));
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
                    | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    /**
     * Check if it's GooglePlay's url
     *
     * @param url
     * @return
     */
    public static boolean isGooglePlayUrl(String url) {
        try {
            if (TextUtils.isEmpty(url)) {
                return false;
            }
            Uri uri = Uri.parse(url);
            if (uri == null || uri.getHost() == null) {
                return false;
            }
            return uri.getHost().equals(HOST_GOOGLE_PLAY)
                    || uri.getHost().equals(HOST_ANDROID_MARKET);
        } catch (Throwable t) {
        }
        return false;
    }

    /**
     * Convert to Market Url
     *
     * @param url
     * @return
     */
    private static String convertToMarketUrl(String url) {
        try {
            String detailsUrl = url.substring(url.indexOf(PATH_DETAILS));
            return PROTOCOL_MARKET + detailsUrl;

        } catch (Throwable e) {
            if (Const.DEBUG) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Open Market
     *
     * @param appUri
     */
    private static boolean openAppInPhone(final Context context, String appUri, boolean hasToast) {
        try {
            Intent intent = null;
            Uri uri = Uri.parse(appUri);
            if (uri.getScheme().equals("intent")) {
                intent = Intent.parseUri(appUri, Intent.URI_INTENT_SCHEME);
            } else {
                intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.setData(uri);
            }

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Throwable e) {
            if (hasToast) {
                SDKContext.getInstance().runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "Detect that the App Market is not installed and cannot be opened through the App Market.", Toast.LENGTH_LONG).show();
                    }
                });
            }

            return false;
        }
        return true;
    }
}
