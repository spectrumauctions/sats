package org.spectrumauctions.sats.core.bidlang.xor;


import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.Bundle;
import org.spectrumauctions.sats.core.model.Good;
import org.spectrumauctions.sats.core.model.UnequalWorldsException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class XORBid<T extends Good> {

    private final Bidder<T> bidder;
    private final List<XORValue<T>> values;

    private XORBid(Builder<T> builder) {
        this.values = Collections.unmodifiableList(new ArrayList<>(builder));
        this.bidder = builder.bidder;
        validate();
    }

    /**
     * Checks that all the goods as well as the bidder are in the same world
     *
     * @throws UnequalWorldsException if validation fails
     */
    private void validate() {
        for (XORValue<T> value : values) {
            for (T good : value.getLicenses()) {
                if (!bidder.getWorld().equals(good.getWorld())) {
                    throw new UnequalWorldsException("bidder and good are not from the same world");
                }
            }
        }
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
    public List<XORValue<T>> getValues() {
        return values;
    }

    /**
     * <p>Builder to create a new set of atomic XORValues, called {@link XORBid}
     * <p>For easy modification of the builder, it extends {@link ArrayList}
     *
     * @param <T> The type of Goods in the atomic XORValues
     * @author Michael Weiss
     */
    public static final class Builder<T extends Good> extends ArrayList<XORValue<T>> {


        private static final long serialVersionUID = -6540446139983915994L;

        private final Bidder<T> bidder;

        /**
         * @param bidder the bidder this bid belongs to
         */
        public Builder(Bidder<T> bidder) {
            super();
            this.bidder = bidder;
        }

        public XORValue<T> containsBundle(Bundle<T> bundle) {
            XORValue<T> exists = null;
            for (XORValue<T> xorValue : this) {
                if (xorValue.getLicenses().equals(bundle)) {
                    exists = xorValue;
                    break;
                }
            }
            return exists;
        }

        /**
         * Creates a new Builder instance with initial XORValues
         *
         * @param bidder the bidder this bid belongs to
         * @param values initial XORValues to be added to this builder, as specified in {@link ArrayList#ArrayList(Collection)}
         */
        public Builder(Bidder<T> bidder, Collection<? extends XORValue<T>> values) {
            super(values);
            this.bidder = bidder;
        }

        /**
         * @return A new, immutable, XORBid containing bids and bidder as specified in this builder
         * @throws UnequalWorldsException if the bidder and at least on of the licenses
         *                                in the XORValues are not from the same world
         */
        public XORBid<T> build() {
            return new XORBid<>(this);
        }

        public void removeFromBid(XORValue<T> existing) {
            super.remove(existing);
        }
    }


}
