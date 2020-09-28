
package com.anythink.core.common.track;


import android.content.Context;

import com.anythink.core.api.AdError;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.entity.AgentInfoBean;
import com.anythink.core.common.net.AgentLogLoader;
import com.anythink.core.common.net.OnHttpLoaderListener;
import com.anythink.core.common.net.socket.AgentSocketData;
import com.anythink.core.common.net.socket.SocketUploadData;
import com.anythink.core.common.utils.CommonLogUtil;
import com.anythink.core.common.utils.SPUtil;
import com.anythink.core.common.utils.task.TaskManager;
import com.anythink.core.strategy.AppStrategy;
import com.anythink.core.strategy.AppStrategyManager;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Agent {

    private final String TAG = "Agent";

    private static Agent sInstance;

    private int MIN_LOG_COUNT = 5;
    private int MAX_LOG_COUNT = 10;

    private long SEND_LOG_INTERVAL = 30 * 60 * 1000L;

    private String LOG_FILE = "";
    private String TMP_FILE = "";

    private Context mContext;

    private File mLogFile;

    private AtomicInteger mLogCount;
    private boolean mIsSendingLog = false;

    private String mAppID = "";

    private OnHttpLoaderListener logListener = new OnHttpLoaderListener() {
        @Override
        public void onLoadStart(int reqCode) {

        }

        @Override
        public void onLoadFinish(int reqCode, Object result) {
            removeLog((int) result);
            mIsSendingLog = false;
            SPUtil.putLong(mContext, Const.SPU_NAME, "LOG_SEND_TIME", System.currentTimeMillis());
        }

        @Override
        public void onLoadError(int reqCode, String msg, AdError errorBean) {
            mIsSendingLog = false;
        }

        @Override
        public void onLoadCanceled(int reqCode) {
            mIsSendingLog = false;
        }

    };

    private SocketUploadData.SocketListener socketListener = new SocketUploadData.SocketListener() {
        @Override
        public void onSuccess(Object result) {
            if (result instanceof AgentSocketData) {
                removeLog(((AgentSocketData) result).getLogCount());
                mIsSendingLog = false;
                SPUtil.putLong(mContext, Const.SPU_NAME, "LOG_SEND_TIME", System.currentTimeMillis());
            }
        }

        @Override
        public void onError(Throwable throwable) {
            mIsSendingLog = false;
        }
    };

    public synchronized static Agent getInstance() {
        if (sInstance == null) {
            sInstance = new Agent();
        }
        return sInstance;
    }

    private Agent() {

    }

    public void init(Context context) {
        if (mContext != null) {
            return;
        }

        mAppID = SDKContext.getInstance().getAppId();
        mContext = context.getApplicationContext();
        //Check file status
        try {
            LOG_FILE = mContext.getApplicationContext().getFilesDir().getAbsolutePath() + File.separator + "log" + File.separator + Const.RESOURCE_HEAD + "_agent_log";
            TMP_FILE = mContext.getApplicationContext().getFilesDir().getAbsolutePath() + File.separator + "log" + File.separator + Const.RESOURCE_HEAD + "_temp_log";

            if (mLogFile == null) {
                mLogFile = new File(LOG_FILE);
                if (!mLogFile.getParentFile().exists()) {
                    mLogFile.getParentFile().mkdirs();
                }
                if (!mLogFile.exists()) {
                    mLogFile.createNewFile();
                }
            }

            LineNumberReader lineNumberReader = null;
            try {
                /**Get the file line number**/
                lineNumberReader = new LineNumberReader(new FileReader(mLogFile));
                lineNumberReader.skip(Long.MAX_VALUE);
                int lineNumber = lineNumberReader.getLineNumber();
                if (mLogCount == null) {
                    mLogCount = new AtomicInteger(lineNumber);
                }
                lineNumberReader.close();
                CommonLogUtil.i(TAG, "init file log count:" + mLogCount.get());
            } catch (Exception e) {
                if (Const.DEBUG) {
                    e.printStackTrace();
                }
            } finally {
                try {
                    if (lineNumberReader != null) {
                        lineNumberReader.close();
                    }
                } catch (IOException e) {
                    if (Const.DEBUG) {
                        e.printStackTrace();
                    }
                }
            }

            if (mLogCount == null) {
                mLogCount = new AtomicInteger(0);
            }

            AppStrategy appStrategy = AppStrategyManager.getInstance(mContext).getAppStrategyByAppId(mAppID);
            MIN_LOG_COUNT = appStrategy.getDaMaxAmount() != 0 ? appStrategy.getDaMaxAmount() : MIN_LOG_COUNT;
            MAX_LOG_COUNT = MIN_LOG_COUNT * 2;
            SEND_LOG_INTERVAL = appStrategy.getDaInterval() != 0 ? appStrategy.getDaInterval() : SEND_LOG_INTERVAL;
            //Send the log over 30 min
            sendLogByTime();


        } catch (Exception e) {
            if (Const.DEBUG) {
                e.printStackTrace();
            }
        } catch (OutOfMemoryError | StackOverflowError e) {
            System.gc();
        } catch (Error e) {
            if (Const.DEBUG) {
                e.printStackTrace();
            }
        } finally {
            if (mLogCount == null) {
                mLogCount = new AtomicInteger(0);
            }
        }


    }

    /**
     * Record log
     */
    protected synchronized void onEvent(AgentInfoBean agentInfoBean) {
        if (mLogFile == null || mLogCount == null) {
            init(SDKContext.getInstance().getContext());
        }

        AppStrategy appStrategy = AppStrategyManager.getInstance(mContext).getAppStrategyByAppId(mAppID);
        MIN_LOG_COUNT = appStrategy.getDaMaxAmount() != 0 ? appStrategy.getDaMaxAmount() : MIN_LOG_COUNT;
        MAX_LOG_COUNT = MIN_LOG_COUNT * 2;
        SEND_LOG_INTERVAL = appStrategy.getDaInterval();

        FileWriter mLogWriter = null;
        try {
            JSONObject jsonObject = agentInfoBean.toJSONObject();

            String log = jsonObject.toString();

            if (Const.DEBUG) {
                SDKContext.getInstance().printJson("AgentEvent", log);
            }

            mLogWriter = new FileWriter(mLogFile, true);
//            CommonLogUtil.i(TAG, "onEvent Log :" + log);
            mLogWriter.append(log);
            mLogWriter.append("\n");
            mLogWriter.flush();

            mLogWriter.close();

            //计数
            mLogCount.incrementAndGet();

        } catch (Exception e) {
            if (Const.DEBUG) {
                e.printStackTrace();
            }
        } catch (OutOfMemoryError | StackOverflowError e) {
            System.gc();
        } catch (Error e) {
            if (Const.DEBUG) {
                e.printStackTrace();
            }
        } finally {
            try {
                if (mLogWriter != null) {
                    mLogWriter.close();
                }
            } catch (IOException e) {
                if (Const.DEBUG) {
                    e.printStackTrace();
                }
            }
        }

        tryToSend();

    }


    private synchronized void tryToSend() {

        if (mContext == null) {
            return;
        }

        if (!mIsSendingLog && mLogCount != null && mLogCount.get() >= MIN_LOG_COUNT) {

            mIsSendingLog = true;

            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(mLogFile));
                int count = 0;
                String line = null;
                List<String> logList = new ArrayList<>();
                while (count < MAX_LOG_COUNT && (line = br.readLine()) != null) {
                    String tempLine = line;
                    logList.add(tempLine);
                    CommonLogUtil.i(TAG, "Try to send:" + tempLine);
                    count++;
                }


                AppStrategy appStrategy = AppStrategyManager.getInstance(SDKContext.getInstance().getContext()).getAppStrategyByAppId(SDKContext.getInstance().getAppId());
                if (appStrategy != null) {
                    switch (appStrategy.getTcpSwitchType()) {
                        case 1: //Only TCP
                            AgentSocketData agentSocketData = new AgentSocketData(logList);
                            agentSocketData.setTcpInfo(1, appStrategy.getTcpRate());
                            agentSocketData.startToUpload(socketListener);
                            break;
                        default: //HTTP(s)
                            AgentLogLoader sAgentLogSender = new AgentLogLoader(mContext, appStrategy.getTcpSwitchType(), logList);
                            sAgentLogSender.start(0, logListener);
                            break;
                    }
                } else {
                    AgentLogLoader sAgentLogSender = new AgentLogLoader(mContext, 0, logList);
                    sAgentLogSender.start(0, logListener);
                }

            } catch (Exception e) {
                mIsSendingLog = false;
                if (Const.DEBUG) {
                    e.printStackTrace();
                }
            } catch (OutOfMemoryError | StackOverflowError e) {
                mIsSendingLog = false;
                System.gc();
            } catch (Throwable throwable) {
                mIsSendingLog = false;
            } finally {
                try {
                    if (br != null) {
                        br.close();
                    }
                } catch (Exception e) {

                }
            }
        }
    }


    private synchronized void removeLog(int lines) {
        BufferedReader br = null;
        try {
            File tempFile = new File(TMP_FILE);
            if (!tempFile.exists()) {
                tempFile.createNewFile();
            }
            br = new BufferedReader(new FileReader(mLogFile));
            FileWriter fw = new FileWriter(tempFile);

            String line = null;
            int count = 0;
            while ((line = br.readLine()) != null) {
                //Logger.v(TAG, "readLine: " +line);
                count++;
                if (count > lines) {
                    fw.append(line);
                    fw.append("\n");
                } else {
                    CommonLogUtil.i(TAG, "Remove log:" + line);
                }
            }
            fw.flush();
            fw.close();
            br.close();

            mLogCount.set(mLogCount.get() - lines < 0 ? 0 : (mLogCount.get() - lines));
            mLogFile.delete();
            tempFile.renameTo(mLogFile);
        } catch (Exception e) {
            if (Const.DEBUG) {
                e.printStackTrace();
            }
        } catch (OutOfMemoryError | StackOverflowError e) {
            if (Const.DEBUG) {
                e.printStackTrace();
            }
            System.gc();
        } catch (Error e) {
            if (Const.DEBUG) {
                e.printStackTrace();
            }
        } catch (Throwable e) {

        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                if (Const.DEBUG) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * exceeds 30minutes, send log
     */
    public void sendLogByTime() {
        TaskManager.getInstance().run_proxy(new Runnable() {
            @Override
            public void run() {
                synchronized (this) {
                    try {
                        if (mContext == null) {
                            return;
                        }
                        long lastTime = SPUtil.getLong(mContext, Const.SPU_NAME, "LOG_SEND_TIME", 0L);
                        if ((System.currentTimeMillis() - lastTime) > SEND_LOG_INTERVAL || (mLogCount != null && mLogCount.get() >= MIN_LOG_COUNT)) {
                            CommonLogUtil.i("Agent", "sendLogByTime:30 minites");
                            if (!mIsSendingLog && mLogCount != null && mLogCount.get() > 0) {

                                mIsSendingLog = true;

                                BufferedReader br = null;
                                try {
                                    br = new BufferedReader(new FileReader(mLogFile));
                                    int count = 0;
                                    String line = null;
                                    List<String> logList = new ArrayList<>();
                                    while (count < MAX_LOG_COUNT && (line = br.readLine()) != null) {
                                        String tmpLine = line;
                                        logList.add(tmpLine);
                                        CommonLogUtil.i(TAG, "SendLogByTime:" + line);
                                        count++;
                                    }
                                    br.close();

                                    if (count == 0) { //如果没有日志则返回
                                        mIsSendingLog = false;
                                        return;
                                    }

                                    //Send Data
                                    AppStrategy appStrategy = AppStrategyManager.getInstance(SDKContext.getInstance().getContext()).getAppStrategyByAppId(SDKContext.getInstance().getAppId());
                                    if (appStrategy != null) {
                                        switch (appStrategy.getTcpSwitchType()) {
                                            case 1: //Only TCP
                                                AgentSocketData agentSocketData = new AgentSocketData(logList);
                                                agentSocketData.setTcpInfo(1, appStrategy.getTcpRate());
                                                agentSocketData.startToUpload(socketListener);
                                                break;
                                            default: //HTTP(s)
                                                AgentLogLoader sAgentLogSender = new AgentLogLoader(mContext, appStrategy.getTcpSwitchType(), logList);
                                                sAgentLogSender.start(0, logListener);
                                                break;
                                        }
                                    } else {
                                        AgentLogLoader sAgentLogSender = new AgentLogLoader(mContext, 0, logList);
                                        sAgentLogSender.start(0, logListener);
                                    }

                                } catch (Exception e) {
                                    mIsSendingLog = false;
                                    if (Const.DEBUG) {
                                        e.printStackTrace();
                                    }
                                } catch (OutOfMemoryError | StackOverflowError e) {
                                    mIsSendingLog = false;
                                    if (Const.DEBUG) {
                                        e.printStackTrace();
                                    }
                                    System.gc();
                                } catch (Error e) {
                                    mIsSendingLog = false;
                                    if (Const.DEBUG) {
                                        e.printStackTrace();
                                    }
                                } finally {
                                    try {
                                        if (br != null) {
                                            br.close();
                                        }
                                    } catch (Exception e) {
                                        if (Const.DEBUG) {
                                            e.printStackTrace();
                                        }
                                    }

                                }
                            }
                        }
                    } catch (Throwable e) {
                        if (Const.DEBUG) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

    }
}
