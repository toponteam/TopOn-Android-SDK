
package com.anythink.core.common.track;


import android.content.Context;

import com.anythink.core.api.AdError;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.entity.AgentInfoBean;
import com.anythink.core.common.net.AgentLogLoader;
import com.anythink.core.common.net.OnHttpLoaderListener;
import com.anythink.core.common.utils.CommonLogUtil;
import com.anythink.core.common.utils.SPUtil;
import com.anythink.core.strategy.AppStrategy;
import com.anythink.core.strategy.AppStrategyManager;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Agent {

    static private final String TAG = "agent";

    static private int MIN_LOG_COUNT = 5;
    static private int MAX_LOG_COUNT = 10;

    static private long SEND_LOG_INTERVAL = 30 * 60 * 1000L;

    static private String LOG_FILE = "";
    static private String TMP_FILE = "";

    private static Context mContext = null;


    private static HashMap<String, Long> sTimerMap = new HashMap();

    private static File mLogFile = null;

    private static AtomicInteger mLogCount;
    private static boolean mIsSendingLog = false;

    private static String mAppID = "";

    private static OnHttpLoaderListener logListener = new OnHttpLoaderListener() {
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


    synchronized static public boolean init(Context c) {
        if (c == null) {
            return false;
        }
        mAppID = SDKContext.getInstance().getAppId();
        mContext = c;

        boolean needToResetLogCount = false;

        if (mLogCount == null) {
            mLogCount = new AtomicInteger(0);
            needToResetLogCount = true;
        }


        //Check file status
        try {
            LOG_FILE = c.getApplicationContext().getFilesDir().getAbsolutePath() + File.separator + "log" + File.separator + Const.RESOURCE_HEAD + "_agent_log";
            TMP_FILE = c.getApplicationContext().getFilesDir().getAbsolutePath() + File.separator + "log" + File.separator + Const.RESOURCE_HEAD + "_temp_log";

            if (mLogFile == null) {
                mLogFile = new File(LOG_FILE);
                if (!mLogFile.getParentFile().exists()) {
                    mLogFile.getParentFile().mkdirs();
                }
                if (!mLogFile.exists()) {
                    mLogFile.createNewFile();
                }
            }

            AppStrategy appStrategy = AppStrategyManager.getInstance(mContext).getAppStrategyByAppId(mAppID);
            MIN_LOG_COUNT = appStrategy.getDaMaxAmount() != 0 ? appStrategy.getDaMaxAmount() : MIN_LOG_COUNT;
            MAX_LOG_COUNT = MIN_LOG_COUNT * 2;
            SEND_LOG_INTERVAL = appStrategy.getDaInterval() != 0 ? appStrategy.getDaInterval() : SEND_LOG_INTERVAL;
            //Send the log over 30 min
            Agent.sendLogByTime(needToResetLogCount);


        } catch (Exception e) {
            if (Const.DEBUG) {
                e.printStackTrace();
            }
            return false;
        } catch (OutOfMemoryError | StackOverflowError e) {
            System.gc();
        } catch (Error e) {
            if (Const.DEBUG) {
                e.printStackTrace();
            }
        }
        return true;
    }


    /**
     * Record log
     */
    protected static synchronized void onEvent(AgentInfoBean agentInfoBean) {
        if (mLogFile == null || mLogCount == null) {
            init(SDKContext.getInstance().getContext());
        }

        AppStrategy appStrategy = AppStrategyManager.getInstance(mContext).getAppStrategyByAppId(mAppID);
        MIN_LOG_COUNT = appStrategy.getDaMaxAmount() != 0 ? appStrategy.getDaMaxAmount() : MIN_LOG_COUNT;
        MAX_LOG_COUNT = MIN_LOG_COUNT * 2;
        SEND_LOG_INTERVAL = appStrategy.getDaInterval();

        try {
            JSONObject jsonObject = agentInfoBean.toJSONObject();

            String log = jsonObject.toString();

            if (Const.DEBUG) {
                SDKContext.getInstance().printJson("AgentEvent", log);
            }

            FileWriter mLogWriter = new FileWriter(mLogFile, true);

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
        }

        tryToSend();

    }


    /**
     * InstantEvent
     *
     * @param key
     */
//    public static void onInstantEvent(final int key, final Map<String, String> otherInfoMap) {
//        try {
//            JSONObject jsonObject = new JSONObject();
//            jsonObject.put(Const.AgentKey.KEY, key);
//            for (String logKey : otherInfoMap.keySet()) {
//                jsonObject.put(logKey, otherInfoMap.get(logKey));
//            }
//            long timeStamp = System.currentTimeMillis();
//            //add timestamp
//            jsonObject.put(Const.AgentKey.TIMESTAMP, String.valueOf(timeStamp));
//
//            String log = jsonObject.toString();
//
//            StringBuffer logStr = new StringBuffer();
//            logStr.append(log);
//            logStr.append("\n");
//            AgentLogLoader agentLogSender = new AgentLogLoader(mContext, mAppID, mAppKey);
//
//            String[] logs = new String[]{logStr.toString()};
//            agentLogSender.setParams(logs, logs.length);
//            agentLogSender.start(0, new OnHttpLoaderListener() {
//                @Override
//                public void onLoadStart(int reqCode) {
//                }
//
//                @Override
//                public void onLoadFinish(int reqCode, Object result) {
//                }
//
//                @Override
//                public void onLoadError(int reqCode, String msg, AdError errorBean) {
//                    //如果失败就放在批量上报
//                    onEvent(key, otherInfoMap);
//                }
//
//                @Override
//                public void onLoadCanceled(int reqCode) {
//                }
//            });
//        } catch (Throwable e) {
//            if (Const.DEBUG) {
//                e.printStackTrace();
//            }
//        }
//
//
//    }


    private static synchronized void tryToSend() {

        if (mContext == null) {
            return;
        }

        if (!mIsSendingLog && mLogCount.get() >= MIN_LOG_COUNT) {

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
                    count++;
                }

                AgentLogLoader sAgentLogSender = new AgentLogLoader(mContext, logList);
                sAgentLogSender.start(0, logListener);

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


    private static synchronized void removeLog(int lines) {
        try {
            File tempFile = new File(TMP_FILE);
            if (!tempFile.exists()) {
                tempFile.createNewFile();
            }
            BufferedReader br = new BufferedReader(new FileReader(mLogFile));
            FileWriter fw = new FileWriter(tempFile);

            String line = null;
            int count = 0;
            while ((line = br.readLine()) != null) {
                //Logger.v(TAG, "readLine: " +line);
                count++;
                if (count > lines) {
                    fw.append(line);
                    fw.append("\n");
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

        }
    }


    /**
     * exceeds 30minutes, send log
     */
    public static void sendLogByTime(boolean needToResetLogCount) {
        if (mContext == null) {
            return;
        }
        long lastTime = SPUtil.getLong(mContext, Const.SPU_NAME, "LOG_SEND_TIME", 0L);
        if ((System.currentTimeMillis() - lastTime) > 30 * 60 * 1000 && !mIsSendingLog) {
            CommonLogUtil.i("Agent", "sendLogByTime:30 minites");
            if (!mIsSendingLog && mLogCount != null && (mLogCount.get() > 0 || needToResetLogCount)) {

                mIsSendingLog = true;

                try {
                    BufferedReader br = new BufferedReader(new FileReader(mLogFile));
                    int count = 0;
                    String line = null;
                    List<String> logList = new ArrayList<>();
                    String[] logs = new String[MAX_LOG_COUNT];
                    while (count < MAX_LOG_COUNT && (line = br.readLine()) != null) {
                        String tmpLine = line;
                        logList.add(tmpLine);
                        count++;
                    }
                    br.close();
                    if (needToResetLogCount) {
                        mLogCount.set(count);
                    }

                    if (count == 0) { //如果没有日志则返回
                        mIsSendingLog = false;
                        return;
                    }

                    AgentLogLoader sAgentLogSender = new AgentLogLoader(mContext, logList);
                    // 交给loader去处理
                    sAgentLogSender.start(0, logListener);

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
                }
            }
        }
    }
}
