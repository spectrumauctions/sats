package org.spectrumauctions.sats.mechanism.cca;

import org.spectrumauctions.sats.core.bidlang.generic.GenericDefinition;
import org.spectrumauctions.sats.core.bidlang.xor.XORBid;
import org.spectrumauctions.sats.core.bidlang.xor.XORValue;
import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.Good;
import org.spectrumauctions.sats.core.model.World;
import org.spectrumauctions.sats.core.model.mrvm.MRVMLicense;
import org.spectrumauctions.sats.mechanism.ccg.CCGMechanism;
import org.spectrumauctions.sats.mechanism.domain.MechanismResult;
import org.spectrumauctions.sats.mechanism.domain.Payment;
import org.spectrumauctions.sats.mechanism.domain.mechanisms.AuctionMechanism;
import org.spectrumauctions.sats.opt.domain.Allocation;
import org.spectrumauctions.sats.opt.domain.DemandQueryMIPBuilder;
import org.spectrumauctions.sats.opt.domain.DemandQueryResult;
import org.spectrumauctions.sats.opt.domain.WinnerDeterminator;
import org.spectrumauctions.sats.opt.xor.XORWinnerDetermination;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import static edu.harvard.econcs.jopt.solver.mip.MIP.MAX_VALUE;

public class CCAMechanism<T extends Good> implements AuctionMechanism<T> {
    /**
     * --> How to compute the price updates?
     * --> Step size of price updates?
     * --> Stopping condition? When does the clock phase stop? -> When each good is just given once
     * --> Raise epsilon for accepted solutions to make it faster
     */

    private List<Bidder<T>> bidders;
    private DemandQueryMIPBuilder<T> demandQueryMIPBuilder;
    private MechanismResult<T> result;
    private Set<XORBid<T>> clockPhaseResult;
    private Set<XORBid<T>> additionalBids;

    private static final BigDecimal SCALE = BigDecimal.valueOf(0.001);

    public CCAMechanism(List<Bidder<T>> bidders, DemandQueryMIPBuilder<T> demandQueryMIPBuilder) {
        this.bidders = bidders;
        this.demandQueryMIPBuilder = demandQueryMIPBuilder;
    }

    @Override
    public MechanismResult<T> getMechanismResult() {
        if (result != null) return result;
        if (clockPhaseResult == null) {
            clockPhaseResult = runClockPhase();
        }
        result = runCCGPhase();
        return result;
    }

    public Set<XORBid<T>> runClockPhase() {
        Set<XORBid<T>> bids = new HashSet<>();
        World world = bidders.iterator().next().getWorld();
        Map<T, BigDecimal> prices = new HashMap<>();
        Set<T> licenses = (Set<T>) world.getLicenses();
        licenses.forEach(l -> prices.put(l, BigDecimal.ZERO));

        Map<GenericDefinition<T>, Integer> genericMap;
        boolean done = false;
        // Clock phase -> Wrap in while loop
        while (!done) {
            genericMap = new HashMap<>();
            //Map<Bidder<T>, DemandQueryResult<T>> demandQueryResults = new HashMap<>();
            for (Bidder<T> bidder : bidders) {
                DemandQueryResult<T> demandQueryResult = demandQueryMIPBuilder.getDemandQueryMipFor(bidder, prices).getResult();
                //demandQueryResults.put(bidder, demandQueryResult);
                if (demandQueryResult.getResultingBundle().getTotalQuantity() > 0) {
                    // Fill the generic map
                    for (Map.Entry<? extends GenericDefinition<T>, Integer> entry : demandQueryResult.getResultingBundle().getQuantities().entrySet()) {
                        GenericDefinition<T> def = entry.getKey();
                        int quantity = entry.getValue();
                        genericMap.put(def, genericMap.getOrDefault(def, 0) + quantity);
                    }

                    Iterator<XORValue<T>> xorIterator = demandQueryResult.getResultingBundle().plainXorIterator();
                    // Add the bids
                    XORBid.Builder<T> xorBidBuilder = new XORBid.Builder<>(bidder);
                    while (xorIterator.hasNext()) {
                        XORValue<T> xorValue = xorIterator.next();
                        BigDecimal bid = BigDecimal.valueOf(xorValue.getLicenses().stream().mapToDouble(l -> prices.get(l).doubleValue()).sum()).multiply(SCALE);
                        XORValue<T> xorValueBid = new XORValue<>(xorValue.getLicenses(), bid);
                        xorBidBuilder.add(xorValueBid);
                    }
                    bids.add(xorBidBuilder.build());
                }
            }

            done = true;
            for (Map.Entry<GenericDefinition<T>, Integer> entry : genericMap.entrySet()) {
                GenericDefinition<T> def = entry.getKey();
                if (def.numberOfLicenses() < entry.getValue()) {
                    T first = def.allLicenses().iterator().next();
                    BigDecimal update = updatePrice(prices.getOrDefault(first, BigDecimal.ZERO));
                    def.allLicenses().forEach(l -> prices.merge(l, update, BigDecimal::add));
                    done = false;
                }
            }
        }
        return bids;
    }

    private BigDecimal updatePrice(BigDecimal current) {
        if (current.equals(BigDecimal.ZERO))
            return BigDecimal.valueOf(1e5);
        else
            return current.divide(BigDecimal.valueOf(2), RoundingMode.HALF_UP);
    }

    public void addAdditionalBids(Set<XORBid<T>> additionalBids) {
        this.additionalBids = additionalBids;
    }

    public MechanismResult<T> runCCGPhase() {
        Set<XORBid<T>> bids = new HashSet<>(clockPhaseResult);
        if (additionalBids != null) {
            bids.addAll(additionalBids);
        }

        double maxValue = bids.stream().mapToDouble(bidSet -> bidSet.getValues().stream().mapToDouble(b -> b.value().doubleValue()).max().orElse(-1)).max().orElse(-2);

        if (maxValue > MAX_VALUE) {
            double scale = MAX_VALUE / maxValue;
            // TODO: Adjust scale here
        }

        XORWinnerDetermination<T> wdp = new XORWinnerDetermination<>(bids);

        result = new CCGMechanism<>(wdp).getMechanismResult();

        return result;
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
}
