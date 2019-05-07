package org.spectrumauctions.sats.mechanism.cca;

import com.google.common.base.Preconditions;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spectrumauctions.sats.core.bidlang.xor.SizeBasedUniqueRandomXOR;
import org.spectrumauctions.sats.core.bidlang.xor.XORBid;
import org.spectrumauctions.sats.core.bidlang.xor.XORValue;
import org.spectrumauctions.sats.core.model.*;
import org.spectrumauctions.sats.core.util.random.JavaUtilRNGSupplier;
import org.spectrumauctions.sats.core.util.random.RNGSupplier;
import org.spectrumauctions.sats.mechanism.cca.priceupdate.NonGenericPriceUpdater;
import org.spectrumauctions.sats.mechanism.cca.priceupdate.SimpleRelativeNonGenericPriceUpdate;
import org.spectrumauctions.sats.mechanism.cca.supplementaryround.NonGenericSupplementaryRound;
import org.spectrumauctions.sats.mechanism.cca.supplementaryround.ProfitMaximizingNonGenericSupplementaryRound;
import org.spectrumauctions.sats.mechanism.ccg.CCGMechanism;
import org.spectrumauctions.sats.mechanism.domain.MechanismResult;
import org.spectrumauctions.sats.mechanism.domain.Payment;
import org.spectrumauctions.sats.mechanism.domain.mechanisms.AuctionMechanism;
import org.spectrumauctions.sats.mechanism.vcg.VCGMechanism;
import org.spectrumauctions.sats.opt.domain.Allocation;
import org.spectrumauctions.sats.opt.domain.NonGenericDemandQueryMIP;
import org.spectrumauctions.sats.opt.domain.NonGenericDemandQueryMIPBuilder;
import org.spectrumauctions.sats.opt.domain.NonGenericDemandQueryResult;
import org.spectrumauctions.sats.opt.xor.XORWinnerDetermination;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static jdk.nashorn.internal.objects.Global.Infinity;

public class NonGenericCCAMechanism<T extends Good> extends CCAMechanism<T> {

    private static final Logger logger = LogManager.getLogger(NonGenericCCAMechanism.class);

    private NonGenericDemandQueryMIPBuilder<T> demandQueryMIPBuilder;
    private Map<Good, BigDecimal> startingPrices = new HashMap<>();

    private NonGenericPriceUpdater<T> priceUpdater = new SimpleRelativeNonGenericPriceUpdate<>();
    private List<NonGenericSupplementaryRound<T>> supplementaryRounds = new ArrayList<>();

    private Collection<XORBid<T>> bidsAfterClockPhase;
    private Collection<XORBid<T>> bidsAfterSupplementaryRound;

    private Map<T, BigDecimal> finalPrices;
    private Map<T, Integer> finalDemand;

    public NonGenericCCAMechanism(List<Bidder<T>> bidders, NonGenericDemandQueryMIPBuilder<T> nonGenericDemandQueryMIPBuilder) {
        super(bidders);
        this.demandQueryMIPBuilder = nonGenericDemandQueryMIPBuilder;
    }

    @Override
    public MechanismResult<T> getMechanismResult() {
        if (result != null) return result;
        if (bidsAfterClockPhase == null) {
            logger.info("Starting clock phase for XOR bids...");
            bidsAfterClockPhase = runClockPhase();
        }
        if (bidsAfterSupplementaryRound == null) {
            logger.info("Starting to collect bids for supplementary round...");
            bidsAfterSupplementaryRound = runSupplementaryRound();
        }
        logger.info("Starting to calculate payments with all collected bids...");
        result = calculatePayments();
        return result;
    }

    @Override
    public Payment<T> recomputePayments() {
        return calculatePayments().getPayment();
    }


    public void setStartingPrice(Good good, BigDecimal price) {
        startingPrices.put(good, price);
    }

    @Override
    public void calculateSampledStartingPrices(int bidsPerBidder, int numberOfWorldSamples, double fraction, long seed) {
        World world = bidders.stream().findAny().map(Bidder::getWorld).orElseThrow(NoSuchFieldError::new);
        try {
            Map<Good, SimpleRegression> regressions = new HashMap<>();
            for (Good good : world.getLicenses()) {
                SimpleRegression regression = new SimpleRegression(false);
                regression.addData(0.0, 0.0);
                regressions.put(good, regression);
            }

            RNGSupplier rngSupplier = new JavaUtilRNGSupplier(seed);
            for (int i = 0; i < numberOfWorldSamples; i++) {
                List<Bidder<T>> alternateBidders = bidders.stream().map(b -> b.drawSimilarBidder(rngSupplier)).collect(Collectors.toList());
                for (Bidder<T> bidder : alternateBidders) {
                    SizeBasedUniqueRandomXOR valueFunction;
                    valueFunction = bidder.getValueFunction(SizeBasedUniqueRandomXOR.class, rngSupplier);
                    valueFunction.setIterations(bidsPerBidder);

                    Iterator<XORValue<T>> bidIterator = valueFunction.iterator();
                    while (bidIterator.hasNext()) {
                        XORValue<T> bid = bidIterator.next();
                        Bundle<T> bundle = bid.getLicenses();
                        BigDecimal value = bid.value();
                        for (Good good : bundle) {
                            double y = value.doubleValue() / bundle.size();
                            regressions.get(good).addData(1.0, y);
                        }
                    }
                }
            }

            double min = Infinity;

            for (Map.Entry<Good, SimpleRegression> entry : regressions.entrySet()) {
                double y = entry.getValue().predict(1);
                double price = y * fraction;
                logger.info("{}:\nFound prediction of {}, setting starting price to {}.",
                        entry.getKey(), y, price);
                setStartingPrice(entry.getKey(), BigDecimal.valueOf(price));
                if (price > 0 && price < min) min = price;
            }

            // If ever a prediction turns out to be zero or negative (which should almost never be the case)
            // it is set to the smallest prediction of the other goods to avoid getting stuck in CCA
            for (Good license : regressions.keySet()) {
                if (startingPrices.get(license).compareTo(BigDecimal.ZERO) < 1) {
                    setStartingPrice(license, BigDecimal.valueOf(min));
                }
            }

        } catch (UnsupportedBiddingLanguageException e) {
            // Catching this error here, because it's very unlikely to happen and we don't want to bother
            // the user with handling this error. We just log it and don't set the starting prices.
            logger.error("Tried to calculate sampled starting prices, but {} doesn't support the " +
                    "SizeBasedUniqueRandomXOR bidding language. Not setting any starting prices.", world);
        }
    }

    @Override
    public CCAMechanism<T> cloneWithoutSupplementaryBids() {
        NonGenericCCAMechanism<T> clone = new NonGenericCCAMechanism<>(bidders, demandQueryMIPBuilder);
        clone.priceUpdater = priceUpdater;
        clone.absoluteResultPoolTolerance = absoluteResultPoolTolerance;
        clone.relativeResultPoolTolerance = relativeResultPoolTolerance;
        clone.clockPhaseNumberOfBundles = clockPhaseNumberOfBundles;
        clone.epsilon = epsilon;
        clone.fallbackStartingPrice = fallbackStartingPrice;
        clone.maxRounds = maxRounds;
        clone.paymentRule = paymentRule;
        clone.timeLimit = timeLimit;
        clone.bidsAfterClockPhase = bidsAfterClockPhase;
        clone.finalPrices = finalPrices;
        clone.finalDemand = finalDemand;
        clone.totalRounds = totalRounds;
        clone.startingPrices = startingPrices;
        return clone;
    }

    public Allocation<T> calculateClockPhaseAllocation() {
        if (bidsAfterClockPhase == null) {
            logger.info("Starting clock phase for XOR bids...");
            bidsAfterClockPhase = runClockPhase();
        }
        Set<XORBid<T>> bids = new HashSet<>(bidsAfterClockPhase);

        XORWinnerDetermination<T> wdp = new XORWinnerDetermination<>(bids);
        return wdp.calculateAllocation();
    }

    public Allocation<T> calculateAllocationAfterSupplementaryRound() {
        if (bidsAfterClockPhase == null) {
            logger.info("Starting clock phase for XOR bids...");
            bidsAfterClockPhase = runClockPhase();
        }
        if (bidsAfterSupplementaryRound == null) {
            logger.info("Starting to collect bids for supplementary round...");
            bidsAfterSupplementaryRound = runSupplementaryRound();
        }
        Set<XORBid<T>> bids = new HashSet<>(bidsAfterSupplementaryRound);

        XORWinnerDetermination<T> wdp = new XORWinnerDetermination<>(bids);
        return wdp.calculateAllocation();
    }

    private Collection<XORBid<T>> runClockPhase() {
        Map<Bidder<T>, XORBid<T>> bids = new HashMap<>();
        bidders.forEach(bidder -> bids.put(bidder, new XORBid.Builder<>(bidder).build()));
        Map<T, BigDecimal> prices = new HashMap<>();
        for (Good good : bidders.stream().findFirst().orElseThrow(IncompatibleWorldException::new).getWorld().getLicenses()) {
            prices.put((T) good, startingPrices.getOrDefault(good, fallbackStartingPrice));
        }

        Map<T, Integer> demand;
        boolean done = false;
        while (!done) {
            Map<T, BigDecimal> currentPrices = prices; // For lambda use
            demand = new HashMap<>();

            for (Bidder<T> bidder : bidders) {
                NonGenericDemandQueryMIP<T> demandQueryMIP = demandQueryMIPBuilder.getDemandQueryMipFor(bidder, prices, epsilon);
                demandQueryMIP.setTimeLimit(getTimeLimit());
                List<? extends NonGenericDemandQueryResult<T>> demandQueryResults = demandQueryMIP.getResultPool(clockPhaseNumberOfBundles);
                Bundle<T> firstBundle = demandQueryResults.get(0).getResultingBundle().getLicenses();
                if (firstBundle.size() > 0) {
                    for (T good : firstBundle) {
                        demand.put(good, demand.getOrDefault(good, 0) + 1);
                    }
                }
                for (NonGenericDemandQueryResult<T> demandQueryResult : demandQueryResults) {
                    if (demandQueryResult.getResultingBundle().getLicenses().size() > 0) {
                        Bundle<T> bundle = demandQueryResult.getResultingBundle().getLicenses();

                        XORBid.Builder<T> xorBidBuilder = new XORBid.Builder<>(bidder, bids.get(bidder).getValues());
                        BigDecimal bid = BigDecimal.valueOf(bundle.stream().mapToDouble(l -> currentPrices.get(l).doubleValue()).sum());
                        XORValue<T> existing = xorBidBuilder.containsBundle(bundle);
                        if (existing != null && existing.value().compareTo(bid) < 1) {
                            xorBidBuilder.removeFromBid(existing);
                        }
                        if (existing == null || existing.value().compareTo(bid) < 0) {
                            xorBidBuilder.add(new XORValue<>(bundle, bid));
                        }

                        XORBid<T> newBid = xorBidBuilder.build();
                        bids.put(bidder, newBid);
                    }
                }
            }
            Map<T, BigDecimal> updatedPrices = priceUpdater.updatePrices(prices, demand);
            if (prices.equals(updatedPrices) || totalRounds >= maxRounds) {
                done = true;
                finalDemand = demand;
                finalPrices = prices;
            } else {
                prices = updatedPrices;
                if (logger.isInfoEnabled()) {
                    int aggregateDemand = 0;
                    int supply = 0;
                    for (Map.Entry<T, BigDecimal> priceEntry : prices.entrySet()) {
                        T def = priceEntry.getKey();
                        aggregateDemand += demand.getOrDefault(def, 0);
                        supply += 1;
                    }
                    logger.info("Round: {} - Demand: {} - Supply: {}", totalRounds, aggregateDemand, supply);
                }
                totalRounds++;
            }
        }
        bidsAfterClockPhase = bids.values();
        return bidsAfterClockPhase;
    }

    private Collection<XORBid<T>> runSupplementaryRound() {
        Collection<XORBid<T>> bids = new HashSet<>();
        if (supplementaryRounds.isEmpty())
            supplementaryRounds.add(new ProfitMaximizingNonGenericSupplementaryRound<>());

        for (Bidder<T> bidder : bidders) {
            List<XORValue<T>> newValues = new ArrayList<>();
            for (NonGenericSupplementaryRound<T> supplementaryRound : supplementaryRounds) {
                newValues.addAll(supplementaryRound.getSupplementaryBids(this, bidder));
            }


            XORBid<T> bidderBid = bidsAfterClockPhase.stream().filter(bid -> bidder.equals(bid.getBidder())).findFirst().orElseThrow(NoSuchElementException::new);

            XORBid<T> newBid = bidderBid.copyOfWithNewValues(newValues);
            bids.add(newBid);
        }
        bidsAfterSupplementaryRound = bids;
        return bids;
    }

    private MechanismResult<T> calculatePayments() {
        Set<XORBid<T>> bids = new HashSet<>(bidsAfterSupplementaryRound);
        XORWinnerDetermination<T> wdp = new XORWinnerDetermination<>(bids);
        AuctionMechanism<T> mechanism;
        switch (paymentRule) {
            case CCG:
                mechanism = new CCGMechanism<>(wdp);
                break;
            case VCG:
            default:
                mechanism = new VCGMechanism<>(wdp);
                break;
        }
        result = mechanism.getMechanismResult();
        return result;
    }

    public int getSupplyMinusDemand() {
        World world = bidders.iterator().next().getWorld();
        Set<T> licenses = (Set<T>) world.getLicenses();
        int aggregateDemand = 0;
        int supply = 0;
        for (T def : licenses) {
            aggregateDemand += finalDemand.getOrDefault(def, 0);
            supply++;
        }
        return supply - aggregateDemand;
    }

    public Collection<XORBid<T>> getBidsAfterClockPhase() {
        if (bidsAfterClockPhase == null) {
            runClockPhase();
        }
        return bidsAfterClockPhase;
    }

    public Collection<XORBid<T>> getBidsAfterSupplementaryRound() {
        if (bidsAfterClockPhase == null) {
            runClockPhase();
        }
        if (bidsAfterSupplementaryRound == null) {
            runSupplementaryRound();
        }
        return bidsAfterSupplementaryRound;
    }

    public Map<Bidder<T>, Integer> getXORBidsCount() {
        Map<Bidder<T>, Integer> map = new HashMap<>();
        bidsAfterClockPhase.forEach(bid -> map.put(bid.getBidder(), bid.getValues().size()));
        return map;
    }

    public void setPriceUpdater(NonGenericPriceUpdater<T> nonGenericPriceUpdater) {
        Preconditions.checkArgument(bidsAfterClockPhase == null, "Already ran clock phase! Set the price updater before.");
        this.priceUpdater = nonGenericPriceUpdater;
    }

    public void addSupplementaryRound(NonGenericSupplementaryRound<T> nonGenericSupplementaryRound) {
        Preconditions.checkArgument(bidsAfterSupplementaryRound == null, "Already ran supplementary round!");
        this.supplementaryRounds.add(nonGenericSupplementaryRound);
    }

    public Map<Bidder<T>, Integer> getBidCountAfterClockPhase() {
        Map<Bidder<T>, Integer> map = new HashMap<>();
        bidsAfterClockPhase.forEach(bid -> map.put(bid.getBidder(), bid.getValues().size()));
        return map;
    }

    public Map<Bidder<T>, Integer> getBidCountAfterSupplementaryRound() {
        Map<Bidder<T>, Integer> map = new HashMap<>();
        bidsAfterSupplementaryRound.forEach(bid -> map.put(bid.getBidder(), bid.getValues().size()));
        return map;
    }

    public NonGenericDemandQueryMIPBuilder<T> getDemandQueryBuilder() {
        return this.demandQueryMIPBuilder;
    }

    public Map<T, BigDecimal> getFinalPrices() {
        return finalPrices;
    }

    public Map<T, BigDecimal> getLastPrices() {
        return priceUpdater.getLastPrices();
    }

    public XORBid<T> getBidAfterClockPhase(Bidder<T> bidder) {
        for (XORBid<T> bid : bidsAfterClockPhase) {
            if (bid.getBidder().equals(bidder)) return bid;
        }
        logger.warn("Couldn't find a bid for bidder {} after clock phase.", bidder);
        return null;
    }


    public XORBid<T> getBidAfterSupplementaryRound(Bidder<T> bidder) {
        for (XORBid<T> bid : bidsAfterSupplementaryRound) {
            if (bid.getBidder().equals(bidder)) return bid;
        }
        logger.warn("Couldn't find a bid for bidder {} after supplementary round.", bidder);
        return null;
    }

    public BigDecimal getStartingPrice(T license) {
        return startingPrices.getOrDefault(license, fallbackStartingPrice);
    }

    @Override
    public double getScale() {
        return 1;
    }
}
