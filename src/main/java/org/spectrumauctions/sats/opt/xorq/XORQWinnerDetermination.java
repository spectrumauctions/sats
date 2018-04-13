package org.spectrumauctions.sats.opt.xorq;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.math.DoubleMath;
import edu.harvard.econcs.jopt.solver.IMIP;
import edu.harvard.econcs.jopt.solver.IMIPResult;
import edu.harvard.econcs.jopt.solver.IMIPSolver;
import edu.harvard.econcs.jopt.solver.client.SolverClient;
import edu.harvard.econcs.jopt.solver.mip.*;
import org.spectrumauctions.sats.core.bidlang.generic.GenericBid;
import org.spectrumauctions.sats.core.bidlang.generic.GenericDefinition;
import org.spectrumauctions.sats.core.bidlang.generic.GenericValue;
import org.spectrumauctions.sats.core.bidlang.generic.GenericValueBidder;
import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.GenericWorld;
import org.spectrumauctions.sats.core.model.Good;
import org.spectrumauctions.sats.core.model.World;
import org.spectrumauctions.sats.opt.domain.Allocation;
import org.spectrumauctions.sats.opt.domain.GenericAllocation;
import org.spectrumauctions.sats.opt.domain.WinnerDeterminator;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class XORQWinnerDetermination<T extends Good> implements WinnerDeterminator<T> {
    private Map<GenericValue<GenericDefinition<T>, T>, Variable> bidVariables = new HashMap<>();
    private Collection<GenericBid<GenericDefinition<T>, T>> bids;
    private IMIP winnerDeterminationProgram;
    private Allocation<T> result = null;
    private GenericWorld<T> world;
    public XORQWinnerDetermination(Set<GenericBid<GenericDefinition<T>, T>> bids) {
        Preconditions.checkNotNull(bids);
        Preconditions.checkArgument(bids.size() > 0);
        this.bids = bids;
        this.world = (GenericWorld<T>) bids.iterator().next().getBidder().getWorld();
        winnerDeterminationProgram = createWinnerDeterminationMIP();
    }

    private IMIP createWinnerDeterminationMIP() {
        MIP winnerDeterminationProgram = new MIP();
        winnerDeterminationProgram.setObjectiveMax(true);

        // Add decision variables and objective terms:
        for (GenericBid<GenericDefinition<T>, T> bid : bids) {
            for (GenericValue<GenericDefinition<T>, T> value : bid.getValues()) {
                Variable bidI = new Variable("Bid " + value.getId(), VarType.BOOLEAN, 0, 1);
                winnerDeterminationProgram.add(bidI);
                winnerDeterminationProgram.addObjectiveTerm(value.getValue().doubleValue(), bidI);
                bidVariables.put(value, bidI);
            }
        }

        Map<GenericDefinition<T>, Constraint> numberOfLotsConstraints = new HashMap<>();

        for (GenericBid<GenericDefinition<T>, T> bid : bids) {
            Constraint exclusiveBids = new Constraint(CompareType.LEQ, 1);
            for (GenericValue<GenericDefinition<T>, T> value : bid.getValues()) {
                exclusiveBids.addTerm(1, bidVariables.get(value));
                for (Map.Entry<GenericDefinition<T>, Integer> entry : value.getQuantities().entrySet()) {
                    GenericDefinition<T> def = entry.getKey();
                    int quantity = entry.getValue();
                    Constraint numberOfLotsConstraint = numberOfLotsConstraints.get(def);
                    if (numberOfLotsConstraint == null) {
                        numberOfLotsConstraint = new Constraint(CompareType.LEQ, def.numberOfLicenses());
                        numberOfLotsConstraints.put(def, numberOfLotsConstraint);
                    }
                    numberOfLotsConstraint.addTerm(quantity, bidVariables.get(value));
                }
            }
            winnerDeterminationProgram.add(exclusiveBids);
        }

        numberOfLotsConstraints.values().forEach(winnerDeterminationProgram::add);

        return winnerDeterminationProgram;
    }

    protected IMIP getMIP() {
        return winnerDeterminationProgram;
    }

    private Allocation<T> solveWinnerDetermination() {
        IMIPSolver solver = new SolverClient();
        IMIPResult mipResult = solver.solve(getMIP());
        return adaptMIPResult(mipResult);
    }

    @Override
    public WinnerDeterminator<T> getWdWithoutBidder(Bidder<T> bidder) {
        return null;
    }

    @Override
    public Allocation<T> calculateAllocation() {
        if (result == null) {
            result = solveWinnerDetermination();
        }
        return result;
    }

    @Override
    public WinnerDeterminator<T> copyOf() {
        return null;
    }

    @Override
    public void adjustPayoffs(Map<Bidder<T>, Double> payoffs) {

    }

    private Variable getBidVariable(GenericValue<GenericDefinition<T>, T> bundleBid) {
        return bidVariables.get(bundleBid);
    }

    private Allocation<T> adaptMIPResult(IMIPResult mipResult) {

        GenericAllocation.Builder<GenericDefinition<T>, T> builder = new GenericAllocation.Builder<>();
        for (GenericBid<GenericDefinition<T>, T> bid : bids) {
            for (GenericValue<GenericDefinition<T>, T> value : bid.getValues()) {
                if (DoubleMath.fuzzyEquals(mipResult.getValue(getBidVariable(value)), 1, 1e-3)) {
                    builder.putGenericValue(bid.getBidder(), value);
                }
            }
        }

        return new GenericAllocation<>(builder);
    }
}
