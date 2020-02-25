package com.anythink.core.cap;

import android.content.Context;

import com.anythink.core.common.db.CommonSDKDBHelper;
import com.anythink.core.common.db.ConfigInfoDao;
import com.anythink.core.common.entity.SDKConfigInfo;
import com.anythink.core.common.utils.CommonLogUtil;
import com.anythink.core.common.utils.task.TaskManager;
import com.anythink.core.strategy.PlaceStrategy;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Ad Cap Manager
 * Created by zhou on 2018/1/10.
 */

public class AdCapManager {
    public class CapInfo {
        private String placeId;
        private int placeCapByDay;
        private int placeCapByHour;

        public Map<String, Integer> unitGroupCapByDayList = new HashMap<String, Integer>();
        public Map<String, Integer> unitGroupCapByHourList = new HashMap<String, Integer>();

        /***
         * Daily cap
         * @param unitGroupId
         * @return
         */
        public int getUnitGroupDayCatById(String unitGroupId) {
            if (unitGroupCapByDayList.containsKey(unitGroupId)) {
                return unitGroupCapByDayList.get(unitGroupId);
            }
            return 0;
        }

        /**
         * Hour cap
         *
         * @param unitGroupId
         * @return
         */
        public int getUnitGroupHourCatById(String unitGroupId) {
            if (unitGroupCapByHourList.containsKey(unitGroupId)) {
                return unitGroupCapByHourList.get(unitGroupId);
            }
            return 0;
        }

        /**
         * Check daily cap of all UnitgroupInfo
         */
        public int getAllUnitGroupDayCap() {
            return placeCapByDay;
        }

        /**
         * Check hour cap of all UnitgroupInfo
         */
        public int getAllUnitGroupHourCap() {
            return placeCapByHour;
        }

        public String getPlaceId() {
            return placeId;
        }

    }

    public static final String TAG = AdCapManager.class.getSimpleName();
    private static AdCapManager mInstance = null;
    private Context mContext;

    private AdCapManager(Context context) {
        mContext = context;
    }


    public static AdCapManager getInstance(Context context) {
        if (mInstance == null) {
            synchronized (AdCapManager.class) {
                mInstance = new AdCapManager(context);
            }
        }
        return mInstance;
    }

    /**
     * Check placement whether out of cap
     *
     * @param placeStrategy
     * @param placementId
     * @return
     */
    public boolean isPlacementOutOfCap(PlaceStrategy placeStrategy, String placementId) {
        final String dayStr = getDayString();
        final String hourStr = getDayHourString();

        List<SDKConfigInfo> sdkConfigInfoList = ConfigInfoDao.getInstance(CommonSDKDBHelper.getInstance(mContext)).queryAllCapByPlaceId(placementId, dayStr);
        CapInfo capInfo = new CapInfo();
        capInfo.placeId = placementId;

        //Default
        capInfo.placeCapByDay = capInfo.placeCapByHour = 0;
        if (sdkConfigInfoList != null) {
            CommonLogUtil.d(TAG, "sdkConfigInfoList:" + sdkConfigInfoList.size());

            for (SDKConfigInfo sdkConfigInfo : sdkConfigInfoList) {
                String key = sdkConfigInfo.getKey();
                String update = sdkConfigInfo.getUpdatetime();
                CommonLogUtil.d(TAG, "sdkConfigInfoList-key:" + key);
                if (key != null && key.startsWith(placementId + "|||")) {
                    if (key.startsWith(placementId + "|||all|||day")) {
                        capInfo.placeCapByDay = Integer.parseInt(sdkConfigInfo.getValue());
                    } else if (key.startsWith(placementId + "|||all|||hour") && hourStr.equals(update)) {
                        //判断是当前小时的
                        capInfo.placeCapByHour = Integer.parseInt(sdkConfigInfo.getValue());
                    } else {
                        CommonLogUtil.d(TAG, "capkey:" + key);
                        CommonLogUtil.d(TAG, "capvalue:" + sdkConfigInfo.getValue());
                        String[] keylist = key.split("\\|\\|\\|");
                        if (keylist.length == 3) {
                            if ("day".equals(keylist[2])) {
                                //Daily cap
                                capInfo.unitGroupCapByDayList.put(keylist[1], Integer.parseInt(sdkConfigInfo.getValue()));
                            } else if ("hour".equals(keylist[2]) && hourStr.equals(update)) {
                                //Hour cap
                                capInfo.unitGroupCapByHourList.put(keylist[1], Integer.parseInt(sdkConfigInfo.getValue()));
                            }
                        }
                    }
                }
            }
        }

        if ((placeStrategy.getUnitCapsDayNumber() == -1 || capInfo.getAllUnitGroupDayCap() < placeStrategy.getUnitCapsDayNumber())
                && (placeStrategy.getUnitCapsHourNumber() == -1 || capInfo.getAllUnitGroupHourCap() < placeStrategy.getUnitCapsHourNumber())) {
            return false;
        }

        return true;
    }

    public boolean isUnitgroupOutOfCap(String placementId, PlaceStrategy.UnitGroupInfo unitGroupInfo) {
        final String dayStr = getDayString();
        final String hourStr = getDayHourString();

        List<SDKConfigInfo> sdkConfigInfoList = ConfigInfoDao.getInstance(CommonSDKDBHelper.getInstance(mContext)).queryAllCapByPlaceId(placementId, dayStr);
        CapInfo capInfo = new CapInfo();
        capInfo.placeId = placementId;

        capInfo.placeCapByDay = capInfo.placeCapByHour = 0;
        if (sdkConfigInfoList != null) {
            CommonLogUtil.d(TAG, "sdkConfigInfoList:" + sdkConfigInfoList.size());

            for (SDKConfigInfo sdkConfigInfo : sdkConfigInfoList) {
                String key = sdkConfigInfo.getKey();
                String update = sdkConfigInfo.getUpdatetime();
                CommonLogUtil.d(TAG, "sdkConfigInfoList-key:" + key);
                if (key != null && key.startsWith(placementId + "|||")) {
                    if (key.startsWith(placementId + "|||all|||day")) {
                        capInfo.placeCapByDay = Integer.parseInt(sdkConfigInfo.getValue());
                    } else if (key.startsWith(placementId + "|||all|||hour") && hourStr.equals(update)) {
                        capInfo.placeCapByHour = Integer.parseInt(sdkConfigInfo.getValue());
                    } else {
                        CommonLogUtil.d(TAG, "capkey:" + key);
                        CommonLogUtil.d(TAG, "capvalue:" + sdkConfigInfo.getValue());
                        String[] keylist = key.split("\\|\\|\\|");
                        if (keylist.length == 3) {
                            if ("day".equals(keylist[2])) {
                                capInfo.unitGroupCapByDayList.put(keylist[1], Integer.parseInt(sdkConfigInfo.getValue()));
                            } else if ("hour".equals(keylist[2]) && hourStr.equals(update)) {
                                capInfo.unitGroupCapByHourList.put(keylist[1], Integer.parseInt(sdkConfigInfo.getValue()));
                            }
                        }
                    }
                }
            }
        }

        int dayCap = capInfo.getUnitGroupDayCatById(unitGroupInfo.unitId);
        int hourCap = capInfo.getUnitGroupHourCatById(unitGroupInfo.unitId);
        if ((unitGroupInfo.capsByHour == -1 || hourCap < unitGroupInfo.capsByHour)
                && (unitGroupInfo.capsByDay == -1 || dayCap < unitGroupInfo.capsByDay)) {
            return false;
        }
        return true;
    }

    /***
     * Get Placement and UnitGroup Cap-Info
     * @param currPlaceId
     * @param currUnitGroupId
     * @return
     */

    public CapInfo getCapByPlaceIdAndUnitGroupId(String currPlaceId, String... currUnitGroupId) {
        final String dayStr = getDayString();

        final String hourStr = getDayHourString();

        List<SDKConfigInfo> sdkConfigInfoList = ConfigInfoDao.getInstance(CommonSDKDBHelper.getInstance(mContext)).queryAllCapByPlaceId(currPlaceId, dayStr);
        CapInfo capInfo = new CapInfo();
        capInfo.placeId = currPlaceId;
        capInfo.placeCapByDay = capInfo.placeCapByHour = 0;
        if (sdkConfigInfoList != null) {
            CommonLogUtil.d(TAG, "sdkConfigInfoList:" + sdkConfigInfoList.size());

            for (SDKConfigInfo sdkConfigInfo : sdkConfigInfoList) {
                String key = sdkConfigInfo.getKey();
                String update = sdkConfigInfo.getUpdatetime();
                CommonLogUtil.d(TAG, "sdkConfigInfoList-key:" + key);
                if (key != null && key.startsWith(currPlaceId + "|||")) {
                    if (key.startsWith(currPlaceId + "|||all|||day")) {
                        capInfo.placeCapByDay = Integer.parseInt(sdkConfigInfo.getValue());
                    } else if (key.startsWith(currPlaceId + "|||all|||hour") && hourStr.equals(update)) {
                        capInfo.placeCapByHour = Integer.parseInt(sdkConfigInfo.getValue());
                    } else {
                        CommonLogUtil.d(TAG, "capkey:" + key);
                        CommonLogUtil.d(TAG, "capvalue:" + sdkConfigInfo.getValue());
                        String[] keylist = key.split("\\|\\|\\|");
                        if (keylist.length == 3) {
                            if ("day".equals(keylist[2])) {
                                capInfo.unitGroupCapByDayList.put(keylist[1], Integer.parseInt(sdkConfigInfo.getValue()));
                            } else if ("hour".equals(keylist[2]) && hourStr.equals(update)) {
                                capInfo.unitGroupCapByHourList.put(keylist[1], Integer.parseInt(sdkConfigInfo.getValue()));
                            }
                        }
                    }
                }
            }
        }


        for (String uid : currUnitGroupId) {
            if (!capInfo.unitGroupCapByDayList.containsKey(uid)) {
                capInfo.unitGroupCapByDayList.put(uid, 0);
            }
            if (!capInfo.unitGroupCapByHourList.containsKey(uid)) {
                capInfo.unitGroupCapByHourList.put(uid, 0);
            }
        }

        return capInfo;
    }

    /***
     * Save Cap
     * @param currPlaceId
     * @param currUnitGroupId
     */
    public void saveOneCap(final String currPlaceId, final String currUnitGroupId) {


        final String dayStr = getDayString();
        final String hourStr = getDayHourString();
        TaskManager.getInstance().run_proxy(new Runnable() {
            @Override
            public void run() {
                String key_day_unit = currPlaceId + "|||" + currUnitGroupId + "|||day";
                String key_hour_unit = currPlaceId + "|||" + currUnitGroupId + "|||hour";

                ConfigInfoDao.getInstance(CommonSDKDBHelper.getInstance(mContext)).addCapTimesByPlaceId(key_day_unit, dayStr);
                ConfigInfoDao.getInstance(CommonSDKDBHelper.getInstance(mContext)).addCapTimesByPlaceId(key_hour_unit, hourStr);

                String key_day_place = currPlaceId + "|||all|||day";
                String key_hour_place = currPlaceId + "|||all|||hour";
                //allcap
                ConfigInfoDao.getInstance(CommonSDKDBHelper.getInstance(mContext)).addCapTimesByPlaceId(key_day_place, dayStr);
                ConfigInfoDao.getInstance(CommonSDKDBHelper.getInstance(mContext)).addCapTimesByPlaceId(key_hour_place, hourStr);

                //Clear the placement's cap outside of today
                ConfigInfoDao.getInstance(CommonSDKDBHelper.getInstance(mContext)).clearOldCapByPlaceAndDayTime(dayStr);
            }
        });


    }

    /**
     * Get current day
     *
     * @return format: yyyyMMdd
     */
    private static String getStringDate(String format) {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        String dateString = formatter.format(currentTime);
        return dateString;
    }


    /**
     * @return format: yyyyMMdd
     */
    private static String getDayString() {
        return getStringDate("yyyyMMdd");
    }

    /**
     * @return format: yyyyMMddHH
     */
    private static String getDayHourString() {
        return getStringDate("yyyyMMddHH");
    }
}
