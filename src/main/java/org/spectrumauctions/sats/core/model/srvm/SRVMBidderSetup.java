package org.spectrumauctions.sats.core.model.srvm;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import org.spectrumauctions.sats.core.model.BidderSetup;
import org.spectrumauctions.sats.core.util.random.DoubleInterval;
import org.spectrumauctions.sats.core.util.random.IntegerInterval;
import org.spectrumauctions.sats.core.util.random.RNGSupplier;
import org.spectrumauctions.sats.core.util.random.UniformDistributionRNG;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Michael Weiss
 */
public class SRVMBidderSetup extends BidderSetup {

    private final Map<String, IntegerInterval> synergyThresholds;
    private final Map<String, BigDecimal> meanBaseValues;
    private final Map<String, DoubleInterval> intraBandSynergyFactors;
    private final DoubleInterval interBandSynergyFactor;
    private final Map<String, DoubleInterval> randomInfluence;
    private final DoubleInterval bidderStrength;

    private SRVMBidderSetup(SRVMBidderSetup.Builder builder) {
        super(builder);
        this.synergyThresholds = ImmutableMap.copyOf(builder.synergyThresholds);
        this.meanBaseValues = ImmutableMap.copyOf(builder.meanBaseValues);
        this.intraBandSynergyFactors = ImmutableMap.copyOf(builder.intraBandSynergyFactors);
        this.interBandSynergyFactor = builder.interBandSynergyFactor;
        this.randomInfluence = ImmutableMap.copyOf(builder.randomInfluence);
        this.bidderStrength = builder.bidderStrength;
    }

    /**
     * Determines the synergy thresholds for a band. The synergy threshold is a quantity which has a big influence on the decreasing synergies for higher quantities. <br>
     */
    public HashMap<SRVMBand, Integer> drawSynergyThresholds(SRVMWorld world, RNGSupplier rngSupplier) {
        UniformDistributionRNG rng = rngSupplier.getUniformDistributionRNG();
        HashMap<SRVMBand, Integer> result = new HashMap<>();
        for (SRVMBand band : world.getBands()) {
            IntegerInterval interval = synergyThresholds.get(band.getName());
            if (interval == null) {
                throw new IllegalArgumentException("No synergies defined for band " + band.getName());
            } else if (!interval.isStrictlyPositive()) {
                throw new IllegalArgumentException("Synergy theshold must be strictly positive");
            } else {
                result.put(band, rng.nextInt(interval));
            }
        }
        return result;
    }

    /**
     * Determines the Base Values for the band.<br>
     * The base values are computed by multiplying the meanBaseValue the bidderStrenght and a random influence parameter.
     */
    public HashMap<SRVMBand, BigDecimal> drawBaseValues(SRVMWorld world, BigDecimal bidderStrength, RNGSupplier rngSupplier) {
        UniformDistributionRNG rng = rngSupplier.getUniformDistributionRNG();
        HashMap<SRVMBand, BigDecimal> result = new HashMap<>();
        for (SRVMBand band : world.getBands()) {
            BigDecimal meanBaseValue = meanBaseValues.get(band.getName());
            DoubleInterval randomInfluenceInterval = this.randomInfluence.get(band.getName());
            if (meanBaseValue == null) {
                throw new IllegalArgumentException("No mean base value defined for band " + band.getName());
            } else if (randomInfluenceInterval == null || !randomInfluenceInterval.isStrictlyPositive()) {
                throw new IllegalArgumentException("Base Value must be defined and strictly positive");
            } else {
                BigDecimal randomInfluence = rng.nextBigDecimal(randomInfluenceInterval);
                BigDecimal baseValue = meanBaseValue.multiply(bidderStrength).multiply(randomInfluence);
                result.put(band, (baseValue));
            }
        }
        return result;
    }


    /**
     * @return Determine the synergies within a band
     */
    public HashMap<SRVMBand, BigDecimal> drawIntraBandSynergyFactors(SRVMWorld world, RNGSupplier rngSupplier) {
        UniformDistributionRNG rng = rngSupplier.getUniformDistributionRNG();
        HashMap<SRVMBand, BigDecimal> result = new HashMap<>();
        for (SRVMBand band : world.getBands()) {
            DoubleInterval interval = intraBandSynergyFactors.get(band.getName());
            if (interval == null) {
                throw new IllegalArgumentException("No synergies defined for band " + band.getName());
            } else if (interval.getMinValue() < 1) {
                throw new IllegalArgumentException("Synergy factor must be at least one");
            } else {
                result.put(band, rng.nextBigDecimal(interval));
            }
        }
        return result;
    }

    /**
     * @return the synergy factor is a factor that applies to the total value if a bidder has licenses of more than one band.
     */
    public BigDecimal drawInterBandSynergyFactor(SRVMWorld world, RNGSupplier rngSupplier) {
        UniformDistributionRNG rng = rngSupplier.getUniformDistributionRNG();
        if (interBandSynergyFactor.getMinValue() < 1) {
            throw new IllegalArgumentException("Synergy factor must be at least one");
        } else {
            return rng.nextBigDecimal(interBandSynergyFactor);
        }
    }

    /**
     * @return a random bidderStrength factor. See {@link SRVMBidder#getBidderStrength()} for more information.
     */
    public BigDecimal drawBidderStrength(SRVMWorld world, RNGSupplier rngSupplier) {
        return rngSupplier.getUniformDistributionRNG().nextBigDecimal(bidderStrength);
    }


    private static abstract class Builder extends BidderSetup.Builder {

        private final Map<String, IntegerInterval> synergyThresholds;
        private final Map<String, BigDecimal> meanBaseValues;
        private final Map<String, DoubleInterval> intraBandSynergyFactors;
        private DoubleInterval interBandSynergyFactor;
        private final Map<String, DoubleInterval> randomInfluence;
        private DoubleInterval bidderStrength;

        Builder(String setupName, int numberOfBidders) {
            super(setupName, numberOfBidders);
            synergyThresholds = new HashMap<>();
            meanBaseValues = new HashMap<>();
            intraBandSynergyFactors = new HashMap<>();
            randomInfluence = new HashMap<>();
            putRandomInfluence(SRVMWorldSetup.Builder.BAND_NAME_A, new DoubleInterval(0.75, 1.25));
            putRandomInfluence(SRVMWorldSetup.Builder.BAND_NAME_B, new DoubleInterval(0.75, 1.25));
            putRandomInfluence(SRVMWorldSetup.Builder.BAND_NAME_C, new DoubleInterval(0.75, 1.25));
            putSynergyThreshold(SRVMWorldSetup.Builder.BAND_NAME_A, new IntegerInterval(4));
            putSynergyThreshold(SRVMWorldSetup.Builder.BAND_NAME_B, new IntegerInterval(2));
            putSynergyThreshold(SRVMWorldSetup.Builder.BAND_NAME_C, new IntegerInterval(2));
            bidderStrength = new DoubleInterval(0.75, 1.25);
            setInterBandSynergyFactor(new DoubleInterval(1.0, 1.2));
        }


        public void putRandomInfluence(String bandName, DoubleInterval randomInfluenceInterval) {
            Preconditions.checkArgument(randomInfluenceInterval.isStrictlyPositive());
            randomInfluence.put(bandName, randomInfluenceInterval);
        }

        public DoubleInterval removeRandomInflucence(String bandName) {
            return randomInfluence.remove(bandName);
        }

        public Map<String, DoubleInterval> getRandomInfluence() {
            return Collections.unmodifiableMap(randomInfluence);
        }

        @Override
        public SRVMBidderSetup build() {
            return new SRVMBidderSetup(this);
        }

        public DoubleInterval getBidderStrength() {
            return bidderStrength;
        }

        public void setBidderStrength(DoubleInterval bidderStrength) {
            this.bidderStrength = bidderStrength;
        }

        public Map<String, IntegerInterval> getSynergyThresholds() {
            return Collections.unmodifiableMap(synergyThresholds);
        }

        public Map<String, BigDecimal> getMeanBaseValues() {
            return Collections.unmodifiableMap(meanBaseValues);
        }

        public Map<String, DoubleInterval> getIntraBandSynergyFactors() {
            return Collections.unmodifiableMap(intraBandSynergyFactors);
        }

        public DoubleInterval getInterBandSynergyFactor() {
            return interBandSynergyFactor;
        }

        public void setInterBandSynergyFactor(DoubleInterval interBandSynergyFactor) {
            this.interBandSynergyFactor = interBandSynergyFactor;
        }

        public void putSynergyThreshold(String bandName, IntegerInterval thresholdInterval) {
            this.synergyThresholds.put(bandName, thresholdInterval);
        }

        public IntegerInterval removeSynergyThreshold(String bandName) {
            return this.synergyThresholds.remove(bandName);
        }

        public void putMeanBaseValue(String bandName, BigDecimal meanBaseValue) {
            meanBaseValues.put(bandName, meanBaseValue);
        }

        public BigDecimal removeBaseValue(String bandName) {
            return meanBaseValues.remove(bandName);
        }

        public void putIntraBandSynergyFactor(String bandName, DoubleInterval synergyFactor) {
            this.intraBandSynergyFactors.put(bandName, synergyFactor);
        }

        public DoubleInterval removeIntraBandSynergyFactor(String bandName) {
            return intraBandSynergyFactors.remove(bandName);
        }
    }

    public static final class SmallBidderBuilder extends Builder {

        /**
         * Create a BidderSetup Builder with Primary Bidder default values (see Kreoemer et. al (2014)).
         */
        public SmallBidderBuilder() {
            super("Small Bidder Setup", 2);
            putMeanBaseValue(SRVMWorldSetup.Builder.BAND_NAME_A, new BigDecimal(0));
            putMeanBaseValue(SRVMWorldSetup.Builder.BAND_NAME_B, new BigDecimal(0));
            putMeanBaseValue(SRVMWorldSetup.Builder.BAND_NAME_C, new BigDecimal(8));
            putIntraBandSynergyFactor(SRVMWorldSetup.Builder.BAND_NAME_A, new DoubleInterval(1.75, 2.25));
            putIntraBandSynergyFactor(SRVMWorldSetup.Builder.BAND_NAME_B, new DoubleInterval(1.75, 2.25));
            putIntraBandSynergyFactor(SRVMWorldSetup.Builder.BAND_NAME_C, new DoubleInterval(1.75, 2.25));
        }

    }

    public static final class HighFrequenceBidderBuilder extends Builder {

        /**
         * Create a BidderSetup Builder with 2.6GHz Bidder default values (see Kreoemer et. al (2014)).
         */
        public HighFrequenceBidderBuilder() {
            super("2.6 Ghz Bidder (High Frequence Bidder) Setup", 1);
            putMeanBaseValue(SRVMWorldSetup.Builder.BAND_NAME_A, new BigDecimal(0));
            putMeanBaseValue(SRVMWorldSetup.Builder.BAND_NAME_B, new BigDecimal(70));
            putMeanBaseValue(SRVMWorldSetup.Builder.BAND_NAME_C, new BigDecimal(15));
            putIntraBandSynergyFactor(SRVMWorldSetup.Builder.BAND_NAME_A, new DoubleInterval(1.75, 2.25));
            putIntraBandSynergyFactor(SRVMWorldSetup.Builder.BAND_NAME_B, new DoubleInterval(1.75, 2.25));
            putIntraBandSynergyFactor(SRVMWorldSetup.Builder.BAND_NAME_C, new DoubleInterval(1.75, 2.25));
        }

    }

    public static final class SecondaryBidderBuilder extends Builder {

        /**
         * Create a BidderSetup Builder with Secondary Bidder default values (see Kreoemer et. al (2014)).
         */
        public SecondaryBidderBuilder() {
            super("Secondary Bidder Setup", 2);
            putMeanBaseValue(SRVMWorldSetup.Builder.BAND_NAME_A, new BigDecimal(200));
            putMeanBaseValue(SRVMWorldSetup.Builder.BAND_NAME_B, new BigDecimal(70));
            putMeanBaseValue(SRVMWorldSetup.Builder.BAND_NAME_C, new BigDecimal(15));
            putIntraBandSynergyFactor(SRVMWorldSetup.Builder.BAND_NAME_A, new DoubleInterval(1.75, 2.25));
            putIntraBandSynergyFactor(SRVMWorldSetup.Builder.BAND_NAME_B, new DoubleInterval(1.75, 2.25));
            putIntraBandSynergyFactor(SRVMWorldSetup.Builder.BAND_NAME_C, new DoubleInterval(1.75, 2.25));
        }

    }


    public static final class PrimaryBidderBuilder extends Builder {

        /**
         * Create a BidderSetup Builder with Primary Bidder default values (see Kreoemer et. al (2014)).
         */
        public PrimaryBidderBuilder() {
            super("Primary Bidder Setup", 2);
            putMeanBaseValue(SRVMWorldSetup.Builder.BAND_NAME_A, new BigDecimal(300));
            putMeanBaseValue(SRVMWorldSetup.Builder.BAND_NAME_B, new BigDecimal(70));
            putMeanBaseValue(SRVMWorldSetup.Builder.BAND_NAME_C, new BigDecimal(15));
            putIntraBandSynergyFactor(SRVMWorldSetup.Builder.BAND_NAME_A, new DoubleInterval(3.75, 4.25));
            putIntraBandSynergyFactor(SRVMWorldSetup.Builder.BAND_NAME_B, new DoubleInterval(1.75, 2.25));
            putIntraBandSynergyFactor(SRVMWorldSetup.Builder.BAND_NAME_C, new DoubleInterval(1.75, 2.25));
        }

    }

}
