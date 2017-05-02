package ch.uzh.ifi.ce.mweiss.sats.opt.vcg.external.domain;

import java.util.Set;

import ch.uzh.ifi.ce.mweiss.sats.core.bidlang.xor.XORValue;
import ch.uzh.ifi.ce.mweiss.sats.core.model.Bundle;
import ch.uzh.ifi.ce.mweiss.sats.core.model.Good;

/**
 * This class represents the allocation of goods to a single bidder.
 * It contains the set of goods allocated as well as the set of bids 
 * leading to this allocation (which in general is a subset of all the 
 * bids made by a particular bidder).
 * 
 */
public final class BidderAllocation<T extends Good> {
    private final double totalValue;
    private Bundle<Good> goods;
    private Set<XORValue<T>> acceptedBids;

    public BidderAllocation(double totalValue, Bundle<Good> goods, Set<XORValue<T>> acceptedBids) {
        this.totalValue = totalValue;
        this.goods = goods;
        this.acceptedBids = acceptedBids;
    };

    public double getTradeValue() {
        return totalValue;
    }

    public double getValue() {
        return totalValue;
    }

    public Set<Good> getGoods() {
        return goods;
    }

    @Override
    public String toString() {
        return "Trade with totalValue: " + getTradeValue();
    }

    public Set<XORValue<T>> getAcceptedBids() {
        return acceptedBids;
    }

    

}
