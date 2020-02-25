package com.anythink.network.nend;

import android.content.Context;

import net.nend.android.NendAdInterstitial;

import java.util.concurrent.ConcurrentHashMap;

public class NendATInterstitialLoadManager {
    private static NendATInterstitialLoadManager sIntance;
    private ConcurrentHashMap<String, NendATInterstitialAdapter> mAdapterMap = new ConcurrentHashMap<>();

    NendAdInterstitial.OnCompletionListenerSpot onCompletionListener;


    public static NendATInterstitialLoadManager getInstance() {
        if (sIntance == null) {
            sIntance = new NendATInterstitialLoadManager();
        }
        return sIntance;
    }

    public void loadAd(Context context, int spotId, String apiKey, final NendATInterstitialAdapter adapter) {
        mAdapterMap.put(spotId + "", adapter);
        if (onCompletionListener == null) {
            onCompletionListener = new NendAdInterstitial.OnCompletionListenerSpot() {
                @Override
                public void onCompletion(NendAdInterstitial.NendAdInterstitialStatusCode nendAdInterstitialStatusCode) {

                }

                @Override
                public void onCompletion(NendAdInterstitial.NendAdInterstitialStatusCode nendAdInterstitialStatusCode, int spotId) {
                    NendATInterstitialAdapter spotAdapter = mAdapterMap.get(spotId + "");
                    switch (nendAdInterstitialStatusCode) {
                        case SUCCESS:
                            if (spotAdapter != null) {
                                spotAdapter.notifyLoaded();
                            }
                            break;
                        case INVALID_RESPONSE_TYPE:
                        case FAILED_AD_REQUEST:
                        case FAILED_AD_INCOMPLETE:
                        case FAILED_AD_DOWNLOAD:
                            if (spotAdapter != null) {
                                spotAdapter.notifyLoadFail("", nendAdInterstitialStatusCode.name());
                            }
                            break;
                        default:
                            if (spotAdapter != null) {
                                spotAdapter.notifyLoadFail("", nendAdInterstitialStatusCode.name());
                            }
                            break;
                    }
                }
            };
            NendAdInterstitial.setListener(onCompletionListener);
        }

        NendAdInterstitial.loadAd(context, apiKey, spotId);
    }

    public void removeAd(int spotId) {
        if (mAdapterMap != null) {
            mAdapterMap.remove(spotId + "");
        }
    }

}
