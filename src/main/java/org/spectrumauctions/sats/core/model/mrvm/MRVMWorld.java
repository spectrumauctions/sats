/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model.mrvm;

import com.google.common.base.Preconditions;
import org.spectrumauctions.sats.core.bidlang.generic.GenericDefinition;
import org.spectrumauctions.sats.core.model.*;
import org.spectrumauctions.sats.core.util.random.RNGSupplier;

import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;

/**
 * @author Michael Weiss
 *
 */
public final class MRVMWorld extends World implements GenericWorld {

    private static final int BIGDECIMAL_PRECISON = 10;

    private static final long serialVersionUID = 2189142937399997527L;

    public static final String MODEL_NAME = "Multi-Region Value Model";
    private final MRVMRegionsMap regionsMap;
    private final HashSet<MRVMBand> bands;

    private transient BigDecimal maximalRegionalCapacity = null;
    private transient Map<MRVMRegionsMap.Region, Map<MRVMBand, GenericDefinition<MRVMLicense>>> genericDefinitions = new HashMap<>();

    public MRVMWorld(MRVMWorldSetup worldSetup, RNGSupplier rngSupplier) {
        super(MODEL_NAME);
        regionsMap = new MRVMRegionsMap(worldSetup, rngSupplier);
        bands = MRVMBand.createBands(this, worldSetup, regionsMap, rngSupplier.getUniformDistributionRNG());
        for (MRVMRegionsMap.Region region : getRegionsMap().getRegions()) {
            genericDefinitions.put(region, new HashMap<>());
            for (MRVMBand band : getBands()) {
                genericDefinitions.get(region).put(band, new MRVMGenericDefinition(band, region));
            }
        }
        store();
    }

    /**
     * @see World#getNumberOfGoods()
     */
    @Override
    public int getNumberOfGoods() {
        int numberOfLicenses = 0;
        for (MRVMBand band : bands) {
            int numberOfRegions = regionsMap.getRegions().size();
            int numberOfLots = band.getNumberOfLots();
            numberOfLicenses += numberOfLots * numberOfRegions;
        }
        return numberOfLicenses;
    }

    public Set<MRVMBand> getBands() {
        return Collections.unmodifiableSet(bands);
    }

    /**
     * @see World#getLicenses()
     */
    @Override
    public Set<MRVMLicense> getLicenses() {
        Set<MRVMLicense> licenses = new HashSet<>();
        for (MRVMBand band : bands) {
            licenses.addAll(band.getLicenses());
        }
        return licenses;
    }

    /**
     * @see World#restorePopulation(long)
     */
    @Override
    public Collection<? extends Bidder<MRVMLicense>> restorePopulation(long populationId) {
        return super.restorePopulation(MRVMBidder.class, populationId);
    }

    /**
     * @see World#refreshFieldBackReferences()
     */
    @Override
    public void refreshFieldBackReferences() {
        for (MRVMBand band : bands) {
            band.refreshFieldBackReferences(this);
        }
    }


    public MRVMRegionsMap getRegionsMap() {
        return regionsMap;
    }


    /**
     * Sorts the licenses of a bundle into subbundles by their band.
     * The returned map contains all bands of the world as keys, even such which are not present with any licenses in the bundle.<br>
     * @param bundle Must be nonempty
     */
    public static Map<MRVMBand, Bundle<MRVMLicense>> getLicensesPerBand(Bundle<MRVMLicense> bundle) {
        Preconditions.checkArgument(!bundle.isEmpty());
        MRVMWorld world = bundle.iterator().next().getWorld();
        return getLicensesPerBand(bundle, world);
    }


    /**
     * Sorts the licenses of a bundle into subbundles by their band.
     * The returned map contains all bands of the world as keys, even such which are not present with any licenses in the bundle.<br>
     */
    public static Map<MRVMBand, Bundle<MRVMLicense>> getLicensesPerBand(Bundle<MRVMLicense> bundle, MRVMWorld world) {
        Map<MRVMBand, Bundle<MRVMLicense>> licensesPerBand = new HashMap<>();
        for (MRVMBand band : world.getBands()) {
            licensesPerBand.put(band, new Bundle<>());
        }
        for (MRVMLicense license : bundle) {
            licensesPerBand.get(license.getBand()).add(license);
        }
        return licensesPerBand;
    }

    /**
     * Counts the number of licenses for each band.
     * The returned map contains all bands of the world as keys, even such which are not present with any licenses in the bundle.<br>
     * @param bundle Must be nonempty
     */
    public static Map<MRVMBand, Integer> quantitiesPerBand(Bundle<MRVMLicense> bundle) {
        Preconditions.checkArgument(bundle.isEmpty()); // Ensure world to be defined
        return quantitiesPerBand(bundle, (MRVMWorld) bundle.getWorld());
    }

    /**
     * Counts the number of licenses for each band.
     * The returned map contains all bands of the world as keys, even such which are not present with any licenses in the bundle.<br>
     * @param bundle Must be nonempty
     */
    public static Map<MRVMBand, Integer> quantitiesPerBand(Bundle<MRVMLicense> bundle, MRVMWorld MRVMWorld) {
        Map<MRVMBand, Bundle<MRVMLicense>> licensesPerBand = getLicensesPerBand(bundle, MRVMWorld);
        Map<MRVMBand, Integer> quantities = new HashMap<>();
        for (MRVMBand band : MRVMWorld.getBands()) {
            try {
                quantities.put(band, licensesPerBand.get(band).size());
            } catch (NullPointerException e) {
                quantities.put(band, 0);
            }
        }
        return quantities;
    }

    /**
     * Defines the c-function, i.e., c(r,x) = sum_{b\in B} cap(b,r,x) [as explained in the paper]
     */
    public static BigDecimal c(MRVMRegionsMap.Region r, Bundle<MRVMLicense> bundle) {
        if (bundle.isEmpty()) {
            return BigDecimal.ZERO;
        }
        Bundle<MRVMLicense> regionalSubBundle = getLicensesPerRegion(bundle).get(r);
        Map<MRVMBand, Integer> bandQuantities = quantitiesPerBand(regionalSubBundle, (MRVMWorld) bundle.getWorld());
        BigDecimal cap = BigDecimal.ZERO;
        for (Entry<MRVMBand, Integer> bandQuantityEntry : bandQuantities.entrySet()) {
            if (bandQuantityEntry.getValue() != 0) {
                BigDecimal bandCap = capOfBand(bandQuantityEntry.getKey(), bandQuantityEntry.getValue());
                cap = cap.add(bandCap);
            }
        }
        return cap;
    }

    /**
     * Calculates the maximum capacity any region can have.
     * The result is cached, hence, calling the method multiple time is not costly.
     */
    public BigDecimal getMaximumRegionalCapacity() {
        if (maximalRegionalCapacity == null) {
            MRVMRegionsMap.Region anyRegion = regionsMap.getRegions().iterator().next();
            maximalRegionalCapacity = c(anyRegion, new Bundle<>(getLicenses()));
        }
        return maximalRegionalCapacity;
    }


    /**
     * Returns the capacity for having <i>numberOfLicenses</i> many {@link MRVMLicense} in {@link MRVMBand} <i>band</i>
     */
    public static BigDecimal capOfBand(MRVMBand band, int numberOfLicenses) {
        if (numberOfLicenses == 0) {
            return BigDecimal.ZERO;
        }
        Preconditions.checkArgument(numberOfLicenses >= 0);
        Preconditions.checkArgument(numberOfLicenses <= band.getNumberOfLots());
        BigDecimal quantity = new BigDecimal(numberOfLicenses);
        BigDecimal baseCapacity = band.getBaseCapacity();
        BigDecimal synergy = band.getSynergy(numberOfLicenses);
        BigDecimal bandCap = quantity.multiply(baseCapacity).multiply(synergy);
        return bandCap;
    }


    /**
     * Sorts the licenses of a bundle into subbundles by their region.<br>
     * @return map that contains all regions of the world as keys, even such which are not present with any licenses in the bundle.<br>
     */
    public static Map<MRVMRegionsMap.Region, Bundle<MRVMLicense>> getLicensesPerRegion(Bundle<MRVMLicense> bundle) {
        Preconditions.checkArgument(!bundle.isEmpty());
        MRVMWorld world = bundle.iterator().next().getWorld();
        Map<MRVMRegionsMap.Region, Bundle<MRVMLicense>> licensesPerRegion = new HashMap<>();
        for (MRVMRegionsMap.Region region : world.getRegionsMap().getRegions()) {
            licensesPerRegion.put(region, new Bundle<>());
        }
        for (MRVMLicense license : bundle) {
            licensesPerRegion.get(license.getRegion()).add(license);
        }
        return licensesPerRegion;
    }


    public List<MRVMBidder> createPopulation(MRVMLocalBidderSetup localSetup,
                                             MRVMRegionalBidderSetup regionalSetup,
                                             MRVMNationalBidderSetup nationalSetup,
                                             RNGSupplier rngSupplier) {
        Collection<MRVMLocalBidderSetup> localSetups = null;
        Collection<MRVMRegionalBidderSetup> regionalSetups = null;
        Collection<MRVMNationalBidderSetup> nationalSetups = null;
        if (localSetup != null) {
            localSetups = new HashSet<>();
            localSetups.add(localSetup);
        }
        if (regionalSetup != null) {
            regionalSetups = new HashSet<>();
            regionalSetups.add(regionalSetup);
        }
        if (nationalSetup != null) {
            nationalSetups = new HashSet<>();
            nationalSetups.add(nationalSetup);
        }
        return createPopulation(localSetups, regionalSetups, nationalSetups, rngSupplier);
    }


    public List<MRVMBidder> createPopulation(Collection<MRVMLocalBidderSetup> localSetups,
                                             Collection<MRVMRegionalBidderSetup> regionalSetups,
                                             Collection<MRVMNationalBidderSetup> nationalSetups,
                                             RNGSupplier rngSupplier) {
        long population = openNewPopulation();
        List<MRVMBidder> bidders = new ArrayList<>();
        int idCount = 0;
        if (localSetups != null) {
            for (MRVMLocalBidderSetup setup : localSetups) {
                for (int i = 0; i < setup.getNumberOfBidders(); i++) {
                    bidders.add(new MRVMLocalBidder(idCount++, population, this, setup, rngSupplier.getUniformDistributionRNG()));
                }
            }
        }
        if (regionalSetups != null) {
            for (MRVMRegionalBidderSetup setup : regionalSetups) {
                for (int i = 0; i < setup.getNumberOfBidders(); i++) {
                    bidders.add(new MRVMRegionalBidder(idCount++, population, this, setup, rngSupplier.getUniformDistributionRNG()));
                }
            }
        }
        if (nationalSetups != null) {
            for (MRVMNationalBidderSetup setup : nationalSetups) {
                for (int i = 0; i < setup.getNumberOfBidders(); i++) {
                    bidders.add(new MRVMNationalBidder(idCount++, population, this, setup, rngSupplier.getUniformDistributionRNG()));
                }
            }
        }
        Preconditions.checkArgument(bidders.size() > 0, "At least one bidder setup with a strictly positive number of bidders is required to generate population");
        return bidders;
    }

    @Override
    public Set<GenericDefinition<MRVMLicense>> getAllGenericDefinitions() {
        Set<GenericDefinition<MRVMLicense>> defs = new HashSet<>();
        for (MRVMRegionsMap.Region region : getRegionsMap().getRegions()) {
            defs.addAll(genericDefinitions.get(region).values());
        }
        return defs;
    }

    @Override
    public GenericDefinition getGenericDefinitionOf(Good license) {
        MRVMLicense mrvmLicense = (MRVMLicense) license;
        Preconditions.checkArgument(genericDefinitions.containsKey(mrvmLicense.getRegion()));
        Preconditions.checkArgument(genericDefinitions.get(mrvmLicense.getRegion()).containsKey(mrvmLicense.getBand()));
        return genericDefinitions.get(mrvmLicense.getRegion()).get(mrvmLicense.getBand());
    }
}
