/*
 * Copyright (C) 2019 Mintegral, Inc. All rights reserved.
 */
package com.anythink.hb.bidder;

import android.text.TextUtils;

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
import com.facebook.ads.AudienceNetworkAds;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.NativeAd;
import com.facebook.ads.RewardedVideoAd;
import com.facebook.bidding.FBAdBidFormat;
import com.facebook.bidding.FBAdBidRequest;
import com.facebook.bidding.FBAdBidResponse;

/**
 * Facebook bidder adapter
 */
public class FacebookBidder implements Bidder {
    private static final String TAG = FacebookBidder.class.getSimpleName();

    private static volatile boolean sdkInitialized = false;

    private HiBidContext mContext;
    private FBAdBidFormat adBidFormat;
    private FBAdBidResponse curBidResponsed = null;
    private BidRequestInfo curBidRequestInfo = null;

    private String bannerSize;

    @Override
    public Class getBidderClass() {
        return FacebookBidder.class;
    }

    @Override
    public BidRequestInfo getBidderRequestInfo() {
        return curBidRequestInfo;
    }

    @Override
    public void init(HiBidContext context) throws BidderInitFailedException, SdkIntegratedException{
        try {
            mContext = context;
            if (!sdkInitialized) {
                AudienceNetworkAds.initialize(mContext.getContext());
                sdkInitialized = true;
            }
        }catch (Exception ex){
            throw new BidderInitFailedException("Facebook Bidder init failed", ex.getCause());
        }catch (NoClassDefFoundError ex){
            throw new SdkIntegratedException("Facebook sdk not integrated!", ex.getCause());
        }

    }

    @Override
    public void bid(final BidRequestInfo bidRequestInfo, String adType, long timeOutMS,
                    final BiddingCallback callBack) throws BiddingException {

        if(bidRequestInfo == null || mContext == null ){
            throw new BiddingException("facebook: bidRequestInfo == null || context == null");
        }

        if (TextUtils.isEmpty(bidRequestInfo.getAppId()) || TextUtils.isEmpty(bidRequestInfo.getPlacementId())){
            throw new BiddingException("facebook: appId == null || placementId == null");
        }

        if(TextUtils.equals(ADType.BANNER, adType)) {
            bannerSize = bidRequestInfo.getBannerSize();
            if(TextUtils.isEmpty(bannerSize)) {
                throw new BiddingException("facebook: banner size == null");
            }
        }

        try {
            curBidRequestInfo = bidRequestInfo;
            Object object = getAdBidFormat(adType);
            if (object != null) {
                adBidFormat = (FBAdBidFormat) object;
            } else {
                BiddingResponse biddingResponse = new BiddingResponse(FacebookBidder.class,
                        "Unsupported facebook AD format!", FacebookBidder.this, bidRequestInfo);
                if (callBack != null) {
                    callBack.onBiddingResponse(biddingResponse);
                    return;
                }
            }

            Object isTestObj = bidRequestInfo.get("isTest");
            boolean isTest = isTestObj != null ? (boolean) isTestObj : false;


            FBAdBidRequest bidRequest = new FBAdBidRequest(
                    mContext.getContext(),
                    curBidRequestInfo.getAppId(),
                    curBidRequestInfo.getPlacementId(),
                    adBidFormat)
                    .withPlatformId(curBidRequestInfo.getPlatformId())
                    .withTimeoutMS((int)timeOutMS)
                    .withTestMode(isTest);

            bidRequest.getFBBid(new FBAdBidRequest.BidResponseCallback() {
                @Override
                public void handleBidResponse(final FBAdBidResponse bidResponse) {
                    BiddingResponse biddingResponse;
                    if (bidResponse != null) {
                        FacebookBidder.this.curBidResponsed = bidResponse;

                        if (bidResponse.isSuccess()) {
                            /**Currency exchange， to US dollar*/
                            biddingResponse = new BiddingResponse(FacebookBidder.class,
                                    bidResponse.getPrice(), bidResponse.getPayload(), FacebookBidder.this, curBidRequestInfo);
                        } else {
                            biddingResponse = new BiddingResponse(FacebookBidder.class,
                                    bidResponse.getErrorMessage(), FacebookBidder.this, curBidRequestInfo);
                        }
                    } else {
                        biddingResponse = new BiddingResponse(FacebookBidder.class,
                                "Facebook bid response is NULL", FacebookBidder.this, curBidRequestInfo);
                    }
                    if (callBack != null) {
                        callBack.onBiddingResponse(biddingResponse);
                    }
                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
            BiddingResponse biddingResponse = new BiddingResponse(MtgBidder.class,
                    e.getMessage(), FacebookBidder.this, curBidRequestInfo);
            if (callBack != null) {
                callBack.onBiddingResponse(biddingResponse);
            }
        }

    }

    @Override
    public void onAuctionNotification(AuctionNotification notification) {
        if (curBidResponsed != null && mContext != null) {
            if (notification.isWinner()) {
                LogUtil.i(TAG, "Facebook Bidder Wins");
                curBidResponsed.notifyWin();
            } else {
                LogUtil.i(TAG, "Facebook Bidder Loss");
                curBidResponsed.notifyLoss();
            }
        }

    }

    @Override
    public Object getAdsRender() throws FailedToGetRenderException {
        if (adBidFormat == null){
            throw new FailedToGetRenderException("Unsupported FACEBOOK AD format!");
        }

        if (mContext == null){
            throw new FailedToGetRenderException("HiBidContext == NULL!");
        }

        Object adsRender = null;
        switch (adBidFormat){
            case NATIVE:{
                adsRender = new NativeAd(mContext.getContext(), curBidResponsed.getPlacementId());
                break;
            }
            case INTERSTITIAL:{
                adsRender = new InterstitialAd(mContext.getContext(), curBidResponsed.getPlacementId());
                break;
            }
            case REWARDED_VIDEO:{
                adsRender = new RewardedVideoAd(mContext.getContext(), curBidResponsed.getPlacementId());
                break;
            }
            default:
                break;
        }
        return adsRender;
    }

    @Override
    public Object getAdBidFormat(String adType) {
        FBAdBidFormat fbAdBidFormat = null;
        switch (adType){
            case ADType.NATIVE:{
                fbAdBidFormat = FBAdBidFormat.NATIVE;
                break;
            }
            case ADType.BANNER:{
                switch (bannerSize) {
                    case "320x50":
                        fbAdBidFormat = FBAdBidFormat.BANNER_HEIGHT_50;
                    break;
                    case "320x90":
                        fbAdBidFormat = FBAdBidFormat.BANNER_HEIGHT_90;
                        break;
                    case "320x250":
                        fbAdBidFormat = FBAdBidFormat.BANNER_HEIGHT_250;
                        break;
                    default:
                        break;
                }
                break;
            }
            case ADType.INTERSTITIAL:{
                fbAdBidFormat = FBAdBidFormat.INTERSTITIAL;
                break;
            }
            case ADType.REWARDED_VIDEO:{
                fbAdBidFormat = FBAdBidFormat.REWARDED_VIDEO;
                break;
            }
            default:{
                break;
            }
        }
        return fbAdBidFormat;
    }

}
