package com.anythink.core.common.entity;

public abstract class BaseAd {

    /**
     * Your {@link BaseAd} subclass should implement this method if the network requires the developer
     * to destroy or cleanup their native ad when they are permanently finished with it.
     * <p>
     * This method is optional.
     */
    public abstract void destroy();
}
