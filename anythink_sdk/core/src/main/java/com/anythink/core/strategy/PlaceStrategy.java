/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.strategy;

import android.text.TextUtils;

import com.anythink.core.api.ATCustomRuleKeys;
import com.anythink.core.api.ATRewardInfo;
import com.anythink.core.common.HeadBiddingKey;
import com.anythink.core.common.MyOfferAPIProxy;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.entity.BaseAdRequestInfo;
import com.anythink.core.common.entity.OwnBaseAdSetting;
import com.anythink.core.common.entity.MyOfferAd;
import com.anythink.core.common.entity.MyOfferSetting;
import com.anythink.core.common.entity.TemplateStrategy;
import com.anythink.core.common.net.PlaceStrategyLoader;
import com.anythink.core.common.utils.CommonLogUtil;
import com.anythink.core.common.utils.CommonUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/***
 * @author Z
 * 2017/12/29.
 */

public class PlaceStrategy {
    private final String TAG = "Placement";
    /***
     * Cache Time
     */
    private long ps_ct;

    /***
     * Request Timeout
     */
    private long ps_ct_out;

    /***
     * Cache Setting switch
     */
    private int pucs;

    /***
     * Placement status
     * 1:open, 0:close
     */
    private int adDeliverySw;

    /**
     * Request mediation number
     */
    private int requestUnitGroupNumber;

    /**
     * Max Daily cap
     */
    private long unitCapsDayNumber;

    /**
     * Max Hourly cap
     */
    private long unitCapsHourNumber;

    /***
     * Request Pacing
     */
    private long unitPacing;

    private int wifiAutoSw;

    private int showType;


    /***
     * refresh status，0:close，1:open
     */
    private int refreshr;

    /**
     * Group id
     */
    private int groupId;

    /**
     * Normal UnitGroup JSON String
     */
    private String normalUnitGroupListStr;

    /**
     * Online UnitGroup JSON String
     */
    private String onlineUnitGroupListStr;

    /**
     * Headbidding UnitGroup JSON String
     */
    private String s2sheadbiddingUnitGroupListStr;
    private String c2sHeadbiddingUnitGroupListStr;
    private String fbInHouseHeadbiddingUnitGroupListStr;

    private List<UnitGroupInfo> mNormalUnitGroupList;
    private List<UnitGroupInfo> mHBUnitGroupList;

//    /**
//     */
//    private List<UnitGroupInfo> unitGroupList;
//
//    /**
//     */
//    private List<UnitGroupInfo> headbiddingUnitGroupList;

//    /**
//     * Current Request UnitGroupInfo List
//     */
//    private List<UnitGroupInfo> sortByEcpmUnitGroupList;

    /***
     * Ad Type
     */
    private int format;

    private int autoRefresh;
    private long autoRefreshTime;

    private long longOverLoadTime; //Long-Timeout
    private long upStatusOverTime; //upstatus avail time
    private int autoRequestUnitgroupAd; //Auto Request Status. 1:Yes, 2:No
    private String asid; //Service Asid

    private TemplateStrategy templateStrategy; //Extra Setting, just for NativeSplash

    /**
     * 4.5.0
     *
     * @return
     */
    private int tracfficGroupId; //AB Test Id
    private String settingId; //Service SettingId
    private int useDefaultMyOffer; //Default MyOffer Switch, 1:true, 0:false

    private int isPreLoadOfferRes; //Pre-Load MyOffer Resource，1:Yes，0:No
    private String myOfferTkMap; //Tracking Map


    private Map<String, Object> sdkCustomMap; //CustomMap for loading

    private Map<String, ATRewardInfo> scenarioRewardMap;
    private ATRewardInfo placementRewardInfo;
    private String currency;
    private String country;

    private long hbStartTime;
    private long hbBidTimeout;

    private String hbRequestUrl;

    /**
     * v5.6.2
     */
    private long loadFailWaitTime;
    private int loadCap;
    private long loadCapInterval;
    private int cachedOffersNum;

    /**
     * 5.6.6
     **/
    private List<MyOfferAd> myOfferAdList;
    private MyOfferSetting myOfferSetting;

    /**
     * v5.7.5
     *
     * @return
     */
    private long fbInHouseTimeout;

    /**
     * v5.7.9
     * @return
     */
    private double accountExchageRate;
    private String accountCurrency;

    public double getAccountExchageRate() {
        return accountExchageRate;
    }

    public void setAccountExchageRate(double exchageRate) {
        this.accountExchageRate = exchageRate;
    }

    public String getAccountCurrency() {
        return accountCurrency;
    }

    public void setAccountCurrency(String accountCurrency) {
        this.accountCurrency = accountCurrency;
    }

    public long getFbInHouseTimeout() {
        return fbInHouseTimeout;
    }

    public void setFbInHouseTimeout(long fbInHouseTimeout) {
        this.fbInHouseTimeout = fbInHouseTimeout;
    }

    public String getFbInHouseHeadbiddingUnitGroupListStr() {
        return fbInHouseHeadbiddingUnitGroupListStr;
    }

    public void setFbInHouseHeadbiddingUnitGroupListStr(String fbInHouseHeadbiddingUnitGroupListStr) {
        this.fbInHouseHeadbiddingUnitGroupListStr = fbInHouseHeadbiddingUnitGroupListStr;
    }

    /**
     * v5.6.8
     */
    private String impressionRevenueForMonitoringPlatformString;

    public String getImpressionRevenueForMonitoringPlatformString() {
        return impressionRevenueForMonitoringPlatformString;
    }

    public void setImpressionRevenueForMonitoringPlatformString(String impressionRevenueForMonitoringPlatformString) {
        this.impressionRevenueForMonitoringPlatformString = impressionRevenueForMonitoringPlatformString;
    }

    /**
     * v5.7.0
     *
     * @param myOfferSetting
     */
    private String adxUnitGroupListStr;
    private String adxAdSettingStr;

    public String getAdxAdSettingStr() {
        return adxAdSettingStr;
    }

    public void setAdxAdSettingStr(String adxAdSettingStr) {
        this.adxAdSettingStr = adxAdSettingStr;
    }

    public String getAdxUnitGroupListStr() {
        return adxUnitGroupListStr;
    }

    public void setAdxUnitGroupListStr(String adxUnitGroupListStr) {
        this.adxUnitGroupListStr = adxUnitGroupListStr;
    }

    public void setMyOfferSetting(MyOfferSetting myOfferSetting) {
        this.myOfferSetting = myOfferSetting;
    }

    public MyOfferSetting getMyOfferSetting() {
        return this.myOfferSetting;
    }

    public List<MyOfferAd> getMyOfferAdList() {
        return myOfferAdList;
    }

    public void setMyOfferAdList(List<MyOfferAd> myOfferAdList) {
        this.myOfferAdList = myOfferAdList;
    }

    public String getHbRequestUrl() {
        return hbRequestUrl;
    }

    public void setHbRequestUrl(String hbRequestUrl) {
        this.hbRequestUrl = hbRequestUrl;
    }

    public List<UnitGroupInfo> getNormalUnitGroupList() {
        return mNormalUnitGroupList;
    }

    public void setNormalUnitGroupList(List<UnitGroupInfo> normalUnitGroupList) {
        mNormalUnitGroupList = normalUnitGroupList;
    }

    public List<UnitGroupInfo> getHBGroupList() {
        return mHBUnitGroupList;
    }

    public void setHeadBiddingList(List<UnitGroupInfo> hbUnitGroupList) {
        mHBUnitGroupList = hbUnitGroupList;
    }

    public long getHbWaitingToRequestTime() {
        return hbStartTime;
    }

    public void setHbWaitingToRequestTime(long hbStartTime) {
        this.hbStartTime = hbStartTime;
    }

    public long getHbBidTimeout() {
        return hbBidTimeout;
    }

    public void setHbBidTimeout(long hbBidTimeout) {
        this.hbBidTimeout = hbBidTimeout;
    }

    public boolean containHBUnitGroupInfo(String adsourceId) {
        if (mHBUnitGroupList == null) {
            return false;
        }
        for (UnitGroupInfo unitGroupInfo : mHBUnitGroupList) {
            if (TextUtils.equals(adsourceId, unitGroupInfo.unitId)) {
                return true;
            }
        }
        return false;
    }

    public Map<String, ATRewardInfo> getScenarioRewardMap() {
        return scenarioRewardMap;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String cc) {
        this.country = cc;
    }

    public void setScenarioRewardMap(Map<String, ATRewardInfo> scenarioRewardMap) {
        this.scenarioRewardMap = scenarioRewardMap;
    }

    public ATRewardInfo getPlacementRewardInfo() {
        return placementRewardInfo;
    }

    public void setPlacementRewardInfo(ATRewardInfo placementRewardInfo) {
        this.placementRewardInfo = placementRewardInfo;
    }

    public Map<String, Object> getSdkCustomMap() {
        return sdkCustomMap;
    }

    public void setSdkCustomMap(Map<String, Object> sdkCustomMap) {
        this.sdkCustomMap = sdkCustomMap;
    }

    public int getIsPreLoadOfferRes() {
        return isPreLoadOfferRes;
    }

    public void setIsPreLoadOfferRes(int isPreLoadOfferRes) {
        this.isPreLoadOfferRes = isPreLoadOfferRes;
    }

    public int getTracfficGroupId() {
        return tracfficGroupId;
    }

    public void setTracfficGroupId(int tracfficGroupId) {
        this.tracfficGroupId = tracfficGroupId;
    }

    public String getSettingId() {
        return settingId;
    }

    public void setSettingId(String settingId) {
        this.settingId = settingId;
    }

    public int getUseDefaultMyOffer() {
        return useDefaultMyOffer;
    }

    public void setUseDefaultMyOffer(int useDefaultMyOffer) {
        this.useDefaultMyOffer = useDefaultMyOffer;
    }


    public long getLongOverLoadTime() {
        return longOverLoadTime;
    }

    public void setLongOverLoadTime(long longOverLoadTime) {
        this.longOverLoadTime = longOverLoadTime;
    }

    public long getUpStatusOverTime() {
        return upStatusOverTime;
    }

    public void setUpStatusOverTime(long upStatusOverTime) {
        this.upStatusOverTime = upStatusOverTime;
    }

    public int getAutoRequestUnitgroupAd() {
        return autoRequestUnitgroupAd;
    }

    public void setAutoRequestUnitgroupAd(int autoRequestUnitgroupAd) {
        this.autoRequestUnitgroupAd = autoRequestUnitgroupAd;
    }

    public String getAsid() {
        return asid;
    }

    public void setAsid(String asid) {
        this.asid = asid;
    }

    public int getAutoRefresh() {
        return autoRefresh;
    }

    public void setAutoRefresh(int autoRefresh) {
        this.autoRefresh = autoRefresh;
    }

    public long getAutoRefreshTime() {
        return autoRefreshTime;
    }

    public void setAutoRefreshTime(long autoRefreshTime) {
        this.autoRefreshTime = autoRefreshTime;
    }

    public int getFormat() {
        return format;
    }

    public void setFormat(int pFormat) {
        format = pFormat;
    }


    public void setPsCacheTime(long ps_ct) {
        this.ps_ct = ps_ct;
    }

    public long getPsUpdateOutTime() {
        return ps_ct_out;
    }

    public void setPsUpdateOutTime(long ps_ct_out) {
        this.ps_ct_out = ps_ct_out;
    }

    public int getPucs() {
        return pucs;
    }

    public void setPucs(int pucs) {
        this.pucs = pucs;
    }

    public int getAdDeliverySw() {
        return adDeliverySw;
    }

    public void setAdDeliverySw(int adDeliverySw) {
        this.adDeliverySw = adDeliverySw;
    }

    public int getRequestUnitGroupNumber() {
        return this.requestUnitGroupNumber;
    }

    public void setRequestUnitGroupNumber(int requestUnitGroupNumber) {
        this.requestUnitGroupNumber = requestUnitGroupNumber;
    }

    public long getUnitCapsDayNumber() {
        return unitCapsDayNumber;
    }

    public void setUnitCapsDayNumber(long unitCapsDayNumber) {
        this.unitCapsDayNumber = unitCapsDayNumber;
    }

    public long getUnitCapsHourNumber() {
        return unitCapsHourNumber;
    }

    public void setUnitCapsHourNumber(long unitCapsHourNumber) {
        this.unitCapsHourNumber = unitCapsHourNumber;
    }

    public long getUnitPacing() {
        return unitPacing;
    }

    public void setUnitPacing(long unitPacing) {
        this.unitPacing = unitPacing;
    }

    public int getWifiAutoSw() {
        return wifiAutoSw;
    }

    public void setWifiAutoSw(int wifiAutoSw) {
        this.wifiAutoSw = wifiAutoSw;
    }


    public int getShowType() {
        return showType;
    }

    public void setShowType(int showType) {
        this.showType = showType;
    }


    public int getRefreshr() {
        return refreshr;
    }

    public void setRefreshr(int refreshr) {
        this.refreshr = refreshr;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

//    public List<UnitGroupInfo> getUnitGroupList() {
//        return unitGroupList;
//    }

//    public void setUnitGroupList(List<UnitGroupInfo> unitGroupList) {
//        this.unitGroupList = unitGroupList;
//    }

//    public List<UnitGroupInfo> getHeadbiddingUnitGroupList() {
//        return headbiddingUnitGroupList;
//    }
//
//    public void setHeadbiddingUnitGroupList(List<UnitGroupInfo> headbiddingUnitGroupList) {
//        this.headbiddingUnitGroupList = headbiddingUnitGroupList;
//    }

    public TemplateStrategy getTemplateStrategy() {
        return templateStrategy;
    }

    public void setTemplateStrategy(TemplateStrategy templateStrategy) {
        this.templateStrategy = templateStrategy;
    }

    public String getNormalUnitGroupListStr() {
        return normalUnitGroupListStr;
    }

    public void setNormalUnitGroupListStr(String normalUnitGroupListStr) {
        this.normalUnitGroupListStr = normalUnitGroupListStr;
    }

    public String getOnlineUnitGroupListStr() {
        return onlineUnitGroupListStr;
    }

    public void setOnlineUnitGroupListStr(String onlineUnitGroupListStr) {
        this.onlineUnitGroupListStr = onlineUnitGroupListStr;
    }

    public String getS2SHeadbiddingUnitGroupListStr() {
        return s2sheadbiddingUnitGroupListStr;
    }

    public void setS2SHeadbiddingUnitGroupListStr(String s2sheadbiddingUnitGroupListStr) {
        this.s2sheadbiddingUnitGroupListStr = s2sheadbiddingUnitGroupListStr;
    }

    public String getC2SHeadbiddingUnitGroupListStr() {
        return c2sHeadbiddingUnitGroupListStr;
    }

    public void setC2SHeadbiddingUnitGroupListStr(String c2sHeadbiddingUnitGroupListStr) {
        this.c2sHeadbiddingUnitGroupListStr = c2sHeadbiddingUnitGroupListStr;
    }

    //    public List<UnitGroupInfo> getSortByEcpmUnitGroupList() {
//        return sortByEcpmUnitGroupList;
//    }

//    public void setSortByEcpmUnitGroupList(List<UnitGroupInfo> sortByEcpmUnitGroupList) {
//        this.sortByEcpmUnitGroupList = sortByEcpmUnitGroupList;
//    }

    /***
     * update time
     */
    private long updateTime;

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public long getLoadFailWaitTime() {
        return loadFailWaitTime;
    }

    public void setLoadFailWaitTime(long loadFailWaitTime) {
        this.loadFailWaitTime = loadFailWaitTime;
    }

    public int getLoadCap() {
        return loadCap;
    }

    public void setLoadCap(int loadCap) {
        this.loadCap = loadCap;
    }

    public long getLoadCapInterval() {
        return loadCapInterval;
    }

    public void setLoadCapInterval(long loadCapInterval) {
        this.loadCapInterval = loadCapInterval;
    }

    public int getCachedOffersNum() {
        return cachedOffersNum;
    }

    public void setCachedOffersNum(int cachedOffersNum) {
        this.cachedOffersNum = cachedOffersNum;
    }

    static class ResponseKey {
        private final static String psidOutTime_key = "ps_id_timeout";
        private final static String ps_ct = "ps_ct";
        private final static String ps_ct_out = "ps_ct_out";
        private final static String pucs = "pucs";
        private final static String adDeliverySw_key = "ad_delivery_sw";
        private final static String requestUnitGroupNumber_key = "req_ug_num";
        private final static String unitCapsDayNumber_key = "unit_caps_d";
        private final static String unitCapsHourNumber_key = "unit_caps_h";
        private final static String unitPacing_key = "unit_pacing";
        private final static String wifiAutoSw_key = "wifi_auto_sw";
        private final static String showType_key = "show_type";
        private final static String refreshr_key = "refresh";
        private final static String unitGroup_key = "ug_list";
        private final static String online_unitGroup_key = "ol_list";
        private final static String group_id_key = "gro_id";
        private final static String unitGroup_hb_list_key = "hb_list";

        /**
         * 5.6.1 Add Headbidding S2S List
         */
        private final static String unitGroup_hb_s2s_list_key = "s2shb_list";


        private final static String format_id_key = "format";
        private final static String auto_refresh = "auto_refresh";//Banner，interstitial、RV refresh switch
        private final static String auto_refresh_time = "auto_refresh_time";

        private final static String long_over_loadtime = "s_t"; //Long-Timeout
        private final static String up_status_overtime = "l_s_t"; //upstatus avail time
        private final static String unitgroup_auto_requestad = "ra"; //Auto Request Status. 1:Yes, 2:No
        private final static String asid = "asid"; //Service Asid
        private final static String tp_ps = "tp_ps";


        /**
         * v4.5.0
         */
        private final static String t_g_id = "t_g_id"; //AB test id
        private final static String s_id = "s_id";//Serice Settingid
        private final static String u_n_f_sw = "u_n_f_sw";//Default MyOffer Switch, 1:true, 0:false
        private final static String m_o = "m_o";//MyOffer List
        private final static String m_o_s = "m_o_s"; //MyOffer Setting
        private final static String m_o_ks = "m_o_ks"; //Tracking Map
        private final static String p_m_o = "p_m_o"; //Pre-Load MyOffer Resource，1:Yes，0:No

        /**
         * v5.5.3
         */
        private final static String callback = "callback"; //Callback info for developer
        private final static String sc_list = "sc_list"; //Scenario info key
        private final static String rw_n = "rw_n"; //Scenario info (reward name key)
        private final static String rw_num = "rw_num"; //Scenario info (reward count key)
        private final static String reward = "reward"; //placement reward info
        private final static String currency = "currency"; //currency
        private final static String cc = "cc"; //country
        /**
         * v5.7.9
         */
        private final static String exch_r = "exch_r"; //exchange rate
        private final static String acct_cy = "acct_cy"; //account currency

        /**
         * v5.5.7
         */
        public final static String hb_start_time = "hb_start_time";
        public final static String hb_bid_timeout = "hb_bid_timeout";

        /**
         * v5.6.1
         */
        public final static String hb_request_url = "addr_bid";

        /**
         * v5.6.2
         */
        public final static String load_fail_wtime = "load_fail_wtime";
        public final static String load_cap = "load_cap";
        public final static String load_cap_time = "load_cap_time";
        public final static String cached_offers_num = "cached_offers_num";

        /**
         * v5.7.0
         */
        public final static String adx_ug_list_key = "adx_list";
        public final static String adx_ad_setting_key = "adx_st";

        /**
         * v5.6.8
         */
        public final static String ilrd = "ilrd";

        /**
         * v5.7.1
         */
        public final static String c2s_hb_list_key = "hb_list";

        /**
         * v5.7.5
         */
        public final static String fb_inh_hb_list_key = "inh_list";
        public final static String fb_inh_timeout_key = "fbhb_bid_wtime";

    }

    public static class UnitGroupInfo implements Comparable<UnitGroupInfo> {
        /**
         * Only use for marking the adsource would not to be load or show.
         * if level == -1 , thie adsource would not to be load or show.
         */
        public int level;
        /***
         * network firm id
         */
        public int networkType;
        /**
         * network name
         */
        public String networkName;
        /**
         * Max Daily Cap
         */
        public int capsByDay;
        /**
         * Max Hourly cap
         */
        public int capsByHour;

        /***
         * Mediation Placement Info
         */
        public String content;
        /***
         * Adapter Class Name
         */
        public String adapterClassName;

        /***
         *  UnitGroup Id
         */
        public String unitGroupId;

        /**
         * Ad Pacing
         */
        public long pacing;

        /**
         * Click Tracking Info
         */
        public String clickTkUrl;

        public int clickTkDelayMinTime;

        public int clickTkDelayMaxTime;


        /**
         * ecpm
         */
        public double ecpm;

        public int bidType;

        public String payload;

        public String errorMsg;

        public int sortType;

        /**
         * create by HB module
         */
        public long bidEndTime;
        /**
         * create by HB module
         */
        public long bidUseTime;

        /***
         * Offer Cache Time
         */
        private long unitADCacheTime;

        /***
         * Request Timeout
         */
        private long unitADRequestOutTime;
        /**
         * Request Num
         */
        private int unitAdRequestNumber;

        /**
         * AdSource Id
         */
        public String unitId;

        /**
         * Head bidding timeout
         */
        public long hbTimeout;

        /**
         * upstatus avail time
         */
        public long upStatusTimeOut;
        /**
         * Ad Data Request Timeout
         */
        public long networkAdDataLoadTimeOut;

        /**
         * Bid Token Avail Time
         */
        public long bidTokenAvailTime;

        /**
         * Show Tracking Switch
         */
        public int showTkSwitch;
        /**
         * Click Tracking Switch
         */
        public int clickTkSwitch;

        /**
         * AdSource request layer-requestLevel
         */
        public int requestLayerLevel;

        /**
         * AdSource ecpm layer-requestLevel
         */
        public int ecpmLayerLevel;

        /**
         * ecpm precision (for normal ad sources and cross promotion): publisher_defined、estimated
         */
        public String ecpmPrecision;

        /**
         * Adsource load fail interval
         */
        public long requetFailInterval;

        /**
         * Bid fail interval
         */
        public long bidFailInterval;

        /**
         * Acccount currency ecpm
         */
        public double accountCurrencyEcpm;

        /**
         * Auto Ready Switch，1:close 2:open
         */
        public int autoReadySwitch;

        /**
         * adsourceType (0: normal  1: s2s   2: c2s   3: adx)
         */
        public int adsourceType;
        public static int TYPE_NORMAL = 0;
        public static int TYPE_HB_S2S = 1;
        public static int TYPE_HB_C2S = 2;
        public static int TYPE_ADX = 3;
        public static int TYPE_ONLINE_API = 4;
        public static int TYPE_FACEBOOK_INHOUSE = 5;


        public UnitGroupInfo() {
        }


        public long getBidTokenAvailTime() {
            return bidTokenAvailTime;
        }

        public void setBidTokenAvailTime(long bidTokenAvailTime) {
            this.bidTokenAvailTime = bidTokenAvailTime;
        }

        public int getSortType() {
            return sortType;
        }

        public void setSortType(int sortType) {
            this.sortType = sortType;
        }

        public long getUnitADCacheTime() {
            return unitADCacheTime;
        }

        public void setUnitADCacheTime(long unitADCacheTime) {
            this.unitADCacheTime = unitADCacheTime;
        }

        public long getUnitADRequestOutTime() {
            return unitADRequestOutTime;
        }

        public void setUnitADRequestOutTime(long unitADRequestOutTime) {
            this.unitADRequestOutTime = unitADRequestOutTime;
        }

        public int getUnitAdRequestNumber() {
            return unitAdRequestNumber;
        }

        public void setUnitAdRequestNumber(int unitAdRequestNumber) {
            this.unitAdRequestNumber = unitAdRequestNumber;
        }

        public long getUnitPacing() {
            return pacing;
        }

        public void setUnitPacing(long pacing) {
            this.pacing = pacing;
        }

        public String getUnitId() {
            return unitId;
        }

        public void setUnitId(String unitId) {
            this.unitId = unitId;
        }

        public String getClickTkUrl() {
            return clickTkUrl;
        }

        public void setClickTkUrl(String clickTkUrl) {
            this.clickTkUrl = clickTkUrl;
        }

        public int getClickTkDelayMinTime() {
            return clickTkDelayMinTime;
        }

        public void setClickTkDelayMinTime(int clickTkDelayMinTime) {
            this.clickTkDelayMinTime = clickTkDelayMinTime;
        }

        public int getClickTkDelayMaxTime() {
            return clickTkDelayMaxTime;
        }

        public void setClickTkDelayMaxTime(int clickTkDelayMaxTime) {
            this.clickTkDelayMaxTime = clickTkDelayMaxTime;
        }

        public double getEcpm() {
            return ecpm;
        }

        public void setEcpm(double ecpm) {
            this.ecpm = ecpm;
        }

        public long getHbTimeout() {
            return hbTimeout;
        }

        public void setHbTimeout(long hbTimeout) {
            this.hbTimeout = hbTimeout;
        }

        public String getPayload() {
            return payload;
        }

        public void setPayload(String payload) {
            this.payload = payload;
        }

        public String getErrorMsg() {
            return errorMsg;
        }

        public void setErrorMsg(String errorMsg) {
            this.errorMsg = errorMsg;
        }

        public void setUpStatusTimeOut(long upStatusTimeOut) {
            this.upStatusTimeOut = upStatusTimeOut;
        }

        public long getUpStatusTimeOut() {
            return this.upStatusTimeOut;
        }

        public void setNetworkAdDataLoadTimeOut(long networkAdDataLoadTimeOut) {
            this.networkAdDataLoadTimeOut = networkAdDataLoadTimeOut;
        }

        public long getNetworkAdDataLoadTimeOut() {
            return this.networkAdDataLoadTimeOut;
        }

        public int getShowTkSwitch() {
            return showTkSwitch;
        }

        public void setShowTkSwitch(int showTkSwitch) {
            this.showTkSwitch = showTkSwitch;
        }

        public int getClickTkSwitch() {
            return clickTkSwitch;
        }

        public void setClickTkSwitch(int clickTkSwitch) {
            this.clickTkSwitch = clickTkSwitch;
        }

        public int getRequestLayLevel() {
            return requestLayerLevel;
        }

        public void setRequestLayerLevel(int layerLevel) {
            requestLayerLevel = layerLevel;
        }

        public int getEcpmLayLevel() {
            return ecpmLayerLevel;
        }

        public void setEcpmLayerLevel(int layerLevel) {
            ecpmLayerLevel = layerLevel;
        }

        public String getEcpmPrecision() {
            return ecpmPrecision;
        }

        public void setEcpmPrecision(String ecpmPrecision) {
            this.ecpmPrecision = ecpmPrecision;
        }

        public void setRequestFailInterval(long requestFailInterval) {
            this.requetFailInterval = requestFailInterval;
        }

        public long getRequestFailInterval() {
            return this.requetFailInterval;
        }

        public long getBidFailInterval() {
            return bidFailInterval;
        }

        public void setBidFailInterval(long bidFailInterval) {
            this.bidFailInterval = bidFailInterval;
        }

        public double getAccountCurrencyEcpm() {
            return accountCurrencyEcpm;
        }

        public void setAccountCurrencyEcpm(double accountCurrencyEcpm) {
            this.accountCurrencyEcpm = accountCurrencyEcpm;
        }

        public int getAutoReadySwitch() {
            return autoReadySwitch;
        }

        public void setAutoReadySwitch(int autoReadySwitch) {
            this.autoReadySwitch = autoReadySwitch;
        }

        @Override
        public int compareTo(UnitGroupInfo o) {
            if (this.ecpm > o.ecpm) {
                return -1;
            } else {
                return 1;
            }
        }
    }


    /***
     * @param jsonStr
     * @return
     */
    public static PlaceStrategy parseStrategy(String jsonStr) {
        if (jsonStr == null) {
            return null;
        }

        try {
            PlaceStrategy strategy = new PlaceStrategy();
            JSONObject jsonObject = new JSONObject(jsonStr);

            if (jsonObject.isNull(ResponseKey.ps_ct)) {
                strategy.setPsCacheTime(0);
            } else {
                strategy.setPsCacheTime(jsonObject.optLong(ResponseKey.ps_ct));
            }

            if (jsonObject.isNull(ResponseKey.ps_ct_out)) {
                strategy.setPsUpdateOutTime(0);
            } else {
                strategy.setPsUpdateOutTime(jsonObject.optLong(ResponseKey.ps_ct_out));
            }

            if (jsonObject.isNull(ResponseKey.pucs)) {
                strategy.setPucs(1);
            } else {
                strategy.setPucs(jsonObject.optInt(ResponseKey.pucs));
            }

            if (jsonObject.isNull(ResponseKey.adDeliverySw_key)) {
                strategy.setAdDeliverySw(1);
            } else {
                strategy.setAdDeliverySw(jsonObject.optInt(ResponseKey.adDeliverySw_key));
            }

            if (jsonObject.isNull(ResponseKey.requestUnitGroupNumber_key)) {
                strategy.setRequestUnitGroupNumber(-1);
            } else {
                strategy.setRequestUnitGroupNumber(jsonObject.optInt(ResponseKey.requestUnitGroupNumber_key));
            }

            if (jsonObject.isNull(ResponseKey.unitCapsDayNumber_key)) {
                strategy.setUnitCapsDayNumber(-1);
            } else {
                strategy.setUnitCapsDayNumber(jsonObject.optLong(ResponseKey.unitCapsDayNumber_key));
            }

            if (jsonObject.isNull(ResponseKey.unitCapsHourNumber_key)) {
                strategy.setUnitCapsHourNumber(-1);
            } else {
                strategy.setUnitCapsHourNumber(jsonObject.optLong(ResponseKey.unitCapsHourNumber_key));
            }

            if (jsonObject.isNull(ResponseKey.unitPacing_key)) {
                strategy.setUnitPacing(-1);
            } else {
                strategy.setUnitPacing(jsonObject.optLong(ResponseKey.unitPacing_key));
            }

            if (jsonObject.isNull(ResponseKey.wifiAutoSw_key)) {
                strategy.setWifiAutoSw(0);
            } else {
                strategy.setWifiAutoSw(jsonObject.optInt(ResponseKey.wifiAutoSw_key));
            }

            if (jsonObject.isNull(ResponseKey.showType_key)) {
                strategy.setShowType(0);
            } else {
                strategy.setShowType(jsonObject.optInt(ResponseKey.showType_key));
            }


            if (jsonObject.isNull(ResponseKey.refreshr_key)) {
                strategy.setRefreshr(0);
            } else {
                strategy.setRefreshr(jsonObject.optInt(ResponseKey.refreshr_key));
            }

            if (jsonObject.isNull(ResponseKey.group_id_key)) {
                strategy.setGroupId(0);
            } else {
                strategy.setGroupId(jsonObject.optInt(ResponseKey.group_id_key));
            }


            if (jsonObject.isNull(ResponseKey.format_id_key)) {
                strategy.setFormat(0);
            } else {
                strategy.setFormat(jsonObject.optInt(ResponseKey.format_id_key));
            }

            if (jsonObject.isNull(ResponseKey.auto_refresh)) {
                strategy.setAutoRefresh(0);
            } else {
                strategy.setAutoRefresh(jsonObject.optInt(ResponseKey.auto_refresh));
            }

            /**3.3.0**/
            if (jsonObject.isNull(ResponseKey.auto_refresh_time)) {
                strategy.setAutoRefresh(0);
            } else {
                strategy.setAutoRefreshTime(jsonObject.optLong(ResponseKey.auto_refresh_time));
            }


            /**3.5.0**/
            if (jsonObject.isNull(ResponseKey.long_over_loadtime)) {
                strategy.setLongOverLoadTime(15 * 60 * 1000L);
            } else {
                strategy.setLongOverLoadTime(jsonObject.optLong(ResponseKey.long_over_loadtime));
            }

            if (jsonObject.isNull(ResponseKey.up_status_overtime)) {
                strategy.setUpStatusOverTime(30 * 60 * 1000L);
            } else {
                strategy.setUpStatusOverTime(jsonObject.optLong(ResponseKey.up_status_overtime));
            }

            if (jsonObject.isNull(ResponseKey.unitgroup_auto_requestad)) {
                strategy.setAutoRequestUnitgroupAd(-1);
            } else {
                strategy.setAutoRequestUnitgroupAd(jsonObject.optInt(ResponseKey.unitgroup_auto_requestad));
            }

            if (jsonObject.isNull(ResponseKey.asid)) {
                strategy.setAsid("");
            } else {
                strategy.setAsid(jsonObject.optString(ResponseKey.asid));
            }

            if (jsonObject.isNull(ResponseKey.tp_ps)) {
                strategy.setTemplateStrategy(null);
            } else {
                try {
                    TemplateStrategy templateStrategy = new TemplateStrategy();
                    JSONObject templateObject = jsonObject.optJSONObject(ResponseKey.tp_ps);
                    templateStrategy.isUseCacheStrategy = templateObject.optInt("pucs") == 1;
                    templateStrategy.defaultDelayTime = templateObject.optLong("apdt");
                    templateStrategy.defaultNetworkFirmId = templateObject.optInt("aprn");
                    templateStrategy.isUseNetConfig = templateObject.optInt("puas") == 1;
                    templateStrategy.countDownTime = templateObject.optLong("cdt");
                    templateStrategy.canSkip = templateObject.optInt("ski_swt") == 1;
                    templateStrategy.isAutoClose = templateObject.optInt("aut_swt") == 1;
                    strategy.setTemplateStrategy(templateStrategy);
                } catch (Exception e) {

                }

            }

            if (jsonObject.isNull(ResponseKey.unitGroup_key)) {
                strategy.setNormalUnitGroupListStr("[]");
            } else {
                strategy.setNormalUnitGroupListStr(jsonObject.optString(ResponseKey.unitGroup_key));
            }

            if (jsonObject.isNull(ResponseKey.online_unitGroup_key)) {
                strategy.setOnlineUnitGroupListStr("[]");
            } else {
                strategy.setOnlineUnitGroupListStr(jsonObject.optString(ResponseKey.online_unitGroup_key));
            }

            strategy.setNormalUnitGroupList(parseNormalUnitGroupInfoList(strategy.getNormalUnitGroupListStr(), strategy.getOnlineUnitGroupListStr()));


            if (jsonObject.isNull(ResponseKey.unitGroup_hb_s2s_list_key)) {
                strategy.setS2SHeadbiddingUnitGroupListStr("[]");
            } else {
                strategy.setS2SHeadbiddingUnitGroupListStr(jsonObject.optString(ResponseKey.unitGroup_hb_s2s_list_key));
            }

            if (jsonObject.isNull(ResponseKey.adx_ug_list_key)) {
                strategy.setAdxUnitGroupListStr("[]");
            } else {
                strategy.setAdxUnitGroupListStr(jsonObject.optString(ResponseKey.adx_ug_list_key));
            }

            if (jsonObject.isNull(ResponseKey.c2s_hb_list_key)) {
                strategy.setC2SHeadbiddingUnitGroupListStr("[]");
            } else {
                strategy.setC2SHeadbiddingUnitGroupListStr(jsonObject.optString(ResponseKey.c2s_hb_list_key));
            }

            if (jsonObject.isNull(ResponseKey.fb_inh_hb_list_key)) {
                strategy.setFbInHouseHeadbiddingUnitGroupListStr("[]");
            } else {
                strategy.setFbInHouseHeadbiddingUnitGroupListStr(jsonObject.optString(ResponseKey.fb_inh_hb_list_key));
            }

            try {
                strategy.setHeadBiddingList(parseHeadBiddingUnitGroupInfoList(strategy.getS2SHeadbiddingUnitGroupListStr()
                        , strategy.getAdxUnitGroupListStr()
                        , strategy.getC2SHeadbiddingUnitGroupListStr()
                        , strategy.getFbInHouseHeadbiddingUnitGroupListStr()));
            } catch (Exception e) {

            }


            long updateTime = 0;
            if (jsonObject.isNull("updateTime")) {
                strategy.setUpdateTime(0);
            } else {
                updateTime = jsonObject.optLong("updateTime");
                strategy.setUpdateTime(updateTime);
            }

            /**
             * v4.5.0
             */
            if (jsonObject.isNull(ResponseKey.t_g_id)) {
                strategy.setTracfficGroupId(-1);
            } else {
                strategy.setTracfficGroupId(jsonObject.optInt(ResponseKey.t_g_id));
            }

            if (jsonObject.isNull(ResponseKey.s_id)) {
                strategy.setSettingId("");
            } else {
                strategy.setSettingId(jsonObject.optString(ResponseKey.s_id));
            }

            if (jsonObject.isNull(ResponseKey.u_n_f_sw)) {
                strategy.setUseDefaultMyOffer(0);
            } else {
                strategy.setUseDefaultMyOffer(jsonObject.optInt(ResponseKey.u_n_f_sw));
            }

            if (jsonObject.isNull(ResponseKey.m_o)) {
                strategy.setMyOfferAdList(null);
            } else {
                strategy.setMyOfferAdList(parseOfferList(jsonObject.optString(ResponseKey.m_o), jsonObject.optString(ResponseKey.m_o_ks), updateTime));
            }

            if (jsonObject.isNull(ResponseKey.m_o_s)) {
            } else {
                strategy.setMyOfferSetting(MyOfferSetting.parseMyOfferSetting(jsonObject.optString(ResponseKey.m_o_s)));
            }

            if (jsonObject.isNull(ResponseKey.p_m_o)) {
                strategy.setIsPreLoadOfferRes(0);
            } else {
                strategy.setIsPreLoadOfferRes(jsonObject.optInt(ResponseKey.p_m_o));
            }

            /**5.5.1 local custom map**/
            if (jsonObject.isNull(PlaceStrategyLoader.CUSTOM_KEY)) {
                strategy.setSdkCustomMap(null);
            } else {
                JSONObject customObject = new JSONObject(jsonObject.optString(PlaceStrategyLoader.CUSTOM_KEY));
                HashMap<String, Object> map = new HashMap<>();
                Iterator<String> jsonKeyIterator = customObject.keys();
                while (jsonKeyIterator.hasNext()) {
                    String key = jsonKeyIterator.next();
                    map.put(key, customObject.opt(key));
                }
                strategy.setSdkCustomMap(map);

            }

            /** v5.5.3 callback info for developer */
            if (!jsonObject.isNull(ResponseKey.callback)) {
                JSONObject callbackObject = new JSONObject(jsonObject.optString(ResponseKey.callback));

                if (!callbackObject.isNull(ResponseKey.sc_list)) {
                    JSONObject scenarioObject = new JSONObject(callbackObject.optString(ResponseKey.sc_list));
                    HashMap<String, ATRewardInfo> map = new HashMap<>();


                    Iterator<String> jsonKeyIterator = scenarioObject.keys();
                    JSONObject rewardObject;
                    while (jsonKeyIterator.hasNext()) {
                        String key = jsonKeyIterator.next();

                        rewardObject = new JSONObject(scenarioObject.optString(key));

                        ATRewardInfo atRewardInfo = new ATRewardInfo();
                        atRewardInfo.rewardName = rewardObject.optString(ResponseKey.rw_n);
                        atRewardInfo.rewardNumber = rewardObject.optInt(ResponseKey.rw_num);
                        map.put(key, atRewardInfo);
                    }
                    strategy.setScenarioRewardMap(map);
                }

                if (!callbackObject.isNull(ResponseKey.reward)) {
                    JSONObject rewardObject = new JSONObject(callbackObject.optString(ResponseKey.reward));
                    ATRewardInfo atRewardInfo = new ATRewardInfo();
                    if (!rewardObject.isNull(ResponseKey.rw_n)) {
                        atRewardInfo.rewardName = rewardObject.optString(ResponseKey.rw_n);
                    }
                    if (!rewardObject.isNull(ResponseKey.rw_num)) {
                        atRewardInfo.rewardNumber = rewardObject.optInt(ResponseKey.rw_num);
                    }
                    strategy.setPlacementRewardInfo(atRewardInfo);
                }

                if (!callbackObject.isNull(ResponseKey.currency)) {
                    strategy.setCurrency(callbackObject.optString(ResponseKey.currency));
                }

                if (!callbackObject.isNull(ResponseKey.cc)) {
                    strategy.setCountry(callbackObject.optString(ResponseKey.cc));
                }

                if (!callbackObject.isNull(ResponseKey.exch_r)) {
                    strategy.setAccountExchageRate(callbackObject.optDouble(ResponseKey.exch_r));
                }
                if (!callbackObject.isNull(ResponseKey.acct_cy)) {
                    strategy.setAccountCurrency(callbackObject.optString(ResponseKey.acct_cy));
                }

            }

            /** v5.5.7 */
            if (jsonObject.isNull(ResponseKey.hb_start_time)) {
                strategy.setHbWaitingToRequestTime(2000L);
            } else {
                strategy.setHbWaitingToRequestTime(jsonObject.optLong(ResponseKey.hb_start_time));
            }
            if (jsonObject.isNull(ResponseKey.hb_bid_timeout)) {
                strategy.setHbBidTimeout(10000L);
            } else {
                strategy.setHbBidTimeout(jsonObject.optLong(ResponseKey.hb_bid_timeout));
            }

            /**v5.6.1**/
            if (jsonObject.isNull(ResponseKey.hb_request_url)) {
                strategy.setHbRequestUrl("");
            } else {
                strategy.setHbRequestUrl(jsonObject.optString(ResponseKey.hb_request_url));
            }

            /**
             * v5.6.2
             */
            if (jsonObject.isNull(ResponseKey.load_fail_wtime)) {
                strategy.setLoadFailWaitTime(10 * 1000);
            } else {
                strategy.setLoadFailWaitTime(jsonObject.optLong(ResponseKey.load_fail_wtime));
            }

            if (jsonObject.isNull(ResponseKey.load_cap)) {
                strategy.setLoadCap(-1);
            } else {
                strategy.setLoadCap(jsonObject.optInt(ResponseKey.load_cap));
            }

            if (jsonObject.isNull(ResponseKey.load_cap_time)) {
                strategy.setLoadCapInterval(900000);
            } else {
                strategy.setLoadCapInterval(jsonObject.optLong(ResponseKey.load_cap_time));
            }

            if (jsonObject.isNull(ResponseKey.cached_offers_num)) {
                strategy.setCachedOffersNum(2);
            } else {
                strategy.setCachedOffersNum(jsonObject.optInt(ResponseKey.cached_offers_num));
            }

            /**
             * v5.6.8
             */
            if (jsonObject.isNull(ResponseKey.ilrd)) {
                strategy.setImpressionRevenueForMonitoringPlatformString(null);
            } else {
                strategy.setImpressionRevenueForMonitoringPlatformString(jsonObject.optString(ResponseKey.ilrd));
            }

            if (jsonObject.isNull(ResponseKey.adx_ad_setting_key)) {
                strategy.setAdxAdSettingStr("");
            } else {
                strategy.setAdxAdSettingStr(jsonObject.optString(ResponseKey.adx_ad_setting_key));
            }

            /**
             * v5.7.5
             */
            if (jsonObject.isNull(ResponseKey.fb_inh_timeout_key)) {
                strategy.setFbInHouseTimeout(4000L);
            } else {
                strategy.setFbInHouseTimeout(jsonObject.optLong(ResponseKey.fb_inh_timeout_key));
            }

            return strategy;
        } catch (Exception e) {
            if (Const.DEBUG) {
                e.printStackTrace();
            }

        }
        return null;

    }


    public boolean isAdOpen() {
        return adDeliverySw == 1;
    }

//    List<UnitGroupInfo> mNofilterList = new ArrayList<>();

    /**
     * Temp no filer UnitGroupList, use to tracking
     *
     * @param noFilterList
     */
//    public synchronized void setNoFilterList(List<UnitGroupInfo> noFilterList) {
//        CommonLogUtil.i("placement", "setNoFilterList Size:" + noFilterList.size());
//        mNofilterList = noFilterList;
//        for (UnitGroupInfo unitGroupInfo : mNofilterList) {
//            unitGroupInfo.level = -1;
//        }
//    }

//    /**
//     * Final Request UnitGroup List
//     *
//     * @param filterList
//     */
//    public synchronized void updateSortUnitgroupList(List<UnitGroupInfo> filterList) {
//        if (sortByEcpmUnitGroupList != null) {
//            sortByEcpmUnitGroupList.clear();
//        } else {
//            sortByEcpmUnitGroupList = new ArrayList<>();
//        }
//
//        CommonLogUtil.i("placement", "update filteSize:" + filterList.size() + "---no filter size:" + mNofilterList.size());
//        sortByEcpmUnitGroupList.addAll(filterList);
//        sortByEcpmUnitGroupList.addAll(mNofilterList);
//    }


//    /**
//     * 转换uglist为jsonArray
//     *
//     * @param unitGroupInfoList
//     * @return
//     */
//    public static JSONArray parseUnitGroupListToJSONArray(List<PlaceStrategy.UnitGroupInfo> unitGroupInfoList) {
//        JSONArray jsonArray = new JSONArray();
//        try {
//            for (PlaceStrategy.UnitGroupInfo unitGroupInfo : unitGroupInfoList) {
//                jsonArray.put(unitGroupInfo.toJSONObject());
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return jsonArray;
//    }

    /**
     * Parse Headbidding List
     *
     * @param s2sHbInfo
     * @param adxHbInfo
     * @return
     */
    public static List<PlaceStrategy.UnitGroupInfo> parseHeadBiddingUnitGroupInfoList(String s2sHbInfo, String adxHbInfo, String c2sHbInfo, String fbInHouseInfo) {
        List<PlaceStrategy.UnitGroupInfo> s2sHbInfoList = parseUnitGroupInfoList(s2sHbInfo, UnitGroupInfo.TYPE_HB_S2S);
        List<PlaceStrategy.UnitGroupInfo> adxHbInfoList = parseUnitGroupInfoList(adxHbInfo, UnitGroupInfo.TYPE_ADX);
        List<PlaceStrategy.UnitGroupInfo> c2sHbInfoList = parseUnitGroupInfoList(c2sHbInfo, UnitGroupInfo.TYPE_HB_C2S);
        List<PlaceStrategy.UnitGroupInfo> fbInHoseHbInfoList = parseUnitGroupInfoList(fbInHouseInfo, UnitGroupInfo.TYPE_FACEBOOK_INHOUSE);

        s2sHbInfoList.addAll(adxHbInfoList);
        s2sHbInfoList.addAll(c2sHbInfoList);
        s2sHbInfoList.addAll(fbInHoseHbInfoList);

        return s2sHbInfoList;
    }

    public static List<PlaceStrategy.UnitGroupInfo> parseNormalUnitGroupInfoList(String normalUnitGroupListStr, String onlineUnitGroupListStr) {
        List<PlaceStrategy.UnitGroupInfo> normalInfoList = parseUnitGroupInfoList(normalUnitGroupListStr, UnitGroupInfo.TYPE_NORMAL);
        List<PlaceStrategy.UnitGroupInfo> onlineInfoList = parseUnitGroupInfoList(onlineUnitGroupListStr, UnitGroupInfo.TYPE_ONLINE_API);

        normalInfoList.addAll(onlineInfoList);

        Collections.sort(normalInfoList);

        return normalInfoList;
    }

    /**
     * jsonArray to uglist
     *
     * @param successArray
     * @return
     */
    private static List<PlaceStrategy.UnitGroupInfo> parseUnitGroupInfoList(String successArray, int adsourceType) {
        boolean isHB = adsourceType != UnitGroupInfo.TYPE_NORMAL && adsourceType != UnitGroupInfo.TYPE_ONLINE_API;
        ArrayList<UnitGroupInfo> unitGroupList = new ArrayList<PlaceStrategy.UnitGroupInfo>();
        try {
            JSONArray unitGroupArray = new JSONArray(successArray);

            PlaceStrategy.UnitGroupInfo unitGroupInfo;
            JSONObject arryItemTemp;
            for (int i = 0; i < unitGroupArray.length(); i++) {
                arryItemTemp = unitGroupArray.getJSONObject(i);
                if (arryItemTemp == null) {
                    continue;
                }
                unitGroupInfo = new PlaceStrategy.UnitGroupInfo();
//                unitGroupInfo.level = i;
                unitGroupInfo.adsourceType = adsourceType;
                unitGroupInfo.bidType = isHB ? 1 : 0;
                if (arryItemTemp.isNull(HeadBiddingKey.unitGroup_adapterClassName_key)) {
                    unitGroupInfo.adapterClassName = "";
                } else {
                    unitGroupInfo.adapterClassName = arryItemTemp.optString(HeadBiddingKey.unitGroup_adapterClassName_key);
                }


                if (arryItemTemp.isNull(HeadBiddingKey.unitGroup_capsByDay_key)) {
                    unitGroupInfo.capsByDay = -1;
                } else {
                    unitGroupInfo.capsByDay = arryItemTemp.optInt(HeadBiddingKey.unitGroup_capsByDay_key);
                }


                if (arryItemTemp.isNull(HeadBiddingKey.unitGroup_capsByHour_key)) {
                    unitGroupInfo.capsByHour = -1;
                } else {
                    unitGroupInfo.capsByHour = arryItemTemp.optInt(HeadBiddingKey.unitGroup_capsByHour_key);
                }

                if (arryItemTemp.isNull(HeadBiddingKey.unitGroup_content_key)) {
                    unitGroupInfo.content = "";
                } else {
                    unitGroupInfo.content = arryItemTemp.optString(HeadBiddingKey.unitGroup_content_key);
                }

                if (arryItemTemp.isNull(HeadBiddingKey.unitGroup_networkType_key)) {
                    unitGroupInfo.networkType = -1;
                } else {
                    unitGroupInfo.networkType = arryItemTemp.optInt(HeadBiddingKey.unitGroup_networkType_key);
                }

                if (arryItemTemp.isNull(HeadBiddingKey.unitGroup_networkName_key)) {
                    unitGroupInfo.networkName = "";
                } else {
                    unitGroupInfo.networkName = arryItemTemp.optString(HeadBiddingKey.unitGroup_networkName_key);
                }

                if (arryItemTemp.isNull(HeadBiddingKey.unitGroup_unitGroupId_key)) {
                    unitGroupInfo.unitGroupId = "unknown";
                } else {
                    unitGroupInfo.unitGroupId = arryItemTemp.optString(HeadBiddingKey.unitGroup_unitGroupId_key);
                }

                if (arryItemTemp.isNull(HeadBiddingKey.unitGroup_unitADCacheTime_key)) {
                    unitGroupInfo.setUnitADCacheTime(0);
                } else {
                    unitGroupInfo.setUnitADCacheTime(arryItemTemp.optInt(HeadBiddingKey.unitGroup_unitADCacheTime_key));
                }


                if (arryItemTemp.isNull(HeadBiddingKey.unitGroup_unitADRequestOutTime_key)) {
                    unitGroupInfo.setUnitADRequestOutTime(0);
                } else {
                    unitGroupInfo.setUnitADRequestOutTime(arryItemTemp.optInt(HeadBiddingKey.unitGroup_unitADRequestOutTime_key));
                }

                if (arryItemTemp.isNull(HeadBiddingKey.unitGroup_unitAdRequestNumber_key)) {
                    unitGroupInfo.setUnitAdRequestNumber(1);
                } else {
                    unitGroupInfo.setUnitAdRequestNumber(arryItemTemp.optInt(HeadBiddingKey.unitGroup_unitAdRequestNumber_key));
                }

                if (arryItemTemp.isNull(HeadBiddingKey.unitGroup_unitGroupId_pacing)) {
                    unitGroupInfo.setUnitPacing(-1);
                } else {
                    unitGroupInfo.setUnitPacing(arryItemTemp.optLong((HeadBiddingKey.unitGroup_unitGroupId_pacing)));
                }

                if (arryItemTemp.isNull(HeadBiddingKey.unitGroup_unit_id_key)) {
                    unitGroupInfo.setUnitId("");
                } else {
                    unitGroupInfo.setUnitId(arryItemTemp.optString(HeadBiddingKey.unitGroup_unit_id_key));
                }

                if (arryItemTemp.isNull(HeadBiddingKey.unitGroup_ecpm)) {
                    unitGroupInfo.setEcpm(0);
                } else {
                    if (!isHB) {
                        unitGroupInfo.setEcpm(arryItemTemp.optDouble(HeadBiddingKey.unitGroup_ecpm));
                    }
                }

                if (arryItemTemp.isNull(HeadBiddingKey.unitGroup_hb_timeout)) {
                    unitGroupInfo.setHbTimeout(2000);
                } else {
                    unitGroupInfo.setHbTimeout(arryItemTemp.optInt(HeadBiddingKey.unitGroup_hb_timeout));
                }

                /**v4.6.0*/
                if (arryItemTemp.isNull(HeadBiddingKey.unitGroup_clickTkUrl)) {
                    unitGroupInfo.setClickTkUrl("");
                } else {
                    unitGroupInfo.setClickTkUrl(arryItemTemp.optString(HeadBiddingKey.unitGroup_clickTkUrl));
                }

                if (arryItemTemp.isNull(HeadBiddingKey.unitGroup_clickTkDelayMinTime)) {
                    unitGroupInfo.setClickTkDelayMinTime(0);
                } else {
                    unitGroupInfo.setClickTkDelayMinTime(arryItemTemp.optInt(HeadBiddingKey.unitGroup_clickTkDelayMinTime));
                }

                if (arryItemTemp.isNull(HeadBiddingKey.unitGroup_clickTkDelayMaxTime)) {
                    unitGroupInfo.setClickTkDelayMaxTime(3000);
                } else {
                    unitGroupInfo.setClickTkDelayMaxTime(arryItemTemp.optInt(HeadBiddingKey.unitGroup_clickTkDelayMaxTime));
                }

                if (arryItemTemp.isNull(HeadBiddingKey.headbidding_payload)) {
                    unitGroupInfo.setPayload("");
                } else {
                    unitGroupInfo.setPayload(arryItemTemp.optString(HeadBiddingKey.headbidding_payload));
                }

                if (arryItemTemp.isNull(HeadBiddingKey.headbidding_errormsg)) {
                    unitGroupInfo.setErrorMsg("");
                } else {
                    unitGroupInfo.setErrorMsg(arryItemTemp.optString(HeadBiddingKey.headbidding_errormsg));
                }

                /**
                 * 5.1.0
                 */
                if (arryItemTemp.isNull(HeadBiddingKey.unitGroup_upstatus_avail_time)) {
                    unitGroupInfo.setUpStatusTimeOut(30 * 60 * 1000L);
                } else {
                    unitGroupInfo.setUpStatusTimeOut(arryItemTemp.optLong(HeadBiddingKey.unitGroup_upstatus_avail_time));
                }

                if (arryItemTemp.isNull(HeadBiddingKey.unitGroup_addata_load_timeout)) {
                    unitGroupInfo.setNetworkAdDataLoadTimeOut(-1);
                } else {
                    unitGroupInfo.setNetworkAdDataLoadTimeOut(arryItemTemp.optLong(HeadBiddingKey.unitGroup_addata_load_timeout));
                }

                if (arryItemTemp.isNull(HeadBiddingKey.unitGroup_hb_token_avail_time)) {
                    unitGroupInfo.setBidTokenAvailTime(30 * 60 * 1000L);
                } else {
                    unitGroupInfo.setBidTokenAvailTime(arryItemTemp.optLong(HeadBiddingKey.unitGroup_hb_token_avail_time));
                }

                /**v5.2.1**/
                if (arryItemTemp.isNull(HeadBiddingKey.headbidding_sortType)) {
                    unitGroupInfo.setSortType(isHB ? 0 : 1);
                } else {
                    unitGroupInfo.setSortType(arryItemTemp.optInt(HeadBiddingKey.headbidding_sortType));
                }

                /**v5.4.0**/
                if (arryItemTemp.isNull(HeadBiddingKey.unitGroup_ShowTKSwitch)) {
                    unitGroupInfo.setShowTkSwitch(1);
                } else {
                    unitGroupInfo.setShowTkSwitch(arryItemTemp.optInt(HeadBiddingKey.unitGroup_ShowTKSwitch));
                }
                if (arryItemTemp.isNull(HeadBiddingKey.unitGroup_ClickTKSwitch)) {
                    unitGroupInfo.setClickTkSwitch(1);
                } else {
                    unitGroupInfo.setClickTkSwitch(arryItemTemp.optInt(HeadBiddingKey.unitGroup_ClickTKSwitch));
                }

                /**v5.5.3**/
                if (arryItemTemp.isNull(HeadBiddingKey.unitGroup_ecpm_level)) {
                    unitGroupInfo.setEcpmLayerLevel(-1);
                } else {
                    unitGroupInfo.setEcpmLayerLevel(arryItemTemp.optInt(HeadBiddingKey.unitGroup_ecpm_level));
                }

                if (arryItemTemp.isNull(HeadBiddingKey.unitGroup_precision)) {
                    unitGroupInfo.setEcpmPrecision("publisher_defined");
                } else {
                    unitGroupInfo.setEcpmPrecision(arryItemTemp.optString(HeadBiddingKey.unitGroup_precision));
                }

                if (arryItemTemp.isNull(HeadBiddingKey.unitGroup_request_fail_interval)) {
                    unitGroupInfo.setRequestFailInterval(0);
                } else {
                    unitGroupInfo.setRequestFailInterval(arryItemTemp.optLong(HeadBiddingKey.unitGroup_request_fail_interval));
                }

                if (arryItemTemp.isNull(HeadBiddingKey.unitGroup_bid_fail_interval)) {
                    unitGroupInfo.setBidFailInterval(0);
                } else {
                    unitGroupInfo.setBidFailInterval(arryItemTemp.optLong(HeadBiddingKey.unitGroup_bid_fail_interval));
                }

                /**
                 * v5.7.9
                 */
                if (arryItemTemp.isNull(HeadBiddingKey.unitGroup_account_currency_ecpm)){
                    unitGroupInfo.setAccountCurrencyEcpm(0);
                } else {
                    unitGroupInfo.setAccountCurrencyEcpm(arryItemTemp.optDouble(HeadBiddingKey.unitGroup_account_currency_ecpm));
                }

                /**
                 * v5.7.9
                 */
                if (arryItemTemp.isNull(HeadBiddingKey.unitGroup_auto_ready_switch)) {
                    unitGroupInfo.setAutoReadySwitch(1);
                } else {
                    unitGroupInfo.setAutoReadySwitch(arryItemTemp.optInt(HeadBiddingKey.unitGroup_auto_ready_switch));
                }

                unitGroupList.add(unitGroupInfo);
            }
        } catch (Exception e) {

        }

        return unitGroupList;

    }

    /**
     * Parse MyOffer List
     *
     * @param offerList
     * @return
     */
    private static List<MyOfferAd> parseOfferList(String offerList, String tkInfoMap, long updateTime) {
        List<MyOfferAd> myOfferAdList = new ArrayList<>();

        try {
            JSONArray jsonArray = new JSONArray(offerList);

            for (int i = 0; i < jsonArray.length(); i++) {
                MyOfferAd myOfferAd = new MyOfferAd();
                JSONObject offerObject = jsonArray.optJSONObject(i);
                myOfferAd.setOfferId(offerObject.optString("o_id"));
                myOfferAd.setCreativeId(offerObject.optString("c_id"));
                myOfferAd.setTitle(offerObject.optString("t"));
                myOfferAd.setPkgName(offerObject.optString("p_g"));
                myOfferAd.setDesc(offerObject.optString("d"));
                myOfferAd.setIconUrl(offerObject.optString("ic_u"));
                myOfferAd.setMainImageUrl(offerObject.optString("im_u"));
                myOfferAd.setEndCardImageUrl(offerObject.optString("f_i_u"));
                myOfferAd.setAdChoiceUrl(offerObject.optString("a_c_u"));
                myOfferAd.setCtaText(offerObject.optString("c_t"));
                myOfferAd.setVideoUrl(offerObject.optString("v_u"));
                myOfferAd.setClickType(offerObject.optInt("l_t"));
                myOfferAd.setPreviewUrl(offerObject.optString("p_u"));
                myOfferAd.setDeeplinkUrl(offerObject.optString("dl"));
                myOfferAd.setClickUrl(offerObject.optString("c_u"));
                myOfferAd.setNoticeUrl(offerObject.optString("ip_u"));

                /**Tracking url handle**/
                myOfferAd.setVideoStartTrackUrl(offerObject.optString("t_u"));
                myOfferAd.setVideoProgress25TrackUrl(offerObject.optString("t_u_25"));
                myOfferAd.setVideoProgress50TrackUrl(offerObject.optString("t_u_50"));
                myOfferAd.setVideoProgress75TrackUrl(offerObject.optString("t_u_75"));
                myOfferAd.setVideoFinishTrackUrl(offerObject.optString("t_u_100"));
                myOfferAd.setEndCardShowTrackUrl(offerObject.optString("s_e_c_t_u"));
                myOfferAd.setEndCardCloseTrackUrl(offerObject.optString("c_t_u"));
                myOfferAd.setImpressionTrackUrl(offerObject.optString("ip_n_u"));
                myOfferAd.setClickTrackUrl(offerObject.optString("c_n_u"));


                myOfferAd.setOfferCap(offerObject.optInt("o_a_d_c"));
                myOfferAd.setOfferPacing(offerObject.optLong("o_a_p"));
                myOfferAd.setUpdateTime(updateTime);

                myOfferAd.setUnitType(offerObject.optInt("unit_type"));
                myOfferAd.setClickMode(offerObject.optInt("c_m"));

                /**v5.6.6**/
                myOfferAd.setBanner320x50Url(offerObject.optString("ext_h_pic"));
                myOfferAd.setBanner320x90Url(offerObject.optString("ext_big_h_pic"));
                myOfferAd.setBanner300x250Url(offerObject.optString("ext_rect_h_pic"));
                myOfferAd.setBanner728x90Url(offerObject.optString("ext_home_h_pic"));

                myOfferAd.setTkInfoMap(tkInfoMap);
                myOfferAdList.add(myOfferAd);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return myOfferAdList;
    }


    /**
     * Get MyOffer by Id
     *
     * @param offerId
     * @return
     */
    public MyOfferAd getMyOfferByOfferId(String offerId) {
        if (myOfferAdList != null) {
            for (MyOfferAd myOfferAd : myOfferAdList) {
                if (TextUtils.equals(offerId, myOfferAd.getOfferId()) && !myOfferAd.isExpire(myOfferSetting)) {
                    return myOfferAd;
                }
            }
        }
        return null;
    }

    /**
     * Check Placement Setting status
     */
    public boolean isPlaceStrategyExpired() {
        CommonLogUtil.d(TAG, "Already cache time -- > " + (System.currentTimeMillis() - updateTime));
        return System.currentTimeMillis() - updateTime > ps_ct;
    }

    /**
     * JSONObject to Map
     */
    public Map<String, Object> getServerExtrasMap(String placementId, String requestId, PlaceStrategy.UnitGroupInfo unitGroupInfo) {
        Map<String, Object> serviceExtras = CommonUtil.jsonObjectToMap(unitGroupInfo.content);
        boolean isAgeLess13 = false;
        try {
            Map<String, Object> customMap = SDKContext.getInstance().getCustomMap();
            if (customMap != null && customMap.containsKey(ATCustomRuleKeys.AGE)) {
                int age = Integer.parseInt(customMap.get(ATCustomRuleKeys.AGE).toString());
                if (age <= 13) {
                    isAgeLess13 = true;
                }
            }
        } catch (Throwable e) {

        }

        AppStrategy appStrategy = AppStrategyManager.getInstance(SDKContext.getInstance().getContext()).getAppStrategyByAppId(SDKContext.getInstance().getAppId());
        serviceExtras.put(Const.NETWORK_REQUEST_PARAMS_KEY.BID_PAYLOAD_KEY, unitGroupInfo.getPayload());
        serviceExtras.put(Const.NETWORK_REQUEST_PARAMS_KEY.APP_CCPA_SWITCH_KEY, appStrategy.getCcpaSwitch() == 3 ? true : false);
        serviceExtras.put(Const.NETWORK_REQUEST_PARAMS_KEY.APP_COPPA_SWITCH_KEY, (appStrategy.getCoppaSwitch() == 2 && isAgeLess13) ? true : false);

        if (Const.FORMAT.NATIVE_FORMAT.equals(String.valueOf(getFormat()))) {
            serviceExtras.put(Const.NETWORK_REQUEST_PARAMS_KEY.REQUEST_AD_NUM, unitGroupInfo.getUnitAdRequestNumber());
        }

//        if (unitGroupInfo.networkType == MyOfferAPIProxy.MYOFFER_NETWORK_FIRM_ID) {
//            MyOfferRequestInfo myOfferRequestInfo = new MyOfferRequestInfo();
//            myOfferRequestInfo.placementId = placementId;
//            myOfferRequestInfo.requestId = requestId;
//            myOfferRequestInfo.myOfferSetting = getMyOfferSetting();
//            if (myOfferRequestInfo.myOfferSetting != null) {
//                myOfferRequestInfo.myOfferSetting.setFormat(format);
//            }
//
//            serviceExtras.put(Const.NETWORK_REQUEST_PARAMS_KEY.MYOFFER_PARAMS_KEY, myOfferRequestInfo);
//        }

        if (unitGroupInfo.networkType == MyOfferAPIProxy.MYOFFER_NETWORK_FIRM_ID || unitGroupInfo.networkType == Const.NETWORK_FIRM.ADX_NETWORK_FIRM_ID ||
                unitGroupInfo.adsourceType == UnitGroupInfo.TYPE_ONLINE_API) { //Adx Network Firm Id
            BaseAdRequestInfo ownBaseAdRequestInfo = new BaseAdRequestInfo();
            ownBaseAdRequestInfo.bidId = unitGroupInfo.getPayload();
            ownBaseAdRequestInfo.networkFirmId = unitGroupInfo.networkType;
            ownBaseAdRequestInfo.adsourceId = unitGroupInfo.unitId;
            ownBaseAdRequestInfo.requestId = requestId;
            ownBaseAdRequestInfo.placementId = placementId;
            ownBaseAdRequestInfo.trafficGroupId = tracfficGroupId;
            ownBaseAdRequestInfo.groupId = groupId;
            ownBaseAdRequestInfo.networkName = unitGroupInfo.networkName;
            if (unitGroupInfo.networkType == MyOfferAPIProxy.MYOFFER_NETWORK_FIRM_ID) {
                ownBaseAdRequestInfo.baseAdSetting = getMyOfferSetting();
            } else {
                ownBaseAdRequestInfo.baseAdSetting = OwnBaseAdSetting.parseAdSetting(getAdxAdSettingStr());
            }
            if (ownBaseAdRequestInfo.baseAdSetting != null) {
                ownBaseAdRequestInfo.baseAdSetting.setFormat(format);
            }

            serviceExtras.put(Const.NETWORK_REQUEST_PARAMS_KEY.BASE_AD_PARAMS_KEY, ownBaseAdRequestInfo);
        }

//        if (unitGroupInfo.networkType == Const.NETWORK_FIRM.ADX_NETWORK_FIRM_ID) { //Adx Network Firm Id
//            AdxRequestInfo adxRequestInfo = new AdxRequestInfo();
//            adxRequestInfo.bidId = unitGroupInfo.getPayload();
//            adxRequestInfo.adsourceId = unitGroupInfo.unitId;
//            adxRequestInfo.groupId = groupId;
//            adxRequestInfo.placementId = placementId;
//            adxRequestInfo.requestId = requestId;
//            adxRequestInfo.trafficGroupId = tracfficGroupId;
//            adxRequestInfo.adxAdSetting = OwnBaseAdSetting.parseAdSetting(getAdxAdSettingStr());
//            if (adxRequestInfo.adxAdSetting != null) {
//                adxRequestInfo.adxAdSetting.setFormat(format);
//            }
//
//            serviceExtras.put(Const.NETWORK_REQUEST_PARAMS_KEY.ADX_PARAMS_KEY, adxRequestInfo);
//        }
//
//        if (unitGroupInfo.adsourceType == UnitGroupInfo.TYPE_ONLINE_API) {
//            OnlineApiRequestInfo onlineApiRequestInfo = new OnlineApiRequestInfo();
//            onlineApiRequestInfo.networkFirmId = unitGroupInfo.networkType;
//            onlineApiRequestInfo.adsourceId = unitGroupInfo.unitId;
//            onlineApiRequestInfo.requestId = requestId;
//            onlineApiRequestInfo.placementId = placementId;
//            onlineApiRequestInfo.trafficGroupId = tracfficGroupId;
//            onlineApiRequestInfo.groupId = groupId;
//            onlineApiRequestInfo.networkName = unitGroupInfo.networkName;
//            onlineApiRequestInfo.ownBaseAdSetting = OwnBaseAdSetting.parseAdSetting(getAdxAdSettingStr());
//            if (onlineApiRequestInfo.ownBaseAdSetting != null) {
//                onlineApiRequestInfo.ownBaseAdSetting.setFormat(format);
//            }
//            serviceExtras.put(Const.NETWORK_REQUEST_PARAMS_KEY.ONLINE_PARAMS_KEY, onlineApiRequestInfo);
//        }

        return serviceExtras;
    }

}
