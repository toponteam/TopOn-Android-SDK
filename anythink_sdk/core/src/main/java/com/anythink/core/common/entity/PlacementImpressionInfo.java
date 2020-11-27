/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.common.entity;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class PlacementImpressionInfo {

    public int format;
    public String placementId;
    public int dayShowCount;
    public int hourShowCount;

    public long showTime;

    public ConcurrentHashMap<String, AdSourceImpressionInfo> adSourceImpressionInfos;

    public static class AdSourceImpressionInfo {
        public String unitId;
        public String hourTimeFormat;
        public String dateTimeFormat;

        public int dayShowCount;
        public int hourShowCount;

        public long showTime;

    }

    public AdSourceImpressionInfo getAdSourceImpressInfo(String adsourceId) {
        if (adSourceImpressionInfos != null) {
            return adSourceImpressionInfos.get(adsourceId);
        }
        return null;
    }

}
