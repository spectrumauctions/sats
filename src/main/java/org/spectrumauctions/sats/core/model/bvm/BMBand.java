/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model.bvm;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.spectrumauctions.sats.core.bidlang.generic.Band;
import org.spectrumauctions.sats.core.bidlang.generic.GenericDefinition;
import org.spectrumauctions.sats.core.model.Good;
import org.spectrumauctions.sats.core.model.IncompatibleWorldException;
import org.spectrumauctions.sats.core.model.World;
import org.spectrumauctions.sats.core.util.random.RNGSupplier;

import java.io.Serializable;
import java.util.*;

/**
 * @author Michael Weiss
 *
 */
public class BMBand extends Band implements GenericDefinition, Serializable {

    private static final long serialVersionUID = 1156082993361102068L;
    private final List<BMLicense> licenses;
    private final long worldId;

    private transient BMWorld world;

    /**
     * Creates a new Band
     * Bands are automatically created when a new {@link BMWorld} instance is created,
     * hence, the use of this constructor is not recommended.
     */
    public BMBand(BMWorld world, String name, int numberOfLicenses, int licenseCounter, RNGSupplier rngSupplier) {
        super(name);
        this.world = world;
        this.worldId = world.getId();
        this.licenses = new ArrayList<>();
        for (int i = 0; i < numberOfLicenses; i++) {
            licenses.add(new BMLicense(licenseCounter++, this, rngSupplier));
        }
    }

    public BMWorld getWorld() {
        return world;
    }

    public Collection<BMLicense> getLicenses() {
        return Collections.unmodifiableCollection(licenses);
    }

    @Override
    public int getNumberOfLicenses() {
        return licenses.size();
    }


    public long getWorldId() {
        return worldId;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((licenses == null) ? 0 : licenses.hashCode());
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
        BMBand other = (BMBand) obj;
        if (licenses == null) {
            if (other.licenses != null)
                return false;
        } else if (!licenses.equals(other.licenses))
            return false;
        return true;
    }

    /**
     * @see GenericDefinition#isPartOf(Good)
     */
    @Override
    public boolean isPartOf(Good license) {
        if (license == null) {
            return false;
        } else if (!(license instanceof BMLicense)) {
            return false;
        }
        BMLicense bmLicense = (BMLicense) license;
        return bmLicense.getBand().equals(this);
    }

    /**
     * @see GenericDefinition#numberOfLicenses()
     */
    @Override
    public int numberOfLicenses() {
        return getNumberOfLicenses();
    }

    /**
     * @see GenericDefinition#allLicenses()
     */
    @Override
    public Set<Good> allLicenses() {
        return new HashSet<>(getLicenses());
    }

    /**
     * @see GenericDefinition#shortJson()
     */
    @Override
    public JsonElement shortJson() {
        JsonObject json = new JsonObject();
        json.addProperty("band", getName());
        return json;
    }


}
