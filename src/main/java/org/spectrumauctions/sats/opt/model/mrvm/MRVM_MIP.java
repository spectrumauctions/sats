/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.opt.model.mrvm;

import com.google.common.base.Preconditions;
import edu.harvard.econcs.jopt.solver.IMIPResult;
import edu.harvard.econcs.jopt.solver.SolveParam;
import edu.harvard.econcs.jopt.solver.client.SolverClient;
import edu.harvard.econcs.jopt.solver.mip.Constraint;
import edu.harvard.econcs.jopt.solver.mip.MIP;
import edu.harvard.econcs.jopt.solver.mip.Variable;
import org.spectrumauctions.sats.core.bidlang.generic.GenericValue;
import org.spectrumauctions.sats.core.model.mrvm.*;
import org.spectrumauctions.sats.core.model.mrvm.MRVMRegionsMap.Region;
import org.spectrumauctions.sats.opt.model.EfficientAllocator;
import org.spectrumauctions.sats.opt.model.GenericAllocation;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Michael Weiss
 *
 */
public class MRVM_MIP implements EfficientAllocator<GenericAllocation<MRVMGenericDefinition>> {

    public static boolean PRINT_SOLVER_RESULT = false;

    private static SolverClient SOLVER = new SolverClient();

    /**
     * If the highest possible value any bidder can have is higher than {@link MIP#MAX_VALUE} - MAXVAL_SAFETYGAP}
     * a non-zero scaling factor for the calculation is chosen.
     */
    private MRVMWorldPartialMip worldPartialMip;
    private Map<MRVMBidder, MRVMBidderPartialMIP> bidderPartialMips;
    private MRVMWorld world;
    private MIP mip;

    public MRVM_MIP(Collection<MRVMBidder> bidders) {
        Preconditions.checkNotNull(bidders);
        Preconditions.checkArgument(bidders.size() > 0);
        world = bidders.iterator().next().getWorld();
        mip = new MIP();
        mip.setSolveParam(SolveParam.RELATIVE_OBJ_GAP, 0.001);
        double scalingFactor = Scalor.scalingFactor(bidders);
        double biggestPossibleValue = Scalor.biggestUnscaledPossibleValue(bidders).doubleValue() / scalingFactor;
        this.worldPartialMip = new MRVMWorldPartialMip(
                bidders,
                biggestPossibleValue);
        worldPartialMip.appendToMip(mip);
        bidderPartialMips = new HashMap<>();
        for (MRVMBidder bidder : bidders) {
            MRVMBidderPartialMIP bidderPartialMIP;
            if (bidder instanceof MRVMNationalBidder) {
                MRVMNationalBidder globalBidder = (MRVMNationalBidder) bidder;
                bidderPartialMIP = new MRVMNationalBidderPartialMip(globalBidder, scalingFactor, worldPartialMip);
            } else if (bidder instanceof MRVMLocalBidder) {
                MRVMLocalBidder globalBidder = (MRVMLocalBidder) bidder;
                bidderPartialMIP = new MRVMLocalBidderPartialMip(globalBidder, scalingFactor, worldPartialMip);
            } else {
                MRVMRegionalBidder globalBidder = (MRVMRegionalBidder) bidder;
                bidderPartialMIP = new MRVMRegionalBidderPartialMip(globalBidder, scalingFactor, worldPartialMip);
            }
            bidderPartialMIP.appendToMip(mip);
            bidderPartialMips.put(bidder, bidderPartialMIP);
        }
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
    public MRVMMipResult calculateAllocation() {
        IMIPResult mipResult = SOLVER.solve(mip);
        if (PRINT_SOLVER_RESULT) {
            System.out.println(mipResult);
        }
        MRVMMipResult.Builder resultBuilder = new MRVMMipResult.Builder(mipResult.getObjectiveValue(), world, mipResult);
        for (Map.Entry<MRVMBidder, MRVMBidderPartialMIP> bidder : bidderPartialMips.entrySet()) {
            Variable bidderValueVar = worldPartialMip.getValueVariable(bidder.getKey());
            double mipUtilityResult = mipResult.getValue(bidderValueVar);
            double svScalingFactor = bidder.getValue().getScalingFactor();
//            if (svScalingFactor != 1) {
//                System.out.println("Scaling SV Value with factor " + svScalingFactor);
//            }
            double unscaledValue = mipUtilityResult * svScalingFactor;
            GenericValue.Builder<MRVMGenericDefinition> valueBuilder = new GenericValue.Builder<>(BigDecimal.valueOf(unscaledValue));
            for (Region region : world.getRegionsMap().getRegions()) {
                for (MRVMBand band : world.getBands()) {
                    Variable xVar = worldPartialMip.getXVariable(bidder.getKey(), region, band);
                    double doubleQuantity = mipResult.getValue(xVar);
                    int quantity = (int) Math.round(doubleQuantity);
                    MRVMGenericDefinition def = new MRVMGenericDefinition(band, region);
                    valueBuilder.putQuantity(def, quantity);
                }
            }
            GenericValue<MRVMGenericDefinition> build = valueBuilder.build();
            resultBuilder.putGenericValue(bidder.getKey(), build);
        }
        return resultBuilder.build();
    }

    public MRVMWorldPartialMip getWorldPartialMip() {
        return worldPartialMip;
    }

    public Map<MRVMBidder, MRVMBidderPartialMIP> getBidderPartialMips() {
        return bidderPartialMips;
    }


}
