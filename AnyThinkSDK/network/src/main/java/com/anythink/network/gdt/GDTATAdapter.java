package com.anythink.network.gdt;

import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.AdError;
import com.anythink.core.api.ErrorCode;
import com.anythink.nativead.unitgroup.api.CustomNativeAd;
import com.anythink.nativead.unitgroup.api.CustomNativeAdapter;
import com.anythink.nativead.unitgroup.api.CustomNativeListener;
import com.qq.e.ads.cfg.VideoOption;
import com.qq.e.ads.nativ.ADSize;
import com.qq.e.ads.nativ.NativeADUnifiedListener;
import com.qq.e.ads.nativ.NativeExpressAD;
import com.qq.e.ads.nativ.NativeExpressADView;
import com.qq.e.ads.nativ.NativeMediaAD;
import com.qq.e.ads.nativ.NativeMediaADData;
import com.qq.e.ads.nativ.NativeUnifiedAD;
import com.qq.e.ads.nativ.NativeUnifiedADData;
import com.qq.e.comm.constants.AdPatternType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhou on 2018/1/16.
 */

public class GDTATAdapter extends CustomNativeAdapter {
    private static final String TAG = GDTATAdapter.class.getSimpleName();

    String mUnitId, mAppid;
    int mAdCount;

    CustomNativeListener mCustomNativeListener;

    //Self-rendering video manager
    private NativeMediaAD mADManager;//

    //Self-rendering2.0
    private NativeUnifiedAD mNativeUnifiedAd;

    //Native Express
    private NativeExpressAD nativeExpressAD;
    Map<NativeExpressADView, GDTATNativeAd> mTemplateNativeDataGDTNativeAdMap = null;
    private int mAdWidth = ADSize.FULL_WIDTH, mAdHeight = ADSize.AUTO_HEIGHT;

    private Map<String, Object> mServerExtras, mLocalExtras;

    int ADTYPE = 3;

    int mUnitVersion = 2;

    @Override
    public void loadNativeAd(Context context, final CustomNativeListener customNativeListener, Map<String, Object> serverExtras, Map<String, Object> localExtras) {
        String appid = "";
        String unitId = "";
        mServerExtras = serverExtras;
        mLocalExtras = localExtras;

        mCustomNativeListener = customNativeListener;
        if (serverExtras.containsKey("app_id")) {
            appid = serverExtras.get("app_id").toString();
        }
        if (serverExtras.containsKey("unit_id")) {
            unitId = serverExtras.get("unit_id").toString();
        }

        if (serverExtras.containsKey("unit_version")) { //version
            mUnitVersion = Integer.parseInt(serverExtras.get("unit_version").toString());
        }

        boolean adTypeServiceCallback = false;
        if (serverExtras.containsKey("unit_type")) {
            int unitType = Integer.parseInt(serverExtras.get("unit_type").toString());
            if (unitType == 1) { //Native Express
                ADTYPE = 3;
            } else if (unitType == 2) { //Self-rendering
                ADTYPE = 1;
            }
            adTypeServiceCallback = true;
        }

        if (TextUtils.isEmpty(appid) || TextUtils.isEmpty(unitId)) {
            if (customNativeListener != null) {
                AdError adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", "GTD appid or unitId is empty.");
                customNativeListener.onNativeAdFailed(this, adError);

            }
            return;
        }

        int requestNum = 1;
        try {
            if (serverExtras != null && serverExtras.containsKey(CustomNativeAd.AD_REQUEST_NUM)) {
                requestNum = Integer.parseInt(serverExtras.get(CustomNativeAd.AD_REQUEST_NUM).toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        mAdCount = requestNum;

        mAppid = appid;
        mUnitId = unitId;


        //location story
        try {
            if (!adTypeServiceCallback) {
                if (localExtras.containsKey(GDTATConst.ADTYPE)) {
                    ADTYPE = Integer.parseInt(localExtras.get(GDTATConst.ADTYPE).toString());
                }
            }

            if (localExtras.containsKey(GDTATConst.AD_WIDTH)) {
                mAdWidth = Integer.parseInt(localExtras.get(GDTATConst.AD_WIDTH).toString());
            }

            if (localExtras.containsKey(GDTATConst.AD_HEIGHT)) {
                mAdHeight = Integer.parseInt(localExtras.get(GDTATConst.AD_HEIGHT).toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        switch (ADTYPE) {
            case 1:
            case 2:
                if (mUnitVersion != 2) {
                    //Picture + Video Self Rendering
                    initNativeVideoAD(context);
                } else { //adslot 2.0
                    initUnifiedAd(context);
                }

                break;
            default:
                //Picture + video template
                initNativeExpressAD(context);
        }
        loadAd();
    }

    /**
     * Self-rendering 2.0
     */
    private void initUnifiedAd(final Context context) {
        mNativeUnifiedAd = new NativeUnifiedAD(context, mAppid, mUnitId, new NativeADUnifiedListener() {
            @Override
            public void onADLoaded(List<NativeUnifiedADData> list) {
                List<CustomNativeAd> customNativeAds = new ArrayList<>();
                if (list != null && list.size() > 0) {
                    for (NativeUnifiedADData unifiedADData : list) {
                        GDTATNativeAd gdtNativeAd = new GDTATNativeAd(context, unifiedADData, mCustomNativeListener, mLocalExtras);
                        customNativeAds.add(gdtNativeAd);
                    }

                    if (mCustomNativeListener != null) {
                        mCustomNativeListener.onNativeAdLoaded(GDTATAdapter.this, customNativeAds);
                    }
                } else {
                    AdError adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", "Ad list is empty");
                    mCustomNativeListener.onNativeAdFailed(GDTATAdapter.this, adError);
                }
            }

            @Override
            public void onNoAD(com.qq.e.comm.util.AdError gdtAdError) {
                AdError adError = ErrorCode.getErrorCode(ErrorCode.noADError, gdtAdError.getErrorCode() + "", gdtAdError.getErrorMsg());
                mCustomNativeListener.onNativeAdFailed(GDTATAdapter.this, adError);
            }
        });

    }

    /**
     * Native Express
     */
    private void initNativeExpressAD(final Context context) {
        mTemplateNativeDataGDTNativeAdMap = new HashMap<>(mAdCount);
        NativeExpressAD.NativeExpressADListener _nativeExpressADListener = new NativeExpressAD.NativeExpressADListener() {

            @Override
            public void onNoAD(com.qq.e.comm.util.AdError pAdError) {
                if (mCustomNativeListener != null) {
                    AdError _adError = ErrorCode.getErrorCode(ErrorCode.noADError, pAdError.getErrorCode() + "", pAdError.getErrorMsg());
                    mCustomNativeListener.onNativeAdFailed(GDTATAdapter.this, _adError);
                }
            }

            @Override
            public void onADLoaded(List<NativeExpressADView> pList) {

                if (pList.size() > 0) {
                    List<CustomNativeAd> customNativeAds = new ArrayList<>();
                    for (NativeExpressADView _nativeADData : pList) {

                        GDTATNativeAd mGdtNativeAd = new GDTATNativeAd(context, _nativeADData, mCustomNativeListener, mLocalExtras);
                        customNativeAds.add(mGdtNativeAd);
                        mTemplateNativeDataGDTNativeAdMap.put(_nativeADData, mGdtNativeAd);
                    }

                    if (mCustomNativeListener != null) {
                        mCustomNativeListener.onNativeAdLoaded(GDTATAdapter.this, customNativeAds);
                    }

                }
            }

            @Override
            public void onRenderFail(NativeExpressADView pNativeExpressADView) {
                if (mCustomNativeListener != null) {
                    AdError _adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", "onRenderFail");
                    mCustomNativeListener.onNativeAdFailed(GDTATAdapter.this, _adError);
                }
            }

            @Override
            public void onRenderSuccess(NativeExpressADView pNativeExpressADView) {
            }

            @Override
            public void onADExposure(NativeExpressADView pNativeExpressADView) {
            }

            @Override
            public void onADClicked(NativeExpressADView pNativeExpressADView) {
                GDTATNativeAd gdtNativeAd = mTemplateNativeDataGDTNativeAdMap.get(pNativeExpressADView);
                if (gdtNativeAd != null) {
                    gdtNativeAd.notifyAdClicked();
                }
            }

            @Override
            public void onADClosed(NativeExpressADView pNativeExpressADView) {
                GDTATNativeAd gdtNativeAd = mTemplateNativeDataGDTNativeAdMap.get(pNativeExpressADView);
                if (gdtNativeAd != null) {
                    gdtNativeAd.notifyAdDislikeClick();
                }
            }

            @Override
            public void onADLeftApplication(NativeExpressADView pNativeExpressADView) {
            }

            @Override
            public void onADOpenOverlay(NativeExpressADView pNativeExpressADView) {
            }

            @Override
            public void onADCloseOverlay(NativeExpressADView pNativeExpressADView) {
            }
        };

        nativeExpressAD = new NativeExpressAD(context, new ADSize(mAdWidth, mAdHeight), mAppid, mUnitId, _nativeExpressADListener); // Context must be Activity
    }


    /***
     * init Self Rendering
     */
    private void initNativeVideoAD(final Context context) {
        NativeMediaAD.NativeMediaADListener listener = new NativeMediaAD.NativeMediaADListener() {

            @Override
            public void onADLoaded(List<NativeMediaADData> adList) {
                GDTATNativeAd mGdtNativeAd;
                if (adList.size() > 0) {
                    List<CustomNativeAd> customNativeAds = new ArrayList<>();
                    for (NativeMediaADData _nativeMediaADData : adList) {
                        NativeMediaADData mAD = _nativeMediaADData;
                        mGdtNativeAd = new GDTATNativeAd(context, mAD, mCustomNativeListener, mLocalExtras);
                        customNativeAds.add(mGdtNativeAd);
                        if (mAD.getAdPatternType() == AdPatternType.NATIVE_VIDEO) {
                            /**
                             * If the native ad is an ad with video material, you also need to call the preLoadVideo interface to load the video material:
                             *    - Loading success: NativeMediaADListener.onADVideoLoaded (NativeMediaADData adData)
                             *    - Loading failed: NativeMediaADListener.onADError (NativeMediaADData adData, int errorCode) , error code is 700
                             */
                            mAD.preLoadVideo();
                        }
                    }

                    if (mCustomNativeListener != null) {
                        mCustomNativeListener.onNativeAdLoaded(GDTATAdapter.this, customNativeAds);
                    }
                }
            }

            @Override
            public void onNoAD(com.qq.e.comm.util.AdError adError) {
                if (mCustomNativeListener != null) {
                    AdError _adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", " no ad return ");
                    mCustomNativeListener.onNativeAdFailed(GDTATAdapter.this, _adError);
                }
            }

            /**
             * The advertising status changes. For App ads, the download / installation status and download progress can change.
             *
             * @param ad    Ad objects with changed status
             */
            @Override
            public void onADStatusChanged(NativeMediaADData ad) {
            }

            @Override
            public void onADError(NativeMediaADData adData, com.qq.e.comm.util.AdError adError) {
                if (mCustomNativeListener != null) {
                    AdError _adError = ErrorCode.getErrorCode(ErrorCode.noADError, adError.getErrorCode() + "", adError.getErrorMsg());
                    mCustomNativeListener.onNativeAdFailed(GDTATAdapter.this, _adError);
                }
            }

            @Override
            public void onADVideoLoaded(NativeMediaADData adData) {

            }

            @Override
            public void onADExposure(NativeMediaADData adData) {

            }

            @Override
            public void onADClicked(NativeMediaADData adData) {

            }
        };

        mADManager = new NativeMediaAD(context, mAppid, mUnitId, listener);
    }


    public void loadAd() {

        if (mADManager != null) {
            try {

                mADManager.loadAD(mAdCount);
            } catch (Exception e) {
                e.printStackTrace();
                AdError adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", "GDT ad load error!." + e.getMessage());
                mCustomNativeListener.onNativeAdFailed(GDTATAdapter.this, adError);

            }
        }


        if (nativeExpressAD != null) {//Native Express
            try {
                nativeExpressAD.setVideoOption(new VideoOption.Builder()
                        .setAutoPlayPolicy(VideoOption.AutoPlayPolicy.ALWAYS)
                        .setAutoPlayMuted(false)
                        .build());

                nativeExpressAD.loadAD(1);
            } catch (Exception e) {
                e.printStackTrace();
                AdError adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", "GDT ad load error!." + e.getMessage());
                mCustomNativeListener.onNativeAdFailed(GDTATAdapter.this, adError);
            }
        }

        if (mNativeUnifiedAd != null) {
            try {
                mNativeUnifiedAd.loadData(mAdCount);
            } catch (Exception e) {
                e.printStackTrace();
                AdError adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", "GDT ad load error!." + e.getMessage());
                mCustomNativeListener.onNativeAdFailed(GDTATAdapter.this, adError);
            }
        }
    }


    @Override
    public String getSDKVersion() {
        return GDTATConst.getNetworkVersion();
    }

    @Override
    public void clean() {

    }

    @Override
    public String getNetworkName() {
        return GDTATInitManager.getInstance().getNetworkName();
    }
}
