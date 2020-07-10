package com.anythink.network.nend;

import android.app.Activity;
import android.content.Context;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.ErrorCode;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialListener;

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
    public void loadInterstitialAd(Context context, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, CustomInterstitialListener customInterstitialListener) {
        mLoadResultListener = customInterstitialListener;

        if (!(context instanceof Activity)) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onInterstitialAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "context must be activity."));
            }
            return;
        }

        Activity activity = (Activity) context;

        if (serverExtras == null) {
            if (customInterstitialListener != null) {
                customInterstitialListener.onInterstitialAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "This placement's params in server is null!"));
            }
            return;
        }

        if (serverExtras.containsKey("api_key") && serverExtras.containsKey("spot_id")) {
            mApiKey = (String) serverExtras.get("api_key");
            mSpotId = Integer.parseInt((String) serverExtras.get("spot_id"));

        } else {
            if (customInterstitialListener != null) {
                customInterstitialListener.onInterstitialAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "app_id or slot_id is empty!"));
            }
            return;
        }


        if (serverExtras.containsKey("is_video")) {
            mInterstitalType = Integer.parseInt(serverExtras.get("is_video").toString());
        }

        //interstitial ad
        if (mInterstitalType == 0) {
            NendATInterstitialLoadManager.getInstance().loadAd(activity, mSpotId, mApiKey, this);
        }


        //interstitial video ad
        if (mInterstitalType == 1) {
            videoAdLoad(activity);
        }

        //fullscreen ad
        if (mInterstitalType == 2) {
            fullscreenAdLoad(activity);
        }
    }

    private void fullscreenAdLoad(Activity activity) {
        mNendAdFullScreen = new NendAdFullBoardLoader(activity, mSpotId, mApiKey);
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

    private void videoAdLoad(Activity activity) {
        mNendAdInterstitalVideo = new NendAdInterstitialVideo(activity, mSpotId, mApiKey);
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
    public void show(Context context) {

        if (mInterstitalType == 0 && context instanceof Activity) {
            NendAdInterstitial.NendAdInterstitialShowResult result = NendAdInterstitial.showAd((Activity) context, mSpotId, new NendAdInterstitial.OnClickListener() {
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

        if (mInterstitalType == 1 && context instanceof Activity) {
            if (mNendAdInterstitalVideo != null) {
                mNendAdInterstitalVideo.showAd((Activity) context);
            }
        }

        if (mInterstitalType == 2 && context instanceof Activity) {
            if (mNendAdFullBoard != null) {
                mNendAdFullBoard.show((Activity) context);
            }
        }
    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

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
    public void clean() {

    }


    public void notifyLoaded() {
        mIsReady = true;
        if (mLoadResultListener != null) {
            mLoadResultListener.onInterstitialAdLoaded(this);
        }
    }

    public void notifyLoadFail(String code, String msg) {
        if (mLoadResultListener != null) {
            mLoadResultListener.onInterstitialAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, code, msg));
        }
    }

    public void notifyShow() {
        mIsReady = false;
        if (mImpressListener != null) {
            mImpressListener.onInterstitialAdShow(this);
        }
    }

    public void notifyClick() {
        if (mImpressListener != null) {
            mImpressListener.onInterstitialAdClicked(this);
        }
    }

    public void notifyClose() {
        if (mImpressListener != null) {
            mImpressListener.onInterstitialAdClose(this);
        }
    }

    @Override
    public String getSDKVersion() {
        return "";
    }

    @Override
    public String getNetworkName() {
        return NendATInitManager.getInstance().getNetworkName();
    }
}
