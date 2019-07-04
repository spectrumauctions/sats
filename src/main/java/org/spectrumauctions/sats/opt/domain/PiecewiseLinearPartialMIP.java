/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.opt.domain;

import edu.harvard.econcs.jopt.solver.IMIP;
import edu.harvard.econcs.jopt.solver.mip.CompareType;
import edu.harvard.econcs.jopt.solver.mip.Constraint;
import edu.harvard.econcs.jopt.solver.mip.VarType;
import edu.harvard.econcs.jopt.solver.mip.Variable;
import org.spectrumauctions.sats.core.util.math.ContinuousPiecewiseLinearFunction;
import org.spectrumauctions.sats.core.util.math.LinearFunction;
import org.spectrumauctions.sats.core.util.math.NotDifferentiableException;
import org.spectrumauctions.sats.core.util.random.DoubleInterval;

import java.math.BigDecimal;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Michael Weiss
 *
 */
public final class PiecewiseLinearPartialMIP extends PartialMIP {

    private final String linearPieceVariableName = "_Z";
    private final String conditionalXVariableName = "_condX";

    /**
     * A boolean auxiliary variable Z_i, 
     * one for each linear part of the piecewise linear function<br>
     * It is 1 iff the function input lies in the i'th linear part 
     * of the piecewise linear function
     * Note, the indexing of the variable name is in {1, 2, 3, ..., k}
     * and not in {0, 1, 2, ..., k-1} as in the file structure. Hence, avoid using this list 
     * directly, but use {@link PiecewiseLinearPartialMIP#getZVar(int)} instead
     */
    private List<Variable> linearPieceVariable;

    /**
     * A double auxiliary variable condX_i, 
     * one for each linear part of the piecewise linear function<br>
     * condX_i = X iff Z_i = 1, condX_i = 0 otherwise, where X is the input
     * of the piecewise linear function.<br><br>
     * Note, the indexing of the variable name is in {1, 2, 3, ..., k}
     * and not in {0, 1, 2, ..., k-1} as in the file structure. Hence, avoid using this list 
     * directly, but use {@link PiecewiseLinearPartialMIP#getConditionalXVar(int)} instead
     */
    private List<Variable> conditionalXVariable;

    private ContinuousPiecewiseLinearFunction func;
    private Variable functionInput;
    private Variable functionOutput;
    private String auxiliaryPartialName;
    private DoubleInterval inputRange;


    public PiecewiseLinearPartialMIP(ContinuousPiecewiseLinearFunction func,
                                     Variable functionInput,
                                     Variable functionOutput,
                                     String auxiliaryPartialName) {
        super();
        this.func = func;
        this.functionInput = functionInput;
        this.functionOutput = functionOutput;
        this.auxiliaryPartialName = auxiliaryPartialName;
        initAuxiliaryVariables();
    }

    private Variable getZVar(int piecenumber) {
        return linearPieceVariable.get(piecenumber - 1);
    }

    private Variable getConditionalXVar(int piecenumber) {
        return conditionalXVariable.get(piecenumber - 1);
    }


    private void initAuxiliaryVariables() {
        this.linearPieceVariable = new ArrayList<>();
        this.conditionalXVariable = new ArrayList<Variable>();
        List<SimpleImmutableEntry<BigDecimal, BigDecimal>> cornerPoints = func.getCornerPoints();
        for (int i = 1; i < cornerPoints.size(); i++) {
            String varName = auxiliaryPartialName.concat(linearPieceVariableName).concat(String.valueOf(i));
            this.linearPieceVariable.add(new Variable(varName, VarType.BOOLEAN, 0, 1));

            varName = auxiliaryPartialName.concat(conditionalXVariableName).concat(String.valueOf(i));
            this.conditionalXVariable.add(new Variable(varName, VarType.DOUBLE, this.functionInput.getLowerBound(), functionInput.getUpperBound()));
        }
    }


    /**
     * Returns a set of constraints which ensure that the auxiliary variables. 
     * The general concept of how this is done is explained in <a href="http://yetanothermathprogrammingconsultant.blogspot.ch/2015/10/piecewise-linear-functions-in-mip-models.html"> this blog post </a>
     * @return
     */
    public Set<Constraint> constrainAuxiliaryVariables() {
        Set<Constraint> result = new HashSet<>();
        List<SimpleImmutableEntry<BigDecimal, BigDecimal>> cornerPoints = func.getCornerPoints();
        // Ensure CornerX_{i-1} * Z_i <= condX_i <= CornerX_i * Z_i
        for (int i = 1; i < cornerPoints.size(); i++) {
            SimpleImmutableEntry<BigDecimal, BigDecimal> lowerCornerPoint = cornerPoints.get(i - 1);
            SimpleImmutableEntry<BigDecimal, BigDecimal> higherCornerPoint = cornerPoints.get(i);
            // First constraint CornerX_{i-1} * Z_i - condX_i <= 0
            Constraint lowerC = new Constraint(CompareType.LEQ, 0);
            lowerC.addTerm(lowerCornerPoint.getKey().doubleValue(), getZVar(i));
            lowerC.addTerm(-1, getConditionalXVar(i));
            result.add(lowerC);
            // Second constraint condX_i  - CornerX_{i} * Z_i - <= 0
            Constraint upperC = new Constraint(CompareType.LEQ, 0);
            upperC.addTerm(higherCornerPoint.getKey().doubleValue() * (-1), getZVar(i));
            upperC.addTerm(1, getConditionalXVar(i));
            result.add(upperC);
        }
        // Ensure that exactly one Z_i = 1
        Constraint zCount = new Constraint(CompareType.EQ, 1);
        for (int i = 1; i < cornerPoints.size(); i++) {
            zCount.addTerm(1, getZVar(i));
        }
        result.add(zCount);
        // Ensure sum of all conditionalX is exaclty equal to the input X       Constraint zCount = new Constraint(CompareType.EQ, 1);
        Constraint condXSum = new Constraint(CompareType.EQ, 0);
        condXSum.addTerm(-1, functionInput);
        for (int i = 1; i < cornerPoints.size(); i++) {
            condXSum.addTerm(1, getConditionalXVar(i));
        }
        result.add(condXSum);
        return result;
    }

    /**
     * Encodes the actual continuous piecewise linear function, i.e., the following term:<br>
     * Y = \sum_{i =1}^r cornerY_{i-1}Z_i + slope(i) * condX_i  - slope(i) * cornerX_{i-1} * Z_i
     *
     * @see
     * <a href="http://yetanothermathprogrammingconsultant.blogspot.ch/2015/10/piecewise-linear-functions-in-mip-models.html"> this blog post </a>
     * @return
     */
    public Constraint constrainFunctionOutputVariable() {
        Constraint c = new Constraint(CompareType.EQ, 0);
        c.addTerm(-1, functionOutput);
        List<SimpleImmutableEntry<BigDecimal, BigDecimal>> cornerPoints = func.getCornerPoints();
        for (int i = 1; i < cornerPoints.size(); i++) {
            BigDecimal lowerX = cornerPoints.get(i - 1).getKey();
            BigDecimal higherX = cornerPoints.get(i).getKey();
            LinearFunction inbetweenFunction;
            try {
                inbetweenFunction = func.functionAt(higherX);
                throw new RuntimeException("point is a corner point. A NotDifferentiableException should be thrown");
            } catch (NotDifferentiableException e) {
                //This exception should be thrown, das we are requesting the function for a corner point.
                inbetweenFunction = e.getLowerAdjacentFunction();
            }
            double yOfLowerX = func.getY(lowerX).doubleValue();
            double slope = inbetweenFunction.getSlope().doubleValue();

            //Add terms to function

            //Term 1: cornerY_{i-1} * Z_i
            c.addTerm(yOfLowerX, getZVar(i));
            //Term 2: slope * condX_i
            c.addTerm(slope, getConditionalXVar(i));
            //Term 3: slope * cornerX_{i-1} * Z_i
            c.addTerm((-1) * slope * lowerX.doubleValue(), getZVar(i));
        }
        return c;
    }


    @Override
    public void appendVariablesToMip(IMIP mip) {
        for (Variable var : getVariables()) {
            mip.add(var);
        }
    }

    @Override
    public Set<Variable> getVariables() {
        Set<Variable> vars = new HashSet<>();
        vars.addAll(super.getVariables());
        vars.addAll(linearPieceVariable);
        vars.addAll(conditionalXVariable);
        return vars;
    }

    @Override
    public void appendConstraintsToMip(IMIP mip) {
        super.appendConstraintsToMip(mip);
        for (Constraint constraint : constrainAuxiliaryVariables()) {
            mip.add(constraint);
        }
        mip.add(constrainFunctionOutputVariable());
    }


}
