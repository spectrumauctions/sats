package ch.uzh.ifi.ce.mweiss.sats.core.model.cats;

import ch.uzh.ifi.ce.mweiss.sats.core.model.BidderSetup;
import ch.uzh.ifi.ce.mweiss.sats.core.util.random.GaussianDistributionRNG;
import ch.uzh.ifi.ce.mweiss.sats.core.util.random.RNGSupplier;
import com.google.common.base.Preconditions;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Fabio Isler
 */
public class CATSBidderSetup extends BidderSetup {

    private final double privateValueMean;
    private final double privateValueStDev;

    private CATSBidderSetup(Builder builder) {
        super(builder);
        this.privateValueMean = builder.privateValueMean;
        this.privateValueStDev = builder.privateValueStDev;
    }

    Map<Long, BigDecimal> drawPrivateValues(RNGSupplier rngSupplier, CATSBidder bidder) {
        GaussianDistributionRNG rng = rngSupplier.getGaussianDistributionRNG();
        CATSWorld world = bidder.getWorld();
        Map<Long, BigDecimal> values = new HashMap<>();
        for (CATSLicense license : world.getLicenses()) {
            values.put(license.getId(), new BigDecimal(rng.nextGaussian(privateValueMean, privateValueStDev)));
        }
        return values;
    }

    public static class Builder extends BidderSetup.Builder {

        // CATS default parameters
        private static final int DEFAULT_NUMBER_OF_BIDDERS = 1;
        private static final double DEFAULT_PRIVATE_VALUE_MEAN = 0;
        private static final double DEFAULT_PRIVATE_VALUE_STDEV = 30;

        private static final String DEFAULT_SETUP_NAME = "CATS Bidder Setup";

        private double privateValueMean;
        private double privateValueStDev;

        public Builder() {
            super(DEFAULT_SETUP_NAME, DEFAULT_NUMBER_OF_BIDDERS);
            this.privateValueMean = DEFAULT_PRIVATE_VALUE_MEAN;
            this.privateValueStDev = DEFAULT_PRIVATE_VALUE_STDEV;
        }

        /**
         * Set the value interval for the private values
         *
         * @param mean The mean of the distribution
         * @param stdev The standard deviation of the distribution
         */
        public void setPrivateValueParameters(double mean, double stdev) {
            Preconditions.checkArgument(stdev >= 0);
            this.privateValueMean = mean;
            this.privateValueStDev = stdev;
        }

        @Override
        public CATSBidderSetup build() {
            return new CATSBidderSetup(this);
        }
    }

}
