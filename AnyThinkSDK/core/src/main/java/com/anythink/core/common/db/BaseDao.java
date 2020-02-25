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
