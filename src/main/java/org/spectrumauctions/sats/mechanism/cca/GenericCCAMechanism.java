package org.spectrumauctions.sats.mechanism.cca;

import com.google.common.base.Preconditions;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spectrumauctions.sats.core.bidlang.generic.GenericBid;
import org.spectrumauctions.sats.core.bidlang.generic.GenericDefinition;
import org.spectrumauctions.sats.core.bidlang.generic.GenericValue;
import org.spectrumauctions.sats.core.bidlang.generic.SimpleRandomOrder.XORQRandomOrderSimple;
import org.spectrumauctions.sats.core.model.*;
import org.spectrumauctions.sats.core.util.random.JavaUtilRNGSupplier;
import org.spectrumauctions.sats.core.util.random.RNGSupplier;
import org.spectrumauctions.sats.mechanism.cca.priceupdate.GenericPriceUpdater;
import org.spectrumauctions.sats.mechanism.cca.priceupdate.SimpleRelativeGenericPriceUpdate;
import org.spectrumauctions.sats.mechanism.cca.supplementaryround.ProfitMaximizingGenericSupplementaryRound;
import org.spectrumauctions.sats.mechanism.cca.supplementaryround.GenericSupplementaryRound;
import org.spectrumauctions.sats.mechanism.ccg.CCGMechanism;
import org.spectrumauctions.sats.mechanism.domain.MechanismResult;
import org.spectrumauctions.sats.mechanism.domain.Payment;
import org.spectrumauctions.sats.mechanism.domain.mechanisms.AuctionMechanism;
import org.spectrumauctions.sats.mechanism.vcg.VCGMechanism;
import org.spectrumauctions.sats.opt.domain.Allocation;
import org.spectrumauctions.sats.opt.domain.GenericDemandQueryMIP;
import org.spectrumauctions.sats.opt.domain.GenericDemandQueryMIPBuilder;
import org.spectrumauctions.sats.opt.domain.GenericDemandQueryResult;
import org.spectrumauctions.sats.opt.xorq.XORQWinnerDetermination;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static jdk.nashorn.internal.objects.Global.Infinity;

public class GenericCCAMechanism<G extends GenericDefinition<T>, T extends Good> extends CCAMechanism<T> {

    private static final Logger logger = LogManager.getLogger(GenericCCAMechanism.class);

    private Collection<GenericBid<G, T>> bidsAfterClockPhase;
    private Collection<GenericBid<G, T>> bidsAfterSupplementaryRound;

    private Map<G, BigDecimal> startingPrices = new HashMap<>();

    private Map<G, BigDecimal> finalPrices;
    private Map<G, Integer> finalDemand;

    private GenericDemandQueryMIPBuilder<G, T> genericDemandQueryMIPBuilder;
    private GenericPriceUpdater<G, T> priceUpdater = new SimpleRelativeGenericPriceUpdate<>();
    private List<GenericSupplementaryRound<G, T>> supplementaryRounds = new ArrayList<>();

    public GenericCCAMechanism(List<Bidder<T>> bidders, GenericDemandQueryMIPBuilder<G, T> genericDemandQueryMIPBuilder) {
        super(bidders);
        this.genericDemandQueryMIPBuilder = genericDemandQueryMIPBuilder;
    }

    public void setStartingPrice(G good, BigDecimal price) {
        startingPrices.put(good, price);
    }

    @Override
    public MechanismResult<T> getMechanismResult() {
        if (result != null) return result;

        if (bidsAfterClockPhase == null) {
            logger.info("Starting clock phase for generic bids...");
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

    @Override
    public void calculateSampledStartingPrices(int bidsPerBidder, int numberOfWorldSamples, double fraction, long seed) {
        GenericWorld<T> world = (GenericWorld<T>) bidders.stream().findAny().map(Bidder::getWorld).orElseThrow(NoSuchFieldError::new);
        try {
            Map<G, SimpleRegression> regressions = new HashMap<>();
            for (GenericDefinition<T> genericDefinition : world.getAllGenericDefinitions()) {
                SimpleRegression regression = new SimpleRegression(false);
                regression.addData(0.0, 0.0);
                regressions.put((G) genericDefinition, regression);
            }

            RNGSupplier rngSupplier = new JavaUtilRNGSupplier(seed);
            for (int i = 0; i < numberOfWorldSamples; i++) {
                List<Bidder<T>> alternateBidders = bidders.stream().map(b -> b.drawSimilarBidder(rngSupplier)).collect(Collectors.toList());
                for (Bidder<T> bidder : alternateBidders) {
                    XORQRandomOrderSimple<G, T> valueFunction;
                    valueFunction = (XORQRandomOrderSimple) bidder.getValueFunction(XORQRandomOrderSimple.class, rngSupplier);
                    valueFunction.setIterations(bidsPerBidder);

                    Iterator<? extends GenericValue<G, T>> bidIterator = valueFunction.iterator();
                    while (bidIterator.hasNext()) {
                        GenericValue<G, T> bid = bidIterator.next();
                        for (Map.Entry<G, Integer> entry : bid.getQuantities().entrySet()) {
                            double y = bid.getValue().doubleValue() * entry.getValue() / bid.getTotalQuantity();
                            regressions.get(entry.getKey()).addData(entry.getValue().doubleValue(), y);
                        }
                    }
                }
            }
            double min = Infinity;
            for (Map.Entry<G, SimpleRegression> entry : regressions.entrySet()) {
                double y = entry.getValue().predict(1);
                double price = y * fraction;
                logger.info("{}:\nFound prediction of {}, setting starting price to {}.",
                        entry.getKey(), y, price);
                setStartingPrice(entry.getKey(), BigDecimal.valueOf(price));
                if (price > 0 && price < min) min = price;
            }

            // If ever a prediction turns out to be zero or negative (which should almost never be the case)
            // it is set to the smallest prediction of the other goods to avoid getting stuck in CCA
            for (G def : regressions.keySet()) {
                if (startingPrices.get(def).compareTo(BigDecimal.ZERO) < 1) {
                    setStartingPrice(def, BigDecimal.valueOf(min));
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
        GenericCCAMechanism<G, T> clone = new GenericCCAMechanism<>(bidders, genericDemandQueryMIPBuilder);
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
            logger.info("Starting clock phase for generic bids...");
            bidsAfterClockPhase = runClockPhase();
        }
        Set<GenericBid<G, T>> bids = new HashSet<>(bidsAfterClockPhase);

        XORQWinnerDetermination<G, T> wdp = new XORQWinnerDetermination<>(bids);
        return wdp.calculateAllocation();
    }

    public Allocation<T> calculateAllocationAfterSupplementaryRound() {
        if (bidsAfterClockPhase == null) {
            logger.info("Starting clock phase for generic bids...");
            bidsAfterClockPhase = runClockPhase();
        }
        if (bidsAfterSupplementaryRound == null) {
            logger.info("Starting to collect bids for supplementary round...");
            bidsAfterSupplementaryRound = runSupplementaryRound();
        }
        Set<GenericBid<G, T>> bids = new HashSet<>(bidsAfterSupplementaryRound);

        XORQWinnerDetermination<G, T> wdp = new XORQWinnerDetermination<>(bids);
        return wdp.calculateAllocation();
    }

    public Collection<GenericBid<G, T>> getBidsAfterSupplementaryRound() {
        if (bidsAfterClockPhase == null) {
            runClockPhase();
        }
        if (bidsAfterSupplementaryRound == null) {
            runSupplementaryRound();
        }
        return bidsAfterSupplementaryRound;
    }

    private Collection<GenericBid<G, T>> runClockPhase() {
        Map<Bidder<T>, GenericBid<G, T>> bids = new HashMap<>();
        GenericWorld<T> world = (GenericWorld<T>) bidders.iterator().next().getWorld();
        Map<G, BigDecimal> prices = new HashMap<>();
        Set<G> genericDefinitions = (Set<G>) world.getAllGenericDefinitions();
        for (G def : genericDefinitions) {
            prices.put(def, startingPrices.getOrDefault(def, fallbackStartingPrice));
        }
        Map<G, Integer> demand;
        boolean done = false;
        while (!done) {
            demand = new HashMap<>();
            for (Bidder<T> bidder : bidders) {
                GenericDemandQueryMIP<G, T> demandQueryMIP = genericDemandQueryMIPBuilder.getDemandQueryMipFor(bidder, prices, epsilon);
                demandQueryMIP.setTimeLimit(getTimeLimit());
                List<? extends GenericDemandQueryResult<G, T>> genericDemandQueryResults = demandQueryMIP.getResultPool(clockPhaseNumberOfBundles);
                // Fill the generic map
                GenericValue<G, T> firstResult = genericDemandQueryResults.get(0).getResultingBundle();
                if (firstResult.getTotalQuantity() > 0) {
                    for (Map.Entry<G, Integer> entry : genericDemandQueryResults.get(0).getResultingBundle().getQuantities().entrySet()) {
                        G def = entry.getKey();
                        int quantity = entry.getValue();
                        demand.put(def, demand.getOrDefault(def, 0) + quantity);
                    }
                }
                for (GenericDemandQueryResult<G, T> genericDemandQueryResult : genericDemandQueryResults) {
                    GenericValue<G, T> genericResult = genericDemandQueryResult.getResultingBundle();
                    if (genericResult.getTotalQuantity() > 0) {

                        BigDecimal bid = BigDecimal.ZERO;
                        for (Map.Entry<G, Integer> entry : genericResult.getQuantities().entrySet()) {
                            BigDecimal quantityTimesPrice = prices.get(entry.getKey()).multiply(BigDecimal.valueOf(entry.getValue()));
                            bid = bid.add(quantityTimesPrice);
                        }

                        GenericBid<G, T> currentBid = bids.getOrDefault(bidder, new GenericBid<>(bidder, new ArrayList<>()));
                        GenericValue<G, T> existingValue = null;
                        for (GenericValue<G, T> value : currentBid.getValues()) {
                            if (value.getQuantities().equals(genericResult.getQuantities())) {
                                existingValue = value;
                                break;
                            }
                        }

                        if (existingValue != null && existingValue.getValue().compareTo(bid) < 0) {
                            currentBid.removeValue(existingValue);
                        }
                        if (existingValue == null || existingValue.getValue().compareTo(bid) < 0) {
                            GenericValue.Builder<G, T> bidBuilder = new GenericValue.Builder<>(bid);
                            genericResult.getQuantities().forEach(bidBuilder::putQuantity);
                            currentBid.addValue(bidBuilder.build());
                        }

                        bids.put(bidder, currentBid);
                    }
                }
            }

            Map<G, BigDecimal> updatedPrices = priceUpdater.updatePrices(prices, demand);
            if (prices.equals(updatedPrices) || totalRounds >= maxRounds) {
                done = true;
                finalDemand = demand;
                finalPrices = prices;
            } else {
                prices = updatedPrices;
                if (logger.isInfoEnabled()) {
                    int aggregateDemand = 0;
                    int supply = 0;
                    for (Map.Entry<G, BigDecimal> priceEntry : prices.entrySet()) {
                        G def = priceEntry.getKey();
                        aggregateDemand += demand.getOrDefault(def, 0);
                        supply += def.numberOfLicenses();
                    }
                    logger.info("Round: {} - Demand: {} - Supply: {}", totalRounds, aggregateDemand, supply);
                }
                totalRounds++;
            }
        }
        bidsAfterClockPhase = bids.values();
        return bidsAfterClockPhase;
    }

    private Collection<GenericBid<G, T>> runSupplementaryRound() {
        Collection<GenericBid<G, T>> bids = new HashSet<>();
        if (supplementaryRounds.isEmpty()) supplementaryRounds.add(new ProfitMaximizingGenericSupplementaryRound<>());

        for (Bidder<T> bidder : bidders) {
            List<GenericValue<G, T>> newValues = new ArrayList<>();
            for (GenericSupplementaryRound<G, T> supplementaryRound : supplementaryRounds) {
                newValues.addAll(supplementaryRound.getSupplementaryBids(this, bidder));
            }

            GenericBid<G, T> bidderBid = bidsAfterClockPhase.stream().filter(bid -> bidder.equals(bid.getBidder())).findFirst().orElseThrow(NoSuchElementException::new);

            GenericBid<G, T> newBid = bidderBid.copyOf();
            newValues.forEach(newBid::addValue);
            bids.add(newBid);
        }
        bidsAfterSupplementaryRound = bids;
        return bids;
    }

    private MechanismResult<T> calculatePayments() {
        Set<GenericBid<G, T>> bids = new HashSet<>(bidsAfterSupplementaryRound);
        XORQWinnerDetermination<G, T> wdp = new XORQWinnerDetermination<>(bids, epsilon);
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
        GenericWorld<T> world = (GenericWorld<T>) bidders.iterator().next().getWorld();
        Set<G> genericDefinitions = (Set<G>) world.getAllGenericDefinitions();
        int aggregateDemand = 0;
        int supply = 0;
        for (G def : genericDefinitions) {
            aggregateDemand += finalDemand.getOrDefault(def, 0);
            supply += def.numberOfLicenses();
        }
        return supply - aggregateDemand;
    }

    public void setPriceUpdater(GenericPriceUpdater<G, T> genericPriceUpdater) {
        Preconditions.checkArgument(bidsAfterClockPhase == null, "Already ran clock phase! Set the price updater before.");
        this.priceUpdater = genericPriceUpdater;
    }

    public void addSupplementaryRound(GenericSupplementaryRound<G, T> genericSupplementaryRound) {
        Preconditions.checkArgument(bidsAfterSupplementaryRound == null, "Already ran supplementary round!");
        this.supplementaryRounds.add(genericSupplementaryRound);
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

    public GenericDemandQueryMIPBuilder<G, T> getDemandQueryBuilder() {
        return this.genericDemandQueryMIPBuilder;
    }

    public Map<G, BigDecimal> getFinalPrices() {
        return finalPrices;
    }

    public Map<G, BigDecimal> getLastPrices() {
        return priceUpdater.getLastPrices();
    }

    public GenericBid<G, T> getBidAfterClockPhase(Bidder<T> bidder) {
        for (GenericBid<G, T> bid : bidsAfterClockPhase) {
            if (bid.getBidder().equals(bidder)) return bid;
        }
        logger.warn("Couldn't find a bid for bidder {} after clock phase.", bidder);
        return null;
    }


    public GenericBid<G, T> getBidAfterSupplementaryRound(Bidder<T> bidder) {
        for (GenericBid<G, T> bid : bidsAfterSupplementaryRound) {
            if (bid.getBidder().equals(bidder)) return bid;
        }
        logger.warn("Couldn't find a bid for bidder {} after supplementary round.", bidder);
        return null;
    }

    public BigDecimal getStartingPrice(G definition) {
        return startingPrices.getOrDefault(definition, fallbackStartingPrice);
    }

    @Override
    public double getScale() {
        return 1;
    }
}
