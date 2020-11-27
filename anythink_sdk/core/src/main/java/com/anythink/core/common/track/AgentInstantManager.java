/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.common.track;

import android.content.Context;

import com.anythink.core.common.InstantUpLoadManager;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.entity.AgentInfoBean;
import com.anythink.core.common.net.socket.AgentSocketData;
import com.anythink.core.common.utils.task.TaskManager;
import com.anythink.core.common.net.AgentLogLoader;
import com.anythink.core.strategy.AppStrategy;
import com.anythink.core.strategy.AppStrategyManager;

import java.util.ArrayList;
import java.util.List;

public class AgentInstantManager extends InstantUpLoadManager<AgentInfoBean> {

    private static AgentInstantManager sIntance;


    private AgentInstantManager(Context context) {
        super(context);

    }

    public synchronized static AgentInstantManager getInstance(Context context) {
        if (sIntance == null) {
            sIntance = new AgentInstantManager(context);
        }
        return sIntance;
    }


    @Override
    protected void sendLoggerToServer(final List<AgentInfoBean> sendInfo) {
        TaskManager.getInstance().run_proxy(new Runnable() {
            @Override
            public void run() {
                List<String> logList = new ArrayList<>();
                for (AgentInfoBean infoBean : sendInfo) {
                    logList.add(infoBean.toJSONObject().toString());
                }

                AppStrategy appStrategy = AppStrategyManager.getInstance(SDKContext.getInstance().getContext()).getAppStrategyByAppId(SDKContext.getInstance().getAppId());
                if (appStrategy != null) {
                    switch (appStrategy.getTcpSwitchType()) {
                        case 1: //Only TCP
                            AgentSocketData agentSocketData = new AgentSocketData(logList);
                            agentSocketData.setTcpInfo(1, appStrategy.getTcpRate());
                            agentSocketData.startToUpload(null);
                            break;
                        default: //HTTP(s)
                            new AgentLogLoader(mApplicationContext, appStrategy.getTcpSwitchType(), logList).start(0, null);
                            break;
                    }
                } else {
                    new AgentLogLoader(mApplicationContext, 0, logList).start(0, null);
                }


            }
        });

    }


}
