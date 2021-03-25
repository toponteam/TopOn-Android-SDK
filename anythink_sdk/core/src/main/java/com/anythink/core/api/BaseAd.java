/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.api;

import com.anythink.core.common.entity.AdTrackingInfo;

import java.util.Map;

public abstract class BaseAd {

    /**
     * Tracking Info
     */
    public abstract void setTrackingInfo(AdTrackingInfo adTrackingInfo);

    public abstract AdTrackingInfo getDetail();

    public abstract void setNetworkInfoMap(Map<String, Object> networkInfoMap);

    public abstract Map<String, Object> getNetworkInfoMap();

    /**
     * Your {@link BaseAd} subclass should implement this method if the network requires the developer
     * to destroy or cleanup their native ad when they are permanently finished with it.
     * <p>
     * This method is optional.
     */
    public abstract void destroy();
}
