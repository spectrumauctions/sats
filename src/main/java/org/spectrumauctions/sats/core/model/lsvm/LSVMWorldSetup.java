package org.spectrumauctions.sats.core.model.lsvm;

import org.spectrumauctions.sats.core.util.PreconditionUtils;
import org.spectrumauctions.sats.core.util.random.IntegerInterval;
import org.spectrumauctions.sats.core.util.random.UniformDistributionRNG;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Fabio Isler
 */
public class LSVMWorldSetup {

    private final IntegerInterval numberOfRowsInterval;
    private final IntegerInterval numberOfColumnsInterval;
    @Getter
    private final boolean isLegacyLSVM;

    private LSVMWorldSetup(LSVMWorldSetupBuilder builder) {
        super();
        this.numberOfRowsInterval = builder.numberOfRowsInterval;
        this.numberOfColumnsInterval = builder.numberOfColumnsInterval;
        this.isLegacyLSVM = builder.isLegacyLSVM();
    }

    Integer drawRowNumber(UniformDistributionRNG rng) {
        return rng.nextInt(numberOfRowsInterval);
    }

    Integer drawColumnNumber(UniformDistributionRNG rng) {
        return rng.nextInt(numberOfColumnsInterval);
    }

    public static class LSVMWorldSetupBuilder {

        private static final int DEFAULT_NUMBER_OF_ROWS = 3;
        private static final int DEFAULT_NUMBER_OF_COLUMNS = 6;

        private IntegerInterval numberOfRowsInterval;
        private IntegerInterval numberOfColumnsInterval;
        
        @Setter @Getter
        private boolean isLegacyLSVM = false;

        public LSVMWorldSetupBuilder() {
            super();
            this.numberOfRowsInterval = new IntegerInterval(DEFAULT_NUMBER_OF_ROWS);
            this.numberOfColumnsInterval = new IntegerInterval(DEFAULT_NUMBER_OF_COLUMNS);
        }

        public void createGridSizeRandomly(IntegerInterval numberOfRows, IntegerInterval numberOfColumns) {
            setNumberOfRowsInterval(numberOfRows);
            setNumberOfColumnsInterval(numberOfColumns);
        }

        public void setNumberOfRowsInterval(IntegerInterval numberOfRows) {
            PreconditionUtils.checkNotNegative(numberOfRows.getMinValue());
            this.numberOfRowsInterval = numberOfRows;
        }

        public void setNumberOfColumnsInterval(IntegerInterval numberOfColumns) {
            PreconditionUtils.checkNotNegative(numberOfColumns.getMinValue());
            this.numberOfColumnsInterval = numberOfColumns;
        }


        public LSVMWorldSetup build() {
            return new LSVMWorldSetup(this);

        }
    }
}
