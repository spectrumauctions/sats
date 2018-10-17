package org.spectrumauctions.sats.core.bidlang.generic.SimpleRandomOrder;

import org.spectrumauctions.sats.core.bidlang.generic.GenericDefinition;
import org.spectrumauctions.sats.core.bidlang.generic.GenericLang;
import org.spectrumauctions.sats.core.bidlang.generic.GenericValue;
import org.spectrumauctions.sats.core.bidlang.generic.GenericValueBidder;
import org.spectrumauctions.sats.core.model.Good;
import org.spectrumauctions.sats.core.util.random.RNGSupplier;
import org.spectrumauctions.sats.core.util.random.UniformDistributionRNG;

import java.util.*;
import java.util.Map.Entry;

/**
 * @author Fabio Isler
 */
public abstract class XORQRandomOrderSimple<T extends GenericDefinition<S>, S extends Good> implements GenericLang<T, S> {

    private static final double MAX_POSSIBLE_BIDS_FACTOR = 0.8;
    private static final int ABSOLUTE_MAX_BIDS = 1000000;
    private static final int DEFAULT_ITERATIONS = 500;
    private final Map<T, Integer> maxQuantities;
    private final int maxBundleSize;
    private final RNGSupplier rngSupplier;


    private final transient long seed;
    private final transient int totalSize;
    private final transient Set<GenericValue> cache;
    private final transient int maxBids;
    private transient int iterations;


    /**
     * @param genericDefinitions A set of generic definitions
     */
    protected XORQRandomOrderSimple(Collection<T> genericDefinitions, RNGSupplier rngSupplier) {
        super();
        this.rngSupplier = rngSupplier;
        this.seed = this.rngSupplier.getUniformDistributionRNG().nextLong();
        Map<T, Integer> orderedMap = new LinkedHashMap<>();
        int quantitySum = 0;
        for (T def : genericDefinitions) {
            quantitySum += def.numberOfLicenses();
            orderedMap.put(def, def.numberOfLicenses());
        }
        this.maxQuantities = Collections.unmodifiableMap(orderedMap);
        this.maxBundleSize = quantitySum;
        this.totalSize = quantitySum;
        this.iterations = DEFAULT_ITERATIONS;
        this.maxBids = setMaxBid(maxQuantities);
        this.cache = new HashSet<>();
    }

    private int setMaxBid(Map<T, Integer> maxQuantities) {
        int maxBids = 1;
        for (int i : maxQuantities.values()) {
            if (Math.abs(maxBids) > ABSOLUTE_MAX_BIDS) break;
            maxBids *= i;
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
    public Iterator<GenericValue<T, S>> iterator() {
        return new SimpleRandomOrderIterator(iterations, rngSupplier.getUniformDistributionRNG(seed));
    }

    protected abstract GenericValueBidder<T> getGenericBidder();

    protected abstract Comparator<T> getDefComparator();

    class SimpleRandomOrderIterator implements Iterator<GenericValue<T, S>> {

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
        public GenericValue<T, S> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            Map<T, Integer> quantities = getRandomQuantities();
            GenericValue.Builder<T, S> genValBuilder = new GenericValue.Builder<>(getGenericBidder());
            for (Entry<T, Integer> entry : quantities.entrySet()) {
                genValBuilder.putQuantity(entry.getKey(), entry.getValue());
            }
            GenericValue<T, S> result = genValBuilder.build();

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
        private Map<T, Integer> getRandomQuantities() {
            Map<T, Integer> quantities = new HashMap<>();
            for (Entry<T, Integer> good : maxQuantities.entrySet()) {
                if (includeGood(good.getValue(), totalSize, maxQuantities.size())) {
                    int quantity = uniRng.nextInt(1, good.getValue());
                    quantities.put(good.getKey(), quantity);
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
