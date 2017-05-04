package org.spectrumauctions.sats.core.model.srvm;


import org.spectrumauctions.sats.core.bidlang.generic.FlatSizeIterators.GenericSizeDecreasing;
import org.spectrumauctions.sats.core.bidlang.generic.FlatSizeIterators.GenericSizeIncreasing;
import org.spectrumauctions.sats.core.bidlang.generic.GenericValueBidder;
import org.spectrumauctions.sats.core.bidlang.generic.SizeOrderedPowerset.GenericPowersetIncreasing;
import org.spectrumauctions.sats.core.bidlang.xor.DecreasingSizeOrderedXOR;
import org.spectrumauctions.sats.core.bidlang.xor.IncreasingSizeOrderedXOR;
import org.spectrumauctions.sats.core.bidlang.xor.SizeBasedUniqueRandomXOR;
import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.UnsupportedBiddingLanguageException;
import org.spectrumauctions.sats.core.model.World;
import org.spectrumauctions.sats.core.util.random.JavaUtilRNGSupplier;
import org.spectrumauctions.sats.core.util.random.RNGSupplier;
import org.spectrumauctions.sats.core.bidlang.BiddingLanguage;
import org.spectrumauctions.sats.core.bidlang.generic.SimpleRandomOrder.XORQRandomOrderSimple;
import org.spectrumauctions.sats.core.bidlang.generic.SizeOrderedPowerset.GenericPowersetDecreasing;
import org.spectrumauctions.sats.core.model.Bundle;
import com.google.common.base.Preconditions;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Michael Weiss
 */
public final class SRMBidder extends Bidder<SRMLicense> implements GenericValueBidder<SRMBand> {

    private static final int CALCSCALE = 5;

    private transient SRMWorld world;
    private final BigDecimal bidderStrength;
    private final Map<String, Integer> synergyThreshold;
    private final Map<String, BigDecimal> baseValues;
    private final Map<String, BigDecimal> intrabandSynergyFactors;


    /**
     * Synergie which apply to the complete bundle as soon as more than one band is represented by the bundle.
     * Attention: The value should be greater or equal to 1 (or in terms of the Kroemer et al model description: 1 + interbandsynergy).
     */
    private final BigDecimal interbandSynergyValue;

    SRMBidder(SRMBidderSetup setup, SRMWorld world, long currentId, long population, RNGSupplier rngSupplier) {
        super(setup, population, currentId, world.getId());
        this.world = world;
        Map<SRMBand, Integer> synergyThreshold = setup.drawSynergyThresholds(world, rngSupplier);
        this.synergyThreshold = bandNameMap(synergyThreshold);
        this.bidderStrength = setup.drawBidderStrength(world, rngSupplier);
        Map<SRMBand, BigDecimal> baseValues = setup.drawBaseValues(world, bidderStrength, rngSupplier);
        this.baseValues = bandNameMap(baseValues);
        Map<SRMBand, BigDecimal> intrabandSynergyFactors = setup.drawIntraBandSynergyFactors(world, rngSupplier);
        this.intrabandSynergyFactors = bandNameMap(intrabandSynergyFactors);
        this.interbandSynergyValue = setup.drawInterBandSynergyFactor(world, rngSupplier);
        store();
    }

    /**
     * Checks if the map contains all bands of this bidders world as key and, if so, returns an equivalent map where the bandNames are keys.
     *
     * @param inputMap
     * @return
     */
    private <T> Map<String, T> bandNameMap(Map<SRMBand, T> inputMap) {
        Preconditions.checkArgument(world.getBands().containsAll(inputMap.keySet()) && world.getBands().size() == inputMap.size(), "Map is not complete for this world");
        Map<String, T> result = new HashMap<>();
        for (Entry<SRMBand, T> inputEntry : inputMap.entrySet()) {
            result.put(inputEntry.getKey().getName(), inputEntry.getValue());
        }
        return result;
    }

    /* (non-Javadoc)
     * @see Bidder#getWorld()
     */
    @Override
    public SRMWorld getWorld() {
        return world;
    }


    /**
     * This random value is used exclusively in {{@link #drawBaseValues(SRMWorld, BigDecimal, RNGSupplier)}, but stored in the bidder for easier analysis.<br>
     * Its mean is typically 1.<br><br>
     * Note that the bidder strength is not the only random influence on the base values,
     * hence a high bidder strength does not imply that a bidder is stronger than others, it simply makes it more likely.
     *
     * @return
     */
    public BigDecimal getBidderStrength() {
        return bidderStrength;
    }


    public Map<String, Integer> getSynergyThreshold() {
        return Collections.unmodifiableMap(synergyThreshold);
    }


    public Map<String, BigDecimal> getBaseValues() {
        return Collections.unmodifiableMap(baseValues);
    }


    public Map<String, BigDecimal> getIntrabandSynergyFactors() {
        return Collections.unmodifiableMap(intrabandSynergyFactors);
    }


    public BigDecimal getInterbandSynergyValue() {
        return interbandSynergyValue;
    }


    @Override
    public BigDecimal calculateValue(Bundle<SRMLicense> licenses) {
        Map<SRMBand, Integer> bandCount = new HashMap<>();
        for (SRMBand band : this.getWorld().getBands()) {
            bandCount.put(band, 0);
        }
        for (SRMLicense license : licenses) {
            bandCount.put(license.getBand(), bandCount.get(license.getBand()) + 1);
        }
        return calculateValue(bandCount);
    }


    /* (non-Javadoc)
     * @see GenericValueBidder#calculateValue(java.util.Map)
     */
    @Override
    public BigDecimal calculateValue(Map<SRMBand, Integer> genericQuantities) {
        BigDecimal bandValuesSum = BigDecimal.ZERO;
        //We count the number of bands with more than 0 licenses in this bundle
        int synergyBandCount = 0;
        for (Entry<SRMBand, Integer> entry : genericQuantities.entrySet()) {
            if (entry.getValue() != 0) {
                bandValuesSum = bandValuesSum.add(getBandValue(entry.getKey(), entry.getValue()));
                synergyBandCount++;
            }
        }
        if (synergyBandCount >= 2) {
            // We have interband synergies
            bandValuesSum = bandValuesSum.multiply(interbandSynergyValue);
        }
        return bandValuesSum;
    }


    private BigDecimal getBandValue(SRMBand band, int quantity) {
        // The min{2,n} or min{4,n} part of the value function
        int firstSummand = quantity > synergyThreshold.get(band.getName()) ? synergyThreshold.get(band.getName()) : quantity;
        // The min{3/4, (n-1)/n} * syn_i(B)} or equivalent for other bands part
        BigDecimal minFraction = new BigDecimal(firstSummand - 1).divide(new BigDecimal(firstSummand), RoundingMode.CEILING);
        BigDecimal synergyFactor = intrabandSynergyFactors.get(band.getName());
        BigDecimal secondSummand = minFraction.multiply(synergyFactor);
        // The marginal decreasing third summand (max{0, ln{n-1)})
        int toLog = quantity - (synergyThreshold.get(band.getName()) - 1);
        BigDecimal thirdSummand;
        if (toLog <= 0) {
            thirdSummand = BigDecimal.ZERO;
        } else {
            double lnApproximation = Math.log(toLog);
            BigDecimal ln = new BigDecimal(lnApproximation, new MathContext(CALCSCALE, RoundingMode.CEILING));
            if (ln.compareTo(BigDecimal.ZERO) >= 0) {
                thirdSummand = ln;
            } else {
                thirdSummand = BigDecimal.ZERO;
            }
        }

        // Calculates product
        BigDecimal firstFactor = new BigDecimal(firstSummand).add(secondSummand).add(thirdSummand);
        BigDecimal baseValue = baseValues.get(band.getName());
        // No need to take random influence and relative bidder strength into account. Is already included in baseValue;
        return firstFactor.multiply(baseValue);
    }


    /* (non-Javadoc)
     * @see Bidder#getValueFunctionRepresentation(java.lang.Class, long)
     */
    @Override
    public <T extends BiddingLanguage> T getValueFunction(Class<T> clazz, long seed)
            throws UnsupportedBiddingLanguageException {
        if (clazz.isAssignableFrom(SizeBasedUniqueRandomXOR.class)) {
            return clazz.cast(
                    new SizeBasedUniqueRandomXOR<>(world.getLicenses(), new JavaUtilRNGSupplier(seed), this));
        } else if (clazz.isAssignableFrom(IncreasingSizeOrderedXOR.class)) {
            return clazz.cast(
                    new IncreasingSizeOrderedXOR<>(world.getLicenses(), this));
        } else if (clazz.isAssignableFrom(DecreasingSizeOrderedXOR.class)) {
            return clazz.cast(
                    new DecreasingSizeOrderedXOR<>(world.getLicenses(), this));
        } else if (clazz.isAssignableFrom(GenericSizeIncreasing.class)) {
            return clazz.cast(
                    SizeOrderedGenericFactory.getSizeOrderedGenericLang(true, this));
        } else if (clazz.isAssignableFrom(GenericSizeDecreasing.class)) {
            return clazz.cast(
                    SizeOrderedGenericFactory.getSizeOrderedGenericLang(false, this));
        } else if (clazz.isAssignableFrom(GenericPowersetIncreasing.class)) {
            return clazz.cast(
                    SizeOrderedGenericPowersetFactory.getSizeOrderedGenericLang(true, this));
        } else if (clazz.isAssignableFrom(GenericPowersetDecreasing.class)) {
            return clazz.cast(
                    SizeOrderedGenericPowersetFactory.getSizeOrderedGenericLang(false, this));
        } else if (clazz.isAssignableFrom(XORQRandomOrderSimple.class)) {
            return clazz.cast(
                    RandomOrderXORQFactory.getXORQRandomOrderSimpleLang(this));
        } else {
            throw new UnsupportedBiddingLanguageException();
        }
    }

    /* (non-Javadoc)
     * @see Bidder#refreshReference(World)
     */
    @Override
    public void refreshReference(World world) {
        Preconditions.checkArgument(world.getId() == getWorldId());
        if (world instanceof SRMWorld) {
            this.world = (SRMWorld) world;
        } else {
            throw new IllegalArgumentException("World is not of correct type");
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((baseValues == null) ? 0 : baseValues.hashCode());
        result = prime * result + ((bidderStrength == null) ? 0 : bidderStrength.hashCode());
        result = prime * result + ((interbandSynergyValue == null) ? 0 : interbandSynergyValue.hashCode());
        result = prime * result + ((intrabandSynergyFactors == null) ? 0 : intrabandSynergyFactors.hashCode());
        result = prime * result + ((synergyThreshold == null) ? 0 : synergyThreshold.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        SRMBidder other = (SRMBidder) obj;
        if (baseValues == null) {
            if (other.baseValues != null)
                return false;
        } else if (!baseValues.equals(other.baseValues))
            return false;
        if (bidderStrength == null) {
            if (other.bidderStrength != null)
                return false;
        } else if (!bidderStrength.equals(other.bidderStrength))
            return false;
        if (interbandSynergyValue == null) {
            if (other.interbandSynergyValue != null)
                return false;
        } else if (!interbandSynergyValue.equals(other.interbandSynergyValue))
            return false;
        if (intrabandSynergyFactors == null) {
            if (other.intrabandSynergyFactors != null)
                return false;
        } else if (!intrabandSynergyFactors.equals(other.intrabandSynergyFactors))
            return false;
        if (synergyThreshold == null) {
            if (other.synergyThreshold != null)
                return false;
        } else if (!synergyThreshold.equals(other.synergyThreshold))
            return false;
        return true;
    }


}
