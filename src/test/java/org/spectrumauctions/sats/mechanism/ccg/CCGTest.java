package org.spectrumauctions.sats.mechanism.ccg;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.spectrumauctions.sats.core.bidlang.xor.XORBid;
import org.spectrumauctions.sats.core.model.Bundle;
import org.spectrumauctions.sats.core.model.mrvm.MRVMBidder;
import org.spectrumauctions.sats.core.model.mrvm.MRVMLicense;
import org.spectrumauctions.sats.core.model.mrvm.MultiRegionModel;
import org.spectrumauctions.sats.mechanism.MockWorld;
import org.spectrumauctions.sats.mechanism.domain.Payment;
import org.spectrumauctions.sats.mechanism.domain.mechanisms.AuctionMechanism;
import org.spectrumauctions.sats.mechanism.vcg.VCGMechanism;
import org.spectrumauctions.sats.opt.domain.WinnerDeterminator;
import org.spectrumauctions.sats.opt.model.mrvm.MRVM_MIP;
import org.spectrumauctions.sats.opt.xor.XORWinnerDetermination;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class CCGTest {

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
            bidders.put((int) bidder.getId(), bidder);
            return bidder(id);
        }
        return fromMap;
    }

    @Test
    public void testSimpleCCG() {
        bidder(1).addBid(new Bundle<>(A), 2);
        bidder(2).addBid(new Bundle<>(B), 2);
        bidder(3).addBid(new Bundle<>(A, B), 2);

        Set<XORBid<MockWorld.MockGood>> xorBids = new HashSet<>();
        xorBids.add(new XORBid.Builder<>(bidder(1), bidder(1).getBids()).build());
        xorBids.add(new XORBid.Builder<>(bidder(2), bidder(2).getBids()).build());
        xorBids.add(new XORBid.Builder<>(bidder(3), bidder(3).getBids()).build());

        WinnerDeterminator<MockWorld.MockGood> wdp = new XORWinnerDetermination<>(xorBids);
        AuctionMechanism<MockWorld.MockGood> am = new CCGMechanism<>(wdp);
        Payment<MockWorld.MockGood> payment = am.getPayment();
        assertEquals(am.getMechanismResult().getAllocation().getTotalValue().doubleValue(), 4, 0.0001);
        assertEquals(payment.paymentOf(bidder(1)).getAmount(), 1, 0.00001);
        assertEquals(payment.paymentOf(bidder(2)).getAmount(), 1, 0.00001);
        assertEquals(payment.paymentOf(bidder(3)).getAmount(), 0, 0.00001);
    }

    @Test
    public void testSimpleCCGWithTraitor() {
        bidder(1).addBid(new Bundle<>(A), 10);
        bidder(2).addBid(new Bundle<>(B), 10);
        bidder(3).addBid(new Bundle<>(C), 10);
        bidder(4).addBid(new Bundle<>(A, B), 6);

        Set<XORBid<MockWorld.MockGood>> xorBids = new HashSet<>();
        xorBids.add(new XORBid.Builder<>(bidder(1), bidder(1).getBids()).build());
        xorBids.add(new XORBid.Builder<>(bidder(2), bidder(2).getBids()).build());
        xorBids.add(new XORBid.Builder<>(bidder(3), bidder(3).getBids()).build());
        xorBids.add(new XORBid.Builder<>(bidder(4), bidder(4).getBids()).build());

        WinnerDeterminator<MockWorld.MockGood> wdp = new XORWinnerDetermination<>(xorBids);
        AuctionMechanism<MockWorld.MockGood> am = new CCGMechanism<>(wdp);
        Payment<MockWorld.MockGood> payment = am.getPayment();
        assertEquals(am.getMechanismResult().getAllocation().getTotalValue().doubleValue(), 30, 0.0001);
        assertEquals(payment.paymentOf(bidder(1)).getAmount(), 3, 0.00001);
        assertEquals(payment.paymentOf(bidder(2)).getAmount(), 3, 0.00001);
        assertEquals(payment.paymentOf(bidder(3)).getAmount(), 0, 0.00001);
        assertEquals(payment.paymentOf(bidder(4)).getAmount(), 0, 0.00001);
    }

    @Test
    public void testCCGWithTraitor() {
        bidder(1).addBid(new Bundle<>(A), 10);
        bidder(2).addBid(new Bundle<>(B), 10);
        bidder(3).addBid(new Bundle<>(C), 10);
        bidder(4).addBid(new Bundle<>(D), 10);
        bidder(5).addBid(new Bundle<>(A, B, C, D, E), 12);
        bidder(6).addBid(new Bundle<>(A, B, E), 8);
        bidder(7).addBid(new Bundle<>(C, D, E), 8);

        Set<XORBid<MockWorld.MockGood>> xorBids = new HashSet<>();
        xorBids.add(new XORBid.Builder<>(bidder(1), bidder(1).getBids()).build());
        xorBids.add(new XORBid.Builder<>(bidder(2), bidder(2).getBids()).build());
        xorBids.add(new XORBid.Builder<>(bidder(3), bidder(3).getBids()).build());
        xorBids.add(new XORBid.Builder<>(bidder(4), bidder(4).getBids()).build());
        xorBids.add(new XORBid.Builder<>(bidder(5), bidder(5).getBids()).build());
        xorBids.add(new XORBid.Builder<>(bidder(6), bidder(6).getBids()).build());
        xorBids.add(new XORBid.Builder<>(bidder(7), bidder(7).getBids()).build());

        WinnerDeterminator<MockWorld.MockGood> wdp = new XORWinnerDetermination<>(xorBids);
        AuctionMechanism<MockWorld.MockGood> am = new CCGMechanism<>(wdp);
        Payment<MockWorld.MockGood> payment = am.getPayment();
        assertEquals(am.getMechanismResult().getAllocation().getTotalValue().doubleValue(), 40, 0.0001);
        assertEquals(payment.paymentOf(bidder(1)).getAmount(), 4, 0.00001);
        assertEquals(payment.paymentOf(bidder(2)).getAmount(), 4, 0.00001);
        assertEquals(payment.paymentOf(bidder(3)).getAmount(), 4, 0.00001);
        assertEquals(payment.paymentOf(bidder(4)).getAmount(), 4, 0.00001);
        assertEquals(payment.paymentOf(bidder(5)).getAmount(), 0, 0.00001);
        assertEquals(payment.paymentOf(bidder(6)).getAmount(), 0, 0.00001);
        assertEquals(payment.paymentOf(bidder(7)).getAmount(), 0, 0.00001);
    }

    @Test
    @Ignore // Takes a long time
    public void testVCGWithStandardMRVM() {
        List<MRVMBidder> bidders = new MultiRegionModel().createNewPopulation(234456867);
        WinnerDeterminator<MRVMLicense> wdp = new MRVM_MIP(bidders);
        AuctionMechanism<MRVMLicense> am = new CCGMechanism<>(wdp);
        Payment<MRVMLicense> payment = am.getPayment();
        assertEquals(3.4251509777579516e7, am.getMechanismResult().getAllocation().getTotalValue().doubleValue(), 1e-2);
    }

}
