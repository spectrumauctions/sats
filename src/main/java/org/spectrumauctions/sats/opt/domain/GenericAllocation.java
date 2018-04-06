/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.opt.domain;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import org.spectrumauctions.sats.core.bidlang.generic.GenericDefinition;
import org.spectrumauctions.sats.core.bidlang.generic.GenericValue;
import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.Bundle;
import org.spectrumauctions.sats.core.model.Good;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Michael Weiss
 *
 */
public class GenericAllocation<T extends GenericDefinition<S>, S extends Good> implements Allocation<S> {

    protected final ImmutableMap<Bidder<S>, GenericValue<T, S>> values;

    protected GenericAllocation(Builder<T, S> builder) {
        this.values = ImmutableMap.copyOf(builder.storedValues);
    }

    public ImmutableMap<T, Integer> getQuantities(Bidder<S> bidder) {
        return values.get(bidder).getQuantities();
    }

    /* (non-Javadoc)
     * @see Allocation#getAllocatedItems(org.spectrumauctions.sats.core.model.Bidder)
     */
    @Override
    public Bundle<S> getAllocation(Bidder<S> bidder) {
        return values.get(bidder).anyConsistentBundle();
    }

    public GenericValue<T, S> getGenericAllocation(Bidder<S> bidder) {
        return values.get(bidder);
    }

    /* (non-Javadoc)
     * @see Allocation#getWinners()
     */
    @Override
    public Collection<Bidder<S>> getWinners() {
        return values.keySet();
    }

    @Override
    public BigDecimal getTotalValue() {
        BigDecimal sum = BigDecimal.ZERO;
        for (GenericValue<T, S> genVal : values.values()) {
            sum.add(genVal.getValue());
        }
        return sum;
    }

    @Override
    public BigDecimal getTradeValue(Bidder<S> bidder) {
        if (bidder == null || !values.containsKey(bidder)) return BigDecimal.ZERO;
        return values.get(bidder).getValue();
    }

    public static class Builder<T extends GenericDefinition<S>, S extends Good> {

        private Map<Bidder<S>, GenericValue<T, S>> storedValues;

        public Builder() {
            this.storedValues = new HashMap<>();
        }

        public void putGenericValue(Bidder<S> bidder, GenericValue<T, S> value) {
            Preconditions.checkNotNull(bidder);
            Preconditions.checkNotNull(value);
            storedValues.put(bidder, value);
        }
    }

}
