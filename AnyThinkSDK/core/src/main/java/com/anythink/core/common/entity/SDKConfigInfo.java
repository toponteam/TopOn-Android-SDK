package com.anythink.core.common.entity;

/**
 * SKD Config
 * Created by zhou on 2017/12/29.
 */

public class SDKConfigInfo {
    public static class TYPE {
        public static String TYPE_CAP = "5";
    }

    /**
     * AppSetting key: appid
     * Cap key:{palceid}_{unitgroupid}_{day|hour}
     */

    private String key;
    /**
     * type：
     * 5:cap
     */
    private String type;
    /**
     * Data content
     */
    private String value;


    /**
     * Daily format: YYYYMMDD
     * Hourly format: YYYYMMDDHH
     */
    private String updatetime;

    public String getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(String updatetime) {
        this.updatetime = updatetime;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
