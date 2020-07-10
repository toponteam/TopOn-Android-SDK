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
