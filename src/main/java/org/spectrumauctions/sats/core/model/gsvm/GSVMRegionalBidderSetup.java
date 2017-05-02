package org.spectrumauctions.sats.core.model.gsvm;

import org.spectrumauctions.sats.core.util.random.DoubleInterval;
import org.spectrumauctions.sats.core.util.random.RNGSupplier;
import org.spectrumauctions.sats.core.util.random.UniformDistributionRNG;
import com.google.common.base.Preconditions;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Fabio Isler
 */
public class GSVMRegionalBidderSetup extends GSVMBidderSetup {

    private GSVMRegionalBidderSetup(GSVMBidderSetup.Builder builder) {
        super(builder);
    }

    @Override
    Map<Long, BigDecimal> drawValues(RNGSupplier rngSupplier, GSVMBidder bidder) {
        UniformDistributionRNG rng = rngSupplier.getUniformDistributionRNG();
        GSVMWorld world = bidder.getWorld();
        Map<Long, BigDecimal> values = new HashMap<>();
        // Add the national licenses
        for (GSVMLicense license : world.getNationalCircle().getLicenses()) {
            if (isInProximity(license.getPosition(), bidder.getBidderPosition(), world.getSize(), true)) {
                values.put(license.getId(), getValueDependingOnRegion(rng, license.getPosition(), world.getSize()));
            }
        }
        // Add the regional licenses
        for (GSVMLicense license : world.getRegionalCircle().getLicenses()) {
            if (isInProximity(license.getPosition(), bidder.getBidderPosition(), world.getSize(), false)) {
                values.put(license.getId(), rng.nextBigDecimal(getRegionalValueInterval()));
            }
        }
        return values;
    }

    private boolean isInProximity(int licensePosition, int bidderPosition, int size, boolean isNationalCircle) {
        int factor = isNationalCircle ? 1 : 2;
        bidderPosition = bidderPosition * 2 / factor;
        return licensePosition == bidderPosition
                || licensePosition == (bidderPosition + 1 / factor) % (size * 2 / factor)
                || licensePosition == (bidderPosition + 2 / factor) % (size * 2 / factor)
                || licensePosition == (bidderPosition + 3 / factor) % (size * 2 / factor);
    }

    public static class Builder extends GSVMBidderSetup.Builder {

        /**
         * Create a BidderSetup Builder
         */
        public Builder() {
            super("Regional Bidder Setup", 6,
                    new DoubleInterval(0, 20), new DoubleInterval(0, 40), new DoubleInterval(0, 20));
        }

        /**
         * Set the value interval for the regional circle
         *
         * @param iv The value interval
         */
        public void setRegionalValueInterval(DoubleInterval iv) {
            Preconditions.checkArgument(iv.getMinValue() >= 0);
            super.regionalValueInterval = iv;
        }

        @Override
        public GSVMRegionalBidderSetup build() {
            return new GSVMRegionalBidderSetup(this);
        }


    }

}
