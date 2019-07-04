/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model.bvm;

import com.google.common.base.Preconditions;
import org.spectrumauctions.sats.core.model.GenericGood;
import org.spectrumauctions.sats.core.model.GenericWorld;
import org.spectrumauctions.sats.core.model.World;
import org.spectrumauctions.sats.core.util.PreconditionUtils;
import org.spectrumauctions.sats.core.util.instancehandling.InstanceHandler;
import org.spectrumauctions.sats.core.util.random.JavaUtilRNGSupplier;
import org.spectrumauctions.sats.core.util.random.RNGSupplier;

import java.util.*;
import java.util.Map.Entry;

/**
 * @author Michael Weiss
 *
 */
public final class BMWorld extends World implements GenericWorld {

    public static final String MODEL_NAME = "Base and MultiBand Value Model";
    private static final long serialVersionUID = 8418773596929829197L;
    private final List<BMBand> bands;

    public BMWorld(BMWorldSetup setup, RNGSupplier rngSupplier) {
        super(MODEL_NAME);
        PreconditionUtils.checkNotNull(setup, rngSupplier);
        bands = createBands(setup, rngSupplier);
        store();
    }


    private List<BMBand> createBands(BMWorldSetup setup, RNGSupplier rngSupplier) {
        List<BMBand> bands = new ArrayList<>();
        int licenseCount = 0;
        for (Entry<String, Integer> entry : setup.bands().entrySet()) {
            bands.add(new BMBand(this, entry.getKey(), entry.getValue(), licenseCount, rngSupplier));
            licenseCount += entry.getValue();
        }
        return bands;
    }

    /**
     * Gets a band with a specific name
     *
     * @param bandName the band name to be queried
     * @return band with this name, null if no such band in this world
     */
    public BMBand getBand(String bandName) {
        for (BMBand band : bands) {
            if (band.getId().equals(bandName))
                return band;
        }
        return null;
    }

    /**
     * @return a list of all bands
     */
    public List<BMBand> getBands() {
        return Collections.unmodifiableList(bands);
    }

    @Override
    public List<BMLicense> getLicenses() {
        List<BMLicense> licenses = new LinkedList<>();
        for (BMBand band : bands) {
            licenses.addAll(band.containedGoods());
        }
        return licenses;
    }

    @Override
    public int getNumberOfGoods() {
        return getLicenses().size();
    }

    /**
     * @return a new population, i.e., a set of bidders, according to the specified bidderSetup
     */
    public List<BMBidder> createPopulation(BMBidderSetup bidderSetup) {
        return createPopulation(bidderSetup, new JavaUtilRNGSupplier());
    }

    /**
     * @see #createPopulation(BMBidderSetup)
     */
    public List<BMBidder> createPopulation(BMBidderSetup bidderSetup, long seed) {
        return createPopulation(bidderSetup, new JavaUtilRNGSupplier(seed));
    }

    /**
     * @see #createPopulation(BMBidderSetup)
     */
    public List<BMBidder> createPopulation(BMBidderSetup bidderSetup, RNGSupplier rngSupplier) {
        Preconditions.checkNotNull(bidderSetup);
        Set<BMBidderSetup> setups = new HashSet<>();
        setups.add(bidderSetup);
        return createPopulation(setups, rngSupplier);
    }

    /**
     * @return a new population, i.e., a set of bidders, according to the specified bidderSetups
     * @param bidderSetups the collection of setups that are the basis for the new population
     */
    public List<BMBidder> createPopulation(Collection<BMBidderSetup> bidderSetups) {
        Preconditions.checkNotNull(bidderSetups);
        return createPopulation(bidderSetups, new JavaUtilRNGSupplier());
    }

    /**
     * @see #createPopulation(Collection)
     */
    public List<BMBidder> createPopulation(Collection<BMBidderSetup> bidderSetups, long seed) {
        return createPopulation(bidderSetups, new JavaUtilRNGSupplier(seed));
    }

    /**
     * @see #createPopulation(Collection)
     */
    public List<BMBidder> createPopulation(Collection<BMBidderSetup> bidderSetups, RNGSupplier rngSupplier) {
        long population = openNewPopulation();
        List<BMBidder> bidders = new ArrayList<>();
        int idCount = 0;
        for (BMBidderSetup setup : bidderSetups) {
            for (int i = 0; i < setup.getNumberOfBidders(); i++) {
                bidders.add(new BMBidder(population, idCount++, this, setup, rngSupplier.getUniformDistributionRNG()));
            }
        }
        return bidders;
    }

    public static BMWorld readWorld(long worldId) {
        return InstanceHandler.getDefaultHandler().readWorld(BMWorld.class, worldId);
    }


    /* (non-Javadoc)
     * @see World#refreshFieldBackReferences()
     */
    @Override
    public void refreshFieldBackReferences() {
        for (BMBand band : bands) {
            band.refreshFieldBackReferences(this);
        }
    }


    /* (non-Javadoc)
     * @see World#restorePopulation(long)
     */
    @Override
    public Collection<BMBidder> restorePopulation(long populationId) {
        return super.restorePopulation(BMBidder.class, populationId);
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((bands == null) ? 0 : bands.hashCode());
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
        BMWorld other = (BMWorld) obj;
        if (bands == null) {
            if (other.bands != null)
                return false;
        } else if (!bands.equals(other.bands))
            return false;
        return true;
    }


    @Override
    public List<? extends GenericGood> getAllGenericDefinitions() {
        return bands;
    }
}
