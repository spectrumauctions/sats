package org.spectrumauctions.sats.mechanism.cca;

import com.google.common.base.Preconditions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spectrumauctions.sats.core.bidlang.xor.XORBid;
import org.spectrumauctions.sats.core.bidlang.xor.XORValue;
import org.spectrumauctions.sats.core.model.*;
import org.spectrumauctions.sats.mechanism.cca.priceupdate.NonGenericPriceUpdater;
import org.spectrumauctions.sats.mechanism.cca.priceupdate.SimpleRelativeNonGenericPriceUpdate;
import org.spectrumauctions.sats.mechanism.cca.supplementaryround.NonGenericSupplementaryRound;
import org.spectrumauctions.sats.mechanism.cca.supplementaryround.ProfitMaximizingNonGenericSupplementaryRound;
import org.spectrumauctions.sats.mechanism.ccg.CCGMechanism;
import org.spectrumauctions.sats.mechanism.domain.MechanismResult;
import org.spectrumauctions.sats.mechanism.domain.mechanisms.AuctionMechanism;
import org.spectrumauctions.sats.mechanism.vcg.VCGMechanism;
import org.spectrumauctions.sats.opt.domain.Allocation;
import org.spectrumauctions.sats.opt.domain.NonGenericDemandQueryMIPBuilder;
import org.spectrumauctions.sats.opt.domain.NonGenericDemandQueryResult;
import org.spectrumauctions.sats.opt.xor.XORWinnerDetermination;

import java.math.BigDecimal;
import java.util.*;

public class NonGenericCCAMechanism<T extends Good> extends CCAMechanism<T> {

    private static final Logger logger = LogManager.getLogger(NonGenericCCAMechanism.class);

    private NonGenericDemandQueryMIPBuilder<T> demandQueryMIPBuilder;
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
            prices.put((T) good, startingPrice);
        }

        Map<T, Integer> demand;
        boolean done = false;
        while (!done) {
            Map<T, BigDecimal> currentPrices = prices; // For lambda use
            demand = new HashMap<>();

            for (Bidder<T> bidder : bidders) {
                List<? extends NonGenericDemandQueryResult<T>> demandQueryResults = demandQueryMIPBuilder.getDemandQueryMipFor(bidder, prices, epsilon).getResultPool(clockPhaseNumberOfBundles);
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
                totalRounds++;
            }
        }
        bidsAfterClockPhase = bids.values();
        return bidsAfterClockPhase;
    }

    private Collection<XORBid<T>> runSupplementaryRound() {
        Collection<XORBid<T>> bids = new HashSet<>();
        if (supplementaryRounds.isEmpty()) supplementaryRounds.add(new ProfitMaximizingNonGenericSupplementaryRound<>());

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
        return bidsAfterClockPhase;
    }

    public Collection<XORBid<T>> getBidsAfterSupplementaryRound() {
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

}
