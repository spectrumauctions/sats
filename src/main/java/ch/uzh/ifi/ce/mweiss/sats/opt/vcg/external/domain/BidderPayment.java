package ch.uzh.ifi.ce.mweiss.sats.opt.vcg.external.domain;

import ch.uzh.ifi.ce.mweiss.sats.core.model.Bidder;

/**
 * This class represents the Payment made by a single {@link Bidder}.
 *
 */
public class BidderPayment {
	private final double amount;
	public BidderPayment(double amount){
		this.amount=amount;
	}
	public double getAmount() {
		return amount;
	}
	@Override
	public String toString(){
		String returnString = "Payment: " + getAmount() +"\n";
		return returnString;
	}
}
