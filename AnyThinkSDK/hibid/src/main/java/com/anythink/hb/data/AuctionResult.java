/*
 * Copyright (C) 2019 Mintegral, Inc. All rights reserved.
 */
package com.anythink.hb.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Auction result for one runtime bidding
 */
public class AuctionResult {
	private String transactionId = "";
	private String unitId = "";
	private BiddingResponse winner = null;
	private List<BiddingResponse> otherBidders = new ArrayList<BiddingResponse>();

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public String getUnitId() {
		return unitId;
	}

	public void setUnitId(String unitId) {
		this.unitId = unitId;
	}

    /**
     *  Winner's price must be greater than zero
     * @return
     */
	public BiddingResponse getWinner(){
		return winner;
	}

	public void setWinner(BiddingResponse winner) {
		this.winner = winner;
	}

	public List<BiddingResponse> getOtherBidders() {
		return otherBidders;
	}

	public void setOtherBidders(List<BiddingResponse> otherBidders) {
		this.otherBidders = otherBidders;
	}

}
