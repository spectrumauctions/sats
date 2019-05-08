package org.spectrumauctions.sats.opt.xorq;

import com.google.common.base.Preconditions;
import com.google.common.math.DoubleMath;
import edu.harvard.econcs.jopt.solver.IMIP;
import edu.harvard.econcs.jopt.solver.IMIPResult;
import edu.harvard.econcs.jopt.solver.IMIPSolver;
import edu.harvard.econcs.jopt.solver.client.SolverClient;
import edu.harvard.econcs.jopt.solver.mip.*;
import org.spectrumauctions.sats.core.bidlang.generic.GenericBid;
import org.spectrumauctions.sats.core.bidlang.generic.GenericDefinition;
import org.spectrumauctions.sats.core.bidlang.generic.GenericValue;
import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.Good;
import org.spectrumauctions.sats.opt.domain.Allocation;
import org.spectrumauctions.sats.opt.domain.GenericAllocation;
import org.spectrumauctions.sats.opt.domain.WinnerDeterminator;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static edu.harvard.econcs.jopt.solver.mip.MIP.MAX_VALUE;

public class XORQWinnerDetermination<G extends GenericDefinition<T>, T extends Good> implements WinnerDeterminator<T> {
    private Map<Bidder<T>, Map<Integer, Variable>> bidVariables = new HashMap<>();
    private Set<GenericBid<G, T>> bids;
    private IMIP winnerDeterminationProgram;
    private Allocation<T> result = null;
    private double scalingFactor = 1;


    public XORQWinnerDetermination(Set<GenericBid<G, T>> bids) {
        Preconditions.checkNotNull(bids);
        Preconditions.checkArgument(bids.size() > 0);
        this.bids = bids;
        double maxValue = 0;
        for (GenericBid<G, T> bid : bids) {
            bidVariables.put(bid.getBidder(), new HashMap<>());
            for (GenericValue<G, T> value : bid.getValues()) {
                if (value.getValue().doubleValue() > maxValue) {
                    maxValue = value.getValue().doubleValue();
                }
            }
        }
        if (maxValue > MIP.MAX_VALUE * 0.9) {
            this.scalingFactor = (MIP.MAX_VALUE * 0.9) / maxValue;
        }

        winnerDeterminationProgram = createWinnerDeterminationMIP();
    }

    private IMIP createWinnerDeterminationMIP() {
        MIP winnerDeterminationProgram = new MIP();
        winnerDeterminationProgram.setObjectiveMax(true);

        // Add decision variables and objective terms:
        for (GenericBid<G, T> bid : bids) {
            for (GenericValue<G, T> value : bid.getValues()) {
                Variable bidI = new Variable("Bid " + value.getId(), VarType.BOOLEAN, 0, 1);
                winnerDeterminationProgram.add(bidI);
                winnerDeterminationProgram.addObjectiveTerm(value.getValue().doubleValue() * scalingFactor, bidI);
                bidVariables.get(bid.getBidder()).put(value.getId(), bidI);
            }
        }

        Map<GenericDefinition<T>, Constraint> numberOfLotsConstraints = new HashMap<>();

        for (GenericBid<G, T> bid : bids) {
            Constraint exclusiveBids = new Constraint(CompareType.LEQ, 1);
            for (GenericValue<G, T> value : bid.getValues()) {
                exclusiveBids.addTerm(1, bidVariables.get(bid.getBidder()).get(value.getId()));
                for (Map.Entry<G, Integer> entry : value.getQuantities().entrySet()) {
                    GenericDefinition<T> def = entry.getKey();
                    int quantity = entry.getValue();
                    Constraint numberOfLotsConstraint = numberOfLotsConstraints.get(def);
                    if (numberOfLotsConstraint == null) {
                        numberOfLotsConstraint = new Constraint(CompareType.LEQ, def.numberOfLicenses());
                        numberOfLotsConstraints.put(def, numberOfLotsConstraint);
                    }
                    numberOfLotsConstraint.addTerm(quantity, bidVariables.get(bid.getBidder()).get(value.getId()));
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
        for (GenericBid<G, T> bidPerBidder : bids) {
            Variable x = new Variable("x_" + bidPerBidder.getBidder().getId(), VarType.BOOLEAN, 0, 1);
            winnerDeterminationProgram.add(x);
            winnerDeterminationProgram.addObjectiveTerm(-payoffs.getOrDefault(bidPerBidder.getBidder(), 0.0) * scalingFactor, x);

            Constraint x1 = new Constraint(CompareType.GEQ, 0);
            Constraint x2 = new Constraint(CompareType.LEQ, 0);
            x1.addTerm(-1, x);
            x2.addTerm(-MAX_VALUE, x);
            bidPerBidder.getValues().forEach(b -> x1.addTerm(1, bidVariables.get(bidPerBidder.getBidder()).get(b.getId())));
            bidPerBidder.getValues().forEach(b -> x2.addTerm(1, bidVariables.get(bidPerBidder.getBidder()).get(b.getId())));
            winnerDeterminationProgram.add(x1);
            winnerDeterminationProgram.add(x2);
        }
    }

    @Override
    public double getScale() {
        return scalingFactor;
    }

    private Variable getBidVariable(Bidder<T> bidder, GenericValue<G, T> bundleBid) {
        return bidVariables.get(bidder).get(bundleBid.getId());
    }

    private Allocation<T> adaptMIPResult(IMIPResult mipResult) {

        GenericAllocation.Builder<G, T> builder = new GenericAllocation.Builder<>();
        for (GenericBid<G, T> bid : bids) {
            for (GenericValue<G, T> value : bid.getValues()) {
                if (DoubleMath.fuzzyEquals(mipResult.getValue(getBidVariable(bid.getBidder(), value)), 1, 1e-3)) {
                    builder.putGenericValue(bid.getBidder(), value);
                }
            }
        }

        return new GenericAllocation<>(builder);
    }
}
