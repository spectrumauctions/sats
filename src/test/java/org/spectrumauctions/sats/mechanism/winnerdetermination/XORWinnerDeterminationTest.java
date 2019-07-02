package org.spectrumauctions.sats.mechanism.winnerdetermination;

import org.junit.Before;
import org.junit.Test;
import org.spectrumauctions.sats.core.bidlang.xor.XORBid;
import org.spectrumauctions.sats.core.model.LicenseBundle;
import org.spectrumauctions.sats.mechanism.MockWorld;
import org.spectrumauctions.sats.mechanism.MockWorld.MockGood;

import org.spectrumauctions.sats.opt.domain.Allocation;
import org.spectrumauctions.sats.opt.domain.WinnerDeterminator;
import org.spectrumauctions.sats.opt.xor.XORWinnerDetermination;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class XORWinnerDeterminationTest {

    private MockWorld.MockGood A;
    private MockWorld.MockGood B;
    private MockWorld.MockGood C;
    private MockWorld.MockGood D;
    private MockWorld.MockGood E;

    private Map<Integer, MockWorld.MockBidder> bidders;

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


    private MockWorld.MockBidder bidder(int id) {
        MockWorld.MockBidder fromMap = bidders.get(id);
        if (fromMap == null) {
            MockWorld.MockBidder bidder = MockWorld.getInstance().createNewBidder();
            bidders.put((int) bidder.getLongId(), bidder);
            return bidder(id);
        }
        return fromMap;
    }

    @Test
    public void testSimpleWinnerDetermination() {
        bidder(1).addBid(new LicenseBundle<>(A), 2);
        bidder(2).addBid(new LicenseBundle<>(A, B, D), 3);
        bidder(3).addBid(new LicenseBundle<>(B, C), 2);
        bidder(4).addBid(new LicenseBundle<>(C, D), 1);

        Set<XORBid<MockGood>> bids = new HashSet<>();
        bids.add(new XORBid.Builder<>(bidder(1), bidder(1).getValues()).build());
        bids.add(new XORBid.Builder<>(bidder(2), bidder(2).getValues()).build());
        bids.add(new XORBid.Builder<>(bidder(3), bidder(3).getValues()).build());
        bids.add(new XORBid.Builder<>(bidder(4), bidder(4).getValues()).build());

        WinnerDeterminator<MockGood> wd = new XORWinnerDetermination<>(bids);
        Allocation<MockGood> result = wd.calculateAllocation();
        assertEquals(result.getTotalValue().compareTo(BigDecimal.valueOf(4)), 0);
        assertEquals(result.getTradeValue(bidder(2)).doubleValue(), 0, 0.01);
        assertEquals(result.getTradeValue(bidder(3)).doubleValue(), 2, 0.0001);
        assertEquals(result.getTradeValue(bidder(1)).doubleValue(), 2, 0.0001);
        assertEquals(result.getTradeValue(bidder(4)).doubleValue(), 0, 0.01);
        // assertTrue(result.getAllocation(bidder(2)).getAcceptedBids().isEmpty());
        // assertTrue(result.getAllocation(bidder(4)).getAcceptedBids().isEmpty());
    }

    @Test
    public void testMediumWinnerDetermination() {
        bidder(0).addBid(new LicenseBundle<>(C, D), 1795.51);
        bidder(1).addBid(new LicenseBundle<>(D), 894.644);
        bidder(2).addBid(new LicenseBundle<>(A, B), 1633.62);
        bidder(3).addBid(new LicenseBundle<>(C), 997.064);
        bidder(4).addBid(new LicenseBundle<>(B, C), 1751.26);
        bidder(5).addBid(new LicenseBundle<>(A, E), 1779.42);
        bidder(6).addBid(new LicenseBundle<>(B), 843.716);
        bidder(7).addBid(new LicenseBundle<>(E), 762.093);
        bidder(8).addBid(new LicenseBundle<>(A), 893.983);
        bidder(9).addBid(new LicenseBundle<>(A, C), 1816.69);
        Set<XORBid<MockGood>> bids = new HashSet<>();
        bids.add(new XORBid.Builder<>(bidder(0), bidder(0).getValues()).build());
        bids.add(new XORBid.Builder<>(bidder(1), bidder(1).getValues()).build());
        bids.add(new XORBid.Builder<>(bidder(2), bidder(2).getValues()).build());
        bids.add(new XORBid.Builder<>(bidder(3), bidder(3).getValues()).build());
        bids.add(new XORBid.Builder<>(bidder(4), bidder(4).getValues()).build());
        bids.add(new XORBid.Builder<>(bidder(5), bidder(5).getValues()).build());
        bids.add(new XORBid.Builder<>(bidder(6), bidder(6).getValues()).build());
        bids.add(new XORBid.Builder<>(bidder(7), bidder(7).getValues()).build());
        bids.add(new XORBid.Builder<>(bidder(8), bidder(8).getValues()).build());
        bids.add(new XORBid.Builder<>(bidder(9), bidder(9).getValues()).build());

        WinnerDeterminator<MockGood> wd = new XORWinnerDetermination<>(bids);
        Allocation<MockGood> result = wd.calculateAllocation();
        assertEquals(result.getTotalValue().doubleValue(), 4514.844, 0);
        assertEquals(result.getTradeValue(bidder(0)).doubleValue(), 0, 0.0001);
        assertEquals(result.getTradeValue(bidder(1)).doubleValue(), 894.644, 0.0001);
        assertEquals(result.getTradeValue(bidder(2)).doubleValue(), 0, 0.0001);
        assertEquals(result.getTradeValue(bidder(4)).doubleValue(), 0, 0.0001);

    }
}
