package com.anythink.network.gdt;

import com.anythink.nativead.unitgroup.api.CustomNativeAd;

interface GDTATNativeLoadListener {
    void notifyLoaded(CustomNativeAd... customNativeAds);

    void notifyError(String errorCode, String errorMsg);
}
