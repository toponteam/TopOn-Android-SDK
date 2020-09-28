package com.anythink.nativead.unitgroup.api;


import com.anythink.core.api.ATBaseAdAdapter;

/**
 * Created by Z on 2018/1/9.
 */

public abstract class CustomNativeAdapter extends ATBaseAdAdapter {

    @Override
    final public boolean isAdReady() {
        return false;
    }

}
