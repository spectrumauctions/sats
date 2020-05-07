package org.spectrumauctions.sats.core.model.gsvm;

import com.google.common.base.Preconditions;
import org.spectrumauctions.sats.core.util.random.DoubleInterval;
import org.spectrumauctions.sats.core.util.random.RNGSupplier;
import org.spectrumauctions.sats.core.util.random.UniformDistributionRNG;

import java.math.BigDecimal;
import java.util.HashMap;

/**
 * @author Fabio Isler
 */
public class GSVMRegionalBidderSetup extends GSVMBidderSetup {

    private GSVMRegionalBidderSetup(GSVMBidderSetup.Builder builder) {
        super(builder);
    }

    @Override
    HashMap<Long, BigDecimal> drawValues(RNGSupplier rngSupplier, GSVMBidder bidder) {
        UniformDistributionRNG rng = rngSupplier.getUniformDistributionRNG();
        GSVMWorld world = bidder.getWorld();
        HashMap<Long, BigDecimal> values = new HashMap<>();
        // Add the national licenses
        for (GSVMLicense license : world.getNationalCircle().getLicenses()) {
            if (isInProximity(license.getPosition(), bidder.getBidderPosition(), world.getSize(), true)) {
                values.put(license.getLongId(), getValueDependingOnRegion(rng, license.getPosition(), world.getSize()));
            }
        }
        // Add the regional licenses
        for (GSVMLicense license : world.getRegionalCircle().getLicenses()) {
            if (isInProximity(license.getPosition(), bidder.getBidderPosition(), world.getSize(), false)) {
                values.put(license.getLongId(), rng.nextBigDecimal(getRegionalValueInterval()));
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
                    new DoubleInterval(0, 20), new DoubleInterval(0, 40), new DoubleInterval(0, 20),4);
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
