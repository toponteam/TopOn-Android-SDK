package com.anythink.rewardvideo.bussiness;

import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.ATBaseAdAdapter;
import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.AdError;
import com.anythink.core.common.CommonMediationManager;
import com.anythink.core.common.base.AnyThinkBaseAdapter;
import com.anythink.core.strategy.PlaceStrategy;
import com.anythink.rewardvideo.api.ATRewardVideoListener;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;

import java.util.Map;

/**
 * RewardedVideo Mediation Manager
 */
public class MediationGroupManager extends CommonMediationManager {
    ATRewardVideoListener mCallbackListener;

    protected MediationGroupManager(Context context) {
        super(context);
    }


    @Override
    public void prepareFormatAdapter(ATBaseAdAdapter baseAdapter) {
    }

    public void setCallbackListener(ATRewardVideoListener listener) {
        mCallbackListener = listener;
    }


    @Override
    public void onDevelopLoaded() {
        if (mIsRefresh) {
            return;
        }
        if (mCallbackListener != null) {
            mCallbackListener.onRewardedVideoAdLoaded();
        }

        mCallbackListener = null;
    }

    @Override
    public void onDeveloLoadFail(AdError adError) {
        if (mIsRefresh) {
            return;
        }
        if (mCallbackListener != null) {
            mCallbackListener.onRewardedVideoAdFailed(adError);
        }

        mCallbackListener = null;
    }

    @Override
    public void removeFormatCallback() {
        mCallbackListener = null;
    }

}
