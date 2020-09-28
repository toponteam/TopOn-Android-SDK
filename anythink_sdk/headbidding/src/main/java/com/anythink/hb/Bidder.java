/*
 * Copyright (C) 2019 Mintegral, Inc. All rights reserved.
 */
package com.anythink.hb;

import com.anythink.hb.callback.BiddingCallback;
import com.anythink.hb.data.AuctionNotification;
import com.anythink.hb.data.BidRequestInfo;
import com.anythink.hb.data.HBDataContext;
import com.anythink.hb.exception.BidderInitFailedException;
import com.anythink.hb.exception.BiddingException;
import com.anythink.hb.exception.FailedToGetRenderException;
import com.anythink.hb.exception.SdkIntegratedException;

public interface Bidder {

	/**
	 * return Bidder Class
	 * @return
	 */
	Class getBidderClass();

	/**
	 * return Bidder Class
	 * @return
	 */
	BidRequestInfo getBidderRequestInfo();

	/**
	 *  bidder sdk init
	 * @param biddingContext
	 * @throws BidderInitFailedException
	 */
	void init(HBDataContext biddingContext) throws BidderInitFailedException, SdkIntegratedException;

	/**
	 * bid request
	 * @param bidRequestInfo request parameters
	 * @param adType advertisement type
	 * @param timeOutMS time out in milliseconds
	 * @param callBack bid request callback
	 * @throws BiddingException
	 */
	void bid(BidRequestInfo bidRequestInfo,
			 String adType, long timeOutMS, BiddingCallback callBack) throws BiddingException;

	/**
	 * bid request notification
	 * @param notification
	 */
	void onAuctionNotification(AuctionNotification notification);

	/**
	 * return ad object
	 * @return
	 * @throws FailedToGetRenderException
	 */
	Object getAdsRender() throws FailedToGetRenderException;

	/**
	 * return ad format, eg. facebook ad format, mtg ad format...
	 * @param adType
	 * @return
	 */
	Object getAdBidFormat(String adType);

}
