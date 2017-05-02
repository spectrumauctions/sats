/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.uzh.ifi.ce.mweiss.sats.opt.model;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import ch.uzh.ifi.ce.mweiss.specval.bidlang.generic.GenericDefinition;
import ch.uzh.ifi.ce.mweiss.specval.bidlang.generic.GenericValue;
import ch.uzh.ifi.ce.mweiss.specval.model.Bidder;

/**
 * @author Michael Weiss
 *
 */
public class GenericAllocation<T extends GenericDefinition> implements Allocation<GenericValue<T>> {

    protected final ImmutableMap<Bidder<?>, GenericValue<T>> values;

    protected GenericAllocation(Builder<T> builder) {
        this.values = ImmutableMap.copyOf(builder.storedValues);
    }
    
    /* (non-Javadoc)
     * @see Allocation#getAllocatedItems(ch.uzh.ifi.ce.mweiss.specval.model.Bidder)
     */
    @Override
    public GenericValue<T> getAllocation(Bidder<?> bidder) {
        return values.get(bidder);
    }

    /* (non-Javadoc)
     * @see Allocation#getBidders()
     */
    @Override
    public Collection<Bidder<?>> getBidders() {
        return values.keySet();
    }
    
	@Override
	public BigDecimal getTotalValue() {
		BigDecimal sum = BigDecimal.ZERO;
		for(GenericValue<T> genVal : values.values()){
			sum.add(genVal.getValue());
		}
		return sum;
	}
  
    public static class Builder<T extends GenericDefinition>{
        
        private Map<Bidder<?>, GenericValue<T>> storedValues;
        
        public Builder() {
            this.storedValues = new HashMap<>();
        }
        
        public void putGenericValue(Bidder<?> bidder, GenericValue<T> value){
            Preconditions.checkNotNull(bidder);
            Preconditions.checkNotNull(value);
            storedValues.put(bidder, value);
        }     
    }
  
}
