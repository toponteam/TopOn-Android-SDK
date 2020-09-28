package com.anythink.network.maio;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.AdError;
import com.anythink.core.api.ErrorCode;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;

import java.util.Map;

import jp.maio.sdk.android.MaioAds;

public class MaioATRewardedVideoAdapter extends CustomRewardVideoAdapter implements MaioATNotify {

    String mZoneId;

    @Override
    public void loadCustomNetworkAd(Context context, Map<String, Object> serverExtras, Map<String, Object> localExtras) {
        String mMediaId = "";
        if (serverExtras.containsKey("media_id")) {
            mMediaId = serverExtras.get("media_id").toString();
        }

        if (serverExtras.containsKey("zone_id")) {
            mZoneId = serverExtras.get("zone_id").toString();
        }

        if (TextUtils.isEmpty(mMediaId) || TextUtils.isEmpty(mZoneId)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", " mediaid or zoneid  is empty.");
            }
            return;
        }

        if (!(context instanceof Activity)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "Maio context must be activity.");
            }
            return;
        }

        if (MaioAds.canShow(mZoneId)) {
            if (mLoadListener != null) {
                mLoadListener.onAdCacheLoaded();
            }
            return;
        }

        MaioATInitManager.getInstance().addLoadResultListener(mZoneId, this);
        MaioATInitManager.getInstance().initSDK(context, serverExtras);
    }

    @Override
    public boolean initNetworkObjectByPlacementId(Context context, Map<String, Object> serverExtras, Map<String, Object> localExtras) {
        if (serverExtras != null) {
            if (serverExtras.containsKey("zone_id")) {
                mZoneId = serverExtras.get("zone_id").toString();
            }
            return true;
        }
        return false;
    }

    @Override
    public void show(Activity activity) {
        MaioATInitManager.getInstance().addListener(mZoneId, this);
        MaioAds.show(mZoneId);
    }

    @Override
    public boolean isAdReady() {
        return MaioAds.canShow(mZoneId);
    }

    @Override
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        return false;
    }

    @Override
    public void destory() {

    }

    @Override
    public String getNetworkName() {
        return MaioATInitManager.getInstance().getNetworkName();
    }

    @Override
    public String getNetworkPlacementId() {
        return mZoneId;
    }

    //internal callback
    @Override
    public void notifyLoaded() {
        if (mLoadListener != null) {
            mLoadListener.onAdCacheLoaded();
        }
    }

    @Override
    public void notifyLoadFail(String code, String msg) {
        if (mLoadListener != null) {
            mLoadListener.onAdLoadError(code, msg);
        }
    }

    @Override
    public void notifyPlayStart() {
        if (mImpressionListener != null) {
            mImpressionListener.onRewardedVideoAdPlayStart();
        }
    }

    @Override
    public void notifyClick() {
        if (mImpressionListener != null) {
            mImpressionListener.onRewardedVideoAdPlayClicked();
        }
    }

    @Override
    public void notifyClose() {
        if (mImpressionListener != null) {
            mImpressionListener.onRewardedVideoAdClosed();
        }
    }

    @Override
    public void notifyPlayEnd(boolean isReward) {
        if (mImpressionListener != null) {
            mImpressionListener.onRewardedVideoAdPlayEnd();
        }

        if (mImpressionListener != null) {
            mImpressionListener.onReward();
        }

    }

    @Override
    public void notifyPlayFail(String code, String msg) {
        if (mImpressionListener != null) {
            mImpressionListener.onRewardedVideoAdPlayFailed(code, msg);
        }
    }

    @Override
    public String getNetworkSDKVersion() {
        return MaioATConst.getNetworkVersion();
    }
}
