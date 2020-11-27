/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.common.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.anythink.core.common.base.Const;


public class CommonLogUtil {

	private static boolean DBG_V = true;
	private static boolean DBG_D = true;
	private static boolean DBG_I = true;
	private static boolean DBG_W = true;
	private static boolean DBG_E = true;
	public static boolean DBG_TOAST = false;
	public static boolean DBG_THROWEXCETON = true;
	private static boolean DBG_LOG_E = false;

	static {
		if (!Const.DEBUG) {
			DBG_V = false;
			DBG_D = false;
			DBG_I = false;
			DBG_W = false;
			DBG_E = false;
			DBG_TOAST = false;
			DBG_THROWEXCETON = false;
			DBG_LOG_E = false;
		}
	}

	private CommonLogUtil() {

	}

	/**
	 * Send a { VERBOSE} log message.
	 * 
	 * @param tag
	 *            Used to identify the source of a log message. It usually
	 *            identifies the class or activity where the log call occurs.
	 * @param msg
	 *            The message you would like logged.
	 */
	public static void v(String tag, String msg) {
		if (DBG_V) {
			Log.v(tag, msg);
		}
	}

	/**
	 * Send a { VERBOSE} log message and log the exception.
	 * 
	 * @param tag
	 *            Used to identify the source of a log message. It usually
	 *            identifies the class or activity where the log call occurs.
	 * @param msg
	 *            The message you would like logged.
	 * @param tr
	 *            An exception to log
	 */
	public static void v(String tag, String msg, Throwable tr) {
		if (DBG_V) {
			Log.v(tag, msg, tr);
		}
	}

	/**
	 * Send a { DEBUG} log message.
	 * 
	 * @param tag
	 *            Used to identify the source of a log message. It usually
	 *            identifies the class or activity where the log call occurs.
	 * @param msg
	 *            The message you would like logged.
	 */
	public static void d(String tag, String msg) {
		if (DBG_D) {
			Log.d(tag, msg);
		}
	}

	/**
	 * Send a { DEBUG} log message and log the exception.
	 * 
	 * @param tag
	 *            Used to identify the source of a log message. It usually
	 *            identifies the class or activity where the log call occurs.
	 * @param msg
	 *            The message you would like logged.
	 * @param tr
	 *            An exception to log
	 */
	public static void d(String tag, String msg, Throwable tr) {
		if (DBG_D) {
			Log.d(tag, msg, tr);
		}
	}

	/**
	 * Send an { INFO} log message.
	 * 
	 * @param tag
	 *            Used to identify the source of a log message. It usually
	 *            identifies the class or activity where the log call occurs.
	 * @param msg
	 *            The message you would like logged.
	 */
	public static void i(String tag, String msg) {
		if (DBG_I) {
			Log.i(tag, msg);
		}
	}

	/**
	 * Send a { INFO} log message and log the exception.
	 * 
	 * @param tag
	 *            Used to identify the source of a log message. It usually
	 *            identifies the class or activity where the log call occurs.
	 * @param msg
	 *            The message you would like logged.
	 * @param tr
	 *            An exception to log
	 */
	public static void i(String tag, String msg, Throwable tr) {
		if (DBG_I) {
			Log.i(tag, msg, tr);
		}
	}

	/**
	 * Send a { WARN} log message.
	 * 
	 * @param tag
	 *            Used to identify the source of a log message. It usually
	 *            identifies the class or activity where the log call occurs.
	 * @param msg
	 *            The message you would like logged.
	 */
	public static void w(String tag, String msg) {
		if (DBG_W) {
			Log.w(tag, msg);
		}
	}

	/**
	 * Send a { WARN} log message and log the exception.
	 * 
	 * @param tag
	 *            Used to identify the source of a log message. It usually
	 *            identifies the class or activity where the log call occurs.
	 * @param msg
	 *            The message you would like logged.
	 * @param tr
	 *            An exception to log
	 */
	public static void w(String tag, String msg, Throwable tr) {
		if (DBG_W) {
			Log.w(tag, msg, tr);
		}
	}

	/*
	 * Send a { WARN} log message and log the exception.
	 * 
	 * @param tag Used to identify the source of a log message. It usually
	 * identifies the class or activity where the log call occurs.
	 * 
	 * @param tr An exception to log
	 */
	public static void w(String tag, Throwable tr) {
		if (DBG_W) {
			Log.w(tag, tr);
		}
	}

	/**
	 * Send an { ERROR} log message.
	 * 
	 * @param tag
	 *            Used to identify the source of a log message. It usually
	 *            identifies the class or activity where the log call occurs.
	 * @param msg
	 *            The message you would like logged.
	 */
	public static void e(String tag, String msg) {
		if (DBG_E) {
			Log.e(tag, msg);
		}
	}

	/**
	 * Send a { ERROR} log message and log the exception.
	 * 
	 * @param tag
	 *            Used to identify the source of a log message. It usually
	 *            identifies the class or activity where the log call occurs.
	 * @param msg
	 *            The message you would like logged.
	 * @param tr
	 *            An exception to log
	 */
	public static void e(String tag, String msg, Throwable tr) {
		if (DBG_E) {
			Log.e(tag, msg, tr);
		}
		if (!DBG_LOG_E) {

			return;
		}
	}

}
