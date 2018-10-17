package org.spectrumauctions.sats.opt.model.gsvm.demandquery;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.gsvm.GSVMBidder;
import org.spectrumauctions.sats.core.model.gsvm.GSVMLicense;
import org.spectrumauctions.sats.core.model.mrvm.MRVMBidder;
import org.spectrumauctions.sats.core.model.mrvm.MRVMGenericDefinition;
import org.spectrumauctions.sats.core.model.mrvm.MRVMLicense;
import org.spectrumauctions.sats.opt.domain.GenericDemandQueryMIP;
import org.spectrumauctions.sats.opt.domain.GenericDemandQueryMIPBuilder;
import org.spectrumauctions.sats.opt.domain.NonGenericDemandQueryMIP;
import org.spectrumauctions.sats.opt.domain.NonGenericDemandQueryMIPBuilder;
import org.spectrumauctions.sats.opt.model.mrvm.demandquery.MRVM_DemandQueryMIP;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @author Fabio Isler
 *
 */
public class GSVM_DemandQueryMIPBuilder implements NonGenericDemandQueryMIPBuilder {

    private static final Logger logger = LogManager.getLogger(GSVM_DemandQueryMIPBuilder.class);

    @Override
    public NonGenericDemandQueryMIP<GSVMLicense> getDemandQueryMipFor(Bidder bidder, Map prices, double epsilon) {
        return new GSVM_DemandQueryMIP((GSVMBidder) bidder, (Map<GSVMLicense, BigDecimal>) prices, epsilon);
    }
}
