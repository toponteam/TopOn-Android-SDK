/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.buiness;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.anythink.basead.entity.AdActivityStartParams;
import com.anythink.basead.entity.ConversionRecord;
import com.anythink.basead.entity.OfferClickResult;
import com.anythink.basead.entity.UserOperateRecord;
import com.anythink.basead.innerad.onlineapi.utils.GDTClickUrlHandler;
import com.anythink.basead.ui.web.WebLandPageActivity;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.entity.BaseAdContent;
import com.anythink.core.common.entity.BaseAdRequestInfo;
import com.anythink.core.common.entity.OnlineApiOffer;
import com.anythink.core.common.track.AgentEventManager;
import com.anythink.core.common.utils.CommonUtil;
import com.anythink.core.common.utils.task.TaskManager;

import java.net.HttpURLConnection;
import java.net.URL;


/**
 * MyOffer Click Controller
 */

public class OfferClickController {
    private final String TAG = getClass().getSimpleName();

    /**
     * Deeplink Click Status
     */
    private final int DEEPLINK_NO_HANDLE = 0;
    private final int DEEPLINK_HANDLE_SUCCESS = 1;
    private final int DEEPLINK_HANDLE_FAIL = 2;

    private final int MAX_JUMP_COUNT = 10;
    private final int MARKET_TYPE = 1;
    private final int BROWSER_TYPE = 2;
    private final int INNER_BROWSER_TYPE = 3;
    private final int APK_TYPE = 4;
    public static final int SYNC_MODE = 0;
    public static final int ASYNC_MODE = 1;
    BaseAdContent mBaseAdContent;

    boolean mIsClicking;
    boolean mIsCancel;

    Context mContext;
    boolean mIsClickAsync;
    BaseAdRequestInfo mBaseAdRequestInfo;


    public OfferClickController(Context context, BaseAdRequestInfo baseAdRequestInfo, BaseAdContent ad) {
        mBaseAdContent = ad;
        mBaseAdRequestInfo = baseAdRequestInfo;
        mContext = context.getApplicationContext();
        mIsClickAsync = OfferAdFunctionUtil.isClickAsync(ad, baseAdRequestInfo.baseAdSetting);
    }


    /**
     * start click
     *
     * @param clickStatusCallback
     */

    public void startClick(final UserOperateRecord userOperateRecord, final ClickStatusCallback clickStatusCallback) {
        if (mIsClicking) {
            return;
        }

        if (clickStatusCallback != null) {
            clickStatusCallback.clickStart();
        }

        mIsClicking = true;
        mIsCancel = false;

        TaskManager.getInstance().run_proxy(new Runnable() {
            @Override
            public void run() {
                /**It will not continue to run if Jump url success **/
                if (jumpUrlClick(userOperateRecord, clickStatusCallback)) {
                    return;
                }

                int deeplinkHandleType = DEEPLINK_NO_HANDLE;

                if (mBaseAdRequestInfo.baseAdSetting.getDeeplinkMode() != 2) {
                    boolean isSuccess = deeplinckClick(userOperateRecord, clickStatusCallback);
                    deeplinkHandleType = isSuccess ? DEEPLINK_HANDLE_SUCCESS : DEEPLINK_HANDLE_FAIL;
                }

                /**
                 * If deeplinck success & deeplink mode == 1, it would not continue to handle click url
                 */
                if (deeplinkHandleType == DEEPLINK_HANDLE_SUCCESS && mBaseAdRequestInfo.baseAdSetting.getDeeplinkMode() == 1) {
                    return;
                }

                /**Open click url**/
                openClickUrl(deeplinkHandleType, userOperateRecord, clickStatusCallback);

            }
        });
    }

    private boolean jumpUrlClick(UserOperateRecord userOperateRecord, ClickStatusCallback clickStatusCallback) {
        userOperateRecord.conversionRecord = new ConversionRecord();
        OfferClickResult result = getOfferClickResult();
        userOperateRecord.conversionRecord.clickId = result != null ? result.clickId : "";
        if (!TextUtils.isEmpty(mBaseAdContent.getJumpUrl())) {
            /**If open deeplink success, it would continue to open click url.**/
            String jumpUrl = mBaseAdContent.getJumpUrl().replaceAll("\\{req_id\\}", mBaseAdRequestInfo.requestId == null ? "" : mBaseAdRequestInfo.requestId);
//            if (jumpUrl.startsWith("http")) {
//                jumpUrl = handleUrl302Result(jumpUrl, false);
//            }

            if (OfferUrlHandler.handleInAppOpenUrl(mContext, jumpUrl, false)) {
                AgentEventManager.sendDeepLinkAgent(mBaseAdRequestInfo.placementId, mBaseAdContent.getOfferId(), mBaseAdContent.getOfferSourceType(), jumpUrl, "1", 1);
                mIsClicking = false;
                if (clickStatusCallback != null) {
                    clickStatusCallback.clickEnd();
                    clickStatusCallback.deeplinkCallback(true);
                }

                return true;
            } else {
                AgentEventManager.sendDeepLinkAgent(mBaseAdRequestInfo.placementId, mBaseAdContent.getOfferId(), mBaseAdContent.getOfferSourceType(), jumpUrl, "0", 1);
            }
        }
        return false;
    }

    /**
     * Handle Deeplink Url
     *
     * @param clickStatusCallback
     * @return
     */
    private boolean deeplinckClick(UserOperateRecord userOperateRecord, ClickStatusCallback clickStatusCallback) {
        userOperateRecord.conversionRecord = new ConversionRecord();
        OfferClickResult result = getOfferClickResult();
        userOperateRecord.conversionRecord.clickId = result != null ? result.clickId : "";
        OfferAdFunctionUtil.sendAdTracking(OfferAdFunctionUtil.APP_START_ACTIVE_TYPE, mBaseAdContent, userOperateRecord);
//        && OfferAdFunctionUtil.isApkInstalled(mContext, mBaseAdContent.getPkgName()
        if (!TextUtils.isEmpty(mBaseAdContent.getDeeplinkUrl())) {
            /**If open deeplink success, it would continue to open click url.**/
            String deepLinkUrl = mBaseAdContent.getDeeplinkUrl().replaceAll("\\{req_id\\}", mBaseAdRequestInfo.requestId == null ? "" : mBaseAdRequestInfo.requestId);
//            if (deepLinkUrl.startsWith("http")) {
//                deepLinkUrl = handleUrl302Result(deepLinkUrl, false);
//            }

            if (OfferUrlHandler.handleInAppOpenUrl(mContext, deepLinkUrl, false)) {
                AgentEventManager.sendDeepLinkAgent(mBaseAdRequestInfo.placementId, mBaseAdContent.getOfferId(), mBaseAdContent.getOfferSourceType(), deepLinkUrl, "1", 0);
                mIsClicking = false;
                if (clickStatusCallback != null) {
                    clickStatusCallback.clickEnd();
                    clickStatusCallback.deeplinkCallback(true);
                }
                OfferAdFunctionUtil.sendAdTracking(OfferAdFunctionUtil.APP_ACTIVE_SUCCESS_TYPE, mBaseAdContent, userOperateRecord);
                return true;
            } else {
                //v5.7.7 Difference fail between Installed and UnInstalled
                if (OfferAdFunctionUtil.isApkInstalled(mContext, mBaseAdContent.getPkgName())) {
                    OfferAdFunctionUtil.sendAdTracking(OfferAdFunctionUtil.APP_DEEPLINK_INSTALLED_FAIL_TYPE, mBaseAdContent, userOperateRecord);
                } else {
                    OfferAdFunctionUtil.sendAdTracking(OfferAdFunctionUtil.APP_DEEPLINK_UNINSTALLED_FAIL_TYPE, mBaseAdContent, userOperateRecord);
                }
                AgentEventManager.sendDeepLinkAgent(mBaseAdRequestInfo.placementId, mBaseAdContent.getOfferId(), mBaseAdContent.getOfferSourceType(), deepLinkUrl, "0", 0);
            }
        }
        return false;
    }


    /**
     * Open Ad click url
     *
     * @param deeplinkHandleType
     * @param clickStatusCallback
     */
    private void openClickUrl(int deeplinkHandleType, UserOperateRecord userOperateRecord, final ClickStatusCallback clickStatusCallback) {
        String clickUrl = (mBaseAdContent.getClickUrl() != null ? mBaseAdContent.getClickUrl() : "").replaceAll("\\{req_id\\}", mBaseAdRequestInfo.requestId == null ? "" : mBaseAdRequestInfo.requestId);

        /**GDT Click url replace with ClickRecord Info**/
        clickUrl = OwnOfferTracker.replaceTrackUrlInfo(clickUrl, userOperateRecord, System.currentTimeMillis());

//        //TODO Test CLick url in cache
//        /**Get the final url**/
//        OfferClickResult offerClickResult = getOfferClickResult();
//        if (offerClickResult != null && !TextUtils.isEmpty(offerClickResult.resultUrl)) {
//            clickUrl = offerClickResult.resultUrl;
//            //Handle Final Result url
//            handleClickResult(clickUrl, deeplinkHandleType, clickStatusCallback);
//            return;
//        }

        OfferClickResult offerClickResult = null;

        if (isGDTOffer()
                && mBaseAdContent.getClickType() == APK_TYPE) {
            offerClickResult = new OfferClickResult("", "", "");
        } else {
            offerClickResult = new OfferClickResult(clickUrl, "", "");
        }

        saveOfferClickResult(offerClickResult);

        boolean isNeedJump = true; //Default need to open app page

        String resultUrl = "";

        switch (mBaseAdContent.getClickType()) {
            case BROWSER_TYPE:
            case INNER_BROWSER_TYPE:
                /**Force GDT Online API Handle**/
                if (isGDTOffer()
                        && !TextUtils.isEmpty(mBaseAdContent.getDeeplinkUrl())) {
                    resultUrl = handleUrl302Result(clickUrl, true);

                    String clickId = GDTClickUrlHandler.getBrandAdUrlResultClickId(resultUrl);
                    offerClickResult.resultUrl = resultUrl;
                    offerClickResult.clickId = clickId;
                    saveOfferClickResult(offerClickResult);

                }

                if (TextUtils.isEmpty(resultUrl)) {
                    resultUrl = offerClickResult.originUrl;
                }

                handleClickResult(resultUrl, deeplinkHandleType, userOperateRecord, clickStatusCallback);
                break;
            case APK_TYPE:
                //Force GDT Click Url
                if (isGDTOffer()
                        && TextUtils.isEmpty(offerClickResult.originUrl)) {
                    OfferClickResult gdtClickResult = GDTClickUrlHandler.handleApkClickUrlResult(mBaseAdRequestInfo, mBaseAdContent, clickUrl);
                    if (gdtClickResult != null) {
                        offerClickResult.originUrl = gdtClickResult.originUrl;
                        offerClickResult.clickId = gdtClickResult.clickId;
                    }
                }

                //Start Apk Url 302
                resultUrl = handleUrl302Result(offerClickResult.originUrl, true);
                offerClickResult.resultUrl = resultUrl;
                saveOfferClickResult(offerClickResult);

                if (TextUtils.isEmpty(resultUrl)) {
                    resultUrl = offerClickResult.originUrl;
                }
                handleClickResult(resultUrl, deeplinkHandleType, userOperateRecord, clickStatusCallback);

                break;
            case MARKET_TYPE:
                if (!clickUrl.startsWith("http")) {
                    handleClickResult(clickUrl, deeplinkHandleType, userOperateRecord, clickStatusCallback);
                    return;
                }

                if (mIsClickAsync) {//async jump
                    handleClickResult(mBaseAdContent.getPreviewUrl(), deeplinkHandleType, userOperateRecord, clickStatusCallback);
                    isNeedJump = false;
                }

                resultUrl = handleUrl302Result(clickUrl, true);
                if (isNeedJump) {
                    if (TextUtils.isEmpty(resultUrl)) {
                        resultUrl = offerClickResult.originUrl;
                    }
                    handleClickResult(resultUrl, deeplinkHandleType, userOperateRecord, clickStatusCallback);
                }
                break;
            default:
                if (TextUtils.isEmpty(resultUrl)) {
                    resultUrl = offerClickResult.originUrl;
                }
                handleClickResult(resultUrl, deeplinkHandleType, userOperateRecord, clickStatusCallback);
                break;
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
    private String handleUrl302Result(final String clickUrl, boolean isClickUrl) {
        /**Get the final url**/
        String startUrl = clickUrl;
        boolean success = false;
        for (int i = 0; i < MAX_JUMP_COUNT; i++) {
            HttpURLConnection conn = null;
            try {
                URL serverUrl = new URL(startUrl);
                conn = (HttpURLConnection) serverUrl
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

                if (success || responseCode == 200) {
                    return startUrl;
                }

                /**Fail to jump**/
                AgentEventManager.sendClickFailAgent(mBaseAdRequestInfo.placementId, mBaseAdContent.getOfferId(), mBaseAdContent.getOfferSourceType(), clickUrl, startUrl, responseCode + "", "");
                return "";
            } catch (Exception e) {
                AgentEventManager.sendClickFailAgent(mBaseAdRequestInfo.placementId, mBaseAdContent.getOfferId(), mBaseAdContent.getOfferSourceType(), clickUrl, startUrl, "", e.getMessage());
                break;
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }
        return "";
    }

    /**
     * Handle GDT Online Api Click
     *
     * @param startUrl
     * @param conn
     * @return
     */
//    private OfferClickResult handleGDTClickResult(String startUrl, HttpURLConnection conn) {
//        OfferClickResult offerClickResult = null;
//        if (mBaseAdContent instanceof OnlineApiOffer && ((OnlineApiOffer) mBaseAdContent).getNetworkFirmId() == Const.NETWORK_FIRM.GDT_ONLINE) {
//            if (!TextUtils.isEmpty(mBaseAdContent.getDeeplinkUrl())
//                    && (mBaseAdContent.getClickType() == BROWSER_TYPE || mBaseAdContent.getClickType() == INNER_BROWSER_TYPE)) {
//                Uri uri = Uri.parse(startUrl);
//                String clickId = uri.getQueryParameter("qz_gdt");
//                offerClickResult = new OfferClickResult(startUrl, clickId);
//            } else if (mBaseAdContent.getClickType() == APK_TYPE) {
//                try {
//                    InputStream inputStream = conn.getInputStream();
//                    InputStreamReader reader = new InputStreamReader(inputStream);
//                    BufferedReader input = new BufferedReader(reader);
//                    String s;
//                    StringBuilder sb = new StringBuilder();
//                    while ((s = input.readLine()) != null) {
//                        sb.append(s);
//                    }
//
//                    JSONObject jsonObject = new JSONObject(sb.toString());
//                    JSONObject dataObject = jsonObject.optJSONObject("data");
//                    String dslink = dataObject.optString("dstlink");
//                    String clickId = dataObject.optString("clickid");
//                    offerClickResult = new OfferClickResult(dslink, clickId);
//                    if (input != null) {
//                        input.close();
//                    }
//                    if (reader != null) {
//                        reader.close();
//                    }
//                    if (inputStream != null) {
//                        inputStream.close();
//                    }
//                } catch (Throwable e) {
//
//                }
//
//            }
//        }
//
//        return offerClickResult;
//    }

    /**
     * Save Click Result
     *
     * @param offerClickResult
     */
    private void saveOfferClickResult(OfferClickResult offerClickResult) {
        OfferClickResultManager.getInstance().putOfferClickResult(mBaseAdContent.getOfferSourceType(), mBaseAdContent.getOfferId(), offerClickResult);
    }

    /**
     * Get Click Result
     */
    private OfferClickResult getOfferClickResult() {
        return OfferClickResultManager.getInstance().getOfferClickResult(mBaseAdContent.getOfferSourceType(), mBaseAdContent.getOfferId());
    }


    /**
     * Handle the result of clicked
     *
     * @param finalUrl
     */
    private void handleClickResult(String finalUrl, int deeplinkHandleType, UserOperateRecord userOperateRecord, ClickStatusCallback clickStatusCallback) {
        if (mIsCancel) {
            mIsClicking = false;
            if (!TextUtils.isEmpty(mBaseAdContent.getJumpUrl()) || !TextUtils.isEmpty(mBaseAdContent.getDeeplinkUrl())) {
                if (clickStatusCallback != null) {
                    clickStatusCallback.deeplinkCallback(false);
                }
            }

            if (clickStatusCallback != null) {
                clickStatusCallback.clickEnd();
            }
            return;
        }

        /**If deeplink success, it would not handle click url result.**/
        if (deeplinkHandleType == DEEPLINK_HANDLE_SUCCESS) {
            return;
        }

        if (deeplinkHandleType == DEEPLINK_NO_HANDLE) {
            if (deeplinckClick(userOperateRecord, clickStatusCallback)) return;
        }

        if (!TextUtils.isEmpty(mBaseAdContent.getJumpUrl()) || !TextUtils.isEmpty(mBaseAdContent.getDeeplinkUrl())) {
            if (clickStatusCallback != null) {
                clickStatusCallback.deeplinkCallback(false);
            }
        }

        //If App Ad had been installed, it would open app.
        if (!TextUtils.isEmpty(mBaseAdContent.getPkgName())) {
            boolean isSuccess = openApp(mContext, mBaseAdContent.getPkgName());
            userOperateRecord.conversionRecord = new ConversionRecord();
            OfferClickResult result = getOfferClickResult();
            userOperateRecord.conversionRecord.clickId = result != null ? result.clickId : "";

            if (isSuccess) {
                OfferAdFunctionUtil.sendAdTracking(OfferAdFunctionUtil.APP_HAS_INSTALL_TYPE, mBaseAdContent, userOperateRecord);
                mIsClicking = false;
                if (clickStatusCallback != null) {
                    clickStatusCallback.clickEnd();
                }
                return;
            } else {
                OfferAdFunctionUtil.sendAdTracking(OfferAdFunctionUtil.APP_NO_INSTALL_TYPE, mBaseAdContent, userOperateRecord);
            }
        }

        finalUrl = TextUtils.isEmpty(finalUrl) ? mBaseAdContent.getPreviewUrl() : finalUrl;

        if (TextUtils.isEmpty(finalUrl)) {
            Log.e(Const.RESOURCE_HEAD, "Offer click result is null.");
            SDKContext.getInstance().runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (TextUtils.isEmpty(mBaseAdContent.getClickUrl())) {
                            Toast.makeText(mContext, CommonUtil.getResId(mContext, "basead_click_empty", "string"), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(mContext, CommonUtil.getResId(mContext, "basead_click_fail", "string"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Throwable e) {

                    }

                }
            });
            mIsClicking = false;
            if (clickStatusCallback != null) {
                clickStatusCallback.clickEnd();
            }
            return;
        }

        switch (mBaseAdContent.getClickType()) {
            case MARKET_TYPE:
                boolean isAppScheme = finalUrl != null && !finalUrl.startsWith("http");
                boolean isOpenSuccess = OfferUrlHandler.handleInAppOpenUrl(mContext, finalUrl, isAppScheme);
                if (!isOpenSuccess && !isAppScheme) {
                    if (mBaseAdRequestInfo.baseAdSetting.getLoadType() == 2) {
                        AdActivityStartParams adActivityStartParams = new AdActivityStartParams();
                        adActivityStartParams.baseAdContent = mBaseAdContent;
                        adActivityStartParams.baseAdRequestInfo = mBaseAdRequestInfo;
                        adActivityStartParams.targetUrl = finalUrl;

                        WebLandPageActivity.start(mContext, adActivityStartParams);
                    } else {
                        OfferUrlHandler.openBrowserUrl(mContext, finalUrl);
                    }

                }
                break;
            case BROWSER_TYPE:
                OfferUrlHandler.openBrowserUrl(mContext, finalUrl);

                break;
            case APK_TYPE:
                downloadApkOrOpenBrowser(finalUrl);
                break;
            case INNER_BROWSER_TYPE:
                AdActivityStartParams adActivityStartParams = new AdActivityStartParams();
                adActivityStartParams.baseAdContent = mBaseAdContent;
                adActivityStartParams.baseAdRequestInfo = mBaseAdRequestInfo;
                adActivityStartParams.targetUrl = finalUrl;

                WebLandPageActivity.start(mContext, adActivityStartParams);
                break;
            default:
                if (mBaseAdRequestInfo.baseAdSetting.getLoadType() == 2) {
                    AdActivityStartParams innterAdActivityStartParams = new AdActivityStartParams();
                    innterAdActivityStartParams.baseAdContent = mBaseAdContent;
                    innterAdActivityStartParams.baseAdRequestInfo = mBaseAdRequestInfo;
                    innterAdActivityStartParams.targetUrl = finalUrl;

                    WebLandPageActivity.start(mContext, innterAdActivityStartParams);
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
     */
    private void downloadApkOrOpenBrowser(final String finalUrl) {
        if (!TextUtils.isEmpty(finalUrl) && finalUrl.contains(".apk")) {
            if (!OfferAdFunctionUtil.startDownloadApp(mContext, mBaseAdRequestInfo, mBaseAdContent, getOfferClickResult(), finalUrl)) {
                OfferUrlHandler.openBrowserUrl(mContext, finalUrl);
            }
        } else {
            OfferUrlHandler.openBrowserUrl(mContext, finalUrl);
        }
    }


    public static boolean openApp(Context context, String pkgName) {
        try {
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(pkgName);
            if (intent != null) {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                return true;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean isGDTOffer() {
        return mBaseAdContent.getOfferFirmId() == Const.NETWORK_FIRM.GDT_ONLINE || (mBaseAdContent instanceof OnlineApiOffer
                && ((OnlineApiOffer) mBaseAdContent).getNetworkFirmId() == Const.NETWORK_FIRM.GDT_ONLINE);
    }

    /**
     * Cancel click
     */
    public void cancelClick() {
        mIsCancel = true;
    }

    public interface ClickStatusCallback {
        void clickStart();

        void clickEnd();

        void deeplinkCallback(boolean isSuccess);

    }
}
