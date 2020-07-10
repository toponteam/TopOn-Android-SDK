package com.anythink.china.common.download.task;


import com.anythink.core.common.base.Const;
import com.anythink.core.common.utils.CommonLogUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 */
public class TaskManager {

    public final static int TYPE_SINGLE = 1;
    public final static int TYPE_NORMAL = 2;
    public final static int TYPE_FIXED = 3;

    private final int IMAGE_POOL_SIZE = 2;

    private static TaskManager sSelf = null;

    private ExecutorService mFixedPool = null;
    private ExecutorService mNormalPool = null;
    private ExecutorService mSinglePool = null;


    protected TaskManager() {
        mFixedPool = Executors.newFixedThreadPool(IMAGE_POOL_SIZE);
        mNormalPool = Executors.newCachedThreadPool();
        mSinglePool = Executors.newSingleThreadExecutor();

    }

    static public TaskManager getInstance() {
        if (sSelf == null) {
            sSelf = new TaskManager();
        }
        return sSelf;
    }

    static protected  void setInstance(TaskManager taskManager){
        sSelf = taskManager;
    }



    public void run(Worker worker, int type) {

        switch(type){

            case TYPE_SINGLE:
                mSinglePool.execute(worker);
                break;

            case TYPE_NORMAL:
                mNormalPool.execute(worker);
                break;

            case TYPE_FIXED:
                mFixedPool.execute(worker);
                break;
            default:;
        }

    }

    public void run(Worker worker) {
        run(worker, TYPE_NORMAL);
    }

    public void run_proxy(final Runnable runnable){
        run_proxyDelayed(runnable,0);
    }

    public void run_proxyDelayed(final Runnable runnable,final long delayed){
        if(runnable!=null){
            Worker worker = new Worker() {
                @Override
                public void work() {
                    try {
                        Thread.sleep(delayed);
                    } catch (InterruptedException e) {
                        if(Const.DEBUG){e.printStackTrace();}
                    }
                    CommonLogUtil.d("t","thread-"+this.getID());
                    runnable.run();

                }
            };
            worker.setID(new Long(System.currentTimeMillis()/1000).intValue());
            run(worker);
        }
    }

    public void release() {
        mSinglePool.shutdown();
        mNormalPool.shutdown();
        mFixedPool.shutdown();
    }



}
