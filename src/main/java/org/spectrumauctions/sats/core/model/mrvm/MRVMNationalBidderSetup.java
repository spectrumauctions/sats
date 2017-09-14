/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model.mrvm;

import com.google.common.base.Preconditions;
import org.spectrumauctions.sats.core.util.random.DoubleInterval;
import org.spectrumauctions.sats.core.util.random.UniformDistributionRNG;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Michael Weiss
 *
 */
public class MRVMNationalBidderSetup extends MRVMBidderSetup {

    /**
     * Parameter used for the calculation of gamma
     *
     */
    private static final double f = 2;

    /**
     * Parameter used for the calculation of gamma
     */
    private final DoubleInterval bInterval;

    /**
     * The highest number of missing regions for which a specific gamma is defined<br>
     * (The gamma for having more than kMax missing regions is the same
     *  as the gamma for having kMax missing regions).
     *  <br><br>
     * Variable is non-final as it might be set to the number of regions of a world,
     * if the world has less than kMax regions
     */
    private double kMax = 2;


    protected MRVMNationalBidderSetup(Builder builder) {
        super(builder);
        this.bInterval = builder.bInterval;
    }


    /**
     * @return a map containing all gammas for a number of uncovered regions between 1 and kMax.
     */
    public Map<Integer, BigDecimal> drawGamma(MRVMWorld world, UniformDistributionRNG rng) {
        if (kMax > world.getRegionsMap().getNumberOfRegions()) {
            kMax = world.getRegionsMap().getNumberOfRegions();
        }
        double b = rng.nextDouble(bInterval);
        Map<Integer, BigDecimal> gammas = new HashMap<>();
        for (int i = 1; i <= kMax; i++) {
            double gamma = 1 - Math.pow(i * b, f);
            Preconditions.checkState(gamma >= 0 && gamma <= 1, "Invalid Gamma, some of the calculation parameters have unallowed values");
            BigDecimal roundedGamma = BigDecimal.valueOf(gamma).setScale(5, BigDecimal.ROUND_HALF_DOWN);
            gammas.put(i, roundedGamma);
        }
        return gammas;
    }

    public static class Builder extends MRVMBidderSetup.Builder {

        private DoubleInterval bInterval;

        public Builder() {
            super("Multi Region Model National Bidder",
                    3,
                    new DoubleInterval(700, 1200),
                    new DoubleInterval(0.08, 0.22));
            this.bInterval = new DoubleInterval(0.1, 0.3);
        }

        /**
         * Sets the interval for the parameter b which is relevant for the draw of the Gamma.
         * @param bInterval The interval, has to be within [0, 0.5]
         */
        public void setbInterval(DoubleInterval bInterval) {
            Preconditions.checkArgument(bInterval.getMinValue() >= 0 && bInterval.getMaxValue() <= 0.5,
                    "bInterval has to be within [0, 0.5]!");
            this.bInterval = bInterval;
        }

        /* (non-Javadoc)
         * @see MRVMBidderSetup.Builder#build()
         */
        @Override
        public MRVMNationalBidderSetup build() {
            return new MRVMNationalBidderSetup(this);
        }

    }


}
