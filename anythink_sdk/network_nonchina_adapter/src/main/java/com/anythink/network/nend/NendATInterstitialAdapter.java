package com.anythink.network.nend;

import android.app.Activity;
import android.content.Context;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.ErrorCode;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;

import net.nend.android.NendAdFullBoard;
import net.nend.android.NendAdFullBoardLoader;
import net.nend.android.NendAdInterstitial;
import net.nend.android.NendAdInterstitialVideo;
import net.nend.android.NendAdVideo;
import net.nend.android.NendAdVideoListener;

import java.util.Map;

public class NendATInterstitialAdapter extends CustomInterstitialAdapter {

    String mApiKey;
    int mSpotId;
    int mInterstitalType;

    NendAdFullBoardLoader mNendAdFullScreen;
    NendAdFullBoard mNendAdFullBoard;
    NendAdInterstitialVideo mNendAdInterstitalVideo;

    boolean mIsReady = false;

    @Override
    public void loadCustomNetworkAd(Context context, Map<String, Object> serverExtras, Map<String, Object> localExtras) {

        if (serverExtras.containsKey("api_key") && serverExtras.containsKey("spot_id")) {
            mApiKey = (String) serverExtras.get("api_key");
            mSpotId = Integer.parseInt((String) serverExtras.get("spot_id"));

        } else {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "app_id or slot_id is empty!");
            }
            return;
        }


        if (serverExtras.containsKey("is_video")) {
            mInterstitalType = Integer.parseInt(serverExtras.get("is_video").toString());
        }

        //interstitial ad
        if (mInterstitalType == 0) {
            NendATInterstitialLoadManager.getInstance().loadAd(context.getApplicationContext(), mSpotId, mApiKey, this);
        }


        //interstitial video ad
        if (mInterstitalType == 1) {
            videoAdLoad(context.getApplicationContext());
        }

        //fullscreen ad
        if (mInterstitalType == 2) {
            fullscreenAdLoad(context.getApplicationContext());
        }
    }

    private void fullscreenAdLoad(Context context) {
        mNendAdFullScreen = new NendAdFullBoardLoader(context.getApplicationContext(), mSpotId, mApiKey);
        mNendAdFullScreen.loadAd(new NendAdFullBoardLoader.Callback() {
            @Override
            public void onSuccess(NendAdFullBoard nendAdFullBoard) {
                mNendAdFullBoard = nendAdFullBoard;
                mNendAdFullBoard.setAdListener(new NendAdFullBoard.FullBoardAdListener() {
                    @Override
                    public void onShowAd(NendAdFullBoard nendAdFullBoard) {
                        notifyShow();
                    }

                    @Override
                    public void onDismissAd(NendAdFullBoard nendAdFullBoard) {
                        notifyClose();
                    }

                    @Override
                    public void onClickAd(NendAdFullBoard nendAdFullBoard) {
                        notifyClick();
                    }
                });
                notifyLoaded();
            }

            @Override
            public void onFailure(NendAdFullBoardLoader.FullBoardAdError fullBoardAdError) {
                notifyLoadFail("", fullBoardAdError.name());
            }
        });
    }

    private void videoAdLoad(Context context) {
        mNendAdInterstitalVideo = new NendAdInterstitialVideo(context.getApplicationContext(), mSpotId, mApiKey);
        mNendAdInterstitalVideo.setAdListener(new NendAdVideoListener() {
            @Override
            public void onLoaded(NendAdVideo nendAdVideo) {
                notifyLoaded();
            }

            @Override
            public void onFailedToLoad(NendAdVideo nendAdVideo, int i) {
                notifyLoadFail(i + "", "");
            }

            @Override
            public void onFailedToPlay(NendAdVideo nendAdVideo) {

            }

            @Override
            public void onShown(NendAdVideo nendAdVideo) {
                notifyShow();
            }

            @Override
            public void onClosed(NendAdVideo nendAdVideo) {
                notifyClose();
            }

            @Override
            public void onStarted(NendAdVideo nendAdVideo) {

            }

            @Override
            public void onStopped(NendAdVideo nendAdVideo) {

            }

            @Override
            public void onCompleted(NendAdVideo nendAdVideo) {

            }

            @Override
            public void onAdClicked(NendAdVideo nendAdVideo) {
                notifyClick();
            }

            @Override
            public void onInformationClicked(NendAdVideo nendAdVideo) {

            }
        });
        mNendAdInterstitalVideo.loadAd();
    }

    @Override
    public void show(Activity activity) {

        if (mInterstitalType == 0 && activity != null) {
            NendAdInterstitial.NendAdInterstitialShowResult result = NendAdInterstitial.showAd(activity, mSpotId, new NendAdInterstitial.OnClickListener() {
                @Override
                public void onClick(NendAdInterstitial.NendAdInterstitialClickType nendAdInterstitialClickType) {
                    switch (nendAdInterstitialClickType) {
                        case CLOSE:
                            notifyClose();
                            NendATInterstitialLoadManager.getInstance().removeAd(mSpotId);
                            break;
                        case DOWNLOAD:
                            notifyClick();
                            break;
                        default:
                            break;
                    }
                }
            });

            if (result == NendAdInterstitial.NendAdInterstitialShowResult.AD_SHOW_SUCCESS) {
                notifyShow();
            }
        }

        if (mInterstitalType == 1 && activity != null) {
            if (mNendAdInterstitalVideo != null) {
                mNendAdInterstitalVideo.showAd(activity);
            }
        }

        if (mInterstitalType == 2 && activity != null) {
            if (mNendAdFullBoard != null) {
                mNendAdFullBoard.show(activity);
            }
        }
    }

    @Override
    public boolean isAdReady() {
        if (mInterstitalType == 0) {
            return mIsReady;
        }

        if (mInterstitalType == 1) {
            return mNendAdInterstitalVideo != null && mNendAdInterstitalVideo.isLoaded() && mIsReady;
        }

        if (mInterstitalType == 2) {
            return mNendAdFullBoard != null && mIsReady;
        }

        return false;
    }

    @Override
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        return false;
    }

    @Override
    public void destory() {
        if (mNendAdFullBoard != null) {
            mNendAdFullBoard.setAdListener(null);
            mNendAdFullBoard = null;
        }

        if (mNendAdInterstitalVideo != null) {
            mNendAdInterstitalVideo.setAdListener(null);
            mNendAdInterstitalVideo = null;
        }

        mNendAdFullScreen = null;
    }


    public void notifyLoaded() {
        mIsReady = true;
        if (mLoadListener != null) {
            mLoadListener.onAdCacheLoaded();
        }
    }

    public void notifyLoadFail(String code, String msg) {
        if (mLoadListener != null) {
            mLoadListener.onAdLoadError(code, msg);
        }
    }

    public void notifyShow() {
        mIsReady = false;
        if (mImpressListener != null) {
            mImpressListener.onInterstitialAdShow();
        }
    }

    public void notifyClick() {
        if (mImpressListener != null) {
            mImpressListener.onInterstitialAdClicked();
        }
    }

    public void notifyClose() {
        if (mImpressListener != null) {
            mImpressListener.onInterstitialAdClose();
        }
    }

    @Override
    public String getNetworkSDKVersion() {
        return "";
    }

    @Override
    public String getNetworkName() {
        return NendATInitManager.getInstance().getNetworkName();
    }

    @Override
    public String getNetworkPlacementId() {
        try {
            return String.valueOf(mSpotId);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
