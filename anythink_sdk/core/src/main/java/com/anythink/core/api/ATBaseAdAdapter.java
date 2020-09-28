package com.anythink.core.api;

import android.content.Context;

import com.anythink.core.common.base.AnyThinkBaseAdapter;

import java.util.Map;

public abstract class ATBaseAdAdapter extends AnyThinkBaseAdapter {
    protected ATCustomLoadListener mLoadListener;

    protected String mUserId = "";
    protected String mUserData = "";

    //Custom Network request Ad
    public abstract void loadCustomNetworkAd(final Context context, final Map<String, Object> serverExtra, final Map<String, Object> localExtra);

    // Release Custom Adapter Resource
    public abstract void destory();

    //Obtain Network Placement Id
    public abstract String getNetworkPlacementId();

    //Obtain Network version
    public abstract String getNetworkSDKVersion();

    //Obtain Network name
    public abstract String getNetworkName();

    //Obtain Network Ready status
    public abstract boolean isAdReady();

    //GDPR Setting（true: Agree to collect user data, false: Disagree to collect user data）
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        return false;
    }

    public boolean initNetworkObjectByPlacementId(final Context context, final Map<String, Object> serverExtras, final Map<String, Object> localExtra) {
        return false;
    }

    public BaseAd getBaseAdObject(Context context) {
        return null;
    }

    //Internal init Network SDK and init Ad Format Object
    final public boolean internalInitNetworkObjectByPlacementId(final Context context, final Map<String, Object> serverExtras, final Map<String, Object> localExtra) {
        if (localExtra != null) {
            mUserId = localExtra.get(ATAdConst.KEY.USER_ID) != null ? localExtra.get(ATAdConst.KEY.USER_ID).toString() : "";
            mUserData = localExtra.get(ATAdConst.KEY.USER_CUSTOM_DATA) != null ? localExtra.get(ATAdConst.KEY.USER_CUSTOM_DATA).toString() : "";
        }
        return initNetworkObjectByPlacementId(context, serverExtras, localExtra);
    }

    //Clean Custom Listener
    private void cleanLoadListener() {
        mLoadListener = null;
    }

    final public void internalLoad(final Context context, final Map<String, Object> serverExtra, final Map<String, Object> localExtra, ATCustomLoadListener loadListener) {
        mLoadListener = loadListener;
        if (localExtra != null) {
            mUserId = localExtra.get(ATAdConst.KEY.USER_ID) != null ? localExtra.get(ATAdConst.KEY.USER_ID).toString() : "";
            mUserData = localExtra.get(ATAdConst.KEY.USER_CUSTOM_DATA) != null ? localExtra.get(ATAdConst.KEY.USER_CUSTOM_DATA).toString() : "";
        }
        loadCustomNetworkAd(context, serverExtra, localExtra);
    }

    public void releaseLoadResource() {
        cleanLoadListener();
    }
}
