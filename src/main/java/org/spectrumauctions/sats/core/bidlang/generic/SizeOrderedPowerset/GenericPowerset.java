package org.spectrumauctions.sats.core.bidlang.generic.SizeOrderedPowerset;

import org.marketdesignresearch.mechlib.domain.Bundle;
import org.marketdesignresearch.mechlib.domain.BundleEntry;
import org.marketdesignresearch.mechlib.domain.bidder.value.BundleValue;
import org.spectrumauctions.sats.core.bidlang.BiddingLanguage;
import org.spectrumauctions.sats.core.model.GenericGood;
import org.spectrumauctions.sats.core.model.UnsupportedBiddingLanguageException;

import java.util.*;
import java.util.Map.Entry;

public abstract class GenericPowerset implements BiddingLanguage {

    final Map<GenericGood, Integer> maxQuantities;
    final int maxBundleSize;

    protected GenericPowerset(List<? extends GenericGood> genericGoods) throws UnsupportedBiddingLanguageException {
        super();
        Map<GenericGood, Integer> orderedMap = new LinkedHashMap<>();
        for (GenericGood good : genericGoods) {
            orderedMap.put(good, good.available());
        }
        this.maxQuantities = Collections.unmodifiableMap(orderedMap);
        this.maxBundleSize = genericGoods.stream().mapToInt(GenericGood::available).sum();
        isFeasibleSize(orderedMap, maxBundleSize);
    }

    GenericPowerset(Map<? extends GenericGood, Integer> maxQuantities, int maxBundleSize) throws UnsupportedBiddingLanguageException {
        super();
        this.maxQuantities = Collections.unmodifiableMap(maxQuantities);
        this.maxBundleSize = maxBundleSize;
        isFeasibleSize(maxQuantities, maxBundleSize);
    }

    protected abstract void isFeasibleSize(Map<? extends GenericGood, Integer> maxQuantities, int maxBundleSize) throws UnsupportedBiddingLanguageException;

    abstract class PowersetIterator implements Iterator<BundleValue> {


        int bundleSize;
        GenericSetsPickN<GenericGood> pickN;

        /**
         * @see java.util.Iterator#next()
         */
        @Override
        public BundleValue next() {
            if (!pickN.hasNext()) {
                intiPickN();
            }
            Map<GenericGood, Integer> quantities = pickN.next();
            HashSet<BundleEntry> bundleEntries = new HashSet<>();
            for (Entry<GenericGood, Integer> entry : quantities.entrySet()) {
                if (entry.getValue() > 0) {
                    bundleEntries.add(new BundleEntry(entry.getKey(), entry.getValue()));
                }
            }
            Bundle bundle = new Bundle(bundleEntries);
            return new BundleValue(getBidder().calculateValue(bundle), bundle);
        }

        abstract void intiPickN();


    }


}
