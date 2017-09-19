/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model.bvm;

import com.google.common.base.Preconditions;
import org.spectrumauctions.sats.core.bidlang.BiddingLanguage;
import org.spectrumauctions.sats.core.bidlang.generic.FlatSizeIterators.GenericSizeDecreasing;
import org.spectrumauctions.sats.core.bidlang.generic.FlatSizeIterators.GenericSizeIncreasing;
import org.spectrumauctions.sats.core.bidlang.generic.GenericValueBidder;
import org.spectrumauctions.sats.core.bidlang.generic.SimpleRandomOrder.XORQRandomOrderSimple;
import org.spectrumauctions.sats.core.bidlang.generic.SizeOrderedPowerset.GenericPowersetDecreasing;
import org.spectrumauctions.sats.core.bidlang.generic.SizeOrderedPowerset.GenericPowersetIncreasing;
import org.spectrumauctions.sats.core.bidlang.xor.DecreasingSizeOrderedXOR;
import org.spectrumauctions.sats.core.bidlang.xor.IncreasingSizeOrderedXOR;
import org.spectrumauctions.sats.core.bidlang.xor.SizeBasedUniqueRandomXOR;
import org.spectrumauctions.sats.core.model.*;
import org.spectrumauctions.sats.core.util.random.JavaUtilRNGSupplier;
import org.spectrumauctions.sats.core.util.random.UniformDistributionRNG;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Michael Weiss
 */
public final class BMBidder extends Bidder<BMLicense> implements GenericValueBidder<BMBand> {

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


    /* (non-Javadoc)
     * @see Bidder#getValue(Bundle)
     */
    @Override
    public BigDecimal calculateValue(Bundle<BMLicense> bundle) {
        if (bundle.isEmpty()) {
            return BigDecimal.ZERO;
        }
        Preconditions.checkArgument(bundle.getWorld().equals(this.getWorld()), "Bundle not from same world as this bidder");
        // Count the number of licenses per band
        Map<BMBand, Integer> quantities = new HashMap<>();
        for (BMBand band : getWorld().getBands()) {
            quantities.put(band, 0);
        }
        for (BMLicense license : bundle) {
            Integer currentValue = quantities.get(license.getBand());
            if (currentValue == null) {
                System.out.println("ERROR: ITEM WITH OUTSIDE-WORLD BAND!!!");
            } else if (currentValue < positiveValueThreshold.get(license.getBand().getName())) { // Free disposal otherwise
                quantities.put(license.getBand(), currentValue + 1);
            }
        }
        return calculateValue(quantities);
    }

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


    /**
     * @see Bidder#getWorld()
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
        if (world instanceof BMWorld) {
            setWorld((BMWorld) world);
        } else {
            throw new IncompatibleWorldException("Wrong world class");
        }

    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((baseValues == null) ? 0 : baseValues.hashCode());
        result = prime * result + ((positiveValueThreshold == null) ? 0 : positiveValueThreshold.hashCode());
        result = prime * result + ((synergyFactors == null) ? 0 : synergyFactors.hashCode());
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
        BMBidder other = (BMBidder) obj;
        if (baseValues == null) {
            if (other.baseValues != null)
                return false;
        } else if (!baseValues.equals(other.baseValues))
            return false;
        if (positiveValueThreshold == null) {
            if (other.positiveValueThreshold != null)
                return false;
        } else if (!positiveValueThreshold.equals(other.positiveValueThreshold))
            return false;
        if (synergyFactors == null) {
            if (other.synergyFactors != null)
                return false;
        } else if (!synergyFactors.equals(other.synergyFactors))
            return false;
        return true;
    }


    /* (non-Javadoc)
     * @see GenericValueBidder#calculateValue(java.util.Map)
     */
    @Override
    public BigDecimal calculateValue(Map<BMBand, Integer> genericQuantities) {
        //Check input
        for (Entry<BMBand, Integer> entry : genericQuantities.entrySet()) {
            Preconditions.checkArgument(entry.getKey().getWorld().equals(this.getWorld()), "Band is not from this world" + entry.getKey().getName());
            Preconditions.checkArgument(entry.getValue() >= 0, "Quantity must not be negative. Band:" + entry.getKey().getName() + "\t Licenses:" + entry.getValue());
            Preconditions.checkArgument(entry.getValue() <= entry.getKey().getNumberOfLicenses(), "Specified too many licenses for this band" + entry.getKey().getName() + "\t Licenses:" + entry.getValue());
        }
        //Calculate Value
        BigDecimal value = BigDecimal.ZERO;
        for (Entry<BMBand, Integer> entry : genericQuantities.entrySet()) {
            int synergyQuantitiyLimit = highestSynergyQuantity(entry.getKey());
            BigDecimal baseValue = getBaseValue(entry.getKey());
            if (entry.getValue() > synergyQuantitiyLimit) {
                // More items than synergy limit
                // items with synergy
                BigDecimal synergyFactor = synergyFactor(entry.getKey(), synergyQuantitiyLimit);
                value = value.add(new BigDecimal(synergyQuantitiyLimit).multiply(synergyFactor).multiply(baseValue));
                // items without synergy
                value = value.add(baseValue.multiply(new BigDecimal(entry.getValue() - synergyQuantitiyLimit)));
            } else {
                // Synergy amongst all items
                BigDecimal synergyFactor = synergyFactor(entry.getKey(), entry.getValue());
                value = value.add(new BigDecimal(entry.getValue()).multiply(synergyFactor).multiply(baseValue));
            }
        }
        return value;
    }


}
