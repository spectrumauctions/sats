/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.opt.model.mrvm;

import com.google.common.base.Preconditions;
import edu.harvard.econcs.jopt.solver.mip.*;
import org.spectrumauctions.sats.core.model.SATSBidder;
import org.spectrumauctions.sats.core.model.mrvm.MRVMBand;
import org.spectrumauctions.sats.core.model.mrvm.MRVMNationalBidder;
import org.spectrumauctions.sats.core.model.mrvm.MRVMRegionsMap.Region;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Michael Weiss
 *
 */
public class MRVMNationalBidderPartialMip extends MRVMBidderPartialMIP {

    public static final String W_ir_VARIABLE_PREFIX = "GlobalBidder_W_ir_";

    private static final String W_i_VARIABLE_PREFIX = "GlobalBidder_W_i_";

    private static final String W_hat_ik_VARIABLE_PREFIX = "GlobalBidder_hat{W}_";

    private static final String PSI_VARIABLE_PREFIX = "GlobalBidder_PSI_";


    private final MRVMNationalBidder bidder;
    private Map<Integer, Variable> psiVariables;
    private Map<Integer, Variable> wHatIKVariables;
    private Variable wIVariable;
    private Map<Region, Variable> wIRVariables;

    /**
     * @param bidder
     * @param worldMip
     */
    public MRVMNationalBidderPartialMip(MRVMNationalBidder bidder, double scalingFactor, MRVMWorldPartialMip worldMip) {
        super(bidder, scalingFactor, worldMip);
        this.bidder = bidder;
        psiVariables = createPsiVariables();
        wHatIKVariables = createwHatIKVariables();
        wIVariable = createWIVariable();
        wIRVariables = createWIRVariables();
    }

    /**
     * @return
     */
    private Map<Region, Variable> createWIRVariables() {
        Map<Region, Variable> result = new HashMap<>();
        for (Region region : bidder.getWorld().getRegionsMap().getRegions()) {
            String name = W_ir_VARIABLE_PREFIX.concat(createIndex(bidder, region));
            result.put(region, new Variable(name, VarType.BOOLEAN, 0, 1));
        }
        return result;
    }

    /**
     * @return
     */
    private Variable createWIVariable() {
        String name = W_i_VARIABLE_PREFIX.concat(String.valueOf(bidder.getLongId()));
        int numberOfRegions = bidder.getWorld().getRegionsMap().getNumberOfRegions();
        return new Variable(name, VarType.INT, 0, numberOfRegions);
    }

    /**
     * @return
     */
    private Map<Integer, Variable> createwHatIKVariables() {
        Map<Integer, Variable> result = new HashMap<>();
        for (int k = 0; k <= bidder.getKMax(); k++) {
            String name = W_hat_ik_VARIABLE_PREFIX.concat(createIndex(bidder, k));
            result.put(k, new Variable(name, VarType.BOOLEAN, 0, 1));
        }
        return result;
    }

    private static String createIndex(SATSBidder bidder, Integer k) {
        return "_i" + bidder.getLongId() +
                ",k" +
                k.toString();
    }

    /**
     * @return
     */
    private Map<Integer, Variable> createPsiVariables() {
        Map<Integer, Variable> result = new HashMap<>();
        for (int k = 0; k <= bidder.getKMax(); k++) {
            String name = PSI_VARIABLE_PREFIX.concat(createIndex(bidder, k));
            result.put(k, new Variable(name, VarType.DOUBLE, 0, MIP.MAX_VALUE));
        }
        return result;
    }

    /**
     * A boolean variable, which is 1 iff possesses at least 1 license in region
     * @param region
     */
    public Variable getWIRVariable(Region region) {
        Variable var = wIRVariables.get(region);
        Preconditions.checkNotNull(var);
        return var;
    }

    /**
     * A integer Variable, stating the number regions this bidder covers
     */
    public Variable getWIVariable() {
        return wIVariable;
    }

    /**
     * A boolean variable, which is 1 iff the agent has exactly k missing regions, for k \in {0,...,k_{max}}
     * @param k
     * @return
     */
    public Variable getWHatIKVariable(int k) {
        return wHatIKVariables.get(k);
    }

    /**
     * A variable static total non-discounted value when missing exactly k regions for k less than k_{max} or k or more
     * regions for k = k_{max} and zero otherwise.
     * @param k
     * @return
     */
    public Variable getPsi(int k) {
        return psiVariables.get(k);
    }

    private Constraint valueConstraint() {
        Constraint constraint = new Constraint(CompareType.EQ, 0);
        constraint.addTerm(-1, worldPartialMip.getValueVariable(bidder));
        for (int k = 0; k <= bidder.getKMax(); k++) {
            double discount = bidder.getGamma(k).doubleValue();
            constraint.addTerm(discount, getPsi(k));
        }
        return constraint;
    }

    /**
     * Encodes the following constraints: <br>
     * W_{i,r} \leq \sum_{b \in B} X_{i,r,b}<br> and <br>
     * W_{i,r} \geq  \frac{1}{\sum{b \in B} n_b} \sum_{b \in B} X_{i,r,b}
     * @return
     */
    List<Constraint> constrainWIR() {
        List<Constraint> constraints = new ArrayList<>();
        int bigM = 0;
        for (MRVMBand band : bidder.getWorld().getBands()) {
            bigM += band.available();
        }
        double smallM = 1d / bigM;

        for (Region region : bidder.getWorld().getRegionsMap().getRegions()) {
            // W_{i,r} - \sum_{b\in B} X_{i,r,b} \leq 0
            Constraint constraintOne = new Constraint(CompareType.GEQ, 0);
            constraintOne.addTerm(-1, getWIRVariable(region));
            // W_{i,r} - smallM \cdot \sum\{b\in B} X_{i,r,b} \geq 0
            Constraint constraintTwo = new Constraint(CompareType.LEQ, 0);
            constraintTwo.addTerm(-1, getWIRVariable(region));

            for (MRVMBand band : bidder.getWorld().getBands()) {
                Variable xVariable = worldPartialMip.getXVariable(bidder, region, band);
                constraintOne.addTerm(1, xVariable);
                constraintTwo.addTerm(smallM, xVariable);
            }
            constraints.add(constraintOne);
            constraints.add(constraintTwo);
        }
        return constraints;
    }

    /**
     * Encode constraint <br>
     * W_i = \sum_{i,r} W_{i,r,b}
     * @return
     */
    Constraint constainWi() {
        Constraint constraint = new Constraint(CompareType.EQ, 0);
        constraint.addTerm(-1, getWIVariable());
        for (Region region : bidder.getWorld().getRegionsMap().getRegions()) {
            constraint.addTerm(1, getWIRVariable(region));
        }
        return constraint;
    }

    /**
     * Encodes the following constraints : <br>
     *  W_i + M \cdot \hat{W}_{i,k} \leq M + |R| - k      \forall  k\in \{0,...,k_{max}-1\}
     * <br>and<br>
     *  W_i -M \dot \hat{W}_{i,k} \geq -M + |R| - k     \forall  k\in \{0,...,k_{max}-1\}
     * <br>and<br>
     * W_i + M \cdot \hat{W}_{i,k_{max}} \leq M + |R| - k_{max}
     * <br>and<br>
     * (|R| -k_{max} + 1) \hat{W}_{i,k_{max}} + W_i \geq \cdot  |R| - k_max + 1;
     * @return
     */
    List<Constraint> constrainWHat() {
        List<Constraint> constraints = new ArrayList<>();
        final int numberOfRegions = bidder.getWorld().getRegionsMap().getNumberOfRegions();
        int kMax = bidder.getKMax();
        for (int k = 0; k < kMax; k++) {
            double bigM = (double) numberOfRegions; //TODO Remove
            // Constraint W_i + M \cdot \hat{W}_{i,k} \leq M + |R| - k
            Constraint constraintOne = new Constraint(CompareType.LEQ, bigM + numberOfRegions - k);
            // Constraint W_i -M \dot \hat{W}_{i,k} \geq -M + |R| - k
            Constraint constraintTwo = new Constraint(CompareType.GEQ, ((-1) * bigM) + numberOfRegions - k);
            Variable WI = getWIVariable();
            constraintOne.addTerm(1, WI);
            constraintTwo.addTerm(1, WI);
            Variable wHat = getWHatIKVariable(k);
            constraintOne.addTerm(bigM, wHat);
            constraintTwo.addTerm((-1) * bigM, wHat);
            constraints.add(constraintOne);
            constraints.add(constraintTwo);
        }
        //
        // kMax-Equations
        //
        double bigM = (double) numberOfRegions; // TODO Remove
        // Constraint W_i + M \cdot \hat{W}_{i,k_{max}} \leq M + |R| - k_{max}
        Constraint constraintOne = new Constraint(CompareType.LEQ, bigM + numberOfRegions - kMax);
        Variable WI = getWIVariable();
        constraintOne.addTerm(1, WI);
        Variable wHat = getWHatIKVariable(kMax);
        constraintOne.addTerm(bigM, wHat);
        constraints.add(constraintOne);
        // Constraint (|R| -k_{max} + 1) \hat{W}_{i,k_{max}} + W_i \geq \cdot  |R| - k_max + 1;
        Constraint constraintTwo = new Constraint(CompareType.GEQ, numberOfRegions - kMax + 1);
        constraintTwo.addTerm(numberOfRegions - kMax + 1, wHat);
        constraintTwo.addTerm(1, WI);
        constraints.add(constraintTwo);

        return constraints;
    }

    List<Constraint> constrainPsi() {
        List<Constraint> result = new ArrayList<>();
        // Big M is the highest possible valuation a bidder can have. 
        final double bigM = worldPartialMip.getBiggestPossibleValue();
        // Constraint One: 
        // -M\hat{W}_{i,k} - \Psi_{i,k} + \sum{r \in R} \Omega_{i,r} \geq -M
        for (int k = 0; k <= bidder.getKMax(); k++) {
            Constraint constraint = new Constraint(CompareType.GEQ, (-1) * bigM);
            constraint.addTerm((-1) * bigM, getWHatIKVariable(k));
            constraint.addTerm(-1, getPsi(k));
            for (Region region : bidder.getWorld().getRegionsMap().getRegions()) {
                constraint.addTerm(1, getOmegaVariable(region));
            }
            result.add(constraint);
        }
        // Constraint Two: 
        // M\hat{W}_{i,k} - \Psi_{i,k} + \sum{r \in R} \Omega_{i,r} \leq M
        for (int k = 0; k <= bidder.getKMax(); k++) {
            Constraint constraint = new Constraint(CompareType.LEQ, bigM);
            constraint.addTerm(-1, getPsi(k));
            constraint.addTerm(bigM, getWHatIKVariable(k));
            for (Region region : bidder.getWorld().getRegionsMap().getRegions()) {
                constraint.addTerm(1, getOmegaVariable(region));
            }
            result.add(constraint);
        }
        // Constraint Three
        // Psi_{i,k} - M\hat{W}_{i,k} \leq 0
        for (int k = 0; k <= bidder.getKMax(); k++) {
            Constraint constraint = new Constraint(CompareType.LEQ, 0);
            constraint.addTerm(1, getPsi(k));
            constraint.addTerm((-1) * bigM, getWHatIKVariable(k));
            result.add(constraint);
        }
        return result;

    }


    public void appendVariablesToMip(MIP mip) {
        super.appendVariablesToMip(mip);
        for (Variable var : psiVariables.values()) {
            mip.add(var);
        }
        for (Variable var : wHatIKVariables.values()) {
            mip.add(var);
        }
        for (Variable var : wIRVariables.values()) {
            mip.add(var);
        }
        mip.add(wIVariable);
    }

    public void appendConstraintsToMip(MIP mip) {
        super.appendConstraintsToMip(mip);
        mip.add(valueConstraint());
        for (Constraint constraint : constrainWIR()) {
            mip.add(constraint);
        }
        mip.add(constainWi());
        for (Constraint constraint : constrainWHat()) {
            mip.add(constraint);
        }
        for (Constraint constraint : constrainPsi()) {
            mip.add(constraint);
        }
    }
}
