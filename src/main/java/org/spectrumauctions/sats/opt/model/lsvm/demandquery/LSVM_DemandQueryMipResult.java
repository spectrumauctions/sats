package org.spectrumauctions.sats.opt.model.lsvm.demandquery;

import org.spectrumauctions.sats.core.bidlang.xor.XORValue;
import org.spectrumauctions.sats.core.model.lsvm.LSVMLicense;
import org.spectrumauctions.sats.opt.domain.NonGenericDemandQueryResult;

import java.math.BigDecimal;

/**
 * @author Fabio Isler
 *
 */
public final class LSVM_DemandQueryMipResult implements NonGenericDemandQueryResult<LSVMLicense> {

    private final BigDecimal totalUtility;
    private final XORValue<LSVMLicense> resultingBundle;

    public LSVM_DemandQueryMipResult(BigDecimal totalUtility, XORValue<LSVMLicense> bundle) {
        this.totalUtility = totalUtility;
        this.resultingBundle = bundle;
    }

    @Override
    public XORValue<LSVMLicense> getResultingBundle() {
        return resultingBundle;
    }


}
