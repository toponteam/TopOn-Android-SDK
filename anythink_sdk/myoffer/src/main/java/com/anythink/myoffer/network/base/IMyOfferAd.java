package com.anythink.myoffer.network.base;

import java.util.Map;

public interface IMyOfferAd {

    void load();
    void show(Map<String, Object> extraMap);
    boolean isReady();
}
