package org.spectrumauctions.sats.opt.xor;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.math.DoubleMath;
import edu.harvard.econcs.jopt.solver.IMIP;
import edu.harvard.econcs.jopt.solver.IMIPResult;
import edu.harvard.econcs.jopt.solver.IMIPSolver;
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

/**
 * Wraps an OR or OR* winner determination
 *
 * @author Benedikt Buenz
 */
public class XORWinnerDetermination<T extends Good> implements WinnerDeterminator<Allocation<T>> {
    private Map<XORValue<T>, Variable> bidVariables = new HashMap<>();
    private Collection<XORBid<T>> bids;
    private IMIP winnerDeterminationProgram;
    private Allocation<T> result = null;
    private World world;


    public XORWinnerDetermination(Collection<XORBid<T>> bids) {
        Preconditions.checkNotNull(bids);
        Preconditions.checkArgument(bids.size() > 0);
        this.bids = bids;
        this.world = bids.iterator().next().getBidder().getWorld();
        winnerDeterminationProgram = createWinnerDeterminationMIP();
    }

    private IMIP createWinnerDeterminationMIP() {
        MIP winnerDeterminationProgram = new MIP();
        winnerDeterminationProgram.setObjectiveMax(true);
        // Add decision variables and objective terms:
        for (XORBid<T> xorBid : bids) {
            for (XORValue<T> bundleBid : xorBid.getValues()) {
                Variable bidI = new Variable("Bid " + bundleBid.getId(), VarType.BOOLEAN, 0, 1);
                winnerDeterminationProgram.add(bidI);
                winnerDeterminationProgram.addObjectiveTerm(bundleBid.value().doubleValue(), bidI);
                bidVariables.put(bundleBid, bidI);
            }
        }
        Map<Good, Constraint> goods = new HashMap<>();

        for (XORBid<T> xorBid : bids) {
            Constraint exclusiveBids = new Constraint(CompareType.LEQ, 1);
            for (XORValue<T> bundleBid : xorBid.getValues()) {
                exclusiveBids.addTerm(1, bidVariables.get(bundleBid));
                for (Good good : bundleBid.getLicenses()) {
                    Constraint noDoubleAssignment = goods.get(good);
                    if (noDoubleAssignment == null) {
                        noDoubleAssignment = new Constraint(CompareType.LEQ, 1);
                        goods.put(good, noDoubleAssignment);
                    }
                    noDoubleAssignment.addTerm(1.0, bidVariables.get(bundleBid));
                }
            }
            winnerDeterminationProgram.add(exclusiveBids);
        }
        for (Constraint noDoubleAssignments : goods.values()) {
            winnerDeterminationProgram.add(noDoubleAssignments);
        }

        return winnerDeterminationProgram;
    }

    protected IMIP getMIP() {
        return winnerDeterminationProgram;
    }

    private Variable getBidVariable(XORValue<T> bundleBid) {
        return bidVariables.get(bundleBid);
    }

    @Override
    public WinnerDeterminator<Allocation<T>> getWdWithoutBidder(Bidder bidder) {
        return null;
    }

    @Override
    public Allocation<T> calculateAllocation() {
        if (result == null) {
            result = solveWinnerDetermination();
        }
        return result;
    }

    private Allocation<T> solveWinnerDetermination() {
        IMIPSolver solver = new SolverClient();
        IMIPResult mipResult = solver.solve(getMIP());
        return adaptMIPResult(mipResult);
    }

    private Allocation<T> adaptMIPResult(IMIPResult mipResult) {

        Map<Bidder<T>, Bundle<T>> trades = new HashMap<>();
        double totalValue = 0;
        for (XORBid<T> xorBid : bids) {
            ImmutableSet.Builder<T> goodsBuilder = ImmutableSet.builder();
            for (XORValue<T> bundleBid : xorBid.getValues()) {
                if (DoubleMath.fuzzyEquals(mipResult.getValue(getBidVariable(bundleBid)), 1, 1e-3)) {
                    goodsBuilder.addAll(bundleBid.getLicenses());
                    totalValue += bundleBid.value().doubleValue();
                }
            }
            Set<T> goods = goodsBuilder.build();
            if (!goods.isEmpty()) {
                trades.put(xorBid.getBidder(), new Bundle<>(goods));
            }
        }

        ItemAllocation.ItemAllocationBuilder<T> builder = new ItemAllocation.ItemAllocationBuilder<>();
        return builder.withAllocation(trades).withTotalValue(new BigDecimal(totalValue)).withWorld(world).build();
    }

}
