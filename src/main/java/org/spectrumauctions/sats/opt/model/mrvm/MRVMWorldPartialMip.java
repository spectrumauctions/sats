/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.opt.model.mrvm;

import com.google.common.base.Preconditions;
import edu.harvard.econcs.jopt.solver.IMIP;
import edu.harvard.econcs.jopt.solver.mip.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spectrumauctions.sats.core.model.mrvm.MRVMBand;
import org.spectrumauctions.sats.core.model.mrvm.MRVMBidder;
import org.spectrumauctions.sats.core.model.mrvm.MRVMRegionsMap;
import org.spectrumauctions.sats.core.model.mrvm.MRVMRegionsMap.Region;
import org.spectrumauctions.sats.core.model.mrvm.MRVMWorld;
import org.spectrumauctions.sats.opt.domain.PartialMIP;

import java.util.*;

/**
 * The class generating the general allocation rules (variables and constraints)<br>
 * It also provides functions to get the allocation variables, used in {@link MRVMBidderPartialMIP} instances.
 *
 * @author Michael Weiss
 *
 */
public class MRVMWorldPartialMip extends PartialMIP {

    private static final Logger logger = LogManager.getLogger(MRVMWorldPartialMip.class);

    public final static String xVariablePrefix = "X_";
    public final static String valueVariablePrefix = "v_";

    private final Map<MRVMBidder, Map<MRVMRegionsMap.Region, Map<MRVMBand, Variable>>> xVariables;
    private final Map<MRVMBidder, Variable> valueVariables;

    private final double biggestPossibleValue;

    private final Set<MRVMBidder> bidders;
    private final MRVMWorld world;

    /**
     * @param bidders2
     * @param biggestPossibleValue The highest (already scaled) value any bidder could have
     */
    MRVMWorldPartialMip(Collection<MRVMBidder> bidders2, double biggestPossibleValue) {
        super();
        Preconditions.checkNotNull(bidders2);
        Preconditions.checkArgument(bidders2.size() > 0);
        Preconditions.checkArgument(biggestPossibleValue <= MIP.MAX_VALUE);
        this.biggestPossibleValue = biggestPossibleValue;
        this.bidders = Collections.unmodifiableSet(new HashSet<>(bidders2));
        world = bidders2.iterator().next().getWorld();
        Preconditions.checkNotNull(world);

        xVariables = initXVariables();
        valueVariables = initValueVariables();
    }

    /**
     * @return
     */
    private Set<Constraint> createNumberOfLicensesConstraints() {
        Set<Constraint> result = new HashSet<>();
        //TODO Replace with faster implementation, possibly change key-ordering of xVariables
        for (MRVMBand band : world.getBands()) {
            int lots = band.getNumberOfLots();
            for (Region region : world.getRegionsMap().getRegions()) {
                Constraint numberOfLotsConstraint = new Constraint(CompareType.LEQ, lots);
                for (MRVMBidder bidder : bidders) {
                    Variable xVar = getXVariable(bidder, region, band);
                    numberOfLotsConstraint.addTerm(1, xVar);
                }
                result.add(numberOfLotsConstraint);
            }
        }
        return result;

    }

    private Map<MRVMBidder, Variable> initValueVariables() {
        Map<MRVMBidder, Variable> result = new HashMap<>();
        for (MRVMBidder bidder : bidders) {
            String varName = new StringBuilder(valueVariablePrefix)
                    .append("_")
                    .append(bidder.getLongId())
                    .toString();
            Variable var = new Variable(varName, VarType.DOUBLE, 0, MIP.MAX_VALUE);
            result.put(bidder, var);
        }
        return Collections.unmodifiableMap(result);
    }

    private Map<MRVMBidder, Map<Region, Map<MRVMBand, Variable>>> initXVariables() {
        Map<MRVMBidder, Map<Region, Map<MRVMBand, Variable>>> result = new HashMap<>();

        for (MRVMBidder bidder : bidders) {
            Map<Region, Map<MRVMBand, Variable>> biddersMap = new HashMap<>();
            for (Region region : world.getRegionsMap().getRegions()) {
                Map<MRVMBand, Variable> bandMap = new HashMap<>();
                for (MRVMBand band : world.getBands()) {
                    String varName = xVariablePrefix.concat(MRVMBidderPartialMIP.createIndex(bidder, region, band));
                    Variable var = new Variable(varName, VarType.INT, 0, band.getNumberOfLots());
                    bandMap.put(band, var);
                }
                biddersMap.put(region, Collections.unmodifiableMap(bandMap));
            }
            result.put(bidder, Collections.unmodifiableMap(biddersMap));
        }
        return Collections.unmodifiableMap(result);
    }

    private void appendObjectiveToMip(IMIP mip) {
        mip.setObjectiveMax(true);

        if (!mip.getObjectiveTerms().isEmpty()) {
            logger.warn("There are already existing objective values, when there's still supposed to be none");
        }
        for (Variable var : valueVariables.values()) {
            mip.addObjectiveTerm(1, var);
        }
    }

    /**
     * {@inheritDoc}
     * Furthermore, this implementation of a PartialMip adds the objective term to the MIP
     */
    @Override
    public void appendToMip(IMIP mip) {
        super.appendToMip(mip);
        appendObjectiveToMip(mip);
    }


    /* (non-Javadoc)
     * @see PartialMIP#appendConstraintsToMip(edu.harvard.econcs.jopt.solver.mip.MIP)
     */
    @Override
    public void appendConstraintsToMip(IMIP mip) {
        super.appendConstraintsToMip(mip);
        for (Constraint c : createNumberOfLicensesConstraints()) {
            mip.add(c);
        }
    }

    @Override
    public void appendVariablesToMip(IMIP mip) {
        super.appendVariablesToMip(mip);
        for (Variable var : valueVariables.values()) {
            mip.add(var);
        }
        for (Map<Region, Map<MRVMBand, Variable>> middleMap : xVariables.values()) {
            for (Map<MRVMBand, Variable> innerMap : middleMap.values()) {
                for (Variable var : innerMap.values()) {
                    mip.add(var);
                }
            }
        }
    }

    /**
     * @throws NullPointerException if the requested variable is not stored.
     */
    public Variable getXVariable(MRVMBidder bidder, Region region, MRVMBand band) {
        Variable var = xVariables.get(bidder).get(region).get(band);
        if (var == null) {
            throw new NullPointerException();
        }
        return var;
    }

    public Set<Variable> getXVariables(MRVMBidder bidder) {
        Set<Variable> variables = new HashSet<>();
        for (Map.Entry<Region, Map<MRVMBand, Variable>> entry : xVariables.get(bidder).entrySet()) {
                variables.addAll(entry.getValue().values());
        }
        return variables;
    }


    public Variable getValueVariable(MRVMBidder bidder) {
        Variable var = valueVariables.get(bidder);
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

}
