
/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.common.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public abstract class CommonAbsDBHelper {


    private DatabaseOpenHelper mDatabaseOpenHelper;

    public CommonAbsDBHelper(Context context) {
        this.mDatabaseOpenHelper = new DatabaseOpenHelper(context, getDBName(), getDBVersion());
    }

    public SQLiteDatabase getReadableDatabase(){
        return mDatabaseOpenHelper.getReadableDatabase();
    }

    public synchronized SQLiteDatabase getWritableDatabase(){
    	SQLiteDatabase sqLiteDatabase=null;
    	try{
    		sqLiteDatabase = mDatabaseOpenHelper.getWritableDatabase();
    	}catch(Exception e){
    		
    	}
        return sqLiteDatabase;
    }

    protected abstract String getDBName();

    protected abstract int getDBVersion();

    protected abstract void onCreateDB(SQLiteDatabase db);

    protected abstract void onUpdateDB(SQLiteDatabase db, int oldVersion, int newVersion);

    protected abstract void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion);

    private class DatabaseOpenHelper extends SQLiteOpenHelper {

        public DatabaseOpenHelper(Context context, String databaseName, int databaseVersion) {
            super(context, databaseName, null, databaseVersion);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            CommonAbsDBHelper.this.onCreateDB(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            CommonAbsDBHelper.this.onUpdateDB(db, oldVersion, newVersion);
        }
        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        	CommonAbsDBHelper.this.onDowngrade(db, oldVersion, newVersion);
        }

    }

}
