/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.common.base;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.anythink.core.common.track.AgentEventManager;
import com.anythink.core.common.utils.task.TaskManager;
import com.anythink.core.strategy.AppStrategy;
import com.anythink.core.strategy.AppStrategyManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.compile;

public class CrashUtil implements Thread.UncaughtExceptionHandler {

    private Thread.UncaughtExceptionHandler mDefaultExceptionHandler;
    private Context mApplicationContext;
    private static CrashUtil sIntance;
    private SharedPreferences mCrashSP;

    private final String CRASH_TYPE = "crash_type";
    private final String CRASH_MSG = "crash_msg";
    private final String PSID_KEY = "psid";

    String defaultPkgCollect = "com.anythink";

    public synchronized static CrashUtil getInstance(Context context) {
        if (sIntance == null) {
            sIntance = new CrashUtil(context);
        }
        return sIntance;
    }


    private CrashUtil(Context context) {
        mApplicationContext = context;
        mCrashSP = mApplicationContext.getSharedPreferences(Const.SPU_CRASH_NAME, Context.MODE_PRIVATE);
    }

    /**
     * init
     */
    public void init() {
        AppStrategy appStrategy = AppStrategyManager.getInstance(mApplicationContext).getAppStrategyByAppId(SDKContext.getInstance().getAppId());
        if (appStrategy != null && appStrategy.getCrashSwitch() == 0) {
            return;
        }

        try {
            TaskManager.getInstance().run_proxy(new Runnable() {
                @Override
                public void run() {
                    tryToSendCrashInSp();
                }
            });
            //Get the default exception handler
            if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CrashUtil)) {
                mDefaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
            }
            Thread.setDefaultUncaughtExceptionHandler(this);
        } catch (Exception e) {
            if (Const.DEBUG) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void uncaughtException(Thread thread, final Throwable throwable) {
        try {
            if (throwable == null) {
                return;
            }

            //Self Crash catch
            handleExceptionError(throwable);

            if (mDefaultExceptionHandler != null && mDefaultExceptionHandler != this && !(mDefaultExceptionHandler instanceof CrashUtil)) {
                mDefaultExceptionHandler.uncaughtException(thread, throwable);
            }
        } catch (Exception e) {
            if (Const.DEBUG) {
                e.printStackTrace();
            }
        }
    }

    private void handleExceptionError(Throwable throwable) {
        try {
            String crashStack = getStackTraceString(throwable);
            if (isNeedToUpLoad(crashStack)) {
                String crashType = getErrorType(crashStack);
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(CRASH_TYPE, URLEncoder.encode(crashType));
                    jsonObject.put(CRASH_MSG, URLEncoder.encode(crashStack));
                    jsonObject.put(PSID_KEY, SDKContext.getInstance().getPsid());
                    //Save Crash Info
                    try {
                        SharedPreferences.Editor editor = mCrashSP.edit();
                        editor.putString(System.currentTimeMillis() + "_crash", jsonObject.toString());
                        editor.commit();
                    } catch (Exception e) {
                    } catch (Error e) {
                    }
                } catch (Exception e) {
                    if (Const.DEBUG) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Throwable e) {

        }

    }


    private boolean isNeedToUpLoad(String crashStack) {
        AppStrategy appStrategy = AppStrategyManager.getInstance(mApplicationContext).getAppStrategyByAppId(SDKContext.getInstance().getAppId());
        if (appStrategy != null) {
            if (appStrategy.getCrashSwitch() == 0) {
                return false;
            }

            String crashPkgFilterList = appStrategy.getCrashList();
            try {
                if (TextUtils.isEmpty(crashPkgFilterList)) {
                    return true;
                }

                JSONArray jsonArray = new JSONArray(crashPkgFilterList);
                for (int i = 0; i < jsonArray.length(); i++) {
                    String pkg = jsonArray.optString(i);
                    if (crashStack.contains(pkg)) {
                        return true;
                    }
                }

            } catch (Throwable e) {
            }

        } else if (crashStack.contains(defaultPkgCollect)) {
            return true;
        }
        return false;
    }

    private void tryToSendCrashInSp() {
        Map<String, ?> crashMap = mCrashSP.getAll();
        for (Object object : crashMap.values()) {
            String cashString = object != null ? object.toString() : "";

            if (!TextUtils.isEmpty(cashString)) {
                try {
                    JSONObject jsonObject = new JSONObject(cashString);
                    String crashType = jsonObject.optString(CRASH_TYPE);
                    String crashMsg = jsonObject.optString(CRASH_MSG);
                    String psid = jsonObject.optString(PSID_KEY);
                    AgentEventManager.sendCrashAgent(crashType, crashMsg, psid);

                } catch (Exception e) {

                }

            }
        }

        if (crashMap.size() > 0) {
            mCrashSP.edit().clear().commit();
        }
    }

    private static String getStackTraceString(Throwable tr) {
        StringWriter sw = null;
        PrintWriter pw = null;
        try {
            if (tr == null) {
                return "";
            }
            Throwable t = new Throwable(Const.SDK_VERSION_NAME, tr);
            while (t != null) {
                if (t instanceof UnknownHostException) {
                    return "";
                }
                t = t.getCause();
            }
            sw = new StringWriter();
            pw = new PrintWriter(sw);
            tr.printStackTrace(pw);
            pw.flush();
            pw.close();
            sw.close();
            return sw.toString();
        } catch (Exception e) {
            if (Const.DEBUG) {
                e.printStackTrace();
            }
        } finally {
            try {
                if (sw != null) {
                    sw.close();
                }

                if (pw != null) {
                    sw.close();
                }
            } catch (Throwable e) {
                if (Const.DEBUG) {
                    e.printStackTrace();
                }
            }

        }
        return "";

    }

    /**
     * matches error type with regex
     */
    private static String getErrorType(String exception) {
        String type = "";
        try {
            Pattern pattern = compile(".*?(Exception|Error|Death)", CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(exception);
            if (matcher.find()) {
                type = matcher.group(0);
            }
            if (!TextUtils.isEmpty(type)) {
                type = type.replaceAll("Caused by:", "").replaceAll(" ", "");
            }
        } catch (Exception e) {
            if (Const.DEBUG) {
                e.printStackTrace();
            }
        }
        return type;
    }
}
