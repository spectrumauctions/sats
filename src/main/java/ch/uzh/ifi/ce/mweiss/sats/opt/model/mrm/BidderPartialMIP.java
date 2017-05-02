/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.uzh.ifi.ce.mweiss.sats.opt.model.mrm;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ch.uzh.ifi.ce.mweiss.sats.opt.imip.PiecewiseLinearPartialMIP;
import com.google.common.base.Preconditions;

import ch.uzh.ifi.ce.mweiss.sats.core.bidlang.generic.Band;
import ch.uzh.ifi.ce.mweiss.sats.core.model.Bidder;
import ch.uzh.ifi.ce.mweiss.sats.core.model.mrm.MRMBand;
import ch.uzh.ifi.ce.mweiss.sats.core.model.mrm.MRMBidder;
import ch.uzh.ifi.ce.mweiss.sats.core.model.mrm.MRMRegionsMap.Region;
import ch.uzh.ifi.ce.mweiss.sats.core.model.mrm.MRMWorld;
import ch.uzh.ifi.ce.mweiss.sats.core.util.math.ContinuousPiecewiseLinearFunction;
import ch.uzh.ifi.ce.mweiss.sats.opt.imip.PartialMIP;
import edu.harvard.econcs.jopt.solver.mip.CompareType;
import edu.harvard.econcs.jopt.solver.mip.Constraint;
import edu.harvard.econcs.jopt.solver.mip.MIP;
import edu.harvard.econcs.jopt.solver.mip.VarType;
import edu.harvard.econcs.jopt.solver.mip.Variable;

/**
 * @author Michael Weiss
 *
 */
public abstract class BidderPartialMIP extends PartialMIP{


    private static final String regionalOmegaPrefix = "aux_Omega";
    private static final String regionalCapacityFractionPrefix ="aux_c";
    private static final String regionalCapacityPrefix = "aux_cap";
    private static final String qualityPrefix = "aux_quality";
    private static final String regionalSVPrefix = "aux_sv";

    
    private Map<Region, Variable> omegaVariables;
    private Map<Region, Variable> cVariables;
    private Map<Region, Map<Band,Variable>> capVariables;
    private Map<Region, Variable> svVariables;
    protected final WorldPartialMip worldPartialMip;
    private final MRMBidder bidder;

    private final double svscaling;
    public BidderPartialMIP(MRMBidder bidder, double scalingFactor, WorldPartialMip worldMip){
        this.bidder = bidder;
        this.worldPartialMip = worldMip;
        this.svscaling = scalingFactor;
        initVariables();
    }
        
    private void initVariables() {
        this.omegaVariables = createOmegaVariables();
        this.cVariables = createCVariables();
        this.capVariables = createCapVariables();
        this.svVariables = createSVVariables();
    }

    private Map<Region,Variable> createSVVariables() {
        Map<Region, Variable> result = new HashMap<>();
        for(Region region : bidder.getWorld().getRegionsMap().getRegions()){
            String varName = regionalSVPrefix.concat(createIndex(bidder, region));
            Variable var = new Variable(varName, VarType.DOUBLE, 0, MIP.MAX_VALUE);
            result.put(region, var);
        }
        return result;
    }

    private Map<Region, Variable> createOmegaVariables() {
        Map<Region, Variable> result = new HashMap<>();
        for(Region region : bidder.getWorld().getRegionsMap().getRegions()){
            String varName = regionalOmegaPrefix.concat(createIndex(bidder, region));
            Variable var = new Variable(varName, VarType.DOUBLE, 0, MIP.MAX_VALUE);
            result.put(region, var);
        }
        return result;
    }

    private Map<Region, Variable> createCVariables() {
        Map<Region, Variable> result = new HashMap<>();
        for(Region region : bidder.getWorld().getRegionsMap().getRegions()){
            String varName = regionalCapacityFractionPrefix.concat(createIndex(bidder, region));
            Variable var = new Variable(varName, VarType.DOUBLE, 0, MIP.MAX_VALUE);
            result.put(region, var);
        }
        return result;
    }

    private Map<Region, Map<Band, Variable>> createCapVariables() {
        Map<Region, Map<Band, Variable>> result = new HashMap<>();
        for(Region region : bidder.getWorld().getRegionsMap().getRegions()){
            Map<Band, Variable> bandCapacityVariables = new HashMap<>();
            for(MRMBand band : bidder.getWorld().getBands()){
                String varName = regionalCapacityPrefix.concat(createIndex(bidder, region, band));
                Variable var = new Variable(varName, VarType.DOUBLE, 0, MIP.MAX_VALUE);
                bandCapacityVariables.put(band, var);
            }
            result.put(region, Collections.unmodifiableMap(bandCapacityVariables));
        }
        return result;
    }

    /**
     * @return
     * @throws NullPointerException if no variable is defined for this region
     */
    Variable getOmegaVariable(Region region){
        Variable var = omegaVariables.get(region);
        Preconditions.checkNotNull(var);
        return var;
    }
    
    /**
     * @return
     * @throws NullPointerException if no variable is defined for this region
     */
    Variable getCVariable(Region region){
        Variable var = cVariables.get(region);
        Preconditions.checkNotNull(var);
        return var;
    }
    
    /**
     * @return
     * @throws NullPointerException if no variable is defined for this region
     */
    Variable getCapVariable(Region region, Band band){
        Variable var = capVariables.get(region).get(band);
        Preconditions.checkNotNull(var);
        return var;
    }

    Variable getSVVariable(Region region) {
        Variable var = svVariables.get(region);
        Preconditions.checkNotNull(var);
        return var;
    }

    static String createIndex(Bidder<?> bidder, Region region){
        StringBuilder builder =  new StringBuilder("_b");
        builder.append(bidder.getId());
        builder.append(",r");
        builder.append(region.getId());
        return builder.toString();
    }
    
    static String createIndex(Bidder<?> bidder, Band band){
        StringBuilder builder =  new StringBuilder("_b");
        builder.append(bidder.getId());
        builder.append(",band_");
        builder.append(band.getName());
        return builder.toString();
    }
    
    static String createIndex(Bidder<?> bidder, Region region, Band band){
        StringBuilder builder =  new StringBuilder("_b");
        builder.append(bidder.getId());
        builder.append(",r");
        builder.append(region.getId());
        builder.append(",band_");
        builder.append(band.getName());
        return builder.toString();
    }

    Set<PartialMIP> generateSVConstraints(){
        Set<PartialMIP> result = new HashSet<>();

        for(Region region : bidder.getWorld().getRegionsMap().getRegions()) {
            Variable svInput = getCVariable(region);
            Variable svOutput = getSVVariable(region);
            ContinuousPiecewiseLinearFunction sv = bidder.svFunction(region);
            String helperVariablesPrefix = new StringBuilder("sv_function").append("_helpervar").append(createIndex(bidder, region)).append("_").toString();

            PiecewiseLinearPartialMIP piecewiseLinearPartialMIP = new PiecewiseLinearPartialMIP(
                    sv,
                    svInput,
                    svOutput,
                    helperVariablesPrefix);
            result.add(piecewiseLinearPartialMIP);
        }
        return result;
    }


    /**
     * Encodes the equations
     * <p><b>
     *     \Omega_{i,r} = \beta_{i,r} * p_r * sv_{i,r}(c_{i,r})
     * </b></p>
     * as
     * <p><b>
     *      \beta_{i,r} * p_r * sv_{i,r}(c_{i,r}) - \Omega_{i_r} = 0
     * </b> </p>
     * @return
     */
    Set<Constraint> generateOmegaConstraints(){
        Set<Constraint> result = new HashSet<>();
        for(Region region : bidder.getWorld().getRegionsMap().getRegions()){
            double beta = bidder.getBeta(region).doubleValue();
            double population = region.getPopulation();
            double scaledFactor = beta * population / svscaling;
            Constraint omega = new Constraint(CompareType.EQ, 0);
            omega.addTerm(-1, getOmegaVariable(region));
            omega.addTerm(scaledFactor, getSVVariable(region));
            result.add(omega);
        }
        return result;
    }

    /**
     * Generates the constraints of the form <br>
     * c_{i,r} = sum_{b \in B} cap_{i,r,b}
     * i.e.,<br>
     * 0 = sum_{b \in B} cap_{i,r,b} - c_{i,r}
     *
     * @return
     */
    Set<Constraint> generateCConstraints(){
        Set<Constraint> result = new HashSet<>();
        for(Region region : bidder.getWorld().getRegionsMap().getRegions()){
            Constraint regionalCConstraint = new Constraint(CompareType.EQ, 0);
            regionalCConstraint.addTerm(-1, getCVariable(region));
            for(Band band : bidder.getWorld().getBands()){
                regionalCConstraint.addTerm(1, getCapVariable(region, band));
            }
            result.add(regionalCConstraint);
        }
        return result;
    }


    Set<PartialMIP> generateCapConstraints(){
        Set<PartialMIP> result = new HashSet<>();
        for(MRMBand band : bidder.getWorld().getBands()){
            ContinuousPiecewiseLinearFunction func = capacity(band);
            for(Region region : bidder.getWorld().getRegionsMap().getRegions()){
                Variable input = worldPartialMip.getXVariable(bidder, region, band);
                Variable output = getCapVariable(region, band);
                String auxiliaryVariableName = new StringBuilder("aux_cap_helper_")
                        .append(createIndex(bidder, region, band))
                        .append("_").
                        toString();
                PiecewiseLinearPartialMIP partialMip = 
                        new PiecewiseLinearPartialMIP(func, 
                                input, 
                                output, 
                                auxiliaryVariableName);
                result.add(partialMip);
            }
        }
        return result;
    }
    /**
     * Returns a continuous piecwise linear function which represents the capacity of a band for all possible input quantities.
     * @param band
     * @return
     */
    ContinuousPiecewiseLinearFunction capacity(MRMBand band){
        //Must ensure all BigDecimals have the same scale, as they are used as keys in a Map
        final int scale = 0;
        Map<BigDecimal, BigDecimal> breakpoints = new HashMap<>();
        breakpoints.put(BigDecimal.ZERO, MRMWorld.capOfBand(band, 0)); //
        BigDecimal lastSynergy = band.getSynergy(0);
        for(int quantity = 1; quantity < band.getNumberOfLots(); quantity++){
            BigDecimal synergy = band.getSynergy(quantity);
            if(synergy.compareTo(lastSynergy) != 0){
                // Synergy Breakpoint: Store both last quantity with previous
                // synergy (to account for piecewise constant synergies)
                // and new quantity in breakpoints
                BigDecimal lowerQuantityCapacity = MRMWorld.capOfBand(band, quantity-1);
                // Note, if there's only one capacity with the previous synergy,
                // an equivalent entry already exists and is overwritten in map
                BigDecimal key = new BigDecimal(quantity-1).setScale(scale);
                breakpoints.put(key, lowerQuantityCapacity);

                // Do the same for the new quantity
                key = new BigDecimal(quantity).setScale(scale);
                BigDecimal thisQuantityCapacity = MRMWorld.capOfBand(band, quantity);
                breakpoints.put(key, thisQuantityCapacity);
            }
        }
        //Add a breakpoint at the end of the function
        BigDecimal key = new BigDecimal(band.getNumberOfLots()).setScale(scale);
        BigDecimal thisQuantityCapacity = MRMWorld.capOfBand(band, band.getNumberOfLots());
        breakpoints.put(key, thisQuantityCapacity);

        ContinuousPiecewiseLinearFunction result = new ContinuousPiecewiseLinearFunction(breakpoints);
        return result;
    }


    public void appendVariablesToMip(MIP mip){
        super.appendVariablesToMip(mip);
        for(Variable var : omegaVariables.values()){
            mip.add(var);
        }
        for(Variable var : cVariables.values()){
            mip.add(var);
        }
        for(Variable var : svVariables.values()){
            mip.add(var);
        }
        for(Map<Band, Variable> innerMap : capVariables.values()){
            for(Variable var : innerMap.values()){
                mip.add(var); 
            }
        }
        for(PartialMIP partialMip : generateCapConstraints()){
            partialMip.appendVariablesToMip(mip);
        }
        for(PartialMIP partialMip : generateSVConstraints()){
            partialMip.appendVariablesToMip(mip);
        }
    }
    
    public void appendConstraintsToMip(MIP mip){
        super.appendConstraintsToMip(mip);
        for(Constraint constraint : generateOmegaConstraints()){
            mip.add(constraint);
        }
        for(Constraint constraint : generateCConstraints()){
            mip.add(constraint);
        }
        for(PartialMIP partialMip : generateCapConstraints()){
            partialMip.appendConstraintsToMip(mip);
        }
        for(PartialMIP partialMip : generateSVConstraints()){
            partialMip.appendConstraintsToMip(mip);
        }
    }


    public double getSVScalingFactor() {
        return svscaling;
    }
}
