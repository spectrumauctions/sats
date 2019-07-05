/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.opt.model.mrvm;

import com.google.common.base.Preconditions;
import edu.harvard.econcs.jopt.solver.IMIP;
import edu.harvard.econcs.jopt.solver.mip.*;
import org.spectrumauctions.sats.core.model.SATSBidder;
import org.spectrumauctions.sats.core.model.mrvm.MRVMBand;
import org.spectrumauctions.sats.core.model.mrvm.MRVMBidder;
import org.spectrumauctions.sats.core.model.mrvm.MRVMRegionsMap.Region;
import org.spectrumauctions.sats.core.model.mrvm.MRVMWorld;
import org.spectrumauctions.sats.core.util.math.ContinuousPiecewiseLinearFunction;
import org.spectrumauctions.sats.opt.domain.PartialMIP;
import org.spectrumauctions.sats.opt.domain.PiecewiseLinearPartialMIP;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author Michael Weiss
 *
 */
public abstract class MRVMBidderPartialMIP extends PartialMIP {


    private static final String regionalOmegaPrefix = "aux_Omega";
    private static final String regionalCapacityFractionPrefix = "aux_c";
    private static final String regionalCapacityPrefix = "aux_cap";
    private static final String qualityPrefix = "aux_quality";
    private static final String regionalSVPrefix = "aux_sv";


    private Map<Region, Variable> omegaVariables;
    private Map<Region, Variable> cVariables;
    private Map<Region, Map<MRVMBand, Variable>> capVariables;
    private Map<Region, Variable> svVariables;
    protected final MRVMWorldPartialMip worldPartialMip;
    private final MRVMBidder bidder;

    private final double scaling;

    public MRVMBidderPartialMIP(MRVMBidder bidder, double scalingFactor, MRVMWorldPartialMip worldMip) {
        this.bidder = bidder;
        this.worldPartialMip = worldMip;
        this.scaling = scalingFactor;
        initVariables();
    }

    private void initVariables() {
        this.omegaVariables = createOmegaVariables();
        this.cVariables = createCVariables();
        this.capVariables = createCapVariables();
        this.svVariables = createSVVariables();
    }

    private Map<Region, Variable> createSVVariables() {
        Map<Region, Variable> result = new HashMap<>();
        for (Region region : bidder.getWorld().getRegionsMap().getRegions()) {
            String varName = regionalSVPrefix.concat(createIndex(bidder, region));
            Variable var = new Variable(varName, VarType.DOUBLE, 0, MIP.MAX_VALUE);
            result.put(region, var);
        }
        return result;
    }

    private Map<Region, Variable> createOmegaVariables() {
        Map<Region, Variable> result = new HashMap<>();
        for (Region region : bidder.getWorld().getRegionsMap().getRegions()) {
            String varName = regionalOmegaPrefix.concat(createIndex(bidder, region));
            Variable var = new Variable(varName, VarType.DOUBLE, 0, MIP.MAX_VALUE);
            result.put(region, var);
        }
        return result;
    }

    private Map<Region, Variable> createCVariables() {
        Map<Region, Variable> result = new HashMap<>();
        for (Region region : bidder.getWorld().getRegionsMap().getRegions()) {
            String varName = regionalCapacityFractionPrefix.concat(createIndex(bidder, region));
            Variable var = new Variable(varName, VarType.DOUBLE, 0, MIP.MAX_VALUE);
            result.put(region, var);
        }
        return result;
    }

    private Map<Region, Map<MRVMBand, Variable>> createCapVariables() {
        Map<Region, Map<MRVMBand, Variable>> result = new HashMap<>();
        for (Region region : bidder.getWorld().getRegionsMap().getRegions()) {
            Map<MRVMBand, Variable> bandCapacityVariables = new HashMap<>();
            for (MRVMBand band : bidder.getWorld().getBands()) {
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
    Variable getOmegaVariable(Region region) {
        Variable var = omegaVariables.get(region);
        Preconditions.checkNotNull(var);
        return var;
    }

    /**
     * @return
     * @throws NullPointerException if no variable is defined for this region
     */
    Variable getCVariable(Region region) {
        Variable var = cVariables.get(region);
        Preconditions.checkNotNull(var);
        return var;
    }

    /**
     * @return
     * @throws NullPointerException if no variable is defined for this region
     */
    Variable getCapVariable(Region region, MRVMBand band) {
        Variable var = capVariables.get(region).get(band);
        Preconditions.checkNotNull(var);
        return var;
    }

    Variable getSVVariable(Region region) {
        Variable var = svVariables.get(region);
        Preconditions.checkNotNull(var);
        return var;
    }

    static String createIndex(SATSBidder bidder, Region region) {
        return "_b" + bidder.getLongId() +
                ",r" +
                region.getId();
    }

    static String createIndex(SATSBidder bidder, MRVMBand band) {
        return "_b" + bidder.getLongId() +
                ",band_" +
                band.getId();
    }

    static String createIndex(SATSBidder bidder, Region region, MRVMBand band) {
        return "_b" + bidder.getLongId() +
                ",r" +
                region.getId() +
                ",band_" +
                band.getId();
    }

    Set<PartialMIP> generateSVConstraints() {
        Set<PartialMIP> result = new HashSet<>();

        for (Region region : bidder.getWorld().getRegionsMap().getRegions()) {
            Variable svInput = getCVariable(region);
            Variable svOutput = getSVVariable(region);
            ContinuousPiecewiseLinearFunction sv = bidder.svFunction(region);
            String helperVariablesPrefix = "sv_function_helpervar" + createIndex(bidder, region) + "_";

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
    Set<Constraint> generateOmegaConstraints() {
        Set<Constraint> result = new HashSet<>();
        for (Region region : bidder.getWorld().getRegionsMap().getRegions()) {
            double beta = bidder.getBeta(region).doubleValue();
            double population = region.getPopulation();
            double scaledFactor = beta * population / scaling;
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
    Set<Constraint> generateCConstraints() {
        Set<Constraint> result = new HashSet<>();
        for (Region region : bidder.getWorld().getRegionsMap().getRegions()) {
            Constraint regionalCConstraint = new Constraint(CompareType.EQ, 0);
            regionalCConstraint.addTerm(-1, getCVariable(region));
            for (MRVMBand band : bidder.getWorld().getBands()) {
                regionalCConstraint.addTerm(1, getCapVariable(region, band));
            }
            result.add(regionalCConstraint);
        }
        return result;
    }


    Set<PartialMIP> generateCapConstraints() {
        Set<PartialMIP> result = new HashSet<>();
        for (MRVMBand band : bidder.getWorld().getBands()) {
            ContinuousPiecewiseLinearFunction func = capLinearFunction(band);
            for (Region region : bidder.getWorld().getRegionsMap().getRegions()) {
                Variable input = worldPartialMip.getXVariable(bidder, region, band);
                Variable output = getCapVariable(region, band);
                String auxiliaryVariableName = "aux_cap_helper_" +
                        createIndex(bidder, region, band) +
                        "_";
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
    ContinuousPiecewiseLinearFunction capLinearFunction(MRVMBand band) {
        //Must ensure all BigDecimals have the same scale, as they are used as keys in a Map
        Map<BigDecimal, BigDecimal> breakpoints = new HashMap<>();
        breakpoints.put(BigDecimal.ZERO, capAt(band, 0)); //
        BigDecimal lastSynergy = band.getSynergy(0);
        for (int quantity = 1; quantity <= band.getNumberOfLots(); quantity++) {
            //Quick and dirty approach - Adding all quantities
            breakpoints.put(BigDecimal.valueOf(quantity), capAt(band,quantity));
        }
        ContinuousPiecewiseLinearFunction result = new ContinuousPiecewiseLinearFunction(breakpoints);
        return result;
    }

    private BigDecimal capAt(MRVMBand band, int quantity){
        return MRVMWorld.capOfBand(band, quantity);
    }


    @Override
    public void appendVariablesToMip(IMIP mip) {
        super.appendVariablesToMip(mip);
        for (Variable var : omegaVariables.values()) {
            mip.add(var);
        }
        for (Variable var : cVariables.values()) {
            mip.add(var);
        }
        for (Variable var : svVariables.values()) {
            mip.add(var);
        }
        for (Map<MRVMBand, Variable> innerMap : capVariables.values()) {
            for (Variable var : innerMap.values()) {
                mip.add(var);
            }
        }
        for (PartialMIP partialMip : generateCapConstraints()) {
            partialMip.appendVariablesToMip(mip);
        }
        for (PartialMIP partialMip : generateSVConstraints()) {
            partialMip.appendVariablesToMip(mip);
        }
    }

    @Override
    public void appendConstraintsToMip(IMIP mip) {
        super.appendConstraintsToMip(mip);
        for (Constraint constraint : generateOmegaConstraints()) {
            mip.add(constraint);
        }
        for (Constraint constraint : generateCConstraints()) {
            mip.add(constraint);
        }
        for (PartialMIP partialMip : generateCapConstraints()) {
            partialMip.appendConstraintsToMip(mip);
        }
        for (PartialMIP partialMip : generateSVConstraints()) {
            partialMip.appendConstraintsToMip(mip);
        }
    }


    public double getScalingFactor() {
        return scaling;
    }
}
