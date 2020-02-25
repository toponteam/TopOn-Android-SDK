package com.anythink.nativead.unitgroup.api;

import com.anythink.core.api.AdError;

import java.util.List;

/**
 * Created by Z on 2018/1/9.
 */

public interface CustomNativeListener {
    void onNativeAdLoaded(CustomNativeAdapter adapter, List<CustomNativeAd> nativeAd);

    void onNativeAdFailed(CustomNativeAdapter adapter, AdError error);
}
