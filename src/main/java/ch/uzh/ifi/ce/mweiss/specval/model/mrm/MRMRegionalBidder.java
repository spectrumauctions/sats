/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.uzh.ifi.ce.mweiss.specval.model.mrm;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.google.common.base.Preconditions;

import ch.uzh.ifi.ce.mweiss.specval.bidlang.BiddingLanguage;
import ch.uzh.ifi.ce.mweiss.specval.model.Bundle;
import ch.uzh.ifi.ce.mweiss.specval.model.UnsupportedBiddingLanguageException;
import ch.uzh.ifi.ce.mweiss.specval.model.World;
import ch.uzh.ifi.ce.mweiss.specval.model.mrm.MRMRegionsMap.Region;
import ch.uzh.ifi.ce.mweiss.specval.util.BigDecimalUtils;
import ch.uzh.ifi.ce.mweiss.specval.util.random.UniformDistributionRNG;

/**
 * @author Michael Weiss
 *
 */
public final class MRMRegionalBidder extends MRMBidder {
    
    private final int homeId;
    transient Region home;

    private final SortedMap<Integer, BigDecimal> distanceDiscounts;
    
    /**
     * @param id
     * @param populationId
     * @param world
     * @param setup
     * @param rng
     */
    MRMRegionalBidder(long id, long populationId, MRMWorld world, MRMRegionalBidderSetup setup,
            UniformDistributionRNG rng) {
        super(id, populationId, world, setup, rng);
        this.home = setup.drawHome(world, rng);
        this.homeId = home.getId();
        this.distanceDiscounts = new TreeMap<>(setup.drawDistanceDiscounts(world, home, rng));
        validateDistanceDiscounts(getWorld(), distanceDiscounts);
        store();
    }
    
    /**
     * Validates if a map with distanceDiscounts is valid for a given world, i.e., if it defines a valid discount for all possible distances
     * @param world
     * @param discounts
     * @throws - NullPointerException if one of this methods argument is null 
     * or if a discount for a distance which is possible in the given is not defined<br>
     * - IllegalArgumentException if one of the defined discounts for a feasible distance is negative.
     * @return
     */
    private void validateDistanceDiscounts(MRMWorld world, SortedMap<Integer, BigDecimal> discounts){
        Preconditions.checkNotNull(world);
        Preconditions.checkNotNull(discounts);
        for(int i = 1; i < world.getRegionsMap().getLongestShortestPath(home); i++){
            Preconditions.checkNotNull(discounts.get(i));
            Preconditions.checkArgument(discounts.get(i).compareTo(BigDecimal.ZERO) >= 0, "Discount must not be negative");
        }
    }

    
    public int getHomeId() {
        return homeId;
    }

    /**
     * {@inheritDoc}
     * If the two regions are not disconnected, the value is 0.
     * @param bundle Is not required for calculation of regional bidders gamma factors and will be ignored.
     */
    @Override
    public BigDecimal gammaFactor(Region r, Bundle<MRMLicense> bundle) {
        int distance = getWorld().getRegionsMap().getDistance(home, r);
        if(distance > distanceDiscounts.lastKey()){
            //Not connected regions
            return BigDecimal.ZERO;
        }
        return distanceDiscounts.get(distance);
    }

    /**
     * {@inheritDoc}
     * @param bundle Is not required for calculation of regional bidders gamma factors and will be ignored.
     */
    @Override
    public Map<Region, BigDecimal> gammaFactors(Bundle<MRMLicense> bundle) {
        Map<Region, BigDecimal> result = new HashMap<>();
        for(Region region : getWorld().getRegionsMap().getRegions()){
            //Note repeately calculating distance is not expensive, as distance is cached in Map Instance
            int distance = getWorld().getRegionsMap().getDistance(home, region);
            BigDecimal discount = distanceDiscounts.get(distance);
            result.put(region, discount);
        }
        return result;
    }

    /* (non-Javadoc)
     * @see ch.uzh.ifi.ce.mweiss.specval.model.Bidder#getValueFunctionRepresentation(java.lang.Class, long)
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

    /* (non-Javadoc)
     * @see ch.uzh.ifi.ce.mweiss.specval.model.Bidder#refreshReference(ch.uzh.ifi.ce.mweiss.specval.model.World)
     */
    @Override
    public void refreshReference(World world){
        super.refreshReference(world);
        Region homeCandidate = getWorld().getRegionsMap().getRegion(homeId);
        if(homeCandidate == null){
            throw new IllegalArgumentException("The specified world does not have this bidders home region");
        }else{
            this.home = homeCandidate;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((distanceDiscounts == null) ? 0 : BigDecimalUtils.hashCodeIgnoringScale(distanceDiscounts));
        result = prime * result + homeId;
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
        MRMRegionalBidder other = (MRMRegionalBidder) obj;
        if (distanceDiscounts == null) {
            if (other.distanceDiscounts != null)
                return false;
        } else if (!BigDecimalUtils.equalIgnoreScaleOnValues(distanceDiscounts, other.distanceDiscounts))
            return false;
        if (homeId != other.homeId)
            return false;
        return true;
    }
    

    
}
