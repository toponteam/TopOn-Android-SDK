package com.anythink.core.cap;

import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.common.db.CommonSDKDBHelper;
import com.anythink.core.common.db.PlacementImpressionDao;
import com.anythink.core.common.entity.PlacementImpressionInfo;
import com.anythink.core.common.utils.task.TaskManager;
import com.anythink.core.strategy.PlaceStrategy;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class AdCapV2Manager {

    private static AdCapV2Manager sIntance;

    PlacementImpressionDao dbDao;

    SimpleDateFormat mDateFormat;
    SimpleDateFormat mHourFormat;

    Context mContext;

    public static AdCapV2Manager getInstance(Context context) {
        if (sIntance == null) {
            sIntance = new AdCapV2Manager(context);
        }
        return sIntance;
    }

    private AdCapV2Manager(Context context) {
        dbDao = PlacementImpressionDao.getInstance(CommonSDKDBHelper.getInstance(context));
        mContext = context;
        mDateFormat = new SimpleDateFormat("yyyyMMdd");
        mHourFormat = new SimpleDateFormat("yyyyMMddHH");
    }

    /**
     * Clean up useless data
     */
    public void cleanUseLessData() {
        //Clean up non-today data
        TaskManager.getInstance().run_proxy(new Runnable() {
            @Override
            public void run() {
                dbDao.clean(mDateFormat.format(new Date(System.currentTimeMillis())));
            }
        });
    }

    /**
     * Check placement whether out of cap
     *
     * @param placeStrategy
     * @param placementId
     * @return
     */
    public boolean isPlacementOutOfCap(PlaceStrategy placeStrategy, String placementId) {

        if (placeStrategy.getUnitCapsDayNumber() == -1
                && placeStrategy.getUnitCapsHourNumber() == -1) {
            return false;
        }

        long currentTime = System.currentTimeMillis();
        String dateFormat = mDateFormat.format(new Date(currentTime));
        String hourFormat = mHourFormat.format(new Date(currentTime));

        PlacementImpressionInfo placementImpressionInfo = dbDao.queryPlacementImpressionInfo(placementId, dateFormat, hourFormat);
        int currentDayShowCount = placementImpressionInfo != null ? placementImpressionInfo.dayShowCount : 0;
        int currentHourShowCount = placementImpressionInfo != null ? placementImpressionInfo.hourShowCount : 0;

        if ((placeStrategy.getUnitCapsDayNumber() == -1 || currentDayShowCount < placeStrategy.getUnitCapsDayNumber())
                && (placeStrategy.getUnitCapsHourNumber() == -1 || currentHourShowCount < placeStrategy.getUnitCapsHourNumber())) {
            return false;
        }
        return true;
    }

    /**
     * check unitgroup show time
     *
     * @param placementId
     * @param unitGroupInfo
     * @return
     */
    public boolean isUnitgroupOutOfCap(String placementId, PlaceStrategy.UnitGroupInfo unitGroupInfo) {
        
        if (unitGroupInfo.capsByHour == -1 && unitGroupInfo.capsByDay == -1) {
            return false;
        }

        long currentTime = System.currentTimeMillis();
        String dateFormat = mDateFormat.format(new Date(currentTime));
        String hourFormat = mHourFormat.format(new Date(currentTime));

        PlacementImpressionInfo.AdSourceImpressionInfo adSourceImpressionInfo = dbDao.queryAdsourceImpressionInfo(placementId, unitGroupInfo.unitId, dateFormat, hourFormat);



        if (adSourceImpressionInfo == null) {
            adSourceImpressionInfo = new PlacementImpressionInfo.AdSourceImpressionInfo();
        }

        if ((unitGroupInfo.capsByHour == -1 || adSourceImpressionInfo.hourShowCount < unitGroupInfo.capsByHour)
                && (unitGroupInfo.capsByDay == -1 || adSourceImpressionInfo.dayShowCount < unitGroupInfo.capsByDay)) {
            return false;
        }

        return true;
    }


    /**
     * Get placement show time
     * @param placementId
     * @return
     */
    public PlacementImpressionInfo getPlacementImpressionInfo(String placementId) {
        long currentTime = System.currentTimeMillis();
        String dateFormat = mDateFormat.format(new Date(currentTime));
        String hourFormat = mHourFormat.format(new Date(currentTime));

        PlacementImpressionInfo placementImpressionInfo = dbDao.queryPlacementImpressionInfo(placementId, dateFormat, hourFormat);
        return placementImpressionInfo;
    }


    /**
     *  Get adsource show time
     * @param placementId
     * @param adsourceId
     * @return
     */
    public PlacementImpressionInfo.AdSourceImpressionInfo getUnitGroupImpressionInfo(String placementId, String adsourceId) {
        long currentTime = System.currentTimeMillis();
        String dateFormat = mDateFormat.format(new Date(currentTime));
        String hourFormat = mHourFormat.format(new Date(currentTime));

        PlacementImpressionInfo.AdSourceImpressionInfo adsourceImpressionInfo = dbDao.queryAdsourceImpressionInfo(placementId, adsourceId, dateFormat, hourFormat);
        return adsourceImpressionInfo;
    }

    /**
     * Get format show time
     * @param format
     * @return
     */
    public Map<String, PlacementImpressionInfo> getFormatShowTime(int format) {
        long currentTime = System.currentTimeMillis();
        String dateFormat = mDateFormat.format(new Date(currentTime));
        String hourFormat = mHourFormat.format(new Date(currentTime));
        return dbDao.queryImpressionByFormat(format, dateFormat, hourFormat);
    }


    public void saveOneCap(final String formatString, final String placementId, final String adsourceId) {

        final long currentTime = System.currentTimeMillis();
        final String dateFormat = mDateFormat.format(new Date(currentTime));
        final String hourFormat = mHourFormat.format(new Date(currentTime));

        final int formatType = Integer.parseInt(formatString);

        PlacementImpressionInfo.AdSourceImpressionInfo adSourceImpressionInfo = getUnitGroupImpressionInfo(placementId, adsourceId);


        if (adSourceImpressionInfo == null) {
            adSourceImpressionInfo = new PlacementImpressionInfo.AdSourceImpressionInfo();
            adSourceImpressionInfo.unitId = adsourceId;
        }

        if (!TextUtils.equals(dateFormat, adSourceImpressionInfo.dateTimeFormat)) {
            adSourceImpressionInfo.dayShowCount = 1;
            adSourceImpressionInfo.dateTimeFormat = dateFormat;
        } else {
            adSourceImpressionInfo.dayShowCount += 1;
        }

        if (!TextUtils.equals(hourFormat, adSourceImpressionInfo.hourTimeFormat)) {
            adSourceImpressionInfo.hourShowCount = 1;
            adSourceImpressionInfo.hourTimeFormat = hourFormat;
        } else {
            adSourceImpressionInfo.hourShowCount += 1;
        }

        adSourceImpressionInfo.showTime = currentTime;

        dbDao.insertOrUpdate(formatType, placementId, adSourceImpressionInfo);

    }
}
