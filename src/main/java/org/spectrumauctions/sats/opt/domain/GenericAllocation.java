/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.opt.domain;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

    private static final Logger logger = LogManager.getLogger(GenericAllocation.class);

    protected final ImmutableMap<Bidder<S>, GenericValue<T, S>> values;

    public GenericAllocation(Builder<T, S> builder) {
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
        return values.values().stream().map(GenericValue::getValue).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal getTradeValue(Bidder<S> bidder) {
        if (bidder == null || !values.containsKey(bidder)) return BigDecimal.ZERO;
        return values.get(bidder).getValue();
    }

    @Override
    public Allocation<S> getAllocationWithTrueValues() {
        GenericAllocation.Builder<T, S> builder = new GenericAllocation.Builder<>();
        for (Map.Entry<Bidder<S>, GenericValue<T, S>> bidderEntry : values.entrySet()) {
            BigDecimal trueValue = bidderEntry.getKey().calculateValue(bidderEntry.getValue().anyConsistentBundle());
            GenericValue.Builder<T, S> trueEntryBuilder = new GenericValue.Builder<>(trueValue);
            for (Map.Entry<T, Integer> quantities : bidderEntry.getValue().getQuantities().entrySet()) {
                trueEntryBuilder.putQuantity(quantities.getKey(), quantities.getValue());
            }
            builder.putGenericValue(bidderEntry.getKey(), trueEntryBuilder.build());
        }
        Allocation<S> allocationWithTrueValues = new GenericAllocation<>(builder);
        if (this.equals(allocationWithTrueValues)) {
            logger.warn("Requested allocation with true values when initial allocation already included true values.");
        }
        return allocationWithTrueValues;
    }

    public static class Builder<G extends GenericDefinition<T>, T extends Good> {

        private Map<Bidder<T>, GenericValue<G, T>> storedValues;

        public Builder() {
            this.storedValues = new HashMap<>();
        }

        public void putGenericValue(Bidder<T> bidder, GenericValue<G, T> value) {
            Preconditions.checkNotNull(bidder);
            Preconditions.checkNotNull(value);
            storedValues.put(bidder, value);
        }
    }

}
