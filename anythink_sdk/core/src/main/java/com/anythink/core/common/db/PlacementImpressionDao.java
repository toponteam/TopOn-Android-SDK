/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.common.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import com.anythink.core.common.base.Const;
import com.anythink.core.common.entity.PlacementImpressionInfo;
import com.anythink.core.common.utils.CommonLogUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlacementImpressionDao extends BaseDao<PlacementImpressionInfo> {

    private final String TAG = PlacementImpressionDao.class.getName();

    private static PlacementImpressionDao instance;

    private PlacementImpressionDao(CommonAbsDBHelper helper) {
        super(helper);
    }

    public static PlacementImpressionDao getInstance(CommonAbsDBHelper helper) {
        if (instance == null) {
            instance = new PlacementImpressionDao(helper);
        }
        return instance;
    }

    public synchronized Map<String, PlacementImpressionInfo> queryImpressionByFormat(int format, String dayFormat, String hourFormat) {

        HashMap<String, PlacementImpressionInfo> maps = new HashMap<>();

        String sql = "SELECT * FROM " + Table.TABLE_NAME + " WHERE "
                + Table.C_FORMAT_NAME + "='" + format + "'";
        Cursor c = null;
        try {
            c = getReadableDatabase().rawQuery(sql, null);
            if (c != null && c.getCount() > 0) {
                while (c.moveToNext()) {
                    String placementId = c.getString(c.getColumnIndex(Table.C_PLACEMENT_NAME));
                    PlacementImpressionInfo placementImpressionInfo = maps.get(placementId);
                    /**Create placement impression info**/
                    if (placementImpressionInfo == null) {
                        placementImpressionInfo = new PlacementImpressionInfo();
                        placementImpressionInfo.placementId = placementId;
                        placementImpressionInfo.format = c.getInt(c.getColumnIndex(Table.C_FORMAT_NAME));
                        placementImpressionInfo.adSourceImpressionInfos = new ConcurrentHashMap<>();
                        maps.put(placementId, placementImpressionInfo);
                    }

                    PlacementImpressionInfo.AdSourceImpressionInfo adSourceImpressionInfo = new PlacementImpressionInfo.AdSourceImpressionInfo();
                    adSourceImpressionInfo.unitId = c.getString(c.getColumnIndex(Table.C_ADSOURCEID_NAME));
                    adSourceImpressionInfo.hourTimeFormat = c.getString(c.getColumnIndex(Table.C_HOURTIME_NAME));
                    adSourceImpressionInfo.dateTimeFormat = c.getString(c.getColumnIndex(Table.C_DATETIME_NAME));

                    /**handle hour show time**/
                    if (!TextUtils.equals(adSourceImpressionInfo.hourTimeFormat, hourFormat)) {
                        adSourceImpressionInfo.hourShowCount = 0;
                    } else {
                        adSourceImpressionInfo.hourShowCount = c.getInt(c.getColumnIndex(Table.C_HOUR_IMPRESSION_NAME));
                    }
                    placementImpressionInfo.hourShowCount += adSourceImpressionInfo.hourShowCount;

                    /**hand day show time**/
                    if (!TextUtils.equals(adSourceImpressionInfo.dateTimeFormat, dayFormat)) {
                        adSourceImpressionInfo.dayShowCount = 0;
                    } else {
                        adSourceImpressionInfo.dayShowCount = c.getInt(c.getColumnIndex(Table.C_DATE_IMPRESSION_NAME));
                    }
                    placementImpressionInfo.dayShowCount += adSourceImpressionInfo.dayShowCount;

                    /**the max time set to placement**/
                    adSourceImpressionInfo.showTime = c.getLong(c.getColumnIndex(Table.C_SHOW_TIME_NAME));
                    if (adSourceImpressionInfo.showTime >= placementImpressionInfo.showTime) {
                        placementImpressionInfo.showTime = adSourceImpressionInfo.showTime;
                    }

                    placementImpressionInfo.adSourceImpressionInfos.put(adSourceImpressionInfo.unitId, adSourceImpressionInfo);
                }
                c.close();
                return maps;
            }
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
        return maps;
    }

    public synchronized PlacementImpressionInfo queryPlacementImpressionInfo(String placementId, String dayFormat, String hourFormat) {
        String sql = "SELECT * FROM " + Table.TABLE_NAME + " WHERE "
                + Table.C_PLACEMENT_NAME + "='" + placementId + "'";
        Cursor c = null;
        try {
            c = getReadableDatabase().rawQuery(sql, null);
            PlacementImpressionInfo placementImpressionInfo = formatData(c, dayFormat, hourFormat);
            return placementImpressionInfo;
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

    public synchronized PlacementImpressionInfo.AdSourceImpressionInfo queryAdsourceImpressionInfo(String placementId, String adsourceId, String dayFormat, String hourFormat) {
        String sql = "SELECT * FROM " + Table.TABLE_NAME + " WHERE " + Table.C_ADSOURCEID_NAME + "='" + adsourceId + "'" + " AND "
                + Table.C_PLACEMENT_NAME + "='" + placementId + "'";
        Cursor c = null;
        try {
            c = getReadableDatabase().rawQuery(sql, null);
            PlacementImpressionInfo.AdSourceImpressionInfo adSourceImpressionInfo = formatAdSourceInfo(c, dayFormat, hourFormat);
            return adSourceImpressionInfo;
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


    public synchronized long insertOrUpdate(int format, String placementId, PlacementImpressionInfo.AdSourceImpressionInfo adSourceImpressionInfo) {
        if (getWritableDatabase() == null || adSourceImpressionInfo == null) {
            return -1;
        }
        try {
            ContentValues cv = new ContentValues();
            cv.put(Table.C_FORMAT_NAME, format);
            cv.put(Table.C_PLACEMENT_NAME, placementId);
            cv.put(Table.C_ADSOURCEID_NAME, adSourceImpressionInfo.unitId);
            cv.put(Table.C_HOURTIME_NAME, adSourceImpressionInfo.hourTimeFormat);
            cv.put(Table.C_HOUR_IMPRESSION_NAME, adSourceImpressionInfo.hourShowCount);
            cv.put(Table.C_DATETIME_NAME, adSourceImpressionInfo.dateTimeFormat);
            cv.put(Table.C_DATE_IMPRESSION_NAME, adSourceImpressionInfo.dayShowCount);
            cv.put(Table.C_SHOW_TIME_NAME, adSourceImpressionInfo.showTime);

            if (isExist(adSourceImpressionInfo.unitId)) {
                CommonLogUtil.d(TAG, "existsByTime--update");
                String where = Table.C_ADSOURCEID_NAME + " = ? ";
                return getWritableDatabase().update(Table.TABLE_NAME, cv, where, new String[]{adSourceImpressionInfo.unitId});
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

    private boolean isExist(String adsourceId) {
        String sql = "SELECT " + Table.C_ADSOURCEID_NAME + " FROM " + Table.TABLE_NAME + " WHERE " + Table.C_ADSOURCEID_NAME + "=? GROUP BY " + Table.C_ADSOURCEID_NAME;
        Cursor c = getReadableDatabase().rawQuery(sql, new String[]{adsourceId});
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


    /**
     * Parse Placement Impression Info
     *
     * @param c
     * @param dayFormat
     * @param hourFormat
     * @return
     */
    private PlacementImpressionInfo formatData(Cursor c, String dayFormat, String hourFormat) {
        if (c != null && c.getCount() > 0) {
            PlacementImpressionInfo placementImpressionInfo = new PlacementImpressionInfo();
            placementImpressionInfo.adSourceImpressionInfos = new ConcurrentHashMap<>();
            while (c.moveToNext()) {

                placementImpressionInfo.format = c.getInt(c.getColumnIndex(Table.C_FORMAT_NAME));
                placementImpressionInfo.placementId = c.getString(c.getColumnIndex(Table.C_PLACEMENT_NAME));

                PlacementImpressionInfo.AdSourceImpressionInfo adSourceImpressionInfo = new PlacementImpressionInfo.AdSourceImpressionInfo();
                adSourceImpressionInfo.unitId = c.getString(c.getColumnIndex(Table.C_ADSOURCEID_NAME));
                adSourceImpressionInfo.hourTimeFormat = c.getString(c.getColumnIndex(Table.C_HOURTIME_NAME));
                adSourceImpressionInfo.dateTimeFormat = c.getString(c.getColumnIndex(Table.C_DATETIME_NAME));

                /**handle hour show time**/
                if (!TextUtils.equals(adSourceImpressionInfo.hourTimeFormat, hourFormat)) {
                    adSourceImpressionInfo.hourShowCount = 0;
                } else {
                    adSourceImpressionInfo.hourShowCount = c.getInt(c.getColumnIndex(Table.C_HOUR_IMPRESSION_NAME));
                }
                placementImpressionInfo.hourShowCount += adSourceImpressionInfo.hourShowCount;

                /**hand day show time**/
                if (!TextUtils.equals(adSourceImpressionInfo.dateTimeFormat, dayFormat)) {
                    adSourceImpressionInfo.dayShowCount = 0;
                } else {
                    adSourceImpressionInfo.dayShowCount = c.getInt(c.getColumnIndex(Table.C_DATE_IMPRESSION_NAME));
                }
                placementImpressionInfo.dayShowCount += adSourceImpressionInfo.dayShowCount;

                /**the max time set to placement**/
                adSourceImpressionInfo.showTime = c.getLong(c.getColumnIndex(Table.C_SHOW_TIME_NAME));
                if (adSourceImpressionInfo.showTime >= placementImpressionInfo.showTime) {
                    placementImpressionInfo.showTime = adSourceImpressionInfo.showTime;
                }

                placementImpressionInfo.adSourceImpressionInfos.put(adSourceImpressionInfo.unitId, adSourceImpressionInfo);
            }
            c.close();
            return placementImpressionInfo;
        }
        return null;
    }

    /**
     * Parse AdSource Impression Info
     *
     * @param c
     * @param dayFormat
     * @param hourFormat
     * @return
     */
    private PlacementImpressionInfo.AdSourceImpressionInfo formatAdSourceInfo(Cursor c, String dayFormat, String hourFormat) {
        if (c != null && c.getCount() > 0) {
            c.moveToNext();
            PlacementImpressionInfo.AdSourceImpressionInfo adSourceImpressionInfo = new PlacementImpressionInfo.AdSourceImpressionInfo();
            adSourceImpressionInfo.unitId = c.getString(c.getColumnIndex(Table.C_ADSOURCEID_NAME));
            adSourceImpressionInfo.hourTimeFormat = c.getString(c.getColumnIndex(Table.C_HOURTIME_NAME));
            adSourceImpressionInfo.dateTimeFormat = c.getString(c.getColumnIndex(Table.C_DATETIME_NAME));

            /**handle hour show time**/
            if (!TextUtils.equals(adSourceImpressionInfo.hourTimeFormat, hourFormat)) {
                adSourceImpressionInfo.hourShowCount = 0;
            } else {
                adSourceImpressionInfo.hourShowCount = c.getInt(c.getColumnIndex(Table.C_HOUR_IMPRESSION_NAME));
            }

            /**hand day show time**/
            if (!TextUtils.equals(adSourceImpressionInfo.dateTimeFormat, dayFormat)) {
                adSourceImpressionInfo.dayShowCount = 0;
            } else {
                adSourceImpressionInfo.dayShowCount = c.getInt(c.getColumnIndex(Table.C_DATE_IMPRESSION_NAME));
            }

            /**the max time set to placement**/
            adSourceImpressionInfo.showTime = c.getLong(c.getColumnIndex(Table.C_SHOW_TIME_NAME));
            return adSourceImpressionInfo;
        }
        return null;
    }


    /**
     * clean date
     *
     * @param dayFormat
     */
    public void clean(String dayFormat) {
        synchronized (this) {
            try {
                String where = Table.C_DATETIME_NAME + "!='" + dayFormat + "'";
                if (getWritableDatabase() == null) {
                    return;
                }
                getWritableDatabase().delete(Table.TABLE_NAME, where, null);
            } catch (Exception e) {
            }
        }
    }

    static public class Table {
        public static final String TABLE_NAME = "placement_ad_impression";
        public static final String C_FORMAT_NAME = "format";
        public static final String C_PLACEMENT_NAME = "placement_id";
        public static final String C_ADSOURCEID_NAME = "adsource_id";
        public static final String C_HOURTIME_NAME = "hour_time";
        public static final String C_HOUR_IMPRESSION_NAME = "hour_imp";
        public static final String C_DATETIME_NAME = "date_time";
        public static final String C_DATE_IMPRESSION_NAME = "date_imp";
        public static final String C_SHOW_TIME_NAME = "show_time";

        public static final String TABLE_CREATE_SQL = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(" +
                C_FORMAT_NAME + " INTEGER ," +
                C_PLACEMENT_NAME + " TEXT ," +
                C_ADSOURCEID_NAME + " TEXT ," +
                C_HOURTIME_NAME + " TEXT ," +
                C_HOUR_IMPRESSION_NAME + " INTEGER ," +
                C_DATETIME_NAME + " TEXT ," +
                C_DATE_IMPRESSION_NAME + " INTEGER , " +
                C_SHOW_TIME_NAME + " INTEGER" +
                ")";
    }

}
