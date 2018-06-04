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
import java.util.stream.Collectors;

import static edu.harvard.econcs.jopt.solver.mip.MIP.MAX_VALUE;

public class XORQWinnerDetermination<T extends Good> implements WinnerDeterminator<T> {
    private Map<GenericValue<GenericDefinition<T>, T>, Variable> bidVariables = new HashMap<>();
    private Set<GenericBid<GenericDefinition<T>, T>> bids;
    private IMIP winnerDeterminationProgram;
    private Allocation<T> result = null;
    private GenericWorld<T> world;
    private double scalingFactor = 1;


    public XORQWinnerDetermination(Set<GenericBid<GenericDefinition<T>, T>> bids) {
        Preconditions.checkNotNull(bids);
        Preconditions.checkArgument(bids.size() > 0);
        this.bids = bids;
        double maxValue = -1;
        for (GenericBid<GenericDefinition<T>, T> bid : bids) {
            for (GenericValue<GenericDefinition<T>, T> value : bid.getValues()) {
                if (value.getValue().doubleValue() > maxValue) {
                    maxValue = value.getValue().doubleValue();
                }
            }
        }
        if (maxValue > MIP.MAX_VALUE * 0.9) {
            this.scalingFactor = 0.9 / maxValue * MIP.MAX_VALUE;
        }

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
                winnerDeterminationProgram.addObjectiveTerm(value.getValue().doubleValue() * scalingFactor, bidI);
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
        return new XORQWinnerDetermination<>(bids.stream().filter(b -> !b.getBidder().equals(bidder)).collect(Collectors.toSet()));
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
        return new XORQWinnerDetermination<>(bids);
    }

    @Override
    public void adjustPayoffs(Map<Bidder<T>, Double> payoffs) {
        for (GenericBid<GenericDefinition<T>, T> bidPerBidder : bids) {
            Variable x = new Variable("x_" + bidPerBidder.getBidder().getId(), VarType.BOOLEAN, 0, 1);
            winnerDeterminationProgram.add(x);
            winnerDeterminationProgram.addObjectiveTerm(-payoffs.getOrDefault(bidPerBidder.getBidder(), 0.0), x);

            Constraint x1 = new Constraint(CompareType.GEQ, 0);
            Constraint x2 = new Constraint(CompareType.LEQ, 0);
            x1.addTerm(-1, x);
            x2.addTerm(-MAX_VALUE, x);
            bidPerBidder.getValues().forEach(b -> x1.addTerm(1, bidVariables.get(b)));
            bidPerBidder.getValues().forEach(b -> x2.addTerm(1, bidVariables.get(b)));
            winnerDeterminationProgram.add(x1);
            winnerDeterminationProgram.add(x2);
        }
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
