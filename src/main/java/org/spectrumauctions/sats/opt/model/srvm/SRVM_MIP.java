/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.opt.model.srvm;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.common.math.DoubleMath;
import edu.harvard.econcs.jopt.solver.ISolution;
import edu.harvard.econcs.jopt.solver.SolveParam;
import edu.harvard.econcs.jopt.solver.client.SolverClient;
import edu.harvard.econcs.jopt.solver.mip.Constraint;
import edu.harvard.econcs.jopt.solver.mip.MIP;
import edu.harvard.econcs.jopt.solver.mip.Variable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.marketdesignresearch.mechlib.domain.Allocation;
import org.marketdesignresearch.mechlib.domain.BidderAllocation;
import org.marketdesignresearch.mechlib.domain.Bundle;
import org.marketdesignresearch.mechlib.domain.BundleEntry;
import org.marketdesignresearch.mechlib.domain.bid.Bids;
import org.marketdesignresearch.mechlib.domain.bidder.Bidder;
import org.marketdesignresearch.mechlib.mechanisms.MetaInfo;
import org.spectrumauctions.sats.core.model.srvm.SRVMBand;
import org.spectrumauctions.sats.core.model.srvm.SRVMBidder;
import org.spectrumauctions.sats.core.model.srvm.SRVMWorld;
import org.spectrumauctions.sats.opt.model.ModelMIP;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Fabio Isler
 */
public class SRVM_MIP extends ModelMIP {

    private static final Logger logger = LogManager.getLogger(SRVM_MIP.class);

    public static boolean PRINT_SOLVER_RESULT = false;

    private static SolverClient SOLVER = new SolverClient();

    private double scalingFactor;

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
        getMIP().setSolveParam(SolveParam.RELATIVE_OBJ_GAP, 0.001);
        scalingFactor = calculateScalingFactor(bidders);
        double biggestPossibleValue = biggestUnscaledPossibleValue(bidders).doubleValue() / scalingFactor;
        this.worldPartialMip = new SRVMWorldPartialMip(
                bidders,
                biggestPossibleValue,
                scalingFactor);
        worldPartialMip.appendToMip(getMIP());
        bidderPartialMips = new HashMap<>();
        for (SRVMBidder bidder : bidders) {
            SRVMBidderPartialMIP bidderPartialMIP;
            bidderPartialMIP = new SRVMBidderPartialMIP(bidder, worldPartialMip);
            bidderPartialMIP.appendToMip(getMIP());
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
            BigDecimal val = bidder.calculateValue(Bundle.of(bidder.getWorld().getLicenses()));
            if (val.compareTo(biggestValue) > 0) {
                biggestValue = val;
            }
        }
        return biggestValue;
    }

    public void addConstraint(Constraint constraint) {
        getMIP().add(constraint);
    }

    public void addVariable(Variable variable) {
        getMIP().add(variable);
    }


    @Override
    public ModelMIP getMIPWithout(Bidder bidder) {
        SRVMBidder srvmBidder = (SRVMBidder) bidder;
        Preconditions.checkArgument(bidderPartialMips.containsKey(srvmBidder));
        return new SRVM_MIP(bidderPartialMips.keySet().stream().filter(b -> !b.equals(srvmBidder)).collect(Collectors.toSet()));
    }

    /* (non-Javadoc)
     * @see EfficientAllocator#calculateEfficientAllocation()
     */
    @Override
    public Allocation adaptMIPResult(ISolution solution) {
        if (PRINT_SOLVER_RESULT) {
            logger.info("Result:\n" + solution);
        }
        Map<Bidder, BidderAllocation> bidderAllocationMap = new HashMap<>();
        for (SRVMBidder bidder : bidderPartialMips.keySet()) {
            double unscaledValue = 0;
            for (SRVMBand band : world.getBands()) {
                Variable bidderVmVar = worldPartialMip.getVmVariable(bidder, band);
                double mipVmUtilityResult = solution.getValue(bidderVmVar);
                Variable bidderVoVar = worldPartialMip.getVoVariable(bidder, band);
                double mipVoUtilityResult = solution.getValue(bidderVoVar);
                double value = bidder.getInterbandSynergyValue().floatValue() * mipVmUtilityResult + mipVoUtilityResult;
                unscaledValue += value * worldPartialMip.getScalingFactor();
            }

            Set<BundleEntry> bundleEntries = new HashSet<>();
            for (SRVMBand band : world.getBands()) {
                Variable xVar = worldPartialMip.getXVariable(bidder, band);
                double doubleQuantity = solution.getValue(xVar);
                int quantity = (int) Math.round(doubleQuantity);
                if (quantity > 0) {
                    bundleEntries.add(new BundleEntry(band, quantity));
                }
            }

            Bundle bundle = new Bundle(bundleEntries);
            BigDecimal value = bidder.getValue(bundle);
            if (!DoubleMath.fuzzyEquals(unscaledValue, value.doubleValue(), 1.0)) {
                logger.warn("MIP value of bidder {}: {}; Actual value: {}. With very high numbers, a " +
                                "deviation can happen. Make sure this is just a relatively small deviation, else check your MIP.",
                        bidder.getName(),
                        BigDecimal.valueOf(unscaledValue).setScale(4, RoundingMode.HALF_UP),
                        value.setScale(4, RoundingMode.HALF_UP));
            }
            if (!bundle.equals(Bundle.EMPTY)) {
                bidderAllocationMap.put(bidder, new BidderAllocation(value, bundle, new HashSet<>()));
            }
        }

        MetaInfo metaInfo = new MetaInfo();
        metaInfo.setNumberOfMIPs(1);
        metaInfo.setMipSolveTime(solution.getSolveTime());

        return new Allocation(bidderAllocationMap, new Bids(), metaInfo);
    }

    @Override
    public ModelMIP copyOf() {
        return new SRVM_MIP(bidderPartialMips.keySet());
    }

    @Override
    protected Collection<Collection<Variable>> getVariablesOfInterest() {
        Collection<Collection<Variable>> variablesOfInterest = new HashSet<>();
        for (SRVMBidder bidder : bidderPartialMips.keySet()) {
            for (SRVMBand band : world.getBands()) {
                variablesOfInterest.add(Sets.newHashSet(worldPartialMip.getXVariable(bidder, band)));
            }
        }
        return variablesOfInterest;
    }

    public SRVMWorldPartialMip getWorldPartialMip() {
        return worldPartialMip;
    }

    public Map<SRVMBidder, SRVMBidderPartialMIP> getBidderPartialMips() {
        return bidderPartialMips;
    }


}
