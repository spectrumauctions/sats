/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.opt.model.srvm;

import com.google.common.base.Preconditions;
import edu.harvard.econcs.jopt.solver.IMIPResult;
import edu.harvard.econcs.jopt.solver.SolveParam;
import edu.harvard.econcs.jopt.solver.client.SolverClient;
import edu.harvard.econcs.jopt.solver.mip.Constraint;
import edu.harvard.econcs.jopt.solver.mip.MIP;
import edu.harvard.econcs.jopt.solver.mip.Variable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spectrumauctions.sats.core.bidlang.generic.GenericValue;
import org.spectrumauctions.sats.core.model.Bundle;
import org.spectrumauctions.sats.core.model.srvm.SRVMBand;
import org.spectrumauctions.sats.core.model.srvm.SRVMBidder;
import org.spectrumauctions.sats.core.model.srvm.SRVMWorld;
import org.spectrumauctions.sats.opt.domain.GenericAllocation;
import org.spectrumauctions.sats.opt.domain.WinnerDeterminator;
import org.spectrumauctions.sats.opt.model.ModelMIP;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Fabio Isler
 */
public class SRVM_MIP extends ModelMIP implements WinnerDeterminator<GenericAllocation<SRVMBand>> {

    private static final Logger logger = LogManager.getLogger(SRVM_MIP.class);

    public static boolean PRINT_SOLVER_RESULT = false;

    private static SolverClient SOLVER = new SolverClient();

    /**
     * If the highest possible value any bidder can have is higher than {@link MIP#MAX_VALUE} - MAXVAL_SAFETYGAP}
     * a non-zero scaling factor for the calculation is chosen.
     */
    public static BigDecimal highestValidVal = BigDecimal.valueOf(MIP.MAX_VALUE - 1000000);
    private SRVMWorldPartialMip worldPartialMip;
    private Map<SRVMBidder, SRVMBidderPartialMIP> bidderPartialMips;
    private SRVMWorld world;

    public SRVM_MIP(Collection<SRVMBidder> bidders) {
        Preconditions.checkNotNull(bidders);
        Preconditions.checkArgument(bidders.size() > 0);
        world = bidders.iterator().next().getWorld();
        getMip().setSolveParam(SolveParam.RELATIVE_OBJ_GAP, 0.001);
        double scalingFactor = calculateScalingFactor(bidders);
        double biggestPossibleValue = biggestUnscaledPossibleValue(bidders).doubleValue() / scalingFactor;
        this.worldPartialMip = new SRVMWorldPartialMip(
                bidders,
                biggestPossibleValue,
                scalingFactor);
        worldPartialMip.appendToMip(getMip());
        bidderPartialMips = new HashMap<>();
        for (SRVMBidder bidder : bidders) {
            SRVMBidderPartialMIP bidderPartialMIP;
            bidderPartialMIP = new SRVMBidderPartialMIP(bidder, worldPartialMip);
            bidderPartialMIP.appendToMip(getMip());
            bidderPartialMips.put(bidder, bidderPartialMIP);
        }
    }

    public static double calculateScalingFactor(Collection<SRVMBidder> bidders) {
        BigDecimal maxVal = biggestUnscaledPossibleValue(bidders);
        if (maxVal.compareTo(highestValidVal) < 0) {
            return 1;
        } else {
            logger.info("Scaling MIP-CALC");
            return maxVal.divide(highestValidVal, RoundingMode.HALF_DOWN).doubleValue();
        }
    }

    /**
     * Returns the biggest possible value any of the passed bidders can have
     *
     * @return
     */
    public static BigDecimal biggestUnscaledPossibleValue(Collection<SRVMBidder> bidders) {
        BigDecimal biggestValue = BigDecimal.ZERO;
        for (SRVMBidder bidder : bidders) {
            BigDecimal val = bidder.calculateValue(new Bundle<>(bidder.getWorld().getLicenses()));
            if (val.compareTo(biggestValue) > 0) {
                biggestValue = val;
            }
        }
        return biggestValue;
    }

    public void addConstraint(Constraint constraint) {
        getMip().add(constraint);
    }

    public void addVariable(Variable variable) {
        getMip().add(variable);
    }


    /* (non-Javadoc)
     * @see EfficientAllocator#calculateEfficientAllocation()
     */
    @Override
    public SRVMMipResult calculateAllocation() {
        IMIPResult mipResult = SOLVER.solve(getMip());
        if (PRINT_SOLVER_RESULT) {
            logger.info("Result:\n" + mipResult);
        }
        SRVMMipResult.Builder resultBuilder = new SRVMMipResult.Builder(mipResult.getObjectiveValue(), world, mipResult);
        for (SRVMBidder bidder : bidderPartialMips.keySet()) {
            double unscaledValue = 0;
            for (SRVMBand band : world.getBands()) {
                Variable bidderVmVar = worldPartialMip.getVmVariable(bidder, band);
                double mipVmUtilityResult = mipResult.getValue(bidderVmVar);
                Variable bidderVoVar = worldPartialMip.getVoVariable(bidder, band);
                double mipVoUtilityResult = mipResult.getValue(bidderVoVar);
                double value = bidder.getInterbandSynergyValue().floatValue() * mipVmUtilityResult + mipVoUtilityResult;
                unscaledValue = value * worldPartialMip.getScalingFactor();
            }

            GenericValue.Builder<SRVMBand> valueBuilder = new GenericValue.Builder<>(BigDecimal.valueOf(unscaledValue));
            for (SRVMBand band : world.getBands()) {
                Variable xVar = worldPartialMip.getXVariable(bidder, band);
                double doubleQuantity = mipResult.getValue(xVar);
                int quantity = (int) Math.round(doubleQuantity);
                valueBuilder.putQuantity(band, quantity);
            }
            resultBuilder.putGenericValue(bidder, valueBuilder.build());
        }
        return resultBuilder.build();
    }

    public SRVMWorldPartialMip getWorldPartialMip() {
        return worldPartialMip;
    }

    public Map<SRVMBidder, SRVMBidderPartialMIP> getBidderPartialMips() {
        return bidderPartialMips;
    }


}
