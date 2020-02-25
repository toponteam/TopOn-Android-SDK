/*
 * Copyright (C) 2019 Mintegral, Inc. All rights reserved.
 */
package com.anythink.hb.bidder;

import android.os.Looper;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.anythink.hb.Bidder;
import com.anythink.hb.LogUtil;
import com.anythink.hb.callback.BiddingCallback;
import com.anythink.hb.constants.ADType;
import com.anythink.hb.data.AuctionNotification;
import com.anythink.hb.data.BidRequestInfo;
import com.anythink.hb.data.BiddingResponse;
import com.anythink.hb.data.HiBidContext;
import com.anythink.hb.exception.BidderInitFailedException;
import com.anythink.hb.exception.BiddingException;
import com.anythink.hb.exception.FailedToGetRenderException;
import com.anythink.hb.exception.SdkIntegratedException;
import com.mintegral.msdk.MIntegralConstans;
import com.mintegral.msdk.MIntegralSDK;
import com.mintegral.msdk.interstitialvideo.out.MTGBidInterstitialVideoHandler;
import com.mintegral.msdk.mtgbid.out.BannerBidRequestParams;
import com.mintegral.msdk.mtgbid.out.BidListennning;
import com.mintegral.msdk.mtgbid.out.BidLossCode;
import com.mintegral.msdk.mtgbid.out.BidManager;
import com.mintegral.msdk.mtgbid.out.BidResponsed;
import com.mintegral.msdk.out.MIntegralSDKFactory;
import com.mintegral.msdk.out.MTGBidRewardVideoHandler;
import com.mintegral.msdk.out.MtgBidNativeHandler;

import java.util.Map;


/**
 * Mintegral bidder adapter
 */
public class MtgBidder implements Bidder {
    private static final String TAG = MtgBidder.class.getSimpleName();

    private static volatile boolean sdkInitialized = false;

    private HiBidContext mContext;
    private Object adBidFormat;
    private BidRequestInfo curBidRequestInfo = null;
    private BidResponsed curBidResponsed = null;
    private String curPlacementId = "";
    private String bannerSize = "";


    @Override
    public Class getBidderClass() {
        return MtgBidder.class;
    }

    @Override
    public BidRequestInfo getBidderRequestInfo() {
        return curBidRequestInfo;
    }

    @Override
    public void init(HiBidContext context) throws BidderInitFailedException, SdkIntegratedException {
        try {
            mContext = context;
            if (!sdkInitialized) {
                final MIntegralSDK sdk = MIntegralSDKFactory.getMIntegralSDK();
                Map<String, String> map = sdk.getMTGConfigurationMap(mContext.getAppId(), mContext.getAppKey());
                sdk.init(map, mContext.getContext());
                sdkInitialized = true;
            }

        } catch (Exception ex) {
            throw new BidderInitFailedException("MtgBidder init failed", ex.getCause());
        } catch (NoClassDefFoundError ex) {
            throw new SdkIntegratedException("Mintegral sdk not integrated!", ex.getCause());
        }
    }


    @Override
    public void bid(final BidRequestInfo bidRequestInfo, String adType, long timeOutMS,
                    final BiddingCallback callBack) throws BiddingException {

        if (bidRequestInfo == null || mContext == null) {
            throw new BiddingException("MtgBidder: bidRequestInfo == null || context == null");
        }

        if (TextUtils.isEmpty(bidRequestInfo.getAppId()) || TextUtils.isEmpty(bidRequestInfo.getPlacementId())) {
            throw new BiddingException("MtgBidder: appId == null || placementId == null");
        }

        if (TextUtils.equals(ADType.BANNER, adType)) {
            bannerSize = bidRequestInfo.getBannerSize();
            if (TextUtils.isEmpty(bannerSize)) {
                throw new BiddingException("facebook: banner size == null");
            }
        }

        try {
            curBidRequestInfo = bidRequestInfo;
            curPlacementId = bidRequestInfo.getPlacementId();

            adBidFormat = getAdBidFormat(adType);
            if (adBidFormat == null) {
                BiddingResponse biddingResponse = new BiddingResponse(MtgBidder.class,
                        "Unsupported MtgBidder AD format!", MtgBidder.this, curBidRequestInfo);
                if (callBack != null) {
                    callBack.onBiddingResponse(biddingResponse);
                    return;
                }
            }

            BidManager manager;
            if (TextUtils.equals(ADType.BANNER, adType)) {// Banner
                if (TextUtils.equals(bannerSize, "smart")) {
                    DisplayMetrics displayMetrics = mContext.getContext().getResources().getDisplayMetrics();
                    int heightPixels = displayMetrics.heightPixels;
                    int width;
                    int height;
                    if (heightPixels < 720) {
                        width = 320;
                        height = 50;
                    } else {
                        width = 720;
                        height = 90;
                    }
                    manager = new BidManager(new BannerBidRequestParams(curBidRequestInfo.getPlacementId(), width, height));
                } else {
                    String[] size = bannerSize.split("x");
                    manager = new BidManager(new BannerBidRequestParams(curBidRequestInfo.getPlacementId(), Integer.parseInt(size[0]), Integer.parseInt(size[1])));
                }
            } else {
                manager = new BidManager(curBidRequestInfo.getPlacementId());
            }
            manager.setBidListener(new BidListennning() {
                @Override
                public void onFailed(String msg) {
                    BiddingResponse biddingResponse = new BiddingResponse(MtgBidder.class, msg,
                            MtgBidder.this, curBidRequestInfo);
                    if (callBack != null) {
                        callBack.onBiddingResponse(biddingResponse);
                    }
                }

                @Override
                public void onSuccessed(BidResponsed bidResponsed) {
                    BiddingResponse biddingResponse = null;
                    if (bidResponsed != null) {
                        curBidResponsed = bidResponsed;

                        /**Currency exchange， to US dollar*/
                        biddingResponse = new BiddingResponse(MtgBidder.class,
                                Double.parseDouble(bidResponsed.getPrice()), bidResponsed.getBidToken(),
                                MtgBidder.this, curBidRequestInfo);
                    } else {
                        biddingResponse = new BiddingResponse(FacebookBidder.class,
                                "Mintegral bid response is NULL", MtgBidder.this, curBidRequestInfo);
                    }
                    if (callBack != null) {
                        callBack.onBiddingResponse(biddingResponse);
                    }
                }
            });
            if (manager != null) {
                Looper.prepare();
                manager.bid();
                Looper.loop();
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
            BiddingResponse biddingResponse = new BiddingResponse(MtgBidder.class,
                    ex.getMessage(), MtgBidder.this, curBidRequestInfo);
            if (callBack != null) {
                callBack.onBiddingResponse(biddingResponse);
            }
        }

    }

    @Override
    public void onAuctionNotification(AuctionNotification notification) {
        if (curBidResponsed != null && mContext != null) {
            if (notification.isWinner()) {
                LogUtil.i(TAG, "Mtg Bidder Wins");
                curBidResponsed.sendWinNotice(mContext.getContext());
            } else {
                if (notification.getReasonCode().equals(AuctionNotification.ReasonCode.Loss)) {
                    LogUtil.i(TAG, "Mtg Bidder Loss");
                    curBidResponsed.sendLossNotice(mContext.getContext(), BidLossCode.bidPriceNotHighest());
                } else if (notification.getReasonCode().equals(AuctionNotification.ReasonCode.Timeout)) {
                    LogUtil.i(TAG, "Mtg Bidder Timeout");
                    curBidResponsed.sendLossNotice(mContext.getContext(), BidLossCode.bidTimeOut());
                } else {
                    LogUtil.i(TAG, "Mtg Bidder Loss");
                }
            }
        }

    }

    @Override
    public Object getAdsRender() throws FailedToGetRenderException {
        if (adBidFormat == null) {
            throw new FailedToGetRenderException("Unsupported MTG AD format!");
        }

        if (mContext == null) {
            throw new FailedToGetRenderException("HiBidContext == NULL!");
        }

        Object adsRender = null;
        switch ((String) adBidFormat) {
            case ADType.NATIVE: {
                Map<String, Object> properties = MtgBidNativeHandler.getNativeProperties(curPlacementId);
                properties.put(MIntegralConstans.PROPERTIES_AD_NUM, 1);
                adsRender = new MtgBidNativeHandler(properties, mContext.getContext());
                break;
            }
            case ADType.INTERSTITIAL: {
                adsRender = new MTGBidInterstitialVideoHandler(mContext.getContext(), curPlacementId);
                break;
            }
            case ADType.REWARDED_VIDEO: {
                adsRender = new MTGBidRewardVideoHandler(mContext.getContext(), curPlacementId);
                break;
            }
            default:
                break;
        }
        return adsRender;
    }

    @Override
    public Object getAdBidFormat(String adType) {
        Object adBidFormat = null;
        switch (adType) {
            case ADType.NATIVE:
            case ADType.BANNER:
            case ADType.INTERSTITIAL:
            case ADType.REWARDED_VIDEO: {
                adBidFormat = adType;
                break;
            }
            default: {
                break;
            }
        }
        return adBidFormat;
    }
}

