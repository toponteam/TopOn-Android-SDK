package com.anythink.rewardvideo.bussiness;

import android.content.Context;

import com.anythink.core.common.FormatLoadParams;
import com.anythink.rewardvideo.api.ATRewardVideoListener;

public class RewardedVideoLoadParams extends FormatLoadParams {
    ATRewardVideoListener listener;
    Context context;
    String userId;
    String userData;
}
