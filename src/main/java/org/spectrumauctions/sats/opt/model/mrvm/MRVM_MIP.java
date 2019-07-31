/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.opt.model.mrvm;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.common.math.DoubleMath;
import edu.harvard.econcs.jopt.solver.ISolution;
import edu.harvard.econcs.jopt.solver.mip.Constraint;
import edu.harvard.econcs.jopt.solver.mip.MIP;
import edu.harvard.econcs.jopt.solver.mip.Variable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.marketdesignresearch.mechlib.core.Allocation;
import org.marketdesignresearch.mechlib.core.BidderAllocation;
import org.marketdesignresearch.mechlib.core.Bundle;
import org.marketdesignresearch.mechlib.core.BundleEntry;
import org.marketdesignresearch.mechlib.core.bid.Bids;
import org.marketdesignresearch.mechlib.core.bidder.Bidder;
import org.marketdesignresearch.mechlib.instrumentation.MipInstrumentation;
import org.marketdesignresearch.mechlib.metainfo.MetaInfo;
import org.spectrumauctions.sats.core.model.mrvm.*;
import org.spectrumauctions.sats.core.model.mrvm.MRVMRegionsMap.Region;
import org.spectrumauctions.sats.opt.model.ModelMIP;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Michael Weiss
 */
public class MRVM_MIP extends ModelMIP {

    private static final Logger logger = LogManager.getLogger(MRVM_MIP.class);

    public static boolean PRINT_SOLVER_RESULT = false;

    /**
     * If the highest possible value any bidder can have is higher than {@link MIP#MAX_VALUE} - MAXVAL_SAFETYGAP}
     * a non-zero scaling factor for the calculation is chosen.
     */
    private MRVMWorldPartialMip worldPartialMip;
    private Map<MRVMBidder, MRVMBidderPartialMIP> bidderPartialMips;
    private MRVMWorld world;
    private Collection<MRVMBidder> bidders;
    private double scalingFactor;

    public MRVM_MIP(Collection<MRVMBidder> bidders) {
        this(bidders, MipInstrumentation.MipPurpose.ALLOCATION, new MipInstrumentation());
    }

    public MRVM_MIP(Collection<MRVMBidder> bidders, MipInstrumentation.MipPurpose purpose, MipInstrumentation mipInstrumentation) {
        super(purpose, mipInstrumentation);
        Preconditions.checkNotNull(bidders);
        Preconditions.checkArgument(bidders.size() > 0);
        world = bidders.iterator().next().getWorld();
        scalingFactor = Scalor.scalingFactor(bidders);
        double biggestPossibleValue = Scalor.biggestUnscaledPossibleValue(bidders).doubleValue() / scalingFactor;
        this.bidders = bidders;
        this.worldPartialMip = new MRVMWorldPartialMip(
                bidders,
                biggestPossibleValue);
        worldPartialMip.appendToMip(getMIP());
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
            bidderPartialMIP.appendToMip(getMIP());
            bidderPartialMips.put(bidder, bidderPartialMIP);
        }
    }


    public void addConstraint(Constraint constraint) {
        getMIP().add(constraint);
    }

    public void addVariable(Variable variable) {
        getMIP().add(variable);
    }

    public void addObjectiveTerm(double coefficient, Variable variable) {
        getMIP().addObjectiveTerm(coefficient, variable);
    }


    @Override
    public MRVM_MIP getMIPWithout(Bidder bidder) {
        MRVMBidder mrvmBidder = (MRVMBidder) bidder;
        Preconditions.checkArgument(bidders.contains(mrvmBidder));
        return new MRVM_MIP(bidders.stream().filter(b -> !b.equals(mrvmBidder)).collect(Collectors.toSet()), MipInstrumentation.MipPurpose.PAYMENT, getMipInstrumentation());
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
        for (Map.Entry<MRVMBidder, MRVMBidderPartialMIP> bidder : bidderPartialMips.entrySet()) {
            Variable bidderValueVar = worldPartialMip.getValueVariable(bidder.getKey());
            double mipUtilityResult = solution.getValue(bidderValueVar);
            double svScalingFactor = bidder.getValue().getScalingFactor();
//            if (svScalingFactor != 1) {
//                logger.info("Scaling SV Value with factor " + svScalingFactor);
//            }
            double unscaledValue = mipUtilityResult * svScalingFactor;
            Set<BundleEntry> bundleEntries = new HashSet<>();
            for (Region region : world.getRegionsMap().getRegions()) {
                for (MRVMBand band : world.getBands()) {
                    Variable xVar = worldPartialMip.getXVariable(bidder.getKey(), region, band);
                    double doubleQuantity = solution.getValue(xVar);
                    int quantity = (int) Math.round(doubleQuantity);
                    if (quantity > 0) {
                        MRVMGenericDefinition def = new MRVMGenericDefinition(band, region);
                        bundleEntries.add(new BundleEntry(def, quantity));
                    }
                }
            }
            Bundle bundle = new Bundle(bundleEntries);
            BigDecimal value = bidder.getKey().getValue(bundle);
            if (!DoubleMath.fuzzyEquals(unscaledValue, value.doubleValue(), 1.0)) {
                logger.warn("MIP value of bidder {}: {}; Actual value: {}. With very high numbers, a " +
                                "deviation can happen. Make sure this is just a relatively small deviation, else check your MIP.",
                        bidder.getKey().getName(),
                        BigDecimal.valueOf(unscaledValue).setScale(4, RoundingMode.HALF_UP),
                        value.setScale(4, RoundingMode.HALF_UP));
            }

            if (!Bundle.EMPTY.equals(bundle)) {
                bidderAllocationMap.put(bidder.getKey(), new BidderAllocation(value, bundle, new HashSet<>()));
            }
        }

        MetaInfo metaInfo = new MetaInfo();
        metaInfo.setNumberOfMIPs(1);
        metaInfo.setMipSolveTime(solution.getSolveTime());

        return new Allocation(bidderAllocationMap, new Bids(), metaInfo);
    }

    @Override
    public ModelMIP copyOf() {
        return new MRVM_MIP(bidders);
    }

    @Override
    protected Collection<Collection<Variable>> getVariablesOfInterest() {
        Collection<Collection<Variable>> variablesOfInterest = new HashSet<>();
        for (Variable variable : getXVariables()) {
            variablesOfInterest.add(Sets.newHashSet(variable));
        }
        return variablesOfInterest;
    }

    public MRVMWorldPartialMip getWorldPartialMip() {
        return worldPartialMip;
    }

    public Map<MRVMBidder, MRVMBidderPartialMIP> getBidderPartialMips() {
        return bidderPartialMips;
    }

    public Collection<Variable> getXVariables() {
        return bidders
                .stream()
                .map(b -> worldPartialMip.getXVariables(b))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
