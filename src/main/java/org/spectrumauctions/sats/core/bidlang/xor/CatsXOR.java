package org.spectrumauctions.sats.core.bidlang.xor;

import com.google.common.base.Preconditions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.marketdesignresearch.mechlib.domain.Bundle;
import org.marketdesignresearch.mechlib.domain.BundleEntry;
import org.marketdesignresearch.mechlib.domain.bidder.value.BundleValue;
import org.spectrumauctions.sats.core.bidlang.BiddingLanguage;
import org.spectrumauctions.sats.core.model.LicenseBundle;
import org.spectrumauctions.sats.core.model.cats.CATSBidder;
import org.spectrumauctions.sats.core.model.cats.CATSLicense;
import org.spectrumauctions.sats.core.model.cats.CATSWorld;
import org.spectrumauctions.sats.core.util.random.RNGSupplier;
import org.spectrumauctions.sats.core.util.random.UniformDistributionRNG;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

/**
 * <p>The original CATS Regions model has a specific way to generate bids, which does not directly translate into our
 * iterator-based way of generating bids. This class provides an iterator that imitates the original bid-generation
 * technique. To do this, it first collects and filters all possible bids and then provides the iterator on that
 * collection.</p>
 *
 * <p>If you prefer to have an iterator that works more like the other iterators in SATS, you can change the behavior of
 * this class via {@link #noCapForSubstitutableGoods()}.
 *
 * In that case, the first bundle of the provided iterator is the initial bundle which the following elements are based
 * on. The next bundles each have one license of the original bundle as a starting point and are extended so that they
 * have the same amount of licenses as the original bundle. In the CATS Regions model, they are called substitutable
 * bids/bundles.</p>
 *
 * Two things to consider when using the option {@link #noCapForSubstitutableGoods()}:
 *  <ul>
 *      <li>If this iterator finds an invalid bundle (identical to the original bundle or not satisfying the budget/
 *          reselling value constraints), it will try to find another one until #MAX_RETRIES is reached. This is rare,
 *          but still make sure to handle this #NoSuchElementException.</li>
 *      <li>The elements which #next() returns are checked to be not identical to the original bundle, but it's
 *          impossible to detect if it's similar to another substitutable bundle. If duplicate substitutable bundles are
 *          an issue, make sure to handle after you iterated through all the elements.</li>
 *  </ul>
 *
 * @author Fabio Isler
 */
public class CatsXOR implements BiddingLanguage {

    private static final Logger logger = LogManager.getLogger(CatsXOR.class);

    private Collection<CATSLicense> goods;
    private CATSBidder bidder;
    private RNGSupplier rngSupplier;
    private CATSWorld world;
    private boolean noCapForSubstitutableGoods;

    public CatsXOR(Collection<CATSLicense> goods, RNGSupplier rngSupplier, CATSBidder bidder) {
        this.goods = goods;
        this.bidder = bidder;
        this.rngSupplier = rngSupplier;
        this.world = goods.stream().findAny().orElseThrow(() -> new IllegalArgumentException("All passed goods must have a world")).getWorld();
        this.noCapForSubstitutableGoods = false;
    }

    public CatsXOR noCapForSubstitutableGoods() {
        this.noCapForSubstitutableGoods = true;
        return this;
    }

    @Override
    public CATSBidder getBidder() {
        return bidder;
    }

    @Override
    public Iterator<BundleValue> iterator() {
        if (noCapForSubstitutableGoods) {
            return new CATSIterator(rngSupplier.getUniformDistributionRNG(), false);
        } else {
            return getCATSXORBids().iterator();
        }
    }

    public Set<BundleValue> getCATSXORBids() {
        TreeSet<BundleValue> sortedSet = new TreeSet<>();
        Set<BundleValue> result = new HashSet<>();

        Iterator<BundleValue> iterator = new CATSIterator(rngSupplier.getUniformDistributionRNG(), true);

        result.add(iterator.next()); // CATS always includes the original bundle

        // Fill the sorted set with all the elements that are not null
        while (iterator.hasNext()) {
            BundleValue next = iterator.next();
            if (next != null) {
                sortedSet.add(next);
            }
        }

        // Get the most valuable elements from the substitutable bids
        for (int i = 0; i < world.getMaxSubstitutableBids() && !sortedSet.isEmpty(); i++) {
            BundleValue val = sortedSet.first();
            if (!result.stream().map(BundleValue::getBundle).collect(Collectors.toList()).contains(val.getBundle())) {
                result.add(val);
            }
            sortedSet.remove(val);
        }
        return result;
    }

    private class CATSIterator implements Iterator<BundleValue> {
        private static final int MAX_RETRIES = 100;

        private final UniformDistributionRNG uniRng;
        private Queue<CATSLicense> originalLicenseQueue;
        private Set<CATSLicense> originalBundle;
        private double minValue;
        private double budget;
        private double minResaleValue;
        private int retries;
        private boolean acceptNulls;

        CATSIterator(UniformDistributionRNG uniRng, boolean acceptNulls) {
            Preconditions.checkArgument(world.getLicenses().size() == goods.size());
            this.uniRng = uniRng;
            this.minValue = 1e10;
            this.retries = 0;
            this.acceptNulls = acceptNulls;
        }

        @Override
        public boolean hasNext() {
            if (originalBundle == null) return true;            // The first bundle has not been created yet
            int licensesLeftToChoose = goods.size() - originalBundle.size();
            return !(originalBundle.size() <= 1)                // The original bundle included only one license
                        && !originalLicenseQueue.isEmpty()      // We're not done yet with creating substitutable bundles
                        && licensesLeftToChoose > 0
                        && retries < MAX_RETRIES;
        }

        @Override
        public BundleValue next() throws NoSuchElementException {
            if (!hasNext())
                throw new NoSuchElementException();

            HashSet<CATSLicense> licenses = new HashSet<>();
            for (Map.Entry<Long, BigDecimal> entry : bidder.getPrivateValues().entrySet()) {
                if (entry.getValue().doubleValue() < minValue) minValue = entry.getValue().doubleValue();
            }

            if (originalLicenseQueue == null) {
                // We didn't construct an original bid yet
                WeightedRandomCollection<CATSLicense> weightedGoods = new WeightedRandomCollection<>(uniRng);
                goods.forEach(g -> {
                    double positivePrivateValue = (bidder.getPrivateValues().get(g.getLongId()).doubleValue() - minValue);
                    weightedGoods.add(positivePrivateValue, g);
                });
                CATSLicense first = weightedGoods.next();
                licenses.add(first);
                while (uniRng.nextDouble() <= world.getAdditionalLocation()) {
                    licenses.add(selectLicenseToAdd(licenses));
                }

                HashSet<BundleEntry> bundleEntries = (HashSet<BundleEntry>) licenses.stream().map(l -> new BundleEntry(l, 1)).collect(Collectors.toSet());
                Bundle bundle = new Bundle(bundleEntries);
                BigDecimal value = bidder.calculateValue(bundle);
                if (value.compareTo(BigDecimal.ZERO) < 0) return next(); // Restart bundle generation for this bidder

                budget = world.getBudgetFactor() * value.doubleValue();
                minResaleValue = world.getResaleFactor() * licenses.stream().mapToDouble(CATSLicense::getCommonValue).sum();
                originalLicenseQueue = new LinkedBlockingQueue<>(licenses);
                originalBundle = licenses;
                return new BundleValue(value, bundle);
            } else {
                CATSLicense first = originalLicenseQueue.poll();
                licenses.add(first);
                while (licenses.size() < originalBundle.size()) {
                    CATSLicense toAdd = selectLicenseToAdd(licenses);
                    if (toAdd != null) licenses.add(toAdd);
                }
                HashSet<BundleEntry> bundleEntries = (HashSet<BundleEntry>) licenses.stream().map(l -> new BundleEntry(l, 1)).collect(Collectors.toSet());
                Bundle bundle = new Bundle(bundleEntries);
                BigDecimal value = bidder.calculateValue(bundle);
                double resaleValue = licenses.stream().mapToDouble(CATSLicense::getCommonValue).sum();
                if (value.doubleValue() >= 0 && value.doubleValue() <= budget
                        && resaleValue >= minResaleValue
                        && !licenses.equals(originalBundle)) {
                    retries = 0; // Found one - reset retries counter
                    return new BundleValue(value, bundle);
                } else {
                    try {
                        return handleNulls(first);
                    } catch (NoValidElementFoundException e) {
                        logger.error(e);
                        logger.error("Returning null.");
                        return null;
                    }
                }
            }
        }

        private BundleValue handleNulls(CATSLicense first) throws NoValidElementFoundException {
            if (acceptNulls) {
                return null;
            }
            originalLicenseQueue.add(first); // Add this license to the original queue again
            if (hasNext() && ++retries < MAX_RETRIES) return next();
            else throw new NoValidElementFoundException();
        }

        private CATSLicense selectLicenseToAdd(Set<CATSLicense> bundle) {
            if (uniRng.nextDouble() <= world.getJumpProbability()) {
                if (goods.size() == bundle.size()) return null; // Prevent infinite loop if there is no other license
                CATSLicense randomLicense;
                do {
                    Iterator<CATSLicense> iterator = goods.iterator();
                    int index = uniRng.nextInt(goods.size());
                    for (int i = 0; i < index; i++) {
                        iterator.next();
                    }
                    randomLicense = iterator.next();
                } while(bundle.contains(randomLicense));

                return randomLicense;
            } else {
                WeightedRandomCollection<CATSLicense> neighbors = new WeightedRandomCollection<>(uniRng);
                // Filter the licenses that are not contained yet in the bundle and where there exists an edge to one
                // of the licenses in the bundle.
                goods.stream().filter(l -> !bundle.contains(l) && edgeExists(l, bundle))
                        .forEach(g -> {
                            double positivePrivateValue = bidder.getPrivateValues().get(g.getLongId()).doubleValue() - minValue;
                            neighbors.add(positivePrivateValue, g);
                        });
                if (neighbors.hasNext()) return neighbors.next();
                else return null;
            }
        }

        private boolean edgeExists(CATSLicense license, Set<CATSLicense> bundle) {
            for (CATSLicense l : bundle) {
                if (world.getGrid().isAdjacent(license.getVertex(), l.getVertex()))
                    return true;
            }
            return false;
        }

        private class NoValidElementFoundException extends Exception {
            NoValidElementFoundException() {
                super("After " + retries + " retries, no other bundle was found " +
                        "that was not identical to the original bundle bid and is valid in terms of budget and " +
                        "min_resale_value constraints. \n" +
                        "Most likely, there are either almost no licenses to choose from or the original bundle is very" +
                        "small and highly valued, so that it's difficult to create another bundle that satisfies the" +
                        "constraints. Try again (maybe with a higher number of goods) or use the the iterator that handles" +
                        "this situation with null-values.");
            }
        }
    }

    private class WeightedRandomCollection<T> implements Iterator<T> {
        private final NavigableMap<Double, T> map = new TreeMap<>();
        private final UniformDistributionRNG random;
        private double total = 0;

        private WeightedRandomCollection(UniformDistributionRNG random) {
            this.random = random;
        }

        public void add(double weight, T result) {
            total += weight;
            map.put(total, result);
        }

        @Override
        public boolean hasNext() {
            return !map.isEmpty();
        }

        public T next() {
            double value = random.nextDouble() * total;
            Map.Entry<Double, T> entry = map.ceilingEntry(value);
            if (entry == null)
                return null;
            return entry.getValue();
        }
    }

}
