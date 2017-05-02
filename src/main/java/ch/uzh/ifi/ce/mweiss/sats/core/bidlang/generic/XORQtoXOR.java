/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.uzh.ifi.ce.mweiss.sats.core.bidlang.generic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import ch.uzh.ifi.ce.mweiss.sats.core.model.Good;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import ch.uzh.ifi.ce.mweiss.sats.core.bidlang.generic.SizeOrderedPowerset.GenericSetsPickN;
import ch.uzh.ifi.ce.mweiss.sats.core.model.Bundle;

/**
 * @author Michael Weiss
 *
 */
public class XORQtoXOR<T extends Good> implements Iterator<Bundle<T>>{

    
    private final ImmutableMap<GenericDefinition, Integer> quantitites;
    private final ImmutableList<GenericDefinition> orderOfDefs;
    List<GenericSetsPickN<T>> pickNiterators;
    List<List<T>> currentSets;
    
    
    
    /**
     * @param quantitites
     * @param orderOfDefs
     */
    XORQtoXOR(Map<? extends GenericDefinition, Integer> quantitites) {
        super();
        ImmutableMap.Builder<GenericDefinition, Integer> nonZeroQuantitiesBuilder = new ImmutableMap.Builder<>();
        ImmutableList.Builder<GenericDefinition> orderBuilder = new ImmutableList.Builder<>();
        for(Entry<? extends GenericDefinition, Integer> quantity : quantitites.entrySet()){
            Preconditions.checkArgument(quantity.getValue() >= 0 , "Quantity %s of generic definition %s is invalid", new Object[]{quantity.getValue(), quantity.getKey()});
            if(quantity.getValue() > 0){
                nonZeroQuantitiesBuilder.put(quantity);
                orderBuilder.add(quantity.getKey());
            }
        }
        this.quantitites = nonZeroQuantitiesBuilder.build();
        this.orderOfDefs = orderBuilder.build();
        Preconditions.checkArgument(this.orderOfDefs.size() != 0, "Must define a strictly positive total quantity in the quantities map");
        currentSets = new ArrayList<>();
        pickNiterators = new ArrayList<>();
        for(int i = 0; i < this.orderOfDefs.size(); i++){
            pickNiterators.add(null); //Ensure that list is big enough for set operations
            currentSets.add(null); // Ensure that list is big enough for set operations
            resetIterator(i);
            if(i > 0){
                List<T> initialGoodSelection = quantityOneLicenses(pickNiterators.get(i).next());
                currentSets.set(i, initialGoodSelection);
            }
        }
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#hasNext()
     */
    @Override
    public boolean hasNext() {
        for(GenericSetsPickN<? extends Good> pickNiter : pickNiterators){
            if(pickNiter.hasNext()){
                return true;
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#next()
     */
    @Override
    public Bundle<T> next() {
        for(int i = 0; i < pickNiterators.size(); i++){
            if(pickNiterators.get(i).hasNext()){
                List<T> quantityOneLicenses = quantityOneLicenses(pickNiterators.get(i).next());
                currentSets.set(i, quantityOneLicenses);
                break;
            }else{
                if(i == pickNiterators.size()-1){
                    //Iterated through all combinations
                    throw new NoSuchElementException();
                }else{
                    resetIterator(i);
                    List<T> quantityOneLicenses = quantityOneLicenses(pickNiterators.get(i).next());
                    currentSets.set(i, quantityOneLicenses);
                    //And go to next round in loop, increasing the next iterator by one
                }
            }
        }
        Bundle<T> bundle = new Bundle<>();
        for(Collection<T> licenses : currentSets){
            bundle.addAll(licenses);
        }
        return bundle;
    }
    
    /**
     * @param next
     * @return
     */
    private List<T> quantityOneLicenses(Map<T, Integer> quantities) {
        List<T> result = new ArrayList<>();
        for(Entry<? extends T, Integer> entry : quantities.entrySet()){
            if(entry.getValue() == 1){
                    result.add(entry.getKey());
            }else if(entry.getValue() != 0){
                throw new IllegalArgumentException("All quantities must be either 0 or 1");
            }
        }
        return result;
    }


    @SuppressWarnings("unchecked")
    private void resetIterator(int iteratorNumber){
        Map<T, Integer> maxQuantities = new LinkedHashMap<>();
        if(iteratorNumber >= orderOfDefs.size()){
            System.out.println("abort");
        }
        for(Good good : orderOfDefs.get(iteratorNumber).allLicenses()){
            try{
                maxQuantities.put((T) good, 1);
            }catch (ClassCastException e) {
                throw new IllegalArgumentException("Generic Definition License Type is different than the requested License Type");
            }
        }
        int bundleSize = quantitites.get(orderOfDefs.get(iteratorNumber));
        GenericSetsPickN<T> pickNIter = new GenericSetsPickN<>(maxQuantities, bundleSize);
        pickNiterators.set(iteratorNumber, pickNIter);
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#remove()
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
    
    

}
