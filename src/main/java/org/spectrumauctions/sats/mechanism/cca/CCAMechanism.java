package org.spectrumauctions.sats.mechanism.cca;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.Good;
import org.spectrumauctions.sats.mechanism.domain.MechanismResult;
import org.spectrumauctions.sats.mechanism.domain.Payment;
import org.spectrumauctions.sats.mechanism.domain.mechanisms.AuctionMechanism;
import org.spectrumauctions.sats.opt.domain.Allocation;
import org.spectrumauctions.sats.opt.domain.WinnerDeterminator;

import java.math.BigDecimal;
import java.util.*;

public abstract class CCAMechanism<T extends Good> implements AuctionMechanism<T> {

    private static final Logger logger = LogManager.getLogger(CCAMechanism.class);

    private static final BigDecimal DEFAULT_STARTING_PRICE = BigDecimal.ZERO;
    private static final double DEFAULT_EPSILON = 0.1;

    protected List<Bidder<T>> bidders;
    protected int totalRounds = 1;
    protected BigDecimal startingPrice = DEFAULT_STARTING_PRICE;
    protected double epsilon = DEFAULT_EPSILON;
    protected MechanismResult<T> result;



    public CCAMechanism(List<Bidder<T>> bidders) {
        this.bidders = bidders;
    }

    @Override
    public abstract MechanismResult<T> getMechanismResult();

    public void setEpsilon(double epsilon) {
        this.epsilon = epsilon;
    }

    @Override
    public Payment<T> getPayment() {
        return getMechanismResult().getPayment();
    }

    @Override
    public WinnerDeterminator<T> getWdWithoutBidder(Bidder<T> bidder) {
        throw new UnsupportedOperationException("Not supported"); // FIXME: Clean up interfaces
    }

    @Override
    public Allocation<T> calculateAllocation() {
        return getMechanismResult().getAllocation();
    }

    @Override
    public WinnerDeterminator<T> copyOf() {
        throw new UnsupportedOperationException("Not supported"); // FIXME: Clean up interfaces
    }

    @Override
    public void adjustPayoffs(Map<Bidder<T>, Double> payoffs) {
        throw new UnsupportedOperationException("Not supported"); // FIXME: Clean up interfaces
    }

    public void setStartingPrice(BigDecimal startingPrice) {
        this.startingPrice = startingPrice;
    }

    public int getTotalRounds() {
        return totalRounds;
    }

}
