package com.anythink.myoffer.buiness;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.Toast;

import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.utils.task.TaskManager;
import com.anythink.myoffer.entity.MyOfferAd;

import java.net.HttpURLConnection;
import java.net.URL;


/**
 * MyOffer Click Controller
 */

public class OfferClickController {

    public static final String HOST_GOOGLE_PLAY = "play.google.com";
    public static final String HOST_ANDROID_MARKET = "market.android.com";
    public static final String PATH_DETAILS = "details?";
    public static final String SCHEME_MARKET = "market";
    public static final String PROTOCOL_MARKET = SCHEME_MARKET + "://";

    private final int MAX_JUMP_COUNT = 10;
    private final int MARKET_TYPE = 1;
    private final int BROWSER_TYPE = 2;
    private final int APK_TYPE = 4;
    private final int SYNC_MODE = 0;
    private final int ASYNC_MODE = 1;
    MyOfferAd mMyOfferAd;

    boolean mIsClicking;
    boolean mIsCancel;

    Context mContext;

    public OfferClickController(Context context, MyOfferAd ad) {
        mMyOfferAd = ad;
        mContext = context.getApplicationContext();
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
        if (clickStatusCallback != null) {
            clickStatusCallback.clickStart();
        }

        mIsClicking = true;
        mIsCancel = false;
        final String clickUrl = (mMyOfferAd.getClickUrl() != null ? mMyOfferAd.getClickUrl() : "").replaceAll("\\{req_id\\}", requestId == null ? "" : requestId);

        if (mMyOfferAd.getClickType() != MARKET_TYPE && mMyOfferAd.getClickType() != APK_TYPE) {//如果是非市场，直接使用浏览器打开
            openBrowserUrl(clickUrl);
            mIsClicking = false;
            if (clickStatusCallback != null) {
                clickStatusCallback.clickEnd();
            }
            return;
        }

        if (clickUrl.endsWith(".apk")) {
            handleClickResult(clickUrl, clickStatusCallback);
            mIsClicking = false;
            return;
        }

        if (!clickUrl.startsWith("http")) {
            handleClickResult(mMyOfferAd.getClickUrl(), clickStatusCallback);
            mIsClicking = false;
            return;
        }

        if (mMyOfferAd.getClickMode() == ASYNC_MODE) {//异步跳转
            handleClickResult(mMyOfferAd.getPreviewUrl(), clickStatusCallback);
            startToHandleClickUrl(null, clickUrl, false);
            return;
        }

        startToHandleClickUrl(clickStatusCallback, clickUrl, true);
    }

    /**
     * @param clickStatusCallback
     * @param clickUrl
     */
    private void startToHandleClickUrl(final ClickStatusCallback clickStatusCallback, final String clickUrl, final boolean isNeedJump) {
        TaskManager.getInstance().run_proxy(new Runnable() {
            @Override
            public void run() {
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

                            if ((isGooglePlayUrl(startUrl) || startUrl.endsWith(".apk"))) {
                                success = true;
                            } else {
                                conn.disconnect();
                                continue;
                            }
                        }
                        conn.disconnect();

                        if (success || responseCode == 200) {
                            if (isNeedJump) {
                                handleClickResult(startUrl, clickStatusCallback);
                            }
                            mIsClicking = false;
                            break;
                        }
                        /**Fail to jump**/
                        if (isNeedJump) {
                            handleClickResult("", clickStatusCallback);
                        }
                        mIsClicking = false;
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                        break;
                    }
                }

                if (mIsClicking) {
                    /**Fail to jump**/
                    if (isNeedJump) {
                        handleClickResult("", clickStatusCallback);
                    }
                    mIsClicking = false;
                }
            }
        });
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
        finalUrl = TextUtils.isEmpty(finalUrl) ? mMyOfferAd.getPreviewUrl() : finalUrl;
        switch (mMyOfferAd.getClickType()) {
            case MARKET_TYPE:
                if (!finalUrl.startsWith("http")) {
                    openMarketApp(finalUrl);
                } else {
                    String googleMarketUrl = convertToMarketUrl(finalUrl);
                    if (!TextUtils.isEmpty(googleMarketUrl)) {
                        openMarketApp(googleMarketUrl);
                    } else {
                        openBrowserUrl(finalUrl);
                    }
                }
                break;
            case BROWSER_TYPE:
                openBrowserUrl(finalUrl);
                break;
            case APK_TYPE:
                downloadApkOrOpenBrowser(finalUrl, clickStatusCallback);
                break;
            default:
                openBrowserUrl(finalUrl);
                break;
        }
        if (clickStatusCallback != null) {
            clickStatusCallback.clickEnd();
        }

    }

    private void downloadApkOrOpenBrowser(String finalUrl, ClickStatusCallback clickStatusCallback) {
        if (SDKContext.getInstance().getChinaHandler() != null) {
            if (!TextUtils.isEmpty(finalUrl) && finalUrl.endsWith(".apk")) {
                if (clickStatusCallback != null) {
                    clickStatusCallback.downloadApp(finalUrl);
                }
                return;
            }
        }
        openBrowserUrl(finalUrl);
    }

    /**
     * Open Markey
     *
     * @param googleMarketUrl
     */
    private void openMarketApp(String googleMarketUrl) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(googleMarketUrl));
            intent.setData(Uri.parse(googleMarketUrl));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        } catch (Throwable e) {
            SDKContext.getInstance().runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, "Detect that the App Market is not installed and cannot be opened through the App Marke", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    /**
     * Open Browser
     *
     * @param finalUrl
     */
    private void openBrowserUrl(String finalUrl) {
        try {
            Intent intent = new Intent("android.intent.action.VIEW",
                    Uri.parse(finalUrl));
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
                    | Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
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
    private boolean isGooglePlayUrl(String url) {
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
    private String convertToMarketUrl(String url) {
        try {
            if (isGooglePlayUrl(url)) {
                String detailsUrl = url.substring(url.indexOf(PATH_DETAILS));
                return PROTOCOL_MARKET + detailsUrl;

            }
        } catch (Throwable e) {
            if (Const.DEBUG) {
                e.printStackTrace();
            }
        }
        return null;
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
