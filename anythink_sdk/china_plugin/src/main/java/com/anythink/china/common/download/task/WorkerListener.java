package com.anythink.china.common.download.task;

/**
 * Created by chenzhf on 2014/12/23.
 */
public interface WorkerListener {
    void onWorkStart(Worker worker);

    void onWorkFinished(Worker worker);
}
