package org.spectrumauctions.sats.mechanism.vcg;

import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.spectrumauctions.sats.core.bidlang.xor.XORBid;
import org.spectrumauctions.sats.core.bidlang.xor.XORValue;
import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.Bundle;
import org.spectrumauctions.sats.mechanism.MockWorld;
import org.spectrumauctions.sats.mechanism.MockWorld.MockGood;
import org.spectrumauctions.sats.mechanism.domain.Payment;
import org.spectrumauctions.sats.mechanism.domain.mechanisms.AuctionMechanism;
import org.spectrumauctions.sats.opt.domain.Allocation;
import org.spectrumauctions.sats.opt.domain.WinnerDeterminator;
import org.spectrumauctions.sats.opt.xor.XORWinnerDetermination;

import java.math.BigDecimal;
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

    private Map<Integer, Bidder<MockWorld.MockGood>> bidders;

    @Before
    public void setUp() {
        A = MockWorld.getInstance().createNewGood();
        B = MockWorld.getInstance().createNewGood();
        C = MockWorld.getInstance().createNewGood();
        D = MockWorld.getInstance().createNewGood();
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

    @Test
    public void testSimpleWinnerDetermination() {
        XORValue<MockWorld.MockGood> bid1 = new XORValue<>(new Bundle<>(A), new BigDecimal(2));
        XORValue<MockWorld.MockGood> bid2 = new XORValue<>(new Bundle<>(A, B, D), new BigDecimal(3));
        XORValue<MockWorld.MockGood> bid3 = new XORValue<>(new Bundle<>(B, C), new BigDecimal(2));
        XORValue<MockWorld.MockGood> bid4 = new XORValue<>(new Bundle<>(C, D), new BigDecimal(1));

        Set<XORBid<MockGood>> xorBids = new HashSet<>();
        xorBids.add(new XORBid.Builder<>(bidder(1), Sets.newHashSet(bid1)).build());
        xorBids.add(new XORBid.Builder<>(bidder(2), Sets.newHashSet(bid2)).build());
        xorBids.add(new XORBid.Builder<>(bidder(3), Sets.newHashSet(bid3)).build());
        xorBids.add(new XORBid.Builder<>(bidder(4), Sets.newHashSet(bid4)).build());

        WinnerDeterminator<Allocation<MockGood>> wdp = new XORWinnerDetermination<>(xorBids);
        AuctionMechanism am = new VCGMechanism<>(wdp);
        Payment<MockGood> payment = am.getPayment();
        assertEquals(am.getMechanismResult().getAllocation().getTotalValue().doubleValue(), 4, 0.0001);
        assertEquals(payment.paymentOf(bidder(1)).getAmount(), 1, 0.00001);
        assertEquals(payment.paymentOf(bidder(2)).getAmount(), 0, 0.00001);
        assertEquals(payment.paymentOf(bidder(3)).getAmount(), 1, 0.00001);
        assertEquals(payment.paymentOf(bidder(4)).getAmount(), 0, 0.00001);
    }

}
