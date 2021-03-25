/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.common.db;

import android.content.ContentValues;
import android.database.Cursor;

import com.anythink.core.common.base.Const;
import com.anythink.core.common.entity.FailRequestInfo;

import java.util.ArrayList;
import java.util.List;


public class FailRequestInfoDao extends BaseDao<FailRequestInfo> {
    private static final String Tag = FailRequestInfoDao.class.getName();
    private static FailRequestInfoDao instance;

    private int MAX_SIZE = 1000;

    public FailRequestInfoDao(CommonAbsDBHelper helper) {
        super(helper);
    }


    public static FailRequestInfoDao getInstance(CommonAbsDBHelper helper) {
        if (instance == null) {
            instance = new FailRequestInfoDao(helper);
        }
        return instance;
    }

    public synchronized void clean() {
        try {
            if (getWritableDatabase() == null) {
                return;
            }
            getWritableDatabase().delete(Table.TABLE_NAME, null, null);
        } catch (Exception e) {
        }
    }

    public synchronized long insertOrUpdate(FailRequestInfo failRequestInfo) {
        if (getWritableDatabase() == null) {
            return -1;
        }
        Cursor c = null;
        try {
            String sql = "SELECT * FROM " + Table.TABLE_NAME;
            c = getReadableDatabase().rawQuery(sql, null);
            if (c.getCount() >= MAX_SIZE) {
                //直接清除以前的数据
                clean();
            }
        } catch (Exception e) {

        } finally {
            try {
                if (c != null) {
                    c.close();
                    c = null;
                }
            } catch (Exception e) {

            }

        }

        try {
            ContentValues cv = new ContentValues();
            cv.put(Table.ID, failRequestInfo.id);
            cv.put(Table.REQUEST_TYPE, failRequestInfo.requestType);
            cv.put(Table.REQUEST_URL, failRequestInfo.requestUrl);
            cv.put(Table.REQUEST_HEAD, failRequestInfo.headerJSONString);
            cv.put(Table.REQUEST_CONTENT, failRequestInfo.content);
            cv.put(Table.REQUEST_FAIL_TIME, failRequestInfo.time);

            return getWritableDatabase().insert(Table.TABLE_NAME, null, cv);
        } catch (Exception e) {
        }
        return -1;
    }

    public synchronized int delete(FailRequestInfo failRequestInfo) {
        if (getWritableDatabase() == null || failRequestInfo == null) {
            return -1;
        }
        return getWritableDatabase().delete(Table.TABLE_NAME, Table.ID + "=?", new String[]{failRequestInfo.id});
    }

    public synchronized List<FailRequestInfo> queryRequestInfo(int number) {
        String sql = "SELECT * FROM " + Table.TABLE_NAME + " ORDER BY " + Table.REQUEST_FAIL_TIME + " DESC LIMIT " + number;
        Cursor c = null;
        try {
            c = getReadableDatabase().rawQuery(sql, null);
            if (c != null && c.getCount() > 0) {
                List<FailRequestInfo> list = new ArrayList<FailRequestInfo>();
                FailRequestInfo temp;
                while (c.moveToNext()) {
                    temp = new FailRequestInfo();
                    temp.id = c.getString(c.getColumnIndex(Table.ID));
                    temp.requestType = c.getInt(c.getColumnIndex(Table.REQUEST_TYPE));
                    temp.requestUrl = c.getString(c.getColumnIndex(Table.REQUEST_URL));
                    temp.headerJSONString = c.getString(c.getColumnIndex(Table.REQUEST_HEAD));
                    temp.content = c.getString(c.getColumnIndex(Table.REQUEST_CONTENT));
                    temp.time = c.getLong(c.getColumnIndex(Table.REQUEST_FAIL_TIME));

                    list.add(temp);
                }
                c.close();
                return list;
            }
            return null;
        } catch (Exception e) {
            if (Const.DEBUG) {
                e.printStackTrace();
            }
        } catch (OutOfMemoryError error) {
            System.gc();
        } catch (Throwable a) {
            if (Const.DEBUG) {
                a.printStackTrace();
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return null;
    }

    static public class Table {
        public static final String TABLE_NAME = "request_info";
        public static final String ID = "id";
        public static final String REQUEST_TYPE = "req_type";
        public static final String REQUEST_URL = "req_url";
        public static final String REQUEST_HEAD = "req_head";
        public static final String REQUEST_CONTENT = "req_content";
        public static final String REQUEST_FAIL_TIME = "time";
        public static final String TABLE_CREATE_SQL = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(" +
                ID + " TEXT, " +
                REQUEST_TYPE + " INTEGER, " +
                REQUEST_URL + " TEXT, " +
                REQUEST_HEAD + " TEXT, " +
                REQUEST_CONTENT + " TEXT, " +
                REQUEST_FAIL_TIME + " INTEGER " +
                ")";
    }
}
