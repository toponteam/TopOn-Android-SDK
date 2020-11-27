/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.listeners;

import com.anythink.basead.entity.OfferError;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class AdEventMessager {

    public static final String TAG = AdEventMessager.class.getSimpleName();

    private AdEventMessager() {
        mEventMap = new HashMap<>(2);
    }

    public static AdEventMessager getInstance() {
        return Holder.sInstance;
    }

    private static class Holder {
        private static final AdEventMessager sInstance = new AdEventMessager();
    }

    private Map<String, OnEventListener> mEventMap;

    public void setListener(String key, OnEventListener listener) {
        mEventMap.put(key,listener);
    }

    public OnEventListener getListener(String key) {
        return mEventMap.get(key);
    }

    public void unRegister(String key) {
        mEventMap.remove(key);
    }


    public interface OnEventListener extends Serializable {
        void onShow();
        void onVideoShowFailed(OfferError error);
        void onVideoPlayStart();
        void onVideoPlayEnd();
        void onReward();
        void onClose();
        void onClick();
    }
}
