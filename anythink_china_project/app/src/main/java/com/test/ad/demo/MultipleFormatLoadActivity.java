package com.test.ad.demo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.anythink.banner.api.ATBannerListener;
import com.anythink.banner.api.ATBannerView;
import com.anythink.core.api.ATAdConst;
import com.anythink.core.api.ATAdInfo;
import com.anythink.core.api.AdError;
import com.anythink.core.common.utils.CommonUtil;
import com.anythink.interstitial.api.ATInterstitial;
import com.anythink.interstitial.api.ATInterstitialListener;
import com.anythink.nativead.api.ATNative;
import com.anythink.nativead.api.ATNativeAdView;
import com.anythink.nativead.api.ATNativeEventListener;
import com.anythink.nativead.api.ATNativeNetworkListener;
import com.anythink.nativead.api.NativeAd;
import com.anythink.rewardvideo.api.ATRewardVideoAd;
import com.anythink.rewardvideo.api.ATRewardVideoListener;

import java.util.HashMap;
import java.util.Map;

public class MultipleFormatLoadActivity extends Activity implements View.OnClickListener {

    private static String TAG = MultipleFormatLoadActivity.class.getSimpleName();

    ATRewardVideoAd mRewardVideoAd;
    ATInterstitial mInterstitialAd;
    ATNative mAtNative;
    ATBannerView mAtBannerView;
    private FrameLayout mFrameLayout;
    private ATNativeAdView mATNativeAdView;
    private NativeDemoRender upArpuRender;

    private String mNativePlacementId = DemoApplicaion.mPlacementId_native_toutiao;
    private String mBannerPlacementId = DemoApplicaion.mPlacementId_banner_toutiao;
    private String mRewardVideoPlacementId = DemoApplicaion.mPlacementId_rewardvideo_toutiao;
    private String mInterstitialPlacementId = DemoApplicaion.mPlacementId_interstitial_toutiao;

    private boolean mNeedLoadRewardVideo = true;
    private boolean mNeedLoadInterstitial = true;
    private boolean mNeedLoadNative = true;
    private boolean mNeedLoadBanner = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiple_format_load);

        init();
        initRewardVideo();
        initInterstitial();
        initNative();
        initBanner();

        initListener();
    }

    private void init() {
        mFrameLayout = ((FrameLayout) findViewById(R.id.ad_container));
    }

    private void initListener() {
        findViewById(R.id.multi_load).setOnClickListener(this);
        findViewById(R.id.show_reward_video).setOnClickListener(this);
        findViewById(R.id.show_interstitial).setOnClickListener(this);
        findViewById(R.id.show_native).setOnClickListener(this);
        findViewById(R.id.show_banner).setOnClickListener(this);
        findViewById(R.id.remove_view).setOnClickListener(this);
    }

    private void initRewardVideo() {

        if (!mNeedLoadRewardVideo) {
            return;
        }

        mRewardVideoAd = new ATRewardVideoAd(this, mRewardVideoPlacementId);
        mRewardVideoAd.setAdListener(new ATRewardVideoListener() {
            @Override
            public void onRewardedVideoAdLoaded() {
                Log.i(TAG, "onRewardedVideoAdLoaded");
            }

            @Override
            public void onRewardedVideoAdFailed(AdError errorCode) {
                Log.i(TAG, "onRewardedVideoAdFailed error:" + errorCode.printStackTrace());
            }

            @Override
            public void onRewardedVideoAdPlayStart(ATAdInfo entity) {
                Log.i(TAG, "onRewardedVideoAdPlayStart, \n" + entity.toString());
            }

            @Override
            public void onRewardedVideoAdPlayEnd(ATAdInfo entity) {
                Log.i(TAG, "onRewardedVideoAdPlayEnd, \n" + entity.toString());
            }

            @Override
            public void onRewardedVideoAdPlayFailed(AdError errorCode, ATAdInfo entity) {
                Log.i(TAG, "onRewardedVideoAdPlayFailed error:" + errorCode.printStackTrace()
                        + "\n" + entity.toString());
            }

            @Override
            public void onRewardedVideoAdClosed(ATAdInfo entity) {
                Log.i(TAG, "onRewardedVideoAdClosed" + "\n" + entity.toString());
            }

            @Override
            public void onRewardedVideoAdPlayClicked(ATAdInfo entity) {
                Log.i(TAG, "onRewardedVideoAdPlayClicked, \n" + entity.toString());
            }

            @Override
            public void onReward(ATAdInfo upArpuAdInfo) {
                Log.i(TAG, "onReward, \n" + upArpuAdInfo.toString());
            }
        });
    }

    private void initInterstitial() {

        if (!mNeedLoadInterstitial) {
            return;
        }

        mInterstitialAd = new ATInterstitial(this, mInterstitialPlacementId);
        mInterstitialAd.setAdListener(new ATInterstitialListener() {
            @Override
            public void onInterstitialAdLoaded() {
                Log.i(TAG, "onInterstitialAdLoaded");
            }

            @Override
            public void onInterstitialAdLoadFail(AdError adError) {
                Log.i(TAG, "onInterstitialAdLoadFail:" + adError.printStackTrace());
            }

            @Override
            public void onInterstitialAdClicked(ATAdInfo entity) {
                Log.i(TAG, "onInterstitialAdClicked, \n" + entity.toString());
            }

            @Override
            public void onInterstitialAdShow(ATAdInfo entity) {
                Log.i(TAG, "onInterstitialAdShow, \n" + entity.toString());
            }

            @Override
            public void onInterstitialAdClose(ATAdInfo entity) {
                Log.i(TAG, "onInterstitialAdClose, \n" + entity.toString());
            }

            @Override
            public void onInterstitialAdVideoStart(ATAdInfo adInfo) {
                Log.i(TAG, "onInterstitialAdVideoStart");
            }

            @Override
            public void onInterstitialAdVideoEnd(ATAdInfo adInfo) {
                Log.i(TAG, "onInterstitialAdVideoEnd");
            }

            @Override
            public void onInterstitialAdVideoError(AdError adError) {
                Log.i(TAG, "onInterstitialAdVideoError");
            }
        });
    }

    private void initNative() {

        if (!mNeedLoadNative) {
            return;
        }

        mAtNative = new ATNative(this.getApplicationContext(), mNativePlacementId, new ATNativeNetworkListener() {
            @Override
            public void onNativeAdLoaded() {
                Log.i(TAG, "onNativeAdLoaded: ");
            }

            @Override
            public void onNativeAdLoadFail(AdError adError) {
                Log.i(TAG, "onNativeAdLoadFail: ");
            }
        });

        Map<String, Object> localMap = new HashMap<>();
        localMap.put(ATAdConst.KEY.AD_WIDTH, CommonUtil.dip2px(this, 250));
        localMap.put(ATAdConst.KEY.AD_HEIGHT, CommonUtil.dip2px(this, 170));
        mAtNative.setLocalExtra(localMap);

        mATNativeAdView = new ATNativeAdView(this);

        upArpuRender = new NativeDemoRender(this);
    }

    private void initBanner() {

        if (!mNeedLoadBanner) {
            return;
        }

        if (mAtBannerView != null) {
            mAtBannerView.destroy();
            mAtBannerView = null;
        }
        mAtBannerView = new ATBannerView(this);
        mAtBannerView.setPlacementId(mBannerPlacementId);
        mAtBannerView.setBackgroundColor(0xffAD4949);
        mAtBannerView.setBannerAdListener(new ATBannerListener() {
            @Override
            public void onBannerLoaded() {
                Log.i("BannerAdActivity", "onBannerLoaded");
            }

            @Override
            public void onBannerFailed(AdError adError) {
                Log.i(TAG, "onBannerFailed:" + adError.printStackTrace());
            }

            @Override
            public void onBannerClicked(ATAdInfo entity) {
                Log.i(TAG, "onBannerClicked, \n" + entity.toString());
            }

            @Override
            public void onBannerShow(ATAdInfo entity) {
                Log.i(TAG, "onBannerShow, \n" + entity.toString());
            }

            @Override
            public void onBannerClose(ATAdInfo adInfo) {
                Log.i(TAG, "onBannerClose");
            }

            @Override
            public void onBannerAutoRefreshed(ATAdInfo entity) {
                Log.i(TAG, "onBannerAutoRefreshed, \n" + entity.toString());
            }

            @Override
            public void onBannerAutoRefreshFail(AdError adError) {
                Log.i(TAG, "onBannerAutoRefreshFail:" + adError.printStackTrace());
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.multi_load:
                multiLoad();
                break;
            case R.id.show_reward_video:
                if (mRewardVideoAd != null) {
                    mRewardVideoAd.show(this);
                }
                break;
            case R.id.show_interstitial:
                if (mInterstitialAd != null) {
                    mInterstitialAd.show(this);
                }
                break;
            case R.id.show_native:
                mFrameLayout.removeAllViews();
                mFrameLayout.addView(mATNativeAdView);

                NativeAd nativeAd = mAtNative.getNativeAd();
                if (nativeAd != null) {
                    nativeAd.setNativeEventListener(new ATNativeEventListener() {
                        @Override
                        public void onAdImpressed(ATNativeAdView view, ATAdInfo entity) {
                            Log.i(TAG, "native ad onAdImpressed--------\n" + entity.toString());
                        }

                        @Override
                        public void onAdClicked(ATNativeAdView view, ATAdInfo entity) {
                            Log.i(TAG, "native ad onAdClicked--------\n" + entity.toString());
                        }

                        @Override
                        public void onAdVideoStart(ATNativeAdView view) {
                            Log.i(TAG, "native ad onAdVideoStart--------");
                        }

                        @Override
                        public void onAdVideoEnd(ATNativeAdView view) {
                            Log.i(TAG, "native ad onAdVideoEnd--------");
                        }

                        @Override
                        public void onAdVideoProgress(ATNativeAdView view, int progress) {
                            Log.i(TAG, "native ad onAdVideoProgress--------:" + progress);
                        }
                    });
                    try {
                        nativeAd.renderAdView(mATNativeAdView, upArpuRender);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(CommonUtil.dip2px(MultipleFormatLoadActivity.this, 20), CommonUtil.dip2px(MultipleFormatLoadActivity.this, 8));
                    layoutParams.gravity = Gravity.TOP | Gravity.RIGHT;

                    nativeAd.prepare(mATNativeAdView, null, layoutParams); //可用于测试是否正常
                } else {
                    Log.i(TAG, "onClick: this placement no cache! ");
                    Toast.makeText(MultipleFormatLoadActivity.this, "this placement no cache!", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.show_banner:
                mFrameLayout.removeAllViews();
                mFrameLayout.addView(mAtBannerView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, CommonUtil.dip2px(getApplicationContext(), 300)));
                break;
            case R.id.remove_view:
                mFrameLayout.removeAllViews();
                break;
        }
    }

    private void multiLoad() {
        if (mNeedLoadRewardVideo) {
            mRewardVideoAd.load();
        }

        if (mNeedLoadInterstitial) {
            mInterstitialAd.load();
        }

        if (mNeedLoadBanner) {
            mAtBannerView.loadAd();
        }

        if (mNeedLoadNative) {
            try {
                mAtNative.makeAdRequest();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
