/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model.mrvm;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.jgrapht.Graph;
import org.jgrapht.generate.GnmRandomGraphGenerator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultUndirectedGraph;
import org.jgrapht.util.SupplierUtil;
import org.spectrumauctions.sats.core.util.math.ContinuousPiecewiseLinearFunction;
import org.spectrumauctions.sats.core.util.math.Function;
import org.spectrumauctions.sats.core.util.random.DoubleInterval;
import org.spectrumauctions.sats.core.util.random.GaussianDistributionRNG;
import org.spectrumauctions.sats.core.util.random.IntegerInterval;
import org.spectrumauctions.sats.core.util.random.UniformDistributionRNG;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * @author Michael Weiss
 *
 */
public final class MRVMWorldSetup {

    private final ImmutableMap<String, BandSetup> bandSetups;
    private final IntegerInterval numberOfRegionsInterval;
    private final IntegerInterval averageAdjacenciesPerRegionInterval;
    private final double populationPerRegionMean;
    private final double populationStandardDeviation;
    private final boolean usePredefinedGraph;
    private final Graph<RegionSetup, DefaultEdge> predefinedGraph;
    private int randomRegionCount = 0;


    private MRVMWorldSetup(MRVMWorldSetupBuilder builder) {
        super();
        this.bandSetups = ImmutableMap.copyOf(builder.bandSetups);
        this.numberOfRegionsInterval = builder.numberOfRegionsInterval;
        this.averageAdjacenciesPerRegionInterval = builder.averageAdjacenciesPerRegionInterval;
        this.populationPerRegionMean = builder.populationPerRegionMean;
        this.populationStandardDeviation = builder.populationStandardDeviation;
        this.usePredefinedGraph = builder.usePredefinedGraph;
        this.predefinedGraph = builder.predefinedGraph;
    }

    public Set<BandSetup> getBandSetups() {
        return ImmutableSet.copyOf(bandSetups.values());
    }

    public Graph<RegionSetup, DefaultEdge> drawGraphStructure(UniformDistributionRNG rng) {
        if (usePredefinedGraph) {
            return predefinedGraph;
        } else {
            return nonPlanarRandomGraphStructure(
                    populationPerRegionMean,
                    populationStandardDeviation,
                    rng,
                    numberOfRegionsInterval,
                    averageAdjacenciesPerRegionInterval);
        }
    }


    /**
     * Generates the parameters for a newly created region which are independent of the adjacency map
     * @author Michael Weiss
     */
    public static final class RegionSetup {
        private final double populationMean;
        private final double populationStandardDeviation;
        private final String notice;

        public RegionSetup(double populationMean, double populationStandardDeviation, String notice) {
            super();
            this.populationMean = populationMean;
            this.populationStandardDeviation = populationStandardDeviation;
            this.notice = notice;
        }

        public String getNotice() {
            return notice;
        }

        public int drawPopulation(GaussianDistributionRNG rng) {
            int population = (int) rng.nextGaussian(populationMean, populationStandardDeviation);
            if (population > 0) {
                return population;
            } else {
                return drawPopulation(rng);
            }
        }

    }


    public static final class BandSetup {

        private final String name;
        private final IntegerInterval numberOfLotsInterval;
        private final DoubleInterval baseCapacity;
        private final ImmutableMap<Integer, BigDecimal> synergies;

        public BandSetup(String name, IntegerInterval numberOfLotsInterval, DoubleInterval baseCapacity, Function synergyfunction) {
            super();
            this.name = name;
            this.numberOfLotsInterval = numberOfLotsInterval;
            this.baseCapacity = baseCapacity;
            this.synergies = calculateSynergies(numberOfLotsInterval.getMaxValue(), synergyfunction);
        }

        public BandSetup(String name, IntegerInterval numberOfLotsInterval, DoubleInterval baseCapacity,
                         ImmutableMap<Integer, BigDecimal> synergies) {
            this.name = name;
            this.numberOfLotsInterval = numberOfLotsInterval;
            this.baseCapacity = baseCapacity;
            this.synergies = synergies;
        }

        private static ImmutableMap<Integer, BigDecimal> calculateSynergies(int maxNumberOfLots, Function synergyfunction) {
            ImmutableMap.Builder<Integer, BigDecimal> builder = ImmutableMap.builder();
            for (int i = 1; i <= maxNumberOfLots; i++) {
                builder.put(i, synergyfunction.getY(new BigDecimal(i)));
            }
            return builder.build();
        }

        public String getName() {
            return name;
        }


        public int drawNumberOfLots(UniformDistributionRNG rng) {
            return rng.nextInt(numberOfLotsInterval);
        }


        public BigDecimal drawBaseCapacity(UniformDistributionRNG rng) {
            return rng.nextBigDecimal(baseCapacity);
        }


        public Map<Integer, BigDecimal> getSynergies() {
            return synergies;
        }


    }

    public static class MRVMWorldSetupBuilder {


        public static final String LOW_PAIRED_NAME = "LOW_PAIRED";
        public static final String HIGH_PAIRED_NAME = "HIGH_PAIRED";
        public static final String UNPAIRED_NAME = "UNPAIRED";

        public Graph<RegionSetup, DefaultEdge> predefinedGraph;
        public boolean usePredefinedGraph;
        private double populationStandardDeviation;
        private double populationPerRegionMean;
        private IntegerInterval averageAdjacenciesPerRegionInterval;
        private IntegerInterval numberOfRegionsInterval;
        private final Map<String, BandSetup> bandSetups;


        public MRVMWorldSetupBuilder() {
            super();
            this.bandSetups = new HashMap<>();
            this.usePredefinedGraph = true;
            this.predefinedGraph = CanadianMap.getInstance().createCanadianMapGraph();
            addDefaultBands();
        }

        private void addDefaultBands() {
            Map<BigDecimal, BigDecimal> cornerPoints = new HashMap<>();
            cornerPoints.put(BigDecimal.ZERO, BigDecimal.ONE);
            //       cornerPoints.put(BigDecimal.valueOf(2), BigDecimal.valueOf(1.4));
            cornerPoints.put(BigDecimal.valueOf(2), BigDecimal.valueOf(1.2));
            cornerPoints.put(BigDecimal.valueOf(4.), BigDecimal.valueOf(1));
            ContinuousPiecewiseLinearFunction synergyFunction = new ContinuousPiecewiseLinearFunction(cornerPoints);
            this.putBandSetup(new BandSetup(LOW_PAIRED_NAME, new IntegerInterval(2),
                    new DoubleInterval(3d, 4d), synergyFunction));
            this.putBandSetup(new BandSetup(HIGH_PAIRED_NAME, new IntegerInterval(3),
                    new DoubleInterval(1.5d, 2.5d), synergyFunction));
            this.putBandSetup(new BandSetup(UNPAIRED_NAME, new IntegerInterval(2),
                    new DoubleInterval(0.5d, 1d), synergyFunction));
        }


        public void createGraphRandomly(
                IntegerInterval numberOfRegions,
                IntegerInterval averageOfAdjacenciesPerRegion,
                double populationPerRegionMean,
                double populationPerRegionStandardDeviation) {
            usePredefinedGraph = false;
            setNumberOfRegionsInterval(numberOfRegions);
            setAverageAdjacenciesPerRegionInterval(averageOfAdjacenciesPerRegion);
            setPopulationPerRegionMean(populationPerRegionMean);
            setPopulationStandardDeviation(populationPerRegionStandardDeviation);
        }

        private void setNumberOfRegionsInterval(IntegerInterval numberOfRegions) {
            this.numberOfRegionsInterval = numberOfRegions;
        }

        public void createPredefinedGraph(Graph<RegionSetup, DefaultEdge> predefinedGraph) {
            this.predefinedGraph = predefinedGraph;
            usePredefinedGraph = true;
        }

        private void setPopulationStandardDeviation(double populationStandardDeviation) {
            this.populationStandardDeviation = populationStandardDeviation;
        }

        private void setPopulationPerRegionMean(double populationPerRegionMean) {
            this.populationPerRegionMean = populationPerRegionMean;
        }

        private void setAverageAdjacenciesPerRegionInterval(IntegerInterval averageAdjacenciesPerRegionInterval) {
            this.averageAdjacenciesPerRegionInterval = averageAdjacenciesPerRegionInterval;
        }


        public void changeNumberOfLots(String bandName, IntegerInterval numberOfLots) {
            BandSetup setup = bandSetups.get(bandName);
            if (setup == null) {
                throw new IllegalArgumentException("Band Name Unknown");
            }
            BandSetup newSetup = new BandSetup(setup.name, setup.numberOfLotsInterval, setup.baseCapacity, setup.synergies);
            bandSetups.put(bandName, newSetup);
        }


        /**
         * Adds an additional bandStructure.<br>
         * If a bandStructure with this name already exists, it is replaced.
         * @param bandStructure a band setup
         */
        public void putBandSetup(BandSetup bandStructure) {
            bandSetups.put(bandStructure.getName(), bandStructure);
        }

        /**
         * Remove a BandSetup
         * @param name the name of the band setup to be removed
         */
        public BandSetup removeBandSetup(String name) {
            return bandSetups.remove(name);
        }


        /**
         * @return an unmodifiable map over all currently stored BandSetups
         */
        public Map<String, BandSetup> bandSetups() {
            return Collections.unmodifiableMap(bandSetups);
        }

        public MRVMWorldSetup build() {
            return new MRVMWorldSetup(this);

        }


    }


    /**
     * Creates a naive, random, not necessarily planar graph
     */
    public Graph<RegionSetup, DefaultEdge> nonPlanarRandomGraphStructure(
            final double populationPerRegionMean,
            final double populationStandardDeviation,
            UniformDistributionRNG rng,
            IntegerInterval numberOfRegionsInterval,
            IntegerInterval averageAdjacenciesPerRegionInterval
    ) {
        int numberOfRegions = rng.nextInt(numberOfRegionsInterval);
        int numberOfAdjacencies = rng.nextInt(averageAdjacenciesPerRegionInterval) * numberOfRegions;
        GnmRandomGraphGenerator<RegionSetup, DefaultEdge> randomGraphGenerator = new GnmRandomGraphGenerator<>(
                numberOfRegions, numberOfAdjacencies, rng.nextLong());
        Supplier<RegionSetup> vertexFactory = () ->
            new RegionSetup(populationPerRegionMean, populationStandardDeviation, randomRegionCount++ + ": randomly created");
        DefaultUndirectedGraph<RegionSetup, DefaultEdge> targetGraph = new DefaultUndirectedGraph<>(vertexFactory, SupplierUtil.createDefaultEdgeSupplier(), false);
        randomGraphGenerator.generateGraph(targetGraph);
        return targetGraph;
    }


}
