/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model.mrvm;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.spectrumauctions.sats.core.bidlang.generic.GenericDefinition;
import org.spectrumauctions.sats.core.model.Good;

import java.util.Set;

/**
 * @author Michael Weiss
 *
 */
public final class MRVMGenericDefinition implements GenericDefinition {

    private final MRVMBand band;
    private final MRVMRegionsMap.Region region;

    private transient ImmutableSet<Good> licenses;


    /**
     * @param band
     * @param region
     * @param quantity
     */
    public MRVMGenericDefinition(MRVMBand band, MRVMRegionsMap.Region region) {
        super();
        Preconditions.checkNotNull(band);
        Preconditions.checkNotNull(region);
        Preconditions.checkArgument(band.getWorld().getRegionsMap().getRegions().contains(region));

        this.band = band;
        this.region = region;
    }


    public MRVMBand getBand() {
        return band;
    }

    public MRVMRegionsMap.Region getRegion() {
        return region;
    }

    @Override
    public String toString() {
        return new StringBuilder("[r=")
                .append(region.toString())
                .append(",b=")
                .append(band.toString())
                .append("]")
                .toString();
    }

    /* (non-Javadoc)
     * @see GenericDefinition#isPartOf(Good)
     */
    @Override
    public boolean isPartOf(Good license) {
        if (license == null) {
            return false;
        } else if (!(license instanceof MRVMLicense)) {
            return false;
        } else {
            MRVMLicense l = (MRVMLicense) license;
            return l.getBand().equals(band) && l.getRegion().equals(region);
        }
    }


    /**
     *
     * @throws IllegalArgumentException if the quantity is negative or exceeds the number of lots in this band.
     */
    public void checkQuantityIsValid(int quantity) {
        Preconditions.checkArgument(quantity >= 0);
        Preconditions.checkArgument(quantity <= band.getNumberOfLots());
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((band == null) ? 0 : band.hashCode());
        result = prime * result + ((region == null) ? 0 : region.hashCode());
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
        MRVMGenericDefinition other = (MRVMGenericDefinition) obj;
        if (band == null) {
            if (other.band != null)
                return false;
        } else if (!band.equals(other.band))
            return false;
        if (region == null) {
            if (other.region != null)
                return false;
        } else if (!region.equals(other.region))
            return false;
        return true;
    }


    /* (non-Javadoc)
     * @see GenericDefinition#numberOfLicenses()
     */
    @Override
    public int numberOfLicenses() {
        return band.getNumberOfLots();
    }

    /* (non-Javadoc)
     * @see GenericDefinition#allLicenses()
     */
    @Override
    public Set<Good> allLicenses() {
        if (licenses == null) {
            ImmutableSet.Builder<Good> licBuilder = new ImmutableSet.Builder<>();
            for (MRVMLicense lic : band.getLicenses()) {
                if (lic.getRegion().equals(region)) {
                    licBuilder.add(lic);
                }
            }
            this.licenses = licBuilder.build();
        }
        return licenses;
    }

    /* (non-Javadoc)
     * @see GenericDefinition#shortJson()
     */
    @Override
    public JsonElement shortJson() {
        JsonObject json = new JsonObject();
        json.addProperty("band", band.getName());
        json.addProperty("region", region.getId());
        return json;
    }


}
