package org.spectrumauctions.sats.opt.vcg.external.winnerdetermination;

import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.spectrumauctions.sats.core.bidlang.xor.XORBid;
import org.spectrumauctions.sats.core.bidlang.xor.XORValue;
import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.Bundle;
import org.spectrumauctions.sats.opt.model.EfficientAllocator;
import org.spectrumauctions.sats.opt.vcg.external.MockWorld;
import org.spectrumauctions.sats.opt.vcg.external.MockWorld.MockGood;
import org.spectrumauctions.sats.opt.vcg.external.domain.Auction;
import org.spectrumauctions.sats.opt.vcg.external.domain.Bids;
import org.spectrumauctions.sats.opt.vcg.external.domain.XORAllocation;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class WinnerDeterminationTest {

    private MockWorld.MockGood A;
    private MockWorld.MockGood B;
    private MockWorld.MockGood C;
    private MockWorld.MockGood D;
    private MockWorld.MockGood E;

    private Map<Integer, Bidder<MockWorld.MockGood>> bidders;

    @Before
    public void setUp() {
        A = MockWorld.getInstance().createNewGood();
        B = MockWorld.getInstance().createNewGood();
        C = MockWorld.getInstance().createNewGood();
        D = MockWorld.getInstance().createNewGood();
        E = MockWorld.getInstance().createNewGood();
        bidders = new HashMap<>();
        MockWorld.getInstance().reset();
    }


    private Bidder<MockWorld.MockGood> bidder(int id) {
        Bidder<MockWorld.MockGood> fromMap = bidders.get(id);
        if (fromMap == null) {
            Bidder<MockWorld.MockGood> bidder = MockWorld.getInstance().createNewBidder();
            bidders.put((int) bidder.getId(), bidder);
            return bidder(id);
        }
        return fromMap;
    }

    @SuppressWarnings({"deprecation", "unchecked"}) //Use deprecated double-function
    @Test
    public void testSimpleWinnerDetermination() {
        XORValue<MockWorld.MockGood> bid1 = new XORValue<MockWorld.MockGood>(new Bundle<>(A), 2);
        XORValue<MockWorld.MockGood> bid2 = new XORValue<MockWorld.MockGood>(new Bundle<>(A, B, D), 3);
        XORValue<MockWorld.MockGood> bid3 = new XORValue<MockWorld.MockGood>(new Bundle<>(B, C), 2);
        XORValue<MockWorld.MockGood> bid4 = new XORValue<MockWorld.MockGood>(new Bundle<>(C, D), 1);

        Bids<MockWorld.MockGood> bids = new Bids<>();
        bids.addBid(new XORBid.Builder<MockWorld.MockGood>(bidder(1), Sets.newHashSet(bid1)).build());
        bids.addBid(new XORBid.Builder<MockWorld.MockGood>(bidder(2), Sets.newHashSet(bid2)).build());
        bids.addBid(new XORBid.Builder<MockWorld.MockGood>(bidder(3), Sets.newHashSet(bid3)).build());
        bids.addBid(new XORBid.Builder<MockWorld.MockGood>(bidder(4), Sets.newHashSet(bid4)).build());

        Auction<MockGood> auction = new Auction<>(bids, Sets.newHashSet(A, B, C, D));
        EfficientAllocator<XORAllocation<MockWorld.MockGood>> wd = new XORWinnerDetermination<>(
                auction);
        XORAllocation<MockWorld.MockGood> result = wd.calculateAllocation();
        assertTrue(result.getTotalValue().compareTo(BigDecimal.valueOf(4)) == 0);
        assertEquals(result.getAllocation(bidder(1)).getTradeValue(), 2, 0.0001);
        assertEquals(result.getAllocation(bidder(2)).getTradeValue(), 0, 0.01);
        assertEquals(result.getAllocation(bidder(3)).getTradeValue(), 2, 0.0001);
        assertEquals(result.getAllocation(bidder(4)).getTradeValue(), 0, 0.01);
        assertTrue(result.getAllocation(bidder(2)).getAcceptedBids().isEmpty());
        assertTrue(result.getAllocation(bidder(4)).getAcceptedBids().isEmpty());
    }

    @Test
    public void testMediumWinnerDetermination() {
        XORValue<MockWorld.MockGood> bid0 = new XORValue<MockWorld.MockGood>(new Bundle<>(C, D), 1795.51);
        XORValue<MockWorld.MockGood> bid1 = new XORValue<MockWorld.MockGood>(new Bundle<>(D), 894.644);
        XORValue<MockWorld.MockGood> bid2 = new XORValue<MockWorld.MockGood>(new Bundle<>(A, B), 1633.62);
        XORValue<MockWorld.MockGood> bid3 = new XORValue<MockWorld.MockGood>(new Bundle<>(C), 997.064);
        XORValue<MockWorld.MockGood> bid4 = new XORValue<MockWorld.MockGood>(new Bundle<>(B, C), 1751.26);
        XORValue<MockWorld.MockGood> bid5 = new XORValue<MockWorld.MockGood>(new Bundle<>(A, E), 1779.42);
        XORValue<MockWorld.MockGood> bid6 = new XORValue<MockWorld.MockGood>(new Bundle<>(B), 843.716);
        XORValue<MockWorld.MockGood> bid7 = new XORValue<MockWorld.MockGood>(new Bundle<>(E), 762.093);
        XORValue<MockWorld.MockGood> bid8 = new XORValue<MockWorld.MockGood>(new Bundle<>(A), 893.983);
        XORValue<MockWorld.MockGood> bid9 = new XORValue<MockWorld.MockGood>(new Bundle<>(A, C), 1816.69);
        Bids<MockWorld.MockGood> bids = new Bids<>();
        bids.addBid(new XORBid.Builder<MockWorld.MockGood>(bidder(0), Sets.newHashSet(bid0)).build());
        bids.addBid(new XORBid.Builder<MockWorld.MockGood>(bidder(1), Sets.newHashSet(bid1)).build());
        bids.addBid(new XORBid.Builder<MockWorld.MockGood>(bidder(2), Sets.newHashSet(bid2)).build());
        bids.addBid(new XORBid.Builder<MockWorld.MockGood>(bidder(3), Sets.newHashSet(bid3)).build());
        bids.addBid(new XORBid.Builder<MockWorld.MockGood>(bidder(4), Sets.newHashSet(bid4)).build());
        bids.addBid(new XORBid.Builder<MockWorld.MockGood>(bidder(5), Sets.newHashSet(bid5)).build());
        bids.addBid(new XORBid.Builder<MockWorld.MockGood>(bidder(6), Sets.newHashSet(bid6)).build());
        bids.addBid(new XORBid.Builder<MockWorld.MockGood>(bidder(7), Sets.newHashSet(bid7)).build());
        bids.addBid(new XORBid.Builder<MockWorld.MockGood>(bidder(8), Sets.newHashSet(bid8)).build());
        bids.addBid(new XORBid.Builder<MockWorld.MockGood>(bidder(9), Sets.newHashSet(bid9)).build());

        Auction<MockWorld.MockGood> auction = new Auction<>(bids, Sets.newHashSet(A, B, C, D, E));
        EfficientAllocator<XORAllocation<MockWorld.MockGood>> wd = new XORWinnerDetermination<>(
                auction);
        XORAllocation<MockWorld.MockGood> result = wd.calculateAllocation();
        assertEquals(result.getTotalAllocationValue(), 4514.844, 0);
        assertEquals(result.getAllocation(bidder(0)).getTradeValue(), 0, 0.0001);
        assertEquals(result.getAllocation(bidder(1)).getTradeValue(), 894.644, 0.0001);
        assertEquals(result.getAllocation(bidder(2)).getTradeValue(), 0, 0.0001);
        assertEquals(result.getAllocation(bidder(4)).getTradeValue(), 0, 0.0001);

    }
}
