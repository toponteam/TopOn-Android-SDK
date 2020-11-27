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
import com.anythink.core.common.entity.SDKConfigInfo;
import com.anythink.core.common.utils.CommonLogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhou on 2017/12/29.
 */

public class ConfigInfoDao extends BaseDao<SDKConfigInfo> {
    private static final String TAG = ConfigInfoDao.class.getName();
    private static ConfigInfoDao instance;

    public ConfigInfoDao(CommonAbsDBHelper helper) {
        super(helper);
    }


    public static ConfigInfoDao getInstance(CommonAbsDBHelper helper) {
        if (instance == null) {
            instance = new ConfigInfoDao(helper);
        }
        return instance;
    }

    /**
     * save config data
     *
     * @param key
     * @param json
     * @param type
     * @return
     */
    public synchronized long insertOrUpdate(String key, String json, String type) {
        if (getWritableDatabase() == null) {
            return -1;
        }

        try {
            ContentValues cv = new ContentValues();
            cv.put(Table.C_KEY_NAME, key);
            cv.put(Table.C_TYPE_NAME, type);
            cv.put(Table.C_VALUE_NAME, json);
            cv.put(Table.C_UPDATETIME_NAME, System.currentTimeMillis() + "");
            if (exists(key, type)) {
                CommonLogUtil.d(TAG, "insertOrUpdate-->Update");
                String where = Table.C_KEY_NAME + " = ? AND " + Table.C_TYPE_NAME + " = ?";
                return getWritableDatabase().update(Table.TABLE_NAME, cv, where, new String[]{key, type});
            } else {
                CommonLogUtil.d(TAG, "insertOrUpdate-->insert");
                return getWritableDatabase().insert(Table.TABLE_NAME, null, cv);
            }
        } catch (Exception e) {
            if (Const.DEBUG) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    public synchronized long insertOrUpdate(SDKConfigInfo info) {
        if (getWritableDatabase() == null || info == null) {
            return -1;
        }
        try {
            ContentValues cv = new ContentValues();
            cv.put(Table.C_KEY_NAME, info.getKey());
            cv.put(Table.C_TYPE_NAME, info.getType());
            cv.put(Table.C_VALUE_NAME, info.getValue());
            cv.put(Table.C_UPDATETIME_NAME, info.getUpdatetime());
            if (existsByTime(info.getKey(), info.getUpdatetime(), info.getType())) {
                CommonLogUtil.d(TAG, "existsByTime--update");
                String where = Table.C_KEY_NAME + " = ? AND " + Table.C_TYPE_NAME + " = ? AND " + Table.C_UPDATETIME_NAME + " = ?  ";
                return getWritableDatabase().update(Table.TABLE_NAME, cv, where, new String[]{info.getKey(), info.getType(), info.getUpdatetime()});
            } else {
                CommonLogUtil.d(TAG, "existsByTime--insert");
                return getWritableDatabase().insert(Table.TABLE_NAME, null, cv);
            }
        } catch (Exception e) {
            if (Const.DEBUG) {
                e.printStackTrace();
            }
        }
        return -1;
    }


    public synchronized boolean exists(String key, String type) {
        String sql = "SELECT " + Table.C_KEY_NAME + " FROM " + Table.TABLE_NAME + " WHERE " + Table.C_KEY_NAME + "=?"
                + " AND " + Table.C_TYPE_NAME + "=?";
        CommonLogUtil.d(TAG, sql);
        Cursor c = getReadableDatabase().rawQuery(sql, new String[]{key, type});
        if (c != null && c.getCount() > 0) {
            c.close();
            return true;
        } else {
            if (c != null) {
                c.close();
            }
            return false;
        }
    }

    public synchronized boolean existsByTime(String key, String currTime, String type) {
        String sql = "SELECT " + Table.C_KEY_NAME + " FROM " + Table.TABLE_NAME + " WHERE " + Table.C_KEY_NAME + "='" + key + "'"
                + " AND " + Table.C_TYPE_NAME + "='" + type + "'"
                + " AND " + Table.C_UPDATETIME_NAME + "='" + currTime + "'";
        CommonLogUtil.d(TAG, "existsByTime---->" + sql);
        Cursor c = getReadableDatabase().rawQuery(sql, null);
        if (c != null && c.getCount() > 0) {
            c.close();
            return true;
        } else {
            if (c != null) {
                c.close();
            }
            return false;
        }
    }

    /***
     * Check daily cap of placement
     *
     * @return
     */
    public synchronized List<SDKConfigInfo> queryAllCapByPlaceId(String placeId, String currTimeStr) {
        String sql = "SELECT * FROM " + Table.TABLE_NAME + " WHERE " + Table.C_KEY_NAME + " like '%" + placeId + "|||%' and " + Table.C_TYPE_NAME + " = '" + SDKConfigInfo.TYPE.TYPE_CAP +
                "' and " + Table.C_UPDATETIME_NAME + " like  '%" + currTimeStr + "%'";

        CommonLogUtil.d(TAG, sql);
        Cursor c = null;
        try {
            c = getReadableDatabase().rawQuery(sql, null);
            return formatData(c);
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

    /**
     * Save one cap
     */
    public synchronized void addCapTimesByPlaceId(String key, String currTimeStr) {
        List<SDKConfigInfo> list = queryAllByKeyAndTime(key, currTimeStr, SDKConfigInfo.TYPE.TYPE_CAP);
        if (list != null && list.size() > 0) {
            CommonLogUtil.d(TAG, "update---->" + key);
            for (SDKConfigInfo temp : list) {
                temp.setValue((Integer.parseInt(temp.getValue()) + 1) + "");
                insertOrUpdate(temp);
            }
        } else {
            CommonLogUtil.d(TAG, "insert---->" + key);
            SDKConfigInfo info = new SDKConfigInfo();
            info.setUpdatetime(currTimeStr);
            info.setValue("1");
            info.setType(SDKConfigInfo.TYPE.TYPE_CAP);
            info.setKey(key);
            insertOrUpdate(info);

        }

    }

    private synchronized List<SDKConfigInfo> formatData(Cursor c) {
        if (c != null && c.getCount() > 0) {
            List<SDKConfigInfo> configInfolist = new ArrayList<SDKConfigInfo>();
            SDKConfigInfo ConfigInfoTmp;
            while (c.moveToNext()) {
                ConfigInfoTmp = new SDKConfigInfo();
                ConfigInfoTmp.setKey(c.getString(c.getColumnIndex(Table.C_KEY_NAME)));
                ConfigInfoTmp.setType(c.getString(c.getColumnIndex(Table.C_TYPE_NAME)));
                ConfigInfoTmp.setValue(c.getString(c.getColumnIndex(Table.C_VALUE_NAME)));
                ConfigInfoTmp.setUpdatetime(c.getString(c.getColumnIndex(Table.C_UPDATETIME_NAME)));
                configInfolist.add(ConfigInfoTmp);
            }
            c.close();
            return configInfolist;
        }
        return null;
    }

    /***
     * Clear the placement's cap outside of today
     */
    public synchronized void clearOldCapByPlaceAndDayTime(String nowTime) {
        try {
            if (getWritableDatabase() == null) {
                return;
            }
            String where = Table.C_UPDATETIME_NAME + "< ? and type = ?";
            getWritableDatabase().delete(Table.TABLE_NAME, where, new String[]{nowTime, SDKConfigInfo.TYPE.TYPE_CAP});
        } catch (Exception e) {
            if (Const.DEBUG) {
                e.printStackTrace();
            }
        }
    }

    /***
     * @param key
     * @param type
     * @return
     */
    public synchronized List<SDKConfigInfo> queryAllByKeyAndTime(String key, String currTime, String type) {
        String sql = "SELECT * FROM " + Table.TABLE_NAME + " WHERE " + Table.C_KEY_NAME + " = ? and " + Table.C_TYPE_NAME + " = ? and " + Table.C_UPDATETIME_NAME + " = ?";
        Cursor c = null;
        try {
            c = getReadableDatabase().rawQuery(sql, new String[]{key, type, currTime});
            return formatData(c);
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

    /***
     * @param key
     * @param type
     * @return
     */
    public synchronized List<SDKConfigInfo> queryAllByKey(String key, String type) {
        String sql = "SELECT * FROM " + Table.TABLE_NAME + " WHERE " + Table.C_KEY_NAME + " = ? and " + Table.C_TYPE_NAME + " = ?";
        CommonLogUtil.d(TAG, sql);
        Cursor c = null;
        try {
            c = getReadableDatabase().rawQuery(sql, new String[]{key, type});
            return formatData(c);
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


    /***
     * 查询数据不包括KEY的数据
     * @param key
     * @param type
     * @return
     */
    public synchronized List<SDKConfigInfo> queryOtherKey(String key, String type) {
        String sql = "SELECT * FROM " + Table.TABLE_NAME + " WHERE " + Table.C_KEY_NAME + " != ? and " + Table.C_TYPE_NAME + " = ?";
        Cursor c = null;
        try {
            c = getReadableDatabase().rawQuery(sql, new String[]{key, type});
            return formatData(c);
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

    /***
     * 查询数据
     * @param type
     * @return
     */
    public synchronized List<SDKConfigInfo> queryAllByType(String type) {
        String sql = "SELECT * FROM " + Table.TABLE_NAME + " WHERE " + Table.C_TYPE_NAME + " = ?";
        Cursor c = null;
        try {
            c = getReadableDatabase().rawQuery(sql, new String[]{type});
            return formatData(c);
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
        public static final String TABLE_NAME = "sdkconfig";
        public static final String C_KEY_NAME = "key";
        public static final String C_TYPE_NAME = "type";
        public static final String C_VALUE_NAME = "value";
        public static final String C_UPDATETIME_NAME = "lastupdatetime";

        public static final String TABLE_CREATE_SQL = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(" +
                C_KEY_NAME + " TEXT ," +
                C_TYPE_NAME + " TEXT ," +
                C_UPDATETIME_NAME + " TEXT ," +
                C_VALUE_NAME + " TEXT " +
                ")";
    }
}
