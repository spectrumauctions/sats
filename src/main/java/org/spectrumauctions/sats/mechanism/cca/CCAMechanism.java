package org.spectrumauctions.sats.mechanism.cca;

import org.spectrumauctions.sats.core.bidlang.generic.GenericBid;
import org.spectrumauctions.sats.core.bidlang.generic.GenericDefinition;
import org.spectrumauctions.sats.core.bidlang.generic.GenericValue;
import org.spectrumauctions.sats.core.bidlang.xor.XORBid;
import org.spectrumauctions.sats.core.bidlang.xor.XORValue;
import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.Bundle;
import org.spectrumauctions.sats.core.model.GenericWorld;
import org.spectrumauctions.sats.core.model.Good;
import org.spectrumauctions.sats.mechanism.ccg.CCGMechanism;
import org.spectrumauctions.sats.mechanism.domain.MechanismResult;
import org.spectrumauctions.sats.mechanism.domain.Payment;
import org.spectrumauctions.sats.mechanism.domain.mechanisms.AuctionMechanism;
import org.spectrumauctions.sats.opt.domain.Allocation;
import org.spectrumauctions.sats.opt.domain.DemandQueryMIPBuilder;
import org.spectrumauctions.sats.opt.domain.DemandQueryResult;
import org.spectrumauctions.sats.opt.domain.WinnerDeterminator;
import org.spectrumauctions.sats.opt.xor.XORWinnerDetermination;
import org.spectrumauctions.sats.opt.xorq.XORQWinnerDetermination;

import java.math.BigDecimal;
import java.util.*;

public class CCAMechanism<T extends Good> implements AuctionMechanism<T> {
    /**
     * --> How to compute the price updates?
     * --> Step size of price updates?
     * --> Stopping condition? When does the clock phase stop? -> When each good is just given once
     */
    private static final BigDecimal DEFAULT_SCALE = BigDecimal.valueOf(0.001);
    private static final BigDecimal DEFAULT_STARTING_PRICE = BigDecimal.ZERO;
    private static final BigDecimal DEFAULT_PRICE_UPDATE = BigDecimal.valueOf(0.5);
    private static final double DEFAULT_EPSILON = 0.1;
    private static final CCAVariant DEFAULT_VARIANT = CCAVariant.XOR_SIMPLE;

    private List<Bidder<T>> bidders;
    private DemandQueryMIPBuilder<GenericDefinition<T>, T> demandQueryMIPBuilder;
    private MechanismResult<T> result;
    private Collection<XORBid<T>> clockPhaseXORResult;
    private Collection<GenericBid<GenericDefinition<T>, T>> clockPhaseGenericResult;
    private Set<XORBid<T>> additionalXORBids;
    private Set<GenericBid<GenericDefinition<T>, T>> additionalGenericBids;

    private BigDecimal scale = DEFAULT_SCALE;
    private BigDecimal startingPrice = DEFAULT_STARTING_PRICE;
    private BigDecimal priceUpdate = DEFAULT_PRICE_UPDATE;
    private double epsilon = DEFAULT_EPSILON;
    private CCAVariant variant = DEFAULT_VARIANT;

    public CCAMechanism(List<Bidder<T>> bidders, DemandQueryMIPBuilder<GenericDefinition<T>, T> demandQueryMIPBuilder) {
        this.bidders = bidders;
        this.demandQueryMIPBuilder = demandQueryMIPBuilder;
    }

    @Override
    public MechanismResult<T> getMechanismResult() {
        if (result != null) return result;
        if (variant != CCAVariant.XORQ) {
            if (clockPhaseXORResult == null) {
                clockPhaseXORResult = runClockPhaseForXORBids();
            }
        } else {
            clockPhaseGenericResult = runClockPhaseForGenericBids();
        }
        result = runCCGPhase();
        return result;
    }

    public Collection<XORBid<T>> runClockPhaseForXORBids() {
        Map<Bidder<T>, XORBid<T>> bids = new HashMap<>();
        GenericWorld<T> world = (GenericWorld<T>) bidders.iterator().next().getWorld();
        Map<GenericDefinition<T>, BigDecimal> prices = new HashMap<>();
        Set<GenericDefinition<T>> genericDefinitions = world.getAllGenericDefinitions();
        genericDefinitions.forEach(def -> prices.put(def, startingPrice));

        Map<GenericDefinition<T>, Integer> genericMap;
        boolean done = false;
        while (!done) {
            genericMap = new HashMap<>();
            for (Bidder<T> bidder : bidders) {
                DemandQueryResult<GenericDefinition<T>, T> demandQueryResult = demandQueryMIPBuilder.getDemandQueryMipFor(bidder, prices, epsilon).getResult();
                if (demandQueryResult.getResultingBundle().getTotalQuantity() > 0) {
                    // Fill the generic map
                    for (Map.Entry<? extends GenericDefinition<T>, Integer> entry : demandQueryResult.getResultingBundle().getQuantities().entrySet()) {
                        GenericDefinition<T> def = entry.getKey();
                        int quantity = entry.getValue();
                        genericMap.put(def, genericMap.getOrDefault(def, 0) + quantity);
                    }

                    XORBid.Builder<T> xorBidBuilder = new XORBid.Builder<>(bidder, bids.get(bidder).getValues());
                    if (variant == CCAVariant.XOR_SIMPLE) {
                        Bundle<T> bundle = demandQueryResult.getResultingBundle().anyConsistentBundle();
                        BigDecimal bid = BigDecimal.valueOf(bundle.stream().mapToDouble(l -> prices.get(world.getGenericDefinitionOf(l)).doubleValue()).sum()).multiply(scale);
                        XORValue<T> existing = xorBidBuilder.containsBundle(bundle);
                        if (existing != null && existing.value().compareTo(bid) < 1) {
                            xorBidBuilder.removeFromBid(existing);
                        }
                        xorBidBuilder.add(new XORValue<>(bundle, bid));
                    } else if (variant == CCAVariant.XOR_FULL) {
                        Iterator<XORValue<T>> xorIterator = demandQueryResult.getResultingBundle().plainXorIterator();
                        if (bids.get(bidder) == null) {
                            bids.put(bidder, new XORBid.Builder<>(bidder).build());
                        }
                        // Add all possible bundles from thie generic definition as bids
                        while (xorIterator.hasNext()) {
                            XORValue<T> xorValue = xorIterator.next();
                            BigDecimal bid = BigDecimal.valueOf(xorValue.getLicenses().stream().mapToDouble(l -> prices.get(world.getGenericDefinitionOf(l)).doubleValue()).sum()).multiply(scale);
                            // Check for existing equal bundles with a lower value, remove them since they're not needed
                            XORValue<T> existing = xorBidBuilder.containsBundle(xorValue.getLicenses());
                            if (existing != null && existing.value().compareTo(bid) < 1) {
                                xorBidBuilder.removeFromBid(existing);
                            }
                            XORValue<T> xorValueBid = new XORValue<>(xorValue.getLicenses(), bid);
                            xorBidBuilder.add(xorValueBid);
                        }
                    }
                    XORBid<T> newBid = xorBidBuilder.build();
                    bids.put(bidder, newBid);
                }
            }

            done = true;
            for (Map.Entry<GenericDefinition<T>, Integer> entry : genericMap.entrySet()) {
                GenericDefinition<T> def = entry.getKey();
                if (def.numberOfLicenses() < entry.getValue()) {
                    BigDecimal update = updatePrice(prices.getOrDefault(def, BigDecimal.ZERO));
                    prices.merge(def, update, BigDecimal::add);
                    done = false;
                }
            }
        }
        clockPhaseXORResult = bids.values();
        return clockPhaseXORResult;
    }

    public Collection<GenericBid<GenericDefinition<T>, T>> runClockPhaseForGenericBids() {
        Map<Bidder<T>, GenericBid<GenericDefinition<T>, T>> bids = new HashMap<>();
        GenericWorld<T> world = (GenericWorld<T>) bidders.iterator().next().getWorld();
        Map<GenericDefinition<T>, BigDecimal> prices = new HashMap<>();
        Set<GenericDefinition<T>> genericDefinitions = world.getAllGenericDefinitions();
        genericDefinitions.forEach(def -> prices.put(def, startingPrice));

        Map<GenericDefinition<T>, Integer> genericMap;
        boolean done = false;
        while (!done) {
            genericMap = new HashMap<>();
            for (Bidder<T> bidder : bidders) {
                DemandQueryResult<GenericDefinition<T>, T> demandQueryResult = demandQueryMIPBuilder.getDemandQueryMipFor(bidder, prices, epsilon).getResult();
                GenericValue<GenericDefinition<T>, T> genericResult = demandQueryResult.getResultingBundle();
                if (genericResult.getTotalQuantity() > 0) {
                    // Fill the generic map
                    for (Map.Entry<? extends GenericDefinition<T>, Integer> entry : demandQueryResult.getResultingBundle().getQuantities().entrySet()) {
                        GenericDefinition<T> def = entry.getKey();
                        int quantity = entry.getValue();
                        genericMap.put(def, genericMap.getOrDefault(def, 0) + quantity);
                    }

                    BigDecimal bid = BigDecimal.ZERO;
                    for (Map.Entry<GenericDefinition<T>, Integer> entry : genericResult.getQuantities().entrySet()) {
                        BigDecimal quantityTimesPriceScaled = prices.get(entry.getKey()).multiply(BigDecimal.valueOf(entry.getValue())).multiply(scale);
                        bid = bid.add(quantityTimesPriceScaled);
                    }

                    // TODO (but less critical than in XOR): Remove lower bids on the exact same generic definition

                    GenericValue.Builder<GenericDefinition<T>, T> bidBuilder = new GenericValue.Builder<>(bid);
                    genericResult.getQuantities().forEach(bidBuilder::putQuantity);
                    GenericBid<GenericDefinition<T>, T> newBid = bids.getOrDefault(bidder, new GenericBid<>(bidder, new ArrayList<>()));
                    newBid.addValue(bidBuilder.build());
                    bids.put(bidder, newBid);
                }
            }

            done = true;
            for (Map.Entry<GenericDefinition<T>, Integer> entry : genericMap.entrySet()) {
                GenericDefinition<T> def = entry.getKey();
                if (def.numberOfLicenses() < entry.getValue()) {
                    BigDecimal update = updatePrice(prices.getOrDefault(def, BigDecimal.ZERO));
                    prices.merge(def, update, BigDecimal::add);
                    done = false;
                }
            }
        }
        clockPhaseGenericResult = bids.values();
        return clockPhaseGenericResult;
    }

    public void addAdditionalXORBids(Set<XORBid<T>> additionalBids) {
        this.additionalXORBids = additionalBids;
    }

    public void addAdditionalGenericBids(Set<GenericBid<GenericDefinition<T>, T>> additionalBids) {
        this.additionalGenericBids = additionalBids;
    }

    public MechanismResult<T> runCCGPhase() {
        if (variant != CCAVariant.XORQ) {
            Set<XORBid<T>> bids = new HashSet<>(clockPhaseXORResult);
            if (additionalXORBids != null) {
                bids.addAll(additionalXORBids);
            }
//
//        double maxValue = bids.stream().mapToDouble(bidSet -> bidSet.getValues().stream().mapToDouble(b -> b.value().doubleValue()).max().orElse(-1)).max().orElse(-2);
//
//        if (maxValue > MAX_VALUE) {
//            double scale = MAX_VALUE / maxValue;
//            // TODO: Adjust bids to the scale here
//        }

            XORWinnerDetermination<T> wdp = new XORWinnerDetermination<>(bids);

            CCGMechanism<T> ccg = new CCGMechanism<>(wdp);
            ccg.setScale(scale);

            result = ccg.getMechanismResult();
        } else {
            Set<GenericBid<GenericDefinition<T>, T>> bids = new HashSet<>(clockPhaseGenericResult);
            if (additionalGenericBids != null) {
                bids.addAll(additionalGenericBids);
            }

            XORQWinnerDetermination<T> wdp = new XORQWinnerDetermination<>(bids);
            CCGMechanism<T> ccg = new CCGMechanism<>(wdp);
            ccg.setScale(scale);

            result = ccg.getMechanismResult();

        }
        return result;
    }

    private BigDecimal updatePrice(BigDecimal current) {
        if (current.equals(BigDecimal.ZERO))
            return BigDecimal.valueOf(1e5);
        else
            return current.multiply(priceUpdate);
    }

    public void setScale(BigDecimal scale) {
        this.scale = scale;
    }

    public void setPriceUpdate(BigDecimal priceUpdate) {
        this.priceUpdate = priceUpdate;
    }

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

    public void setVariant(CCAVariant variant) {
        this.variant = variant;
    }
}
