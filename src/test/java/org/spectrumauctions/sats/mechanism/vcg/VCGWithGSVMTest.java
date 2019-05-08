package org.spectrumauctions.sats.mechanism.vcg;

import org.junit.Test;
import org.spectrumauctions.sats.core.bidlang.xor.*;
import org.spectrumauctions.sats.core.model.gsvm.GSVMBidder;
import org.spectrumauctions.sats.core.model.gsvm.GSVMLicense;
import org.spectrumauctions.sats.core.model.gsvm.GlobalSynergyValueModel;
import org.spectrumauctions.sats.core.util.random.JavaUtilRNGSupplier;
import org.spectrumauctions.sats.mechanism.domain.Payment;
import org.spectrumauctions.sats.mechanism.domain.mechanisms.AuctionMechanism;
import org.spectrumauctions.sats.opt.domain.WinnerDeterminator;
import org.spectrumauctions.sats.opt.model.gsvm.GSVMStandardMIP;
import org.spectrumauctions.sats.opt.xor.XORWinnerDetermination;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class VCGWithGSVMTest {

    @Test
    public void testVCGWithStandardGSVM() {
        List<GSVMBidder> bidders = new GlobalSynergyValueModel().createNewPopulation(234556782);
        WinnerDeterminator<GSVMLicense> wdp = new GSVMStandardMIP(bidders);
        AuctionMechanism<GSVMLicense> am = new VCGMechanism<>(wdp);
        Payment<GSVMLicense> payment = am.getPayment();
        double totalValue = am.getMechanismResult().getAllocation().getTotalValue().doubleValue();
        double sumOfValues = bidders.stream().mapToDouble(b -> am.getMechanismResult().getAllocation().getTradeValue(b).doubleValue()).sum();
        assertEquals(500.0105998484576, am.getMechanismResult().getAllocation().getTotalValue().doubleValue(), 1e-6);
        assertEquals(totalValue, sumOfValues, 1e-6);
    }

    @Test
    public void testVCGWithXORBidsFromGSVM() {
        long seed = 5000L;

        while (true) {
            List<GSVMBidder> bidders = new GlobalSynergyValueModel().createNewPopulation(seed++);
            System.out.println("Using seed " + seed);
            Collection<XORBid<GSVMLicense>> bids = new HashSet<>();
            for (GSVMBidder bidder : bidders) {
                SizeBasedUniqueRandomXOR<GSVMLicense> lang = new SizeBasedUniqueRandomXOR<>(bidder.getWorld().getLicenses(), new JavaUtilRNGSupplier(seed), bidder);
                lang.setDistribution(4, 1.);
                lang.setIterations(250);
                Iterator<XORValue<GSVMLicense>> iterator = lang.iterator();
                XORBid.Builder<GSVMLicense> builder = new XORBid.Builder<>(bidder);
                while (iterator.hasNext()) {
                    builder.add(iterator.next());
                }
                bids.add(builder.build());
            }
            XORWinnerDetermination<GSVMLicense> wdp = new XORWinnerDetermination<>(bids);
            AuctionMechanism<GSVMLicense> am = new VCGMechanism<>(wdp);
            Payment<GSVMLicense> payment = am.getPayment();
            System.out.println("Done");
        }

    }
}
