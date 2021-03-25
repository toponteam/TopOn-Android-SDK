/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.entity;

/**
 * For Tracking Record
 */
public class UserOperateRecord {
    public String requestId;
    public String scenario;

    public int requestWidth; //Api Request Width
    public int requestHeight; //Api Request Height
    public int realWidth; //Ad Width;
    public int realHeight; //Ad Height


    public AdClickRecord adClickRecord;
    public VideoViewRecord videoViewRecord;
    public ConversionRecord conversionRecord;

    public UserOperateRecord(String requestId, String scenario) {
        this.requestId = requestId;
        this.scenario = scenario;
    }

}
