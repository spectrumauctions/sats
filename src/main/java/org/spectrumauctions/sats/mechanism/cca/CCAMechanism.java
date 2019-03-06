package org.spectrumauctions.sats.mechanism.cca;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.Good;
import org.spectrumauctions.sats.mechanism.PaymentRuleEnum;
import org.spectrumauctions.sats.mechanism.domain.MechanismResult;
import org.spectrumauctions.sats.mechanism.domain.Payment;
import org.spectrumauctions.sats.mechanism.domain.mechanisms.AuctionMechanism;
import org.spectrumauctions.sats.opt.domain.Allocation;
import org.spectrumauctions.sats.opt.domain.WinnerDeterminator;

import java.math.BigDecimal;
import java.util.*;

public abstract class CCAMechanism<T extends Good> implements AuctionMechanism<T> {

    private static final Logger logger = LogManager.getLogger(CCAMechanism.class);

    protected static final BigDecimal DEFAULT_STARTING_PRICE = BigDecimal.ZERO;
    private static final double DEFAULT_EPSILON = 0.1;
    private static final int DEFAULT_MAX_ROUNDS = 1000;
    private static final int DEFAULT_TIME_LIMIT = 600;
    private static final int DEFAULT_REL_RESULT_POOL_TOLERANCE = 0;
    private static final int DEFAULT_ABS_RESULT_POOL_TOLERANCE = 0;
    private static final int DEFAULT_CLOCKPHASE_NUMBER_OF_BUNDLES = 1;

    protected List<Bidder<T>> bidders;
    protected int totalRounds = 1;
    protected BigDecimal fallbackStartingPrice = DEFAULT_STARTING_PRICE;
    protected double epsilon = DEFAULT_EPSILON;
    protected int maxRounds = DEFAULT_MAX_ROUNDS;
    protected double timeLimit = DEFAULT_TIME_LIMIT;

    protected double relativeResultPoolTolerance = DEFAULT_REL_RESULT_POOL_TOLERANCE;
    protected double absoluteResultPoolTolerance = DEFAULT_ABS_RESULT_POOL_TOLERANCE;
    protected PaymentRuleEnum paymentRule = PaymentRuleEnum.VCG;

    // The number of bundles returned in a demand query in the clock phase
    protected int clockPhaseNumberOfBundles = DEFAULT_CLOCKPHASE_NUMBER_OF_BUNDLES;

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

    public void setFallbackStartingPrice(BigDecimal fallbackStartingPrice) {
        this.fallbackStartingPrice = fallbackStartingPrice;
    }

    public void calculateSampledStartingPrices(int bidsPerBidder, int numberOfWorldSamples, double fraction) {
        calculateSampledStartingPrices(bidsPerBidder, numberOfWorldSamples, fraction, System.currentTimeMillis());
    }

    /**
     * This method allows to simulate a certain knowledge about the player's values.
     * It can be compared to the auctioneer conducting some research before the auction about the bidder's preferences.
     * That way, the starting prices can be adjusted to speed up the auction without sacrificing efficiency.
     * This is simulated by drawing a number of bids from newly created (but based on the same value distributions)
     * bidders
     *
     * @param bidsPerBidder         How many bids are collected per bidder in each world
     * @param numberOfWorldSamples  How many parallel worlds are created to collect these bids
     * @param fraction              The fraction of the estimated value that is turned into the starting price
     * @param seed                  The seed used for drawing the values
     */
    public abstract void calculateSampledStartingPrices(int bidsPerBidder, int numberOfWorldSamples, double fraction, long seed);

    public void setMaxRounds(int maxRounds) {
        this.maxRounds = maxRounds;
    }

    public double getTimeLimit() {
        return timeLimit;
    }

    /**
     * This time limit (in seconds) is applied to all the demand queries
     */
    public void setTimeLimit(double timeLimit) {
        this.timeLimit = timeLimit;
    }

    public double getAbsoluteResultPoolTolerance() {
        return absoluteResultPoolTolerance;
    }


    public double getRelativeResultPoolTolerance() {
        return relativeResultPoolTolerance;
    }

    /**
     * In some cases in CCA (e.g., the profit maximizing supplementary round), we're interested in a collection of
     * most optimal results.
     * Since that can take a long time and setting a time limit is sometimes not the best option to solve this,
     * the user can specify a result pool tolerance.
     *
     * This tolerance defines "how far the worst solution is allowed to be from the best solution to stop looking
     * for other solutions and return the current collection of solutions".
     *
     */
    public void setAbsoluteResultPoolTolerance(double absoluteResultPoolTolerance) {
        this.absoluteResultPoolTolerance = absoluteResultPoolTolerance;
    }

    /**
     *  Same as {@link #setAbsoluteResultPoolTolerance(double)}, but in a relative way (e.g., 0.1 for 10% tolerance).
     *  This is usually more helpful than the absolute tolerance, but if the best solution has an objective value of 0,
     *  the relative tolerance won't work.
     */
    public void setRelativeResultPoolTolerance(double relativeResultPoolTolerance) {
        this.relativeResultPoolTolerance = relativeResultPoolTolerance;
    }

    public void setClockPhaseNumberOfBundles(int clockPhaseNumberOfBundles) {
        this.clockPhaseNumberOfBundles = clockPhaseNumberOfBundles;
    }

    public int getTotalRounds() {
        return totalRounds;
    }

    public void setPaymentRule(PaymentRuleEnum paymentRule) {
        this.paymentRule = paymentRule;
    }

    public double getEpsilon() {
        return epsilon;
    }
}
