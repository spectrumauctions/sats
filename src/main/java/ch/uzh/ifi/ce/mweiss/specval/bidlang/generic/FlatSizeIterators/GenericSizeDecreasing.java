/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.uzh.ifi.ce.mweiss.specval.bidlang.generic.FlatSizeIterators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import com.google.common.collect.Sets;

import ch.uzh.ifi.ce.mweiss.specval.bidlang.generic.GenericDefinition;
import ch.uzh.ifi.ce.mweiss.specval.bidlang.generic.GenericValue;
import ch.uzh.ifi.ce.mweiss.specval.bidlang.generic.GenericValue.Builder;
import ch.uzh.ifi.ce.mweiss.specval.model.UnsupportedBiddingLanguageException;

/**
 * @author Michael Weiss
 *
 */
public abstract class GenericSizeDecreasing<T extends GenericDefinition> extends GenericSizeOrdered<T>{

    /**
     * @param allPossibleGenericDefintions
     * @throws UnsupportedBiddingLanguageException
     */
    protected GenericSizeDecreasing(Collection<T> allPossibleGenericDefintions) throws UnsupportedBiddingLanguageException {
        super(allPossibleGenericDefintions);
    }

    @Override
    public Iterator<GenericValue<T>> iterator() {
        return new DecreasingIterator();
    }
    
    private class DecreasingIterator implements Iterator<GenericValue<T>>{
        
        private Iterator<Set<T>> definitionPowersetIterator;
        int round = 0;
        private Map<T, Integer> roundSize;
        
        private boolean hasNext;
        
        protected DecreasingIterator(){
            initNextRound();
        }
        
        private void initNextRound() {
            roundSize = new HashMap<>();
            for(T def : allDefintions){
                int quantity = def.numberOfLicenses() - round;
                if(quantity < 0){
                    quantity = 0;
                }
                roundSize.put(def, quantity);
            }
            round++;
            initPowersetIterator();
        }
        
        private void initPowersetIterator(){
            // Create set of definition with leftover quantities
            Set<T> leftOverQuantities = new HashSet<>();
            for(T def : allDefintions){
                if(roundSize.get(def) > 0){
                    leftOverQuantities.add(def);
                }
            }
            if(leftOverQuantities.size() == 0){
                hasNext = false;
                return;
            }else{
                hasNext = true;
                Set<Set<T>> definitionPowerset = Sets.powerSet(leftOverQuantities);
                List<Set<T>> sorted = new ArrayList<>(definitionPowerset);
                Collections.sort(sorted,getIncreasingSizeComparator());
                definitionPowersetIterator = sorted.iterator();
            }
            
        }
        
        
        /* (non-Javadoc)
         * @see java.util.Iterator#hasNext()
         */
        @Override
        public boolean hasNext() {
            return hasNext;
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#next()
         */
        @Override
        public GenericValue<T> next() {
            if(!hasNext()){
                throw new NoSuchElementException();
            }
            GenericValue.Builder<T> val = new Builder<T>(getGenericBidder());
            Set<T> toSubstract = definitionPowersetIterator.next();
            for(T def : allDefintions){
                int quantity = roundSize.get(def);
                if(toSubstract.contains(def)){
                    quantity--;
                }
                val.putQuantity(def, quantity);
            }
            if(!definitionPowersetIterator.hasNext()){
                initNextRound();
            }
            return val.build();
        }   
        
        /* (non-Javadoc)
         * @see java.util.Iterator#remove()
         */
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
        
    }


    
}
