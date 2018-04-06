package org.spectrumauctions.sats.mechanism.vcg;

import org.junit.Before;
import org.junit.Test;
import org.spectrumauctions.sats.core.bidlang.xor.XORBid;
import org.spectrumauctions.sats.core.model.Bundle;
import org.spectrumauctions.sats.mechanism.MockWorld;
import org.spectrumauctions.sats.mechanism.MockWorld.MockGood;
import org.spectrumauctions.sats.mechanism.domain.Payment;
import org.spectrumauctions.sats.mechanism.domain.mechanisms.AuctionMechanism;
import org.spectrumauctions.sats.opt.domain.Allocation;
import org.spectrumauctions.sats.opt.domain.WinnerDeterminator;
import org.spectrumauctions.sats.opt.xor.XORWinnerDetermination;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class VCGTest {

    private MockWorld.MockGood A;
    private MockWorld.MockGood B;
    private MockWorld.MockGood C;
    private MockWorld.MockGood D;

    private Map<Integer, MockWorld.MockBidder> bidders;

    @Before
    public void setUp() {
        A = MockWorld.getInstance().createNewGood();
        B = MockWorld.getInstance().createNewGood();
        C = MockWorld.getInstance().createNewGood();
        D = MockWorld.getInstance().createNewGood();
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
    public void testSimpleVCG() {
        bidder(1).addBid(new Bundle<>(A), 2);
        bidder(2).addBid(new Bundle<>(A, B, D), 3);
        bidder(3).addBid(new Bundle<>(B, C), 2);
        bidder(4).addBid(new Bundle<>(C, D), 1);

        Set<XORBid<MockGood>> xorBids = new HashSet<>();
        xorBids.add(new XORBid.Builder<>(bidder(1), bidder(1).getBids()).build());
        xorBids.add(new XORBid.Builder<>(bidder(2), bidder(2).getBids()).build());
        xorBids.add(new XORBid.Builder<>(bidder(3), bidder(3).getBids()).build());
        xorBids.add(new XORBid.Builder<>(bidder(4), bidder(4).getBids()).build());

        WinnerDeterminator<Allocation<MockGood>, MockGood> wdp = new XORWinnerDetermination<>(xorBids);
        AuctionMechanism<Allocation<MockGood>, MockGood> am = new VCGMechanism<>(wdp);
        Payment<MockGood> payment = am.getPayment();
        assertEquals(am.getMechanismResult().getAllocation().getTotalValue().doubleValue(), 4, 0.0001);
        assertEquals(payment.paymentOf(bidder(1)).getAmount(), 1, 0.00001);
        assertEquals(payment.paymentOf(bidder(2)).getAmount(), 0, 0.00001);
        assertEquals(payment.paymentOf(bidder(3)).getAmount(), 1, 0.00001);
        assertEquals(payment.paymentOf(bidder(4)).getAmount(), 0, 0.00001);
    }

}
