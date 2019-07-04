/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model.bvm;

import lombok.EqualsAndHashCode;
import org.spectrumauctions.sats.core.model.License;
import org.spectrumauctions.sats.core.model.IncompatibleWorldException;
import org.spectrumauctions.sats.core.model.World;
import org.spectrumauctions.sats.core.util.random.RNGSupplier;

/**
 * @author Michael Weiss
 *
 */
@EqualsAndHashCode(callSuper = true)
public class BMLicense extends License {

    private static final long serialVersionUID = -597579273878128574L;

    private transient BMBand band;
    private final String bandName;

    private transient BMWorld world;


    /**
     * Licenses are automatically created when a new {@link BMWorld} instance is created,
     * hence, the use of this constructor is not recommended.
     */
    BMLicense(int id, BMBand band, RNGSupplier rngSupplier) {
        super(id, band.getWorldId());
        this.band = band;
        this.bandName = band.getId();
        this.world = band.getWorld();
    }


    public BMBand getBand() {
        return band;
    }


    private void setWorld(BMWorld world) {
        if (getWorldId() != world.getId()) {
            throw new IncompatibleWorldException("The stored worldId does not represent the passed world reference");
        }
        this.world = world;
    }

    private void setBand(BMBand band) {
        if (!bandName.equals(band.getId()) || band.getWorldId() != getWorldId()) {
            throw new IncompatibleWorldException("The stored worldId / bandName do not represent the passed band reference");
        }
        this.band = band;
    }


    /**
     * Method is called after deserialization, there is not need to call it on any other occasion.<br>
     * See {@link World#refreshFieldBackReferences()} for explanations.
     * @param bmBand the band this license belongs to
     */
    public void refreshFieldBackReferences(BMBand bmBand) {
        setWorld(bmBand.getWorld());
        setBand(bmBand);
    }


    /* (non-Javadoc)
     * @see License#getWorld()
     */
    @Override
    public BMWorld getWorld() {
        return world;
    }


}
