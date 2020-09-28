package com.anythink.banner.business;

import android.content.Context;

import com.anythink.core.api.AdError;
import com.anythink.banner.api.ATBannerView;
import com.anythink.core.common.CommonAdManager;
import com.anythink.core.common.CommonMediationManager;
import com.anythink.core.common.PlacementAdManager;
import com.anythink.core.common.base.Const;


/**
 * Ad request manager
 */

public class AdLoadManager extends CommonAdManager<BannerLoadParams> {


    public static final String TAG = "Banner" + AdLoadManager.class.getSimpleName();


    private AdLoadManager(Context context, String placementId) {
        super(context, placementId);
    }


    public static AdLoadManager getInstance(Context context, String placementId) {

        CommonAdManager adLoadManager = PlacementAdManager.getInstance().getAdManager(placementId);
        if (adLoadManager == null || !(adLoadManager instanceof AdLoadManager)) {
            adLoadManager = new AdLoadManager(context, placementId);
            PlacementAdManager.getInstance().addAdManager(placementId, adLoadManager);
        }
        adLoadManager.refreshContext(context);
        return (AdLoadManager) adLoadManager;
    }

    /**
     * Ad Request
     *
     * @param listener
     */
    public void startLoadAd(Context context, final ATBannerView bannerView, final boolean isRefresh, final InnerBannerListener listener) {

        BannerLoadParams bannerLoadParams = new BannerLoadParams();
        bannerLoadParams.bannerView = bannerView;
        bannerLoadParams.listener = listener;
        bannerLoadParams.context = context;
        bannerLoadParams.isRefresh = isRefresh;

        super.startLoadAd(mApplicationContext, Const.FORMAT.BANNER_FORMAT, mPlacementId, bannerLoadParams);

    }


    @Override
    public CommonMediationManager createFormatMediationManager(BannerLoadParams bannerLoadParams) {
        MediationGroupManager mediaionGroupManager = new MediationGroupManager(bannerLoadParams.context);
        mediaionGroupManager.setCallbackListener(bannerLoadParams.listener);
        mediaionGroupManager.setRefresh(bannerLoadParams.isRefresh);
        mediaionGroupManager.setATBannerView(bannerLoadParams.bannerView);
        return mediaionGroupManager;
    }

    @Override
    public void onCallbackOfferHasExist(BannerLoadParams bannerLoadParams, String placementId, String requestId) {
        if (bannerLoadParams.listener != null) {
            bannerLoadParams.listener.onBannerLoaded(bannerLoadParams.isRefresh);
        }
    }

    @Override
    public void onCallbacInternalError(BannerLoadParams bannerLoadParams, String placementId, String requestId, AdError adError) {
        if (bannerLoadParams.listener != null) {
            bannerLoadParams.listener.onBannerFailed(bannerLoadParams.isRefresh, adError);
        }

    }


}
