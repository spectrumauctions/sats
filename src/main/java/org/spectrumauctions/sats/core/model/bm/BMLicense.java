/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model.bm;

import org.spectrumauctions.sats.core.model.Good;
import org.spectrumauctions.sats.core.model.World;
import org.spectrumauctions.sats.core.model.IncompatibleWorldException;
import org.spectrumauctions.sats.core.util.random.RNGSupplier;

/**
 * @author Michael Weiss
 *
 */
public class BMLicense extends Good {

    private static final long serialVersionUID = -597579273878128574L;

    private transient BMBand band;
    private final String bandName;
    
    private transient BMWorld world;
    

    /**
     * Licenses are automatically created when a new {@link BMWorld} instance is created,
     * hence, the use of this constructor is not recommended.
     * @param world
     * @param name
     * @param numberOfLicenses
     * @param licenseCounter
     * @param rngSupplier
     */
    BMLicense(int id, BMBand band, RNGSupplier rngSupplier) {
        super(id, band.getWorldId());
        this.band = band;
        this.bandName = band.getName();
        this.world = band.getWorld();
    }


    public BMBand getBand() {
        return band;
    }


    private void setWorld(BMWorld world) {
        if(getWorldId() != world.getId()){
            throw new IncompatibleWorldException("The stored worldId does not represent the passed world reference");
        }
        this.world = world;
    }
    
    private void setBand(BMBand band) {
        if(! bandName.equals(band.getName()) || band.getWorldId() != getWorldId()){
            throw new IncompatibleWorldException("The stored worldId / bandName do not represent the passed band reference");
        }
        this.band = band;
    }


    /**
     * Method is called after deserialization, there is not need to call it on any other occasion.<br>
     * See {@link World#refreshFieldBackReferences()} for explanations.
     * @param bmBand
     */
    public void refreshFieldBackReferences(BMBand bmBand) {
        setWorld(bmBand.getWorld());
        setBand(bmBand);   
    }


    /* (non-Javadoc)
     * @see Good#getWorld()
     */
    @Override
    public BMWorld getWorld() {
        return world;
    }
    
    

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((bandName == null) ? 0 : bandName.hashCode());
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
        BMLicense other = (BMLicense) obj;
        if (bandName == null) {
            if (other.bandName != null)
                return false;
        } else if (!bandName.equals(other.bandName))
            return false;
        return true;
    }

    
    

}
