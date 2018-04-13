package org.spectrumauctions.sats.mechanism.cca;

import org.junit.Test;
import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.mrvm.MRVMBidder;
import org.spectrumauctions.sats.core.model.mrvm.MRVMLicense;
import org.spectrumauctions.sats.core.model.mrvm.MultiRegionModel;
import org.spectrumauctions.sats.mechanism.ccg.CCGMechanism;
import org.spectrumauctions.sats.mechanism.domain.Payment;
import org.spectrumauctions.sats.mechanism.domain.mechanisms.AuctionMechanism;
import org.spectrumauctions.sats.opt.domain.WinnerDeterminator;
import org.spectrumauctions.sats.opt.model.mrvm.MRVM_MIP;
import org.spectrumauctions.sats.opt.model.mrvm.demandquery.MRVM_DemandQueryMIPBuilder;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class CCATest {

    @Test
    public void testCCGWithStandardMRVM() {
        List<Bidder<MRVMLicense>> bidders = new MultiRegionModel().createNewPopulation(234456867).stream()
                .map(b -> (Bidder<MRVMLicense>) b).collect(Collectors.toList());
        AuctionMechanism<MRVMLicense> cca = new CCAMechanism<>(bidders, new MRVM_DemandQueryMIPBuilder());
        Payment<MRVMLicense> payment = cca.getPayment();
        assertEquals(3.4251509777579516e7, cca.getMechanismResult().getAllocation().getTotalValue().doubleValue(), 1e-2);
    }

    @Test
    public void testCustomizedCCGWithStandardMRVM() {
        List<Bidder<MRVMLicense>> bidders = new MultiRegionModel().createNewPopulation(234456867).stream()
                .map(b -> (Bidder<MRVMLicense>) b).collect(Collectors.toList());
        CCAMechanism<MRVMLicense> cca = new CCAMechanism<>(bidders, new MRVM_DemandQueryMIPBuilder());
        cca.setStartingPrice(BigDecimal.valueOf(1e6));
        cca.setPriceUpdate(BigDecimal.valueOf(0.5));
        cca.setEpsilon(0.1);
        Payment<MRVMLicense> payment = cca.getPayment();
        assertEquals(3.4251509777579516e7, cca.getMechanismResult().getAllocation().getTotalValue().doubleValue(), 1e-2);
    }

}
