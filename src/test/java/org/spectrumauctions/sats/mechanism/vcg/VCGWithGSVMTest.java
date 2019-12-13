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
        List<GSVMBidder> bidders = new GlobalSynergyValueModel().createPopulation(234556782);
        WinnerDeterminator<GSVMLicense> wdp = new GSVMStandardMIP(bidders);
        AuctionMechanism<GSVMLicense> am = new VCGMechanism<>(wdp);
        Payment<GSVMLicense> payment = am.getPayment();
        double totalValue = am.getMechanismResult().getAllocation().getTotalValue().doubleValue();
        double sumOfValues = bidders.stream().mapToDouble(b -> am.getMechanismResult().getAllocation().getTradeValue(b).doubleValue()).sum();
        assertEquals(500.0105998484576, am.getMechanismResult().getAllocation().getTotalValue().doubleValue(), 1e-6);
        assertEquals(totalValue, sumOfValues, 1e-6);
    }
}
