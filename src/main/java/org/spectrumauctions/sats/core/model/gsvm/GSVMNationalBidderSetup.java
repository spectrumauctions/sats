package org.spectrumauctions.sats.core.model.gsvm;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;

import org.marketdesignresearch.mechlib.core.allocationlimits.AllocationLimit;
import org.marketdesignresearch.mechlib.core.allocationlimits.DefaultBundleSizeAndGoodAllocationLimit;
import org.marketdesignresearch.mechlib.core.allocationlimits.DefaultGoodAllocationLimit;
import org.spectrumauctions.sats.core.util.random.DoubleInterval;
import org.spectrumauctions.sats.core.util.random.RNGSupplier;
import org.spectrumauctions.sats.core.util.random.UniformDistributionRNG;

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
            values.put(license.getLongId(), getValueDependingOnRegion(rng, license.getPosition(), world.getSize()));
        }
        return values;
    }

    @Override
    public AllocationLimit getAllocationLimit(GSVMBidder bidder) {
    	if(bidder.getWorld().isLegacyGSVM())
    		return AllocationLimit.NO;
    	
        if (getActivityLimitOverride() > -1) {
        	new DefaultBundleSizeAndGoodAllocationLimit(getActivityLimitOverride(), Arrays.asList(bidder.getWorld().getNationalCircle().getLicenses()));
        }
        
        return new DefaultGoodAllocationLimit(Arrays.asList(bidder.getWorld().getNationalCircle().getLicenses()));
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
