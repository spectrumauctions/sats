package org.spectrumauctions.sats.mechanism.cca;

import org.junit.Test;
import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.mrvm.MRVMLicense;
import org.spectrumauctions.sats.core.model.mrvm.MultiRegionModel;
import org.spectrumauctions.sats.mechanism.domain.MechanismResult;
import org.spectrumauctions.sats.opt.model.mrvm.demandquery.MRVM_DemandQueryMIPBuilder;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

public class CCATest {

    @Test
    public void testCustomizedCCAWithStandardMRVM() {
        List<Bidder<MRVMLicense>> bidders = new MultiRegionModel().createNewPopulation(234456867).stream()
                .map(b -> (Bidder<MRVMLicense>) b).collect(Collectors.toList());
        CCAMechanism<MRVMLicense> cca = new CCAMechanism<>(bidders, new MRVM_DemandQueryMIPBuilder());
        cca.setVariant(CCAVariant.XORQ);
        cca.setStartingPrice(BigDecimal.ZERO);
        cca.setPriceUpdate(BigDecimal.valueOf(0.1));
        cca.setEpsilon(0.1);
        MechanismResult<MRVMLicense> result = cca.getMechanismResult();
        assertTrue(result.getAllocation().getTotalValue().doubleValue() > 0);
    }



}
