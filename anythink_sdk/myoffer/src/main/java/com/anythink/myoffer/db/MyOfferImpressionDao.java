package com.anythink.myoffer.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.anythink.myoffer.entity.MyOfferImpression;

import java.util.ArrayList;
import java.util.List;

/**
 * MyOffer Impression Info
 */
public class MyOfferImpressionDao {
    private static MyOfferImpressionDao capDao;
    private Context context;

    private MyOfferImpressionDao(Context context) {
        this.context = context;
    }

    public static MyOfferImpressionDao getInstance(Context context) {
        if (capDao == null) {
            capDao = new MyOfferImpressionDao(context);
        }
        return capDao;
    }

    public synchronized MyOfferImpression queryAll(String offerId) {
        String tabWhere = "";
        tabWhere = " WHERE " + Table.OFFER_ID + " = '" + offerId + "'";
        String sql = "SELECT * FROM " + Table.TABLE_NAME + tabWhere;
        Cursor c = null;
        try {
            c = MyOfferDBHelper.getInstance(context).getReadableDatabase().rawQuery(sql, null);

            if (c != null && c.getCount() > 0) {
                c.moveToNext();
                MyOfferImpression myOfferAdImpression = parseMyOfferImpressByDB(c);
                c.close();
                return myOfferAdImpression;
            }
        } catch (Exception e) {
        } catch (OutOfMemoryError error) {
            System.gc();
        } catch (Throwable a) {

        } finally {
            if (c != null) {
                c.close();
            }
        }
        return null;
    }


    /**
     * Check MyOffer's Impression info which is out of cap
     *
     * @return
     */
    public synchronized List<MyOfferImpression> queryOutOfCap(String date) {
        String tabWhere = "";
        tabWhere = " WHERE " + Table.OFFER_CAP + " <= " + Table.OFFER_SHOW_NUM + " AND " + Table.RECORD_DATE + "='" + date + "' AND " + Table.OFFER_CAP + " != -1";
        String sql = "SELECT * FROM " + Table.TABLE_NAME + tabWhere;
        Cursor c = null;
        try {
            c = MyOfferDBHelper.getInstance(context).getReadableDatabase().rawQuery(sql, null);
            if (c != null && c.getCount() > 0) {
                List<MyOfferImpression> myOfferImpressionList = new ArrayList<>();
                while (c.moveToNext()) {
                    MyOfferImpression myOfferAdImpression = parseMyOfferImpressByDB(c);
                    myOfferImpressionList.add(myOfferAdImpression);
                }

                c.close();
                return myOfferImpressionList;
            }
        } catch (Exception e) {
        } catch (OutOfMemoryError error) {
            System.gc();
        } catch (Throwable a) {

        } finally {
            if (c != null) {
                c.close();
            }
        }
        return null;
    }

    /**
     * Delete Impression info by date
     *
     * @param date
     */
    public synchronized void deleteOutOfDateImpression(String date) {
        synchronized (this) {
            try {
                String where = Table.RECORD_DATE + " != '" + date + "'";
                if (MyOfferDBHelper.getInstance(context).getWritableDatabase() == null) {
                    return;
                }
                MyOfferDBHelper.getInstance(context).getWritableDatabase().delete(Table.TABLE_NAME, where, null);
            } catch (Exception e) {
            }
        }
    }

    public synchronized long insertOrupdateMyOfferImpression(MyOfferImpression myOfferImpression) {
        synchronized (this) {
            try {
                if (myOfferImpression == null) {
                    return 0;
                }
                if (MyOfferDBHelper.getInstance(context).getWritableDatabase() == null) {
                    return -1;
                }

                ContentValues cv = new ContentValues();
                cv.put(Table.OFFER_ID, myOfferImpression.offerId);
                cv.put(Table.OFFER_SHOW_NUM, myOfferImpression.showNum);
                cv.put(Table.OFFER_SHOW_TIME, myOfferImpression.showTime);
                cv.put(Table.RECORD_DATE, myOfferImpression.recordDate);
                cv.put(Table.OFFER_CAP, myOfferImpression.offerCap);
                cv.put(Table.OFFER_PACING, myOfferImpression.offerPacing);


                if (exists(myOfferImpression.offerId)) {
                    String where = Table.OFFER_ID + " = '" + myOfferImpression.offerId + "'";
                    return MyOfferDBHelper.getInstance(context).getWritableDatabase().update(Table.TABLE_NAME, cv, where, null);
                } else {

                    return MyOfferDBHelper.getInstance(context).getWritableDatabase().insert(Table.TABLE_NAME, null, cv);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return -1;
        }
    }

    public synchronized boolean exists(String offerId) {
        String sql = "SELECT " + Table.OFFER_ID + " FROM " + Table.TABLE_NAME + " WHERE " + Table.OFFER_ID + "='" + offerId + "'";
        Cursor c = MyOfferDBHelper.getInstance(context).getReadableDatabase().rawQuery(sql, null);
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
     * Parse DB Info
     *
     * @param c
     * @return
     */
    private MyOfferImpression parseMyOfferImpressByDB(Cursor c) {
        MyOfferImpression myOfferAdImpression = new MyOfferImpression();
        myOfferAdImpression.offerId = c.getString(c.getColumnIndex(Table.OFFER_ID));
        myOfferAdImpression.showNum = c.getInt(c.getColumnIndex(Table.OFFER_SHOW_NUM));
        myOfferAdImpression.showTime = c.getLong(c.getColumnIndex(Table.OFFER_SHOW_TIME));
        myOfferAdImpression.recordDate = c.getString(c.getColumnIndex(Table.RECORD_DATE));
        myOfferAdImpression.offerCap = c.getInt(c.getColumnIndex(Table.OFFER_CAP));
        myOfferAdImpression.offerPacing = c.getLong(c.getColumnIndex(Table.OFFER_PACING));
        return myOfferAdImpression;
    }

    public static class Table {

        public static final String TABLE_NAME = "my_offer_cap_pacing";

        public static final String OFFER_ID = "offer_id";
        public static final String OFFER_CAP = "offer_cap";
        public static final String OFFER_PACING = "offer_pacing";
        public static final String OFFER_SHOW_NUM = "show_num";
        public static final String OFFER_SHOW_TIME = "show_time";

        public static final String RECORD_DATE = "record_date";


        public static final String TABLE_CREATE_SQL = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                + OFFER_ID + " TEXT,"
                + OFFER_CAP + " INTEGER,"
                + OFFER_PACING + " INTEGER,"
                + OFFER_SHOW_NUM + " INTEGER,"
                + OFFER_SHOW_TIME + " INTEGER,"
                + RECORD_DATE + " INTEGER"
                + " )";

    }
}
