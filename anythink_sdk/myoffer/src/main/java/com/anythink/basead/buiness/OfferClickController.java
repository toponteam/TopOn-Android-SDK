/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.buiness;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.Toast;

import com.anythink.basead.entity.AdActivityStartParams;
import com.anythink.basead.ui.web.WebLandPageActivity;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.entity.BaseAdContent;
import com.anythink.core.common.entity.BaseAdSetting;
import com.anythink.core.common.track.AgentEventManager;
import com.anythink.core.common.utils.CommonLogUtil;
import com.anythink.core.common.utils.task.TaskManager;

import java.net.HttpURLConnection;
import java.net.URL;


/**
 * MyOffer Click Controller
 */

public class OfferClickController {
    private final String TAG = getClass().getSimpleName();

    private final int MAX_JUMP_COUNT = 10;
    private final int MARKET_TYPE = 1;
    private final int BROWSER_TYPE = 2;
    private final int INNER_BROWSER_TYPE = 3;
    private final int APK_TYPE = 4;
    public static final int SYNC_MODE = 0;
    public static final int ASYNC_MODE = 1;
    BaseAdContent mBaseAdContent;
    private String mPlacementId;
    private String mRequestId;

    boolean mIsClicking;
    boolean mIsCancel;

    Context mContext;
    boolean mIsClickAsync;
    BaseAdSetting mBaseAdSetting;

    public OfferClickController(Context context, String placementId, BaseAdContent ad, BaseAdSetting baseAdSetting) {
        mBaseAdContent = ad;
        mPlacementId = placementId;
        mContext = context.getApplicationContext();
        mBaseAdSetting = baseAdSetting;
        mIsClickAsync = OfferAdFunctionUtil.isClickAsync(ad, baseAdSetting);
    }

    /**
     * start click
     *
     * @param clickStatusCallback
     */
    public void startClick(final String requestId, final ClickStatusCallback clickStatusCallback) {
        if (mIsClicking) {
            return;
        }
        mRequestId = requestId;

        if (clickStatusCallback != null) {
            clickStatusCallback.clickStart();
        }

        mIsClicking = true;
        mIsCancel = false;

        TaskManager.getInstance().run_proxy(new Runnable() {
            @Override
            public void run() {
                if (!TextUtils.isEmpty(mBaseAdContent.getDeeplinkUrl()) && isApkInstalled(mContext, mBaseAdContent.getPkgName())) {
                    /**If open deeplink success, it would continue to open click url.**/
                    String deepLinkUrl = mBaseAdContent.getDeeplinkUrl().replaceAll("\\{req_id\\}", requestId == null ? "" : requestId);
                    //TODO Test DeepLink Open
                    if (OfferUrlHandler.handleInAppOpenUrl(mContext, deepLinkUrl, false)) {
                        AgentEventManager.sendDeepLinkAgent(mPlacementId, mBaseAdContent.getOfferId(), mBaseAdContent.getOfferSourceType(), deepLinkUrl, "1");
                        mIsClicking = false;
                        if (clickStatusCallback != null) {
                            clickStatusCallback.clickEnd();
                        }
                        return;
                    } else {
                        AgentEventManager.sendDeepLinkAgent(mPlacementId, mBaseAdContent.getOfferId(), mBaseAdContent.getOfferSourceType(), deepLinkUrl, "0");
                    }
                }

                /**Open click url**/
                openClickUrl(requestId, clickStatusCallback);

            }
        });
    }


    /**
     * Open Ad click url
     *
     * @param requestId
     * @param clickStatusCallback
     */
    private void openClickUrl(String requestId, final ClickStatusCallback clickStatusCallback) {
        final String clickUrl = (mBaseAdContent.getClickUrl() != null ? mBaseAdContent.getClickUrl() : "").replaceAll("\\{req_id\\}", requestId == null ? "" : requestId);

        boolean isNeedJump = true; //Default need to open app page
        if (mBaseAdContent.getClickType() != MARKET_TYPE && mBaseAdContent.getClickType() != APK_TYPE) {
            handleClickResult(clickUrl, clickStatusCallback);
            return;
        }

        if (clickUrl.endsWith(".apk")) {
            handleClickResult(clickUrl, clickStatusCallback);
            return;
        }

        if (!clickUrl.startsWith("http")) {
            handleClickResult(mBaseAdContent.getClickUrl(), clickStatusCallback);
            return;
        }

        if (mIsClickAsync) {//async jump
            handleClickResult(mBaseAdContent.getPreviewUrl(), clickStatusCallback);
            isNeedJump = false;
        }

        if (isNeedJump) {
            String resultUrl = handleUrl302Result(clickUrl);
            handleClickResult(resultUrl, clickStatusCallback);
        }
    }

    /**
     * Open DeepLink url
     *
     * @param context
     * @param deepLinkUrl
     * @return
     */
//    private boolean openDeepLink(Context context, String deepLinkUrl) {
//        boolean openSuccessed = false;
//        try {
//            if (!TextUtils.isEmpty(deepLinkUrl)) {
//                Uri uri = Uri.parse(deepLinkUrl);
//                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//                intent.setData(uri);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                context.startActivity(intent);
//                openSuccessed = true;
//            }
//        } catch (Throwable t) {
//            CommonLogUtil.e(TAG, t.getMessage(), t);
//        }
//        return openSuccessed;
//    }


    /**
     * Handle url to redirect
     *
     * @param clickUrl
     * @return
     */
    private String handleUrl302Result(String clickUrl) {
        String startUrl = clickUrl;
        boolean success = false;
        for (int i = 0; i < MAX_JUMP_COUNT; i++) {
            try {
                URL serverUrl = new URL(startUrl);
                HttpURLConnection conn = (HttpURLConnection) serverUrl
                        .openConnection();
                conn.setRequestMethod("GET");
                // Must set to false. If not, it will auto redirect to Location
                conn.setInstanceFollowRedirects(false);

                conn.setConnectTimeout(30 * 1000);
                conn.connect();
                int responseCode = conn.getResponseCode();
                if (responseCode == 302) {
                    startUrl = conn.getHeaderField("Location");

                    if (OfferUrlHandler.isGooglePlayUrl(startUrl) || startUrl.contains(".apk") || !startUrl.startsWith("http")) {
                        success = true;
                    } else {
                        conn.disconnect();
                        continue;
                    }
                }
                conn.disconnect();

                if (success || responseCode == 200) {
                    return startUrl;
                }

                /**Fail to jump**/
                AgentEventManager.sendClickFailAgent(mPlacementId, mBaseAdContent.getOfferId(), mBaseAdContent.getOfferSourceType(), mBaseAdContent.getClickUrl(), startUrl, responseCode + "", "");
                return "";
            } catch (Exception e) {
                AgentEventManager.sendClickFailAgent(mPlacementId, mBaseAdContent.getOfferId(), mBaseAdContent.getOfferSourceType(), mBaseAdContent.getClickUrl(), startUrl, "", e.getMessage());
                break;
            }
        }
        return "";
    }


    /**
     * Handle the result of clicked
     *
     * @param finalUrl
     */
    private void handleClickResult(String finalUrl, ClickStatusCallback clickStatusCallback) {
        if (mIsCancel) {
            return;
        }
        finalUrl = TextUtils.isEmpty(finalUrl) ? mBaseAdContent.getPreviewUrl() : finalUrl;
        switch (mBaseAdContent.getClickType()) {
            case MARKET_TYPE:
                //TODO Test Open Market
                boolean isAppScheme = finalUrl != null && !finalUrl.startsWith("http");
                boolean isOpenSuccess = OfferUrlHandler.handleInAppOpenUrl(mContext, finalUrl, isAppScheme);
                if (!isOpenSuccess && !isAppScheme) {
                    OfferUrlHandler.openBrowserUrl(mContext, finalUrl);
                }
                break;
            case BROWSER_TYPE:
                if (TextUtils.isEmpty(finalUrl)) {
                    OfferUrlHandler.openBrowserUrl(mContext, mBaseAdContent.getClickUrl());
                } else {
                    OfferUrlHandler.openBrowserUrl(mContext, finalUrl);
                }

                break;
            case APK_TYPE:
                downloadApkOrOpenBrowser(finalUrl, clickStatusCallback);
                break;
            case INNER_BROWSER_TYPE:
                AdActivityStartParams adActivityStartParams = new AdActivityStartParams();
                adActivityStartParams.baseAdContent = mBaseAdContent;
                adActivityStartParams.baseAdSetting = mBaseAdSetting;
                adActivityStartParams.requestId = mRequestId;

                WebLandPageActivity.start(mContext, adActivityStartParams);
                break;
            default:
                if (TextUtils.isEmpty(finalUrl)) {
                    OfferUrlHandler.openBrowserUrl(mContext, mBaseAdContent.getClickUrl());
                } else {
                    OfferUrlHandler.openBrowserUrl(mContext, finalUrl);
                }
                break;
        }
        mIsClicking = false;
        if (clickStatusCallback != null) {
            clickStatusCallback.clickEnd();
        }

    }

    /**
     * Apk url open
     *
     * @param finalUrl
     * @param clickStatusCallback
     */
    private void downloadApkOrOpenBrowser(String finalUrl, ClickStatusCallback clickStatusCallback) {
        if (SDKContext.getInstance().getChinaHandler() != null) {
            if (!TextUtils.isEmpty(finalUrl) && finalUrl.endsWith(".apk")) {
                if (clickStatusCallback != null) {
                    clickStatusCallback.downloadApp(finalUrl);
                }
                return;
            }
        }
        OfferUrlHandler.openBrowserUrl(mContext, finalUrl);
    }


//    /**
//     * Check if it's GooglePlay's url
//     *
//     * @param url
//     * @return
//     */
//    private boolean isGooglePlayUrl(String url) {
//        try {
//            if (TextUtils.isEmpty(url)) {
//                return false;
//            }
//            Uri uri = Uri.parse(url);
//            if (uri == null || uri.getHost() == null) {
//                return false;
//            }
//            return uri.getHost().equals(HOST_GOOGLE_PLAY)
//                    || uri.getHost().equals(HOST_ANDROID_MARKET);
//        } catch (Throwable t) {
//        }
//        return false;
//    }
//
//    /**
//     * Convert to Market Url
//     *
//     * @param url
//     * @return
//     */
//    private String convertToMarketUrl(String url) {
//        try {
//            if (isGooglePlayUrl(url)) {
//                String detailsUrl = url.substring(url.indexOf(PATH_DETAILS));
//                return PROTOCOL_MARKET + detailsUrl;
//
//            }
//        } catch (Throwable e) {
//            if (Const.DEBUG) {
//                e.printStackTrace();
//            }
//        }
//        return null;
//    }

    public boolean isApkInstalled(Context context, String pkgName) {
        if (context == null || TextUtils.isEmpty(pkgName)) {
            return false;
        }
        try {
            ApplicationInfo info = context.getPackageManager()
                    .getApplicationInfo(pkgName, PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /**
     * Cance click
     */
    public void cancelClick() {
        mIsCancel = true;
    }

    public interface ClickStatusCallback {
        public void clickStart();

        public void clickEnd();

        public void downloadApp(String url);
    }
}
