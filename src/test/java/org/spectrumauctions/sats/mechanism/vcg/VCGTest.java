package org.spectrumauctions.sats.mechanism.vcg;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.spectrumauctions.sats.core.bidlang.xor.XORBid;
import org.spectrumauctions.sats.core.model.Bundle;
import org.spectrumauctions.sats.core.model.gsvm.GSVMBidder;
import org.spectrumauctions.sats.core.model.gsvm.GSVMLicense;
import org.spectrumauctions.sats.core.model.gsvm.GlobalSynergyValueModel;
import org.spectrumauctions.sats.core.model.lsvm.LSVMBidder;
import org.spectrumauctions.sats.core.model.lsvm.LSVMLicense;
import org.spectrumauctions.sats.core.model.lsvm.LocalSynergyValueModel;
import org.spectrumauctions.sats.core.model.mrvm.MRVMBidder;
import org.spectrumauctions.sats.core.model.mrvm.MRVMLicense;
import org.spectrumauctions.sats.core.model.mrvm.MultiRegionModel;
import org.spectrumauctions.sats.core.model.srvm.SRVMBidder;
import org.spectrumauctions.sats.core.model.srvm.SRVMLicense;
import org.spectrumauctions.sats.core.model.srvm.SingleRegionModel;
import org.spectrumauctions.sats.mechanism.MockWorld;
import org.spectrumauctions.sats.mechanism.MockWorld.MockGood;
import org.spectrumauctions.sats.mechanism.domain.Payment;
import org.spectrumauctions.sats.mechanism.domain.mechanisms.AuctionMechanism;
import org.spectrumauctions.sats.opt.domain.WinnerDeterminator;
import org.spectrumauctions.sats.opt.model.gsvm.GSVMStandardMIP;
import org.spectrumauctions.sats.opt.model.lsvm.LSVMStandardMIP;
import org.spectrumauctions.sats.opt.model.mrvm.MRVM_MIP;
import org.spectrumauctions.sats.opt.model.srvm.SRVM_MIP;
import org.spectrumauctions.sats.opt.xor.XORWinnerDetermination;

import java.util.*;

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
    @Ignore // Takes a long time
    public void testVCGWithStandardMRVM() {
        List<MRVMBidder> bidders = new MultiRegionModel().createNewPopulation(234456867);
        WinnerDeterminator<MRVMLicense> wdp = new MRVM_MIP(bidders);
        AuctionMechanism<MRVMLicense> am = new VCGMechanism<>(wdp);
        Payment<MRVMLicense> payment = am.getPayment();
        assertEquals(3.42515584861134e7, am.getMechanismResult().getAllocation().getTotalValue().doubleValue(), 1e-2);
    }

    @Test
    public void testVCGWithStandardGSVM() {
        List<GSVMBidder> bidders = new GlobalSynergyValueModel().createNewPopulation(234456867);
        WinnerDeterminator<GSVMLicense> wdp = new GSVMStandardMIP(bidders);
        AuctionMechanism<GSVMLicense> am = new VCGMechanism<>(wdp);
        Payment<GSVMLicense> payment = am.getPayment();
        assertEquals(447.42366, am.getMechanismResult().getAllocation().getTotalValue().doubleValue(), 1e-2);
    }

    @Test(expected = UnsupportedOperationException.class) // Needs refactoring of the MIP creation
    public void testVCGWithStandardLSVM() {
        List<LSVMBidder> bidders = new LocalSynergyValueModel().createNewPopulation(234456867);
        WinnerDeterminator<LSVMLicense> wdp = new LSVMStandardMIP(bidders);
        AuctionMechanism<LSVMLicense> am = new VCGMechanism<>(wdp);
        Payment<LSVMLicense> payment = am.getPayment();
    }

    @Test
    public void testVCGWithStandardSRVM() {
        List<SRVMBidder> bidders = new SingleRegionModel().createNewPopulation(234456867);
        WinnerDeterminator<SRVMLicense> wdp = new SRVM_MIP(bidders);
        AuctionMechanism<SRVMLicense> am = new VCGMechanism<>(wdp);
        Payment<SRVMLicense> payment = am.getPayment();
        assertEquals(5313.114211, am.getMechanismResult().getAllocation().getTotalValue().doubleValue(), 1e-2);
    }



}
