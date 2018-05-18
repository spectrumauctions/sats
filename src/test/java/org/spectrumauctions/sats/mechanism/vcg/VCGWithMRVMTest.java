package org.spectrumauctions.sats.mechanism.vcg;

import org.junit.Test;
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
import org.spectrumauctions.sats.mechanism.domain.Payment;
import org.spectrumauctions.sats.mechanism.domain.mechanisms.AuctionMechanism;
import org.spectrumauctions.sats.opt.domain.WinnerDeterminator;
import org.spectrumauctions.sats.opt.model.gsvm.GSVMStandardMIP;
import org.spectrumauctions.sats.opt.model.lsvm.LSVMStandardMIP;
import org.spectrumauctions.sats.opt.model.mrvm.MRVM_MIP;
import org.spectrumauctions.sats.opt.model.srvm.SRVM_MIP;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class VCGWithMRVMTest {

    @Test
    public void testVCGWithStandardMRVM() {
        List<MRVMBidder> bidders = new MultiRegionModel().createNewPopulation(234456867);
        WinnerDeterminator<MRVMLicense> wdp = new MRVM_MIP(bidders);
        AuctionMechanism<MRVMLicense> am = new VCGMechanism<>(wdp);
        Payment<MRVMLicense> payment = am.getPayment();
        double totalValue = am.getMechanismResult().getAllocation().getTotalValue().doubleValue();
        double sumOfValues = bidders.stream().mapToDouble(b -> am.getMechanismResult().getAllocation().getTradeValue(b).doubleValue()).sum();
        assertEquals(totalValue, sumOfValues, 1e-6);
        assertEquals(3.42515584861134e7, am.getMechanismResult().getAllocation().getTotalValue().doubleValue(), 1e-2);
    }
}
