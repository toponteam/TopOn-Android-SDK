package com.anythink.china.common.download.task;


import com.anythink.core.common.base.Const;
import com.anythink.core.common.utils.CommonLogUtil;
import com.anythink.core.common.utils.task.Worker;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 */
public class TaskManager {

    public final static int TYPE_SINGLE = 1;
    public final static int TYPE_NORMAL = 2;


    private static TaskManager sSelf = null;

    private ExecutorService mNormalPool = null;


    protected TaskManager() {
        mNormalPool = Executors.newSingleThreadExecutor();

    }

    static public TaskManager getInstance() {
        if (sSelf == null) {
            sSelf = new TaskManager();
        }
        return sSelf;
    }

    static protected void setInstance(TaskManager taskManager) {
        sSelf = taskManager;
    }


    public void run(Worker worker, int type) {
        mNormalPool.execute(worker);
    }

    public void run(Worker worker) {
        run(worker, TYPE_NORMAL);
    }

    public void run_proxy(final Runnable runnable) {
        run_proxyDelayed(runnable, 0);
    }

    public void run_proxyDelayed(final Runnable runnable, final long delayed) {
        if (runnable != null) {
            Worker worker = new Worker() {
                @Override
                public void work() {
                    try {
                        Thread.sleep(delayed);
                    } catch (InterruptedException e) {
                        if (Const.DEBUG) {
                            e.printStackTrace();
                        }
                    }
                    CommonLogUtil.d("t", "thread-" + this.getID());
                    runnable.run();

                }
            };
            worker.setID(Long.valueOf(System.currentTimeMillis() / 1000).intValue());
            run(worker);
        }
    }

    public void release() {
        mNormalPool.shutdown();
    }


}
