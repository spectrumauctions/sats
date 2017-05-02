/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.uzh.ifi.ce.mweiss.sats.opt.model.srm;

import ch.uzh.ifi.ce.mweiss.sats.core.bidlang.generic.GenericValue;
import ch.uzh.ifi.ce.mweiss.sats.core.model.Bundle;
import ch.uzh.ifi.ce.mweiss.sats.core.model.srm.SRMBand;
import ch.uzh.ifi.ce.mweiss.sats.core.model.srm.SRMBidder;
import ch.uzh.ifi.ce.mweiss.sats.core.model.srm.SRMWorld;
import ch.uzh.ifi.ce.mweiss.sats.opt.model.EfficientAllocator;
import ch.uzh.ifi.ce.mweiss.sats.opt.model.GenericAllocation;
import com.google.common.base.Preconditions;
import edu.harvard.econcs.jopt.solver.IMIPResult;
import edu.harvard.econcs.jopt.solver.SolveParam;
import edu.harvard.econcs.jopt.solver.client.SolverClient;
import edu.harvard.econcs.jopt.solver.mip.Constraint;
import edu.harvard.econcs.jopt.solver.mip.MIP;
import edu.harvard.econcs.jopt.solver.mip.Variable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Fabio Isler
 */
public class SRM_MIP implements EfficientAllocator<GenericAllocation<SRMBand>> {

    public static boolean PRINT_SOLVER_RESULT = false;

    private static SolverClient SOLVER = new SolverClient();

    /**
     * If the highest possible value any bidder can have is higher than {@link MIP#MAX_VALUE} - MAXVAL_SAFETYGAP}
     * a non-zero scaling factor for the calculation is chosen.
     */
    public static BigDecimal highestValidVal = BigDecimal.valueOf(MIP.MAX_VALUE - 1000000);
    private SRMWorldPartialMip worldPartialMip;
    private Map<SRMBidder, SRMBidderPartialMIP> bidderPartialMips;
    private SRMWorld world;
    private MIP mip;

    public SRM_MIP(Collection<SRMBidder> bidders) {
        Preconditions.checkNotNull(bidders);
        Preconditions.checkArgument(bidders.size() > 0);
        world = bidders.iterator().next().getWorld();
        mip = new MIP();
        mip.setSolveParam(SolveParam.RELATIVE_OBJ_GAP, 0.001);
        double scalingFactor = calculateScalingFactor(bidders);
        double biggestPossibleValue = biggestUnscaledPossibleValue(bidders).doubleValue() / scalingFactor;
        this.worldPartialMip = new SRMWorldPartialMip(
                bidders,
                biggestPossibleValue,
                scalingFactor);
        worldPartialMip.appendToMip(mip);
        bidderPartialMips = new HashMap<>();
        for (SRMBidder bidder : bidders) {
            SRMBidderPartialMIP bidderPartialMIP;
            bidderPartialMIP = new SRMBidderPartialMIP(bidder, worldPartialMip);
            bidderPartialMIP.appendToMip(mip);
            bidderPartialMips.put(bidder, bidderPartialMIP);
        }
    }

    public static double calculateScalingFactor(Collection<SRMBidder> bidders) {
        BigDecimal maxVal = biggestUnscaledPossibleValue(bidders);
        if (maxVal.compareTo(highestValidVal) < 0) {
            return 1;
        } else {
            System.out.println("Scaling MIP-CALC");
            return maxVal.divide(highestValidVal, RoundingMode.HALF_DOWN).doubleValue();
        }
    }

    /**
     * Returns the biggest possible value any of the passed bidders can have
     *
     * @return
     */
    public static BigDecimal biggestUnscaledPossibleValue(Collection<SRMBidder> bidders) {
        BigDecimal biggestValue = BigDecimal.ZERO;
        for (SRMBidder bidder : bidders) {
            BigDecimal val = bidder.calculateValue(new Bundle<>(bidder.getWorld().getLicenses()));
            if (val.compareTo(biggestValue) > 0) {
                biggestValue = val;
            }
        }
        return biggestValue;
    }

    public void addConstraint(Constraint constraint) {
        mip.add(constraint);
    }

    public void addVariable(Variable variable) {
        mip.add(variable);
    }


    /* (non-Javadoc)
     * @see EfficientAllocator#calculateEfficientAllocation()
     */
    @Override
    public SRMMipResult calculateAllocation() {
        IMIPResult mipResult = SOLVER.solve(mip);
        if (PRINT_SOLVER_RESULT) {
            System.out.println(mipResult);
        }
        SRMMipResult.Builder resultBuilder = new SRMMipResult.Builder(mipResult.getObjectiveValue(), world, mipResult);
        for (SRMBidder bidder : bidderPartialMips.keySet()) {
            double unscaledValue = 0;
            for (SRMBand band : world.getBands()) {
                Variable bidderVmVar = worldPartialMip.getVmVariable(bidder, band);
                double mipVmUtilityResult = mipResult.getValue(bidderVmVar);
                Variable bidderVoVar = worldPartialMip.getVoVariable(bidder, band);
                double mipVoUtilityResult = mipResult.getValue(bidderVoVar);
                double value = bidder.getInterbandSynergyValue().floatValue() * mipVmUtilityResult + mipVoUtilityResult;
                unscaledValue = value * worldPartialMip.getScalingFactor();
            }

            GenericValue.Builder<SRMBand> valueBuilder = new GenericValue.Builder<>(BigDecimal.valueOf(unscaledValue));
            for (SRMBand band : world.getBands()) {
                Variable xVar = worldPartialMip.getXVariable(bidder, band);
                double doubleQuantity = mipResult.getValue(xVar);
                int quantity = (int) Math.round(doubleQuantity);
                valueBuilder.putQuantity(band, quantity);
            }
            resultBuilder.putGenericValue(bidder, valueBuilder.build());
        }
        return resultBuilder.build();
    }

    public SRMWorldPartialMip getWorldPartialMip() {
        return worldPartialMip;
    }

    public Map<SRMBidder, SRMBidderPartialMIP> getBidderPartialMips() {
        return bidderPartialMips;
    }


}
