package org.spectrumauctions.sats.core.bidlang.generic.SizeOrderedPowerset;

import org.spectrumauctions.sats.core.bidlang.generic.GenericDefinition;
import org.spectrumauctions.sats.core.bidlang.generic.GenericLang;
import org.spectrumauctions.sats.core.bidlang.generic.GenericValue;
import org.spectrumauctions.sats.core.bidlang.generic.GenericValueBidder;
import org.spectrumauctions.sats.core.model.Good;
import org.spectrumauctions.sats.core.model.UnsupportedBiddingLanguageException;

import java.util.*;
import java.util.Map.Entry;

public abstract class GenericPowerset<T extends GenericDefinition<S>, S extends Good> implements GenericLang<T, S> {

    final Map<T, Integer> maxQuantities;
    final int maxBundleSize;

    protected GenericPowerset(List<T> genericDefinitions) throws UnsupportedBiddingLanguageException {
        super();
        Map<T, Integer> orderedMap = new LinkedHashMap<>();
        int quantitySum = 0;
        for (T def : genericDefinitions) {
            quantitySum += def.numberOfLicenses();
            orderedMap.put(def, def.numberOfLicenses());
        }
        this.maxQuantities = Collections.unmodifiableMap(orderedMap);
        this.maxBundleSize = quantitySum;
        isFeasibleSize(maxQuantities, maxBundleSize);
    }

    GenericPowerset(Map<T, Integer> maxQuantities, int maxBundleSize) throws UnsupportedBiddingLanguageException {
        super();
        isFeasibleSize(maxQuantities, maxBundleSize);
        this.maxQuantities = Collections.unmodifiableMap(new LinkedHashMap<>(maxQuantities));
        this.maxBundleSize = maxBundleSize;
    }

    protected abstract void isFeasibleSize(Map<T, Integer> maxQuantities, int maxBundleSize) throws UnsupportedBiddingLanguageException;

    protected abstract GenericValueBidder<T> getGenericBidder();

    abstract class PowersetIterator implements Iterator<GenericValue<T, S>> {


        int bundleSize;
        GenericSetsPickN<T> pickN;

        /**
         * @see java.util.Iterator#next()
         */
        @Override
        public GenericValue<T, S> next() {
            if (!pickN.hasNext()) {
                intiPickN();
            }
            Map<T, Integer> quantities = pickN.next();
            GenericValue.Builder<T, S> genValBuilder = new GenericValue.Builder<>(getGenericBidder());
            for (Entry<T, Integer> entry : quantities.entrySet()) {
                genValBuilder.putQuantity(entry.getKey(), entry.getValue());
            }
            return genValBuilder.build();
        }

        abstract void intiPickN();


    }


}
