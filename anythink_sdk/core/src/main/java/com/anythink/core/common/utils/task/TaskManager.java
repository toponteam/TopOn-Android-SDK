/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.common.utils.task;


import android.os.Looper;

import com.anythink.core.common.base.Const;
import com.anythink.core.common.utils.CommonLogUtil;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 */
public class TaskManager {

    public final static int TYPE_SINGLE = 1;
    public final static int TYPE_NORMAL = 2;
    public final static int TYPE_NETWORK_REQUEST = 3;
    public final static int TYPE_TCP_LOG = 4;
    public final static int TYPE_IMAGE_TYPE = 5;
    public final static int TYPE_PRELOAD_TASK = 6;

    private static TaskManager sSelf = null;

    private ExecutorService mNormalPool = null;
    private ExecutorService mSinglePool = null;
    private ExecutorService mNetworkReuqestPool = null;
    private ExecutorService mTcpLogPool = null;
    private ExecutorService mImagePool = null;
    private ExecutorService mOfferPreLoadPool = null;


    protected TaskManager() {
        mNormalPool = Executors.newCachedThreadPool();
        mSinglePool = Executors.newSingleThreadExecutor();
        mNetworkReuqestPool = Executors.newCachedThreadPool();

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


    public synchronized void run(Worker worker, int type) {

        switch (type) {

            case TYPE_SINGLE:
                mSinglePool.execute(worker);
                break;
            case TYPE_NORMAL:
                mNormalPool.execute(worker);
                break;
            case TYPE_NETWORK_REQUEST:
                mNetworkReuqestPool.execute(worker);
                break;
            case TYPE_TCP_LOG:
                if (mTcpLogPool == null) {
                    mTcpLogPool = Executors.newSingleThreadExecutor();
                }
                mTcpLogPool.execute(worker);
                break;
            case TYPE_IMAGE_TYPE:
                if (mImagePool == null) {
                    mImagePool = Executors.newFixedThreadPool(5);
                }
                mImagePool.execute(worker);
                break;
            case TYPE_PRELOAD_TASK:
                if (mOfferPreLoadPool == null) {
                    mOfferPreLoadPool = Executors.newSingleThreadExecutor();
                }
                mOfferPreLoadPool.execute(worker);
                break;
            default:
                ;
        }

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

    public void runNetworkRequest(final Runnable runnable) {
        if (runnable != null) {
            Worker worker = new Worker() {
                @Override
                public void work() {
                    runnable.run();

                }
            };
            worker.setID(Long.valueOf(System.currentTimeMillis() / 1000).intValue());
            run(worker, TYPE_NETWORK_REQUEST);
        }
    }

    public void release() {
        mSinglePool.shutdown();
        mNormalPool.shutdown();
    }


}
