/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.bidlang.generic;

import org.spectrumauctions.sats.core.bidlang.xor.XORValue;
import org.spectrumauctions.sats.core.model.Bundle;
import org.spectrumauctions.sats.core.model.Good;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import java.math.BigDecimal;
import java.util.*;

public final class GenericValue<T extends GenericDefinition> {

    private final int totalQuantity;
    private final ImmutableMap<T, Integer> quantities;
    private final BigDecimal value;

    private final transient int size;

    private GenericValue(Builder<T> builder) {
        this.quantities = ImmutableMap.copyOf(builder.quantities);
        int totalQuantity = 0;
        for (Integer quantity : quantities.values()) {
            totalQuantity += quantity;
        }
        this.totalQuantity = totalQuantity;
        this.value = builder.value;
        this.size = calcSize();
    }

    public int getQuantity(T definition) {
        Integer result = quantities.get(definition);
        if (result == null) {
            return 0;
        }
        return result;
    }

    public BigDecimal getValue() {
        return value;
    }
    
    public Bundle<?> anyConsistentBundle(){
        Bundle bundle = new Bundle<>();
        for(Map.Entry<T, Integer> entry : quantities.entrySet()){
            List<? extends Good> objects = new ArrayList<>(entry.getKey().allLicenses());
            bundle.addAll(objects.subList(0, entry.getValue()));
        }
        return bundle;
    }
    

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GenericValue<?> that = (GenericValue<?>) o;

        if (totalQuantity != that.totalQuantity) return false;
        if (size != that.size) return false;
        if (quantities != null ? !quantities.equals(that.quantities) : that.quantities != null) return false;
        return value != null ? value.equals(that.value) : that.value == null;
    }

    @Override
    public int hashCode() {
        int result = totalQuantity;
        result = 31 * result + (quantities != null ? quantities.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + size;
        return result;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public ImmutableMap<T, Integer> getQuantities() {
        return quantities;
    }

    /**
     * The number of licenses in this bundle
     *
     * @return
     */
    private int calcSize() {
        int size = 0;
        for (int quantity : quantities.values()) {
            size += quantity;
        }
        return size;
    }

    /**
     * An iterator over all XORValues consistent with this XOR-Q (XOR with quantities) instance
     */
    public Iterator<XORValue<?>> plainXorIterator() {
        return new Iterator<XORValue<?>>() {

            private XORQtoXOR<Good> quantitiesIter = new XORQtoXOR<>(quantities);

            @Override
            public boolean hasNext() {
                return quantitiesIter.hasNext();
            }

            @Override
            public XORValue<?> next() {
                Bundle<Good> bundle = quantitiesIter.next();
                return new XORValue<>(bundle, GenericValue.this.value);
            }

            /* (non-Javadoc)
             * @see java.util.Iterator#remove()
             */
            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

        };
    }


    public int getSize() {
        return size;
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        for (GenericDefinition property : quantities.keySet()) {
            res.append(property.toString()).append(":").append(quantities.get(property)).append(" ");
        }
        res.append("\t").append(value);
        return res.toString();
    }

    public static class Builder<T extends GenericDefinition> {

        private BigDecimal value;
        private GenericValueBidder<T> bidder;

        private Map<T, Integer> quantities;

        public Builder(BigDecimal value) {
            this.quantities = new HashMap<>();
            this.value = value;
        }

        public Builder(GenericValueBidder<T> bidder) {
            this.quantities = new HashMap<>();
            this.bidder = bidder;
        }

        public void putQuantity(T def, int quantity) {
            Preconditions.checkNotNull(def);
            Preconditions.checkNotNull(quantity);
            Preconditions.checkArgument(quantity >= 0);
            quantities.put(def, quantity);
        }

        public GenericValue<T> build() {
            if (value == null) {
                value = bidder.calculateValue(quantities);
            }
            return new GenericValue<>(this);
        }
    }
}
