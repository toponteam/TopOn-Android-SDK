package com.anythink.interstitial.business;

import android.content.Context;

import com.anythink.core.api.AdError;
import com.anythink.core.common.CommonCacheCountdownTimer;
import com.anythink.core.common.base.AnyThinkBaseAdapter;
import com.anythink.core.common.entity.AdTrackingInfo;
import com.anythink.core.strategy.PlaceStrategy;
import com.anythink.interstitial.business.utils.CustomInterstitialAdapterParser;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialListener;

public class CacheCountdownTimer extends CommonCacheCountdownTimer {

    public CacheCountdownTimer(long millisInFuture, long countDownInterval, PlaceStrategy.UnitGroupInfo unitGroupInfo, AdTrackingInfo adTrackingInfo) {
        super(millisInFuture, countDownInterval, unitGroupInfo, adTrackingInfo);
    }


    @Override
    public void onTick(long millisUntilFinished) {

    }


    @Override
    protected void startLoadAd(final Context activityContext, final long startTime, final AnyThinkBaseAdapter adapter) {
        final Context applicationContext = activityContext.getApplicationContext();

        String placementId = mAdTrackingInfo != null ? mAdTrackingInfo.getmPlacementId() : "";
        CustomInterstitialAdapterParser.loadInterstitialAd(activityContext, (CustomInterstitialAdapter) adapter, adapter.getmUnitgroupInfo(), PlaceStrategy.getServerExtrasMap(placementId, adapter.getmUnitgroupInfo(), ""), null, new CustomInterstitialListener() {
            @Override
            public void onInterstitialAdDataLoaded(CustomInterstitialAdapter adapter) {
                onAdDataLoaded(startTime, adapter);
            }

            @Override
            public void onInterstitialAdLoaded(CustomInterstitialAdapter adapter) {
                onAdLoaded(applicationContext, startTime, adapter, null);
            }

            @Override
            public void onInterstitialAdLoadFail(CustomInterstitialAdapter adapter, AdError adError) {
                onAdError(applicationContext, startTime, adapter, adError);
            }

//            @Override
//            public void onInterstitialAdVideoStart(CustomInterstitialAdapter adapter) {
//                if (adapter != null) {
//                    AdTrackingInfo adTrackingInfo = adapter.getTrackingInfo();
//                    //发送视频开始统计
//                    AdTrackingManager.getInstance(applicationContext).addAdTrackingInfo(TrackingV2Loader.AD_RV_START_TYPE, adTrackingInfo);
////                    new TrackingLoader(applicationContext, adTrackingInfo, TrackingLoader.AD_RV_START_TYPE, 0).start(0, null);
//
//
////                HashMap<String, String> map = new HashMap<>();
////                map.put(Const.AgentKey.REQUESTID, mCurrentReqeustId);
////                map.put(Const.AgentKey.PSID, mCurrentStrategy.getPsid());
////                map.put(Const.AgentKey.SESSIONID, mCurrentStrategy.getSessionId());
////                map.put(Const.AgentKey.UNITGROUPID, adapter.getGroupId().toString());
////                map.put(Const.AgentKey.UNITID, mCurrentPlacementId);
////                map.put(Const.AgentKey.MSG, String.valueOf(adTrackingInfo.getmNetworkType()));
////
////                Agent.onEvent(1004701, map);
//
//                    if (mListener != null) {
//                        mListener.onInterstitialAdVideoStart();
//                    }
//                }
//
//            }
//
//            @Override
//            public void onInterstitialAdVideoEnd(CustomInterstitialAdapter adapter) {
//                if (adapter != null) {
//                    AdTrackingInfo adTrackingInfo = adapter.getTrackingInfo();
//                    //发送统计
//                    AdTrackingManager.getInstance(applicationContext).addAdTrackingInfo(TrackingV2Loader.AD_RV_CLOSE_TYPE, adTrackingInfo);
////                    new TrackingLoader(applicationContext, adTrackingInfo, TrackingLoader.AD_RV_CLOSE_TYPE, 0).start(0, null);
//
//                    if (mListener != null) {
//                        mListener.onInterstitialAdVideoEnd();
//                    }
//                }
//
//
//            }
//
//            @Override
//            public void onInterstitialAdVideoError(CustomInterstitialAdapter adapter, final AdError errorCode) {
////            if (mIsRelease) {
////                return;
////            }
//                if (adapter != null) {
//                    AdTrackingInfo adTrackingInfo = adapter.getTrackingInfo();
//                    //发送统计
////                    new TrackingLoader(applicationContext, adTrackingInfo, TrackingLoader.AD_RV_START_TYPE, 0).start(0, null);
//
////                HashMap<String, String> map = new HashMap<>();
////                map.put(Const.AgentKey.REQUESTID, mCurrentReqeustId);
////                map.put(Const.AgentKey.PSID, mCurrentStrategy.getPsid());
////                map.put(Const.AgentKey.SESSIONID, mCurrentStrategy.getSessionId());
////                map.put(Const.AgentKey.UNITGROUPID, adapter.getGroupId().toString());
////                map.put(Const.AgentKey.UNITID, mCurrentPlacementId);
////                map.put(Const.AgentKey.MSG, String.valueOf(adTrackingInfo.getmNetworkType()));
////                map.put(Const.AgentKey.MSG1, errorCode.getPlatformCode());
////                map.put(Const.AgentKey.MSG2, errorCode.getPlatformMSG());
////
////                Agent.onEvent(1004701, map);
//
//                    if (mListener != null) {
//                        mListener.onInterstitialAdVideoError(errorCode);
//                    }
//                }
//
//
//            }
//
//            @Override
//            public void onInterstitialAdClose(CustomInterstitialAdapter adapter) {
//                if (adapter != null) {
//                    if (mListener != null) {
//                        mListener.onInterstitialAdClose(UpArpuAdInfo.fromAdapter(adapter));
//                    }
//
//                    AdTrackingInfo adTrackingInfo = adapter.getTrackingInfo();
//
//                    /**日志输出**/
//                    adapter.log(Const.LOGKEY.CLOSE, Const.LOGKEY.SUCCESS, "");
////                    AgentEventManager.onAdEventAgent(adTrackingInfo, Const.LOGKEY.CLOSE, adTrackingInfo.getmPsid(), adTrackingInfo.getmSessionId(), adTrackingInfo.getmGroupId(), adTrackingInfo.getmPlacementId(), adTrackingInfo.getmRequestId()
////                            , adTrackingInfo.getmNetworkType() + "", adTrackingInfo.getmNetworkContent(), adTrackingInfo.getmLevel() + ""
////                            , "1", "", "", ""
////                            , adTrackingInfo.getLoadStatus() == AdTrackingInfo.SHORT_OVERTIME_CALLBACK ? "1" : "0", adTrackingInfo.getRequestType() + "", "1", "", "");
//                    //close掉之后释放
//                    adapter.clean();
//                }
//
//
//            }
//
//            @Override
//            public void onInterstitialAdClicked(CustomInterstitialAdapter adapter) {
//                if (adapter != null) {
//                    adapter.log(Const.LOGKEY.CLICK, Const.LOGKEY.SUCCESS, "");
//
//                    AdTrackingInfo adTrackingInfo = adapter.getTrackingInfo();
//                    //发送统计
//                    AdTrackingManager.getInstance(applicationContext).addAdTrackingInfo(TrackingV2Loader.AD_CLICK_TYPE, adTrackingInfo);
////                    new TrackingLoader(applicationContext, adTrackingInfo, TrackingLoader.AD_CLICK_TYPE, 0).start(0, null);
//
////                    AgentEventManager.onAdEventAgent(adTrackingInfo, Const.LOGKEY.CLICK, adTrackingInfo.getmPsid(), adTrackingInfo.getmSessionId(), adTrackingInfo.getmGroupId(), adTrackingInfo.getmPlacementId(), adTrackingInfo.getmRequestId()
////                            , adTrackingInfo.getmNetworkType() + "", adTrackingInfo.getmNetworkContent(), adTrackingInfo.getmLevel() + ""
////                            , "1", "", "", ""
////                            , adTrackingInfo.getLoadStatus() == AdTrackingInfo.SHORT_OVERTIME_CALLBACK ? "1" : "0", adTrackingInfo.getRequestType() + "", "1", "", "");
//
//                    if (mListener != null) {
//                        mListener.onInterstitialAdClicked(UpArpuAdInfo.fromAdapter(adapter));
//                    }
//                }
//            }
//
//            @Override
//            public void onInterstitialAdShow(CustomInterstitialAdapter adapter) {
//
//                if (adapter != null) {
//                    /**日志输出**/
//                    adapter.log(Const.LOGKEY.IMPRESSION, Const.LOGKEY.SUCCESS, "");
//
////                    /**回调给app的埋点**/
////                    AgentEventManager.onAdShowEventAgent(adapter.getTrackingInfo(), "1", "");
//                    //发送展示统计
//                    AdTrackingInfo adTrackingInfo = adapter.getTrackingInfo();
//                    AdTrackingManager.getInstance(applicationContext).addAdTrackingInfo(TrackingV2Loader.AD_SHOW_TYPE, adTrackingInfo);
////                    new TrackingLoader(applicationContext, adTrackingInfo, TrackingLoader.AD_SHOW_TYPE, 0).start(0, null);
//                    if (mListener != null) {
//                        mListener.onInterstitialAdShow(UpArpuAdInfo.fromAdapter(adapter));
//                    }
//                }
//            }


        });
    }

}
