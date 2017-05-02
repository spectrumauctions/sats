/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.uzh.ifi.ce.mweiss.satsopt.model.mrm;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import com.google.common.base.Preconditions;

import ch.uzh.ifi.ce.mweiss.specval.bidlang.generic.GenericValue;
import ch.uzh.ifi.ce.mweiss.specval.model.Bundle;
import ch.uzh.ifi.ce.mweiss.specval.model.mrm.MRMBand;
import ch.uzh.ifi.ce.mweiss.specval.model.mrm.MRMBidder;
import ch.uzh.ifi.ce.mweiss.specval.model.mrm.MRMGenericDefinition;
import ch.uzh.ifi.ce.mweiss.specval.model.mrm.MRMGlobalBidder;
import ch.uzh.ifi.ce.mweiss.specval.model.mrm.MRMLicense;
import ch.uzh.ifi.ce.mweiss.specval.model.mrm.MRMLocalBidder;
import ch.uzh.ifi.ce.mweiss.specval.model.mrm.MRMRegionalBidder;
import ch.uzh.ifi.ce.mweiss.specval.model.mrm.MRMRegionsMap.Region;
import ch.uzh.ifi.ce.mweiss.specval.model.mrm.MRMWorld;
import ch.uzh.ifi.ce.mweiss.satsopt.model.EfficientAllocator;
import ch.uzh.ifi.ce.mweiss.satsopt.model.GenericAllocation;
import edu.harvard.econcs.jopt.solver.IMIPResult;
import edu.harvard.econcs.jopt.solver.SolveParam;
import edu.harvard.econcs.jopt.solver.client.SolverClient;
import edu.harvard.econcs.jopt.solver.mip.Constraint;
import edu.harvard.econcs.jopt.solver.mip.MIP;
import edu.harvard.econcs.jopt.solver.mip.Variable;

/**
 * @author Michael Weiss
 *
 */
public class MRM_MIP implements EfficientAllocator<GenericAllocation<MRMGenericDefinition>>{
    
    public static boolean PRINT_SOLVER_RESULT = false;
    
    private static SolverClient SOLVER = new SolverClient();
    
    /**
     * If the highest possible value any bidder can have is higher than {@link MIP#MAX_VALUE} - MAXVAL_SAFETYGAP}
     * a non-zero scaling factor for the calculation is chosen.
     */
    public static BigDecimal highestValidVal = BigDecimal.valueOf(MIP.MAX_VALUE - 1000000);
    private WorldPartialMip worldPartialMip;
    private Map<MRMBidder, BidderPartialMIP> bidderPartialMips;
    private MRMWorld world;
    private MIP mip;
    
    public MRM_MIP(Collection<MRMBidder> bidders){
        Preconditions.checkNotNull(bidders);
        Preconditions.checkArgument(bidders.size() > 0);
        world = bidders.iterator().next().getWorld();
        mip = new MIP();
        mip.setSolveParam(SolveParam.RELATIVE_OBJ_GAP, 0.001);
        double scalingFactor = calculateScalingFactor(bidders);
        double biggestPossibleValue = biggestUnscaledPossibleValue(bidders).doubleValue() / scalingFactor;
        this.worldPartialMip = new WorldPartialMip(
                bidders, 
                biggestPossibleValue,
                scalingFactor                );
        double svscalingFactor = calculateSVScalingFactor(bidders);
        worldPartialMip.appendToMip(mip);
        bidderPartialMips = new HashMap<>();
        for(MRMBidder bidder : bidders){
            BidderPartialMIP bidderPartialMIP;
            if(bidder instanceof MRMGlobalBidder){
                MRMGlobalBidder globalBidder = (MRMGlobalBidder) bidder;
                bidderPartialMIP = new GlobalBidderPartialMip(globalBidder, svscalingFactor, worldPartialMip);
            }else if (bidder instanceof MRMLocalBidder){
                MRMLocalBidder globalBidder = (MRMLocalBidder) bidder;
                bidderPartialMIP = new LocalBidderPartialMip(globalBidder, svscalingFactor, worldPartialMip);
            }else{
                MRMRegionalBidder globalBidder = (MRMRegionalBidder) bidder;
                bidderPartialMIP = new RegionalBidderPartialMip(globalBidder, svscalingFactor, worldPartialMip);
            }  
            bidderPartialMIP.appendToMip(mip);
            bidderPartialMips.put(bidder, bidderPartialMIP);
        }
    }

    private static double calculateSVScalingFactor(Collection<MRMBidder> bidders) {
        MRMBidder biggestAlphaBidder = bidders.stream().max(Comparator.comparing(b -> b.getAlpha())).get();
        Region biggestRegion = bidders.stream().findAny().get().getWorld().getRegionsMap().getRegions().stream()
                .max(Comparator.comparing(r -> r.getPopulation())).get();
        BigDecimal biggestAlpha = biggestAlphaBidder.getAlpha();
        BigDecimal biggestPopulation = BigDecimal.valueOf(biggestRegion.getPopulation());
        BigDecimal biggestC = bidders.stream().findAny().get().getWorld().getMaximumRegionalCapacity();
        BigDecimal securityBuffer = BigDecimal.valueOf(100000);
        BigDecimal biggestSv =  biggestAlpha.multiply(biggestPopulation).multiply(biggestC).add(securityBuffer);
        BigDecimal MIP_MAX_VALUE = BigDecimal.valueOf(MIP.MAX_VALUE);
        return biggestSv.divide(MIP_MAX_VALUE, RoundingMode.HALF_DOWN).doubleValue();

    }

    public static double calculateScalingFactor(Collection<MRMBidder> bidders){
        BigDecimal maxVal = biggestUnscaledPossibleValue(bidders);
        if(maxVal.compareTo(highestValidVal) < 0){
            return 1;
        }else{
            System.out.println("Scaling MIP-CALC");
            return  maxVal.divide(highestValidVal, RoundingMode.HALF_DOWN).doubleValue();           
        }
    }
    
    /**
     * Returns the biggest possible value any of the passed bidders can have
     * @return
     */
    public static BigDecimal biggestUnscaledPossibleValue(Collection<MRMBidder> bidders){
        BigDecimal biggestValue = BigDecimal.ZERO;
        for(MRMBidder bidder : bidders){
            BigDecimal val = bidder.calculateValue(new Bundle<MRMLicense>(bidder.getWorld().getLicenses()));
            if(val.compareTo(biggestValue) > 0){
                biggestValue = val;
            }
        }
        return biggestValue;
    }
    public void addConstraint(Constraint constraint){
        mip.add(constraint);
    }
    
    public void addVariable(Variable variable){
        mip.add(variable);
    }
    
    
    /* (non-Javadoc)
     * @see ch.uzh.ifi.ce.mweiss.satsopt.model.EfficientAllocator#calculateEfficientAllocation()
     */
    @Override
    public MipResult calculateAllocation() {
        IMIPResult mipResult = SOLVER.solve(mip);
        if(PRINT_SOLVER_RESULT) {
            System.out.println(mipResult);
        }
        MipResult.Builder resultBuilder = new MipResult.Builder(mipResult.getObjectiveValue(), world, mipResult);
        for(Map.Entry<MRMBidder, BidderPartialMIP> bidder : bidderPartialMips.entrySet()){
            Variable bidderValueVar = worldPartialMip.getValueVariable(bidder.getKey());
            double mipUtilityResult = mipResult.getValue(bidderValueVar);
            double svScalingFactor = bidder.getValue().getSVScalingFactor();
            if(svScalingFactor != 1){
                System.out.println("Scaling SV Value with factor " + svScalingFactor);
            }
            double omegaScalingFactor = worldPartialMip.getScalingFactor();
            if(omegaScalingFactor != 1){
                System.out.println("Scaling Omega Value with factor " + omegaScalingFactor);
            }
            double unscaledValue = mipUtilityResult * omegaScalingFactor * svScalingFactor;
            GenericValue.Builder<MRMGenericDefinition> valueBuilder = new GenericValue.Builder<>(BigDecimal.valueOf(unscaledValue));
            for(Region region : world.getRegionsMap().getRegions()){
                for(MRMBand band : world.getBands()){
                    Variable xVar = worldPartialMip.getXVariable(bidder.getKey(), region, band);
                    double doubleQuantity = mipResult.getValue(xVar);
                    int quantity = (int) Math.round(doubleQuantity);
                    MRMGenericDefinition def = new MRMGenericDefinition(band, region);
                    valueBuilder.putQuantity(def, quantity);
                }
            }
            GenericValue<MRMGenericDefinition> build = valueBuilder.build();
            resultBuilder.putGenericValue(bidder.getKey(), build);
        }
        return resultBuilder.build();
    }

    public WorldPartialMip getWorldPartialMip() {
        return worldPartialMip;
    }

    public Map<MRMBidder, BidderPartialMIP> getBidderPartialMips() {
        return bidderPartialMips;
    }
    

}
