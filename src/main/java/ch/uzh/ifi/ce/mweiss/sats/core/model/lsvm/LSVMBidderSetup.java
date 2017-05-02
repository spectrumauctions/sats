package ch.uzh.ifi.ce.mweiss.sats.core.model.lsvm;

import ch.uzh.ifi.ce.mweiss.sats.core.model.BidderSetup;
import ch.uzh.ifi.ce.mweiss.sats.core.util.PreconditionUtils;
import ch.uzh.ifi.ce.mweiss.sats.core.util.random.DoubleInterval;
import ch.uzh.ifi.ce.mweiss.sats.core.util.random.IntegerInterval;
import ch.uzh.ifi.ce.mweiss.sats.core.util.random.RNGSupplier;
import ch.uzh.ifi.ce.mweiss.sats.core.util.random.UniformDistributionRNG;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Fabio Isler
 */
public class LSVMBidderSetup extends BidderSetup {

    private final DoubleInterval valueInterval;
    private final int proximitySize;
    private final int a;
    private final int b;

    private LSVMBidderSetup(Builder builder) {
        super(builder);
        this.valueInterval = builder.valueInterval;
        this.proximitySize = builder.proximitySize;
        this.a = builder.a;
        this.b = builder.b;
    }

    LSVMLicense drawFavorite(RNGSupplier rngSupplier, LSVMWorld world) {
        UniformDistributionRNG rng = rngSupplier.getUniformDistributionRNG();
        int row = rng.nextInt(new IntegerInterval(0, world.getGrid().getNumberOfRows() - 1));
        int column = rng.nextInt(new IntegerInterval(0, world.getGrid().getNumberOfColumns() - 1));
        return world.getGrid().getLicense(row, column);
    }

    Map<Long, BigDecimal> drawValues(RNGSupplier rngSupplier, LSVMBidder lsvmBidder) {
        UniformDistributionRNG rng = rngSupplier.getUniformDistributionRNG();
        Map<Long, BigDecimal> values = new HashMap<>();
        for (LSVMLicense license : lsvmBidder.getProximity()) {
            values.put(license.getId(), rng.nextBigDecimal(this.valueInterval));
        }
        return values;
    }

    public DoubleInterval getValueInterval() {
        return valueInterval;
    }

    int getProximitySize() {
        return proximitySize;
    }

    int getLsvmA() {
        return a;
    }

    int getLsvmB() {
        return b;
    }

    private static abstract class Builder extends BidderSetup.Builder {

        private DoubleInterval valueInterval;
        private int proximitySize;
        private int a;
        private int b;

        private Builder(String setupName, int numberOfBidders, DoubleInterval valueInterval, int proximitySize, int a, int b) {
            super(setupName, numberOfBidders);
            this.valueInterval = valueInterval;
            this.proximitySize = proximitySize;
            this.a = a;
            this.b = b;
        }

        public void setValueInterval(DoubleInterval newInterval) {
            this.valueInterval = newInterval;
        }

        public void setLsvmA(int a) {
            this.a = a;
        }

        public void setLsvmB(int b) {
            this.b = b;
        }

        @Override
        public LSVMBidderSetup build() {
            return new LSVMBidderSetup(this);
        }

    }

    public static class NationalBidderBuilder extends Builder {

        /**
         * Create a BidderSetup Builder
         */
        public NationalBidderBuilder() {
            super("National Bidder Setup", 1, new DoubleInterval(3, 9), -1, 320, 10);
        }


    }

    public static class RegionalBidderBuilder extends Builder {

        /**
         * Create a BidderSetup Builder
         */
        public RegionalBidderBuilder() {
            super("Regional Bidder Setup", 5, new DoubleInterval(3, 20), 2, 160, 4);
        }

        public void setProximitySize(int proximitySize) {
            PreconditionUtils.checkNotNegative(proximitySize);
            super.proximitySize = proximitySize;
        }

    }
}
