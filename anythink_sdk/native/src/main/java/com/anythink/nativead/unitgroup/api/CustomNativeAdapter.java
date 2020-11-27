/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

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
