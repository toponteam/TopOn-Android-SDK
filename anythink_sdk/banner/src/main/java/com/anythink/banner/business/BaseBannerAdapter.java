package com.anythink.banner.business;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import com.anythink.core.common.AdCacheManager;
import com.anythink.core.common.CommonAdManager;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.AnyThinkBaseAdapter;
import com.anythink.core.common.entity.AdCacheInfo;
import com.anythink.core.common.entity.AdTrackingInfo;
import com.anythink.core.common.net.TrackingV2Loader;
import com.anythink.core.common.track.AdTrackingManager;
import com.anythink.core.common.utils.CommonSDKUtil;
import com.anythink.core.common.utils.TrackingInfoUtil;
import com.anythink.core.common.utils.task.TaskManager;

public abstract class BaseBannerAdapter extends AnyThinkBaseAdapter {

    public final void notfiyShow(final Context context, final AdCacheInfo adCacheInfo) {
        TaskManager.getInstance().run_proxy(new Runnable() {
            @Override
            public void run() {
                AdTrackingInfo adTrackingInfo = getTrackingInfo();
                if (adTrackingInfo != null) {
                    /**Debug log**/
                    BaseBannerAdapter.this.log(Const.LOGKEY.IMPRESSION, Const.LOGKEY.SUCCESS, "");

                    String placementId = adTrackingInfo.getmPlacementId();
                    CommonAdManager commonAdManager = CommonAdManager.getInstance(placementId);
                    String currentRequestId = commonAdManager != null ? commonAdManager.getCurrentRequestId() : "";
                    adTrackingInfo.setCurrentRequestId(currentRequestId);
                    long timestamp = System.currentTimeMillis();
                    adTrackingInfo.setmShowId(CommonSDKUtil.creatImpressionId(adTrackingInfo.getmRequestId(), adTrackingInfo.getmUnitGroupUnitId(), timestamp));

                    /**Must set before AdCacheManager.saveShowTime()，don't suggest to do it in UI-Thread**/
                    TrackingInfoUtil.fillTrackingInfoShowTime(context, adTrackingInfo);
                    //Ad Tracking
                    AdTrackingManager.getInstance(context).addAdTrackingInfo(TrackingV2Loader.AD_SHOW_TYPE, adTrackingInfo, timestamp);
                    AdTrackingManager.getInstance(context).addAdTrackingInfo(TrackingV2Loader.AD_SDK_SHOW_TYPE, adTrackingInfo);

                    //保存展示
                    AdCacheManager.getInstance().saveShowTime(context.getApplicationContext(), adCacheInfo);
                }
            }
        });


    }

    public abstract View getBannerView();


}
