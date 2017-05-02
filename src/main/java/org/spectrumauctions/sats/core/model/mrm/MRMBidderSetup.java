/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model.mrm;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import org.spectrumauctions.sats.core.util.random.DoubleInterval;
import org.spectrumauctions.sats.core.util.random.UniformDistributionRNG;
import org.spectrumauctions.sats.core.model.BidderSetup;

/**
 * @author Michael Weiss
 *
 */
public abstract class MRMBidderSetup extends BidderSetup{
    
    private final DoubleInterval alphaInterval;
    private final DoubleInterval betaInterval;
    private final DoubleInterval zLowInterval;
	private final DoubleInterval zHighInterval;

	private static final BigDecimal NONZERO_INCREMENT = BigDecimal.valueOf(0.01);


    protected MRMBidderSetup(Builder builder) {
        super(builder);
        this.alphaInterval = builder.alphaInterval;
        this.betaInterval = builder.betaInterval;
        this.zLowInterval = builder.zLowInterval;
        this.zHighInterval = builder.zHighInterval;
    }

    /**
     * Draws the Alpha-Parameter uniformly at random.<br>
     * Alpha is a parameter defining an expected profit per served customer, if quality of service and regional discount are ignored.<br>
     * It can be understood as a relative bidder strength parameter.
     * @param rng
     * @return a BigDecimal in [0,1]
     */
    public BigDecimal drawAlpha(UniformDistributionRNG rng) {
        return rng.nextBigDecimal(alphaInterval);
    }

    /**
     * Draws the Beta-Parameter uniformly at random.<br> 
     * Beta is a parameter defining the target market share this bidder intends to cover. <br>
     * The bidders value for a bundle increases heavily as soon as the capacity share he has in a region gets close to beta.
     * @param rng
     * @return a BigDecimal in [0,1]
     */
    public BigDecimal drawBeta(MRMRegionsMap.Region r, UniformDistributionRNG rng) {
        return rng.nextBigDecimal(betaInterval);
    }


    public Map<Integer, BigDecimal> drawZLow(Map<Integer, BigDecimal> betas, MRMWorld world, UniformDistributionRNG rng) {
        Map<Integer, BigDecimal> result = new HashMap<>();
        for(Map.Entry<Integer, BigDecimal> beta : betas.entrySet()) {
            BigDecimal minTerm = beta.getValue().subtract(BigDecimal.valueOf(0.3));
            if (minTerm.compareTo(BigDecimal.ZERO) < 0) {
                minTerm = NONZERO_INCREMENT;
            }
            BigDecimal dividend = minTerm.multiply(world.getMaximumRegionalCapacity());
            MRMRegionsMap.Region region = world.getRegionsMap().getRegion(beta.getKey());
            BigDecimal divisor = BigDecimal.valueOf(region.getPopulation()).multiply(beta.getValue());
            BigDecimal zvalue = dividend.divide(divisor, RoundingMode.HALF_UP);
            result.put(beta.getKey(),zvalue);
        }
        return result;
    }


	public Map<Integer, BigDecimal> drawZHigh(Map<Integer, BigDecimal> betas, MRMWorld world, UniformDistributionRNG rng) {
        Map<Integer, BigDecimal> result = new HashMap<>();
        for(Map.Entry<Integer, BigDecimal> beta : betas.entrySet()) {
            BigDecimal maxTerm = beta.getValue().add(BigDecimal.valueOf(0.3));
            if (maxTerm.compareTo(BigDecimal.ONE) > 0) {
                maxTerm = BigDecimal.ONE;
            }
            BigDecimal dividend = maxTerm.multiply(world.getMaximumRegionalCapacity());
            MRMRegionsMap.Region region = world.getRegionsMap().getRegion(beta.getKey());
            BigDecimal divisor = BigDecimal.valueOf(region.getPopulation()).multiply(beta.getValue());
            BigDecimal zvalue = dividend.divide(divisor, BigDecimal.ROUND_HALF_UP);
            result.put(beta.getKey(),zvalue);
        }
        return result;
	}

    
    public static abstract class Builder extends BidderSetup.Builder{
        
        private DoubleInterval alphaInterval;
        private DoubleInterval betaInterval;
        private DoubleInterval zLowInterval;
    	private DoubleInterval zHighInterval;
        
  
        /**
         * @param alphaInterval
         * @param betaInterval
         */
        protected Builder(String setupName, int numberOfBidders, DoubleInterval alphaInterval, DoubleInterval betaInterval) {
            super(setupName, numberOfBidders);
            this.alphaInterval = alphaInterval;
            this.betaInterval = betaInterval;
        }

        
        
        /**
         * The interval from which the alpha value will be drawn. <br>
         * See {@link MRMBidderSetup#alphaInterval} for explanation of alpha-parameter
         * @return
         */
        public DoubleInterval getAlphaInterval() {
            return alphaInterval;
        }



        /**
         * The interval from which the alpha value will be drawn. <br>
         * See {@link MRMBidderSetup#alphaInterval} for explanation of alpha-parameter
         * @return
         */
        public void setAlphaInterval(DoubleInterval alphaInterval) {
            this.alphaInterval = alphaInterval;
        }



        /**
         * The interval from which the beta value will be drawn. <br>
         * See {@link MRMBidderSetup#betaInterval} for explanation of beta-parameter
         * @return
         */
        public DoubleInterval getBetaInterval() {
            return betaInterval;
        }



        /**
         * The interval from which the beta value will be drawn. <br>
         * See {@link MRMBidderSetup#betaInterval} for explanation of beta-parameter
         * @return
         */
        public void setBetaInterval(DoubleInterval betaInterval) {
            this.betaInterval = betaInterval;
        }

        
        
        public DoubleInterval getzLowInterval() {
			return zLowInterval;
		}



		public DoubleInterval getzHighInterval() {
			return zHighInterval;
		}



		public void setzLowInterval(DoubleInterval zLowInterval) {
			this.zLowInterval = zLowInterval;
		}



		public void setzHighInterval(DoubleInterval zHighInterval) {
			this.zHighInterval = zHighInterval;
		}



		@Override
        public abstract MRMBidderSetup build();
    }
	

}
