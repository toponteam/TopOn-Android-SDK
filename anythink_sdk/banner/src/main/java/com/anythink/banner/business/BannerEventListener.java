/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.banner.business;

import com.anythink.banner.unitgroup.api.CustomBannerAdapter;
import com.anythink.banner.unitgroup.api.CustomBannerEventListener;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.entity.AdTrackingInfo;
import com.anythink.core.common.net.TrackingV2Loader;
import com.anythink.core.common.track.AdTrackingManager;
import com.anythink.core.common.track.AgentEventManager;
import com.anythink.core.common.utils.CommonSDKUtil;

public class BannerEventListener implements CustomBannerEventListener {

    InnerBannerListener listener;
    boolean isRefresh;
    CustomBannerAdapter bannerAdapter;

    public BannerEventListener(InnerBannerListener bannerListener, CustomBannerAdapter bannerAdapter, boolean isRefresh){
        this.isRefresh = isRefresh;
        listener = bannerListener;
        this.bannerAdapter = bannerAdapter;
    }

    @Override
    public void onBannerAdClose() {
        if (bannerAdapter != null) {
            if (listener != null) {
                listener.onBannerClose(isRefresh, bannerAdapter);
            }
            AdTrackingInfo adTrackingInfo = bannerAdapter.getTrackingInfo();

            /**Debug log**/
            CommonSDKUtil.printAdTrackingInfoStatusLog(adTrackingInfo, Const.LOGKEY.CLOSE, Const.LOGKEY.SUCCESS, "");

            if (adTrackingInfo != null) {
                AgentEventManager.onAdCloseAgent(adTrackingInfo, false);
            }

        }

    }

    @Override
    public void onBannerAdShow() {
    }

    @Override
    public void onBannerAdClicked() {

        if (bannerAdapter != null) {
            AdTrackingInfo adTrackingInfo = bannerAdapter.getTrackingInfo();
            //Ad Tracking
            AdTrackingManager.getInstance(SDKContext.getInstance().getContext()).addAdTrackingInfo(TrackingV2Loader.AD_CLICK_TYPE, adTrackingInfo);

            /**Debug log**/
            CommonSDKUtil.printAdTrackingInfoStatusLog(adTrackingInfo, Const.LOGKEY.CLICK, Const.LOGKEY.SUCCESS, "");

            if (listener != null) {
                listener.onBannerClicked(isRefresh, bannerAdapter);
            }
        }

    }
}
