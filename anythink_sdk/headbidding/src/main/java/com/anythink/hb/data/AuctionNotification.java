/*
 * Copyright (C) 2019 Mintegral, Inc. All rights reserved.
 */
package com.anythink.hb.data;

/**
 * notify bidders with auction result: win/loss/timeout
 */
public class AuctionNotification {
	public enum ReasonCode {
		Win,
		Loss,
		Timeout
	}

	private AuctionNotification(boolean isWinner, ReasonCode reasonCode, String reasonDescription){
		this.isWinner = isWinner;
		this.reasonCode = reasonCode;
		this.reasonDescription = reasonDescription;
	}

	private boolean isWinner;
	private ReasonCode reasonCode;
	private String reasonDescription;

	public boolean isWinner() {
		return isWinner;
	}

	public void setWinner(boolean isWinner) {
		this.isWinner = isWinner;
	}

	public ReasonCode getReasonCode() {
		return reasonCode;
	}

	public void setReasonCode(ReasonCode reasonCode) {
		this.reasonCode = reasonCode;
	}

	public String getReasonDescription() {
		return reasonDescription;
	}

	public void setReasonDescription(String reasonDescription) {
		this.reasonDescription = reasonDescription;
	}


	public static AuctionNotification getAuctionNotification(ReasonCode reasonCode) {
		AuctionNotification auctionNotification = null;
		switch (reasonCode){
			case Win: {
				auctionNotification = new AuctionNotification(true, ReasonCode.Win, "");
				break;
			}
			case Loss: {
				auctionNotification = new AuctionNotification(false, ReasonCode.Loss, "");
				break;
			}
			case Timeout: {
				auctionNotification = new AuctionNotification(false, ReasonCode.Timeout, "");
				break;
			}
			default: {
				break;
			}
		}
		return auctionNotification;
	}
}
