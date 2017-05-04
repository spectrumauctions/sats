/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model.mrvm;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.spectrumauctions.sats.core.bidlang.generic.Band;
import org.spectrumauctions.sats.core.model.IncompatibleWorldException;
import org.spectrumauctions.sats.core.model.World;
import org.spectrumauctions.sats.core.util.random.UniformDistributionRNG;
import com.google.common.collect.ImmutableMap;

/**
 * @author Michael Weiss
 *
 */
public final class MRVMBand extends Band implements Serializable{

    private static final long serialVersionUID = -4482949789084377013L;
    private final long worldId;
    private final BigDecimal baseCapacity;
    private final int numberOfLots;

    private final List<MRVMLicense> licenses;
    private final Map<Integer, BigDecimal> synergies;
    
    private transient MRVMWorld world;

    public static Set<MRVMBand> createBands(MRVMWorld world, MRVMWorldSetup worldSetup, MRVMRegionsMap regionsMap, UniformDistributionRNG rng) {
        Set<MRVMWorldSetup.BandSetup> bandSetups = worldSetup.getBandSetups();
        Set<MRVMBand> bands = new HashSet<>();
        int currentLicenseId = 0;
        for(MRVMWorldSetup.BandSetup bandSetup : bandSetups){
            MRVMBand band = new MRVMBand(bandSetup, world, currentLicenseId, rng);
            currentLicenseId += band.getNumberOfLicenses();
            bands.add(band);
        }
        return bands;
    }


    private MRVMBand(MRVMWorldSetup.BandSetup bandSetup, MRVMWorld world, int licenseStartId, UniformDistributionRNG rng) {
        super(bandSetup.getName());
        this.world = world;
        this.numberOfLots = bandSetup.drawNumberOfLots(rng);
        this.worldId = world.getId();
        this.baseCapacity = bandSetup.drawBaseCapacity(rng);
        this.synergies = ImmutableMap.copyOf(bandSetup.getSynergies());
        this.licenses = MRVMLicense.createLicenses(this, licenseStartId, world.getRegionsMap());
    
    }

    /**
     * Gives the synergy factor for having a specific number of licenses of this band in the same region.<br>
     * If no synergy factor is explicity stored for a specific quantity, for the next lower quantity with known synergy is returned.<br>
     * The synergy for quantity 1 is always 1;
     * @param quantity
     * @return
     */
    public BigDecimal getSynergy(int quantity){
        if(quantity < 0 || quantity > numberOfLots){
            throw new IllegalArgumentException("Immpossible quantity");
        }else if(quantity <= 1){
            //If quantity is 0 or 1, return synergy one 
            //(note that synergy for quantity = 0 is without effect, as it will be multiplied with 0 in the val calc)
            return BigDecimal.ONE;
        }else{
            BigDecimal synergy = synergies.get(quantity);
            if(synergy == null){
                return getSynergy(quantity -1);
            }else{
                return synergy;
            }
        }
    }

    public BigDecimal calculateCAP(int quantity){
        return MRVMWorld.capOfBand(this, quantity);
    }
    
    public BigDecimal getBaseCapacity() {
        return baseCapacity;
    }

    public MRVMWorld getWorld() {
        return world;
    }


    public Collection<MRVMLicense> getLicenses() {
        return Collections.unmodifiableCollection(licenses);
    }

    public int getNumberOfLots(){
        return numberOfLots;
    }
    
    @Override
    public int getNumberOfLicenses() {
        return licenses.size();
    }

    
    public long getWorldId() {
        return worldId;
    }

    /**
     * Must only be called by {@link MRVMWorld#refreshFieldBackReferences()}.
     * Explicit definition of private setter to prevent from generating setter by accident.
     */
    private void setWorld(MRVMWorld world){
        if(getWorldId() != world.getId()){
            throw new IncompatibleWorldException("The stored worldId does not represent the passed world reference");
        }
        this.world = world;
    }


    /**
     * Method is called after deserialization, there is not need to call it on any other occasion.<br>
     * See {@link World#refreshFieldBackReferences()} for explanations.
     * @param bmBand
     */
    public void refreshFieldBackReferences(MRVMWorld world) {
        setWorld(world);
        for(MRVMLicense license : licenses){
            license.refreshFieldBackReferences(this);
        }    
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((baseCapacity == null) ? 0 : baseCapacity.hashCode());
        result = prime * result + ((licenses == null) ? 0 : licenses.hashCode());
        result = prime * result + numberOfLots;
        result = prime * result + ((synergies == null) ? 0 : synergies.hashCode());
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MRVMBand other = (MRVMBand) obj;
        if (baseCapacity == null) {
            if (other.baseCapacity != null)
                return false;
        } else if (!baseCapacity.equals(other.baseCapacity))
            return false;
        if (licenses == null) {
            if (other.licenses != null)
                return false;
        } else if (!licenses.equals(other.licenses))
            return false;
        if (numberOfLots != other.numberOfLots)
            return false;
        if (synergies == null) {
            if (other.synergies != null)
                return false;
        } else if (!synergies.equals(other.synergies))
            return false;
        return true;
    }



    
    
    
}
