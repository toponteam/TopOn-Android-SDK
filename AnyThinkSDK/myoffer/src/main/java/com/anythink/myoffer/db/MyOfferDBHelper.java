
package com.anythink.myoffer.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.anythink.core.common.base.Const;

public class MyOfferDBHelper extends SQLiteOpenHelper {
    private static MyOfferDBHelper sIntance;
    private static final int DB_VERSION = 2; //DB version

    public static MyOfferDBHelper getInstance(Context context) {
        if (sIntance == null) {
            sIntance = new MyOfferDBHelper(context, Const.RESOURCE_HEAD + "_myoffer", null, DB_VERSION);
        }
        return sIntance;
    }

    @Override
    public SQLiteDatabase getReadableDatabase() {
        SQLiteDatabase sqLiteDatabase = null;
        try {
            sqLiteDatabase = super.getReadableDatabase();
        } catch (Exception e) {

        }
        return sqLiteDatabase;
    }

    @Override
    public synchronized SQLiteDatabase getWritableDatabase() {
        SQLiteDatabase sqLiteDatabase = null;
        try {
            sqLiteDatabase = super.getWritableDatabase();
        } catch (Exception e) {

        }
        return sqLiteDatabase;
    }

    public MyOfferDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (int i = oldVersion; i < newVersion; i++) {
            switch (i) { //版本2：增加MyOffer的Click Mode的写入
                case 1:
                    db.execSQL(MyOfferAdDao.Table.UPGRADE_TO_2_SQL);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    private void createTable(SQLiteDatabase db) {
        try {
            db.execSQL(MyOfferAdDao.Table.TABLE_CREATE_SQL);
            db.execSQL(MyOfferImpressionDao.Table.TABLE_CREATE_SQL);
            onUpgrade(db, 1, DB_VERSION);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dropTable(SQLiteDatabase db) {
        try {
            db.execSQL("DROP TABLE IF EXISTS '" + MyOfferAdDao.Table.TABLE_CREATE_SQL + "'");
            db.execSQL("DROP TABLE IF EXISTS '" + MyOfferImpressionDao.Table.TABLE_CREATE_SQL + "'");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
