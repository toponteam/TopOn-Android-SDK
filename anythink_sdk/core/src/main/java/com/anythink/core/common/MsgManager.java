package com.anythink.core.common;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.entity.AdTrackingInfo;
import com.anythink.core.common.entity.AdTrackingLogBean;
import com.anythink.core.common.net.TrackingV2Loader;
import com.anythink.core.common.utils.CommonLogUtil;
import com.anythink.core.common.utils.MsgUtil;
import com.anythink.core.strategy.AppStrategy;
import com.anythink.core.strategy.PlaceStrategy;
import com.anythink.core.strategy.PlaceStrategyManager;

import org.json.JSONObject;

import java.util.Map;

public class MsgManager {
    public static final String TAG = MsgManager.class.getSimpleName();
    private static MsgManager mInstance;

    private Context mContext;

    private MsgManager(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public synchronized static MsgManager getInstance(Context context) {
        if (mInstance == null) {
            synchronized (MsgManager.class) {
                if (mInstance == null) {
                    mInstance = new MsgManager(context);
                }
            }
        }
        return mInstance;
    }

    public void handleTK(final int businessType, final AdTrackingLogBean logBean, final AppStrategy appStrategy) {
        try {
            SDKContext.getInstance().runOnThreadPool(new Runnable() {
                @Override
                public void run() {

                    try {
                        if (!TextUtils.equals(AdTrackingInfo.AD_INTERSTITIAL_TYPE, logBean.adTrackingInfo.getmAdType())
                                && !TextUtils.equals(AdTrackingInfo.AD_REWARDVIDEO_TYPE, logBean.adTrackingInfo.getmAdType())) {
                            return;
                        }

                        PlaceStrategy placeStrategy = PlaceStrategyManager.getInstance(SDKContext.getInstance().getContext()).getPlaceStrategyByAppIdAndPlaceId(logBean.adTrackingInfo.getmPlacementId());
                        if (placeStrategy == null) {
                            return;
                        }
                        final String adsourceId = ((AdTrackingInfo) logBean.adTrackingInfo).getmUnitGroupUnitId();
                        if (TextUtils.isEmpty(adsourceId)) {
                            return;
                        }

                        boolean isNeedSend = false;
                        String action = null;
                        switch (businessType) {
                            case TrackingV2Loader.AD_SDK_SHOW_TYPE: {
                                isNeedSend = ((AdTrackingInfo) logBean.adTrackingInfo).getmShowTkSwitch() == 1;

                                Map<String, String> noticeMap = appStrategy.getNoticeMap();
                                action = noticeMap.get("show");

                                break;
                            }
                            case TrackingV2Loader.AD_CLICK_TYPE: {
                                isNeedSend = ((AdTrackingInfo) logBean.adTrackingInfo).getmClickTkSwtich() == 1;

                                Map<String, String> noticeMap = appStrategy.getNoticeMap();
                                action = noticeMap.get("click");

                                break;
                            }
                        }

                        if (isNeedSend && !TextUtils.isEmpty(action)) {//开始发送消息
                            JSONObject commonObject = MsgUtil.getCommonObject();
                            CommonLogUtil.d(TAG, "common -> " + commonObject.toString());
                            CommonLogUtil.d(TAG, "data -> " + logBean.toJSONObject().toString());

                            String cm = commonObject.toString();
                            String dt = logBean.toJSONObject().toString();

                            sendMsg(action, cm, dt, adsourceId, ((AdTrackingInfo) logBean.adTrackingInfo));
                        }
                    } catch (Throwable e) {

                    }
                }
            });
        } catch (Throwable e) {

        }
    }

    public void handleInit(final AppStrategy appStrategy) {
        SDKContext.getInstance().runOnThreadPool(new Runnable() {
            @Override
            public void run() {
                try {
                    Map<String, String> noticeMap = appStrategy.getNoticeMap();
                    String action = noticeMap != null ? noticeMap.get("init") : "";
                    if (!TextUtils.isEmpty(action)) {
                        if (mContext == null) {
                            return;
                        }

                        Intent intent = new Intent(action);
                        intent.putExtra(action, appStrategy.getTC());

                        intent.setPackage(mContext.getPackageName());
                        mContext.sendBroadcast(intent);
                    }

                } catch (Throwable e) {
                }
            }
        });
    }

    private void sendMsg(String action, String common, String data, String adsourceId, AdTrackingInfo adTrackingInfo) {
        if (mContext == null) {
            return;
        }

        try {
            Intent intent = new Intent(action);
            intent.putExtra("common", common);
            intent.putExtra("data", data);
            intent.putExtra("adsourceId", adsourceId);
            intent.putExtra("networkType", String.valueOf(adTrackingInfo.getmNetworkType()));
            intent.putExtra("format", adTrackingInfo.getmAdType());

            intent.setPackage(mContext.getPackageName());
            mContext.sendBroadcast(intent);
        } catch (Throwable e) {

        }

    }


}
