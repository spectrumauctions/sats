package org.spectrumauctions.sats.mechanism.winnerdetermination;

import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.spectrumauctions.sats.core.bidlang.generic.GenericBid;
import org.spectrumauctions.sats.core.bidlang.generic.GenericDefinition;
import org.spectrumauctions.sats.mechanism.MockWorld;
import org.spectrumauctions.sats.mechanism.MockWorld.MockBand;
import org.spectrumauctions.sats.mechanism.MockWorld.MockGood;
import org.spectrumauctions.sats.opt.domain.Allocation;
import org.spectrumauctions.sats.opt.domain.WinnerDeterminator;
import org.spectrumauctions.sats.opt.xorq.XORQWinnerDetermination;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class XORQWinnerDeterminationTest {

    private MockGood A;
    private MockGood B;
    private MockGood C;
    private MockGood D;
    private MockGood E;

    private MockBand B0;
    private MockBand B1;
    private MockBand B2;

    private Map<Integer, MockWorld.MockBidder> bidders;

    @Before
    public void setUp() {
        A = MockWorld.getInstance().createNewGood();
        B = MockWorld.getInstance().createNewGood();
        B0 = MockWorld.getInstance().createNewBand(Sets.newHashSet(A, B));
        C = MockWorld.getInstance().createNewGood();
        D = MockWorld.getInstance().createNewGood();
        B1 = MockWorld.getInstance().createNewBand(Sets.newHashSet(C, D));
        E = MockWorld.getInstance().createNewGood();
        B2 = MockWorld.getInstance().createNewBand(Sets.newHashSet(E));
        bidders = new HashMap<>();
        MockWorld.getInstance().reset();
    }


    private MockWorld.MockBidder bidder(int id) {
        MockWorld.MockBidder fromMap = bidders.get(id);
        if (fromMap == null) {
            MockWorld.MockBidder bidder = MockWorld.getInstance().createNewBidder();
            bidders.put((int) bidder.getId(), bidder);
            return bidder(id);
        }
        return fromMap;
    }

    @Test
    public void testSimpleWinnerDetermination() {
        Map<MockBand, Integer> bid1 = new HashMap<>();
        bid1.put(B0, 1);
        bidder(1).addGenericBid(bid1, 1);

        Map<MockBand, Integer> bid2 = new HashMap<>();
        bid2.put(B0, 1);
        bid2.put(B1, 1);
        bid2.put(B2, 1);
        bidder(2).addGenericBid(bid2, 3);

        Map<MockBand, Integer> bid3 = new HashMap<>();
        bid3.put(B1, 1);
        bidder(3).addGenericBid(bid3, 1);

        Map<MockBand, Integer> bid4 = new HashMap<>();
        bid4.put(B0, 2);
        bid4.put(B1, 2);
        bid4.put(B2, 1);
        bidder(4).addGenericBid(bid4, 4);

        Set<GenericBid<GenericDefinition<MockGood>, MockGood>> bids = new HashSet<>();
        bids.add(new GenericBid<>(bidder(1), bidder(1).getGenericBids()));
        bids.add(new GenericBid<>(bidder(2), bidder(2).getGenericBids()));
        bids.add(new GenericBid<>(bidder(3), bidder(3).getGenericBids()));
        bids.add(new GenericBid<>(bidder(4), bidder(4).getGenericBids()));

        WinnerDeterminator<MockGood> wd = new XORQWinnerDetermination<>(bids);
        Allocation<MockGood> result = wd.calculateAllocation();
        assertEquals(5, result.getTotalValue().doubleValue(), 1e-6);
        assertEquals(1, result.getTradeValue(bidder(1)).doubleValue(), 1e-6);
        assertEquals(3, result.getTradeValue(bidder(2)).doubleValue(), 1e-6);
        assertEquals(1, result.getTradeValue(bidder(3)).doubleValue(), 1e-6);
        assertEquals(0, result.getTradeValue(bidder(4)).doubleValue(), 1e-6);
    }
}
