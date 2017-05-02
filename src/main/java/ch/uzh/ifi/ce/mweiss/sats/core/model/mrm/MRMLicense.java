/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.uzh.ifi.ce.mweiss.sats.core.model.mrm;

import java.util.ArrayList;
import java.util.List;

import ch.uzh.ifi.ce.mweiss.sats.core.model.Good;
import ch.uzh.ifi.ce.mweiss.sats.core.model.IncompatibleWorldException;
import ch.uzh.ifi.ce.mweiss.sats.core.model.World;

/**
 * @author Michael Weiss
 *
 */
public class MRMLicense extends Good {
    
    private static final long serialVersionUID = 2814831255330638720L;
    
    private final String bandName;
    private transient MRMBand band;
    
    private transient MRMWorld world;
    
    private final int regionId;
    private transient MRMRegionsMap.Region region;



    public static List<MRMLicense> createLicenses(MRMBand band, int startId, MRMRegionsMap regionsMap){
        List<MRMLicense> licenses = new ArrayList<>();
        for(int i = 0; i < band.getNumberOfLots(); i++){
            for (MRMRegionsMap.Region region : regionsMap.getRegions()){
                MRMLicense license = new MRMLicense(startId++, band, region);
                licenses.add(license);
            }
        }
        return licenses;
    }
    
    private MRMLicense(long id,  MRMBand band, MRMRegionsMap.Region region) {
        super(id, band.getWorldId());
        this.band = band;
        this.bandName = band.getName();
        this.world = band.getWorld();
        this.regionId = region.getId();
        this.region = region;
    }

     

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((bandName == null) ? 0 : bandName.hashCode());
        result = prime * result + regionId;
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
        MRMLicense other = (MRMLicense) obj;
        if (bandName == null) {
            if (other.bandName != null)
                return false;
        } else if (!bandName.equals(other.bandName))
            return false;
        if (regionId != other.regionId)
            return false;
        return true;
    }

    /* (non-Javadoc)
     * @see Good#getWorld()
     */
    @Override
    public MRMWorld getWorld() {
        return world;
    }

    
    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public String getBandName() {
        return bandName;
    }

    public MRMBand getBand() {
        return band;
    }

    @Override
    public long getWorldId() {
        return worldId;
    }

    public int getRegionId() {
        return regionId;
    }

    public MRMRegionsMap.Region getRegion() {
        return region;
    }

    /**
     * Must only be called by {@link #refreshFieldBackReferences(World)}.
     * Explicit definition of private setter to prevent from generating setter by accident.
     */
    private void setBand(MRMBand band){
        if(! getBandName().equals(band.getName())){
            throw new IncompatibleWorldException("The stored worldId does not represent the passed world reference");
        }
        this.band = band;
    }

    /**
     * Method is called after deserialization, there is not need to call it on any other occasion.<br>
     * See {@link World#refreshFieldBackReferences()} for explanations.
     * @param bmBand
     */
    public void refreshFieldBackReferences(MRMBand band) {
        setBand(band);
        this.world = band.getWorld();
        this.region = world.getRegionsMap().getRegion(regionId);        
    }
    
    

}
