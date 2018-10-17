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

public class VCGWithSRVMTest {

    @Test
    public void testVCGWithStandardSRVM() {
        List<SRVMBidder> bidders = new SingleRegionModel().createNewPopulation(234456867);
        WinnerDeterminator<SRVMLicense> wdp = new SRVM_MIP(bidders);
        AuctionMechanism<SRVMLicense> am = new VCGMechanism<>(wdp);
        Payment<SRVMLicense> payment = am.getPayment();
        double totalValue = am.getMechanismResult().getAllocation().getTotalValue().doubleValue();
        double sumOfValues = bidders.stream().mapToDouble(b -> am.getMechanismResult().getAllocation().getTradeValue(b).doubleValue()).sum();
        assertEquals(1405.9873, am.getMechanismResult().getAllocation().getTotalValue().doubleValue(), 1e-6);
        assertEquals(totalValue, sumOfValues, 1e-6);
    }

}
