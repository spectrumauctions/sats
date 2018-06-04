package org.spectrumauctions.sats.core.bidlang.generic;


import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.Good;

import java.util.ArrayList;
import java.util.List;

public class GenericBid<S extends GenericDefinition<T>, T extends Good> {

    private final Bidder<T> bidder;
    private List<GenericValue<S, T>> values;

    public GenericBid(Bidder<T> bidder, List<GenericValue<S, T>> values) {
        this.bidder = bidder;
        this.values = values;
    }

    /**
     * @return The bidder for which this bid
     */
    public Bidder<T> getBidder() {
        return bidder;
    }

    /**
     * @return Returns an unmodifiable list of all (atomic) XOR values in this bid
     */
    public List<GenericValue<S, T>> getValues() {
        return values;
    }

    public void addValue(GenericValue<S, T> value) {
        values.add(value);
    }

    public void removeValue(GenericValue<S, T> value) {
        values.remove(value);
    }

    public GenericBid<S, T> copyOf() {
        ArrayList<GenericValue<S, T>> newValues = new ArrayList<>(this.values);
        return new GenericBid<>(this.bidder, newValues);
    }
}
