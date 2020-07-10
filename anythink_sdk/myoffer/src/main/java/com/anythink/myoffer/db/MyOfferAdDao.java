package com.anythink.myoffer.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.anythink.myoffer.entity.MyOfferAd;

import java.util.ArrayList;
import java.util.List;

/**
 * MyOffer DB
 */
public class MyOfferAdDao {
    private static MyOfferAdDao campaignDao;
    private Context context;

    private MyOfferAdDao(Context context) {
        this.context = context;
    }

    public static MyOfferAdDao getInstance(Context context) {
        if (campaignDao == null) {
            campaignDao = new MyOfferAdDao(context);
        }
        return campaignDao;
    }

    public synchronized void deleteAll() {
        try {
            if (MyOfferDBHelper.getInstance(context).getWritableDatabase() == null) {
                return;
            }
            MyOfferDBHelper.getInstance(context).getWritableDatabase().delete(Table.TABLE_NAME, null, null);
        } catch (Exception e) {
        }
    }


    public synchronized void deleteByPlacementId(String placementid) {
        synchronized (this) {
            try {
                String where = Table.PLACEMENT_ID + " = '" + placementid + "'";
                if (MyOfferDBHelper.getInstance(context).getWritableDatabase() == null) {
                    return;
                }
                MyOfferDBHelper.getInstance(context).getWritableDatabase().delete(Table.TABLE_NAME, where, null);
            } catch (Exception e) {
            }
        }
    }


    private synchronized long insertOrUpdate(SQLiteDatabase sqLiteDatabase, MyOfferAd myOfferAd, String toponPlacementId) {
        try {
            if (myOfferAd == null) {
                return 0;
            }
            if (MyOfferDBHelper.getInstance(context).getWritableDatabase() == null) {
                return -1;
            }

            ContentValues cv = new ContentValues();
            cv.put(Table.PLACEMENT_ID, toponPlacementId);
            cv.put(Table.OFFER_ID, myOfferAd.getOfferId());
            cv.put(Table.CREATIVE_ID, myOfferAd.getCreativeId());
            cv.put(Table.TITLE, myOfferAd.getTitle());
            cv.put(Table.DESC, myOfferAd.getDesc());
            cv.put(Table.ICON_URL, myOfferAd.getIconUrl());
            cv.put(Table.IMAGE_URL, myOfferAd.getMainImageUrl());
            cv.put(Table.ENDCARD_IMAGE_URL, myOfferAd.getEndCardImageUrl());
            cv.put(Table.ADCHOICE_URL, myOfferAd.getAdChoiceUrl());
            cv.put(Table.CTA, myOfferAd.getCtaText());
            cv.put(Table.VIDEO_URL, myOfferAd.getVideoUrl());
            cv.put(Table.CLICK_TYPE, myOfferAd.getClickType());
            cv.put(Table.PREVIEW_URL, myOfferAd.getPreviewUrl());
            cv.put(Table.DEEPLINK_URL, myOfferAd.getDeeplinkUrl());
            cv.put(Table.CLICK_URL, myOfferAd.getClickUrl());
            cv.put(Table.NOTICE_IMPRESSION_URL, myOfferAd.getNoticeUrl());
            cv.put(Table.VIDEO_START_TRACKING_URL, myOfferAd.getVideoStartTrackUrl());
            cv.put(Table.VIDEO_PROGRESS_25_TRACKING_URL, myOfferAd.getVideoProgress25TrackUrl());
            cv.put(Table.VIDEO_PROGRESS_50_TRACKING_URL, myOfferAd.getVideoProgress50TrackUrl());
            cv.put(Table.VIDEO_PROGRESS_75_TRACKING_URL, myOfferAd.getVideoProgress75TrackUrl());
            cv.put(Table.VIDEO_FINISH_TRACKING_URL, myOfferAd.getVideoFinishTrackUrl());
            cv.put(Table.ENDCARD_SHOW_TRACKING_URL, myOfferAd.getEndCardShowTrackUrl());
            cv.put(Table.ENDCARD_CLOSE_TRACKING_URL, myOfferAd.getEndCardCloseTrackUrl());
            cv.put(Table.IMPRESSION_TRACKING_URL, myOfferAd.getImpressionTrackUrl());
            cv.put(Table.CLICK_TRACKING_URL, myOfferAd.getClickTrackUrl());
            cv.put(Table.PKG_NAME, myOfferAd.getPkgName());
            cv.put(Table.OFFER_CAP, myOfferAd.getOfferCap());
            cv.put(Table.OFFER_PACING, myOfferAd.getOfferPacing());
            cv.put(Table.OFFER_TYPE, myOfferAd.getOfferType());
            cv.put(Table.UPDATE_TIME, myOfferAd.getUpdateTime());
            cv.put(Table.CLICK_MODE, myOfferAd.getClickMode());


            if (exists(myOfferAd.getOfferId(), toponPlacementId)) {
                String where = Table.OFFER_ID + " = '" + myOfferAd.getOfferId() + "' AND " + Table.PLACEMENT_ID + " = '" + toponPlacementId + "'";

                return sqLiteDatabase.update(Table.TABLE_NAME, cv, where, null);
            } else {
                return sqLiteDatabase.insert(Table.TABLE_NAME, null, cv);
            }
        } catch (Exception e) {
        }
        return -1;
    }

    /**
     * @param list
     * @param toponPlacementId
     */
    public synchronized void insertOrUpdate(List<MyOfferAd> list, String toponPlacementId) {

        synchronized (this) {
            if (list == null || list.size() == 0) {
                return;
            }

            SQLiteDatabase sqLiteDatabase = MyOfferDBHelper.getInstance(context).getWritableDatabase();
            try {
                sqLiteDatabase.beginTransaction();
                for (MyOfferAd myOfferAd : list) {
                    insertOrUpdate(sqLiteDatabase, myOfferAd, toponPlacementId);
                }

                sqLiteDatabase.setTransactionSuccessful();
            } catch (Exception e) {

            } finally {
                sqLiteDatabase.endTransaction();
            }
        }

    }

    public synchronized boolean exists(String offerId, String topOnPlacementid) {
        String sql = "SELECT " + Table.OFFER_ID + " FROM " + Table.TABLE_NAME + " WHERE " + Table.OFFER_ID + "='" + offerId + "'"
                + " AND " + Table.PLACEMENT_ID + " = '" + topOnPlacementid + "'";
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
     * @param placementid
     * @param offerId
     * @return
     */
    public synchronized MyOfferAd queryOfferById(String placementid, String offerId) {
        String tabWhere = "";
        tabWhere = " WHERE " + Table.PLACEMENT_ID + " = '" + placementid + "'" + " AND " + Table.OFFER_ID + " = '" + offerId + "'";
        String sql = "SELECT * FROM " + Table.TABLE_NAME + tabWhere;
        Cursor c = null;
        try {
            c = MyOfferDBHelper.getInstance(context).getReadableDatabase().rawQuery(sql, null);

            if (c != null && c.getCount() > 0) {
                c.moveToNext();
                MyOfferAd myOfferAd = parseDBMyOfferAd(c);

                c.close();
                return myOfferAd;
            }
        } catch (Exception e) {
            // TODO: handle exception
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
     * @return
     */
    public synchronized List<MyOfferAd> queryAllGroupByOfferId() {
        String tabWhere = "";
        tabWhere = " group by " + Table.OFFER_ID;
        String sql = "SELECT * FROM " + Table.TABLE_NAME + tabWhere;
        Cursor c = null;
        try {
            c = MyOfferDBHelper.getInstance(context).getReadableDatabase().rawQuery(sql, null);

            if (c != null && c.getCount() > 0) {
                List<MyOfferAd> myOfferAdList = new ArrayList<>();
                while (c.moveToNext()) {
                    MyOfferAd myOfferAd = parseDBMyOfferAd(c);
                    myOfferAdList.add(myOfferAd);
                }


                c.close();
                return myOfferAdList;
            }
        } catch (Exception e) {
            // TODO: handle exception
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
     * @return
     */
    public synchronized List<MyOfferAd> queryAllGroupByOfferIdByPlacementId(String placementId) {
        String tabWhere = "";
        tabWhere = " where " + Table.PLACEMENT_ID + "='" + placementId + "'";
        String sql = "SELECT * FROM " + Table.TABLE_NAME + tabWhere;
        Cursor c = null;
        try {
            c = MyOfferDBHelper.getInstance(context).getReadableDatabase().rawQuery(sql, null);

            if (c != null && c.getCount() > 0) {
                List<MyOfferAd> myOfferAdList = new ArrayList<>();
                while (c.moveToNext()) {
                    MyOfferAd myOfferAd = parseDBMyOfferAd(c);
                    myOfferAdList.add(myOfferAd);
                }


                c.close();
                return myOfferAdList;
            }
        } catch (Exception e) {
            // TODO: handle exception
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

    private MyOfferAd parseDBMyOfferAd(Cursor c) {
        MyOfferAd myOfferAd = new MyOfferAd();
        myOfferAd.setOfferId(c.getString(c.getColumnIndex(Table.OFFER_ID)));
        myOfferAd.setCreativeId(c.getString(c.getColumnIndex(Table.CREATIVE_ID)));
        myOfferAd.setTitle(c.getString(c.getColumnIndex(Table.TITLE)));
        myOfferAd.setDesc(c.getString(c.getColumnIndex(Table.DESC)));
        myOfferAd.setIconUrl(c.getString(c.getColumnIndex(Table.ICON_URL)));
        myOfferAd.setMainImageUrl(c.getString(c.getColumnIndex(Table.IMAGE_URL)));
        myOfferAd.setEndCardImageUrl(c.getString(c.getColumnIndex(Table.ENDCARD_IMAGE_URL)));
        myOfferAd.setAdChoiceUrl(c.getString(c.getColumnIndex(Table.ADCHOICE_URL)));
        myOfferAd.setCtaText(c.getString(c.getColumnIndex(Table.CTA)));
        myOfferAd.setVideoUrl(c.getString(c.getColumnIndex(Table.VIDEO_URL)));
        myOfferAd.setClickType(c.getInt(c.getColumnIndex(Table.CLICK_TYPE)));
        myOfferAd.setPreviewUrl(c.getString(c.getColumnIndex(Table.PREVIEW_URL)));
        myOfferAd.setDeeplinkUrl(c.getString(c.getColumnIndex(Table.DEEPLINK_URL)));
        myOfferAd.setClickUrl(c.getString(c.getColumnIndex(Table.CLICK_URL)));
        myOfferAd.setNoticeUrl(c.getString(c.getColumnIndex(Table.NOTICE_IMPRESSION_URL)));
        myOfferAd.setPkgName(c.getString(c.getColumnIndex(Table.PKG_NAME)));

        myOfferAd.setVideoStartTrackUrl(c.getString(c.getColumnIndex(Table.VIDEO_START_TRACKING_URL)));
        myOfferAd.setVideoProgress25TrackUrl(c.getString(c.getColumnIndex(Table.VIDEO_PROGRESS_25_TRACKING_URL)));
        myOfferAd.setVideoProgress50TrackUrl(c.getString(c.getColumnIndex(Table.VIDEO_PROGRESS_50_TRACKING_URL)));
        myOfferAd.setVideoProgress75TrackUrl(c.getString(c.getColumnIndex(Table.VIDEO_PROGRESS_75_TRACKING_URL)));
        myOfferAd.setVideoFinishTrackUrl(c.getString(c.getColumnIndex(Table.VIDEO_FINISH_TRACKING_URL)));
        myOfferAd.setEndCardShowTrackUrl(c.getString(c.getColumnIndex(Table.ENDCARD_SHOW_TRACKING_URL)));
        myOfferAd.setEndCardCloseTrackUrl(c.getString(c.getColumnIndex(Table.ENDCARD_CLOSE_TRACKING_URL)));

        myOfferAd.setImpressionTrackUrl(c.getString(c.getColumnIndex(Table.IMPRESSION_TRACKING_URL)));
        myOfferAd.setClickTrackUrl(c.getString(c.getColumnIndex(Table.CLICK_TRACKING_URL)));
        myOfferAd.setUpdateTime(c.getLong(c.getColumnIndex(Table.UPDATE_TIME)));

        myOfferAd.setOfferCap(c.getInt(c.getColumnIndex(Table.OFFER_CAP)));
        myOfferAd.setOfferPacing(c.getLong(c.getColumnIndex(Table.OFFER_PACING)));
        myOfferAd.setOfferType(c.getInt(c.getColumnIndex(Table.OFFER_TYPE)));
        myOfferAd.setClickMode(c.getInt(c.getColumnIndex(Table.CLICK_MODE)));
        return myOfferAd;
    }


    public static class Table {

        public static final String TABLE_NAME = "my_offer_info";

        public static final String PLACEMENT_ID = "topon_pl_id";
        public static final String OFFER_ID = "offer_id";
        public static final String CREATIVE_ID = "creative_id";
        public static final String TITLE = "title";
        public static final String DESC = "desc";
        public static final String ICON_URL = "icon_url";
        public static final String IMAGE_URL = "image_url";
        public static final String ENDCARD_IMAGE_URL = "endcard_image_url";
        public static final String ADCHOICE_URL = "adchoice_url";
        public static final String CTA = "cta";
        public static final String VIDEO_URL = "video_url";
        public static final String CLICK_TYPE = "click_type";
        public static final String PREVIEW_URL = "preview_url";
        public static final String DEEPLINK_URL = "deeplink_url";
        public static final String CLICK_URL = "click_url";
        public static final String NOTICE_IMPRESSION_URL = "notice_url";

        public static final String VIDEO_START_TRACKING_URL = "video_start_tk_url";
        public static final String VIDEO_PROGRESS_25_TRACKING_URL = "video_25_tk_url";
        public static final String VIDEO_PROGRESS_50_TRACKING_URL = "video_50_tk_url";
        public static final String VIDEO_PROGRESS_75_TRACKING_URL = "video_75_tk_url";
        public static final String VIDEO_FINISH_TRACKING_URL = "video_end_tk_url";
        public static final String ENDCARD_SHOW_TRACKING_URL = "endcard_show_tk_url";
        public static final String ENDCARD_CLOSE_TRACKING_URL = "endcard_close_tk_url";
        public static final String IMPRESSION_TRACKING_URL = "impression_tk_url";
        public static final String CLICK_TRACKING_URL = "click_tk_url";

        public static final String PKG_NAME = "pkg";
        public static final String OFFER_CAP = "cap";
        public static final String OFFER_PACING = "pacing";

        public static final String OFFER_TYPE = "offer_type";

        public static final String UPDATE_TIME = "update_time";

        public static final String CLICK_MODE = "click_mode";


        public static final String TABLE_CREATE_SQL = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                + PLACEMENT_ID + " TEXT,"
                + OFFER_ID + " TEXT,"
                + CREATIVE_ID + " TEXT,"
                + TITLE + " TEXT,"
                + DESC + " TEXT,"
                + ICON_URL + " TEXT,"
                + IMAGE_URL + " TEXT,"
                + ENDCARD_IMAGE_URL + " TEXT,"
                + ADCHOICE_URL + " TEXT,"
                + CTA + " TEXT,"
                + VIDEO_URL + " TEXT,"
                + CLICK_TYPE + " INTEGER,"
                + PREVIEW_URL + " TEXT,"
                + DEEPLINK_URL + " TEXT,"
                + CLICK_URL + " TEXT,"
                + NOTICE_IMPRESSION_URL + " TEXT,"
                + VIDEO_START_TRACKING_URL + " TEXT,"
                + VIDEO_PROGRESS_25_TRACKING_URL + " TEXT,"
                + VIDEO_PROGRESS_50_TRACKING_URL + " TEXT,"
                + VIDEO_PROGRESS_75_TRACKING_URL + " TEXT,"
                + VIDEO_FINISH_TRACKING_URL + " TEXT,"
                + ENDCARD_SHOW_TRACKING_URL + " TEXT,"
                + ENDCARD_CLOSE_TRACKING_URL + " TEXT,"
                + IMPRESSION_TRACKING_URL + " TEXT,"
                + CLICK_TRACKING_URL + " TEXT,"
                + PKG_NAME + " TEXT, "
                + OFFER_CAP + " INTEGER, "
                + OFFER_PACING + " INTEGER, "
                + OFFER_TYPE + " INTERGR, "
                + UPDATE_TIME + " INTEGER"
                + " )";

        public static final String UPGRADE_TO_2_SQL = "alter table " + Table.TABLE_NAME + " add column " + MyOfferAdDao.Table.CLICK_MODE + " INTEGER";

    }
}