/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.banner.business;

import android.content.Context;

import com.anythink.banner.api.ATBannerView;
import com.anythink.banner.unitgroup.api.CustomBannerAdapter;
import com.anythink.core.api.ATBaseAdAdapter;
import com.anythink.core.api.AdError;
import com.anythink.core.common.CommonMediationManager;

/**
 * Banner Mediation Manager
 */
public class MediationGroupManager extends CommonMediationManager {

    private ATBannerView mBannerView;
    InnerBannerListener mCallbackListener;

    protected MediationGroupManager(Context context) {
        super(context);
    }

    public void setCallbackListener(InnerBannerListener listener) {
        mCallbackListener = listener;
    }

    public void setATBannerView(ATBannerView bannerView) {
        this.mBannerView = bannerView;
    }

    @Override
    public void onDevelopLoaded() {
        if (mCallbackListener != null) {
            mCallbackListener.onBannerLoaded(mIsRefresh);
        }
    }

    @Override
    public void onDeveloLoadFail(final AdError adError) {
        if (mCallbackListener != null) {
            mCallbackListener.onBannerFailed(mIsRefresh, adError);
        }
    }

    @Override
    public void prepareFormatAdapter(ATBaseAdAdapter baseAdapter) {
        if (baseAdapter instanceof CustomBannerAdapter) {
            ((CustomBannerAdapter) baseAdapter).setATBannerView(mBannerView);
        }
    }


    @Override
    public void removeFormatCallback() {
        mCallbackListener = null;
    }


}
