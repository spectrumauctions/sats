/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model.mrvm;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import org.spectrumauctions.sats.core.bidlang.BiddingLanguage;
import org.spectrumauctions.sats.core.model.Bundle;
import org.spectrumauctions.sats.core.model.UnsupportedBiddingLanguageException;
import org.spectrumauctions.sats.core.util.BigDecimalUtils;
import org.spectrumauctions.sats.core.util.random.UniformDistributionRNG;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author Michael Weiss
 *
 */
public final class MRVMNationalBidder extends MRVMBidder {

    private static final long serialVersionUID = 6947670485542817609L;
    /**
     * Stores the gamma value of the bidder for a given number of uncovered regions
     */
    private final SortedMap<Integer, BigDecimal> gammaValues;


    MRVMNationalBidder(long id, long populationId, MRVMWorld world, MRVMNationalBidderSetup setup,
                       UniformDistributionRNG rng) {
        super(id, populationId, world, setup, rng);
        Map<Integer, BigDecimal> gammaInput = setup.drawGamma(world, rng);
        //TODO Do nicely, no ImmutableSortedMap conversion
        gammaValues = new TreeMap<>(buildGammaMap(gammaInput));
        store();
    }

    private static ImmutableSortedMap<Integer, BigDecimal> buildGammaMap(Map<Integer, BigDecimal> gammaInput) {
        ImmutableSortedMap.Builder<Integer, BigDecimal> mapBuilder = ImmutableSortedMap.naturalOrder();
        SortedMap<Integer, BigDecimal> sortedInput = new TreeMap<>(gammaInput);
        Preconditions.checkArgument(sortedInput.firstKey() == 1,
                "Gamma Values must (exclusively) be specified for quantities 1 and (optionally) higher",
                sortedInput);
        Preconditions.checkArgument(sortedInput.lastKey().equals(sortedInput.size()), ""
                        + "Gamma Values must be specified for all capacities in {0, ..., k_{max}}, where k_{max} > 0 is any natural number > 0",
                sortedInput);

        for (Entry<Integer, BigDecimal> inputGammaEntry : sortedInput.entrySet()) {
            Preconditions.checkArgument(inputGammaEntry.getValue().compareTo(BigDecimal.ZERO) >= 0, "Gamma must not be negative", inputGammaEntry);
            mapBuilder.put(inputGammaEntry);
        }
        return mapBuilder.build();
    }

    /**
     * {@inheritDoc}
     * @param r Not required for gamma calculation of national bidder and will be ignored
     */
    @Override
    public BigDecimal gammaFactor(MRVMRegionsMap.Region r, Bundle<MRVMLicense> bundle) {
        int uncoveredRegions = countUncoveredRegions(bundle);
        BigDecimal gamma = getGamma(uncoveredRegions);
        return gamma;
    }

    /**
     * {@inheritDoc}<br><br>
     * As gamma is not dependent on the region for national bidders, the returned map will contain the same value for all keys,
     * i.e. the same gamma for all regions
     */
    @Override
    public Map<MRVMRegionsMap.Region, BigDecimal> gammaFactors(Bundle<MRVMLicense> bundle) {
        BigDecimal gamma = gammaFactor(null, bundle);
        Map<MRVMRegionsMap.Region, BigDecimal> result = new HashMap<>();
        for (MRVMRegionsMap.Region region : getWorld().getRegionsMap().getRegions()) {
            result.put(region, gamma);
        }
        return result;
    }

    public int getKMax() {
        return gammaValues.lastKey();
    }

    private int countUncoveredRegions(Bundle<MRVMLicense> bundle) {
        Map<MRVMRegionsMap.Region, Bundle<MRVMLicense>> licensesPerRegion = MRVMWorld.getLicensesPerRegion(bundle);
        int uncoveredCount = 0;
        for (Bundle<MRVMLicense> regionalBundle : licensesPerRegion.values()) {
            if (regionalBundle.isEmpty()) {
                uncoveredCount++;
            }
        }
        return uncoveredCount;
    }

    /**
     * Reads the gammavalue form the stored map.
     * If uncoveredRegions greater than {@link #getKMax()}, then the value for kMax is returned (see model writeup for explanation).
     * @param uncoveredRegions the number of regions that are uncovered
     * @return the gamma value for the number of uncovered regions
     */
    public BigDecimal getGamma(int uncoveredRegions) {
        if (uncoveredRegions > getKMax()) {
            uncoveredRegions = getKMax();
        } else if (uncoveredRegions == 0) {
            return BigDecimal.ONE;
        }
        return gammaValues.get(uncoveredRegions);
    }

    /* (non-Javadoc)
     * @see Bidder#getValueFunctionRepresentation(java.lang.Class, long)
     */
    @Override
    public <T extends BiddingLanguage> T getValueFunction(Class<T> type, long seed)
            throws UnsupportedBiddingLanguageException {
        return super.getValueFunction(type, seed);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((gammaValues == null) ? 0 : BigDecimalUtils.hashCodeIgnoringScale(gammaValues));
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
        MRVMNationalBidder other = (MRVMNationalBidder) obj;
        if (gammaValues == null) {
            if (other.gammaValues != null)
                return false;
        } else if (!BigDecimalUtils.equalIgnoreScaleOnValues(gammaValues, other.gammaValues)) {
            return false;
        }
        return true;
    }


}
