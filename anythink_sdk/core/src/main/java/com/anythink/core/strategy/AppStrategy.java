/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.strategy;

import com.anythink.core.api.ATSDK;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.base.UploadDataLevelManager;
import com.anythink.core.common.entity.DynamicUrlSettings;
import com.anythink.core.common.entity.NetworkInfoBean;
import com.anythink.core.common.net.AppStrategyLoader;
import com.anythink.core.common.utils.CommonLogUtil;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/***
 * @author zhou
 * 2017/12/29.
 */
public class AppStrategy {
    public static final String TAG = AppStrategy.class.getSimpleName();

    protected boolean isLocalStrategy;
    /***
     * Avail time
     */
    private long strategyOutTime;

    /***
     * req_ver
     */
    private String req_ver;
    /***
     * Update time
     */
    private long updateTime;

    /**
     * GDPR Level，0:PERSONALIZED ，1:NONPERSONALIZED
     */
    private int gdpr_sdcs;
    /**
     * GDPR switch
     */
    private int gdpr_so;
    /**
     * GDPR url
     */
    private String gdpr_nu;
    /**
     * Country code of EU
     */
    private String gdpr_a;
    /**
     * EU-Traffic, 1:true，0:false
     */
    private int gdpr_ia;

    /**
     * Splash Timeout
     *
     * @return
     */
    private long placementTimeOut;

    private String upId;

    /**
     * Logger Info
     */
    private String logger;

    /**
     * Tracking url
     **/
    private String tkAddress;
    /**
     * Tracking num
     **/
    private int tkMaxAmount;
    /**
     * Tracking interval
     **/
    private long tkInterval;
    /**
     * No Tracking Keys
     **/
    private int[] tkNoTrackingType;
    /**
     * Agent url
     **/
    private String daAddress;
    /**
     * Agent num
     **/
    private int daMaxAmount;
    /**
     * Agent interval
     **/
    private long daInterval;
    /**
     * No Agent Keys
     **/
    private String daNotKeys;
    /**
     * Instant Agent Keys
     **/
    private String daRealTimeKeys;

    /**
     * Psid avail time
     *
     * @return
     */
    private long psidTimeOut;

    /**
     * Myoffer cache size
     *
     * @return
     */
    private long offerCacheSize;

    /**
     * Use Default Mediation GDPR switch
     */
    private int useNetworkDefaultGDPR;

    /**
     * Notice Map
     */
    private Map noticeMap;

    /**
     * Pre-init Mediation's info
     */
    private String preinitStr;

    private String t_c;

    private String dataLevel;

    private String abTestId;

    private ConcurrentHashMap<String, NetworkInfoBean> tkInfoMap;

    private int recreatePsidIntervalWhenHotBoot;
    private int useCountDownSwitchAfterLeaveApp;

    private Map<String, String> daRtKeyFtMap;//da key -> format string array
    private Map<String, String> tkNoTFtMap;//tk key -> format string array
    private Map<String, String> daNotKeyFtMap;//da key -> format string array

    private int crashSwitch;

    private String crashList;

    private int tcpSwitchType;

    private String tcpDomain;

    private int tcpPort;

    private String tcpRate;

    private String systemId;

    private String bkId;

    private OfmStrategy ofmStrategy;

    Map<String, Object> appCustomMap;

    /**
     * v5.7.8
     */
    public OfmStrategy getOfmStrategy() {
        return ofmStrategy;
    }

    public void setOfmStrategy(OfmStrategy ofmStrategy) {
        this.ofmStrategy = ofmStrategy;
    }

    /**
     * v5.7.0 AdxSetting
     *
     * @return
     */
    private DynamicUrlSettings adxSetting;

    /**
     * v5.7.9
     * @return
     */
    private int ccpaSwitch;
    private int coppaSwitch;

    /**
     * v5.7.20
     */
    private String tkNoTrackingNetworkFirmId;
    private String daNoTrackingNetworkFirmId;

    public int getCcpaSwitch() {
        return ccpaSwitch;
    }

    public void setCcpaSwitch(int ccpaSwitch) {
        this.ccpaSwitch = ccpaSwitch;
    }

    public int getCoppaSwitch() {
        return coppaSwitch;
    }

    public void setCoppaSwitch(int coppaSwitch) {
        this.coppaSwitch = coppaSwitch;
    }

    public DynamicUrlSettings getDynamicUrlSettings() {
        return adxSetting;
    }

    public void setDynamicUrlSetting(DynamicUrlSettings adxSetting) {
        this.adxSetting = adxSetting;
    }

    public String getSystemId() {
        return systemId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    public String getBkId() {
        return bkId;
    }

    public void setBkId(String bkId) {
        this.bkId = bkId;
    }

    public String getTcpRate() {
        return tcpRate;
    }

    public void setTcpRate(String tcpRate) {
        this.tcpRate = tcpRate;
    }

    public int getTcpSwitchType() {
        return tcpSwitchType;
    }

    public void setTcpSwitchType(int tcpSwitchType) {
        this.tcpSwitchType = tcpSwitchType;
    }

    public String getTcpDomain() {
        return tcpDomain;
    }

    public void setTcpDomain(String tcpDomain) {
        this.tcpDomain = tcpDomain;
    }

    public int getTcpPort() {
        return tcpPort;
    }

    public void setTcpPort(int tcpPort) {
        this.tcpPort = tcpPort;
    }

    public int getCrashSwitch() {
        return crashSwitch;
    }

    public void setCrashSwitch(int crashSwitch) {
        this.crashSwitch = crashSwitch;
    }

    public String getCrashList() {
        return crashList;
    }

    public void setCrashList(String crashList) {
        this.crashList = crashList;
    }

    public String getAbTestId() {
        return abTestId;
    }

    public void setAbTestId(String abTestId) {
        this.abTestId = abTestId;
    }

    public int getRecreatePsidIntervalWhenHotBoot() {
        return recreatePsidIntervalWhenHotBoot;
    }

    public void setRecreatePsidIntervalWhenHotBoot(int recreatePsidIntervalWhenHotBoot) {
        this.recreatePsidIntervalWhenHotBoot = recreatePsidIntervalWhenHotBoot;
    }

    public int getUseCountDownSwitchAfterLeaveApp() {
        return useCountDownSwitchAfterLeaveApp;
    }

    public void setUseCountDownSwitchAfterLeaveApp(int useCountDownSwitchAfterLeaveApp) {
        this.useCountDownSwitchAfterLeaveApp = useCountDownSwitchAfterLeaveApp;
    }

    public String getDataLevel() {
        return dataLevel;
    }

    public void setDataLevel(String dataLevel) {
        this.dataLevel = dataLevel;
    }

    public boolean isLocalStrategy() {
        return isLocalStrategy;
    }

    public String getTC() {
        return t_c;
    }

    public void setTC(String tc) {
        this.t_c = tc;
    }

    public ConcurrentHashMap<String, NetworkInfoBean> getTkInfoMap() {
        return tkInfoMap;
    }

    public void setTkInfoMap(ConcurrentHashMap<String, NetworkInfoBean> tkInfoMap) {
        this.tkInfoMap = tkInfoMap;
    }


    public int getUseNetworkDefaultGDPR() {
        return useNetworkDefaultGDPR;
    }

    public void setUseNetworkDefaultGDPR(int useNetworkDefaultGDPR) {
        this.useNetworkDefaultGDPR = useNetworkDefaultGDPR;
    }

    public long getOfferCacheSize() {
        return offerCacheSize;
    }

    public void setOfferCacheSize(long offerCacheSize) {
        this.offerCacheSize = offerCacheSize;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public Map<String, String> getNoticeMap() {
        return noticeMap;
    }

    public void setNoticeMap(Map noticeMap) {
        this.noticeMap = noticeMap;
    }

    public String getPreinitStr() {
        return preinitStr;
    }

    public void setPreinitStr(String preinitStr) {
        this.preinitStr = preinitStr;
    }

    static class ResponseKey {
        private static String strategyOutTime_key = "scet";
        private static String req_ver_key = "req_ver";

        private static String gdpr_sdcs = "gdpr_sdcs";
        private static String gdpr_so = "gdpr_so";
        private static String gdpr_nu = "gdpr_nu";
        private static String gdpr_a = "gdpr_a";
        private static String gdpr_ia = "gdpr_ia";
        private static String pl_n = "pl_n";

        private static String upid = "upid";

        private static String logger = "logger";
        private static String tk_address = "tk_address";
        private static String tk_max_amount = "tk_max_amount";
        private static String tk_interval = "tk_interval";
        private static String da_address = "da_address";
        private static String da_max_amount = "da_max_amount";
        private static String da_interval = "da_interval";

        private static String new_psid_time = "n_psid_tm";

        private static String myoffer_cache_size = "c_a";

        private static String tk_firm = "tk_firm";

        private static String n_l = "n_l";

        private static String preinit = "preinit";

        private static String nw_eu_def = "nw_eu_def";

        private static String t_c = "t_c";

        private static String data_level = "data_level";

        private static String psid_hl = "psid_hl";
        private static String la_sw = "la_sw";


        /**
         * v5.5.5
         */
        private static String da_rt_keys_ft = "da_rt_keys_ft";
        private static String tk_no_t_ft = "tk_no_t_ft";
        private static String da_not_keys_ft = "da_not_keys_ft";

        /**
         * 5.6.1
         */
        private static String ab_test_id = "abtest_id";

        /**
         * 5.6.2
         */
        private static String crash_switch = "crash_sw";
        private static String crash_list = "crash_list";

        /**
         * 5.6.3
         */
        private static String tcp_domain = "tcp_domain";
        private static String tcp_port = "tcp_port";
        private static String tcp_tk_da_type = "tcp_tk_da_type";
        private static String tcp_rate = "tcp_rate";

        /**
         * 5.6.6
         */
        private static String system_id = "sy_id";

        /**
         * 5.7.0 AdxSetting
         */
        private static String adx_setting_key = "adx";
        private static String adx_req_addr_key = "req_addr";
        private static String adx_bid_addr_key = "bid_addr";
        private static String adx_tk_addr_key = "tk_addr";

        /**
         * 5.7.3 Online Api
         */
        private static String online_req_addr_key = "ol_req_addr";

        /**
         * v5.7.8
         */
        private static String ofm_data = "ofm_data";

        /**
         * 5.7.9
         */
        private static String ccpa_switch_key = "ccpa_sw";
        private static String coppa_switch_key = "coppa_sw";


        /**
         * 5.7.20
         */
        private static String tk_no_nt_t = "tk_no_nt_t";
        private static String da_no_nt_k = "da_no_nt_k";


    }



    public long getPsidTimeOut() {
        return psidTimeOut;
    }

    public void setPsidTimeOut(long psidTimeOut) {
        this.psidTimeOut = psidTimeOut;
    }

//    public String getUpId() {
//        return upId;
//    }

//    public void setUpId(String upId) {
//        this.upId = upId;
//    }

    public long getStrategyOutTime() {
        return strategyOutTime;
    }

    public void setStrategyOutTime(long strategyOutTime) {
        this.strategyOutTime = strategyOutTime;
    }


    public String getReq_ver() {
        return req_ver;
    }

    public void setReq_ver(String req_ver) {
        this.req_ver = req_ver;
    }


    public int getGdprSdcs() {
        return gdpr_sdcs;
    }

    public void setGdprSdcs(int gdpr_sdcs) {
        this.gdpr_sdcs = gdpr_sdcs;
    }

    public int getGdprSo() {
        return gdpr_so;
    }

    public void setGdprSo(int gdpr_so) {
        this.gdpr_so = gdpr_so;
    }

    public String getGdprNu() {
        return gdpr_nu;
    }

    public void setGdprNu(String gdpr_nu) {
        this.gdpr_nu = gdpr_nu;
    }

    public String getGdprA() {
        return gdpr_a;
    }

    public void setGdprA(String gdpr_a) {
        this.gdpr_a = gdpr_a;
    }

    public int getGdprIa() {
        return gdpr_ia;
    }

    public void setGdprIa(int gdpr_ia) {
        this.gdpr_ia = gdpr_ia;
    }


    public long getPlacementTimeOut() {
        return placementTimeOut;
    }

    public void setPlacementTimeOut(long splashTimeOut) {
        this.placementTimeOut = splashTimeOut;
    }

    public void setLogger(String logger) {
        this.logger = logger;
    }

    public String getLogger() {
        return this.logger;
    }

    public String getTkAddress() {
        return tkAddress;
    }

    public void setTkAddress(String tkAddress) {
        this.tkAddress = tkAddress;
    }

    public int getTkMaxAmount() {
        return tkMaxAmount;
    }

    public void setTkMaxAmount(int tkMaxAmount) {
        this.tkMaxAmount = tkMaxAmount;
    }

    public long getTkInterval() {
        return tkInterval;
    }

    public void setTkInterval(long tkInterval) {
        this.tkInterval = tkInterval;
    }

    public String getDaAddress() {
        return daAddress;
    }

    public void setDaAddress(String daAddress) {
        this.daAddress = daAddress;
    }

    public int getDaMaxAmount() {
        return daMaxAmount;
    }

    public void setDaMaxAmount(int daMaxAmount) {
        this.daMaxAmount = daMaxAmount;
    }

    public long getDaInterval() {
        return daInterval;
    }

    public void setDaInterval(long daInterval) {
        this.daInterval = daInterval;
    }

    public Map<String, String> getDaRtKeyFtMap() {
        return daRtKeyFtMap;
    }

    public void setDaRtKeyFtMap(Map<String, String> daRtKeyFtMap) {
        this.daRtKeyFtMap = daRtKeyFtMap;
    }

    public Map<String, String> getTkNoTFtMap() {
        return tkNoTFtMap;
    }

    public void setTkNoTFtMap(Map<String, String> tkNoTFtMap) {
        this.tkNoTFtMap = tkNoTFtMap;
    }

    public Map<String, String> getDaNotKeyFtMap() {
        return daNotKeyFtMap;
    }

    public void setDaNotKeyFtMap(Map<String, String> daNotKeyFtMap) {
        this.daNotKeyFtMap = daNotKeyFtMap;
    }

    public Map<String, Object> getAppCustomMap() {
        return appCustomMap;
    }

    public void setAppCustomMap(Map<String, Object> appCustomMap) {
        this.appCustomMap = appCustomMap;
    }

    public void setTkNoTrackingNetworkFirmId(String tk_no_nt_type) {
        this.tkNoTrackingNetworkFirmId = tk_no_nt_type;
    }

    public String getTkNoTrackingNetworkFirmId() {
        return tkNoTrackingNetworkFirmId;
    }

    public void setDaNoTrackingNetworkFirmId(String da_no_nt_key) {
        this.daNoTrackingNetworkFirmId = da_no_nt_key;
    }

    public String getDaNoTrackingNetworkFirmId() {
        return daNoTrackingNetworkFirmId;
    }


    /***
     * Parse AppSetting' Json String
     * @param jsonStr
     * @return
     */
    public static AppStrategy parseStrategy(String jsonStr) {
        if (jsonStr == null || "".equals(jsonStr)) {
            return null;
        }
        CommonLogUtil.i(TAG, jsonStr);
        AppStrategy strategy = new AppStrategy();
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);

            if (jsonObject.isNull(ResponseKey.req_ver_key)) {
                strategy.setReq_ver("unkown");
            } else {
                strategy.setReq_ver(jsonObject.optString(ResponseKey.req_ver_key));
            }

            if (jsonObject.isNull(ResponseKey.strategyOutTime_key)) {
                strategy.setStrategyOutTime(Const.DEFAULT_SDK_KEY.APPSTRATEGY_DEFAULT_OUTTIME);
            } else {
                strategy.setStrategyOutTime(jsonObject.optLong(ResponseKey.strategyOutTime_key));
            }

            if (jsonObject.isNull(ResponseKey.gdpr_sdcs)) {
                strategy.setGdprSdcs(0);
            } else {
                strategy.setGdprSdcs(jsonObject.optInt(ResponseKey.gdpr_sdcs));

            }

            if (jsonObject.isNull(ResponseKey.gdpr_so)) {
                strategy.setGdprSo(0);
            } else {
                strategy.setGdprSo(jsonObject.optInt(ResponseKey.gdpr_so));
            }

            if (jsonObject.isNull(ResponseKey.gdpr_nu)) {
                strategy.setGdprNu("");
            } else {
                strategy.setGdprNu(jsonObject.optString(ResponseKey.gdpr_nu));

            }

            if (jsonObject.isNull(ResponseKey.gdpr_a)) {
                strategy.setGdprA("[\"AT\",\"BE\",\"BG\",\"HR\",\"CY\",\"CZ\",\"DK\",\"EE\",\"FI\",\"FR\",\"DE\",\"GR\",\"HU\",\"IS\",\"IE\",\"IT\",\"LV\",\"LI\",\"LT\",\"LU\",\"MT\",\"NL\",\"NO\",\"PL\",\"PT\",\"RO\",\"SK\",\"SI\",\"ES\",\"SE\",\"GB\",\"UK\"]");
            } else {
                strategy.setGdprA(jsonObject.optString(ResponseKey.gdpr_a));
            }

            if (jsonObject.isNull(ResponseKey.gdpr_ia)) {
                strategy.setGdprIa(0);
            } else {
                strategy.setGdprIa(jsonObject.optInt(ResponseKey.gdpr_ia));

            }


            if (jsonObject.isNull(ResponseKey.pl_n)) {
                strategy.setPlacementTimeOut(5000L);
            } else {
                strategy.setPlacementTimeOut(jsonObject.optLong(ResponseKey.pl_n));
            }


            if (!jsonObject.isNull(ResponseKey.logger)) {
                JSONObject loggerObject = jsonObject.optJSONObject(ResponseKey.logger);
                strategy.setLogger(loggerObject.toString());

                strategy.setTkAddress(loggerObject.optString(ResponseKey.tk_address));
                strategy.setTkMaxAmount(loggerObject.optInt(ResponseKey.tk_max_amount));
                strategy.setTkInterval(loggerObject.optLong(ResponseKey.tk_interval));

                strategy.setDaAddress(loggerObject.optString(ResponseKey.da_address));
                strategy.setDaMaxAmount(loggerObject.optInt(ResponseKey.da_max_amount));
                strategy.setDaInterval(loggerObject.optLong(ResponseKey.da_interval));

                ConcurrentHashMap tempMap = new ConcurrentHashMap<>();
                try {
                    if (!loggerObject.isNull(ResponseKey.tk_firm)) {
                        JSONObject tkFirmObject = new JSONObject(loggerObject.optString(ResponseKey.tk_firm));
                        Iterator<String> keyIterator = tkFirmObject.keys();
                        while (keyIterator.hasNext()) {
                            String key = keyIterator.next();
                            NetworkInfoBean networkInfoBean = new NetworkInfoBean();
                            JSONObject itemObject = tkFirmObject.optJSONObject(key);
                            networkInfoBean.tkLoadSwitch = itemObject.optInt("tk_fi_re_sw");
                            networkInfoBean.tkImpressionSwitch = itemObject.optInt("tk_im_sw");
                            networkInfoBean.tkShowSwtich = itemObject.optInt("tk_sh_sw");
                            networkInfoBean.tkClickSwitch = itemObject.optInt("tk_ck_sw");
                            networkInfoBean.detailInfo = itemObject.optString("pg_m_li");
                            tempMap.put(key, networkInfoBean);
                        }
                    }
                } catch (Exception e) {

                }
                strategy.setTkInfoMap(tempMap);


                /** v5.5.5**/
                if (loggerObject.isNull(ResponseKey.da_rt_keys_ft)) {
                    strategy.setDaRtKeyFtMap(null);
                } else {
                    try {
                        JSONObject object = new JSONObject(loggerObject.optString(ResponseKey.da_rt_keys_ft));

                        Iterator<String> keys = object.keys();
                        Map<String, String> map = new HashMap<>();
                        String key;
                        while (keys.hasNext()) {
                            key = keys.next();
                            map.put(key, object.optString(key));
                        }
                        strategy.setDaRtKeyFtMap(map);
                    } catch (Throwable e) {
                        strategy.setDaRtKeyFtMap(null);
                    }
                }

                if (loggerObject.isNull(ResponseKey.da_not_keys_ft)) {
                    strategy.setDaNotKeyFtMap(null);
                } else {
                    try {
                        JSONObject object = new JSONObject(loggerObject.optString(ResponseKey.da_not_keys_ft));

                        Iterator<String> keys = object.keys();
                        Map<String, String> map = new HashMap<>();
                        String key;
                        while (keys.hasNext()) {
                            key = keys.next();
                            map.put(key, object.optString(key));
                        }

                        strategy.setDaNotKeyFtMap(map);
                    } catch (Throwable e) {
                        strategy.setDaNotKeyFtMap(null);
                    }
                }

                if (loggerObject.isNull(ResponseKey.tk_no_t_ft)) {
                    strategy.setTkNoTFtMap(null);
                } else {
                    try {
                        JSONObject object = new JSONObject(loggerObject.optString(ResponseKey.tk_no_t_ft));

                        Iterator<String> keys = object.keys();
                        Map<String, String> map = new HashMap<>();
                        String key;
                        while (keys.hasNext()) {
                            key = keys.next();
                            map.put(key, object.optString(key));
                        }

                        strategy.setTkNoTFtMap(map);
                    } catch (Throwable e) {
                        strategy.setTkNoTFtMap(null);
                    }
                }


                /**
                 * v5.6.3
                 */
                strategy.setTcpDomain(loggerObject.optString(ResponseKey.tcp_domain));
                strategy.setTcpPort(loggerObject.optInt(ResponseKey.tcp_port));
                strategy.setTcpSwitchType(loggerObject.optInt(ResponseKey.tcp_tk_da_type));
                strategy.setTcpRate(loggerObject.optString(ResponseKey.tcp_rate));

                /**
                 * v5.7.20
                 */
                if (loggerObject.isNull(ResponseKey.tk_no_nt_t)) {
                    strategy.setTkNoTrackingNetworkFirmId(null);
                } else {
                    strategy.setTkNoTrackingNetworkFirmId(loggerObject.optString(ResponseKey.tk_no_nt_t));
                }
                if (loggerObject.isNull(ResponseKey.da_no_nt_k)) {
                    strategy.setDaNoTrackingNetworkFirmId(null);
                } else {
                    strategy.setDaNoTrackingNetworkFirmId(loggerObject.optString(ResponseKey.da_no_nt_k));
                }
            }

            if (!jsonObject.isNull(ResponseKey.new_psid_time)) {
                strategy.setPsidTimeOut(jsonObject.optLong(ResponseKey.new_psid_time));
            }

            if (!jsonObject.isNull(ResponseKey.myoffer_cache_size)) {
                strategy.setOfferCacheSize(jsonObject.optLong(ResponseKey.myoffer_cache_size));
            }

            /**
             * v5.4.0
             */
            if (!jsonObject.isNull(ResponseKey.n_l)) {
                String n_l_json = jsonObject.optString(ResponseKey.n_l);
                JSONObject n_l_json_object = new JSONObject(n_l_json);
                Iterator<String> keyIterator = n_l_json_object.keys();
                String key;
                Map<String, String> noticeMap = new HashMap<>();
                while (keyIterator.hasNext()) {
                    key = keyIterator.next();
                    noticeMap.put(key, n_l_json_object.optString(key));
                }
                strategy.setNoticeMap(noticeMap);
            }

            if (!jsonObject.isNull(ResponseKey.t_c)) {
                String tc_json = jsonObject.optString(ResponseKey.t_c);
                strategy.setTC(tc_json);
            }


            /** v5.4.1 */
            if (!jsonObject.isNull(ResponseKey.preinit)) {
                strategy.setPreinitStr(jsonObject.optString(ResponseKey.preinit));
            }

            if (!jsonObject.isNull(ResponseKey.nw_eu_def)) {
                strategy.setUseNetworkDefaultGDPR(jsonObject.optInt(ResponseKey.nw_eu_def));
            }

            /** v5.5.1**/
            if (!jsonObject.isNull(ResponseKey.data_level)) {
                strategy.setDataLevel(jsonObject.optString(ResponseKey.data_level));
            }


            /** v5.5.4**/
            if (jsonObject.isNull(ResponseKey.psid_hl)) {
                strategy.setRecreatePsidIntervalWhenHotBoot(60000);
            } else {
                strategy.setRecreatePsidIntervalWhenHotBoot(jsonObject.optInt(ResponseKey.psid_hl));
            }
            if (jsonObject.isNull(ResponseKey.la_sw)) {
                strategy.setUseCountDownSwitchAfterLeaveApp(0);
            } else {
                strategy.setUseCountDownSwitchAfterLeaveApp(jsonObject.optInt(ResponseKey.la_sw));
            }


            /**v5.6.1**/
            if (jsonObject.isNull(ResponseKey.ab_test_id)) {
                strategy.setAbTestId("");
            } else {
                strategy.setAbTestId(jsonObject.optString(ResponseKey.ab_test_id));
            }

            /**
             * v5.6.2
             */
            if (jsonObject.isNull(ResponseKey.crash_switch)) {
                strategy.setCrashSwitch(1);
            } else {
                strategy.setCrashSwitch(jsonObject.optInt(ResponseKey.crash_switch));
            }

            if (jsonObject.isNull(ResponseKey.crash_list)) {
                strategy.setCrashList("");
            } else {
                strategy.setCrashList(jsonObject.optString(ResponseKey.crash_list));
            }

            /**
             * v5.6.5
             */
            if (jsonObject.isNull(ResponseKey.system_id)) {
                strategy.setSystemId("");
            } else {
                strategy.setSystemId(jsonObject.optString(ResponseKey.system_id));
            }

            /**
             * v5.7.0
             */
            if (jsonObject.isNull(ResponseKey.adx_setting_key)) {
                strategy.setDynamicUrlSetting(null);
            } else {
                DynamicUrlSettings adxSetting = new DynamicUrlSettings();
                JSONObject adxSettingObject = jsonObject.optJSONObject(ResponseKey.adx_setting_key);

                adxSetting.setAdxRequestHttpUrl(adxSettingObject.optString(ResponseKey.adx_req_addr_key));
                adxSetting.setAdxBidRequestHttpUrl(adxSettingObject.optString(ResponseKey.adx_bid_addr_key));
                adxSetting.setAdxTrackRequestHttpUrl(adxSettingObject.optString(ResponseKey.adx_tk_addr_key));

                /**
                 * v5.7.3
                 */
                adxSetting.setOnlineApiRequestHttpUrl(adxSettingObject.optString(ResponseKey.online_req_addr_key));
                strategy.setDynamicUrlSetting(adxSetting);
            }

            /**
             * v.5.7.8
             */
            strategy.setOfmStrategy(OfmStrategy.parseOfmStrategy(jsonObject.optString(ResponseKey.ofm_data)));


            /**5.7.8 local custom map**/
            if (jsonObject.isNull(AppStrategyLoader.CUSTOM_KEY)) {
                strategy.setAppCustomMap(null);
            } else {
                JSONObject customObject = new JSONObject(jsonObject.optString(AppStrategyLoader.CUSTOM_KEY));
                HashMap<String, Object> map = new HashMap<>();
                Iterator<String> jsonKeyIterator = customObject.keys();
                while (jsonKeyIterator.hasNext()) {
                    String key = jsonKeyIterator.next();
                    map.put(key, customObject.opt(key));
                }
                strategy.setAppCustomMap(map);
            }
            /**
             * v5.7.9
             */
            strategy.setCcpaSwitch(jsonObject.optInt(ResponseKey.ccpa_switch_key));
            strategy.setCoppaSwitch(jsonObject.optInt(ResponseKey.coppa_switch_key));

        } catch (Exception e) {
            if (Const.DEBUG) {
                e.printStackTrace();
            }

        }

        return strategy;

    }


    /**
     * GDPR Setting
     *
     * @param serviceExtras
     */
    public static void fillGdprData(Map<String, Object> serviceExtras) {
        AppStrategy appStrategy = AppStrategyManager.getInstance(SDKContext.getInstance().getContext()).getAppStrategyByAppId(SDKContext.getInstance().getAppId());
        boolean isEU = appStrategy != null && appStrategy.getGdprIa() == 1;
        boolean isUseNetworkDefaultGDPR = appStrategy != null && appStrategy.getUseNetworkDefaultGDPR() == 1;
        UploadDataLevelManager uploadDataLevelManager = UploadDataLevelManager.getInstance(SDKContext.getInstance().getContext());
        serviceExtras.put("gdpr_consent", uploadDataLevelManager.isNetworkGDPRConsent());
        serviceExtras.put("is_eu", isEU);

        boolean needSetNetworkGDPR = false;

        if (appStrategy.isLocalStrategy()) {
            /**level=UNKNOWN (Use Mediation's default GDPR setting）**/
            needSetNetworkGDPR = uploadDataLevelManager.getUploadDataLevel() != ATSDK.UNKNOWN;
        } else {
            if (uploadDataLevelManager.getUploadDataLevel() == ATSDK.UNKNOWN) { /**UNKNOWNN**/
                if (appStrategy.getGdprIa() == 0) { /**Not EU-Traffic**/
                    needSetNetworkGDPR = false;
                } else { /**EU-Traffic**/
                    if (isUseNetworkDefaultGDPR) {
                        /**Set by Service Setting**/
                        needSetNetworkGDPR = false;
                    } else {
                        needSetNetworkGDPR = true;
                    }
                }
            } else { /**Not UNKNOW**/
                needSetNetworkGDPR = true;
            }
        }

        //Status of Setting Mediation GDPR config
        serviceExtras.put("need_set_gdpr", needSetNetworkGDPR);
    }


    public static boolean needToSetNetworkGDPR() {
        AppStrategy appStrategy = AppStrategyManager.getInstance(SDKContext.getInstance().getContext()).getAppStrategyByAppId(SDKContext.getInstance().getAppId());
        boolean isEU = appStrategy != null && appStrategy.getGdprIa() == 1;
        boolean isUseNetworkDefaultGDPR = appStrategy != null && appStrategy.getUseNetworkDefaultGDPR() == 1;
        UploadDataLevelManager uploadDataLevelManager = UploadDataLevelManager.getInstance(SDKContext.getInstance().getContext());

        boolean needSetNetworkGDPR = false;

        if (appStrategy.isLocalStrategy()) {
            /**level=UNKNOWN (Use Mediation's default GDPR setting）**/
            needSetNetworkGDPR = uploadDataLevelManager.getUploadDataLevel() != ATSDK.UNKNOWN;
        } else {
            if (uploadDataLevelManager.getUploadDataLevel() == ATSDK.UNKNOWN) { /**UNKNOWNN**/
                if (appStrategy.getGdprIa() == 0) { /**Not EU-Traffic**/
                    needSetNetworkGDPR = false;
                } else { /**EU-Traffic**/
                    if (isUseNetworkDefaultGDPR) {
                        /**Set by Service Setting**/
                        needSetNetworkGDPR = false;
                    } else {
                        needSetNetworkGDPR = true;
                    }
                }
            } else { /**Not UNKNOW**/
                needSetNetworkGDPR = true;
            }
        }

        //Status of Setting Mediation GDPR config
        return needSetNetworkGDPR;
    }

}
