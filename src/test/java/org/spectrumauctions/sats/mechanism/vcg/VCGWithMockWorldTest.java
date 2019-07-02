package org.spectrumauctions.sats.mechanism.vcg;

import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.spectrumauctions.sats.core.bidlang.generic.GenericBid;
import org.spectrumauctions.sats.core.bidlang.generic.GenericDefinition;
import org.spectrumauctions.sats.core.bidlang.xor.XORBid;
import org.spectrumauctions.sats.core.model.LicenseBundle;
import org.spectrumauctions.sats.mechanism.MockWorld;
import org.spectrumauctions.sats.mechanism.MockWorld.MockGood;
import org.spectrumauctions.sats.mechanism.MockWorld.MockBand;
import org.spectrumauctions.sats.mechanism.domain.Payment;
import org.spectrumauctions.sats.mechanism.domain.mechanisms.AuctionMechanism;
import org.spectrumauctions.sats.opt.domain.WinnerDeterminator;
import org.spectrumauctions.sats.opt.xor.XORWinnerDetermination;
import org.spectrumauctions.sats.opt.xorq.XORQWinnerDetermination;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class VCGWithMockWorldTest {

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
            bidders.put((int) bidder.getLongId(), bidder);
            return bidder(id);
        }
        return fromMap;
    }

    @Test
    public void testXORVCG() {
        bidder(1).addBid(new LicenseBundle<>(A), 2);
        bidder(2).addBid(new LicenseBundle<>(A, B, D), 3);
        bidder(3).addBid(new LicenseBundle<>(B, C), 2);
        bidder(4).addBid(new LicenseBundle<>(C, D), 1);

        Set<XORBid<MockGood>> xorBids = new HashSet<>();
        xorBids.add(new XORBid.Builder<>(bidder(1), bidder(1).getValues()).build());
        xorBids.add(new XORBid.Builder<>(bidder(2), bidder(2).getValues()).build());
        xorBids.add(new XORBid.Builder<>(bidder(3), bidder(3).getValues()).build());
        xorBids.add(new XORBid.Builder<>(bidder(4), bidder(4).getValues()).build());

        WinnerDeterminator<MockGood> wdp = new XORWinnerDetermination<>(xorBids);
        AuctionMechanism<MockGood> am = new VCGMechanism<>(wdp);
        Payment<MockGood> payment = am.getPayment();
        assertEquals(am.getMechanismResult().getAllocation().getTotalValue().doubleValue(), 4, 0.0001);
        assertEquals(payment.paymentOf(bidder(1)).getAmount(), 1, 0.00001);
        assertEquals(payment.paymentOf(bidder(2)).getAmount(), 0, 0.00001);
        assertEquals(payment.paymentOf(bidder(3)).getAmount(), 1, 0.00001);
        assertEquals(payment.paymentOf(bidder(4)).getAmount(), 0, 0.00001);
    }

    @Test
    public void testXORQVCG() {
        Map<MockWorld.MockBand, Integer> bid1 = new HashMap<>();
        bid1.put(B0, 1);
        bid1.put(B1, 1);
        bidder(1).addGenericBid(bid1, 2);

        Map<MockWorld.MockBand, Integer> bid2 = new HashMap<>();
        bid2.put(B0, 1);
        bid2.put(B1, 1);
        bid2.put(B2, 1);
        bidder(2).addGenericBid(bid2, 3);

        Map<MockWorld.MockBand, Integer> bid3 = new HashMap<>();
        bid3.put(B1, 1);
        bidder(3).addGenericBid(bid3, 1.5);

        Map<MockWorld.MockBand, Integer> bid4 = new HashMap<>();
        bid4.put(B0, 2);
        bid4.put(B1, 2);
        bid4.put(B2, 1);
        bidder(4).addGenericBid(bid4, 4);

        Set<GenericBid<GenericDefinition<MockGood>, MockGood>> bids = new HashSet<>();
        bids.add(new GenericBid<>(bidder(1), bidder(1).getGenericBids()));
        bids.add(new GenericBid<>(bidder(2), bidder(2).getGenericBids()));
        bids.add(new GenericBid<>(bidder(3), bidder(3).getGenericBids()));
        bids.add(new GenericBid<>(bidder(4), bidder(4).getGenericBids()));

        WinnerDeterminator<MockGood> wdp = new XORQWinnerDetermination<>(bids);
        AuctionMechanism<MockGood> am = new VCGMechanism<>(wdp);
        Payment<MockGood> payment = am.getPayment();
        assertEquals(3.5, payment.getTotalPayments(), 1e-6);
        assertEquals(1.5, payment.paymentOf(bidder(1)).getAmount(), 1e-6);
        assertEquals(2, payment.paymentOf(bidder(2)).getAmount(), 1e-6);
        assertEquals(0, payment.paymentOf(bidder(3)).getAmount(), 1e-6);
        assertEquals(0, payment.paymentOf(bidder(4)).getAmount(), 1e-6);
    }



}
