package com.anythink.core.common.base;

import android.app.Activity;
import android.content.Context;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.ATSDK;
import com.anythink.core.common.entity.AdTrackingInfo;
import com.anythink.core.strategy.PlaceStrategy;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.Map;

/**
 * All Mediation's BaseAdapter
 */
public abstract class AnyThinkBaseAdapter {
    private AdTrackingInfo mTrackingInfo;
    private PlaceStrategy.UnitGroupInfo mUnitgroupInfo;
    boolean isRefresh;
    protected WeakReference<Activity> mActivityRef;

    public void setTrackingInfo(AdTrackingInfo adTrackingInfo) {
        mTrackingInfo = adTrackingInfo;
    }

    public AdTrackingInfo getTrackingInfo() {
        return mTrackingInfo;
    }

    public PlaceStrategy.UnitGroupInfo getmUnitgroupInfo() {
        return mUnitgroupInfo;
    }

    public void setmUnitgroupInfo(PlaceStrategy.UnitGroupInfo mUnitgroupInfo) {
        this.mUnitgroupInfo = mUnitgroupInfo;
    }

    public void setRefresh(boolean isRefresh) {
        this.isRefresh = isRefresh;
    }

    public boolean isRefresh() {
        return this.isRefresh;
    }

    public void refreshActivityContext(Activity activity) {
        mActivityRef = new WeakReference<>(activity);
    }

    @Deprecated
    protected void log(String tag, String msg) {
    }

    public boolean initNetworkObjectByPlacementId(Context context, Map<String, Object> serverExtras, ATMediationSetting mediationSetting) {
        return false;
    }

    public abstract boolean isAdReady();

    public void log(String action, String status, String extraMsg) {
        if (ATSDK.NETWORK_LOG_DEBUG) {
            if (mTrackingInfo != null) {
                try {
                    JSONObject jsonObject = new JSONObject();
                    if (mTrackingInfo.ismIsDefaultNetwork()) {
                        jsonObject.put("isDefault", true);
                    }
                    jsonObject.put("placemengId", mTrackingInfo.getmPlacementId());
                    jsonObject.put("adType", mTrackingInfo.getAdTypeString());
                    jsonObject.put("action", action);
                    jsonObject.put("refresh", mTrackingInfo.getmRefresh());
                    jsonObject.put("result", status);
                    jsonObject.put("position", mTrackingInfo.getmLevel());
                    jsonObject.put("networkType", mTrackingInfo.getmNetworkType());
                    jsonObject.put("networkName", mTrackingInfo.getNetworkName());
                    jsonObject.put("networkUnit", mTrackingInfo.getmNetworkContent());
                    jsonObject.put("msg", extraMsg);
                    jsonObject.put("hourly_frequency", mTrackingInfo.getmHourlyFrequency());
                    jsonObject.put("daily_frequency", mTrackingInfo.getmDailyFrequency());
                    jsonObject.put("network_list", mTrackingInfo.getmNetworkList());
                    jsonObject.put("request_network_num", mTrackingInfo.getmRequestNetworkNum());
                    SDKContext.getInstance().printJson(Const.RESOURCE_HEAD + "_network", jsonObject.toString());

                } catch (Exception e) {

                }

            }
        }
    }

    public abstract String getSDKVersion();

    public abstract void clean();

    public abstract String getNetworkName();

}
