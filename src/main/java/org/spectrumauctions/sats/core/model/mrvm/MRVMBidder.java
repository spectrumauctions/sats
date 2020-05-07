/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model.mrvm;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import edu.harvard.econcs.jopt.solver.IMIP;
import edu.harvard.econcs.jopt.solver.SolveParam;
import edu.harvard.econcs.jopt.solver.mip.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.NotImplementedException;
import org.marketdesignresearch.mechlib.core.Allocation;
import org.marketdesignresearch.mechlib.core.Bundle;
import org.marketdesignresearch.mechlib.core.BundleEntry;
import org.marketdesignresearch.mechlib.core.price.Prices;
import org.marketdesignresearch.mechlib.instrumentation.MipInstrumentation;
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
import org.spectrumauctions.sats.core.util.math.ContinuousPiecewiseLinearFunction;
import org.spectrumauctions.sats.core.util.random.RNGSupplier;
import org.spectrumauctions.sats.core.util.random.UniformDistributionRNG;
import org.spectrumauctions.sats.opt.model.mrvm.MRVM_MIP;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * @author Michael Weiss
 */
@EqualsAndHashCode(callSuper = true)
public abstract class MRVMBidder extends SATSBidder {

    private static final long serialVersionUID = 8394009700504454313L;
    private transient MRVMWorld world;

    /**
     * A parameter defining an expected profit per served customer, if quality of service and regional discount are ignored.<br>
     * It can be understood as a relative bidder strength parameter.
     */
    private final BigDecimal alpha;

    /**
     * A parameter defining the target market share this bidder intends to cover, per region. <br>
     * The bidders value for a bundle increases heavily as soon as the capacity share he has in a region gets close to.
     * <p>
     * key: regionId, value: beta
     */
    private final HashMap<Integer, BigDecimal> beta;

    /**
     * <p>
     * key: regionId, value: beta
     */
    private final HashMap<Integer, BigDecimal> zLow;

    /**
     * <p>
     * key: regionId, value: beta
     */
    private final HashMap<Integer, BigDecimal> zHigh;


    MRVMBidder(long id, long populationId, MRVMWorld world, MRVMBidderSetup setup, UniformDistributionRNG rng) {
        super(setup, populationId, id, world.getId());
        this.world = world;
        this.alpha = setup.drawAlpha(rng);
        this.beta = drawBeta(world, setup, rng);
        this.zLow = setup.drawZLow(beta, world, rng);
        zLow.forEach((key, value) -> Preconditions.checkArgument(value.compareTo(BigDecimal.ZERO) > 0));
        this.zHigh = setup.drawZHigh(beta, world, rng);
        assertRegionalValuesAssigned();
    }

    private HashMap<Integer, BigDecimal> drawBeta(MRVMWorld world, MRVMBidderSetup setup, UniformDistributionRNG rng) {
        HashMap<Integer, BigDecimal> tempBeta = new HashMap<>();
        for (MRVMRegionsMap.Region region : world.getRegionsMap().getRegions()) {
            tempBeta.put(region.getId(), setup.drawBeta(region, rng));
        }
        return tempBeta;
    }

    private void assertRegionalValuesAssigned() {
        for (MRVMRegionsMap.Region region : world.getRegionsMap().getRegions()) {
            Preconditions.checkArgument(getBeta(region) != null);
            Preconditions.checkArgument(getzLow(region) != null);
            Preconditions.checkArgument(getzHigh(region) != null);
        }
        if (beta.size() != world.getRegionsMap().getNumberOfRegions()) {
            throw new IllegalArgumentException("Defined beta for region which is not part of this world");
        }
    }

    /**
     * Calculates the omega factor (i.e., the regional value)
     */
    public BigDecimal omegaFactor(MRVMRegionsMap.Region r, BigDecimal sv) {
        BigDecimal population = new BigDecimal(String.valueOf(r.getPopulation()));
        return sv.multiply(getBeta(r)).multiply(population);
    }


    /**
     * Calculates the sv-function [See description in paper]
     */
    public BigDecimal svFunction(MRVMRegionsMap.Region region, BigDecimal c) {

        Preconditions.checkArgument(c.compareTo(BigDecimal.ZERO) >= 0
                        && c.compareTo(world.getMaximumRegionalCapacity()) <= 0,
                "c must be between 0 and the c for all licenses (="
                        + world.getMaximumRegionalCapacity().toString()
                        + ") but is actually "
                        + c.toString());
        return svFunction(region).getY(c);
    }

    public ContinuousPiecewiseLinearFunction svFunction(MRVMRegionsMap.Region region) {
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
     *
     * @param r      The region for which the discount is requested
     * @param bundle The complete bundle (not only containing the licenses of r).
     */
    public abstract BigDecimal gammaFactor(MRVMRegionsMap.Region r, Set<MRVMLicense> bundle);

    /**
     * Calculates the gamma factors for all regions. For explanations of the gamma factors, see {@link #gammaFactor(MRVMRegionsMap.Region, Set)}
     *
     * @param bundle The bundle for which the discounts will be calculated.
     */
    public abstract Map<MRVMRegionsMap.Region, BigDecimal> gammaFactors(Set<MRVMLicense> bundle);

    @Override
    public BigDecimal calculateValue(Bundle bundle) {
        if (bundle.getBundleEntries().isEmpty()) {
            return BigDecimal.ZERO;
        }
        //TODO: Change this very naive approach to a faster one, where generics don't have to be transformed into bundles
        Set<MRVMLicense> licenses = bundle.getBundleEntries().stream().filter(be -> be.getGood() instanceof MRVMLicense && be.getAmount() == 1).map(be -> (MRVMLicense) be.getGood()).collect(Collectors.toSet());
        Set<BundleEntry> genericBundleEntries = bundle.getBundleEntries().stream().filter(be -> be.getGood() instanceof MRVMGenericDefinition).collect(Collectors.toSet());
        Preconditions.checkArgument(licenses.size() + genericBundleEntries.size() == bundle.getBundleEntries().size(), "Bundle contains other goods than MRVMLicenses or MRVMGenericDefinitions");
        for (BundleEntry entry : genericBundleEntries) {
            MRVMGenericDefinition def = (MRVMGenericDefinition) entry.getGood();
            List<MRVMLicense> containedLicenses = def.containedGoods();
            int required = entry.getAmount();
            int alreadyThere = (int) licenses.stream().filter(containedLicenses::contains).count();
            int index = 0;
            while (alreadyThere < required && index < def.getQuantity()) {
                if (!licenses.contains(containedLicenses.get(index))) {
                    licenses.add(containedLicenses.get(index));
                    alreadyThere++;
                }
                index++;
            }
        }
        //Pre filters the map such that for regional calculations, only licenses for the according region are in the passed (sub-)bundles.
        //This is for speedup of the calculation, but has no effect on the outcome of the value.
        BigDecimal totalValue = BigDecimal.ZERO;
        Map<MRVMRegionsMap.Region, Set<MRVMLicense>> regionalBundles = MRVMWorld.getLicensesPerRegion(licenses);
        //For speedup of calculation of national bidders, pre-compute gamma Factors for all requions in advance
        Map<MRVMRegionsMap.Region, BigDecimal> gammaFactors = gammaFactors(licenses);
        //Calculate Regional Discounted Values and add them to total value
        for (Entry<MRVMRegionsMap.Region, Set<MRVMLicense>> regionalBundleEntry : regionalBundles.entrySet()) {
            BigDecimal c = MRVMWorld.c(regionalBundleEntry.getKey(), regionalBundleEntry.getValue());
            BigDecimal sv = svFunction(regionalBundleEntry.getKey(), c);
            BigDecimal regionalValue = omegaFactor(regionalBundleEntry.getKey(), sv);
            //Gamma Factor requires complete bundle (for national bidder to calculate #uncovered regions)
            BigDecimal gammaFactor = gammaFactors.get(regionalBundleEntry.getKey());
            BigDecimal discountedRegionalValue = regionalValue.multiply(gammaFactor);
            totalValue = totalValue.add(discountedRegionalValue);
        }
        return totalValue;
    }


    @Override
    public MRVMWorld getWorld() {
        return this.world;
    }

    private void setWorld(MRVMWorld world) {
        this.world = world;
    }

    public BigDecimal getzLow(MRVMRegionsMap.Region region) {
        return zLow.get(region.getId());
    }

    public BigDecimal getzHigh(MRVMRegionsMap.Region region) {
        return zHigh.get(region.getId());
    }

    public BigDecimal getAlpha() {
        return alpha;
    }

    public BigDecimal getBeta(MRVMRegionsMap.Region region) {
        return beta.get(region.getId());
    }

    @Override
    public Set<Bundle> getBestBundles(Prices prices, int maxNumberOfBundles, boolean allowNegative) {
        MRVM_MIP mip = new MRVM_MIP(Sets.newHashSet(this));
        mip.setMipInstrumentation(getMipInstrumentation());
        mip.setPurpose(MipInstrumentation.MipPurpose.DEMAND_QUERY);

        double scalingFactor = mip.getBidderPartialMips().get(this).getScalingFactor();
        Variable priceVar = new Variable("p", VarType.DOUBLE, 0, MIP.MAX_VALUE);
        mip.addVariable(priceVar);
        mip.addObjectiveTerm(-1, priceVar);
        Constraint price = new Constraint(CompareType.EQ, 0);
        price.addTerm(-1, priceVar);
        for (MRVMGenericDefinition bandInRegion : getWorld().getAllGenericDefinitions()) {
            Variable xVariable = mip.getWorldPartialMip().getXVariable(this, bandInRegion.getRegion(), bandInRegion.getBand());
            price.addTerm(prices.getPrice(Bundle.of(bandInRegion)).getAmount().doubleValue() / scalingFactor, xVariable);
        }
        mip.addConstraint(price);
        
        mip.setEpsilon(DEFAULT_DEMAND_QUERY_EPSILON);
        mip.setTimeLimit(DEFAULT_DEMAND_QUERY_TIME_LIMIT);
        
        this.bidderTypeSpecificDemandQueryMIPAdjustments(mip);

        List<Allocation> optimalAllocations = mip.getBestAllocations(maxNumberOfBundles, allowNegative);

        Set<Bundle> result = optimalAllocations.stream()
                .map(allocation -> allocation.allocationOf(this).getBundle())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (result.isEmpty()) result.add(Bundle.EMPTY);
        return result;
    }
    
    protected void bidderTypeSpecificDemandQueryMIPAdjustments(MRVM_MIP mip) {
    	// Do nothing here
    }

    /**
     * @see SATSBidder#refreshReference(World)
     */
    @Override
    public void refreshReference(World world) {
        if (world instanceof MRVMWorld) {
            setWorld((MRVMWorld) world);
        } else {
            throw new IncompatibleWorldException("Wrong world class");
        }

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
            return clazz.cast(RandomOrderXORQFactory.getXORQRandomOrderSimpleLang(this, rngSupplier));
        } else {
            throw new UnsupportedBiddingLanguageException();
        }
    }
}
