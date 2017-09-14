/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model.srvm;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.spectrumauctions.sats.core.bidlang.generic.Band;
import org.spectrumauctions.sats.core.bidlang.generic.GenericDefinition;
import org.spectrumauctions.sats.core.model.Good;
import org.spectrumauctions.sats.core.util.random.RNGSupplier;

import java.util.*;
import java.util.Map.Entry;

/**
 * @author Michael Weiss
 *
 */
public final class SRVMBand extends Band implements GenericDefinition {

    private static final long serialVersionUID = 8297467604786037769L;
    private final List<SRVMLicense> licenses;
    private final long worldId;

    private transient SRVMWorld world;

    //package-private 
    static Set<SRVMBand> createBands(SRVMWorld world, SRVMWorldSetup setup, RNGSupplier rngSupplier) {
        Set<SRVMBand> bands = new HashSet<>();
        int startId = 0;
        for (Entry<String, Integer> bandDefinition : setup.defineBands(rngSupplier).entrySet()) {
            bands.add(new SRVMBand(bandDefinition.getKey(), world, bandDefinition.getValue(), startId));
            startId += bandDefinition.getValue();
        }
        Preconditions.checkArgument(bands.size() != 0, "WorldSetup has to define at least one band");
        return bands;
    }

    private SRVMBand(String name, SRVMWorld world, int numberOfLicenses, int startId) {
        super(name);
        this.world = world;
        this.worldId = world.getId();
        List<SRVMLicense> builder = new ArrayList<>();
        for (int i = 0; i < numberOfLicenses; i++) {
            builder.add(new SRVMLicense(startId++, this));
        }
        this.licenses = Collections.unmodifiableList(builder);
    }

    public SRVMWorld getWorld() {
        return world;
    }


    public List<SRVMLicense> getLicenses() {
        return Collections.unmodifiableList(licenses);
    }


    @Override
    public String getName() {
        return name;
    }


    /**
     * See {@link SRVMWorld#refreshFieldBackReferences()} for purpose of this method
     */
    void refreshFieldBackReferences(SRVMWorld world) {
        Preconditions.checkArgument(world.getId() == this.worldId);
        this.world = world;
        for (SRVMLicense license : licenses) {
            license.refreshFieldBackReferences(this);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((licenses == null) ? 0 : licenses.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        SRVMBand other = (SRVMBand) obj;
        if (licenses == null) {
            if (other.licenses != null)
                return false;
        } else if (!licenses.equals(other.licenses))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

    /* (non-Javadoc)
     * @see Band#getNumberOfLicenses()
     */
    @Override
    public int getNumberOfLicenses() {
        return licenses.size();
    }

    /* (non-Javadoc)
     * @see GenericDefinition#isPartOf(Good)
     */
    @Override
    public boolean isPartOf(Good license) {
        if (license == null) {
            return false;
        } else if (!(license instanceof SRVMLicense)) {
            return false;
        }
        SRVMLicense SRVMLicense = (SRVMLicense) license;
        return SRVMLicense.getBand().equals(this);
    }

    /* (non-Javadoc)
     * @see GenericDefinition#numberOfLicenses()
     */
    @Override
    public int numberOfLicenses() {
        return licenses.size();
    }

    /* (non-Javadoc)
     * @see GenericDefinition#allLicenses()
     */
    @Override
    public Set<SRVMLicense> allLicenses() {
        return new HashSet<>(licenses);
    }


    /* (non-Javadoc)
     * @see GenericDefinition#shortJson()
     */
    @Override
    public JsonElement shortJson() {
        JsonObject json = new JsonObject();
        json.addProperty("band", getName());
        return json;
    }


}
