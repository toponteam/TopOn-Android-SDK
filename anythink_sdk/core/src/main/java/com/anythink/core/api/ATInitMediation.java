package com.anythink.core.api;

import android.content.Context;

import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.base.UploadDataLevelManager;
import com.anythink.core.common.track.AgentEventManager;
import com.anythink.core.common.utils.task.TaskManager;
import com.anythink.core.strategy.AppStrategy;
import com.anythink.core.strategy.AppStrategyManager;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ATInitMediation {

    private static ConcurrentHashMap<Integer, Boolean> networkGDPRSettingStatus = new ConcurrentHashMap<>();

    public abstract void initSDK(Context context, Map<String, Object> serviceExtras);

    protected void logGDPRSetting(final int networkFirmId) {
        TaskManager.getInstance().run_proxy(new Runnable() {
            @Override
            public void run() {
                if (networkGDPRSettingStatus.get(networkFirmId) == null || networkGDPRSettingStatus.get(networkFirmId)) {
                    UploadDataLevelManager levelManager = UploadDataLevelManager.getInstance(SDKContext.getInstance().getContext());

                    AppStrategy appStrategy = AppStrategyManager.getInstance(SDKContext.getInstance().getContext()).getAppStrategyByAppId(SDKContext.getInstance().getAppId());

                    //DataConcent=Unknown，gdpr_ia=true，set to NonPersonalized
                    if (levelManager.getUploadDataLevel() == ATSDK.UNKNOWN && appStrategy.getGdprIa() == 1 && appStrategy.getUseNetworkDefaultGDPR() == 0) {
                        AgentEventManager.appSettingGDPRUpdate(1, levelManager.getUploadDataLevel(), appStrategy.getGdprIa());
                    }

                    //DataConcent=Nonpersonalized，gdpr_ia=false，gdpr_so=0
                    if (levelManager.getUploadDataLevel() == ATSDK.NONPERSONALIZED && appStrategy.getGdprSo() == 0 && appStrategy.getGdprIa() == 0) {
                        AgentEventManager.appSettingGDPRUpdate(2, levelManager.getUploadDataLevel(), appStrategy.getGdprIa());
                    }
                    networkGDPRSettingStatus.put(networkFirmId, true);
                }
            }
        });

    }


    public String getNetworkName() {
        return "";
    }

    public String getNetworkSDKClass() {
        return "";
    }

    public Map<String, Boolean> getPluginClassStatus() {
        return null;
    }

    public List getActivityStatus() {
        return null;
    }

    public List getServiceStatus() {
        return null;
    }

    public List getProviderStatus() {
        return null;
    }
}
