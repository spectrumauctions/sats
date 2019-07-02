/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model.mrvm;

import org.apache.commons.lang3.NotImplementedException;
import org.marketdesignresearch.mechlib.domain.Bundle;
import org.marketdesignresearch.mechlib.domain.price.Prices;
import org.spectrumauctions.sats.core.bidlang.BiddingLanguage;
import org.spectrumauctions.sats.core.model.LicenseBundle;
import org.spectrumauctions.sats.core.model.SATSBidder;
import org.spectrumauctions.sats.core.model.UnsupportedBiddingLanguageException;
import org.spectrumauctions.sats.core.util.random.RNGSupplier;
import org.spectrumauctions.sats.core.util.random.UniformDistributionRNG;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author Michael Weiss
 *
 */
public final class MRVMLocalBidder extends MRVMBidder {

    private static final long serialVersionUID = -7654713373213024311L;
    /**
     * Caches the gamma factors.<br>
     * This is only instantiated at its first use.
     */
    private transient Map<MRVMRegionsMap.Region, BigDecimal> gammaFactorCache = null;

    /**
     * Stores the ids of all regions for which this bidder is interested
     */
    final Set<Integer> regionsOfInterest;

    MRVMLocalBidder(long id, long populationId, MRVMWorld world, MRVMLocalBidderSetup setup,
                    UniformDistributionRNG rng) {
        super(id, populationId, world, setup, rng);
        Set<MRVMRegionsMap.Region> regionsOfInterest = setup.drawRegionsOfInterest(world, rng);
        Set<Integer> regionsOfInterestIds = new HashSet<>();
        for (MRVMRegionsMap.Region region : regionsOfInterest) {
            if (!getWorld().getRegionsMap().getRegions().contains(region)) {
                throw new IllegalArgumentException("Region of Interest of this bidder is not part of the same world as this bidder");
            }
            regionsOfInterestIds.add(region.getId());
        }
        this.regionsOfInterest = Collections.unmodifiableSet(regionsOfInterestIds);
        store();
    }

    /**
     * Transforms a bidders {@link MRVMLocalBidder#regionsOfInterest} into a format suitable for {@link #gammaFactor(MRVMRegionsMap.Region, LicenseBundle)}
     */
    private static Map<MRVMRegionsMap.Region, BigDecimal> mapGammaFactors(MRVMWorld world, Set<Integer> regionsOfInterest) {
        Map<MRVMRegionsMap.Region, BigDecimal> result = new HashMap<>();
        for (MRVMRegionsMap.Region region : world.getRegionsMap().getRegions()) {
            BigDecimal gammaFactor;
            if (regionsOfInterest.contains(region.getId())) {
                gammaFactor = BigDecimal.ONE;
            } else {
                gammaFactor = BigDecimal.ZERO;
            }
            result.put(region, gammaFactor);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     * @param bundle Is not required for calculation of local bidders gamma factors and will be ignored.
     */
    @Override
    public BigDecimal gammaFactor(MRVMRegionsMap.Region r, LicenseBundle<MRVMLicense> bundle) {
        return gammaFactors(bundle).get(r);
    }

    /**
     * {@inheritDoc}
     * @param bundle Is not required for calculation of local bidders gamma factors and will be ignored.
     */
    @Override
    public Map<MRVMRegionsMap.Region, BigDecimal> gammaFactors(Set<MRVMLicense> bundle) {
        if (gammaFactorCache == null) {
            gammaFactorCache = mapGammaFactors(getWorld(), regionsOfInterest);
        }
        return Collections.unmodifiableMap(gammaFactorCache);
    }

    @Override
    public <T extends BiddingLanguage> T getValueFunction(Class<T> type, RNGSupplier rngSupplier)
            throws UnsupportedBiddingLanguageException {
        return super.getValueFunction(type, rngSupplier);
    }

    @Override
    public MRVMLocalBidder drawSimilarBidder(RNGSupplier rngSupplier) {
        return new MRVMLocalBidder(getLongId(), getPopulation(), getWorld(), (MRVMLocalBidderSetup) getSetup(), rngSupplier.getUniformDistributionRNG());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((regionsOfInterest == null) ? 0 : regionsOfInterest.hashCode());
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
        MRVMLocalBidder other = (MRVMLocalBidder) obj;
        if (regionsOfInterest == null) {
            if (other.regionsOfInterest != null)
                return false;
        } else if (!regionsOfInterest.containsAll(other.regionsOfInterest) && regionsOfInterest.size() == other.regionsOfInterest.size())
            return false;
        return true;
    }
}
