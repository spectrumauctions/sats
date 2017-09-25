/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model.srvm;

import com.google.common.collect.ImmutableSet;
import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.World;
import org.spectrumauctions.sats.core.util.random.RNGSupplier;

import java.util.*;

/**
 * @author Michael Weiss
 *
 */
public final class SRVMWorld extends World {

    private static final long serialVersionUID = 1766287015715986936L;
    private final HashSet<SRVMBand> bands;

    private transient Integer numberOfGoods = null;
    private transient ImmutableSet<SRVMLicense> licenses = null;

    public SRVMWorld(SRVMWorldSetup setup, RNGSupplier rngSupplier) {
        super("Single-Region Value Model");
        this.bands = new HashSet<>(SRVMBand.createBands(this, setup, rngSupplier));
        store();
    }


    /**
     * @see World#getNumberOfGoods()
     */
    @Override
    public int getNumberOfGoods() {
        if (numberOfGoods == null) {
            int count = 0;
            for (SRVMBand band : bands) {
                count += band.getLicenses().size();
            }
            numberOfGoods = count;
        }
        return numberOfGoods;
    }

    /**
     * {@inheritDoc}
     * @return An immutable set containing all licenses.
     */
    @Override
    public ImmutableSet<SRVMLicense> getLicenses() {
        if (licenses == null) {
            ImmutableSet.Builder<SRVMLicense> builder = ImmutableSet.builder();
            for (SRVMBand band : bands) {
                builder.addAll(band.getLicenses());
            }
            this.licenses = builder.build();
        }
        return licenses;
    }

    public Set<SRVMBand> getBands() {
        return Collections.unmodifiableSet(bands);
    }

    public List<SRVMBidder> createPopulation(Collection<SRVMBidderSetup> bidderSetups, RNGSupplier rngSupplier) {
        long population = openNewPopulation();
        long currentId = 0;
        List<SRVMBidder> bidders = new ArrayList<>();
        for (SRVMBidderSetup setup : bidderSetups) {
            for (int i = 0; i < setup.getNumberOfBidders(); i++) {
                bidders.add(new SRVMBidder(setup, this, currentId++, population, rngSupplier));
            }
        }
        return bidders;
    }


    /**
     * @see World#restorePopulation(long)
     */
    @Override
    public Collection<? extends Bidder<SRVMLicense>> restorePopulation(long populationId) {
        return super.restorePopulation(SRVMBidder.class, populationId);
    }

    /**
     * @see World#refreshFieldBackReferences()
     */
    @Override
    public void refreshFieldBackReferences() {
        for (SRVMBand band : bands) {
            band.refreshFieldBackReferences(this);
        }
    }


}
