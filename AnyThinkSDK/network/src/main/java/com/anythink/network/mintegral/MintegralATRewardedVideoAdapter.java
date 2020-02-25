package com.anythink.network.mintegral;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.ErrorCode;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoListener;
import com.mintegral.msdk.out.MTGBidRewardVideoHandler;
import com.mintegral.msdk.out.MTGRewardVideoHandler;
import com.mintegral.msdk.out.RewardVideoListener;

import java.util.Map;

/**
 * Created by zhou on 2018/6/27.
 */
public class MintegralATRewardedVideoAdapter extends CustomRewardVideoAdapter {
    private final String TAG = MintegralATRewardedVideoAdapter.class.getSimpleName();

    MTGRewardVideoHandler mMvRewardVideoHandler;
    MTGBidRewardVideoHandler mMvBidRewardVideoHandler;
    MintegralRewardedVideoSetting mMintegralMediationSetting;
    String place_id = "";
    String mPayload;

    /***
     * init
     */
    private void init(Context context) {

        RewardVideoListener videoListener = new RewardVideoListener() {

            @Override
            public void onVideoLoadSuccess(String pUnitId) {
                if (mLoadResultListener != null) {
                    mLoadResultListener.onRewardedVideoAdLoaded(MintegralATRewardedVideoAdapter.this);
                }
            }

            @Override
            public void onLoadSuccess(String s) {
                if (mLoadResultListener != null) {
                    mLoadResultListener.onRewardedVideoAdDataLoaded(MintegralATRewardedVideoAdapter.this);
                }
            }

            @Override
            public void onVideoLoadFail(String pErrorMSG) {
                if (mLoadResultListener != null) {
                    mLoadResultListener.onRewardedVideoAdFailed(MintegralATRewardedVideoAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, "", pErrorMSG));
                }
            }

            @Override
            public void onAdShow() {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayStart(MintegralATRewardedVideoAdapter.this);
                }
            }

            @Override
            public void onAdClose(boolean pIsCompleteView, String pRewardName,
                                  float pRewardAmout) {
                if (mImpressionListener != null) {
                    if (pIsCompleteView) {
                        mImpressionListener.onReward(MintegralATRewardedVideoAdapter.this);
                    }
                    mImpressionListener.onRewardedVideoAdClosed(MintegralATRewardedVideoAdapter.this);
                }
            }

            @Override
            public void onShowFail(String pErrorMSG) {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayFailed(MintegralATRewardedVideoAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, "", pErrorMSG));
                }

            }

            @Override
            public void onVideoAdClicked(String pUnitId) {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayClicked(MintegralATRewardedVideoAdapter.this);
                }
            }

            @Override
            public void onVideoComplete(String s) {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayEnd(MintegralATRewardedVideoAdapter.this);
                }
            }

            @Override
            public void onEndcardShow(String s) {

            }
        };

        if (TextUtils.isEmpty(mPayload)) {
            mMvRewardVideoHandler = new MTGRewardVideoHandler(context.getApplicationContext(), place_id);
            mMvRewardVideoHandler.setRewardVideoListener(videoListener);
        } else {
            mMvBidRewardVideoHandler = new MTGBidRewardVideoHandler(context.getApplicationContext(), place_id);
            mMvBidRewardVideoHandler.setRewardVideoListener(videoListener);
        }
    }

    @Override
    public void loadRewardVideoAd(final Activity activity, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, CustomRewardVideoListener customRewardVideoListener) {
        mLoadResultListener = customRewardVideoListener;
        if (activity == null) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onRewardedVideoAdFailed(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "activity is null."));
            }
            return;
        }
        if (mediationSetting != null && mediationSetting instanceof MintegralRewardedVideoSetting) {
            mMintegralMediationSetting = (MintegralRewardedVideoSetting) mediationSetting;

        }

        if (serverExtras == null) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onRewardedVideoAdFailed(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "This placement's params in server is null!"));
            }
            return;
        } else {

            String appid = (String) serverExtras.get("appid");
            String appkey = (String) serverExtras.get("appkey");
            place_id = (String) serverExtras.get("unitid");

            if (TextUtils.isEmpty(appid) || TextUtils.isEmpty(appkey) || TextUtils.isEmpty(place_id)) {
                if (mLoadResultListener != null) {
                    mLoadResultListener.onRewardedVideoAdFailed(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "mintegral appid, appkey or unitid is empty!"));
                }
                return;
            }
        }

        if (serverExtras.containsKey("payload")) {
            mPayload = serverExtras.get("payload").toString();
        }

        MintegralATInitManager.getInstance().initSDK(activity.getApplicationContext(), serverExtras, new MintegralATInitManager.InitCallback() {
            @Override
            public void onSuccess() {
                //init
                init(activity);
                //load ad
                startLoad();
            }

            @Override
            public void onError(Throwable e) {
                if (mLoadResultListener != null) {
                    mLoadResultListener.onRewardedVideoAdFailed(MintegralATRewardedVideoAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, "", e.getMessage()));
                }
            }
        });
    }

    @Override
    public boolean initNetworkObjectByPlacementId(Context context, Map<String, Object> serverExtras, ATMediationSetting mediationSetting) {
        if (serverExtras != null) {
            if (serverExtras.containsKey("appid") && serverExtras.containsKey("appkey") && serverExtras.containsKey("unitid")) {
                place_id = serverExtras.get("unitid").toString();
                init(context);
                return true;
            }
        }
        return false;
    }

    /***
     * load ad
     */
    public void startLoad() {
        if (mMvRewardVideoHandler != null) {
            mMvRewardVideoHandler.load();
        }

        if (mMvBidRewardVideoHandler != null) {
            mMvBidRewardVideoHandler.loadFromBid(mPayload);
        }
    }

    @Override
    public void clean() {
    }

    @Override
    public void onResume(Activity activity) {

    }

    @Override
    public void onPause(Activity activity) {

    }

    @Override
    public boolean isAdReady() {
        if (mMvRewardVideoHandler != null) {
            return mMvRewardVideoHandler.isReady();
        }

        if (mMvBidRewardVideoHandler != null) {
            return mMvBidRewardVideoHandler.isBidReady();
        }
        return false;
    }

    @Override
    public void show(Activity activity) {
        if (mMvRewardVideoHandler != null) {
            mMvRewardVideoHandler.show("1", mUserId);
        }

        if (mMvBidRewardVideoHandler != null) {
            mMvBidRewardVideoHandler.showFromBid("1", mUserId);
        }
    }


    @Override
    public String getSDKVersion() {
        return MintegralATConst.getNetworkVersion();
    }

    @Override
    public String getNetworkName() {
        return MintegralATInitManager.getInstance().getNetworkName();
    }
}