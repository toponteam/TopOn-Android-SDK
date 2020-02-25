package com.anythink.myoffer.buiness;

import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.common.entity.MyOfferInitInfo;
import com.anythink.myoffer.buiness.resource.MyOfferLoader;
import com.anythink.myoffer.db.MyOfferAdDao;
import com.anythink.myoffer.entity.MyOfferAd;
import com.anythink.myoffer.entity.MyOfferImpression;
import com.anythink.myoffer.entity.MyOfferSetting;
import com.anythink.network.myoffer.MyOfferErrorCode;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
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

    public void initOfferList(final MyOfferInitInfo myOfferInitInfo) {
//        TaskManager.getInstance().run_proxy(new Runnable() {
//            @Override
//            public void run() {
                /**Remove old myoffer info by placementid**/
                MyOfferAdDao.getInstance(mContext).deleteByPlacementId(myOfferInitInfo.placementId);
                if (TextUtils.isEmpty(myOfferInitInfo.offerList)) {
                    return;
                }
                List<MyOfferAd> myOfferAdList = parseOfferList(myOfferInitInfo.offerList, myOfferInitInfo.tkInfoMap);
                /**Update placement's MyOffer List**/
                MyOfferAdDao.getInstance(mContext).insertOrUpdate(myOfferAdList, myOfferInitInfo.placementId);
                /**
                 * PreLoad Resource
                 */
                if (myOfferInitInfo.isNeedPreloadRes) {
                    MyOfferSetting setting = MyOfferSetting.parseMyOfferSetting(myOfferInitInfo.settings);
                    MyOfferResourceManager.getInstance().preLoadOfferList(myOfferAdList, setting);
                }
//            }
//        });
    }


    /**
     * Get Offer in Caches
     *
     * @param toponPlacementId
     * @param offerId
     * @return
     */
    public MyOfferAd getAdCache(String toponPlacementId, String offerId) {
        return MyOfferAdDao.getInstance(mContext).queryOfferById(toponPlacementId, offerId);
    }


    /**
     * Get default offer order by cap
     */
    public String getDefaultCacheOfferId(String placementId) {
        List<MyOfferAd> myOfferAdList = MyOfferAdDao.getInstance(mContext).queryAllGroupByOfferIdByPlacementId(placementId);
        List<MyOfferImpression> myOfferImpressionList = new ArrayList<>();
        if (myOfferAdList == null || myOfferAdList.size() == 0) {
            return "";
        }

        /**
         * Check MyOffer Resource
         */
        for (int index = myOfferAdList.size() - 1; index >= 0; index--) {
            MyOfferAd myOfferAd = myOfferAdList.get(index);
            if (!MyOfferResourceManager.getInstance().isExist(myOfferAd)) {
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
    public String getCacheOfferId() {
        List<MyOfferAd> myOfferAdList = MyOfferAdDao.getInstance(mContext).queryAllGroupByOfferId();
        JSONObject cacheOffers = new JSONObject();
        if (myOfferAdList != null) {
            try {
                for (MyOfferAd myOfferAd : myOfferAdList) {
                    if (MyOfferResourceManager.getInstance().isExist(myOfferAd)) {
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
     * Parse the MyOffer's JSONObject
     *
     * @param offerList
     * @return
     */
    private List<MyOfferAd> parseOfferList(String offerList, String tkInfoMap) {
        List<MyOfferAd> myOfferAdList = new ArrayList<>();
        JSONObject tkInfoObject = null;
        try {
            tkInfoObject = new JSONObject(tkInfoMap);
        } catch (Exception e) {

        }

        try {
            JSONArray jsonArray = new JSONArray(offerList);

            for (int i = 0; i < jsonArray.length(); i++) {
                MyOfferAd myOfferAd = new MyOfferAd();
                JSONObject offerObject = jsonArray.optJSONObject(i);
                myOfferAd.setOfferId(offerObject.optString("o_id"));
                myOfferAd.setCreativeId(offerObject.optString("c_id"));
                myOfferAd.setTitle(offerObject.optString("t"));
                myOfferAd.setPkgName(offerObject.optString("p_g"));
                myOfferAd.setDesc(offerObject.optString("d"));
                myOfferAd.setIconUrl(offerObject.optString("ic_u"));
                myOfferAd.setMainImageUrl(offerObject.optString("im_u"));
                myOfferAd.setEndCardImageUrl(offerObject.optString("f_i_u"));
                myOfferAd.setAdChoiceUrl(offerObject.optString("a_c_u"));
                myOfferAd.setCtaText(offerObject.optString("c_t"));
                myOfferAd.setVideoUrl(offerObject.optString("v_u"));
                myOfferAd.setClickType(offerObject.optInt("l_t"));
                myOfferAd.setPreviewUrl(offerObject.optString("p_u"));
                myOfferAd.setDeeplinkUrl(offerObject.optString("dl"));
                myOfferAd.setClickUrl(offerObject.optString("c_u"));
                myOfferAd.setNoticeUrl(offerObject.optString("ip_u"));

                /**Tracking url handle**/
                myOfferAd.setVideoStartTrackUrl(handleTKUrlReplace(offerObject.optString("t_u"), tkInfoObject));
                myOfferAd.setVideoProgress25TrackUrl(handleTKUrlReplace(offerObject.optString("t_u_25"), tkInfoObject));
                myOfferAd.setVideoProgress50TrackUrl(handleTKUrlReplace(offerObject.optString("t_u_50"), tkInfoObject));
                myOfferAd.setVideoProgress75TrackUrl(handleTKUrlReplace(offerObject.optString("t_u_75"), tkInfoObject));
                myOfferAd.setVideoFinishTrackUrl(handleTKUrlReplace(offerObject.optString("t_u_100"), tkInfoObject));
                myOfferAd.setEndCardShowTrackUrl(handleTKUrlReplace(offerObject.optString("s_e_c_t_u"), tkInfoObject));
                myOfferAd.setEndCardCloseTrackUrl(handleTKUrlReplace(offerObject.optString("c_t_u"), tkInfoObject));
                myOfferAd.setImpressionTrackUrl(handleTKUrlReplace(offerObject.optString("ip_n_u"), tkInfoObject));
                myOfferAd.setClickTrackUrl(handleTKUrlReplace(offerObject.optString("c_n_u"), tkInfoObject));


                myOfferAd.setOfferCap(offerObject.optInt("o_a_d_c"));
                myOfferAd.setOfferPacing(offerObject.optLong("o_a_p"));
                myOfferAd.setUpdateTime(System.currentTimeMillis());

                myOfferAd.setOfferType(offerObject.optInt("f_t"));
                myOfferAd.setClickMode(offerObject.optInt("c_m"));
                myOfferAdList.add(myOfferAd);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return myOfferAdList;
    }

    /**
     * Download MyOffer's Resource
     */
    public void load(MyOfferAd myOfferAd, MyOfferSetting myOfferSetting, final MyOfferLoader.MyOfferLoaderListener listener) {
        if(MyOfferImpressionRecordManager.getInstance(mContext).isOfferInCap(myOfferAd)) { // Cap
            if(listener != null) {
                listener.onFailed(MyOfferErrorCode.get(MyOfferErrorCode.outOfCapError, MyOfferErrorCode.fail_out_of_cap));
            }
            return;
        }
        else if(MyOfferImpressionRecordManager.getInstance(mContext).isOfferInPacing(myOfferAd)) { // Pacing
            if(listener != null) {
                listener.onFailed(MyOfferErrorCode.get(MyOfferErrorCode.inPacingError, MyOfferErrorCode.fail_in_pacing));
            }
            return;
        }
        MyOfferResourceManager.getInstance().load(myOfferAd, myOfferSetting, listener);
    }


    /**
     * Check if MyOffer Resource exist
     * @param myOfferAd
     * @param isDefault
     * @return
     */
    public boolean isReady(MyOfferAd myOfferAd, boolean isDefault) {
        if (mContext == null || myOfferAd == null) {
            return false;
        }
        if (isDefault) {
            return MyOfferResourceManager.getInstance().isExist(myOfferAd);
        } else {
            return !MyOfferImpressionRecordManager.getInstance(mContext).isOfferInCap(myOfferAd)
                    && !MyOfferImpressionRecordManager.getInstance(mContext).isOfferInPacing(myOfferAd)
                    && MyOfferResourceManager.getInstance().isExist(myOfferAd);
        }

    }


    /**
     * Replace String in url
     * @param url
     * @param tkInfoObject
     * @return
     */
    public String handleTKUrlReplace(String url, JSONObject tkInfoObject) {
        if (tkInfoObject == null) {
            return url;
        }
        Iterator<String> keyIterator = tkInfoObject.keys();
        while (keyIterator.hasNext()) {
            String key = keyIterator.next();
            url = url.replaceAll("\\{" + key + "\\}", tkInfoObject.optString(key));
        }
        return url;
    }

}
