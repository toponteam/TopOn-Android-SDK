package com.anythink.network.nend;

import android.content.Context;

import com.anythink.core.api.ATInitMediation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NendATInitManager extends ATInitMediation {

    public static final String TAG = NendATInitManager.class.getSimpleName();
    private static NendATInitManager sInstance;

    private NendATInitManager() {

    }

    public static NendATInitManager getInstance() {
        if (sInstance == null) {
            sInstance = new NendATInitManager();
        }
        return sInstance;
    }

    @Override
    public void initSDK(Context context, Map<String, Object> serviceExtras) {

    }

    @Override
    public String getNetworkName() {
        return "Nend";
    }

    @Override
    public String getNetworkSDKClass() {
        return "net.nend.android.internal.ui.activities.video.NendAdRewardedVideoActivity";
    }

    @Override
    public List getActivityStatus() {
        ArrayList<String> list = new ArrayList<>();
        list.add("net.nend.android.internal.ui.activities.fullboard.NendAdFullBoardActivity");
        list.add("net.nend.android.internal.ui.activities.interstitial.NendAdInterstitialActivity");
        list.add("net.nend.android.internal.ui.activities.video.NendAdInterstitialVideoActivity");
        list.add("net.nend.android.internal.ui.activities.video.NendAdRewardedVideoActivity");
        list.add("net.nend.android.internal.ui.activities.formats.FullscreenVideoPlayingActivity");
        return list;
    }
}
