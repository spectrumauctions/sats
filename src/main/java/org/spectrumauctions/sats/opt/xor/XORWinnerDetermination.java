package org.spectrumauctions.sats.opt.xor;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.math.DoubleMath;
import edu.harvard.econcs.jopt.solver.IMIP;
import edu.harvard.econcs.jopt.solver.IMIPResult;
import edu.harvard.econcs.jopt.solver.IMIPSolver;
import edu.harvard.econcs.jopt.solver.SolveParam;
import edu.harvard.econcs.jopt.solver.client.SolverClient;
import edu.harvard.econcs.jopt.solver.mip.*;
import org.spectrumauctions.sats.core.bidlang.xor.XORBid;
import org.spectrumauctions.sats.core.bidlang.xor.XORValue;
import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.Bundle;
import org.spectrumauctions.sats.core.model.Good;
import org.spectrumauctions.sats.core.model.World;
import org.spectrumauctions.sats.opt.domain.Allocation;
import org.spectrumauctions.sats.opt.domain.ItemAllocation;
import org.spectrumauctions.sats.opt.domain.WinnerDeterminator;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Wraps an OR or OR* winner determination
 *
 * @author Benedikt Buenz
 */
public class XORWinnerDetermination<T extends Good> implements WinnerDeterminator<T> {
    private Map<Bidder<T>, Map<Integer, Variable>> bidVariables = new HashMap<>();
    private Collection<XORBid<T>> bids;
    private IMIP winnerDeterminationProgram;
    private Allocation<T> result = null;
    private World world;
    private double scalingFactor = 1;


    public XORWinnerDetermination(Collection<XORBid<T>> bids) {
        this(bids, 1e-6);
    }

    public XORWinnerDetermination(Collection<XORBid<T>> bids, double epsilon) {
        Preconditions.checkNotNull(bids);
        Preconditions.checkArgument(!bids.isEmpty());
        this.bids = bids;
        double maxValue = -1;
        for (XORBid<T> bid : bids) {
            bidVariables.put(bid.getBidder(), new HashMap<>());
            for (XORValue<T> value : bid.getValues()) {
                if (value.value().doubleValue() > maxValue) {
                    maxValue = value.value().doubleValue();
                }
            }
        }
        if (maxValue > MIP.MAX_VALUE * 0.9) {
            this.scalingFactor = (MIP.MAX_VALUE * 0.9) / maxValue;
        }
        this.world = bids.iterator().next().getBidder().getWorld();
        winnerDeterminationProgram = createWinnerDeterminationMIP();
        winnerDeterminationProgram.setSolveParam(SolveParam.RELATIVE_OBJ_GAP, epsilon);
    }

    private IMIP createWinnerDeterminationMIP() {
        MIP wdp = new MIP();
        wdp.setObjectiveMax(true);
        // Add decision variables and objective terms:
        for (XORBid<T> xorBid : bids) {
            for (XORValue<T> bundleBid : xorBid.getValues()) {
                Variable bidI = new Variable("Bid_" + xorBid.getBidder().getId() + "_" + bundleBid.getId(), VarType.BOOLEAN, 0, 1);
                wdp.add(bidI);
                wdp.addObjectiveTerm(bundleBid.value().doubleValue() * scalingFactor, bidI);
                bidVariables.get(xorBid.getBidder()).put(bundleBid.getId(), bidI);
            }
        }
        Map<Good, Constraint> goods = new HashMap<>();

        for (XORBid<T> xorBid : bids) {
            Constraint exclusiveBids = new Constraint(CompareType.LEQ, 1);
            for (XORValue<T> bundleBid : xorBid.getValues()) {
                exclusiveBids.addTerm(1, bidVariables.get(xorBid.getBidder()).get(bundleBid.getId()));
                for (Good good : bundleBid.getLicenses()) {
                    Constraint noDoubleAssignment = goods.get(good);
                    if (noDoubleAssignment == null) {
                        noDoubleAssignment = new Constraint(CompareType.LEQ, 1);
                        goods.put(good, noDoubleAssignment);
                    }
                    noDoubleAssignment.addTerm(1.0, bidVariables.get(xorBid.getBidder()).get(bundleBid.getId()));
                }
            }
            wdp.add(exclusiveBids);
        }
        for (Constraint noDoubleAssignments : goods.values()) {
            wdp.add(noDoubleAssignments);
        }

        return wdp;
    }

    protected IMIP getMIP() {
        return winnerDeterminationProgram;
    }

    private Variable getBidVariable(Bidder<T> bidder, XORValue<T> bundleBid) {
        return bidVariables.get(bidder).get(bundleBid.getId());
    }

    @Override
    public WinnerDeterminator<T> getWdWithoutBidder(Bidder bidder) {
        return new XORWinnerDetermination<>(bids.stream().filter(b -> !b.getBidder().equals(bidder)).collect(Collectors.toSet()));
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
        return new XORWinnerDetermination<>(bids);
    }

    @Override
    public void adjustPayoffs(Map<Bidder<T>, Double> payoffs) {
        for (XORBid<T> bidPerBidder : bids) {
            Variable x = new Variable("x_" + bidPerBidder.getBidder().getId(), VarType.BOOLEAN, 0, 1);
            winnerDeterminationProgram.add(x);
            winnerDeterminationProgram.addObjectiveTerm(-payoffs.getOrDefault(bidPerBidder.getBidder(), 0.0) * scalingFactor, x);

            Constraint x1 = new Constraint(CompareType.GEQ, 0);
            Constraint x2 = new Constraint(CompareType.LEQ, 0);
            x1.addTerm(-1, x);
            x2.addTerm(-MIP.MAX_VALUE, x);
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

    private Allocation<T> solveWinnerDetermination() {
        IMIPSolver solver = new SolverClient();
        IMIPResult mipResult = solver.solve(getMIP());
        return adaptMIPResult(mipResult);
    }

    private Allocation<T> adaptMIPResult(IMIPResult mipResult) {

        Map<Bidder<T>, Bundle<T>> trades = new HashMap<>();
        Map<Bidder<T>, BigDecimal> declaredValues = new HashMap<>();
        BigDecimal totalValue = BigDecimal.ZERO;
        for (XORBid<T> xorBid : bids) {
            BigDecimal bidValue = BigDecimal.ZERO;
            ImmutableSet.Builder<T> goodsBuilder = ImmutableSet.builder();
            for (XORValue<T> bundleBid : xorBid.getValues()) {
                if (DoubleMath.fuzzyEquals(mipResult.getValue(getBidVariable(xorBid.getBidder(), bundleBid)), 1, 1e-3)) {
                    goodsBuilder.addAll(bundleBid.getLicenses());
                    totalValue = totalValue.add(bundleBid.value());
                    bidValue = bidValue.add(bundleBid.value());
                }
            }
            Set<T> goods = goodsBuilder.build();
            if (!goods.isEmpty()) {
                trades.put(xorBid.getBidder(), new Bundle<>(goods));
                declaredValues.put(xorBid.getBidder(), bidValue);
            }
        }

        ItemAllocation.ItemAllocationBuilder<T> builder = new ItemAllocation.ItemAllocationBuilder<>();
        return builder
                .withAllocation(trades)
                .withTotalValue(totalValue)
                .withDeclaredValues(declaredValues)
                .withWorld(world).build();
    }

    @Override
    public String toString() {
        return "XORWinnerDetermination{" +
                "scalingFactor=" + scalingFactor +
                ", winnerDeterminationProgram=" + winnerDeterminationProgram +
                '}';
    }

}
