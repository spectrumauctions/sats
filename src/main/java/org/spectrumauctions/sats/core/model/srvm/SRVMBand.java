/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model.srvm;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.EqualsAndHashCode;
import org.spectrumauctions.sats.core.model.GenericGood;
import org.spectrumauctions.sats.core.util.random.RNGSupplier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

/**
 * @author Michael Weiss
 *
 */
@EqualsAndHashCode(callSuper = true)
public final class SRVMBand extends GenericGood {

    private static final long serialVersionUID = 8297467604786037769L;
    private final ImmutableList<SRVMLicense> licenses;

    private transient SRVMWorld world;

    //package-private 
    static List<SRVMBand> createBands(SRVMWorld world, SRVMWorldSetup setup, RNGSupplier rngSupplier) {
        List<SRVMBand> bands = new ArrayList<>();
        int startId = 0;
        for (Entry<String, Integer> bandDefinition : setup.defineBands(rngSupplier).entrySet()) {
            bands.add(new SRVMBand(bandDefinition.getKey(), world, bandDefinition.getValue(), startId));
            startId += bandDefinition.getValue();
        }
        Preconditions.checkArgument(bands.size() != 0, "WorldSetup has to define at least one band");
        return bands;
    }

    private SRVMBand(String name, SRVMWorld world, int numberOfLicenses, int startId) {
        super(name, world.getId());
        this.world = world;
        List<SRVMLicense> builder = new ArrayList<>();
        for (int i = 0; i < numberOfLicenses; i++) {
            builder.add(new SRVMLicense(startId++, this));
        }
        this.licenses = ImmutableList.copyOf(builder);
    }

    public SRVMWorld getWorld() {
        return world;
    }


    public List<SRVMLicense> getLicenses() {
        return Collections.unmodifiableList(licenses);
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
    public int available() {
        return licenses.size();
    }

    public boolean isPartOf(SRVMLicense license) {
        return license != null && license.getBand().equals(this);
    }

    @Override
    public List<SRVMLicense> containedGoods() {
        return Collections.unmodifiableList(licenses);
    }

    @Override
    public JsonElement shortJson() {
        JsonObject json = new JsonObject();
        json.addProperty("band", getId());
        return json;
    }


}
