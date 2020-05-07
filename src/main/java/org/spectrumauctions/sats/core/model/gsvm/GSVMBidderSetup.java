package org.spectrumauctions.sats.core.model.gsvm;

import com.google.common.base.Preconditions;
import org.spectrumauctions.sats.core.model.BidderSetup;
import org.spectrumauctions.sats.core.util.random.DoubleInterval;
import org.spectrumauctions.sats.core.util.random.RNGSupplier;
import org.spectrumauctions.sats.core.util.random.UniformDistributionRNG;

import java.math.BigDecimal;
import java.util.HashMap;

/**
 * @author Fabio Isler
 */
public abstract class GSVMBidderSetup extends BidderSetup {

    private final DoubleInterval lowNationalValueInterval;
    private final DoubleInterval highNationalValueInterval;
    private final DoubleInterval regionalValueInterval;
    private final int activityLimit;

    GSVMBidderSetup(Builder builder) {
        super(builder);
        this.lowNationalValueInterval = builder.lowNationalValueInterval;
        this.highNationalValueInterval = builder.highNationalValueInterval;
        this.regionalValueInterval = builder.regionalValueInterval;
        this.activityLimit = builder.activityLimit;
    }

    public DoubleInterval getLowNationalValueInterval() {
        return lowNationalValueInterval;
    }

    public DoubleInterval getHighNationalValueInterval() {
        return highNationalValueInterval;
    }

    public DoubleInterval getRegionalValueInterval() {
        return regionalValueInterval;
    }

    abstract HashMap<Long, BigDecimal> drawValues(RNGSupplier rngSupplier, GSVMBidder bidder);

    /**
     * Represents the value distribution inside a circle, given by the Global Synergy Value Model.
     * The standard size is 6, where the first and last third has low values and the second third has low values.
     * This distribution is applied to any given size.
     *
     * @param rng      The random number generator to calculate the values
     * @param position The position of the license
     * @return The value for the license
     */
    BigDecimal getValueDependingOnRegion(UniformDistributionRNG rng, int position, int size) {
        if (position < size / 3 * 2) {
            return rng.nextBigDecimal(lowNationalValueInterval);
        } else if (position < size / 3 * 4) {
            return rng.nextBigDecimal(highNationalValueInterval);
        } else {
            return rng.nextBigDecimal(lowNationalValueInterval);
        }
    }

    protected static abstract class Builder extends BidderSetup.Builder {

        protected DoubleInterval lowNationalValueInterval;
        protected DoubleInterval highNationalValueInterval;
        protected DoubleInterval regionalValueInterval;
        protected int activityLimit;

        protected Builder(String setupName, int numberOfBidders,
                          DoubleInterval lnvi, DoubleInterval hnvi, DoubleInterval rvi, int activityLimit) {
            super(setupName, numberOfBidders);
            this.lowNationalValueInterval = lnvi;
            this.highNationalValueInterval = hnvi;
            this.regionalValueInterval = rvi;
            this.activityLimit = activityLimit;
        }

        /**
         * Set the value interval for the lower-valued part of the national circle
         *
         * @param iv The value interval
         */
        public void setLowNationalValueInterval(DoubleInterval iv) {
            Preconditions.checkArgument(iv.getMinValue() >= 0);
            this.lowNationalValueInterval = iv;
        }

        /**
         * Set the value interval for the higher-valued part of the national circle
         *
         * @param iv The value interval
         */
        public void setHighNationalValueInterval(DoubleInterval iv) {
            Preconditions.checkArgument(iv.getMinValue() >= 0);
            this.highNationalValueInterval = iv;
        }

        @Override
        public abstract GSVMBidderSetup build();
    }
    
    public int getActivityLimit() {
    	return this.activityLimit;
    }

}
