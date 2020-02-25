package com.anythink.myoffer.network.base;

import com.anythink.network.myoffer.MyOfferError;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class MyOfferAdMessager {

    public static final String TAG = MyOfferAdMessager.class.getSimpleName();

    private MyOfferAdMessager() {
        mEventMap = new HashMap<>(2);
    }

    public static MyOfferAdMessager getInstance() {
        return Holder.sInstance;
    }

    private static class Holder {
        private static final MyOfferAdMessager sInstance = new MyOfferAdMessager();
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
        void onVideoShowFailed(MyOfferError error);
        void onVideoPlayStart();
        void onVideoPlayEnd();
        void onReward();
        void onClose();
        void onClick();
    }
}
