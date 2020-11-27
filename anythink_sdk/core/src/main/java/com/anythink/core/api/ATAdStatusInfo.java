/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.api;

public class ATAdStatusInfo {
    private boolean mIsLoading;
    private boolean mIsReady;
    private ATAdInfo mATTopAdInfo;

    public ATAdStatusInfo(boolean isLoading, boolean isReady, ATAdInfo atAdInfo){
        mIsLoading = isLoading;
        mIsReady = isReady;
        mATTopAdInfo = atAdInfo;
    }

    public boolean isLoading() {
        return mIsLoading;
    }

    public boolean isReady() {
        return mIsReady;
    }

    public ATAdInfo getATTopAdInfo() {
        return mATTopAdInfo;
    }

    @Override
    public String toString() {
        return "ATAdStatusInfo{" +
                "isLoading=" + mIsLoading +
                ", isReady=" + mIsReady +
                ", topAdInfo=" + (mATTopAdInfo != null ? mATTopAdInfo : "null") +
                '}';
    }
}
