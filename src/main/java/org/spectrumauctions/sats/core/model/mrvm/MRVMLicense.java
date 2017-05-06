/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model.mrvm;

import org.spectrumauctions.sats.core.model.Good;
import org.spectrumauctions.sats.core.model.IncompatibleWorldException;
import org.spectrumauctions.sats.core.model.World;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Weiss
 *
 */
public class MRVMLicense extends Good {

    private static final long serialVersionUID = 2814831255330638720L;

    private final String bandName;
    private transient MRVMBand band;

    private transient MRVMWorld world;

    private final int regionId;
    private transient MRVMRegionsMap.Region region;


    public static List<MRVMLicense> createLicenses(MRVMBand band, int startId, MRVMRegionsMap regionsMap) {
        List<MRVMLicense> licenses = new ArrayList<>();
        for (int i = 0; i < band.getNumberOfLots(); i++) {
            for (MRVMRegionsMap.Region region : regionsMap.getRegions()) {
                MRVMLicense license = new MRVMLicense(startId++, band, region);
                licenses.add(license);
            }
        }
        return licenses;
    }

    private MRVMLicense(long id, MRVMBand band, MRVMRegionsMap.Region region) {
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
        MRVMLicense other = (MRVMLicense) obj;
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
    public MRVMWorld getWorld() {
        return world;
    }


    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public String getBandName() {
        return bandName;
    }

    public MRVMBand getBand() {
        return band;
    }

    @Override
    public long getWorldId() {
        return worldId;
    }

    public int getRegionId() {
        return regionId;
    }

    public MRVMRegionsMap.Region getRegion() {
        return region;
    }

    /**
     * Must only be called by {@link #refreshFieldBackReferences(World)}.
     * Explicit definition of private setter to prevent from generating setter by accident.
     */
    private void setBand(MRVMBand band) {
        if (!getBandName().equals(band.getName())) {
            throw new IncompatibleWorldException("The stored worldId does not represent the passed world reference");
        }
        this.band = band;
    }

    /**
     * Method is called after deserialization, there is not need to call it on any other occasion.<br>
     * See {@link World#refreshFieldBackReferences()} for explanations.
     * @param bmBand
     */
    public void refreshFieldBackReferences(MRVMBand band) {
        setBand(band);
        this.world = band.getWorld();
        this.region = world.getRegionsMap().getRegion(regionId);
    }


}
