package org.spectrumauctions.sats.opt.model.mrvm.demandquery;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import edu.harvard.econcs.jopt.solver.IMIPResult;
import edu.harvard.econcs.jopt.solver.SolveParam;
import edu.harvard.econcs.jopt.solver.client.SolverClient;
import edu.harvard.econcs.jopt.solver.mip.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spectrumauctions.sats.core.bidlang.generic.GenericDefinition;
import org.spectrumauctions.sats.core.bidlang.generic.GenericValue;
import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.mrvm.*;
import org.spectrumauctions.sats.core.model.mrvm.MRVMRegionsMap.Region;
import org.spectrumauctions.sats.opt.domain.DemandQueryMIP;
import org.spectrumauctions.sats.opt.domain.DemandQueryMIPBuilder;
import org.spectrumauctions.sats.opt.model.ModelMIP;
import org.spectrumauctions.sats.opt.model.mrvm.MRVM_MIP;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @author Fabio Isler
 *
 */
public class MRVM_DemandQueryMIPBuilder implements DemandQueryMIPBuilder {

    private static final Logger logger = LogManager.getLogger(MRVM_DemandQueryMIPBuilder.class);

    @Override
    public DemandQueryMIP<MRVMGenericDefinition, MRVMLicense> getDemandQueryMipFor(Bidder bidder, Map prices, double epsilon) {
        return new MRVM_DemandQueryMIP((MRVMBidder) bidder, (Map<MRVMGenericDefinition, BigDecimal>) prices, epsilon);
    }
}
