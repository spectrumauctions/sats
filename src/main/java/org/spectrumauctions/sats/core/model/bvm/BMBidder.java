/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model.bvm;

import com.google.common.base.Preconditions;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.marketdesignresearch.mechlib.core.Bundle;
import org.marketdesignresearch.mechlib.core.BundleEntry;
import org.marketdesignresearch.mechlib.core.Good;
import org.marketdesignresearch.mechlib.core.price.Prices;
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
import org.spectrumauctions.sats.core.util.random.UniformDistributionRNG;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Michael Weiss
 */
@EqualsAndHashCode(callSuper = true)
public final class BMBidder extends SATSBidder {

    private static final Logger logger = LogManager.getLogger(BMBidder.class);

    private static final long serialVersionUID = 3132260871321701148L;
    private transient BMWorld world;

    /**
     * Key: BandName<br>
     * Value: {Key: Quantity of the band,  Value: Synergy Factor}
     */
    private final HashMap<String, Map<Integer, BigDecimal>> synergyFactors;
    /**
     * Key: BandName<br>
     * Value: Base value of the band
     */
    private final HashMap<String, BigDecimal> baseValues;
    /**
     * Key: BandName<br>
     * Value: Positive Value threshold of the band, i.e., until which quantity have the licenses positive marginal utility
     */
    private final HashMap<String, Integer> positiveValueThreshold;

    /**
     * Create a new bidder. The use of this constructor is not recommended.
     * Use {@link BMWorld#createPopulation(java.util.Collection)} instead, to create new bidder sets.
     */
    public BMBidder(long population, int bidderId, BMWorld world, BMBidderSetup setup, UniformDistributionRNG rng) {
        super(setup, population, bidderId, world.getId());
        this.world = world;
        HashMap<String, Map<Integer, BigDecimal>> synergyFactors = new HashMap<>();
        HashMap<String, BigDecimal> baseValues = new HashMap<>();
        HashMap<String, Integer> positiveValueThreshold = new HashMap<>();
        for (BMBand band : world.getBands()) {
            synergyFactors.put(band.getName(), setup.drawSynergyFactors(band, rng));
            baseValues.put(band.getName(), setup.drawBaseValue(band, rng));
            positiveValueThreshold.put(band.getName(), setup.drawPositiveValueThreshold(band, rng));
        }
        this.synergyFactors = synergyFactors;
        this.baseValues = baseValues;
        this.positiveValueThreshold = positiveValueThreshold;
        store();
    }


//    /**
//     * @see SATSBidder#getValue(LicenseBundle)
//     */
//    @Override
//    public BigDecimal calculateValue(Bundle bundle) {
//        if (bundle.getBundleEntries().isEmpty()) {
//            return BigDecimal.ZERO;
//        }
//        Preconditions.checkArgument(bundle.getWorld().equals(this.getWorld()), "Bundle not from same world as this bidder");
//        // Count the number of licenses per band
//        Map<BMBand, Integer> quantities = new HashMap<>();
//        for (BMBand band : getWorld().getBands()) {
//            quantities.put(band, 0);
//        }
//        for (BMLicense license : bundle) {
//            Integer currentValue = quantities.get(license.getBand());
//            if (currentValue == null) {
//                logger.error("ITEM WITH OUTSIDE-WORLD BAND!");
//            } else if (currentValue < positiveValueThreshold.get(license.getBand().getName())) { // Free disposal otherwise
//                quantities.put(license.getBand(), currentValue + 1);
//            }
//        }
//        return calculateValue(quantities);
//    }

    /**
     * Returns the synergy factor for a given band and quantity. Special Cases
     * (rules apply in this order): <br>
     * - returns 1 for quantity 1 <br>
     * - returns 0 for quantity 0 <br>
     * - If no synergy factors stored for band, returns 1 <br>
     * - The highest explicitely stored quantity is defined as the upper limit of
     * items with synergies, see {@link #highestSynergyQuantity(BMBand)}. <br>
     * - If quantity has no specified synergy factor, the synergy factor for next
     * lower known quantity is returned.
     */
    public BigDecimal synergyFactor(BMBand band, int quantity) {
        Preconditions.checkArgument(quantity >= 0);
        if (quantity == 1)
            return BigDecimal.ONE;
        if (quantity == 0)
            return BigDecimal.ZERO;
        BigDecimal synFactor = synergyFactors.get(band.getName()).get(quantity);
        if (synFactor != null) {
            return synFactor;
        } else {
            return synergyFactor(band, quantity - 1);
        }
    }

    public BigDecimal getBaseValue(BMBand band) {
        return baseValues.get(band.getName());
    }

    /**
     * @return the maximal number of items within a band, for which synergies
     * apply. For additional items in the same band, only their base value
     * (without synergies) is added to the total value, if they are not excluded by the {@link #positiveValueThreshold}.
     */
    public int highestSynergyQuantity(BMBand band) {
        if (synergyFactors.get(band.getName()) == null)
            return 1;
        if (synergyFactors.get(band.getName()) == null || synergyFactors.get(band.getName()).isEmpty()) {
            return 1;
        }
        Integer result = Collections.max(synergyFactors.get(band.getName()).keySet());
        if (result == null || result < 1) {
            return 1;
        }
        return result;
    }

    @Override
    public SATSBidder drawSimilarBidder(RNGSupplier rngSupplier) {
        return new BMBidder(getPopulation(), (int) getLongId(), getWorld(), (BMBidderSetup) getSetup(), rngSupplier.getUniformDistributionRNG());
    }


    /**
     * @see SATSBidder#getWorld()
     */
    @Override
    public BMWorld getWorld() {
        return world;
    }

    /**
     * Must only be called by {@link #refreshReference(World)}.
     * Explicit definition of private setter to prevent from generating setter by accident.
     */
    private void setWorld(BMWorld world) {
        if (getWorldId() != world.getId()) {
            throw new IncompatibleWorldException("The stored worldId does not represent the passed world reference");
        }
        this.world = world;
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
        if (world instanceof BMWorld) {
            setWorld((BMWorld) world);
        } else {
            throw new IncompatibleWorldException("Wrong world class");
        }

    }

    @Override
    public BigDecimal calculateValue(Bundle bundle) {
        if (bundle.getBundleEntries().isEmpty()) return BigDecimal.ZERO;
        // First, if there are only single licenses, construct the generic map
        Map<Good, Integer> combined = new HashMap<>();
        for (BundleEntry bundleEntry : bundle.getBundleEntries()) {
            if (bundleEntry.getGood() instanceof BMLicense) {
                Good band = getWorld().getGenericDefinitionOf((License) bundleEntry.getGood());
                int currentValue = combined.getOrDefault(band, 0);
                if (currentValue < positiveValueThreshold.get(band.getName())) { // Free disposal otherwise
                    combined.put(band, currentValue + 1);
                }
            } else if (bundleEntry.getGood() instanceof BMBand) {
                Good band = bundleEntry.getGood();
                int currentValue = combined.getOrDefault(band, 0);
                if (currentValue < positiveValueThreshold.get(band.getName())) { // Free disposal otherwise
                    combined.put(band, currentValue + bundleEntry.getAmount());
                }
            } else {
                throw new WrongConfigException("A good specified in a bundle is neither a BMLicense nor a BMBand!");
            }
        }

        bundle = new Bundle(combined);
        //Check input
        for (BundleEntry entry : bundle.getBundleEntries()) {
            Preconditions.checkArgument(entry.getGood() instanceof BMBand);
            BMBand band = (BMBand) entry.getGood();
            Preconditions.checkArgument(band.getWorld().equals(this.getWorld()), "Band is not from this world" + band.getName());
            Preconditions.checkArgument(entry.getAmount() >= 0, "Quantity must not be negative. Band:" + band.getName() + "\t Licenses:" + entry.getAmount());
            Preconditions.checkArgument(entry.getAmount() <= band.getQuantity(), "Specified too many licenses for this band" + band.getName() + "\t Licenses:" + entry.getAmount());
        }
        //Calculate Value
        BigDecimal value = BigDecimal.ZERO;
        for (BundleEntry entry : bundle.getBundleEntries()) {
            BMBand band = (BMBand) entry.getGood();
            int synergyQuantitiyLimit = highestSynergyQuantity(band);
            BigDecimal baseValue = getBaseValue(band);
            if (entry.getAmount() > synergyQuantitiyLimit) {
                // More items than synergy limit
                // items with synergy
                BigDecimal synergyFactor = synergyFactor(band, synergyQuantitiyLimit);
                value = value.add(new BigDecimal(synergyQuantitiyLimit).multiply(synergyFactor).multiply(baseValue));
                // items without synergy
                value = value.add(baseValue.multiply(new BigDecimal(entry.getAmount() - synergyQuantitiyLimit)));
            } else {
                // Synergy amongst all items
                BigDecimal synergyFactor = synergyFactor(band, entry.getAmount());
                value = value.add(new BigDecimal(entry.getAmount()).multiply(synergyFactor).multiply(baseValue));
            }
        }
        return value;
    }


    @Override
    public List<Bundle> getBestBundles(Prices prices, int maxNumberOfBundles, boolean allowNegative) {
        throw new NotImplementedException("Demand Query to be implemented");
    }

}
