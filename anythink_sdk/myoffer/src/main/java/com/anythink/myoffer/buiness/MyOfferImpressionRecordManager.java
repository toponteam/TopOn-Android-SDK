package com.anythink.myoffer.buiness;

import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.common.entity.MyOfferAd;
import com.anythink.core.common.utils.task.TaskManager;
import com.anythink.core.strategy.PlaceStrategy;
import com.anythink.core.strategy.PlaceStrategyManager;
import com.anythink.myoffer.db.MyOfferImpressionDao;
import com.anythink.myoffer.entity.MyOfferImpression;

import org.json.JSONArray;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class MyOfferImpressionRecordManager {
    private static MyOfferImpressionRecordManager sIntance;
    private Context mContext;
    private SimpleDateFormat mDateFormat;
    private ConcurrentHashMap<String, MyOfferImpression> impressionMap = new ConcurrentHashMap<>();

    private MyOfferImpressionRecordManager(Context context) {
        mContext = context.getApplicationContext();
        mDateFormat = new SimpleDateFormat("yyyyMMdd");
    }

    public static MyOfferImpressionRecordManager getInstance(Context context) {
        if (sIntance == null) {
            sIntance = new MyOfferImpressionRecordManager(context);
        }
        return sIntance;
    }

    /**
     * Record Impression
     *
     * @param myOfferAd
     */
    public void recordImpression(MyOfferAd myOfferAd) {
        long currentTime = System.currentTimeMillis();
        String dateFormat = mDateFormat.format(new Date(currentTime));
        final MyOfferImpression myOfferImpression = getOfferImpreesion(myOfferAd);

        if (myOfferImpression.recordDate.equals(dateFormat)) {
            /**The same date**/
            myOfferImpression.showNum = myOfferImpression.showNum + 1;
        } else {
            /**Update the date and show number**/
            myOfferImpression.showNum = 1;
            myOfferImpression.recordDate = dateFormat;
        }

        /**Record impression time**/
        myOfferImpression.showTime = currentTime;
        TaskManager.getInstance().run_proxy(new Runnable() {
            @Override
            public void run() {
                MyOfferImpressionDao.getInstance(mContext).deleteOutOfDateImpression(myOfferImpression.recordDate);
                /**
                 * update db
                 */
                MyOfferImpressionDao.getInstance(mContext).insertOrupdateMyOfferImpression(myOfferImpression);
            }
        });

    }

    /**
     * Check if offer is in cap
     *
     * @param myOfferAd
     * @return
     */
    public boolean isOfferInCap(MyOfferAd myOfferAd) {
        MyOfferImpression myOfferImpression = getOfferImpreesion(myOfferAd);

        if (myOfferAd.getOfferCap() == -1) {
            return false;
        }

        if (myOfferImpression.showNum >= myOfferAd.getOfferCap()) {
            return true;
        }

        return false;
    }

    /**
     * Check if offer is in pacing
     *
     * @param myOfferAd
     * @return
     */
    public boolean isOfferInPacing(MyOfferAd myOfferAd) {
        long currentTime = System.currentTimeMillis();
        MyOfferImpression myOfferImpression = getOfferImpreesion(myOfferAd);

        if (currentTime - myOfferImpression.showTime <= myOfferAd.getOfferPacing()) {
            return true;
        }

        return false;
    }


    /**
     * Get the offerids which is out of cap
     *
     * @return
     */
    public String getOutOfCapOfferIds() {
        long currentTime = System.currentTimeMillis();
        String dateFormat = mDateFormat.format(new Date(currentTime));
        List<MyOfferImpression> outOfCapList = MyOfferImpressionDao.getInstance(mContext).queryOutOfCap(dateFormat);
        JSONArray jsonArray = new JSONArray();
        if (outOfCapList != null) {
            for (MyOfferImpression myOfferImpression : outOfCapList) {
                jsonArray.put(myOfferImpression.offerId);
            }
        }
        return jsonArray.toString();
    }

    /**
     * Check the offer cap in placement
     *
     * @param toponPlacementId
     * @return
     */
    public boolean checkOffersOutOfCap(String toponPlacementId) {

        boolean isAllOutCap = true;
        PlaceStrategy placeStrategy = PlaceStrategyManager.getInstance(mContext).getPlaceStrategyByAppIdAndPlaceId(toponPlacementId);
        if (placeStrategy == null) {
            return false;
        }

        List<MyOfferAd> myOfferAdList = placeStrategy.getMyOfferAdList();
        /**If no offer, return no in cap.*/
        if (myOfferAdList == null || myOfferAdList.size() <= 0) {
            isAllOutCap = false;
        } else {
            for (MyOfferAd myOfferAd : myOfferAdList) {
                if (!isOfferInCap(myOfferAd)) {
                    isAllOutCap = false;
                    break;
                }
            }
        }

        return isAllOutCap;
    }


    /**
     * Get Offer impression info
     *
     * @param myOfferAd
     * @return
     */
    public MyOfferImpression getOfferImpreesion(MyOfferAd myOfferAd) {
        long currentTime = System.currentTimeMillis();
        String dateFormat = mDateFormat.format(new Date(currentTime));
        MyOfferImpression myOfferImpression = impressionMap.get(myOfferAd.getOfferId());
        if (myOfferImpression == null) {
            myOfferImpression = MyOfferImpressionDao.getInstance(mContext).queryAll(myOfferAd.getOfferId());
            if (myOfferImpression == null) {
                myOfferImpression = new MyOfferImpression();
                myOfferImpression.offerId = myOfferAd.getOfferId();
                myOfferImpression.offerCap = myOfferAd.getOfferCap();
                myOfferImpression.offerPacing = myOfferAd.getOfferPacing();
                myOfferImpression.showTime = 0;
                myOfferImpression.showNum = 0;
                myOfferImpression.recordDate = dateFormat;
            }
            impressionMap.put(myOfferAd.getOfferId(), myOfferImpression);
        }

        /**If date has been changed, update the date**/
        if (!TextUtils.equals(dateFormat, myOfferImpression.recordDate)) {
            myOfferImpression.recordDate = dateFormat;
            myOfferImpression.showNum = 0;
        }
        return myOfferImpression;
    }


}
