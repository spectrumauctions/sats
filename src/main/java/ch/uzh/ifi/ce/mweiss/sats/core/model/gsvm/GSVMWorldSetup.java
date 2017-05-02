package ch.uzh.ifi.ce.mweiss.sats.core.model.gsvm;

import ch.uzh.ifi.ce.mweiss.sats.core.util.random.IntegerInterval;
import ch.uzh.ifi.ce.mweiss.sats.core.util.random.UniformDistributionRNG;
import com.google.common.base.Preconditions;

/**
 * @author Fabio Isler
 */
public final class GSVMWorldSetup {

    private final IntegerInterval sizeInterval;


    private GSVMWorldSetup(GSVMWorldSetupBuilder builder) {
        super();
        this.sizeInterval = builder.sizeInterval;
    }

    Integer drawSize(UniformDistributionRNG rng) {
        return rng.nextInt(sizeInterval);
    }

    public static class GSVMWorldSetupBuilder {

        private static final int DEFAULT_SIZE = 6;

        private IntegerInterval sizeInterval;


        public GSVMWorldSetupBuilder() {
            super();
            this.sizeInterval = new IntegerInterval(DEFAULT_SIZE);
        }

        public void setSizeInterval(IntegerInterval sizeInterval) {
            Preconditions.checkArgument(sizeInterval.getMinValue() > 0);
            this.sizeInterval = sizeInterval;
        }

        public GSVMWorldSetup build() {
            return new GSVMWorldSetup(this);

        }

    }
}
