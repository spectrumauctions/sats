/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.opt.model.srvm;

import com.google.common.base.Preconditions;
import edu.harvard.econcs.jopt.solver.mip.*;
import org.spectrumauctions.sats.core.bidlang.generic.Band;
import org.spectrumauctions.sats.core.model.srvm.SRVMBand;
import org.spectrumauctions.sats.core.model.srvm.SRVMBidder;
import org.spectrumauctions.sats.core.model.srvm.SRVMWorld;
import org.spectrumauctions.sats.opt.imip.PartialMIP;

import java.util.*;

/**
 * The class generating the general allocation rules (variables and constraints)<br>
 * It also provides functions to get the allocation variables, used in {@link SRVMBidderPartialMIP} instances.
 *
 * @author Michael Weiss
 */
public class SRVMWorldPartialMip extends PartialMIP {

    public final static String xVariablePrefix = "X";
    public final static String vmVariablePrefix = "VM";
    public final static String voVariablePrefix = "VO";

    private final Map<SRVMBidder, Map<Band, Variable>> xVariables;
    private final Map<SRVMBidder, Map<Band, Variable>> vmVariables;
    private final Map<SRVMBidder, Map<Band, Variable>> voVariables;

    private final double biggestPossibleValue;

    private final Set<SRVMBidder> bidders;
    private final SRVMWorld world;
    private final double scalingFactor;

    /**
     * @param bidders2
     * @param biggestPossibleValue The highest (already scaled) value any bidder could have
     * @param scalingFactor
     */
    SRVMWorldPartialMip(Collection<SRVMBidder> bidders2, double biggestPossibleValue, double scalingFactor) {
        super();
        Preconditions.checkNotNull(bidders2);
        Preconditions.checkArgument(bidders2.size() > 0);
        Preconditions.checkArgument(biggestPossibleValue <= MIP.MAX_VALUE);
        this.biggestPossibleValue = biggestPossibleValue;
        this.scalingFactor = scalingFactor;
        this.bidders = Collections.unmodifiableSet(new HashSet<>(bidders2));
        world = bidders2.iterator().next().getWorld();
        Preconditions.checkNotNull(world);

        xVariables = initXVariables();
        vmVariables = initValueVariables(vmVariablePrefix);
        voVariables = initValueVariables(voVariablePrefix);
    }

    private Set<Constraint> createNumberOfLicensesConstraints() {
        Set<Constraint> result = new HashSet<>();
        for (SRVMBand band : world.getBands()) {
            Constraint numberOfLotsConstraint = new Constraint(CompareType.LEQ, band.getNumberOfLicenses());
            for (SRVMBidder bidder : bidders) {
                Variable xVar = getXVariable(bidder, band);
                numberOfLotsConstraint.addTerm(1, xVar);
            }
            result.add(numberOfLotsConstraint);
        }
        return result;

    }

    private Map<SRVMBidder, Map<Band, Variable>> initValueVariables(String prefix) {
        Map<SRVMBidder, Map<Band, Variable>> result = new HashMap<>();
        for (SRVMBidder bidder : bidders) {
            Map<Band, Variable> temp = new HashMap<>();
            for (SRVMBand band : world.getBands()) {
                String varName = prefix + "_" + bidder.getId() + "_" + band.getName();
                Variable var = new Variable(varName, VarType.DOUBLE, 0, MIP.MAX_VALUE);
                temp.put(band, var);
            }
            result.put(bidder, temp);
        }
        return Collections.unmodifiableMap(result);
    }

    private Map<SRVMBidder, Map<Band, Variable>> initXVariables() {
        Map<SRVMBidder, Map<Band, Variable>> result = new HashMap<>();

        for (SRVMBidder bidder : bidders) {
            Map<Band, Variable> bandMap = new HashMap<>();
            for (SRVMBand band : world.getBands()) {
                String varName = xVariablePrefix.concat(SRVMBidderPartialMIP.createIndex(bidder, band));
                Variable var = new Variable(varName, VarType.INT, 0, band.getNumberOfLicenses());
                bandMap.put(band, var);
            }
            result.put(bidder, Collections.unmodifiableMap(bandMap));
        }
        return Collections.unmodifiableMap(result);
    }

    private void appendObjectiveToMip(MIP mip) {
        mip.setObjectiveMax(true);
        if ((mip.getLinearObjectiveTerms() != null && mip.getQuadraticObjectiveTerms() != null)
                || mip.getObjectiveTerms().size() != 0) {
            //TODO Log Warning
        }
        for (SRVMBidder bidder : bidders) {
            double syni = bidder.getInterbandSynergyValue().floatValue();
            for (SRVMBand band : world.getBands()) {

                // Add VM variables
                mip.addObjectiveTerm(syni, vmVariables.get(bidder).get(band));

                // Add VO variables
                mip.addObjectiveTerm(1, voVariables.get(bidder).get(band));

            }
        }
    }

    /**
     * {@inheritDoc}
     * Furthermore, this implementation of a PartialMip adds the objective term to the MIP
     */
    @Override
    public void appendToMip(MIP mip) {
        super.appendToMip(mip);
        appendObjectiveToMip(mip);
    }


    /* (non-Javadoc)
     * @see PartialMIP#appendConstraintsToMip(edu.harvard.econcs.jopt.solver.mip.MIP)
     */
    @Override
    public void appendConstraintsToMip(MIP mip) {
        super.appendConstraintsToMip(mip);
        for (Constraint c : createNumberOfLicensesConstraints()) {
            mip.add(c);
        }
    }

    @Override
    public void appendVariablesToMip(MIP mip) {
        super.appendVariablesToMip(mip);
        for (Map.Entry<SRVMBidder, Map<Band, Variable>> bidderMapEntry : vmVariables.entrySet()) {
            for (Map.Entry<Band, Variable> bandVariableEntry : bidderMapEntry.getValue().entrySet()) {
                mip.add(bandVariableEntry.getValue());
            }
        }
        for (Map.Entry<SRVMBidder, Map<Band, Variable>> bidderMapEntry : voVariables.entrySet()) {
            for (Map.Entry<Band, Variable> bandVariableEntry : bidderMapEntry.getValue().entrySet()) {
                mip.add(bandVariableEntry.getValue());
            }
        }
        for (Map<Band, Variable> innerMap : xVariables.values()) {
            for (Variable var : innerMap.values()) {
                mip.add(var);
            }
        }
    }

    /**
     * @throws NullPointerException if the requested variable is not stored.
     */
    public Variable getXVariable(SRVMBidder bidder, Band band) {
        Variable var = xVariables.get(bidder).get(band);
        if (var == null) {
            throw new NullPointerException();
        }
        return var;
    }


    public Variable getVmVariable(SRVMBidder bidder, Band band) {
        Variable var = vmVariables.get(bidder).get(band);
        if (var == null) {
            throw new NullPointerException();
        }
        return var;
    }

    public Variable getVoVariable(SRVMBidder bidder, Band band) {
        Variable var = voVariables.get(bidder).get(band);
        if (var == null) {
            throw new NullPointerException();
        }
        return var;
    }

    /**
     * @return
     */
    public double getBiggestPossibleValue() {
        return biggestPossibleValue;
    }

    /**
     * @return
     */
    public double getScalingFactor() {
        return scalingFactor;
    }
}
