/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model.bvm;

import lombok.EqualsAndHashCode;
import org.marketdesignresearch.mechlib.domain.Good;
import org.spectrumauctions.sats.core.model.GenericGood;
import org.spectrumauctions.sats.core.model.IncompatibleWorldException;
import org.spectrumauctions.sats.core.model.License;
import org.spectrumauctions.sats.core.model.World;
import org.spectrumauctions.sats.core.util.random.RNGSupplier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Michael Weiss
 *
 */
@EqualsAndHashCode(callSuper = true)
public class BMBand extends GenericGood {

    private static final long serialVersionUID = 1156082993361102068L;
    private final List<BMLicense> licenses;

    private transient BMWorld world;

    /**
     * Creates a new Band
     * Bands are automatically created when a new {@link BMWorld} instance is created,
     * hence, the use of this constructor is not recommended.
     */
    public BMBand(BMWorld world, String name, int numberOfLicenses, int licenseCounter, RNGSupplier rngSupplier) {
        super(name, world.getId());
        this.world = world;
        this.licenses = new ArrayList<>();
        for (int i = 0; i < numberOfLicenses; i++) {
            licenses.add(new BMLicense(licenseCounter++, this, rngSupplier));
        }
    }

    public BMWorld getWorld() {
        return world;
    }

    @Override
    public List<BMLicense> containedGoods() {
        return licenses;
    }

    @Override
    public int available() {
        return licenses.size();
    }

    /**
     * Must only be called by {@link World#refreshFieldBackReferences()}.
     * Explicit definition of private setter to prevent from generating setter by accident.
     */
    private void setWorld(BMWorld world) {
        if (getWorldId() != world.getId()) {
            throw new IncompatibleWorldException("The stored worldId does not represent the passed world reference");
        }
        this.world = world;
    }

    /**
     * Method is called after deserialization, there is not need to call it on any other occasion.<br>
     * See {@link World#refreshFieldBackReferences()} for explanations.
     * @param world the world this band belongs to
     */
    public void refreshFieldBackReferences(BMWorld world) {
        setWorld(world);
        for (BMLicense license : licenses) {
            license.refreshFieldBackReferences(this);
        }
    }

    public boolean isPartOf(BMLicense license) {
        return license != null && license.getBand().equals(this);
    }

}
