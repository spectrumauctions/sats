/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.uzh.ifi.ce.mweiss.specval.model.mrm;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Preconditions;

import ch.uzh.ifi.ce.mweiss.specval.util.random.DoubleInterval;
import ch.uzh.ifi.ce.mweiss.specval.util.random.IntegerInterval;
import ch.uzh.ifi.ce.mweiss.specval.util.random.UniformDistributionRNG;

/**
 * @author Michael Weiss
 *
 */
public class MRMGlobalBidderSetup extends MRMBidderSetup {

    /**
     * Parameter used for the calculation of gamma
     * 
     */
    private final double f = 2;
    
    /**
     * Parameter used for the calculation of gamma
     */
    private final double b = 0.2;
    
    /**
     * The highest number of missing regions for which a specific gamma is defined<br>
     * (The gamma for having more than kMax missing regions is the same
     *  as the gamma for having kMax missing regions).
     *  <br><br>
     * Variable is non-final as it might be set to the number of regions of a world,
     * if the world has less than kMax regions
     */
    private double kMax = 4;
    

    protected MRMGlobalBidderSetup(Builder builder) {
        super(builder);
    }




    /**
     * Returns a map containig all gammas for a number of uncovered regions between 1 and kMax.
     * @return
     */
    public Map<Integer, BigDecimal> drawGamma(MRMWorld world, UniformDistributionRNG rng) {
        if(kMax > world.getRegionsMap().getNumberOfRegions()){
            kMax =  world.getRegionsMap().getNumberOfRegions();
        }
        Map<Integer, BigDecimal> gammas = new HashMap<>();
        for(int i = 1; i <= kMax; i++){
            double gamma = 1 - Math.pow(i * b, f);
            Preconditions.checkState(gamma >= 0 && gamma <= 1, "Invalid Gamma, some of the calculation parameters have unallowed values");
            BigDecimal roundedGamma = BigDecimal.valueOf(gamma).setScale(5, BigDecimal.ROUND_HALF_DOWN);
            gammas.put(i,roundedGamma);
        }
        return gammas;
    }
    
    public static class Builder extends MRMBidderSetup.Builder{

        
        /**
         * @param alphaInterval
         * @param betaInterval
         */
        public Builder() {
            super("Multi Region Model Global Bidder",
                    3,
                    new DoubleInterval(800,1400), 
                    new DoubleInterval(0.1,0.2));
        }



        /* (non-Javadoc)
         * @see ch.uzh.ifi.ce.mweiss.specval.model.mrm.MRMBidderSetup.Builder#build()
         */
        @Override
        public MRMGlobalBidderSetup build() {
            return new MRMGlobalBidderSetup(this);
        }
        
    }
    
    

    
    
}
