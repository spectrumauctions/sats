/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model.mrvm;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.EqualsAndHashCode;
import org.spectrumauctions.sats.core.model.GenericGood;

import java.util.List;

/**
 * @author Michael Weiss
 *
 */
@EqualsAndHashCode(callSuper = true)
public final class MRVMGenericDefinition extends GenericGood {

    private final MRVMBand band;
    private final MRVMRegionsMap.Region region;

    private transient ImmutableList<MRVMLicense> licenses;

    public MRVMGenericDefinition(MRVMBand band, MRVMRegionsMap.Region region) {
        super(band.getName(), band.getWorldId());
        Preconditions.checkNotNull(band);
        Preconditions.checkNotNull(region);
        Preconditions.checkArgument(band.getWorld().getRegionsMap().getRegions().contains(region));

        this.band = band;
        this.region = region;
    }

    public MRVMWorld getWorld() {
        return band.getWorld();
    }


    public MRVMBand getBand() {
        return band;
    }

    public MRVMRegionsMap.Region getRegion() {
        return region;
    }

    @Override
    public String toString() {
        return "[r=" +
                region.toString() +
                ",b=" +
                band.toString() +
                "]";
    }

    /**
     * @throws IllegalArgumentException if the quantity is negative or exceeds the number of lots in this band.
     */
    public void checkQuantityIsValid(int quantity) {
        Preconditions.checkArgument(quantity >= 0);
        Preconditions.checkArgument(quantity <= band.getNumberOfLots());
    }

    @Override
    public List<MRVMLicense> containedGoods() {
        if (licenses == null) {
            ImmutableList.Builder<MRVMLicense> licBuilder = new ImmutableList.Builder<>();
            for (MRVMLicense lic : band.containedGoods()) {
                if (lic.getRegion().equals(region)) {
                    licBuilder.add(lic);
                }
            }
            this.licenses = licBuilder.build();
        }
        return licenses;
    }

    @Override
    public JsonElement shortJson() {
        JsonObject json = new JsonObject();
        json.addProperty("band", band.getName());
        json.addProperty("region", region.getId());
        return json;
    }


}
