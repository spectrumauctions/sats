package org.spectrumauctions.sats.opt.model.lsvm.demandquery;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.lsvm.LSVMBidder;
import org.spectrumauctions.sats.core.model.lsvm.LSVMLicense;
import org.spectrumauctions.sats.opt.domain.NonGenericDemandQueryMIP;
import org.spectrumauctions.sats.opt.domain.NonGenericDemandQueryMIPBuilder;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @author Fabio Isler
 *
 */
public class LSVM_DemandQueryMIPBuilder implements NonGenericDemandQueryMIPBuilder {

    private static final Logger logger = LogManager.getLogger(LSVM_DemandQueryMIPBuilder.class);

    @Override
    public NonGenericDemandQueryMIP<LSVMLicense> getDemandQueryMipFor(Bidder bidder, Map prices, double epsilon) {
        return new LSVM_DemandQueryMIP((LSVMBidder) bidder, (Map<LSVMLicense, BigDecimal>) prices, epsilon);
    }
}
