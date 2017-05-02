package ch.uzh.ifi.ce.mweiss.sats.opt.vcg.external.winnerdetermination;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import ch.uzh.ifi.ce.mweiss.sats.opt.model.EfficientAllocator;
import ch.uzh.ifi.ce.mweiss.sats.opt.vcg.external.domain.Auction;
import ch.uzh.ifi.ce.mweiss.sats.opt.vcg.external.domain.BidderAllocation;
import ch.uzh.ifi.ce.mweiss.sats.opt.vcg.external.domain.XORAllocation;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.math.DoubleMath;

import ch.uzh.ifi.ce.mweiss.specval.bidlang.xor.XORValue;
import ch.uzh.ifi.ce.mweiss.specval.model.Bidder;
import ch.uzh.ifi.ce.mweiss.specval.model.Bundle;
import ch.uzh.ifi.ce.mweiss.specval.model.Good;
import edu.harvard.econcs.jopt.solver.IMIP;
import edu.harvard.econcs.jopt.solver.IMIPResult;
import edu.harvard.econcs.jopt.solver.IMIPSolver;
import edu.harvard.econcs.jopt.solver.client.SolverClient;
import edu.harvard.econcs.jopt.solver.mip.Variable;

public abstract class WinnerDetermination<T extends Good> implements EfficientAllocator<XORAllocation<T>> {
    private XORAllocation<T> result = null;
    private Auction<T> auction;

    public WinnerDetermination(Auction<T> auction) {
        this.auction = auction;
    }

    protected abstract IMIP getMIP();

    @Override
    public XORAllocation<T> calculateAllocation() {
        if (result == null) {
            result = solveWinnerDetermination();
        }
        return result;
    }

    protected Auction<T> getAuction() {
        return auction;
    }

    private XORAllocation<T> solveWinnerDetermination() {
        IMIPSolver solver = new SolverClient();
        IMIPResult mipResult = solver.solve(getMIP());
        return adaptMIPResult(mipResult);
    }

    protected abstract Variable getBidVariable(XORValue<T> bundleBid);

    protected XORAllocation<T> adaptMIPResult(IMIPResult mipResult) {
        Map<Bidder<T>, BidderAllocation<T>> trades = new HashMap<>();
        for (Bidder<T> bidder : auction.getBidders()) {
            double totalValue = 0;
            Builder<Good> goodsBuilder = ImmutableSet.<Good> builder();
            Builder<XORValue<T>> bundleBids = ImmutableSet.<XORValue<T>> builder();
            for (XORValue<T> bundleBid : auction.getBid(bidder).getValues()) {
                if (DoubleMath.fuzzyEquals(mipResult.getValue(getBidVariable(bundleBid)), 1, 1e-3)) {
                    goodsBuilder.addAll(bundleBid.getLicenses());
                    bundleBids.add(bundleBid);
                    totalValue += bundleBid.getValue();
                }
            }
            Set<Good> goods = goodsBuilder.build();
            if (!goods.isEmpty()) {
                trades.put(bidder, new BidderAllocation<>(totalValue, new Bundle<>(goods), bundleBids.build()));
            }
        }

        return new XORAllocation<>(trades);
    }

}