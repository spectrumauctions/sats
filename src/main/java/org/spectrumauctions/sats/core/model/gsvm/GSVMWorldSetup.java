package org.spectrumauctions.sats.core.model.gsvm;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;
import org.spectrumauctions.sats.core.util.random.IntegerInterval;
import org.spectrumauctions.sats.core.util.random.UniformDistributionRNG;

/**
 * @author Fabio Isler
 */
public final class GSVMWorldSetup {

    private final IntegerInterval sizeInterval;
    @Getter
    private final boolean isLegacyGSVM;


    private GSVMWorldSetup(GSVMWorldSetupBuilder builder) {
        super();
        this.sizeInterval = builder.sizeInterval;
        this.isLegacyGSVM = builder.isLegacyGSVM;
    }

    Integer drawSize(UniformDistributionRNG rng) {
        return rng.nextInt(sizeInterval);
    }

    public static class GSVMWorldSetupBuilder {

        private static final int DEFAULT_SIZE = 6;

        private IntegerInterval sizeInterval;

        @Setter @Getter
        private boolean isLegacyGSVM = false;


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
