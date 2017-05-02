package org.spectrumauctions.sats.opt.vcg.external.vcg;

import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.spectrumauctions.sats.core.bidlang.xor.XORBid;
import org.spectrumauctions.sats.core.bidlang.xor.XORValue;
import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.Bundle;
import org.spectrumauctions.sats.opt.vcg.external.MockWorld;
import org.spectrumauctions.sats.opt.vcg.external.MockWorld.MockGood;
import org.spectrumauctions.sats.opt.vcg.external.domain.Auction;
import org.spectrumauctions.sats.opt.vcg.external.domain.Bids;
import org.spectrumauctions.sats.opt.vcg.external.domain.Payment;
import org.spectrumauctions.sats.opt.vcg.external.domain.mechanisms.AuctionMechanism;

import java.util.HashMap;
import java.util.Map;

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
        XORValue<MockWorld.MockGood> bid1 = new XORValue<MockWorld.MockGood>(new Bundle<>(A), 2);
        XORValue<MockWorld.MockGood> bid2 = new XORValue<MockWorld.MockGood>(new Bundle<>(A, B, D), 3);
        XORValue<MockWorld.MockGood> bid3 = new XORValue<MockWorld.MockGood>(new Bundle<>(B, C), 2);
        XORValue<MockWorld.MockGood> bid4 = new XORValue<MockWorld.MockGood>(new Bundle<>(C, D), 1);

        Bids<MockWorld.MockGood> bids = new Bids<>();
        bids.addBid(new XORBid.Builder<MockWorld.MockGood>(bidder(1), Sets.newHashSet(bid1)).build());
        bids.addBid(new XORBid.Builder<MockWorld.MockGood>(bidder(2), Sets.newHashSet(bid2)).build());
        bids.addBid(new XORBid.Builder<MockWorld.MockGood>(bidder(3), Sets.newHashSet(bid3)).build());
        bids.addBid(new XORBid.Builder<MockWorld.MockGood>(bidder(4), Sets.newHashSet(bid4)).build());

        Auction auction = new Auction(bids, Sets.newHashSet(A, B, C, D));
        AuctionMechanism am = new XORVCGAuction<>(auction);
        Payment<MockGood> payment = am.getPayment();
        assertEquals(am.getAuctionResult().getAllocation().getTotalAllocationValue(), 4, 0.0001);
        assertEquals(payment.paymentOf(bidder(1)).getAmount(), 1, 0.00001);
        assertEquals(payment.paymentOf(bidder(2)).getAmount(), 0, 0.00001);
        assertEquals(payment.paymentOf(bidder(3)).getAmount(), 1, 0.00001);
        assertEquals(payment.paymentOf(bidder(4)).getAmount(), 0, 0.00001);
    }

}
