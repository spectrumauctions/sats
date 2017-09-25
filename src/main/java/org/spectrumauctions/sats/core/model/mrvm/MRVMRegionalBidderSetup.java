/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model.mrvm;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSortedMap;
import org.spectrumauctions.sats.core.util.random.DoubleInterval;
import org.spectrumauctions.sats.core.util.random.UniformDistributionRNG;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Weiss
 *
 */
public class MRVMRegionalBidderSetup extends MRVMBidderSetup {

    private final double exponentFactor;
    private final double base;


    protected MRVMRegionalBidderSetup(Builder builder) {
        super(builder);
        this.exponentFactor = builder.exponentFactor;
        this.base = builder.base;
    }


    /**
     * Selects one of the regions at random
     */
    public MRVMRegionsMap.Region drawHome(MRVMWorld world, UniformDistributionRNG rng) {
        List<MRVMRegionsMap.Region> regions = new ArrayList<>(world.getRegionsMap().getRegions());
        int index = rng.nextInt(regions.size());
        return regions.get(index);
    }

    /**
     * Determines the discount in value for distant regions
     */
    public ImmutableSortedMap<Integer, BigDecimal> drawDistanceDiscounts(MRVMWorld world, MRVMRegionsMap.Region home, UniformDistributionRNG rng) {
        ImmutableSortedMap.Builder<Integer, BigDecimal> distanceDiscount = ImmutableSortedMap.naturalOrder();
        distanceDiscount.put(0, BigDecimal.ONE);
        int maxDistance = world.getRegionsMap().getLongestShortestPath(home);
        for (int i = 1; i <= maxDistance; i++) {
            double exponent = exponentFactor * i * (-1);
            double gamma = Math.pow(base, exponent);

            Preconditions.checkState(gamma >= 0 && gamma <= 1, "Invalid Gamma, some of the calculation parameters have unallowed values");

            BigDecimal roundedGamma = BigDecimal.valueOf(gamma).setScale(5, BigDecimal.ROUND_HALF_DOWN);
            distanceDiscount.put(i, roundedGamma);
        }
        return distanceDiscount.build();
    }


    public static class Builder extends MRVMBidderSetup.Builder {

        private double exponentFactor;
        private double base;

        public Builder() {
            super("Multi Region Model Regional Bidder",
                    4,
                    new DoubleInterval(500, 840),
                    new DoubleInterval(0.04, 0.1));
            this.exponentFactor = 1.25;
            this.base = 2;
        }


        /**
         * Specify the Gamma Function, i.e., the discount the regional bidder has on values for regions with distance d
         * from his home (i.e., {@link MRVMRegionsMap#getDistance(MRVMRegionsMap.Region, MRVMRegionsMap.Region)}<br><br>
         * It has the shape <i><b>base</b>.pow(-x * <b>exponentFactor</b>)</i>.<br>
         * ExponentFactor and base can be specified with this function.
         * @param base Has to be greater than 0
         * @param exponentFactor Has to be greater than 0
         */
        public void setGammaShape(double base, double exponentFactor) {
            Preconditions.checkArgument(base > 0);
            Preconditions.checkArgument(exponentFactor > 0);
            this.base = base;
            this.exponentFactor = exponentFactor;
        }

        /**
         * See {@link #setGammaShape(double, double) for meaning of this parameter
         */
        public double getExponentFactor() {
            return exponentFactor;
        }


        /**
         * See {@link #setGammaShape(double, double) for meaning of this parameter
         */
        public double getBase() {
            return base;
        }


        /**
         * @see MRVMBidderSetup.Builder#build()
         */
        @Override
        public MRVMRegionalBidderSetup build() {
            return new MRVMRegionalBidderSetup(this);
        }

    }


}
