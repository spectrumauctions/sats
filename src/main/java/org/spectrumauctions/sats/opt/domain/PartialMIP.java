/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.opt.domain;

import edu.harvard.econcs.jopt.solver.IMIP;
import edu.harvard.econcs.jopt.solver.mip.Constraint;
import edu.harvard.econcs.jopt.solver.mip.MIP;
import edu.harvard.econcs.jopt.solver.mip.Variable;

import java.util.HashSet;
import java.util.Set;

/**
 * A PartialMIP defines a set of constraints and auxiliary variables. 
 * A Mip can be formed from an objective function and one or many ParialMIP
 *
 * @author Michael Weiss
 *
 */
public class PartialMIP {

    private final Set<Variable> manuallyAddedVariables;

    private final Set<Constraint> manuallyAddedConstraints;


    protected PartialMIP() {
        super();
        this.manuallyAddedVariables = new HashSet<>();
        this.manuallyAddedConstraints = new HashSet<>();
    }

    /**
     * Use this function to manually add an additional {@link Variable} to this Partial MIP.<br>
     * Once a {@link Variable} is added, it cannot be removed anymore.
     * @param var The variable to add
     */
    public void addVariable(Variable var) {
        manuallyAddedVariables.add(var);
    }

    /**
     * Use this function to manually add an additional {@link Constraint} to this Partial MIP.
     * Once a {@link Constraint} is added, it cannot be removed anymore.
     * @param constraint The constraint to add
     */
    public void addConstraint(Constraint constraint) {
        manuallyAddedConstraints.add(constraint);
    }

    /**
     * Adds the generated variables and constraints to an existing {@link MIP} instance.
     * @param mip
     */
    public void appendToMip(IMIP mip) {
        appendVariablesToMip(mip);
        appendConstraintsToMip(mip);
    }

    public void appendVariablesToMip(IMIP mip) {
        for (Variable var : getVariables()) {
            mip.add(var);
        }
    }

    public Set<Variable> getVariables() {
        Set<Variable> vars = new HashSet<>();
        vars.addAll(manuallyAddedVariables);
        return vars;
    }

    public void appendConstraintsToMip(IMIP mip) {
        for (Constraint constraint : manuallyAddedConstraints) {
            mip.add(constraint);
        }
    }


}
