/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.uzh.ifi.ce.mweiss.sats.opt.model.mrm;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Preconditions;

import ch.uzh.ifi.ce.mweiss.sats.core.bidlang.generic.Band;
import ch.uzh.ifi.ce.mweiss.sats.core.model.mrm.MRMBand;
import ch.uzh.ifi.ce.mweiss.sats.core.model.mrm.MRMBidder;
import ch.uzh.ifi.ce.mweiss.sats.core.model.mrm.MRMRegionsMap.Region;
import ch.uzh.ifi.ce.mweiss.sats.core.model.mrm.MRMWorld;
import ch.uzh.ifi.ce.mweiss.sats.opt.imip.PartialMIP;
import edu.harvard.econcs.jopt.solver.mip.*;

/**
 * The class generating the general allocation rules (variables and constraints)<br>
 * It also provides functions to get the allocation variables, used in {@link BidderPartialMIP} instances.
 *
 * @author Michael Weiss
 *
 */
public class WorldPartialMip extends PartialMIP {

    public final static String xVariablePrefix = "X_";
    public final static String valueVariablePrefix = "v_";

    private final Map<MRMBidder,Map<Region, Map<Band, Variable>>> xVariables;
    private final Map<MRMBidder, Variable> valueVariables;

    private final double biggestPossibleValue;

    private final Set<MRMBidder> bidders;
    private final MRMWorld world;
    private final double scalingFactor;

    /**
     * @param bidders2
     * @param biggestPossibleValue The highest (already scaled) value any bidder could have
     * @param scalingFactor
     */
    WorldPartialMip(Collection<MRMBidder> bidders2, double biggestPossibleValue, double scalingFactor) {
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
        valueVariables = initValueVariables();
    }

    /**
     * @return
     */
    private Set<Constraint> createNumberOfLicensesConstraints() {
        Set<Constraint> result =new HashSet<>();
        //TODO Replace with faster implementation, possibly change key-ordering of xVariables
        for(MRMBand band : world.getBands()){
            int lots = band.getNumberOfLots();
            for(Region region : world.getRegionsMap().getRegions()){
                Constraint numberOfLotsConstraint = new Constraint(CompareType.LEQ, lots);
                for(MRMBidder bidder : bidders){
                    Variable xVar = getXVariable(bidder, region, band);
                    numberOfLotsConstraint.addTerm(1, xVar);
                }
                result.add(numberOfLotsConstraint);
            }
        }
        return result;

    }

    private Map<MRMBidder, Variable> initValueVariables() {
        Map<MRMBidder, Variable> result = new HashMap<>();
        for(MRMBidder bidder : bidders){
            String varName = new StringBuilder(valueVariablePrefix)
                    .append("_")
                    .append(bidder.getId())
                    .toString();
            Variable var = new Variable(varName, VarType.DOUBLE, 0, MIP.MAX_VALUE);
            result.put(bidder, var);
        }
        return Collections.unmodifiableMap(result);
    }

    private Map<MRMBidder,Map<Region, Map<Band, Variable>>> initXVariables(){
        Map<MRMBidder,Map<Region, Map<Band, Variable>>> result = new HashMap<>();

        for(MRMBidder bidder : bidders){
            Map<Region, Map<Band, Variable>> biddersMap = new HashMap<>();
            for(Region region : world.getRegionsMap().getRegions()){
                Map<Band, Variable> bandMap = new HashMap<>();
                for(MRMBand band : world.getBands()){
                    String varName = xVariablePrefix.concat(BidderPartialMIP.createIndex(bidder, region, band));
                    Variable var = new Variable(varName, VarType.INT, 0, band.getNumberOfLots());
                    bandMap.put(band, var);
                }
                biddersMap.put(region, Collections.unmodifiableMap(bandMap));
            }
            result.put(bidder, Collections.unmodifiableMap(biddersMap));
        }
        return Collections.unmodifiableMap(result);
    }

    private void appendObjectiveToMip(MIP mip){
        mip.setObjectiveMax(true);
        if((mip.getLinearObjectiveTerms() != null && mip.getQuadraticObjectiveTerms() != null)
                || mip.getObjectiveTerms().size() != 0){
            //TODO Log Warning
        }
        for(Variable var : valueVariables.values()){
            mip.addObjectiveTerm(1, var);
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
        for(Constraint c : createNumberOfLicensesConstraints()){
            mip.add(c);
        }
    }

    @Override
    public void appendVariablesToMip(MIP mip){
        super.appendVariablesToMip(mip);
        for(Variable var : valueVariables.values()){
            mip.add(var);
        }
        for(Map<Region, Map<Band, Variable>>  middleMap: xVariables.values()){
            for(Map<Band, Variable> innerMap : middleMap.values()){
                for(Variable var : innerMap.values()){
                    mip.add(var);
                }
            }
        }
    }

    /**
     * @throws NullPointerException if the requested variable is not stored.
     */
    public Variable getXVariable(MRMBidder bidder, Region region, MRMBand band) {
        Variable var = xVariables.get(bidder).get(region).get(band);
        if(var == null){
            throw new NullPointerException();
        }
        return var;
    }


    public Variable getValueVariable(MRMBidder bidder){
        Variable var = valueVariables.get(bidder);
        if(var == null){
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
