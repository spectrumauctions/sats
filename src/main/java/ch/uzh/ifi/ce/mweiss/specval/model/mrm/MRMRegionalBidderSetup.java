/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.uzh.ifi.ce.mweiss.specval.model.mrm;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSortedMap;

import ch.uzh.ifi.ce.mweiss.specval.model.mrm.MRMRegionsMap.Region;
import ch.uzh.ifi.ce.mweiss.specval.util.random.DoubleInterval;
import ch.uzh.ifi.ce.mweiss.specval.util.random.UniformDistributionRNG;

/**
 * @author Michael Weiss
 *
 */
public class MRMRegionalBidderSetup extends MRMBidderSetup {

    private final double exponentFactor;
    private final double base;
    

    protected MRMRegionalBidderSetup(Builder builder) {
        super(builder);
        this.exponentFactor = builder.exponentFactor;
        this.base = builder.base;
    }


    /**
     * Selects one of the regions at random
     * @param world
     * @param rng
     * @return
     */
    public Region drawHome(MRMWorld world, UniformDistributionRNG rng) {
        List<Region> regions = new ArrayList<>(world.getRegionsMap().getRegions());
        int index = rng.nextInt(regions.size());
        return regions.get(index);
    }

    /**
     * Determines the discount in value for distant regions
     * @param world
     * @param home
     * @param rng
     * @return
     */
    public ImmutableSortedMap<Integer, BigDecimal> drawDistanceDiscounts(MRMWorld world, Region home, UniformDistributionRNG rng) {
        ImmutableSortedMap.Builder<Integer, BigDecimal> distanceDiscount = ImmutableSortedMap.naturalOrder();
        distanceDiscount.put(0, BigDecimal.ONE);
        int maxDistance = world.getRegionsMap().getLongestShortestPath(home);
        for(int i = 1; i <= maxDistance ; i++){
            double exponent = exponentFactor*i*(-1);
            double gamma = Math.pow(base, exponent);
            try{
                Preconditions.checkState(gamma >= 0 && gamma <= 1, "Invalid Gamma, some of the calculation parameters have unallowed values");
            }catch(IllegalStateException e){
                //TODO Delete Try/Catch again
                System.out.println("Invalid Gamma");
            }
            BigDecimal roundedGamma = BigDecimal.valueOf(gamma).setScale(5, BigDecimal.ROUND_HALF_DOWN);
            distanceDiscount.put(i,roundedGamma);
        }
        return distanceDiscount.build();
    }
    


    
    public static class Builder extends MRMBidderSetup.Builder{

        private double exponentFactor;
        private double base;
        
        /**
         * @param alphaInterval
         * @param betaInterval
         */
        public Builder() {
            super("Multi Region Model Regional Bidder",
                4,
                new DoubleInterval(700,950), 
                new DoubleInterval(0.1,0.2));
            this.exponentFactor = 0.9;
            this.base = 2;
        }



        /**
         * Specify the Gamma Function, i.e., the discount the regional bidder has on values for regions with distance d from his home (i.e., {@link MRMRegionsMap#getDistance(home, d)}<br><br>
         * It has the shape <i><b>base</b>.pow(-x * <b>exponentFactor</b>)</i>.<br>
         * ExponentFactor and base can be specified with this function.
         * @param  Has to be greater than 0
         * @param exponentFactor Has to be greater than 0
         */
        public void setGammaShape(double base, double exponentFactor){
            Preconditions.checkArgument(base > 0);
            Preconditions.checkArgument(exponentFactor > 0);
            this.base = base;
            this.exponentFactor = exponentFactor;
        }

        /**
         * See {@link #setGammaShape(double, double) for meaning of this parameter
         * @return
         */
        public double getExponentFactor() {
            return exponentFactor;
        }


        /**
         * See {@link #setGammaShape(double, double) for meaning of this parameter
         * @return
         */
        public double getBase() {
            return base;
        }


        /* (non-Javadoc)
         * @see ch.uzh.ifi.ce.mweiss.specval.model.mrm.MRMBidderSetup.Builder#build()
         */
        @Override
        public MRMRegionalBidderSetup build() {
            return new MRMRegionalBidderSetup(this);
        }
        
    }
    
    
   

}
