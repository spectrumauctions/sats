/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model.bm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.spectrumauctions.sats.core.model.World;
import org.spectrumauctions.sats.core.util.PreconditionUtils;
import com.google.common.base.Preconditions;

import org.spectrumauctions.sats.core.util.instancehandling.InstanceHandler;
import org.spectrumauctions.sats.core.util.random.JavaUtilRNGSupplier;
import org.spectrumauctions.sats.core.util.random.RNGSupplier;

/**
 * @author Michael Weiss
 *
 */
public class BMWorld extends World {

    public static final String MODEL_NAME = "Base and MultiBand Value Model";
    private final List<BMBand> bands;
    
    public BMWorld(BMWorldSetup setup, RNGSupplier rngSupplier){
        super(MODEL_NAME); 
        PreconditionUtils.checkNotNull(setup, rngSupplier);
        bands = createBands(setup, rngSupplier);
        store();
    }
    
    
    private List<BMBand> createBands(BMWorldSetup setup, RNGSupplier rngSupplier){
        List<BMBand> bands = new ArrayList<BMBand>();
        int licenseCount = 0;
        for (Entry<String, Integer> entry : setup.bands().entrySet()) {
            bands.add(new BMBand(this, entry.getKey(), entry.getValue(), licenseCount, rngSupplier));
            licenseCount += entry.getValue();
        }
        return bands;
    }
    
    /**
     * Gets a band with a specific name
     * 
     * @param bandName
     * @return band with this name, null if no such band in this world
     */
    public BMBand getBand(String bandName) {
        for (BMBand band : bands) {
            if (band.getName().equals(bandName))
                return band;
        }
        return null;
    }

    /**
     * Returns all bands
     * 
     * @return
     */
    public List<BMBand> getBands() {
        return Collections.unmodifiableList(bands);
    }

    @Override
    public Set<BMLicense> getLicenses() {
        Set<BMLicense> licenses = new HashSet<BMLicense>();
        for (BMBand band : bands) {
            licenses.addAll(band.getLicenses());
        }
        return licenses;
    }
    
    @Override
    public int getNumberOfGoods() {
        return getLicenses().size();
    }

    /**
     * 
     * Creates a new population, i.e., a set of bidders, according to the specified bidderSetup
     * @param bidderSetups
     * @return
     */
    public List<BMBidder> createPopulation(BMBidderSetup bidderSetup) {
        return createPopulation(bidderSetup, new JavaUtilRNGSupplier());
    }
    
    /**
     * See {@link #createPopulation(BMBidderSetup)} for JavaDoc.
     * @param bidderSetup
     * @param seed A seed used to generate random numbers for the bidders random parameters
     * @return
     */
    public List<BMBidder> createPopulation(BMBidderSetup bidderSetup, long seed) {
        return createPopulation(bidderSetup, new JavaUtilRNGSupplier(seed));
    }
    
    /**
     * See {@link #createPopulation(BMBidderSetup)} for JavaDoc.
     * @param bidderSetup
     * @param rngSupplier A rng supplier used to generate random numbers for the bidders random parameters
     * @return
     */
    public List<BMBidder> createPopulation(BMBidderSetup bidderSetup, RNGSupplier rngSupplier){
        Preconditions.checkNotNull(bidderSetup);
        Set<BMBidderSetup> setups = new HashSet<>();
        setups.add(bidderSetup);
        return createPopulation(setups, rngSupplier);
    }
    
    /**
     * 
     * Creates a new population, i.e., a set of bidders, according to the specified bidderSetups
     * @param bidderSetups
     * @return
     */
    public List<BMBidder> createPopulation(Collection<BMBidderSetup> bidderSetups){
        Preconditions.checkNotNull(bidderSetups);
        return createPopulation(bidderSetups, new JavaUtilRNGSupplier());
    }
    
    /**
     * See {@link #createPopulation(Collection)} for JavaDoc.
     * @param bidderSetups
     * @param seed A seed used to generate random numbers for the bidders random parameters
     * @return
     */
    public List<BMBidder> createPopulation(Collection<BMBidderSetup> bidderSetups, long seed){
        return createPopulation(bidderSetups, new JavaUtilRNGSupplier(seed));
    }
    
    /**
     * See {@link #createPopulation(Collection)} for JavaDoc.
     * @param bidderSetups
     * @param rngSupplier A rng supplier used to generate the bidders random parameters
     * @return
     */
    public List<BMBidder> createPopulation(Collection<BMBidderSetup> bidderSetups, RNGSupplier rngSupplier){
        long population = openNewPopulation();
        List<BMBidder> bidders = new ArrayList<>();
        int idCount = 0;
        for(BMBidderSetup setup : bidderSetups){
            for(int i = 0; i < setup.getNumberOfBidders(); i++){
                bidders.add(new BMBidder(population, idCount++, this, setup, rngSupplier.getUniformDistributionRNG()));
            }
        }
        return bidders;
    }
    
    public static BMWorld readWorld(long worldId){
        return InstanceHandler.getDefaultHandler().readWorld(BMWorld.class, worldId);
    }


    /* (non-Javadoc)
     * @see World#refreshFieldBackReferences()
     */
    @Override
    public void refreshFieldBackReferences() {
        for(BMBand band : bands){
            band.refreshFieldBackReferences(this);
        }
    }


    /* (non-Javadoc)
     * @see World#restorePopulation(long)
     */
    @Override
    public Collection<BMBidder> restorePopulation(long populationId) {
        return super.restorePopulation(BMBidder.class, populationId);
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((bands == null) ? 0 : bands.hashCode());
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
        BMWorld other = (BMWorld) obj;
        if (bands == null) {
            if (other.bands != null)
                return false;
        } else if (!bands.equals(other.bands))
            return false;
        return true;
    }



}
