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
import android.os.Looper;
import android.text.TextUtils;
import android.util.TypedValue;

import com.anythink.basead.net.AdxNoticeUrlLoader;
import com.anythink.basead.net.AdxOfferTkLoader;
import com.anythink.basead.net.MyOfferTkLoader;
import com.anythink.basead.net.NoticeUrlLoader;
import com.anythink.basead.ui.ApkConfirmDialogActivity;
import com.anythink.china.common.ApkDownloadManager;
import com.anythink.china.common.download.ApkRequest;
import com.anythink.china.common.resource.ApkResource;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.entity.AdxAdSetting;
import com.anythink.core.common.entity.AdxOffer;
import com.anythink.core.common.entity.AdxTrackObject;
import com.anythink.core.common.entity.BaseAdContent;
import com.anythink.core.common.entity.BaseAdSetting;
import com.anythink.core.common.entity.MyOfferAd;
import com.anythink.core.common.res.ImageLoader;
import com.anythink.core.common.res.ResourceEntry;
import com.anythink.core.common.utils.CommonUtil;
import com.anythink.core.common.utils.task.TaskManager;

import org.json.JSONObject;

import java.util.Map;

public class OfferAdFunctionUtil {

    public static final int VIDEO_START_TYPE = 1;
    public static final int VIDEO_PROGRESS25_TYPE = 2;
    public static final int VIDEO_PROGRESS50_TYPE = 3;
    public static final int VIDEO_PROGRESS75_TYPE = 4;
    public static final int VIDEO_FINISH_TYPE = 5;
    public static final int ENDCARD_SHOW_TYPE = 6;
    public static final int ENDCARD_CLOSE_TYPE = 7;
    public static final int IMPRESSION_TYPE = 8;
    public static final int CLICK_TYPE = 9;

    public static final int NOTICE_WIN_TYPE = 10;
    public static final int VIDEO_PAUSE_TYPE = 11;
    public static final int VIDEO_MUTE_TYPE = 12;
    public static final int VIDEO_NO_MUTE_TYPE = 13;
    public static final int VIDEO_CLICK_TYPE = 14;
    public static final int APK_DOWNLOAD_START_TYPE = 15;
    public static final int APK_DOWNLOAD_END_TYPE = 16;
    public static final int APK_INSTALL_TYPE = 17;


    public static void startDownloadApp(final Context context, final String requestId, final BaseAdSetting myOfferSetting, final BaseAdContent myOfferAd, final String url) {
        SDKContext.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (SDKContext.getInstance().getChinaHandler() != null) {
                    if (1 == myOfferSetting.getApkDownloadConfirm()) {
                        ApkConfirmDialogActivity.start(context, requestId, myOfferSetting, myOfferAd, url);
                    } else {
                        realStartDownloadApp(context, requestId, myOfferSetting, myOfferAd, url);
                    }
                } else {
                    OfferUrlHandler.openBrowserUrl(context, url);
                }

            }
        });

    }

    public static void realStartDownloadApp(final Context context, final String requestId, final BaseAdSetting myOfferSetting, final BaseAdContent myOfferAd, final String url) {
        if (ApkResource.isApkInstalled(SDKContext.getInstance().getContext(), myOfferAd.getPkgName())) {
            //App was installed， open it
            ApkResource.openApp(SDKContext.getInstance().getContext(), myOfferAd.getPkgName());
        } else {
            //App not exist, download it
            ApkRequest apkRequest = new ApkRequest();
            apkRequest.requestId = requestId;
            apkRequest.offerId = myOfferAd.getOfferId();
            apkRequest.url = url;
            apkRequest.pkgName = myOfferAd.getPkgName();
            apkRequest.title = myOfferAd.getTitle();
            int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, SDKContext.getInstance().getContext().getResources().getDisplayMetrics());
            apkRequest.icon = ImageLoader.getInstance(context).getBitmapFromDiskCache(new ResourceEntry(ResourceEntry.INTERNAL_CACHE_TYPE, myOfferAd.getIconUrl()), size, size);


            ApkDownloadManager.getInstance(SDKContext.getInstance().getContext()).setOfferCacheTime(myOfferSetting.getOfferCacheTime());
            ApkDownloadManager.getInstance(SDKContext.getInstance().getContext()).checkAndCleanApk();
            ApkDownloadManager.getInstance(SDKContext.getInstance().getContext()).handleClick(apkRequest);
        }
    }


    public static void sendAdTracking(final String requestId, final BaseAdContent baseAdContent, final int tkType, final String scenario) {
        TaskManager.getInstance().run_proxy(new Runnable() {
            @Override
            public void run() {
                if (baseAdContent instanceof MyOfferAd) {
                    MyOfferAd myOfferAd = (MyOfferAd) baseAdContent;
                    if (tkType == OfferAdFunctionUtil.IMPRESSION_TYPE) {
                        new NoticeUrlLoader(myOfferAd.getNoticeUrl(), requestId).start(0, null);
                    }

                    MyOfferTkLoader myOfferTkLoader = new MyOfferTkLoader(tkType, myOfferAd, requestId);
                    myOfferTkLoader.setScenario(scenario);
                    myOfferTkLoader.start(0, null);
                } else {
                    sendAdxAdTracking(tkType, baseAdContent, scenario);
                }
            }
        });
    }

    public static void sendAdxAdTracking(final int tkType, final BaseAdContent baseAdContent) {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            TaskManager.getInstance().run_proxy(new Runnable() {
                @Override
                public void run() {
                    sendAdxAdTracking(tkType, baseAdContent, "");
                }
            });
        } else {
            sendAdxAdTracking(tkType, baseAdContent, "");
        }
    }

    private static void sendAdxAdTracking(final int tkType, final BaseAdContent baseAdContent, final String scenario) {
        if (baseAdContent instanceof AdxOffer) {
            AdxOffer adxOffer = (AdxOffer) baseAdContent;
            AdxTrackObject adxTrackObject = adxOffer.getAdxTrackObject();
            String replaceJSONString = adxTrackObject.getReplaceJSONString();

            Map<String, Object> replaceMap = CommonUtil.jsonObjectToMap(replaceJSONString);

            sendAdxNoticeUrl(tkType, adxOffer, adxTrackObject, replaceMap);

            sendAdxTopOnTracking(tkType, scenario, adxOffer, adxTrackObject, replaceMap);
        }
    }

    private static void sendAdxNoticeUrl(int tkType, AdxOffer adxOffer, AdxTrackObject adxTrackObject, Map<String, Object> replaceMap) {

        String[] urls = null;
        try {
            switch (tkType) {
                case OfferAdFunctionUtil.VIDEO_START_TYPE:
                    urls = adxTrackObject.getVideoStartUrls();
                    break;
                case OfferAdFunctionUtil.VIDEO_PROGRESS25_TYPE:
                    urls = adxTrackObject.getVideoProgress25Urls();
                    break;
                case OfferAdFunctionUtil.VIDEO_PROGRESS50_TYPE:
                    urls = adxTrackObject.getVideoProgress50Urls();
                    break;
                case OfferAdFunctionUtil.VIDEO_PROGRESS75_TYPE:
                    urls = adxTrackObject.getVideoProgress75Urls();
                    break;
                case OfferAdFunctionUtil.VIDEO_FINISH_TYPE:
                    urls = adxTrackObject.getVideoProgress100Urls();
                    break;
                case OfferAdFunctionUtil.ENDCARD_SHOW_TYPE:
                    urls = adxTrackObject.getEndcardShowUrls();
                    break;
                case OfferAdFunctionUtil.ENDCARD_CLOSE_TYPE:
                    urls = adxTrackObject.getEndcardCloseUrls();
                    break;
                case OfferAdFunctionUtil.IMPRESSION_TYPE:
                    urls = adxTrackObject.getImpressionUrls();
                    break;
                case OfferAdFunctionUtil.CLICK_TYPE:
                    urls = adxTrackObject.getClickUrls();
                    break;
                case OfferAdFunctionUtil.NOTICE_WIN_TYPE:
                    urls = adxTrackObject.getNoticeWinUrls();
                    break;
                case OfferAdFunctionUtil.VIDEO_PAUSE_TYPE:
                    urls = adxTrackObject.getVideoPauseUrls();
                    break;
                case OfferAdFunctionUtil.VIDEO_MUTE_TYPE:
                    urls = adxTrackObject.getVideoMuteUrls();
                    break;
                case OfferAdFunctionUtil.VIDEO_NO_MUTE_TYPE:
                    urls = adxTrackObject.getVideoVoiceUrls();
                    break;
                case OfferAdFunctionUtil.APK_DOWNLOAD_START_TYPE:
                    urls = adxTrackObject.getApkDownloadStartUrls();
                    break;
                case OfferAdFunctionUtil.APK_DOWNLOAD_END_TYPE:
                    urls = adxTrackObject.getApkDownloadEndUrls();
                    break;
                case OfferAdFunctionUtil.APK_INSTALL_TYPE:
                    urls = adxTrackObject.getApkInstallUrls();
                    break;
                case OfferAdFunctionUtil.VIDEO_CLICK_TYPE:
                    urls = adxTrackObject.getVideoClickUrls();
                    break;
            }

            if (urls != null) {
                for (String url : urls) {
                    new AdxNoticeUrlLoader(tkType, url, adxOffer, replaceMap).start(0, null);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    private static void sendAdxTopOnTracking(int tkType, String scenario, AdxOffer adxOffer, AdxTrackObject adxTrackObject, Map<String, Object> replaceMap) {
        String trackUrlJSONString = "";
        switch (tkType) {
            case OfferAdFunctionUtil.VIDEO_START_TYPE:
                trackUrlJSONString = adxTrackObject.getTpVideoStartJSONString();
                break;
            case OfferAdFunctionUtil.VIDEO_PROGRESS25_TYPE:
                trackUrlJSONString = adxTrackObject.getTpVideoProgress25JSONString();
                break;
            case OfferAdFunctionUtil.VIDEO_PROGRESS50_TYPE:
                trackUrlJSONString = adxTrackObject.getTpVideoProgress50JSONString();
                break;
            case OfferAdFunctionUtil.VIDEO_PROGRESS75_TYPE:
                trackUrlJSONString = adxTrackObject.getTpVideoProgress75JSONString();
                break;
            case OfferAdFunctionUtil.VIDEO_FINISH_TYPE:
                trackUrlJSONString = adxTrackObject.getTpVideoProgress100JSONString();
                break;
            case OfferAdFunctionUtil.ENDCARD_SHOW_TYPE:
                trackUrlJSONString = adxTrackObject.getTpEndcardShowJSONString();
                break;
            case OfferAdFunctionUtil.ENDCARD_CLOSE_TYPE:
                trackUrlJSONString = adxTrackObject.getTpEndcardCloseJSONString();
                break;
            case OfferAdFunctionUtil.IMPRESSION_TYPE:
                trackUrlJSONString = adxTrackObject.getTpImpressionJSONString();
                break;
            case OfferAdFunctionUtil.CLICK_TYPE:
                trackUrlJSONString = adxTrackObject.getTpClickJSONString();
                break;
            case OfferAdFunctionUtil.NOTICE_WIN_TYPE:
                trackUrlJSONString = adxTrackObject.getTpNoticeWinJSONString();
                break;
            case OfferAdFunctionUtil.VIDEO_PAUSE_TYPE:
                trackUrlJSONString = adxTrackObject.getTpVideoPauseJSONString();
                break;
            case OfferAdFunctionUtil.VIDEO_MUTE_TYPE:
                trackUrlJSONString = adxTrackObject.getTpVideoMuteJSONString();
                break;
            case OfferAdFunctionUtil.VIDEO_NO_MUTE_TYPE:
                trackUrlJSONString = adxTrackObject.getTpVideoVoiceJSONStrings();
                break;
            case OfferAdFunctionUtil.APK_DOWNLOAD_START_TYPE:
                trackUrlJSONString = adxTrackObject.getTpApkDownloadStartJSONString();
                break;
            case OfferAdFunctionUtil.APK_DOWNLOAD_END_TYPE:
                trackUrlJSONString = adxTrackObject.getTpApkDownloadEndJSONString();
                break;
            case OfferAdFunctionUtil.APK_INSTALL_TYPE:
                trackUrlJSONString = adxTrackObject.getTpApkInstallJSONString();
                break;
            case OfferAdFunctionUtil.VIDEO_CLICK_TYPE:
                trackUrlJSONString = adxTrackObject.getTpVideoClickJSONString();
                break;
        }

        if (checkAdxTrackingIsEmpty(trackUrlJSONString)) {
            return;
        }

        AdxOfferTkLoader adxOfferTkLoader = new AdxOfferTkLoader(tkType, adxOffer, trackUrlJSONString, replaceMap);
        adxOfferTkLoader.setScenario(scenario);
        adxOfferTkLoader.start(0, null);
    }

    public static boolean isUploadUserAgent(int tkType, BaseAdSetting baseAdSetting) {
        boolean result = false;

        switch (tkType) {
            case OfferAdFunctionUtil.IMPRESSION_TYPE:
            case OfferAdFunctionUtil.VIDEO_START_TYPE:
            case OfferAdFunctionUtil.VIDEO_PROGRESS25_TYPE:
            case OfferAdFunctionUtil.VIDEO_PROGRESS50_TYPE:
            case OfferAdFunctionUtil.VIDEO_PROGRESS75_TYPE:
            case OfferAdFunctionUtil.VIDEO_FINISH_TYPE:
                if (baseAdSetting instanceof AdxAdSetting) {
                    AdxAdSetting adxAdSetting = (AdxAdSetting) baseAdSetting;
                    result = adxAdSetting.getIpua() == 1;
                }
                break;
            case OfferAdFunctionUtil.CLICK_TYPE:
                if (baseAdSetting instanceof AdxAdSetting) {
                    AdxAdSetting adxAdSetting = (AdxAdSetting) baseAdSetting;
                    result = adxAdSetting.getClua() == 1;
                }
                break;
        }

        return result;
    }

    public static boolean isClickAsync(BaseAdContent baseAdContent, BaseAdSetting baseAdSetting) {
        boolean isClickAsync = false;
        if (baseAdContent instanceof AdxOffer) {
            if (baseAdSetting instanceof AdxAdSetting) {
                isClickAsync = ((AdxAdSetting) baseAdSetting).getClickmode() == OfferClickController.ASYNC_MODE;
            }
        } else if (baseAdContent instanceof MyOfferAd) {
            isClickAsync = ((MyOfferAd) baseAdContent).getClickMode() == OfferClickController.ASYNC_MODE;
        }
        return isClickAsync;
    }

    private static boolean checkAdxTrackingIsEmpty(String trackString) {
        if (TextUtils.isEmpty(trackString)) {
            return true;
        }
        try {
            JSONObject jsonObject = new JSONObject(trackString);
            if (jsonObject.length() > 0) {
                return false;
            }
        } catch (Throwable e) {

        }
        return true;
    }

}
