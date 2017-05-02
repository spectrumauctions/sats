package ch.uzh.ifi.ce.mweiss.sats.opt.vcg.external.domain;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableSet;

import ch.uzh.ifi.ce.mweiss.specval.bidlang.xor.XORBid;
import ch.uzh.ifi.ce.mweiss.specval.bidlang.xor.XORValue;
import ch.uzh.ifi.ce.mweiss.specval.model.Bidder;
import ch.uzh.ifi.ce.mweiss.specval.model.Good;

/**
 * This class represents an Auction, containing a set of goods as 
 * well as bids placed on those goods.
 *
 */
public final class Auction<T extends Good> {
	private final Bids<T> bids;
	private final Set<T> goods;

	public Auction(Bids<T> bids, Set<T> goods) {
		this.bids = bids;
		this.goods = ImmutableSet.copyOf(goods);
	}

	public Set<Bidder<T>> getBidders() {
		return bids.getBidders();
	}

	public Collection<XORBid<T>> getBidCollection() {
		return bids.getBids();
	}

	public Bids<T> getBids() {
		return bids;
	}

	/**
	 * 
	 * @param bidder
	 *            to be removed
	 * @return A new auction without the bidder
	 */

	public Auction<T> without(Bidder<T> bidder) {
		return new Auction<T>(bids.without(bidder), goods);

	}

	/**
	 * @return Unmodifiable Set of all Good in this Auction
	 */
	public Set<T> getGoods() {
		return Collections.unmodifiableSet(goods);
	}

	public XORBid<T> getBid(Bidder<T> bidder) {
		return bids.getBid(bidder);
	}

	public Auction<T> withLowBidsRemoved(double goodReservePrice) {
		List<XORBid<T>> newBids = new ArrayList<>();
		for (Bidder<T> bidder : getBidders()) {
			XORBid<T> bid = getBid(bidder);
			Set<XORValue<T>> filteredBundleBids = bid
					.getValues()
					.stream()
					.filter(bb -> bb.value().compareTo(new BigDecimal(bb.getLicenses().size() * goodReservePrice, MathContext.DECIMAL64)) >= 0)
							.collect(Collectors.toSet());
			XORBid<T> newBid = new XORBid.Builder<T>(bidder, filteredBundleBids).build();
			newBids.add(newBid);
		}
		Bids<T> newAuctionBids = new Bids<>(newBids);
		return new Auction<>(newAuctionBids, goods);
	}

}
