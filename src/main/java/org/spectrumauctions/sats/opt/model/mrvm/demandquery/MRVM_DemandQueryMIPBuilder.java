package org.spectrumauctions.sats.opt.model.mrvm.demandquery;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.mrvm.*;
import org.spectrumauctions.sats.opt.domain.GenericDemandQueryMIP;
import org.spectrumauctions.sats.opt.domain.GenericDemandQueryMIPBuilder;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @author Fabio Isler
 *
 */
public class MRVM_DemandQueryMIPBuilder implements GenericDemandQueryMIPBuilder {

    private static final Logger logger = LogManager.getLogger(MRVM_DemandQueryMIPBuilder.class);

    @Override
    public GenericDemandQueryMIP<MRVMGenericDefinition, MRVMLicense> getDemandQueryMipFor(Bidder bidder, Map prices, double epsilon) {
        return new MRVM_DemandQueryMIP((MRVMBidder) bidder, (Map<MRVMGenericDefinition, BigDecimal>) prices, epsilon);
    }
}
