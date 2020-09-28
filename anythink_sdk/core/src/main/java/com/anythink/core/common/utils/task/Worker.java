package com.anythink.core.common.utils.task;

public abstract class Worker implements Runnable  {

    public final static int TYPE_NORMAL = 1;
    public final static int TYPE_PHOTO = 2;
    public final static int TYPE_PRECLICK = 3;

    protected boolean mRunning = true;
    protected WorkerListener mWorkerStatus;
    protected int mType = TYPE_NORMAL;
    private long mWorkID = 0;

    public void setID(long id){
        mWorkID = id;
    }

    public long getID(){
        return mWorkID;
    }

    public void setStatusListener(WorkerListener listener){
        mWorkerStatus = listener;
    }

    @Override
    public void run() {

        if(mWorkerStatus != null){
            mWorkerStatus.onWorkStart(this);
        }

        //这里，就是run()
        work();

        if(mWorkerStatus != null){
            mWorkerStatus.onWorkFinished(this);
        }
//        mRunning = false;
    }

    abstract public void work();


}
