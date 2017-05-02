/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.uzh.ifi.ce.mweiss.sats.core.model.mrm;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import ch.uzh.ifi.ce.mweiss.sats.core.bidlang.generic.FlatSizeIterators.GenericSizeIncreasing;
import ch.uzh.ifi.ce.mweiss.sats.core.bidlang.generic.GenericValueBidder;
import ch.uzh.ifi.ce.mweiss.sats.core.bidlang.generic.SizeOrderedPowerset.GenericPowersetIncreasing;
import ch.uzh.ifi.ce.mweiss.sats.core.bidlang.xor.DecreasingSizeOrderedXOR;
import ch.uzh.ifi.ce.mweiss.sats.core.bidlang.xor.SizeBasedUniqueRandomXOR;
import ch.uzh.ifi.ce.mweiss.sats.core.model.*;
import ch.uzh.ifi.ce.mweiss.sats.core.util.random.JavaUtilRNGSupplier;
import ch.uzh.ifi.ce.mweiss.sats.core.util.random.UniformDistributionRNG;
import com.google.common.base.Preconditions;

import ch.uzh.ifi.ce.mweiss.sats.core.bidlang.BiddingLanguage;
import ch.uzh.ifi.ce.mweiss.sats.core.bidlang.generic.FlatSizeIterators.GenericSizeDecreasing;
import ch.uzh.ifi.ce.mweiss.sats.core.bidlang.generic.SimpleRandomOrder.XORQRandomOrderSimple;
import ch.uzh.ifi.ce.mweiss.sats.core.bidlang.generic.SizeOrderedPowerset.GenericPowersetDecreasing;
import ch.uzh.ifi.ce.mweiss.sats.core.bidlang.xor.IncreasingSizeOrderedXOR;
import ch.uzh.ifi.ce.mweiss.sats.core.model.mrm.MRMRegionsMap.Region;
import ch.uzh.ifi.ce.mweiss.sats.core.util.math.ContinuousPiecewiseLinearFunction;


/**
 * @author Michael Weiss
 *
 */
public abstract class MRMBidder extends Bidder<MRMLicense> implements GenericValueBidder<MRMGenericDefinition> {
    

    private transient MRMWorld world;

    /**
     * A parameter defining an expected profit per served customer, if quality of service and regional discount are ignored.<br>
     * It can be understood as a relative bidder strength parameter.
     */
    private final BigDecimal alpha;

    /**
     * A parameter defining the target market share this bidder intends to cover, per region. <br>
     * The bidders value for a bundle increases heavily as soon as the capacity share he has in a region gets close to.
     * <p>
     *     key: regionId, value: beta
     */
    private final Map<Integer, BigDecimal> beta;

    /**
     * <p>
     *     key: regionId, value: beta
     *
     */
    private final Map<Integer, BigDecimal> zLow;

    /**
     * <p>
     *     key: regionId, value: beta
     *
     */
    private final Map<Integer, BigDecimal> zHigh;




	MRMBidder(long id, long populationId, MRMWorld world, MRMBidderSetup setup, UniformDistributionRNG rng) {
        super(setup, populationId, id, world.getId());
        this.world = world;
        this.alpha = setup.drawAlpha(rng);
        this.beta = drawBeta(world, setup, rng);
        this.zLow = setup.drawZLow(beta, world, rng);
        zLow.entrySet().stream().forEach(z -> Preconditions.checkArgument(z.getValue().compareTo(BigDecimal.ZERO)  > 0 ));
        this.zHigh = setup.drawZHigh(beta, world, rng);
        assertRegionalValuesAssigned();
    }

    private Map<Integer, BigDecimal> drawBeta(MRMWorld world, MRMBidderSetup setup, UniformDistributionRNG rng) {
    	Map<Integer, BigDecimal> tempBeta = new HashMap<>();
        for(Region region : world.getRegionsMap().getRegions()){
        	tempBeta.put(region.getId(), setup.drawBeta(region, rng));
        }
        return Collections.unmodifiableMap(tempBeta);
    }
   
    private void assertRegionalValuesAssigned(){
  for(Region region : world.getRegionsMap().getRegions()){
    Preconditions.checkArgument(getBeta(region) != null);
        Preconditions.checkArgument(getzLow(region)!= null);
            Preconditions.checkArgument(getzHigh(region) != null);
    	}
    	if(beta.size() != world.getRegionsMap().getNumberOfRegions()){
        throw new IllegalArgumentException("Defined beta for region which is not part of this world");
    	}
    }

    /**
     * Calculates the omega factor (i.e., the regional value)
     * @return
     */
    public BigDecimal omegaFactor(Region r, BigDecimal sv) {
        BigDecimal population = new BigDecimal(String.valueOf(r.getPopulation()));
        return sv.multiply(alpha).multiply(getBeta(r)).multiply(population);
    }


    /**
     * Calculates the sv-function [See description in paper]
     */
    public BigDecimal svFunction(Region region, BigDecimal c){

        Preconditions.checkArgument(c.compareTo(BigDecimal.ZERO) >= 0
                        && c.compareTo(world.getMaximumRegionalCapacity()) <= 0,
                "c must be between 0 and the c for all licenses (="
                        + world.getMaximumRegionalCapacity().toString()
                        + ") but is actually "
                        + c.toString());
    	return svFunction(region).getY(c);
    }

    public ContinuousPiecewiseLinearFunction svFunction(Region region){
        int population = region.getPopulation();
        BigDecimal beta = this.getBeta(region);
        Map<BigDecimal, BigDecimal> cornerPoints = new HashMap<>();
        cornerPoints.put(BigDecimal.ZERO, BigDecimal.ZERO);
        BigDecimal x1 = getzLow(region).multiply(BigDecimal.valueOf(population)).multiply(beta);
        BigDecimal y1 = BigDecimal.valueOf(0.27).multiply(alpha);
        cornerPoints.put(x1, y1);
        BigDecimal x2 = getzHigh(region).multiply(BigDecimal.valueOf(population)).multiply(beta);
        BigDecimal y2 = BigDecimal.valueOf(0.73).multiply(alpha);
        cornerPoints.put(x2, y2);
        BigDecimal x3 = world.getMaximumRegionalCapacity();
        BigDecimal y3 = alpha;
        cornerPoints.put(x3, y3);
        return new ContinuousPiecewiseLinearFunction(cornerPoints);
    }

    /**
     * Calculates the gamma factor, as explained in the model writeup. <br>
     * The gamma factor represents a bidder-specific discount of the the regional (omega) values.
     * @param r The region for which the discount is requested
     * @param bundle The complete bundle (not only containing the licenses of r).
     * @return
     */
    public abstract BigDecimal gammaFactor(Region r, Bundle<MRMLicense> bundle);

    /**
     * Calculates the gamma factors for all regions. For explanations of the gamma factors, see {@link #gammaFactor(Region, Bundle)}
     * @param bundle The bundle for which the discounts will be calculated.
     * @return
     */
    public abstract Map<Region, BigDecimal> gammaFactors(Bundle<MRMLicense> bundle);

    @Override
    public BigDecimal calculateValue(Bundle<MRMLicense> bundle) {
        if (bundle.isEmpty()) {
            return BigDecimal.ZERO;
        }
        //Pre filters the map such that for regional calculations, only licenses for the according region are in the passed (sub-)bundles.
        //This is for speedup of the calculation, but has no effect on the outcome of the value.
        BigDecimal totalValue = BigDecimal.ZERO;
        Map<Region, Bundle<MRMLicense>> regionalBundles = MRMWorld.getLicensesPerRegion(bundle);
        //For speedup of calculation of global bidders, pre-compute gamma Factors for all requions in advance
        Map<Region, BigDecimal> gammaFactors = gammaFactors(bundle);
        //Calculate Regional Discounted Values and add them to total value
        for (Entry<Region, Bundle<MRMLicense>> regionalBundleEntry : regionalBundles.entrySet()) {
            BigDecimal c = MRMWorld.c(regionalBundleEntry.getKey(), regionalBundleEntry.getValue());
            BigDecimal regionalBeta = getBeta(regionalBundleEntry.getKey());
            int population = regionalBundleEntry.getKey().getPopulation();
            BigDecimal sv = svFunction(regionalBundleEntry.getKey(), c);
            BigDecimal regionalValue = omegaFactor(regionalBundleEntry.getKey(), sv);
            //Gamma Factor requires complete bundle (for global bidder to calculate #uncovered regions)
            BigDecimal gammaFactor = gammaFactors.get(regionalBundleEntry.getKey());
            BigDecimal discountedRegionalValue = regionalValue.multiply(gammaFactor);
            totalValue = totalValue.add(discountedRegionalValue);
        }
        return totalValue;
    }

    /* (non-Javadoc)
     * @see GenericValueBidder#calculateValue(java.util.Map)
     */
    @Override
    public BigDecimal calculateValue(Map<MRMGenericDefinition, Integer> genericQuantities) {
        //TODO: Change this very naive approach to a faster one, where generics don't have to be transformed into bundles
        Bundle<MRMLicense> bundle = new Bundle<>();
        Map<MRMGenericDefinition, Integer> addedQuantities = new HashMap<>();
        for (Region region : getWorld().getRegionsMap().getRegions()) {
            for (MRMBand band : getWorld().getBands()) {
                addedQuantities.put(new MRMGenericDefinition(band, region), 0);
            }
        }
        for (MRMLicense license : getWorld().getLicenses()) {
            MRMGenericDefinition def = new MRMGenericDefinition(license.getBand(), license.getRegion());
            Integer requiredQuantity = genericQuantities.get(def);
            Integer addedQuantity = addedQuantities.get(def);
            if (requiredQuantity != null && requiredQuantity > addedQuantity) {
                bundle.add(license);
                addedQuantities.put(def, addedQuantity + 1);
            }
        }
        return calculateValue(bundle);
    }


    @Override
    public MRMWorld getWorld() {
        return this.world;
    }

    private void setWorld(MRMWorld world) {
        this.world = world;
    }
    
    public BigDecimal getzLow(Region region) {
		return zLow.get(region.getId());
	}

	public BigDecimal getzHigh(Region region) {
		return zHigh.get(region.getId());
	}

    public BigDecimal getAlpha() {
        return alpha;
    }

    public BigDecimal getBeta(Region region) {
        return beta.get(region.getId());
    }

    /* (non-Javadoc)
     * @see Bidder#refreshReference(World)
     */
    @Override
    public void refreshReference(World world) {
        if (world instanceof MRMWorld) {
            setWorld((MRMWorld) world);
        } else {
            throw new IncompatibleWorldException("Wrong world class");
        }

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
            return clazz.cast(RandomOrderXORQFactory.getXORQRandomOrderSimpleLang(this));
        } else {
            throw new UnsupportedBiddingLanguageException();
        }
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((alpha == null) ? 0 : alpha.hashCode());
        result = prime * result + ((beta == null) ? 0 : beta.hashCode());
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
        MRMBidder other = (MRMBidder) obj;
        if (alpha == null) {
            if (other.alpha != null)
                return false;
        } else if (!alpha.equals(other.alpha))
            return false;
        if (beta == null) {
            if (other.beta != null)
                return false;
        } else if (!beta.equals(other.beta))
            return false;
        return true;
    }
    
    
}
