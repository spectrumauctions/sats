package ch.uzh.ifi.ce.mweiss.sats.core.model.cats;

import ch.uzh.ifi.ce.mweiss.sats.core.model.cats.graphalgorithms.Graph;
import ch.uzh.ifi.ce.mweiss.sats.core.util.random.DoubleInterval;
import ch.uzh.ifi.ce.mweiss.sats.core.util.random.IntegerInterval;
import ch.uzh.ifi.ce.mweiss.sats.core.util.random.UniformDistributionRNG;
import ch.uzh.ifi.ce.mweiss.sats.core.model.cats.graphalgorithms.Mesh2D;
import ch.uzh.ifi.ce.mweiss.sats.core.util.random.RNGSupplier;
import com.google.common.base.Preconditions;

/**
 * @author Fabio Isler
 */
public class CATSWorldSetup {

    private final IntegerInterval numberOfRowsInterval;
    private final IntegerInterval numberOfColumnInterval;
    private final DoubleInterval commonValueInterval;

    private final double threeProb;         //Probability of removing an edge adjacent to a particular vertex

    private final double additionalNeigh;   //Probability of adding an additional neighbor
    private final double additivity;
    private final boolean useQuadraticPricingOption;

    private CATSWorldSetup(Builder builder) {
        super();
        this.numberOfRowsInterval = builder.numberOfRowsInterval;
        this.numberOfColumnInterval = builder.numberOfColumnsInterval;
        this.threeProb = builder.threeProb;
        this.additionalNeigh = builder.additionalNeigh;
        this.commonValueInterval = builder.commonValueInterval;
        this.additivity = builder.additivity;
        this.useQuadraticPricingOption = builder.useQuadraticPricingOption;
    }


    public double getAdditivity() {
        return additivity;
    }

    Integer drawNumberOfRows(RNGSupplier rngSupplier) {
        UniformDistributionRNG rng = rngSupplier.getUniformDistributionRNG();
        return rng.nextInt(numberOfRowsInterval);
    }

    Integer drawNumberOfColumns(RNGSupplier rngSupplier) {
        UniformDistributionRNG rng = rngSupplier.getUniformDistributionRNG();
        return rng.nextInt(numberOfColumnInterval);
    }

    Double drawCommonValue(RNGSupplier rngSupplier) {
        UniformDistributionRNG rng = rngSupplier.getUniformDistributionRNG();
        return rng.nextDouble(commonValueInterval);
    }

    Graph buildProximityGraph(int _numberOfRows, int _numberOfColumns, RNGSupplier rngSupplier) {
        Graph _graph = new Mesh2D(_numberOfRows, _numberOfColumns);

        UniformDistributionRNG generator = rngSupplier.getUniformDistributionRNG();

        for (int n = 0; n < _numberOfRows * _numberOfColumns; ++n) {
            if (!(((n + 1) % _numberOfColumns == 0) || ((n + 1) % _numberOfColumns == 1) || ((n + 1) / _numberOfColumns == 0) || (n / _numberOfColumns == _numberOfRows - 1))) {
                if (generator.nextDouble() <= this.threeProb) {
                    int numberOfAdjacentNeighs = _graph.getAdjacencyLists().get(n).size();
                    int edgeToRemove = (int) (generator.nextDouble() * numberOfAdjacentNeighs);
                    _graph.removeEdge(n + 1, edgeToRemove);
                }

                int numberOfDNeigh = 0;
                while ((generator.nextDouble() <= this.additionalNeigh) && (numberOfDNeigh < 4)) {
                    int newDNeigh = 0;

                    if (numberOfDNeigh == 0)
                        newDNeigh = (n + 1) - _numberOfColumns - 1;
                    else if (numberOfDNeigh == 1)
                        newDNeigh = (n + 1) - _numberOfColumns + 1;
                    else if (numberOfDNeigh == 2)
                        newDNeigh = (n + 1) + _numberOfColumns + 1;
                    else if (numberOfDNeigh == 3)
                        newDNeigh = (n + 1) + _numberOfColumns - 1;

                    _graph.addEdge((n + 1), newDNeigh);
                    numberOfDNeigh += 1;
                }
            }
        }
        return _graph;
    }

    public boolean useQuadraticPricingOption() {
        return useQuadraticPricingOption;
    }

    public static class Builder {

        // CATS default parameters
        private static final int DEFAULT_NUMBER_OF_ROWS = 16;       // Specified as sqrt(256)
        private static final int DEFAULT_NUMBER_OF_COLUMNS = 16;    // Specified as sqrt(256)
        private static final double DEFAULT_THREE_PROB = 1.0;
        private static final double DEFAULT_ADDITIONAL_NEIGHBOR = 0.2;
        private static final double DEFAULT_ADDITIVITY = 0.2;
        private static final boolean DEFAULT_QUADRATIC_PRICING_FLAG = false;
        private static final double DEFAULT_MAX_GOOD_VALUE = 100;
        private static final double DEFAULT_COMMON_VALUE_MIN = 1;   // Specified as rand*(max-1)-1
        private static final double DEFAULT_COMMON_VALUE_MAX = DEFAULT_MAX_GOOD_VALUE;
        private static final double DEFAULT_MAX_SUBSTITUTABLE_BIDS = 5;
        private static final double DEFAULT_ADDITIONAL_LOCATION = 0.9;
        private static final double DEFAULT_JUMP_PROB = 0.05;
        private static final double DEFAULT_DEVIATION = 0.5;
        private static final double DEFAULT_BUDGET_FACTOR = 1.5;
        private static final double DEFAULT_RESALE_FACTOR = 0.5;

        private DoubleInterval commonValueInterval;
        private IntegerInterval numberOfRowsInterval;
        private IntegerInterval numberOfColumnsInterval;
        private double threeProb;
        private double additionalNeigh;
        private double additivity;
        private boolean useQuadraticPricingOption;

        public Builder() {
            super();
            this.numberOfRowsInterval = new IntegerInterval(DEFAULT_NUMBER_OF_ROWS);
            this.numberOfColumnsInterval = new IntegerInterval(DEFAULT_NUMBER_OF_COLUMNS);
            this.commonValueInterval = new DoubleInterval(DEFAULT_COMMON_VALUE_MIN, DEFAULT_COMMON_VALUE_MAX);
            this.threeProb = DEFAULT_THREE_PROB;
            this.additionalNeigh = DEFAULT_ADDITIONAL_NEIGHBOR;
            this.additivity = DEFAULT_ADDITIVITY;
            this.useQuadraticPricingOption = DEFAULT_QUADRATIC_PRICING_FLAG;
        }

        public void setNumberOfRowsInterval(IntegerInterval numberOfRowsInterval) {
            Preconditions.checkArgument(numberOfRowsInterval.getMinValue() > 0);
            this.numberOfRowsInterval = numberOfRowsInterval;
        }

        public void setNumberOfColumnsInterval(IntegerInterval numberOfColumnsInterval) {
            Preconditions.checkArgument(numberOfColumnsInterval.getMinValue() > 0);
            this.numberOfColumnsInterval = numberOfColumnsInterval;
        }

        public void setCommonValueInterval(DoubleInterval commonValueInterval) {
            Preconditions.checkArgument(commonValueInterval.getMinValue() >= 0);
            this.commonValueInterval = commonValueInterval;
        }

        public void setThreeProb(double threeProb) {
            this.threeProb = threeProb;
        }

        public void setAdditionalNeigh(double additionalNeigh) {
            this.additionalNeigh = additionalNeigh;
        }

        public void setAdditivity(double additivity) {
            this.additivity = additivity;
        }

        public void setUseQuadraticPricingOption(boolean bool) {
            this.useQuadraticPricingOption = bool;
        }

        public CATSWorldSetup build() {
            return new CATSWorldSetup(this);
        }

    }
}
