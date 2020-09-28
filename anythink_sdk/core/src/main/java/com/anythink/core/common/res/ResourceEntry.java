package com.anythink.core.common.res;

public class ResourceEntry {

    public final static int INTERNAL_CACHE_TYPE = 1;
    public final static int CUSTOM_IMAGE_CACHE_TYPE = 2;

    public ResourceEntry(int resourceType, String resourceUrl) {
        this.resourceType = resourceType;
        this.resourceUrl = resourceUrl;
    }

    public int resourceType;
    public String resourceUrl;
}
