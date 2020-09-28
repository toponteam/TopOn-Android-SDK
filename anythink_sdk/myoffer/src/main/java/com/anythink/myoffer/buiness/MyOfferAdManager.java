package com.anythink.myoffer.buiness;

import android.content.Context;
import android.util.TypedValue;

import com.anythink.china.common.ApkDownloadManager;
import com.anythink.china.common.download.ApkRequest;
import com.anythink.china.common.resource.ApkResource;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.entity.MyOfferAd;
import com.anythink.core.common.entity.MyOfferSetting;
import com.anythink.core.common.res.ImageLoader;
import com.anythink.core.common.res.ResourceEntry;
import com.anythink.core.common.utils.task.TaskManager;
import com.anythink.core.strategy.PlaceStrategy;
import com.anythink.core.strategy.PlaceStrategyManager;
import com.anythink.myoffer.buiness.resource.MyOfferLoader;
import com.anythink.myoffer.entity.MyOfferImpression;
import com.anythink.myoffer.net.MyOfferTkLoader;
import com.anythink.myoffer.net.NoticeUrlLoader;
import com.anythink.myoffer.ui.ApkConfirmDialogActivity;
import com.anythink.network.myoffer.MyOfferErrorCode;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MyOfferAdManager {
    private static MyOfferAdManager sIntance;
    private Context mContext;

    private MyOfferAdManager(Context context) {
        mContext = context.getApplicationContext();
    }

    public static MyOfferAdManager getInstance(Context context) {
        if (sIntance == null) {
            sIntance = new MyOfferAdManager(context);
        }
        return sIntance;
    }

    public void preloadOfferList(final String placementId) {
//        TaskManager.getInstance().run_proxy(new Runnable() {
//            @Override
//            public void run() {
        PlaceStrategy placeStrategy = PlaceStrategyManager.getInstance(mContext).getPlaceStrategyByAppIdAndPlaceId(placementId);

        if (placeStrategy == null) {
            return;
        }

        List<MyOfferAd> myOfferAdList = placeStrategy.getMyOfferAdList();

        if (myOfferAdList == null) {
            return;
        }
        /**
         * PreLoad Resource
         */
        MyOfferSetting setting = placeStrategy.getMyOfferSetting();
        if (setting == null) {
            return;
        }
        MyOfferResourceManager.getInstance().preLoadOfferList(placementId, myOfferAdList, setting);
    }


    /**
     * Get Offer in Caches
     *
     * @param toponPlacementId
     * @param offerId
     * @return
     */
    public MyOfferAd getAdCache(String toponPlacementId, String offerId) {
        PlaceStrategy placeStrategy = PlaceStrategyManager.getInstance(mContext).getPlaceStrategyByAppIdAndPlaceId(toponPlacementId);
        if (placeStrategy == null) {
            return null;
        }

        MyOfferAd myOfferAd = placeStrategy.getMyOfferByOfferId(offerId);
        return myOfferAd;
    }


    /**
     * Get default offer order by cap
     */
    public String getDefaultCacheOfferId(String placementId) {
        PlaceStrategy placeStrategy = PlaceStrategyManager.getInstance(mContext).getPlaceStrategyByAppIdAndPlaceId(placementId);
        if (placeStrategy == null) {
            return "";
        }

        List<MyOfferAd> myOfferAdList = placeStrategy.getMyOfferAdList();
        List<MyOfferImpression> myOfferImpressionList = new ArrayList<>();
        if (myOfferAdList == null || myOfferAdList.size() == 0) {
            return "";
        }

        /**
         * Check MyOffer Resource
         */
        for (int index = myOfferAdList.size() - 1; index >= 0; index--) {
            MyOfferAd myOfferAd = myOfferAdList.get(index);
            if (!MyOfferResourceManager.getInstance().isExist(myOfferAd, placeStrategy.getMyOfferSetting())) {
                myOfferAdList.remove(index);
            } else {
                myOfferImpressionList.add(MyOfferImpressionRecordManager.getInstance(mContext).getOfferImpreesion(myOfferAd));
            }
        }

        if (myOfferImpressionList == null || myOfferImpressionList.size() == 0) {
            return "";
        }

        Collections.sort(myOfferImpressionList, new Comparator<MyOfferImpression>() {
            @Override
            public int compare(MyOfferImpression myOfferImpressionA, MyOfferImpression myOfferImpressionB) {
                return ((Integer) myOfferImpressionA.showNum).compareTo(myOfferImpressionB.showNum);
            }
        });


        String offerId = myOfferImpressionList.get(0).offerId;

        return offerId;
    }

    /**
     * Get offerids in Caches
     *
     * @return
     */
    public String getCacheOfferId(String format, MyOfferSetting myOfferSetting) {
        PlaceStrategyManager placeStrategyManager = PlaceStrategyManager.getInstance(mContext);
        List<MyOfferAd> myOfferAdList = placeStrategyManager.getMyOfferListByFormat(format);
        JSONObject cacheOffers = new JSONObject();
        if (myOfferAdList != null) {
            try {
                for (MyOfferAd myOfferAd : myOfferAdList) {
                    if (MyOfferResourceManager.getInstance().isExist(myOfferAd, myOfferSetting)) {
                        cacheOffers.put(myOfferAd.getOfferId(), myOfferAd.getCreativeId());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return cacheOffers.toString();
    }


    /**
     * Download MyOffer's Resource
     */
    public void load(String placementId, MyOfferAd myOfferAd, MyOfferSetting myOfferSetting, final MyOfferLoader.MyOfferLoaderListener listener) {
        if (MyOfferImpressionRecordManager.getInstance(mContext).isOfferInCap(myOfferAd)) { // Cap
            if (listener != null) {
                listener.onFailed(MyOfferErrorCode.get(MyOfferErrorCode.outOfCapError, MyOfferErrorCode.fail_out_of_cap));
            }
            return;
        } else if (MyOfferImpressionRecordManager.getInstance(mContext).isOfferInPacing(myOfferAd)) { // Pacing
            if (listener != null) {
                listener.onFailed(MyOfferErrorCode.get(MyOfferErrorCode.inPacingError, MyOfferErrorCode.fail_in_pacing));
            }
            return;
        }
        MyOfferResourceManager.getInstance().load(placementId, myOfferAd, myOfferSetting, listener);
    }


    /**
     * Check if MyOffer Resource exist
     *
     * @param myOfferAd
     * @param isDefault
     * @return
     */
    public boolean isReady(MyOfferAd myOfferAd, MyOfferSetting myOfferSetting, boolean isDefault) {
        if (mContext == null || myOfferAd == null) {
            return false;
        }
        if (isDefault) {
            return MyOfferResourceManager.getInstance().isExist(myOfferAd, myOfferSetting);
        } else {
            return !MyOfferImpressionRecordManager.getInstance(mContext).isOfferInCap(myOfferAd)
                    && !MyOfferImpressionRecordManager.getInstance(mContext).isOfferInPacing(myOfferAd)
                    && MyOfferResourceManager.getInstance().isExist(myOfferAd, myOfferSetting);
        }

    }


    public void startDownloadApp(final String requestId, final MyOfferSetting myOfferSetting, final MyOfferAd myOfferAd, final String url) {
        SDKContext.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (1 == myOfferSetting.getApkDownloadConfirm()) {
                    ApkConfirmDialogActivity.start(mContext, requestId, myOfferSetting, myOfferAd, url);
                } else {
                    realStartDownloadApp(requestId, myOfferSetting, myOfferAd, url);
                }
            }
        });

    }

    public void realStartDownloadApp(final String requestId, final MyOfferSetting myOfferSetting, final MyOfferAd myOfferAd, final String url) {
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
            apkRequest.icon = ImageLoader.getInstance(mContext).getBitmapFromDiskCache(new ResourceEntry(ResourceEntry.INTERNAL_CACHE_TYPE, myOfferAd.getIconUrl()), size, size);


            ApkDownloadManager.getInstance(SDKContext.getInstance().getContext()).setOfferCacheTime(myOfferSetting.getOfferCacheTime());
            ApkDownloadManager.getInstance(SDKContext.getInstance().getContext()).checkAndCleanApk();
            ApkDownloadManager.getInstance(SDKContext.getInstance().getContext()).handleClick(apkRequest);
        }
    }


    public void sendAdTracking(final String requestId, final MyOfferAd myOfferAd, final int tkType, final String scenario) {
        TaskManager.getInstance().run_proxy(new Runnable() {
            @Override
            public void run() {
                if (tkType == MyOfferTkLoader.IMPRESSION_TYPE) {
                    new NoticeUrlLoader(myOfferAd.getNoticeUrl(), requestId).start(0, null);
                }

                MyOfferTkLoader myOfferTkLoader = new MyOfferTkLoader(tkType, myOfferAd, requestId);
                myOfferTkLoader.setScenario(scenario);
                myOfferTkLoader.start(0, null);
            }
        });

    }

}
