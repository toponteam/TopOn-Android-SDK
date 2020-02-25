package com.anythink.network.mopub;

import android.content.Context;
import android.view.View;

import com.anythink.banner.api.ATBannerView;
import com.anythink.banner.unitgroup.api.CustomBannerAdapter;
import com.anythink.banner.unitgroup.api.CustomBannerListener;
import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.ErrorCode;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubView;

import java.util.Map;

/**
 * Created by Z on 2018/6/27.
 */

public class MopubATBannerAdapter extends CustomBannerAdapter {
    private final String TAG = MopubATBannerAdapter.class.getSimpleName();

    CustomBannerListener mListener;
    String adUnitId;
    MoPubView mBannerView;

    private void startLoad(Context context) {
        MoPubView moPubView = new MoPubView(context);
        moPubView.setAdUnitId(adUnitId);
        moPubView.setAutorefreshEnabled(false);
        moPubView.setBannerAdListener(new MoPubView.BannerAdListener() {
            @Override
            public void onBannerLoaded(MoPubView banner) {
                mBannerView = banner;
                if (mListener != null) {
                    mListener.onBannerAdLoaded(MopubATBannerAdapter.this);
                }
            }

            @Override
            public void onBannerFailed(MoPubView banner, MoPubErrorCode errorCode) {
                if (mListener != null) {
                    mListener.onBannerAdLoadFail(MopubATBannerAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, errorCode.getIntCode() + "", errorCode.toString()));
                }
                if (banner != null) {
                    banner.destroy();
                }
            }

            @Override
            public void onBannerClicked(MoPubView banner) {
                if (mListener != null) {
                    mListener.onBannerAdClicked(MopubATBannerAdapter.this);
                }
            }

            @Override
            public void onBannerExpanded(MoPubView banner) {
                if (mListener != null) {
                    mListener.onBannerAdShow(MopubATBannerAdapter.this);
                }
            }

            @Override
            public void onBannerCollapsed(MoPubView banner) {
                if (mListener != null) {
                    mListener.onBannerAdClose(MopubATBannerAdapter.this);
                }
            }
        });
        moPubView.loadAd();
    }

    @Override
    public View getBannerView() {
        return mBannerView;
    }

    @Override
    public void clean() {
        if (mBannerView != null) {
            mBannerView.destroy();
            mBannerView = null;
        }
    }

    @Override
    public void loadBannerAd(ATBannerView anythinkBannerView, final Context activity, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, CustomBannerListener customBannerListener) {
        mListener = customBannerListener;

        if (activity == null) {
            if (mListener != null) {
                mListener.onBannerAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "activity is null."));
            }
            return;
        }

        if (serverExtras == null) {
            if (mListener != null) {
                mListener.onBannerAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "This placement's params in server is null!"));
            }
            return;
        } else {
            if (serverExtras.containsKey("unitid")) {
                adUnitId = (String) serverExtras.get("unitid");

            } else {
                if (mListener != null) {
                    mListener.onBannerAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "unitid is empty!"));
                }
                return;
            }
        }

        MopubATInitManager.getInstance().initSDK(activity.getApplicationContext(), serverExtras, new MopubATInitManager.InitListener() {
            @Override
            public void initSuccess() {
                startLoad(activity);
            }
        });
    }

    @Override
    public String getSDKVersion() {
        return MopubATConst.getNetworkVersion();
    }

    @Override
    public String getNetworkName() {
        return MopubATInitManager.getInstance().getNetworkName();
    }
}