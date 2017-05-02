package ch.uzh.ifi.ce.mweiss.sats.opt.vcg.external.vcg;

import java.util.HashMap;
import java.util.Map;

import ch.uzh.ifi.ce.mweiss.sats.opt.vcg.external.domain.Auction;
import ch.uzh.ifi.ce.mweiss.sats.opt.vcg.external.domain.BidderPayment;
import ch.uzh.ifi.ce.mweiss.sats.opt.vcg.external.domain.Payment;
import ch.uzh.ifi.ce.mweiss.specval.model.Bidder;
import ch.uzh.ifi.ce.mweiss.specval.model.Good;
import ch.uzh.ifi.ce.mweiss.sats.opt.vcg.external.domain.AuctionResult;

public class ReservePriceVCGAuction<T extends Good> extends XORVCGAuction<T> {
	private double goodReservePrice;
	public ReservePriceVCGAuction(Auction<T> auction, double goodReservePrice) {
		super(auction);
		this.goodReservePrice=goodReservePrice;
		
	}
	@Override
	 protected AuctionResult<T> calculateVCGPrices(Auction<T> auction){
		Auction<T> adaptedAuction = auction.withLowBidsRemoved(goodReservePrice);
		AuctionResult<T> result= super.calculateVCGPrices(adaptedAuction);
		Map<Bidder<T>,BidderPayment> newPaymentMap = new HashMap<>(result.getAllocation().getWinners().size());
		for(Bidder<T> winner : result.getAllocation().getWinners()){
			double vcgPayment=result.getPayment().paymentOf(winner).getAmount();
			double reservePrice = result.getAllocation().getAllocation(winner).getGoods().size()*goodReservePrice;
			double newPrice = Math.max(vcgPayment, reservePrice);
			newPaymentMap.put(winner, new BidderPayment(newPrice));
		}
		Payment<T> newPayment = new Payment<>(newPaymentMap);
		return new AuctionResult<T>(newPayment, result.getAllocation());
	}

}
