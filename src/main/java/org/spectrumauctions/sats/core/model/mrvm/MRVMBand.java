/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model.mrvm;

import com.google.common.collect.ImmutableMap;
import lombok.EqualsAndHashCode;
import org.spectrumauctions.sats.core.model.GenericGood;
import org.spectrumauctions.sats.core.model.IncompatibleWorldException;
import org.spectrumauctions.sats.core.model.World;
import org.spectrumauctions.sats.core.util.random.UniformDistributionRNG;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author Michael Weiss
 *
 */
@EqualsAndHashCode(callSuper = true)
public final class MRVMBand extends GenericGood {

    private static final long serialVersionUID = -4482949789084377013L;
    private final BigDecimal baseCapacity;
    private final int numberOfLots;

    private final List<MRVMLicense> licenses;
    private final Map<Integer, BigDecimal> synergies;

    private transient MRVMWorld world;

    public static HashSet<MRVMBand> createBands(MRVMWorld world, MRVMWorldSetup worldSetup, MRVMRegionsMap regionsMap, UniformDistributionRNG rng) {
        Set<MRVMWorldSetup.BandSetup> bandSetups = worldSetup.getBandSetups();
        HashSet<MRVMBand> bands = new HashSet<>();
        int currentLicenseId = 0;
        for (MRVMWorldSetup.BandSetup bandSetup : bandSetups) {
            MRVMBand band = new MRVMBand(bandSetup, world, currentLicenseId, rng);
            currentLicenseId += band.available();
            bands.add(band);
        }
        return bands;
    }


    private MRVMBand(MRVMWorldSetup.BandSetup bandSetup, MRVMWorld world, int licenseStartId, UniformDistributionRNG rng) {
        super(bandSetup.getName(), world.getId());
        this.world = world;
        this.numberOfLots = bandSetup.drawNumberOfLots(rng);
        this.baseCapacity = bandSetup.drawBaseCapacity(rng);
        this.synergies = ImmutableMap.copyOf(bandSetup.getSynergies());
        this.licenses = MRVMLicense.createLicenses(this, licenseStartId, world.getRegionsMap());

    }

    /**
     * @param quantity number of licenses in this band in the same region
     * @return the synergy factor for having a specific number of licenses of this band in the same region.<br>
     * If no synergy factor is explicitly stored for a specific quantity, for the next lower quantity with known synergy is returned.<br>
     * The synergy for quantity 1 is always 1;
     */
    public BigDecimal getSynergy(int quantity) {
        if (quantity < 0 || quantity > numberOfLots) {
            throw new IllegalArgumentException("Immpossible quantity");
        } else if (quantity <= 1) {
            //If quantity is 0 or 1, return synergy one 
            //(note that synergy for quantity = 0 is without effect, as it will be multiplied with 0 in the val calc)
            return BigDecimal.ONE;
        } else {
            BigDecimal synergy = synergies.get(quantity);
            if (synergy == null) {
                return getSynergy(quantity - 1);
            } else {
                return synergy;
            }
        }
    }

    public BigDecimal calculateCAP(int quantity) {
        return MRVMWorld.capOfBand(this, quantity);
    }

    public BigDecimal getBaseCapacity() {
        return baseCapacity;
    }

    public MRVMWorld getWorld() {
        return world;
    }


    @Override
    public List<MRVMLicense> containedGoods() {
        return Collections.unmodifiableList(licenses);
    }

    public int getNumberOfLots() {
        return numberOfLots;
    }

    @Override
    public int available() {
        return licenses.size();
    }


    public long getWorldId() {
        return worldId;
    }

    /**
     * Must only be called by {@link MRVMWorld#refreshFieldBackReferences()}.
     * Explicit definition of private setter to prevent from generating setter by accident.
     */
    private void setWorld(MRVMWorld world) {
        if (getWorldId() != world.getId()) {
            throw new IncompatibleWorldException("The stored worldId does not represent the passed world reference");
        }
        this.world = world;
    }


    /**
     * Method is called after deserialization, there is not need to call it on any other occasion.<br>
     * @see World#refreshFieldBackReferences() for explanations.
     */
    public void refreshFieldBackReferences(MRVMWorld world) {
        setWorld(world);
        for (MRVMLicense license : licenses) {
            license.refreshFieldBackReferences(this);
        }
    }
}
