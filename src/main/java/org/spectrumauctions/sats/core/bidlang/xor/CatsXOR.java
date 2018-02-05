package org.spectrumauctions.sats.core.bidlang.xor;

import com.google.common.base.Preconditions;
import org.spectrumauctions.sats.core.model.Bundle;
import org.spectrumauctions.sats.core.model.cats.CATSBidder;
import org.spectrumauctions.sats.core.model.cats.CATSLicense;
import org.spectrumauctions.sats.core.model.cats.CATSWorld;
import org.spectrumauctions.sats.core.util.random.RNGSupplier;
import org.spectrumauctions.sats.core.util.random.UniformDistributionRNG;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author Fabio Isler
 */
public class CatsXOR implements XORLanguage<CATSLicense> {
    private Collection<CATSLicense> goods;
    private CATSBidder bidder;
    private RNGSupplier rngSupplier;
    private CATSWorld world;

    public CatsXOR(Collection<CATSLicense> goods, RNGSupplier rngSupplier, CATSBidder bidder) {
        this.goods = goods;
        this.bidder = bidder;
        this.rngSupplier = rngSupplier;
        this.world = goods.stream().findAny().orElseThrow(IllegalArgumentException::new).getWorld();
    }

    @Override
    public CATSBidder getBidder() {
        return bidder;
    }

    @Override
    public Iterator<XORValue<CATSLicense>> iterator() {
        return new CATSIterator(rngSupplier.getUniformDistributionRNG());
    }

    private class CATSIterator implements Iterator<XORValue<CATSLicense>> {
        private final UniformDistributionRNG uniRng;
        private List<CATSLicense> originalBundleSet;
        private double minValue;
        private double budget;
        private double minResaleValue;
        private int originalBundleSize;

        public CATSIterator(UniformDistributionRNG uniRng) {
            Preconditions.checkArgument(world.getLicenses().size() == goods.size());
            this.uniRng = uniRng;
            this.minValue = 1e10;
        }

        @Override
        public boolean hasNext() {
            // Either the original bundle set has not been created yet, or it's empty again
            return originalBundleSet == null || !originalBundleSet.isEmpty();
        }

        @Override
        public XORValue<CATSLicense> next() {
            if (!hasNext())
                throw new NoSuchElementException();

            Bundle<CATSLicense> bundle = new Bundle<>();
            for (Map.Entry<Long, BigDecimal> entry : bidder.getPrivateValues().entrySet()) {
                if (entry.getValue().doubleValue() < minValue) minValue = entry.getValue().doubleValue();
            }

            if (originalBundleSet == null) {
                // We didn't construct an original bid yet
                WeightedRandomCollection weightedGoods = new WeightedRandomCollection(uniRng);
                goods.forEach(g -> {
                    double positivePrivateValue = (bidder.getPrivateValues().get(g.getId()).doubleValue() - minValue);
                    weightedGoods.add(positivePrivateValue, g);
                });
                CATSLicense first = weightedGoods.nextWeightedRandom();
                bundle.add(first);
                while (uniRng.nextDouble() <= world.getAdditionalLocation()) {
                    bundle.add(selectLicenseToAdd(bundle));
                }

                BigDecimal value = bidder.calculateValue(bundle);
                if (value.compareTo(BigDecimal.ZERO) < 0) return next(); // Restart bundle generation for this bidder

                budget = world.getBudgetFactor() * value.doubleValue();
                minResaleValue = world.getResaleFactor() * bundle.stream().mapToDouble(CATSLicense::getCommonValue).sum();
                originalBundleSize = bundle.size();
                originalBundleSet = new ArrayList<>(bundle);
                return new XORValue<>(bundle, value);
            } else {
                CATSLicense first = originalBundleSet.remove(0);
                bundle.add(first);
                while (bundle.size() < originalBundleSize) {
                    bundle.add(selectLicenseToAdd(bundle));
                }
                BigDecimal value = bidder.calculateValue(bundle);
                double resaleValue = bundle.stream().mapToDouble(CATSLicense::getCommonValue).sum();
                if (value.doubleValue() >= 0 && value.doubleValue() <= budget
                        && resaleValue >= minResaleValue) {
                    return new XORValue<>(bundle, value);
                } else {
                    if (hasNext()) {
                        return next();
                    } else {
                        throw new NoSuchElementException("No more bundles available");
                    }
                }
            }
        }

        private CATSLicense selectLicenseToAdd(Bundle<CATSLicense> bundle) {
            if (uniRng.nextDouble() <= world.getJumpProbability()) {
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
                WeightedRandomCollection neighbors = new WeightedRandomCollection(uniRng);
                // Filter the licenses that are not contained yet in the bundle and where there exists an edge to one
                // of the licenses in the bundle.
                goods.stream().filter(l -> !bundle.contains(l) && edgeExists(l, bundle))
                        .forEach(g -> {
                            double positivePrivateValue = bidder.getPrivateValues().get(g.getId()).doubleValue() - minValue;
                            neighbors.add(positivePrivateValue, g);
                        });
                return neighbors.nextWeightedRandom();
            }
        }

        private boolean edgeExists(CATSLicense license, Bundle<CATSLicense> bundle) {
            for (CATSLicense l : bundle) {
                if (world.getGrid().isAdjacent(license.getVertex(), l.getVertex()))
                    return true;
            }
            return false;
        }

        /**
         * @see Iterator#remove()
         */
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private class WeightedRandomCollection {
        private final NavigableMap<Double, CATSLicense> map = new TreeMap<>();
        private final UniformDistributionRNG random;
        private double total = 0;

        public WeightedRandomCollection(UniformDistributionRNG random) {
            this.random = random;
        }

        public void add(double weight, CATSLicense result) {
            total += weight;
            map.put(total, result);
        }

        public CATSLicense nextWeightedRandom() {

            double value = random.nextDouble() * total;
            Map.Entry<Double, CATSLicense> entry = map.higherEntry(value);
            return entry.getValue();
        }
    }

}
