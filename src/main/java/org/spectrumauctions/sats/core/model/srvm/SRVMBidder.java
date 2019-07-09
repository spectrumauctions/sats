package org.spectrumauctions.sats.core.model.srvm;


import com.google.common.base.Preconditions;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.NotImplementedException;
import org.marketdesignresearch.mechlib.domain.Bundle;
import org.marketdesignresearch.mechlib.domain.BundleEntry;
import org.marketdesignresearch.mechlib.domain.Good;
import org.marketdesignresearch.mechlib.domain.price.Prices;
import org.spectrumauctions.sats.core.bidlang.BiddingLanguage;
import org.spectrumauctions.sats.core.bidlang.generic.FlatSizeIterators.GenericSizeDecreasing;
import org.spectrumauctions.sats.core.bidlang.generic.FlatSizeIterators.GenericSizeIncreasing;
import org.spectrumauctions.sats.core.bidlang.generic.SimpleRandomOrder.XORQRandomOrderSimple;
import org.spectrumauctions.sats.core.bidlang.generic.SizeOrderedPowerset.GenericPowersetDecreasing;
import org.spectrumauctions.sats.core.bidlang.generic.SizeOrderedPowerset.GenericPowersetIncreasing;
import org.spectrumauctions.sats.core.bidlang.xor.DecreasingSizeOrderedXOR;
import org.spectrumauctions.sats.core.bidlang.xor.IncreasingSizeOrderedXOR;
import org.spectrumauctions.sats.core.bidlang.xor.SizeBasedUniqueRandomXOR;
import org.spectrumauctions.sats.core.model.*;
import org.spectrumauctions.sats.core.util.random.RNGSupplier;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Michael Weiss
 */
@EqualsAndHashCode(callSuper = true)
public final class SRVMBidder extends SATSBidder {

    private static final int CALCSCALE = 5;
    private static final long serialVersionUID = -4577743658098455267L;

    private transient SRVMWorld world;
    private final BigDecimal bidderStrength;
    private final HashMap<String, Integer> synergyThreshold;
    private final HashMap<String, BigDecimal> baseValues;
    private final HashMap<String, BigDecimal> intrabandSynergyFactors;


    /**
     * Synergy which apply to the complete bundle as soon as more than one band is represented by the bundle.
     * Attention: The value should be greater or equal to 1 (or in terms of the Kroemer et al model description: 1 + interbandsynergy).
     */
    private final BigDecimal interbandSynergyValue;

    SRVMBidder(SRVMBidderSetup setup, SRVMWorld world, long currentId, long population, RNGSupplier rngSupplier) {
        super(setup, population, currentId, world.getId());
        this.world = world;
        HashMap<SRVMBand, Integer> synergyThreshold = setup.drawSynergyThresholds(world, rngSupplier);
        this.synergyThreshold = bandNameMap(synergyThreshold);
        this.bidderStrength = setup.drawBidderStrength(world, rngSupplier);
        HashMap<SRVMBand, BigDecimal> baseValues = setup.drawBaseValues(world, bidderStrength, rngSupplier);
        this.baseValues = bandNameMap(baseValues);
        HashMap<SRVMBand, BigDecimal> intrabandSynergyFactors = setup.drawIntraBandSynergyFactors(world, rngSupplier);
        this.intrabandSynergyFactors = bandNameMap(intrabandSynergyFactors);
        this.interbandSynergyValue = setup.drawInterBandSynergyFactor(world, rngSupplier);
        store();
    }

    /**
     * Checks if the map contains all bands of this bidders world as key and, if so, returns an equivalent map where the bandNames are keys.
     *
     * @param inputMap the input map of the bands
     * @return the names of the bands as strings
     */
    private <T> HashMap<String, T> bandNameMap(Map<SRVMBand, T> inputMap) {
        Preconditions.checkArgument(world.getBands().containsAll(inputMap.keySet()) && world.getBands().size() == inputMap.size(), "Map is not complete for this world");
        HashMap<String, T> result = new HashMap<>();
        for (Entry<SRVMBand, T> inputEntry : inputMap.entrySet()) {
            result.put(inputEntry.getKey().getName(), inputEntry.getValue());
        }
        return result;
    }

    /* (non-Javadoc)
     * @see SATSBidder#getWorld()
     */
    @Override
    public SRVMWorld getWorld() {
        return world;
    }


    /**
     * This random value is used exclusively in {{@link SRVMBidderSetup#drawBaseValues(SRVMWorld, BigDecimal, RNGSupplier)}, but stored in the bidder for easier analysis.<br>
     * Its mean is typically 1.<br><br>
     * Note that the bidder strength is not the only random influence on the base values,
     * hence a high bidder strength does not imply that a bidder is stronger than others, it simply makes it more likely.
     *
     * @return the bidder strength
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
    public BigDecimal calculateValue(Bundle bundle) {
        if (bundle.getBundleEntries().isEmpty()) return BigDecimal.ZERO;
        // First, if there are only single licenses, construct the generic map
        Map<Good, Integer> combined = new HashMap<>();
        for (BundleEntry bundleEntry : bundle.getBundleEntries()) {
            if (bundleEntry.getGood() instanceof SRVMLicense) {
                Good band = getWorld().getGenericDefinitionOf((License) bundleEntry.getGood());
                combined.put(band, combined.getOrDefault(band, 0) + 1);
            } else if (bundleEntry.getGood() instanceof SRVMBand) {
                Good band = bundleEntry.getGood();
                combined.put(band, combined.getOrDefault(band, 0) + bundleEntry.getAmount());
            } else {
                throw new WrongConfigException("A good specified in a bundle is neither a SRVMLicense nor a SRVMBand!");
            }
        }

        bundle = new Bundle(combined);
        BigDecimal bandValuesSum = BigDecimal.ZERO;
        //We count the number of bands with more than 0 licenses in this bundle
        int synergyBandCount = 0;
        for (BundleEntry entry : bundle.getBundleEntries()) {
            bandValuesSum = bandValuesSum.add(getBandValue((SRVMBand) entry.getGood(), entry.getAmount()));
            synergyBandCount++;
        }
        if (synergyBandCount >= 2) {
            // We have interband synergies
            bandValuesSum = bandValuesSum.multiply(interbandSynergyValue);
        }
        return bandValuesSum;
    }


    private BigDecimal getBandValue(SRVMBand band, int quantity) {
        // The min{2,n} or min{4,n} part of the value function
        int firstSummand = quantity > synergyThreshold.get(band.getName()) ? synergyThreshold.get(band.getName()) : quantity;
        // The min{3/4, (n-1)/n} * syn_i(B)} or equivalent for other bands part
        BigDecimal minFraction = new BigDecimal(firstSummand - 1).divide(new BigDecimal(firstSummand), CALCSCALE, RoundingMode.CEILING);
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

    @Override
    public SRVMBidder drawSimilarBidder(RNGSupplier rngSupplier) {
        return new SRVMBidder((SRVMBidderSetup) getSetup(), getWorld(), getLongId(), getPopulation(), rngSupplier);
    }

    @Override
    public <T extends BiddingLanguage> T getValueFunction(Class<T> clazz, RNGSupplier rngSupplier)
            throws UnsupportedBiddingLanguageException {
        if (clazz.isAssignableFrom(SizeBasedUniqueRandomXOR.class)) {
            return clazz.cast(
                    new SizeBasedUniqueRandomXOR(world.getLicenses(), rngSupplier, this));
        } else if (clazz.isAssignableFrom(IncreasingSizeOrderedXOR.class)) {
            return clazz.cast(
                    new IncreasingSizeOrderedXOR(world.getLicenses(), this));
        } else if (clazz.isAssignableFrom(DecreasingSizeOrderedXOR.class)) {
            return clazz.cast(
                    new DecreasingSizeOrderedXOR(world.getLicenses(), this));
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

    /**
     * @see SATSBidder#refreshReference(World)
     */
    @Override
    public void refreshReference(World world) {
        Preconditions.checkArgument(world.getId() == getWorldId());
        if (world instanceof SRVMWorld) {
            this.world = (SRVMWorld) world;
        } else {
            throw new IllegalArgumentException("World is not of correct type");
        }
    }


    @Override
    public List<Bundle> getBestBundles(Prices prices, int maxNumberOfBundles, boolean allowNegative) {
        throw new NotImplementedException("Demand Query to be implemented");
    }
}
