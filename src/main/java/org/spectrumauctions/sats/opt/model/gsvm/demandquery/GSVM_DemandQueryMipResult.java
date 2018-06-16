package org.spectrumauctions.sats.opt.model.gsvm.demandquery;

import org.spectrumauctions.sats.core.bidlang.xor.XORValue;
import org.spectrumauctions.sats.core.model.gsvm.GSVMLicense;
import org.spectrumauctions.sats.opt.domain.NonGenericDemandQueryResult;

import java.math.BigDecimal;

/**
 * @author Fabio Isler
 *
 */
public final class GSVM_DemandQueryMipResult implements NonGenericDemandQueryResult<GSVMLicense> {

    private final BigDecimal totalUtility;
    private final XORValue<GSVMLicense> resultingBundle;

    public GSVM_DemandQueryMipResult(BigDecimal totalUtility, XORValue<GSVMLicense> bundle) {
        this.totalUtility = totalUtility;
        this.resultingBundle = bundle;
    }

    @Override
    public XORValue<GSVMLicense> getResultingBundle() {
        return resultingBundle;
    }


}
