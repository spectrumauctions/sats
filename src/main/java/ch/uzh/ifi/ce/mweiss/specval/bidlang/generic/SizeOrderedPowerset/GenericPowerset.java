package ch.uzh.ifi.ce.mweiss.specval.bidlang.generic.SizeOrderedPowerset;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ch.uzh.ifi.ce.mweiss.specval.bidlang.generic.GenericDefinition;
import ch.uzh.ifi.ce.mweiss.specval.bidlang.generic.GenericLang;
import ch.uzh.ifi.ce.mweiss.specval.bidlang.generic.GenericValue;
import ch.uzh.ifi.ce.mweiss.specval.bidlang.generic.GenericValueBidder;
import ch.uzh.ifi.ce.mweiss.specval.model.UnsupportedBiddingLanguageException;

public abstract class GenericPowerset <T extends GenericDefinition> implements GenericLang<T>{

    final Map<T, Integer> maxQuantities;
    final int maxBundleSize;
    

    /**
     * @param maxQuantities, in increasing order of priority
     * @throws UnsupportedBiddingLanguageException 
     */
    protected GenericPowerset(List<T> genericDefinitions) throws UnsupportedBiddingLanguageException {
        super();
        Map<T, Integer> orderedMap = new LinkedHashMap<>();
        int quantitySum = 0;
        for(T def : genericDefinitions){
            quantitySum += def.numberOfLicenses();
            orderedMap.put(def, def.numberOfLicenses());
        }
        this.maxQuantities = Collections.unmodifiableMap(orderedMap);
        this.maxBundleSize = quantitySum;
        isFeasibleSize(maxQuantities, maxBundleSize);
    }


    
    /**
     * @param maxQuantities
     * @param maxBundleSize
     * @throws UnsupportedBiddingLanguageException 
     */
    GenericPowerset(Map<T, Integer> maxQuantities, int maxBundleSize) throws UnsupportedBiddingLanguageException {
        super();
        isFeasibleSize(maxQuantities, maxBundleSize);
        this.maxQuantities = Collections.unmodifiableMap(new LinkedHashMap<>(maxQuantities));
        this.maxBundleSize = maxBundleSize;
    }
    
    protected abstract void isFeasibleSize(Map<T, Integer> maxQuantities, int maxBundleSize) throws UnsupportedBiddingLanguageException;

    protected abstract GenericValueBidder<T> getGenericBidder();
    
    abstract class PowersetIterator implements Iterator<GenericValue<T>>{
        

        int bundleSize;
        GenericSetsPickN<T> pickN;
        
        /* (non-Javadoc)
         * @see java.util.Iterator#next()
         */
        @Override
        public GenericValue<T> next() {
            if(!pickN.hasNext()){
                intiPickN();
            }
            Map<T, Integer> quantities = pickN.next();
            GenericValue.Builder<T> genValBuilder = new GenericValue.Builder<>(getGenericBidder());
            for(Entry<T, Integer> entry : quantities.entrySet()){
                genValBuilder.putQuantity(entry.getKey(), entry.getValue());
            }
            return genValBuilder.build();
        }

        abstract void intiPickN();
        
        
    }
    
    
    
}
