package com.anythink.banner.business;

import android.content.Context;
import android.view.View;

import com.anythink.core.common.CommonAdManager;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.AnyThinkBaseAdapter;
import com.anythink.core.common.entity.AdTrackingInfo;
import com.anythink.core.common.net.TrackingV2Loader;
import com.anythink.core.common.track.AdTrackingManager;

public abstract class BaseBannerAdapter extends AnyThinkBaseAdapter {

    public final void notfiyShow(Context context) {
        AdTrackingInfo adTrackingInfo = getTrackingInfo();
        if (adTrackingInfo != null) {
            /**Debug log**/
            this.log(Const.LOGKEY.IMPRESSION, Const.LOGKEY.SUCCESS, "");

            String placementId = adTrackingInfo.getmPlacementId();
            CommonAdManager commonAdManager = CommonAdManager.getInstance(placementId);
            String currentRequestId = commonAdManager != null ? commonAdManager.getCurrentRequestId() : "";
            adTrackingInfo.setCurrentRequestId(currentRequestId);

            //Ad Tracking
            AdTrackingManager.getInstance(context).addAdTrackingInfo(TrackingV2Loader.AD_SHOW_TYPE, adTrackingInfo);
            AdTrackingManager.getInstance(context).addAdTrackingInfo(TrackingV2Loader.AD_SDK_SHOW_TYPE, adTrackingInfo);
        }

    }

    public abstract View getBannerView();


}
