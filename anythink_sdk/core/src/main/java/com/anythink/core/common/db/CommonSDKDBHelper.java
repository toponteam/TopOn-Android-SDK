/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.common.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.anythink.core.common.base.Const;


/**
 * SDK DB
 * Created by zhou on 2017/12/29.
 */

public class CommonSDKDBHelper extends CommonAbsDBHelper {

    private static CommonSDKDBHelper mHelper;

    private CommonSDKDBHelper(Context context) {
        super(context);
    }

    public static CommonSDKDBHelper getInstance(Context context) {
        if (mHelper == null) {
            synchronized (CommonSDKDBHelper.class) {
                mHelper = new CommonSDKDBHelper(context.getApplicationContext());
            }
        }

        return mHelper;
    }

    @Override
    protected String getDBName() {
        return Const.RESOURCE_HEAD + ".db";
    }

    @Override

    protected int getDBVersion() {
        return Const.SDK_VERSION_DB;
    }

    @Override
    protected void onCreateDB(SQLiteDatabase db) {
        createTable(db);
    }

    /***
     * Upgrade
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    protected void onUpdateDB(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (int i = oldVersion; i < newVersion; i++) {
            switch (i) { //Version 3：add Placement impression table
                case 1:
                case 2:
                    less0t1(db);
                    break;
                case 3:
                    db.execSQL(PlacementImpressionDao.Table.TABLE_CREATE_SQL);
                    break;
                default:
                    break;
            }
        }
    }


    private void createTable(SQLiteDatabase db) {
        try {
            db.execSQL(ConfigInfoDao.Table.TABLE_CREATE_SQL);
            db.execSQL(FailRequestInfoDao.Table.TABLE_CREATE_SQL);
            onUpdateDB(db, 3, Const.SDK_VERSION_DB);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dropTable(SQLiteDatabase db) {
        try {
            db.execSQL("DROP TABLE IF EXISTS '" + ConfigInfoDao.Table.TABLE_NAME + "'");
            db.execSQL("DROP TABLE IF EXISTS '" + FailRequestInfoDao.Table.TABLE_NAME + "'");
            db.execSQL("DROP TABLE IF EXISTS '" + PlacementImpressionDao.Table.TABLE_NAME + "'");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Downgrade
     *
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    protected void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        dropTable(db);
        createTable(db);
    }

    private void less0t1(SQLiteDatabase db) {
        dropTable(db);
        createTable(db);
    }
}
