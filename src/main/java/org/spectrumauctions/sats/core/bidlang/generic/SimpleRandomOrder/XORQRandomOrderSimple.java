package org.spectrumauctions.sats.core.bidlang.generic.SimpleRandomOrder;

import org.marketdesignresearch.mechlib.domain.Bundle;
import org.marketdesignresearch.mechlib.domain.BundleEntry;
import org.marketdesignresearch.mechlib.domain.Good;
import org.marketdesignresearch.mechlib.domain.bidder.value.BundleValue;
import org.spectrumauctions.sats.core.bidlang.BiddingLanguage;
import org.spectrumauctions.sats.core.model.GenericGood;
import org.spectrumauctions.sats.core.util.random.RNGSupplier;
import org.spectrumauctions.sats.core.util.random.UniformDistributionRNG;

import java.util.*;
import java.util.Map.Entry;

/**
 * @author Fabio Isler
 */
public abstract class XORQRandomOrderSimple implements BiddingLanguage {

    private static final double MAX_POSSIBLE_BIDS_FACTOR = 0.8;
    private static final int ABSOLUTE_MAX_BIDS = 1000000;
    private static final int DEFAULT_ITERATIONS = 500;
    private final int maxBundleSize;
    private final RNGSupplier rngSupplier;
    private final List<? extends GenericGood> genericGoods;


    private final transient int totalSize;
    private final transient Set<BundleValue> cache;
    private final transient int maxBids;
    private transient int iterations;


    /**
     * @param genericGoods A set of generic goods
     */
    protected XORQRandomOrderSimple(List<? extends GenericGood> genericGoods, RNGSupplier rngSupplier) {
        super();
        this.genericGoods = genericGoods;
        this.rngSupplier = rngSupplier;
        int quantitySum = genericGoods.stream().mapToInt(GenericGood::available).sum();
        this.maxBundleSize = quantitySum;
        this.totalSize = quantitySum;
        this.iterations = DEFAULT_ITERATIONS;
        this.maxBids = setMaxBid();
        this.cache = new HashSet<>();
    }

    private int setMaxBid() {
        int maxBids = 1;
        for (GenericGood good : genericGoods) {
            if (Math.abs(maxBids) > ABSOLUTE_MAX_BIDS) break;
            maxBids *= good.available();
        }
        return maxBids;
    }

    /**
     * Set the number of iterations in which a bid is created.
     * However, consider that the maximum number of bids that can be created is 0.8 * maxNumberOfBids:
     * (sizeOfA * sizeOfB * ... * sizeOfZ) for goods A - Z
     *
     * @param iterations The number of bids that should be created
     */
    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    /* (non-Javadoc)
     * @see GenericLang#iterator()
     */
    @Override
    public Iterator<BundleValue> iterator() {
        return new SimpleRandomOrderIterator(iterations, rngSupplier.getUniformDistributionRNG());
    }

    class SimpleRandomOrderIterator implements Iterator<BundleValue> {

        private final UniformDistributionRNG uniRng;
        private int remainingIterations;

        SimpleRandomOrderIterator(int iterations, UniformDistributionRNG uniRng) {
            this.remainingIterations = iterations;
            this.uniRng = uniRng;
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#hasNext()
         */
        @Override
        public boolean hasNext() {
            return remainingIterations > 0
                    && cache.size() < MAX_POSSIBLE_BIDS_FACTOR * maxBids;
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#next()
         */
        @Override
        public BundleValue next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            Map<GenericGood, Integer> quantities = getRandomQuantities();
            HashSet<BundleEntry> bundleEntries = new HashSet<>();
            for (Entry<GenericGood, Integer> entry : quantities.entrySet()) {
                bundleEntries.add(new BundleEntry(entry.getKey(), entry.getValue()));
            }
            Bundle bundle = new Bundle(bundleEntries);
            BundleValue result = new BundleValue(getBidder().calculateValue(bundle), bundle);

            if (!cache.contains(result)) {
                cache.add(result);
                remainingIterations--;
                return result;
            }

            return next();
        }

        /**
         * Populate the bid with quantities
         *
         * @return A map of random quantities of a randomly defined number of goods
         */
        private Map<GenericGood, Integer> getRandomQuantities() {
            Map<GenericGood, Integer> quantities = new HashMap<>();
            for (GenericGood good : genericGoods) {
                if (includeGood(good.available(), totalSize, genericGoods.size())) {
                    int quantity = uniRng.nextInt(1, good.available());
                    quantities.put(good, quantity);
                }
            }
            return quantities;
        }

        /**
         * Heuristic to define whether a good should be included in the bid or not
         *
         * @param quantity      Available quantity of that particular good
         * @param totalQuantity Sum of all goods' quantities
         * @param numberOfGoods Total number of available goods
         * @return Whether this good should be included in the bid
         */
        private boolean includeGood(int quantity, int totalQuantity, int numberOfGoods) {
            double base = 1.0 / numberOfGoods;
            double bonus = 1.0 / totalQuantity * quantity;
            return base + bonus >= uniRng.nextDouble();
        }
    }
}
