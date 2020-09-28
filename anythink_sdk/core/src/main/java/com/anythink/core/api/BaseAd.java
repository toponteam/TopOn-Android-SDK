package com.anythink.core.api;

import com.anythink.core.common.entity.AdTrackingInfo;

public abstract class BaseAd {

    /**
     * Tracking Info
     */
    public abstract void setTrackingInfo(AdTrackingInfo adTrackingInfo);

    public abstract AdTrackingInfo getDetail();
    /**
     * Your {@link BaseAd} subclass should implement this method if the network requires the developer
     * to destroy or cleanup their native ad when they are permanently finished with it.
     * <p>
     * This method is optional.
     */
    public abstract void destroy();
}
