package org.spectrumauctions.sats.mechanism.cca;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spectrumauctions.sats.core.bidlang.generic.GenericBid;
import org.spectrumauctions.sats.core.bidlang.generic.GenericDefinition;
import org.spectrumauctions.sats.core.bidlang.generic.GenericValue;
import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.GenericWorld;
import org.spectrumauctions.sats.core.model.Good;
import org.spectrumauctions.sats.mechanism.cca.priceupdate.GenericPriceUpdater;
import org.spectrumauctions.sats.mechanism.cca.priceupdate.SimpleRelativeGenericPriceUpdate;
import org.spectrumauctions.sats.mechanism.cca.supplementaryround.ProfitMaximizingGenericSupplementaryRound;
import org.spectrumauctions.sats.mechanism.cca.supplementaryround.GenericSupplementaryRound;
import org.spectrumauctions.sats.mechanism.ccg.CCGMechanism;
import org.spectrumauctions.sats.mechanism.domain.MechanismResult;
import org.spectrumauctions.sats.mechanism.vcg.VCGMechanism;
import org.spectrumauctions.sats.opt.domain.Allocation;
import org.spectrumauctions.sats.opt.domain.GenericDemandQueryMIPBuilder;
import org.spectrumauctions.sats.opt.domain.GenericDemandQueryResult;
import org.spectrumauctions.sats.opt.xorq.XORQWinnerDetermination;

import java.math.BigDecimal;
import java.util.*;

public class GenericCCAMechanism<G extends GenericDefinition<T>, T extends Good> extends CCAMechanism<T> {

    private static final Logger logger = LogManager.getLogger(GenericCCAMechanism.class);

    private Collection<GenericBid<G, T>> bidsAfterClockPhase;
    private Collection<GenericBid<G, T>> bidsAfterSupplementaryRound;
    private Map<G, BigDecimal> finalPrices;
    private Map<G, Integer> finalDemand;


    private GenericDemandQueryMIPBuilder<G, T> genericDemandQueryMIPBuilder;
    private GenericPriceUpdater<G, T> priceUpdater = new SimpleRelativeGenericPriceUpdate<>();
    private GenericSupplementaryRound<G, T> supplementaryRound = new ProfitMaximizingGenericSupplementaryRound<>();


    public GenericCCAMechanism(List<Bidder<T>> bidders, GenericDemandQueryMIPBuilder<G, T> genericDemandQueryMIPBuilder) {
        super(bidders);
        this.genericDemandQueryMIPBuilder = genericDemandQueryMIPBuilder;
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

        logger.info("Starting CCG with all collected bids...");
        result = runVCGPhase();
        return result;
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

    public Collection<GenericBid<G, T>> runClockPhase() {
        Map<Bidder<T>, GenericBid<G, T>> bids = new HashMap<>();
        GenericWorld<T> world = (GenericWorld<T>) bidders.iterator().next().getWorld();
        Map<G, BigDecimal> prices = new HashMap<>();
        Set<G> genericDefinitions = (Set<G>) world.getAllGenericDefinitions();
        for (G def : genericDefinitions) {
            prices.put(def, startingPrice);
        }
        Map<G, Integer> demand;
        boolean done = false;
        while (!done) {
            demand = new HashMap<>();
            for (Bidder<T> bidder : bidders) {
                GenericDemandQueryResult<G, T> genericDemandQueryResult = genericDemandQueryMIPBuilder.getDemandQueryMipFor(bidder, prices, epsilon).getResult();
                GenericValue<G, T> genericResult = genericDemandQueryResult.getResultingBundle();
                if (genericResult.getTotalQuantity() > 0) {
                    // Fill the generic map
                    for (Map.Entry<G, Integer> entry : genericDemandQueryResult.getResultingBundle().getQuantities().entrySet()) {
                        G def = entry.getKey();
                        int quantity = entry.getValue();
                        demand.put(def, demand.getOrDefault(def, 0) + quantity);
                    }

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
                        bids.put(bidder, currentBid);
                    }

                    bids.put(bidder, currentBid);
                }
            }

            Map<G, BigDecimal> updatedPrices = priceUpdater.updatePrices(prices, demand);
            if (prices.equals(updatedPrices)) {
                done = true;
                finalDemand = demand;
                finalPrices = prices;
            } else {
                prices = updatedPrices;
                totalRounds++;
            }
        }
        bidsAfterClockPhase = bids.values();
        return bidsAfterClockPhase;
    }

    private Collection<GenericBid<G, T>> runSupplementaryRound() {
        Collection<GenericBid<G, T>> bids = new HashSet<>();
        // Supplementary totalRounds
        for (Bidder<T> bidder : bidders) {
            Set<GenericValue<G, T>> newValues = supplementaryRound.getSupplementaryBids(bidder, genericDemandQueryMIPBuilder.getDemandQueryMipFor(bidder, finalPrices, epsilon));

            GenericBid bidderBid = bidsAfterClockPhase.stream().filter(bid -> bidder.equals(bid.getBidder())).findFirst().orElseThrow(NoSuchElementException::new);

            GenericBid newBid = bidderBid.copyOf();
            newValues.forEach(newBid::addValue);
            bids.add(newBid);
        }
        bidsAfterSupplementaryRound = bids;
        return bids;
    }

    public MechanismResult<T> runVCGPhase() {
        Set<GenericBid<G, T>> bids = new HashSet<>(bidsAfterSupplementaryRound);
        XORQWinnerDetermination<G, T> wdp = new XORQWinnerDetermination<>(bids);
        VCGMechanism<T> ccg = new VCGMechanism<>(wdp);
        result = ccg.getMechanismResult();
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
        this.priceUpdater = genericPriceUpdater;
    }

    public void setSupplementaryRound(GenericSupplementaryRound<G, T> genericSupplementaryRound) {
        this.supplementaryRound = genericSupplementaryRound;
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
}
