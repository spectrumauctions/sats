/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.uzh.ifi.ce.mweiss.sats.core.model.mrm;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ch.uzh.ifi.ce.mweiss.sats.core.model.Bidder;
import ch.uzh.ifi.ce.mweiss.sats.core.model.World;
import com.google.common.base.Preconditions;

import ch.uzh.ifi.ce.mweiss.sats.core.model.Bundle;
import ch.uzh.ifi.ce.mweiss.sats.core.model.mrm.MRMRegionsMap.Region;
import ch.uzh.ifi.ce.mweiss.sats.core.util.random.RNGSupplier;

/**
 * @author Michael Weiss
 *
 */
public class MRMWorld extends World {

    private static final int BIGDECIMAL_PRECISON = 10;
    
    private static final long serialVersionUID = 2189142937399997527L;
    
    public static final String MODEL_NAME = "Multi-Region Value Model";
    private final MRMRegionsMap regionsMap;
    private final Set<MRMBand> bands;
    
    private transient BigDecimal maximalRegionalCapacity = null;


    public MRMWorld(MRMWorldSetup worldSetup, RNGSupplier rngSupplier) {
        super(MODEL_NAME);
        regionsMap = new MRMRegionsMap(worldSetup, rngSupplier);
        bands = Collections.unmodifiableSet(MRMBand.createBands(this, worldSetup, regionsMap, rngSupplier.getUniformDistributionRNG()));
        store();
    }


    /* (non-Javadoc)
     * @see World#getNumberOfGoods()
     */
    @Override
    public int getNumberOfGoods() {
        int numberOfLicenses = 0;
        for(MRMBand band : bands){
            int numberOfRegions = regionsMap.getRegions().size();
            int numberOfLots = band.getNumberOfLots();
            numberOfLicenses += numberOfLots*numberOfRegions;
        }
        return numberOfLicenses;
    }

    public Set<MRMBand> getBands(){
        return Collections.unmodifiableSet(bands);
    }
    
    /* (non-Javadoc)
     * @see World#getLicenses()
     */
    @Override
    public Set<MRMLicense> getLicenses() {
        Set<MRMLicense> licenses = new HashSet<>();
        for(MRMBand band : bands){
            licenses.addAll(band.getLicenses());
        }
        return licenses;
    }

    /* (non-Javadoc)
     * @see World#restorePopulation(long)
     */
    @Override
    public Collection<? extends Bidder<MRMLicense>> restorePopulation(long populationId) {
        return super.restorePopulation(MRMBidder.class, populationId);
    }

    /* (non-Javadoc)
     * @see World#refreshFieldBackReferences()
     */
    @Override
    public void refreshFieldBackReferences() {
        for(MRMBand band : bands){
            band.refreshFieldBackReferences(this);
        }
    }


    /**
     * @return
     */
    public MRMRegionsMap getRegionsMap() {
        return regionsMap;
    }

    
    /**
     * Sorts the licenses of a bundle into subbundles by their band.
     * The returned map contains all bands of the world as keys, even such which are not present with any licenses in the bundle.<br>
     * @param bundle Must be nonempty
     * @return
     */
    public static Map<MRMBand, Bundle<MRMLicense>> getLicensesPerBand(Bundle<MRMLicense> bundle){
        Preconditions.checkArgument(!bundle.isEmpty());
        MRMWorld world = bundle.iterator().next().getWorld();
        return getLicensesPerBand(bundle, world);
    }


    /**
     * Sorts the licenses of a bundle into subbundles by their band.
     * The returned map contains all bands of the world as keys, even such which are not present with any licenses in the bundle.<br>
     * @return
     */
    public static Map<MRMBand, Bundle<MRMLicense>> getLicensesPerBand(Bundle<MRMLicense> bundle, MRMWorld world){
        Map<MRMBand, Bundle<MRMLicense>> licensesPerBand = new HashMap<>();
        for(MRMBand band : world.getBands()){
            licensesPerBand.put(band, new Bundle<MRMLicense>());
        }
        for(MRMLicense license : bundle ){
            licensesPerBand.get(license.getBand()).add(license);
        }
        return licensesPerBand;
    }
    
    /**
     * Counts the number of licenses for each band.
     * The returned map contains all bands of the world as keys, even such which are not present with any licenses in the bundle.<br>
     * @param bundle Must be nonempty
     * @return
     */
    public static Map<MRMBand, Integer> quantitiesPerBand(Bundle<MRMLicense> bundle){
        Preconditions.checkArgument(bundle.isEmpty()); // Ensure world to be defined
        return quantitiesPerBand(bundle, (MRMWorld) bundle.getWorld());
    }

    /**
     * Counts the number of licenses for each band.
     * The returned map contains all bands of the world as keys, even such which are not present with any licenses in the bundle.<br>
     * @param bundle Must be nonempty
     * @return
     */
    public static Map<MRMBand, Integer> quantitiesPerBand(Bundle<MRMLicense> bundle, MRMWorld mrmWorld){
        Map<MRMBand, Bundle<MRMLicense>> licensesPerBand = getLicensesPerBand(bundle, mrmWorld);
        Map<MRMBand, Integer> quantities = new HashMap<>();
        for(MRMBand band : mrmWorld.getBands()){
            try{
                quantities.put(band, licensesPerBand.get(band).size());
            }catch (NullPointerException e){
                quantities.put(band, 0);
            }
        }
        return quantities;
    }

    /**
     * Defines the c-function, i.e., c(r,x) = sum_{b\in B} cap(b,r,x) [as explained in the paper]
     * @param r
     * @param bundle
     * @return
     */
    public static BigDecimal c(Region r, Bundle<MRMLicense> bundle){
        if(bundle.isEmpty()){
            return BigDecimal.ZERO;
        }
        Bundle<MRMLicense> regionalSubBundle = getLicensesPerRegion(bundle).get(r);
        Map<MRMBand, Integer> bandQuantities = quantitiesPerBand(regionalSubBundle, (MRMWorld) bundle.getWorld());
        BigDecimal cap = BigDecimal.ZERO;
        for(Entry<MRMBand, Integer> bandQuantityEntry : bandQuantities.entrySet()){
            if(bandQuantityEntry.getValue() != 0){
                BigDecimal bandCap = capOfBand(bandQuantityEntry.getKey(), bandQuantityEntry.getValue());
                cap = cap.add(bandCap);
            }       
        }
        return cap;
    }
    
    /**
     * Calculates the maximum capacity any region can have.
     * The result is cached, hence, calling the method multiple time is not costly.
     * @return
     */
    public BigDecimal getMaximumRegionalCapacity() {
        if(maximalRegionalCapacity == null){
            Region anyRegion = regionsMap.getRegions().iterator().next();
            maximalRegionalCapacity = c(anyRegion, new Bundle<>(getLicenses()));
        }
        return maximalRegionalCapacity;
    }
    
    
    /**
     * Returns the capacity for having <i>numberOfLicenses</i> many {@link MRMLicense} in {@link MRMBand} <i>band</i>
     * @param band
     * @param numberOfLicenses
     * @return
     */
    public static BigDecimal capOfBand(MRMBand band, int numberOfLicenses){
        if(numberOfLicenses == 0){
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
     * The returned map contains all regions of the world as keys, even such which are not present with any licenses in the bundle.<br>
     * @param bundle 
     * @return
     */
    public static Map<Region, Bundle<MRMLicense>> getLicensesPerRegion(Bundle<MRMLicense> bundle){
        Preconditions.checkArgument(!bundle.isEmpty());
        MRMWorld world = bundle.iterator().next().getWorld();
        Map<MRMRegionsMap.Region, Bundle<MRMLicense>> licensesPerRegion = new HashMap<>();
        for(Region region : world.getRegionsMap().getRegions()){
            licensesPerRegion.put(region, new Bundle<MRMLicense>());
        }
        for(MRMLicense license : bundle ){
            licensesPerRegion.get(license.getRegion()).add(license);
        }
        return licensesPerRegion;
    }
    
    
    public List<MRMBidder> createPopulation(MRMLocalBidderSetup localSetup, 
            MRMRegionalBidderSetup regionalSetup, 
            MRMGlobalBidderSetup globalSetup, 
            RNGSupplier rngSupplier){
       Collection<MRMLocalBidderSetup> localSetups = null;
       Collection<MRMRegionalBidderSetup> regionalSetups = null;
       Collection<MRMGlobalBidderSetup> globalSetups = null;  
       if(localSetup != null){
           localSetups = new HashSet<>();
           localSetups.add(localSetup);
       }
       if(regionalSetup != null){
           regionalSetups = new HashSet<>();
           regionalSetups.add(regionalSetup);
       }
       if(globalSetup != null){
           globalSetups = new HashSet<>();
           globalSetups.add(globalSetup);
       }
       return createPopulation(localSetups, regionalSetups, globalSetups, rngSupplier);        
    }
        
        
    public List<MRMBidder> createPopulation(Collection<MRMLocalBidderSetup> localSetups, 
            Collection<MRMRegionalBidderSetup> regionalSetups, 
            Collection<MRMGlobalBidderSetup> globalSetups, 
            RNGSupplier rngSupplier){
        long population = openNewPopulation();
        List<MRMBidder> bidders = new ArrayList<>();
        int idCount = 0;
        if(localSetups != null){
            for(MRMLocalBidderSetup setup : localSetups){
                for(int i = 0; i < setup.getNumberOfBidders(); i++){
                    bidders.add(new MRMLocalBidder(idCount++, population, this, setup, rngSupplier.getUniformDistributionRNG()));
                }
            }
        }
        if(regionalSetups != null){
            for(MRMRegionalBidderSetup setup : regionalSetups){
                for(int i = 0; i < setup.getNumberOfBidders(); i++){
                    bidders.add(new MRMRegionalBidder(idCount++, population, this, setup, rngSupplier.getUniformDistributionRNG()));
                }
            }
        }
        if(globalSetups != null){
            for(MRMGlobalBidderSetup setup : globalSetups){
                for(int i = 0; i < setup.getNumberOfBidders(); i++){
                    bidders.add(new MRMGlobalBidder(idCount++, population, this, setup, rngSupplier.getUniformDistributionRNG()));
                }
            }
        }
        Preconditions.checkArgument(bidders.size() > 0, "At least one bidder setup with a strictly positive number of bidders is required to generate population");
        return bidders;
    }
   
}
