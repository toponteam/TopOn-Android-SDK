/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.common.db;

import android.database.sqlite.SQLiteDatabase;

/**
 * 
 */
public class BaseDao<T> {

	protected CommonAbsDBHelper mHelper = null;

	public BaseDao(CommonAbsDBHelper helper) {
		mHelper = helper;
	}

	protected synchronized SQLiteDatabase getReadableDatabase() {
		return mHelper.getReadableDatabase();
	}

	protected synchronized SQLiteDatabase getWritableDatabase() {
		return mHelper.getWritableDatabase();
	}


}
