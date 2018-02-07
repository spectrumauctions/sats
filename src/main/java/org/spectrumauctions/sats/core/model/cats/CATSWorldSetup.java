package org.spectrumauctions.sats.core.model.cats;

import com.google.common.base.Preconditions;
import org.spectrumauctions.sats.core.model.cats.graphalgorithms.Mesh2D;
import org.spectrumauctions.sats.core.util.random.DoubleInterval;
import org.spectrumauctions.sats.core.util.random.IntegerInterval;
import org.spectrumauctions.sats.core.util.random.RNGSupplier;
import org.spectrumauctions.sats.core.util.random.UniformDistributionRNG;

/**
 * @author Fabio Isler
 */
public class CATSWorldSetup {

    private final IntegerInterval numberOfRowsInterval;
    private final IntegerInterval numberOfColumnInterval;
    private final IntegerInterval numberOfGoodsInterval;
    private final DoubleInterval commonValueInterval;

    private final double threeProb;         //Probability of removing an edge adjacent to a particular vertex

    private final double additionalNeigh;   //Probability of adding an additional neighbor
    private final double additivity;
    private final boolean useQuadraticPricingOption;
    private final double deviation;
    private final double additionalLocation;
    private double budgetFactor;
    private double resaleFactor;
    private double jumpProbability;
    private int maxSubstitutableBids;

    private CATSWorldSetup(Builder builder) {
        super();
        this.numberOfRowsInterval = builder.numberOfRowsInterval;
        this.numberOfColumnInterval = builder.numberOfColumnsInterval;
        this.numberOfGoodsInterval = builder.numberOfGoodsInterval;
        this.threeProb = builder.threeProb;
        this.additionalNeigh = builder.additionalNeigh;
        this.commonValueInterval = builder.commonValueInterval;
        this.additivity = builder.additivity;
        this.useQuadraticPricingOption = builder.useQuadraticPricingOption;
        this.deviation = builder.deviation;
        this.additionalLocation = builder.additionalLocation;
        this.budgetFactor = builder.budgetFactor;
        this.resaleFactor = builder.resaleFactor;
        this.jumpProbability = builder.jumpProbability;
        this.maxSubstitutableBids = builder.maxSubstitutableBids;
    }

    public double getAdditivity() {
        return additivity;
    }

    public double getDeviation() {
        return deviation;
    }

    public double getAdditionalLocation() {
        return additionalLocation;
    }

    public double getBudgetFactor() {
        return budgetFactor;
    }

    public double getResaleFactor() {
        return resaleFactor;
    }

    public double getJumpProbability() {
        return jumpProbability;
    }

    /**
     * If the numberOfGoodsInterval is set, the number of rows and columns are ignored and the determination of those
     * falls back to the CATS definition (int) Math.floor(Math.sqrt(numberOfGoods)).
     *
     * @return whether the numberOfGoodsInterval has been defined
     */
    public boolean hasDefinedNumberOfGoodsInterval() {
        return this.numberOfGoodsInterval != null;
    }

    Integer drawNumberOfRows(RNGSupplier rngSupplier) {
        UniformDistributionRNG rng = rngSupplier.getUniformDistributionRNG();
        return rng.nextInt(numberOfRowsInterval);
    }

    Integer drawNumberOfColumns(RNGSupplier rngSupplier) {
        UniformDistributionRNG rng = rngSupplier.getUniformDistributionRNG();
        return rng.nextInt(numberOfColumnInterval);
    }

    Integer drawNumberOfGoods(RNGSupplier rngSupplier) {
        UniformDistributionRNG rng = rngSupplier.getUniformDistributionRNG();
        return rng.nextInt(numberOfGoodsInterval);
    }

    Double drawCommonValue(RNGSupplier rngSupplier) {
        UniformDistributionRNG rng = rngSupplier.getUniformDistributionRNG();
        return rng.nextDouble(commonValueInterval);
    }

    Mesh2D buildProximityGraph(int _numberOfRows, int _numberOfColumns, RNGSupplier rngSupplier) {
        Mesh2D _graph = new Mesh2D(_numberOfRows, _numberOfColumns);

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

    public int getMaxSubstitutableBids() {
        return maxSubstitutableBids;
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
        private static final double DEFAULT_COMMON_VALUE_MIN = 1;   // Specified as rand*(max-1)+1
        private static final double DEFAULT_COMMON_VALUE_MAX = DEFAULT_MAX_GOOD_VALUE;
        private static final int DEFAULT_MAX_SUBSTITUTABLE_BIDS = 5;
        private static final double DEFAULT_ADDITIONAL_LOCATION = 0.9;
        private static final double DEFAULT_JUMP_PROB = 0.05;
        private static final double DEFAULT_DEVIATION = 0.5;
        private static final double DEFAULT_BUDGET_FACTOR = 1.5;
        private static final double DEFAULT_RESALE_FACTOR = 0.5;

        private DoubleInterval commonValueInterval;
        private IntegerInterval numberOfRowsInterval;
        private IntegerInterval numberOfColumnsInterval;
        private IntegerInterval numberOfGoodsInterval;
        private double threeProb;
        private double additionalNeigh;
        private double additivity;
        private boolean useQuadraticPricingOption;
        private double deviation;
        private double additionalLocation;
        private double budgetFactor;
        private double resaleFactor;
        private double jumpProbability;
        private int maxSubstitutableBids;

        public Builder() {
            super();
            this.numberOfRowsInterval = new IntegerInterval(DEFAULT_NUMBER_OF_ROWS);
            this.numberOfColumnsInterval = new IntegerInterval(DEFAULT_NUMBER_OF_COLUMNS);
            this.commonValueInterval = new DoubleInterval(DEFAULT_COMMON_VALUE_MIN, DEFAULT_COMMON_VALUE_MAX);
            this.threeProb = DEFAULT_THREE_PROB;
            this.additionalNeigh = DEFAULT_ADDITIONAL_NEIGHBOR;
            this.additivity = DEFAULT_ADDITIVITY;
            this.useQuadraticPricingOption = DEFAULT_QUADRATIC_PRICING_FLAG;
            this.deviation = DEFAULT_DEVIATION;
            this.additionalLocation = DEFAULT_ADDITIONAL_LOCATION;
            this.budgetFactor = DEFAULT_BUDGET_FACTOR;
            this.resaleFactor = DEFAULT_RESALE_FACTOR;
            this.jumpProbability = DEFAULT_JUMP_PROB;
            this.maxSubstitutableBids = DEFAULT_MAX_SUBSTITUTABLE_BIDS;
        }

        public void setNumberOfRowsInterval(IntegerInterval numberOfRowsInterval) {
            Preconditions.checkArgument(numberOfRowsInterval.getMinValue() >= 2,
                    "Please choose a number of columns interval that starts at least at 2," +
                            "so that a 2x2 grid can be created");
            this.numberOfRowsInterval = numberOfRowsInterval;
        }

        public void setNumberOfColumnsInterval(IntegerInterval numberOfColumnsInterval) {
            Preconditions.checkArgument(numberOfColumnsInterval.getMinValue() >= 2,
                    "Please choose a number of columns interval that starts at least at 2," +
                            "so that a 2x2 grid can be created");
            this.numberOfColumnsInterval = numberOfColumnsInterval;
        }

        public void setNumberOfGoodsInterval(IntegerInterval numberOfGoodsInterval) {
            Preconditions.checkArgument(numberOfGoodsInterval.getMinValue() >= 4,
                    "Please choose a number of goods interval that starts at least at 4, so that " +
                            "a 2x2 grid can be created.");
            this.numberOfGoodsInterval = numberOfGoodsInterval;
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

        public void setDeviation(double deviation) {
            this.deviation = deviation;
        }

        public void setAdditionalLocation(double additionalLocation) {
            this.additionalLocation = additionalLocation;
        }

        public void setBudgetFactor(double budgetFactor) {
            this.budgetFactor = budgetFactor;
        }

        public void setJumpProbability(double jumpProbability) {
            this.jumpProbability = jumpProbability;
        }

        public void setMaxSubstitutableBids(int maxSubstitutableBids) {
            this.maxSubstitutableBids = maxSubstitutableBids;
        }

        public CATSWorldSetup build() {
            return new CATSWorldSetup(this);
        }

        public int getDefaultNumberOfGoods() {
            return DEFAULT_NUMBER_OF_COLUMNS * DEFAULT_NUMBER_OF_ROWS;
        }
    }
}
