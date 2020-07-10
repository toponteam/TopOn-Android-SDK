package com.anythink.core.strategy;

import android.content.Context;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;

import com.anythink.core.api.AdError;
import com.anythink.core.api.ErrorCode;
import com.anythink.core.common.MyOfferAPIProxy;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.net.OnHttpLoaderListener;
import com.anythink.core.common.net.PlaceStrategyLoader;
import com.anythink.core.common.utils.CommonLogUtil;
import com.anythink.core.common.utils.SPUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Z on 2017/12/28.
 */

public class PlaceStrategyManager {
    public static final String TAG = PlaceStrategyManager.class.getSimpleName();
    private static PlaceStrategyManager mInstance = null;
    private Context mContext;

    public interface StrategyloadListener {
        public void loadStrategySuccess(PlaceStrategy placeStrategy);

        public void loadStrategyFailed(AdError errorBean);
    }


    private ConcurrentHashMap<String, PlaceStrategy> placeStrategyHashMap = new ConcurrentHashMap<String, PlaceStrategy>();


    private PlaceStrategyManager(Context context) {
        mContext = context;
    }

    public synchronized static PlaceStrategyManager getInstance(Context context) {
        if (mInstance == null) {
            synchronized (PlaceStrategyManager.class) {
                if (mInstance == null) {
                    mInstance = new PlaceStrategyManager(context);
                }
            }
        }
        return mInstance;
    }

    public void cleanerPlaceStrategy() {
        placeStrategyHashMap.clear();
    }

    public void cleanPlaceStrategyCheck() {
        SPUtil.clear(mContext, Const.SPU_PLACEMENT_STRATEGY_UPDATE_CHECK_NAME);
    }

    /***
     * @param placeid
     */
    public void savePlaceStrategy(Context context, String placeid, PlaceStrategy placeStrategy, String placementJSON) {
        String appId = SDKContext.getInstance().getAppId();
        appId = appId != null ? appId : "";

        synchronized (PlaceStrategyManager.this) {
            placeStrategyHashMap.put(appId + placeid, placeStrategy);
        }

        //如果内容为空则不再存储
        String encodeStrategy = TextUtils.isEmpty(placementJSON) ? "" : placementJSON;//CommonBase64Util.newBase64EncodeCommon(placementJSON);//CommonUtil.encrypt(handleAESKey(placeid), placementJSON);
        SPUtil.putString(context, Const.SPU_NAME, appId + placeid + "_" + Const.SPUKEY.SPU_PLACEMENT_STRATEGY_TYPE, encodeStrategy);
    }

    /***
     * @param placeid
     * @return
     */
    public PlaceStrategy getPlaceStrategyByAppIdAndPlaceId(String placeid) {
        String appId = SDKContext.getInstance().getAppId();
        appId = appId != null ? appId : "";
        if (placeStrategyHashMap.containsKey(appId + placeid)) {
            return placeStrategyHashMap.get(appId + placeid);
        } else {
            String placementStrategyStr = SPUtil.getString(mContext, Const.SPU_NAME, appId + placeid + "_" + Const.SPUKEY.SPU_PLACEMENT_STRATEGY_TYPE, "");
            if (!TextUtils.isEmpty(placementStrategyStr)) {
                PlaceStrategy placeStrategy = PlaceStrategy.parseStrategy(placementStrategyStr);
                if (placeStrategy != null) {
                    placeStrategyHashMap.put(appId + placeid, placeStrategy);
                }
                return placeStrategy;
            }
            CommonLogUtil.d(TAG, "no key[" + appId + placeid + "]");
            return null;
        }
    }

    /***
     * @param appId
     * @param appKey
     * @param placeId
     */
    public void requestStrategy(final PlaceStrategy oldStrategy, final String appId, final String appKey, final String placeId, final StrategyloadListener strategyloadListener) {
        /**CountDownTimer must be used in main-thread**/
        SDKContext.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                final String settingId = oldStrategy != null ? oldStrategy.getSettingId() : null; //获取当前策略的settingId

                Map<String, Object> customMap = SDKContext.getInstance().getCustomMap(placeId);
                if (oldStrategy != null) {
                    boolean needToRefreshStrategy = false;
                    if (customMap != null) {
                        needToRefreshStrategy = !customMap.equals(oldStrategy.getSdkCustomMap());
                    }
                    /**Check MyOffer Cap**/
                    if (needToRefreshStrategy || oldStrategy.isPlaceStrategyExpired() || MyOfferAPIProxy.getIntance().checkOffersOutOfCap(mContext, placeId)) { //策略过期

                        CommonLogUtil.d(TAG, "Placement strategy expired。。。。");

                        final boolean[] isTimerUp = new boolean[1];
                        final long psUpdateOutTime = oldStrategy.getPsUpdateOutTime();

                        final CountDownTimer timer = new CountDownTimer(psUpdateOutTime, psUpdateOutTime) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                            }

                            @Override
                            public void onFinish() {
                                CommonLogUtil.i(TAG, "Timer onFinish，load AD by old strategy");
                                isTimerUp[0] = true;
                                strategyloadListener.loadStrategySuccess(oldStrategy);
                            }
                        };

                        if (psUpdateOutTime == 0) {
                            isTimerUp[0] = true;
                            strategyloadListener.loadStrategySuccess(oldStrategy);
                        } else {
                            CommonLogUtil.i(TAG, "Update placement strategy，start timer");
                            timer.start();
                        }

                        PlaceStrategyLoader placeStrategyLoader = new PlaceStrategyLoader(mContext, appId, appKey, placeId, settingId, customMap);
                        placeStrategyLoader.start(0, new OnHttpLoaderListener() {

                            @Override
                            public void onLoadStart(int reqCode) {
                            }

                            @Override
                            public void onLoadFinish(int reqCode, Object result) {
                                String json = (String) result;
                                try {
                                    JSONObject jsonObject = new JSONObject(json);
                                    jsonObject.put("updateTime", System.currentTimeMillis());
                                    json = jsonObject.toString();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                final PlaceStrategy curr = PlaceStrategy.parseStrategy(json);

                                if (curr != null) {
                                    savePlaceStrategy(mContext, placeId, curr, curr.getPucs() == 1 ? json : "");
                                }


                                MyOfferAPIProxy.getIntance().initTopOnOffer(mContext, placeId, curr.getMyOfferList(), curr.getMyOfferTkMap(), curr.getMyOfferSetting(), curr.getIsPreLoadOfferRes() == 1);


                                SDKContext.getInstance().runOnMainThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        CommonLogUtil.i(TAG, "Update placement strategy success，cancel timer");
                                        if (timer != null) {
                                            timer.cancel();
                                        }
                                    }
                                });

                                /**If time out, Ad request would use the old placementsetting**/
                                if (isTimerUp[0]) {
                                    return;
                                }

                                if (curr != null) {
                                    if (strategyloadListener != null) {
                                        strategyloadListener.loadStrategySuccess(curr);
                                    }
                                } else {
                                    if (strategyloadListener != null) {
                                        strategyloadListener.loadStrategyFailed(ErrorCode.getErrorCode(ErrorCode.placeStrategyError, "", "Placement Service error."));
                                    }
                                }


                            }

                            @Override
                            public void onLoadError(int reqCode, String msg, AdError errorBean) {
                                CommonLogUtil.e(TAG, "place laod f!:" + msg);

                                //Vertify the appid,appkey,placement
                                if (ErrorCode.statuError.equals(errorBean.getCode())
                                        && (ErrorCode.placementIdError.equals(errorBean.getPlatformCode()) //
                                        || ErrorCode.appIdError.equals(errorBean.getPlatformCode())
                                        || ErrorCode.appKeyError.equals(errorBean.getPlatformCode()))) {
                                    //Invalid placement
                                    String key = appId + placeId + appKey;
                                    CommonLogUtil.e(TAG, "code: " + errorBean.getPlatformCode() + "msg: " + errorBean.getPlatformMSG() + ", key -> " + key);
                                    SPUtil.putLong(mContext, Const.SPU_PLACEMENT_STRATEGY_UPDATE_CHECK_NAME, key, System.currentTimeMillis());
                                }

                                SDKContext.getInstance().runOnMainThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        CommonLogUtil.i(TAG, "Update placement strategy success，cancel timer");
                                        if (timer != null) {
                                            timer.cancel();
                                        }
                                    }
                                });

                                /**If time out, Ad request would use the old placementsetting**/
                                if (isTimerUp[0]) {
                                    return;
                                }

                                /**If request fail, Ad request would use the old placementsetting**/
                                if (strategyloadListener != null) {
                                    strategyloadListener.loadStrategySuccess(oldStrategy);
                                }
                            }

                            @Override
                            public void onLoadCanceled(int reqCode) {
                                if (isTimerUp[0]) {
                                    return;
                                }

                                /**If request fail, Ad request would use the old placementsetting**/
                                if (strategyloadListener != null) {
                                    strategyloadListener.loadStrategySuccess(oldStrategy);
                                }
                            }
                        });
                    } else {
                        if (strategyloadListener != null) {
                            strategyloadListener.loadStrategySuccess(oldStrategy);
                        }
                    }

                } else {
                    /**No PlacementSetting exist**/
                    PlaceStrategyLoader placeStrategyLoader = new PlaceStrategyLoader(mContext, appId, appKey, placeId, settingId, customMap);
                    placeStrategyLoader.start(0, new OnHttpLoaderListener() {

                        @Override
                        public void onLoadStart(int reqCode) {
                        }

                        @Override
                        public void onLoadFinish(int reqCode, Object result) {
                            String json = (String) result;
                            try {
                                JSONObject jsonObject = new JSONObject(json);
                                jsonObject.put("updateTime", System.currentTimeMillis());
                                json = jsonObject.toString();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            final PlaceStrategy curr = PlaceStrategy.parseStrategy(json);

                            if (curr != null) {
                                savePlaceStrategy(mContext, placeId, curr, curr.getPucs() == 1 ? json : "");

                                MyOfferAPIProxy.getIntance().initTopOnOffer(mContext, placeId, curr.getMyOfferList(), curr.getMyOfferTkMap(), curr.getMyOfferSetting(), curr.getIsPreLoadOfferRes() == 1);
                                if (strategyloadListener != null) {
                                    strategyloadListener.loadStrategySuccess(curr);
                                }
                            } else {
                                if (strategyloadListener != null) {
                                    strategyloadListener.loadStrategyFailed(ErrorCode.getErrorCode(ErrorCode.placeStrategyError, "", "Placement Service error."));
                                }
                            }


                        }

                        @Override
                        public void onLoadError(int reqCode, String msg, AdError errorBean) {
                            CommonLogUtil.e(TAG, "place laod f!:" + msg);

                            //Vertify the appid,appkey,placement
                            if (ErrorCode.statuError.equals(errorBean.getCode())
                                    && (ErrorCode.placementIdError.equals(errorBean.getPlatformCode()) //
                                    || ErrorCode.appIdError.equals(errorBean.getPlatformCode())
                                    || ErrorCode.appKeyError.equals(errorBean.getPlatformCode()))) {
                                //Invalid placement
                                String key = appId + placeId + appKey;
                                CommonLogUtil.e(TAG, "code: " + errorBean.getPlatformCode() + "msg: " + errorBean.getPlatformMSG() + ", key -> " + key);
                                SPUtil.putLong(mContext, Const.SPU_PLACEMENT_STRATEGY_UPDATE_CHECK_NAME, key, System.currentTimeMillis());
                            }

                            if (strategyloadListener != null) {
                                strategyloadListener.loadStrategyFailed(errorBean);
                            }
                        }

                        @Override
                        public void onLoadCanceled(int reqCode) {
                            if (strategyloadListener != null) {
                                strategyloadListener.loadStrategyFailed(ErrorCode.getErrorCode(ErrorCode.exception, "", "by canceled"));
                            }
                        }
                    });
                }
            }
        });


    }

}
