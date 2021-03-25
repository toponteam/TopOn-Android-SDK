/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.buiness;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.anythink.basead.entity.OfferClickResult;
import com.anythink.basead.entity.UserOperateRecord;
import com.anythink.basead.net.MyOfferTkLoader;
import com.anythink.basead.net.NoticeUrlLoader;
import com.anythink.core.api.IExHandler;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.entity.AdxOffer;
import com.anythink.core.common.entity.BaseAdContent;
import com.anythink.core.common.entity.BaseAdRequestInfo;
import com.anythink.core.common.entity.BaseAdSetting;
import com.anythink.core.common.entity.MyOfferAd;
import com.anythink.core.common.entity.OwnBaseAdContent;
import com.anythink.core.common.entity.OwnBaseAdSetting;
import com.anythink.core.common.utils.task.TaskManager;


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
    public static final int VIDEO_RESUME_TYPE = 15;
    public static final int VIDEO_SKIP_TYPE = 16;
    public static final int VIDEO_ERROR_TYPE = 17;

    public static final int APK_DOWNLOAD_START_TYPE = 18;
    public static final int APK_DOWNLOAD_END_TYPE = 19;
    public static final int APK_INSTALL_START_TYPE = 20;
    public static final int APK_INSTALL_FINISH_TYPE = 21;
    public static final int APK_INSTALL_FINISH_AND_ACTIVE_TYPE = 22;

    public static final int APP_START_ACTIVE_TYPE = 23; //Only for Deeplink and Apk offer
    public static final int APP_ACTIVE_SUCCESS_TYPE = 24;//Only for Deeplink and Apk offer
    public static final int APP_HAS_INSTALL_TYPE = 25;
    public static final int APP_NO_INSTALL_TYPE = 26;
    public static final int APP_UNKOWN_TYPE = 27;

    /**Add by v5.7.7**/
    public static final int APP_DEEPLINK_INSTALLED_FAIL_TYPE = 28;
    public static final int APP_DEEPLINK_UNINSTALLED_FAIL_TYPE = 29;

    public static final int VIDEO_DOWNLOAD_SUCCESS_TYPE = 30;
    public static final int VIDEO_REWARDED_TYPE = 31;
    public static final int VIDEO_DIRECT_PROGRESS_TYPE = 32;

    public static boolean startDownloadApp(final Context context, final BaseAdRequestInfo baseAdRequestInfo, final BaseAdContent baseAdContent, final OfferClickResult clickResult, final String url) {
        try {
            IExHandler iexHandler = SDKContext.getInstance().getExHandler();
            String clickId = (clickResult != null && !TextUtils.isEmpty(clickResult.clickId)) ? clickResult.clickId : "";
            if (iexHandler != null) {
                iexHandler.handleOfferClick(context, baseAdRequestInfo, baseAdContent, url, clickId, new Runnable() {
                    @Override
                    public void run() {
                        if (baseAdContent instanceof OwnBaseAdContent) {
                            OfferStatusManager.getInstance(context.getApplicationContext()).registerOfferStatusBroadcastReceiver();
                            OfferStatusManager.getInstance(context.getApplicationContext()).register(baseAdContent.getOfferId(), baseAdContent);
                        }
                    }
                });
                return true;
            }
        } catch (Throwable e) {
            return false;
        }

        return false;
    }


    /**
     * UI Tracking API
     *
     * @param baseAdContent
     * @param tkType
     * @param userOperateRecord
     */
    public static void sendAdTracking(final int tkType, final BaseAdContent baseAdContent, @NonNull final UserOperateRecord userOperateRecord) {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (baseAdContent instanceof MyOfferAd) {
                    MyOfferAd myOfferAd = (MyOfferAd) baseAdContent;
                    if (tkType == OfferAdFunctionUtil.IMPRESSION_TYPE) {
                        new NoticeUrlLoader(myOfferAd.getNoticeUrl(), userOperateRecord.requestId).start(0, null);
                    }

                    MyOfferTkLoader myOfferTkLoader = new MyOfferTkLoader(tkType, myOfferAd, userOperateRecord.requestId);
                    myOfferTkLoader.setScenario(userOperateRecord.scenario);
                    myOfferTkLoader.start(0, null);
                } else {
                    OwnOfferTracker.sendAdTracking(tkType, (OwnBaseAdContent) baseAdContent, userOperateRecord);
                }
            }
        };

        if (Looper.getMainLooper() == Looper.myLooper()) {
            runnable.run();
        } else {
            TaskManager.getInstance().run_proxy(runnable);
        }

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
                if (baseAdSetting instanceof OwnBaseAdSetting) {
                    OwnBaseAdSetting adxAdSetting = (OwnBaseAdSetting) baseAdSetting;
                    result = adxAdSetting.getIpua() == 1;
                }
                break;
            case OfferAdFunctionUtil.CLICK_TYPE:
                if (baseAdSetting instanceof OwnBaseAdSetting) {
                    OwnBaseAdSetting adxAdSetting = (OwnBaseAdSetting) baseAdSetting;
                    result = adxAdSetting.getClua() == 1;
                }
                break;
        }

        return result;
    }

    public static boolean isClickAsync(BaseAdContent baseAdContent, BaseAdSetting baseAdSetting) {
        boolean isClickAsync = false;
        if (baseAdContent instanceof AdxOffer) {
            if (baseAdSetting instanceof OwnBaseAdSetting) {
                isClickAsync = ((OwnBaseAdSetting) baseAdSetting).getClickmode() == OfferClickController.ASYNC_MODE;
            }
        } else if (baseAdContent instanceof MyOfferAd) {
            isClickAsync = ((MyOfferAd) baseAdContent).getClickMode() == OfferClickController.ASYNC_MODE;
        }
        return isClickAsync;
    }

    public static boolean isApkInstalled(Context context, String pkgName) {
        if (context == null || TextUtils.isEmpty(pkgName)) {
            return false;
        }
        try {
            ApplicationInfo info = context.getPackageManager()
                    .getApplicationInfo(pkgName, PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        } catch (Throwable e) {
            return false;
        }
    }


}
