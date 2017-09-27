package org.spectrumauctions.sats.core.model.gsvm;

import org.spectrumauctions.sats.core.util.random.DoubleInterval;
import org.spectrumauctions.sats.core.util.random.RNGSupplier;
import org.spectrumauctions.sats.core.util.random.UniformDistributionRNG;

import java.math.BigDecimal;
import java.util.HashMap;

/**
 * @author Fabio Isler
 */
public class GSVMNationalBidderSetup extends GSVMBidderSetup {

    private GSVMNationalBidderSetup(GSVMBidderSetup.Builder builder) {
        super(builder);
    }

    @Override
    HashMap<Long, BigDecimal> drawValues(RNGSupplier rngSupplier, GSVMBidder bidder) {
        UniformDistributionRNG rng = rngSupplier.getUniformDistributionRNG();
        GSVMWorld world = bidder.getWorld();
        HashMap<Long, BigDecimal> values = new HashMap<>();
        // Add all national licenses
        for (GSVMLicense license : world.getNationalCircle().getLicenses()) {
            values.put(license.getId(), getValueDependingOnRegion(rng, license.getPosition(), world.getSize()));
        }
        return values;
    }

    public static class Builder extends GSVMBidderSetup.Builder {

        /**
         * Create a BidderSetup Builder
         */
        public Builder() {
            super("National Bidder Setup", 1,
                    new DoubleInterval(0, 10), new DoubleInterval(0, 20), new DoubleInterval(0, 0));
        }

        @Override
        public GSVMNationalBidderSetup build() {
            return new GSVMNationalBidderSetup(this);
        }


    }

}
