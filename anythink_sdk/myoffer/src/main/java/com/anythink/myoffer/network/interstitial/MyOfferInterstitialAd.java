package com.anythink.myoffer.network.interstitial;

import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.common.utils.CommonLogUtil;
import com.anythink.myoffer.buiness.MyOfferAdManager;
import com.anythink.myoffer.buiness.MyOfferImpressionRecordManager;
import com.anythink.myoffer.buiness.resource.MyOfferLoader;
import com.anythink.myoffer.entity.MyOfferAd;
import com.anythink.myoffer.network.base.MyOfferAdMessager;
import com.anythink.myoffer.network.base.MyOfferBaseAd;
import com.anythink.myoffer.ui.MyOfferAdActivity;
import com.anythink.network.myoffer.MyOfferError;
import com.anythink.network.myoffer.MyOfferErrorCode;

import java.util.Map;

public class MyOfferInterstitialAd extends MyOfferBaseAd {

    public static final String TAG = MyOfferInterstitialAd.class.getSimpleName();

    private MyOfferInterstitialAdListener mListener;
    private MyOfferAd mMyOfferAd;

    public MyOfferInterstitialAd(Context context, String placementId, String offerId, String myoffer_setting) {
        super(context, placementId, offerId, myoffer_setting, false);
    }

    public MyOfferInterstitialAd(Context context, String placementId, String offerId, String myoffer_setting, boolean isDefault) {
        super(context, placementId, offerId, myoffer_setting, isDefault);
    }


    public void setListener(MyOfferInterstitialAdListener listener) {
        this.mListener = listener;
    }

    @Override
    public void load() {
        try {
            if(TextUtils.isEmpty(mOfferId) || TextUtils.isEmpty(mPlacementId)) {
                if(mListener != null) {
                    mListener.onAdLoadFailed(MyOfferErrorCode.get(MyOfferErrorCode.noADError, MyOfferErrorCode.fail_params));
                }
                return;
            }
            mMyOfferAd = MyOfferAdManager.getInstance(mContext).getAdCache(mPlacementId, mOfferId);
            if(mMyOfferAd == null) {//说明还没保存
                if(mListener != null) {
                    mListener.onAdLoadFailed(MyOfferErrorCode.get(MyOfferErrorCode.noADError, MyOfferErrorCode.fail_no_offer));
                }
                return;
            }
            if (mMyOfferSetting == null) {
                if (mListener != null) {
                    mListener.onAdLoadFailed(MyOfferErrorCode.get(MyOfferErrorCode.noSettingError, MyOfferErrorCode.fail_no_setting));
                }
                return;
            }

            MyOfferAdManager.getInstance(mContext).load(mPlacementId, mMyOfferAd, mMyOfferSetting, new MyOfferLoader.MyOfferLoaderListener() {
                @Override
                public void onSuccess() {
                    if(mListener != null) {
                        mListener.onAdLoaded();
                    }
                }

                @Override
                public void onFailed(MyOfferError error) {
                    if(mListener != null) {
                        mListener.onAdLoadFailed(error);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            if(mListener != null) {
                mListener.onAdLoadFailed(MyOfferErrorCode.get(MyOfferErrorCode.unknow, e.getMessage()));
            }
        }

    }

    @Override
    public void show(Map<String, Object> extraMap) {

        try {
            if (mContext == null) {
                if (mListener != null) {
                    mListener.onVideoShowFailed(MyOfferErrorCode.get(MyOfferErrorCode.noADError, MyOfferErrorCode.fail_null_context));
                }
                return;
            }

            final String requestId = extraMap.get(MyOfferBaseAd.EXTRA_REQUEST_ID).toString();
            final String scenario = extraMap.get(MyOfferBaseAd.EXTRA_SCENARIO).toString();
            final int orientation = (int) extraMap.get(MyOfferBaseAd.EXTRA_ORIENTATION);

            long timeStamp = System.currentTimeMillis();
            MyOfferAdMessager.getInstance().setListener(mPlacementId + mOfferId + timeStamp, new MyOfferAdMessager.OnEventListener() {
                @Override
                public void onShow() {
                    CommonLogUtil.d(TAG, "onShow.......");
                    if (mListener != null) {
                        mListener.onAdShow();
                    }
                    MyOfferImpressionRecordManager.getInstance(mContext).recordImpression(mMyOfferAd);
                }

                @Override
                public void onVideoShowFailed(MyOfferError error) {
                    CommonLogUtil.d(TAG, "onVideoShowFailed......." + error.printStackTrace());
                    if (mListener != null) {
                        mListener.onVideoShowFailed(error);
                    }
                }

                @Override
                public void onVideoPlayStart() {
                    CommonLogUtil.d(TAG, "onVideoPlayStart.......");
                    if (mListener != null) {
                        mListener.onVideoAdPlayStart();
                    }
                }

                @Override
                public void onVideoPlayEnd() {
                    CommonLogUtil.d(TAG, "onVideoPlayEnd.......");
                    if (mListener != null) {
                        mListener.onVideoAdPlayEnd();
                    }
                }

                @Override
                public void onReward() {
                }

                @Override
                public void onClose() {
                    CommonLogUtil.d(TAG, "onClose.......");
                    if (mListener != null) {
                        mListener.onAdClosed();
                    }
                    MyOfferAdMessager.getInstance().unRegister(mPlacementId + mOfferId);
                }

                @Override
                public void onClick() {
                    CommonLogUtil.d(TAG, "onClick.......");
                    if (mListener != null) {
                        mListener.onAdClick();
                    }
                }

            });

            MyOfferAdActivity.start(mContext, requestId, scenario, MyOfferAdActivity.FORMAT_INTERSTITIAL,
                    mMyOfferAd, mPlacementId, mOfferId, mMyOfferSetting, orientation, timeStamp);
        } catch (Exception e) {
            e.printStackTrace();
            if (mListener != null) {
                mListener.onVideoShowFailed(MyOfferErrorCode.get(MyOfferErrorCode.unknow, e.getMessage()));
            }
        }

    }

    @Override
    public boolean isReady() {

        try {
            if(mContext == null) {
                CommonLogUtil.d(TAG, "isReady() context = null!");
                return false;
            } else if(TextUtils.isEmpty(mPlacementId)) {
                CommonLogUtil.d(TAG, "isReady() mPlacementId = null!");
                return false;
            } else if(TextUtils.isEmpty(mOfferId)) {
                CommonLogUtil.d(TAG, "isReady() mOfferId = null!");
                return false;
            }

            if (mMyOfferAd == null) {
                mMyOfferAd = MyOfferAdManager.getInstance(mContext).getAdCache(mPlacementId, mOfferId);
                if (mMyOfferAd == null) {
                    CommonLogUtil.d(TAG, "isReady() MyOffer no exist!");
                    return false;
                }
            }

            return MyOfferAdManager.getInstance(mContext).isReady(mMyOfferAd, mIsDefault);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


}
