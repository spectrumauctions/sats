/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.uzh.ifi.ce.mweiss.sats.core.model.mrm;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ch.uzh.ifi.ce.mweiss.sats.core.model.UnsupportedBiddingLanguageException;
import ch.uzh.ifi.ce.mweiss.sats.core.util.random.UniformDistributionRNG;
import ch.uzh.ifi.ce.mweiss.sats.core.bidlang.BiddingLanguage;
import ch.uzh.ifi.ce.mweiss.sats.core.model.Bundle;
import ch.uzh.ifi.ce.mweiss.sats.core.model.mrm.MRMRegionsMap.Region;

/**
 * @author Michael Weiss
 *
 */
public final class MRMLocalBidder extends MRMBidder {

    /**
     * Caches the gamma factors.<br>
     * This is only instantiated at its first use.
     */
    private transient Map<Region, BigDecimal> gammaFactorCache = null;
    
    /**
     * Stores the id's of all regions for which this bidder is interested;
     */
    final Set<Integer> regionsOfInterest;
    
    /**
     * @param id
     * @param populationId
     * @param world
     * @param setup
     * @param rng
     */
    MRMLocalBidder(long id, long populationId, MRMWorld world, MRMLocalBidderSetup setup,
            UniformDistributionRNG rng) {
        super(id, populationId, world, setup, rng);
        Set<Region> regionsOfInterest = setup.drawRegionsOfInterest(world, rng);
          Set<Integer> regionsOfInterestIds = new HashSet<>();
          for(Region region : regionsOfInterest){
              if(! getWorld().getRegionsMap().getRegions().contains(region)){
                  throw new IllegalArgumentException("Region of Interest of this bidder is not part of the same world as this bidder");
              }
              regionsOfInterestIds.add(region.getId());
          }
          this.regionsOfInterest = Collections.unmodifiableSet(regionsOfInterestIds);
        store();
    }

    /**
     * Transforms a bidders {@link MRMLocalBidder#regionsOfInterest} into a format suitable for {@link #gammaFactor(Region, Bundle)}
     * @param world
     * @param regionsOfInterest
     * @return
     */
    private static Map<Region, BigDecimal> mapGammaFactors(MRMWorld world, Set<Integer> regionsOfInterest){
        Map<Region, BigDecimal> result = new HashMap<>();
        for(Region region : world.getRegionsMap().getRegions()){
            BigDecimal gammaFactor;
            if(regionsOfInterest.contains(region.getId())){
                gammaFactor = BigDecimal.ONE;
            }else{
                gammaFactor = BigDecimal.ZERO;
            }
            result.put(region,gammaFactor);
        }
        return result;
    }
    
    /**
     * {@inheritDoc}
     * @param bundle Is not required for calculation of local bidders gamma factors and will be ignored.
     */
    @Override
    public BigDecimal gammaFactor(Region r, Bundle<MRMLicense> bundle) {
        return gammaFactors(bundle).get(r);
    }

    /**
     * {@inheritDoc}
     * @param bundle Is not required for calculation of local bidders gamma factors and will be ignored.
     */
    @Override
    public Map<Region, BigDecimal> gammaFactors(Bundle<MRMLicense> bundle) {
        if(gammaFactorCache == null){
            gammaFactorCache = mapGammaFactors(getWorld(), regionsOfInterest);
        }
        return Collections.unmodifiableMap(gammaFactorCache);
    }

    /* (non-Javadoc)
     * @see Bidder#getValueFunctionRepresentation(java.lang.Class, long)
     */
    @Override
    public <T extends BiddingLanguage> T getValueFunction(Class<T> type, long seed)
            throws UnsupportedBiddingLanguageException {
        try{
            return super.getValueFunction(type,seed);
        }catch(UnsupportedBiddingLanguageException e){
            // This bidder cannot provide any other bidding languages other than the ones super-bidder can.
            throw e;
        }
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
        MRMLocalBidder other = (MRMLocalBidder) obj;
        if (regionsOfInterest == null) {
            if (other.regionsOfInterest != null)
                return false;
        } else if (!regionsOfInterest.containsAll(other.regionsOfInterest) && regionsOfInterest.size() == other.regionsOfInterest.size())
            return false;
        return true;
    }

    
    
    

}
