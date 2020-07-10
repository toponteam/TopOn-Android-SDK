package com.anythink.core.common.track;

import android.content.Context;

import com.anythink.core.common.InstantUpLoadManager;
import com.anythink.core.common.entity.AgentInfoBean;
import com.anythink.core.common.utils.task.TaskManager;
import com.anythink.core.common.net.AgentLogLoader;

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
                new AgentLogLoader(mApplicationContext, logList).start(0, null);
            }
        });

    }


}
