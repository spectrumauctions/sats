package org.spectrumauctions.sats.opt.vcg.external.domain;

import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.Bundle;
import org.spectrumauctions.sats.core.model.Good;
import org.spectrumauctions.sats.opt.model.Allocation;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * This class represents the Allocation after a WinnerDetermination. It contains
 * the total welfare of the Allocation as well as a Map of Bidders to their
 * trades.</p> Each winning bidder has exactly one associated trade. Non winning
 * bidders are not included
 *
 * @author Benedikt Buenz
 * @author Michael Weiss
 */
public class XORAllocation<T extends Good> implements Allocation<BidderAllocation<T>> {

    private final double totalValue;
    private final Map<Bidder<T>, BidderAllocation<T>> trades;

    /**
     * @param totalValue
     * @param trades     map of winning bidders to allocations
     * @param solveTime
     */

    public XORAllocation(Map<Bidder<T>, BidderAllocation<T>> trades) {
        double totalValue = 0;
        for (BidderAllocation<T> allocation : trades.values()) {
            totalValue += allocation.getTradeValue();
        }
        this.totalValue = totalValue;
        this.trades = trades;
    }

    public double getTotalAllocationValue() {
        return totalValue;
    }

    public Collection<BidderAllocation<T>> getTrades() {
        return trades.values();
    }

    /**
     * The Map only includes winning bidders
     *
     * @return
     */
    public Map<Bidder<T>, BidderAllocation<T>> getTradesMap() {
        return trades;
    }

    public boolean isWinner(Bidder bidder) {
        return trades.containsKey(bidder);
    }

    @Deprecated
    public BidderAllocation tradeOf(Bidder bidder) {
        return trades.get(bidder);
    }

    public Set<Bidder<T>> getWinners() {
        return trades.keySet();
    }


    @Override
    public String toString() {
        return "Allocation[trades=" + trades + "]";

    }

    @Override
    public Collection<? extends Bidder<?>> getBidders() {
        return trades.keySet();
    }

    @Override
    public BidderAllocation<T> getAllocation(Bidder<?> bidder) {
        BidderAllocation<T> result = trades.get(bidder);
        if (result == null) {
            return new BidderAllocation<>(0, new Bundle<>(), new HashSet<>());
        }
        return result;
    }

    @Override
    public BigDecimal getTotalValue() {
        return BigDecimal.valueOf(totalValue);
    }


}
