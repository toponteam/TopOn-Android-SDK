package com.anythink.nativead.unitgroup.api;

import android.content.Context;

import com.anythink.core.common.base.AnyThinkBaseAdapter;

import java.util.Map;

/**
 * Created by Z on 2018/1/9.
 */

public abstract class CustomNativeAdapter extends AnyThinkBaseAdapter {

    public abstract void loadNativeAd(final Context context
            , final CustomNativeListener customNativeListener
            , final Map<String, Object> serverExtras
            , final Map<String, Object> localExtras
    );

    @Override
    public boolean isAdReady() {
        return false;
    }

}
