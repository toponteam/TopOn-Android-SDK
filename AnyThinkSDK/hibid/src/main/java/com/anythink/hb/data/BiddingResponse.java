/*
 * Copyright (C) 2019 Mintegral, Inc. All rights reserved.
 */
package com.anythink.hb.data;

import com.anythink.hb.Bidder;

/**
 * Response for one bidder's bid request
 */
public class BiddingResponse implements Comparable<BiddingResponse>{

	private Class bidderClass;
	private double biddingPriceUSD;
    private Object payload;
	private Bidder bidder;
	private BidRequestInfo bidRequestInfo;
	private String errorMessage;

	public BiddingResponse(){

	}

	public BiddingResponse(Class bidderClass, double biddingPriceUSD, Object payload,
						   Bidder bidder, BidRequestInfo bidRequestInfo) {
		this.bidderClass = bidderClass;
		this.biddingPriceUSD = biddingPriceUSD;
		this.payload = payload;
		this.bidder = bidder;
		this.bidRequestInfo = bidRequestInfo;
	}

	public BiddingResponse(Class bidderClass, String errorMessage, Bidder bidder, BidRequestInfo bidRequestInfo) {
		this.bidderClass = bidderClass;
		this.errorMessage = errorMessage;
		this.bidder = bidder;
		this.bidRequestInfo = bidRequestInfo;
	}

	public Class getBidderClass() {
		return bidderClass;
	}

	public void setBidderClass(Class bidderClass) {
		this.bidderClass = bidderClass;
	}

	public double getBiddingPriceUSD() {
		return biddingPriceUSD;
	}

	public void setBiddingPriceUSD(double biddingPriceUSD) {
		this.biddingPriceUSD = biddingPriceUSD;
	}

	public Object getPayload() {
		return payload;
	}

	public void setPayload(Object payload) {
		this.payload = payload;
	}

	public Bidder getBidder() {
		return bidder;
	}

	public void setBidder(Bidder bidder) {
		this.bidder = bidder;
	}

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

	public BidRequestInfo getBidRequestInfo() {
		return bidRequestInfo;
	}

	public void setBidRequestInfo(BidRequestInfo bidRequestInfo) {
		this.bidRequestInfo = bidRequestInfo;
	}

	@Override
	public int compareTo(BiddingResponse other) {
		if (this.biddingPriceUSD > other.getBiddingPriceUSD()) {
			return -1;
		} else if (this.biddingPriceUSD == other.getBiddingPriceUSD()) {
			return 0;
		} else {
			return 1;
		}
	}


}
